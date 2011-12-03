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

package com.badlogic.gdx.backends.lwjgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implementation of the {@link GL10} interface based on Jogl. Fixed point vertex arrays are emulated.
 * 
 * @author mzechner */
class LwjglGL10 implements GL10 {
	private IntBuffer tempInt;
	private FloatBuffer tempFloat;

	public LwjglGL10 () {
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

	public final void glActiveTexture (int texture) {
		GL13.glActiveTexture(texture);
	}

	public final void glAlphaFunc (int func, float ref) {
		GL11.glAlphaFunc(func, ref);
	}

	public final void glBindTexture (int target, int texture) {
		GL11.glBindTexture(target, texture);
	}

	public final void glBlendFunc (int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public final void glClear (int mask) {
		GL11.glClear(mask);
	}

	public final void glClearColor (float red, float green, float blue, float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}

	public final void glClearDepthf (float depth) {
		GL11.glClearDepth(depth);
	}

	public final void glClearStencil (int s) {
		GL11.glClearStencil(s);
	}

	public final void glClientActiveTexture (int texture) {
		try {
			GL13.glClientActiveTexture(texture);
		} catch (Throwable ex) {
			// FIXME this is for my lousy netbook...
		}
	}

	public final void glColor4f (float red, float green, float blue, float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}

	public final void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GL11.glColorMask(red, green, blue, alpha);
	}

	public final void glColorPointer (int size, int type, int stride, Buffer pointer) {
		if (pointer instanceof FloatBuffer && type == GL10.GL_FLOAT)
			GL11.glColorPointer(size, stride, (FloatBuffer)pointer);
		else if (pointer instanceof ByteBuffer && type == GL10.GL_FLOAT)
			GL11.glColorPointer(size, stride, ((ByteBuffer)pointer).asFloatBuffer()); // FIXME yes, that's why it sucks... GC will
// be happy.
		else if (pointer instanceof ByteBuffer && type == GL10.GL_UNSIGNED_BYTE)
			GL11.glColorPointer(size, true, stride, (ByteBuffer)pointer);
		else
			throw new GdxRuntimeException("Can't use " + pointer.getClass().getName()
				+ " with this method, use FloatBuffer or ByteBuffer. blame LWJGL");
	}

	public final void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		if (!(data instanceof ByteBuffer))
			throw new GdxRuntimeException("Can't use " + data.getClass().getName()
				+ " with this method. Use ByteBuffer. Blame LWJGL");
		GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, (ByteBuffer)data);
	}

	public final void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height,
		int format, int imageSize, Buffer data) {
		if (!(data instanceof ByteBuffer))
			throw new GdxRuntimeException("Can't use " + data.getClass().getName()
				+ " with this method. Use ByteBuffer. Blame LWJGL");
		GL13.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, (ByteBuffer)data);
	}

	public final void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL11.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public final void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public final void glCullFace (int mode) {
		GL11.glCullFace(mode);
	}

	public final void glDeleteTextures (int n, IntBuffer textures) {
		GL11.glDeleteTextures(textures);
	}

	public final void glDepthFunc (int func) {
		GL11.glDepthFunc(func);
	}

	public final void glDepthMask (boolean flag) {
		GL11.glDepthMask(flag);
	}

	public final void glDepthRangef (float zNear, float zFar) {
		GL11.glDepthRange(zNear, zFar);
	}

	public final void glDisable (int cap) {
		GL11.glDisable(cap);
	}

	public final void glDisableClientState (int array) {
		GL11.glDisableClientState(array);
	}

	public final void glDrawArrays (int mode, int first, int count) {
		GL11.glDrawArrays(mode, first, count);
	}

	public final void glDrawElements (int mode, int count, int type, Buffer indices) {
		if (indices instanceof ShortBuffer && type == GL10.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, (ShortBuffer)indices);
		else if (indices instanceof ByteBuffer && type == GL10.GL_UNSIGNED_SHORT)
			GL11.glDrawElements(mode, ((ByteBuffer)indices).asShortBuffer()); // FIXME yay...
		else if (indices instanceof ByteBuffer && type == GL10.GL_UNSIGNED_BYTE)
			GL11.glDrawElements(mode, (ByteBuffer)indices);
		else
			throw new GdxRuntimeException("Can't use " + indices.getClass().getName()
				+ " with this method. Use ShortBuffer or ByteBuffer instead. Blame LWJGL");
	}

	public final void glEnable (int cap) {
		GL11.glEnable(cap);
	}

	public final void glEnableClientState (int array) {
		GL11.glEnableClientState(array);
	}

	public final void glFinish () {
		GL11.glFinish();
	}

	public final void glFlush () {
		GL11.glFlush();
	}

	public final void glFogf (int pname, float param) {
		GL11.glFogf(pname, param);
	}

	public final void glFogfv (int pname, FloatBuffer params) {
		GL11.glFog(pname, params);
	}

	public final void glFrontFace (int mode) {
		GL11.glFrontFace(mode);
	}

	public final void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) {
		GL11.glFrustum(left, right, bottom, top, zNear, zFar);
	}

	public final void glGenTextures (int n, IntBuffer textures) {
		GL11.glGenTextures(textures);
	}

	public final int glGetError () {
		return GL11.glGetError();
	}

	public final void glGetIntegerv (int pname, IntBuffer params) {
		GL11.glGetInteger(pname, params);
	}

	public final String glGetString (int name) {
		return GL11.glGetString(name);
	}

	public final void glHint (int target, int mode) {
		GL11.glHint(target, mode);
	}

	public final void glLightModelf (int pname, float param) {
		GL11.glLightModelf(pname, param);
	}

	public final void glLightModelfv (int pname, FloatBuffer params) {
		GL11.glLightModel(pname, params);
	}

	public final void glLightf (int light, int pname, float param) {
		GL11.glLightf(light, pname, param);
	}

	public final void glLightfv (int light, int pname, FloatBuffer params) {
		GL11.glLight(light, pname, params);
	}

	public final void glLineWidth (float width) {
		GL11.glLineWidth(width);
	}

	public final void glLoadIdentity () {
		GL11.glLoadIdentity();
	}

	public final void glLoadMatrixf (FloatBuffer m) {
		GL11.glLoadMatrix(m);
	}

	public final void glLogicOp (int opcode) {
		GL11.glLogicOp(opcode);
	}

	public final void glMaterialf (int face, int pname, float param) {
		GL11.glMaterialf(face, pname, param);
	}

	public final void glMaterialfv (int face, int pname, FloatBuffer params) {
		GL11.glMaterial(face, pname, params);
	}

	public final void glMatrixMode (int mode) {
		GL11.glMatrixMode(mode);
	}

	public final void glMultMatrixf (FloatBuffer m) {
		GL11.glMultMatrix(m);
	}

	public final void glMultiTexCoord4f (int target, float s, float t, float r, float q) {
		GL13.glMultiTexCoord4f(target, s, t, r, q);
	}

	public final void glNormal3f (float nx, float ny, float nz) {
		GL11.glNormal3f(nx, ny, nz);
	}

	public final void glNormalPointer (int type, int stride, Buffer pointer) {
		if (pointer instanceof FloatBuffer && type == GL11.GL_FLOAT)
			GL11.glNormalPointer(stride, (FloatBuffer)pointer);
		else if (pointer instanceof ByteBuffer && type == GL11.GL_FLOAT)
			GL11.glNormalPointer(stride, ((ByteBuffer)pointer).asFloatBuffer());
		else if (pointer instanceof ByteBuffer && type == GL11.GL_BYTE)
			GL11.glNormalPointer(stride, (ByteBuffer)pointer);
		else
			throw new GdxRuntimeException("Can't use " + pointer.getClass().getName()
				+ " with this method. GL10.GL_SHORT not supported. Use FloatBuffer instead. Blame LWJGL");
	}

	public final void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}

	public final void glPixelStorei (int pname, int param) {
		GL11.glPixelStorei(pname, param);
	}

	public final void glPointSize (float size) {
		GL11.glPointSize(size);
	}

	public final void glPolygonOffset (float factor, float units) {
		GL11.glPolygonOffset(factor, units);
	}

	public final void glPopMatrix () {
		GL11.glPopMatrix();
	}

	public final void glPushMatrix () {
		GL11.glPushMatrix();
	}

	public final void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glReadPixels(x, y, width, height, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL");
	}

	public final void glRotatef (float angle, float x, float y, float z) {
		GL11.glRotatef(angle, x, y, z);
	}

	public final void glSampleCoverage (float value, boolean invert) {
		GL13.glSampleCoverage(value, invert);
	}

	public final void glScalef (float x, float y, float z) {
		GL11.glScalef(x, y, z);
	}

	public final void glScissor (int x, int y, int width, int height) {
		GL11.glScissor(x, y, width, height);
	}

	public final void glShadeModel (int mode) {
		GL11.glShadeModel(mode);
	}

	public final void glStencilFunc (int func, int ref, int mask) {
		GL11.glStencilFunc(func, ref, mask);
	}

	public final void glStencilMask (int mask) {
		GL11.glStencilMask(mask);
	}

	public final void glStencilOp (int fail, int zfail, int zpass) {
		GL11.glStencilOp(fail, zfail, zpass);
	}

	public final void glTexCoordPointer (int size, int type, int stride, Buffer pointer) {
		if (pointer instanceof ShortBuffer && type == GL10.GL_SHORT)
			GL11.glTexCoordPointer(size, stride, (ShortBuffer)pointer);
		else if (pointer instanceof ByteBuffer && type == GL10.GL_SHORT)
			GL11.glTexCoordPointer(size, stride, ((ByteBuffer)pointer).asShortBuffer());
		else if (pointer instanceof FloatBuffer && type == GL10.GL_FLOAT)
			GL11.glTexCoordPointer(size, stride, (FloatBuffer)pointer);
		else if (pointer instanceof ByteBuffer && type == GL10.GL_FLOAT)
			GL11.glTexCoordPointer(size, stride, ((ByteBuffer)pointer).asFloatBuffer());
		else
			throw new GdxRuntimeException(
				"Can't use "
					+ pointer.getClass().getName()
					+ " with this method. Use ShortBuffer or FloatBuffer or ByteBuffer instead with GL_FLOAT or GL_SHORT. GL_BYTE is not supported. Blame LWJGL");
	}

	public final void glTexEnvf (int target, int pname, float param) {
		GL11.glTexEnvf(target, pname, param);
	}

	public final void glTexEnvfv (int target, int pname, FloatBuffer params) {
		GL11.glTexEnv(target, pname, params);
	}

	public final void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, Buffer pixels) {

		if (pixels == null)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			 GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public final void glTexParameterf (int target, int pname, float param) {
		// LwjglGraphics.major is should to be 1 if we are in LwjglGL10.
		if (LwjglGraphics.minor < 2 && param == GL12.GL_CLAMP_TO_EDGE) param = GL11.GL_CLAMP;
		GL11.glTexParameterf(target, pname, param);
	}

	public final void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");

	}

	public final void glTranslatef (float x, float y, float z) {
		GL11.glTranslatef(x, y, z);
	}

	public final void glVertexPointer (int size, int type, int stride, Buffer pointer) {
		if (pointer instanceof FloatBuffer && type == GL10.GL_FLOAT)
			GL11.glVertexPointer(size, stride, ((FloatBuffer)pointer));
		else if (pointer instanceof ByteBuffer && type == GL10.GL_FLOAT)
			GL11.glVertexPointer(size, stride, ((ByteBuffer)pointer).asFloatBuffer());
		else
			throw new GdxRuntimeException("Can't use " + pointer.getClass().getName()
				+ " with this method. Use FloatBuffer or ByteBuffers with GL10.GL_FLOAT instead. Blame LWJGL");
	}

	public final void glViewport (int x, int y, int width, int height) {
		GL11.glViewport(x, y, width, height);
	}

	public final void glDeleteTextures (int n, int[] textures, int offset) {
		GL11.glDeleteTextures(toBuffer(n, textures, offset));
	}

	public final void glFogfv (int pname, float[] params, int offset) {
		GL11.glFog(pname, toBuffer(params, offset));
	}

	public final void glGenTextures (int n, int[] textures, int offset) {
		for (int i = offset; i < offset + n; i++)
			textures[i] = GL11.glGenTextures();
	}

	IntBuffer getBuffer = BufferUtils.createIntBuffer(100);

	public final void glGetIntegerv (int pname, int[] params, int offset) {
		GL11.glGetInteger(pname, getBuffer);
		// FIXME Yeah, so. This sucks as well :D LWJGL does not set pos/lim.
		for (int i = offset, j = 0; i < params.length; i++, j++) {
			if (j == getBuffer.capacity()) return;
			params[i] = getBuffer.get(j);
		}
	}

	public final void glLightModelfv (int pname, float[] params, int offset) {
		GL11.glLightModel(pname, toBuffer(params, offset));
	}

	public final void glLightfv (int light, int pname, float[] params, int offset) {
		GL11.glLight(light, pname, toBuffer(params, offset));
	}

	public final void glLoadMatrixf (float[] m, int offset) {
		GL11.glLoadMatrix(toBuffer(m, offset));
	}

	public final void glMaterialfv (int face, int pname, float[] params, int offset) {
		GL11.glMaterial(face, pname, toBuffer(params, offset));
	}

	public final void glMultMatrixf (float[] m, int offset) {
		GL11.glMultMatrix(toBuffer(m, offset));
	}

	public final void glTexEnvfv (int target, int pname, float[] params, int offset) {
		glTexEnvf(target, pname, params[offset]);
	}

	public void glPolygonMode (int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}
}
