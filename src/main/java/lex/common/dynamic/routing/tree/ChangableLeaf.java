package lex.common.dynamic.routing.tree;

public interface ChangableLeaf {
    /**
     * Adds the full path of certain leaf.
     * 
     * @param path, a full path.
     */
    void addLeaf(String path);

    /**
     * Removes the full path of certain leaf.
     * 
     * @param path, the full path.
     */
    void removeLeaf(String path);

    /**
     * Find the full path of certain leaf by input key.
     * @param key
     * @return String, full path of the leaf which is hit by key.
     */
    String findLeaf(String key);

    /**
     * Optional, overrides this method when concrete class needs to implement this
     * function.
     */
    default void rebuild() {
    };
}
