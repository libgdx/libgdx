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

import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/** This class wraps a byte buffer to be a int buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by the adapter. It must NOT be accessed outside the
 * adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter. The adapter extends Buffer, thus has its own position
 * and limit.</li>
 * </ul>
 * </p> */
final class DirectReadWriteIntBufferAdapter extends IntBuffer implements HasArrayBufferView {

	static IntBuffer wrap (DirectReadWriteByteBuffer byteBuffer) {
		return new DirectReadWriteIntBufferAdapter((DirectReadWriteByteBuffer)byteBuffer.slice());
	}

	private final DirectReadWriteByteBuffer byteBuffer;
	private final Int32Array intArray;

	DirectReadWriteIntBufferAdapter (DirectReadWriteByteBuffer byteBuffer) {
		super((byteBuffer.capacity() >> 2));
		this.byteBuffer = byteBuffer;
		this.byteBuffer.clear();
		this.intArray = TypedArrays.createInt32Array(byteBuffer.byteArray.buffer(), byteBuffer.byteArray.byteOffset(), capacity);
	}

	// TODO(haustein) This will be slow
	@Override
	public IntBuffer asReadOnlyBuffer () {
		DirectReadOnlyIntBufferAdapter buf = new DirectReadOnlyIntBufferAdapter(byteBuffer);
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public IntBuffer compact () {
		byteBuffer.limit(limit << 2);
		byteBuffer.position(position << 2);
		byteBuffer.compact();
		byteBuffer.clear();
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public IntBuffer duplicate () {
		DirectReadWriteIntBufferAdapter buf = new DirectReadWriteIntBufferAdapter((DirectReadWriteByteBuffer)byteBuffer.duplicate());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public int get () {
// if (position == limit) {
// throw new BufferUnderflowException();
// }
		return intArray.get(position++);
	}

	@Override
	public int get (int index) {
// if (index < 0 || index >= limit) {
// throw new IndexOutOfBoundsException();
// }
		return intArray.get(index);
	}

	@Override
	public boolean isDirect () {
		return true;
	}

	@Override
	public boolean isReadOnly () {
		return false;
	}

	@Override
	public ByteOrder order () {
		return byteBuffer.order();
	}

	@Override
	protected int[] protectedArray () {
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
	public IntBuffer put (int c) {
// if (position == limit) {
// throw new BufferOverflowException();
// }
		intArray.set(position++, c);
		return this;
	}

	@Override
	public IntBuffer put (int index, int c) {
// if (index < 0 || index >= limit) {
// throw new IndexOutOfBoundsException();
// }
		intArray.set(index, c);
		return this;
	}

	@Override
	public IntBuffer slice () {
		byteBuffer.limit(limit << 2);
		byteBuffer.position(position << 2);
		IntBuffer result = new DirectReadWriteIntBufferAdapter((DirectReadWriteByteBuffer)byteBuffer.slice());
		byteBuffer.clear();
		return result;
	}

	public ArrayBufferView getTypedArray () {
		return intArray;
	}

	public int getElementSize () {
		return 4;
	}
}
