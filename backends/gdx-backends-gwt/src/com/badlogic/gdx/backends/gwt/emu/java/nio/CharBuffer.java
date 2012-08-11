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

package java.nio;

import java.io.IOException;

/** A buffer of chars.
 * <p>
 * A char buffer can be created in either one of the following ways:
 * </p>
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new char array and create a buffer based on it;</li>
 * <li>{@link #wrap(char[]) Wrap} an existing char array to create a new buffer;</li>
 * <li>{@link #wrap(CharSequence) Wrap} an existing char sequence to create a new buffer;</li>
 * <li>Use {@link java.nio.ByteBuffer#asCharBuffer() ByteBuffer.asCharBuffer} to create a char buffer based on a byte buffer.</li>
 * </ul>
 * 
 * @since Android 1.0 */
public abstract class CharBuffer extends Buffer implements Comparable<CharBuffer>, CharSequence, Appendable {// , Readable {

	/** Creates a char buffer based on a newly allocated char array.
	 * 
	 * @param capacity the capacity of the new buffer.
	 * @return the created char buffer.
	 * @throws IllegalArgumentException if {@code capacity} is less than zero.
	 * @since Android 1.0 */
	public static CharBuffer allocate (int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		return BufferFactory.newCharBuffer(capacity);
	}

	/** Creates a new char buffer by wrapping the given char array.
	 * <p>
	 * Calling this method has the same effect as {@code wrap(array, 0, array.length)}.
	 * </p>
	 * 
	 * @param array the char array which the new buffer will be based on.
	 * @return the created char buffer.
	 * @since Android 1.0 */
	public static CharBuffer wrap (char[] array) {
		return wrap(array, 0, array.length);
	}

	/** Creates a new char buffer by wrapping the given char array.
	 * <p>
	 * The new buffer's position will be {@code start}, limit will be {@code start + len}, capacity will be the length of the array.
	 * </p>
	 * 
	 * @param array the char array which the new buffer will be based on.
	 * @param start the start index, must not be negative and not greater than {@code array.length}.
	 * @param len the length, must not be negative and not greater than {@code array.length - start}.
	 * @return the created char buffer.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code len} is invalid.
	 * @since Android 1.0 */
	public static CharBuffer wrap (char[] array, int start, int len) {
		int length = array.length;
		if ((start < 0) || (len < 0) || (long)start + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}

		CharBuffer buf = BufferFactory.newCharBuffer(array);
		buf.position = start;
		buf.limit = start + len;

		return buf;
	}

	/** Creates a new char buffer by wrapping the given char sequence.
	 * <p>
	 * Calling this method has the same effect as {@code wrap(chseq, 0, chseq.length())}.
	 * </p>
	 * 
	 * @param chseq the char sequence which the new buffer will be based on.
	 * @return the created char buffer.
	 * @since Android 1.0 */
	public static CharBuffer wrap (CharSequence chseq) {
		return BufferFactory.newCharBuffer(chseq);
	}

	/** Creates a new char buffer by wrapping the given char sequence.
	 * <p>
	 * The new buffer's position will be {@code start}, limit will be {@code end}, capacity will be the length of the char sequence.
	 * The new buffer is read-only.
	 * </p>
	 * 
	 * @param chseq the char sequence which the new buffer will be based on.
	 * @param start the start index, must not be negative and not greater than {@code chseq.length()}.
	 * @param end the end index, must be no less than {@code start} and no greater than {@code chseq.length()}.
	 * @return the created char buffer.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code end} is invalid.
	 * @since Android 1.0 */
	public static CharBuffer wrap (CharSequence chseq, int start, int end) {
		if (chseq == null) {
			throw new NullPointerException();
		}
		if (start < 0 || end < start || end > chseq.length()) {
			throw new IndexOutOfBoundsException();
		}

		CharBuffer result = BufferFactory.newCharBuffer(chseq);
		result.position = start;
		result.limit = end;
		return result;
	}

	/** Constructs a {@code CharBuffer} with given capacity.
	 * 
	 * @param capacity the capacity of the buffer.
	 * @since Android 1.0 */
	CharBuffer (int capacity) {
		super(capacity);
	}

	/** Returns the char array which this buffer is based on, if there is one.
	 * 
	 * @return the char array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on an array, but it is read-only.
	 * @exception UnsupportedOperationException if this buffer is not based on an array.
	 * @since Android 1.0 */
	public final char[] array () {
		return protectedArray();
	}

	/** Returns the offset of the char array which this buffer is based on, if there is one.
	 * <p>
	 * The offset is the index of the array corresponds to the zero position of the buffer.
	 * </p>
	 * 
	 * @return the offset of the char array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on an array but it is read-only.
	 * @exception UnsupportedOperationException if this buffer is not based on an array.
	 * @since Android 1.0 */
	public final int arrayOffset () {
		return protectedArrayOffset();
	}

	/** Returns a read-only buffer that shares its content with this buffer.
	 * <p>
	 * The returned buffer is guaranteed to be a new instance, even if this buffer is read-only itself. The new buffer's position,
	 * limit, capacity and mark are the same as this buffer's.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means this buffer's change of content will be visible to the new
	 * buffer. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a read-only version of this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer asReadOnlyBuffer ();

	/** Returns the character located at the specified index in the buffer. The index value is referenced from the current buffer
	 * position.
	 * 
	 * @param index the index referenced from the current buffer position. It must not be less than zero but less than the value
	 *           obtained from a call to {@code remaining()}.
	 * @return the character located at the specified index (referenced from the current position) in the buffer.
	 * @exception IndexOutOfBoundsException if the index is invalid.
	 * @since Android 1.0 */
	public final char charAt (int index) {
		if (index < 0 || index >= remaining()) {
			throw new IndexOutOfBoundsException();
		}
		return get(position + index);
	}

	/** Compacts this char buffer.
	 * <p>
	 * The remaining chars will be moved to the head of the buffer, starting from position zero. Then the position is set to
	 * {@code remaining()}; the limit is set to capacity; the mark is cleared.
	 * </p>
	 * 
	 * @return this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer compact ();

	/** Compare the remaining chars of this buffer to another char buffer's remaining chars.
	 * 
	 * @param otherBuffer another char buffer.
	 * @return a negative value if this is less than {@code otherBuffer}; 0 if this equals to {@code otherBuffer}; a positive value
	 *         if this is greater than {@code otherBuffer}.
	 * @exception ClassCastException if {@code otherBuffer} is not a char buffer.
	 * @since Android 1.0 */
	public int compareTo (CharBuffer otherBuffer) {
		int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining() : otherBuffer.remaining();
		int thisPos = position;
		int otherPos = otherBuffer.position;
		char thisByte, otherByte;
		while (compareRemaining > 0) {
			thisByte = get(thisPos);
			otherByte = otherBuffer.get(otherPos);
			if (thisByte != otherByte) {
				return thisByte < otherByte ? -1 : 1;
			}
			thisPos++;
			otherPos++;
			compareRemaining--;
		}
		return remaining() - otherBuffer.remaining();
	}

	/** Returns a duplicated buffer that shares its content with this buffer.
	 * <p>
	 * The duplicated buffer's initial position, limit, capacity and mark are the same as this buffer's. The duplicated buffer's
	 * read-only property and byte order are the same as this buffer's, too.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a duplicated buffer that shares its content with this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer duplicate ();

	/** Checks whether this char buffer is equal to another object.
	 * <p>
	 * If {@code other} is not a char buffer then {@code false} is returned. Two char buffers are equal if and only if their
	 * remaining chars are exactly the same. Position, limit, capacity and mark are not considered.
	 * </p>
	 * 
	 * @param other the object to compare with this char buffer.
	 * @return {@code true} if this char buffer is equal to {@code other}, {@code false} otherwise.
	 * @since Android 1.0 */
	public boolean equals (Object other) {
		if (!(other instanceof CharBuffer)) {
			return false;
		}
		CharBuffer otherBuffer = (CharBuffer)other;

		if (remaining() != otherBuffer.remaining()) {
			return false;
		}

		int myPosition = position;
		int otherPosition = otherBuffer.position;
		boolean equalSoFar = true;
		while (equalSoFar && (myPosition < limit)) {
			equalSoFar = get(myPosition++) == otherBuffer.get(otherPosition++);
		}

		return equalSoFar;
	}

	/** Returns the char at the current position and increases the position by 1.
	 * 
	 * @return the char at the current position.
	 * @exception BufferUnderflowException if the position is equal or greater than limit.
	 * @since Android 1.0 */
	public abstract char get ();

	/** Reads chars from the current position into the specified char array and increases the position by the number of chars read.
	 * <p>
	 * Calling this method has the same effect as {@code get(dest, 0, dest.length)}.
	 * </p>
	 * 
	 * @param dest the destination char array.
	 * @return this buffer.
	 * @exception BufferUnderflowException if {@code dest.length} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public CharBuffer get (char[] dest) {
		return get(dest, 0, dest.length);
	}

	/** Reads chars from the current position into the specified char array, starting from the specified offset, and increases the
	 * position by the number of chars read.
	 * 
	 * @param dest the target char array.
	 * @param off the offset of the char array, must not be negative and not greater than {@code dest.length}.
	 * @param len The number of chars to read, must be no less than zero and no greater than {@code dest.length - off}.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception BufferUnderflowException if {@code len} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public CharBuffer get (char[] dest, int off, int len) {
		int length = dest.length;
		if ((off < 0) || (len < 0) || (long)off + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}

		if (len > remaining()) {
			throw new BufferUnderflowException();
		}
		for (int i = off; i < off + len; i++) {
			dest[i] = get();
		}
		return this;
	}

	/** Returns a char at the specified index; the position is not changed.
	 * 
	 * @param index the index, must not be negative and less than limit.
	 * @return a char at the specified index.
	 * @exception IndexOutOfBoundsException if index is invalid.
	 * @since Android 1.0 */
	public abstract char get (int index);

	/** Indicates whether this buffer is based on a char array and is read/write.
	 * 
	 * @return {@code true} if this buffer is based on a byte array and provides read/write access, {@code false} otherwise.
	 * @since Android 1.0 */
	public final boolean hasArray () {
		return protectedHasArray();
	}

	/** Calculates this buffer's hash code from the remaining chars. The position, limit, capacity and mark don't affect the hash
	 * code.
	 * 
	 * @return the hash code calculated from the remaining chars.
	 * @since Android 1.0 */
	public int hashCode () {
		int myPosition = position;
		int hash = 0;
		while (myPosition < limit) {
			hash = hash + get(myPosition++);
		}
		return hash;
	}

	/** Indicates whether this buffer is direct. A direct buffer will try its best to take advantage of native memory APIs and it
	 * may not stay in the Java heap, so it is not affected by garbage collection.
	 * <p>
	 * A char buffer is direct if it is based on a byte buffer and the byte buffer is direct.
	 * </p>
	 * 
	 * @return {@code true} if this buffer is direct, {@code false} otherwise.
	 * @since Android 1.0 */
	public abstract boolean isDirect ();

	/** Returns the number of remaining chars.
	 * 
	 * @return the number of remaining chars.
	 * @since Android 1.0 */
	public final int length () {
		return remaining();
	}

	/** Returns the byte order used by this buffer when converting chars from/to bytes.
	 * <p>
	 * If this buffer is not based on a byte buffer, then this always returns the platform's native byte order.
	 * </p>
	 * 
	 * @return the byte order used by this buffer when converting chars from/to bytes.
	 * @since Android 1.0 */
	public abstract ByteOrder order ();

	/** Child class implements this method to realize {@code array()}.
	 * 
	 * @see #array() */
	abstract char[] protectedArray ();

	/** Child class implements this method to realize {@code arrayOffset()}.
	 * 
	 * @see #arrayOffset() */
	abstract int protectedArrayOffset ();

	/** Child class implements this method to realize {@code hasArray()}.
	 * 
	 * @see #hasArray() */
	abstract boolean protectedHasArray ();

	/** Writes the given char to the current position and increases the position by 1.
	 * 
	 * @param c the char to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is equal or greater than limit.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer put (char c);

	/** Writes chars from the given char array to the current position and increases the position by the number of chars written.
	 * <p>
	 * Calling this method has the same effect as {@code put(src, 0, src.length)}.
	 * </p>
	 * 
	 * @param src the source char array.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code src.length}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public final CharBuffer put (char[] src) {
		return put(src, 0, src.length);
	}

	/** Writes chars from the given char array, starting from the specified offset, to the current position and increases the
	 * position by the number of chars written.
	 * 
	 * @param src the source char array.
	 * @param off the offset of char array, must not be negative and not greater than {@code src.length}.
	 * @param len the number of chars to write, must be no less than zero and no greater than {@code src.length - off}.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code len}.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer put (char[] src, int off, int len) {
		int length = src.length;
		if ((off < 0) || (len < 0) || (long)off + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}

		if (len > remaining()) {
			throw new BufferOverflowException();
		}
		for (int i = off; i < off + len; i++) {
			put(src[i]);
		}
		return this;
	}

	/** Writes all the remaining chars of the {@code src} char buffer to this buffer's current position, and increases both buffers'
	 * position by the number of chars copied.
	 * 
	 * @param src the source char buffer.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code src.remaining()} is greater than this buffer's {@code remaining()}.
	 * @exception IllegalArgumentException if {@code src} is this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer put (CharBuffer src) {
		if (src == this) {
			throw new IllegalArgumentException();
		}
		if (src.remaining() > remaining()) {
			throw new BufferOverflowException();
		}

		char[] contents = new char[src.remaining()];
		src.get(contents);
		put(contents);
		return this;
	}

	/** Writes a char to the specified index of this buffer; the position is not changed.
	 * 
	 * @param index the index, must be no less than zero and less than the limit.
	 * @param c the char to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if index is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer put (int index, char c);

	/** Writes all chars of the given string to the current position of this buffer, and increases the position by the length of
	 * string.
	 * <p>
	 * Calling this method has the same effect as {@code put(str, 0, str.length())}.
	 * </p>
	 * 
	 * @param str the string to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than the length of string.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public final CharBuffer put (String str) {
		return put(str, 0, str.length());
	}

	/** Writes chars of the given string to the current position of this buffer, and increases the position by the number of chars
	 * written.
	 * 
	 * @param str the string to write.
	 * @param start the first char to write, must not be negative and not greater than {@code str.length()}.
	 * @param end the last char to write (excluding), must be less than {@code start} and not greater than {@code str.length()}.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code end - start}.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code end} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer put (String str, int start, int end) {
		int length = str.length();
		if (start < 0 || end < start || end > length) {
			throw new IndexOutOfBoundsException();
		}

		if (end - start > remaining()) {
			throw new BufferOverflowException();
		}
		for (int i = start; i < end; i++) {
			put(str.charAt(i));
		}
		return this;
	}

	/** Returns a sliced buffer that shares its content with this buffer.
	 * <p>
	 * The sliced buffer's capacity will be this buffer's {@code remaining()}, and its zero position will correspond to this
	 * buffer's current position. The new buffer's position will be 0, limit will be its capacity, and its mark is cleared. The new
	 * buffer's read-only property and byte order are same as this buffer.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a sliced buffer that shares its content with this buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer slice ();

	/** Returns a new char buffer representing a sub-sequence of this buffer's current remaining content.
	 * <p>
	 * The new buffer's position will be {@code position() + start}, limit will be {@code position() + end}, capacity will be the
	 * same as this buffer. The new buffer's read-only property and byte order are the same as this buffer.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @param start the start index of the sub-sequence, referenced from the current buffer position. Must not be less than zero
	 *           and not greater than the value obtained from a call to {@code remaining()}.
	 * @param end the end index of the sub-sequence, referenced from the current buffer position. Must not be less than
	 *           {@code start} and not be greater than the value obtained from a call to {@code remaining()}.
	 * @return a new char buffer represents a sub-sequence of this buffer's current remaining content.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code end} is invalid.
	 * @since Android 1.0 */
	public abstract CharSequence subSequence (int start, int end);

	/** Returns a string representing the current remaining chars of this buffer.
	 * 
	 * @return a string representing the current remaining chars of this buffer.
	 * @since Android 1.0 */
	public String toString () {
		StringBuffer strbuf = new StringBuffer();
		for (int i = position; i < limit; i++) {
			strbuf.append(get(i));
		}
		return strbuf.toString();
	}

	/** Writes the given char to the current position and increases the position by 1.
	 * 
	 * @param c the char to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is equal or greater than limit.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer append (char c) {
		return put(c);
	}

	/** Writes all chars of the given character sequence {@code csq} to the current position of this buffer, and increases the
	 * position by the length of the csq.
	 * <p>
	 * Calling this method has the same effect as {@code append(csq.toString())}.
	 * </p>
	 * If the {@code CharSequence} is {@code null} the string "null" will be written to the buffer.
	 * 
	 * @param csq the {@code CharSequence} to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than the length of csq.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer append (CharSequence csq) {
		if (csq != null) {
			return put(csq.toString());
		}
		return put("null"); //$NON-NLS-1$
	}

	/** Writes chars of the given {@code CharSequence} to the current position of this buffer, and increases the position by the
	 * number of chars written.
	 * 
	 * @param csq the {@code CharSequence} to write.
	 * @param start the first char to write, must not be negative and not greater than {@code csq.length()}.
	 * @param end the last char to write (excluding), must be less than {@code start} and not greater than {@code csq.length()}.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code end - start}.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code end} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public CharBuffer append (CharSequence csq, int start, int end) {
		if (csq == null) {
			csq = "null"; //$NON-NLS-1$
		}
		CharSequence cs = csq.subSequence(start, end);
		if (cs.length() > 0) {
			return put(cs.toString());
		}
		return this;
	}

	/** Reads characters from this buffer and puts them into {@code target}. The number of chars that are copied is either the
	 * number of remaining chars in this buffer or the number of remaining chars in {@code target}, whichever is smaller.
	 * 
	 * @param target the target char buffer.
	 * @throws IllegalArgumentException if {@code target} is this buffer.
	 * @throws IOException if an I/O error occurs.
	 * @throws ReadOnlyBufferException if no changes may be made to the contents of {@code target}.
	 * @return the number of chars copied or -1 if there are no chars left to be read from this buffer.
	 * @since Android 1.0 */
	public int read (CharBuffer target) throws IOException {
		if (target == this) {
			throw new IllegalArgumentException();
		}
		if (remaining() == 0) {
			return target.remaining() == 0 ? 0 : -1;
		}
		int result = Math.min(target.remaining(), remaining());
		char[] chars = new char[result];
		get(chars);
		target.put(chars);
		return result;
	}
}
