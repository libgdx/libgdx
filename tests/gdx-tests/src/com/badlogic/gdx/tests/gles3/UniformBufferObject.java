
package com.badlogic.gdx.tests.gles3;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

/** UBO that binds to a specified Binding Point, which may be bound to an uniform block in a shader. A binding point is similar to
 * a texture unit, and makes it possible to (A) share the data of the uniform block buffer between different shaders and (B)
 * preserve uniform data for later.
 * <p>
 * For more information, consult:
 * <li>http://www.opengl.org/wiki/Uniform_Buffer_Object
 * <li>http://www.lighthouse3d.com/tutorials/glsl-core-tutorial/3490-2/
 * 
 * @author mattijs driel */
public class UniformBufferObject implements Disposable {
	private int bufferID;
	private int currentBindingPoint;

	private ByteBuffer dataBuffer;
	private IntBuffer intbuf;

	private boolean isDirty = false;

	public UniformBufferObject (int bytesize, int bindingPoint) {
		this(BufferUtils.newByteBuffer(bytesize), bindingPoint);
	}

	public UniformBufferObject (ByteBuffer dataBuffer, int bindingPoint) {
		this.dataBuffer = dataBuffer;

		intbuf = BufferUtils.newIntBuffer(1);
		Gdx.gl30.glGenBuffers(1, intbuf);
		bufferID = intbuf.get(0);

		Gdx.gl30.glBindBuffer(GL30.GL_UNIFORM_BUFFER, bufferID);
		Gdx.gl30.glBufferData(GL30.GL_UNIFORM_BUFFER, dataBuffer.capacity(), dataBuffer, GL30.GL_DYNAMIC_DRAW);

		remapBindingPoint(bindingPoint);
	}

	/** Changes the binding point used by this UBO */
	public void remapBindingPoint (int newBindingPoint) {
		currentBindingPoint = newBindingPoint;
		Gdx.gl30.glBindBufferBase(GL30.GL_UNIFORM_BUFFER, currentBindingPoint, bufferID);
	}

	public int getBindingPoint () {
		return currentBindingPoint;
	}

	public ByteBuffer getDataBuffer () {
		isDirty = true;
		return dataBuffer;
	}

	/** Binds the buffer to GL_UNIFORM_BUFFER. Modifies the data if a call to getDataBuffer was done. */
	public void bind () {
		Gdx.gl30.glBindBuffer(GL30.GL_UNIFORM_BUFFER, bufferID);
		if (isDirty) {
			dataBuffer.position(0);
			Gdx.gl30.glBufferSubData(GL30.GL_UNIFORM_BUFFER, 0, dataBuffer.capacity(), dataBuffer);
			isDirty = false;
		}
	}

	@Override
	public void dispose () {
		intbuf.position(0);
		Gdx.gl30.glDeleteBuffers(1, intbuf);
	}
}
