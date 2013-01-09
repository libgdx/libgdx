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

package com.badlogic.gdx.backends.jogl;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.badlogic.gdx.graphics.GL11;

/** An implementation of the {@link GL11} interface based on JoGLContext.getCurrentGL(). Fixed point vertex arrays are emulated. Some glGetXXX methods
 * are not implemented.
 * 
 * @author mzechner */
public final class JoglGL11 extends JoglGL10 implements GL11 {

	public JoglGL11 () {
		super();
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		GLContext.getCurrentGL().glBindBuffer(target, buffer);
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		GLContext.getCurrentGL().glBufferData(target, size, data, usage);
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		GLContext.getCurrentGL().glBufferSubData(target, offset, size, data);
	}

	@Override
	public void glClipPlanef (int plane, float[] equation, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glClipPlanef (int plane, FloatBuffer equation) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glColor4ub (byte red, byte green, byte blue, byte alpha) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glDeleteBuffers (int n, int[] buffers, int offset) {
		GLContext.getCurrentGL().glDeleteBuffers(n, buffers, offset);
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GLContext.getCurrentGL().glDeleteBuffers(n, buffers);
	}

	@Override
	public void glGenBuffers (int n, int[] buffers, int offset) {
		GLContext.getCurrentGL().glGenBuffers(n, buffers, offset);
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		GLContext.getCurrentGL().glGenBuffers(n, buffers);
	}

	@Override
	public void glGetBooleanv (int pname, boolean[] params, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glGetBooleanv (int pname, IntBuffer params) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().glGetBufferParameteriv(target, pname, params, offset);
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetClipPlanef (int pname, float[] eqn, int offset) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glGetClipPlanef (int pname, FloatBuffer eqn) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glGetFloatv (int pname, float[] params, int offset) {
		GLContext.getCurrentGL().glGetFloatv(pname, params, offset);
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		GLContext.getCurrentGL().glGetFloatv(pname, params);
	}

	@Override
	public void glGetLightfv (int light, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGetLightfv(light, pname, params, offset);
	}

	@Override
	public void glGetLightfv (int light, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetLightfv(light, pname, params);
	}

	@Override
	public void glGetMaterialfv (int face, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGetMaterialfv(face, pname, params, offset);
	}

	@Override
	public void glGetMaterialfv (int face, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetMaterialfv(face, pname, params);
	}

	@Override
	public void glGetPointerv (int pname, Buffer[] params) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glGetTexEnviv (int env, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glGetTexEnviv(env, pname, params, offset);
	}

	@Override
	public void glGetTexEnviv (int env, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glGetTexEnviv(env, pname, params);
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().glGetTexParameterfv(target, pname, params, offset);
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().glGetTexParameteriv(target, pname, params, offset);
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetTexParameteriv(target, pname, params);
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		return GLContext.getCurrentGL().glIsBuffer(buffer);
	}

	@Override
	public boolean glIsEnabled (int cap) {
		return GLContext.getCurrentGL().glIsEnabled(cap);
	}

	@Override
	public boolean glIsTexture (int texture) {
		return GLContext.getCurrentGL().glIsTexture(texture);
	}

	@Override
	public void glPointParameterf (int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glPointParameterf(pname, param);
	}

	@Override
	public void glPointParameterfv (int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glPointParameterfv(pname, params, offset);
	}

	@Override
	public void glPointParameterfv (int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glPointParameterfv(pname, params);
	}

	@Override
	public void glPointSizePointerOES (int type, int stride, Buffer pointer) {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void glTexEnvi (int target, int pname, int param) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvi(target, pname, param);
	}

	@Override
	public void glTexEnviv (int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnviv(target, pname, params, offset);
	}

	@Override
	public void glTexEnviv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnviv(target, pname, params);
	}

	@Override
	public void glTexParameterfv (int target, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().glTexParameterfv(target, pname, params, offset);
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().glTexParameterfv(target, pname, params);
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		GLContext.getCurrentGL().glTexParameteri(target, pname, param);
	}

	@Override
	public void glTexParameteriv (int target, int pname, int[] params, int offset) {
		GLContext.getCurrentGL().glTexParameteriv(target, pname, params, offset);
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glTexParameteriv(target, pname, params);
	}

	@Override
	public void glColorPointer (int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glColorPointer(size, type, stride, pointer);
	}

	@Override
	public void glNormalPointer (int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glNormalPointer(type, stride, pointer);
	}

	@Override
	public void glTexCoordPointer (int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public void glVertexPointer (int size, int type, int stride, int pointer) {
		GLContext.getCurrentGL().getGL2ES1().glVertexPointer(size, type, stride, pointer);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		GLContext.getCurrentGL().glDrawElements(mode, count, type, indices);
	}
}
