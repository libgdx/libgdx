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
 * ReadOnlyIntArrayBuffer extends IntArrayBuffer with all the write methods throwing read only exception.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p> */
final class ReadOnlyIntArrayBuffer extends IntArrayBuffer {

	static ReadOnlyIntArrayBuffer copy (IntArrayBuffer other, int markOfOther) {
		ReadOnlyIntArrayBuffer buf = new ReadOnlyIntArrayBuffer(other.capacity(), other.backingArray, other.offset);
		buf.limit = other.limit();
		buf.position = other.position();
		buf.mark = markOfOther;
		return buf;
	}

	ReadOnlyIntArrayBuffer (int capacity, int[] backingArray, int arrayOffset) {
		super(capacity, backingArray, arrayOffset);
	}

	public IntBuffer asReadOnlyBuffer () {
		return duplicate();
	}

	public IntBuffer compact () {
		throw new ReadOnlyBufferException();
	}

	public IntBuffer duplicate () {
		return copy(this, mark);
	}

	public boolean isReadOnly () {
		return true;
	}

	protected int[] protectedArray () {
		throw new ReadOnlyBufferException();
	}

	protected int protectedArrayOffset () {
		throw new ReadOnlyBufferException();
	}

	protected boolean protectedHasArray () {
		return false;
	}

	public IntBuffer put (int c) {
		throw new ReadOnlyBufferException();
	}

	public IntBuffer put (int index, int c) {
		throw new ReadOnlyBufferException();
	}

	public IntBuffer put (IntBuffer buf) {
		throw new ReadOnlyBufferException();
	}

	public final IntBuffer put (int[] src, int off, int len) {
		throw new ReadOnlyBufferException();
	}

	public IntBuffer slice () {
		return new ReadOnlyIntArrayBuffer(remaining(), backingArray, offset + position);
	}

}
