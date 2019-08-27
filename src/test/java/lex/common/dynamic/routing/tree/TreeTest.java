package lex.common.dynamic.routing.tree;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lex.common.consistent.hashing.HashingAlgorithm;
import lex.common.dynamic.routing.tree.TreeKeeper;
import lex.common.dynamic.routing.tree.TreeNode;

public class TreeTest {
	private HashingAlgorithm algorithm = new HashingAlgorithm() {

		@Override
		public Long hash(Object key) {
			return md5HashingAlgorithm(key.toString());
		}
	};

	TreeNode root = new TreeNode("ROOT");

	@Test
	public void testSplitKeyLevel1() {
		String testKey = "D1";
		List<String> splittedLevels = TreeKeeper.splitKey(testKey);
		Assert.assertTrue(splittedLevels.size() == 1);
		Assert.assertTrue("D1".equals(splittedLevels.get(0)));
	}

	@Test
	public void testSplitKeyLevel3() {
		String testKey = "D1-S1-T1";
		List<String> splittedLevels = TreeKeeper.splitKey(testKey);
		Assert.assertTrue(splittedLevels.size() == 3);
		Assert.assertTrue("D1".equals(splittedLevels.get(0)));
		Assert.assertTrue("S1".equals(splittedLevels.get(1)));
		Assert.assertTrue("T1".equals(splittedLevels.get(2)));
	}

	@Test
	public void testAddPath() {
		buildTree();

		Assert.assertNotNull(root.children);
	}

	@Test
	public void testFindLeaf() {
		buildTree();

		String value = "canyouseeme";
		StringBuilder sb = new StringBuilder();
		TreeKeeper.findLeaf(root, value, sb, algorithm);
		String foundPath1 = sb.toString();

		sb = new StringBuilder();
		TreeKeeper.findLeaf(root, value, sb, algorithm);
		String foundPath2 = sb.toString();

		Assert.assertTrue(foundPath1.equals(foundPath2));

		String value2 = "canyouseeme2";
		sb = new StringBuilder();
		TreeKeeper.findLeaf(root, value2, sb, algorithm);
		String foundPath3 = sb.toString();

		String value3 = "canyouseeme2";
		sb = new StringBuilder();
		TreeKeeper.findLeaf(root, value3, sb, algorithm);
		String foundPath4 = sb.toString();
		Assert.assertTrue(foundPath3.equals(foundPath4));

		Assert.assertFalse(foundPath3.equals(foundPath2));
	}

	@Test
	public void testRemoveLeaf() {
	}

	private void buildTree() {
		List<String> keys = new ArrayList<>();
		keys.add("D1");
		keys.add("S1");
		keys.add("T1");
		TreeKeeper.addPath(algorithm, root, keys);

		List<String> keys4 = new ArrayList<>();
		keys4.add("D2");
		keys4.add("S1");
		keys4.add("T1");
		TreeKeeper.addPath(algorithm, root, keys4);

		List<String> keys2 = new ArrayList<>();
		keys2.add("D1");
		keys2.add("S2");
		keys2.add("T1");
		TreeKeeper.addPath(algorithm, root, keys2);

		List<String> keys3 = new ArrayList<>();
		keys3.add("D1");
		keys3.add("S1");
		keys3.add("T2");
		TreeKeeper.addPath(algorithm, root, keys3);
	}
}
