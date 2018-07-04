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

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;

/** Class with static helper methods to increase the speed of array/direct buffer and direct buffer/direct buffer transfers
 * 
 * @author mzechner, xoppa */
public final class BufferUtils {
	static Array<ByteBuffer> unsafeBuffers = new Array<ByteBuffer>();
	static int allocatedUnsafe = 0;

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
		if (dst instanceof ByteBuffer)
			dst.limit(numFloats << 2);
		else if (dst instanceof FloatBuffer) dst.limit(numFloats);

		copyJni(src, dst, numFloats, offset);
		dst.position(0);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (byte[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (short[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (char[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (int[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (long[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (float[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position and limit will stay
	 * the same. <b>The Buffer must be a direct Buffer with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param numElements the number of elements to copy.
	 * @param dst the destination Buffer, its position is used as an offset. */
	public static void copy (double[] src, int srcOffset, int numElements, Buffer dst) {
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (char[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 1));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 1);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (int[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (long[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (float[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 2));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 2);
	}

	/** Copies the contents of src to dst, starting from src[srcOffset], copying numElements elements. The {@link Buffer}
	 * instance's {@link Buffer#position()} is used to define the offset into the Buffer itself. The position will stay the same,
	 * the limit will be set to position + numElements. <b>The Buffer must be a direct Buffer with native byte order. No error
	 * checking is performed</b>.
	 * 
	 * @param src the source array.
	 * @param srcOffset the offset into the source array.
	 * @param dst the destination Buffer, its position is used as an offset.
	 * @param numElements the number of elements to copy. */
	public static void copy (double[] src, int srcOffset, Buffer dst, int numElements) {
		dst.limit(dst.position() + bytesToElements(dst, numElements << 3));
		copyJni(src, srcOffset, dst, positionInBytes(dst), numElements << 3);
	}

	/** Copies the contents of src to dst, starting from the current position of src, copying numElements elements (using the data
	 * type of src, no matter the datatype of dst). The dst {@link Buffer#position()} is used as the writing offset. The position
	 * of both Buffers will stay the same. The limit of the src Buffer will stay the same. The limit of the dst Buffer will be set
	 * to dst.position() + numElements, where numElements are translated to the number of elements appropriate for the dst Buffer
	 * data type. <b>The Buffers must be direct Buffers with native byte order. No error checking is performed</b>.
	 * 
	 * @param src the source Buffer.
	 * @param dst the destination Buffer.
	 * @param numElements the number of elements to copy. */
	public static void copy (Buffer src, Buffer dst, int numElements) {
		int numBytes = elementsToBytes(src, numElements);
		dst.limit(dst.position() + bytesToElements(dst, numBytes));
		copyJni(src, positionInBytes(src), dst, positionInBytes(dst), numBytes);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
		switch (dimensions) {
		case 4:
			transformV4M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 3:
			transformV3M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 2:
			transformV2M4Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components of the vector (2 for xy, 3 for xyz or 4 for xyzw)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix4 matrix, int offset) {
		switch (dimensions) {
		case 4:
			transformV4M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 3:
			transformV3M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 2:
			transformV2M4Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The {@link Buffer#position()} is used as the
	 * offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix) {
		transform(data, dimensions, strideInBytes, count, matrix, 0);
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with,
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (Buffer data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
		switch (dimensions) {
		case 3:
			transformV3M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		case 2:
			transformV2M3Jni(data, strideInBytes, count, matrix.val, positionInBytes(data) + offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	/** Multiply float vector components within the buffer with the specified matrix. The specified offset value is added to the
	 * {@link Buffer#position()} and used as the offset.
	 * @param data The buffer to transform.
	 * @param dimensions The number of components (x, y, z) of the vector (2 for xy or 3 for xyz)
	 * @param strideInBytes The offset between the first and the second vector to transform
	 * @param count The number of vectors to transform
	 * @param matrix The matrix to multiply the vector with,
	 * @param offset The offset within the buffer (in bytes relative to the current position) to the vector */
	public static void transform (float[] data, int dimensions, int strideInBytes, int count, Matrix3 matrix, int offset) {
		switch (dimensions) {
		case 3:
			transformV3M3Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		case 2:
			transformV2M3Jni(data, strideInBytes, count, matrix.val, offset);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	public static long findFloats (Buffer vertex, int strideInBytes, Buffer vertices, int numVertices) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices);
	}

	public static long findFloats (float[] vertex, int strideInBytes, Buffer vertices, int numVertices) {
		return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, float[] vertices, int numVertices) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices);
	}

	public static long findFloats (float[] vertex, int strideInBytes, float[] vertices, int numVertices) {
		return find(vertex, 0, strideInBytes, vertices, 0, numVertices);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
	}

	public static long findFloats (float[] vertex, int strideInBytes, Buffer vertices, int numVertices, float epsilon) {
		return find(vertex, 0, strideInBytes, vertices, positionInBytes(vertices), numVertices, epsilon);
	}

	public static long findFloats (Buffer vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
		return find(vertex, positionInBytes(vertex), strideInBytes, vertices, 0, numVertices, epsilon);
	}

	public static long findFloats (float[] vertex, int strideInBytes, float[] vertices, int numVertices, float epsilon) {
		return find(vertex, 0, strideInBytes, vertices, 0, numVertices, epsilon);
	}

	private static int positionInBytes (Buffer dst) {
		if (dst instanceof ByteBuffer)
			return dst.position();
		else if (dst instanceof ShortBuffer)
			return dst.position() << 1;
		else if (dst instanceof CharBuffer)
			return dst.position() << 1;
		else if (dst instanceof IntBuffer)
			return dst.position() << 2;
		else if (dst instanceof LongBuffer)
			return dst.position() << 3;
		else if (dst instanceof FloatBuffer)
			return dst.position() << 2;
		else if (dst instanceof DoubleBuffer)
			return dst.position() << 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	private static int bytesToElements (Buffer dst, int bytes) {
		if (dst instanceof ByteBuffer)
			return bytes;
		else if (dst instanceof ShortBuffer)
			return bytes >>> 1;
		else if (dst instanceof CharBuffer)
			return bytes >>> 1;
		else if (dst instanceof IntBuffer)
			return bytes >>> 2;
		else if (dst instanceof LongBuffer)
			return bytes >>> 3;
		else if (dst instanceof FloatBuffer)
			return bytes >>> 2;
		else if (dst instanceof DoubleBuffer)
			return bytes >>> 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	private static int elementsToBytes (Buffer dst, int elements) {
		if (dst instanceof ByteBuffer)
			return elements;
		else if (dst instanceof ShortBuffer)
			return elements << 1;
		else if (dst instanceof CharBuffer)
			return elements << 1;
		else if (dst instanceof IntBuffer)
			return elements << 2;
		else if (dst instanceof LongBuffer)
			return elements << 3;
		else if (dst instanceof FloatBuffer)
			return elements << 2;
		else if (dst instanceof DoubleBuffer)
			return elements << 3;
		else
			throw new GdxRuntimeException("Can't copy to a " + dst.getClass().getName() + " instance");
	}

	public static FloatBuffer newFloatBuffer (int numFloats) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numFloats * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asFloatBuffer();
	}

	public static DoubleBuffer newDoubleBuffer (int numDoubles) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numDoubles * 8);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asDoubleBuffer();
	}

	public static ByteBuffer newByteBuffer (int numBytes) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numBytes);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	public static ShortBuffer newShortBuffer (int numShorts) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numShorts * 2);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asShortBuffer();
	}

	public static CharBuffer newCharBuffer (int numChars) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numChars * 2);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asCharBuffer();
	}

	public static IntBuffer newIntBuffer (int numInts) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numInts * 4);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asIntBuffer();
	}

	public static LongBuffer newLongBuffer (int numLongs) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(numLongs * 8);
		buffer.order(ByteOrder.nativeOrder());
		return buffer.asLongBuffer();
	}

	public static void disposeUnsafeByteBuffer (ByteBuffer buffer) {
		int size = buffer.capacity();
		synchronized (unsafeBuffers) {
			if (!unsafeBuffers.removeValue(buffer, true))
				throw new IllegalArgumentException("buffer not allocated with newUnsafeByteBuffer or already disposed");
		}
		allocatedUnsafe -= size;
		freeMemory(buffer);
	}

	public static boolean isUnsafeByteBuffer (ByteBuffer buffer) {
		synchronized (unsafeBuffers) {
			return unsafeBuffers.contains(buffer, true);
		}
	}

	/** Allocates a new direct ByteBuffer from native heap memory using the native byte order. Needs to be disposed with
	 * {@link #disposeUnsafeByteBuffer(ByteBuffer)}. */
	public static ByteBuffer newUnsafeByteBuffer (int numBytes) {
		ByteBuffer buffer = newDisposableByteBuffer(numBytes);
		buffer.order(ByteOrder.nativeOrder());
		allocatedUnsafe += numBytes;
		synchronized (unsafeBuffers) {
			unsafeBuffers.add(buffer);
		}
		return buffer;
	}

	/** Returns the address of the Buffer, it assumes it is an unsafe buffer.
	 * @param buffer The Buffer to ask the address for.
	 * @return the address of the Buffer. */
	public static long getUnsafeBufferAddress (Buffer buffer) {
		return getBufferAddress(buffer) + buffer.position();
	}

	/** Registers the given ByteBuffer as an unsafe ByteBuffer. The ByteBuffer must have been allocated in native code, pointing to
	 * a memory region allocated via malloc. Needs to be disposed with {@link #disposeUnsafeByteBuffer(ByteBuffer)}.
	 * @param buffer the {@link ByteBuffer} to register
	 * @return the ByteBuffer passed to the method */
	public static ByteBuffer newUnsafeByteBuffer (ByteBuffer buffer) {
		allocatedUnsafe += buffer.capacity();
		synchronized (unsafeBuffers) {
			unsafeBuffers.add(buffer);
		}
		return buffer;
	}

	/** @return the number of bytes allocated with {@link #newUnsafeByteBuffer(int)} */
	public static int getAllocatedBytesUnsafe () {
		return allocatedUnsafe;
	}

	// @off
	/*JNI 
	#include <stdio.h>
	#include <stdlib.h>
	#include <string.h>
	*/

	/** Frees the memory allocated for the ByteBuffer, which MUST have been allocated via {@link #newUnsafeByteBuffer(ByteBuffer)}
	 * or in native code. */
	private static native void freeMemory (ByteBuffer buffer); /*
		free(buffer);
	 */
	
	private static native ByteBuffer newDisposableByteBuffer (int numBytes); /*
		return env->NewDirectByteBuffer((char*)malloc(numBytes), numBytes);
	*/
	
	private static native long getBufferAddress (Buffer buffer); /*
	    return (jlong) buffer;
	*/
	
	/** Writes the specified number of zeros to the buffer. This is generally faster than reallocating a new buffer. */
	public static native void clear (ByteBuffer buffer, int numBytes); /*
		memset(buffer, 0, numBytes);
	*/
	
	private native static void copyJni (float[] src, Buffer dst, int numFloats, int offset); /*
		memcpy(dst, src + offset, numFloats << 2 );
	*/

	private native static void copyJni (byte[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

	private native static void copyJni (char[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

	private native static void copyJni (short[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	 */

	private native static void copyJni (int[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/
	
	private native static void copyJni (long[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

	private native static void copyJni (float[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/
	
	private native static void copyJni (double[] src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/

	private native static void copyJni (Buffer src, int srcOffset, Buffer dst, int dstOffset, int numBytes); /*
		memcpy(dst + dstOffset, src + srcOffset, numBytes);
	*/
	
	/*JNI
	template<size_t n1, size_t n2> void transform(float * const &src, float * const &m, float * const &dst) {}
	
	template<> inline void transform<4, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2], w = src[3];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + w * m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + w * m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + w * m[14];
		dst[3] = x * m[ 3] + y * m[ 7] + z * m[11] + w * m[15]; 
	}
	
	template<> inline void transform<3, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[ 0] + y * m[ 4] + z * m[ 8] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + z * m[ 9] + m[13];
		dst[2] = x * m[ 2] + y * m[ 6] + z * m[10] + m[14]; 
	}
	
	template<> inline void transform<2, 4>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[ 0] + y * m[ 4] + m[12]; 
		dst[1] = x * m[ 1] + y * m[ 5] + m[13]; 
	}
	
	template<> inline void transform<3, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1], z = src[2];
		dst[0] = x * m[0] + y * m[3] + z * m[6]; 
		dst[1] = x * m[1] + y * m[4] + z * m[7];
		dst[2] = x * m[2] + y * m[5] + z * m[8]; 
	}
	
	template<> inline void transform<2, 3>(float * const &src, float * const &m, float * const &dst) {
		const float x = src[0], y = src[1];
		dst[0] = x * m[0] + y * m[3] + m[6]; 
		dst[1] = x * m[1] + y * m[4] + m[7]; 
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	template<size_t n1, size_t n2> void transform(float * const &v, int const &stride, unsigned short * const &indices, int const &count, float * const &m, int offset) {
		for (int i = 0; i < count; i++) {
			transform<n1, n2>(&v[offset], m, &v[offset]);
			offset += stride;
		}
	}
	
	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size, const float &epsilon) {
   	for (unsigned int i = 0; i < size; i++)
   		if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && ((lhs[i] > rhs[i] ? lhs[i] - rhs[i] : rhs[i] - lhs[i]) > epsilon))
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count, const float &epsilon) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size, epsilon))
				return (long)i;
		return -1;
	}

	inline bool compare(float * const &lhs, float * const & rhs, const unsigned int &size) {
   	for (unsigned int i = 0; i < size; i++)
      	if ((*(unsigned int*)&lhs[i] != *(unsigned int*)&rhs[i]) && lhs[i] != rhs[i])
         	return false;
		return true;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, const unsigned int &count) {
		for (unsigned int i = 0; i < count; i++)
			if (compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}

	inline unsigned int calcHash(float * const &vertex, const unsigned int &size) {
		unsigned int result = 0;
		for (unsigned int i = 0; i < size; ++i)
			result += ((*((unsigned int *)&vertex[i])) & 0xffffff80) >> (i & 0x7);
		return result & 0x7fffffff;
	}
	
	long find(float * const &vertex, const unsigned int &size, float * const &vertices, unsigned int * const &hashes, const unsigned int &count) {
		const unsigned int hash = calcHash(vertex, size);
		for (unsigned int i = 0; i < count; i++)
			if (hashes[i] == hash && compare(&vertices[i*size], vertex, size))
				return (long)i;
		return -1;
	}
	*/
	
	private native static void transformV4M4Jni (Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<4, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);  
	*/
	
	private native static void transformV4M4Jni (float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<4, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);  
	*/
	
	private native static void transformV3M4Jni (Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV3M4Jni (float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV2M4Jni (Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV2M4Jni (float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 4>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/

	private native static void transformV3M3Jni (Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV3M3Jni (float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<3, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV2M3Jni (Buffer data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static void transformV2M3Jni (float[] data, int strideInBytes, int count, float[] matrix, int offsetInBytes); /*
		transform<2, 3>((float*)data, strideInBytes / 4, count, (float*)matrix, offsetInBytes / 4);
	*/
	
	private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/
	
	private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/
	
	private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/
	
	private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices);
	*/
	
	private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/
	
	private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, Buffer vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/
	
	private native static long find(Buffer vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/
	
	private native static long find(float[] vertex, int vertexOffsetInBytes, int strideInBytes, float[] vertices, int verticesOffsetInBytes, int numVertices, float epsilon); /*
		return find((float *)&vertex[vertexOffsetInBytes / 4], (unsigned int)(strideInBytes / 4), (float*)&vertices[verticesOffsetInBytes / 4], (unsigned int)numVertices, epsilon);
	*/
}
