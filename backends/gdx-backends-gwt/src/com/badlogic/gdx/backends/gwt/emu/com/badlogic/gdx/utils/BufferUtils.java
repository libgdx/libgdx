/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/** Class with static helper methods to increase the speed of array/direct buffer and direct buffer/direct buffer transfers
 * 
 * @author mzechner */
public class BufferUtils {
	/** Copies numFloats floats from src starting at offset to dst. Dst is assumed to be a direct {@link Buffer}. The method will
	 * crash if that is not the case. The position and limit of the buffer are ignored, the copy is placed at position 0 in the
	 * buffer. After the copying process the position of the buffer is set to 0 and its limit is set to numFloats * 4 if it is a
	 * ByteBuffer and numFloats if it is a FloatBuffer. In case the Buffer is neither a ByteBuffer nor a FloatBuffer the limit is
	 * not set. This is an expert method, use at your own risk.
	 * 
	 * @param src the source array
	 * @param dst the destination buffer, has to be a direct Buffer
	 * @param numFloats the number of floats to copy
	 * @param offset the offset in src to start copying from */
	public static void copy (float[] src, Buffer dst, int numFloats, int offset) {
		FloatBuffer floatBuffer = null;
		if(dst instanceof ByteBuffer) {
			floatBuffer = ((ByteBuffer)dst).asFloatBuffer();
		} else 
		if(dst instanceof FloatBuffer) {
			floatBuffer = (FloatBuffer)dst;
		} else {
			throw new GdxRuntimeException("dst must be a ByteBuffer or FloatBuffer");
		}
		
		floatBuffer.clear();
		dst.position(0);
		floatBuffer.put(src, offset, numFloats);
		dst.position(0);
		if (dst instanceof ByteBuffer) dst.limit(numFloats << 2);
		else dst.limit(numFloats);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (byte[] src, int srcOffset, Buffer dst, int numElements) {
		if(!(dst instanceof ByteBuffer)) throw new GdxRuntimeException("dst must be a ByteBuffer");
		
		ByteBuffer byteBuffer = (ByteBuffer)dst;
		int oldPosition = byteBuffer.position();
		byteBuffer.put(src, srcOffset, numElements);
		byteBuffer.position(oldPosition);
		byteBuffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (short[] src, int srcOffset, Buffer dst, int numElements) {
		ShortBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asShortBuffer();
		else if(dst instanceof ShortBuffer) buffer = (ShortBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or ShortBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (char[] src, int srcOffset, Buffer dst, int numElements) {
		CharBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asCharBuffer();
		else if(dst instanceof CharBuffer) buffer = (CharBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or CharBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (int[] src, int srcOffset, Buffer dst, int numElements) {
		IntBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asIntBuffer();
		else if(dst instanceof IntBuffer) buffer = (IntBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or IntBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (long[] src, int srcOffset, Buffer dst, int numElements) {
		LongBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asLongBuffer();
		else if(dst instanceof LongBuffer) buffer = (LongBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or LongBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (float[] src, int srcOffset, Buffer dst, int numElements) {
		FloatBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asFloatBuffer();
		else if(dst instanceof FloatBuffer) buffer = (FloatBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or FloatBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer} instance's
	 * {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same, the limit
	 * will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error checking is
	 * performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (double[] src, int srcOffset, Buffer dst, int numElements) {
		DoubleBuffer buffer = null;
		if(dst instanceof ByteBuffer) buffer = ((ByteBuffer)dst).asDoubleBuffer();
		else if(dst instanceof DoubleBuffer) buffer = (DoubleBuffer)dst;
		if(buffer == null) throw new GdxRuntimeException("dst must be a ByteBuffer or DoubleBuffer");
		
		int oldPosition = buffer.position();
		buffer.put(src, srcOffset, numElements);
		buffer.position(oldPosition);
		buffer.limit(oldPosition + numElements);
	}

//	/** Copies the contents of src to dst, starting from the current position of src, copying numElements elements (using the data
//	 * type of src, no matter the datatype of dst). The dst {@link Buffer#position()} is used as the writing offset. The position
//	 * of both Buffers will stay the same. The limit of the src Buffer will stay the same. The limit of the dst Buffer will be set
//	 * to dst.position() + numElements, where numElements are translated to the number of elements appropriate for the dst Buffer
//	 * data type. <b>The Buffers must be direct Buffers with native byte order. No error checking is performed</b>.
//	 * 
//	 * @param src the source Buffer.
//	 * @param dst the destination Buffer.
//	 * @param numElements the number of elements to copy. */
//	public static void copy (Buffer src, Buffer dst, int numElements) {
//		int numBytes = elementsToBytes(src, numElements);
//		copyJni(src, positionInBytes(src), dst, positionInBytes(dst), numBytes);
//		dst.limit(dst.position() + bytesToElements(dst, numBytes));
//	}

	public static FloatBuffer newFloatBuffer (int numFloats) {
		return FloatBuffer.wrap(new float[numFloats]);
	}

	public static DoubleBuffer newDoubleBuffer (int numDoubles) {
		return DoubleBuffer.wrap(new double[numDoubles]);
	}

	public static ByteBuffer newByteBuffer (int numBytes) {
		return ByteBuffer.wrap(new byte[numBytes]);
	}

	public static ShortBuffer newShortBuffer (int numShorts) {
		return ShortBuffer.wrap(new short[numShorts]);
	}

	public static CharBuffer newCharBuffer (int numChars) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	public static IntBuffer newIntBuffer (int numInts) {
		return IntBuffer.wrap(new int[numInts]);
	}

	public static LongBuffer newLongBuffer (int numLongs) {
		return LongBuffer.wrap(new long[numLongs]);
	}
}
