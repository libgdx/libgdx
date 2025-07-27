/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/** Builds a string from constituent parts providing a more flexible and powerful API than {@link StringBuffer} and
 * {@link java.lang.StringBuilder}.
 * <p>
 * The main differences from StringBuffer/StringBuilder are:
 * <ul>
 * <li>Not final or synchronized
 * <li>Direct access to the char[] and length
 * <li>Appending int and long does not allocate
 * <li>Compare against strings without allocating
 * <li>Implements equals and hashCode
 * <li>char[] grows by 50% rather than doubling
 * <li>Additional builder-style methods:
 * <ul>
 * <li>appendWithSeparators - adds an array of values, with a separator
 * <li>appendPadding - adds padding characters
 * <li>appendFixedLength - adds a fixed width field to the builder
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
 * <li>asReader - uses the internal buffer as the source of a Reader
 * <li>asWriter - allows a Writer to write directly to the internal buffer
 * </ul>
 * </ul>
 * The aim has been to provide an API that mimics very closely what StringBuffer provides, but with additional methods. It should
 * be noted that some edge cases, with invalid indices or null input, have been altered - see individual methods.
 * @author org.apache.commons.text */
public class StringBuilder implements CharSequence, Appendable {
	private static final int CAPACITY = 16;
	private static final int EOS = -1;
	private static final int FALSE_STRING_SIZE = 5;
	private static final int TRUE_STRING_SIZE = 4;
	private static final String NULL = "null";
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/** The maximum buffer size to allocate. This is the size used in {@link java.util.ArrayList}, as some VMs reserve header words
	 * in an array. */
	private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;

	public char[] chars;
	public int length;

	/** Constructs an empty builder with an initial capacity of 16 characters. */
	public StringBuilder () {
		this(CAPACITY);
	}

	/** Constructs an instance from a reference to a character array.
	 * @param initialBuffer a reference to a character array, must not be null.
	 * @param len The length of the subarray to be used; must be non-negative and no larger than {@code initialBuffer.length}. The
	 *           new builder's size will be set to {@code len}.
	 * @throws NullPointerException If {@code initialBuffer} is null.
	 * @throws IllegalArgumentException if {@code len} is bad. */
	private StringBuilder (char[] initialBuffer, int len) {
		chars = Objects.requireNonNull(initialBuffer, "initialBuffer");
		if (len < 0 || len > initialBuffer.length)
			throw new IllegalArgumentException("initialBuffer.length=" + initialBuffer.length + ", length=" + len);
		length = len;
	}

	/** Constructs an instance from a character sequence, allocating 16 extra characters for growth. */
	public StringBuilder (CharSequence seq) {
		this(seq.length() + CAPACITY);
		append(seq);
	}

	/** Constructs an instance with the specified initial capacity.
	 * @param initialCapacity the initial capacity, zero or less will be converted to 16 */
	public StringBuilder (int initialCapacity) {
		chars = new char[initialCapacity <= 0 ? CAPACITY : initialCapacity];
	}

	/** Constructs an instance from a string, allocating 16 extra characters for growth. */
	public StringBuilder (String str) {
		this(str.length() + CAPACITY);
		append(str);
	}

	/** Constructs an instance from a string builder, allocating 16 extra characters for growth. */
	public StringBuilder (StringBuilder str) {
		this(str.length() + CAPACITY);
		append(str);
	}

	/** Appends a boolean value to the string builder. */
	public StringBuilder append (boolean value) {
		if (value) {
			ensureCapacityInternal(length + TRUE_STRING_SIZE);
			appendTrue(length);
		} else {
			ensureCapacityInternal(length + FALSE_STRING_SIZE);
			appendFalse(length);
		}
		return this;
	}

	/** Appends a char value to the string builder. */
	public StringBuilder append (char ch) {
		int len = length();
		ensureCapacityInternal(len + 1);
		chars[length++] = ch;
		return this;
	}

	/** Appends a char array to the string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null char[] ch) {
		if (ch == null) return appendNull();
		int strLen = ch.length;
		if (strLen > 0) {
			int len = length();
			ensureCapacityInternal(len + strLen);
			System.arraycopy(ch, 0, chars, len, strLen);
			length += strLen;
		}
		return this;
	}

	/** Appends a char array to the string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive
	 * @throws StringIndexOutOfBoundsException if {@code startIndex} is not in the range {@code 0 <= startIndex <= chars.length}
	 * @throws StringIndexOutOfBoundsException if {@code length < 0}
	 * @throws StringIndexOutOfBoundsException if {@code startIndex + len > chars.length} */
	public StringBuilder append (@Null char[] ch, int startIndex, int len) {
		if (ch == null) return appendNull();
		if (startIndex < 0 || startIndex > ch.length)
			throw new StringIndexOutOfBoundsException("Invalid startIndex: " + startIndex);
		if (len < 0 || startIndex + len > ch.length) throw new StringIndexOutOfBoundsException("Invalid length: " + len);
		if (len > 0) {
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			System.arraycopy(ch, startIndex, chars, currentLength, len);
			length += len;
		}
		return this;
	}

	/** Appends the contents of a char buffer to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null CharBuffer str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends the contents of a char buffer to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder append (@Null CharBuffer buf, int startIndex, int len) {
		if (buf == null) return appendNull();
		if (buf.hasArray()) {
			int totalLength = buf.remaining();
			if (startIndex < 0 || startIndex > totalLength) throw new StringIndexOutOfBoundsException("startIndex must be valid");
			if (len < 0 || startIndex + len > totalLength) throw new StringIndexOutOfBoundsException("length must be valid");
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			System.arraycopy(buf.array(), buf.arrayOffset() + buf.position() + startIndex, chars, currentLength, len);
			length += len;
		} else
			append(buf.toString(), startIndex, len);
		return this;
	}

	/** Appends a CharSequence to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null CharSequence seq) {
		if (seq == null) return appendNull();
		if (seq instanceof StringBuilder) return append((StringBuilder)seq);
		if (seq instanceof java.lang.StringBuilder) return append((java.lang.StringBuilder)seq);
		if (seq instanceof StringBuffer) return append((StringBuffer)seq);
		if (seq instanceof CharBuffer) return append((CharBuffer)seq);
		return append(seq.toString());
	}

	/** Appends part of a CharSequence to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive */
	public StringBuilder append (@Null CharSequence seq, int startIndex, int endIndex) {
		if (seq == null) return appendNull();
		if (endIndex <= 0) throw new StringIndexOutOfBoundsException("endIndex must be valid");
		if (startIndex >= endIndex) throw new StringIndexOutOfBoundsException("endIndex must be greater than startIndex");
		return append(seq.toString(), startIndex, endIndex - startIndex);
	}

	/** Appends a double value to the string builder using {@code String.valueOf}. */
	public StringBuilder append (double value) {
		return append(String.valueOf(value));
	}

	/** Appends a float value to the string builder using {@code String.valueOf}. */
	public StringBuilder append (float value) {
		return append(String.valueOf(value));
	}

	/** Appends an int value to the string builder. The {@code int} value is converted to chars without memory allocation. */
	public StringBuilder append (int value) {
		return append(value, 0, '0');
	}

	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add */
	public StringBuilder append (int value, int minLength) {
		return append(value, minLength, '0');
	}

	/** Appends the string representation of the specified {@code int} value. The {@code int} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix */
	public StringBuilder append (int value, final int minLength, final char prefix) {
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

	/** Appends a long value to the string builder. The {@code long} value is converted to chars without memory allocation. */
	public StringBuilder append (long value) {
		return append(value, 0, '0');
	}

	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add */
	public StringBuilder append (long value, int minLength) {
		return append(value, minLength, '0');
	}

	/** Appends the string representation of the specified {@code long} value. The {@code long} value is converted to chars without
	 * memory allocation.
	 * @param minLength the minimum number of characters to add
	 * @param prefix the character to use as prefix */
	public StringBuilder append (long value, int minLength, char prefix) {
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

	/** Appends an object to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null Object obj) {
		if (obj == null) return appendNull();
		if (obj instanceof CharSequence) return append((CharSequence)obj);
		return append(obj.toString());
	}

	/** Appends a string to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null String str) {
		if (str == null)
			appendNull();
		else {
			int len = str.length();
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			str.getChars(0, len, chars, currentLength);
			length += len;
		}
		return this;
	}

	/** Appends part of a string to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive
	 * @throws StringIndexOutOfBoundsException if {@code startIndex} is not in the range {@code 0 <= startIndex <= str.length()}
	 * @throws StringIndexOutOfBoundsException if {@code len < 0}
	 * @throws StringIndexOutOfBoundsException if {@code startIndex + len > str.length()} */
	public StringBuilder append (@Null String str, int startIndex, int len) {
		if (str == null) return appendNull();
		if (startIndex < 0 || startIndex > str.length()) throw new StringIndexOutOfBoundsException("startIndex must be valid");
		if (len < 0 || startIndex + len > str.length()) throw new StringIndexOutOfBoundsException("length must be valid");
		if (len > 0) {
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			str.getChars(startIndex, startIndex + len, chars, currentLength);
			length += len;
		}
		return this;
	}

	/** Calls {@link String#format(String, Object...)} and appends the result.
	 * @see String#format(String, Object...) */
	public StringBuilder append (String format, Object... objs) {
		return append(String.format(format, objs));
	}

	/** Appends a string buffer to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null StringBuffer str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends part of a string buffer to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder append (@Null StringBuffer str, int startIndex, int len) {
		if (str == null) return appendNull();
		if (startIndex < 0 || startIndex > str.length()) throw new StringIndexOutOfBoundsException("startIndex must be valid");
		if (len < 0 || startIndex + len > str.length()) throw new StringIndexOutOfBoundsException("length must be valid");
		if (len > 0) {
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			str.getChars(startIndex, startIndex + len, chars, currentLength);
			length += len;
		}
		return this;
	}

	/** Appends a StringBuilder to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null java.lang.StringBuilder str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends part of a StringBuilder to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder append (@Null java.lang.StringBuilder str, int startIndex, int len) {
		if (str == null) return appendNull();
		if (startIndex < 0 || startIndex > str.length()) throw new StringIndexOutOfBoundsException("startIndex must be valid");
		if (len < 0 || startIndex + len > str.length()) throw new StringIndexOutOfBoundsException("length must be valid");
		if (len > 0) {
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			str.getChars(startIndex, startIndex + len, chars, currentLength);
			length += len;
		}
		return this;
	}

	/** Appends another string builder to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder append (@Null StringBuilder str) {
		if (str == null)
			appendNull();
		else
			append(str, 0, str.length());
		return this;
	}

	/** Appends part of a string builder to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder append (@Null StringBuilder str, int startIndex, int len) {
		if (str == null) return appendNull();
		if (startIndex < 0 || startIndex > str.length()) throw new StringIndexOutOfBoundsException("startIndex must be valid");
		if (len < 0 || startIndex + len > str.length()) throw new StringIndexOutOfBoundsException("length must be valid");
		if (len > 0) {
			int currentLength = length();
			ensureCapacityInternal(currentLength + len);
			str.getChars(startIndex, startIndex + len, chars, currentLength);
			length += len;
		}
		return this;
	}

	/** Appends each item in an iterable to the builder without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public StringBuilder appendAll (Iterable<?> iterable) {
		for (Iterator iter = iterable.iterator(); iter.hasNext();)
			append(iter.next());
		return this;
	}

	/** Appends each item in an iterator to the builder without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public StringBuilder appendAll (Iterator<?> iter) {
		while (iter.hasNext())
			append(iter.next());
		return this;
	}

	/** Appends each item in an array to the builder without any separators. Each object is appended using
	 * {@link #append(Object)}. */
	public <T> StringBuilder appendAll (T... array) {
		if (array.length > 0) {
			for (Object element : array)
				append(element);
		}
		return this;
	}

	/** Appends {@code "false"}. */
	private void appendFalse (int index) {
		chars[index++] = 'f';
		chars[index++] = 'a';
		chars[index++] = 'l';
		chars[index++] = 's';
		chars[index] = 'e';
		length += FALSE_STRING_SIZE;
	}

	/** Appends an object to the builder padding on the left to a fixed width. The {@code String.valueOf} of the {@code int} value
	 * is used. If the formatted value is larger than the length, the left hand side is lost.
	 * @param width the fixed field width, zero or negative has no effect */
	public StringBuilder appendFixedWidthPadLeft (int value, int width, char padChar) {
		return appendFixedWidthPadLeft(String.valueOf(value), width, padChar);
	}

	/** Appends an object to the builder padding on the left to a fixed width. The {@code toString} of the object is used. If the
	 * object is larger than the length, the left hand side is lost. If the object is null {@code "null"} is used.
	 * @param obj the object to append, null uses ""
	 * @param width the fixed field width, zero or negative has no effect */
	public StringBuilder appendFixedWidthPadLeft (@Null Object obj, int width, char padChar) {
		if (width > 0) {
			ensureCapacityInternal(length + width);
			String str = Objects.toString(obj, NULL);
			int strLen = str.length();
			if (strLen >= width)
				str.getChars(strLen - width, strLen, chars, length);
			else {
				int padLen = width - strLen;
				int toIndex = length + padLen;
				Arrays.fill(chars, length, toIndex, padChar);
				str.getChars(0, strLen, chars, toIndex);
			}
			length += width;
		}
		return this;
	}

	/** Appends an object to the builder padding on the right to a fixed length. The {@code String.valueOf} of the {@code int}
	 * value is used. If the object is larger than the length, the right hand side is lost.
	 * @param width the fixed field width, zero or negative has no effect */
	public StringBuilder appendFixedWidthPadRight (int value, int width, char padChar) {
		return appendFixedWidthPadRight(String.valueOf(value), width, padChar);
	}

	/** Appends an object to the builder padding on the right to a fixed length. The {@code toString} of the object is used. If the
	 * object is larger than the length, the right hand side is lost.
	 * @param obj the object to append, null uses {@code "null"}
	 * @param width the fixed field width, zero or negative has no effect */
	public StringBuilder appendFixedWidthPadRight (@Null Object obj, int width, char padChar) {
		if (width > 0) {
			ensureCapacityInternal(length + width);
			String str = Objects.toString(obj, NULL);
			int strLen = str.length();
			if (strLen >= width)
				str.getChars(0, width, chars, length);
			else {
				str.getChars(0, strLen, chars, length);
				int fromIndex = length + strLen;
				Arrays.fill(chars, fromIndex, fromIndex + width - strLen, padChar);
			}
			length += width;
		}
		return this;
	}

	/** Appends a boolean value followed by a new line to the string builder. */
	public StringBuilder appendln (boolean value) {
		return append(value).appendNewLine();
	}

	/** Appends a char value followed by a new line to the string builder. */
	public StringBuilder appendln (char ch) {
		return append(ch).appendNewLine();
	}

	/** Appends a char array followed by a new line to the string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder appendln (@Null char[] ch) {
		return append(ch).appendNewLine();
	}

	/** Appends a char array followed by a new line to the string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder appendln (@Null char[] ch, int startIndex, int len) {
		return append(ch, startIndex, len).appendNewLine();
	}

	/** Appends a double value followed by a new line to the string builder using {@code String.valueOf}. */
	public StringBuilder appendln (double value) {
		return append(value).appendNewLine();
	}

	/** Appends a float value followed by a new line to the string builder using {@code String.valueOf}. */
	public StringBuilder appendln (float value) {
		return append(value).appendNewLine();
	}

	/** Appends an int value followed by a new line to the string builder using {@code String.valueOf}. */
	public StringBuilder appendln (int value) {
		return append(value).appendNewLine();
	}

	/** Appends a long value followed by a new line to the string builder using {@code String.valueOf}. */
	public StringBuilder appendln (long value) {
		return append(value).appendNewLine();
	}

	/** Appends an object followed by a new line to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder appendln (@Null Object obj) {
		return append(obj).appendNewLine();
	}

	/** Appends a string followed by a new line to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder appendln (@Null String str) {
		return append(str).appendNewLine();
	}

	/** Appends a string followed by a new line to this string builder. Appending null will call {@link #appendNull()}.
	 * @deprecated Use {@link #appendln(String)} */
	public StringBuilder appendLine (@Null String str) {
		append(str);
		return append('\n');
	}

	/** Appends part of a string followed by a new line to this string builder. Appending null will call {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder appendln (@Null String str, int startIndex, int len) {
		return append(str, startIndex, len).appendNewLine();
	}

	/** Calls {@link String#format(String, Object...)} and appends the result.
	 * @see String#format(String, Object...) */
	public StringBuilder appendln (String format, Object... objs) {
		return append(format, objs).appendNewLine();
	}

	/** Appends a string buffer followed by a new line to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder appendln (@Null StringBuffer str) {
		return append(str).appendNewLine();
	}

	/** Appends part of a string buffer followed by a new line to this string builder. Appending null will call
	 * {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder appendln (@Null StringBuffer str, int startIndex, int len) {
		return append(str, startIndex, len).appendNewLine();
	}

	/** Appends a string builder followed by a new line to this string builder. Appending null will call {@link #appendNull()}. */
	public StringBuilder appendln (@Null java.lang.StringBuilder str) {
		return append(str).appendNewLine();
	}

	/** Appends part of a string builder followed by a new line to this string builder. Appending null will call
	 * {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder appendln (@Null java.lang.StringBuilder str, int startIndex, int len) {
		return append(str, startIndex, len).appendNewLine();
	}

	/** Appends another string builder followed by a new line to this string builder. Appending null will call
	 * {@link #appendNull()}. */
	public StringBuilder appendln (@Null StringBuilder str) {
		return append(str).appendNewLine();
	}

	/** Appends part of a string builder followed by a new line to this string builder. Appending null will call
	 * {@link #appendNull()}.
	 * @param startIndex the start index, inclusive */
	public StringBuilder appendln (@Null StringBuilder str, int startIndex, int len) {
		return append(str, startIndex, len).appendNewLine();
	}

	/** Appends {@code \n}. */
	public StringBuilder appendln () {
		return append('\n');
	}

	/** Appends {@code \n}. */
	public StringBuilder appendNewLine () {
		return append('\n');
	}

	/** Appends {@code "null"}. */
	public StringBuilder appendNull () {
		int len = length;
		length = len + 4;
		ensureCapacityInternal(length);
		chars[len] = 'n';
		chars[len + 1] = 'u';
		chars[len + 2] = 'l';
		chars[len + 3] = 'l';
		return this;
	}

	/** Appends the pad character to the builder the specified number of times.
	 * @param padCount negative means no append */
	public StringBuilder appendPadding (int padCount, char padChar) {
		if (padCount > 0) {
			ensureCapacityInternal(length + padCount);
			Arrays.fill(chars, length, length + padCount, padChar);
			length += padCount;
		}
		return this;
	}

	/** Appends a separator if the builder is currently non-empty. The separator is appended using {@link #append(char)}.
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
	public StringBuilder appendSeparator (char separator) {
		if (notEmpty()) append(separator);
		return this;
	}

	/** Appends one of both separators to the builder If the builder is currently empty it will append the defaultIfEmpty-separator
	 * Otherwise it will append the standard-separator The separator is appended using {@link #append(char)}.
	 * @param standard the separator if builder is not empty
	 * @param defaultIfEmpty the separator if builder is empty */
	public StringBuilder appendSeparator (char standard, char defaultIfEmpty) {
		if (isEmpty())
			append(defaultIfEmpty);
		else
			append(standard);
		return this;
	}

	/** Appends a separator to the builder if the loop index is greater than zero. The separator is appended using
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
	public StringBuilder appendSeparator (char separator, int loopIndex) {
		if (loopIndex > 0) append(separator);
		return this;
	}

	/** Appends a separator if the builder is currently non-empty. The separator is appended using {@link #append(String)}.
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
	public StringBuilder appendSeparator (@Null String separator) {
		return appendSeparator(separator, null);
	}

	/** Appends a separator to the builder if the loop index is greater than zero. The separator is appended using
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
	public StringBuilder appendSeparator (@Null String separator, int loopIndex) {
		if (separator != null && loopIndex > 0) append(separator);
		return this;
	}

	/** Appends one of both separators to the StrBuilder. If the builder is currently empty, it will append the
	 * defaultIfEmpty-separator, otherwise it will append the standard-separator.
	 * <p>
	 * The separator is appended using {@link #append(String)}.
	 * <p>
	 * This method is for example useful for constructing queries
	 * 
	 * <pre>
	* StrBuilder whereClause = new StrBuilder();
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
	 * @param standard the separator if builder is not empty, null means no separator
	 * @param defaultIfEmpty the separator if builder is empty, null means no separator */
	public StringBuilder appendSeparator (@Null String standard, @Null String defaultIfEmpty) {
		String str = isEmpty() ? defaultIfEmpty : standard;
		if (str != null) append(str);
		return this;
	}

	/** Appends current contents of this {@code StrBuilder} to the provided {@link Appendable}.
	 * <p>
	 * This method tries to avoid doing any extra copies of contents.
	 * @throws IOException if an I/O error occurs.
	 * @see #readFrom(Readable) */
	public void appendTo (Appendable appendable) throws IOException {
		if (appendable instanceof Writer)
			((Writer)appendable).write(chars, 0, length);
		else if (appendable instanceof java.lang.StringBuilder)
			((java.lang.StringBuilder)appendable).append(chars, 0, length);
		else if (appendable instanceof StringBuffer)
			((StringBuffer)appendable).append(chars, 0, length);
		else if (appendable instanceof CharBuffer)
			((CharBuffer)appendable).put(chars, 0, length);
		else
			appendable.append(this);
	}

	/** Appends {@code "true"}. */
	private void appendTrue (int index) {
		chars[index++] = 't';
		chars[index++] = 'r';
		chars[index++] = 'u';
		chars[index] = 'e';
		length += TRUE_STRING_SIZE;
	}

	/** Appends an iterable placing separators between each value, but not before the first or after the last. Each object is
	 * appended using {@link #append(Object)}.
	 * @param separator the separator to use, null means no separator */
	public StringBuilder appendWithSeparators (Iterable<?> iterable, @Null String separator) {
		appendWithSeparators(iterable.iterator(), separator);
		return this;
	}

	/** Appends an iterator placing separators between each value, but not before the first or after the last. Each object is
	 * appended using {@link #append(Object)}.
	 * @param separator the separator to use, null means no separator */
	public StringBuilder appendWithSeparators (Iterator<?> it, @Null String separator) {
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
	public StringBuilder appendWithSeparators (Object[] array, @Null String separator) {
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
	public StringBuilder appendCodePoint (int codePoint) {
		append(Character.toChars(codePoint));
		return this;
	}

	/** Gets the contents of this builder as a Reader.
	 * <p>
	 * This method allows the contents of the builder to be read using any standard method that expects a Reader.
	 * <p>
	 * To use, simply create a {@code StrBuilder}, populate it with data, call {@code asReader}, and then read away.
	 * <p>
	 * The internal character array is shared between the builder and the reader. This allows you to append to the builder after
	 * creating the reader, and the changes will be picked up. Note however, that no synchronization occurs, so you must perform
	 * all operations with the builder and the reader in one thread.
	 * <p>
	 * The returned reader supports marking, and ignores the flush method.
	 * @return a reader that reads from this builder */
	public Reader reader () {
		return new StringBuilderReader();
	}

	/** Gets this builder as a Writer that can be written to.
	 * <p>
	 * This method allows you to populate the contents of the builder using any standard method that takes a Writer.
	 * <p>
	 * To use, simply create a {@code StrBuilder}, call {@code asWriter}, and populate away. The data is available at any time
	 * using the methods of the {@code StrBuilder}.
	 * <p>
	 * The internal character array is shared between the builder and the writer. This allows you to intermix calls that append to
	 * the builder and write using the writer and the changes will be occur correctly. Note however, that no synchronization
	 * occurs, so you must perform all operations with the builder and the writer in one thread.
	 * <p>
	 * The returned writer ignores the close and flush methods.
	 * @return a writer that populates this builder */
	public Writer writer () {
		return new StringBuilderWriter();
	}

	/** Gets the current size of the internal character array buffer.
	 * @return The capacity */
	public int capacity () {
		return chars.length;
	}

	/** Gets the character at the specified index.
	 * @see #setCharAt(int, char)
	 * @see #deleteCharAt(int)
	 * @return The character at the index
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public char charAt (int index) {
		validateIndex(index);
		return chars[index];
	}

	/** Retrieves the Unicode code point value at the {@code index}.
	 * @param index the index to the {@code char} code unit.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than or equal to {@link #length()}.
	 * @see Character
	 * @see Character#codePointAt(char[], int, int) */
	public int codePointAt (int index) {
		if (index < 0 || index >= length) throw new StringIndexOutOfBoundsException(index);
		return Character.codePointAt(chars, index, length);
	}

	/** Retrieves the Unicode code point value that precedes the {@code index}.
	 * @param index the index to the {@code char} code unit within this object.
	 * @return the Unicode code point value.
	 * @throws IndexOutOfBoundsException if {@code index} is less than 1 or greater than {@link #length()}.
	 * @see Character
	 * @see Character#codePointBefore(char[], int, int) */
	public int codePointBefore (int index) {
		if (index < 1 || index > length) throw new StringIndexOutOfBoundsException(index);
		return Character.codePointBefore(chars, index);
	}

	/** Calculates the number of Unicode code points between {@code beginIndex} and {@code endIndex}.
	 * @param beginIndex the inclusive beginning index of the subsequence.
	 * @param endIndex the exclusive end index of the subsequence.
	 * @return the number of Unicode code points in the subsequence.
	 * @throws IndexOutOfBoundsException if {@code beginIndex} is negative or greater than {@code endIndex} or {@code endIndex} is
	 *            greater than {@link #length()}.
	 * @see Character
	 * @see Character#codePointCount(char[], int, int) */
	public int codePointCount (int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > length || beginIndex > endIndex) throw new StringIndexOutOfBoundsException();
		return Character.codePointCount(chars, beginIndex, endIndex - beginIndex);
	}

	/** Returns the index that is offset {@code codePointOffset} code points from {@code index}.
	 * @param index the index to calculate the offset from.
	 * @param codePointOffset the number of code points to count.
	 * @return the index that is {@code codePointOffset} code points away from index.
	 * @throws IndexOutOfBoundsException if {@code index} is negative or greater than {@link #length()} or if there aren't enough
	 *            code points before or after {@code index} to match {@code codePointOffset}.
	 * @see Character
	 * @see Character#offsetByCodePoints(char[], int, int, int, int) */
	public int offsetByCodePoints (int index, int codePointOffset) {
		return Character.offsetByCodePoints(chars, 0, length, index, codePointOffset);
	}

	/** Clears the string builder (convenience Collections API style method).
	 * <p>
	 * This method does not reduce the size of the internal character buffer. To do that, call {@code clear()} followed by
	 * {@link #minimizeCapacity()}.
	 * <p>
	 * This method is the same as {@link #setLength(int)} called with zero and is provided to match the API of Collections. */
	public StringBuilder clear () {
		length = 0;
		return this;
	}

	/** Tests if the string builder contains the specified char.
	 * @return true if the builder contains the character */
	public boolean contains (char ch) {
		char[] thisBuf = chars;
		for (int i = 0; i < length; i++)
			if (thisBuf[i] == ch) return true;
		return false;
	}

	/** Tests if the string builder contains the specified string.
	 * @return true if the builder contains the string */
	public boolean contains (String str) {
		return indexOf(str, 0) >= 0;
	}

	public boolean containsIgnoreCase (String str) {
		return indexOfIgnoreCase(str, 0) != -1;
	}

	/** Deletes the characters between the two specified indices.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive, must be valid except that if too large it is treated as end of string
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder delete (int startIndex, int endIndex) {
		int actualEndIndex = validateRange(startIndex, endIndex);
		int len = actualEndIndex - startIndex;
		if (len > 0) deleteImpl(startIndex, actualEndIndex, len);
		return this;
	}

	/** Deletes the character wherever it occurs in the builder. */
	public StringBuilder deleteAll (char ch) {
		for (int i = 0; i < length; i++) {
			if (chars[i] == ch) {
				int start = i;
				while (++i < length)
					if (chars[i] != ch) break;
				int len = i - start;
				deleteImpl(start, i, len);
				i -= len;
			}
		}
		return this;
	}

	/** Deletes the string wherever it occurs in the builder. */
	public StringBuilder deleteAll (String str) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		int len = str.length();
		if (len > 0) {
			int index = indexOf(str, 0);
			while (index >= 0) {
				deleteImpl(index, index + len, len);
				index = indexOf(str, index);
			}
		}
		return this;
	}

	/** Deletes the character at the specified index.
	 * @see #charAt(int)
	 * @see #setCharAt(int, char)
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder deleteCharAt (int index) {
		validateIndex(index);
		deleteImpl(index, index + 1, 1);
		return this;
	}

	/** Deletes the character wherever it occurs in the builder. */
	public StringBuilder deleteFirst (char ch) {
		for (int i = 0; i < length; i++) {
			if (chars[i] == ch) {
				deleteImpl(i, i + 1, 1);
				break;
			}
		}
		return this;
	}

	/** Deletes the string wherever it occurs in the builder. */
	public StringBuilder deleteFirst (String str) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		int len = str.length();
		if (len > 0) {
			int index = indexOf(str, 0);
			if (index >= 0) deleteImpl(index, index + len, len);
		}
		return this;
	}

	/** Internal method to delete a range without validation.
	 * @param startIndex the start index
	 * @param endIndex the end index (exclusive)
	 * @param len the length
	 * @throws IndexOutOfBoundsException if any index is invalid */
	private void deleteImpl (int startIndex, int endIndex, int len) {
		System.arraycopy(chars, endIndex, chars, startIndex, length - endIndex);
		length -= len;
	}

	/** Gets the character at the specified index before deleting it.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int)
	 * @return The character at the index
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public char drainChar (int index) {
		validateIndex(index);
		char c = chars[index];
		deleteCharAt(index);
		return c;
	}

	/** Drains (copies, then deletes) this character sequence into the specified array. This is equivalent to copying the
	 * characters from this sequence into the target and then deleting those character from this sequence.
	 * @param startIndex first index to copy, inclusive.
	 * @param endIndex last index to copy, exclusive.
	 * @param target the target array.
	 * @param targetIndex the index to start copying in the target.
	 * @return How many characters were copied (then deleted). If this builder is empty, return {@code 0}. */
	public int drainChars (int startIndex, int endIndex, char[] target, int targetIndex) {
		int len = endIndex - startIndex;
		if (isEmpty() || len == 0 || target.length == 0) return 0;
		int actualLen = Math.min(Math.min(length, len), target.length - targetIndex);
		getChars(startIndex, startIndex + actualLen, target, targetIndex);
		delete(startIndex, startIndex + actualLen);
		return actualLen;
	}

	/** Checks whether this builder ends with the specified string.
	 * @return true if the builder ends with the string */
	public boolean endsWith (String str) {
		int len = str.length();
		if (len == 0) return true;
		if (len > length) return false;
		int pos = length - len;
		for (int i = 0; i < len; i++, pos++)
			if (chars[pos] != str.charAt(i)) return false;
		return true;
	}

	/** Tests the capacity and ensures that it is at least the size specified.
	 * <p>
	 * Note: This method can be used to minimise memory reallocations during repeated addition of values by pre-allocating the
	 * character buffer. The method ignores a negative {@code capacity} argument.
	 * @throws OutOfMemoryError if the capacity cannot be allocated */
	public StringBuilder ensureCapacity (int capacity) {
		if (capacity > 0) ensureCapacityInternal(capacity);
		return this;
	}

	/** Ensures that the buffer is at least the size specified. The {@code capacity} argument is treated as an unsigned integer.
	 * <p>
	 * This method will raise an {@link OutOfMemoryError} if the capacity is too large for an array, or cannot be allocated.
	 * @throws OutOfMemoryError if the capacity cannot be allocated */
	private void ensureCapacityInternal (int capacity) {
		// Check for overflow of the current buffer.
		// Assumes capacity is an unsigned integer up to Integer.MAX_VALUE * 2
		// (the largest possible addition of two maximum length arrays).
		if (capacity - chars.length > 0) resizeBuffer(capacity);
	}

	/** Copies this character array into the specified array.
	 * @param target the target array, null will cause an array to be created
	 * @return The input array, unless that was null or too small */
	public char[] getChars (@Null char[] target) {
		int len = length();
		if (target == null || target.length < len) target = new char[len];
		System.arraycopy(chars, 0, target, 0, len);
		return target;
	}

	/** Copies this character array into the specified array.
	 * @param startIndex first index to copy, inclusive.
	 * @param endIndex last index to copy, exclusive.
	 * @param target the target array, must not too small.
	 * @param targetIndex the index to start copying in target.
	 * @throws NullPointerException if the array is null.
	 * @throws IndexOutOfBoundsException if any index is invalid. */
	public void getChars (int startIndex, int endIndex, char[] target, int targetIndex) {
		if (startIndex < 0) throw new StringIndexOutOfBoundsException(startIndex);
		if (endIndex < 0 || endIndex > length()) throw new StringIndexOutOfBoundsException(endIndex);
		if (startIndex > endIndex) throw new StringIndexOutOfBoundsException("end < start");
		System.arraycopy(chars, startIndex, target, targetIndex, endIndex - startIndex);
	}

	/** Searches the string builder to find the first reference to the specified char.
	 * @return The first index of the character, or -1 if not found */
	public int indexOf (char ch) {
		return indexOf(ch, 0);
	}

	/** Searches the string builder to find the first reference to the specified char.
	 * @param startIndex the index to start at, invalid index rounded to edge
	 * @return The first index of the character, or -1 if not found */
	public int indexOf (char ch, int startIndex) {
		startIndex = Math.max(0, startIndex);
		if (startIndex >= length) return -1;
		char[] thisBuf = chars;
		for (int i = startIndex; i < length; i++)
			if (thisBuf[i] == ch) return i;
		return -1;
	}

	/** Searches the string builder to find the first reference to the specified string.
	 * @return The first index of the string, or -1 if not found */
	public int indexOf (String str) {
		return indexOf(str, 0);
	}

	/** Searches the string builder to find the first reference to the specified string starting searching from the given index.
	 * @param startIndex the index to start at, invalid index rounded to edge
	 * @return The first index of the string, or -1 if not found */
	public int indexOf (String str, int startIndex) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		startIndex = Math.max(0, startIndex);
		if (startIndex >= length) return -1;
		int strLen = str.length();
		if (strLen == 1) return indexOf(str.charAt(0), startIndex);
		if (strLen == 0) return startIndex;
		if (strLen > length) return -1;
		char[] thisBuf = chars;
		int searchLen = length - strLen + 1;
		for (int i = startIndex; i < searchLen; i++) {
			boolean found = true;
			for (int j = 0; j < strLen && found; j++)
				found = str.charAt(j) == thisBuf[i + j];
			if (found) return i;
		}
		return -1;
	}

	public int indexOfIgnoreCase (String str, int startIndex) {
		if (startIndex < 0) startIndex = 0;
		int len = str.length();
		if (len == 0) return startIndex < length || startIndex == 0 ? startIndex : length;
		int maxIndex = length - len;
		if (startIndex > maxIndex) return -1;
		char firstUpper = Character.toUpperCase(str.charAt(0));
		char firstLower = Character.toLowerCase(firstUpper);
		while (true) {
			int i = startIndex;
			boolean found = false;
			for (; i <= maxIndex; i++) {
				char c = chars[i];
				if (c == firstUpper || c == firstLower) {
					found = true;
					break;
				}
			}
			if (!found) return -1;
			int o1 = i, o2 = 0;
			while (++o2 < len) {
				char c = chars[++o1];
				char upper = Character.toUpperCase(str.charAt(o2));
				if (c != upper && c != Character.toLowerCase(upper)) break;
			}
			if (o2 == len) return i;
			startIndex = i + 1;
		}
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, boolean value) {
		validateIndex(index);
		if (value) {
			ensureCapacityInternal(length + TRUE_STRING_SIZE);
			System.arraycopy(chars, index, chars, index + TRUE_STRING_SIZE, length - index);
			appendTrue(index);
		} else {
			ensureCapacityInternal(length + FALSE_STRING_SIZE);
			System.arraycopy(chars, index, chars, index + FALSE_STRING_SIZE, length - index);
			appendFalse(index);
		}
		return this;
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, char value) {
		validateIndex(index);
		ensureCapacityInternal(length + 1);
		System.arraycopy(chars, index, chars, index + 1, length - index);
		chars[index] = value;
		length++;
		return this;
	}

	/** Inserts the character array into this builder. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, @Null char[] ch) {
		validateIndex(index);
		if (ch == null) return insert(index, NULL);
		int len = ch.length;
		if (len > 0) {
			ensureCapacityInternal(length + len);
			System.arraycopy(chars, index, chars, index + len, length - index);
			System.arraycopy(ch, 0, chars, index, len);
			length += len;
		}
		return this;
	}

	/** Inserts part of the character array into this builder. Inserting null will use {@code "null"}.
	 * @param offset the offset into the character array to start at
	 * @param len the length of the character array part to copy, must be positive
	 * @throws IndexOutOfBoundsException if any index is invalid */
	public StringBuilder insert (int index, @Null char[] ch, int offset, int len) {
		validateIndex(index);
		if (ch == null) return insert(index, NULL);
		if (offset < 0 || offset > ch.length) throw new StringIndexOutOfBoundsException("Invalid offset: " + offset);
		if (len < 0 || offset + len > ch.length) throw new StringIndexOutOfBoundsException("Invalid length: " + len);
		if (len > 0) {
			ensureCapacityInternal(length + len);
			System.arraycopy(chars, index, chars, index + len, length - index);
			System.arraycopy(ch, offset, chars, index, len);
			length += len;
		}
		return this;
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, double value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, float value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, int value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the value into this builder.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, long value) {
		return insert(index, String.valueOf(value));
	}

	/** Inserts the string representation of an object into this builder. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, @Null Object obj) {
		if (obj == null) return insert(index, NULL);
		return insert(index, obj.toString());
	}

	/** Inserts the string into this builder. Inserting null will use {@code "null"}.
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder insert (int index, @Null String str) {
		if (index < 0 || index > length) throw new StringIndexOutOfBoundsException(index);
		if (str == null) str = NULL;
		int strLen = str.length();
		if (strLen > 0) {
			int newSize = length + strLen;
			ensureCapacityInternal(newSize);
			System.arraycopy(chars, index, chars, index + strLen, length - index);
			length = newSize;
			str.getChars(0, strLen, chars, index);
		}
		return this;
	}

	/** Checks is the string builder is empty (convenience Collections API style method).
	 * <p>
	 * This method is the same as checking {@link #length()} and is provided to match the API of Collections.
	 * @return {@code true} if the size is {@code 0}. */
	public boolean isEmpty () {
		return length == 0;
	}

	/** Checks is the string builder is not empty.
	 * <p>
	 * This method is the same as checking {@link #length()}.
	 * @return {@code true} if the size is not {@code 0}. */
	public boolean notEmpty () {
		return length != 0;
	}

	/** Searches the string builder to find the last reference to the specified char.
	 * @return The last index of the character, or -1 if not found */
	public int lastIndexOf (char ch) {
		return lastIndexOf(ch, length - 1);
	}

	/** Searches the string builder to find the last reference to the specified char.
	 * @param startIndex the index to start at, invalid index rounded to edge
	 * @return The last index of the character, or -1 if not found */
	public int lastIndexOf (char ch, int startIndex) {
		startIndex = startIndex >= length ? length - 1 : startIndex;
		if (startIndex < 0) return -1;
		for (int i = startIndex; i >= 0; i--)
			if (chars[i] == ch) return i;
		return -1;
	}

	/** Searches the string builder to find the last reference to the specified string.
	 * @return The last index of the string, or -1 if not found */
	public int lastIndexOf (String str) {
		return lastIndexOf(str, length - 1);
	}

	/** Searches the string builder to find the last reference to the specified string starting searching from the given index.
	 * @param startIndex the index to start at, invalid index rounded to edge
	 * @return The last index of the string, or -1 if not found */
	public int lastIndexOf (String str, int startIndex) {
		if (str == null) throw new IllegalArgumentException("str cannot be null.");
		startIndex = startIndex >= length ? length - 1 : startIndex;
		if (startIndex < 0) return -1;
		int strLen = str.length();
		if (strLen == 0) return startIndex;
		if (strLen > length) return -1;
		if (strLen == 1) return lastIndexOf(str.charAt(0), startIndex);
		for (int i = startIndex - strLen + 1; i >= 0; i--) {
			boolean found = true;
			for (int j = 0; j < strLen && found; j++)
				found = str.charAt(j) == chars[i + j];
			if (found) return i;
		}
		return -1;
	}

	/** Extracts the leftmost characters from the string builder without throwing an exception.
	 * <p>
	 * This method extracts the left {@code length} characters from the builder. If this many characters are not available, the
	 * whole builder is returned. Thus the returned string may be shorter than the length requested.
	 * @param len the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String leftString (int len) {
		if (len <= 0) return "";
		if (len >= length) return new String(chars, 0, length);
		return new String(chars, 0, len);
	}

	/** Gets the length of the string builder.
	 * @return The length */
	public int length () {
		return length;
	}

	/** Extracts some characters from the middle of the string builder without throwing an exception.
	 * <p>
	 * This method extracts {@code length} characters from the builder at the specified index. If the index is negative it is
	 * treated as zero. If the index is greater than the builder size, it is treated as the builder size. If the length is
	 * negative, the empty string is returned. If insufficient characters are available in the builder, as much as possible is
	 * returned. Thus the returned string may be shorter than the length requested.
	 * @param index the index to start at, negative means zero
	 * @param len the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String midString (int index, int len) {
		if (index < 0) index = 0;
		if (len <= 0 || index >= length) return "";
		if (length <= index + len) return new String(chars, index, length - index);
		return new String(chars, index, len);
	}

	/** Minimizes the capacity to the actual length of the string. */
	public StringBuilder minimizeCapacity () {
		if (chars.length > length) reallocate(length);
		return this;
	}

	/** If possible, reads chars from the provided {@link CharBuffer} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read.
	 * @see #appendTo(Appendable) */
	public int readFrom (CharBuffer charBuffer) {
		int oldSize = length;
		int remaining = charBuffer.remaining();
		ensureCapacityInternal(length + remaining);
		charBuffer.get(chars, length, remaining);
		length += remaining;
		return length - oldSize;
	}

	/** If possible, reads all chars from the provided {@link Readable} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read
	 * @throws IOException if an I/O error occurs.
	 * @see #appendTo(Appendable) */
	public int readFrom (Readable readable) throws IOException {
		if (readable instanceof Reader) return readFrom((Reader)readable);
		if (readable instanceof CharBuffer) return readFrom((CharBuffer)readable);
		int oldSize = length;
		while (true) {
			ensureCapacityInternal(length + 1);
			CharBuffer buf = CharBuffer.wrap(chars, length, chars.length - length);
			int read = readable.read(buf);
			if (read == EOS) break;
			length += read;
		}
		return length - oldSize;
	}

	/** If possible, reads all chars from the provided {@link Reader} directly into underlying character buffer without making
	 * extra copies.
	 * @return The number of characters read or -1 if we reached the end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @see #appendTo(Appendable) */
	public int readFrom (Reader reader) throws IOException {
		int oldSize = length;
		ensureCapacityInternal(length + 1);
		int readCount = reader.read(chars, length, chars.length - length);
		if (readCount == EOS) return EOS;
		do {
			length += readCount;
			ensureCapacityInternal(length + 1);
			readCount = reader.read(chars, length, chars.length - length);
		} while (readCount != EOS);
		return length - oldSize;
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
		int oldSize = length;
		ensureCapacityInternal(length + count);
		int target = count;
		int readCount = reader.read(chars, length, target);
		if (readCount == EOS) return EOS;
		do {
			target -= readCount;
			length += readCount;
			readCount = reader.read(chars, length, target);
		} while (target > 0 && readCount != EOS);
		return length - oldSize;
	}

	/** Reallocates the buffer to the new length. */
	private void reallocate (int len) {
		chars = Arrays.copyOf(chars, len);
	}

	/** Replaces a portion of the string builder with another string. The length of the inserted string does not have to match the
	 * removed length.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive, must be valid except that if too large it is treated as end of string
	 * @param replaceStr the string to replace with
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder replace (int startIndex, int endIndex, String replaceStr) {
		endIndex = validateRange(startIndex, endIndex);
		replaceImpl(startIndex, endIndex, endIndex - startIndex, replaceStr, replaceStr.length());
		return this;
	}

	/** Replaces the search character with the replace character throughout the builder. */
	public StringBuilder replaceAll (char search, char replace) {
		if (search != replace) {
			for (int i = 0; i < length; i++)
				if (chars[i] == search) chars[i] = replace;
		}
		return this;
	}

	/** Replaces the search string with the replace string throughout the builder. */
	public StringBuilder replaceAll (String searchStr, String replaceStr) {
		int searchLen = searchStr.length();
		if (searchLen > 0) {
			int replaceLen = replaceStr.length();
			int index = indexOf(searchStr, 0);
			while (index >= 0) {
				replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
				index = indexOf(searchStr, index + replaceLen);
			}
		}
		return this;
	}

	/** Replaces the search char with the replace string throughout the builder. */
	public StringBuilder replace (char find, String replace) {
		int replaceLength = replace.length();
		int index = 0;
		while (true) {
			while (true) {
				if (index == length) return this;
				if (chars[index] == find) break;
				index++;
			}
			replaceImpl(index, index + 1, 1, replace, replaceLength);
			index += replaceLength;
		}
	}

	/** Replaces the first instance of the search character with the replace character in the builder. */
	public StringBuilder replaceFirst (char search, char replace) {
		if (search != replace) {
			for (int i = 0; i < length; i++) {
				if (chars[i] == search) {
					chars[i] = replace;
					break;
				}
			}
		}
		return this;
	}

	/** Replaces the first instance of the search string with the replace string.
	 * @param replaceStr the replace string, null is equivalent to an empty string */
	public StringBuilder replaceFirst (String searchStr, String replaceStr) {
		int searchLen = searchStr.length();
		if (searchLen > 0) {
			int index = indexOf(searchStr, 0);
			if (index >= 0) {
				int replaceLen = replaceStr.length();
				replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
			}
		}
		return this;
	}

	/** Internal method to delete a range without validation.
	 * @param startIndex the start index
	 * @param endIndex the end index (exclusive)
	 * @param removeLen the length to remove (endIndex - startIndex)
	 * @param insertStr the string to replace with, can be null if insertLen is 0
	 * @param insertLen the length of the insert string
	 * @throws IndexOutOfBoundsException if any index is invalid */
	private void replaceImpl (int startIndex, int endIndex, int removeLen, @Null String insertStr, int insertLen) {
		int newSize = length - removeLen + insertLen;
		if (insertLen != removeLen) {
			ensureCapacityInternal(newSize);
			System.arraycopy(chars, endIndex, chars, startIndex + insertLen, length - endIndex);
			length = newSize;
		}
		if (insertLen > 0) insertStr.getChars(0, insertLen, chars, startIndex);
	}

	/** Resizes the buffer to at least the size specified.
	 * @throws OutOfMemoryError if the {@code minCapacity} is negative */
	private void resizeBuffer (int minCapacity) {
		// Overflow-conscious code treats the min and new capacity as unsigned.
		int oldCapacity = chars.length;
		int newCapacity = (oldCapacity >> 1) + oldCapacity + 2;
		if (Integer.compareUnsigned(newCapacity, minCapacity) < 0) newCapacity = minCapacity;
		if (Integer.compareUnsigned(newCapacity, MAX_BUFFER_SIZE) > 0) newCapacity = createPositiveCapacity(minCapacity);
		reallocate(newCapacity);
	}

	/** Reverses the string builder. Surrogate pairs are kept together. */
	public StringBuilder reverse () {
		if (length < 2) return this;
		int end = length - 1;
		char frontHigh = chars[0];
		char endLow = chars[end];
		boolean allowFrontSur = true, allowEndSur = true;
		for (int i = 0, mid = length / 2; i < mid; i++, --end) {
			char frontLow = chars[i + 1];
			char endHigh = chars[end - 1];
			boolean surAtFront = allowFrontSur && frontLow >= 0xdc00 && frontLow <= 0xdfff && frontHigh >= 0xd800
				&& frontHigh <= 0xdbff;
			if (surAtFront && length < 3) return this;
			boolean surAtEnd = allowEndSur && endHigh >= 0xd800 && endHigh <= 0xdbff && endLow >= 0xdc00 && endLow <= 0xdfff;
			allowFrontSur = allowEndSur = true;
			if (surAtFront == surAtEnd) {
				if (surAtFront) { // both surrogates
					chars[end] = frontLow;
					chars[end - 1] = frontHigh;
					chars[i] = endHigh;
					chars[i + 1] = endLow;
					frontHigh = chars[i + 2];
					endLow = chars[end - 2];
					i++;
					end--;
				} else { // neither surrogates
					chars[end] = frontHigh;
					chars[i] = endLow;
					frontHigh = frontLow;
					endLow = endHigh;
				}
			} else if (surAtFront) { // surrogate only at the front
				chars[end] = frontLow;
				chars[i] = endLow;
				endLow = endHigh;
				allowFrontSur = false;
			} else { // surrogate only at the end
				chars[end] = frontHigh;
				chars[i] = endHigh;
				frontHigh = frontLow;
				allowEndSur = false;
			}
		}
		if ((length & 1) == 1 && (!allowFrontSur || !allowEndSur)) chars[end] = allowFrontSur ? endLow : frontHigh;
		return this;
	}

	/** Extracts the rightmost characters from the string builder without throwing an exception.
	 * <p>
	 * This method extracts the right {@code len} characters from the builder. If this many characters are not available, the whole
	 * builder is returned. Thus the returned string may be shorter than the length requested.
	 * @param len the number of characters to extract, negative returns empty string
	 * @return The new string */
	public String rightString (int len) {
		if (len <= 0) return "";
		if (len >= length) return new String(chars, 0, length);
		return new String(chars, length - len, len);
	}

	/** Clears and sets this builder to the given value.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int) */
	public StringBuilder set (CharSequence str) {
		clear();
		append(str);
		return this;
	}

	/** Sets the character at the specified index.
	 * @see #charAt(int)
	 * @see #deleteCharAt(int)
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public StringBuilder setCharAt (int index, char ch) {
		validateIndex(index);
		chars[index] = ch;
		return this;
	}

	/** Updates the length of the builder by either dropping the last characters or adding filler of Unicode zero.
	 * @param len the length to set to, must be zero or positive
	 * @throws IndexOutOfBoundsException if the length is negative */
	public StringBuilder setLength (int len) {
		if (len < 0) throw new StringIndexOutOfBoundsException(len);
		if (len < length)
			length = len;
		else if (len > length) {
			ensureCapacityInternal(len);
			int oldEnd = length;
			length = len;
			Arrays.fill(chars, oldEnd, len, '\0');
		}
		return this;
	}

	/** Checks whether this builder starts with the specified string.
	 * @param str the string to search for, null returns false
	 * @return true if the builder starts with the string */
	public boolean startsWith (String str) {
		int len = str.length();
		if (len == 0) return true;
		if (len > length) return false;
		for (int i = 0; i < len; i++)
			if (chars[i] != str.charAt(i)) return false;
		return true;
	}

	public CharSequence subSequence (int startIndex, int endIndex) {
		if (startIndex < 0) throw new StringIndexOutOfBoundsException(startIndex);
		if (endIndex > length) throw new StringIndexOutOfBoundsException(endIndex);
		if (startIndex > endIndex) throw new StringIndexOutOfBoundsException(endIndex - startIndex);
		return substring(startIndex, endIndex);
	}

	/** Extracts a portion of this string builder as a string.
	 * @param start the start index, inclusive
	 * @return The new string
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public String substring (int start) {
		return substring(start, length);
	}

	/** Extracts a portion of this string builder as a string.
	 * <p>
	 * Note: This method treats an endIndex greater than the length of the builder as equal to the length of the builder, and
	 * continues without error, unlike StringBuffer or String.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive, must be valid except that if too large it is treated as end of string
	 * @return The new string
	 * @throws IndexOutOfBoundsException if the index is invalid */
	public String substring (int startIndex, int endIndex) {
		endIndex = validateRange(startIndex, endIndex);
		return new String(chars, startIndex, endIndex - startIndex);
	}

	/** Copies the builder's character array into a new character array.
	 * @return a new array that represents the contents of the builder */
	public char[] toCharArray () {
		return Arrays.copyOf(chars, length);
	}

	/** Copies part of the builder's character array into a new character array.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive, must be valid except that if too large it is treated as end of string
	 * @return a new array that holds part of the contents of the builder
	 * @throws IndexOutOfBoundsException if startIndex is invalid, or if endIndex is invalid (but endIndex greater than size is
	 *            valid) */
	public char[] toCharArray (int startIndex, int endIndex) {
		endIndex = validateRange(startIndex, endIndex);
		return Arrays.copyOfRange(chars, startIndex, endIndex);
	}

	/** Gets a String version of the string builder, creating a new instance each time the method is called.
	 * <p>
	 * Note that unlike StringBuffer, the string version returned is independent of the string builder.
	 * @return The builder as a String */
	public String toString () {
		if (length == 0) return "";
		return new String(chars, 0, length);
	}

	/** Returns the current String representation and clears the StringBuilder.
	 * @return a String containing the characters in this instance. */
	public String toStringAndClear () {
		String string = toString();
		clear();
		return string;
	}

	/** Trims the builder by removing characters less than or equal to a space from the beginning and end. */
	public StringBuilder trim () {
		if (length == 0) return this;
		int len = length;
		char[] buf = chars;
		int pos = 0;
		while (pos < len && buf[pos] <= ' ')
			pos++;
		while (pos < len && buf[len - 1] <= ' ')
			len--;
		if (len < length) delete(len, length);
		if (pos > 0) delete(0, pos);
		return this;
	}

	public void shrink () {
		if (length < chars.length) {
			char[] newValue = new char[length];
			System.arraycopy(chars, 0, newValue, 0, length);
			chars = newValue;
		}
	}

	/** Resizes {@link #chars} if its length is more than {@link #length()}.
	 * @deprecated Use {@link #shrink()}. */
	@Deprecated
	public void trimToSize () {
		shrink();
	}

	/** Validates that an index is in the range {@code 0 <= index <= size}.
	 * @throws IndexOutOfBoundsException Thrown when the index is not the range {@code 0 <= index <= size}. */
	protected void validateIndex (int index) {
		if (index < 0 || index >= length) throw new StringIndexOutOfBoundsException(index);
	}

	/** Validates parameters defining a range of the builder.
	 * @param startIndex the start index, inclusive
	 * @param endIndex the end index, exclusive, must be valid except that if too large it is treated as end of string
	 * @return A valid end index.
	 * @throws StringIndexOutOfBoundsException if the index is invalid */
	protected int validateRange (int startIndex, int endIndex) {
		if (startIndex < 0) throw new StringIndexOutOfBoundsException(startIndex);
		if (endIndex > length) endIndex = length;
		if (startIndex > endIndex) throw new StringIndexOutOfBoundsException("end < start");
		return endIndex;
	}

	/** Tests the contents of this builder against another to see if they contain the same character content.
	 * @param obj the object to check, null returns false
	 * @return true if the builders contain the same characters in the same order */
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StringBuilder other = (StringBuilder)obj;
		int length = this.length;
		if (length != other.length) return false;
		char[] chars = this.chars, chars2 = other.chars;
		for (int i = 0; i < length; i++)
			if (chars[i] != chars2[i]) return false;
		return true;
	}

	/** Tests the contents of this builder against another to see if they contain the same character content.
	 * @param other the object to check, null returns false
	 * @return true if the builders contain the same characters in the same order */
	public boolean equals (StringBuilder other) {
		if (this == other) return true;
		if ((other == null) || (length != other.length)) return false;
		int length = this.length;
		if (length != other.length) return false;
		char[] chars = this.chars, chars2 = other.chars;
		for (int i = 0; i < length; i++)
			if (chars[i] != chars2[i]) return false;
		return true;
	}

	/** Tests the contents of this builder against another to see if they contain the same character content ignoring case.
	 * @param other the object to check, null returns false
	 * @return true if the builders contain the same characters in the same order */
	public boolean equalsIgnoreCase (StringBuilder other) {
		if (this == other) return true;
		if (other == null) return false;
		int length = this.length;
		if (length != other.length) return false;
		char[] chars = this.chars, chars2 = other.chars;
		for (int i = 0; i < length; i++) {
			char c = chars[i];
			char upper = Character.toUpperCase(chars2[i]);
			if (c != upper && c != Character.toLowerCase(upper)) return false;
		}
		return true;
	}

	public boolean equalsIgnoreCase (@Null String other) {
		if (other == null) return false;
		int length = this.length;
		if (length != other.length()) return false;
		char[] chars = this.chars;
		for (int i = 0; i < length; i++) {
			char c = chars[i];
			char upper = Character.toUpperCase(other.charAt(i));
			if (c != upper && c != Character.toLowerCase(upper)) return false;
		}
		return true;
	}

	public int hashCode () {
		char[] chars = this.chars;
		int result = 31 + length;
		for (int index = 0; index < length; ++index)
			result = 31 * result + chars[index];
		return result;
	}

	/** Creates a positive capacity at least as large the minimum required capacity. If the minimum capacity is negative then this
	 * throws an OutOfMemoryError as no array can be allocated.
	 * @return the capacity
	 * @throws OutOfMemoryError if the {@code minCapacity} is negative */
	private static int createPositiveCapacity (int minCapacity) {
		if (minCapacity < 0) // overflow
			throw new OutOfMemoryError("Unable to allocate array size: " + Integer.toUnsignedString(minCapacity));
		// This is called when we require buffer expansion to a very big array.
		// Use the conservative maximum buffer size if possible, otherwise the biggest required.
		//
		// Note: In this situation JDK 1.8 java.util.ArrayList returns Integer.MAX_VALUE.
		// This excludes some VMs that can exceed MAX_BUFFER_SIZE but not allocate a full
		// Integer.MAX_VALUE length array.
		// The result is that we may have to allocate an array of this size more than once if
		// the capacity must be expanded again.
		return Math.max(minCapacity, MAX_BUFFER_SIZE);
	}

	/** Constructs an instance from a reference to a character array. Changes to the input chars are reflected in this instance
	 * until the internal buffer needs to be reallocated. Using a reference to an array allows the instance to be initialized
	 * without copying the input array.
	 * @param initialBuffer The initial array that will back the new builder.
	 * @return A new instance. */
	public static StringBuilder wrap (char[] initialBuffer) {
		Objects.requireNonNull(initialBuffer, "initialBuffer");
		return new StringBuilder(initialBuffer, initialBuffer.length);
	}

	/** Constructs an instance from a reference to a character array. Changes to the input chars are reflected in this instance
	 * until the internal buffer needs to be reallocated. Using a reference to an array allows the instance to be initialized
	 * without copying the input array.
	 * @param initialBuffer The initial array that will back the new builder.
	 * @param len The length of the subarray to be used; must be non-negative and no larger than {@code initialBuffer.length}. The
	 *           new builder's size will be set to {@code length}.
	 * @return A new instance. */
	public static StringBuilder wrap (char[] initialBuffer, int len) {
		return new StringBuilder(initialBuffer, len);
	}

	/** @return the number of characters required to represent the value with the specified radix */
	public static int numChars (int value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0)
			++result;
		return result;
	}

	/** @return the number of characters required to represent the value with the specified radix */
	public static int numChars (long value, int radix) {
		int result = (value < 0) ? 2 : 1;
		while ((value /= radix) != 0)
			++result;
		return result;
	}

	class StringBuilderReader extends Reader {
		private int mark;
		private int pos;

		StringBuilderReader () {
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

		public int read (char[] b, int off, int len) {
			if (off < 0 || len < 0 || off > b.length || off + len > b.length || off + len < 0) throw new IndexOutOfBoundsException();
			if (len == 0) return 0;
			if (pos >= length) return -1;
			if (pos + len > length) len = length - pos;
			StringBuilder.this.getChars(pos, pos + len, b, off);
			pos += len;
			return len;
		}

		public boolean ready () {
			return pos < length;
		}

		public void reset () {
			pos = mark;
		}

		public long skip (long n) {
			if (pos + n > length) n = length - pos;
			if (n < 0) return 0;
			pos = Math.addExact(pos, Math.toIntExact(n));
			return n;
		}
	}

	class StringBuilderWriter extends Writer {
		StringBuilderWriter () {
		}

		public void close () {
		}

		public void flush () {
		}

		public void write (char[] cbuf) {
			StringBuilder.this.append(cbuf);
		}

		public void write (char[] cbuf, int off, int len) {
			StringBuilder.this.append(cbuf, off, len);
		}

		public void write (int c) {
			StringBuilder.this.append((char)c);
		}

		public void write (String str) {
			StringBuilder.this.append(str);
		}

		public void write (String str, int off, int len) {
			StringBuilder.this.append(str, off, len);
		}
	}
}
