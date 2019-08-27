package lex.common.dynamic.routing.datasource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

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
            theTree = new DynamicHashingTree(initMd5Hashing());
        } else {
            theTree = new DynamicHashingTree(builder.hashingAlgorithm);
        }

        setTargetDataSources(buildDataSourceMap(builder.dataSourceMap));
        afterPropertiesSet();
    }

    /**
     * Default hashing algorithm.
     * 
     * @return MD5 Hash
     */
    private HashingAlgorithm initMd5Hashing() {
        return new HashingAlgorithm() {

            @Override
            public Long hash(Object key) {
                return md5HashingAlgorithm(key.toString());
            }
        };
    }

    private Map<Object, Object> buildDataSourceMap(Map<Object, List<Object>> dataSourceMap) {
        Map<Object, Object> dynamicDataSourceMap = new HashMap<>();
        Iterator<Map.Entry<Object, List<Object>>> iterator = dataSourceMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Object, List<Object>> entry = iterator.next();
            String key = (String) entry.getKey();
            theTree.addLeaf(key);

            List<Object> values = (List<Object>) entry.getValue();
            int size = values.size();
            for (int index = 0; index < size; index++) {
                String dataSourceKey = buildKey(key, index == 0);

                if (dataSourceKey == null) {
                    // throw exception? should not happen...
                }
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

    String buildKey(String keyPath, boolean usePrime) {
        return usePrime ? theTree.findLeaf(keyPath).concat(DataSourceConstant.MASTER_SUFFIX)
                : theTree.findLeaf(keyPath).concat(DataSourceConstant.SLAVE_SUFFIX);
    }

    public static class Builder {
        private Map<Object, List<Object>> dataSourceMap = new HashMap<>();
        private HashingAlgorithm hashingAlgorithm = null;

        public Builder addDataSource(String routing, List<Object> dataSources) {
            dataSourceMap.put(routing, dataSources);
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
