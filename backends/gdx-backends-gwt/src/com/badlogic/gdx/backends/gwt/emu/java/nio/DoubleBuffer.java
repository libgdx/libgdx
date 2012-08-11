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

/** A buffer of doubles.
 * <p>
 * A double buffer can be created in either one of the following ways:
 * </p>
 * <ul>
 * <li>{@link #allocate(int) Allocate} a new double array and create a buffer based on it;</li>
 * <li>{@link #wrap(double[]) Wrap} an existing double array to create a new buffer;</li>
 * <li>Use {@link java.nio.ByteBuffer#asDoubleBuffer() ByteBuffer.asDoubleBuffer} to create a double buffer based on a byte buffer.
 * </li>
 * </ul>
 * 
 * @since Android 1.0 */
public abstract class DoubleBuffer extends Buffer implements Comparable<DoubleBuffer> {

	/** Creates a double buffer based on a newly allocated double array.
	 * 
	 * @param capacity the capacity of the new buffer.
	 * @return the created double buffer.
	 * @throws IllegalArgumentException if {@code capacity} is less than zero.
	 * @since Android 1.0 */
	public static DoubleBuffer allocate (int capacity) {
		if (capacity < 0) {
			throw new IllegalArgumentException();
		}
		return BufferFactory.newDoubleBuffer(capacity);
	}

	/** Creates a new double buffer by wrapping the given double array.
	 * <p>
	 * Calling this method has the same effect as {@code wrap(array, 0, array.length)}.
	 * </p>
	 * 
	 * @param array the double array which the new buffer will be based on.
	 * @return the created double buffer.
	 * @since Android 1.0 */
	public static DoubleBuffer wrap (double[] array) {
		return wrap(array, 0, array.length);
	}

	/** Creates a new double buffer by wrapping the given double array.
	 * <p>
	 * The new buffer's position will be {@code start}, limit will be {@code start + len}, capacity will be the length of the array.
	 * </p>
	 * 
	 * @param array the double array which the new buffer will be based on.
	 * @param start the start index, must not be negative and not greater than {@code array.length}.
	 * @param len the length, must not be negative and not greater than {@code array.length - start}.
	 * @return the created double buffer.
	 * @exception IndexOutOfBoundsException if either {@code start} or {@code len} is invalid.
	 * @since Android 1.0 */
	public static DoubleBuffer wrap (double[] array, int start, int len) {
		int length = array.length;
		if (start < 0 || len < 0 || (long)start + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}

		DoubleBuffer buf = BufferFactory.newDoubleBuffer(array);
		buf.position = start;
		buf.limit = start + len;

		return buf;
	}

	/** Constructs a {@code DoubleBuffer} with given capacity.
	 * 
	 * @param capacity the capacity of the buffer. */
	DoubleBuffer (int capacity) {
		super(capacity);
	}

	/** Returns the double array which this buffer is based on, if there is one.
	 * 
	 * @return the double array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on an array but it is read-only.
	 * @exception UnsupportedOperationException if this buffer is not based on an array.
	 * @since Android 1.0 */
	public final double[] array () {
		return protectedArray();
	}

	/** Returns the offset of the double array which this buffer is based on, if there is one.
	 * <p>
	 * The offset is the index of the array corresponding to the zero position of the buffer.
	 * </p>
	 * 
	 * @return the offset of the double array which this buffer is based on.
	 * @exception ReadOnlyBufferException if this buffer is based on an array, but it is read-only.
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
	 * The new buffer shares its content with this buffer, which means that this buffer's change of content will be visible to the
	 * new buffer. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a read-only version of this buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer asReadOnlyBuffer ();

	/** Compacts this double buffer.
	 * <p>
	 * The remaining doubles will be moved to the head of the buffer, staring from position zero. Then the position is set to
	 * {@code remaining()}; the limit is set to capacity; the mark is cleared.
	 * </p>
	 * 
	 * @return this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer compact ();

	/** Compare the remaining doubles of this buffer to another double buffer's remaining doubles.
	 * 
	 * @param otherBuffer another double buffer.
	 * @return a negative value if this is less than {@code other}; 0 if this equals to {@code other}; a positive value if this is
	 *         greater than {@code other}.
	 * @exception ClassCastException if {@code other} is not a double buffer.
	 * @since Android 1.0 */
	public int compareTo (DoubleBuffer otherBuffer) {
		int compareRemaining = (remaining() < otherBuffer.remaining()) ? remaining() : otherBuffer.remaining();
		int thisPos = position;
		int otherPos = otherBuffer.position;
		// BEGIN android-changed
		double thisDouble, otherDouble;
		while (compareRemaining > 0) {
			thisDouble = get(thisPos);
			otherDouble = otherBuffer.get(otherPos);
			// checks for double and NaN inequality
			if ((thisDouble != otherDouble) && ((thisDouble == thisDouble) || (otherDouble == otherDouble))) {
				return thisDouble < otherDouble ? -1 : 1;
			}
			thisPos++;
			otherPos++;
			compareRemaining--;
		}
		// END android-changed
		return remaining() - otherBuffer.remaining();
	}

	/** Returns a duplicated buffer that shares its content with this buffer.
	 * <p>
	 * The duplicated buffer's position, limit, capacity and mark are the same as this buffer's. The duplicated buffer's read-only
	 * property and byte order are the same as this buffer's, too.
	 * </p>
	 * <p>
	 * The new buffer shares its content with this buffer, which means either buffer's change of content will be visible to the
	 * other. The two buffer's position, limit and mark are independent.
	 * </p>
	 * 
	 * @return a duplicated buffer that shares its content with this buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer duplicate ();

	/** Checks whether this double buffer is equal to another object.
	 * <p>
	 * If {@code other} is not a double buffer then {@code false} is returned. Two double buffers are equal if and only if their
	 * remaining doubles are exactly the same. Position, limit, capacity and mark are not considered.
	 * </p>
	 * 
	 * @param other the object to compare with this double buffer.
	 * @return {@code true} if this double buffer is equal to {@code other}, {@code false} otherwise.
	 * @since Android 1.0 */
	public boolean equals (Object other) {
		if (!(other instanceof DoubleBuffer)) {
			return false;
		}
		DoubleBuffer otherBuffer = (DoubleBuffer)other;

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

	/** Returns the double at the current position and increases the position by 1.
	 * 
	 * @return the double at the current position.
	 * @exception BufferUnderflowException if the position is equal or greater than limit.
	 * @since Android 1.0 */
	public abstract double get ();

	/** Reads doubles from the current position into the specified double array and increases the position by the number of doubles
	 * read.
	 * <p>
	 * Calling this method has the same effect as {@code get(dest, 0, dest.length)}.
	 * </p>
	 * 
	 * @param dest the destination double array.
	 * @return this buffer.
	 * @exception BufferUnderflowException if {@code dest.length} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public DoubleBuffer get (double[] dest) {
		return get(dest, 0, dest.length);
	}

	/** Reads doubles from the current position into the specified double array, starting from the specified offset, and increases
	 * the position by the number of doubles read.
	 * 
	 * @param dest the target double array.
	 * @param off the offset of the double array, must not be negative and not greater than {@code dest.length}.
	 * @param len the number of doubles to read, must be no less than zero and not greater than {@code dest.length - off}.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception BufferUnderflowException if {@code len} is greater than {@code remaining()}.
	 * @since Android 1.0 */
	public DoubleBuffer get (double[] dest, int off, int len) {
		int length = dest.length;
		if (off < 0 || len < 0 || (long)off + (long)len > length) {
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

	/** Returns a double at the specified index; the position is not changed.
	 * 
	 * @param index the index, must not be negative and less than limit.
	 * @return a double at the specified index.
	 * @exception IndexOutOfBoundsException if index is invalid.
	 * @since Android 1.0 */
	public abstract double get (int index);

	/** Indicates whether this buffer is based on a double array and is read/write.
	 * 
	 * @return {@code true} if this buffer is based on a double array and provides read/write access, {@code false} otherwise.
	 * @since Android 1.0 */
	public final boolean hasArray () {
		return protectedHasArray();
	}

// /**
// * Calculates this buffer's hash code from the remaining chars. The
// * position, limit, capacity and mark don't affect the hash code.
// *
// * @return the hash code calculated from the remaining chars.
// * @since Android 1.0
// */
// public int hashCode() {
// int myPosition = position;
// int hash = 0;
// long l;
// while (myPosition < limit) {
// l = Double.doubleToLongBits(get(myPosition++));
// hash = hash + ((int) l) ^ ((int) (l >> 32));
// }
// return hash;
// }

	/** Indicates whether this buffer is direct. A direct buffer will try its best to take advantage of native memory APIs and it
	 * may not stay in the Java heap, so it is not affected by garbage collection.
	 * <p>
	 * A double buffer is direct if it is based on a byte buffer and the byte buffer is direct.
	 * </p>
	 * 
	 * @return {@code true} if this buffer is direct, {@code false} otherwise.
	 * @since Android 1.0 */
	public abstract boolean isDirect ();

	/** Returns the byte order used by this buffer when converting doubles from/to bytes.
	 * <p>
	 * If this buffer is not based on a byte buffer, then this always returns the platform's native byte order.
	 * </p>
	 * 
	 * @return the byte order used by this buffer when converting doubles from/to bytes.
	 * @since Android 1.0 */
	public abstract ByteOrder order ();

	/** Child class implements this method to realize {@code array()}.
	 * 
	 * @see #array() */
	abstract double[] protectedArray ();

	/** Child class implements this method to realize {@code arrayOffset()}.
	 * 
	 * @see #arrayOffset() */
	abstract int protectedArrayOffset ();

	/** Child class implements this method to realize {@code hasArray()}.
	 * 
	 * @see #hasArray() */
	abstract boolean protectedHasArray ();

	/** Writes the given double to the current position and increases the position by 1.
	 * 
	 * @param d the double to write.
	 * @return this buffer.
	 * @exception BufferOverflowException if position is equal or greater than limit.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer put (double d);

	/** Writes doubles from the given double array to the current position and increases the position by the number of doubles
	 * written.
	 * <p>
	 * Calling this method has the same effect as {@code put(src, 0, src.length)}.
	 * </p>
	 * 
	 * @param src the source double array.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code src.length}.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public final DoubleBuffer put (double[] src) {
		return put(src, 0, src.length);
	}

	/** Writes doubles from the given double array, starting from the specified offset, to the current position and increases the
	 * position by the number of doubles written.
	 * 
	 * @param src the source double array.
	 * @param off the offset of double array, must not be negative and not greater than {@code src.length}.
	 * @param len the number of doubles to write, must be no less than zero and not greater than {@code src.length - off}.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code remaining()} is less than {@code len}.
	 * @exception IndexOutOfBoundsException if either {@code off} or {@code len} is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public DoubleBuffer put (double[] src, int off, int len) {
		int length = src.length;
		if (off < 0 || len < 0 || (long)off + (long)len > length) {
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

	/** Writes all the remaining doubles of the {@code src} double buffer to this buffer's current position, and increases both
	 * buffers' position by the number of doubles copied.
	 * 
	 * @param src the source double buffer.
	 * @return this buffer.
	 * @exception BufferOverflowException if {@code src.remaining()} is greater than this buffer's {@code remaining()}.
	 * @exception IllegalArgumentException if {@code src} is this buffer.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public DoubleBuffer put (DoubleBuffer src) {
		if (src == this) {
			throw new IllegalArgumentException();
		}
		if (src.remaining() > remaining()) {
			throw new BufferOverflowException();
		}
		double[] doubles = new double[src.remaining()];
		src.get(doubles);
		put(doubles);
		return this;
	}

	/** Write a double to the specified index of this buffer and the position is not changed.
	 * 
	 * @param index the index, must not be negative and less than the limit.
	 * @param d the double to write.
	 * @return this buffer.
	 * @exception IndexOutOfBoundsException if index is invalid.
	 * @exception ReadOnlyBufferException if no changes may be made to the contents of this buffer.
	 * @since Android 1.0 */
	public abstract DoubleBuffer put (int index, double d);

	/** Returns a sliced buffer that shares its content with this buffer.
	 * <p>
	 * The sliced buffer's capacity will be this buffer's {@code remaining()}, and its zero position will correspond to this
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
	public abstract DoubleBuffer slice ();

	/** Returns a string representing the state of this double buffer.
	 * 
	 * @return A string representing the state of this double buffer.
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
}
