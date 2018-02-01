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
import java.nio.LongBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;

/** @author Daniel Holderbaum
 * @author Jan Polák */
public class GL30Interceptor extends GLInterceptor implements GL30 {

	protected final GL30 gl30;

	protected GL30Interceptor (GLProfiler glProfiler, GL30 gl30) {
		super(glProfiler);
		this.gl30 = gl30;
	}
	
	private void check () {
		int error = gl30.glGetError();
		while (error != GL20.GL_NO_ERROR) {
			glProfiler.getListener().onError(error);
			error = gl30.glGetError();
		}
	}

	@Override
	public void glActiveTexture (int texture) {
		calls++;
		gl30.glActiveTexture(texture);
		check();
	}

	@Override
	public void glBindTexture (int target, int texture) {
		textureBindings++;
		calls++;
		gl30.glBindTexture(target, texture);
		check();
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		calls++;
		gl30.glBlendFunc(sfactor, dfactor);
		check();
	}

	@Override
	public void glClear (int mask) {
		calls++;
		gl30.glClear(mask);
		check();
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		calls++;
		gl30.glClearColor(red, green, blue, alpha);
		check();
	}

	@Override
	public void glClearDepthf (float depth) {
		calls++;
		gl30.glClearDepthf(depth);
		check();
	}

	@Override
	public void glClearStencil (int s) {
		calls++;
		gl30.glClearStencil(s);
		check();
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		calls++;
		gl30.glColorMask(red, green, blue, alpha);
		check();
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		calls++;
		gl30.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
		check();
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		calls++;
		gl30.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
		check();
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		calls++;
		gl30.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
		check();
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		calls++;
		gl30.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
		check();
	}

	@Override
	public void glCullFace (int mode) {
		calls++;
		gl30.glCullFace(mode);
		check();
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		calls++;
		gl30.glDeleteTextures(n, textures);
		check();
	}

	@Override
	public void glDeleteTexture (int texture) {
		calls++;
		gl30.glDeleteTexture(texture);
		check();
	}

	@Override
	public void glDepthFunc (int func) {
		calls++;
		gl30.glDepthFunc(func);
		check();
	}

	@Override
	public void glDepthMask (boolean flag) {
		calls++;
		gl30.glDepthMask(flag);
		check();
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		calls++;
		gl30.glDepthRangef(zNear, zFar);
		check();
	}

	@Override
	public void glDisable (int cap) {
		calls++;
		gl30.glDisable(cap);
		check();
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawArrays(mode, first, count);
		check();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawElements(mode, count, type, indices);
		check();
	}

	@Override
	public void glEnable (int cap) {
		calls++;
		gl30.glEnable(cap);
		check();
	}

	@Override
	public void glFinish () {
		calls++;
		gl30.glFinish();
		check();
	}

	@Override
	public void glFlush () {
		calls++;
		gl30.glFlush();
		check();
	}

	@Override
	public void glFrontFace (int mode) {
		calls++;
		gl30.glFrontFace(mode);
		check();
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		calls++;
		gl30.glGenTextures(n, textures);
		check();
	}

	@Override
	public int glGenTexture () {
		calls++;
		int result = gl30.glGenTexture();
		check();
		return result;
	}

	@Override
	public int glGetError () {
		calls++;
		//Errors by glGetError are undetectable
		return gl30.glGetError();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		calls++;
		gl30.glGetIntegerv(pname, params);
		check();
	}

	@Override
	public String glGetString (int name) {
		calls++;
		String result = gl30.glGetString(name);
		check();
		return result;
	}

	@Override
	public void glHint (int target, int mode) {
		calls++;
		gl30.glHint(target, mode);
		check();
	}

	@Override
	public void glLineWidth (float width) {
		calls++;
		gl30.glLineWidth(width);
		check();
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		calls++;
		gl30.glPixelStorei(pname, param);
		check();
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		calls++;
		gl30.glPolygonOffset(factor, units);
		check();
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		calls++;
		gl30.glReadPixels(x, y, width, height, format, type, pixels);
		check();
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		calls++;
		gl30.glScissor(x, y, width, height);
		check();
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		calls++;
		gl30.glStencilFunc(func, ref, mask);
		check();
	}

	@Override
	public void glStencilMask (int mask) {
		calls++;
		gl30.glStencilMask(mask);
		check();
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		calls++;
		gl30.glStencilOp(fail, zfail, zpass);
		check();
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		calls++;
		gl30.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		check();
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		calls++;
		gl30.glTexParameterf(target, pname, param);
		check();
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		calls++;
		gl30.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
		check();
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		calls++;
		gl30.glViewport(x, y, width, height);
		check();
	}

	@Override
	public void glAttachShader (int program, int shader) {
		calls++;
		gl30.glAttachShader(program, shader);
		check();
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		calls++;
		gl30.glBindAttribLocation(program, index, name);
		check();
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		calls++;
		gl30.glBindBuffer(target, buffer);
		check();
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		calls++;
		gl30.glBindFramebuffer(target, framebuffer);
		check();
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		calls++;
		gl30.glBindRenderbuffer(target, renderbuffer);
		check();
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		calls++;
		gl30.glBlendColor(red, green, blue, alpha);
		check();
	}

	@Override
	public void glBlendEquation (int mode) {
		calls++;
		gl30.glBlendEquation(mode);
		check();
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		calls++;
		gl30.glBlendEquationSeparate(modeRGB, modeAlpha);
		check();
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		calls++;
		gl30.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
		check();
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		calls++;
		gl30.glBufferData(target, size, data, usage);
		check();
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		calls++;
		gl30.glBufferSubData(target, offset, size, data);
		check();
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		calls++;
		int result = gl30.glCheckFramebufferStatus(target);
		check();
		return result;
	}

	@Override
	public void glCompileShader (int shader) {
		calls++;
		gl30.glCompileShader(shader);
		check();
	}

	@Override
	public int glCreateProgram () {
		calls++;
		int result = gl30.glCreateProgram();
		check();
		return result;
	}

	@Override
	public int glCreateShader (int type) {
		calls++;
		int result = gl30.glCreateShader(type);
		check();
		return result;
	}

	@Override
	public void glDeleteBuffer (int buffer) {
		calls++;
		gl30.glDeleteBuffer(buffer);
		check();
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		calls++;
		gl30.glDeleteBuffers(n, buffers);
		check();
	}

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		calls++;
		gl30.glDeleteFramebuffer(framebuffer);
		check();
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		calls++;
		gl30.glDeleteFramebuffers(n, framebuffers);
		check();
	}

	@Override
	public void glDeleteProgram (int program) {
		calls++;
		gl30.glDeleteProgram(program);
		check();
	}

	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		calls++;
		gl30.glDeleteRenderbuffer(renderbuffer);
		check();
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		calls++;
		gl30.glDeleteRenderbuffers(n, renderbuffers);
		check();
	}

	@Override
	public void glDeleteShader (int shader) {
		calls++;
		gl30.glDeleteShader(shader);
		check();
	}

	@Override
	public void glDetachShader (int program, int shader) {
		calls++;
		gl30.glDetachShader(program, shader);
		check();
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		calls++;
		gl30.glDisableVertexAttribArray(index);
		check();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawElements(mode, count, type, indices);
		check();
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		calls++;
		gl30.glEnableVertexAttribArray(index);
		check();
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		calls++;
		gl30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
		check();
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		calls++;
		gl30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
		check();
	}

	@Override
	public int glGenBuffer () {
		calls++;
		int result = gl30.glGenBuffer();
		check();
		return result;
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		calls++;
		gl30.glGenBuffers(n, buffers);
		check();
	}

	@Override
	public void glGenerateMipmap (int target) {
		calls++;
		gl30.glGenerateMipmap(target);
		check();
	}

	@Override
	public int glGenFramebuffer () {
		calls++;
		int result = gl30.glGenFramebuffer();
		check();
		return result;
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		calls++;
		gl30.glGenFramebuffers(n, framebuffers);
		check();
	}

	@Override
	public int glGenRenderbuffer () {
		calls++;
		int result = gl30.glGenRenderbuffer();
		check();
		return result;
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		calls++;
		gl30.glGenRenderbuffers(n, renderbuffers);
		check();
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		calls++;
		String result = gl30.glGetActiveAttrib(program, index, size, type);
		check();
		return result;
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		calls++;
		String result = gl30.glGetActiveUniform(program, index, size, type);
		check();
		return result;
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		calls++;
		gl30.glGetAttachedShaders(program, maxcount, count, shaders);
		check();
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		calls++;
		int result = gl30.glGetAttribLocation(program, name);
		check();
		return result;
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		calls++;
		gl30.glGetBooleanv(pname, params);
		check();
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl30.glGetBufferParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		calls++;
		gl30.glGetFloatv(pname, params);
		check();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		calls++;
		gl30.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
		check();
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		calls++;
		gl30.glGetProgramiv(program, pname, params);
		check();
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		calls++;
		String result = gl30.glGetProgramInfoLog(program);
		check();
		return result;
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl30.glGetRenderbufferParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		calls++;
		gl30.glGetShaderiv(shader, pname, params);
		check();
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		calls++;
		String result = gl30.glGetShaderInfoLog(shader);
		check();
		return result;
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		calls++;
		gl30.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
		check();
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		calls++;
		gl30.glGetTexParameterfv(target, pname, params);
		check();
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl30.glGetTexParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		calls++;
		gl30.glGetUniformfv(program, location, params);
		check();
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		calls++;
		gl30.glGetUniformiv(program, location, params);
		check();
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		calls++;
		int result = gl30.glGetUniformLocation(program, name);
		check();
		return result;
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		calls++;
		gl30.glGetVertexAttribfv(index, pname, params);
		check();
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		calls++;
		gl30.glGetVertexAttribiv(index, pname, params);
		check();
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		calls++;
		gl30.glGetVertexAttribPointerv(index, pname, pointer);
		check();
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		calls++;
		boolean result = gl30.glIsBuffer(buffer);
		check();
		return result;
	}

	@Override
	public boolean glIsEnabled (int cap) {
		calls++;
		boolean result = gl30.glIsEnabled(cap);
		check();
		return result;
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		calls++;
		boolean result = gl30.glIsFramebuffer(framebuffer);
		check();
		return result;
	}

	@Override
	public boolean glIsProgram (int program) {
		calls++;
		boolean result = gl30.glIsProgram(program);
		check();
		return result;
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		calls++;
		boolean result = gl30.glIsRenderbuffer(renderbuffer);
		check();
		return result;
	}

	@Override
	public boolean glIsShader (int shader) {
		calls++;
		boolean result = gl30.glIsShader(shader);
		check();
		return result;
	}

	@Override
	public boolean glIsTexture (int texture) {
		calls++;
		boolean result = gl30.glIsTexture(texture);
		check();
		return result;
	}

	@Override
	public void glLinkProgram (int program) {
		calls++;
		gl30.glLinkProgram(program);
		check();
	}

	@Override
	public void glReleaseShaderCompiler () {
		calls++;
		gl30.glReleaseShaderCompiler();
		check();
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		calls++;
		gl30.glRenderbufferStorage(target, internalformat, width, height);
		check();
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		calls++;
		gl30.glSampleCoverage(value, invert);
		check();
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		calls++;
		gl30.glShaderBinary(n, shaders, binaryformat, binary, length);
		check();
	}

	@Override
	public void glShaderSource (int shader, String string) {
		calls++;
		gl30.glShaderSource(shader, string);
		check();
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		calls++;
		gl30.glStencilFuncSeparate(face, func, ref, mask);
		check();
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		calls++;
		gl30.glStencilMaskSeparate(face, mask);
		check();
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		calls++;
		gl30.glStencilOpSeparate(face, fail, zfail, zpass);
		check();
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		calls++;
		gl30.glTexParameterfv(target, pname, params);
		check();
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		calls++;
		gl30.glTexParameteri(target, pname, param);
		check();
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		calls++;
		gl30.glTexParameteriv(target, pname, params);
		check();
	}

	@Override
	public void glUniform1f (int location, float x) {
		calls++;
		gl30.glUniform1f(location, x);
		check();
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		calls++;
		gl30.glUniform1fv(location, count, v);
		check();
	}

	@Override
	public void glUniform1fv (int location, int count, float[] v, int offset) {
		calls++;
		gl30.glUniform1fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform1i (int location, int x) {
		calls++;
		gl30.glUniform1i(location, x);
		check();
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		calls++;
		gl30.glUniform1iv(location, count, v);
		check();
	}

	@Override
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		calls++;
		gl30.glUniform1iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		calls++;
		gl30.glUniform2f(location, x, y);
		check();
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		calls++;
		gl30.glUniform2fv(location, count, v);
		check();
	}

	@Override
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		calls++;
		gl30.glUniform2fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		calls++;
		gl30.glUniform2i(location, x, y);
		check();
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		calls++;
		gl30.glUniform2iv(location, count, v);
		check();
	}

	@Override
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		calls++;
		gl30.glUniform2iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		calls++;
		gl30.glUniform3f(location, x, y, z);
		check();
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		calls++;
		gl30.glUniform3fv(location, count, v);
		check();
	}

	@Override
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		calls++;
		gl30.glUniform3fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		calls++;
		gl30.glUniform3i(location, x, y, z);
		check();
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		calls++;
		gl30.glUniform3iv(location, count, v);
		check();
	}

	@Override
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		calls++;
		gl30.glUniform3iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		calls++;
		gl30.glUniform4f(location, x, y, z, w);
		check();
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		calls++;
		gl30.glUniform4fv(location, count, v);
		check();
	}

	@Override
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		calls++;
		gl30.glUniform4fv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		calls++;
		gl30.glUniform4i(location, x, y, z, w);
		check();
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		calls++;
		gl30.glUniform4iv(location, count, v);
		check();
	}

	@Override
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		calls++;
		gl30.glUniform4iv(location, count, v, offset);
		check();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix2fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl30.glUniformMatrix2fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix3fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl30.glUniformMatrix3fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix4fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		calls++;
		gl30.glUniformMatrix4fv(location, count, transpose, value, offset);
		check();
	}

	@Override
	public void glUseProgram (int program) {
		shaderSwitches++;
		calls++;
		gl30.glUseProgram(program);
		check();
	}

	@Override
	public void glValidateProgram (int program) {
		calls++;
		gl30.glValidateProgram(program);
		check();
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		calls++;
		gl30.glVertexAttrib1f(indx, x);
		check();
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		calls++;
		gl30.glVertexAttrib1fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		calls++;
		gl30.glVertexAttrib2f(indx, x, y);
		check();
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		calls++;
		gl30.glVertexAttrib2fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		calls++;
		gl30.glVertexAttrib3f(indx, x, y, z);
		check();
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		calls++;
		gl30.glVertexAttrib3fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		calls++;
		gl30.glVertexAttrib4f(indx, x, y, z, w);
		check();
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		calls++;
		gl30.glVertexAttrib4fv(indx, values);
		check();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		calls++;
		gl30.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		check();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		calls++;
		gl30.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		check();
	}

	// GL30 Unique

	@Override
	public void glReadBuffer (int mode) {
		calls++;
		gl30.glReadBuffer(mode);
		check();
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawRangeElements(mode, start, end, count, type, indices);
		check();
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawRangeElements(mode, start, end, count, type, offset);
		check();
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels) {
		calls++;
		gl30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
		check();
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		calls++;
		gl30.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
		check();
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, Buffer pixels) {
		calls++;
		gl30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
		check();
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		calls++;
		gl30.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
		check();
	}

	@Override
	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		calls++;
		gl30.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
		check();
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		calls++;
		gl30.glGenQueries(n, ids, offset);
		check();
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		calls++;
		gl30.glGenQueries(n, ids);
		check();
	}

	@Override
	public void glDeleteQueries (int n, int[] ids, int offset) {
		calls++;
		gl30.glDeleteQueries(n, ids, offset);
		check();
	}

	@Override
	public void glDeleteQueries (int n, IntBuffer ids) {
		calls++;
		gl30.glDeleteQueries(n, ids);
		check();
	}

	@Override
	public boolean glIsQuery (int id) {
		calls++;
		final boolean result = gl30.glIsQuery(id);
		check();
		return result;
	}

	@Override
	public void glBeginQuery (int target, int id) {
		calls++;
		gl30.glBeginQuery(target, id);
		check();
	}

	@Override
	public void glEndQuery (int target) {
		calls++;
		gl30.glEndQuery(target);
		check();
	}

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		calls++;
		gl30.glGetQueryiv(target, pname, params);
		check();
	}

	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		calls++;
		gl30.glGetQueryObjectuiv(id, pname, params);
		check();
	}

	@Override
	public boolean glUnmapBuffer (int target) {
		calls++;
		final boolean result = gl30.glUnmapBuffer(target);
		check();
		return result;
	}

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		calls++;
		final Buffer result = gl30.glGetBufferPointerv(target, pname);
		check();
		return result;
	}

	@Override
	public void glDrawBuffers (int n, IntBuffer bufs) {
		drawCalls++;
		calls++;
		gl30.glDrawBuffers(n, bufs);
		check();
	}

	@Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix2x3fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix3x2fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix2x4fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix4x2fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix3x4fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		calls++;
		gl30.glUniformMatrix4x3fv(location, count, transpose, value);
		check();
	}

	@Override
	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		calls++;
		gl30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
		check();
	}

	@Override
	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		calls++;
		gl30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
		check();
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		calls++;
		gl30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
		check();
	}

	@Override
	public void glFlushMappedBufferRange (int target, int offset, int length) {
		calls++;
		gl30.glFlushMappedBufferRange(target, offset, length);
		check();
	}

	@Override
	public void glBindVertexArray (int array) {
		calls++;
		gl30.glBindVertexArray(array);
		check();
	}

	@Override
	public void glDeleteVertexArrays (int n, int[] arrays, int offset) {
		calls++;
		gl30.glDeleteVertexArrays(n, arrays, offset);
		check();
	}

	@Override
	public void glDeleteVertexArrays (int n, IntBuffer arrays) {
		calls++;
		gl30.glDeleteVertexArrays(n, arrays);
		check();
	}

	@Override
	public void glGenVertexArrays (int n, int[] arrays, int offset) {
		calls++;
		gl30.glGenVertexArrays(n, arrays, offset);
		check();
	}

	@Override
	public void glGenVertexArrays (int n, IntBuffer arrays) {
		calls++;
		gl30.glGenVertexArrays(n, arrays);
		check();
	}

	@Override
	public boolean glIsVertexArray (int array) {
		calls++;
		final boolean result = gl30.glIsVertexArray(array);
		check();
		return result;
	}

	@Override
	public void glBeginTransformFeedback (int primitiveMode) {
		calls++;
		gl30.glBeginTransformFeedback(primitiveMode);
		check();
	}

	@Override
	public void glEndTransformFeedback () {
		calls++;
		gl30.glEndTransformFeedback();
		check();
	}

	@Override
	public void glBindBufferRange (int target, int index, int buffer, int offset, int size) {
		calls++;
		gl30.glBindBufferRange(target, index, buffer, offset, size);
		check();
	}

	@Override
	public void glBindBufferBase (int target, int index, int buffer) {
		calls++;
		gl30.glBindBufferBase(target, index, buffer);
		check();
	}

	@Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		calls++;
		gl30.glTransformFeedbackVaryings(program, varyings, bufferMode);
		check();
	}

	@Override
	public void glVertexAttribIPointer (int index, int size, int type, int stride, int offset) {
		calls++;
		gl30.glVertexAttribIPointer(index, size, type, stride, offset);
		check();
	}

	@Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		calls++;
		gl30.glGetVertexAttribIiv(index, pname, params);
		check();
	}

	@Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		calls++;
		gl30.glGetVertexAttribIuiv(index, pname, params);
		check();
	}

	@Override
	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		calls++;
		gl30.glVertexAttribI4i(index, x, y, z, w);
		check();
	}

	@Override
	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		calls++;
		gl30.glVertexAttribI4ui(index, x, y, z, w);
		check();
	}

	@Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		calls++;
		gl30.glGetUniformuiv(program, location, params);
		check();
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		calls++;
		final int result = gl30.glGetFragDataLocation(program, name);
		check();
		return result;
	}

	@Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		calls++;
		gl30.glUniform1uiv(location, count, value);
		check();
	}

	@Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		calls++;
		gl30.glUniform3uiv(location, count, value);
		check();
	}

	@Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		calls++;
		gl30.glUniform4uiv(location, count, value);
		check();
	}

	@Override
	public void glClearBufferiv (int buffer, int drawbuffer, IntBuffer value) {
		calls++;
		gl30.glClearBufferiv(buffer, drawbuffer, value);
		check();
	}

	@Override
	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		calls++;
		gl30.glClearBufferuiv(buffer, drawbuffer, value);
		check();
	}

	@Override
	public void glClearBufferfv (int buffer, int drawbuffer, FloatBuffer value) {
		calls++;
		gl30.glClearBufferfv(buffer, drawbuffer, value);
		check();
	}

	@Override
	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		calls++;
		gl30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
		check();
	}

	@Override
	public String glGetStringi (int name, int index) {
		calls++;
		final String result = gl30.glGetStringi(name, index);
		check();
		return result;
	}

	@Override
	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		calls++;
		gl30.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
		check();
	}

	@Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		calls++;
		gl30.glGetUniformIndices(program, uniformNames, uniformIndices);
		check();
	}

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		calls++;
		gl30.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params);
		check();
	}

	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		calls++;
		final int result = gl30.glGetUniformBlockIndex(program, uniformBlockName);
		check();
		return result;
	}

	@Override
	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		calls++;
		gl30.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
		check();
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		calls++;
		gl30.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
		check();
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		calls++;
		final String result = gl30.glGetActiveUniformBlockName(program, uniformBlockIndex);
		check();
		return result;
	}

	@Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		calls++;
		gl30.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
		check();
	}

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instanceCount) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawArraysInstanced(mode, first, count, instanceCount);
		check();
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		vertexCount.put(count);
		drawCalls++;
		calls++;
		gl30.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
		check();
	}

	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		calls++;
		gl30.glGetInteger64v(pname, params);
		check();
	}

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		calls++;
		gl30.glGetBufferParameteri64v(target, pname, params);
		check();
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		calls++;
		gl30.glGenSamplers(count, samplers, offset);
		check();
	}

	@Override
	public void glGenSamplers (int count, IntBuffer samplers) {
		calls++;
		gl30.glGenSamplers(count, samplers);
		check();
	}

	@Override
	public void glDeleteSamplers (int count, int[] samplers, int offset) {
		calls++;
		gl30.glDeleteSamplers(count, samplers, offset);
		check();
	}

	@Override
	public void glDeleteSamplers (int count, IntBuffer samplers) {
		calls++;
		gl30.glDeleteSamplers(count, samplers);
		check();
	}

	@Override
	public boolean glIsSampler (int sampler) {
		calls++;
		final boolean result = gl30.glIsSampler(sampler);
		check();
		return result;
	}

	@Override
	public void glBindSampler (int unit, int sampler) {
		calls++;
		gl30.glBindSampler(unit, sampler);
		check();
	}

	@Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		calls++;
		gl30.glSamplerParameteri(sampler, pname, param);
		check();
	}

	@Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		calls++;
		gl30.glSamplerParameteriv(sampler, pname, param);
		check();
	}

	@Override
	public void glSamplerParameterf (int sampler, int pname, float param) {
		calls++;
		gl30.glSamplerParameterf(sampler, pname, param);
		check();
	}

	@Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		calls++;
		gl30.glSamplerParameterfv(sampler, pname, param);
		check();
	}

	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		calls++;
		gl30.glGetSamplerParameteriv(sampler, pname, params);
		check();
	}

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		calls++;
		gl30.glGetSamplerParameterfv(sampler, pname, params);
		check();
	}

	@Override
	public void glVertexAttribDivisor (int index, int divisor) {
		calls++;
		gl30.glVertexAttribDivisor(index, divisor);
		check();
	}

	@Override
	public void glBindTransformFeedback (int target, int id) {
		calls++;
		gl30.glBindTransformFeedback(target, id);
		check();
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, int[] ids, int offset) {
		calls++;
		gl30.glDeleteTransformFeedbacks(n, ids, offset);
		check();
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		calls++;
		gl30.glDeleteTransformFeedbacks(n, ids);
		check();
	}

	@Override
	public void glGenTransformFeedbacks (int n, int[] ids, int offset) {
		calls++;
		gl30.glGenTransformFeedbacks(n, ids, offset);
		check();
	}

	@Override
	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		calls++;
		gl30.glGenTransformFeedbacks(n, ids);
		check();
	}

	@Override
	public boolean glIsTransformFeedback (int id) {
		calls++;
		final boolean result = gl30.glIsTransformFeedback(id);
		check();
		return result;
	}

	@Override
	public void glPauseTransformFeedback () {
		calls++;
		gl30.glPauseTransformFeedback();
		check();
	}

	@Override
	public void glResumeTransformFeedback () {
		calls++;
		gl30.glResumeTransformFeedback();
		check();
	}

	@Override
	public void glProgramParameteri (int program, int pname, int value) {
		calls++;
		gl30.glProgramParameteri(program, pname, value);
		check();
	}

	@Override
	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		calls++;
		gl30.glInvalidateFramebuffer(target, numAttachments, attachments);
		check();
	}

	@Override
	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		calls++;
		gl30.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
		check();
	}
}
