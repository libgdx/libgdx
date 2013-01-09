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
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.badlogic.gdx.graphics.GL10;

/** An implementation of the {@link GL10} interface based on Jogl. Fixed point vertex arrays are emulated.
 * 
 * @author mzechner */
class JoglGL10 implements GL10 {

	public JoglGL10 () {
	}

	@Override
	public final void glActiveTexture (int texture) {
		GLContext.getCurrentGL().glActiveTexture(texture);
	}

	@Override
	public final void glAlphaFunc (int func, float ref) {
		GLContext.getCurrentGL().getGL2ES1().glAlphaFunc(func, ref);
	}

	@Override
	public final void glBindTexture (int target, int texture) {
		GLContext.getCurrentGL().glBindTexture(target, texture);
	}

	@Override
	public final void glBlendFunc (int sfactor, int dfactor) {
		GLContext.getCurrentGL().glBlendFunc(sfactor, dfactor);
	}

	@Override
	public final void glClear (int mask) {
		GLContext.getCurrentGL().glClear(mask);
	}

	@Override
	public final void glClearColor (float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().glClearColor(red, green, blue, alpha);
	}

	@Override
	public final void glClearDepthf (float depth) {
		GLContext.getCurrentGL().glClearDepth(depth);
	}

	@Override
	public final void glClearStencil (int s) {
		GLContext.getCurrentGL().glClearStencil(s);
	}

	@Override
	public final void glClientActiveTexture (int texture) {
		try {
			GLContext.getCurrentGL().getGL2ES1().glClientActiveTexture(texture);
		} catch (Throwable ex) {
			// FIXME this is for my lousy netbook...
		}
	}

	@Override
	public final void glColor4f (float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().getGL2ES1().glColor4f(red, green, blue, alpha);
	}

	@Override
	public final void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GLContext.getCurrentGL().glColorMask(red, green, blue, alpha);
	}

	@Override
	public final void glColorPointer (int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glColorPointer(size, type, stride, pointer);
	}

	@Override
	public final void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GLContext.getCurrentGL().glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override
	public final void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height,
		int format, int imageSize, Buffer data) {
		GLContext.getCurrentGL().glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override
	public final void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GLContext.getCurrentGL().glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public final void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GLContext.getCurrentGL().glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public final void glCullFace (int mode) {
		GLContext.getCurrentGL().glCullFace(mode);
	}

	@Override
	public final void glDeleteTextures (int n, IntBuffer textures) {
		GLContext.getCurrentGL().glDeleteTextures(n, textures);
	}

	@Override
	public final void glDepthFunc (int func) {
		GLContext.getCurrentGL().glDepthFunc(func);
	}

	@Override
	public final void glDepthMask (boolean flag) {
		GLContext.getCurrentGL().glDepthMask(flag);
	}

	@Override
	public final void glDepthRangef (float zNear, float zFar) {
		GLContext.getCurrentGL().glDepthRange(zNear, zFar);
	}

	@Override
	public final void glDisable (int cap) {
		GLContext.getCurrentGL().glDisable(cap);
	}

	@Override
	public final void glDisableClientState (int array) {
		GLContext.getCurrentGL().getGL2ES1().glDisableClientState(array);
	}

	@Override
	public final void glDrawArrays (int mode, int first, int count) {
		GLContext.getCurrentGL().glDrawArrays(mode, first, count);
	}

	@Override
	public final void glDrawElements (int mode, int count, int type, Buffer indices) {
		// nothing to do here per documentation
		GLContext.getCurrentGL().glDrawElements(mode, count, type, indices);
	}

	@Override
	public final void glEnable (int cap) {
		GLContext.getCurrentGL().glEnable(cap);
	}

	@Override
	public final void glEnableClientState (int array) {
		GLContext.getCurrentGL().getGL2ES1().glEnableClientState(array);
	}

	@Override
	public final void glFinish () {
		GLContext.getCurrentGL().glFinish();
	}

	@Override
	public final void glFlush () {
		GLContext.getCurrentGL().glFlush();
	}

	@Override
	public final void glFogf (int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glFogf(pname, param);
	}

	@Override
	public final void glFogfv (int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glFogfv(pname, params);
	}

	@Override
	public final void glFrontFace (int mode) {
		GLContext.getCurrentGL().glFrontFace(mode);
	}

	@Override
	public final void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) {
		GLContext.getCurrentGL().getGL2ES1().glFrustum(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public final void glGenTextures (int n, IntBuffer textures) {
		GLContext.getCurrentGL().glGenTextures(n, textures);
	}

	@Override
	public final int glGetError () {
		return GLContext.getCurrentGL().glGetError();
	}

	@Override
	public final void glGetIntegerv (int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetIntegerv(pname, params);
	}

	@Override
	public final String glGetString (int name) {
		return GLContext.getCurrentGL().glGetString(name);
	}

	@Override
	public final void glHint (int target, int mode) {
		GLContext.getCurrentGL().glHint(target, mode);
	}

	@Override
	public final void glLightModelf (int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelf(pname, param);
	}

	@Override
	public final void glLightModelfv (int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelfv(pname, params);
	}

	@Override
	public final void glLightf (int light, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glLightf(light, pname, param);
	}

	@Override
	public final void glLightfv (int light, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glLightfv(light, pname, params);
	}

	@Override
	public final void glLineWidth (float width) {
		GLContext.getCurrentGL().glLineWidth(width);
	}

	@Override
	public final void glLoadIdentity () {
		GLContext.getCurrentGL().getGL2ES1().glLoadIdentity();
	}

	@Override
	public final void glLoadMatrixf (FloatBuffer m) {
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(m);
	}

	@Override
	public final void glLogicOp (int opcode) {
		GLContext.getCurrentGL().getGL2ES1().glLogicOp(opcode);
	}

	@Override
	public final void glMaterialf (int face, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialf(face, pname, param);
	}

	@Override
	public final void glMaterialfv (int face, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialfv(face, pname, params);
	}

	@Override
	public final void glMatrixMode (int mode) {
		GLContext.getCurrentGL().getGL2ES1().glMatrixMode(mode);
	}

	@Override
	public final void glMultMatrixf (FloatBuffer m) {
		GLContext.getCurrentGL().getGL2ES1().glMultMatrixf(m);
	}

	@Override
	public final void glMultiTexCoord4f (int target, float s, float t, float r, float q) {
		GLContext.getCurrentGL().getGL2ES1().glMultiTexCoord4f(target, s, t, r, q);
	}

	@Override
	public final void glNormal3f (float nx, float ny, float nz) {
		GLContext.getCurrentGL().getGL2ES1().glNormal3f(nx, ny, nz);
	}

	@Override
	public final void glNormalPointer (int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glNormalPointer(type, stride, pointer);
	}

	@Override
	public final void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) {
		GLContext.getCurrentGL().getGL2ES1().glOrtho(left, right, bottom, top, zNear, zFar);
	}

	@Override
	public final void glPixelStorei (int pname, int param) {
		GLContext.getCurrentGL().glPixelStorei(pname, param);
	}

	@Override
	public final void glPointSize (float size) {
		GLContext.getCurrentGL().getGL2ES1().glPointSize(size);
	}

	@Override
	public final void glPolygonOffset (float factor, float units) {
		GLContext.getCurrentGL().glPolygonOffset(factor, units);
	}

	@Override
	public final void glPopMatrix () {
		GLContext.getCurrentGL().getGL2ES1().glPopMatrix();
	}

	@Override
	public final void glPushMatrix () {
		GLContext.getCurrentGL().getGL2ES1().glPushMatrix();
	}

	@Override
	public final void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GLContext.getCurrentGL().glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public final void glRotatef (float angle, float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glRotatef(angle, x, y, z);
	}

	@Override
	public final void glSampleCoverage (float value, boolean invert) {
		GLContext.getCurrentGL().glSampleCoverage(value, invert);
	}

	@Override
	public final void glScalef (float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glScalef(x, y, z);
	}

	@Override
	public final void glScissor (int x, int y, int width, int height) {
		GLContext.getCurrentGL().glScissor(x, y, width, height);
	}

	@Override
	public final void glShadeModel (int mode) {
		GLContext.getCurrentGL().getGL2ES1().glShadeModel(mode);
	}

	@Override
	public final void glStencilFunc (int func, int ref, int mask) {
		GLContext.getCurrentGL().glStencilFunc(func, ref, mask);
	}

	@Override
	public final void glStencilMask (int mask) {
		GLContext.getCurrentGL().glStencilMask(mask);
	}

	@Override
	public final void glStencilOp (int fail, int zfail, int zpass) {
		GLContext.getCurrentGL().glStencilOp(fail, zfail, zpass);
	}

	@Override
	public final void glTexCoordPointer (int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glTexCoordPointer(size, type, stride, pointer);
	}

	@Override
	public final void glTexEnvf (int target, int pname, float param) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvf(target, pname, param);
	}

	@Override
	public final void glTexEnvfv (int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvfv(target, pname, params);
	}

	@Override
	public final void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, Buffer pixels) {
		GLContext.getCurrentGL().glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	@Override
	public final void glTexParameterf (int target, int pname, float param) {
		// JoglGL10.major is should to be 1 if we are in JoglGL10.
		if (JoglGraphics.minor < 2 && param == GL.GL_CLAMP_TO_EDGE) param = GL2.GL_CLAMP;
		GLContext.getCurrentGL().glTexParameterf(target, pname, param);
	}

	@Override
	public final void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, Buffer pixels) {
		GLContext.getCurrentGL().glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public final void glTranslatef (float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES1().glTranslatef(x, y, z);
	}

	@Override
	public final void glVertexPointer (int size, int type, int stride, Buffer pointer) {
		GLContext.getCurrentGL().getGL2ES1().glVertexPointer(size, GL10.GL_FLOAT, stride, pointer);
	}

	@Override
	public final void glViewport (int x, int y, int width, int height) {
		GLContext.getCurrentGL().glViewport(x, y, width, height);
	}

	@Override
	public final void glDeleteTextures (int n, int[] textures, int offset) {
		GLContext.getCurrentGL().glDeleteTextures(n, textures, offset);
	}

	@Override
	public final void glFogfv (int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glFogfv(pname, params, offset);
	}

	@Override
	public final void glGenTextures (int n, int[] textures, int offset) {
		GLContext.getCurrentGL().glGenTextures(n, textures, offset);
	}

	@Override
	public final void glGetIntegerv (int pname, int[] params, int offset) {
		GLContext.getCurrentGL().glGetIntegerv(pname, params, offset);
	}

	@Override
	public final void glLightModelfv (int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLightModelfv(pname, params, offset);
	}

	@Override
	public final void glLightfv (int light, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLightfv(light, pname, params, offset);
	}

	@Override
	public final void glLoadMatrixf (float[] m, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glLoadMatrixf(m, offset);
	}

	@Override
	public final void glMaterialfv (int face, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glMaterialfv(face, pname, params, offset);
	}

	@Override
	public final void glMultMatrixf (float[] m, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glMultMatrixf(m, offset);
	}

	@Override
	public final void glTexEnvfv (int target, int pname, float[] params, int offset) {
		GLContext.getCurrentGL().getGL2ES1().glTexEnvfv(target, pname, params, offset);
	}

	@Override
	public void glPolygonMode (int face, int mode) {
		//TODO GL2ES1
		GLContext.getCurrentGL().getGL2().glPolygonMode(face, mode);
	}
}
