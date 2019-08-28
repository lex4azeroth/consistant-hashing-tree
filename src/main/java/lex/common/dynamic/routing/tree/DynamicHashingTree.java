package lex.common.dynamic.routing.tree;

import java.util.List;

import lex.common.consistent.hashing.HashingAlgorithm;
import lex.common.constant.TreeConstant;

public class DynamicHashingTree implements ChangableLeaf {
    private final TreeNode root = new TreeNode(TreeConstant.ROOT_KEY);
    private final HashingAlgorithm algorithm;
    private final String pathSeparator;
    private int numberOfReplications = 13;

    public DynamicHashingTree(HashingAlgorithm algorithm) {
        this.algorithm = algorithm;
        this.pathSeparator = TreeConstant.DEFAULT_PATH_SEPARATOR;
    }
    
    public DynamicHashingTree(HashingAlgorithm algorithm, int numberOfReplications) {
        this.algorithm = algorithm;
        this.pathSeparator = TreeConstant.DEFAULT_PATH_SEPARATOR;
        this.numberOfReplications = numberOfReplications;
    }

    public DynamicHashingTree(HashingAlgorithm algorithm, String pathSepreator) {
        this.algorithm = algorithm;
        this.pathSeparator = pathSepreator;
    }
    
    public DynamicHashingTree(HashingAlgorithm algorithm, String pathSeparator, int numberOfReplications) {
        this.algorithm = algorithm;
        this.pathSeparator = pathSeparator;
        this.numberOfReplications = numberOfReplications;
    }
    
    @Override
    public void addLeaf(String pathKey) {
        List<String> keys = TreeKeeper.splitKey(pathKey, pathSeparator);
        TreeKeeper.addPath(algorithm, root, keys, numberOfReplications);
    }

    @Override
    public void removeLeaf(String pathKey) {
        List<String> keys = TreeKeeper.splitKey(pathKey, pathSeparator);
        TreeKeeper.removeLeaf(root, keys, algorithm, numberOfReplications);
    }

    @Override
    public String findLeaf(String value) {
        StringBuilder pathBuilder = new StringBuilder();
        TreeKeeper.findLeaf(root, value, pathBuilder, algorithm);
        return pathBuilder.toString();
    }
}