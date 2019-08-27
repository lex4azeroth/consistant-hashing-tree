package lex.common.dynamic.routing.tree;

import java.util.List;
import java.util.SortedMap;

public class TreeNode {
    String key = null;
    String value = null;
    List<TreeNode> children = null;
    SortedMap<Long, String> hashRing = null;

    public TreeNode(String string) {
        this.key = string;
    }
}
