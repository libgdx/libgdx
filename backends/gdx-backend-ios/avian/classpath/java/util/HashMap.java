/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class HashMap<K, V> implements Map<K, V> {
  private static final int MinimumCapacity = 16;

  private int size;
  private Cell[] array;
  private final Helper helper;

  public HashMap(int capacity, Helper<K, V> helper) {
    if (capacity > 0) {
      array = new Cell[nextPowerOfTwo(capacity)];
    }
    this.helper = helper;
  }

  public HashMap(int capacity) {
    this(capacity, new MyHelper());
  }

  public HashMap() {
    this(0);
  }

  public HashMap(Map<K, V> map) {
    this(map.size());
    for (Map.Entry<K, V> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public String toString() {
    return Collections.toString(this);
  }

  private static int nextPowerOfTwo(int n) {
    int r = 1;
    while (r < n) r <<= 1;
    return r;
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public int size() {
    return size;
  }

  private void grow() {
    if (array == null || size >= array.length * 2) {
      resize(array == null ? MinimumCapacity : array.length * 2);
    }
  }

  private void shrink() {
    if (array.length / 2 >= MinimumCapacity && size <= array.length / 3) {
      resize(array.length / 2);
    }
  }

  private void resize(int capacity) {
    Cell<K, V>[] newArray = null;
    if (capacity != 0) {
      capacity = nextPowerOfTwo(capacity);
      if (array != null && array.length == capacity) {
        return;
      }

      newArray = new Cell[capacity];
      if (array != null) {
        for (int i = 0; i < array.length; ++i) {
          Cell<K, V> next;
          for (Cell<K, V> c = array[i]; c != null; c = next) {
            next = c.next();
            int index = c.hashCode() & (capacity - 1);
            c.setNext(newArray[index]);
            newArray[index] = c;
          }
        }
      }
    }
    array = newArray;
  }

  private Cell<K, V> find(Object key) {
    if (array != null) {
      int index = helper.hash(key) & (array.length - 1);
      for (Cell<K, V> c = array[index]; c != null; c = c.next()) {
        if (helper.equal(key, c.getKey())) {
          return c;
        }
      }
    }

    return null;
  }

  private void insert(Cell<K, V> cell) {
    ++ size;

    grow();

    int index = cell.hashCode() & (array.length - 1);
    cell.setNext(array[index]);
    array[index] = cell;
  }

  public void remove(Cell<K, V> cell) {
    int index = cell.hashCode() & (array.length - 1);
    Cell<K, V> p = null;
    for (Cell<K, V> c = array[index]; c != null; c = c.next()) {
      if (c == cell) {
        if (p == null) {
          array[index] = c.next();
        } else {
          p.setNext(c.next());
        }
        -- size;
        break;
      }
    }

    shrink();
  }

  private Cell<K, V> putCell(K key, V value) {
    Cell<K, V> c = find(key);
    if (c == null) {
      insert(helper.make(key, value, null));
    } else {
      c.setValue(value);
    }
    return c;
  }

  public boolean containsKey(Object key) {
    return find(key) != null;
  }

  public boolean containsValue(Object value) {
    if (array != null) {
      for (int i = 0; i < array.length; ++i) {
        for (Cell<K, V> c = array[i]; c != null; c = c.next()) {
          if (helper.equal(value, c.getValue())) {
            return true;
          }
        }
      }
    }  
	
    return false;
  }

  public V get(Object key) {
    Cell<K, V> c = find(key);
    return (c == null ? null : c.getValue());
  }

  public Cell<K, V> removeCell(Object key) {
    Cell<K, V> old = null;
    if (array != null) {
      int index = helper.hash(key) & (array.length - 1);
      Cell<K, V> p = null;
      for (Cell<K, V> c = array[index]; c != null; c = c.next()) {
        if (helper.equal(key, c.getKey())) {
          old = c;
          if (p == null) {
            array[index] = c.next();
          } else {
            p.setNext(c.next());
          }
          -- size;
          break;
        }
        p = c;
      }

      shrink();
    }
    return old;
  }

  public V put(K key, V value) {
    Cell<K, V> c = find(key);
    if (c == null) {
      insert(helper.make(key, value, null));
      return null;
    } else {
      V old = c.getValue();
      c.setValue(value);
      return old;
    }
  }

  public void putAll(Map<? extends K,? extends V> elts) {
    for (Map.Entry<? extends K, ? extends V> entry : elts.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public V remove(Object key) {
    Cell<K, V> c = removeCell((K)key);
    return (c == null ? null : c.getValue());
  }

  public void clear() {
    array = null;
    size = 0;
  }

  public Set<Entry<K, V>> entrySet() {
    return new EntrySet();
  }

  public Set<K> keySet() {
    return new KeySet();
  }

  public Collection<V> values() {
    return new Values();
  }

  Iterator<Entry<K, V>> iterator() {
    return new MyIterator();
  }

  interface Cell<K, V> extends Entry<K, V> {
    public HashMap.Cell<K, V> next();

    public void setNext(HashMap.Cell<K, V> next);
  }

  interface Helper<K, V> {
    public Cell<K, V> make(K key, V value, Cell<K, V> next);
    
    public int hash(K key);

    public boolean equal(K a, K b);
  }

  private static class MyCell<K, V> implements Cell<K, V> {
    public final K key;
    public V value;
    public Cell<K, V> next;
    public int hashCode;

    public MyCell(K key, V value, Cell<K, V> next, int hashCode) {
      this.key = key;
      this.value = value;
      this.next = next;
      this.hashCode = hashCode;
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

    public HashMap.Cell<K, V> next() {
      return next;
    }

    public void setNext(HashMap.Cell<K, V> next) {
      this.next = next;
    }

    public int hashCode() {
      return hashCode;
    }
  }

  static class MyHelper<K, V> implements Helper<K, V> {
    public Cell<K, V> make(K key, V value, Cell<K, V> next) {
      return new MyCell(key, value, next, hash(key));
    }

    public int hash(K a) {
      return (a == null ? 0 : a.hashCode());
    }

    public boolean equal(K a, K b) {
      return (a == null && b == null) || (a != null && a.equals(b));
    }
  }

  private class EntrySet implements Set<Entry<K, V>> {
    public int size() {
      return HashMap.this.size();
    }

    public boolean isEmpty() {
      return HashMap.this.isEmpty();
    }

    public boolean contains(Object o) {
      return (o instanceof Entry<?,?>)
        && containsKey(((Entry<?,?>)o).getKey());
    }

    public boolean add(Entry<K, V> e) {
      return putCell(e.getKey(), e.getValue()) != null;
    }

    public boolean addAll(Collection<? extends Entry<K, V>> collection) {
      boolean change = false;
      for (Entry<K, V> e: collection) if (add(e)) change = true;
      return change;
    }

    public boolean remove(Object o) {
      return (o instanceof Entry<?,?>) && remove((Entry<?,?>)o);
    }

    public boolean remove(Entry<K, V> e) {
      return removeCell(e.getKey()) != null;
    }

    public Object[] toArray() {
      return toArray(new Object[size()]);      
    }

    public <T> T[] toArray(T[] array) {
      return Collections.toArray(this, array);      
    }

    public void clear() {
      HashMap.this.clear();
    }

    public Iterator<Entry<K, V>> iterator() {
      return new MyIterator();
    }
  }

  private class KeySet implements Set<K> {
    public int size() {
      return HashMap.this.size();
    }

    public boolean isEmpty() {
      return HashMap.this.isEmpty();
    }

    public boolean contains(Object key) {
      return containsKey(key);
    }

    public boolean add(K key) {
      return putCell(key, null) != null;
    }

    public boolean addAll(Collection<? extends K> collection) {
      boolean change = false;
      for (K k: collection) if (add(k)) change = true;
      return change;
    }

    public boolean remove(Object key) {
      return removeCell(key) != null;
    }

    public Object[] toArray() {
      return toArray(new Object[size()]);      
    }

    public <T> T[] toArray(T[] array) {
      return Collections.toArray(this, array);      
    }

    public void clear() {
      HashMap.this.clear();
    }

    public Iterator<K> iterator() {
      return new Collections.KeyIterator(new MyIterator());
    }
  }

  private class Values implements Collection<V> {
    public int size() {
      return HashMap.this.size();
    }

    public boolean isEmpty() {
      return HashMap.this.isEmpty();
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
      HashMap.this.clear();
    }

    public Iterator<V> iterator() {
      return new Collections.ValueIterator(new MyIterator());
    }
  }

  private class MyIterator implements Iterator<Entry<K, V>> {
    private int currentIndex = -1;
    private int nextIndex = -1;
    private Cell<K, V> previousCell;
    private Cell<K, V> currentCell;
    private Cell<K, V> nextCell;

    public MyIterator() {
      hasNext();
    }

    public Entry<K, V> next() {
      if (hasNext()) {
        if (currentCell != null) {
          if (currentCell.next() != null) {
            previousCell = currentCell;
          } else {
            previousCell = null;
          }
        }

        currentCell = nextCell;
        currentIndex = nextIndex;

        nextCell = nextCell.next();

        return currentCell;
      } else {
        throw new NoSuchElementException();
      }
    }

    public boolean hasNext() {
      if (array != null) {
        while (nextCell == null && ++ nextIndex < array.length) {
          if (array[nextIndex] != null) {
            nextCell = array[nextIndex];
            return true;
          }
        }
      }
      return nextCell != null;
    }

    public void remove() {
      if (currentCell != null) {
        if (previousCell == null) {
          array[currentIndex] = currentCell.next();
        } else {
          previousCell.setNext(currentCell.next());
          if (previousCell.next() == null) {
            previousCell = null;
          }
        }
        currentCell = null;
        -- size;
      } else {
        throw new IllegalStateException();
      }
    }
  }
}
