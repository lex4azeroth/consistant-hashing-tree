package lex.common.dynamic.routing.tree;

import java.util.List;

import lex.common.consistent.hashing.HashingAlgorithm;
import lex.common.constant.TreeConstant;

public class DynamicHashingTree implements ChangableLeaf {
    private TreeNode root = new TreeNode(TreeConstant.ROOT_KEY);
    private HashingAlgorithm algorithm;
    private String pathSepreator;

    public DynamicHashingTree(HashingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.pathSepreator = TreeConstant.DEFAULT_PATH_SEPARATOR;
    }
    
    public DynamicHashingTree(HashingAlgorithm algorithm, String pathSepreator) {
        this.algorithm = algorithm;
        this.pathSepreator = pathSepreator;
    }

    @Override
    public void addLeaf(String pathKey) {
        List<String> keys = TreeKeeper.splitKey(pathKey, pathSepreator);
        TreeKeeper.addPath(algorithm, root, keys);

    }

    @Override
    public void removeLeaf(String key) {
        // TODO Auto-generated method stub

    }

    @Override
    public String findLeaf(String key) {
        StringBuilder pathBuilder = new StringBuilder();
        TreeKeeper.findLeaf(root, key, pathBuilder, algorithm);
        return pathBuilder.toString();
    }
}
