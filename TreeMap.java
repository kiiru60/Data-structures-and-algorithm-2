import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of a sorted map using a binary search tree.
 */
public class TreeMap<K,V> extends AbstractSortedMap<K,V> {

  public class Node<K, V> extends MapEntry<K, V> {
    Node<K, V> left = null, right = null, parent = null;
    public Node(Node<K, V> p) {
      super(null, null);
      parent = p;
    }

    public Node<K, V> getLeft() {return left;}
    public Node<K, V> getRight() {return right;}
    public Node<K, V> getParent() {return parent;}

    public void setEntry(K k, V v) {
      setKey(k);
      setValue(v);
    }

    public void setLeft(Node<K, V> n) {left = n;}
    public void setRight(Node<K, V> n) {right = n;}
    public void setParent(Node<K, V> n) {parent = n;}

    public boolean isExternal() {
      return left == null && right == null;
    }

    public boolean isInternal() {
      return !isExternal();
    }

    public void expandExternal(K k, V v) {
      setEntry(k, v);
      left = new Node<>(this);
      right = new Node<>(this);
    }
  }

  Node<K, V> root = new Node<>(null);
  int size = 0;

  /** Constructs an empty map using the natural ordering of keys. */
  public TreeMap() {
    super();                  // the AbstractSortedMap constructor
  }

  /**
   * Constructs an empty map using the given comparator to order keys.
   * @param comp comparator defining the order of keys in the map
   */
  public TreeMap(Comparator<K> comp) {
    super(comp);              // the AbstractSortedMap constructor
  }

  /**
   * Returns the number of entries in the map.
   * @return number of entries in the map
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * Returns the value associated with the specified key, or null if no such entry exists.
   * @param key  the key whose associated value is to be returned
   * @return the associated value, or null if no such entry exists
   */
  @Override
  public V get(K key) throws IllegalArgumentException {
    checkKey(key);                          // may throw IllegalArgumentException
    return treeSearch(key).getValue();
  }

  /**
   * Associates the given value with the given key. If an entry with
   * the key was already in the map, this replaced the previous value
   * with the new one and returns the old value. Otherwise, a new
   * entry is added and null is returned.
   * @param key    key with which the specified value is to be associated
   * @param value  value to be associated with the specified key
   * @return the previous value associated with the key (or null, if no such entry)
   */
  @Override
  public V put(K key, V value) throws IllegalArgumentException {
    checkKey(key);                          // may throw IllegalArgumentException
    Node<K,V> n = treeSearch(key);
    if(n.isExternal()) {                    // key is new
      n.expandExternal(key, value);
      size++;
      return null;
    }
    else {                                // replacing existing key
      return n.setValue(value);
    }
  }

  /**
   * Removes the entry with the specified key, if present, and returns
   * its associated value. Otherwise does nothing and returns null.
   * @param key  the key whose entry is to be removed from the map
   * @return the previous value associated with the removed key, or null if no such entry exists
   */
  @Override
  public V remove(K key) throws IllegalArgumentException {
    checkKey(key);                          // may throw IllegalArgumentException
    Node<K, V> p = treeSearch(key);
    if(p.isExternal()) {                    // key not found
      return null;
    }
    else {
      V old = p.getValue();
      if (p.getLeft().isInternal() && p.getRight().isInternal()) { // both children are internal
        Node<K, V> z = treeMin(p.getRight());
        p.setEntry(z.getKey(), z.getValue());
        p = z;
      } // now p has at most one child that is an internal node
      Node<K, V> leaf = p.getLeft().isExternal() ? p.getLeft() : p.getRight();
      remove(leaf);
      remove(p);                            // sib is promoted in p's place
      size--;
      return old;
    }
  }

  // Support for iteration
  /**
   * Returns an iterable collection of all key-value entries of the map.
   *
   * @return iterable collection of the map's entries
   */
  @Override
  public Iterable<Entry<K,V>> entrySet() {
    ArrayList<Entry<K, V>> l = new ArrayList<>();
    inorder(root, l);
    return l;
  }

  /**
   * Returns the position in p's subtree having the given key (or else the terminal leaf).
   * @param key  a target key
   * @return Node holding key, or last node reached during search
   */
  private Node<K,V> treeSearch(K key) {
    Node<K, V> n = root;
    while(n.isInternal()) {
      int comp = compare(key, n);
      if (comp == 0)
        return n;                          // key found; return its position
      else if (comp < 0)
        n = n.getLeft();
      else
        n = n.getRight();
    }
    return n;
  }

  /**
   * Returns Node with the minimal key in the subtree rooted at Position p.
   * @param n  a Node of the tree serving as root of a subtree
   * @return Node with minimal key in subtree
   */
  private Node<K, V> treeMin(Node<K, V> n) {
    while(n.getLeft() != null)
      n = n.getLeft();
    return n = n.getParent();              // we want the parent of the leaf
  }

  /**
   * Removes a node from the tree
   * @param n Node to be removed
   */
  private void remove(Node<K, V> n) {
    Node<K, V> child = n.getLeft() != null ? n.getLeft() : n.getRight();
    if(child != null)
      child.setParent(n.getParent());  // child's grandparent becomes its parent
    if(n == root)
      root = child;                       // child becomes root
    else {
      Node<K, V> parent = n.getParent();
      if(n == parent.getLeft())
        parent.setLeft(child);
      else
        parent.setRight(child);
    }
    n.setEntry(null, null);                // help garbage collection
    n.setLeft(null);
    n.setRight(null);
    n.setParent(null);
  }

  /**
   * Recursive inorder traversal. Nodes are collected as entries along the way.
   * @param n Root of subtree
   * @param l List of entries to put all nodes into
   */
  private void inorder(Node<K, V> n, ArrayList<Entry<K, V>> l) {
    if(n.isExternal()) return;
    inorder(n.getLeft(), l);
    l.add(n);
    inorder(n.getRight(), l);
  }
}
