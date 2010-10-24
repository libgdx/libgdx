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

package com.badlogic.gdx.backends.applet;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.badlogic.gdx.graphics.GL20;

/**
 * An implementation of the {@link GL20} interface based on Jogl. Note that Jogl shaders and OpenGL ES shaders will not be 100%
 * compatible. Some glGetXXX methods are not implemented.
 * 
 * @author mzechner
 * 
 */
final class AppletGL20 implements GL20 {
	private final GL gl;

	public AppletGL20 (GL gl) {
		this.gl = gl;
	}

	@Override public void glActiveTexture (int texture) {
		gl.glActiveTexture(texture);
	}

	@Override public void glAttachShader (int program, int shader) {
		gl.glAttachShader(program, shader);
	}

	@Override public void glBindAttribLocation (int program, int index, String name) {
		gl.glBindAttribLocation(program, index, name);
	}

	@Override public void glBindBuffer (int target, int buffer) {
		gl.glBindBuffer(target, buffer);
	}

	@Override public void glBindFramebuffer (int target, int framebuffer) {
		gl.glBindFramebufferEXT(target, framebuffer);
	}

	@Override public void glBindRenderbuffer (int target, int renderbuffer) {
		gl.glBindRenderbufferEXT(target, renderbuffer);
	}

	@Override public void glBindTexture (int target, int texture) {
		gl.glBindTexture(target, texture);
	}

	@Override public void glBlendColor (float red, float green, float blue, float alpha) {
		gl.glBlendColor(red, green, blue, alpha);
	}

	@Override public void glBlendEquation (int mode) {
		gl.glBlendEquation(mode);
	}

	@Override public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		gl.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	@Override public void glBlendFunc (int sfactor, int dfactor) {
		gl.glBlendFunc(sfactor, dfactor);
	}

	@Override public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override public void glBufferData (int target, int size, Buffer data, int usage) {
		gl.glBufferData(target, size, data, usage);
	}

	@Override public void glBufferSubData (int target, int offset, int size, Buffer data) {
		gl.glBufferSubData(target, offset, size, data);
	}

	@Override public int glCheckFramebufferStatus (int target) {
		return gl.glCheckFramebufferStatusEXT(target);
	}

	@Override public void glClear (int mask) {
		gl.glClear(mask);
	}

	@Override public void glClearColor (float red, float green, float blue, float alpha) {
		gl.glClearColor(red, green, blue, alpha);
	}

	@Override public void glClearDepthf (float depth) {
		gl.glClearDepth(depth);
	}

	@Override public void glClearStencil (int s) {
		gl.glClearStencil(s);
	}

	@Override public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		gl.glColorMask(red, green, blue, alpha);
	}

	@Override public void glCompileShader (int shader) {
		gl.glCompileShader(shader);
	}

	@Override public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		gl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height,
		int format, int imageSize, Buffer data) {
		gl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height,
		int border) {
		gl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		gl.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override public int glCreateProgram () {
		return gl.glCreateProgram();
	}

	@Override public int glCreateShader (int type) {
		return gl.glCreateShader(type);
	}

	@Override public void glCullFace (int mode) {
		gl.glCullFace(mode);
	}

	@Override public void glDeleteBuffers (int n, IntBuffer buffers) {
		gl.glDeleteBuffers(n, buffers);
	}

	@Override public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		gl.glDeleteFramebuffersEXT(n, framebuffers);
	}

	@Override public void glDeleteProgram (int program) {
		gl.glDeleteProgram(program);
	}

	@Override public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		gl.glDeleteRenderbuffersEXT(n, renderbuffers);
	}

	@Override public void glDeleteShader (int shader) {
		gl.glDeleteShader(shader);
	}

	@Override public void glDeleteTextures (int n, IntBuffer textures) {
		gl.glDeleteTextures(n, textures);
	}

	@Override public void glDepthFunc (int func) {
		gl.glDepthFunc(func);
	}

	@Override public void glDepthMask (boolean flag) {
		gl.glDepthMask(flag);
	}

	@Override public void glDepthRangef (float zNear, float zFar) {
		gl.glDepthRange(zNear, zFar);
	}

	@Override public void glDetachShader (int program, int shader) {
		gl.glDetachShader(program, shader);
	}

	@Override public void glDisable (int cap) {
		gl.glDisable(cap);
	}

	@Override public void glDisableVertexAttribArray (int index) {
		gl.glDisableVertexAttribArray(index);
	}

	@Override public void glDrawArrays (int mode, int first, int count) {
		gl.glDrawArrays(mode, first, count);
	}

	@Override public void glDrawElements (int mode, int count, int type, Buffer indices) {
		gl.glDrawElements(mode, count, type, indices);
	}

	@Override public void glEnable (int cap) {
		gl.glEnable(cap);
	}

	@Override public void glEnableVertexAttribArray (int index) {
		gl.glEnableVertexAttribArray(index);
	}

	@Override public void glFinish () {
		gl.glFinish();
	}

	@Override public void glFlush () {
		gl.glFlush();
	}

	@Override public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		gl.glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		gl.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
	}

	@Override public void glFrontFace (int mode) {
		gl.glFrontFace(mode);
	}

	@Override public void glGenBuffers (int n, IntBuffer buffers) {
		gl.glGenBuffers(n, buffers);
	}

	@Override public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		gl.glGenFramebuffersEXT(n, framebuffers);
	}

	@Override public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		gl.glGenRenderbuffersEXT(n, renderbuffers);
	}

	@Override public void glGenTextures (int n, IntBuffer textures) {
		gl.glGenTextures(n, textures);
	}

	@Override public void glGenerateMipmap (int target) {
		gl.glGenerateMipmapEXT(target);
	}

	@Override public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		throw new UnsupportedOperationException("not implemented"); // FIXME
	}

	@Override public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		throw new UnsupportedOperationException("not implemented"); // FIXME
	}

	@Override public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		gl.glGetAttachedShaders(program, maxcount, (IntBuffer)count, shaders);
	}

	@Override public int glGetAttribLocation (int program, String name) {
		return gl.glGetAttribLocation(program, name);
	}

	@Override public void glGetBooleanv (int pname, Buffer params) {
		throw new UnsupportedOperationException("not implemented"); // FIXME
	}

	@Override public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		gl.glGetBufferParameteriv(target, pname, params);
	}

	@Override public int glGetError () {
		return gl.glGetError();
	}

	@Override public void glGetFloatv (int pname, FloatBuffer params) {
		gl.glGetFloatv(pname, params);
	}

	@Override public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		gl.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params);
	}

	@Override public void glGetIntegerv (int pname, IntBuffer params) {
		gl.glGetIntegerv(pname, params);
	}

	@Override public String glGetProgramInfoLog (int program) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		gl.glGetProgramInfoLog(program, 1024 * 10, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override public void glGetProgramiv (int program, int pname, IntBuffer params) {
		gl.glGetProgramiv(program, pname, params);
	}

	@Override public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		gl.glGetRenderbufferParameterivEXT(target, pname, params);
	}

	@Override public String glGetShaderInfoLog (int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		gl.glGetShaderInfoLog(shader, 1024 * 10, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		throw new UnsupportedOperationException("unsupported, won't implement.");
	}

	@Override public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		gl.glGetShaderiv(shader, pname, params);
	}

	@Override public String glGetString (int name) {
		return gl.glGetString(name);
	}

	@Override public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		gl.glGetTexParameterfv(target, pname, params);
	}

	@Override public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		gl.glGetTexParameteriv(target, pname, params);
	}

	@Override public int glGetUniformLocation (int program, String name) {
		return gl.glGetUniformLocation(program, name);
	}

	@Override public void glGetUniformfv (int program, int location, FloatBuffer params) {
		gl.glGetUniformfv(program, location, params);
	}

	@Override public void glGetUniformiv (int program, int location, IntBuffer params) {
		gl.glGetUniformiv(program, location, params);
	}

	@Override public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		gl.glGetVertexAttribfv(index, pname, params);
	}

	@Override public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		gl.glGetVertexAttribiv(index, pname, params);
	}

	@Override public void glHint (int target, int mode) {
		gl.glHint(target, mode);
	}

	@Override public boolean glIsBuffer (int buffer) {
		return gl.glIsBuffer(buffer);
	}

	@Override public boolean glIsEnabled (int cap) {
		return gl.glIsEnabled(cap);
	}

	@Override public boolean glIsFramebuffer (int framebuffer) {
		return gl.glIsFramebufferEXT(framebuffer);
	}

	@Override public boolean glIsProgram (int program) {
		return gl.glIsProgram(program);
	}

	@Override public boolean glIsRenderbuffer (int renderbuffer) {
		return gl.glIsRenderbufferEXT(renderbuffer);
	}

	@Override public boolean glIsShader (int shader) {
		return gl.glIsShader(shader);
	}

	@Override public boolean glIsTexture (int texture) {
		return gl.glIsTexture(texture);
	}

	@Override public void glLineWidth (float width) {
		gl.glLineWidth(width);
	}

	@Override public void glLinkProgram (int program) {
		gl.glLinkProgram(program);
	}

	@Override public void glPixelStorei (int pname, int param) {
		gl.glPixelStorei(pname, param);
	}

	@Override public void glPolygonOffset (float factor, float units) {
		gl.glPolygonOffset(factor, units);
	}

	@Override public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		gl.glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override public void glReleaseShaderCompiler () {
		// nothing to do here
	}

	@Override public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		gl.glRenderbufferStorageEXT(target, internalformat, width, height);
	}

	@Override public void glSampleCoverage (float value, boolean invert) {
		gl.glSampleCoverage(value, invert);
	}

	@Override public void glScissor (int x, int y, int width, int height) {
		gl.glScissor(x, y, width, height);
	}

	@Override public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override public void glShaderSource (int shader, String string) {
		gl.glShaderSource(shader, 1, new String[] {string}, null, 0);

	}

	@Override public void glStencilFunc (int func, int ref, int mask) {
		gl.glStencilFunc(func, ref, mask);
	}

	@Override public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		gl.glStencilFuncSeparate(face, func, ref, mask);
	}

	@Override public void glStencilMask (int mask) {
		gl.glStencilMask(mask);
	}

	@Override public void glStencilMaskSeparate (int face, int mask) {
		gl.glStencilMaskSeparate(face, mask);
	}

	@Override public void glStencilOp (int fail, int zfail, int zpass) {
		gl.glStencilOp(fail, zfail, zpass);
	}

	@Override public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		gl.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, Buffer pixels) {
		gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	@Override public void glTexParameterf (int target, int pname, float param) {
		gl.glTexParameterf(target, pname, param);
	}

	@Override public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		gl.glTexParameterfv(target, pname, params);
	}

	@Override public void glTexParameteri (int target, int pname, int param) {
		gl.glTexParameteri(target, pname, param);
	}

	@Override public void glTexParameteriv (int target, int pname, IntBuffer params) {
		gl.glTexParameteriv(target, pname, params);
	}

	@Override public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, Buffer pixels) {
		gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override public void glUniform1f (int location, float x) {
		gl.glUniform1f(location, x);
	}

	@Override public void glUniform1fv (int location, int count, FloatBuffer v) {
		gl.glUniform1fv(location, count, v);
	}

	@Override public void glUniform1i (int location, int x) {
		gl.glUniform1i(location, x);
	}

	@Override public void glUniform1iv (int location, int count, IntBuffer v) {
		gl.glUniform1iv(location, count, v);
	}

	@Override public void glUniform2f (int location, float x, float y) {
		gl.glUniform2f(location, x, y);
	}

	@Override public void glUniform2fv (int location, int count, FloatBuffer v) {
		gl.glUniform2fv(location, count, v);
	}

	@Override public void glUniform2i (int location, int x, int y) {
		gl.glUniform2i(location, x, y);
	}

	@Override public void glUniform2iv (int location, int count, IntBuffer v) {
		gl.glUniform2iv(location, count, v);
	}

	@Override public void glUniform3f (int location, float x, float y, float z) {
		gl.glUniform3f(location, x, y, z);
	}

	@Override public void glUniform3fv (int location, int count, FloatBuffer v) {
		gl.glUniform3fv(location, count, v);
	}

	@Override public void glUniform3i (int location, int x, int y, int z) {
		gl.glUniform3i(location, x, y, z);
	}

	@Override public void glUniform3iv (int location, int count, IntBuffer v) {
		gl.glUniform3iv(location, count, v);
	}

	@Override public void glUniform4f (int location, float x, float y, float z, float w) {
		gl.glUniform4f(location, x, y, z, w);
	}

	@Override public void glUniform4fv (int location, int count, FloatBuffer v) {
		gl.glUniform4fv(location, count, v);
	}

	@Override public void glUniform4i (int location, int x, int y, int z, int w) {
		gl.glUniform4i(location, x, y, z, w);
	}

	@Override public void glUniform4iv (int location, int count, IntBuffer v) {
		gl.glUniform4iv(location, count, v);
	}

	@Override public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		gl.glUniformMatrix2fv(location, count, transpose, value);
	}

	@Override public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		gl.glUniformMatrix3fv(location, count, transpose, value);
	}

	@Override public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		gl.glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override public void glUseProgram (int program) {
		gl.glUseProgram(program);
	}

	@Override public void glValidateProgram (int program) {
		gl.glValidateProgram(program);
	}

	@Override public void glVertexAttrib1f (int indx, float x) {
		gl.glVertexAttrib1f(indx, x);
	}

	@Override public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		gl.glVertexAttrib1fv(indx, values);
	}

	@Override public void glVertexAttrib2f (int indx, float x, float y) {
		gl.glVertexAttrib2f(indx, x, y);
	}

	@Override public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		gl.glVertexAttrib2fv(indx, values);
	}

	@Override public void glVertexAttrib3f (int indx, float x, float y, float z) {
		gl.glVertexAttrib3f(indx, x, y, z);
	}

	@Override public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		gl.glVertexAttrib3fv(indx, values);
	}

	@Override public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		gl.glVertexAttrib4f(indx, x, y, z, w);
	}

	@Override public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		gl.glVertexAttrib4fv(indx, values);
	}

	@Override public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		gl.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override public void glViewport (int x, int y, int width, int height) {
		gl.glViewport(x, y, width, height);
	}

	@Override public void glDrawElements (int mode, int count, int type, int indices) {
		gl.glDrawElements(mode, count, type, indices);
	}

	@Override public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		gl.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

}
