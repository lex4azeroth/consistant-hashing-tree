package lex.common.dynamic.routing.datasource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import lex.common.consistent.hashing.ConsistentHash;
import lex.common.consistent.hashing.HashingAlgorithm;
import lex.common.constant.DataSourceConstant;
import lex.common.constant.TreeConstant;
import lex.common.dynamic.routing.key.KeyHolder;
import lex.common.dynamic.routing.tree.DynamicHashingTree;

public final class DynamicRoutingDataSource extends AbstractRoutingDataSource implements KeyHolder {
    private final DynamicHashingTree theTree;
    private final ThreadLocal<String> KEY_HOLDER = new ThreadLocal<String>() {
        public String initialValue() {
            return TreeConstant.UNDEFINED_KEY;
        }
    };

    private DynamicRoutingDataSource(Builder builder) {
        if (null == builder.hashingAlgorithm) {
            theTree = new DynamicHashingTree(ConsistentHash.initMd5Hashing(), 67);
        } else {
            theTree = new DynamicHashingTree(builder.hashingAlgorithm, 67);
        }

        activeTargetDataSources(buildDataSourceMap(builder.dataSourceMap));
    }

    private synchronized void activeTargetDataSources(Map<Object, Object> targetDataSources) {
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }

    /**
     * Maps source map (Map<Object, List<Object>>) to target map
     * 
     * @param dataSourceMap,
     *            The key of this map is used to identify a list of data source. The
     *            value is a list of data source to bind. This data structure is
     *            designed to support the scenario in which case one key is bound
     *            with prime/slave(s) data sources. By design, the first item of the
     *            list is known as prime data source and the rest is(are) slave(s).
     * 
     *            For example: key: "D1-T1-S1", value: [DataSourcePrime,
     *            DataSourceSlave, ...]
     * @return
     */
    private Map<Object, Object> buildDataSourceMap(Map<Object, List<Object>> dataSourceMap) {
        Map<Object, Object> dynamicDataSourceMap = new HashMap<>();
        Iterator<Map.Entry<Object, List<Object>>> iterator = dataSourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, List<Object>> entry = iterator.next();
            String key = (String) entry.getKey();
            synchronized (this) {
                theTree.addLeaf(key);
            }

            List<Object> values = (List<Object>) entry.getValue();
            int size = values.size();
            for (int index = 0; index < size; index++) {
                // String dataSourceKey = buildKey(key, index == 0);
                String dataSourceKey = (index == 0) ? key.concat(DataSourceConstant.MASTER_SUFFIX)
                        : key.concat(DataSourceConstant.SLAVE_SUFFIX);

                if (dataSourceKey == null) {
                    // TODO: throw exception? should not happen...
                }

                System.out.println(dataSourceKey);
                dynamicDataSourceMap.put(dataSourceKey, values.get(index));
            }
        }
        return dynamicDataSourceMap;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return getKey();
    }

    @Override
    public String getKey() {
        return KEY_HOLDER.get();
    }

    @Override
    public void setKey(String key, boolean usePrime) {
        KEY_HOLDER.set(buildKey(key, usePrime));
    }

    @Override
    public void removeKey(String key) {
        KEY_HOLDER.remove();
    }

    /**
     * Dynamically build the key for prime/slave data source.
     * 
     * @param keyPath,
     *            the key retrieved from hash tree
     * @param usePrime,
     *            prime or not
     * @return
     */
    synchronized String buildKey(String keyPath, boolean usePrime) {
        return usePrime ? theTree.findLeaf(keyPath).concat(DataSourceConstant.MASTER)
                : theTree.findLeaf(keyPath).concat(DataSourceConstant.SLAVE);
    }

    /**
     * Appends data source,
     * 
     * @param rout
     * @param dataSources
     */
    public void appendDataSource(String rout, List<Object> dataSources) {
        Map<Object, List<Object>> dataSourceMap = new HashMap<>();
        dataSourceMap.put(rout, dataSources);
        activeTargetDataSources(buildDataSourceMap(dataSourceMap));
    }

    /**
     * Tries to erase the rout key out of hash tree, BUT keep the data source in
     * target data sources. In this case, related data source won't be hit by inputs
     * and no need to reload the target data sources.
     * 
     * BE CAREFUL!! The method will return False if the rout to be erased is
     * concurrently in use. For the invoker, make sure handle the False and retry in
     * appropriated times.
     * 
     * @param rout
     */
    public synchronized boolean eraseDataSourceKey(String rout) {
        if (KEY_HOLDER.get().startsWith(rout)) {
            // The key is in use.
            return false;
        } else {
            removeKey(rout);
            theTree.removeLeaf(rout);
            return true;
        }
    }

    public static class Builder {
        private Map<Object, List<Object>> dataSourceMap = new HashMap<>();
        private HashingAlgorithm hashingAlgorithm = null;

        public Builder addDataSource(String rout, List<Object> dataSources) {
            dataSourceMap.put(rout, dataSources);
            return this;
        }

        public Builder addHashingAlgorithm(HashingAlgorithm hashingAlgorithm) {
            this.hashingAlgorithm = hashingAlgorithm;
            return this;
        }

        public DynamicRoutingDataSource build() {
            return new DynamicRoutingDataSource(this);
        }
    }
}