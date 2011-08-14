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

/** This class wraps a byte buffer to be a char buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by the adapter. It must NOT be accessed outside the
 * adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter. The adapter extends Buffer, thus has its own position
 * and limit.</li>
 * </ul>
 * </p> */
final class CharToByteBufferAdapter extends CharBuffer { // implements DirectBuffer {

	static CharBuffer wrap (ByteBuffer byteBuffer) {
		return new CharToByteBufferAdapter(byteBuffer.slice());
	}

	private final ByteBuffer byteBuffer;

	CharToByteBufferAdapter (ByteBuffer byteBuffer) {
		super((byteBuffer.capacity() >> 1));
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
	public CharBuffer asReadOnlyBuffer () {
		CharToByteBufferAdapter buf = new CharToByteBufferAdapter(byteBuffer.asReadOnlyBuffer());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public CharBuffer compact () {
		if (byteBuffer.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		byteBuffer.limit(limit << 1);
		byteBuffer.position(position << 1);
		byteBuffer.compact();
		byteBuffer.clear();
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public CharBuffer duplicate () {
		CharToByteBufferAdapter buf = new CharToByteBufferAdapter(byteBuffer.duplicate());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public char get () {
		if (position == limit) {
			throw new BufferUnderflowException();
		}
		return byteBuffer.getChar(position++ << 1);
	}

	@Override
	public char get (int index) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		return byteBuffer.getChar(index << 1);
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
	protected char[] protectedArray () {
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
	public CharBuffer put (char c) {
		if (position == limit) {
			throw new BufferOverflowException();
		}
		byteBuffer.putChar(position++ << 1, c);
		return this;
	}

	@Override
	public CharBuffer put (int index, char c) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		byteBuffer.putChar(index << 1, c);
		return this;
	}

	@Override
	public CharBuffer slice () {
		byteBuffer.limit(limit << 1);
		byteBuffer.position(position << 1);
		CharBuffer result = new CharToByteBufferAdapter(byteBuffer.slice());
		byteBuffer.clear();
		return result;
	}

	@Override
	public CharSequence subSequence (int start, int end) {
		if (start < 0 || end < start || end > remaining()) {
			throw new IndexOutOfBoundsException();
		}

		CharBuffer result = duplicate();
		result.limit(position + end);
		result.position(position + start);
		return result;
	}
}
