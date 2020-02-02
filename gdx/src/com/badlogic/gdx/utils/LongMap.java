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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An unordered map where the keys are unboxed longs and values are objects. This implementation uses linear probing with the
 * backward-shift algorithm for removal, and finds space for keys using Fibonacci hashing instead of the more-common power-of-two
 * mask. Null keys are not allowed. No allocation is done except when growing the table size.
 * <br>
 * This map uses Fibonacci hashing to help distribute what may be very bad hashCode() results across the
 * whole capacity. See <a href="https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">Malte Skarupke's blog post</a>
 * for more information on Fibonacci hashing. It uses linear probing to resolve collisions, which is far from the academically
 * optimal algorithm, but performs considerably better in practice than most alternatives, and combined with Fibonacci hashing, it
 * can handle "normal" generated hashCode() implementations, and not just theoretically optimal hashing functions. Even if all
 * hashCode()s this is given collide, it will still work, just slowly; the older libGDX implementation using cuckoo hashing would
 * crash with an OutOfMemoryError with under 50 collisions.
 * <br>
 * This map performs very fast contains and remove (typically O(1), worst case O(n) due to occasional probing, but still very
 * fast). Add may be a bit slower, depending on hash collisions, but this data structure is somewhat collision-resistant.
 * Load factors greater than 0.91 greatly increase the chances the map will have to rehash to the next higher POT size.
 * Memory usage is excellent, and the aforementioned collision-resistance helps avoid too much capacity resizing.
 * <br>
 * The <a href="http://codecapsule.com/2013/11/17/robin-hood-hashing-backward-shift-deletion/">backward-shift algorithm</a>
 * used during removal apparently is key to the good performance of this implementation, even though this doesn't use Robin Hood
 * hashing; the performance of {@link #remove(long)} has improved considerably over the previous libGDX version.
 * <br>
 * Iteration won't be as fast here as with OrderedSet and OrderedMap.
 *
 * @author Tommy Ettinger
 * @author Nathan Sweet
 */
public class LongMap<V> implements Iterable<LongMap.Entry<V>> {
	public int size;

	private long[] keyTable;
	private V[] valueTable;

	private V zeroValue;
	private boolean hasZeroValue;

	private float loadFactor;
	private int threshold;
	/**
	 * Used by {@link #place(long)} to bit-shift the upper bits of a {@code long} into a usable range (less than or
	 * equal to {@link #mask}, greater than or equal to 0). If you're setting it in a subclass, this shift can be
	 * negative, which is a convenient way to match the number of bits in mask; if mask is a 7-bit number, then a shift
	 * of -7 will correctly shift the upper 7 bits into the lowest 7 positions. If using what this class sets, shift
	 * will be greater than 32 and less than 64; if you use this shift with an int, it will still correctly move the
	 * upper bits of an int to the lower bits, thanks to Java's implicit modulus on shifts.
	 * <br>
	 * You can also use {@link #mask} to mask the low bits of a number, which may be faster for some hashCode()s, if you
	 * reimplement {@link #place(long)}.
	 */
	private int shift;
	/**
	 * The bitmask used to contain hashCode()s to the indices that can be fit into the key array this uses. This should
	 * always be all-1-bits in its low positions; that is, it must be a power of two minus 1. If you subclass and change
	 * {@link #place(long)}, you may want to use this instead of {@link #shift} to isolate usable bits of a hash.
	 */
	private int mask;

	private Entries entries1, entries2;
	private Values values1, values2;
	private Keys keys1, keys2;

	/**
	 * Creates a new map with an initial capacity of 51 and a load factor of 0.8.
	 */
	public LongMap () {
		this(51, 0.8f);
	}

	/**
	 * Creates a new map with a load factor of 0.8.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public LongMap (int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/**
	 * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public LongMap (int initialCapacity, float loadFactor) {
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

		keyTable = new long[initialCapacity];
		valueTable = (V[])new Object[initialCapacity];
	}

	/**
	 * Creates a new map identical to the specified map.
	 */
	public LongMap (LongMap<? extends V> map) {
		this((int)(map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
		zeroValue = map.zeroValue;
		hasZeroValue = map.hasZeroValue;
	}

	/**
	 * Finds an array index between 0 and {@link #mask}, both inclusive, corresponding to the hash code of {@code item}.
	 * By default, this uses "Fibonacci Hashing" on the long {@code item} with mixed upper and lower bits; this multiplies
	 * {@code item} by a long constant (2 to the 64, divided by the golden ratio) and shifts the high-quality
	 * uppermost bits into the lowest positions so they can be used as array indices. The multiplication by a long may
	 * be somewhat slow on GWT, but it will be correct across all platforms and won't lose precision. Using Fibonacci
	 * Hashing allows even very poor hashCode() implementations, such as those that only differ in their upper bits, to
	 * work in a hash table without heavy collision rates. It has known problems when all or most hashCode()s are
	 * multiples of larger Fibonacci numbers; see <a href="https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">this blog post by Malte Skarupke</a>
	 * for more details. In the unlikely event that most of your hashCode()s are Fibonacci numbers, you can subclass
	 * this to change this method, which is a one-liner in this form:
	 * {@code return (int) ((item ^ item >>> 32) * 0x9E3779B97F4A7C15L >>> shift);}
	 * <br>
	 * This can be overridden by subclasses, which you may want to do if your key type needs special consideration for
	 * its hash (such as if you use arrays as keys, which still requires that the arrays are not modified). Subclasses
	 * that don't need the collision decrease of Fibonacci Hashing (assuming the keys are well-distributed) may do
	 * fine with a simple implementation:
	 * {@code return ((int)(item ^ item >>> 32) & mask);}
	 *
	 * @param item a key that this method will use to get a hashed position
	 * @return an int between 0 and {@link #mask}, both inclusive
	 */
	private int place (final long item) {
		// shift is always greater than 32, less than 64
		return (int)((item ^ item >>> 32) * 0x9E3779B97F4A7C15L >>> shift);
	}

	private int locateKey (final long key) {
		return locateKey(key, place(key));
	}

	/**
	 * Given a key and its initial placement to try in an array, this finds the actual location of the key in the array
	 * if it is present, or -1 if the key is not present. This can be overridden if a subclass needs to compare for
	 * equality differently than just by using == with int keys, but only within the same package.
	 *
	 * @param key       a K key that will be checked for equality if a similar-seeming key is found
	 * @param placement as calculated by {@link #place(long)}, almost always with {@code place(key)}
	 * @return the location in the key array of key, if found, or -1 if it was not found.
	 */
	private int locateKey (final long key, final int placement) {
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

	public V put (long key, V value) {
		if (key == 0) {
			V oldValue = zeroValue;
			zeroValue = value;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
			}
			return oldValue;
		}

		int b = place(key);
		int loc = locateKey(key, b);
		// an identical key already exists
		if (loc != -1) {
			V tv = valueTable[loc];
			valueTable[loc] = value;
			return tv;
		}
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;

		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break
			if (keyTable[i] == 0) {
				keyTable[i] = key;
				valueTable[i] = value;

				if (++size >= threshold) {
					resize(keyTable.length << 1);
				}
				return null;
			}
		}
		// never reached
	}

	public void putAll (LongMap<? extends V> map) {
		ensureCapacity(map.size);
		if (map.hasZeroValue)
			put(0, map.zeroValue);
		final long[] keyTable = map.keyTable;
		final V[] valueTable = map.valueTable;
		long k;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			if ((k = keyTable[i]) != 0)
				put(k, valueTable[i]);
		}
	}
	// the old version; I think the new way avoids a little work
//	   ensureCapacity(map.size);
//		for (Entry<? extends V> entry : map.entries())
//			put(entry.key, entry.value);

	/**
	 * Skips checks for existing keys.
	 */
	private void putResize (long key, V value) {
		if (key == 0) {
			zeroValue = value;
			if (!hasZeroValue) {
				hasZeroValue = true;
				size++;
			}
			return;
		}
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		int b = place(key);
		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break (resize is later)
			if (keyTable[i] == 0) {
				keyTable[i] = key;
				valueTable[i] = value;

				if (++size >= threshold) {
					resize(keyTable.length << 1);
				}
				return;
			}
		}
	}

	public V get (long key) {
		if (key == 0) {
			if (!hasZeroValue)
				return null;
			return zeroValue;
		}
		final int placement = place(key);
		for (int i = placement; ; i = i + 1 & mask) {
			// empty space is available
			if (keyTable[i] == 0) {
				return null;
			}
			if (key == (keyTable[i])) {
				return valueTable[i];
			}
		}
	}

	public V get (long key, V defaultValue) {
		if (key == 0) {
			if (!hasZeroValue)
				return defaultValue;
			return zeroValue;
		}
		final int placement = place(key);
		for (int i = placement; ; i = i + 1 & mask) {
			// empty space is available
			if (keyTable[i] == 0) {
				return defaultValue;
			}
			if (key == (keyTable[i])) {
				return valueTable[i];
			}
		}
	}

	public V remove (long key) {
		if (key == 0) {
			if (!hasZeroValue)
				return null;
			V oldValue = zeroValue;
			zeroValue = null;
			hasZeroValue = false;
			size--;
			return oldValue;
		}

		int loc = locateKey(key);
		if (loc == -1) {
			return null;
		}
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		V oldValue = valueTable[loc];
		while ((key = keyTable[loc + 1 & mask]) != 0L && (loc + 1 & mask) != place(key)) {
			keyTable[loc] = key;
			valueTable[loc] = valueTable[++loc & mask];
		}
		keyTable[loc] = 0;
		valueTable[loc] = null;
		--size;
		return oldValue;
	}

	/**
	 * Returns true if the map has one or more items.
	 */
	public boolean notEmpty () {
		return size > 0;
	}

	/**
	 * Returns true if the map is empty.
	 */
	public boolean isEmpty () {
		return size == 0;
	}

	/**
	 * Reduces the size of the backing arrays to be the specified capacity or less. If the capacity is already less, nothing is
	 * done. If the map contains more items than the specified capacity, the next highest power of two capacity is used instead.
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
	 * Clears the map and reduces the size of the backing arrays to be the specified capacity if they are larger.
	 */
	public void clear (int maximumCapacity) {
		if (keyTable.length <= maximumCapacity) {
			clear();
			return;
		}
		zeroValue = null;
		hasZeroValue = false;
		size = 0;
		resize(maximumCapacity);
	}

	public void clear () {
		if (size == 0)
			return;
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		for (int i = keyTable.length; i > 0; ) {
			keyTable[--i] = 0;
			valueTable[i] = null;
		}
		size = 0;
		zeroValue = null;
		hasZeroValue = false;
	}

	/**
	 * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
	 * be an expensive operation.
	 *
	 * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
	 *                 {@link #equals(Object)}.
	 */
	public boolean containsValue (Object value, boolean identity) {
		final V[] valueTable = this.valueTable;
		if (value == null) {
			if (hasZeroValue && zeroValue == null)
				return true;
			long[] keyTable = this.keyTable;
			for (int i = valueTable.length; i-- > 0; )
				if (keyTable[i] != 0 && valueTable[i] == null)
					return true;
		} else if (identity) {
			if (value == zeroValue)
				return true;
			for (int i = valueTable.length; i-- > 0; )
				if (valueTable[i] == value)
					return true;
		} else {
			if (hasZeroValue && value.equals(zeroValue))
				return true;
			for (int i = valueTable.length; i-- > 0; )
				if (value.equals(valueTable[i]))
					return true;
		}
		return false;

	}

	public boolean containsKey (long key) {
		if (key == 0)
			return hasZeroValue;
		return locateKey(key) != -1;
	}

	/**
	 * Returns the key for the specified value, or <tt>notFound</tt> if it is not in the map. Note this traverses the entire map
	 * and compares every value, which may be an expensive operation.
	 *
	 * @param identity If true, uses == to compare the specified value with values in the map. If false, uses
	 *                 {@link #equals(Object)}.
	 */
	public long findKey (Object value, boolean identity, long notFound) {
		final V[] valueTable = this.valueTable;
		if (value == null) {
			if (hasZeroValue && zeroValue == null)
				return 0;
			long[] keyTable = this.keyTable;
			for (int i = valueTable.length; i-- > 0; )
				if (keyTable[i] != 0 && valueTable[i] == null)
					return keyTable[i];
		} else if (identity) {
			if (value == zeroValue)
				return 0;
			for (int i = valueTable.length; i-- > 0; )
				if (valueTable[i] == value)
					return keyTable[i];
		} else {
			if (hasZeroValue && value.equals(zeroValue))
				return 0;
			for (int i = valueTable.length; i-- > 0; )
				if (value.equals(valueTable[i]))
					return keyTable[i];
		}
		return notFound;
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

		final long[] oldKeyTable = keyTable;
		final V[] oldValueTable = valueTable;

		keyTable = new long[newSize];
		valueTable = (V[])new Object[newSize];

		int oldSize = size;
		size = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				long key = oldKeyTable[i];
				if (key != 0)
					putResize(key, oldValueTable[i]);
			}
		}
	}

	public int hashCode () {
		int h = size;
		if (hasZeroValue && zeroValue != null) {
			h = zeroValue.hashCode();
		}
		long[] keyTable = this.keyTable;
		V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			long key = keyTable[i];
			if (key != 0) {
				h ^= key ^ key >>> 32;
				V value = valueTable[i];
				if (value != null) {
					h += value.hashCode();
				}
			}
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof LongMap))
			return false;
		LongMap other = (LongMap)obj;
		if (other.size != size)
			return false;
		if (other.hasZeroValue != hasZeroValue)
			return false;
		if (hasZeroValue) {
			if (other.zeroValue == null) {
				if (zeroValue != null)
					return false;
			} else {
				if (!other.zeroValue.equals(zeroValue))
					return false;
			}
		}
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			long key = keyTable[i];
			if (key != 0) {
				V value = valueTable[i];
				if (value == null) {
					if (other.get(key, ObjectMap.dummy) != null)
						return false;
				} else {
					if (!value.equals(other.get(key)))
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * Uses == for comparison of each value.
	 */
	public boolean equalsIdentity (Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof LongMap))
			return false;
		LongMap other = (LongMap)obj;
		if (other.size != size)
			return false;
		if (other.hasZeroValue != hasZeroValue)
			return false;
		if (hasZeroValue && zeroValue != other.zeroValue)
			return false;
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			long key = keyTable[i];
			if (key != 0 && valueTable[i] != other.get(key, ObjectMap.dummy))
				return false;
		}
		return true;
	}

	public String toString () {
		if (size == 0)
			return "[]";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		buffer.append('[');
		final long[] keyTable = this.keyTable;
		final V[] valueTable = this.valueTable;
		int i = keyTable.length;
		if (hasZeroValue) {
			buffer.append("0=");
			buffer.append(zeroValue);
		} else {
			while (i-- > 0) {
				long key = keyTable[i];
				if (key == 0)
					continue;
				buffer.append(key);
				buffer.append('=');
				buffer.append(valueTable[i]);
				break;
			}
		}
		while (i-- > 0) {
			long key = keyTable[i];
			if (key == 0)
				continue;
			buffer.append(", ");
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}

	public Iterator<Entry<V>> iterator () {
		return entries();
	}

	/**
	 * Returns an iterator for the entries in the map. Remove is supported.
	 * <p>
	 * If {@link Collections#allocateIterators} is false, the same iterator instance is returned each time this method is called.
	 * Use the {@link Entries} constructor for nested or multithreaded iteration.
	 */
	public Entries<V> entries () {
		if (Collections.allocateIterators)
			return new Entries(this);
		if (entries1 == null) {
			entries1 = new Entries(this);
			entries2 = new Entries(this);
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

	/**
	 * Returns an iterator for the values in the map. Remove is supported.
	 * <p>
	 * If {@link Collections#allocateIterators} is false, the same iterator instance is returned each time this method is called.
	 * Use the {@link Entries} constructor for nested or multithreaded iteration.
	 */
	public Values<V> values () {
		if (Collections.allocateIterators)
			return new Values(this);
		if (values1 == null) {
			values1 = new Values(this);
			values2 = new Values(this);
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

	/**
	 * Returns an iterator for the keys in the map. Remove is supported.
	 * <p>
	 * If {@link Collections#allocateIterators} is false, the same iterator instance is returned each time this method is called.
	 * Use the {@link Entries} constructor for nested or multithreaded iteration.
	 */
	public Keys keys () {
		if (Collections.allocateIterators)
			return new Keys(this);
		if (keys1 == null) {
			keys1 = new Keys(this);
			keys2 = new Keys(this);
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

	static public class Entry<V> {
		public long key;
		public V value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static private class MapIterator<V> {
		static final int INDEX_ILLEGAL = -2;
		static final int INDEX_ZERO = -1;

		public boolean hasNext;

		final LongMap<V> map;
		int nextIndex, currentIndex;
		boolean valid = true;

		public MapIterator (LongMap<V> map) {
			this.map = map;
			reset();
		}

		public void reset () {
			currentIndex = INDEX_ILLEGAL;
			nextIndex = INDEX_ZERO;
			if (map.hasZeroValue)
				hasNext = true;
			else
				findNextIndex();
		}

		void findNextIndex () {
			hasNext = false;
			long[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++nextIndex < n; ) {
				if (keyTable[nextIndex] != 0) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove () {
			if (currentIndex == INDEX_ZERO && map.hasZeroValue) {
				map.zeroValue = null;
				map.hasZeroValue = false;
			} else if (currentIndex < 0) {
				throw new IllegalStateException("next must be called before remove.");
			} else {
				final long[] keyTable = map.keyTable;
				final V[] valueTable = map.valueTable;
				int loc = currentIndex;
				long key;
				final int mask = map.mask;
				while ((key = keyTable[loc + 1 & mask]) != 0 && (loc + 1 & mask) != map.place(key)) {
					keyTable[loc] = key;
					valueTable[loc] = valueTable[loc + 1 & mask];
					++loc;
				}
				if(loc != currentIndex) --nextIndex;
				keyTable[loc] = 0;
				valueTable[loc] = null;
			}
			currentIndex = INDEX_ILLEGAL;
			map.size--;
		}
	}

	static public class Entries<V> extends MapIterator<V> implements Iterable<Entry<V>>, Iterator<Entry<V>> {
		private Entry<V> entry = new Entry();

		public Entries (LongMap map) {
			super(map);
		}

		/**
		 * Note the same entry instance is returned each time this method is called.
		 */
		public Entry<V> next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			long[] keyTable = map.keyTable;
			if (nextIndex == INDEX_ZERO) {
				entry.key = 0;
				entry.value = map.zeroValue;
			} else {
				entry.key = keyTable[nextIndex];
				entry.value = map.valueTable[nextIndex];
			}
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		public boolean hasNext () {
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public Iterator<Entry<V>> iterator () {
			return this;
		}

		public void remove () {
			super.remove();
		}
	}

	static public class Values<V> extends MapIterator<V> implements Iterable<V>, Iterator<V> {
		public Values (LongMap<V> map) {
			super(map);
		}

		public boolean hasNext () {
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public V next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			V value;
			if (nextIndex == INDEX_ZERO)
				value = map.zeroValue;
			else
				value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public Iterator<V> iterator () {
			return this;
		}

		/**
		 * Returns a new array containing the remaining values.
		 */
		public Array<V> toArray () {
			Array array = new Array(true, map.size);
			while (hasNext)
				array.add(next());
			return array;
		}

		public void remove () {
			super.remove();
		}
	}

	static public class Keys extends MapIterator {
		public Keys (LongMap map) {
			super(map);
		}

		public long next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			long key = nextIndex == INDEX_ZERO ? 0 : map.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		/**
		 * Returns a new array containing the remaining keys.
		 */
		public LongArray toArray () {
			LongArray array = new LongArray(true, map.size);
			while (hasNext)
				array.add(next());
			return array;
		}

		/**
		 * Adds the remaining values to the specified array.
		 */
		public LongArray toArray (LongArray array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
