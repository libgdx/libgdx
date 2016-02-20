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

package com.badlogic.gdx.backends.jglfw;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class JglfwUtil {
	static private ByteBuffer buffer = ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder());
	static private IntBuffer bufferInt = buffer.asIntBuffer();
	static private FloatBuffer bufferFloat = buffer.asFloatBuffer();

	static private void ensureSize (int n) {
		if (buffer.capacity() < n) {
			buffer = ByteBuffer.allocateDirect(n).order(ByteOrder.nativeOrder());
			bufferInt = buffer.asIntBuffer();
			bufferFloat = buffer.asFloatBuffer();
		}
	}

	static ByteBuffer toBuffer (boolean[] src, int offset) {
		int n = src.length - offset;
		ensureSize(n);
		buffer.position(0);
		for (boolean value : src)
			buffer.put(value ? (byte)1 : 0);
		return buffer;
	}

	static IntBuffer toBuffer (int[] src, int offset) {
		int n = src.length - offset;
		ensureSize(n * 4);
		bufferInt.position(0);
		bufferInt.put(src, offset, n);
		return bufferInt;
	}

	static FloatBuffer toBuffer (float[] src, int offset) {
		int n = src.length - offset;
		ensureSize(n * 4);
		bufferFloat.position(0);
		bufferFloat.put(src, offset, n);
		return bufferFloat;
	}

	static void toArray (ByteBuffer src, boolean[] dst, int offset) {
		for (int i = 0; i < dst.length - offset; i++) {
			dst[i + offset] = src.get(i) != 0;
		}
	}

	static void toArray (IntBuffer src, int[] dst, int offset) {
		src.position(0);
		src.get(dst, offset, dst.length - offset);
	}

	static void toArray (FloatBuffer src, float[] dst, int offset) {
		src.position(0);
		src.get(dst, offset, dst.length - offset);
	}
}
