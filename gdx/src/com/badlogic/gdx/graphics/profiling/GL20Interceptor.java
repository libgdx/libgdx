/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.graphics.profiling;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL20;

/** @author Daniel Holderbaum
 * @author Jan Polák */
public class GL20Interceptor extends GLInterceptor implements GL20 {

	protected final GL20 gl20;

	protected GL20Interceptor (GLProfiler glProfiler, GL20 gl20) {
		super(glProfiler);
		this.gl20 = gl20;
	}
	
	private void check () {
		int error = gl20.glGetError();
		while (error != GL20.GL_NO_ERROR) {
			glProfiler.getListener().onError(error);
			error = gl20.glGetError();
		}
	}

	@Override
	public void glActiveTexture (int texture) {
		calls++;
		gl20.glActiveTexture(texture);
		check();
	}

	@Override
	public void glBindTexture (int target, int texture) {
		textureBindings++;
		calls++;
		gl20.glBindTexture(target, texture);
		check();
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		calls++;
		gl20.glBlendFunc(sfactor, dfactor);
		check();
	}

	@Override
	public void glClear (int mask) {
		calls++;
		gl20.glClear(mask);
		check();
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		calls++;
		gl20.glClearColor(red, green, blue, alpha);
		check();
	}

	@Override
	public void glClearDepthf (float depth) {
		calls++;
		gl20.glClearDepthf(depth);
		check();
	}

	@Override
	public void glClearStencil (int s) {
		calls++;
		gl20.glClearStencil(s);
		check();
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		calls++;
		gl20.glColorMask(red, green, blue, alpha);
		check();
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		calls++;
		gl20.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
		check();
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		calls++;
		gl20.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
		check();
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		calls++;
		gl20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
		check();
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		calls++;
		gl20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
		check();
	}

	@Override
	public void glCullFace (int mode) {
		calls++;
		gl20.glCullFace(mode);
		check();
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		calls++;
		gl20.glDeleteTextures(n, textures);
		check();
	}

	@Override
	public void glDeleteTexture (int texture) {
		calls++;
		gl20.glDeleteTexture(texture);
		check();
	}

	@Override
	public void glDepthFunc (int func) {
		calls++;
		gl20.glDepthFunc(func);
		check();
	}

	@Override
	public void glDepthMask (boolean flag) {
		calls++;
		gl20.glDepthMask(flag);
		check();
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		calls++;
		gl20.glDepthRangef(zNear, zFar);
		check();
	}

	@Override
	public void glDisable (int cap) {
		calls++;
		gl20.glDisable(cap);
		check();
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl20.glDrawArrays(mode, first, count);
		check();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl20.glDrawElements(mode, count, type, indices);
		check();
	}

	@Override
	public void glEnable (int cap) {
		calls++;
		gl20.glEnable(cap);
		check();
	}

	@Override
	public void glFinish () {
		calls++;
		gl20.glFinish();
		check();
	}

	@Override
	public void glFlush () {
		calls++;
		gl20.glFlush();
		check();
	}

	@Override
	public void glFrontFace (int mode) {
		calls++;
		gl20.glFrontFace(mode);
		check();
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		calls++;
		gl20.glGenTextures(n, textures);
		check();
	}

	@Override
	public int glGenTexture () {
		calls++;
		int result = gl20.glGenTexture();
		check();
		return result;
	}

	@Override
	public int glGetError () {
		calls++;
		//Errors by glGetError are undetectable
		return gl20.glGetError();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		calls++;
		gl20.glGetIntegerv(pname, params);
		check();
	}

	@Override
	public String glGetString (int name) {
		calls++;
		String result = gl20.glGetString(name);
		check();
		return result;
	}

	@Override
	public void glHint (int target, int mode) {
		calls++;
		gl20.glHint(target, mode);
		check();
	}

	@Override
	public void glLineWidth (float width) {
		calls++;
		gl20.glLineWidth(width);
		check();
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		calls++;
		gl20.glPixelStorei(pname, param);
		check();
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		calls++;
		gl20.glPolygonOffset(factor, units);
		check();
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		calls++;
		gl20.glReadPixels(x, y, width, height, format, type, pixels);
		check();
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		calls++;
		gl20.glScissor(x, y, width, height);
		check();
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		calls++;
		gl20.glStencilFunc(func, ref, mask);
		check();
	}

	@Override
	public void glStencilMask (int mask) {
		calls++;
		gl20.glStencilMask(mask);
		check();
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		calls++;
		gl20.glStencilOp(fail, zfail, zpass);
		check();
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		calls++;
		gl20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		check();
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		calls++;
		gl20.glTexParameterf(target, pname, param);
		check();
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		calls++;
		gl20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
		check();
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		calls++;
		gl20.glViewport(x, y, width, height);
		check();
	}

	@Override
	public void glAttachShader (int program, int shader) {
		calls++;
		gl20.glAttachShader(program, shader);
		check();
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		calls++;
		gl20.glBindAttribLocation(program, index, name);
		check();
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		calls++;
		gl20.glBindBuffer(target, buffer);
		check();
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		calls++;
		gl20.glBindFramebuffer(target, framebuffer);
		check();
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		calls++;
		gl20.glBindRenderbuffer(target, renderbuffer);
		check();
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		calls++;
		gl20.glBlendColor(red, green, blue, alpha);
		check();
	}

	@Override
	public void glBlendEquation (int mode) {
		calls++;
		gl20.glBlendEquation(mode);
		check();
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		calls++;
		gl20.glBlendEquationSeparate(modeRGB, modeAlpha);
		check();
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		calls++;
		gl20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
		check();
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		calls++;
		gl20.glBufferData(target, size, data, usage);
		check();
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		calls++;
		gl20.glBufferSubData(target, offset, size, data);
		check();
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		calls++;
		int result = gl20.glCheckFramebufferStatus(target);
		check();
		return result;
	}

	@Override
	public void glCompileShader (int shader) {
		calls++;
		gl20.glCompileShader(shader);
		check();
	}

	@Override
	public int glCreateProgram () {
		calls++;
		int result = gl20.glCreateProgram();
		check();
		return result;
	}

	@Override
	public int glCreateShader (int type) {
		calls++;
		int result = gl20.glCreateShader(type);
		check();
		return result;
	}

	@Override
	public void glDeleteBuffer (int buffer) {
		calls++;
		gl20.glDeleteBuffer(buffer);
		check();
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		calls++;
		gl20.glDeleteBuffers(n, buffers);
		check();
	}

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		calls++;
		gl20.glDeleteFramebuffer(framebuffer);
		check();
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		calls++;
		gl20.glDeleteFramebuffers(n, framebuffers);
		check();
	}

	@Override
	public void glDeleteProgram (int program) {
		calls++;
		gl20.glDeleteProgram(program);
		check();
	}

	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		calls++;
		gl20.glDeleteRenderbuffer(renderbuffer);
		check();
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		calls++;
		gl20.glDeleteRenderbuffers(n, renderbuffers);
		check();
	}

	@Override
	public void glDeleteShader (int shader) {
		calls++;
		gl20.glDeleteShader(shader);
		check();
	}

	@Override
	public void glDetachShader (int program, int shader) {
		calls++;
		gl20.glDetachShader(program, shader);
		check();
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		calls++;
		gl20.glDisableVertexAttribArray(index);
		check();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl20.glDrawElements(mode, count, type, indices);
		check();
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		calls++;
		gl20.glEnableVertexAttribArray(index);
		check();
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		calls++;
		gl20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
		check();
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		calls++;
		gl20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
		check();
	}

	@Override
	public int glGenBuffer () {
		calls++;
		int result = gl20.glGenBuffer();
		check();
		return result;
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		calls++;
		gl20.glGenBuffers(n, buffers);
		check();
	}

	@Override
	public void glGenerateMipmap (int target) {
		calls++;
		gl20.glGenerateMipmap(target);
		check();
	}

	@Override
	public int glGenFramebuffer () {
		calls++;
		int result = gl20.glGenFramebuffer();
		check();
		return result;
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		calls++;
		gl20.glGenFramebuffers(n, framebuffers);
		check();
	}

	@Override
	public int glGenRenderbuffer () {
		calls++;
		int result = gl20.glGenRenderbuffer();
		check();
		return result;
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		calls++;
		gl20.glGenRenderbuffers(n, renderbuffers);
		check();
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		calls++;
		String result = gl20.glGetActiveAttrib(program, index, size, type);
		check();
		return result;
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		calls++;
		String result = gl20.glGetActiveUniform(program, index, size, type);
		check();
		return result;
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		calls++;
		gl20.glGetAttachedShaders(program, maxcount, count, shaders);
		check();
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		calls++;
		int result = gl20.glGetAttribLocation(program, name);
		check();
		return result;
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		calls++;
		gl20.glGetBooleanv(pname, params);
		check();
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl20.glGetBufferParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		calls++;
		gl20.glGetFloatv(pname, params);
		check();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		calls++;
		gl20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
		check();
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		calls++;
		gl20.glGetProgramiv(program, pname, params);
		check();
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		calls++;
		String result = gl20.glGetProgramInfoLog(program);
		check();
		return result;
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl20.glGetRenderbufferParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		calls++;
		gl20.glGetShaderiv(shader, pname, params);
		check();
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		calls++;
		String result = gl20.glGetShaderInfoLog(shader);
		check();
		return result;
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		calls++;
		gl20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
		check();
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		calls++;
		gl20.glGetTexParameterfv(target, pname, params);
		check();
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl20.glGetTexParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		calls++;
		gl20.glGetUniformfv(program, location, params);
		check();
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		calls++;
		gl20.glGetUniformiv(program, location, params);
		check();
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		calls++;
		int result = gl20.glGetUniformLocation(program, name);
		check();
		return result;
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		calls++;
		gl20.glGetVertexAttribfv(index, pname, params);
		check();
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		calls++;
		gl20.glGetVertexAttribiv(index, pname, params);
		check();
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		calls++;
		gl20.glGetVertexAttribPointerv(index, pname, pointer);
		check();
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		calls++;
		boolean result = gl20.glIsBuffer(buffer);
		check();
		return result;
	}

	@Override
	public boolean glIsEnabled (int cap) {
		calls++;
		boolean result = gl20.glIsEnabled(cap);
		check();
		return result;
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		calls++;
		boolean result = gl20.glIsFramebuffer(framebuffer);
		check();
		return result;
	}

	@Override
	public boolean glIsProgram (int program) {
		calls++;
		boolean result = gl20.glIsProgram(program);
		check();
		return result;
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		calls++;
		boolean result = gl20.glIsRenderbuffer(renderbuffer);
		check();
		return result;
	}

	@Override
	public boolean glIsShader (int shader) {
		calls++;
		boolean result = gl20.glIsShader(shader);
		check();
		return result;
	}

	@Override
	public boolean glIsTexture (int texture) {
		calls++;
		boolean result = gl20.glIsTexture(texture);
		check();
		return result;
	}

	@Override
	public void glLinkProgram (int program) {
		calls++;
		gl20.glLinkProgram(program);
		check();
	}

	@Override
	public void glReleaseShaderCompiler () {
		calls++;
		gl20.glReleaseShaderCompiler();
		check();
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		calls++;
		gl20.glRenderbufferStorage(target, internalformat, width, height);
		check();
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		calls++;
		gl20.glSampleCoverage(value, invert);
		check();
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		calls++;
		gl20.glShaderBinary(n, shaders, binaryformat, binary, length);
		check();
	}

	@Override
	public void glShaderSource (int shader, String string) {
		calls++;
		gl20.glShaderSource(shader, string);
		check();
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		calls++;
		gl20.glStencilFuncSeparate(face, func, ref, mask);
		check();
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		calls++;
		gl20.glStencilMaskSeparate(face, mask);
		check();
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		calls++;
		gl20.glStencilOpSeparate(face, fail, zfail, zpass);
		check();
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		calls++;
		gl20.glTexParameterfv(target, pname, params);
		check();
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		calls++;
		gl20.glTexParameteri(target, pname, param);
		check();
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl20.glTexParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glUniform1f (int location, float x) {
		calls++;
		gl20.glUniform1f(location, x);
		check();
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		calls++;
		gl20.glUniform1fv(location, count, v);
		check();
	}

	@Override
	public void glUniform1fv (int location, int count, float[] v, int offset) {
		calls++;
		gl20.glUniform1fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform1i (int location, int x) {
		calls++;
		gl20.glUniform1i(location, x);
		check();
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		calls++;
		gl20.glUniform1iv(location, count, v);
		check();
	}

	@Override
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		calls++;
		gl20.glUniform1iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		calls++;
		gl20.glUniform2f(location, x, y);
		check();
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		calls++;
		gl20.glUniform2fv(location, count, v);
		check();
	}

	@Override
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		calls++;
		gl20.glUniform2fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		calls++;
		gl20.glUniform2i(location, x, y);
		check();
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		calls++;
		gl20.glUniform2iv(location, count, v);
		check();
	}

	@Override
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		calls++;
		gl20.glUniform2iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		calls++;
		gl20.glUniform3f(location, x, y, z);
		check();
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		calls++;
		gl20.glUniform3fv(location, count, v);
		check();
	}

	@Override
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		calls++;
		gl20.glUniform3fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		calls++;
		gl20.glUniform3i(location, x, y, z);
		check();
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		calls++;
		gl20.glUniform3iv(location, count, v);
		check();
	}

	@Override
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		calls++;
		gl20.glUniform3iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		calls++;
		gl20.glUniform4f(location, x, y, z, w);
		check();
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		calls++;
		gl20.glUniform4fv(location, count, v);
		check();
	}

	@Override
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		calls++;
		gl20.glUniform4fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		calls++;
		gl20.glUniform4i(location, x, y, z, w);
		check();
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		calls++;
		gl20.glUniform4iv(location, count, v);
		check();
	}

	@Override
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		calls++;
		gl20.glUniform4iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl20.glUniformMatrix2fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl20.glUniformMatrix2fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl20.glUniformMatrix3fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl20.glUniformMatrix3fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl20.glUniformMatrix4fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl20.glUniformMatrix4fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUseProgram (int program) {
		shaderSwitches++;
		calls++;
		gl20.glUseProgram(program);
		check();
	}

	@Override
	public void glValidateProgram (int program) {
		calls++;
		gl20.glValidateProgram(program);
		check();
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		calls++;
		gl20.glVertexAttrib1f(indx, x);
		check();
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		calls++;
		gl20.glVertexAttrib1fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		calls++;
		gl20.glVertexAttrib2f(indx, x, y);
		check();
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		calls++;
		gl20.glVertexAttrib2fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		calls++;
		gl20.glVertexAttrib3f(indx, x, y, z);
		check();
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		calls++;
		gl20.glVertexAttrib3fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		calls++;
		gl20.glVertexAttrib4f(indx, x, y, z, w);
		check();
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		calls++;
		gl20.glVertexAttrib4fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		calls++;
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		check();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		calls++;
		gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		check();
	}
}
