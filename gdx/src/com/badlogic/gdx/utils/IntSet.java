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

import com.badlogic.gdx.math.MathUtils;

import java.util.NoSuchElementException;

/**
 * An unordered set where the items are unboxed ints. This implementation uses linear probing with th backward-shift algorithm for
 * removal, and finds space for keys using Fibonacci hashing instead of the more-common power-of-two mask.
 * Null keys are not allowed. No allocation is done except when growing the table size.
 * <br>
 * This set uses Fibonacci hashing to help distribute what may be very bad hashCode() results across the
 * whole capacity. See <a href="https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte Skarupke's blog post</a>
 * for more information on Fibonacci hashing. It uses linear probing to resolve collisions, which is far from the academically
 * optimal algorithm, but performs considerably better in practice than most alternatives, and combined with Fibonacci hashing, it
 * can handle "normal" generated hashCode() implementations, and not just theoretically optimal hashing functions. Even if all
 * hashCode()s this is given collide, it will still work, just slowly; the older libGDX implementation using cuckoo hashing would
 * crash with an OutOfMemoryError with under 50 collisions.
 * <br>
 * This set performs very fast contains and remove (typically O(1), worst case O(n) due to occasional probing, but still very
 * fast). Add may be a bit slower, depending on hash collisions, but this data structure is somewhat collision-resistant.
 * Load factors greater than 0.91 greatly increase the chances the set will have to rehash to the next higher POT size.
 * Memory usage is excellent, and the aforementioned collision-resistance helps avoid too much capacity resizing.
 * <br>
 * Iteration won't be as fast here as with OrderedSet and OrderedMap.
 *
 * @author Tommy Ettinger
 * @author Nathan Sweet
 */
public class IntSet {
	public int size;

	private int[] keyTable;
	boolean hasZeroValue;

	private float loadFactor;
	private int shift, mask, threshold;

	private IntSetIterator iterator1, iterator2;

	/**
	 * Creates a new set with an initial capacity of 51 and a load factor of 0.8.
	 */
	public IntSet () {
		this(51, 0.8f);
	}

	/**
	 * Creates a new set with a load factor of 0.8.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public IntSet (int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/**
	 * Creates a new set with the specified initial capacity and load factor. This set will hold initialCapacity items before
	 * growing the backing table.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public IntSet (int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
		if (loadFactor <= 0f || loadFactor >= 1f)
			throw new IllegalArgumentException("loadFactor must be > 0 and < 1: " + loadFactor);
		initialCapacity = MathUtils.nextPowerOfTwo((int)Math.ceil(initialCapacity / loadFactor));
		if (initialCapacity > 1 << 30)
			throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);

		this.loadFactor = loadFactor;

		threshold = (int)(initialCapacity * loadFactor);
		mask = initialCapacity - 1;
		shift = Long.numberOfLeadingZeros(mask);

		keyTable = new int[initialCapacity];
	}

	/**
	 * Creates a new set identical to the specified set.
	 */
	public IntSet (IntSet set) {
		this((int)(set.keyTable.length * set.loadFactor), set.loadFactor);
		System.arraycopy(set.keyTable, 0, keyTable, 0, set.keyTable.length);
		size = set.size;
		hasZeroValue = set.hasZeroValue;
	}

	private int place (final int item) {
		// shift is always greater than 32, less than 64
		return (int)(item * 0x9E3779B97F4A7C15L >>> shift);
	}

	private int locateKey (final int key) {
		return locateKey(key, place(key));
	}

	private int locateKey (final int key, final int placement) {
		for (int i = placement; ; i = i + 1 & mask) {
			// empty space is available
			if (keyTable[i] == 0) {
				return -1;
			}
			if (key == (keyTable[i])) {
				return i;
			}
		}
	}

	/**
	 * Returns true if the key was not already in the set.
	 */
	public boolean add (int key) {
		if (key == 0) {
			if (hasZeroValue)
				return false;
			hasZeroValue = true;
			size++;
			return true;
		}

		int b = place(key);
		int loc = locateKey(key, b);
		// an identical key already exists
		if (loc != -1) {
			return false;
		}
		final int[] keyTable = this.keyTable;

		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break
			if (keyTable[i] == 0) {
				keyTable[i] = key;
				
				if (++size >= threshold) {
					resize(keyTable.length << 1);
				}
				return true;
			}
		}
	}

	public void addAll (IntArray array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll (IntArray array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException(
				"offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (int... array) {
		addAll(array, 0, array.length);
	}

	public void addAll (int[] array, int offset, int length) {
		ensureCapacity(length);
		for (int i = offset, n = i + length; i < n; i++)
			add(array[i]);
	}

	public void addAll (IntSet set) {
		ensureCapacity(set.size);
		if (set.hasZeroValue)
			add(0);
		final int[] keyTable = set.keyTable;
		int k;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			if ((k = keyTable[i]) != 0)
				add(k);
		}

//		ensureCapacity(set.size);
//		IntSetIterator iterator = set.iterator();
//		while (iterator.hasNext)
//			add(iterator.next());
	}

	/**
	 * Skips checks for existing keys.
	 */
	private void addResize (int key) {
		if (key == 0) {
			if (!hasZeroValue && size++ >= threshold)
				resize(keyTable.length << 1);
			hasZeroValue = true;
			return;
		}

		final int[] keyTable = this.keyTable;
		int b = place(key);
		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break (resize is later)
			if (keyTable[i] == 0) {
				keyTable[i] = key;

				if (++size >= threshold) {
					resize(keyTable.length << 1);
				}
				return;
			}
		}
	}

	/**
	 * Returns true if the key was removed.
	 */
	public boolean remove (int key) {
		if (key == 0) {
			if (!hasZeroValue)
				return false;
			hasZeroValue = false;
			size--;
			return true;
		}

		int loc = locateKey(key);
		if (loc == -1) {
			return false;
		}
		while ((key = keyTable[loc + 1 & mask]) != 0 && (loc + 1 & mask) != place(key)) {
			keyTable[loc] = key;
			++loc;
		}
		keyTable[loc] = 0;
		--size;
		return true;
	}

	/**
	 * Returns true if the set has one or more items.
	 */
	public boolean notEmpty () {
		return size > 0;
	}

	/**
	 * Returns true if the set is empty.
	 */
	public boolean isEmpty () {
		return size == 0;
	}

	/**
	 * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
	 * done. If the set contains more items than the specified capacity, the next highest power of two capacity is used instead.
	 */
	public void shrink (int maximumCapacity) {
		if (maximumCapacity < 0)
			throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
		if (size > maximumCapacity)
			maximumCapacity = size;
		if (keyTable.length <= maximumCapacity)
			return;
		resize(MathUtils.nextPowerOfTwo(maximumCapacity));
	}

	/**
	 * Clears the set and reduces the size of the backing arrays to be the specified capacity if they are larger.
	 */
	public void clear (int maximumCapacity) {
		if (keyTable.length <= maximumCapacity) {
			clear();
			return;
		}
		hasZeroValue = false;
		size = 0;
		resize(maximumCapacity);
	}

	public void clear () {
		if (size == 0)
			return;
		final int[] keyTable = this.keyTable;
		for (int i = keyTable.length; i > 0; ) {
			keyTable[--i] = 0;
		}
		size = 0;
		hasZeroValue = false;
	}

	public boolean contains (int key) {
		if (key == 0)
			return hasZeroValue;
		// inlined locateKey()
		for (int i = (int)(key * 0x9E3779B97F4A7C15L >>> shift); ; i = i + 1 & mask) {
			// empty space is available
			if (keyTable[i] == 0) {
				return false;
			}
			if (key == (keyTable[i])) {
				return true;
			}
		}
	}

	public int first () {
		if (hasZeroValue)
			return 0;
		int[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++)
			if (keyTable[i] != 0)
				return keyTable[i];
		throw new IllegalStateException("IntSet is empty.");
	}

	/**
	 * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity (int additionalCapacity) {
		if (additionalCapacity < 0)
			throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold)
			resize(MathUtils.nextPowerOfTwo((int)Math.ceil(sizeNeeded / loadFactor)));
	}

	private void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		final int[] oldKeyTable = keyTable;

		keyTable = new int[newSize];
		
		int oldSize = size;
		size = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				int key = oldKeyTable[i];
				if (key != 0)
					addResize(key);
			}
		}
	}

	public int hashCode () {
		int h = size;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			if (keyTable[i] != 0) {
				h += keyTable[i];
			}
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (!(obj instanceof IntSet))
			return false;
		IntSet other = (IntSet)obj;
		if (other.size != size)
			return false;
		if (other.hasZeroValue != hasZeroValue)
			return false;
		int[] keyTable = this.keyTable;
		for (int i = 0, n = keyTable.length; i < n; i++)
			if (keyTable[i] != 0 && !other.contains(keyTable[i]))
				return false;
		return true;
	}

	public String toString () {
		if (size == 0)
			return "[]";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		buffer.append('[');
		int[] keyTable = this.keyTable;
		int i = keyTable.length;
		if (hasZeroValue)
			buffer.append("0");
		else {
			while (i-- > 0) {
				int key = keyTable[i];
				if (key == 0)
					continue;
				buffer.append(key);
				break;
			}
		}
		while (i-- > 0) {
			int key = keyTable[i];
			if (key == 0)
				continue;
			buffer.append(", ");
			buffer.append(key);
		}
		buffer.append(']');
		return buffer.toString();
	}

	/**
	 * Returns an iterator for the keys in the set. Remove is supported.
	 * <p>
	 * If {@link Collections#allocateIterators} is false, the same iterator instance is returned each time this method is called.
	 * Use the {@link IntSetIterator} constructor for nested or multithreaded iteration.
	 */
	public IntSetIterator iterator () {
		if (Collections.allocateIterators)
			return new IntSetIterator(this);
		if (iterator1 == null) {
			iterator1 = new IntSetIterator(this);
			iterator2 = new IntSetIterator(this);
		}
		if (!iterator1.valid) {
			iterator1.reset();
			iterator1.valid = true;
			iterator2.valid = false;
			return iterator1;
		}
		iterator2.reset();
		iterator2.valid = true;
		iterator1.valid = false;
		return iterator2;
	}

	static public IntSet with (int... array) {
		IntSet set = new IntSet();
		set.addAll(array);
		return set;
	}

	static public class IntSetIterator {
		static final int INDEX_ILLEGAL = -2;
		static final int INDEX_ZERO = -1;

		public boolean hasNext;

		final IntSet set;
		int nextIndex, currentIndex;
		boolean valid = true;

		public IntSetIterator (IntSet set) {
			this.set = set;
			reset();
		}

		public void reset () {
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			if (set.hasZeroValue)
				hasNext = true;
			else
				findNextIndex();
		}

		void findNextIndex () {
			hasNext = false;
			int[] keyTable = set.keyTable;
			for (int n = keyTable.length; ++nextIndex < n; ) {
				if (keyTable[nextIndex] != 0) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove () {
			if (currentIndex == INDEX_ZERO && set.hasZeroValue) {
				set.hasZeroValue = false;
			} else if (currentIndex < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else {
				set.keyTable[currentIndex] = 0;

				int[] keyTable = set.keyTable;
				final int mask = set.mask;
				int loc = currentIndex;
				int key;
				while ((key = keyTable[loc + 1 & mask]) != 0 && (loc + 1 & mask) != set.place(key)) {
					keyTable[loc] = key;
					++loc;
				}
				if(loc != currentIndex) --nextIndex;
				keyTable[loc] = 0;
			}
			currentIndex = INDEX_ILLEGAL;
			set.size--;
		}

		public int next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			int key = nextIndex == INDEX_ZERO ? 0 : set.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		/**
		 * Returns a new array containing the remaining keys.
		 */
		public IntArray toArray () {
			IntArray array = new IntArray(true, set.size);
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
