
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.graphics.GL10;
import static com.badlogic.jglfw.gl.GL.*;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class JglfwGL10 implements GL10 {
	public void glActiveTexture (int texture) {
		glActiveTexture(texture);
	}

	public void glBindTexture (int target, int texture) {
		glBindTexture(target, texture);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		glBlendFunc(sfactor, dfactor);
	}

	public void glClear (int mask) {
		glClear(mask);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf (float depth) {
		glClearDepthf(depth);
	}

	public void glClearStencil (int s) {
		glClearStencil(s);
	}

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		glColorMask(red, green, blue, alpha);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public void glCullFace (int mode) {
		glCullFace(mode);
	}

	public void glDeleteTextures (int n, IntBuffer textures) {
		glDeleteTextures(n, textures);
	}

	public void glDepthFunc (int func) {
		glDepthFunc(func);
	}

	public void glDepthMask (boolean flag) {
		glDepthMask(flag);
	}

	public void glDepthRangef (float zNear, float zFar) {
		glDepthRangef(zNear, zFar);
	}

	public void glDisable (int cap) {
		glDisable(cap);
	}

	public void glDrawArrays (int mode, int first, int count) {
		glDrawArrays(mode, first, count);
	}

	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		glDrawElements(mode, count, type, indices);
	}

	public void glEnable (int cap) {
		glEnable(cap);
	}

	public void glFinish () {
		glFinish();
	}

	public void glFlush () {
		glFlush();
	}

	public void glFrontFace (int mode) {
		glFrontFace(mode);
	}

	public void glGenTextures (int n, IntBuffer textures) {
		glGenTextures(n, textures);
	}

	public int glGetError () {
		return glGetError();
	}

	public void glGetIntegerv (int pname, IntBuffer params) {
		glGetIntegerv(pname, params);
	}

	public String glGetString (int name) {
		return glGetString(name);
	}

	public void glHint (int target, int mode) {
		glHint(target, mode);
	}

	public void glLineWidth (float width) {
		glLineWidth(width);
	}

	public void glPixelStorei (int pname, int param) {
		glPixelStorei(pname, param);
	}

	public void glPolygonOffset (float factor, float units) {
		glPolygonOffset(factor, units);
	}

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		glReadPixels(x, y, width, height, format, type, pixels);
	}

	public void glScissor (int x, int y, int width, int height) {
		glScissor(x, y, width, height);
	}

	public void glStencilFunc (int func, int ref, int mask) {
		glStencilFunc(func, ref, mask);
	}

	public void glStencilMask (int mask) {
		glStencilMask(mask);
	}

	public void glStencilOp (int fail, int zfail, int zpass) {
		glStencilOp(fail, zfail, zpass);
	}

	public void glTexImage2D (int target, int level, int internalFormat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}

	public void glTexParameterf (int target, int pname, float param) {
		glTexParameterf(target, pname, param);
	}

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	public void glViewport (int x, int y, int width, int height) {
		glViewport(x, y, width, height);
	}

	public void glAlphaFunc (int func, float ref) {
		glAlphaFunc(func, ref);
	}

	public void glClientActiveTexture (int texture) {
		glClientActiveTexture(texture);
	}

	public void glColor4f (float red, float green, float blue, float alpha) {
		glColor4f(red, green, blue, alpha);
	}

	public void glColorPointer (int size, int type, int stride, Buffer pointer) {
		glColorPointer(size, type, stride, pointer);
	}

	public void glDeleteTextures (int n, int[] textures, int offset) {
		glDeleteTextures(n, textures, offset);
	}

	public void glDisableClientState (int array) {
		glDisableClientState(array);
	}

	public void glEnableClientState (int array) {
		glEnableClientState(array);
	}

	public void glFogf (int pname, float param) {
		glFogf(pname, param);
	}

	public void glFogfv (int pname, float[] params, int offset) {
		glFogfv(pname, params, offset);
	}

	public void glFogfv (int pname, FloatBuffer params) {
		glFogfv(pname, params);
	}

	public void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) {
		glFrustumf(left, right, bottom, top, zNear, zFar);
	}

	public void glGenTextures (int n, int[] textures, int offset) {
		glGenTextures(n, textures, offset);
	}

	public void glGetIntegerv (int pname, int[] params, int offset) {
		glGetIntegerv(pname, params, offset);
	}

	public void glLightModelf (int pname, float param) {
		glLightModelf(pname, param);
	}

	public void glLightModelfv (int pname, float[] params, int offset) {
		glLightModelfv(pname, params, offset);
	}

	public void glLightModelfv (int pname, FloatBuffer params) {
		glLightModelfv(pname, params);
	}

	public void glLightf (int light, int pname, float param) {
		glLightf(light, pname, param);
	}

	public void glLightfv (int light, int pname, float[] params, int offset) {
		glLightfv(light, pname, params, offset);
	}

	public void glLightfv (int light, int pname, FloatBuffer params) {
		glLightfv(light, pname, params);
	}

	public void glLoadIdentity () {
		glLoadIdentity();
	}

	public void glLoadMatrixf (float[] m, int offset) {
		glLoadMatrixf(m, offset);
	}

	public void glLoadMatrixf (FloatBuffer m) {
		glLoadMatrixf(m);
	}

	public void glLogicOp (int opcode) {
		glLogicOp(opcode);
	}

	public void glMaterialf (int face, int pname, float param) {
		glMaterialf(face, pname, param);
	}

	public void glMaterialfv (int face, int pname, float[] params, int offset) {
		glMaterialfv(face, pname, params, offset);
	}

	public void glMaterialfv (int face, int pname, FloatBuffer params) {
		glMaterialfv(face, pname, params);
	}

	public void glMatrixMode (int mode) {
		glMatrixMode(mode);
	}

	public void glMultMatrixf (float[] m, int offset) {
		glMultMatrixf(m, offset);
	}

	public void glMultMatrixf (FloatBuffer m) {
		glMultMatrixf(m);
	}

	public void glMultiTexCoord4f (int target, float s, float t, float r, float q) {
		glMultiTexCoord4f(target, s, t, r, q);
	}

	public void glNormal3f (float nx, float ny, float nz) {
		glNormal3f(nx, ny, nz);
	}

	public void glNormalPointer (int type, int stride, Buffer pointer) {
		glNormalPointer(type, stride, pointer);
	}

	public void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) {
		glOrthof(left, right, bottom, top, zNear, zFar);
	}

	public void glPointSize (float size) {
		glPointSize(size);
	}

	public void glPopMatrix () {
		glPopMatrix();
	}

	public void glPushMatrix () {
		glPushMatrix();
	}

	public void glRotatef (float angle, float x, float y, float z) {
		glRotatef(angle, x, y, z);
	}

	public void glSampleCoverage (float value, boolean invert) {
		glSampleCoverage(value, invert);
	}

	public void glScalef (float x, float y, float z) {
		glScalef(x, y, z);
	}

	public void glShadeModel (int mode) {
		glShadeModel(mode);
	}

	public void glTexCoordPointer (int size, int type, int stride, Buffer pointer) {
		glTexCoordPointer(size, type, stride, pointer);
	}

	public void glTexEnvf (int target, int pname, float param) {
		glTexEnvf(target, pname, param);
	}

	public void glTexEnvfv (int target, int pname, float[] params, int offset) {
		glTexEnvfv(target, pname, params, offset);
	}

	public void glTexEnvfv (int target, int pname, FloatBuffer params) {
		glTexEnvfv(target, pname, params);
	}

	public void glTranslatef (float x, float y, float z) {
		glTranslatef(x, y, z);
	}

	public void glVertexPointer (int size, int type, int stride, Buffer pointer) {
		glVertexPointer(size, type, stride, pointer);
	}

	public void glPolygonMode (int face, int mode) {
		glPolygonMode(face, mode);
	}
}
