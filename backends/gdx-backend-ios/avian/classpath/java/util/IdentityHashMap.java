/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class IdentityHashMap<K, V> implements Map<K, V> {
  private final HashMap<K, V> map;

  public IdentityHashMap(int capacity) {
    map = new HashMap(capacity, new MyHelper());
  }

  public IdentityHashMap() {
    this(0);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public int size() {
    return map.size();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public V get(Object key) {
    return map.get(key);
  }

  public V put(K key, V value) {
    return map.put(key, value);
  }

  public void putAll(Map<? extends K,? extends V> elts) {
    map.putAll(elts);
  }

  public V remove(Object key) {
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

  private static class MyHelper<K, V>
    extends HashMap.MyHelper<K, V>
  {
    public int hash(K a) {
      return (a == null ? 0 : System.identityHashCode(a));
    }

    public boolean equal(K a, K b) {
      return a == b;
    }    
  }
}
