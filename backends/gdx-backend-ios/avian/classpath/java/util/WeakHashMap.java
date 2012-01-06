/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class WeakHashMap<K, V> implements Map<K, V> {
  private final HashMap<K, V> map;
  private final ReferenceQueue queue;

  public WeakHashMap(int capacity) {
    map = new HashMap(capacity, new MyHelper());
    queue = new ReferenceQueue();
  }

  public WeakHashMap() {
    this(0);
  }

  private void poll() {
    for (MyCell<K, V> c = (MyCell<K, V>) queue.poll();
         c != null;
         c = (MyCell<K, V>) queue.poll())
    {
      map.remove(c);
    }
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public int size() {
    return map.size();
  }

  public boolean containsKey(Object key) {
    poll();
    return map.containsKey(key);
  }

  public boolean containsValue(Object  value) {
    poll();
    return map.containsValue(value);
  }

  public V get(Object key) {
    poll();
    return map.get(key);
  }

  public V put(K key, V value) {
    poll();
    return map.put(key, value);
  }

  public void putAll(Map<? extends K,? extends V> elts) {
    map.putAll(elts);
  }

  public V remove(Object key) {
    poll();
    return map.remove(key);
  }

  public void clear() {
    map.clear();
  }

  public Set<Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public Collection<V> values() {
    return map.values();
  }

  private static class MyCell<K, V>
    extends WeakReference<K>
    implements HashMap.Cell<K, V>
  {
    public V value;
    public HashMap.Cell<K, V> next;
    public int hashCode;

    public MyCell(K key, ReferenceQueue queue, V value,
                  HashMap.Cell<K, V> next, int hashCode)
    {
      super(key, queue);
      this.value = value;
      this.next = next;
      this.hashCode = hashCode;
    }

    public K getKey() {
      return get();
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

  private class MyHelper<K, V>
    extends HashMap.MyHelper<K, V>
  {
    public HashMap.Cell<K, V> make(K key, V value, HashMap.Cell<K, V> next) {
      return new MyCell(key, queue, value, next, hash(key));
    }
  }
}
