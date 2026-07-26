/*******************************************************************************
 * Copyright 2026 See AUTHORS file.
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

/** An unordered set that uses identity comparison for its keys. Null keys are not allowed. No allocation is done except when
 * growing the table size.
 * <p>
 * This class performs fast contains and remove (typically O(1), worst case O(n) but that is rare in practice). Add may be
 * slightly slower, depending on hash collisions. Hashcodes are rehashed to reduce collisions and the need to resize. Load factors
 * greater than 0.91 greatly increase the chances to resize to the next higher POT size.
 * <p>
 * Unordered sets and maps are not designed to provide especially fast iteration. Iteration is faster with OrderedSet and
 * OrderedMap.
 * <p>
 * This implementation uses linear probing with the backward shift algorithm for removal. Linear probing continues to work even
 * when all hashCodes collide, just more slowly.
 * @author Tommy Ettinger
 * @author Nathan Sweet */
public class IdentitySet<T> extends ObjectSet<T> {
	/** Creates a new set with an initial capacity of 51 and a load factor of 0.8. */
	public IdentitySet () {
		super();
	}

	/** Creates a new set with a load factor of 0.8.
	 * @param initialCapacity The backing array size is initialCapacity / loadFactor, increased to the next power of two. */
	public IdentitySet (int initialCapacity) {
		super(initialCapacity);
	}

	/** Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 * @param initialCapacity The backing array size is initialCapacity / loadFactor, increased to the next power of two. */
	public IdentitySet (int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/** Creates a new set identical to the specified set. */
	public IdentitySet (IdentitySet<? extends T> set) {
		super(set);
	}

	protected int place (T item) {
		return System.identityHashCode(item) >>> shift;
	}

	int locateKey (T key) {
		if (key == null) throw new IllegalArgumentException("key cannot be null.");
		T[] keyTable = this.keyTable;
		for (int i = place(key);; i = i + 1 & mask) {
			T other = keyTable[i];
			if (other == null) return -(i + 1); // Empty space is available.
			if (other == key) return i; // Same key was found.
		}
	}

	public int hashCode () {
		int h = size;
		T[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			T key = keyTable[i];
			if (key != null) h += System.identityHashCode(key);
		}
		return h;
	}

	static public <T> IdentitySet<T> with (T... array) {
		IdentitySet<T> set = new IdentitySet<T>(array.length);
		set.addAll(array);
		return set;
	}
}
