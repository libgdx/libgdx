/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class Hashtable<K, V> implements Map<K, V> {
  private final HashMap<K, V> map;

  public Hashtable(int capacity) {
    map = new HashMap(capacity);
  }

  public Hashtable() {
    this(0);
  }

  public Hashtable(Map<? extends K,? extends V> m) {
    this(m.size());
    for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  public synchronized String toString() {
    return map.toString();
  }

  public synchronized boolean isEmpty() {
    return map.isEmpty();
  }

  public synchronized int size() {
    return map.size();
  }

  public synchronized boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public synchronized boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public synchronized V get(Object key) {
    return map.get(key);
  }

  public synchronized V put(K key, V value) {
    return map.put(key, value);
  }

  public synchronized void putAll(Map<? extends K,? extends V> elts) {
    map.putAll(elts);
  }

  public synchronized V remove(Object key) {
    return map.remove(key);
  }

  public synchronized void clear() {
    map.clear();
  }

  public Enumeration<K> keys() {
    return new Collections.IteratorEnumeration(keySet().iterator());
  }

  public Enumeration<V> elements() {
    return new Collections.IteratorEnumeration(values().iterator());
  }

  public Set<Entry<K, V>> entrySet() {
    return new Collections.SynchronizedSet(this, map.entrySet());
  }

  public Set<K> keySet() {
    return new Collections.SynchronizedSet(this, map.keySet());
  }

  public Collection<V> values() {
    return new Collections.SynchronizedCollection(this, map.values());
  }

}
