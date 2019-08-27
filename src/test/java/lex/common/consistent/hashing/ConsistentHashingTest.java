package lex.common.consistent.hashing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lex.common.consistent.hashing.ConsistentHashing;

public class ConsistentHashingTest {

	public static void main(String[] args) {

		String db1 = "db connection 1";
		String db2 = "db connection 2";
		String db3 = "db connection 3";
		String db4 = "db connection 4";

		List<String> connections = new ArrayList<>();
		connections.add(db1);
		connections.add(db2);
		connections.add(db3);
		connections.add(db4);

		Map<String, Integer> resultMap = new HashMap<>();
		resultMap.put(db1, 0);
		resultMap.put(db2, 0);
		resultMap.put(db3, 0);
		resultMap.put(db4, 0);

		// ConsistentHashing<String> hashing = new ConsistentHashing<String>(new
		// HashingAlgorithm() {
		//
		// @Override
		// public Long hash(Object key) {
		// return fnv1HashingAlgorithm(key.toString());
		// }
		// }, 67, connections);

		ConsistentHashing<String> hashing = new ConsistentHashing<String>(67, connections);

		doHashOnly(hashing);

		// doHash(hashing, resultMap);

	}

	public static void doHashOnly(ConsistentHashing<String> hashing) {
		long start = System.currentTimeMillis();
		long keys = 40000000l;
		for (long key = 0l; key < keys; key++) {
			hashing.get(key);
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}

	public static void doHash(ConsistentHashing<String> hashing, Map<String, Integer> resultMap) {
		long start = System.currentTimeMillis();
		long keys = 40000000l;
		for (long key = 0l; key < keys; key++) {
			// long startIn = System.nanoTime();
			String hit = hashing.get(key);
			// long endIn = System.nanoTime();
			// String output = "Single hit: " + (endIn - startIn);
			// System.out.println(output);
			if (resultMap.containsKey(hit)) {
				Integer currentValue = resultMap.get(hit);
				Integer newValue = Integer.valueOf(currentValue.intValue() + 1);
				resultMap.put(hit, newValue);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start);

		Iterator iterator = resultMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Integer> entry = (Entry<String, Integer>) iterator.next();
			String output = entry.getKey() + " hitted: " + entry.getValue();
			System.out.println(output);
		}

	}

}
