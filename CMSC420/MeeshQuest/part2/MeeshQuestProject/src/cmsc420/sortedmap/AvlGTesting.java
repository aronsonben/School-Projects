package cmsc420.sortedmap;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

public class AvlGTesting {

	/**
	 * Testing hashCode(), toString()
	 */
	@Test
	public void testBasic() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(3);
		tree.put(1, "A");
		tree.size();
		assertEquals(1, tree.size());
		int hash = tree.hashCode();
		assertTrue(hash > 0);
		assertTrue(tree.toString() != "");
	}
	
	@Test
	public void testAvlGEquality1() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(3);
		tree.put(1, "A");
		AvlGTree<Integer, String> tree2 = new AvlGTree<Integer, String>(3);
		tree2.put(1, "A");
		assertTrue(tree.hashCode() == tree2.hashCode());
		assertTrue(tree.equals(tree2));
		TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>();
		treeMap.put(1, "A");
		assertTrue(tree.equals(treeMap));
	}
	
	@Test
	public void testAvlGEquality2() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(3);
		tree.put(1, "A");
		tree.put(2, "B");
		tree.put(3, "C");
		System.out.println(tree.toString());
		AvlGTree<Integer, String> tree2 = new AvlGTree<Integer, String>(3);
		tree2.put(3, "C");
		tree2.put(2, "B");
		tree2.put(1, "A");
		print(tree2.toString());
		assertTrue(tree.equals(tree2));
	}

	@Test
	public void testAvlGEquality3() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(3);
		tree.put(1, "A");
		tree.put(2, "B");
		tree.put(3, "C");
		TreeMap<Integer, String> tree2 = new TreeMap<Integer, String>();
		tree2.put(2, "B");
		tree2.put(1, "A");
		tree2.put(3, "C");
		TreeMap<Integer, String> tree3 = new TreeMap<Integer, String>();
		tree3.put(1, "A");
		tree3.put(2, "B");
		assertTrue(tree.equals(tree2));
		//assertTrue(tree2.equals(tree3));
	}
	
	@Test
	public void testAvlGEquality4() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(3);
		tree.put(3, "A");
		tree.put(5, "B");
		tree.put(4, "C");
		tree.put(6, "D");
		tree.put(8, "E");
		tree.put(10, "F");
		TreeMap<Integer, String> tree2 = new TreeMap<Integer, String>();
		tree2.put(2, "B");
		tree2.put(1, "A");
		tree2.put(3, "C");
		tree2.put(4, "D");
		tree2.put(5, "E");
		tree2.put(6, "F");
		TreeMap<Integer, String> tree3 = new TreeMap<Integer, String>();
		tree3.put(3, "A");
		tree3.put(5, "B");
		tree3.put(4, "C");
		tree3.put(6, "D");
		tree3.put(8, "E");
		tree3.put(10, "F");
		//print(tree.toString());
		//print(tree2.toString());
		assertFalse(tree.equals(tree2));
		assertTrue(tree.equals(tree3));
		assertTrue(tree3.equals(tree));
	}
	
	@Test
	public void testEntrySet1() {
		AvlGTree<Integer, String> tree = new AvlGTree<Integer, String>(1);
		TreeMap<Integer, String> tree2 = new TreeMap<Integer, String>();
		tree.put(1, "A");
		tree.put(2, "B");
		tree.put(3, "C");
		tree.put(4, "D");
		tree.put(5, "E");
		tree.put(6, "E");
		tree2.put(1, "A");
		tree2.put(2, "B");
		tree2.put(3, "C");
		tree2.put(4, "D");
		tree2.put(5, "E");
		tree2.put(6, "E");
		assertTrue(tree.size() == tree2.size());
		assertTrue(tree.equals(tree2));
		assertTrue(tree.hashCode()==tree2.hashCode());
		assertTrue(tree.entrySet().equals(tree2.entrySet()));
		Set<Entry<Integer, String>> eSet = tree.entrySet();
		Set<Entry<Integer, String>> eSet2 = tree2.entrySet();
		
		Iterator<Entry<Integer, String>> setIterator = tree.entrySet().iterator();
		while(setIterator.hasNext()) {
			Entry<Integer, String> n = setIterator.next();
			assertTrue(tree.containsKey(n.getKey()));
			assertTrue(tree.containsValue(n.getValue()));
			assertTrue(tree.get(n.getKey()).equals(n.getValue()));
			assertTrue(eSet.contains(n));
		}
		assertTrue(eSet.size() == 6);
		assertTrue(eSet.size() == eSet2.size());
		assertTrue(eSet.hashCode() == eSet2.hashCode());
		assertTrue(eSet.toString().equals(eSet2.toString()));
		tree.put(7, "F");
		assertFalse(tree.equals(tree2));
		assertFalse(tree.entrySet().equals(tree2.entrySet()));
	}
	
	/** Inspired/Derived from GitHub credited in ReadMe.txt */
	@Test
	public void testEntries(){
		AvlGTree<String, Integer> tree = new AvlGTree<String, Integer>(String.CASE_INSENSITIVE_ORDER, 1);
		TreeMap<String, Integer> tree2=new TreeMap<String, Integer>();
		tree.put("a", 1);
		tree.put("b", 2);
		tree.put("c", 3);
		tree.put("d", 4);
		tree.put("e", 5);
		tree2.putAll(tree);
		Iterator<Entry<String, Integer>> iter = tree.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> n = iter.next();
			assertTrue(tree2.containsKey(n.getKey()));
			assertTrue(tree2.containsValue(n.getValue()));
			assertTrue(tree.containsKey(n.getKey()));
		}
		Iterator<Entry<String, Integer>> iter2 = tree2.entrySet().iterator();
		while(iter2.hasNext()) {
			Entry<String, Integer> n = iter2.next();
			assertTrue(tree.containsKey(n.getKey()));
			assertTrue(tree.containsValue(n.getValue()));
			assertTrue(tree2.containsKey(n.getKey()));
		}
		Set<Entry<String, Integer>> eSet = tree.entrySet();
		Iterator<Entry<String, Integer>> setIter1 = eSet.iterator();
		while(setIter1.hasNext()) {
			Entry<String, Integer> n = setIter1.next();
			assertTrue(eSet.contains(n));
		}
		assertTrue(eSet.size() == tree.size());
	}
	
	// inspired from github code (credit in readme)
	@Test
	public void test1(){
		AvlGTree<String, Integer> t1=new AvlGTree<String, Integer>(String.CASE_INSENSITIVE_ORDER, 1);
		TreeMap<String, Integer> t2=new TreeMap<String, Integer>();
		assertTrue(t1.equals(t2));
		
		assertTrue(t1.hashCode()==t2.hashCode());
		assertTrue(t2.equals(t1));
		for(int var=0; var<100; var++){
			t1.put(Integer.toString(var), var);
			assertFalse(t1.equals(t2));
			assertFalse(t1.hashCode()==t2.hashCode());
			t2.put(Integer.toString(var), var);
			assertTrue(t1.equals(t2));				// error
			assertTrue(t1.hashCode()==t2.hashCode());
		}
		assertTrue(t1.equals(t2));
		Set<java.util.Map.Entry<String, Integer>> s1=t1.entrySet();
		Set<java.util.Map.Entry<String, Integer>> s2=t2.entrySet();
		assertTrue(s1.equals(s2));
		assertTrue(s2.equals(s1));
		Iterator<java.util.Map.Entry<String, Integer>> i1=s1.iterator();
		Iterator<java.util.Map.Entry<String, Integer>> i2=s2.iterator();
		while(i1.hasNext()){
			assertTrue(s1.contains(i1.next()));
		}
		t1.put("101", 101);
		assertFalse(t1.equals(t2));
		Node<String, Integer> a=new Node<String, Integer>("101", 101);
		t2.put("101", 101);
		assertTrue(s1.contains(a));
		assertTrue(s2.contains(a));
		assertTrue(t1.equals(t2));
		assertTrue(t1.put("0", 42).equals(t2.put("0", 42)));
	}
	
	/**
	 * Some more simple tests for: isEmpty, size, put, clear, equals
	 */
	@Test
	public void testFuncs1(){
		AvlGTree<String, Integer> tree=new AvlGTree<String, Integer>(1);
		assertTrue(tree.isEmpty());
		
		TreeMap<String, Integer> tree2=new TreeMap<String, Integer>();
		
		// test put both ways (AvlG->TreeMap && TreeMap->AvlG)
		tree.put("a", 1);
		assertFalse(tree2.equals(tree));
		assertFalse(tree.equals(tree2));
		
		tree.clear();
		assertTrue(tree.size() == 0);
		assertTrue(tree.isEmpty());
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertTrue(tree.size() == tree2.size());
	}
	
	/**
	 * Testing firstKey, lastKey, comparator
	 */
	@Test
	public void testFuncs2(){
		AvlGTree<Integer, String> tree=new AvlGTree<Integer, String>(1);
		assertTrue(tree.comparator() == null);
		
		TreeMap<Integer, String> tree2=new TreeMap<Integer, String>();
		
		tree.put(1, "a");
		tree.put(2, "b");
		tree2.put(1, "a");
		tree2.put(2, "b");
		
		assertTrue(tree.firstKey() == 1);
		assertTrue(tree2.firstKey() == tree.firstKey());
		assertTrue(tree.firstKey() == tree2.firstKey());
		assertTrue(tree.lastKey() == 2);
		assertTrue(tree2.lastKey() == tree.lastKey());
		assertTrue(tree.lastKey() == tree2.lastKey());
		
	}
	
	/**
	 * Testing containsKey, containsValue
	 */
	@Test
	public void testFuncs3(){
		AvlGTree<Integer, String> tree=new AvlGTree<Integer, String>(1);
		
		TreeMap<Integer, String> tree2=new TreeMap<Integer, String>();
		
		tree.put(1, "a");
		tree.put(2, "b");
		tree2.put(1, "a");
		tree2.put(2, "b");
		
		assertTrue(tree.containsKey(1));
		assertTrue(tree2.containsKey(1));
		assertTrue(tree.containsValue("a"));
		assertTrue(tree2.containsValue("a"));
		assertFalse(tree.containsKey(4));
		assertFalse(tree2.containsKey(4));
		tree.clear();
		tree2.clear();
		assertFalse(tree.containsKey(1));
		assertFalse(tree2.containsKey(1));
		assertFalse(tree.containsValue("a"));
		assertFalse(tree2.containsValue("a"));
	}
	
	
	
	private static void print(String arg) {
		System.out.println(arg);
	}
	
	
	
	
}

