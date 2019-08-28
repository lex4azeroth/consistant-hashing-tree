package lex.common.consistent.hashing;

import java.util.SortedMap;

import lex.common.constant.HashConstant;

public class ConsistentHash {

    /**
     * Add new node key into hash ring.
     * 
     * @param nodeValue
     * @param ring
     * @param numberOfReplicas
     * @param hashingAlgorithm
     */
    public static void addNodeIntoHashRing(String nodeValue, SortedMap<Long, String> ring, int numberOfReplicas,
            HashingAlgorithm hashingAlgorithm) {
        for (int i = 0; i < numberOfReplicas; i++) {
            ring.put(hashingAlgorithm.hash(nodeValue.toString() + HashConstant.VIRTUAL_NODE_LINKER + i), nodeValue);
        }
    }

    /**
     * Gets hit hash node by input key
     * 
     * @param hashRing
     * @param key
     * @param hashingAlgorithm
     * @return
     */
    public static String getHitHashNode(SortedMap<Long, String> hashRing, String key,
            HashingAlgorithm hashingAlgorithm) {
        synchronized (hashRing) {
            if (hashRing.isEmpty()) {
                return null;
            }

            long hash = hashingAlgorithm.hash(key);
            if (!hashRing.containsKey(hash)) {
                SortedMap<Long, String> tailMap = hashRing.tailMap(hash);
                hash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
            }

            return hashRing.get(hash);
        }
    }

    public static void remove(String nodeValue, SortedMap<Long, String> hashRing, int numberOfReplicas,
            HashingAlgorithm hashingAlgorithm) {
        synchronized (hashRing) {
            for (int i = 0; i < numberOfReplicas; i++) {
                hashRing.remove(hashingAlgorithm.hash(nodeValue.toString() + HashConstant.VIRTUAL_NODE_LINKER + i));
            }
        }
    }
    
    /**
     * Default hashing algorithm.
     * 
     * @return MD5 Hash
     */
    public static HashingAlgorithm initMd5Hashing() {
        return new HashingAlgorithm() {

            @Override
            public Long hash(Object key) {
                return md5HashingAlgorithm(key.toString());
            }
        };
    }
}
