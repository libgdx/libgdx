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

/**
 * An unordered map that uses int keys. Avoids the boxing that occurs with HashMap<Integer, T>.
 * @author Nathan Sweet
 * @author christop widulle
 */
public class IntHashMap<T> {
	public int size;

	Entry[] table;
	private float loadFactor;
	private int mask, capacity, threshold;
	private Entries entries;
	private Values values;
	private Keys keys;

	public IntHashMap () {
		this(16, 0.75f);
	}

	public IntHashMap (int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IntHashMap (int initialCapacity, float loadFactor) {
		if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large.");
		if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		if (loadFactor <= 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		capacity = MathUtils.nextPowerOfTwo(initialCapacity);
		this.loadFactor = loadFactor;
		threshold = (int)(capacity * loadFactor);
		table = new Entry[capacity];
		mask = capacity - 1;
	}

	public T put (int key, T value) {
		int index = key & mask;
		for (Entry e = table[index]; e != null; e = e.next) {
			if (e.key != key) continue;
			// Key already exists.
			Object oldValue = e.value;
			e.value = value;
			return (T)oldValue;
		}
		table[index] = new Entry(key, value, table[index]);
		if (size++ >= threshold) resize(2 * capacity);
		return null;
	}

	public T get (int key) {
		int index = key & mask;
		for (Entry e = table[index]; e != null; e = e.next)
			if (e.key == key) return (T)e.value;
		return null;
	}

	/**
	 * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is reused each
	 * time this method is called.
	 */
	public IterableIterator<Entry<T>> entries () {
		if (entries == null)
			entries = new Entries();
		else
			entries.reset();
		return entries;
	}

	/**
	 * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is reused each time
	 * this method is called.
	 */
	public IterableIterator<T> values () {
		if (values == null)
			values = new Values();
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
			keys = new Keys();
		else
			keys.reset();
		return keys;
	}

	/**
	 * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may be
	 * an expensive operation.
	 */
	public boolean containsValue (Object value, boolean identity) {
		Entry[] table = this.table;
		if (identity || value == null) {
			for (int i = table.length; i-- > 0;)
				for (Entry e = table[i]; e != null; e = e.next)
					if (e.value == value) return true;
		} else {
			for (int i = table.length; i-- > 0;)
				for (Entry e = table[i]; e != null; e = e.next)
					if (value.equals(e.value)) return true;
		}
		return false;
	}

	public boolean containsKey (int key) {
		int index = key & mask;
		for (Entry e = table[index]; e != null; e = e.next)
			if (e.key == key) return true;
		return false;
	}

	public T remove (int key) {
		int index = key & mask;
		Entry prev = table[index];
		Entry e = prev;
		while (e != null) {
			Entry next = e.next;
			if (e.key == key) {
				size--;
				if (prev == e)
					table[index] = next;
				else
					prev.next = next;
				return (T)e.value;
			}
			prev = e;
			e = next;
		}
		return null;
	}

	public void clear () {
		Entry[] table = this.table;
		for (int index = table.length; --index >= 0;)
			table[index] = null;
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
		Entry[] newTable = new Entry[newSize];
		Entry[] table = this.table;
		int bucketmask = newSize - 1;
		for (int i = 0; i < table.length; i++) {
			Entry entry = table[i];
			if (entry != null) {
				table[i] = null;
				do {
					Entry next = entry.next;
					int index = entry.key & bucketmask;
					entry.next = newTable[index];
					newTable[index] = entry;
					entry = next;
				} while (entry != null);
			}
		}
		this.table = newTable;
		capacity = newSize;
		threshold = (int)(newSize * loadFactor);
		mask = capacity - 1;
	}

	public String toString () {
		if (size == 0) return "[]";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		Entry[] table = this.table;
		int i = table.length;
		while (i-- > 0) {
			Entry e = table[i];
			if (e == null) continue;
			buffer.append(e.key);
			buffer.append('=');
			buffer.append(e.value);
			for (e = e.next; e != null; e = e.next) {
				buffer.append(", ");
				buffer.append(e.key);
				buffer.append('=');
				buffer.append(e.value);
			}
			break;
		}
		while (i-- > 0) {
			for (Entry e = table[i]; e != null; e = e.next) {
				buffer.append(", ");
				buffer.append(e.key);
				buffer.append('=');
				buffer.append(e.value);
			}
		}
		buffer.append(']');
		return buffer.toString();
	}

	class Entries implements IterableIterator {
		private int index = -1;
		Entry<T> entry = null;

		public boolean hasNext () {
			if (entry != null && entry.next != null) {
				entry = entry.next;
				return true;
			}
			while (true) {
				index++;
				if (index >= table.length) return false;
				if (table[index] == null) continue;
				entry = table[index];
				return true;
			}
		}

		public Object next () {
			return entry;
		}

		public void remove () {
			Entry prev = table[index];
			Entry e = prev;
			while (e != null) {
				Entry next = e.next;
				if (e == entry) {
					size--;
					if (prev == e)
						table[index] = next;
					else
						prev.next = next;
					return;
				}
				prev = e;
				e = next;
			}
		}

		void reset () {
			index = -1;
			entry = null;
		}

		public Iterator iterator () {
			return this;
		}
	}

	class Values extends Entries {
		public T next () {
			return entry.value;
		}
	}

	public class Keys {
		private int index = -1;
		Entry<T> entry = null;

		public boolean hasNext () {
			if (entry != null && entry.next != null) {
				entry = entry.next;
				return true;
			}
			while (true) {
				index++;
				if (index >= table.length) return false;
				if (table[index] == null) continue;
				entry = table[index];
				return true;
			}
		}

		public int next () {
			return entry.key;
		}

		public void remove () {
			Entry prev = table[index];
			Entry e = prev;
			while (e != null) {
				Entry next = e.next;
				if (e == entry) {
					size--;
					if (prev == e)
						table[index] = next;
					else
						prev.next = next;
					return;
				}
				prev = e;
				e = next;
			}
		}

		void reset () {
			index = -1;
			entry = null;
		}
	}

	/**
	 * An entry in the map. The value may be changed directly.
	 */
	static public class Entry<T> {
		public final int key;
		public T value;
		Entry next;

		Entry (int k, T v, Entry n) {
			key = k;
			value = v;
			next = n;
		}

		public String toString () {
			return key + "=" + value;
		}
	}
}
