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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Modification of the {@link VertexBufferObjectSubData} class.
 * Sets the glVertexAttribDivisor for every {@link VertexAttribute} automatically.
 *
 * @author mrdlink
 */
public class InstanceBufferObjectSubData implements InstanceData {

	final VertexAttributes attributes;
	final FloatBuffer buffer;
	final ByteBuffer byteBuffer;
	int bufferHandle;
	final boolean isDirect;
	final boolean isStatic;
	final int usage;
	boolean isDirty = false;
	boolean isBound = false;

	/**
	 * Constructs a new interleaved InstanceBufferObject.
	 *
	 * @param isStatic           whether the vertex data is static.
	 * @param numInstances       the maximum number of vertices
	 * @param instanceAttributes the {@link VertexAttributes}.
	 */
	public InstanceBufferObjectSubData (boolean isStatic, int numInstances, VertexAttribute... instanceAttributes) {
		this(isStatic, numInstances, new VertexAttributes(instanceAttributes));
	}

	/**
	 * Constructs a new interleaved InstanceBufferObject.
	 *
	 * @param isStatic           whether the vertex data is static.
	 * @param numInstances       the maximum number of vertices
	 * @param instanceAttributes the {@link VertexAttribute}s.
	 */
	public InstanceBufferObjectSubData (boolean isStatic, int numInstances, VertexAttributes instanceAttributes) {
		this.isStatic = isStatic;
		this.attributes = instanceAttributes;
		byteBuffer = BufferUtils.newByteBuffer(this.attributes.vertexSize * numInstances);
		isDirect = true;

		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
		buffer = byteBuffer.asFloatBuffer();
		bufferHandle = createBufferObject();
		buffer.flip();
		byteBuffer.flip();
	}

	private int createBufferObject () {
		int result = Gdx.gl20.glGenBuffer();
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, result);
		Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
		Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		return result;
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}

	/**
	 * Effectively returns {@link #getNumInstances()}.
	 *
	 * @return number of instances in this buffer
	 */
	@Override
	public int getNumInstances () {
		return buffer.limit() * 4 / attributes.vertexSize;
	}

	/**
	 * Effectively returns {@link #getNumMaxInstances()}.
	 *
	 * @return maximum number of instances in this buffer
	 */
	@Override
	public int getNumMaxInstances () {
		return byteBuffer.capacity() / attributes.vertexSize;
	}

	@Override
	public FloatBuffer getBuffer () {
		isDirty = true;
		return buffer;
	}

	private void bufferChanged () {
		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), null, usage);
			Gdx.gl20.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
			isDirty = false;
		}
	}

	@Override
	public void setInstanceData (float[] data, int offset, int count) {
		isDirty = true;
		if (isDirect) {
			BufferUtils.copy(data, byteBuffer, count, offset);
			buffer.position(0);
			buffer.limit(count);
		} else {
			buffer.clear();
			buffer.put(data, offset, count);
			buffer.flip();
			byteBuffer.position(0);
			byteBuffer.limit(buffer.limit() << 2);
		}

		bufferChanged();
	}

	@Override
	public void setInstanceData (FloatBuffer data, int count) {
		isDirty = true;
		if (isDirect) {
			BufferUtils.copy(data, byteBuffer, count);
			buffer.position(0);
			buffer.limit(count);
		} else {
			buffer.clear();
			buffer.put(data);
			buffer.flip();
			byteBuffer.position(0);
			byteBuffer.limit(buffer.limit() << 2);
		}

		bufferChanged();
	}

	@Override
	public void updateInstanceData (int targetOffset, float[] data, int sourceOffset, int count) {
		isDirty = true;
		if (isDirect) {
			final int pos = byteBuffer.position();
			byteBuffer.position(targetOffset * 4);
			BufferUtils.copy(data, sourceOffset, count, byteBuffer);
			byteBuffer.position(pos);
		} else
			throw new GdxRuntimeException("Buffer must be allocated direct."); // Should never happen

		bufferChanged();
	}

	@Override
	public void updateInstanceData (int targetOffset, FloatBuffer data, int sourceOffset, int count) {
		isDirty = true;
		if (isDirect) {
			final int pos = byteBuffer.position();
			byteBuffer.position(targetOffset * 4);
			data.position(sourceOffset * 4);
			BufferUtils.copy(data, byteBuffer, count);
			byteBuffer.position(pos);
		} else
			throw new GdxRuntimeException("Buffer must be allocated direct."); // Should never happen

		bufferChanged();
	}

	/**
	 * Binds this InstanceBufferObject for rendering via glDrawArraysInstanced or glDrawElementsInstanced
	 *
	 * @param shader the shader
	 */
	@Override
	public void bind (final ShaderProgram shader) {
		bind(shader, null);
	}

	@Override
	public void bind (final ShaderProgram shader, final int[] locations) {
		final GL20 gl = Gdx.gl20;

		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
		if (isDirty) {
			byteBuffer.limit(buffer.limit() * 4);
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}

		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0)
					continue;
				int unitOffset = +attribute.unit;
				shader.enableVertexAttribute(location + unitOffset);

				shader.setVertexAttribute(location + unitOffset, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
				Gdx.gl30.glVertexAttribDivisor(location + unitOffset, 1);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0)
					continue;
				int unitOffset = +attribute.unit;
				shader.enableVertexAttribute(location + unitOffset);

				shader.setVertexAttribute(location + unitOffset, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize, attribute.offset);
				Gdx.gl30.glVertexAttribDivisor(location + unitOffset, 1);
			}
		}
		isBound = true;
	}

	/**
	 * Unbinds this InstanceBufferObject.
	 *
	 * @param shader the shader
	 */
	@Override
	public void unbind (final ShaderProgram shader) {
		unbind(shader, null);
	}

	@Override
	public void unbind (final ShaderProgram shader, final int[] locations) {
		final GL20 gl = Gdx.gl20;
		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0)
					continue;
				int unitOffset = +attribute.unit;
				shader.disableVertexAttribute(location + unitOffset);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0)
					continue;
				int unitOffset = +attribute.unit;
				shader.enableVertexAttribute(location + unitOffset);
			}
		}
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/**
	 * Invalidates the InstanceBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
	 */
	public void invalidate () {
		bufferHandle = createBufferObject();
		isDirty = true;
	}

	/**
	 * Disposes of all resources this InstanceBufferObject uses.
	 */
	@Override
	public void dispose () {
		GL20 gl = Gdx.gl20;
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		gl.glDeleteBuffer(bufferHandle);
		bufferHandle = 0;
	}

	/**
	 * Returns the InstanceBufferObject handle
	 *
	 * @return the InstanceBufferObject handle
	 */
	public int getBufferHandle () {
		return bufferHandle;
	}
}
