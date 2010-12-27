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

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.utils.ObjectMap.Entry;

/**
 * An unordered map that uses long keys. Uses open addressing and linear probing, which avoids allocation. In contrast,
 * java.util.HashMap uses chained entries and allocates on put. This class also avoids the boxing that occurs with HashMap<Long,
 * T>.<br>
 * <br>
 * Warning: Integer.MIN_VALUE cannot be used as a key.
 * @author Nathan Sweet
 */
public class LongMap<V> {
	static private final int EXTRA = 4;
	static private final int EMPTY = Integer.MIN_VALUE;

	public int size;

	long[] keyTable;
	V[] valueTable;
	private float loadFactor;
	private int mask, capacity, threshold;
	private Entries entries;
	private Values values;
	private Keys keys;

	public LongMap () {
		this(16, 0.75f);
	}

	public LongMap (int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public LongMap (int initialCapacity, float loadFactor) {
		if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large.");
		if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		if (loadFactor <= 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		capacity = MathUtils.nextPowerOfTwo(initialCapacity);
		this.loadFactor = loadFactor;
		threshold = (int)(capacity * loadFactor);
		keyTable = new long[capacity + EXTRA];
		for (int i = keyTable.length - 1; i >= 0; i--)
			keyTable[i] = EMPTY;
		valueTable = (V[])new Object[capacity + EXTRA];
		mask = capacity - 1;
	}

	public V put (long key, V value) {
		long[] keyTable = this.keyTable;
		int index = (int)(key & mask);
		while (true) {
			long existingKey = keyTable[index];
			if (existingKey == EMPTY || existingKey == key) break;
			index++;
		}
		keyTable[index] = key;
		V oldValue = valueTable[index];
		valueTable[index] = value;
		if (size++ >= threshold || keyTable[keyTable.length - 1] != EMPTY) resize(capacity << 1);
		return oldValue;
	}

	public void putAll (LongMap<V> map) {
		ensureCapacity(map.size); // Conservative in case map has keys already in this map.
		for (Entry<V> entry : map.entries())
			put(entry.key, entry.value);
	}

	public V get (long key) {
		long[] keyTable = this.keyTable;
		int index = (int)(key & mask);
		while (true) {
			long existingKey = keyTable[index];
			if (existingKey == EMPTY) return null;
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
			long[] keyTable = this.keyTable;
			for (int i = keyTable.length; i-- > 0;)
				if (keyTable[i] != EMPTY && valueTable[i] == null) return true;
		} else if (identity) {
			for (int i = valueTable.length; i-- > 0;)
				if (valueTable[i] == value) return true;
		} else {
			for (int i = valueTable.length; i-- > 0;)
				if (value.equals(valueTable[i])) return true;
		}
		return false;
	}

	public boolean containsKey (long key) {
		long[] keyTable = this.keyTable;
		int index = (int)(key & mask);
		while (true) {
			long existingKey = keyTable[index];
			if (existingKey == EMPTY) return false;
			if (existingKey == key) return true;
			index++;
		}
	}

	public V remove (long key) {
		long[] keyTable = this.keyTable;
		int index = (int)(key & mask);
		while (true) {
			long existingKey = keyTable[index];
			if (existingKey == EMPTY) return null;
			if (existingKey == key) {
				keyTable[index] = EMPTY;
				V value = valueTable[index];
				valueTable[index] = null;
				return value;
			}
			index++;
		}
	}

	public void clear () {
		long[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = keyTable.length; i-- > 0;) {
			if (keyTable[i] != EMPTY) {
				keyTable[i] = EMPTY;
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
		long[] newKeyTable = new long[newSize];
		for (int i = newSize - 1; i >= 0; i--)
			newKeyTable[i] = EMPTY;
		V[] newValueTable = (V[])new Object[newSize];

		long[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			long key = keyTable[i];
			if (key != EMPTY) {
				int index = (int)(key & newMask);
				while (true) {
					if (newKeyTable[index] == EMPTY) break;
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

		long[] keyTable = this.keyTable;
		int i = keyTable.length;
		while (i-- > 0) {
			long key = keyTable[i];
			if (key == EMPTY) continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
			break;
		}
		while (i-- > 0) {
			long key = keyTable[i];
			if (key == EMPTY) continue;
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
	public Entries<V> entries () {
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
	public Keys keys () {
		if (keys == null)
			keys = new Keys(this);
		else
			keys.reset();
		return keys;
	}

	static public class Entry<V> {
		public long key;
		public V value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static public class Entries<V> implements Iterable<Entry<V>>, Iterator<Entry<V>> {
		Entry<V> entry = new Entry();
		int index = -1;
		private final LongMap map;

		public Entries (LongMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			long[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != EMPTY) return true;
			return false;
		}

		public Entry<V> next () {
			entry.key = map.keyTable[index];
			entry.value = (V)map.valueTable[index];
			return entry;
		}

		public void remove () {
			map.keyTable[index] = EMPTY;
			map.valueTable[index] = null;
			map.size--;
			index--;
		}

		public void reset () {
			index = -1;
		}

		public Iterator<Entry<V>> iterator () {
			return this;
		}
	}

	static public class Values<V> implements Iterable<V>, Iterator<V> {
		int index = -1;
		private final LongMap map;

		public Values (LongMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			long[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != EMPTY) return true;
			return false;
		}

		public V next () {
			return (V)map.valueTable[index];
		}

		public void remove () {
			map.keyTable[index] = EMPTY;
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

	static public class Keys {
		int index = -1;
		private final LongMap map;

		public Keys (LongMap map) {
			this.map = map;
		}

		public boolean hasNext () {
			long[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++index < n;)
				if (keyTable[index] != EMPTY) return true;
			return false;
		}

		public long next () {
			return map.keyTable[index];
		}

		public void remove () {
			map.keyTable[index] = EMPTY;
			map.valueTable[index] = null;
			map.size--;
			index--;
		}

		public void reset () {
			index = -1;
		}

		public LongArray toArray () {
			LongArray array = new LongArray(map.size);
			while (hasNext())
				array.add(next());
			return array;
		}
	}
}
