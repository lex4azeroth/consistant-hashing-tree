package lex.common.consistent.hashing;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Consistent Hashing
 *
 * @param <T>
 *            Type of Node
 */
@Deprecated
public class ConsistentHashing<T> {

    /**
     * Number of node replicas
     */
    private final int numberOfReplicas;

    /**
     * Consistent Hashing Rings
     */
    private final SortedMap<Long, T> circle = Collections.synchronizedSortedMap(new TreeMap<>());

    HashingAlgorithm hashingAlgorithm;

    /**
     * Constructor, default md5 hashing algorithm
     *
     * @param numberOfReplicas
     * @param nodes
     */
    public ConsistentHashing(int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashingAlgorithm = new HashingAlgorithm() {

            @Override
            public Long hash(Object key) {
                return md5HashingAlgorithm(key.toString());
            }
        };

        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * Constructor, user defined hashing algorithm
     *
     * @param hashingAlgorithm
     * @param numberOfReplicas
     * @param nodes
     */
    public ConsistentHashing(HashingAlgorithm hashingAlgorithm, int numberOfReplicas, Collection<T> nodes) {
        this.numberOfReplicas = numberOfReplicas;
        this.hashingAlgorithm = hashingAlgorithm;

        for (T node : nodes) {
            add(node);
        }
    }

    /**
     * Add node into hash ring, including its virtual nodes.
     *
     * @param node
     */
    public void add(T node) {
        synchronized (circle) {
            for (int i = 0; i < numberOfReplicas; i++) {
                circle.put(hashingAlgorithm.hash(node.toString() + "#" + i), node);
            }
        }
    }

    /**
     * Remove node from hash ring, including its virtual nodes.
     *
     * @param node
     */
    public void remove(T node) {
        synchronized (circle) {
            for (int i = 0; i < numberOfReplicas; i++) {
                circle.remove(hashingAlgorithm.hash(node.toString() + "#" + i));
            }
        }
    }

    /**
     * Get the node
     *
     * @param key
     * @return node
     */
    public T get(Object key) {
        synchronized (circle) {
            if (circle.isEmpty()) {
                return null;
            }

            long hash = hashingAlgorithm.hash(key);
            if (!circle.containsKey(hash)) {
                SortedMap<Long, T> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);
        }
    }
}
