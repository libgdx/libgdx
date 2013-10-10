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

package com.badlogic.gdx.graphics.glutils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * In IndexBufferObject wraps OpenGL's index buffer functionality to be used in conjunction with VBOs. This class can be
 * seamlessly used with OpenGL ES 1.x and 2.0.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 * 
 * <p>
 * You can also use this to store indices for vertex arrays. Do not call {@link #bind()} or {@link #unbind()} in this case but
 * rather use {@link #getBuffer()} to use the buffer directly with glDrawElements. You must also create the IndexBufferObject with
 * the second constructor and specify isDirect as true as glDrawElements in conjunction with vertex arrays needs direct buffers.
 * </p>
 * 
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * 
 * @author mzechner */
public class IndexBufferObjectSubData implements IndexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

	ShortBuffer buffer;
	ByteBuffer byteBuffer;
	int bufferHandle;
	final boolean isDirect;
	boolean isDirty = true;
	boolean isBound = false;
	final int usage;

	/** Creates a new IndexBufferObject.
	 * 
	 * @param isStatic whether the index buffer is static
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public IndexBufferObjectSubData (boolean isStatic, int maxIndices) {
// if (Gdx.app.getType() == ApplicationType.Android
// && Gdx.app.getVersion() < 5) {
// byteBuffer = ByteBuffer.allocate(maxIndices * 2);
// byteBuffer.order(ByteOrder.nativeOrder());
// isDirect = false;
// } else {
		byteBuffer = BufferUtils.newByteBuffer(maxIndices * 2);
		isDirect = true;
// }
		usage = isStatic ? GL11.GL_STATIC_DRAW : GL11.GL_DYNAMIC_DRAW;
		buffer = byteBuffer.asShortBuffer();
		buffer.flip();
		byteBuffer.flip();
		bufferHandle = createBufferObject();
	}

	/** Creates a new IndexBufferObject to be used with vertex arrays.
	 * 
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public IndexBufferObjectSubData (int maxIndices) {
		byteBuffer = BufferUtils.newByteBuffer(maxIndices * 2);
		this.isDirect = true;

		usage = GL11.GL_STATIC_DRAW;
		buffer = byteBuffer.asShortBuffer();
		buffer.flip();
		byteBuffer.flip();
		bufferHandle = createBufferObject();
	}

	private int createBufferObject () {
		if (Gdx.gl20 != null) {
			Gdx.gl20.glGenBuffers(1, tmpHandle);
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, tmpHandle.get(0));
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
			Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
			return tmpHandle.get(0);
		} else if (Gdx.gl11 != null) {
			Gdx.gl11.glGenBuffers(1, tmpHandle);
			Gdx.gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, tmpHandle.get(0));
			Gdx.gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
			Gdx.gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
			return tmpHandle.get(0);
		}

		return 0;
	}

	/** @return the number of indices currently stored in this buffer */
	public int getNumIndices () {
		return buffer.limit();
	}

	/** @return the maximum number of indices this IndexBufferObject can store. */
	public int getNumMaxIndices () {
		return buffer.capacity();
	}

	/** <p>
	 * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
	 * copied to this IndexBufferObject.
	 * </p>
	 * 
	 * <p>
	 * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
	 * </p>
	 * 
	 * @param indices the vertex data
	 * @param offset the offset to start copying the data from
	 * @param count the number of floats to copy */
	public void setIndices (short[] indices, int offset, int count) {
		isDirty = true;
		buffer.clear();
		buffer.put(indices, offset, count);
		buffer.flip();
		byteBuffer.position(0);
		byteBuffer.limit(count << 1);

		if (isBound) {
			if (Gdx.gl11 != null) {
				GL11 gl = Gdx.gl11;
// gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, byteBuffer
// .limit(), byteBuffer, usage);
				gl.glBufferSubData(GL11.GL_ELEMENT_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
			} else if (Gdx.gl20 != null) {
				GL20 gl = Gdx.gl20;
// gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer
// .limit(), byteBuffer, usage);
				gl.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);

			}
			isDirty = false;
		}
	}

	/** <p>
	 * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
	 * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
	 * </p>
	 * 
	 * @return the underlying short buffer. */
	public ShortBuffer getBuffer () {
		isDirty = true;
		return buffer;
	}

	/** Binds this IndexBufferObject for rendering with glDrawElements. */
	public void bind () {
		if (bufferHandle == 0) throw new GdxRuntimeException("buuh");

		if (Gdx.gl11 != null) {
			GL11 gl = Gdx.gl11;
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
			if (isDirty) {
// gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, byteBuffer
// .limit(), byteBuffer, usage);
				byteBuffer.limit(buffer.limit() * 2);
				gl.glBufferSubData(GL11.GL_ELEMENT_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
				isDirty = false;
			}
		} else {
			GL20 gl = Gdx.gl20;
			gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
			if (isDirty) {
// gl.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, byteBuffer
// .limit(), byteBuffer, usage);
				byteBuffer.limit(buffer.limit() * 2);
				gl.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
				isDirty = false;
			}
		}
		isBound = true;
	}

	/** Unbinds this IndexBufferObject. */
	public void unbind () {
		if (Gdx.gl11 != null) {
			Gdx.gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		} else if (Gdx.gl20 != null) {
			Gdx.gl20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
		}
		isBound = false;
	}

	/** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
		bufferHandle = createBufferObject();
		isDirty = true;
	}

	/** Disposes this IndexBufferObject and all its associated OpenGL resources. */
	public void dispose () {
		if (Gdx.gl20 != null) {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			tmpHandle.flip();
			GL20 gl = Gdx.gl20;
			gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;
		} else if (Gdx.gl11 != null) {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			tmpHandle.flip();
			GL11 gl = Gdx.gl11;
			gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;
		}
	}
}
