package cmsc420.sortedmap;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * E. Wang's AVL-g tree implementation.
 */
public class AvlGTree<K, V> extends AbstractMap<K, V> implements
        SortedMap<K, V> {
    static final boolean summer2014 = false; 
    
	public final int g;
   
    private Comparator<? super K> comparator = null;
    private AvlNode<K, V> root = null;
    private long size = 0;
    private int modCount = 0;
    private EntrySet entrySet = null;
    private KeySet keySet = null;
    private Values values = null;

    public AvlGTree() {
        this.g = 1;
    }

    public AvlGTree(final Comparator<? super K> comp) {
        this.comparator = comp;
        this.g = 2;
    }

    public AvlGTree(final int g) {
        this.g = g;
    }

    public AvlGTree(final Comparator<? super K> comp, final int g) {
        this.comparator = comp;
        this.g = g;
    }

    public Comparator<? super K> comparator() {
        return comparator;
    }

    public void clear() {
        modCount++;
        size = 0;
        root = null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        if (size > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        else
            return (int) size;
    }

    public int height() {
        return root.getHeight();
    }

    public boolean containsKey(Object key) {
        if (key == null)
            throw new NullPointerException();
        return getNode(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value == null)
            throw new NullPointerException();
        return nodeContainsValue(root, value);
    }

    public V get(Object key) {
        if (key == null)
            throw new NullPointerException();

        AvlNode<K, V> p = getNode(key);
        return (p == null ? null : p.value);
    }

    public V put(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException();

        AvlNode<K, V> t = root;
        if (t == null) {
            root = new AvlNode<K, V>(key, value, comparator);
            size = 1;
            modCount++;
            return null;
        }
        AvlNode<K, V> e = new AvlNode<K, V>(key, value, comparator);
        V oldValue = root.add(e);

        modCount++;
        if (oldValue == null) {
            fixAfterModification(e);
            size++;
            return null;
        } else {
            return oldValue;
        }
    }

    public V remove(Object key) {
    	AvlNode<K,V> p = getNode(key);
        if (p == null)
            return null;

        V oldValue = p.getValue();
        //root = deleteNode(root, p.getKey());
        deleteNode(p);
        return oldValue;
    }

    public K firstKey() {
        return key(getFirstNode());
    }

    public K lastKey() {
        return key(getLastNode());
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        EntrySet es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }
    
    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new SubMap<K, V>(this, fromKey, toKey);
    }
    
    /* *********************************************************** */
    /* ** Unneeded Methods *************************************** */
    /* *********************************************************** */
    
    public Set<K> keySet() {
        if (summer2014) return null; 
    	KeySet ks = keySet;
    	return (ks != null) ? ks : (keySet = new KeySet());
    }

    public Collection<V> values() {
    	if (summer2014) return null; 
    	Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    public SortedMap<K, V> headMap(K toKey) {
    	if (summer2014) return null; 
        return new SubMap<K, V>(this, null, toKey);
    }
    
    public SortedMap<K, V> tailMap(K fromKey) {
    	if (summer2014) return null; 
    	return new SubMap<K, V>(this, fromKey, null);
    }
    
    static final class AvlNode<K, V> implements Map.Entry<K, V> {
        private K key;
        private V value;
        public AvlNode<K, V> left = null;
        public AvlNode<K, V> right = null;
        public AvlNode<K, V> parent = null;
        Comparator<? super K> comparator;
        private int leftHeight;
        private int rightHeight;

        AvlNode(K key, V value, Comparator<? super K> comp) {
            this.key = key;
            this.value = value;
            this.parent = null;
            this.comparator = comp;
            this.leftHeight = 0;
            this.rightHeight = 0;
        }

        public V add(AvlNode<K, V> node) {
            int cmp = compare(node.key, this.key);
            if (cmp < 0) {
                if (left == null) {
                    leftHeight = 1;
                    left = node;
                    left.parent = this;
                    return null;
                } else {
                    V ret = this.left.add(node);
                    if (ret == null)
                        leftHeight = left.getHeight();
                    return ret;
                }
            } else if (cmp > 0) {
                if (right == null) {
                    rightHeight = 1;
                    right = node;
                    right.parent = this;
                    return null;
                } else {
                    V ret = this.right.add(node);
                    if (ret == null)
                        rightHeight = right.getHeight();
                    return ret;
                }
            } else {
                return this.setValue(node.value);
            }
        }

        public int hashCode() {
            int keyHash = (key == null ? 0 : key.hashCode());
            int valueHash = (value == null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

            return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
        }

        public String toString() {
            return key + "=" + value;
        }
        
        public String fullString() {
        	return "{" + key + "=" + value + " L=" + left + " R=" + right +
        			" P=" + parent + " H="+this.getHeight()+ "}";
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public int getHeight() {
            return 1 + Math.max(leftHeight, rightHeight);
        }

        public int getBalance() {
            return leftHeight - rightHeight;
        }

        public boolean isLeaf() {
            return left == null && right == null;
        }

        @SuppressWarnings({ "unchecked" })
        private int compare(Object k1, Object k2) {
            return comparator == null ? ((Comparable<? super K>) k1)
                    .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
        }

        public Node buildXmlNode(final Node parent) {
            final Element e = parent.getOwnerDocument().createElement("node");
            e.setAttribute("key", key.toString());
            e.setAttribute("value", value.toString());

            if (left != null) {
                e.appendChild(left.buildXmlNode(e));
            } else {
                e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
            }

            if (right != null) {
                e.appendChild(right.buildXmlNode(e));
            } else {
                e.appendChild(e.getOwnerDocument().createElement("emptyChild"));
            }
            return e;
        }
    }
    
    /* ******************************************************* */
    /* ** Auxiliary Methods ********************************** */
    /* ******************************************************* */
    
    /* Inspiration courtesy geeksforgeeks.org Avl Tree delete and 
     * grepcode.com Java TreeMap source code.
     */
    private void deleteNode(AvlNode<K,V> node) {
    	// TODO: deleteNode
    	 modCount++;
         size--;

         // If strictly internal, copy successor's element to p and then make p
         // point to successor.
         if (node.left != null && node.right != null) {
             AvlNode<K,V> s = successor(node);
             node.key = s.getKey();
             node.value = s.getValue();
             node = s;
         } // node has 2 children

         // Start fixup at replacement node, if it exists.
         AvlNode<K,V> replacement = (node.left != null ? node.left : node.right);

         if (replacement != null) {
             // Link replacement to parent
             replacement.parent = node.parent;
             if (node.parent == null)
                 root = replacement;
             else if (node == node.parent.left)
                 node.parent.left  = replacement;
             else
                 node.parent.right = replacement;

             // Null out links so they are OK to use by fixAfterDeletion.
             node.left = node.right = node.parent = null;

             // Fix replacement
             if (node.getBalance() < 0)
                 fixAfterModification(replacement);
         } else if (node.parent == null) { // return if we are the only node.
             root = null;
         } else { //  No children. Use self as phantom replacement and unlink.
        	 if (node.getBalance() < 0)
                 fixAfterModification(node);

             if (node.parent != null) {
                 if (node == node.parent.left)
                     node.parent.left = null;
                 else if (node == node.parent.right)
                     node.parent.right = null;
                 node.parent = null;
             }
         }
    }
    	
    
    /** Finds the smallest node in the given tree */
	private AvlNode<K,V> minValueNode(AvlNode<K,V> node) {
	     AvlNode<K,V> current = node;
	
	     /* loop down to find the leftmost leaf */
	     while (current.left != null)
	        current = current.left;
	
	     return current;
	}

    private final AvlNode<K, V> getNode(Object key) {
        AvlNode<K, V> p = root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0)
                p = p.left;
            else if (cmp > 0)
                p = p.right;
            else
                return p;
        }
        return null;
    }

    private final boolean nodeContainsValue(AvlNode<K, V> node, Object value) {
        if (node == null)
            return false;

        if (node.value.equals(value))
            return true;
        else
            return nodeContainsValue(node.left, value)
                    || nodeContainsValue(node.right, value);
    }

    private final AvlNode<K, V> getFirstNode() {
        AvlNode<K, V> p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }

    private final AvlNode<K, V> getLastNode() {
        AvlNode<K, V> p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }

    private final NodeIterator getNodeIterator() {
        return new NodeIterator(getFirstNode());
    }

    private final ReverseNodeIterator getReverseNodeIterator() {
        return new ReverseNodeIterator(getLastNode());
    }

    private static <K, V> AvlNode<K, V> successor(AvlNode<K, V> t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            AvlNode<K, V> p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            AvlNode<K, V> p = t.parent;
            AvlNode<K, V> ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private static <K, V> AvlNode<K, V> predecessor(AvlNode<K, V> t) {
        if (t == null)
            return null;
        else if (t.left != null) {
            AvlNode<K, V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
        } else {
            AvlNode<K, V> p = t.parent;
            AvlNode<K, V> ch = t;
            while (p != null && ch == p.left) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    private void fixAfterModification(AvlNode<K, V> e) {
        //System.out.println("Fix: "+e.fullString());
        //System.out.println(e.getBalance()+" "+e.leftHeight+" "+e.rightHeight);
    	
        if (e.getBalance() > g) {
            if (e.left.getBalance() >= 0)
                e = rotateRight(e);
            else
                e = rotateLeftRight(e);
        } else if (e.getBalance() < -g) {
            if (e.right.getBalance() <= 0) {
            	//System.out.println("Before rotate: "+e.fullString());
                e = rotateLeft(e);
                //System.out.println("After  rotate: "+e.fullString());
            } else {
                e = rotateRightLeft(e);
            }
        }

        if (e.parent != null) {
            fixAfterModification(e.parent);
        }
        else {
        	//System.out.println("Set root equal: "+e.fullString());
            this.root = e;
        }
    }

    private AvlNode<K, V> rotateRight(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> l = p.left;
        p.left = l.right;
        if (l.right != null)
            l.right.parent = p;
        l.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.right == p)
                p.parent.right = l;
            else
                p.parent.left = l;
        }
        l.right = p;
        p.parent = l;

        p.leftHeight = l.rightHeight;
        l.rightHeight = p.getHeight();
        updateHeight(l);
        return l;
    }

    private AvlNode<K, V> rotateLeft(AvlNode<K, V> p) {
        if (p == null)
            return null;

        AvlNode<K, V> r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent != null) {
            if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
        }
        r.left = p;
        p.parent = r;

        p.rightHeight = r.leftHeight;
        r.leftHeight = p.getHeight();
        updateHeight(r);
        return r;

    }

    private AvlNode<K, V> rotateRightLeft(AvlNode<K, V> p) {
        p.right = rotateRight(p.right);
        return rotateLeft(p);
    }

    private AvlNode<K, V> rotateLeftRight(AvlNode<K, V> p) {
        p.left = rotateLeft(p.left);
        return rotateRight(p);
    }

    private void updateHeight(AvlNode<K, V> n) {
    	//System.out.println("(updateHeight) for "+n);
        
    	if (n.parent == null) {
    		//System.out.println("null parent");
            return;
    	}

        if (n.parent.left == n) {
        	//System.out.println("Left (height): "+n.getHeight());
            n.parent.leftHeight = n.getHeight();
        } else {
        	//System.out.println("Right (height): "+n.getHeight());
            n.parent.rightHeight = n.getHeight();
        }

        if (n.parent != null) {
            updateHeight(n.parent);
        }

    }
    
    /**
     * Find the parent child that equals 'node' and set it to 'newNode'
     * @param parent of the node (2nd param)
     * @param node being deleted
     * @param newNode update value of node with newNode (might be null)
     * @return updated node
     */
    private AvlNode<K,V> setCorrectChild(AvlNode<K,V> parent, AvlNode<K,V> node, AvlNode<K,V> newNode) {
    	if(parent == null || node == null)
    		return null;
    	if(parent.left == null) {
    		parent.right = newNode;
    		return parent.right;
    	} else if(parent.right == null) {
    		parent.left = newNode;
    		return parent.left;
    	} else if(parent.left.equals(node)) {
    		//System.out.println("Left: "+parent.left.fullString());
    		parent.left = newNode;
    		return parent.left;
    	} else if(parent.right.equals(node)) {
    		parent.right = newNode;
    		return parent.right;
    	} else return null;
    }
    
    /** 
     * Give an integer corresponding to the correct side the parent should
     * 	update. Likely only used at a couple points in "deleteNode()"	
     * @param parent of the node being deleted in deleteNode
     * @param node being deleted
     * @return 1 for the right child
     * @return -1 for the left child
     * @return 0 for an error, etc.
     */
    private int subtreeSide(AvlNode<K,V> parent, AvlNode<K,V> node) {
    	if(parent == null || node == null)
    		return 0;
    	if(parent.left == null) {
    		return 1;
    	} else if(parent.right == null) {
    		return -1;
    	} else if(parent.left.equals(node)) {
    		return -1;
    	} else if(parent.right.equals(node)) {
    		return 1;
    	} else return 0;
    }
    
    @SuppressWarnings("unused")
	private void preOrder(AvlNode<K,V> node) {
    	preOrder2(node);
    	System.out.println();
    }
    
    private void preOrder2(AvlNode<K,V> node) {
    	if(node != null) {
    		System.out.print(node.getKey()+" ");
    		preOrder2(node.left);
    		preOrder2(node.right);
    	}
    }
    
    protected AvlNode<K,V> getRoot() {
    	return this.root;
    }

    private static <K> K key(Map.Entry<K, ?> e) {
        if (e == null)
            throw new NoSuchElementException();
        return e.getKey();
    }

    @SuppressWarnings("unchecked")
    private final int compare(Object k1, Object k2) {
        return comparator == null ? ((Comparable<? super K>) k1)
                .compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }
    
    class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            return new EntryIterator(getFirstNode());
        }

        public boolean add(Map.Entry<K, V> o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            AvlGTree.this.clear();
        }

        public int size() {
            return AvlGTree.this.size();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            V value = entry.getValue();
            AvlNode<K, V> p = getNode(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
        }

        public boolean equals(final Object other) {
            if (other == null)
                return false;
            int i = ((Collection<?>) other).size(), j = size();
            return ((Collection<?>) other).containsAll(this) && i == j;
        }

        public boolean remove(Object o) {
        	if (!(o instanceof Map.Entry)) {
                return false;
        	}
            Map.Entry<K,V> entry = (Map.Entry<K,V>) o;
            V value = entry.getValue();
            Entry<K,V> p = getNode(entry.getKey());
            if (p != null && valEquals(p.getValue(), value)) {
                AvlGTree.this.remove(p.getKey());
                return true;
            }
            return false;
        }
    }

    class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator(getFirstNode());
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean contains(Object o) {
            return AvlGTree.this.containsKey(o);
        }

        public void clear() {
            AvlGTree.this.clear();
        }

        public int size() {
            return AvlGTree.this.size();
        }

        public boolean equals(final Object other) {
            if (other == null)
                return false;
            int i = ((Collection<?>) other).size(), j = size();
            return ((Collection<?>) other).containsAll(this) && i == j;
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator(getFirstNode());
        }

        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Object o) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            AvlGTree.this.clear();
        }

        public int size() {
            return AvlGTree.this.size();
        }

        public boolean contains(Object o) {
            return AvlGTree.this.containsValue(o);
        }

        public boolean equals(final Object other) {
            if (other == null)
                return false;
            int i = ((Collection<?>) other).size(), j = size();
            return ((Collection<?>) other).containsAll(this) && i == j;
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    abstract class PrivateNodeIterator<T> implements Iterator<T> {
        AvlNode<K, V> next;
        AvlNode<K, V> lastReturned;
        int expectedModCount;

        public PrivateNodeIterator(AvlNode<K, V> first) {
            expectedModCount = modCount;
            lastReturned = null;
            next = first;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final AvlNode<K, V> nextNode() {
            AvlNode<K, V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();

            next = successor(e);
            lastReturned = e;
            return e;
        }

        final AvlNode<K, V> prevNode() {
            AvlNode<K, V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            next = predecessor(e);
            lastReturned = e;
            return e;
        }

        public void remove() {
        	if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // deleted entries are replaced by their successors
            if (lastReturned.left != null && lastReturned.right != null)
                next = lastReturned;
            
            AvlGTree.this.remove(lastReturned.getKey());
            
            expectedModCount = modCount;
            lastReturned = null;
        }
    }

    final class NodeIterator extends PrivateNodeIterator<AvlNode<K, V>> {
        NodeIterator(AvlNode<K, V> first) {
            super(first);
        }

        public AvlNode<K, V> next() {
            return nextNode();
        }
    }

    final class ReverseNodeIterator extends PrivateNodeIterator<AvlNode<K, V>> {
        ReverseNodeIterator(AvlNode<K, V> last) {
            super(last);
        }

        public AvlNode<K, V> next() {
            return prevNode();
        }
    }

    final class EntryIterator extends PrivateNodeIterator<Map.Entry<K, V>> {
        EntryIterator(AvlNode<K, V> first) {
            super(first);
        }

        public Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    final class KeyIterator extends PrivateNodeIterator<K> {
        KeyIterator(AvlNode<K, V> first) {
            super(first);
        }

        public K next() {
            return nextNode().key;
        }
    }

    final class ValueIterator extends PrivateNodeIterator<V> {
        ValueIterator(AvlNode<K, V> first) {
            super(first);
        }

        public V next() {
            return nextNode().value;
        }
    }

    private final static boolean valEquals(Object o1, Object o2) {
        return (o1 == null ? o2 == null : o1.equals(o2));
    }

    @SuppressWarnings("hiding")
    final class SubMap<K, V> extends AbstractMap<K, V> implements
            SortedMap<K, V> {
        final AvlGTree<K, V> m;
        final K low;
        final K high;
        EntrySetView entrySetView = null;

        SubMap(AvlGTree<K, V> m, K low, K high) {
            if (low == null && high == null)
                throw new IllegalArgumentException();

            if (low != null && high != null)
                if (m.compare(low, high) > 0)
                    throw new IllegalArgumentException();

            this.m = m;
            this.low = low;
            this.high = high;
        }

        public Comparator<? super K> comparator() {
            return m.comparator();
        }

        public final V put(K key, V value) {
            if (!inRange(key))
                throw new IllegalArgumentException("key out of range");
            return m.put(key, value);
        }

        public final V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        public K firstKey() {
            return key(getFirstNode());
        }

        AvlNode<K, V> getFirstNode() {
            if (low == null) {
                AvlNode<K, V> first = m.getFirstNode();
                if (compare(first.getKey(), high) < 0)
                    return first;
                else
                    return null;
            } else {
                Iterator<AvlNode<K, V>> i = m.getNodeIterator();
                AvlNode<K, V> e;
                while (i.hasNext()) {
                    e = i.next();
                    int cmp = m.compare(e.getKey(), low);
                    if (cmp >= 0)
                        return e;
                }
                return null;
            }
        }

        public K lastKey() {
            return key(getLastNode());
        }

        final Entry<K, V> getLastNode() {
            if (high == null) {
                AvlNode<K, V> last = m.getLastNode();
                if (compare(last.getKey(), low) >= 0)
                    return last;
                else
                    return null;
            } else {
                Iterator<AvlNode<K, V>> i = m.getReverseNodeIterator();
                Entry<K, V> e;
                while (i.hasNext()) {
                    e = i.next();
                    int cmp = m.compare(e.getKey(), high);
                    if (cmp < 0)
                        return e;
                }
                return null;
            }
        }

        public Set<Map.Entry<K, V>> entrySet() {
            EntrySetView esv = entrySetView;
            return (esv != null) ? esv : (entrySetView = new EntrySetView());
        }

        public SortedMap<K, V> headMap(K toKey) {
            if (!inRange(toKey))
                throw new IllegalArgumentException();

            return new SubMap<K, V>(m, low, toKey);
        }

        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            if (!inRange(fromKey) || !inRange(toKey))
                throw new IllegalArgumentException();

            return new SubMap<K, V>(m, fromKey, toKey);
        }

        public SortedMap<K, V> tailMap(K fromKey) {
            if (!inRange(fromKey))
                throw new IllegalArgumentException();

            return new SubMap<K, V>(m, fromKey, high);
        }

        final boolean tooLow(Object key) {
            if (low != null) {
                int c = m.compare(key, low);
                if (c < 0)
                    return true;
            }
            return false;
        }

        final boolean tooHigh(Object key) {
            if (high != null) {
                int c = m.compare(key, high);
                if (c >= 0)
                    return true;
            }
            return false;
        }

        final boolean inRange(Object key) {
            return !tooLow(key) && !tooHigh(key);
        }

        public boolean equals(final Object other) {
            if (other == this)
                return true;
            else if (other instanceof SubMap) {
                @SuppressWarnings("unchecked")
                SubMap<?, ?> otherMap = (SubMap<?, ?>) other;
                return otherMap.m.equals(m) && low == null
                        ^ low.equals(otherMap.low) && high == null
                        ^ high.equals(otherMap.low);
            } else if (other instanceof Map) {
                Map<?, ?> otherMap = (Map<?, ?>) other;
                return entrySet().containsAll(otherMap.entrySet())
                        && otherMap.size() == size();
            } else
                return false;
        }

        class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    int expectedModCount = m.modCount;
                    AvlNode<K, V> next = getFirstNode();
                    @SuppressWarnings("unused")
					AvlNode<K, V> lastReturned = null;

                    public boolean hasNext() {
                        if (next != null)
                            return inRange(next.key);
                        else
                            return false;
                    }

                    public java.util.Map.Entry<K, V> next() {
                        AvlNode<K, V> e = next;
                        if (e == null)
                            throw new NoSuchElementException();
                        if (m.modCount != expectedModCount)
                            throw new ConcurrentModificationException();

                        next = successor(e);
                        if (next != null && !inRange(next.key))
                            next = null;

                        lastReturned = e;
                        return e;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                int size = 0;
                Iterator<Entry<K, V>> i = iterator();
                while (i.hasNext()) {
                    size++;
                    i.next();
                }
                return size;
            }

            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }
        }
    }

    public Node createXml(final Node parent) {
        final Element rootNode = parent.getOwnerDocument().createElement(
                "AvlGTree");
        rootNode.setAttribute("height",
                root == null ? "-1" : String.valueOf(root.getHeight()-1));
        rootNode.setAttribute("maxImbalance", String.valueOf(g));
        rootNode.setAttribute("cardinality", String.valueOf(size()));
        rootNode.appendChild(root == null ? parent.getOwnerDocument()
                .createElement("emptyChild") : root.buildXmlNode(rootNode));
        return rootNode;
    }
}
