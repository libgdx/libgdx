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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implementation of the {@link GL20} interface based on JoGLContext.getCurrentGL(). Note that Jogl shaders and OpenGL ES shaders will not be 100%
 * compatible. Some glGetXXX methods are not implemented.
 * 
 * @author mzechner */
final class JoglGL20 implements GL20 {

	public JoglGL20 () {
	}

	@Override
	public void glActiveTexture (int texture) {
		GLContext.getCurrentGL().glActiveTexture(texture);
	}

	@Override
	public void glAttachShader (int program, int shader) {
		GLContext.getCurrentGL().getGL2ES2().glAttachShader(program, shader);
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		GLContext.getCurrentGL().getGL2ES2().glBindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		GLContext.getCurrentGL().glBindBuffer(target, buffer);
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		GLContext.getCurrentGL().glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		GLContext.getCurrentGL().glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public void glBindTexture (int target, int texture) {
		GLContext.getCurrentGL().glBindTexture(target, texture);
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().getGL2ES2().glBlendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation (int mode) {
		GLContext.getCurrentGL().glBlendEquation(mode);
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GLContext.getCurrentGL().glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		GLContext.getCurrentGL().glBlendFunc(sfactor, dfactor);
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLContext.getCurrentGL().glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
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
	public int glCheckFramebufferStatus (int target) {
		return GLContext.getCurrentGL().glCheckFramebufferStatus(target);
	}

	@Override
	public void glClear (int mask) {
		GLContext.getCurrentGL().glClear(mask);
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		GLContext.getCurrentGL().glClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf (float depth) {
		GLContext.getCurrentGL().glClearDepth(depth);
	}

	@Override
	public void glClearStencil (int s) {
		GLContext.getCurrentGL().glClearStencil(s);
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GLContext.getCurrentGL().glColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompileShader (int shader) {
		GLContext.getCurrentGL().getGL2ES2().glCompileShader(shader);
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GLContext.getCurrentGL().glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GLContext.getCurrentGL().glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GLContext.getCurrentGL().glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GLContext.getCurrentGL().glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public int glCreateProgram () {
		return GLContext.getCurrentGL().getGL2ES2().glCreateProgram();
	}

	@Override
	public int glCreateShader (int type) {
		return GLContext.getCurrentGL().getGL2ES2().glCreateShader(type);
	}

	@Override
	public void glCullFace (int mode) {
		GLContext.getCurrentGL().glCullFace(mode);
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GLContext.getCurrentGL().glDeleteBuffers(n, buffers);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GLContext.getCurrentGL().glDeleteFramebuffers(n, framebuffers);
	}

	@Override
	public void glDeleteProgram (int program) {
		GLContext.getCurrentGL().getGL2ES2().glDeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GLContext.getCurrentGL().glDeleteRenderbuffers(n, renderbuffers);
	}

	@Override
	public void glDeleteShader (int shader) {
		GLContext.getCurrentGL().getGL2ES2().glDeleteShader(shader);
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		GLContext.getCurrentGL().glDeleteTextures(n, textures);
	}

	@Override
	public void glDepthFunc (int func) {
		GLContext.getCurrentGL().glDepthFunc(func);
	}

	@Override
	public void glDepthMask (boolean flag) {
		GLContext.getCurrentGL().glDepthMask(flag);
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		GLContext.getCurrentGL().glDepthRange(zNear, zFar);
	}

	@Override
	public void glDetachShader (int program, int shader) {
		GLContext.getCurrentGL().getGL2ES2().glDetachShader(program, shader);
	}

	@Override
	public void glDisable (int cap) {
		GLContext.getCurrentGL().glDisable(cap);
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		GLContext.getCurrentGL().getGL2ES2().glDisableVertexAttribArray(index);
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		GLContext.getCurrentGL().glDrawArrays(mode, first, count);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		GLContext.getCurrentGL().glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glEnable (int cap) {
		GLContext.getCurrentGL().glEnable(cap);
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		GLContext.getCurrentGL().getGL2ES2().glEnableVertexAttribArray(index);
	}

	@Override
	public void glFinish () {
		GLContext.getCurrentGL().glFinish();
	}

	@Override
	public void glFlush () {
		GLContext.getCurrentGL().glFlush();
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLContext.getCurrentGL().glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GLContext.getCurrentGL().glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFrontFace (int mode) {
		GLContext.getCurrentGL().glFrontFace(mode);
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		GLContext.getCurrentGL().glGenBuffers(n, buffers);
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GLContext.getCurrentGL().glGenFramebuffers(n, framebuffers);
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GLContext.getCurrentGL().glGenRenderbuffers(n, renderbuffers);
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		GLContext.getCurrentGL().glGenTextures(n, textures);
	}

	@Override
	public void glGenerateMipmap (int target) {
		GLContext.getCurrentGL().glGenerateMipmap(target);
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		int[] length = new int[1];
		int[] sizeTmp = new int[2];
		int[] typeTmp = new int[1];
		byte[] name = new byte[256];
		GLContext.getCurrentGL().getGL2ES2().glGetActiveAttrib(program, index, 256, length, 0, sizeTmp, 0, typeTmp, 0, name, 0);

		size.put(sizeTmp[0]);
		if (type instanceof IntBuffer) ((IntBuffer)type).put(typeTmp[0]);
		return new String(name, 0, length[0]);
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		int[] length = new int[1];
		int[] sizeTmp = new int[2];
		int[] typeTmp = new int[1];
		byte[] name = new byte[256];
		GLContext.getCurrentGL().getGL2ES2().glGetActiveUniform(program, index, 256, length, 0, sizeTmp, 0, typeTmp, 0, name, 0);

		size.put(sizeTmp[0]);
		if (type instanceof IntBuffer) ((IntBuffer)type).put(typeTmp[0]);
		return new String(name, 0, length[0]);
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GLContext.getCurrentGL().getGL2ES2().glGetAttachedShaders(program, maxcount, (IntBuffer)count, shaders);
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		return GLContext.getCurrentGL().getGL2ES2().glGetAttribLocation(program, name);
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		if (!(params instanceof ByteBuffer)) throw new GdxRuntimeException("params must be a direct ByteBuffer");
		GLContext.getCurrentGL().glGetBooleanv(pname, (ByteBuffer)params);
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetBufferParameteriv(target, pname, params);
	}

	@Override
	public int glGetError () {
		return GLContext.getCurrentGL().glGetError();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		GLContext.getCurrentGL().glGetFloatv(pname, params);
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetIntegerv(pname, params);
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLContext.getCurrentGL().getGL2ES2().glGetProgramInfoLog(program, 1024 * 10, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetProgramiv(program, pname, params);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLContext.getCurrentGL().getGL2ES2().glGetShaderInfoLog(shader, 1024 * 10, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		throw new UnsupportedOperationException("unsupported, won't implement.");
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetShaderiv(shader, pname, params);
	}

	@Override
	public String glGetString (int name) {
		return GLContext.getCurrentGL().glGetString(name);
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().glGetTexParameterfv(target, pname, params);
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glGetTexParameteriv(target, pname, params);
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		return GLContext.getCurrentGL().getGL2ES2().glGetUniformLocation(program, name);
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetUniformfv(program, location, params);
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetUniformiv(program, location, params);
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetVertexAttribfv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GLContext.getCurrentGL().getGL2ES2().glGetVertexAttribiv(index, pname, params);
	}

	@Override
	public void glHint (int target, int mode) {
		GLContext.getCurrentGL().glHint(target, mode);
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
	public boolean glIsFramebuffer (int framebuffer) {
		return GLContext.getCurrentGL().glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsProgram (int program) {
		return GLContext.getCurrentGL().getGL2ES2().glIsProgram(program);
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return GLContext.getCurrentGL().glIsRenderbuffer(renderbuffer);
	}

	@Override
	public boolean glIsShader (int shader) {
		return GLContext.getCurrentGL().getGL2ES2().glIsShader(shader);
	}

	@Override
	public boolean glIsTexture (int texture) {
		return GLContext.getCurrentGL().glIsTexture(texture);
	}

	@Override
	public void glLineWidth (float width) {
		GLContext.getCurrentGL().glLineWidth(width);
	}

	@Override
	public void glLinkProgram (int program) {
		GLContext.getCurrentGL().getGL2ES2().glLinkProgram(program);
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		GLContext.getCurrentGL().glPixelStorei(pname, param);
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		GLContext.getCurrentGL().glPolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GLContext.getCurrentGL().glReadPixels(x, y, width, height, format, type, pixels);
	}

	@Override
	public void glReleaseShaderCompiler () {
		// nothing to do here
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GLContext.getCurrentGL().glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		GLContext.getCurrentGL().glSampleCoverage(value, invert);
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		GLContext.getCurrentGL().glScissor(x, y, width, height);
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	@Override
	public void glShaderSource (int shader, String string) {
		GLContext.getCurrentGL().getGL2ES2().glShaderSource(shader, 1, new String[] {string}, null, 0);

	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		GLContext.getCurrentGL().glStencilFunc(func, ref, mask);
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GLContext.getCurrentGL().getGL2ES2().glStencilFuncSeparate(face, func, ref, mask);
	}

	@Override
	public void glStencilMask (int mask) {
		GLContext.getCurrentGL().glStencilMask(mask);
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		GLContext.getCurrentGL().getGL2ES2().glStencilMaskSeparate(face, mask);
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		GLContext.getCurrentGL().glStencilOp(fail, zfail, zpass);
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GLContext.getCurrentGL().getGL2ES2().glStencilOpSeparate(face, fail, zfail, zpass);
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		GLContext.getCurrentGL().glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		GLContext.getCurrentGL().glTexParameterf(target, pname, param);
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
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GLContext.getCurrentGL().glTexParameteriv(target, pname, params);
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		GLContext.getCurrentGL().glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	@Override
	public void glUniform1f (int location, float x) {
		GLContext.getCurrentGL().getGL2ES2().glUniform1f(location, x);
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform1fv(location, count, v);
	}

	@Override
	public void glUniform1i (int location, int x) {
		GLContext.getCurrentGL().getGL2ES2().glUniform1i(location, x);
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform1iv(location, count, v);
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		GLContext.getCurrentGL().getGL2ES2().glUniform2f(location, x, y);
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform2fv(location, count, v);
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		GLContext.getCurrentGL().getGL2ES2().glUniform2i(location, x, y);
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform2iv(location, count, v);
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES2().glUniform3f(location, x, y, z);
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform3fv(location, count, v);
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		GLContext.getCurrentGL().getGL2ES2().glUniform3i(location, x, y, z);
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform3iv(location, count, v);
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		GLContext.getCurrentGL().getGL2ES2().glUniform4f(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform4fv(location, count, v);
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		GLContext.getCurrentGL().getGL2ES2().glUniform4i(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		GLContext.getCurrentGL().getGL2ES2().glUniform4iv(location, count, v);
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLContext.getCurrentGL().getGL2ES2().glUniformMatrix2fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLContext.getCurrentGL().getGL2ES2().glUniformMatrix3fv(location, count, transpose, value);
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLContext.getCurrentGL().getGL2ES2().glUniformMatrix4fv(location, count, transpose, value);
	}

	@Override
	public void glUseProgram (int program) {
		GLContext.getCurrentGL().getGL2ES2().glUseProgram(program);
	}

	@Override
	public void glValidateProgram (int program) {
		GLContext.getCurrentGL().getGL2ES2().glValidateProgram(program);
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib1f(indx, x);
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib1fv(indx, values);
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib2f(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib2fv(indx, values);
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib3f(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib3fv(indx, values);
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib4f(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttrib4fv(indx, values);
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		GLContext.getCurrentGL().glViewport(x, y, width, height);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		GLContext.getCurrentGL().glDrawElements(mode, count, type, indices);
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GLContext.getCurrentGL().getGL2ES2().glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}

}
