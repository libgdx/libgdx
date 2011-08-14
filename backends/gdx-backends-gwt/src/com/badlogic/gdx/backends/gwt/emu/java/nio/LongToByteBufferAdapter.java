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

//import org.apache.harmony.nio.internal.DirectBuffer;
//import org.apache.harmony.luni.platform.PlatformAddress;

/** This class wraps a byte buffer to be a long buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by the adapter. It must NOT be accessed outside the
 * adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter. The adapter extends Buffer, thus has its own position
 * and limit.</li>
 * </ul>
 * </p> */
final class LongToByteBufferAdapter extends LongBuffer {// implements DirectBuffer {

	static LongBuffer wrap (ByteBuffer byteBuffer) {
		return new LongToByteBufferAdapter(byteBuffer.slice());
	}

	private final ByteBuffer byteBuffer;

	LongToByteBufferAdapter (ByteBuffer byteBuffer) {
		super((byteBuffer.capacity() >> 3));
		this.byteBuffer = byteBuffer;
		this.byteBuffer.clear();
	}

// public int getByteCapacity() {
// if (byteBuffer instanceof DirectBuffer) {
// return ((DirectBuffer) byteBuffer).getByteCapacity();
// }
// assert false : byteBuffer;
// return -1;
// }
//
// public PlatformAddress getEffectiveAddress() {
// if (byteBuffer instanceof DirectBuffer) {
// return ((DirectBuffer) byteBuffer).getEffectiveAddress();
// }
// assert false : byteBuffer;
// return null;
// }
//
// public PlatformAddress getBaseAddress() {
// if (byteBuffer instanceof DirectBuffer) {
// return ((DirectBuffer) byteBuffer).getBaseAddress();
// }
// assert false : byteBuffer;
// return null;
// }
//
// public boolean isAddressValid() {
// if (byteBuffer instanceof DirectBuffer) {
// return ((DirectBuffer) byteBuffer).isAddressValid();
// }
// assert false : byteBuffer;
// return false;
// }
//
// public void addressValidityCheck() {
// if (byteBuffer instanceof DirectBuffer) {
// ((DirectBuffer) byteBuffer).addressValidityCheck();
// } else {
// assert false : byteBuffer;
// }
// }
//
// public void free() {
// if (byteBuffer instanceof DirectBuffer) {
// ((DirectBuffer) byteBuffer).free();
// } else {
// assert false : byteBuffer;
// }
// }

	@Override
	public LongBuffer asReadOnlyBuffer () {
		LongToByteBufferAdapter buf = new LongToByteBufferAdapter(byteBuffer.asReadOnlyBuffer());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public LongBuffer compact () {
		if (byteBuffer.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		byteBuffer.limit(limit << 3);
		byteBuffer.position(position << 3);
		byteBuffer.compact();
		byteBuffer.clear();
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public LongBuffer duplicate () {
		LongToByteBufferAdapter buf = new LongToByteBufferAdapter(byteBuffer.duplicate());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public long get () {
		if (position == limit) {
			throw new BufferUnderflowException();
		}
		return byteBuffer.getLong(position++ << 3);
	}

	@Override
	public long get (int index) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		return byteBuffer.getLong(index << 3);
	}

	@Override
	public boolean isDirect () {
		return byteBuffer.isDirect();
	}

	@Override
	public boolean isReadOnly () {
		return byteBuffer.isReadOnly();
	}

	@Override
	public ByteOrder order () {
		return byteBuffer.order();
	}

	@Override
	protected long[] protectedArray () {
		throw new UnsupportedOperationException();
	}

	@Override
	protected int protectedArrayOffset () {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean protectedHasArray () {
		return false;
	}

	@Override
	public LongBuffer put (long c) {
		if (position == limit) {
			throw new BufferOverflowException();
		}
		byteBuffer.putLong(position++ << 3, c);
		return this;
	}

	@Override
	public LongBuffer put (int index, long c) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		byteBuffer.putLong(index << 3, c);
		return this;
	}

	@Override
	public LongBuffer slice () {
		byteBuffer.limit(limit << 3);
		byteBuffer.position(position << 3);
		LongBuffer result = new LongToByteBufferAdapter(byteBuffer.slice());
		byteBuffer.clear();
		return result;
	}

}
