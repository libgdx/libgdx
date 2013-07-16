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
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** <p>
 * A {@link VertexData} implementation based on OpenGL vertex buffer objects.
 * </p>
 * 
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object. This class
 * can be used seamlessly with OpenGL ES 1.x and 2.0.
 * </p>
 * 
 * <p>
 * In case OpenGL ES 2.0 is used in the application the data is bound via glVertexAttribPointer() according to the attribute
 * aliases specified via {@link VertexAttributes} in the constructor.
 * </p>
 * 
 * <p>
 * Uses indirect Buffers on Android 1.5/1.6 to fix GC invocation due to leaking PlatformAddress instances.
 * </p>
 * 
 * <p>
 * VertexBufferObjects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 * 
 * @author mzechner */
public class VertexBufferObjectSubData implements VertexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

	final VertexAttributes attributes;
	final FloatBuffer buffer;
	final ByteBuffer byteBuffer;
	int bufferHandle;
	final boolean isDirect;
	final boolean isStatic;
	final int usage;
	boolean isDirty = false;
	boolean isBound = false;

	/** Constructs a new interleaved VertexBufferObject.
	 * 
	 * @param isStatic whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes the {@link VertexAttribute}s. */
	public VertexBufferObjectSubData (boolean isStatic, int numVertices, VertexAttribute... attributes) {
		this.isStatic = isStatic;
		this.attributes = new VertexAttributes(attributes);
// if (Gdx.app.getType() == ApplicationType.Android
// && Gdx.app.getVersion() < 5) {
// byteBuffer = ByteBuffer.allocate(this.attributes.vertexSize
// * numVertices);
// byteBuffer.order(ByteOrder.nativeOrder());
// isDirect = false;
// } else {
		byteBuffer = BufferUtils.newByteBuffer(this.attributes.vertexSize * numVertices);
		isDirect = true;
// }
		usage = isStatic ? GL11.GL_STATIC_DRAW : GL11.GL_DYNAMIC_DRAW;
		buffer = byteBuffer.asFloatBuffer();
		bufferHandle = createBufferObject();
		buffer.flip();
		byteBuffer.flip();
	}

	private int createBufferObject () {
		if (Gdx.gl20 != null) {
			Gdx.gl20.glGenBuffers(1, tmpHandle);
			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, tmpHandle.get(0));
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
			Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		} else {
			Gdx.gl11.glGenBuffers(1, tmpHandle);
			Gdx.gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, tmpHandle.get(0));
			Gdx.gl11.glBufferData(GL11.GL_ARRAY_BUFFER, byteBuffer.capacity(), null, usage);
			Gdx.gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		}
		return tmpHandle.get(0);
	}

	/** {@inheritDoc} */
	@Override
	public VertexAttributes getAttributes () {
		return attributes;
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
	public FloatBuffer getBuffer () {
		isDirty = true;
		return buffer;
	}

	/** {@inheritDoc} */
	@Override
	public void setVertices (float[] vertices, int offset, int count) {
		isDirty = true;
		if (isDirect) {
			BufferUtils.copy(vertices, byteBuffer, count, offset);
			buffer.position(0);
			buffer.limit(count);
		} else {
			buffer.clear();
			buffer.put(vertices, offset, count);
			buffer.flip();
			byteBuffer.position(0);
			byteBuffer.limit(buffer.limit() << 2);
		}

		if (isBound) {
			if (Gdx.gl20 != null) {
				GL20 gl = Gdx.gl20;
				gl.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
			} else {
				GL11 gl = Gdx.gl11;
				gl.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
			}
			isDirty = false;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void bind () {
		GL11 gl = Gdx.gl11;

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, bufferHandle);
		if (isDirty) {
			byteBuffer.limit(buffer.limit() * 4);
			gl.glBufferSubData(GL11.GL_ARRAY_BUFFER, 0, byteBuffer.limit(), byteBuffer);
// gl.glBufferData(GL11.GL_ARRAY_BUFFER, byteBuffer.limit(),
// byteBuffer, usage);
			isDirty = false;
		}

		int textureUnit = 0;
		int numAttributes = attributes.size();

		for (int i = 0; i < numAttributes; i++) {
			VertexAttribute attribute = attributes.get(i);

			switch (attribute.usage) {
			case Usage.Position:
				gl.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				gl.glVertexPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
				break;

			case Usage.Color:
			case Usage.ColorPacked:
				int colorType = GL10.GL_FLOAT;
				if (attribute.usage == Usage.ColorPacked) colorType = GL11.GL_UNSIGNED_BYTE;

				gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
				gl.glColorPointer(attribute.numComponents, colorType, attributes.vertexSize, attribute.offset);
				break;

			case Usage.Normal:
				gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
				gl.glNormalPointer(GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
				break;

			case Usage.TextureCoordinates:
				gl.glClientActiveTexture(GL10.GL_TEXTURE0 + textureUnit);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glTexCoordPointer(attribute.numComponents, GL10.GL_FLOAT, attributes.vertexSize, attribute.offset);
				textureUnit++;
				break;

			default:
				throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
			}
		}

		isBound = true;
	}

	/** Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
	 * 
	 * @param shader the shader */
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
				shader.enableVertexAttribute(location);
	
				if (attribute.usage == Usage.ColorPacked)
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true, attributes.vertexSize,
						attribute.offset);
				else
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false, attributes.vertexSize,
						attribute.offset);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0)
					continue;
				shader.enableVertexAttribute(location);
	
				if (attribute.usage == Usage.ColorPacked)
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_UNSIGNED_BYTE, true, attributes.vertexSize,
						attribute.offset);
				else
					shader.setVertexAttribute(location, attribute.numComponents, GL20.GL_FLOAT, false, attributes.vertexSize,
						attribute.offset);
			}
		}
		isBound = true;
	}

	@Override
	public void unbind () {
		GL11 gl = Gdx.gl11;
		int textureUnit = 0;
		int numAttributes = attributes.size();

		for (int i = 0; i < numAttributes; i++) {

			VertexAttribute attribute = attributes.get(i);
			switch (attribute.usage) {
			case Usage.Position:
				break; // no-op, we also need a position bound in gles
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
				throw new GdxRuntimeException("unkown vertex attribute type: " + attribute.usage);
			}
		}

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/** Unbinds this VertexBufferObject.
	 * 
	 * @param shader the shader */
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
				shader.disableVertexAttribute(attributes.get(i).alias);
			}
		} else {
			for (int i = 0; i < numAttributes; i++) {
				final int location = locations[i];
				if (location >= 0)
					shader.disableVertexAttribute(location);
			}
		}
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/** Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	public void invalidate () {
		bufferHandle = createBufferObject();
		isDirty = true;
	}

	/** Disposes of all resources this VertexBufferObject uses. */
	@Override
	public void dispose () {
		if (Gdx.gl20 != null) {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			tmpHandle.flip();
			GL20 gl = Gdx.gl20;
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;
		} else {
			tmpHandle.clear();
			tmpHandle.put(bufferHandle);
			tmpHandle.flip();
			GL11 gl = Gdx.gl11;
			gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
			gl.glDeleteBuffers(1, tmpHandle);
			bufferHandle = 0;
		}
	}

	/** Returns the VBO handle
	 * @return the VBO handle */
	public int getBufferHandle () {
		return bufferHandle;
	}
}
