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
import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;

/** <p>
 * Convenience class for working with OpenGL vertex arrays. It interleaves all data in the order you specified in the constructor
 * via {@link VertexAttribute}.
 * </p>
 * 
 * <p>
 * This class is not compatible with OpenGL 3+ core profiles. For this {@link VertexBufferObject}s are needed.
 * </p>
 * 
 * @author mzechner, Dave Clayton <contact@redskyforge.com> */
public class VertexArray implements VertexData {
	final VertexAttributes attributes;
	final FloatBuffer buffer;
	final ByteBuffer byteBuffer;
	boolean isBound = false;

	/** Constructs a new interleaved VertexArray
	 * 
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttribute}s */
	public VertexArray (int numVertices, VertexAttribute... attributes) {
		this(numVertices, new VertexAttributes(attributes));
	}

	/** Constructs a new interleaved VertexArray
	 * 
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttributes} */
	public VertexArray (int numVertices, VertexAttributes attributes) {
		this.attributes = attributes;
		byteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
		buffer = byteBuffer.asFloatBuffer();
		buffer.flip();
		byteBuffer.flip();
	}

	@Override
	public void dispose () {
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	@Override
	public FloatBuffer getBuffer () {
		return buffer;
	}

	@Override
	public int getNumVertices () {
		return buffer.limit() * 4 / attributes.vertexSize;
	}

	public int getNumMaxVertices () {
		return byteBuffer.capacity() / attributes.vertexSize;
	}

	@Override
	public void setVertices (float[] vertices, int offset, int count) {
		BufferUtils.copy(vertices, byteBuffer, count, offset);
		buffer.position(0);
		buffer.limit(count);
	}

	@Override
	public void updateVertices (int targetOffset, float[] vertices, int sourceOffset, int count) {
		final int pos = byteBuffer.position();
		byteBuffer.position(targetOffset * 4);
		BufferUtils.copy(vertices, sourceOffset, count, byteBuffer);
		byteBuffer.position(pos);
	}

	@Override
	public void bind (final ShaderProgram shader) {
		bind(shader, null);
	}

	@Override
	public void bind (final ShaderProgram shader, final int[] locations) {
		final int numAttributes = attributes.size();
		byteBuffer.limit(buffer.limit() * 4);
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				if (attribute.type == GL20.GL_FLOAT) {
					buffer.position(attribute.offset / 4);
					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, buffer);
				} else {
					byteBuffer.position(attribute.offset);
					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, byteBuffer);
				}
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				if (attribute.type == GL20.GL_FLOAT) {
					buffer.position(attribute.offset / 4);
					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, buffer);
				} else {
					byteBuffer.position(attribute.offset);
					shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized,
						attributes.vertexSize, byteBuffer);
				}
			}
		}
		isBound = true;
	}

	/** Unbinds this VertexBufferObject.
	 * 
	 * @param shader the shader */
	@Override
	public void unbind (ShaderProgram shader) {
		unbind(shader, null);
	}

	@Override
	public void unbind (ShaderProgram shader, int[] locations) {
		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				shader.disableVertexAttribute(attributes.get(i).alias);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final int location = locations[i];
				if (location >= 0) shader.disableVertexAttribute(location);
			}
		}
		isBound = false;
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}
	
	@Override
	public void invalidate () {
	}
}
