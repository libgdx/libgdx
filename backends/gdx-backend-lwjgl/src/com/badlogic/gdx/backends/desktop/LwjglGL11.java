/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.desktop;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;

import com.badlogic.gdx.graphics.GL10;

/**
 * An implementation of the {@link GL11} interface based on Jogl. Fixed point vertex arrays are emulated. Some glGetXXX methods
 * are not implemented.
 * 
 * @author mzechner
 * 
 */
public final class LwjglGL11 extends LwjglGL10 implements com.badlogic.gdx.graphics.GL11 {
	private float tmpFloat[] = new float[1000];
	private IntBuffer tempInt;
	private FloatBuffer tempFloat;

	public LwjglGL11 () {
		tempInt = BufferUtils.createIntBuffer(8);
		tempFloat = BufferUtils.createFloatBuffer(8);
	}

	private IntBuffer toBuffer (int n, int[] src, int offset) {
		if (tempInt.capacity() < n)
			tempInt = BufferUtils.createIntBuffer(n);
		else
			tempInt.clear();
		tempInt.put(src, offset, n);
		tempInt.flip();
		return tempInt;
	}

	private IntBuffer toBuffer (int[] src, int offset) {
		int n = src.length - offset;
		if (tempInt.capacity() < n)
			tempInt = BufferUtils.createIntBuffer(n);
		else
			tempInt.clear();
		tempInt.put(src, offset, n);
		tempInt.flip();
		return tempInt;
	}

	private FloatBuffer toBuffer (float[] src, int offset) {
		int n = src.length - offset;
		if (tempFloat.capacity() < n)
			tempFloat = BufferUtils.createFloatBuffer(n);
		else
			tempFloat.clear();
		tempFloat.put(src, offset, src.length - offset);
		tempFloat.flip();
		return tempFloat;
	}

	public void glBindBuffer (int target, int buffer) {
		ARBVertexBufferObject.glBindBufferARB(target, buffer);
	}

	public void glBufferData (int target, int size, Buffer data, int usage) {
		if (data instanceof ByteBuffer)
			GL15.glBufferData(target, (ByteBuffer)data, usage);
		else if (data instanceof IntBuffer)
			GL15.glBufferData(target, (IntBuffer)data, usage);
		else if (data instanceof FloatBuffer)
			GL15.glBufferData(target, (FloatBuffer)data, usage);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferData(target, (DoubleBuffer)data, usage);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferData(target, (ShortBuffer)data, usage);
	}

	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		if (data instanceof ByteBuffer)
			GL15.glBufferSubData(target, offset, (ByteBuffer)data);
		else if (data instanceof IntBuffer)
			GL15.glBufferSubData(target, offset, (IntBuffer)data);
		else if (data instanceof FloatBuffer)
			GL15.glBufferSubData(target, offset, (FloatBuffer)data);
		else if (data instanceof DoubleBuffer)
			GL15.glBufferSubData(target, offset, (DoubleBuffer)data);
		else if (data instanceof ShortBuffer) //
			GL15.glBufferSubData(target, offset, (ShortBuffer)data);
	}

	public void glClipPlanef (int plane, float[] equation, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glClipPlanef (int plane, FloatBuffer equation) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glClipPlanex (int plane, int[] equation, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glClipPlanex (int plane, IntBuffer equation) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glColor4ub (byte red, byte green, byte blue, byte alpha) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glDeleteBuffers (int n, int[] buffers, int offset) {
		GL15.glDeleteBuffers(toBuffer(n, buffers, offset));
	}

	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GL15.glDeleteBuffers(buffers);
	}

	public void glGenBuffers (int n, int[] buffers, int offset) {
		for (int i = offset; i < offset + n; i++)
			buffers[offset] = GL15.glGenBuffers();
	}

	public void glGenBuffers (int n, IntBuffer buffers) {
		GL15.glGenBuffers(buffers);
	}

	public void glGetBooleanv (int pname, boolean[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetBooleanv (int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetBufferParameteriv (int target, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL15.glGetBufferParameter(target, pname, params);
	}

	public void glGetClipPlanef (int pname, float[] eqn, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetClipPlanef (int pname, FloatBuffer eqn) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetClipPlanex (int pname, int[] eqn, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetClipPlanex (int pname, IntBuffer eqn) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetFixedv (int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetFixedv (int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetFloatv (int pname, float[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetFloatv (int pname, FloatBuffer params) {
		GL11.glGetFloat(pname, params);
	}

	public void glGetLightfv (int light, int pname, float[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetLightfv (int light, int pname, FloatBuffer params) {
		GL11.glGetLight(light, pname, params);
	}

	public void glGetLightxv (int light, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetLightxv (int light, int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetMaterialfv (int face, int pname, float[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetMaterialfv (int face, int pname, FloatBuffer params) {
		GL11.glGetMaterial(face, pname, params);
	}

	public void glGetMaterialxv (int face, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetMaterialxv (int face, int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetPointerv (int pname, Buffer[] params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexEnviv (int env, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexEnviv (int env, int pname, IntBuffer params) {
		GL11.glGetTexEnv(env, pname, params);
	}

	public void glGetTexEnvxv (int env, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexEnvxv (int env, int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexParameterfv (int target, int pname, float[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glGetTexParameter(target, pname, params);
	}

	public void glGetTexParameteriv (int target, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glGetTexParameter(target, pname, params);
	}

	public void glGetTexParameterxv (int target, int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glGetTexParameterxv (int target, int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public boolean glIsBuffer (int buffer) {
		return GL15.glIsBuffer(buffer);
	}

	public boolean glIsEnabled (int cap) {
		return GL11.glIsEnabled(cap);
	}

	public boolean glIsTexture (int texture) {
		return GL11.glIsTexture(texture);
	}

	public void glPointParameterf (int pname, float param) {
		GL14.glPointParameterf(pname, param);
	}

	public void glPointParameterfv (int pname, float[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glPointParameterfv (int pname, FloatBuffer params) {
		GL14.glPointParameter(pname, params);
	}

	public void glPointParameterx (int pname, int param) {
		GL14.glPointParameterf(pname, FIXED_TO_FLOAT * param);
	}

	public void glPointParameterxv (int pname, int[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glPointParameterxv (int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glPointSizePointerOES (int type, int stride, Buffer pointer) {
		throw new UnsupportedOperationException("not implemented");
	}

	public void glTexEnvi (int target, int pname, int param) {
		GL11.glTexEnvi(target, pname, param);
	}

	public void glTexEnviv (int target, int pname, int[] params, int offset) {
		GL11.glTexEnv(target, pname, toBuffer(params, offset));
	}

	public void glTexEnviv (int target, int pname, IntBuffer params) {
		GL11.glTexEnv(target, pname, params);
	}

	public void glTexParameterfv (int target, int pname, float[] params, int offset) {
		GL11.glTexParameter(target, pname, toBuffer(params, offset));
	}

	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GL11.glTexParameter(target, pname, params);
	}

	public void glTexParameteri (int target, int pname, int param) {
		GL11.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv (int target, int pname, int[] params, int offset) {
		GL11.glTexParameter(target, pname, toBuffer(params, offset));
	}

	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GL11.glTexParameter(target, pname, params);
	}

	public void glTexParameterxv (int target, int pname, int[] params, int offset) {
		if (tmpFloat.length < params.length) tmpFloat = new float[params.length];
		for (int i = 0; i < params.length; i++)
			tmpFloat[i] = params[i];
		glTexParameterfv(target, pname, tmpFloat, offset);
	}

	public void glTexParameterxv (int target, int pname, IntBuffer params) {
		if (tmpFloat.length < params.capacity()) tmpFloat = new float[params.capacity()];
		int i = 0;
		while (params.hasRemaining())
			tmpFloat[i] = FIXED_TO_FLOAT * params.get();
		glTexParameterfv(target, pname, tmpFloat, 0);
	}

	public void glColorPointer (int size, int type, int stride, int pointer) {
		if (type == GL10.GL_FIXED) type = GL10.GL_FLOAT;
		GL11.glColorPointer(size, type, stride, pointer);
	}

	public void glNormalPointer (int type, int stride, int pointer) {
		if (type == GL10.GL_FIXED) type = GL10.GL_FLOAT;
		GL11.glNormalPointer(type, stride, pointer);
	}

	public void glTexCoordPointer (int size, int type, int stride, int pointer) {
		if (type == GL10.GL_FIXED) type = GL10.GL_FLOAT;
		GL11.glTexCoordPointer(size, type, stride, pointer);
	}

	public void glVertexPointer (int size, int type, int stride, int pointer) {
		if (type == GL10.GL_FIXED) type = GL10.GL_FLOAT;
		GL11.glVertexPointer(size, type, stride, pointer);
	}

	public void glDrawElements (int mode, int count, int type, int indices) {
		GL11.glDrawElements(mode, count, type, indices);
	}
}
