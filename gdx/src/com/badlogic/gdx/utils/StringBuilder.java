/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.utils;

import java.util.Arrays;

/** A {@link java.lang.StringBuilder} that implements equals and hashcode.
 * @see CharSequence
 * @see Appendable
 * @see java.lang.StringBuilder
 * @see String */
public class StringBuilder implements Appendable, CharSequence {
	static final int INITIAL_CAPACITY = 16;

	public char[] chars;
	public int length;

	private static final char[] digits = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
	
	/** @return the number of characters required to represent the specified value with the specified radix */ 
	public static int numChars(int value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0) ++result;
		return result;
	}
	
	/** @return the number of characters required to represent the specified value with the specified radix */
	public static int numChars(long value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0) ++result;
		return result;
	}

	/*
	 * Returns the character array.
	 */
	final char[] getValue () {
		return chars;
	}

	/** Constructs an instance with an initial capacity of {@code 16}.
	 * 
	 * @see #capacity() */
	public StringBuilder () {
		chars = new char[INITIAL_CAPACITY];
	}

	/** Constructs an instance with the specified capacity.
	 * 
	 * @param capacity the initial capacity to use.
	 * @throws NegativeArraySizeException if the specified {@code capacity} is negative.
	 * @see #capacity() */
	public StringBuilder (int capacity) {
		if (capacity < 0) {
			throw new NegativeArraySizeException();
		}
		chars = new char[capacity];
	}

	/** Constructs an instance that's initialized with the contents of the specified {@code CharSequence}. The capacity of the new
	 * builder will be the length of the {@code CharSequence} plus 16.
	 * 
	 * @param seq the {@code CharSequence} to copy into the builder.
	 * @throws NullPointerException if {@code seq} is {@code null}. */
	public StringBuilder (CharSequence seq) {
		this(seq.toString());
	}

	public StringBuilder (StringBuilder builder) {
		length = builder.length;
		chars = new char[length + INITIAL_CAPACITY];
		System.arraycopy(builder.chars, 0, chars, 0, length);
	}

	/** Constructs an instance that's initialized with the contents of the specified {@code String}. The capacity of the new builder
	 * will be the length of the {@code String} plus 16.
	 * 
	 * @param string the {@code String} to copy into the builder.
	 * @throws NullPointerException if {@code str} is {@code null}. */
	public StringBuilder (String string) {
		length = string.length();
		chars = new char[length + INITIAL_CAPACITY];
		string.getChars(0, length, chars, 0);
	}

	private void enlargeBuffer (int min) {
		int newSize = (chars.length >> 1) + chars.length + 2;
		char[] newData = new char[min > newSize ? min : newSize];
		System.arraycopy(chars, 0, newData, 0, length);
		chars = newData;
	}

	final void appendNull () {
		int newSize = length + 4;
		if (newSize > chars.length) {
			enlargeBuffer(newSize);
		}
		chars[length++] = 'n';
		chars[length++] = 'u';
		chars[length++] = 'l';
		chars[length++] = 'l';
	}

	final void append0 (char[] value) {
		int newSize = length + value.length;
		if (newSize > chars.length) {
			enlargeBuffer(newSize);
		}
		System.arraycopy(value, 0, chars, length, value.length);
		length = newSize;
	}

	final void append0 (char[] value, int offset, int length) {
		// Force null check of chars first!
		if (offset > value.length || offset < 0) {
			throw new ArrayIndexOutOfBoundsException("Offset out of bounds: " + offset);
		}
		if (length < 0 || value.length - offset < length) {
			throw new ArrayIndexOutOfBoundsException("Length out of bounds: " + length);
		}

		int newSize = this.length + length;
		if (newSize > chars.length) {
			enlargeBuffer(newSize);
		}
		System.arraycopy(value, offset, chars, this.length, length);
		this.length = newSize;
	}

	final void append0 (char ch) {
		if (length == chars.length) {
			enlargeBuffer(length + 1);
		}
		chars[length++] = ch;
	}

	final void append0 (String string) {
		if (string == null) {
			appendNull();
			return;
		}
		int adding = string.length();
		int newSize = length + adding;
		if (newSize > chars.length) {
			enlargeBuffer(newSize);
		}
		string.getChars(0, adding, chars, length);
		length = newSize;
	}

	final void append0 (CharSequence s, int start, int end) {
		if (s == null) {
			s = "null";
		}
		if (start < 0 || end < 0 || start > end || end > s.length()) {
			throw new IndexOutOfBoundsException();
		}

		append0(s.subSequence(start, end).toString());
	}

	/** Returns the number of characters that can be held without growing.
	 * 
	 * @return the capacity
	 * @see #ensureCapacity
	 * @see #length */
	public int capacity () {
		return chars.length;
	}

	/** Retrieves the character at the {@code index}.
	 * 
	 * @param index the index of the character to retrieve.
	 * @return the char value.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to the current {@link #length()}. */
	public char charAt (int index) {
		if (index < 0 || index >= length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return chars[index];
	}

	final void delete0 (int start, int end) {
		if (start >= 0) {
			if (end > length) {
				end = length;
			}
			if (end == start) {
				return;
			}
			if (end > start) {
				int count = length - end;
				if (count >= 0) System.arraycopy(chars, end, chars, start, count);
				length -= end - start;
				return;
			}
		}
		throw new StringIndexOutOfBoundsException();
	}

	final void deleteCharAt0 (int location) {
		if (0 > location || location >= length) {
			throw new StringIndexOutOfBoundsException(location);
		}
		int count = length - location - 1;
		if (count > 0) {
			System.arraycopy(chars, location + 1, chars, location, count);
		}
		length--;
	}

	/** Ensures that this object has a minimum capacity available before requiring the internal buffer to be enlarged. The general
	 * policy of this method is that if the {@code minimumCapacity} is larger than the current {@link #capacity()}, then the
	 * capacity will be increased to the largest value of either the {@code minimumCapacity} or the current capacity multiplied by
	 * two plus two. Although this is the general policy, there is no guarantee that the capacity will change.
	 * 
	 * @param min the new minimum capacity to set. */
	public void ensureCapacity (int min) {
		if (min > chars.length) {
			int twice = (chars.length << 1) + 2;
			enlargeBuffer(twice > min ? twice : min);
		}
	}

	/** Copies the requested sequence of characters to the {@code char[]} passed starting at {@code destStart}.
	 * 
	 * @param start the inclusive start index of the characters to copy.
	 * @param end the exclusive end index of the characters to copy.
	 * @param dest the {@code char[]} to copy the characters to.
	 * @param destStart the inclusive start index of {@code dest} to begin copying to.
	 * @throws IndexOutOfBoundsException if the {@code start} is negative, the {@code destStart} is negative, the {@code start} is
	 *            greater than {@code end}, the {@code end} is greater than the current {@link #length()} or
	 *            {@code destStart + end - begin} is greater than {@code dest.length}. */
	public void getChars (int start, int end, char[] dest, int destStart) {
		if (start > length || end > length || start > end) {
			throw new StringIndexOutOfBoundsException();
		}
		System.arraycopy(chars, start, dest, destStart, end - start);
	}

	final void insert0 (int index, char[] value) {
		if (0 > index || index > length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		if (value.length != 0) {
			move(value.length, index);
			System.arraycopy(value, 0, value, index, value.length);
			length += value.length;
		}
	}

	final void insert0 (int index, char[] value, int start, int length) {
		if (0 <= index && index <= length) {
			// start + length could overflow, start/length maybe MaxInt
			if (start >= 0 && 0 <= length && length <= value.length - start) {
				if (length != 0) {
					move(length, index);
					System.arraycopy(value, start, chars, index, length);
					this.length += length;
				}
				return;
			}
			throw new StringIndexOutOfBoundsException("offset " + start + ", length " + length + ", char[].length " + value.length);
		}
		throw new StringIndexOutOfBoundsException(index);
	}

	final void insert0 (int index, char ch) {
		if (0 > index || index > length) {
			// RI compatible exception type
			throw new ArrayIndexOutOfBoundsException(index);
		}
		move(1, index);
		chars[index] = ch;
		length++;
	}

	final void insert0 (int index, String string) {
		if (0 <= index && index <= length) {
			if (string == null) {
				string = "null";
			}
			int min = string.length();
			if (min != 0) {
				move(min, index);
				string.getChars(0, min, chars, index);
				length += min;
			}
		} else {
			throw new StringIndexOutOfBoundsException(index);
		}
	}

	final void insert0 (int index, CharSequence s, int start, int end) {
		if (s == null) {
			s = "null";
		}
		if (index < 0 || index > length || start < 0 || end < 0 || start > end || end > s.length()) {
			throw new IndexOutOfBoundsException();
		}
		insert0(index, s.subSequence(start, end).toString());
	}

	/** The current length.
	 * 
	 * @return the number of characters contained in this instance. */
	public int length () {
		return length;
	}

	private void move (int size, int index) {
		if (chars.length - length >= size) {
			System.arraycopy(chars, index, chars, index + size, length - index); // index == count case is no-op
			return;
		}
		int a = length + size, b = (chars.length << 1) + 2;
		int newSize = a > b ? a : b;
		char[] newData = new char[newSize];
		System.arraycopy(chars, 0, newData, 0, index);
		// index == count case is no-op
		System.arraycopy(chars, index, newData, index + size, length - index);
		chars = newData;
	}

	final void replace0 (int start, int end, String string) {
		if (start >= 0) {
			if (end > length) {
				end = length;
			}
			if (end > start) {
				int stringLength = string.length();
				int diff = end - start - stringLength;
				if (diff > 0) { // replacing with fewer characters
					// index == count case is no-op
					System.arraycopy(chars, end, chars, start + stringLength, length - end);
				} else if (diff < 0) {
					// replacing with more characters...need some room
					move(-diff, end);
				}
				string.getChars(0, stringLength, chars, start);
				length -= diff;
				return;
			}
			if (start == end) {
				if (string == null) {
					throw new NullPointerException();
				}
				insert0(start, string);
				return;
			}
		}
		throw new StringIndexOutOfBoundsException();
	}

	final void reverse0 () {
		if (length < 2) {
			return;
		}
		int end = length - 1;
		char frontHigh = chars[0];
		char endLow = chars[end];
		boolean allowFrontSur = true, allowEndSur = true;
		for (int i = 0, mid = length / 2; i < mid; i++, --end) {
			char frontLow = chars[i + 1];
			char endHigh = chars[end - 1];
			boolean surAtFront = allowFrontSur && frontLow >= 0xdc00 && frontLow <= 0xdfff && frontHigh >= 0xd800
				&& frontHigh <= 0xdbff;
			if (surAtFront && length < 3) {
				return;
			}
			boolean surAtEnd = allowEndSur && endHigh >= 0xd800 && endHigh <= 0xdbff && endLow >= 0xdc00 && endLow <= 0xdfff;
			allowFrontSur = allowEndSur = true;
			if (surAtFront == surAtEnd) {
				if (surAtFront) {
					// both surrogates
					chars[end] = frontLow;
					chars[end - 1] = frontHigh;
					chars[i] = endHigh;
					chars[i + 1] = endLow;
					frontHigh = chars[i + 2];
					endLow = chars[end - 2];
					i++;
					end--;
				} else {
					// neither surrogates
					chars[end] = frontHigh;
					chars[i] = endLow;
					frontHigh = frontLow;
					endLow = endHigh;
				}
			} else {
				if (surAtFront) {
					// surrogate only at the front
					chars[end] = frontLow;
					chars[i] = endLow;
					endLow = endHigh;
					allowFrontSur = false;
				} else {
					// surrogate only at the end
					chars[end] = frontHigh;
					chars[i] = endHigh;
					frontHigh = frontLow;
					allowEndSur = false;
				}
			}
		}
		if ((length & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
			chars[end] = allowFrontSur ? endLow : frontHigh;
		}
	}

	/** Sets the character at the {@code index}.
	 * 
	 * @param index the zero-based index of the character to replace.
	 * @param ch the character to set.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to the current {@link #length()}. */
	public void setCharAt (int index, char ch) {
		if (0 > index || index >= length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		chars[index] = ch;
	}

	/** Sets the current length to a new value. If the new length is larger than the current length, then the new characters at the
	 * end of this object will contain the {@code char} value of {@code \u0000}.
	 * 
	 * @param newLength the new length of this StringBuilder.
	 * @exception IndexOutOfBoundsException if {@code length < 0}.
	 * @see #length */
	public void setLength (int newLength) {
		if (newLength < 0) {
			throw new StringIndexOutOfBoundsException(newLength);
		}
		if (newLength > chars.length) {
			enlargeBuffer(newLength);
		} else {
			if (length < newLength) {
				Arrays.fill(chars, length, newLength, (char)0);
			}
		}
		length = newLength;
	}

	/** Returns the String value of the subsequence from the {@code start} index to the current end.
	 * 
	 * @param start the inclusive start index to begin the subsequence.
	 * @return a String containing the subsequence.
	 * @throws StringIndexOutOfBoundsException if {@code start} is negative or greater than the current {@link #length()}. */
	public String substring (int start) {
		if (0 <= start && start <= length) {
			if (start == length) {
				return "";
			}

			// Remove String sharing for more performance
			return new String(chars, start, length - start);
		}
		throw new StringIndexOutOfBoundsException(start);
	}

	/** Returns the String value of the subsequence from the {@code start} index to the {@code end} index.
	 * 
	 * @param start the inclusive start index to begin the subsequence.
	 * @param end the exclusive end index to end the subsequence.
	 * @return a String containing the subsequence.
	 * @throws StringIndexOutOfBoundsException if {@code start} is negative, greater than {@code end} or if {@code end} is greater
	 *            than the current {@link #length()}. */
	public String substring (int start, int end) {
		if (0 <= start && start <= end && end <= length) {
			if (start == end) {
				return "";
			}

			// Remove String sharing for more performance
			return new String(chars, start, end - start);
		}
		throw new StringIndexOutOfBoundsException();
	}

	/** Returns the current String representation.
	 * 
	 * @return a String containing the characters in this instance. */
	@Override
	public String toString () {
		if (length == 0) return "";
		return new String(chars, 0, length);
	}

	/** Returns a {@code CharSequence} of the subsequence from the {@code start} index to the {@code end} index.
	 * 
	 * @param start the inclusive start index to begin the subsequence.
	 * @param end the exclusive end index to end the subsequence.
	 * @return a CharSequence containing the subsequence.
	 * @throws IndexOutOfBoundsException if {@code start} is negative, greater than {@code end} or if {@code end} is greater than
	 *            the current {@link #length()}.
	 * @since 1.4 */
	public CharSequence subSequence (int start, int end) {
		return substring(start, end);
	}

	/** Searches for the first index of the specified character. The search for the character starts at the beginning and moves
	 * towards the end.
	 * 
	 * @param string the string to find.
	 * @return the index of the specified character, -1 if the character isn't found.
	 * @see #lastIndexOf(String)
	 * @since 1.4 */
	public int indexOf (String string) {
		return indexOf(string, 0);
	}

	/** Searches for the index of the specified character. The search for the character starts at the specified offset and moves
	 * towards the end.
	 * 
	 * @param subString the string to find.
	 * @param start the starting offset.
	 * @return the index of the specified character, -1 if the character isn't found
	 * @see #lastIndexOf(String,int)
	 * @since 1.4 */
	public int indexOf (String subString, int start) {
		if (start < 0) {
			start = 0;
		}
		int subCount = subString.length();
		if (subCount > 0) {
			if (subCount + start > length) {
				return -1;
			}
			char firstChar = subString.charAt(0);
			while (true) {
				int i = start;
				boolean found = false;
				for (; i < length; i++) {
					if (chars[i] == firstChar) {
						found = true;
						break;
					}
				}
				if (!found || subCount + i > length) {
					return -1; // handles subCount > count || start >= count
				}
				int o1 = i, o2 = 0;
				while (++o2 < subCount && chars[++o1] == subString.charAt(o2)) {
					// Intentionally empty
				}
				if (o2 == subCount) {
					return i;
				}
				start = i + 1;
			}
		}
		return start < length || start == 0 ? start : length;
	}

	/** Searches for the last index of the specified character. The search for the character starts at the end and moves towards the
	 * beginning.
	 * 
	 * @param string the string to find.
	 * @return the index of the specified character, -1 if the character isn't found.
	 * @throws NullPointerException if {@code string} is {@code null}.
	 * @see String#lastIndexOf(java.lang.String)
	 * @since 1.4 */
	public int lastIndexOf (String string) {
		return lastIndexOf(string, length);
	}

	/** Searches for the index of the specified character. The search for the character starts at the specified offset and moves
	 * towards the beginning.
	 * 
	 * @param subString the string to find.
	 * @param start the starting offset.
	 * @return the index of the specified character, -1 if the character isn't found.
	 * @throws NullPointerException if {@code subString} is {@code null}.
	 * @see String#lastIndexOf(String,int)
	 * @since 1.4 */
	public int lastIndexOf (String subString, int start) {
		int subCount = subString.length();
		if (subCount <= length && start >= 0) {
			if (subCount > 0) {
				if (start > length - subCount) {
					start = length - subCount; // count and subCount are both
				}
				// >= 1
				char firstChar = subString.charAt(0);
				while (true) {
					int i = start;
					boolean found = false;
					for (; i >= 0; --i) {
						if (chars[i] == firstChar) {
							found = true;
							break;
						}
					}
					if (!found) {
						return -1;
					}
					int o1 = i, o2 = 0;
					while (++o2 < subCount && chars[++o1] == subString.charAt(o2)) {
						// Intentionally empty
					}
					if (o2 == subCount) {
						return i;
					}
					start = i - 1;
				}
			}
			return start < length ? start : length;
		}
		return -1;
	}

	/** Trims off any extra capacity beyond the current length. Note, this method is NOT guaranteed to change the capacity of this
	 * object.
	 * 
	 * @since 1.5 */
	public void trimToSize () {
		if (length < chars.length) {
			char[] newValue = new char[length];
			System.arraycopy(chars, 0, newValue, 0, length);
			chars = newValue;
		}
	}

	/** Retrieves the Unicode code point value at the {@code index}.
	 * 
	 * @param index the index to the {@code char} code unit.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to {@link #length()}.
	 * @see Character
	 * @see Character#codePointAt(char[], int, int)
	 * @since 1.5 */
	public int codePointAt (int index) {
		if (index < 0 || index >= length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return Character.codePointAt(chars, index, length);
	}

	/** Retrieves the Unicode code point value that precedes the {@code index}.
	 * 
	 * @param index the index to the {@code char} code unit within this object.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is less than 1 or greater than {@link #length()}.
	 * @see Character
	 * @see Character#codePointBefore(char[], int, int)
	 * @since 1.5 */
	public int codePointBefore (int index) {
		if (index < 1 || index > length) {
			throw new StringIndexOutOfBoundsException(index);
		}
		return Character.codePointBefore(chars, index);
	}

	/** Calculates the number of Unicode code points between {@code beginIndex} and {@code endIndex}.
	 * 
	 * @param beginIndex the inclusive beginning index of the subsequence.
	 * @param endIndex the exclusive end index of the subsequence.
	 * @return the number of Unicode code points in the subsequence.
	 * @throws IndexOutOfBoundsException if {@code beginIndex} is negative or greater than {@code endIndex} or {@code endIndex} is
	 *            greater than {@link #length()}.
	 * @see Character
	 * @see Character#codePointCount(char[], int, int)
	 * @since 1.5 */
	public int codePointCount (int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > length || beginIndex > endIndex) {
			throw new StringIndexOutOfBoundsException();
		}
		return Character.codePointCount(chars, beginIndex, endIndex - beginIndex);
	}

	/** Returns the index that is offset {@code codePointOffset} code points from {@code index}.
	 * 
	 * @param index the index to calculate the offset from.
	 * @param codePointOffset the number of code points to count.
	 * @return the index that is {@code codePointOffset} code points away from index.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than {@link #length()} or if there aren't enough
	 *            code points before or after {@code index} to match {@code codePointOffset}.
	 * @see Character
	 * @see Character#offsetByCodePoints(char[], int, int, int, int)
	 * @since 1.5 */
	public int offsetByCodePoints (int index, int codePointOffset) {
		return Character.offsetByCodePoints(chars, 0, length, index, codePointOffset);
	}

	/** Appends the string representation of the specified {@code boolean} value. The {@code boolean} value is converted to a String
	 * according to the rule defined by {@link String#valueOf(boolean)}.
	 * 
	 * @param b the {@code boolean} value to append.
	 * @return this builder.
	 * @see String#valueOf(boolean) */
	public StringBuilder append (boolean b) {
		append0(b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		return this;
	}

	/** Appends the string representation of the specified {@code char} value. The {@code char} value is converted to a string
	 * according to the rule defined by {@link String#valueOf(char)}.
	 * 
	 * @param c the {@code char} value to append.
	 * @return this builder.
	 * @see String#valueOf(char) */
	public StringBuilder append (char c) {
		append0(c);
		return this;
	}

	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code int} value to append.
	 * @return this builder.
	 * @see String#valueOf(int) */
	public StringBuilder append (int value) {
		return append(value, 0);
	}
	
	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code int} value to append.
	 * @param minLength the minimum number of characters to add
	 * @return this builder.
	 * @see String#valueOf(int) */
	public StringBuilder append (int value, int minLength) {
		return append(value, minLength, '0');
	}
	
	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code int} value to append.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix
	 * @return this builder.
	 * @see String#valueOf(int) */
	public StringBuilder append (int value, final int minLength, final char prefix) {
		if (value == Integer.MIN_VALUE) {
			append0("-2147483648");
			return this;
		}
		if (value < 0) {
			append0('-');
			value = -value;
		}
		if (minLength > 1) {
			for (int j = minLength - numChars(value, 10); j > 0; --j)
				append(prefix);
		}
		if (value >= 10000) {
			if (value >= 1000000000) append0(digits[(int)((long)value % 10000000000L / 1000000000L)]);
			if (value >= 100000000) append0(digits[value % 1000000000 / 100000000]);
			if (value >= 10000000) append0(digits[value % 100000000 / 10000000]);
			if (value >= 1000000) append0(digits[value % 10000000 / 1000000]);
			if (value >= 100000) append0(digits[value % 1000000 / 100000]);
			append0(digits[value % 100000 / 10000]);
		}
		if (value >= 1000) append0(digits[value % 10000 / 1000]);
		if (value >= 100) append0(digits[value % 1000 / 100]);
		if (value >= 10) append0(digits[value % 100 / 10]);
		append0(digits[value % 10]);
		return this;
	}

	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code long} value.
	 * @return this builder. */
	public StringBuilder append (long value) {
		return append(value, 0);
	}
	
	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code long} value.
	 * @param minLength the minimum number of characters to add
	 * @return this builder. */
	public StringBuilder append (long value, int minLength) {
		return append(value, minLength, '0');
	}
		
	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to a string
	 * without memory allocation.
	 * 
	 * @param value the {@code long} value.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix
	 * @return this builder. */
	public StringBuilder append (long value, int minLength, char prefix) {
		if (value == Long.MIN_VALUE) {
			append0("-9223372036854775808");
			return this;
		}
		if (value < 0L) {
			append0('-');
			value = -value;
		}
		if (minLength > 1) {
			for (int j = minLength - numChars(value, 10); j > 0; --j)
				append(prefix);
		}
		if (value >= 10000) {
			if (value >= 1000000000000000000L) append0(digits[(int)(value % 10000000000000000000D / 1000000000000000000L)]);
			if (value >= 100000000000000000L) append0(digits[(int)(value % 1000000000000000000L / 100000000000000000L)]);
			if (value >= 10000000000000000L) append0(digits[(int)(value % 100000000000000000L / 10000000000000000L)]);
			if (value >= 1000000000000000L) append0(digits[(int)(value % 10000000000000000L / 1000000000000000L)]);
			if (value >= 100000000000000L) append0(digits[(int)(value % 1000000000000000L / 100000000000000L)]);
			if (value >= 10000000000000L) append0(digits[(int)(value % 100000000000000L / 10000000000000L)]);
			if (value >= 1000000000000L) append0(digits[(int)(value % 10000000000000L / 1000000000000L)]);
			if (value >= 100000000000L) append0(digits[(int)(value % 1000000000000L / 100000000000L)]);
			if (value >= 10000000000L) append0(digits[(int)(value % 100000000000L / 10000000000L)]);
			if (value >= 1000000000L) append0(digits[(int)(value % 10000000000L / 1000000000L)]);
			if (value >= 100000000L) append0(digits[(int)(value % 1000000000L / 100000000L)]);
			if (value >= 10000000L) append0(digits[(int)(value % 100000000L / 10000000L)]);
			if (value >= 1000000L) append0(digits[(int)(value % 10000000L / 1000000L)]);
			if (value >= 100000L) append0(digits[(int)(value % 1000000L / 100000L)]);
			append0(digits[(int)(value % 100000L / 10000L)]);
		}
		if (value >= 1000L) append0(digits[(int)(value % 10000L / 1000L)]);
		if (value >= 100L) append0(digits[(int)(value % 1000L / 100L)]);
		if (value >= 10L) append0(digits[(int)(value % 100L / 10L)]);
		append0(digits[(int)(value % 10L)]);
		return this;
	}

	/** Appends the string representation of the specified {@code float} value. The {@code float} value is converted to a string
	 * according to the rule defined by {@link String#valueOf(float)}.
	 * 
	 * @param f the {@code float} value to append.
	 * @return this builder. */
	public StringBuilder append (float f) {
		append0(Float.toString(f));
		return this;
	}

	/** Appends the string representation of the specified {@code double} value. The {@code double} value is converted to a string
	 * according to the rule defined by {@link String#valueOf(double)}.
	 * 
	 * @param d the {@code double} value to append.
	 * @return this builder.
	 * @see String#valueOf(double) */
	public StringBuilder append (double d) {
		append0(Double.toString(d));
		return this;
	}

	/** Appends the string representation of the specified {@code Object}. The {@code Object} value is converted to a string
	 * according to the rule defined by {@link String#valueOf(Object)}.
	 * 
	 * @param obj the {@code Object} to append.
	 * @return this builder.
	 * @see String#valueOf(Object) */
	public StringBuilder append (Object obj) {
		if (obj == null) {
			appendNull();
		} else {
			append0(obj.toString());
		}
		return this;
	}

	/** Appends the contents of the specified string. If the string is {@code null}, then the string {@code "null"} is appended.
	 * 
	 * @param str the string to append.
	 * @return this builder. */
	public StringBuilder append (String str) {
		append0(str);
		return this;
	}

	/** Appends the string representation of the specified {@code char[]}. The {@code char[]} is converted to a string according to
	 * the rule defined by {@link String#valueOf(char[])}.
	 * 
	 * @param ch the {@code char[]} to append..
	 * @return this builder.
	 * @see String#valueOf(char[]) */
	public StringBuilder append (char[] ch) {
		append0(ch);
		return this;
	}

	/** Appends the string representation of the specified subset of the {@code char[]}. The {@code char[]} value is converted to a
	 * String according to the rule defined by {@link String#valueOf(char[],int,int)}.
	 * 
	 * @param str the {@code char[]} to append.
	 * @param offset the inclusive offset index.
	 * @param len the number of characters.
	 * @return this builder.
	 * @throws ArrayIndexOutOfBoundsException if {@code offset} and {@code len} do not specify a valid subsequence.
	 * @see String#valueOf(char[],int,int) */
	public StringBuilder append (char[] str, int offset, int len) {
		append0(str, offset, len);
		return this;
	}

	/** Appends the string representation of the specified {@code CharSequence}. If the {@code CharSequence} is {@code null}, then
	 * the string {@code "null"} is appended.
	 * 
	 * @param csq the {@code CharSequence} to append.
	 * @return this builder. */
	public StringBuilder append (CharSequence csq) {
		if (csq == null) {
			appendNull();
		} else {
			append0(csq.toString());
		}
		return this;
	}

	public StringBuilder append (StringBuilder builder) {
		if (builder == null)
			appendNull();
		else
			append0(builder.chars, 0, builder.length);
		return this;
	}

	/** Appends the string representation of the specified subsequence of the {@code CharSequence}. If the {@code CharSequence} is
	 * {@code null}, then the string {@code "null"} is used to extract the subsequence from.
	 * 
	 * @param csq the {@code CharSequence} to append.
	 * @param start the beginning index.
	 * @param end the ending index.
	 * @return this builder.
	 * @throws IndexOutOfBoundsException if {@code start} or {@code end} are negative, {@code start} is greater than {@code end} or
	 *            {@code end} is greater than the length of {@code csq}. */
	public StringBuilder append (CharSequence csq, int start, int end) {
		append0(csq, start, end);
		return this;
	}

	public StringBuilder append (StringBuilder builder, int start, int end) {
		if (builder == null)
			appendNull();
		else
			append0(builder.chars, start, end);
		return this;
	}

	/** Appends the encoded Unicode code point. The code point is converted to a {@code char[]} as defined by
	 * {@link Character#toChars(int)}.
	 * 
	 * @param codePoint the Unicode code point to encode and append.
	 * @return this builder.
	 * @see Character#toChars(int) */
	public StringBuilder appendCodePoint (int codePoint) {
		append0(Character.toChars(codePoint));
		return this;
	}

	/** Deletes a sequence of characters specified by {@code start} and {@code end}. Shifts any remaining characters to the left.
	 * 
	 * @param start the inclusive start index.
	 * @param end the exclusive end index.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code start} is less than zero, greater than the current length or greater than
	 *            {@code end}. */
	public StringBuilder delete (int start, int end) {
		delete0(start, end);
		return this;
	}

	/** Deletes the character at the specified index. shifts any remaining characters to the left.
	 * 
	 * @param index the index of the character to delete.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code index} is less than zero or is greater than or equal to the current
	 *            length. */
	public StringBuilder deleteCharAt (int index) {
		deleteCharAt0(index);
		return this;
	}

	/** Inserts the string representation of the specified {@code boolean} value at the specified {@code offset}. The
	 * {@code boolean} value is converted to a string according to the rule defined by {@link String#valueOf(boolean)}.
	 * 
	 * @param offset the index to insert at.
	 * @param b the {@code boolean} value to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length}.
	 * @see String#valueOf(boolean) */
	public StringBuilder insert (int offset, boolean b) {
		insert0(offset, b ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		return this;
	}

	/** Inserts the string representation of the specified {@code char} value at the specified {@code offset}. The {@code char}
	 * value is converted to a string according to the rule defined by {@link String#valueOf(char)}.
	 * 
	 * @param offset the index to insert at.
	 * @param c the {@code char} value to insert.
	 * @return this builder.
	 * @throws IndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(char) */
	public StringBuilder insert (int offset, char c) {
		insert0(offset, c);
		return this;
	}

	/** Inserts the string representation of the specified {@code int} value at the specified {@code offset}. The {@code int} value
	 * is converted to a String according to the rule defined by {@link String#valueOf(int)}.
	 * 
	 * @param offset the index to insert at.
	 * @param i the {@code int} value to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(int) */
	public StringBuilder insert (int offset, int i) {
		insert0(offset, Integer.toString(i));
		return this;
	}

	/** Inserts the string representation of the specified {@code long} value at the specified {@code offset}. The {@code long}
	 * value is converted to a String according to the rule defined by {@link String#valueOf(long)}.
	 * 
	 * @param offset the index to insert at.
	 * @param l the {@code long} value to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {code length()}.
	 * @see String#valueOf(long) */
	public StringBuilder insert (int offset, long l) {
		insert0(offset, Long.toString(l));
		return this;
	}

	/** Inserts the string representation of the specified {@code float} value at the specified {@code offset}. The {@code float}
	 * value is converted to a string according to the rule defined by {@link String#valueOf(float)}.
	 * 
	 * @param offset the index to insert at.
	 * @param f the {@code float} value to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(float) */
	public StringBuilder insert (int offset, float f) {
		insert0(offset, Float.toString(f));
		return this;
	}

	/** Inserts the string representation of the specified {@code double} value at the specified {@code offset}. The {@code double}
	 * value is converted to a String according to the rule defined by {@link String#valueOf(double)}.
	 * 
	 * @param offset the index to insert at.
	 * @param d the {@code double} value to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(double) */
	public StringBuilder insert (int offset, double d) {
		insert0(offset, Double.toString(d));
		return this;
	}

	/** Inserts the string representation of the specified {@code Object} at the specified {@code offset}. The {@code Object} value
	 * is converted to a String according to the rule defined by {@link String#valueOf(Object)}.
	 * 
	 * @param offset the index to insert at.
	 * @param obj the {@code Object} to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(Object) */
	public StringBuilder insert (int offset, Object obj) {
		insert0(offset, obj == null ? "null" : obj.toString()); //$NON-NLS-1$
		return this;
	}

	/** Inserts the specified string at the specified {@code offset}. If the specified string is null, then the String
	 * {@code "null"} is inserted.
	 * 
	 * @param offset the index to insert at.
	 * @param str the {@code String} to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}. */
	public StringBuilder insert (int offset, String str) {
		insert0(offset, str);
		return this;
	}

	/** Inserts the string representation of the specified {@code char[]} at the specified {@code offset}. The {@code char[]} value
	 * is converted to a String according to the rule defined by {@link String#valueOf(char[])}.
	 * 
	 * @param offset the index to insert at.
	 * @param ch the {@code char[]} to insert.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see String#valueOf(char[]) */
	public StringBuilder insert (int offset, char[] ch) {
		insert0(offset, ch);
		return this;
	}

	/** Inserts the string representation of the specified subsequence of the {@code char[]} at the specified {@code offset}. The
	 * {@code char[]} value is converted to a String according to the rule defined by {@link String#valueOf(char[],int,int)}.
	 * 
	 * @param offset the index to insert at.
	 * @param str the {@code char[]} to insert.
	 * @param strOffset the inclusive index.
	 * @param strLen the number of characters.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}, or
	 *            {@code strOffset} and {@code strLen} do not specify a valid subsequence.
	 * @see String#valueOf(char[],int,int) */
	public StringBuilder insert (int offset, char[] str, int strOffset, int strLen) {
		insert0(offset, str, strOffset, strLen);
		return this;
	}

	/** Inserts the string representation of the specified {@code CharSequence} at the specified {@code offset}. The
	 * {@code CharSequence} is converted to a String as defined by {@link CharSequence#toString()}. If {@code s} is {@code null},
	 * then the String {@code "null"} is inserted.
	 * 
	 * @param offset the index to insert at.
	 * @param s the {@code CharSequence} to insert.
	 * @return this builder.
	 * @throws IndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}.
	 * @see CharSequence#toString() */
	public StringBuilder insert (int offset, CharSequence s) {
		insert0(offset, s == null ? "null" : s.toString()); //$NON-NLS-1$
		return this;
	}

	/** Inserts the string representation of the specified subsequence of the {@code CharSequence} at the specified {@code offset}.
	 * The {@code CharSequence} is converted to a String as defined by {@link CharSequence#subSequence(int, int)}. If the
	 * {@code CharSequence} is {@code null}, then the string {@code "null"} is used to determine the subsequence.
	 * 
	 * @param offset the index to insert at.
	 * @param s the {@code CharSequence} to insert.
	 * @param start the start of the subsequence of the character sequence.
	 * @param end the end of the subsequence of the character sequence.
	 * @return this builder.
	 * @throws IndexOutOfBoundsException if {@code offset} is negative or greater than the current {@code length()}, or
	 *            {@code start} and {@code end} do not specify a valid subsequence.
	 * @see CharSequence#subSequence(int, int) */
	public StringBuilder insert (int offset, CharSequence s, int start, int end) {
		insert0(offset, s, start, end);
		return this;
	}

	/** Replaces the specified subsequence in this builder with the specified string.
	 * 
	 * @param start the inclusive begin index.
	 * @param end the exclusive end index.
	 * @param str the replacement string.
	 * @return this builder.
	 * @throws StringIndexOutOfBoundsException if {@code start} is negative, greater than the current {@code length()} or greater
	 *            than {@code end}.
	 * @throws NullPointerException if {@code str} is {@code null}. */
	public StringBuilder replace (int start, int end, String str) {
		replace0(start, end, str);
		return this;
	}

	/** Reverses the order of characters in this builder.
	 * 
	 * @return this buffer. */
	public StringBuilder reverse () {
		reverse0();
		return this;
	}

	public int hashCode () {
		final int prime = 31;
		int result = 1;
		result = prime + length;
		result = prime * result + Arrays.hashCode(chars);
		return result;
	}

	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StringBuilder other = (StringBuilder)obj;
		int length = this.length;
		if (length != other.length) return false;
		char[] chars = this.chars;
		char[] chars2 = other.chars;
		if (chars == chars2) return true;
		if (chars == null || chars2 == null) return false;
		for (int i = 0; i < length; i++)
			if (chars[i] != chars2[i]) return false;
		return true;
	}
}
