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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import com.badlogic.gdx.math.MathUtils;

/** A resizable, ordered or unordered char array. Avoids the boxing that occurs with ArrayList<Character>. If unordered, this
 * class avoids a memory copy when removing single chars (the last element is moved to the removed element's position).
 * <p>
 * This class provides a more flexible and powerful API than {@link StringBuffer} and {@link StringBuilder}, the main differences
 * being:
 * <ul>
 * <li>JDK classes have performance advantages from intrinsics on most JVMs
 * <li>Not final or synchronized
 * <li>Direct access to the char[] and length
 * <li>Appending int or long does not allocate
 * <li>Compare against strings without allocating
 * <li>Implements equals and hashCode
 * <li>char[] grows by 50% rather than doubling
 * <li>Additional builder-style methods:
 * <ul>
 * <li>appendWithSeparators - adds an array of values, with a separator
 * <li>appendPadding - adds padding characters
 * <li>appendFixedLength - adds a fixed width field
 * <li>toCharArray/getChars - simpler ways to get a range of the character array
 * <li>delete/replace/replace(char,String) - delete or replace chars and strings
 * <li>leftString/rightString/midString - substring without exceptions
 * <li>contains/equals/equalsIgnoreCase/etc - string methods
 * <li>size/clear/isEmpty/notEmpty - collections style API methods
 * <li>codePointAt/codePointCount/reverse/etc - Unicode surrogate aware methods
 * <li>toStringAndClear/etc - general convenience
 * <li>Readable/Appendable/CharBuffer methods
 * </ul>
 * <li>Views:
 * <ul>
 * <li>reader() - uses the internal buffer as the source of a Reader
 * <li>writer() - allows a Writer to write directly to the internal buffer
 * </ul>
 * </ul>
 * @author Nathan Sweet
 * @author org.apache.commons.text.TextStringBuilder */
public class CharArray implements CharSequence, Appendable {
	static private final int CAPACITY = 16;
	static private final int EOS = -1;
	static private final int FALSE_STRING_SIZE = 5;
	static private final int TRUE_STRING_SIZE = 4;
	static private final String NULL = "null";
	static private final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/** The maximum buffer size to allocate. This is the size used in {@link java.util.ArrayList}, as some VMs reserve header words
	 * in an array. */
	static private final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

	public char[] items;
	public int size;
	public boolean ordered;

	/** Creates an ordered array with a capacity of 16. */
	public CharArray () {
		this(true, CAPACITY);
	}

	/** Creates an ordered array with the specified capacity. */
	public CharArray (int capacity) {
		this(true, capacity);
	}

	/** @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy.
	 * @param capacity Any elements added beyond this will cause the backing array to be grown. */
	public CharArray (boolean ordered, int capacity) {
		this.ordered = ordered;
		items = new char[capacity];
	}

	/** Creates a new array containing the elements in the specific array. The new array will be ordered if the specific array is
	 * ordered. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be
	 * grown. */
	public CharArray (CharArray array) {
		this.ordered = array.ordered;
		size = array.size;
		items = new char[size];
		System.arraycopy(array.items, 0, items, 0, size);
	}

	/** Creates a new ordered array containing the elements in the specified array. The capacity is set to the number of elements,
	 * so any subsequent elements added will cause the backing array to be grown. */
	public CharArray (char[] array) {
		this(true, array, 0, array.length);
	}

	/** Creates a new array containing the elements in the specified array. The capacity is set to the number of elements, so any
	 * subsequent elements added will cause the backing array to be grown.
	 * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy. */
	public CharArray (boolean ordered, char[] array, int start, int count) {
		this(ordered, count);
		size = count;
		System.arraycopy(array, start, items, 0, count);
	}

	/** Constructs an instance from a character sequence, allocating 16 extra characters for growth. */
	public CharArray (CharSequence seq) {
		this(seq.length() + CAPACITY);
		append(seq);
	}

	/** Constructs an instance from a string, allocating 16 extra characters for growth. */
	public CharArray (String str) {
		this(str.length() + CAPACITY);
		append(str);
	}

	/** Constructs an instance from a string builder, allocating 16 extra characters for growth. */
	public CharArray (StringBuilder str) {
		this(str.length() + CAPACITY);
		append(str);
	}

	/** Constructs an instance that uses the specified array directly, until reallocation is needed. */
	private CharArray (char[] initialBuffer, int length) {
		Objects.requireNonNull(initialBuffer, "initialBuffer");
		items = initialBuffer;
		size = length;
	}

	public void add (char value) {
		if (size == items.length) resizeBuffer(size + 1);
		items[size++] = value;
	}

	public void add (char value1, char value2) {
		if (size + 1 >= items.length) resizeBuffer(size + 2);
		items[size] = value1;
		items[size + 1] = value2;
		size += 2;
	}

	public void add (char value1, char value2, char value3) {
		if (size + 2 >= items.length) resizeBuffer(size + 3);
		char[] items = this.items;
		items[size] = value1;
		items[size + 1] = value2;
		items[size + 2] = value3;
		size += 3;
	}

	public void add (char value1, char value2, char value3, char value4) {
		if (size + 3 >= items.length) resizeBuffer(size + 4);
		char[] items = this.items;
		items[size] = value1;
		items[size + 1] = value2;
		items[size + 2] = value3;
		items[size + 3] = value4;
		size += 4;
	}

	public void addAll (CharArray array) {
		addAll(array.items, 0, array.size);
	}

	public void addAll (CharArray array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll(array.items, offset, length);
	}

	public void addAll (char... array) {
		addAll(array, 0, array.length);
	}

	public void addAll (char[] array, int offset, int length) {
		int sizeNeeded = size + length;
		if (sizeNeeded > items.length) resizeBuffer(sizeNeeded);
		System.arraycopy(array, offset, items, size, length);
		size += length;
	}

	public char get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		return items[index];
	}

	public void set (int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] = value;
	}

	public void incr (int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] += value;
	}

	public void incr (char value) {
		char[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			items[i] += value;
	}

	public void mul (int index, char value) {
		if (index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
		items[index] *= value;
	}

	public void mul (char value) {
		char[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			items[i] *= value;
	}

	public void swap (int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
		if (second >= size) throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
		char[] items = this.items;
		char firstValue = items[first];
		items[first] = items[second];
		items[second] = firstValue;
	}

	/** Returns true if the specified value was replaced successfully with the replacement
	 * @param value the char to be replaced
	 * @param replacement the first value will be replaced by this replacement if found
	 * @return if value was found and replaced */
	public boolean replaceFirst (char value, char replacement) {
		if (value != replacement) {
			char[] items = this.items;
			for (int i = 0, n = size; i < n; i++) {
				if (items[i] == value) {
					items[i] = replacement;
					return true;
				}
			}
		}
		return false;
	}

	/** Returns the number of replacements done.
	 * @param value the char to be replaced
	 * @param replacement all occurrences of value will be replaced by this replacement
	 * @return the number of replacements done */
	public int replaceAll (char value, char replacement) {
		int replacements = 0;
		if (value != replacement) {
			char[] items = this.items;
			for (int i = 0, n = size; i < n; i++) {
				if (items[i] == value) {
					items[i] = replacement;
					replacements++;
				}
			}
		}
		return replacements;
	}

	public boolean contains (char value) {
		int i = size - 1;
		char[] items = this.items;
		while (i >= 0)
			if (items[i--] == value) return true;
		return false;
	}

	public int indexOf (char value) {
		char[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i] == value) return i;
		return -1;
	}

	public int lastIndexOf (char value) {
		char[] items = this.items;
		for (int i = size - 1; i >= 0; i--)
			if (items[i] == value) return i;
		return -1;
	}

	/** Removes the first occurence of the specified value. */
	public boolean removeValue (char value) {
		char[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	/** Removes and returns the item at the specified index. */
	public char removeIndex (int index) {
		validateIndex(index);
		char[] items = this.items;
		char value = items[index];
		size--;
		if (ordered)
			System.arraycopy(items, index + 1, items, index, size - index);
		else
			items[index] = items[size];
		return value;
	}

	/** Removes the items between the specified indices, inclusive. */
	public void removeRange (int start, int end) {
		int n = size;
		validateRange(start, end);
		int count = end - start + 1, lastIndex = n - count;
		if (ordered)
			System.arraycopy(items, start + count, items, start, n - (start + count));
		else {
			int i = Math.max(lastIndex, end + 1);
			System.arraycopy(items, i, items, start, n - i);
		}
		size = n - count;
	}

	/** Removes from this array the first instance of each element contained in the specified array.
	 * @return true if this array was modified. */
	public boolean removeAll (CharArray array) {
		int size = this.size;
		int startSize = size;
		char[] items = this.items;
		for (int i = 0, n = array.size; i < n; i++) {
			char item = array.get(i);
			for (int ii = 0; ii < size; ii++) {
				if (item == items[ii]) {
					removeIndex(ii);
					size--;
					break;
				}
			}
		}
		return size != startSize;
	}

	/** Removes and returns the last item. */
	public char pop () {
		return items[--size];
	}

	/** Returns the last item. */
	public char peek () {
		return items[size - 1];
	}

	/** Returns the first item. */
	public char first () {
		if (size == 0) throw new IllegalStateException("Array is empty.");
		return items[0];
	}

	/** Returns true if the array has one or more items. */
	public boolean notEmpty () {
		return size > 0;
	}

	/** Returns true if the array is empty. */
	public boolean isEmpty () {
		return size == 0;
	}

	/** Clears this CharArray.
	 * <p>
	 * This method does not reduce the size of the internal character buffer. To do that, call {@code clear()} followed by
	 * {@link #shrink()}.
	 * <p>
	 * This method is the same as {@link #setLength(int)} called with zero and is provided to match the API of Collections. */
	public void clear () {
		size = 0;
	}

	/** Sets the array size, leaving any values beyond the current size undefined.
	 * @return {@link #items} */
	public char[] setSize (int newSize) {
		if (newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
		if (newSize > items.length) resize(Math.max(8, newSize));
		size = newSize;
		return items;
	}

	/** Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items
	 * have been removed, or if it is known that more items will not be added.
	 * @return {@link #items} */
	public char[] shrink () {
		if (items.length > size) resize(size);
		return items;
	}

	/** Resizes {@link #items} if its length is more than {@link #size}. */
	public void trimToSize () {
		shrink();
	}

	/** Increases the size of the backing array to accommodate at least the specified number of additional items. Useful before
	 * adding many items to avoid multiple backing array resizes.
	 * @return {@link #items}
	 * @throws RuntimeException if the capacity cannot be allocated */
	public char[] ensureCapacity (int additionalCapacity) {
		if (additionalCapacity < 0) throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded - items.length > 0) resizeBuffer(sizeNeeded);
		return items;
	}

	private void require (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded - items.length > 0) resizeBuffer(sizeNeeded);
	}

	/** Resizes the buffer to at least the size specified.
	 * @throws RuntimeException if the {@code minCapacity} is negative */
	private void resizeBuffer (int minCapacity) {
		int oldCapacity = items.length;
		int newCapacity = (oldCapacity >> 1) + oldCapacity + 2; // +50%.
		// Overflow-conscious code treats minCapacity and newCapacity as unsigned.
		if ((newCapacity ^ 0x80000000) < (minCapacity ^ 0x80000000)) newCapacity = minCapacity;
		if ((newCapacity ^ 0x80000000) > (MAX_BUFFER_SIZE ^ 0x80000000)) {
			if (minCapacity < 0) // Overflow.
				throw new RuntimeException("Unable to allocate array size: " + Long.toString(minCapacity & 0xFFFFFFFFL));
			newCapacity = Math.max(minCapacity, MAX_BUFFER_SIZE);
		}
		resize(newCapacity);
	}

	protected char[] resize (int newSize) {
		items = Arrays.copyOf(items, newSize);
		return items;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}

	public void shuffle () {
		char[] items = this.items;
		for (int i = size - 1; i >= 0; i--) {
			int ii = MathUtils.random(i);
			char temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	/** Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is
	 * taken. */
	public void truncate (int newSize) {
		if (newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
		if (size > newSize) size = newSize;
	}

	/** Returns a random item from the array, or zero if the array is empty. */
	public char random () {
		if (size == 0) throw new IllegalStateException();
		return items[MathUtils.random(0, size - 1)];
	}

	public char[] toArray () {
		char[] array = new char[size];
		System.arraycopy(items, 0, array, 0, size);
		return array;
	}

	/** Appends a boolean value to this CharArray. */
	public CharArray append (boolean value) {
		if (value) {
			require(TRUE_STRING_SIZE);
			appendTrue(size);
		} else {
			require(FALSE_STRING_SIZE);
			appendFalse(size);
		}
		return this;
	}

	/** Appends a char value to this CharArray. */
	public CharArray append (char value) {
		require(1);
		items[size++] = value;
		return this;
	}

	/** Appends a char array to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null char[] ch) {
		if (ch == null) return appendNull();
		int strLength = ch.length;
		if (strLength > 0) {
			require(strLength);
			System.arraycopy(ch, 0, items, size, strLength);
			size += strLength;
		}
		return this;
	}

	/** Appends a char array to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @throws IndexOutOfBoundsException if {@code start} is not in the range {@code 0 <= start <= chars.length}
	 * @throws IndexOutOfBoundsException if {@code length < 0}
	 * @throws IndexOutOfBoundsException if {@code start + length > chars.length} */
	public CharArray append (@Null char[] ch, int start, int length) {
		if (ch == null) return appendNull();
		if (start < 0 || start > ch.length) throw new IndexOutOfBoundsException("Invalid start: " + start);
		if (length < 0 || start + length > ch.length) throw new IndexOutOfBoundsException("Invalid length: " + length);
		if (length > 0) {
			require(length);
			System.arraycopy(ch, start, items, size, length);
			size += length;
		}
		return this;
	}

	/** Appends the contents of a char buffer to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null CharBuffer str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends the contents of a char buffer to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray append (@Null CharBuffer buf, int start, int end) {
		if (buf == null) return appendNull();
		if (buf.hasArray()) {
			int totalLength = buf.remaining();
			if (start < 0 || end < 0 || start > end || end > totalLength) throw new IndexOutOfBoundsException();
			int length = end - start;
			require(length);
			System.arraycopy(buf.array(), buf.arrayOffset() + buf.position() + start, items, size, length);
			size += length;
		} else
			append(buf.toString(), start, end);
		return this;
	}

	/** Appends a CharSequence to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null CharSequence seq) {
		if (seq == null) return appendNull();
		if (seq instanceof CharArray) return append((CharArray)seq);
		if (seq instanceof StringBuilder) return append((StringBuilder)seq);
		if (seq instanceof StringBuffer) return append((StringBuffer)seq);
		if (seq instanceof CharBuffer) return append((CharBuffer)seq);
		return append(seq.toString());
	}

	/** Appends part of a CharSequence to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray append (@Null CharSequence seq, int start, int end) {
		if (seq == null) return appendNull();
		if (start < 0 || end < 0 || start > end || end > seq.length()) throw new IndexOutOfBoundsException();
		return append(seq.toString(), start, end);
	}

	/** Appends a double value to this CharArray using {@code String.valueOf}. */
	public CharArray append (double value) {
		return append(String.valueOf(value));
	}

	/** Appends a float value to this CharArray using {@code String.valueOf}. */
	public CharArray append (float value) {
		return append(String.valueOf(value));
	}

	/** Appends an int value to this CharArray. The {@code int} value is converted to chars without memory allocation. */
	public CharArray append (int value) {
		return append(value, 0, '0');
	}

	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add */
	public CharArray append (int value, int minLength) {
		return append(value, minLength, '0');
	}

	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix */
	public CharArray append (int value, final int minLength, final char prefix) {
		if (value == Integer.MIN_VALUE) {
			append("-2147483648");
			return this;
		}
		if (value < 0) {
			append('-');
			value = -value;
		}
		if (minLength > 1) {
			for (int j = minLength - numChars(value, 10); j > 0; --j)
				append(prefix);
		}
		if (value >= 10000) {
			if (value >= 1000000000) append(DIGITS[(int)((long)value % 10000000000L / 1000000000L)]);
			if (value >= 100000000) append(DIGITS[value % 1000000000 / 100000000]);
			if (value >= 10000000) append(DIGITS[value % 100000000 / 10000000]);
			if (value >= 1000000) append(DIGITS[value % 10000000 / 1000000]);
			if (value >= 100000) append(DIGITS[value % 1000000 / 100000]);
			append(DIGITS[value % 100000 / 10000]);
		}
		if (value >= 1000) append(DIGITS[value % 10000 / 1000]);
		if (value >= 100) append(DIGITS[value % 1000 / 100]);
		if (value >= 10) append(DIGITS[value % 100 / 10]);
		append(DIGITS[value % 10]);
		return this;
	}

	/** Appends a long value to this CharArray. The {@code long} value is converted to chars without memory allocation. */
	public CharArray append (long value) {
		return append(value, 0, '0');
	}

	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add */
	public CharArray append (long value, int minLength) {
		return append(value, minLength, '0');
	}

	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix */
	public CharArray append (long value, int minLength, char prefix) {
		if (value == Long.MIN_VALUE) {
			append("-9223372036854775808");
			return this;
		}
		if (value < 0L) {
			append('-');
			value = -value;
		}
		if (minLength > 1) {
			for (int j = minLength - numChars(value, 10); j > 0; --j)
				append(prefix);
		}
		if (value >= 10000) {
			if (value >= 1000000000000000000L) append(DIGITS[(int)(value % 10000000000000000000D / 1000000000000000000L)]);
			if (value >= 100000000000000000L) append(DIGITS[(int)(value % 1000000000000000000L / 100000000000000000L)]);
			if (value >= 10000000000000000L) append(DIGITS[(int)(value % 100000000000000000L / 10000000000000000L)]);
			if (value >= 1000000000000000L) append(DIGITS[(int)(value % 10000000000000000L / 1000000000000000L)]);
			if (value >= 100000000000000L) append(DIGITS[(int)(value % 1000000000000000L / 100000000000000L)]);
			if (value >= 10000000000000L) append(DIGITS[(int)(value % 100000000000000L / 10000000000000L)]);
			if (value >= 1000000000000L) append(DIGITS[(int)(value % 10000000000000L / 1000000000000L)]);
			if (value >= 100000000000L) append(DIGITS[(int)(value % 1000000000000L / 100000000000L)]);
			if (value >= 10000000000L) append(DIGITS[(int)(value % 100000000000L / 10000000000L)]);
			if (value >= 1000000000L) append(DIGITS[(int)(value % 10000000000L / 1000000000L)]);
			if (value >= 100000000L) append(DIGITS[(int)(value % 1000000000L / 100000000L)]);
			if (value >= 10000000L) append(DIGITS[(int)(value % 100000000L / 10000000L)]);
			if (value >= 1000000L) append(DIGITS[(int)(value % 10000000L / 1000000L)]);
			if (value >= 100000L) append(DIGITS[(int)(value % 1000000L / 100000L)]);
			append(DIGITS[(int)(value % 100000L / 10000L)]);
		}
		if (value >= 1000L) append(DIGITS[(int)(value % 10000L / 1000L)]);
		if (value >= 100L) append(DIGITS[(int)(value % 1000L / 100L)]);
		if (value >= 10L) append(DIGITS[(int)(value % 100L / 10L)]);
		append(DIGITS[(int)(value % 10L)]);
		return this;
	}

	/** Appends an object to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null Object obj) {
		if (obj == null) return appendNull();
		if (obj instanceof CharSequence) return append((CharSequence)obj);
		return append(obj.toString());
	}

	/** Appends a string to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null String str) {
		if (str == null)
			appendNull();
		else {
			int length = str.length();
			require(length);
			str.getChars(0, length, items, size);
			size += length;
		}
		return this;
	}

	/** Appends the specified separator if this CharArray is not empty, then the specified string. */
	public CharArray append (String str, String separator) {
		if (size > 0) append(separator);
		append(str);
		return this;
	}

	/** Appends part of a string to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @throws IndexOutOfBoundsException if {@code start} is not in the range {@code 0 <= start <= str.length()}
	 * @throws IndexOutOfBoundsException if {@code length < 0}
	 * @throws IndexOutOfBoundsException if {@code start + length > str.length()} */
	public CharArray append (@Null String str, int start, int end) {
		if (str == null) return appendNull();
		if (start < 0 || end < 0 || start > end || end > str.length()) throw new IndexOutOfBoundsException();
		int length = end - start;
		if (length > 0) {
			require(length);
			str.getChars(start, end, items, size);
			size += length;
		}
		return this;
	}

	/** Appends a string buffer to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null StringBuffer str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends part of a string buffer to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray append (@Null StringBuffer str, int start, int end) {
		if (str == null) return appendNull();
		if (start < 0 || end < 0 || start > end || end > str.length()) throw new IndexOutOfBoundsException();
		int length = end - start;
		if (length > 0) {
			require(length);
			str.getChars(start, end, items, size);
			size += length;
		}
		return this;
	}

	/** Appends a StringBuilder to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null StringBuilder str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends part of a StringBuilder to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray append (@Null StringBuilder str, int start, int end) {
		if (str == null) return appendNull();
		if (start < 0 || end < 0 || start > end || end > str.length()) throw new IndexOutOfBoundsException();
		int length = end - start;
		if (length > 0) {
			require(length);
			str.getChars(start, end, items, size);
			size += length;
		}
		return this;
	}

	/** Appends another CharArray to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray append (@Null CharArray str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.size);
		return this;
	}

	/** Appends part of a CharArray to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray append (@Null CharArray str, int start, int end) {
		if (str == null) return appendNull();
		if (start < 0 || end < 0 || start > end || end > str.size) throw new IndexOutOfBoundsException();
		int length = end - start;
		if (length > 0) {
			require(length);
			str.getChars(start, end, items, size);
			size += length;
		}
		return this;
	}

	/** Appends each item in an iterable to this CharArray without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public CharArray appendAll (Iterable<?> iterable) {
		for (Iterator iter = iterable.iterator(); iter.hasNext();)
			append(iter.next());
		return this;
	}

	/** Appends each item in an iterator to this CharArray without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public CharArray appendAll (Iterator<?> iter) {
		while (iter.hasNext())
			append(iter.next());
		return this;
	}

	/** Appends each item in an array to this CharArray without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public <T> CharArray appendAll (T... array) {
		if (array.length > 0) {
			for (Object element : array)
				append(element);
		}
		return this;
	}

	/** Appends {@code "false"}. */
	private void appendFalse (int index) {
		items[index++] = 'f';
		items[index++] = 'a';
		items[index++] = 'l';
		items[index++] = 's';
		items[index] = 'e';
		size += FALSE_STRING_SIZE;
	}

	/** Appends an object to this CharArray padding on the left to a fixed width. The {@code String.valueOf} of the {@code int}
	 * value is used. If the formatted value is larger than the length, the left hand side is lost.
	 * @param width the fixed field width, zero or negative has no effect */
	public CharArray appendFixedWidthPadLeft (int value, int width, char padChar) {
		return appendFixedWidthPadLeft(String.valueOf(value), width, padChar);
	}

	/** Appends an object to this CharArray padding on the left to a fixed width. The {@code toString} of the object is used. If
	 * the object is larger than the length, the left hand side is lost. If the object is null {@code "null"} is used.
	 * @param obj the object to append, null uses ""
	 * @param width the fixed field width, zero or negative has no effect */
	public CharArray appendFixedWidthPadLeft (@Null Object obj, int width, char padChar) {
		if (width > 0) {
			require(width);
			String str = Objects.toString(obj, NULL);
			int strLength = str.length();
			if (strLength >= width)
				str.getChars(strLength - width, strLength, items, size);
			else {
				int padLen = width - strLength;
				int toIndex = size + padLen;
				Arrays.fill(items, size, toIndex, padChar);
				str.getChars(0, strLength, items, toIndex);
			}
			size += width;
		}
		return this;
	}

	/** Appends an object to this CharArray padding on the right to a fixed length. The {@code String.valueOf} of the {@code int}
	 * value is used. If the object is larger than the length, the right hand side is lost.
	 * @param width the fixed field width, zero or negative has no effect */
	public CharArray appendFixedWidthPadRight (int value, int width, char padChar) {
		return appendFixedWidthPadRight(String.valueOf(value), width, padChar);
	}

	/** Appends an object to this CharArray padding on the right to a fixed length. The {@code toString} of the object is used. If
	 * the object is larger than the length, the right hand side is lost.
	 * @param obj the object to append, null uses {@code "null"}
	 * @param width the fixed field width, zero or negative has no effect */
	public CharArray appendFixedWidthPadRight (@Null Object obj, int width, char padChar) {
		if (width > 0) {
			require(width);
			String str = Objects.toString(obj, NULL);
			int strLength = str.length();
			if (strLength >= width)
				str.getChars(0, width, items, size);
			else {
				str.getChars(0, strLength, items, size);
				int fromIndex = size + strLength;
				Arrays.fill(items, fromIndex, fromIndex + width - strLength, padChar);
			}
			size += width;
		}
		return this;
	}

	/** Appends a boolean value followed by a new line to this CharArray. */
	public CharArray appendln (boolean value) {
		return append(value).appendLine();
	}

	/** Appends a char value followed by a new line to this CharArray. */
	public CharArray appendln (char ch) {
		return append(ch).appendLine();
	}

	/** Appends a char array followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null char[] ch) {
		return append(ch).appendLine();
	}

	/** Appends a char array followed by a new line to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive */
	public CharArray appendln (@Null char[] ch, int start, int length) {
		return append(ch, start, length).appendLine();
	}

	/** Appends a double value followed by a new line to this CharArray using {@code String.valueOf}. */
	public CharArray appendln (double value) {
		return append(value).appendLine();
	}

	/** Appends a float value followed by a new line to this CharArray using {@code String.valueOf}. */
	public CharArray appendln (float value) {
		return append(value).appendLine();
	}

	/** Appends an int value followed by a new line to this CharArray using {@code String.valueOf}. */
	public CharArray appendln (int value) {
		return append(value).appendLine();
	}

	/** Appends a long value followed by a new line to this CharArray using {@code String.valueOf}. */
	public CharArray appendln (long value) {
		return append(value).appendLine();
	}

	/** Appends an object followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null Object obj) {
		return append(obj).appendLine();
	}

	/** Appends a string followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null String str) {
		append(str);
		return append('\n');
	}

	/** Appends a string followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendLine (@Null String str) {
		append(str);
		return append('\n');
	}

	/** Appends part of a string followed by a new line to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray appendln (@Null String str, int start, int end) {
		return append(str, start, end).appendLine();
	}

	/** Appends a string buffer followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null StringBuffer str) {
		return append(str).appendLine();
	}

	/** Appends part of a string buffer followed by a new line to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray appendln (@Null StringBuffer str, int start, int end) {
		return append(str, start, end).appendLine();
	}

	/** Appends a string builder followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null StringBuilder str) {
		return append(str).appendLine();
	}

	/** Appends part of a string builder followed by a new line to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray appendln (@Null StringBuilder str, int start, int end) {
		return append(str, start, end).appendLine();
	}

	/** Appends another CharArray followed by a new line to this CharArray. Appending null will call {@link #appendNull()}. */
	public CharArray appendln (@Null CharArray str) {
		return append(str).appendLine();
	}

	/** Appends part of a CharArray followed by a new line to this CharArray. Appending null will call {@link #appendNull()}.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive */
	public CharArray appendln (@Null CharArray str, int start, int end) {
		return append(str, start, end).appendLine();
	}

	/** Appends {@code \n}. */
	public CharArray appendln () {
		return append('\n');
	}

	/** Appends {@code \n}. */
	public CharArray appendLine () {
		return append('\n');
	}

	/** Appends {@code "null"}. */
	public CharArray appendNull () {
		require(4);
		int length = size;
		items[length] = 'n';
		items[length + 1] = 'u';
		items[length + 2] = 'l';
		items[length + 3] = 'l';
		size = length + 4;
		return this;
	}

	/** Appends the pad character to this CharArray the specified number of times.
	 * @param padCount negative means no append */
	public CharArray appendPadding (int padCount, char padChar) {
		if (padCount > 0) {
			require(padCount);
			Arrays.fill(items, size, size + padCount, padChar);
			size += padCount;
		}
		return this;
	}

	/** Appends a separator if this CharArray is currently non-empty. The separator is appended using {@link #append(char)}.
	 * <p>
	 * This method is useful for adding a separator each time around the loop except the first.
	 * 
	 * <pre>
	 * for (Iterator it = list.iterator(); it.hasNext();) {
	 * 	appendSeparator(',');
	 * 	append(it.next());
	 * }
	 * </pre>
	 * <p>
	 * Note that for this simple example, you should use {@link #appendWithSeparators(Iterable, String)}. */
	public CharArray appendSeparator (char separator) {
		if (notEmpty()) append(separator);
		return this;
	}

	/** Appends one of both separators to this CharArray If this CharArray is currently empty it will append the
	 * defaultIfEmpty-separator Otherwise it will append the standard-separator The separator is appended using
	 * {@link #append(char)}.
	 * @param standard the separator if this CharArray is not empty
	 * @param defaultIfEmpty the separator if this CharArray is empty */
	public CharArray appendSeparator (char standard, char defaultIfEmpty) {
		if (isEmpty())
			append(defaultIfEmpty);
		else
			append(standard);
		return this;
	}

	/** Appends a separator to this CharArray if the loop index is greater than zero. The separator is appended using
	 * {@link #append(char)}.
	 * <p>
	 * This method is useful for adding a separator each time around the loop except the first.
	 * 
	 * <pre>
	 * for (int i = 0; i &lt; list.size(); i++) {
	 * 	appendSeparator(",", i);
	 * 	append(list.get(i));
	 * }
	 * </pre>
	 * <p>
	 * Note that for this simple example, you should use {@link #appendWithSeparators(Iterable, String)}. */
	public CharArray appendSeparator (char separator, int loopIndex) {
		if (loopIndex > 0) append(separator);
		return this;
	}

	/** Appends a separator if this CharArray is currently non-empty. The separator is appended using {@link #append(String)}.
	 * <p>
	 * This method is useful for adding a separator each time around the loop except the first.
	 * 
	 * <pre>
	 * for (Iterator it = list.iterator(); it.hasNext();) {
	 * 	appendSeparator(",");
	 * 	append(it.next());
	 * }
	 * </pre>
	 * <p>
	 * Note that for this simple example, you should use {@link #appendWithSeparators(Iterable, String)}.
	 * @param separator the separator to use, null means no separator */
	public CharArray appendSeparator (@Null String separator) {
		return appendSeparator(separator, null);
	}

	/** Appends a separator to this CharArray if the loop index is greater than zero. The separator is appended using
	 * {@link #append(String)}.
	 * <p>
	 * This method is useful for adding a separator each time around the loop except the first.
	 * 
	 * <pre>
	 * for (int i = 0; i &lt; list.size(); i++) {
	 * 	appendSeparator(",", i);
	 * 	append(list.get(i));
	 * }
	 * </pre>
	 * <p>
	 * Note that for this simple example, you should use {@link #appendWithSeparators(Iterable, String)}.
	 * @param separator the separator to use, null means no separator */
	public CharArray appendSeparator (@Null String separator, int loopIndex) {
		if (separator != null && loopIndex > 0) append(separator);
		return this;
	}

	/** Appends one of both separators to this CharArray. If this CharArray is currently empty, it will append the
	 * defaultIfEmpty-separator, otherwise it will append the standard-separator.
	 * <p>
	 * The separator is appended using {@link #append(String)}.
	 * <p>
	 * This method is for example useful for constructing queries
	 * 
	 * <pre>
	* CharArray whereClause = new CharArray();
	* if (searchCommand.getPriority() != null) {
	*  whereClause.appendSeparator(" and", " where");
	*  whereClause.append(" priority = ?")
	* }
	* if (searchCommand.getComponent() != null) {
	*  whereClause.appendSeparator(" and", " where");
	*  whereClause.append(" component = ?")
	* }
	* selectClause.append(whereClause)
	 * </pre>
	 * 
	 * @param standard the separator if this CharArray is not empty, null means no separator
	 * @param defaultIfEmpty the separator if this CharArray is empty, null means no separator */
	public CharArray appendSeparator (@Null String standard, @Null String defaultIfEmpty) {
		String str = isEmpty() ? defaultIfEmpty : standard;
		if (str != null) append(str);
		return this;
	}

	/** Appends current contents of this {@code CharArray} to the provided {@link Appendable}.
	 * <p>
	 * This method tries to avoid doing any extra copies of contents.
	 * @throws IOException if an I/O error occurs.
	 * @see #readFrom(Readable) */
	public void appendTo (Appendable appendable) throws IOException {
		if (appendable instanceof Writer)
			((Writer)appendable).write(items, 0, size);
		else if (appendable instanceof StringBuilder)
			((StringBuilder)appendable).append(items, 0, size);
		else if (appendable instanceof StringBuffer)
			((StringBuffer)appendable).append(items, 0, size);
		else if (appendable instanceof CharBuffer)
			((CharBuffer)appendable).put(items, 0, size);
		else
			appendable.append(this);
	}

	/** Appends {@code "true"}. */
	private void appendTrue (int index) {
		items[index++] = 't';
		items[index++] = 'r';
		items[index++] = 'u';
		items[index] = 'e';
		size += TRUE_STRING_SIZE;
	}

	/** Appends an iterable placing separators between each value, but not before the first or after the last. Each object is
	 * appended using {@link #append(Object)}.
	 * @param separator the separator to use, null means no separator */
	public CharArray appendWithSeparators (Iterable<?> iterable, @Null String separator) {
		appendWithSeparators(iterable.iterator(), separator);
		return this;
	}

	/** Appends an iterator placing separators between each value, but not before the first or after the last. Each object is
	 * appended using {@link #append(Object)}.
	 * @param separator the separator to use, null means no separator */
	public CharArray appendWithSeparators (Iterator<?> it, @Null String separator) {
		String sep = Objects.toString(separator, "");
		while (it.hasNext()) {
			append(it.next());
			if (it.hasNext()) append(sep);
		}
		return this;
	}

	/** Appends an array placing separators between each value, but not before the first or after the last. Each object is appended
	 * using {@link #append(Object)}.
	 * @param separator the separator to use, null means no separator */
	public CharArray appendWithSeparators (Object[] array, @Null String separator) {
		if (array.length > 0) {
			String sep = Objects.toString(separator, "");
			append(array[0]);
			for (int i = 1; i < array.length; i++) {
				append(sep);
				append(array[i]);
			}
		}
		return this;
	}

	/** Appends the encoded Unicode code point. The code point is converted to a {@code char[]} as defined by
	 * {@link Character#toChars(int)}.
	 * @see Character#toChars(int) */
	public CharArray appendCodePoint (int codePoint) {
		append(Character.toChars(codePoint));
		return this;
	}

	/** Gets the contents of this CharArray as a Reader.
	 * <p>
	 * This method allows the contents of this CharArray to be read using any standard method that expects a Reader.
	 * <p>
	 * To use, simply create a {@code CharArray}, populate it with data, call {@code asReader}, and then read away.
	 * <p>
	 * The internal character array is shared between this CharArray and the reader. This allows you to append to this CharArray
	 * after creating the reader, and the changes will be picked up. Note however, that no synchronization occurs, so you must
	 * perform all operations with this CharArray and the reader in one thread.
	 * <p>
	 * The returned reader supports marking, and ignores the flush method.
	 * @return a reader that reads from this CharArray */
	public Reader reader () {
		return new CharArrayReader();
	}

	/** Gets this CharArray as a Writer that can be written to.
	 * <p>
	 * This method allows you to populate the contents of this CharArray using any standard method that takes a Writer.
	 * <p>
	 * To use, simply create a {@code CharArray}, call {@code asWriter}, and populate away. The data is available at any time using
	 * the methods of the {@code CharArray}.
	 * <p>
	 * The internal character array is shared between this CharArray and the writer. This allows you to intermix calls that append
	 * to this CharArray and write using the writer and the changes will be occur correctly. Note however, that no synchronization
	 * occurs, so you must perform all operations with this CharArray and the writer in one thread.
	 * <p>
	 * The returned writer ignores the close and flush methods.
	 * @return a writer that populates this CharArray */
	public Writer writer () {
		return new CharArrayWriter();
	}

	/** Gets the character at the specified index.
	 * @see #setCharAt(int, char)
	 * @see #deleteCharAt(int)
	 * @return The character at the index
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public char charAt (int index) {
		return items[index];
	}

	/** Retrieves the Unicode code point value at the {@code index}.
	 * @param index the index to the {@code char} code unit.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to {@link #size}.
	 * @see Character
	 * @see Character#codePointAt(char[], int, int) */
	public int codePointAt (int index) {
		validateIndex(index);
		return Character.codePointAt(items, index, size);
	}

	/** Retrieves the Unicode code point value that precedes the {@code index}.
	 * @param index the index to the {@code char} code unit within this object.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is less than 1 or greater than {@link #size}.
	 * @see Character
	 * @see Character#codePointBefore(char[], int, int) */
	public int codePointBefore (int index) {
		if (index < 1 || index > size) throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
		return Character.codePointBefore(items, index);
	}

	/** Calculates the number of Unicode code points between {@code begin} and {@code end}.
	 * @param begin the inclusive beginning index of the subsequence.
	 * @param end the exclusive end index of the subsequence.
	 * @return the number of Unicode code points in the subsequence.
	 * @throws IndexOutOfBoundsException if {@code begin} is negative or greater than {@code end} or {@code end} is greater than
	 *            {@link #size}.
	 * @see Character
	 * @see Character#codePointCount(char[], int, int) */
	public int codePointCount (int begin, int end) {
		if (begin < 0 || end > size || begin > end) throw new IndexOutOfBoundsException();
		return Character.codePointCount(items, begin, end - begin);
	}

	/** Returns the index that is offset {@code codePointOffset} code points from {@code index}.
	 * @param index the index to calculate the offset from.
	 * @param codePointOffset the number of code points to count.
	 * @return the index that is {@code codePointOffset} code points away from index.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than {@link #size} or if there aren't enough code
	 *            points before or after {@code index} to match {@code codePointOffset}.
	 * @see Character
	 * @see Character#offsetByCodePoints(char[], int, int, int, int) */
	public int offsetByCodePoints (int index, int codePointOffset) {
		return Character.offsetByCodePoints(items, 0, size, index, codePointOffset);
	}

	/** Tests if this CharArray contains the specified string.
	 * @return true if this CharArray contains the string */
	public boolean contains (String str) {
		return indexOf(str, 0) >= 0;
	}

	public boolean containsIgnoreCase (String str) {
		return indexOfIgnoreCase(str, 0) != -1;
	}

	/** Deletes the characters between the two specified indices.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray delete (int start, int end) {
		int actualEnd = validateRange(start, end);
		int length = actualEnd - start;
		if (length > 0) delete(start, actualEnd, length);
		return this;
	}

	/** Deletes the character wherever it occurs in the CharArray. */
	public CharArray deleteAll (char ch) {
		for (int i = 0; i < size; i++) {
			if (items[i] == ch) {
				int start = i;
				while (++i < size)
					if (items[i] != ch) break;
				int length = i - start;
				delete(start, i, length);
				i -= length;
			}
		}
		return this;
	}

	/** Deletes the string wherever it occurs in the CharArray. */
	public CharArray deleteAll (String str) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		int length = str.length();
		if (length > 0) {
			int index = indexOf(str, 0);
			while (index >= 0) {
				delete(index, index + length, length);
				index = indexOf(str, index);
			}
		}
		return this;
	}

	/** Deletes the character at the specified index.
	 * @see #charAt(int)
	 * @see #setCharAt(int, char)
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray deleteCharAt (int index) {
		validateIndex(index);
		delete(index, index + 1, 1);
		return this;
	}

	/** Deletes the character wherever it occurs in the CharArray. */
	public CharArray deleteFirst (char ch) {
		for (int i = 0; i < size; i++) {
			if (items[i] == ch) {
				delete(i, i + 1, 1);
				break;
			}
		}
		return this;
	}

	/** Deletes the string wherever it occurs in the CharArray. */
	public CharArray deleteFirst (String str) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		int length = str.length();
		if (length > 0) {
			int index = indexOf(str, 0);
			if (index >= 0) delete(index, index + length, length);
		}
		return this;
	}

	/** Internal method to delete a range without validation.
	 * @param start the start index
	 * @param end the end index (exclusive)
	 * @param length the length
	 * @throws IndexOutOfBoundsException if any index is invalid */
	private void delete (int start, int end, int length) {
		System.arraycopy(items, end, items, start, size - end);
		size -= length;
	}

	/** Gets the character at the specified index before deleting it.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int)
	 * @return The character at the index
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public char drainChar (int index) {
		validateIndex(index);
		char c = items[index];
		deleteCharAt(index);
		return c;
	}

	/** Drains (copies, then deletes) this character sequence into the specified array. This is equivalent to copying the
	 * characters from this sequence into the target and then deleting those character from this sequence.
	 * @param start first index to copy, inclusive.
	 * @param end last index to copy, exclusive.
	 * @param target the target array.
	 * @param targetIndex the index to start copying in the target.
	 * @return How many characters were copied (then deleted). If this CharArray is empty, return {@code 0}. */
	public int drainChars (int start, int end, char[] target, int targetIndex) {
		int length = end - start;
		if (isEmpty() || length == 0 || target.length == 0) return 0;
		int actualLength = Math.min(Math.min(size, length), target.length - targetIndex);
		getChars(start, start + actualLength, target, targetIndex);
		delete(start, start + actualLength);
		return actualLength;
	}

	/** Checks whether this CharArray ends with the specified string.
	 * @return true if this CharArray ends with the string */
	public boolean endsWith (String str) {
		int length = str.length();
		if (length == 0) return true;
		if (length > size) return false;
		int pos = size - length;
		for (int i = 0; i < length; i++, pos++)
			if (items[pos] != str.charAt(i)) return false;
		return true;
	}

	/** Copies this character array into the specified array.
	 * @param target the target array, null will cause an array to be created
	 * @return The input array, unless that was null or too small */
	public char[] getChars (@Null char[] target) {
		int length = size;
		if (target == null || target.length < length) target = new char[length];
		System.arraycopy(items, 0, target, 0, length);
		return target;
	}

	/** Copies this character array into the specified array.
	 * @param start first index to copy, inclusive.
	 * @param end last index to copy, exclusive.
	 * @param target the target array, must not too small.
	 * @param targetIndex the index to start copying in target.
	 * @throws NullPointerException if the array is null.
	 * @throws IndexOutOfBoundsException if any index is invalid. */
	public void getChars (int start, int end, char[] target, int targetIndex) {
		if (start < 0) throw new IndexOutOfBoundsException("start: " + start);
		if (end < 0 || end > size) throw new IndexOutOfBoundsException("end: " + end + ", size: " + size);
		if (start > end) throw new IndexOutOfBoundsException("end < start");
		System.arraycopy(items, start, target, targetIndex, end - start);
	}

	/** Searches this CharArray to find the first reference to the specified char.
	 * @param start the index to start at, invalid index rounded to edge
	 * @return The first index of the character, or -1 if not found */
	public int indexOf (char ch, int start) {
		start = Math.max(0, start);
		if (start >= size) return -1;
		char[] thisBuf = items;
		for (int i = start, n = size; i < n; i++)
			if (thisBuf[i] == ch) return i;
		return -1;
	}

	/** Searches this CharArray to find the first reference to the specified string.
	 * @return The first index of the string, or -1 if not found */
	public int indexOf (String str) {
		return indexOf(str, 0);
	}

	/** Searches this CharArray to find the first reference to the specified string starting searching from the given index.
	 * @param start the index to start at, invalid index rounded to edge
	 * @return The first index of the string, or -1 if not found */
	public int indexOf (String str, int start) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		start = Math.max(0, start);
		if (start >= size) return -1;
		int strLen = str.length();
		if (strLen == 1) return indexOf(str.charAt(0), start);
		if (strLen == 0) return start;
		if (strLen > size) return -1;
		char[] thisBuf = items;
		int searchLen = size - strLen + 1;
		for (int i = start; i < searchLen; i++) {
			boolean found = true;
			for (int j = 0; j < strLen && found; j++)
				found = str.charAt(j) == thisBuf[i + j];
			if (found) return i;
		}
		return -1;
	}

	public int indexOfIgnoreCase (String str, int start) {
		if (start < 0) start = 0;
		int length = str.length();
		if (length == 0) return start < size || start == 0 ? start : size;
		int maxIndex = size - length;
		if (start > maxIndex) return -1;
		char firstUpper = Character.toUpperCase(str.charAt(0));
		char firstLower = Character.toLowerCase(firstUpper);
		while (true) {
			int i = start;
			boolean found = false;
			for (; i <= maxIndex; i++) {
				char c = items[i];
				if (c == firstUpper || c == firstLower) {
					found = true;
					break;
				}
			}
			if (!found) return -1;
			int o1 = i, o2 = 0;
			while (++o2 < length) {
				char c = items[++o1];
				char upper = Character.toUpperCase(str.charAt(o2));
				if (c != upper && c != Character.toLowerCase(upper)) break;
			}
			if (o2 == length) return i;
			start = i + 1;
		}
	}

	/** Inserts the value into this CharArray.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, boolean value) {
		validateIndex(index);
		if (value) {
			require(TRUE_STRING_SIZE);
			System.arraycopy(items, index, items, index + TRUE_STRING_SIZE, size - index);
			appendTrue(index);
		} else {
			require(FALSE_STRING_SIZE);
			System.arraycopy(items, index, items, index + FALSE_STRING_SIZE, size - index);
			appendFalse(index);
		}
		return this;
	}

	public void insert (int index, char value) {
		validateIndex(index);
		require(1);
		char[] items = this.items;
		if (ordered)
			System.arraycopy(items, index, items, index + 1, size - index);
		else
			items[size] = items[index];
		size++;
		items[index] = value;
	}

	/** Inserts the specified number of items at the specified index. The new items will have values equal to the values at those
	 * indices before the insertion. */
	public void insertRange (int index, int count) {
		validateIndex(index);
		int sizeNeeded = size + count;
		if (sizeNeeded > items.length) items = resize(Math.max(Math.max(8, sizeNeeded), (int)(size * 1.75f)));
		System.arraycopy(items, index, items, index + count, size - index);
		size = sizeNeeded;
	}

	/** Inserts the character array into this CharArray. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, @Null char[] ch) {
		validateIndex(index);
		if (ch == null) return insert(index, NULL);
		int length = ch.length;
		if (length > 0) {
			require(length);
			System.arraycopy(items, index, items, index + length, size - index);
			System.arraycopy(ch, 0, items, index, length);
			size += length;
		}
		return this;
	}

	/** Inserts part of the character array into this CharArray. Inserting null will use {@code "null"}.
	 * @param offset the offset into the character array to start at
	 * @param length the length of the character array part to copy
	 * @throws IndexOutOfBoundsException if any index is invalid */
	public CharArray insert (int index, @Null char[] ch, int offset, int length) {
		validateIndex(index);
		if (ch == null) return insert(index, NULL);
		if (offset < 0 || offset > ch.length) throw new IndexOutOfBoundsException("Invalid offset: " + offset);
		if (length < 0 || offset + length > ch.length) throw new IndexOutOfBoundsException("Invalid length: " + length);
		if (length > 0) {
			require(length);
			System.arraycopy(items, index, items, index + length, size - index);
			System.arraycopy(ch, offset, items, index, length);
			size += length;
		}
		return this;
	}

	/** Inserts the value into this CharArray.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, double value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this CharArray.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, float value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this CharArray.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, int value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this CharArray.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, long value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the string representation of an object into this CharArray. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, @Null Object obj) {
		if (obj == null) return insert(index, NULL);
		return insert(index, obj.toString());
	}

	/** Inserts the string into this CharArray. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray insert (int index, @Null String str) {
		validateIndex(index);
		if (str == null) str = NULL;
		int strLength = str.length();
		if (strLength > 0) {
			require(strLength);
			System.arraycopy(items, index, items, index + strLength, size - index);
			size += strLength;
			str.getChars(0, strLength, items, index);
		}
		return this;
	}

	/** Searches this CharArray to find the last reference to the specified char.
	 * @param start the index to start at, invalid index rounded to edge
	 * @return The last index of the character, or -1 if not found */
	public int lastIndexOf (char ch, int start) {
		start = start >= size ? size - 1 : start;
		if (start < 0) return -1;
		for (int i = start; i >= 0; i--)
			if (items[i] == ch) return i;
		return -1;
	}

	/** Searches this CharArray to find the last reference to the specified string.
	 * @return The last index of the string, or -1 if not found */
	public int lastIndexOf (String str) {
		return lastIndexOf(str, size - 1);
	}

	/** Searches this CharArray to find the last reference to the specified string starting searching from the given index.
	 * @param start the index to start at, invalid index rounded to edge
	 * @return The last index of the string, or -1 if not found */
	public int lastIndexOf (String str, int start) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		start = start >= size ? size - 1 : start;
		if (start < 0) return -1;
		int strLen = str.length();
		if (strLen == 0) return start;
		if (strLen > size) return -1;
		if (strLen == 1) return lastIndexOf(str.charAt(0), start);
		for (int i = start - strLen + 1; i >= 0; i--) {
			boolean found = true;
			for (int j = 0; j < strLen && found; j++)
				found = str.charAt(j) == items[i + j];
			if (found) return i;
		}
		return -1;
	}

	/** Extracts the leftmost characters from this CharArray without throwing an exception.
	 * <p>
	 * This method extracts the left {@code length} characters from this CharArray. If this many characters are not available, the
	 * whole CharArray is returned. Thus the returned string may be shorter than the length requested.
	 * @param length the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String leftString (int length) {
		if (length <= 0) return "";
		if (length >= size) return new String(items, 0, size);
		return new String(items, 0, length);
	}

	/** Gets the length of this CharArray.
	 * @return The length */
	public int length () {
		return size;
	}

	public int capacity () {
		return items.length;
	}

	/** Extracts some characters from the middle of this CharArray without throwing an exception.
	 * <p>
	 * This method extracts {@code length} characters from this CharArray at the specified index. If the index is negative it is
	 * treated as zero. If the index is greater than this CharArray size, it is treated as this CharArray size. If the length is
	 * negative, the empty string is returned. If insufficient characters are available in this CharArray, as much as possible is
	 * returned. Thus the returned string may be shorter than the length requested.
	 * @param index the index to start at, negative means zero
	 * @param length the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String midString (int index, int length) {
		if (index < 0) index = 0;
		if (length <= 0 || index >= size) return "";
		if (size <= index + length) return new String(items, index, size - index);
		return new String(items, index, length);
	}

	/** If possible, reads chars from the provided {@link CharBuffer} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read.
	 * @see #appendTo(Appendable) */
	public int readFrom (CharBuffer charBuffer) {
		int oldSize = size;
		int remaining = charBuffer.remaining();
		require(remaining);
		charBuffer.get(items, size, remaining);
		size += remaining;
		return size - oldSize;
	}

	/** If possible, reads all chars from the provided {@link Readable} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read
	 * @throws IOException if an I/O error occurs.
	 * @see #appendTo(Appendable) */
	public int readFrom (Readable readable) throws IOException {
		if (readable instanceof Reader) return readFrom((Reader)readable);
		if (readable instanceof CharBuffer) return readFrom((CharBuffer)readable);
		int oldSize = size;
		while (true) {
			require(1);
			CharBuffer buf = CharBuffer.wrap(items, size, items.length - size);
			int read = readable.read(buf);
			if (read == EOS) break;
			size += read;
		}
		return size - oldSize;
	}

	/** If possible, reads all chars from the provided {@link Reader} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read or -1 if we reached the end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @see #appendTo(Appendable) */
	public int readFrom (Reader reader) throws IOException {
		int oldSize = size;
		require(1);
		int readCount = reader.read(items, size, items.length - size);
		if (readCount == EOS) return EOS;
		do {
			size += readCount;
			require(1);
			readCount = reader.read(items, size, items.length - size);
		} while (readCount != EOS);
		return size - oldSize;
	}

	/** If possible, reads {@code count} chars from the provided {@link Reader} directly into underlying character buffer without
	 * making extra copies.
	 * @param count The maximum characters to read, a value &lt;= 0 returns 0.
	 * @return The number of characters read. If less than {@code count}, then we've reached the end-of-stream, or -1 if we reached
	 *         the end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @see #appendTo(Appendable) */
	public int readFrom (Reader reader, int count) throws IOException {
		if (count <= 0) return 0;
		int oldSize = size;
		require(count);
		int target = count;
		int readCount = reader.read(items, size, target);
		if (readCount == EOS) return EOS;
		do {
			target -= readCount;
			size += readCount;
			readCount = reader.read(items, size, target);
		} while (target > 0 && readCount != EOS);
		return size - oldSize;
	}

	/** Replaces a portion of this CharArray with another string. The length of the inserted string does not have to match the
	 * removed length.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive
	 * @param replaceStr the string to replace with
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray replace (int start, int end, String replaceStr) {
		end = validateRange(start, end);
		replace(start, end, end - start, replaceStr, replaceStr.length());
		return this;
	}

	/** Replaces the search string with the replace string throughout this CharArray. */
	public CharArray replaceAll (String searchStr, String replaceStr) {
		int searchLength = searchStr.length();
		if (searchLength > 0) {
			int replaceLength = replaceStr.length();
			int index = indexOf(searchStr, 0);
			while (index >= 0) {
				replace(index, index + searchLength, searchLength, replaceStr, replaceLength);
				index = indexOf(searchStr, index + replaceLength);
			}
		}
		return this;
	}

	/** Replaces the search char with the replace string throughout this CharArray. */
	public CharArray replace (char find, String replace) {
		int replaceLength = replace.length();
		int index = 0;
		while (true) {
			while (true) {
				if (index == size) return this;
				if (items[index] == find) break;
				index++;
			}
			replace(index, index + 1, 1, replace, replaceLength);
			index += replaceLength;
		}
	}

	/** Replaces the first instance of the search string with the replace string.
	 * @param replaceStr the replace string, null is equivalent to an empty string */
	public CharArray replaceFirst (String searchStr, String replaceStr) {
		int searchLength = searchStr.length();
		if (searchLength > 0) {
			int index = indexOf(searchStr, 0);
			if (index >= 0) {
				int replaceLength = replaceStr.length();
				replace(index, index + searchLength, searchLength, replaceStr, replaceLength);
			}
		}
		return this;
	}

	/** Internal method to delete a range without validation.
	 * @param start the start index
	 * @param end the end index (exclusive)
	 * @param removeLength the length to remove (end - start)
	 * @param insertStr the string to replace with, can be null if insertLen is 0
	 * @param insertLength the length of the insert string
	 * @throws IndexOutOfBoundsException if any index is invalid */
	private void replace (int start, int end, int removeLength, @Null String insertStr, int insertLength) {
		int newSize = size - removeLength + insertLength;
		if (insertLength != removeLength) {
			require(newSize);
			System.arraycopy(items, end, items, start + insertLength, size - end);
			size = newSize;
		}
		if (insertLength > 0) insertStr.getChars(0, insertLength, items, start);
	}

	public void reverse () {
		char[] items = this.items;
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			char temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	/** Reverses this CharArray, keeping surrogate pairs together. */
	public CharArray reverseCodePoints () {
		if (size < 2) return this;
		int end = size - 1;
		char frontHigh = items[0];
		char endLow = items[end];
		boolean allowFrontSur = true, allowEndSur = true;
		for (int i = 0, mid = size / 2; i < mid; i++, --end) {
			char frontLow = items[i + 1];
			char endHigh = items[end - 1];
			boolean surAtFront = allowFrontSur && frontLow >= 0xdc00 && frontLow <= 0xdfff && frontHigh >= 0xd800
				&& frontHigh <= 0xdbff;
			if (surAtFront && size < 3) return this;
			boolean surAtEnd = allowEndSur && endHigh >= 0xd800 && endHigh <= 0xdbff && endLow >= 0xdc00 && endLow <= 0xdfff;
			allowFrontSur = allowEndSur = true;
			if (surAtFront == surAtEnd) {
				if (surAtFront) { // both surrogates
					items[end] = frontLow;
					items[end - 1] = frontHigh;
					items[i] = endHigh;
					items[i + 1] = endLow;
					frontHigh = items[i + 2];
					endLow = items[end - 2];
					i++;
					end--;
				} else { // neither surrogates
					items[end] = frontHigh;
					items[i] = endLow;
					frontHigh = frontLow;
					endLow = endHigh;
				}
			} else if (surAtFront) { // surrogate only at the front
				items[end] = frontLow;
				items[i] = endLow;
				endLow = endHigh;
				allowFrontSur = false;
			} else { // surrogate only at the end
				items[end] = frontHigh;
				items[i] = endHigh;
				frontHigh = frontLow;
				allowEndSur = false;
			}
		}
		if ((size & 1) == 1 && (!allowFrontSur || !allowEndSur)) items[end] = allowFrontSur ? endLow : frontHigh;
		return this;
	}

	/** Extracts the rightmost characters from this CharArray without throwing an exception.
	 * <p>
	 * This method extracts the right {@code length} characters from this CharArray. If this many characters are not available, the
	 * whole CharArray is returned. Thus the returned string may be shorter than the length requested.
	 * @param length the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String rightString (int length) {
		if (length <= 0) return "";
		if (length >= size) return new String(items, 0, size);
		return new String(items, size - length, length);
	}

	/** Clears and sets this CharArray to the given value.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int) */
	public CharArray set (CharSequence str) {
		clear();
		append(str);
		return this;
	}

	/** Sets the character at the specified index.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int)
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public CharArray setCharAt (int index, char ch) {
		validateIndex(index);
		items[index] = ch;
		return this;
	}

	/** Updates the length of this CharArray by either dropping the last characters or adding filler of Unicode zero.
	 * @param length the length to set to, must be zero or positive
	 * @throws IndexOutOfBoundsException if the length is negative */
	public CharArray setLength (int length) {
		if (length < 0) throw new IndexOutOfBoundsException("length: " + length);
		if (length < size)
			size = length;
		else if (length > size) {
			require(length - size);
			int oldEnd = size;
			size = length;
			Arrays.fill(items, oldEnd, length, '\0');
		}
		return this;
	}

	/** Checks whether this CharArray starts with the specified string.
	 * @param str the string to search for, null returns false
	 * @return true if this CharArray starts with the string */
	public boolean startsWith (String str) {
		int length = str.length();
		if (length == 0) return true;
		if (length > size) return false;
		for (int i = 0; i < length; i++)
			if (items[i] != str.charAt(i)) return false;
		return true;
	}

	public CharSequence subSequence (int start, int end) {
		validateRange(start, end);
		return substring(start, end);
	}

	/** Extracts a portion of this CharArray as a string.
	 * @param start the start index, inclusive
	 * @return The new string
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public String substring (int start) {
		return substring(start, size);
	}

	/** Extracts a portion of this CharArray as a string.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive
	 * @return The new string
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public String substring (int start, int end) {
		end = validateRange(start, end);
		return new String(items, start, end - start);
	}

	/** Copies this CharArray's character array into a new character array.
	 * @return a new array that represents the contents of this CharArray */
	public char[] toCharArray () {
		return Arrays.copyOf(items, size);
	}

	/** Copies part of this CharArray's character array into a new character array.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive
	 * @return a new array that holds part of the contents of this CharArray
	 * @throws IndexOutOfBoundsException if start is invalid, or if end is invalid (but end greater than size is valid) */
	public char[] toCharArray (int start, int end) {
		end = validateRange(start, end);
		return Arrays.copyOfRange(items, start, end);
	}

	/** Returns a String version of this CharArray, creating a new instance each time the method is called. */
	public String toString () {
		if (size == 0) return "";
		return new String(items, 0, size);
	}

	public String toString (String separator) {
		if (size == 0) return "";
		char[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}

	/** Returns the current String representation and clears this CharArray.
	 * @return a String containing the characters in this instance. */
	public String toStringAndClear () {
		String string = toString();
		clear();
		return string;
	}

	/** Trims this CharArray by removing characters less than or equal to a space from the beginning and end. */
	public CharArray trim () {
		if (size == 0) return this;
		int length = size;
		char[] buf = items;
		int pos = 0;
		while (pos < length && buf[pos] <= ' ')
			pos++;
		while (pos < length && buf[length - 1] <= ' ')
			length--;
		if (length < size) delete(length, size);
		if (pos > 0) delete(0, pos);
		return this;
	}

	/** Validates that an index is in the range {@code 0 <= index <= size}.
	 * @throws IndexOutOfBoundsException Thrown when the index is not the range {@code 0 <= index <= size}. */
	protected void validateIndex (int index) {
		if (index < 0 || index > size) throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
	}

	/** Validates parameters defining a range of this CharArray.
	 * @param start the start index, inclusive
	 * @param end the end index, exclusive
	 * @return A valid end index.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	protected int validateRange (int start, int end) {
		if (start < 0) throw new IndexOutOfBoundsException("start: " + start);
		if (end > size) throw new IndexOutOfBoundsException("end: " + end + ", size: " + size);
		if (start > end) throw new IndexOutOfBoundsException("start: " + start + ", end: " + end);
		return end;
	}

	/** Tests the contents of this CharArray against another to see if they contain the same character content.
	 * @param obj the object to check, null returns false
	 * @return true if the CharArrays contain the same characters in the same order */
	/** Returns false if either array is unordered. */
	public boolean equals (Object object) {
		if (this == object) return true;
		if (!ordered) return false;
		if (object == null) return false;
		if (!(object instanceof CharArray)) return false;
		CharArray other = (CharArray)object;
		if (!other.ordered) return false;
		int length = this.size;
		if (length != other.size) return false;
		char[] chars = this.items, chars2 = other.items;
		for (int i = 0; i < length; i++)
			if (chars[i] != chars2[i]) return false;
		return true;
	}

	/** Tests the contents of this CharArray against another to see if they contain the same character content.
	 * @param other the object to check, null returns false
	 * @return true if the CharArrays contain the same characters in the same order */
	public boolean equals (CharArray other) {
		if (this == other) return true;
		if ((other == null) || (size != other.size)) return false;
		int length = this.size;
		if (length != other.size) return false;
		char[] chars = this.items, chars2 = other.items;
		for (int i = 0; i < length; i++)
			if (chars[i] != chars2[i]) return false;
		return true;
	}

	/** Tests the contents of this CharArray against another to see if they contain the same character content ignoring case.
	 * @param other the object to check, null returns false
	 * @return true if the CharArrays contain the same characters in the same order */
	public boolean equalsIgnoreCase (CharArray other) {
		if (this == other) return true;
		if (other == null) return false;
		int length = this.size;
		if (length != other.size) return false;
		char[] chars = this.items, chars2 = other.items;
		for (int i = 0; i < length; i++) {
			char c = chars[i];
			char upper = Character.toUpperCase(chars2[i]);
			if (c != upper && c != Character.toLowerCase(upper)) return false;
		}
		return true;
	}

	public boolean equalsString (@Null String other) {
		if (other == null) return false;
		int length = this.size;
		if (length != other.length()) return false;
		char[] chars = this.items;
		for (int i = 0; i < length; i++)
			if (chars[i] != other.charAt(i)) return false;
		return true;
	}

	public boolean equalsIgnoreCase (@Null String other) {
		if (other == null) return false;
		int length = this.size;
		if (length != other.length()) return false;
		char[] chars = this.items;
		for (int i = 0; i < length; i++) {
			char c = chars[i];
			char upper = Character.toUpperCase(other.charAt(i));
			if (c != upper && c != Character.toLowerCase(upper)) return false;
		}
		return true;
	}

	public int hashCode () {
		if (!ordered) return super.hashCode();
		char[] chars = this.items;
		int result = 31 + size;
		for (int index = 0; index < size; ++index)
			result = 31 * result + chars[index];
		return result;
	}

	/** Constructs an instance from a reference to a character array. Changes to the input chars are reflected in this instance
	 * until the internal buffer needs to be reallocated. Using a reference to an array allows the instance to be initialized
	 * without copying the input array.
	 * @param initialBuffer The initial array that will back the new CharArray.
	 * @return A new instance. */
	static public CharArray wrap (char[] initialBuffer) {
		return new CharArray(initialBuffer, initialBuffer.length);
	}

	/** Constructs an instance from a reference to a character array. Changes to the input chars are reflected in this instance
	 * until the internal buffer needs to be reallocated. Using a reference to an array allows the instance to be initialized
	 * without copying the input array.
	 * @param initialBuffer The initial array that will back the new CharArray.
	 * @param length The length of the subarray to be used; must be non-negative and no larger than {@code initialBuffer.length}.
	 *           The new CharArray's size will be set to {@code length}.
	 * @return A new instance. */
	static public CharArray wrap (char[] initialBuffer, int length) {
		return new CharArray(initialBuffer, length);
	}

	/** @see #CharArray(char[]) */
	static public CharArray with (char... array) {
		return new CharArray(array);
	}

	/** @return the number of characters required to represent the value with the specified radix */
	static public int numChars (int value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0)
			++result;
		return result;
	}

	/** @return the number of characters required to represent the value with the specified radix */
	static public int numChars (long value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0)
			++result;
		return result;
	}

	class CharArrayReader extends Reader {
		private int mark;
		private int pos;

		CharArrayReader () {
		}

		public void close () {
		}

		public void mark (int readAheadLimit) {
			mark = pos;
		}

		public boolean markSupported () {
			return true;
		}

		public int read () {
			if (!ready()) return -1;
			return charAt(pos++);
		}

		public int read (char[] b, int off, int length) {
			if (off < 0 || length < 0 || off > b.length || off + length > b.length || off + length < 0)
				throw new IndexOutOfBoundsException();
			if (length == 0) return 0;
			if (pos >= size) return -1;
			if (pos + length > size) length = size - pos;
			CharArray.this.getChars(pos, pos + length, b, off);
			pos += length;
			return length;
		}

		public boolean ready () {
			return pos < size;
		}

		public void reset () {
			pos = mark;
		}

		public long skip (long n) {
			if (pos + n > size) n = size - pos;
			if (n < 0) return 0;
			pos += (int)n;
			return n;
		}
	}

	class CharArrayWriter extends Writer {
		CharArrayWriter () {
		}

		public void close () {
		}

		public void flush () {
		}

		public void write (char[] cbuf) {
			CharArray.this.append(cbuf);
		}

		public void write (char[] cbuf, int off, int length) {
			CharArray.this.append(cbuf, off, length);
		}

		public void write (int c) {
			CharArray.this.append((char)c);
		}

		public void write (String str) {
			CharArray.this.append(str);
		}

		public void write (String str, int off, int length) {
			CharArray.this.append(str, off, length);
		}
	}
}
