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

    private final static int replicas = 13;

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

    private static TreeNode fillTheNode(TreeNode treeNode, String currentKey, int numberOfReplications,
            HashingAlgorithm hashingAlgorithm) {
        makeChildrenIfNecessary(treeNode);
        makeRingIfNecessary(treeNode);
        TreeNode node = new TreeNode(currentKey);
        treeNode.children.add(node);
        ConsistentHash.addNodeIntoHashRing(currentKey, treeNode.hashRing, replicas, hashingAlgorithm);
        return node;
    }

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
     * Add the full path of certain leaf.
     * 
     * @param hashingAlgorithm
     * @param treeNode
     * @param keys
     */
    static void addPath(HashingAlgorithm hashingAlgorithm, TreeNode treeNode, List<String> keys) {
        String currentKey = keys.get(0);
        int size = keys.size();
        TreeNode existedNode = findExistedChild(treeNode, currentKey);
        if (existedNode != null) {
            addPath(hashingAlgorithm, existedNode, keys.subList(1, size));
        } else {
            TreeNode node = fillTheNode(treeNode, currentKey, replicas, hashingAlgorithm);

            if (size == 1) { // leaf it is
                return;
            } else {
                addPath(hashingAlgorithm, node, keys.subList(1, size));
            }
        }
    }

    /**
     * Find the leaf from the tree root
     * 
     * @param root
     * @param anyValue,
     *            will be hashed and hit a node through out the path
     * @param fullPathBuilder,
     *            to build the full path as result
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
     * Remove the full path of certain leaf.
     * 
     * @param treeNode
     * @param keys
     * @param fullPathBuilder
     * @param hashingAlgorithm
     */
    static void removeLeaf(TreeNode treeNode, List<String> keys, HashingAlgorithm hashingAlgorithm) {
        String currentKey = keys.get(0);
        int size = keys.size();
        TreeNode existedNode = findExistedChild(treeNode, currentKey);
        if (existedNode != null) {
            if (size == 1) {
                // leaf it is
                treeNode.children.remove(existedNode);
                ConsistentHash.remove(currentKey, treeNode.hashRing, replicas, hashingAlgorithm);
            } else {
                removeLeaf(treeNode, keys.subList(1, size), hashingAlgorithm);
            }
        } else {
            return;
        }
    }
}
