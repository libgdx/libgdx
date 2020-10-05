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

/** An unordered map that uses identity comparison for the object keys. Null keys are not allowed. No allocation is done except
 * when growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration. Iteration is faster with OrderedSet and
 * OrderedMap.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Hashcodes are rehashed using Fibonacci
 * hashing, instead of the more common power-of-two mask, to better distribute poor hashCodes (see <a href=
 * "https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte
 * Skarupke's blog post</a>). Linear probing continues to work even when all hashCodes collide, just more slowly.
 * @author Tommy Ettinger
 * @author Nathan Sweet */
public class IdentityMap<K, V> extends ObjectMap<K, V> {
	/** Creates a new map with an initial capacity of 51 and a load factor of 0.8. */
	public IdentityMap () {
		super();
	}

	/** Creates a new map with a load factor of 0.8.
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public IdentityMap (int initialCapacity) {
		super(initialCapacity);
	}

	/** Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two. */
	public IdentityMap (int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/** Creates a new map identical to the specified map. */
	public IdentityMap (IdentityMap<K, V> map) {
		super(map);
	}

	protected int place (K item) {
		return (int)(System.identityHashCode(item) * 0x9E3779B97F4A7C15L >>> shift);
	}

	int locateKey (K key) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		K[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			K other = keyTable[i];
			if (other == null) return -(i + 1); // Empty space is available.
			if (other == key) return i; // Same key was found.
		}
	}

	public int hashCode () {
		int h = size;
		K[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			K key = keyTable[i];
			if (key != null) {
				h += System.identityHashCode(key);
				V value = valueTable[i];
				if (value != null) h += value.hashCode();
			}
		}
		return h;
	}
}
