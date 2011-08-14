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

/** IntArrayBuffer, ReadWriteIntArrayBuffer and ReadOnlyIntArrayBuffer compose the implementation of array based int buffers.
 * <p>
 * ReadWriteIntArrayBuffer extends IntArrayBuffer with all the write methods.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p> */
final class ReadWriteIntArrayBuffer extends IntArrayBuffer {

	static ReadWriteIntArrayBuffer copy (IntArrayBuffer other, int markOfOther) {
		ReadWriteIntArrayBuffer buf = new ReadWriteIntArrayBuffer(other.capacity(), other.backingArray, other.offset);
		buf.limit = other.limit();
		buf.position = other.position();
		buf.mark = markOfOther;
		return buf;
	}

	ReadWriteIntArrayBuffer (int[] array) {
		super(array);
	}

	ReadWriteIntArrayBuffer (int capacity) {
		super(capacity);
	}

	ReadWriteIntArrayBuffer (int capacity, int[] backingArray, int arrayOffset) {
		super(capacity, backingArray, arrayOffset);
	}

	public IntBuffer asReadOnlyBuffer () {
		return ReadOnlyIntArrayBuffer.copy(this, mark);
	}

	public IntBuffer compact () {
		System.arraycopy(backingArray, position + offset, backingArray, offset, remaining());
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	public IntBuffer duplicate () {
		return copy(this, mark);
	}

	public boolean isReadOnly () {
		return false;
	}

	protected int[] protectedArray () {
		return backingArray;
	}

	protected int protectedArrayOffset () {
		return offset;
	}

	protected boolean protectedHasArray () {
		return true;
	}

	public IntBuffer put (int c) {
		if (position == limit) {
			throw new BufferOverflowException();
		}
		backingArray[offset + position++] = c;
		return this;
	}

	public IntBuffer put (int index, int c) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		backingArray[offset + index] = c;
		return this;
	}

	public IntBuffer put (int[] src, int off, int len) {
		int length = src.length;
		if (off < 0 || len < 0 || (long)off + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}
		if (len > remaining()) {
			throw new BufferOverflowException();
		}
		System.arraycopy(src, off, backingArray, offset + position, len);
		position += len;
		return this;
	}

	public IntBuffer slice () {
		return new ReadWriteIntArrayBuffer(remaining(), backingArray, offset + position);
	}

}
