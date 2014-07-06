/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.badlogic.gdx.utils.IntMap.Entries;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.IntMap.Values;
import com.badlogic.gdx.utils.IntMap.Keys;

/**
 * Wrapper class to treat {@link IntMap} objects as if they were immutable.
 * However, note that the indexed values could be modified if they are mutable.
 * 
 * @author David Saltares
 */
public class ImmutableIntMap<V> implements Iterable<IntMap.Entry<V>> {
	private final IntMap<V> map;
	private ImmutableEntries entries1, entries2;
	private ImmutableValues values1, values2;
	private ImmutableKeys keys1, keys2;
	
	public ImmutableIntMap(IntMap<V> map) {
		this.map = map;
	}
	
	public int size() {
		return map.size;
	}
	
	public V get(int key) {
		return map.get(key);
	}
	
	public V get(int key, V defaultValue) {
		return map.get(key, defaultValue);
	}
	
	public boolean containsValue(Object object, boolean identity) {
		return map.containsValue(object, identity);
	}
	
	public boolean containsKey(int key) {
		return map.containsKey(key);
	}
	
	public int findKey(Object object, boolean identity, int notFound) {
		return map.findKey(object, identity, notFound);
	}
	
	public String toString () {
		return map.toString();
	}
	
	/** Returns an iterator for the entries in the map. Calling {@link Entries#remove()} will throw an
	 * {@link GdxRuntimeException}. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public ImmutableEntries<V> entries() {
		if (entries1 == null) {
			entries1 = new ImmutableEntries(map.entries());
			entries2 = new ImmutableEntries(map.entries());
		}
		if (!entries1.valid) {
			entries1.reset();
			entries1.valid = true;
			entries2.valid = false;
			return entries1;
		}
		entries2.reset();
		entries2.valid = true;
		entries1.valid = false;
		return entries2;
	}
	
	/** Returns an iterator for the values in the map. Calling {@link Values#remove()} will throw an
	 * {@link GdxRuntimeException}. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public ImmutableValues<V> values() {
		if (values1 == null) {
			values1 = new ImmutableValues(map.values());
			values2 = new ImmutableValues(map.values());
		}
		if (!values1.valid) {
			values1.reset();
			values1.valid = true;
			values2.valid = false;
			return values1;
		}
		values2.reset();
		values2.valid = true;
		values1.valid = false;
		return values2;
	}
	
	/** Returns an iterator for the keys in the map. Calling {@link Keys#remove()} will throw an
	 * {@link GdxRuntimeException}. Note that the same iterator instance is returned each time
	 * this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration. */
	public ImmutableKeys keys() {
		if (keys1 == null) {
			keys1 = new ImmutableKeys(map.keys());
			keys2 = new ImmutableKeys(map.keys());
		}
		if (!keys1.valid) {
			keys1.reset();
			keys1.valid = true;
			keys2.valid = false;
			return keys1;
		}
		keys2.reset();
		keys2.valid = true;
		keys1.valid = false;
		return keys2;
	}
	
	public Iterator<Entry<V>> iterator () {
		return entries();
	}
	
	static public class ImmutableEntries<V> implements Iterable<Entry<V>>, Iterator<Entry<V>> {
		private final Entries<V> entries;
		
		boolean valid = true;
		
		public ImmutableEntries (Entries<V> entries) {
			this.entries = entries;
		}
		
		public void reset () {
			entries.reset();
		}

		void findNextIndex () {
			entries.findNextIndex();
		}

		public void remove () {
			throw new GdxRuntimeException("Remove not allowed.");
		}

		public boolean hasNext () {
			return entries.hasNext();
		}

		public Entry<V> next () {
			return entries.next();
		}

		public Iterator<Entry<V>> iterator () {
			return entries.iterator();
		}
	}
	
	static public class ImmutableValues<V> implements Iterable<V>, Iterator<V>  {
		private final Values<V> values;
		
		boolean valid = true;
		
		public ImmutableValues (Values<V> values) {
			this.values = values;
		}

		public void reset () {
			values.reset();
		}

		void findNextIndex () {
			values.findNextIndex();
		}
		
		public void remove () {
			throw new GdxRuntimeException("Remove not allowed.");
		}

		public boolean hasNext () {
			return values.hasNext();
		}

		public V next () {
			return values.next();
		}

		public Iterator<V> iterator () {
			return values.iterator();
		}
	}
	
	static public class ImmutableKeys  {
		private final Keys keys;
		
		boolean valid = true;
		
		public ImmutableKeys (Keys keys) {
			this.keys = keys;
		}
		
		public void reset () {
			keys.reset();
		}

		void findNextIndex () {
			keys.findNextIndex();
		}
		
		public int next () {
			return keys.next();
		}

		public IntArray toArray () {
			return keys.toArray();
		}
	}
}
