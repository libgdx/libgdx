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

import com.google.gwt.corp.compatibility.Endianness;
import com.google.gwt.corp.compatibility.StringToByteBuffer;

/** A buffer for bytes.
 * <p>
 * A byte buffer can be created in either one of the following ways:
 * </p>
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new byte array and create a buffer based on it;</li>
 * <li>{@link #allocateDirect(int) Allocate} a memory block and create a direct buffer based on it;</li>
 * <li>{@link #wrap(byte[]) Wrap} an existing byte array to create a new buffer.</li>
 * </ul>
 * @since Android 1.0 */
public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer>, StringToByteBuffer {

	/** Creates a byte buffer based on a newly allocated byte array.
	 * 
	 * @param capacity the capacity of the new buffer
	 * @return the created byte buffer.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 * @since Android 1.0 */
	public static ByteBuffer allocate (int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		return BufferFactory.newByteBuffer(capacity);
	}

	/** Creates a direct byte buffer based on a newly allocated memory block.
	 * 
	 * @param capacity the capacity of the new buffer
	 * @return the created byte buffer.
	 * @throws IllegalArgumentException if {@code capacity < 0}.
	 * @since Android 1.0 */
	public static ByteBuffer allocateDirect (int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		return BufferFactory.newDirectByteBuffer(capacity);
	}

	/** Creates a new byte buffer by wrapping the given byte array.
	 * <p>
	 * Calling this method has the same effect as {@code wrap(array, 0, array.length)}.
	 * </p>
	 * 
	 * @param array the byte array which the new buffer will be based on
	 * @return the created byte buffer.
	 * @since Android 1.0 */
	public static ByteBuffer wrap (byte[] array) {
		return BufferFactory.newByteBuffer(array);
	}

	/** Creates a new byte buffer by wrapping the given byte array.
	 * <p>
	 * The new buffer's position will be {@code start}, limit will be {@code start + len}, capacity will be the length of the array.
	 * </p>
	 * 
	 * @param array the byte array which the new buffer will be based on.
	 * @param start the start index, must not be negative and not greater than {@code array.length}.
	 * @param len the length, must not be negative and not greater than {@code array.length - start}.
	 * @return the created byte buffer.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code len} is invalid.
	 * @since Android 1.0 */
	public static ByteBuffer wrap (byte[] array, int start, int len) {
		int length = array.length;
		if ((start < 0) || (len < 0) || ((long)start + (long)len > length)) {
			throw new IndexOutOfBoundsException();
		}

		ByteBuffer buf = BufferFactory.newByteBuffer(array);
		buf.position = start;
		buf.limit = start + len;

		return buf;
	}

	/** The byte order of this buffer, default is {@code BIG_ENDIAN}. */
	Endianness order = Endianness.BIG_ENDIAN;

	/** Constructs a {@code ByteBuffer} with given capacity.
	 * 
	 * @param capacity the capacity of the buffer. */
	ByteBuffer (int capacity) {
		super(capacity);
	}

	/** Returns the byte array which this buffer is based on, if there is one.
	 * 
	 * @return the byte array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on a read-only array.
	 * @exception UnsupportedOperationException if this buffer is not based on an array.
	 * @since Android 1.0 */
	public final byte[] array () {
		return protectedArray();
	}

	/** Returns the offset of the byte array which this buffer is based on, if there is one.
	 * <p>
	 * The offset is the index of the array which corresponds to the zero position of the buffer.
	 * </p>
	 * 
	 * @return the offset of the byte array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on a read-only array.
	 * @exception UnsupportedOperationException if this buffer is not based on an array.
	 * @since Android 1.0 */
	public final int arrayOffset () {
		return protectedArrayOffset();
	}

	/** Returns a char buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by two, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a char buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract CharBuffer asCharBuffer ();

	/** Returns a double buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by eight, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a double buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer asDoubleBuffer ();

	/** Returns a float buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by four, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a float buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract FloatBuffer asFloatBuffer ();

	/** Returns a int buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by four, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a int buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract IntBuffer asIntBuffer ();

	/** Returns a long buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by eight, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a long buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract LongBuffer asLongBuffer ();

	/** Returns a read-only buffer that shares its content with this buffer.
	 * <p>
	 * The returned buffer is guaranteed to be a new instance, even if this buffer is read-only itself. The new buffer's position,
	 * limit, capacity and mark are the same as this buffer.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means this buffer's change of content will be visible to the new
	 * buffer. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a read-only version of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer asReadOnlyBuffer ();

	/** Returns a short buffer which is based on the remaining content of this byte buffer.
	 * <p>
	 * The new buffer's position is zero, its limit and capacity is the number of remaining bytes divided by two, and its mark is
	 * not set. The new buffer's read-only property and byte order are the same as this buffer's. The new buffer is direct if this
	 * byte buffer is direct.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a short buffer which is based on the content of this byte buffer.
	 * @since Android 1.0 */
	public abstract ShortBuffer asShortBuffer ();

	/** Compacts this byte buffer.
	 * <p>
	 * The remaining bytes will be moved to the head of the buffer, starting from position zero. Then the position is set to
	 * {@code remaining()}; the limit is set to capacity; the mark is cleared.
	 * </p>
	 * 
	 * @return this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer compact ();

	/** Compares the remaining bytes of this buffer to another byte buffer's remaining bytes.
	 * 
	 * @param otherBuffer another byte buffer.
	 * @return a negative value if this is less than {@code other}; 0 if this equals to {@code other}; a positive value if this is
	 *         greater than {@code other}.
	 * @exception ClassCastException if {@code other} is not a byte buffer.
	 * @since Android 1.0 */
	public int compareTo (ByteBuffer otherBuffer) {
		int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining() : otherBuffer.remaining();
		int thisPos = position;
		int otherPos = otherBuffer.position;
		byte thisByte, otherByte;
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
	 * The duplicated buffer's position, limit, capacity and mark are the same as this buffer's. The duplicated buffer's read-only
	 * property and byte order are the same as this buffer's too.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a duplicated buffer that shares its content with this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer duplicate ();

	/** Checks whether this byte buffer is equal to another object.
	 * <p>
	 * If {@code other} is not a byte buffer then {@code false} is returned. Two byte buffers are equal if and only if their
	 * remaining bytes are exactly the same. Position, limit, capacity and mark are not considered.
	 * </p>
	 * 
	 * @param other the object to compare with this byte buffer.
	 * @return {@code true} if this byte buffer is equal to {@code other}, {@code false} otherwise.
	 * @since Android 1.0 */
	public boolean equals (Object other) {
		if (!(other instanceof ByteBuffer)) {
			return false;
		}
		ByteBuffer otherBuffer = (ByteBuffer)other;

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

	/** Returns the byte at the current position and increases the position by 1.
	 * 
	 * @return the byte at the current position.
	 * @exception BufferUnderflowException if the position is equal or greater than limit.
	 * @since Android 1.0 */
	public abstract byte get ();

	/** Reads bytes from the current position into the specified byte array and increases the position by the number of bytes read.
	 * <p>
	 * Calling this method has the same effect as {@code get(dest, 0, dest.length)}.
	 * </p>
	 * 
	 * @param dest the destination byte array.
	 * @return this buffer.
	 * @exception BufferUnderflowException if {@code dest.length} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public ByteBuffer get (byte[] dest) {
		return get(dest, 0, dest.length);
	}

	/** Reads bytes from the current position into the specified byte array, starting at the specified offset, and increases the
	 * position by the number of bytes read.
	 * 
	 * @param dest the target byte array.
	 * @param off the offset of the byte array, must not be negative and not greater than {@code dest.length}.
	 * @param len the number of bytes to read, must not be negative and not greater than {@code dest.length - off}
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception BufferUnderflowException if {@code len} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public ByteBuffer get (byte[] dest, int off, int len) {
		int length = dest.length;
		if ((off < 0) || (len < 0) || ((long)off + (long)len > length)) {
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

	/** Returns the byte at the specified index and does not change the position.
	 * 
	 * @param index the index, must not be negative and less than limit.
	 * @return the byte at the specified index.
	 * @exception IndexOutOfBoundsException if index is invalid.
	 * @since Android 1.0 */
	public abstract byte get (int index);

	/** Returns the char at the current position and increases the position by 2.
	 * <p>
	 * The 2 bytes starting at the current position are composed into a char according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the char at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 2}.
	 * @since Android 1.0 */
	public abstract char getChar ();

	/** Returns the char at the specified index.
	 * <p>
	 * The 2 bytes starting from the specified index are composed into a char according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 2}.
	 * @return the char at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract char getChar (int index);

	/** Returns the double at the current position and increases the position by 8.
	 * <p>
	 * The 8 bytes starting from the current position are composed into a double according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the double at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 8}.
	 * @since Android 1.0 */
	public abstract double getDouble ();

	/** Returns the double at the specified index.
	 * <p>
	 * The 8 bytes starting at the specified index are composed into a double according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 8}.
	 * @return the double at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract double getDouble (int index);

	/** Returns the float at the current position and increases the position by 4.
	 * <p>
	 * The 4 bytes starting at the current position are composed into a float according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the float at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 4}.
	 * @since Android 1.0 */
	public abstract float getFloat ();

	/** Returns the float at the specified index.
	 * <p>
	 * The 4 bytes starting at the specified index are composed into a float according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 4}.
	 * @return the float at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract float getFloat (int index);

	/** Returns the int at the current position and increases the position by 4.
	 * <p>
	 * The 4 bytes starting at the current position are composed into a int according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the int at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 4}.
	 * @since Android 1.0 */
	public abstract int getInt ();

	/** Returns the int at the specified index.
	 * <p>
	 * The 4 bytes starting at the specified index are composed into a int according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 4}.
	 * @return the int at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract int getInt (int index);

	/** Returns the long at the current position and increases the position by 8.
	 * <p>
	 * The 8 bytes starting at the current position are composed into a long according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the long at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 8}.
	 * @since Android 1.0 */
	public abstract long getLong ();

	/** Returns the long at the specified index.
	 * <p>
	 * The 8 bytes starting at the specified index are composed into a long according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 8}.
	 * @return the long at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract long getLong (int index);

	/** Returns the short at the current position and increases the position by 2.
	 * <p>
	 * The 2 bytes starting at the current position are composed into a short according to the current byte order and returned.
	 * </p>
	 * 
	 * @return the short at the current position.
	 * @exception BufferUnderflowException if the position is greater than {@code limit - 2}.
	 * @since Android 1.0 */
	public abstract short getShort ();

	/** Returns the short at the specified index.
	 * <p>
	 * The 2 bytes starting at the specified index are composed into a short according to the current byte order and returned. The
	 * position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 2}.
	 * @return the short at the specified index.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @since Android 1.0 */
	public abstract short getShort (int index);

	/** Indicates whether this buffer is based on a byte array and provides read/write access.
	 * 
	 * @return {@code true} if this buffer is based on a byte array and provides read/write access, {@code false} otherwise.
	 * @since Android 1.0 */
	public final boolean hasArray () {
		return protectedHasArray();
	}

	/** Calculates this buffer's hash code from the remaining chars. The position, limit, capacity and mark don't affect the hash
	 * code.
	 * 
	 * @return the hash code calculated from the remaining bytes.
	 * @since Android 1.0 */
	public int hashCode () {
		int myPosition = position;
		int hash = 0;
		while (myPosition < limit) {
			hash = hash + get(myPosition++);
		}
		return hash;
	}

	/** Indicates whether this buffer is direct.
	 * 
	 * @return {@code true} if this buffer is direct, {@code false} otherwise.
	 * @since Android 1.0 */
	public abstract boolean isDirect ();

	/** Returns the byte order used by this buffer when converting bytes from/to other primitive types.
	 * <p>
	 * The default byte order of byte buffer is always {@link ByteOrder#BIG_ENDIAN BIG_ENDIAN}
	 * </p>
	 * 
	 * @return the byte order used by this buffer when converting bytes from/to other primitive types.
	 * @since Android 1.0 */
	public final ByteOrder order () {
		return order == Endianness.BIG_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	/** Sets the byte order of this buffer.
	 * 
	 * @param byteOrder the byte order to set. If {@code null} then the order will be {@link ByteOrder#LITTLE_ENDIAN LITTLE_ENDIAN}
	 *           .
	 * @return this buffer.
	 * @see ByteOrder
	 * @since Android 1.0 */
	public final ByteBuffer order (ByteOrder byteOrder) {
		return orderImpl(byteOrder);
	}

	ByteBuffer orderImpl (ByteOrder byteOrder) {
		order = byteOrder == ByteOrder.BIG_ENDIAN ? Endianness.BIG_ENDIAN : Endianness.LITTLE_ENDIAN;
		return this;
	}

	/** Child class implements this method to realize {@code array()}.
	 * 
	 * @see #array()
	 * @since Android 1.0 */
	abstract byte[] protectedArray ();

	/** Child class implements this method to realize {@code arrayOffset()}.
	 * 
	 * @see #arrayOffset()
	 * @since Android 1.0 */
	abstract int protectedArrayOffset ();

	/** Child class implements this method to realize {@code hasArray()}.
	 * 
	 * @see #hasArray()
	 * @since Android 1.0 */
	abstract boolean protectedHasArray ();

	/** Writes the given byte to the current position and increases the position by 1.
	 * 
	 * @param b the byte to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is equal or greater than limit.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer put (byte b);

	/** Writes bytes in the given byte array to the current position and increases the position by the number of bytes written.
	 * <p>
	 * Calling this method has the same effect as {@code put(src, 0, src.length)}.
	 * </p>
	 * 
	 * @param src the source byte array.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code src.length}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public final ByteBuffer put (byte[] src) {
		return put(src, 0, src.length);
	}

	/** Writes bytes in the given byte array, starting from the specified offset, to the current position and increases the position
	 * by the number of bytes written.
	 * 
	 * @param src the source byte array.
	 * @param off the offset of byte array, must not be negative and not greater than {@code src.length}.
	 * @param len the number of bytes to write, must not be negative and not greater than {@code src.length - off}.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code len}.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public ByteBuffer put (byte[] src, int off, int len) {
		int length = src.length;
		if ((off < 0) || (len < 0) || ((long)off + (long)len > length)) {
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

	/** Writes all the remaining bytes of the {@code src} byte buffer to this buffer's current position, and increases both buffers'
	 * position by the number of bytes copied.
	 * 
	 * @param src the source byte buffer.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code src.remaining()} is greater than this buffer's {@code remaining()}.
	 * @exception IllegalArgumentException if {@code src} is this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public ByteBuffer put (ByteBuffer src) {
		if (src == this) {
			throw new IllegalArgumentException();
		}
		if (src.remaining() > remaining()) {
			throw new BufferOverflowException();
		}
		byte[] contents = new byte[src.remaining()];
		src.get(contents);
		put(contents);
		return this;
	}

	/** Write a byte to the specified index of this buffer without changing the position.
	 * 
	 * @param index the index, must not be negative and less than the limit.
	 * @param b the byte to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer put (int index, byte b);

	/** Writes the given char to the current position and increases the position by 2.
	 * <p>
	 * The char is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the char to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 2}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putChar (char value);

	/** Writes the given char to the specified index of this buffer.
	 * <p>
	 * The char is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 2}.
	 * @param value the char to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putChar (int index, char value);

	/** Writes the given double to the current position and increases the position by 8.
	 * <p>
	 * The double is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the double to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 8}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putDouble (double value);

	/** Writes the given double to the specified index of this buffer.
	 * <p>
	 * The double is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 8}.
	 * @param value the double to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putDouble (int index, double value);

	/** Writes the given float to the current position and increases the position by 4.
	 * <p>
	 * The float is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the float to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 4}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putFloat (float value);

	/** Writes the given float to the specified index of this buffer.
	 * <p>
	 * The float is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 4}.
	 * @param value the float to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putFloat (int index, float value);

	/** Writes the given int to the current position and increases the position by 4.
	 * <p>
	 * The int is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the int to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 4}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putInt (int value);

	/** Writes the given int to the specified index of this buffer.
	 * <p>
	 * The int is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 4}.
	 * @param value the int to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putInt (int index, int value);

	/** Writes the given long to the current position and increases the position by 8.
	 * <p>
	 * The long is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the long to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 8}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putLong (long value);

	/** Writes the given long to the specified index of this buffer.
	 * <p>
	 * The long is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 8}.
	 * @param value the long to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putLong (int index, long value);

	/** Writes the given short to the current position and increases the position by 2.
	 * <p>
	 * The short is converted to bytes using the current byte order.
	 * </p>
	 * 
	 * @param value the short to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is greater than {@code limit - 2}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putShort (short value);

	/** Writes the given short to the specified index of this buffer.
	 * <p>
	 * The short is converted to bytes using the current byte order. The position is not changed.
	 * </p>
	 * 
	 * @param index the index, must not be negative and equal or less than {@code limit - 2}.
	 * @param value the short to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if {@code index} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer putShort (int index, short value);

	/** Returns a sliced buffer that shares its content with this buffer.
	 * <p>
	 * The sliced buffer's capacity will be this buffer's {@code remaining()}, and it's zero position will correspond to this
	 * buffer's current position. The new buffer's position will be 0, limit will be its capacity, and its mark is cleared. The new
	 * buffer's read-only property and byte order are the same as this buffer's.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a sliced buffer that shares its content with this buffer.
	 * @since Android 1.0 */
	public abstract ByteBuffer slice ();

	/** Returns a string representing the state of this byte buffer.
	 * 
	 * @return a string representing the state of this byte buffer.
	 * @since Android 1.0 */
	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getName());
		buf.append(", status: capacity="); //$NON-NLS-1$
		buf.append(capacity());
		buf.append(" position="); //$NON-NLS-1$
		buf.append(position());
		buf.append(" limit="); //$NON-NLS-1$
		buf.append(limit());
		return buf.toString();
	}

	public ByteBuffer stringToByteBuffer (String s) {
		return new StringByteBuffer(s);
	}
}
