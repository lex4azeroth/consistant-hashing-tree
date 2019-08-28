package lex.common.dynamic.routing.key;

public interface KeyHolder {

    /**
     * Gets key
     * @return
     */
    String getKey();

    /**
     * Sets key
     * @param key
     * @param usePrime, prime data source or not
     */
    void setKey(String key, boolean usePrime);

    /**
     * Removes key
     * @param key
     */
    void removeKey(String key);
}
