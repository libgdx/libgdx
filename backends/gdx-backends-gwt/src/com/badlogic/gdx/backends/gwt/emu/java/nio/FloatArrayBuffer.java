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

/** FloatArrayBuffer, ReadWriteFloatArrayBuffer and ReadOnlyFloatArrayBuffer compose the implementation of array based float
 * buffers.
 * <p>
 * FloatArrayBuffer implements all the shared readonly methods and is extended by the other two classes.
 * </p>
 * <p>
 * All methods are marked final for runtime performance.
 * </p> */
abstract class FloatArrayBuffer extends FloatBuffer {

	protected final float[] backingArray;

	protected final int offset;

	FloatArrayBuffer (float[] array) {
		this(array.length, array, 0);
	}

	FloatArrayBuffer (int capacity) {
		this(capacity, new float[capacity], 0);
	}

	FloatArrayBuffer (int capacity, float[] backingArray, int offset) {
		super(capacity);
		this.backingArray = backingArray;
		this.offset = offset;
	}

	public final float get () {
		if (position == limit) {
			throw new BufferUnderflowException();
		}
		return backingArray[offset + position++];
	}

	public final float get (int index) {
		if (index < 0 || index >= limit) {
			throw new IndexOutOfBoundsException();
		}
		return backingArray[offset + index];
	}

	public final FloatBuffer get (float[] dest, int off, int len) {
		int length = dest.length;
		if (off < 0 || len < 0 || (long)off + (long)len > length) {
			throw new IndexOutOfBoundsException();
		}
		if (len > remaining()) {
			throw new BufferUnderflowException();
		}
		System.arraycopy(backingArray, offset + position, dest, off, len);
		position += len;
		return this;
	}

	public final boolean isDirect () {
		return false;
	}

	public final ByteOrder order () {
		return ByteOrder.nativeOrder();
	}

}
