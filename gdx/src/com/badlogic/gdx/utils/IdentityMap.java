/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.utils;

import java.util.Iterator;

import com.badlogic.gdx.utils.ObjectMap.Entry;

/**
 * An unordered map that uses int keys. Uses open addressing and linear probing, which avoids allocation. In contrast,
 * java.util.HashMap uses chained entries and allocates on put.<br>
 * <br>
 * Warning: null cannot be used as a key.
 * @author Nathan Sweet
 */
public class IdentityMap<K, V> {
	static private final int EXTRA = 4;

	public int size;

	K[] keyTable;
	V[] valueTable;
	private float loadFactor;
	private int mask, capacity, threshold;
	private Entries entries;
	private Values values;
	private Keys keys;

	public IdentityMap () {
		this(16, 0.75f);
	}

	public IdentityMap (int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IdentityMap (int initialCapacity, float loadFactor) {
		if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large.");
		if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		if (loadFactor <= 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		capacity = MathUtils.nextPowerOfTwo(initialCapacity);
		this.loadFactor = loadFactor;
		threshold = (int)(capacity * loadFactor);
		keyTable = (K[])new Object[capacity + EXTRA];
		valueTable = (V[])new Object[capacity + EXTRA];
		mask = capacity - 1;
	}

	public V put (K key, V value) {
		K[] keyTable = this.keyTable;
		int index = System.identityHashCode(key) & mask;
		while (true) {
			K existingKey = keyTable[index];
			if (existingKey == null || existingKey == key) break;
			index++;
		}
		keyTable[index] = key;
		V oldValue = valueTable[index];
		valueTable[index] = value;
		if (size++ >= threshold || keyTable[keyTable.length - 1] != null) resize(capacity << 1);
		return oldValue;
	}

	public void putAll (IdentityMap<K, V> map) {
		ensureCapacity(map.size); // Conservative in case map has keys already in this map.
		for (Entry<K, V> entry : map.entries())
			put(entry.key, entry.value);
	}

	public V get (K key) {
		K[] keyTable = this.keyTable;
		int index = System.identityHashCode(key) & mask;
		while (true) {
			K existingKey = keyTable[index];
			if (existingKey == null) return null;
			if (existingKey == key) return valueTable[index];
			index++;
		}
	}

	/**
	 * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may be
	 * an expensive operation.
	 */
	public boolean containsValue (Object value, boolean identity) {
		V[] valueTable = this.valueTable;
		if (value == null) {
			K[] keyTable = this.keyTable;
			for (int i = keyTable.length; i-- > 0;)
				if (keyTable[i] != null && valueTable[i] == null) return true;
		} else if (identity) {
			for (int i = valueTable.length; i-- > 0;)
				if (valueTable[i] == value) return true;
		} else {
			for (int i = valueTable.length; i-- > 0;)
				if (value.equals(valueTable[i])) return true;
		}
		return false;
	}

	public boolean containsKey (K key) {
		K[] keyTable = this.keyTable;
		int index = System.identityHashCode(key) & mask;
		while (true) {
			K existingKey = keyTable[index];
			if (existingKey == null) return false;
			if (existingKey == key) return true;
			index++;
		}
	}

	public V remove (K key) {
		K[] keyTable = this.keyTable;
		int index = System.identityHashCode(key) & mask;
		while (true) {
			K existingKey = keyTable[index];
			if (existingKey == null) return null;
			if (existingKey == key) {
				keyTable[index] = null;
				V value = valueTable[index];
				valueTable[index] = null;
				return value;
			}
			index++;
		}
	}

	public void clear () {
		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = keyTable.length; i-- > 0;) {
			if (keyTable[i] != null) {
				keyTable[i] = null;
				valueTable[i] = null;
			}
		}
		size = 0;
	}

	/**
	 * Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold) resize(MathUtils.nextPowerOfTwo(sizeNeeded));
	}

	private void resize (int newSize) {
		int newMask = newSize - 1;
		mask = newMask;
		capacity = newSize;
		threshold = (int)(newSize * loadFactor);

		newSize += EXTRA; // Allow the last hash bucket to have extra room for linear probing.
		K[] newKeyTable = (K[])new Object[newSize];
		V[] newValueTable = (V[])new Object[newSize];

		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			K key = keyTable[i];
			if (key != null) {
				int index = System.identityHashCode(key) & newMask;
				while (true) {
					if (newKeyTable[index] == null) break;
					index++;
				}
				if (index == newSize) {
					// Very unlikely, but possible for the last bucket to have more than EXTRA keys hashed to it.
					resize(capacity << 1);
					return;
				}
				newKeyTable[index] = key;
				newValueTable[index] = valueTable[i];
			}
		}
		this.keyTable = newKeyTable;
		this.valueTable = newValueTable;
	}

	public String toString () {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');

		K[] keyTable = this.keyTable;
		int i = keyTable.length;
		while (i-- > 0) {
			K key = keyTable[i];
			if (key == null) continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
			break;
		}
		while (i-- > 0) {
			K key = keyTable[i];
			if (key == null) continue;
			buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	/**
	 * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is reused each
	 * time this method is called.
	 */
	public Entries<K, V> entries () {
		if (entries == null)
			entries = new Entries(this);
		else
			entries.reset();
		return entries;
	}

	/**
	 * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is reused each time
	 * this method is called.
	 */
	public Values<V> values () {
		if (values == null)
			values = new Values(this);
		else
			values.reset();
		return values;
	}

	/**
	 * Returns an iterator for the keys in the map. Remove is supported. Note that the same iterator instance is reused each time
	 * this method is called.
	 */
	public Keys<K> keys () {
		if (keys == null)
			keys = new Keys(this);
		else
			keys.reset();
		return keys;
	}

	static public class Entry<K, V> {
		public K key;
		public V value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static public class Entries<K, V> implements Iterable<Entry<K, V>>, Iterator<Entry<K, V>> {
		Entry<K, V> entry = new Entry();
		int index = -1;
		private final IdentityMap map;

		public Entries (IdentityMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			Object[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != null) return true;
			return false;
		}

		public Entry<K, V> next () {
			entry.key = (K)map.keyTable[index];
			entry.value = (V)map.valueTable[index];
			return entry;
		}

		public void remove () {
			map.keyTable[index] = null;
			map.valueTable[index] = null;
			map.size--;
			index--;
		}

		public void reset () {
			index = -1;
		}

		public Iterator<Entry<K, V>> iterator () {
			return this;
		}
	}

	static public class Values<V> implements Iterable<V>, Iterator<V> {
		int index = -1;
		private final IdentityMap map;

		public Values (IdentityMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			Object[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != null) return true;
			return false;
		}

		public V next () {
			return (V)map.valueTable[index];
		}

		public void remove () {
			map.keyTable[index] = null;
			map.valueTable[index] = null;
			map.size--;
			index--;
		}

		public void reset () {
			index = -1;
		}

		public Iterator<V> iterator () {
			return this;
		}

		public Array<V> toArray () {
			Array array = new Array(map.size);
			while (hasNext())
				array.add(next());
			return array;
		}
	}

	static public class Keys<K> implements Iterable<K>, Iterator<K> {
		int index = -1;
		private final IdentityMap map;

		public Keys (IdentityMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			Object[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != null) return true;
			return false;
		}

		public K next () {
			return (K)map.keyTable[index];
		}

		public void remove () {
			map.keyTable[index] = null;
			map.valueTable[index] = null;
			map.size--;
			index--;
		}

		public void reset () {
			index = -1;
		}

		public Iterator<K> iterator () {
			return this;
		}

		public Array<K> toArray () {
			Array array = new Array(map.size);
			while (hasNext())
				array.add(next());
			return array;
		}
	}
}
