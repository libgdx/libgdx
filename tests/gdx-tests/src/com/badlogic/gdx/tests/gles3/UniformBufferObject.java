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

package com.badlogic.gdx.tests.gles3;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

/** UBO that binds to a specified Binding Point, which may be bound to an uniform block in a shader. A binding point is similar to
 * a texture unit, and makes it possible to use the data of the UBO with different shaders and later calls to the same shader.
 * <p>
 * For more information, consult:
 * <li>http://www.opengl.org/wiki/Uniform_Buffer_Object
 * <li>http://www.lighthouse3d.com/tutorials/glsl-core-tutorial/3490-2/
 * 
 * @author mattijs driel */
public class UniformBufferObject implements Disposable {
	private final int glHandle;
	private int currentBindingPoint;

	private final ByteBuffer dataBuffer;
	private final int byteCapacity;
	private static final IntBuffer singleInt = BufferUtils.newIntBuffer(1);

	private boolean isDirty = false;
	private int changeOffset;
	private int changeSize;

	public UniformBufferObject (int bytesize, int bindingPoint) {
		this.dataBuffer = BufferUtils.newByteBuffer(bytesize);
		this.byteCapacity = dataBuffer.capacity();

		singleInt.position(0);
		Gdx.gl30.glGenBuffers(1, singleInt);
		glHandle = singleInt.get(0);

		Gdx.gl30.glBindBuffer(GL30.GL_UNIFORM_BUFFER, glHandle);
		Gdx.gl30.glBufferData(GL30.GL_UNIFORM_BUFFER, byteCapacity, dataBuffer, GL30.GL_DYNAMIC_DRAW);

		remapBindingPoint(bindingPoint);
	}

	/** Changes the binding point used by this UBO */
	public void remapBindingPoint (int newBindingPoint) {
		currentBindingPoint = newBindingPoint;
		Gdx.gl30.glBindBufferBase(GL30.GL_UNIFORM_BUFFER, currentBindingPoint, glHandle);
	}

	public int getBindingPoint () {
		return currentBindingPoint;
	}

	public int getByteCapacity () {
		return byteCapacity;
	}

	public ByteBuffer getDataBuffer () {
		return getDataBuffer(0, byteCapacity);
	}

	public ByteBuffer getDataBuffer (int offset, int length) {
		changeOffset = offset;
		changeSize = length;
		isDirty = true;
		
		dataBuffer.position(changeOffset);
		return dataBuffer;
	}

	/** Binds the buffer to GL_UNIFORM_BUFFER. Modifies the data if a call to getDataBuffer was done. */
	public void bind () {
		Gdx.gl30.glBindBuffer(GL30.GL_UNIFORM_BUFFER, glHandle);
		if (isDirty) {
			dataBuffer.position(0);
			Gdx.gl30.glBufferSubData(GL30.GL_UNIFORM_BUFFER, changeOffset, changeSize, dataBuffer);
			isDirty = false;
		}
	}

	@Override
	public void dispose () {
		singleInt.position(0);
		singleInt.put(glHandle);
		singleInt.position(0);
		Gdx.gl30.glDeleteBuffers(1, singleInt);
	}
}
