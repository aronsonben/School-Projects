package cmsc420.sortedmap;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Test;

import cmsc420.sortedmap.AvlGTree.AvlNode;
import cmsc420.sortedmap.AvlGTree.EntrySet;



public class AvlGTests {

	/**
	 * Running this before even changing any AvlG code. Test some basics of the AvlGTree.
	 */
	@Test
	public void testBasics1() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		assertTrue(avl1.isEmpty());
		assertTrue(tm1.isEmpty());
		assertTrue(avl1.size() == 0);
		assertTrue(tm1.size() == 0);
		avl1.put(1, "1");
		assertFalse(avl1.size() == tm1.size());
		tm1.put(1, "1");
		// Basics
		assertTrue(avl1.equals(tm1));
		assertTrue(avl1.toString().equals(tm1.toString()));
		assertTrue(avl1.hashCode()==tm1.hashCode());
		for(int i=2; i < 10; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		assertTrue(avl1.equals(tm1));
		assertTrue(avl1.toString().equals(tm1.toString()));
		assertTrue(avl1.hashCode()==tm1.hashCode());
		assertFalse(avl1.isEmpty());
		assertFalse(tm1.isEmpty());
		// Other funcs
		for(int i=1; i < 10; i++) {
			assertTrue(avl1.get(i).equals(String.valueOf(i)));
			assertTrue(tm1.get(i).equals(String.valueOf(i)));
			assertTrue(avl1.containsKey(i));
			assertTrue(tm1.containsKey(i));
			assertTrue(avl1.containsValue(String.valueOf(i)));
			assertTrue(tm1.containsValue(String.valueOf(i)));
		}
		assertTrue(avl1.firstKey().equals(tm1.firstKey()));
		assertTrue(avl1.lastKey().equals(tm1.lastKey()));
	}
	
	/**
	 * Testing EntrySet and SubMap before changing any AvlGTree code.
	 */
	@Test
	public void testBasics2() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		for(int i=0; i < 10; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		Iterator<Entry<Integer, String>> avlIter = avl1.entrySet().iterator();
		while(avlIter.hasNext()) {
			Entry<Integer, String> e = avlIter.next();
			assertTrue(avl1.containsKey(e.getKey()));
			assertTrue(tm1.containsKey(e.getKey()));
		}
		Iterator<Entry<Integer, String>> tmIter = tm1.entrySet().iterator();
		while(tmIter.hasNext()) {
			Entry<Integer, String> e = tmIter.next();
			assertTrue(avl1.containsKey(e.getKey()));
			assertTrue(tm1.containsKey(e.getKey()));
		}
		SortedMap<Integer, String> avlSub1 = avl1.subMap(3, 7);
		SortedMap<Integer, String> tmSub1 = tm1.subMap(3, 7);
		assertTrue(avlSub1.size() == tmSub1.size());
		assertTrue(avlSub1.containsValue("5"));
		assertTrue(tmSub1.containsValue("5"));
		assertTrue(avlSub1.size() < avl1.size());
		assertTrue(avlSub1.firstKey().equals(tmSub1.firstKey()));
		assertFalse(avlSub1.lastKey().equals(new Integer(7)));
	}
	
	@Test
	public void testRemove1() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<Integer, String>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		for(int i=0; i < 20; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		avl1.remove(1);
		tm1.remove(1);
		
		assertTrue(avl1.equals(tm1));
		assertTrue(avl1.toString().equals(tm1.toString()));
		assertTrue(avl1.hashCode() == tm1.hashCode());
		assertTrue(avl1.size()==tm1.size());
		
		int ct = 0;
		Iterator<Map.Entry<Integer, String>> iter = avl1.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer, String> e = iter.next();
			Integer ekey = e.getKey();
			System.out.println("Remove: "+e.toString());
			iter.remove();
			//System.out.println("(Test) After: "+avl1.toString());
			preOrder(avl1.getRoot());
			assertFalse(avl1.containsKey(ekey));
		}
		
		//System.out.println(avl1.toString());
		assertTrue(avl1.size() == 0);
		assertTrue(avl1.isEmpty());
	}

	@Test
	public void testRemove2() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<Integer, String>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		
		avl1.put(2, "2");
		avl1.put(0, "0");
		avl1.put(3, "3");
		avl1.put(4, "4");
		tm1.put(2, "2");
		tm1.put(0, "0");
		tm1.put(3, "3");
		tm1.put(4, "4");
		
		preOrder(avl1.getRoot());
		inOrder(avl1.getRoot());
		
		// Removing root
		Integer rt = avl1.getRoot().getKey();
		avl1.remove(rt);
		tm1.remove(rt);
		
		assertFalse(avl1.containsKey(rt));
		preOrder(avl1.getRoot());
		inOrder(avl1.getRoot());
		assertTrue(avl1.equals(tm1));
		
		// NOTE: Below test is sculpted around the following tree:
		//		3
		//	0		4
		avl1.remove(0);
		tm1.remove(0);
		
		// remove root with one child
		System.out.println("remove 3");
		avl1.remove(3);
		tm1.remove(3);
		
		assertFalse(avl1.containsKey(3));
		assertTrue(avl1.equals(tm1));
		assertTrue(avl1.size() == tm1.size());
	}
	
	@Test
	public void testRemove3() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<Integer, String>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		
		for(int i=0; i < 100; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		assertTrue(avl1.equals(tm1));
		// remove all
		int ct = 0;
		Iterator<Map.Entry<Integer, String>> iter = avl1.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<Integer,String> temp = iter.next();
			Integer key = temp.getKey();
			System.out.println("Removing: "+key);
			iter.remove();
			assertFalse(avl1.containsKey(key));
			ct++;
			if(ct == 50)
				break;
			//if(key == 13)
			//	break;
			//System.out.println("--END "+key+"--");
		}
		Iterator<Map.Entry<Integer, String>> iter2 = tm1.entrySet().iterator();
		while(iter2.hasNext()) {
			Entry<Integer,String> temp = iter2.next();
			Integer key = temp.getKey();
			iter2.remove();
			assertFalse(tm1.containsKey(key));
		}
		
		assertTrue(avl1.size() == 0);
		assertTrue(avl1.isEmpty());
		assertTrue(avl1.equals(tm1));
	}
	
	// testing remove from Entry Set
	@Test
	public void testRemove4() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<Integer, String>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		
		for(int i=0; i < 50; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		
		Set<Entry<Integer, String>> eset1 = avl1.entrySet();
		Set<Entry<Integer, String>> eset2 = tm1.entrySet();
		Iterator<Map.Entry<Integer, String>> iter = avl1.entrySet().iterator();
		/*while(iter.hasNext()) {
			Entry<Integer, String> e = iter.next();
			eset1.remove(e);
		}*/
	
		//assertTrue(eset1.isEmpty());
		//assertTrue(eset1.equals(eset2));
	}
	
	@Test
	public void testRemoveFirstLast() {
		GuardedAvlGTree<Integer, String> avl1 = new GuardedAvlGTree<Integer, String>(null, 1);
		TreeMap<Integer, String> tm1 = new TreeMap<Integer, String>();
		
		for(int i=0; i < 50; i++) {
			avl1.put(i, String.valueOf(i));
			tm1.put(i, String.valueOf(i));
		}
		
		Integer first = avl1.firstKey();
		String firstV = avl1.get(first);
		String firstV2 = tm1.get(tm1.firstKey());
		assertEquals(avl1.remove(first), firstV);
		assertEquals(tm1.remove(tm1.firstKey()), firstV2);
		Integer last = avl1.lastKey();
		String lastV = avl1.get(last);
		String lastV2 = tm1.get(tm1.lastKey());
		assertEquals(avl1.remove(last), lastV);
		assertEquals(tm1.remove(tm1.lastKey()), lastV2);
		
	}
	
	/**
	 * Attempting to solve the public test: "testBasicDeleteFirstLast" (#53) issue
	 */
	@Test
	public void testDeleteFirstLast() {
		GuardedAvlGTree<String, Integer> avl1 = new GuardedAvlGTree<String, Integer>(null, 1);
		TreeMap<String, Integer> tm1 = new TreeMap<String, Integer>();
		
		//for(int i=0; i < 500; i++) {
		for(int i=199; i >= 0; i--) {
			avl1.put(String.valueOf(i), i);
			tm1.put(String.valueOf(i), i);
		}
		
		Iterator<Map.Entry<String, Integer>> iter = avl1.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Integer> temp = iter.next();
			String key = temp.getKey();
			//System.out.println("Removing: "+key);
			iter.remove();
			assertFalse(avl1.containsKey(key));
		}
		Iterator<Map.Entry<String, Integer>> iter2 = tm1.entrySet().iterator();
		while(iter2.hasNext()) {
			Entry<String, Integer> temp = iter2.next();
			String key = temp.getKey();
			iter2.remove();
			assertFalse(tm1.containsKey(key));
		}
		
		/*while(!avl1.isEmpty()) {
			String last = avl1.lastKey();
			avl1.remove(last);
			assertFalse(avl1.containsKey(last));
			assertFalse(avl1.containsValue(String.valueOf(last)));
		}
		while(!tm1.isEmpty()) {
			String last = tm1.lastKey();
			tm1.remove(last);
			assertFalse(tm1.containsKey(last));
			assertFalse(tm1.containsValue(String.valueOf(last)));
		}*/
		assertTrue(avl1.hashCode() == tm1.hashCode());
		assertTrue(avl1.isEmpty());
	}
	
	
	@SuppressWarnings("unused")
	private void preOrder(AvlGTree.AvlNode<Integer,String> node) {
    	preOrder2(node);
    	System.out.println();
    }
    
    private void preOrder2(AvlGTree.AvlNode<Integer,String> node) {
    	if(node != null) {
    		System.out.print(node.getKey()+" ");
    		preOrder2(node.left);
    		preOrder2(node.right);
    	}
    }
    
    @SuppressWarnings("unused")
	private void inOrder(AvlGTree.AvlNode<Integer,String> node) {
    	inOrder2(node);
    	System.out.println();
    }
    
    private void inOrder2(AvlGTree.AvlNode<Integer,String> node) {
    	if(node != null) {
    		preOrder2(node.left);
    		System.out.print(node.getKey()+" ");
    		preOrder2(node.right);
    	}
    }
}
