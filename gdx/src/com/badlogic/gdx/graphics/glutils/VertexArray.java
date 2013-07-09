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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.BufferUtils;

/** <p>
 * Convenience class for working with OpenGL vertex arrays. It interleaves all data in the order you specified in the constructor
 * via {@link VertexAttribute}.
 * </p>
 * 
 * <p>
 * This class does not support shaders and for that matter OpenGL ES 2.0. For this {@link VertexBufferObject}s are needed.
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

	/** {@inheritDoc} */
	@Override
	public void dispose () {
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);
	}

	/** {@inheritDoc} */
	@Override
	public FloatBuffer getBuffer () {
		return buffer;
	}

	/** {@inheritDoc} */
	@Override
	public int getNumVertices () {
		return buffer.limit() * 4 / attributes.vertexSize;
	}

	/** {@inheritDoc} */
	public int getNumMaxVertices () {
		return byteBuffer.capacity() / attributes.vertexSize;
	}

	/** {@inheritDoc} */
	@Override
	public void setVertices (float[] vertices, int offset, int count) {
		BufferUtils.copy(vertices, byteBuffer, count, offset);
		buffer.position(0);
		buffer.limit(count);
	}

	@Override
	public void bind () {
		GL10 gl = Gdx.gl10;
		int textureUnit = 0;
		int numAttributes = attributes.size();

		byteBuffer.limit(buffer.limit() * 4);

		for (int i = 0; i < numAttributes; i++) {
			VertexAttribute attribute = attributes.get(i);

			switch (attribute.usage) {
			case Usage.Position:
				byteBuffer.position(attribute.offset);
				gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				gl.glVertexPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, byteBuffer);
				break;

			case Usage.Color:
			case Usage.ColorPacked:
				int colorType = GL10.GL_FLOAT;
				if (attribute.usage == Usage.ColorPacked) colorType = GL11.GL_UNSIGNED_BYTE;
				byteBuffer.position(attribute.offset);
				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glColorPointer(attribute.numComponents, colorType, attributes.vertexSize, byteBuffer);
				break;

			case Usage.Normal:
				byteBuffer.position(attribute.offset);
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glNormalPointer(GL10.GL_FLOAT, attributes.vertexSize, byteBuffer);
				break;

			case Usage.TextureCoordinates:
				gl.glClientActiveTexture(GL10.GL_TEXTURE0 + textureUnit);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				byteBuffer.position(attribute.offset);
				gl.glTexCoordPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, byteBuffer);
				textureUnit++;
				break;

			default:
				// throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
			}
		}

		isBound = true;
	}

	@Override
	public void unbind () {
		GL10 gl = Gdx.gl10;
		int textureUnit = 0;
		int numAttributes = attributes.size();

		for (int i = 0; i < numAttributes; i++) {

			VertexAttribute attribute = attributes.get(i);
			switch (attribute.usage) {
			case Usage.Position:
				break; // no-op, we always need a position bound in gles
			case Usage.Color:
			case Usage.ColorPacked:
				gl.glDisableClientState(GL11.GL_COLOR_ARRAY);
				break;
			case Usage.Normal:
				gl.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				break;
			case Usage.TextureCoordinates:
				gl.glClientActiveTexture(GL11.GL_TEXTURE0 + textureUnit);
				gl.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				textureUnit++;
				break;
			default:
				// throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
			}
		}
		byteBuffer.position(0);
		isBound = false;
	}
	
	@Override
	public void bind (final ShaderProgram shader) {
		bind(shader, null);
	}
	
	@Override
	public void bind (final ShaderProgram shader, final int[] locations) {
		final GL20 gl = Gdx.gl20;
		final int numAttributes = attributes.size();
		byteBuffer.limit(buffer.limit() * 4);
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0)
					continue;
				shader.enableVertexAttribute(location);
				
				byteBuffer.position(attribute.offset);
				if (attribute.usage == Usage.ColorPacked)
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true, 
						attributes.vertexSize, byteBuffer);
				else
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false, 
						attributes.vertexSize, byteBuffer);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0)
					continue;
				shader.enableVertexAttribute(location);
				
				byteBuffer.position(attribute.offset);
				if (attribute.usage == Usage.ColorPacked)
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true, 
						attributes.vertexSize, byteBuffer);
				else
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false, 
						attributes.vertexSize, byteBuffer);
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
		final GL20 gl = Gdx.gl20;
		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				shader.disableVertexAttribute(attributes.get(i).alias);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final int location = locations[i];
				if (location >= 0)
					shader.disableVertexAttribute(location);
			}
		}
		isBound = false;
	}

	@Override
	public VertexAttributes getAttributes () {
		return attributes;
	}
}
