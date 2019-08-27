package lex.common.consistent.hashing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface HashingAlgorithm {

    Long hash(Object key);

    /**
     * MD5 hashing algorithm
     *
     * @param key
     * @return
     */
    default long md5HashingAlgorithm(String key) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(key.getBytes());
            byte[] bKey = md5.digest();
            long res = ((long) (bKey[3] & 0xFF) << 24) | ((long) (bKey[2] & 0xFF) << 16)
                    | ((long) (bKey[1] & 0xFF) << 8) | (long) (bKey[0] & 0xFF);
            return res;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return 0l;
    }

    /**
     * FNV1 hashing algorithm
     *
     * @param key
     * @return
     */
    default long fnv1HashingAlgorithm(String key) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < key.length(); i++)
            hash = (hash ^ key.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        return hash;
    }

}
