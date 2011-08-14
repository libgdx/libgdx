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

/** DoubleArrayBuffer, ReadWriteDoubleArrayBuffer and ReadOnlyDoubleArrayBuffer compose the implementation of array based double
 * buffers.
 * <p>
 * ReadOnlyDoubleArrayBuffer extends DoubleArrayBuffer with all the write methods throwing read only exception.
 * </p>
 * <p>
 * This class is marked final for runtime performance.
 * </p> */
final class ReadOnlyDoubleArrayBuffer extends DoubleArrayBuffer {

	static ReadOnlyDoubleArrayBuffer copy (DoubleArrayBuffer other, int markOfOther) {
		ReadOnlyDoubleArrayBuffer buf = new ReadOnlyDoubleArrayBuffer(other.capacity(), other.backingArray, other.offset);
		buf.limit = other.limit();
		buf.position = other.position();
		buf.mark = markOfOther;
		return buf;
	}

	ReadOnlyDoubleArrayBuffer (int capacity, double[] backingArray, int arrayOffset) {
		super(capacity, backingArray, arrayOffset);
	}

	public DoubleBuffer asReadOnlyBuffer () {
		return duplicate();
	}

	public DoubleBuffer compact () {
		throw new ReadOnlyBufferException();
	}

	public DoubleBuffer duplicate () {
		return copy(this, mark);
	}

	public boolean isReadOnly () {
		return true;
	}

	protected double[] protectedArray () {
		throw new ReadOnlyBufferException();
	}

	protected int protectedArrayOffset () {
		throw new ReadOnlyBufferException();
	}

	protected boolean protectedHasArray () {
		return false;
	}

	public DoubleBuffer put (double c) {
		throw new ReadOnlyBufferException();
	}

	public DoubleBuffer put (int index, double c) {
		throw new ReadOnlyBufferException();
	}

	public final DoubleBuffer put (double[] src, int off, int len) {
		throw new ReadOnlyBufferException();
	}

	public final DoubleBuffer put (DoubleBuffer buf) {
		throw new ReadOnlyBufferException();
	}

	public DoubleBuffer slice () {
		return new ReadOnlyDoubleArrayBuffer(remaining(), backingArray, offset + position);
	}

}
