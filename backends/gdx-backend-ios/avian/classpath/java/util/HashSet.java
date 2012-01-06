/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class HashSet<T> extends AbstractSet<T> implements Set<T> {
  private static final Object Value = new Object();

  private final HashMap<T, Object> map;

  public HashSet(Collection<? extends T> c) {
    map = new HashMap(c.size());
    addAll(c);
  }

  public HashSet(int capacity) {
    map = new HashMap(capacity);
  }

  public HashSet() {
    this(0);
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean contains(Object element) {
    return map.containsKey(element);
  }

  public boolean add(T element) {
    return map.put(element, Value) != Value;
  }

  public boolean addAll(Collection<? extends T> collection) {
    boolean change = false;
    for (T t: collection) if (add(t)) change = true;
    return change;
  }

  public boolean remove(Object element) {
    return map.remove(element) != Value;
  }

  public void clear() {
    map.clear();
  }

  public Iterator<T> iterator() {
    return new MyIterator(map.iterator());
  }

  public String toString() {
    return Collections.toString(this);
  }

  private static class MyIterator<T> implements Iterator<T> {
    private final Iterator<Map.Entry<T, Object>> it;

    public MyIterator(Iterator<Map.Entry<T, Object>> it) {
      this.it = it;
    }

    public T next() {
      return it.next().getKey();
    }

    public boolean hasNext() {
      return it.hasNext();
    }

    public void remove() {
      it.remove();
    }
  }
}
