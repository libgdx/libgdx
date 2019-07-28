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
 * Modification of the {@link VertexBufferObject} class.
 * Sets the glVertexAttribDivisor for every {@link VertexAttribute} automatically.
 *
 * @author mrdlink
 */
public class InstanceBufferObject implements InstanceData {

	private VertexAttributes attributes;
	private FloatBuffer buffer;
	private ByteBuffer byteBuffer;
	private boolean ownsBuffer;
	private int bufferHandle;
	private int usage;
	boolean isDirty = false;
	boolean isBound = false;

	public InstanceBufferObject (boolean isStatic, int numVertices, VertexAttribute... attributes) {
		this(isStatic, numVertices, new VertexAttributes(attributes));
	}

	public InstanceBufferObject (boolean isStatic, int numVertices, VertexAttributes instanceAttributes) {
		if (Gdx.gl30 == null)
			throw new GdxRuntimeException("InstanceBufferObject requires a device running with GLES 3.0 compatibilty");

		bufferHandle = Gdx.gl20.glGenBuffer();

		ByteBuffer data = BufferUtils.newByteBuffer(instanceAttributes.vertexSize * numVertices);
		data.limit(0);
		setBuffer(data, true, instanceAttributes);
		setUsage(isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW);
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}

	@Override
	public int getNumInstances () {
		return buffer.limit() * 4 / attributes.vertexSize;
	}

	@Override
	public int getNumMaxInstances () {
		return byteBuffer.capacity() / attributes.vertexSize;
	}

	@Override
	public FloatBuffer getBuffer () {
		isDirty = true;
		return buffer;
	}

	/**
	 * Low level method to reset the buffer and attributes to the specified values. Use with care!
	 *
	 * @param data
	 * @param ownsBuffer
	 * @param value
	 */
	protected void setBuffer (Buffer data, boolean ownsBuffer, VertexAttributes value) {
		if (isBound)
			throw new GdxRuntimeException("Cannot change attributes while VBO is bound");
		attributes = value;
		if (data instanceof ByteBuffer)
			byteBuffer = (ByteBuffer)data;
		else
			throw new GdxRuntimeException("Only ByteBuffer is currently supported");
		this.ownsBuffer = ownsBuffer;

		final int l = byteBuffer.limit();
		byteBuffer.limit(byteBuffer.capacity());
		buffer = byteBuffer.asFloatBuffer();
		byteBuffer.limit(l);
		buffer.limit(l / 4);
	}

	private void bufferChanged () {
		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), null, usage);
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}
	}

	@Override
	public void setInstanceData (float[] data, int offset, int count) {
		isDirty = true;
		BufferUtils.copy(data, byteBuffer, count, offset);
		buffer.position(0);
		buffer.limit(count);
		bufferChanged();
	}

	@Override
	public void setInstanceData (FloatBuffer data, int count) {
		isDirty = true;
		BufferUtils.copy(data, byteBuffer, count);
		buffer.position(0);
		buffer.limit(count);
		bufferChanged();
	}

	@Override
	public void updateInstanceData (int targetOffset, float[] data, int sourceOffset, int count) {
		isDirty = true;
		final int pos = byteBuffer.position();
		byteBuffer.position(targetOffset * 4);
		BufferUtils.copy(data, sourceOffset, count, byteBuffer);
		byteBuffer.position(pos);
		buffer.position(0);
		bufferChanged();
	}

	@Override
	public void updateInstanceData (int targetOffset, FloatBuffer data, int sourceOffset, int count) {
		isDirty = true;
		final int pos = byteBuffer.position();
		byteBuffer.position(targetOffset * 4);
		data.position(sourceOffset * 4);
		BufferUtils.copy(data, byteBuffer, count);
		byteBuffer.position(pos);
		buffer.position(0);
		bufferChanged();
	}

	/**
	 * @return The GL enum used in the call to {@link GL20#glBufferData(int, int, java.nio.Buffer, int)}, e.g. GL_STATIC_DRAW or
	 * GL_DYNAMIC_DRAW
	 */
	protected int getUsage () {
		return usage;
	}

	/**
	 * Set the GL enum used in the call to {@link GL20#glBufferData(int, int, java.nio.Buffer, int)}, can only be called when the
	 * VBO is not bound.
	 */
	protected void setUsage (int value) {
		if (isBound)
			throw new GdxRuntimeException("Cannot change usage while VBO is bound");
		usage = value;
	}

	/**
	 * Binds this InstanceBufferObject for rendering via glDrawArraysInstanced or glDrawElementsInstanced
	 *
	 * @param shader the shader
	 */
	@Override
	public void bind (ShaderProgram shader) {
		bind(shader, null);
	}

	@Override
	public void bind (ShaderProgram shader, int[] locations) {
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
	@Override
	public void invalidate () {
		bufferHandle = Gdx.gl20.glGenBuffer();
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
}
