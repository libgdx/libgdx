package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * <p>
 * A {@link VertexData} implementation that uses vertex buffer objects and vertex array objects.
 * (This is required for OpenGL 3.0+ core profiles. In particular, the default VAO has been
 * deprecated, as has the use of client memory for passing vertex attributes.) Use of VAOs should
 * give a slight performance benefit since you don't have to bind the attributes on every draw
 * anymore.
 * </p>
 *
 * <p>
 * If the OpenGL ES context was lost you can call {@link #invalidate()} to recreate a new OpenGL vertex buffer object.
 * </p>
 *
 * <p>
 * VertexBufferObjectWithVAO objects must be disposed via the {@link #dispose()} method when no longer needed
 * </p>
 *
 * Code adapted from {@link VertexBufferObject}.
 * @author mzechner, Dave Clayton <contact@redskyforge.com>, Nate Austin <nate.austin gmail>
 */
public class VertexBufferObjectWithVAO implements VertexData {
	final static IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);

	final VertexAttributes attributes;
	final FloatBuffer buffer;
	final ByteBuffer byteBuffer;
	int bufferHandle;
	final boolean isStatic;
	final int usage;
	boolean isDirty = false;
	boolean isBound = false;
	boolean vaoDirty = true;
	int vaoHandle = -1;


	/**
	 * Constructs a new interleaved VertexBufferObjectWithVAO.
	 *
	 * @param isStatic    whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes  the {@link com.badlogic.gdx.graphics.VertexAttribute}s.
	 */
	public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttribute... attributes) {
		this(isStatic, numVertices, new VertexAttributes(attributes));
	}

	/**
	 * Constructs a new interleaved VertexBufferObjectWithVAO.
	 *
	 * @param isStatic    whether the vertex data is static.
	 * @param numVertices the maximum number of vertices
	 * @param attributes  the {@link VertexAttributes}.
	 */
	public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttributes attributes) {
		this.isStatic = isStatic;
		this.attributes = attributes;

		byteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
		buffer = byteBuffer.asFloatBuffer();
		buffer.flip();
		byteBuffer.flip();
		bufferHandle = createBufferObject();
		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
	}

	private int createBufferObject() {
		Gdx.gl20.glGenBuffers(1, tmpHandle);
		return tmpHandle.get(0);
	}

	@Override
	public VertexAttributes getAttributes() {
		return attributes;
	}

	@Override
	public int getNumVertices() {
		return buffer.limit() * 4 / attributes.vertexSize;
	}

	@Override
	public int getNumMaxVertices() {
		return byteBuffer.capacity() / attributes.vertexSize;
	}

	@Override
	public FloatBuffer getBuffer() {
		isDirty = true;
		return buffer;
	}

	private void bufferChanged() {
		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}
	}

	@Override
	public void setVertices(float[] vertices, int offset, int count) {
		isDirty = true;
		BufferUtils.copy(vertices, byteBuffer, count, offset);
		buffer.position(0);
		buffer.limit(count);
		bufferChanged();
	}

	@Override
	public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
		isDirty = true;
		final int pos = byteBuffer.position();
		byteBuffer.position(targetOffset * 4);
		BufferUtils.copy(vertices, sourceOffset, count, byteBuffer);
		byteBuffer.position(pos);
		buffer.position(0);
		bufferChanged();
	}

	/**
	 * Binds this VertexBufferObject for rendering via glDrawArrays or glDrawElements
	 *
	 * @param shader the shader
	 */
	@Override
	public void bind(ShaderProgram shader) {
		bind(shader, null);
	}

	@Override
	public void bind(ShaderProgram shader, int[] locations) {
		GL30 gl = Gdx.gl30;
		if (vaoDirty || !gl.glIsVertexArray(vaoHandle)) {
			tmpHandle.clear();
			gl.glGenVertexArrays(1, tmpHandle);
			vaoHandle = tmpHandle.get(0);
			gl.glBindVertexArray(vaoHandle);

			//initialize the VAO with our vertex attributes and buffer:
			bindAttributes(shader, locations);
			vaoDirty = false;

		} else {
			//else simply bind the VAO.
			gl.glBindVertexArray(vaoHandle);
		}
		//if our data has changed upload it:
		bindData(gl);

		isBound = true;
	}

	private void bindAttributes(ShaderProgram shader, int[] locations) {
		final GL20 gl = Gdx.gl20;
		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
		final int numAttributes = attributes.size();
		if (locations == null) {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = shader.getAttributeLocation(attribute.alias);
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
						attribute.offset);
			}

		} else {
			for (int i = 0; i < numAttributes; i++) {
				final VertexAttribute attribute = attributes.get(i);
				final int location = locations[i];
				if (location < 0) continue;
				shader.enableVertexAttribute(location);

				shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, attributes.vertexSize,
						attribute.offset);
			}
		}
	}

	private void bindData(GL20 gl) {
		if (isDirty) {
			gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, bufferHandle);
			byteBuffer.limit(buffer.limit() * 4);
			gl.glBufferData(GL20.GL_ARRAY_BUFFER, byteBuffer.limit(), byteBuffer, usage);
			isDirty = false;
		}
	}

	/**
	 * Unbinds this VertexBufferObject.
	 *
	 * @param shader the shader
	 */
	@Override
	public void unbind(final ShaderProgram shader) {
		unbind(shader, null);
	}

	@Override
	public void unbind(final ShaderProgram shader, final int[] locations) {
		GL30 gl = Gdx.gl30;
		gl.glBindVertexArray(0);
		isBound = false;
	}

	//TODO: should invalidate be added to the VertexData interface?
	/**
	 * Invalidates the VertexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss.
	 */
	public void invalidate() {
		bufferHandle = createBufferObject();
		isDirty = true;
		vaoDirty = true;
	}

	/**
	 * Disposes of all resources this VertexBufferObject uses.
	 */
	@Override
	public void dispose() {
		GL30 gl = Gdx.gl30;
		tmpHandle.clear();
		tmpHandle.put(bufferHandle);
		tmpHandle.flip();

		gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
		gl.glDeleteBuffers(1, tmpHandle);
		bufferHandle = 0;
		BufferUtils.disposeUnsafeByteBuffer(byteBuffer);

		if (gl.glIsVertexArray(vaoHandle)) {
			tmpHandle.clear();
			tmpHandle.put(vaoHandle);
			tmpHandle.flip();
			gl.glDeleteVertexArrays(1, tmpHandle);
		}
	}
}
