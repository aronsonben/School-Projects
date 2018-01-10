package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.text.NavigationFilter;

import org.w3c.dom.views.AbstractView;



public class AvlGTree<K,V> extends AbstractMap<K,V> implements SortedMap<K,V> {

	 Node<K,V> root;
	 private final Comparator<? super K> comparator;
	 private final int g;
	 
	 private int size = 0;
	 private int modCount = 0;
	 private ArrayList<V> valuesIn = new ArrayList<>();
	 private boolean changedTree = false;
	 
	 /** Global EntrySet object */
	 private EntrySet entrySet;
	 
	 public AvlGTree() {
		 root = null;
		 comparator = null;
		 g = 1;
	 }
	 public AvlGTree(Comparator<? super K> comparator) {
		 root = null;
		 this.comparator = comparator;
		 g = 1;
	 }
	 public AvlGTree(int g) {
		 root = null;
		 comparator = null;
		 this.g = g;
	 }
	 public AvlGTree(Comparator<? super K> comparator, int g) { 
		 root = null;
		 this.comparator = comparator;
		 this.g = g;
	 }
	 
	 
	 // A utility function to get height of the tree
	 public int height(Node<K,V> N) {
	     if (N == null)
	         return 0;
	
	     return N.height;
	 }
	
	 // A utility function to get maximum of two integers
	 int max(int a, int b) {
	     return (a > b) ? a : b;
	 }
	
	 // A utility function to right rotate subtree rooted with y
	 // See the diagram given above.
	 Node<K,V> rightRotate(Node<K,V> y) {
	     Node<K,V> x = y.left;
	     Node<K,V> T2 = x.right;
	
	     //System.out.println("rightRotate: "+ y.getKey() + " and " + x.getKey());
	     
	     // Perform rotation
	     x.right = y;
	     x.parent = null;
	     y.parent = x;
	     y.left = T2;
	
	     // Update heights
	     y.height = max(height(y.left), height(y.right)) + 1;
	     x.height = max(height(x.left), height(x.right)) + 1;
	
	     // Return new root
	     return x;
	 }
	
	 // A utility function to left rotate subtree rooted with x
	 // See the diagram given above.
	 Node<K,V> leftRotate(Node<K,V> x) {
		 //System.out.println("LeftRotate: "+x.toString());
	     Node<K, V> y = x.right;
	     Node<K, V> T2 = y.left;
	
	     // Perform rotation
	     //System.out.println("--Rotate: "+y.left+"->"+x+" -- "+x.right+"->"+T2);
	     y.left = x;
	     y.parent = null;	// added
	     x.parent = y;		// added
	     x.right = T2;
	     
	     
	     //  Update heights
	     x.height = max(height(x.left), height(x.right)) + 1;
	     y.height = max(height(y.left), height(y.right)) + 1;
	
	     // Return new root
	     return y;
	 }
	
	 // Get Balance factor of node N
	 int getBalance(Node<K,V> N) {
	     if (N == null)
	         return 0;
	     
	     return height(N.left) - height(N.right);
	 }
	
	 /* See "p2_extra.txt" for original insert before this change, adapting the TreeMap source code */
	 Node<K,V> insert(Node<K,V> node, Node<K,V> parent, K key, V value) {
	
	     /* 1.  Perform the normal BST insertion */
	     if (node == null) {
	    	 //Node<K,V> newNode;
	    	 changedTree = true;
	    	 size++;
	    	 valuesIn.add(value);
	    	 /*if(parent == null) {
	    		 newNode = new Node(key, value, null);
	    	 } else {
	    		 newNode = new Node(key, value, parent);
	    	 }
	         return newNode;*/
	    	 return new Node<K,V>(key, value, parent);
	     }
	     
	     /* Split computation depending on presence of a comparator */
	     Comparator<? super K> comp = comparator;
	     if(comp != null) {
	    	 
	    	 /* *** With a Comparator *** */
	    	 if (comp.compare(key, node.getKey()) < 0) {
		         node.left = insert(node.left, node, key, value);
	    	 } else if (comp.compare(key, node.getKey()) > 0) {
		         node.right = insert(node.right, node, key, value);
		     } else { // Duplicate keys not allowed
		         return node;
		     }
	    	 
	    	 /* 2. Update height of this ancestor node */
		     node.height = 1 + max(height(node.left),
		                           height(node.right));
		
		     /* 3. Get the balance factor of this ancestor
		           node to check whether this node became
		           unbalanced */
		     
		     int balance = getBalance(node);
		     
		     // If this node becomes unbalanced, then there
		     // are 4 cases Left Left Case
		     if (balance > g && comp.compare(key, node.left.getKey()) < 0) {
		         return rightRotate(node);
		     }
		
		     // Right Right Case
		     if (balance < -g && comp.compare(key, node.right.getKey()) > 0) {
		    	 return leftRotate(node);
		     }
		
		     // Left Right Case
		     if (balance > g && comp.compare(key, node.left.getKey()) > 0) {
		         node.left = leftRotate(node.left);
		         return rightRotate(node);
		     }
		
		     // Right Left Case
		     if (balance < -g && comp.compare(key, node.right.getKey()) < 0) {
		         node.right = rightRotate(node.right);
		         return leftRotate(node);
		     }
		     
		     /* return the (unchanged) node pointer */
		     return node;
	     } else {
	    	 
	    	 /* *** Without a Comparator *** */
	    	 if(key == null) 
	    		 throw new NullPointerException();
	    	 Comparable<? super K> k = (Comparable<? super K>) key;
	    	 if (k.compareTo(node.getKey()) < 0) {
		         node.left = insert(node.left, node, key, value);
	    	 } else if (k.compareTo(node.getKey()) > 0) {
		         node.right = insert(node.right, node, key, value);
	    	 } else { // Duplicate keys not allowed
		         return node;
	    	 }
	    	 
	    	 
	    	 /* 2. Update height of this ancestor node */
		     node.height = 1 + max(height(node.left),
		                           height(node.right));
		
		     /* 3. Get the balance factor of this ancestor
		           node to check whether this node became
		           unbalanced */
		    
		     int balance = getBalance(node);
		     
		     //System.out.println("height: "+height(node)+" for node: "+node.toString()+" balance="+balance);
		     
		     // If this node becomes unbalanced, then there
		     // are 4 cases Left Left Case
		     if (balance > g && k.compareTo(node.left.getKey()) < 0) {
		    	 //System.out.println("offbalance1");
		         return rightRotate(node);
		     }
	    	 
		     // Right Right Case
		     if (balance < -g && k.compareTo(node.right.getKey()) > 0) {
		    	 //System.out.println("offbalance2");
		    	 Node<K,V> lr = leftRotate(node);
		         return lr;
		    	 //return leftRotate(node);
		     }
		
		     // Left Right Case
		     if (balance > g && k.compareTo(node.left.getKey()) > 0) {
		    	 //System.out.println("offbalance3");
		         node.left = leftRotate(node.left);
		         return rightRotate(node);
		     }
		
		     // Right Left Case
		     if (balance < -g && k.compareTo(node.right.getKey()) < 0) {
		    	 //System.out.println("offbalance4");
		         node.right = rightRotate(node.right);
		         return leftRotate(node);
		     }
		     
		     /* return the (unchanged) node pointer */
		     return node;
	     }
	 }
	
	 // A utility function to print preorder traversal
	 // of the tree.
	 // The function also prints height of every node
	 void preOrder(Node<K,V> node) {
	     if (node != null) {
	         System.out.print(node.getKey() + " ");
	         preOrder(node.left);
	         preOrder(node.right);
	     }
	 }

	 /* ********************************************************* */
	 /* ********** Sorted Map Interface Methods ***************** */
	 /* ********************************************************* */

	 @Override
	public V put(Object key, Object value) {
		if(key == null)
			throw new NullPointerException();
		K k = (K)key;
		V val = (V)value;
		//System.out.println("put: "+key.toString());
		root = insert(root, null, k, val);
		//System.out.println(this.toString());
		
		// Successfully put it in (supposedly)
		if(changedTree) {
			changedTree = false;
			modCount++;
			return val;
		} else {
			return null;
		}
	}
	 
	public Node<K, V> getRoot() {
		return this.root;
	}
	 
	@Override
	public void clear() {
		modCount++;
		size = 0;
		valuesIn.clear();
		root = null;
	}

	@Override
	public boolean containsKey(Object key) {
		if(key == null) 
			throw new NullPointerException();
		if(comparator == null) {
			Comparable<? super K> kcomp = (Comparable<? super K>) key;
			return (findNode(root, key, kcomp) != null);
		} else {
			Comparator<? super K> comp = comparator;
			K keyObj = (K)key;
			return (findNodeWComparator(root, keyObj, comp) != null);
		}
	}


	@Override
	public boolean containsValue(Object value) {
		if(value == null) {
			return false;
		}
		V val = (V)value;
		if(valuesIn.contains(val))
			return true;
		else return false;
	}


	@Override
	public V get(Object key) {
		if(key == null) 
			throw new NullPointerException();
		Node<K,V> n;
		if(this.comparator == null) {
			Comparable<? super K> kcomp = (Comparable<? super K>) key;
			n = findNode(root, key, kcomp);
		} else {
			Comparator<? super K> comp = comparator;
			K keyObj = (K)key;
			n = findNodeWComparator(root, keyObj, comp);
		}
		return n.getValue();
	}

	
	/* Could do "size() == 0" from TreeMap but this seems to be more efficient (from SkipList) */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		Iterator i = map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<? extends K, ? extends V> me = (Map.Entry<? extends K, ? extends V>) i.next();
			this.put(me.getKey(), me.getValue());
		}
    }


	@Override
	public V remove(Object key) {
		// Don't have to do for part 2
		modCount--;
		return null;
	}


	@Override
	public int size() {
		return size;
	}


	@Override
	public Comparator<? super K> comparator() {
		if(comparator == null)
			return null;
		else
			return comparator;
		
	}


	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		EntrySet eset = entrySet;
		return (eset != null) ? eset : (entrySet = new EntrySet());
	}


	@Override
	public K firstKey() {
		if(root != null)
			return firstKeyAux(root);
		else 
			return null;
	}
	
	@Override
	public K lastKey() {
		if(root != null)
			return lastKeyAux(root);
		else 
			return null;
	}

	/*
	@Override
	public String toString() {
		Iterator<Map.Entry<K, V>> i = entrySet().iterator();
        if (! i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Map.Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key   == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            //System.out.println(e.right);
            if (! i.hasNext())
                return sb.append('}').toString();
            sb.append(", ");
        }
	}
	*/
	
	
	@Override
	public SortedMap subMap(Object fromKey, Object toKey) {
		return new SubMap((K)fromKey, (K)toKey);
		//return subMap((K)fromKey, true, (K)toKey, false);
	}

	
	/*
	// testing - AbstractMap.equals()
	public boolean equals(Object o) {
        if (o == this) {
        	return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }
        Map<K,V> m = (Map<K,V>) o;
        if (m.size() != size()) {
        	return false;
        }

        try {
	        Iterator<Entry<K,V>> i = entrySet().iterator();
	        while (i.hasNext()) {
	            Entry<K,V> e = i.next();
	            K key = e.getKey();
	            V value = e.getValue();
	            if (value == null) {
	                if (!(m.get(key)==null && m.containsKey(key)))
	                    return false;
	            } else {
	                if (!value.equals(m.get(key))) {
	                    return false;
	                }
	            }
	        }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }
    */

	/* ******************************* */
	/* ***** Auxilliary methods ****** */
	/* ******************************* */
	
	
	/* ***** Start Aux: "containsKey()" ******* */
	
	/** Helper method to find first key */
	private K firstKeyAux(Node<K,V> n) {
		if(n == null) {
			return null;
		} else if(n != null && n.left == null) {
			return n.getKey();
		} else {
			return firstKeyAux(n.left);
		}
	}
	
	/** Helper method to find last key */
	private K lastKeyAux(Node<K,V> n) {
		if(n == null) {
			return null;
		} else if(n != null && n.right == null) {
			return n.getKey();
		} else {
			return firstKeyAux(n.right);
		}
	}

	
	/** containsKey auxiliary method to find a Node in the tree to see if it contains the specified key 
	 * This is used when the user has not specified a Comparator 
	 * */
	private Node<K,V> findNode(Node<K,V> node, Object key, Comparable<? super K> kcomp) {
		if(node != null) {
			int cmpResult = kcomp.compareTo(node.getKey());
			if(cmpResult < 0) 
				return findNode(node.left, key, kcomp);
			else if(cmpResult > 0) 
				return findNode(node.right, key, kcomp);
			else
				return node;
		}
		return null;
	}
	
	/** containsKey() aux. method if user has specified a comparator object 
	 * Inspired by TreeMap's 
	 * */
	private Node<K,V> findNodeWComparator(Node<K,V> node, K key, Comparator<? super K> comp) {
		if(node != null) {
			int cmpResult = comp.compare(key, node.getKey());
			if(cmpResult < 0) 
				return findNodeWComparator(node.left, key, comp);
			else if(cmpResult > 0) 
				return findNodeWComparator(node.right, key, comp);
			else
				return node;
		}
		return null;
	}
	
	/** successor() method similar to that of TreeMap's. Will return the appropriate "next" 
	 * node in the AVL Tree. For example, if doing a traversal from the left side of tree, will 
	 * get either the right subtree or the parent if the right subtree is null. 
	 * @param e
	 * @return successor node or null if not possible
	 */
	protected Node<K,V> successor(Node<K,V> e) {
		if(e == null) 
			return null;
		else if(e.right != null) {
			Node<K,V> n2 = e.right;
			while(n2.left != null) {
				n2 = n2.left;
			}
			return n2;
		} else {
			Node<K,V> n2 = e.parent;
			Node<K,V> checkNode = e;
			while(n2 != null && checkNode == n2.right) {
				checkNode = n2;
				n2 = n2.parent;
			}
			return n2;
		}
	}
	
	/** Similar to that of successor() but can be used for traversing the tree backwards.
	 * Inspiration from TreeMap's predecessor() method.
	 * @param n
	 * @return predecessor node or null if not possible
	 */
	protected Node<K,V> predecessor(Node<K,V> n) {
		if(n == null) 
			return null;
		else if(n.left != null) {
			Node<K,V> n2 = n.left;
			while(n2.right != null) {
				n2 = n2.right;
			}
			return n2;
		} else {
			Node<K,V> n2 = n.parent;
			Node<K,V> checkNode = n;
			while(n2 != null && checkNode == n2.left) {
				checkNode = n2;
				n2 = n2.parent;
			}
			return n2;
		}
	}
	
	/**
	 * Get first entry in tree according to the "key-sort" function (from
	 * TreeMap source code)
	 * @return
	 */
	final Node<K,V> getFirstEntry() {
		Node<K,V> n = root;
		if(n != null) 
			while(n.left != null)
				n = n.left;
		return n;
	}
	
	 /**
     * Returns the last Entry in the Map (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Node<K,V> getLastEntry() {
        Node<K,V> p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }
	
    /**
     * Returns this map's entry for the given key, or null if the map
     * does not contain an entry for the key.
     */
	final Node<K,V> getEntry(Object key) {
		// Offload comparator-based version for sake of performance
        if (comparator != null)
            return getEntryUsingComparator(key);
        if (key == null)
            throw new NullPointerException();
        Comparable<? super K> k = (Comparable<? super K>) key;
        Node<K,V> p = root;
        while (p != null) {
            int cmp = k.compareTo(p.getKey());
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
	}
	
	 /**
     * Version of getEntry using comparator. Split off from getEntry
     * for performance. 
     */
    final Node<K,V> getEntryUsingComparator(Object key) {
        K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            Node<K,V> p = root;
            while (p != null) {
                int cmp = cpr.compare(k, p.getKey());
                if (cmp < 0)
                    p = p.left;
                else if (cmp > 0)
                    p = p.right;
                else
                    return p;
            }
        }
        return null;
    }
	
    /**
     * Test two values for equality.  Differs from o1.equals(o2) only in
     * that it copes with <tt>null</tt> o1 properly.
     */
    final static boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    
	/* ***** End Aux: "containsKey()" ***************** */
	
	/* ******************************* */
	/* ******* Unneeded methods ****** */
	/* ******************************* */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set keySet() {
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedMap tailMap(Object fromKey) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection values() {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedMap headMap(Object toKey) {
		return null;
	}
	
	/* ******************************* */
	/* ******* Inner classes ****** */
	/* ******************************* */
	
	// Node class was here
	
	/**
	 * Returns the AvlGTree wrapped as a dynamic Set. Changes reflected in the
	 * AvlGTree are reflected in the Set concurrently.
	 */
	protected class EntrySet extends AbstractSet<Map.Entry<K, V>> {

		@Override
		public void clear() {
			AvlGTree.this.clear();
		}

		@Override
		public boolean contains(Object arg0) {
			 if (!(arg0 instanceof Map.Entry))
                return false;
            Map.Entry<K,V> entry = (Map.Entry<K,V>) arg0;
            V value = entry.getValue();
            Entry<K,V> p = getEntry(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator(getFirstEntry());
		}


		@Override
		public int size() {
			return AvlGTree.this.size();
		}
		
		// this was used for testing something
		/*
		@Override
		public boolean equals(Object o) {
	        if (o == this)
	            return true;

	        if (!(o instanceof Set)) {
	            return false;
	        } 
	        
	        Collection c = (Collection) o;
	        if (c.size() != size()) {
	            return false;
	        }
	        
	        try {
	            return containsAll(c);
	        } catch (ClassCastException unused)   {
	            return false;
	        } catch (NullPointerException unused) {
	            return false;
	        }
	    }
		*/
		
	}
	
	/** Iterator to iterate through entries of set(?) */
	protected class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {
		
		EntryIterator(Node<K,V> first) {
			super(first);
		}

		@Override
		public Map.Entry<K,V> next() {
			// TODO Auto-generated method stub
			return nextEntry();
		}
		
	}
	
	/**
     * Base class for TreeMap Iterators
     */
    abstract class PrivateEntryIterator<T> implements Iterator<T> {
    	Node<K,V> next;
    	Node<K,V> lastReturned;
        int expectedModCount;

        PrivateEntryIterator(Node<K,V> first) {
            expectedModCount = modCount;
            lastReturned = null;
            next = first;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextEntry() {
        	Node<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = successor(e);
            lastReturned = e;
            return e;
        }

    }
	
	protected class SubMap extends AbstractMap<K,V> implements SortedMap<K,V>  {

		/** All objects in SubMap are greater than 
		 * this "fromKey" that is in the AvlGTree
		 */
		protected K fromKey;
		
		/** All objects in SubMap are less than
		 * this "toKey" that is in the AvlGTree
		 */
		protected K toKey;
		protected int subsize = 0;
		private boolean haveComp = false; 
		private Comparator<? super K> comp;
		
		public SubMap(K fromKey, K toKey) {
			if(fromKey == null || toKey == null) 
				throw new NullPointerException();
			if(AvlGTree.this.comparator() == null) {
				Comparable<? super K> k = (Comparable<? super K>) fromKey;
				if(k.compareTo(toKey) > 0) {
					throw new IllegalArgumentException("fromKey is larger than toKey");
				}
			} else {
				haveComp = true;
				this.comp = AvlGTree.this.comparator();
				if(AvlGTree.this.comparator.compare(fromKey, toKey) > 0) 
					throw new IllegalArgumentException("fromKey is larger than toKey");
			}
			if (!inRange(fromKey))
                throw new IllegalArgumentException("fromKey out of range");
            if (!inRange(toKey))
	                throw new IllegalArgumentException("toKey out of range");
			this.fromKey = fromKey;
			this.toKey = toKey;
		}

		@Override
		public boolean containsKey(Object key) {
			if(key == null)
				throw new NullPointerException();
			return (inRange((K)key) && AvlGTree.this.containsKey(key));
		}

		/* might be able to use AbstractMap's containsValue() if this doesn't work */
		@Override
		public boolean containsValue(Object value) {
			if(value == null) 
				throw new NullPointerException();
			SubMapEntryIterator i = iterator();
			Node<K,V> temp;
			while(i.hasNext()) {
				temp = i.getCurrent();
				if(temp.getValue().equals((V)value)) 
					return true;
				
				i.next();
			}
			return false;
		}

		@Override
		public V get(Object key) {
			if(!inRange((K)key))
				return null;
			return AvlGTree.this.get(key);
		}

		@Override
		public boolean isEmpty() {
			return entrySet().isEmpty();
		}
		
		public SubMapEntryIterator iterator() {
			if(haveComp)
				return new SubMapEntryIterator(findFirstWComp(comp), findLastWComp(comp));
			else
				return new SubMapEntryIterator(findFirstNoComp(), findLastNoComp());
		}

		@Override
		public Object put(Object key, Object value) {
			if(key == null || !inRange((K)key))
				return null;
			return AvlGTree.this.put(key, value);
		}

		@Override
		public V remove(Object key) {
			if(!inRange((K)key))
				return null;
			return AvlGTree.this.remove(key);
		}

		@Override
		public int size() {
			return entrySet().size();
		}

		@Override
		public Comparator comparator() {
			return AvlGTree.this.comparator();
		}

		@Override
		public Set entrySet() {
			return new SubMapEntrySet(this, fromKey, toKey);
		}

		@Override
		public K firstKey() {
			Node<K,V> n;
			if(AvlGTree.this.comparator() == null) 
				n = findFirstNoComp();
			else
				n = findFirstWComp(AvlGTree.this.comparator);
			if(n == null) 
				return null;
			else 
				return n.getKey();
		}

		@Override
		public SortedMap subMap(Object fromKey, Object toKey) {
			if(fromKey == null || toKey == null) 
				throw new NullPointerException();
			if(AvlGTree.this.comparator() == null) {
				Comparable<? super K> k = (Comparable<? super K>) ((K)fromKey);
				if(k.compareTo((K)toKey) > 0) {
					throw new IllegalArgumentException("fromKey is larger than toKey");
				}
			} else {
				if(AvlGTree.this.comparator.compare((K)fromKey, (K)toKey) > 0) 
					throw new IllegalArgumentException("fromKey is larger than toKey");
			}
			if (!inRange((K)fromKey))
                throw new IllegalArgumentException("fromKey out of range");
            if (!inRange((K)toKey))
	                throw new IllegalArgumentException("toKey out of range");
			return new SubMap((K)fromKey, (K)toKey);
		}
		
		@Override
		public K lastKey() {
			Node<K,V> n;
			if(AvlGTree.this.comparator() == null) 
				n = findLastNoComp();
			else
				n = findLastWComp(AvlGTree.this.comparator);
			if(n == null) 
				return null;
			else 
				return n.getKey();
		}
		
		/* ********************** */
		/* ** Unneeded methods ** */
		/* ********************** */
		
		@Override
		public SortedMap<K,V> headMap(Object toKey) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<K> keySet() {
			throw new UnsupportedOperationException();
		}

		@Override
		public SortedMap<K,V> tailMap(Object fromKey) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<V> values() {
			throw new UnsupportedOperationException();
		}
		
		/* *************************** */
		/* ** SubMap helper methods ** */
		/* *************************** */
		
		/**
		 * Locates the first element in the backing <tt>AvlGTree</tt> greater
		 * than or equal to <tt>fromKey</tt>.
		 * @return first element >= fromKey
		 */
		protected Node<K,V> findFirstNoComp() {
			// if fromKey is removed from this SubMap, we'll need a new "first"
			// element.
			// So really, we want find the first element greater than or equal
			// to fromKey
			/*if (size() <= 0)
				return null;*/
			Comparable<? super K> fkey = (Comparable<? super K>)fromKey;
			Node<K,V> curr = AvlGTree.this.root;
			while(curr != null) {
				int cmpResult = fkey.compareTo(curr.getKey());
				if(cmpResult < 0) {
					curr = curr.left;
				} else if(cmpResult > 0) {
					curr = curr.right;
				} else {
					return curr;
				}
			}
			return null;
		}
		
		/** 
		 * Same as "findNodeNoComp()" but uses the backing AvlGTree's 
		 * comparator to do comparisons instead of the Key object's 
		 * natural comparator.
		 */
		protected Node<K,V> findFirstWComp(Comparator<? super K> comparator) {
			/*if (size() <= 0)
				return null;*/
			Comparator<? super K> comp = comparator;
			Node<K,V> curr = AvlGTree.this.root;
			while(curr != null) {
				int cmpResult = comp.compare(fromKey, curr.getKey());
				if(cmpResult < 0) {
					curr = curr.left;
				} else if(cmpResult > 0) {
					curr = curr.right;
				} else {
					return curr;
				}
			}
			return null;
		}
		
		/** 
		 * Find last element that is before the toKey. Use with no given Comparator
		 */
		protected Node<K,V> findLastNoComp() {
			Comparable<? super K> fkey = (Comparable<? super K>)toKey;
			Node<K,V> curr = AvlGTree.this.root;
			while(curr != null) {
				int cmpResult = fkey.compareTo(curr.getKey());
				if(cmpResult < 0) {
					curr = curr.left;
				} else if(cmpResult > 0) {
					curr = curr.right;
				} else {
					return curr;
				}
			}
			return null;
		}
		
		/** 
		 * Find last element that is before the toKey. Use with given Comparator.
		 */
		protected Node<K,V> findLastWComp(Comparator<? super K> comparator) {
			Comparator<? super K> comp = comparator;
			Node<K,V> curr = AvlGTree.this.root;
			while(curr != null) {
				int cmpResult = comp.compare(toKey, curr.getKey());
				if(cmpResult < 0) {
					curr = curr.left;
				} else if(cmpResult > 0) {
					curr = curr.right;
				} else {
					return curr;
				}
			}
			return null;
		}
		
		
		/** 
		 * Checks to see if the given key is located between fromKey and toKey
		 */
		protected boolean inRange(K key) {
			if(key == null) 
				throw new NullPointerException();
			if(AvlGTree.this.comparator() == null) {
				Comparable<? super K> fk = (Comparable<? super K>)fromKey;
				Comparable<? super K> tk = (Comparable<? super K>)toKey;
				return ((fk.compareTo(key) < 0) && (tk.compareTo(key) > 0)) ? true : false;
			} else {
				Comparator<? super K> comp = AvlGTree.this.comparator;
				return ((comp.compare(fromKey, key) < 0) && (comp.compare(toKey, key) > 0)) ? true : false;
			}
		}
		
	}
	
	protected class SubMapEntrySet extends AbstractSet<Node<K,V>>  {
		private final SubMap sm;
		private final K fromKey, toKey;
		
		public SubMapEntrySet(SubMap sm, K fromKey, K toKey) {
			this.sm = sm;
			this.fromKey = fromKey;
			this.toKey = toKey;
		}
		
		public boolean contains(Object arg0) {
			 if (!(arg0 instanceof Node))
                return false;
            Node<K,V> entry = (Node<K,V>) arg0;
            V value = entry.getValue();
            Node<K,V> p = getEntry(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
		}
		
		@Override
		public boolean isEmpty() {
			return AvlGTree.this.isEmpty();
		}

		public SubMapEntryIterator iterator() {
			if(AvlGTree.this.comparator() == null) {
				return new SubMapEntryIterator(sm.findFirstNoComp(), sm.findLastNoComp());
			} else {
				return new SubMapEntryIterator(sm.findFirstWComp(AvlGTree.this.comparator()), 
											sm.findLastWComp(AvlGTree.this.comparator()));
			}
			
		}


		@Override
		public int size() {
			SubMapEntryIterator iter = this.iterator();
			int subsize = 0;
			while(iter.hasNext()) {
				subsize++;
				iter.next();
			}
			return subsize;
		}
		
	}

	abstract class SubMapIterator<T> implements Iterator<T> {
        Node<K,V> lastReturned;
        Node<K,V> next;
        final Object fenceKey;
        int expectedModCount;

        SubMapIterator(Node<K,V> first,
                       Node<K,V> fence) {
            expectedModCount = AvlGTree.this.modCount;
            lastReturned = null;
            next = first;
            fenceKey = fence.getKey();
        }

        public final boolean hasNext() {
            return next != null && next.getKey() != fenceKey;
        }

        final Node<K,V> nextEntry() {
            Node<K,V> e = next;
            if (e == null || e.getKey() == fenceKey)
                throw new NoSuchElementException();
            if (AvlGTree.this.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = successor(e);
            lastReturned = e;
            return e;
        }

    }
	
	protected class SubMapEntryIterator<T> extends SubMapIterator<Map.Entry<K, V>> {
		
		SubMapEntryIterator(Node<K,V> fromNode, Node<K,V> toNode) {
			super(fromNode, toNode);
		}
		
		@Override
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
		
		public Node<K,V> getCurrent() {
			return (next != null ? next : null);
		}
	}
	
	/* ************************************************************** */
	/* ******** Main *********** */
	/* ************************************************************** */
	
	
	
	/* Main helpers *************************************** */
	
	private void inorderTraversal(Node<K,V> root) {
        if(root !=null){
            helper(root);
        }
	}
	 
    private void helper(Node<K,V> p){
        if(p.left!=null)
            helper(p.left);
 
        System.out.print(p.getKey() + " ");
 
        if(p.right!=null)
            helper(p.right);
    }
	
	
	
	
	
	
}