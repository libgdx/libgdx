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

package com.badlogic.gdx.backends.iosrobovm;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;

public class IOSGLES10 implements GL10, GL11 {
	// @off
	/*JNI
	#ifdef __APPLE__
	#include <TargetConditionals.h>
	
	#if TARGET_OS_IPHONE	
	#include <OpenGLES/ES1/gl.h>
	#include <OpenGLES/ES1/glext.h>
	#include <stdio.h>
	*/

	@Override
	public native void glActiveTexture (int texture); /*
		glActiveTexture(texture);
	*/

	@Override
	public native void glBindTexture (int target, int texture); /*
		glBindTexture(target, texture);
	 */

	@Override
	public native void glBlendFunc (int sfactor, int dfactor); /*
		glBlendFunc(sfactor, dfactor);
	 */

	@Override
	public native void glClear (int mask); /*
		glClear(mask);
	 */

	@Override
	public native void glClearColor (float red, float green, float blue, float alpha); /*
		glClearColor(red, green, blue, alpha);
	 */

	@Override
	public native void glClearDepthf (float depth); /*
		glClearDepthf(depth);
	 */

	@Override
	public native void glClearStencil (int s); /*
		glClearStencil(s);
	 */

	@Override
	public native void glColorMask (boolean red, boolean green, boolean blue, boolean alpha); /*
		glColorMask(red, green, blue, alpha);
	 */

	@Override
	public native void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data); /*
		// FIXME
	 */

	@Override
	public native void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data); /*
		// FIXME
	 */

	@Override
	public native void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border); /*
		// FIXME
	 */
	
	@Override
	public native void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);  /*
		glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	 */

	@Override
	public native void glCullFace (int mode); /*
		glCullFace(mode);
	*/

	@Override
	public native void glDeleteTextures (int n, IntBuffer textures);  /*
		// FIXME
	 */
	
	@Override
	public native void glDepthFunc (int func); /*
		glDepthFunc(func);
	 */

	@Override
	public native void glDepthMask (boolean flag);  /*
		glDepthMask(flag);
	 */

	@Override
	public native void glDepthRangef (float zNear, float zFar);  /*
		glDepthRangef(zNear, zFar);
	 */

	@Override
	public native void glDisable (int cap); /*
		glDisable(cap);
	 */

	@Override
	public native void glDrawArrays (int mode, int first, int count); /*
		glDrawArrays(mode, first, count);
	 */

	@Override
	public native void glDrawElements (int mode, int count, int type, Buffer indices); /*
		// FIXME
	 */

	@Override
	public native void glEnable (int cap); /*
		glEnable(cap);
	 */

	@Override
	public native void glFinish (); /*
		glFinish();
	 */

	@Override
	public native void glFlush (); /*
		glFlush();
	 */

	@Override
	public native void glFrontFace (int mode); /*
		glFrontFace(mode);
	 */

	@Override
	public native void glGenTextures (int n, IntBuffer textures); /*
		// FIXME
	 */

	@Override
	public native int glGetError (); /*
		return glGetError();
	 */

	@Override
	public native void glGetIntegerv (int pname, IntBuffer params); /*
		// FIXME
	 */

	@Override
	public native String glGetString (int name); /*
		// FIXME
	 */

	@Override
	public native void glHint (int target, int mode); /*
		glHint(target, mode);
	 */

	@Override
	public native void glLineWidth (float width); /*
		glLineWidth(width);
	 */

	@Override
	public native void glPixelStorei (int pname, int param); /*
		glPixelStorei(pname, param);
 */

	@Override
	public native void glPolygonOffset (float factor, float units); /*
		glPolygonOffset(factor, units);
 */

	@Override
	public native void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels); /*
	// FIXME
 */

	@Override
	public native void glScissor (int x, int y, int width, int height); /*
		glScissor(x, y, width, height);
 */

	@Override
	public native void glStencilFunc (int func, int ref, int mask); /*
		glStencilFunc(func, ref, mask);
 */
	
	@Override
	public native void glStencilMask (int mask); /*
		glStencilMask(mask);
 */

	@Override
	public native void glStencilOp (int fail, int zfail, int zpass); /*
		glStencilOp(fail, zfail, zpass);
 */

	@Override
	public native void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels); /*
	// FIXME
 */

	@Override
	public native void glTexParameterf (int target, int pname, float param); /*
		glTexParameterf(target, pname, param);
 */

	@Override
	public native void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels); /*
	// FIXME
 */

	@Override
	public native void glViewport (int x, int y, int width, int height); /*
		glViewport(x, y, width, height);
 */

	@Override
	public native void glClipPlanef (int plane, float[] equation, int offset); /*
	// FIXME
 */

	@Override
	public native void glClipPlanef (int plane, FloatBuffer equation); /*
	// FIXME
 */

	@Override
	public native void glGetClipPlanef (int pname, float[] eqn, int offset); /*
	// FIXME
 */

	@Override
	public native void glGetClipPlanef (int pname, FloatBuffer eqn); /*
	// FIXME
 */

	@Override
	public native void glGetFloatv (int pname, float[] params, int offset); /*
	// FIXME
 */
	
	@Override
	public native void glGetFloatv (int pname, FloatBuffer params); /*
	// FIXME
 */

	@Override
	public native void glGetLightfv (int light, int pname, float[] params, int offset); /*
	// FIXME
 */

	@Override
	public native void glGetLightfv (int light, int pname, FloatBuffer params); /*
	// FIXME
 */

	@Override
	public native void glGetMaterialfv (int face, int pname, float[] params, int offset); /*
	// FIXME
 */

	@Override
	public native void glGetMaterialfv (int face, int pname, FloatBuffer params); /*
	// FIXME
 */

	@Override
	public native void glGetTexParameterfv (int target, int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetTexParameterfv (int target, int pname, FloatBuffer params); /*
	// FIXME
 */

	@Override
	public native void glPointParameterf (int pname, float param) ; /*
	// FIXME
 */

	@Override
	public native void glPointParameterfv (int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glPointParameterfv (int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glTexParameterfv (int target, int pname, float[] params, int offset); /*
	// FIXME
 */

	@Override
	public native void glTexParameterfv (int target, int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glBindBuffer (int target, int buffer) ; /*
	// FIXME
 */

	@Override
	public native void glBufferData (int target, int size, Buffer data, int usage) ; /*
	// FIXME
 */

	@Override
	public native void glBufferSubData (int target, int offset, int size, Buffer data) ; /*
	// FIXME
 */

	@Override
	public native void glColor4ub (byte red, byte green, byte blue, byte alpha) ; /*
	// FIXME
 */

	@Override
	public native void glDeleteBuffers (int n, int[] buffers, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glDeleteBuffers (int n, IntBuffer buffers) ; /*
	// FIXME
 */

	@Override
	public native void glGetBooleanv (int pname, boolean[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetBooleanv (int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glGetBufferParameteriv (int target, int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetBufferParameteriv (int target, int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glGenBuffers (int n, int[] buffers, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGenBuffers (int n, IntBuffer buffers) ; /*
	// FIXME
 */

	@Override
	public void glGetPointerv (int pname, Buffer[] params) {
		// FIXME
	}

	@Override
	public native void glGetTexEnviv (int envi, int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetTexEnviv (int envi, int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glGetTexParameteriv (int target, int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetTexParameteriv (int target, int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native boolean glIsBuffer (int buffer) ; /*
		return glIsBuffer(buffer);
 */

	@Override
	public native boolean glIsEnabled (int cap) ; /*
		return glIsEnabled(cap);
 */

	@Override
	public native boolean glIsTexture (int texture) ; /*
		glIsTexture(texture);
 */

	@Override
	public native void glTexEnvi (int target, int pname, int param) ; /*
		glTexEnvi(target, pname, param);
 */

	@Override
	public native void glTexEnviv (int target, int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glTexEnviv (int target, int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glTexParameteri (int target, int pname, int param) ; /*
		glTexParameteri(target, pname, param);
 */

	@Override
	public native void glTexParameteriv (int target, int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glTexParameteriv (int target, int pname, IntBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glPointSizePointerOES (int type, int stride, Buffer pointer) ; /*
	// FIXME
 */

	@Override
	public native void glVertexPointer (int size, int type, int stride, int pointer) ; /*
		glVertexPointer(size, type, stride, (void*)pointer);
 */

	@Override
	public native void glColorPointer (int size, int type, int stride, int pointer) ; /*
		glColorPointer(size, type, stride, (void*)pointer);
 */

	@Override
	public native void glNormalPointer (int type, int stride, int pointer); /*
		glNormalPointer(type, stride, (void*)pointer);
 */

	@Override
	public native void glTexCoordPointer (int size, int type, int stride, int pointer); /*
		glTexCoordPointer(size, type, stride, (void*)pointer);
 */

	@Override
	public native void glDrawElements (int mode, int count, int type, int indices) ; /*
		glDrawElements(mode, count, type, (void*)indices);
 */
	
	@Override
	public native void glAlphaFunc (int func, float ref) ; /*
		glAlphaFunc(func, ref);
 */

	@Override
	public native void glClientActiveTexture (int texture) ; /*
		glClientActiveTexture(texture);
 */

	@Override
	public native void glColor4f (float red, float green, float blue, float alpha) ; /*
		glColor4f(red, green, blue, alpha);
 */

	@Override
	public native void glColorPointer (int size, int type, int stride, Buffer pointer) ; /*
		glColorPointer(size, type, stride, pointer);
 */

	@Override
	public native void glDeleteTextures (int n, int[] textures, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glDisableClientState (int array) ; /*
		glDisableClientState(array);
 */

	@Override
	public native void glEnableClientState (int array) ; /*
		glEnableClientState(array);
 */

	@Override
	public native void glFogf (int pname, float param) ; /*
		glFogf(pname, param);
 */

	@Override
	public native void glFogfv (int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glFogfv (int pname, FloatBuffer params); /*
	// FIXME
 */

	@Override
	public native void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar) ; /*
		glFrustumf(left, right, bottom, top, zNear, zFar);
 */

	@Override
	public native void glGenTextures (int n, int[] textures, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glGetIntegerv (int pname, int[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glLightModelf (int pname, float param) ; /*
		glLightModelf(pname, param);
 */

	@Override
	public native void glLightModelfv (int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glLightModelfv (int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glLightf (int light, int pname, float param) ; /*
		glLightf(light, pname, param);
 */

	@Override
	public native void glLightfv (int light, int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glLightfv (int light, int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glLoadIdentity () ; /*
		glLoadIdentity();
 */

	@Override
	public native void glLoadMatrixf (float[] m, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glLoadMatrixf (FloatBuffer m) ; /*
	// FIXME
 */

	@Override
	public native void glLogicOp (int opcode) ; /*
		glLogicOp(opcode);
 */

	@Override
	public native void glMaterialf (int face, int pname, float param) ; /*
		glMaterialf(face, pname, param);
 */

	@Override
	public native void glMaterialfv (int face, int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glMaterialfv (int face, int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glMatrixMode (int mode) ; /*
		glMatrixMode(mode);
 */

	@Override
	public native void glMultMatrixf (float[] m, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glMultMatrixf (FloatBuffer m) ; /*
	// FIXME
 */

	@Override
	public native void glMultiTexCoord4f (int target, float s, float t, float r, float q) ; /*
		glMultiTexCoord4f(target, s, t, r, q);
 */

	@Override
	public native void glNormal3f (float nx, float ny, float nz) ; /*
		glNormal3f(nx, ny, nz);
 */

	@Override
	public native void glNormalPointer (int type, int stride, Buffer pointer) ; /*
	// FIXME
 */

	@Override
	public native void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar) ; /*
		glOrthof(left, right, bottom, top, zNear, zFar);
 */

	@Override
	public native void glPointSize (float size) ; /*
		glPointSize(size);
 */

	@Override
	public native void glPopMatrix () ; /*
		glPopMatrix();
 */

	@Override
	public native void glPushMatrix () ; /*
		glPushMatrix();
 */

	@Override
	public native void glRotatef (float angle, float x, float y, float z) ; /*
		glRotatef(angle, x, y, z);
 */

	@Override
	public native void glSampleCoverage (float value, boolean invert) ; /*
	// FIXME
 */

	@Override
	public native void glScalef (float x, float y, float z) ; /*
		glScalef(x, y, z);
 */

	@Override
	public native void glShadeModel (int mode) ; /*
		glShadeModel(mode);
 */

	@Override
	public native void glTexCoordPointer (int size, int type, int stride, Buffer pointer) ; /*
	// FIXME
 */

	@Override
	public native void glTexEnvf (int target, int pname, float param) ; /*
		glTexEnvf(target, pname, param);
 */

	@Override
	public native void glTexEnvfv (int target, int pname, float[] params, int offset) ; /*
	// FIXME
 */

	@Override
	public native void glTexEnvfv (int target, int pname, FloatBuffer params) ; /*
	// FIXME
 */

	@Override
	public native void glTranslatef (float x, float y, float z) ; /*
		glTranslatef(x, y, z);
 */

	@Override
	public native void glVertexPointer (int size, int type, int stride, Buffer pointer) ; /*
	// FIXME
 */

	@Override
	public native void glPolygonMode (int face, int mode) ; /*		
 */
	// @off
	/*JNI
	#endif
	#endif
	*/
}