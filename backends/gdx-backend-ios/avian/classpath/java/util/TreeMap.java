/* Copyright (c) 2009-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class TreeMap<K,V> implements Map<K,V> {
  private TreeSet<MyEntry<K,V>> set;

  public TreeMap(final Comparator<K> comparator) {
    set = new TreeSet(new Comparator<MyEntry<K,V>>() {
        public int compare(MyEntry<K,V> a, MyEntry<K,V> b) {
          return comparator.compare(a.key, b.key);
        }
      });
  }

  public TreeMap() {
    this(new Comparator<K>() {
        public int compare(K a, K b) {
          return ((Comparable) a).compareTo(b);
        }
    });
  }

  public String toString() {
    return Collections.toString(this);
  }

  public V get(Object key) {
    MyEntry<K,V> e = set.find(new MyEntry(key, null));
    return e == null ? null : e.value;
  }

  public V put(K key, V value) {
    MyEntry<K,V> e = set.addAndReplace(new MyEntry(key, value));
    return e == null ? null : e.value;
  }

  public void putAll(Map<? extends K,? extends V> elts) {
    for (Map.Entry<? extends K, ? extends V> entry : elts.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }
    
  public V remove(Object key) {
    MyEntry<K,V> e = set.removeAndReturn(new MyEntry(key, null));
    return e == null ? null : e.value;
  }

  public void clear() {
    set.clear();
  }

  public int size() {
    return set.size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean containsKey(Object key) {
    return set.contains(new MyEntry(key, null));
  }

  private boolean equal(Object a, Object b) {
    return a == null ? b == null : a.equals(b);
  }

  public boolean containsValue(Object value) {
    for (V v: values()) {
      if (equal(v, value)) {
        return true;
      }
    }
    return false;
  }

  public Set<Entry<K, V>> entrySet() {
    return (Set<Entry<K, V>>) (Set) set;
  }

  public Set<K> keySet() {
    return new KeySet();
  }

  public Collection<V> values() {
    return new Values();
  }

  private static class MyEntry<K,V> implements Entry<K,V> {
    public final K key;
    public V value;

    public MyEntry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }

    public V getValue() {
      return value;
    }

    public V setValue(V value) {
      V old = this.value;
      this.value = value;
      return old;
    }
    
  }

  private class KeySet implements Set<K> {
    public int size() {
      return TreeMap.this.size();
    }

    public boolean isEmpty() {
      return TreeMap.this.isEmpty();
    }

    public boolean contains(Object key) {
      return containsKey(key);
    }

    public boolean add(K key) {
      return set.addAndReplace(new MyEntry(key, null)) != null;
    }

    public boolean addAll(Collection<? extends K> collection) {
      boolean change = false;
      for (K k: collection) if (add(k)) change = true;
      return change;
    }

    public boolean remove(Object key) {
      return set.removeAndReturn(new MyEntry(key, null)) != null;
    }

    public Object[] toArray() {
      return toArray(new Object[size()]);      
    }

    public <T> T[] toArray(T[] array) {
      return Collections.toArray(this, array);      
    }

    public void clear() {
      TreeMap.this.clear();
    }

    public Iterator<K> iterator() {
      return new Collections.KeyIterator(set.iterator());
    }
  }

  private class Values implements Collection<V> {
    public int size() {
      return TreeMap.this.size();
    }

    public boolean isEmpty() {
      return TreeMap.this.isEmpty();
    }

    public boolean contains(Object value) {
      return containsValue(value);
    }

    public boolean add(V value) {
      throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends V> collection) {
      throw new UnsupportedOperationException();      
    }

    public boolean remove(Object value) {
      throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
      return toArray(new Object[size()]);      
    }

    public <T> T[] toArray(T[] array) {
      return Collections.toArray(this, array);      
    }

    public void clear() {
      TreeMap.this.clear();
    }

    public Iterator<V> iterator() {
      return new Collections.ValueIterator(set.iterator());
    }
  }
}
