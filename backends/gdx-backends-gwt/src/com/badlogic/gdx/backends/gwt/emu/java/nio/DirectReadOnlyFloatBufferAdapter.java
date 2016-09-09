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
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/** This class wraps a byte buffer to be a float buffer.
 * <p>
 * Implementation notice:
 * <ul>
 * <li>After a byte buffer instance is wrapped, it becomes privately owned by the adapter. It must NOT be accessed outside the
 * adapter any more.</li>
 * <li>The byte buffer's position and limit are NOT linked with the adapter. The adapter extends Buffer, thus has its own position
 * and limit.</li>
 * </ul>
 * </p> */
final class DirectReadOnlyFloatBufferAdapter extends FloatBuffer implements HasArrayBufferView {
// implements DirectBuffer {

	static FloatBuffer wrap (DirectByteBuffer byteBuffer) {
		return new DirectReadOnlyFloatBufferAdapter((DirectByteBuffer)byteBuffer.slice());
	}

	private final DirectByteBuffer byteBuffer;
	private final Float32Array floatArray;

	DirectReadOnlyFloatBufferAdapter (DirectByteBuffer byteBuffer) {
		super((byteBuffer.capacity() >> 2));
		this.byteBuffer = byteBuffer;
		this.byteBuffer.clear();
		this.floatArray = TypedArrays.createFloat32Array(byteBuffer.byteArray.buffer(), byteBuffer.byteArray.byteOffset(), capacity);
	}

	@Override
	public FloatBuffer asReadOnlyBuffer () {
		DirectReadOnlyFloatBufferAdapter buf = new DirectReadOnlyFloatBufferAdapter(byteBuffer);
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public FloatBuffer compact () {
		throw new ReadOnlyBufferException();
	}

	@Override
	public FloatBuffer duplicate () {
		DirectReadOnlyFloatBufferAdapter buf = new DirectReadOnlyFloatBufferAdapter((DirectByteBuffer)byteBuffer.duplicate());
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public float get () {
// if (position == limit) {
// throw new BufferUnderflowException();
// }
		return floatArray.get(position++);
	}

	@Override
	public float get (int index) {
// if (index < 0 || index >= limit) {
// throw new IndexOutOfBoundsException();
// }
		return floatArray.get(index);
	}

	@Override
	public boolean isDirect () {
		return true;
	}

	@Override
	public boolean isReadOnly () {
		return true;
	}

	@Override
	public ByteOrder order () {
		return byteBuffer.order();
	}

	@Override
	float[] protectedArray () {
		throw new UnsupportedOperationException();
	}

	@Override
	int protectedArrayOffset () {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean protectedHasArray () {
		return false;
	}

	@Override
	public FloatBuffer put (float c) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public FloatBuffer put (int index, float c) {
		throw new ReadOnlyBufferException();
	}

	@Override
	public FloatBuffer slice () {
		byteBuffer.limit(limit << 2);
		byteBuffer.position(position << 2);
		FloatBuffer result = new DirectReadOnlyFloatBufferAdapter((DirectByteBuffer)byteBuffer.slice());
		byteBuffer.clear();
		return result;
	}

	public ArrayBufferView getTypedArray () {
		return floatArray;
	}

	public int getElementSize () {
		return 4;
	}
}
