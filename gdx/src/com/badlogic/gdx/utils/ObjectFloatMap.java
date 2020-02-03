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
 * An unordered map where the keys are objects and the values are unboxed floats. This implementation uses linear probing with the
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
 * Iteration won't be as fast here as with OrderedSet and OrderedMap.
 *
 * @author Tommy Ettinger
 * @author Nathan Sweet
 */
public class ObjectFloatMap<K> implements Iterable<ObjectFloatMap.Entry<K>> {

	public int size;

	K[] keyTable;
	float[] valueTable;

	float loadFactor;
	int threshold;
	/**
	 * Used by {@link #place(Object)} to bit-shift the upper bits of a {@code long} into a usable range (less than or
	 * equal to {@link #mask}, greater than or equal to 0). If you're setting it in a subclass, this shift can be
	 * negative, which is a convenient way to match the number of bits in mask; if mask is a 7-bit number, then a shift
	 * of -7 will correctly shift the upper 7 bits into the lowest 7 positions. If using what this class sets, shift
	 * will be greater than 32 and less than 64; if you use this shift with an int, it will still correctly move the
	 * upper bits of an int to the lower bits, thanks to Java's implicit modulus on shifts.
	 * <br>
	 * You can also use {@link #mask} to mask the low bits of a number, which may be faster for some hashCode()s, if you
	 * reimplement {@link #place(Object)}.
	 */
	protected int shift;
	/**
	 * The bitmask used to contain hashCode()s to the indices that can be fit into the key array this uses. This should
	 * always be all-1-bits in its low positions; that is, it must be a power of two minus 1. If you subclass and change
	 * {@link #place(Object)}, you may want to use this instead of {@link #shift} to isolate usable bits of a hash.
	 */
	protected int mask;

	Entries entries1, entries2;
	Values values1, values2;
	Keys keys1, keys2;

	/**
	 * Creates a new map with an initial capacity of 51 and a load factor of 0.8.
	 */
	public ObjectFloatMap () {
		this(51, 0.8f);
	}

	/**
	 * Creates a new map with a load factor of 0.8.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public ObjectFloatMap (int initialCapacity) {
		this(initialCapacity, 0.8f);
	}

	/**
	 * Creates a new map with the specified initial capacity and load factor. This map will hold initialCapacity items before
	 * growing the backing table.
	 *
	 * @param initialCapacity If not a power of two, it is increased to the next nearest power of two.
	 */
	public ObjectFloatMap (int initialCapacity, float loadFactor) {
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

		keyTable = (K[])new Object[initialCapacity];
		valueTable = new float[initialCapacity];
	}

	/**
	 * Creates a new map identical to the specified map.
	 */
	public ObjectFloatMap (ObjectFloatMap<? extends K> map) {
		this((int)Math.floor(map.keyTable.length * map.loadFactor), map.loadFactor);
		System.arraycopy(map.keyTable, 0, keyTable, 0, map.keyTable.length);
		System.arraycopy(map.valueTable, 0, valueTable, 0, map.valueTable.length);
		size = map.size;
	}

	/**
	 * Finds an array index between 0 and {@link #mask}, both inclusive, corresponding to the hash code of {@code item}.
	 * By default, this uses "Fibonacci Hashing" on the {@link Object#hashCode()} of {@code item}; this multiplies
	 * {@code item.hashCode()} by a long constant (2 to the 64, divided by the golden ratio) and shifts the high-quality
	 * uppermost bits into the lowest positions so they can be used as array indices. The multiplication by a long may
	 * be somewhat slow on GWT, but it will be correct across all platforms and won't lose precision. Using Fibonacci
	 * Hashing allows even very poor hashCode() implementations, such as those that only differ in their upper bits, to
	 * work in a hash table without heavy collision rates. It has known problems when all or most hashCode()s are
	 * multiples of larger Fibonacci numbers; see <a href="https://probablydance.com/2018/06/16/fibonacci-hashing-the-optimization-that-the-world-forgot-or-a-better-alternative-to-integer-modulo/">this blog post by Malte Skarupke</a>
	 * for more details. In the unlikely event that most of your hashCode()s are Fibonacci numbers, you can subclass
	 * this to change this method, which is a one-liner in this form:
	 * {@code return (int) (item.hashCode() * 0x9E3779B97F4A7C15L >>> shift);}
	 * <br>
	 * This can be overridden by subclasses, which you may want to do if your key type needs special consideration for
	 * its hash (such as if you can't modify or extend a particular class that has an incorrect hashCode()). Subclasses
	 * that don't need the collision decrease of Fibonacci Hashing (assuming the key class has a good hashCode()) may do
	 * fine with a simple implementation:
	 * {@code return (item.hashCode() & mask);}
	 *
	 * @param item a key that this method will hash, by default by calling {@link Object#hashCode()} on it; non-null
	 * @return an int between 0 and {@link #mask}, both inclusive
	 */
	protected int place (final K item) {
		// shift is always greater than 32, less than 64
		return (int)(item.hashCode() * 0x9E3779B97F4A7C15L >>> shift);
	}

	private int locateKey (final K key) {
		return locateKey(key, place(key));
	}

	/**
	 * Given a key and its initial placement to try in an array, this finds the actual location of the key in the array
	 * if it is present, or -1 if the key is not present. This can be overridden if a subclass needs to compare for
	 * equality differently than just by calling {@link Object#equals(Object)}, but only within the same package.
	 *
	 * @param key       a K key that will be checked for equality if a similar-seeming key is found
	 * @param placement as calculated by {@link #place(Object)}, almost always with {@code place(key)}
	 * @return the location in the key array of key, if found, or -1 if it was not found.
	 */
	int locateKey (final K key, final int placement) {
		for (int i = placement; ; i = i + 1 & mask) {
			// empty space is available
			if (keyTable[i] == null) {
				return -1;
			}
			if (key.equals(keyTable[i])) {
				return i;
			}
		}
	}

	/**
	 * Doesn't return a value, unlike other maps.
	 * You can use {@link #get(Object, float)} with a defaultValue of {@link Float#NaN} if you want to tell with
	 * certainty that a key is not present; comparing with NaN is tricky but {@link Float#isNaN(float)} makes it easy.
	 * If isNaN returns true, you can generally act like another Map had returned null in the same situation (meaning
	 * the value is unusable). This works because this class will never insert a NaN value into the map unless one is
	 * explicitly inserted, and since NaN acts so strangely in its everyday usage, virtually all code will not place NaN
	 * in a map.
	 */
	public void put (K key, float value) {
		if (key == null)
			throw new IllegalArgumentException("key cannot be null.");
		K[] keyTable = this.keyTable;
		float[] valueTable = this.valueTable;
		
		int b = place(key);
		int loc = locateKey(key, b);
		// an identical key already exists
		if (loc != -1) {
			valueTable[loc] = value;
			return;
		}
		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break (resize is later)
			if (keyTable[i] == null) {
				keyTable[i] = key;
				valueTable[i] = value;
				break;
			}
		}
		if (++size >= threshold) {
			resize(keyTable.length << 1);
		}
	}

	public void putAll (ObjectFloatMap<K> map) {
		ensureCapacity(map.size);
		final K[] keyTable = map.keyTable;
		final float[] valueTable = map.valueTable;
		K k;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			if ((k = keyTable[i]) != null)
				put(k, valueTable[i]);
		}
	}
//		ensureCapacity(map.size);
//		for (Entry<K> entry : map)
//			put(entry.key, entry.value);
//	}

	/**
	 * Skips checks for existing keys.
	 */
	private void putResize (K key, float value) {
		K[] keyTable = this.keyTable;
		float[] valueTable = this.valueTable;

		int b = place(key);
		for (int i = b; ; i = (i + 1) & mask) {
			// space is available so we insert and break (resize is later)
			if (keyTable[i] == null) {
				keyTable[i] = key;
				valueTable[i] = value;
				break;
			}
		}
		if (++size >= threshold) {
			resize(keyTable.length << 1);
		}
	}

	/**
	 * Returns the value for the specified key, or the default value if the key is not in the map.
	 */
	public float get (K key, float defaultValue) {
		final int loc = locateKey(key);
		return loc == -1 ? defaultValue : valueTable[loc];
	}

	/**
	 * Returns the key's current value and increments the stored value. If the key is not in the map, defaultValue + increment is
	 * put into the map.
	 */
	public float getAndIncrement (K key, float defaultValue, float increment) {
		final int loc = locateKey(key);
		// key was not found
		if (loc == -1) {
			// because we know there's no existing duplicate key, we can use putResize().
			putResize(key, defaultValue + increment);
			return defaultValue;
		}
		final float oldValue = valueTable[loc];
		valueTable[loc] += increment;
		return oldValue;
	}

	public float remove (K key, float defaultValue) {
		int loc = locateKey(key);
		if (loc == -1) {
			return defaultValue;
		}
		final K[] keyTable = this.keyTable;
		final float[] valueTable = this.valueTable;
		final float oldValue = valueTable[loc];
		int nl = (loc + 1 & mask);
		while ((key = keyTable[nl]) != null && nl != place(key)) {
			keyTable[loc] = key;
			valueTable[loc] = valueTable[nl];
			loc = nl;
			nl = loc + 1 & mask;
		}
		keyTable[loc] = null;
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
		size = 0;
		resize(maximumCapacity);
	}

	public void clear () {
		if (size == 0)
			return;
		K[] keyTable = this.keyTable;
		for (int i = keyTable.length; i > 0; ) {
			keyTable[--i] = null;
		}
		size = 0;
	}

	/**
	 * Returns true if the specified value is in the map. Note this traverses the entire map and compares every value, which may
	 * be an expensive operation.
	 */
	public boolean containsValue (float value) {
		final K[] keyTable = this.keyTable;
		final float[] valueTable = this.valueTable;
		for (int i = valueTable.length; i-- > 0; )
			if (keyTable[i] != null && valueTable[i] == value)
				return true;
		return false;
	}

	public boolean containsKey (K key) {
		return locateKey(key) != -1;
	}

	/**
	 * Returns the key for the specified value, or null if it is not in the map. Note this traverses the entire map and compares
	 * every value, which may be an expensive operation.
	 */
	public K findKey (float value) {
		final K[] keyTable = this.keyTable;
		final float[] valueTable = this.valueTable;
		for (int i = valueTable.length; i-- > 0; ) {
			K key = keyTable[i];
			if (key != null && valueTable[i] == value)
				return key;
		}
		return null;
	}

	/**
	 * Increases the size of the backing array to accommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= threshold)
			resize(MathUtils.nextPowerOfTwo((int)Math.ceil(sizeNeeded / loadFactor)));
	}

	final void resize (int newSize) {
		int oldCapacity = keyTable.length;
		threshold = (int)(newSize * loadFactor);
		mask = newSize - 1;
		shift = Long.numberOfLeadingZeros(mask);

		K[] oldKeyTable = keyTable;
		float[] oldValueTable = valueTable;

		keyTable = (K[])new Object[newSize];
		valueTable = new float[newSize];
		
		int oldSize = size;
		size = 0;
		if (oldSize > 0) {
			for (int i = 0; i < oldCapacity; i++) {
				K key = oldKeyTable[i];
				if (key != null)
					putResize(key, oldValueTable[i]);
			}
		}
	}

	public int hashCode () {
		int h = size;
		K[] keyTable = this.keyTable;
		float[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			K key = keyTable[i];
			if (key != null) {
				h ^= key.hashCode();
				final int value = NumberUtils.floatToRawIntBits(valueTable[i]);
				// the upper bits change more reliably than lower ones in value; xorshift to improve lower bits
				h += value ^ value >>> 16 ^ value >>> 21;
			}
		}
		return h;
	}

	public boolean equals (Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ObjectFloatMap))
			return false;
		ObjectFloatMap other = (ObjectFloatMap)obj;
		if (other.size != size)
			return false;
		K[] keyTable = this.keyTable;
		float[] valueTable = this.valueTable;
		for (int i = 0, n = keyTable.length; i < n; i++) {
			K key = keyTable[i];
			if (key != null) {
				float otherValue = other.get(key, 0);
				if (otherValue == 0 && !other.containsKey(key))
					return false;
				if (otherValue != valueTable[i])
					return false;
			}
		}
		return true;
	}

	public String toString (String separator) {
		return toString(separator, false);
	}

	public String toString () {
		return toString(", ", true);
	}

	private String toString (String separator, boolean braces) {
		if (size == 0)
			return braces ? "{}" : "";
		java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
		if (braces)
			buffer.append('{');
		K[] keyTable = this.keyTable;
		float[] valueTable = this.valueTable;
		int i = keyTable.length;
		while (i-- > 0) {
			K key = keyTable[i];
			if (key == null)
				continue;
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
			break;
		}
		while (i-- > 0) {
			K key = keyTable[i];
			if (key == null)
				continue;
			buffer.append(separator);
			buffer.append(key);
			buffer.append('=');
			buffer.append(valueTable[i]);
		}
		if (braces)
			buffer.append('}');
		return buffer.toString();
	}

	public Entries<K> iterator () {
		return entries();
	}

	/**
	 * Returns an iterator for the entries in the map. Remove is supported. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link Entries} constructor for nested or multithreaded iteration.
	 */
	public Entries<K> entries () {
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
	 * Returns an iterator for the values in the map. Remove is supported. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link Values} constructor for nested or multithreaded iteration.
	 */
	public Values values () {
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
	 * Returns an iterator for the keys in the map. Remove is supported. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link Keys} constructor for nested or multithreaded iteration.
	 */
	public Keys<K> keys () {
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

	static public class Entry<K> {
		public K key;
		public float value;

		public String toString () {
			return key + "=" + value;
		}
	}

	static private class MapIterator<K> {
		public boolean hasNext;

		final ObjectFloatMap<K> map;
		int nextIndex, currentIndex;
		boolean valid = true;

		public MapIterator (ObjectFloatMap<K> map) {
			this.map = map;
			reset();
		}

		public void reset () {
			currentIndex = -1;
			nextIndex = -1;
			findNextIndex();
		}

		void findNextIndex () {
			hasNext = false;
			K[] keyTable = map.keyTable;
			for (int n = keyTable.length; ++nextIndex < n; ) {
				if (keyTable[nextIndex] != null) {
					hasNext = true;
					break;
				}
			}
		}

		public void remove () {
			if (currentIndex < 0)
				throw new IllegalStateException("next must be called before remove.");
			final K[] keyTable = map.keyTable;
			final float[] valueTable = map.valueTable;
			final int mask = map.mask;
			int loc = currentIndex, nl = (loc + 1 & mask);
			K key;
			while ((key = keyTable[nl]) != null && nl != map.place(key)) {
				keyTable[loc] = key;
				valueTable[loc] = valueTable[nl];
				loc = nl;
				nl = loc + 1 & mask;
			}
			if(loc != currentIndex) --nextIndex;
			keyTable[loc] = null;
			--map.size;
			currentIndex = -1;
		}
	}

	static public class Entries<K> extends MapIterator<K> implements Iterable<Entry<K>>, Iterator<Entry<K>> {
		Entry<K> entry = new Entry<K>();

		public Entries (ObjectFloatMap<K> map) {
			super(map);
		}

		/**
		 * Note the same entry instance is returned each time this method is called.
		 */
		public Entry<K> next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			K[] keyTable = map.keyTable;
			entry.key = keyTable[nextIndex];
			entry.value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return entry;
		}

		public boolean hasNext () {
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public Entries<K> iterator () {
			return this;
		}
		public void remove () {
			super.remove();
		}

	}

	static public class Values extends MapIterator<Object> {
		public Values (ObjectFloatMap<?> map) {
			super((ObjectFloatMap<Object>)map);
		}

		public boolean hasNext () {
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public float next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			float value = map.valueTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return value;
		}

		public Values iterator () {
			return this;
		}

		/**
		 * Returns a new array containing the remaining values.
		 */
		public FloatArray toArray () {
			FloatArray array = new FloatArray(true, map.size);
			while (hasNext)
				array.add(next());
			return array;
		}

		/**
		 * Adds the remaining values to the specified array.
		 */
		public FloatArray toArray (FloatArray array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}

	static public class Keys<K> extends MapIterator<K> implements Iterable<K>, Iterator<K> {
		public Keys (ObjectFloatMap<K> map) {
			super(map);
		}

		public boolean hasNext () {
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			return hasNext;
		}

		public K next () {
			if (!hasNext)
				throw new NoSuchElementException();
			if (!valid)
				throw new GdxRuntimeException("#iterator() cannot be used nested.");
			K key = map.keyTable[nextIndex];
			currentIndex = nextIndex;
			findNextIndex();
			return key;
		}

		public Keys<K> iterator () {
			return this;
		}

		/**
		 * Returns a new array containing the remaining keys.
		 */
		public Array<K> toArray () {
			return toArray(new Array<K>(true, map.size));
		}

		/**
		 * Adds the remaining keys to the array.
		 */
		public Array<K> toArray (Array<K> array) {
			while (hasNext)
				array.add(next());
			return array;
		}
	}
}
