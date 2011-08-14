/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

import com.google.gwt.corp.compatibility.Numbers;

/** HeapByteBuffer, ReadWriteHeapByteBuffer and ReadOnlyHeapByteBuffer compose the implementation of array based byte buffers.
 * <p>
 * ReadWriteHeapByteBuffer extends HeapByteBuffer with all the write methods.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p> */
final class ReadWriteHeapByteBuffer extends HeapByteBuffer {

	static ReadWriteHeapByteBuffer copy (HeapByteBuffer other, int markOfOther) {
		ReadWriteHeapByteBuffer buf = new ReadWriteHeapByteBuffer(other.backingArray, other.capacity(), other.offset);
		buf.limit = other.limit();
		buf.position = other.position();
		buf.mark = markOfOther;
		buf.order(other.order());
		return buf;
	}

	ReadWriteHeapByteBuffer (byte[] backingArray) {
		super(backingArray);
	}

	ReadWriteHeapByteBuffer (int capacity) {
		super(capacity);
	}

	ReadWriteHeapByteBuffer (byte[] backingArray, int capacity, int arrayOffset) {
		super(backingArray, capacity, arrayOffset);
	}

	public ByteBuffer asReadOnlyBuffer () {
		return ReadOnlyHeapByteBuffer.copy(this, mark);
	}

	public ByteBuffer compact () {
		System.arraycopy(backingArray, position + offset, backingArray, offset, remaining());
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	public ByteBuffer duplicate () {
		return copy(this, mark);
	}

	public boolean isReadOnly () {
		return false;
	}

	protected byte[] protectedArray () {
		return backingArray;
	}

	protected int protectedArrayOffset () {
		return offset;
	}

	protected boolean protectedHasArray () {
		return true;
	}

	public ByteBuffer put (byte b) {
		if (position == limit) {
			throw new BufferOverflowException();
		}
		backingArray[offset + position++] = b;
		return this;
	}

	public ByteBuffer put (int index, byte b) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		backingArray[offset + index] = b;
		return this;
	}

	/*
	 * Override ByteBuffer.put(byte[], int, int) to improve performance.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see java.nio.ByteBuffer#put(byte[], int, int)
	 */
	public ByteBuffer put (byte[] src, int off, int len) {
		if (off < 0 || len < 0 || (long)off + (long)len > src.length) {
			throw new IndexOutOfBoundsException();
		}
		if (len > remaining()) {
			throw new BufferOverflowException();
		}
		if (isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		System.arraycopy(src, off, backingArray, offset + position, len);
		position += len;
		return this;
	}

	public ByteBuffer putDouble (double value) {
		return putLong(Numbers.doubleToRawLongBits(value));
	}

	public ByteBuffer putDouble (int index, double value) {
		return putLong(index, Numbers.doubleToRawLongBits(value));
	}

	public ByteBuffer putFloat (float value) {
		return putInt(Numbers.floatToIntBits(value));
	}

	public ByteBuffer putFloat (int index, float value) {
		return putInt(index, Numbers.floatToIntBits(value));
	}

	public ByteBuffer putInt (int value) {
		int newPosition = position + 4;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		store(position, value);
		position = newPosition;
		return this;
	}

	public ByteBuffer putInt (int index, int value) {
		if (index < 0 || (long)index + 4 > limit) {
			throw new IndexOutOfBoundsException();
		}
		store(index, value);
		return this;
	}

	public ByteBuffer putLong (int index, long value) {
		if (index < 0 || (long)index + 8 > limit) {
			throw new IndexOutOfBoundsException();
		}
		store(index, value);
		return this;
	}

	public ByteBuffer putLong (long value) {
		int newPosition = position + 8;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		store(position, value);
		position = newPosition;
		return this;
	}

	public ByteBuffer putShort (int index, short value) {
		if (index < 0 || (long)index + 2 > limit) {
			throw new IndexOutOfBoundsException();
		}
		store(index, value);
		return this;
	}

	public ByteBuffer putShort (short value) {
		int newPosition = position + 2;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		store(position, value);
		position = newPosition;
		return this;
	}

	public ByteBuffer slice () {
		ReadWriteHeapByteBuffer slice = new ReadWriteHeapByteBuffer(backingArray, remaining(), offset + position);
		slice.order = order;
		return slice;
	}
}
