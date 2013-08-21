/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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

/** An {@link ObjectMap} that also stores keys in an {@link Array} using the insertion order. There is some additional overhead for
 * put and remove. Iteration over the {@link #entries()}, {@link #keys()}, and {@link #values()} is ordered. Keys can also be
 * accessed and the order changed using {@link #orderedKeys()}.
 * @author Nathan Sweet */
public class OrderedMap<K, V> extends ObjectMap<K, V> {
	final Array<K> keys;

	public OrderedMap () {
		super();
		keys = new Array();
	}

	public OrderedMap (int initialCapacity) {
		super(initialCapacity);
		keys = new Array(initialCapacity);
	}

	public OrderedMap (int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		keys = new Array(initialCapacity);
	}

	public V put (K key, V value) {
		if (!containsKey(key)) keys.add(key);
		return super.put(key, value);
	}

	public V remove (K key) {
		keys.removeValue(key, false);
		return super.remove(key);
	}

	public void clear (int maximumCapacity) {
		keys.clear();
		super.clear(maximumCapacity);
	}

	public void clear () {
		keys.clear();
		super.clear();
	}

	public Array<K> orderedKeys () {
		return keys;
	}

	public Entries<K, V> entries () {
		return new Entries(this) {
			void advance () {
				nextIndex++;
				hasNext = nextIndex < map.size;
			}

			public Entry next () {
				entry.key = keys.get(nextIndex);
				entry.value = map.get(entry.key);
				advance();
				return entry;
			}

			public void remove () {
				map.remove(entry.key);
			}
		};
	}

	public Keys<K> keys () {
		return new Keys(this) {
			void advance () {
				nextIndex++;
				hasNext = nextIndex < map.size;
			}

			public K next () {
				K key = keys.get(nextIndex);
				advance();
				return key;
			}

			public void remove () {
				map.remove(keys.get(nextIndex - 1));
			}
		};
	}

	public Values<V> values () {
		return new Values(this) {
			void advance () {
				nextIndex++;
				hasNext = nextIndex < map.size;
			}

			public V next () {
				V value = (V)map.get(keys.get(nextIndex));
				advance();
				return value;
			}

			public void remove () {
				map.remove(keys.get(nextIndex - 1));
			}
		};
	}

	public String toString () {
		if (size == 0) return "{}";
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('{');
		Array<K> keys = this.keys;
		for (int i = 0, n = keys.size; i < n; i++) {
			K key = keys.get(i);
			if (i > 0) buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(get(key));
		}
		buffer.append('}');
		return buffer.toString();
	}
}
