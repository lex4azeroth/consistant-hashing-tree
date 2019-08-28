package lex.common.dynamic.routing.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import lex.common.consistent.hashing.ConsistentHash;
import lex.common.consistent.hashing.HashingAlgorithm;
import lex.common.constant.TreeConstant;

public final class TreeKeeper {

    public static List<String> splitKey(String key) {
        return splitKey(key, TreeConstant.DEFAULT_PATH_SEPARATOR);
    }

    public static List<String> splitKey(String key, String sepreator) {
        String[] levels = key.split(sepreator);
        return Arrays.asList(levels);
    }

    private static void makeChildrenIfNecessary(TreeNode treeNode) {
        if (treeNode.children == null) {
            treeNode.children = new ArrayList<>();
        }
    }

    private static void makeRingIfNecessary(TreeNode treeNode) {
        if (treeNode.hashRing == null) {
            treeNode.hashRing = Collections.synchronizedSortedMap(new TreeMap<>());
        }
    }

    /**
     * Fills the node with its key, children and hash ring.
     * 
     * @param treeNode
     * @param currentKey
     * @param numberOfReplications
     * @param hashingAlgorithm, default md5
     * @return {@code}TreeNode
     */
    private static TreeNode fillTheNode(TreeNode treeNode, String currentKey, int numberOfReplications,
            HashingAlgorithm hashingAlgorithm) {
        makeChildrenIfNecessary(treeNode);
        makeRingIfNecessary(treeNode);
        TreeNode node = new TreeNode(currentKey);
        node.parent = treeNode;
        treeNode.children.add(node);
        ConsistentHash.addNodeIntoHashRing(currentKey, treeNode.hashRing, numberOfReplications, hashingAlgorithm);
        return node;
    }

    /**
     * Finds an existed child node by its key via traversal accessing the tree from
     * root.
     * 
     * @param treeNode
     * @param currentKey,
     *            value of node key
     * @return, the found child, or null if not found
     */
    private static TreeNode findExistedChild(TreeNode treeNode, String currentKey) {
        if (treeNode.children != null) {
            int size = treeNode.children.size();
            for (int index = 0; index < size; index++) {
                TreeNode existedNode = treeNode.children.get(index);
                if (existedNode.key.equals(currentKey)) {
                    return existedNode;
                }
            }
        }

        return null;
    }

    /**
     * Adds the full path of certain leaf via traversal accessing the tree from
     * root.
     * 
     * @param hashingAlgorithm,
     *            default md5
     * @param treeNode
     * @param keys,
     *            separated keys of leaf path
     * @param numberOfReplications
     */
    static void addPath(HashingAlgorithm hashingAlgorithm, TreeNode treeNode, List<String> keys,
            int numberOfReplications) {
        String currentKey = keys.get(0);
        int size = keys.size();
        TreeNode existedNode = findExistedChild(treeNode, currentKey);
        if (existedNode != null) {
            addPath(hashingAlgorithm, existedNode, keys.subList(1, size), numberOfReplications);
        } else {
            TreeNode node = fillTheNode(treeNode, currentKey, numberOfReplications, hashingAlgorithm);

            if (size == 1) { // leaf it is
                return;
            } else {
                addPath(hashingAlgorithm, node, keys.subList(1, size), numberOfReplications);
            }
        }
    }

    /**
     * Finds the full path of certain leaf via traversal accessing the tree from
     * root.
     * 
     * @param treeNode
     * @param anyValue,
     *            will be hashed and hit a node through out the path
     * @param fullPathBuilder,
     *            to build the full path as result
     * @param hashingAlgorithm,
     *            default md5
     */
    static void findLeaf(TreeNode treeNode, String anyValue, StringBuilder fullPathBuilder,
            HashingAlgorithm hashingAlgorithm) {

        if (TreeConstant.ROOT_KEY.equals(treeNode.key)) {
            if (treeNode.children == null || treeNode.hashRing == null) {
                // At least log a warning later.
                return;
            }
        }

        String nodeKey = ConsistentHash.getHitHashNode(treeNode.hashRing, anyValue, hashingAlgorithm);

        List<TreeNode> children = treeNode.children;

        int size = children.size();
        for (int index = 0; index < size; index++) {
            TreeNode node = children.get(index);
            if (node.key.equals(nodeKey)) {
                fullPathBuilder.append(node.key);
                fullPathBuilder.append(TreeConstant.DEFAULT_PATH_SEPARATOR);

                if (node.children != null) {
                    findLeaf(node, anyValue, fullPathBuilder, hashingAlgorithm);
                }
            }
        }
    }

    /**
     * Removes the full path of certain leaf via traversal accessing the tree from
     * root.
     * 
     * @param treeNode
     * @param keys,
     *            separated keys of leaf path
     * @param hashingAlgorithm,
     *            default md5
     * @param numberOfReplications
     */
    static void removeLeaf(TreeNode treeNode, List<String> keys, HashingAlgorithm hashingAlgorithm,
            int numberOfReplications) {
        String currentKey = keys.get(0);
        int size = keys.size();
        TreeNode existedNode = findExistedChild(treeNode, currentKey);
        if (existedNode != null) {
            if (size == 1) {
                // leaf it is
                eraseNode(existedNode.key, existedNode, numberOfReplications, hashingAlgorithm);

            } else {
                removeLeaf(existedNode, keys.subList(1, size), hashingAlgorithm, numberOfReplications);
            }
        } else {
            return;
        }

        if (treeNode.children.size() == 0) {
            eraseNode(treeNode.key, treeNode, numberOfReplications, hashingAlgorithm);
        }
    }

    /**
     * Erases node from its parent and removes related virtual nodes as well.
     * 
     * @param key,
     *            string value of current node
     * @param node,
     *            {@code}TreeNode current node
     * @param numberOfReplications
     * @param hashingAlgorithm,
     *            default md5
     */
    private static void eraseNode(String key, TreeNode node, int numberOfReplications,
            HashingAlgorithm hashingAlgorithm) {
        node.parent.children.remove(node);
        ConsistentHash.remove(node.key, node.parent.hashRing, numberOfReplications, hashingAlgorithm);
    }
    
    
    /**
     * overrides this method when concrete class needs to verify certain leaf by path.
     * 
     * @param keys,
     *            separated keys of leaf path
     * @param node, {@code}TreeNode
     * 
     * @return true if the path is found, false on the opposite.
     */
    static boolean verifyLeaf(List<String> keys, TreeNode node) {
        String currentKey = keys.get(0);
        int size = keys.size();
        TreeNode existedNode = findExistedChild(node, currentKey);

        if (existedNode != null) {
            if (size == 1) {
                return true;
            }
            
            return verifyLeaf(keys.subList(1, size), existedNode);
        } else {
            return false;
        }
    }
}