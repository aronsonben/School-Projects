package cmsc420.sortedmap;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;


@SuppressWarnings("serial")
public class Node<K,V> extends SimpleEntry<K, V> implements Map.Entry<K, V> {
	int height;
	Node<K,V> left = null;
	Node<K,V> right = null;
	Node<K,V> parent;
	 
	public Node(K arg0, V arg1, Node<K,V> parent) {
		super(arg0, arg1);
		this.parent = parent;
		height = 1;
	}
	
	public Node(K arg0, V arg1) {
		super(arg0, arg1);
		this.parent = null;
		height = 1;
	}
	
	public Node(Map.Entry<K, V> e) {
		super(e.getKey(), e.getValue());
		parent = null;
		height = 1;
	}
	
	public int getHeight() {
		return this.height-1;
	}
	
	public Node<K,V> getLeft() {
		return this.left;
	}
	
	public Node<K,V> getRight() {
		return this.right;
	}
	
	public Node<K,V> getParent() {
		return this.parent;
	}
}
