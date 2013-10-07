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

package com.badlogic.gdx.backends.jglfw;

import static com.badlogic.gdx.backends.jglfw.JglfwUtil.*;
import static com.badlogic.jglfw.utils.Memory.*;

import com.badlogic.gdx.graphics.GL11;
import com.badlogic.jglfw.gl.GL;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class JglfwGL11 extends JglfwGL10 implements GL11 {
	public void glClipPlanef (int plane, float[] equation, int offset) {
		GL.glClipPlane(plane, toBuffer(equation, offset), 0);
	}

	public void glClipPlanef (int plane, FloatBuffer equation) {
		GL.glClipPlane(plane, equation, getPosition(equation));
	}

	public void glGetClipPlanef (int pname, float[] eqn, int offset) {
		FloatBuffer buffer = toBuffer(eqn, offset);
		GL.glGetClipPlane(pname, buffer, 0);
		toArray(buffer, eqn, offset);
	}

	public void glGetClipPlanef (int pname, FloatBuffer eqn) {
		GL.glGetClipPlane(pname, eqn, getPosition(eqn));
	}

	public void glGetFloatv (int pname, float[] params, int offset) {
		FloatBuffer buffer = toBuffer(params, offset);
		GL.glGetFloatv(pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetFloatv (int pname, FloatBuffer params) {
		GL.glGetFloatv(pname, params, getPosition(params));
	}

	public void glGetLightfv (int light, int pname, float[] params, int offset) {
		FloatBuffer buffer = toBuffer(params, offset);
		GL.glGetLightfv(light, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetLightfv (int light, int pname, FloatBuffer params) {
		GL.glGetLightfv(light, pname, params, getPosition(params));
	}

	public void glGetMaterialfv (int face, int pname, float[] params, int offset) {
		FloatBuffer buffer = toBuffer(params, offset);
		GL.glGetMaterialfv(face, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetMaterialfv (int face, int pname, FloatBuffer params) {
		GL.glGetMaterialfv(face, pname, params, getPosition(params));
	}

	public void glGetTexParameterfv (int target, int pname, float[] params, int offset) {
		FloatBuffer buffer = toBuffer(params, offset);
		GL.glGetTexParameterfv(target, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL.glGetTexParameterfv(target, pname, params, getPosition(params));
	}

	public void glPointParameterf (int pname, float param) {
		GL.glPointParameterf(pname, param);
	}

	public void glPointParameterfv (int pname, float[] params, int offset) {
		GL.glPointParameterfv(pname, toBuffer(params, offset), 0);
	}

	public void glPointParameterfv (int pname, FloatBuffer params) {
		GL.glPointParameterfv(pname, params, getPosition(params));
	}

	public void glTexParameterfv (int target, int pname, float[] params, int offset) {
		GL.glTexParameterfv(target, pname, toBuffer(params, offset), 0);
	}

	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GL.glTexParameterfv(target, pname, params, getPosition(params));
	}

	public void glBindBuffer (int target, int buffer) {
		GL.glBindBuffer(target, buffer);
	}

	public void glBufferData (int target, int size, Buffer data, int usage) {
		GL.glBufferData(target, size, data, getPosition(data), usage);
	}

	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		GL.glBufferSubData(target, offset, size, data, getPosition(data));
	}

	public void glColor4ub (byte red, byte green, byte blue, byte alpha) {
		GL.glColor4ub(red, green, blue, alpha);
	}

	public void glDeleteBuffers (int n, int[] buffers, int offset) {
		GL.glDeleteBuffers(n, toBuffer(buffers, offset), 0);
	}

	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GL.glDeleteBuffers(n, buffers, getPosition(buffers));
	}

	public void glGetBooleanv (int pname, boolean[] params, int offset) {
		ByteBuffer buffer = toBuffer(params, offset);
		GL.glGetBooleanv(pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetBooleanv (int pname, IntBuffer params) {
		GL.glGetBooleanv(pname, params, getPosition(params));
	}

	public void glGetBufferParameteriv (int target, int pname, int[] params, int offset) {
		IntBuffer buffer = toBuffer(params, offset);
		GL.glGetBufferParameteriv(target, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL.glGetBufferParameteriv(target, pname, params, getPosition(params));
	}

	public void glGenBuffers (int n, int[] buffers, int offset) {
		IntBuffer buffer = toBuffer(buffers, offset);
		GL.glGenBuffers(n, buffer, 0);
		toArray(buffer, buffers, offset);
	}

	public void glGenBuffers (int n, IntBuffer buffers) {
		GL.glGenBuffers(n, buffers, getPosition(buffers));
	}

	public void glGetPointerv (int pname, Buffer[] params) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	public void glGetTexEnviv (int env, int pname, int[] params, int offset) {
		IntBuffer buffer = toBuffer(params, offset);
		GL.glGetTexEnviv(env, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetTexEnviv (int env, int pname, IntBuffer params) {
		GL.glGetTexEnviv(env, pname, params, getPosition(params));
	}

	public void glGetTexParameteriv (int target, int pname, int[] params, int offset) {
		IntBuffer buffer = toBuffer(params, offset);
		GL.glGetTexParameteriv(target, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL.glGetTexParameteriv(target, pname, params, getPosition(params));
	}

	public boolean glIsBuffer (int buffer) {
		return GL.glIsBuffer(buffer);
	}

	public boolean glIsEnabled (int cap) {
		return GL.glIsEnabled(cap);
	}

	public boolean glIsTexture (int texture) {
		return GL.glIsTexture(texture);
	}

	public void glTexEnvi (int target, int pname, int param) {
		GL.glTexEnvi(target, pname, param);
	}

	public void glTexEnviv (int target, int pname, int[] params, int offset) {
		IntBuffer buffer = toBuffer(params, offset);
		GL.glTexEnviv(target, pname, buffer, 0);
		toArray(buffer, params, offset);
	}

	public void glTexEnviv (int target, int pname, IntBuffer params) {
		GL.glTexEnviv(target, pname, params, getPosition(params));
	}

	public void glTexParameteri (int target, int pname, int param) {
		GL.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv (int target, int pname, int[] params, int offset) {
		GL.glTexParameteriv(target, pname, toBuffer(params, offset), 0);
	}

	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GL.glTexParameteriv(target, pname, params, getPosition(params));
	}

	public void glPointSizePointerOES (int type, int stride, Buffer pointer) {
		// FIXME
		throw new UnsupportedOperationException();
	}

	public void glVertexPointer (int size, int type, int stride, int pointer) {
		GL.glVertexPointer(size, type, stride, pointer);
	}

	public void glColorPointer (int size, int type, int stride, int pointer) {
		GL.glColorPointer(size, type, stride, pointer);
	}

	public void glNormalPointer (int type, int stride, int pointer) {
		GL.glNormalPointer(type, stride, pointer);
	}

	public void glTexCoordPointer (int size, int type, int stride, int pointer) {
		GL.glTexCoordPointer(size, type, stride, pointer);
	}

	public void glDrawElements (int mode, int count, int type, int indices) {
		GL.glDrawElements(mode, count, type, indices);
	}
}