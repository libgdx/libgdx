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

package com.badlogic.gdx.backends.gwt;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.webgl.client.WebGLRenderingContext;

public class GwtGL20Debug extends GwtGL20 {

	protected GwtGL20Debug (WebGLRenderingContext gl) {
		super(gl);
	}

	private void checkError () {
		int error = 0;
		if ((error = gl.getError()) != GL_NO_ERROR) {
			throw new GdxRuntimeException("GL error: " + error + ", " + Integer.toHexString(error));
		}
	}

	@Override
	public void glActiveTexture (int texture) {
		super.glActiveTexture(texture);
		checkError();
	}

	@Override
	public void glBindTexture (int target, int texture) {
		 
		super.glBindTexture(target, texture);
		checkError();
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		 
		super.glBlendFunc(sfactor, dfactor);
		checkError();
	}

	@Override
	public void glClear (int mask) {
		 
		super.glClear(mask);
		checkError();
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		 
		super.glClearColor(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glClearDepthf (float depth) {
		 
		super.glClearDepthf(depth);
		checkError();
	}

	@Override
	public void glClearStencil (int s) {
		 
		super.glClearStencil(s);
		checkError();
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		 
		super.glColorMask(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		 
		super.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
		checkError();
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		 
		super.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
		checkError();
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		 
		super.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
		checkError();
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		 
		super.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
		checkError();
	}

	@Override
	public void glCullFace (int mode) {
		 
		super.glCullFace(mode);
		checkError();
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		 
		super.glDeleteTextures(n, textures);
		checkError();
	}

	@Override
	public void glDepthFunc (int func) {
		 
		super.glDepthFunc(func);
		checkError();
	}

	@Override
	public void glDepthMask (boolean flag) {
		 
		super.glDepthMask(flag);
		checkError();
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		 
		super.glDepthRangef(zNear, zFar);
		checkError();
	}

	@Override
	public void glDisable (int cap) {
		 
		super.glDisable(cap);
		checkError();
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		 
		super.glDrawArrays(mode, first, count);
		checkError();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		 
		super.glDrawElements(mode, count, type, indices);
		checkError();
	}

	@Override
	public void glEnable (int cap) {
		 
		super.glEnable(cap);
		checkError();
	}

	@Override
	public void glFinish () {
		 
		super.glFinish();
		checkError();
	}

	@Override
	public void glFlush () {
		 
		super.glFlush();
		checkError();
	}

	@Override
	public void glFrontFace (int mode) {
		 
		super.glFrontFace(mode);
		checkError();
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		 
		super.glGenTextures(n, textures);
		checkError();
	}

	@Override
	public int glGetError () {
		 
		return super.glGetError();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		 
		super.glGetIntegerv(pname, params);
		checkError();
	}

	@Override
	public String glGetString (int name) {
		 
		return super.glGetString(name);
	}

	@Override
	public void glHint (int target, int mode) {
		 
		super.glHint(target, mode);
		checkError();
	}

	@Override
	public void glLineWidth (float width) {
		 
		super.glLineWidth(width);
		checkError();
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		 
		super.glPixelStorei(pname, param);
		checkError();
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		 
		super.glPolygonOffset(factor, units);
		checkError();
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		 
		super.glReadPixels(x, y, width, height, format, type, pixels);
		checkError();
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		 
		super.glScissor(x, y, width, height);
		checkError();
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		 
		super.glStencilFunc(func, ref, mask);
		checkError();
	}

	@Override
	public void glStencilMask (int mask) {
		 
		super.glStencilMask(mask);
		checkError();
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		 
		super.glStencilOp(fail, zfail, zpass);
		checkError();
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		 
		super.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
		checkError();
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		 
		super.glTexParameterf(target, pname, param);
		checkError();
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		 
		super.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
		checkError();
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		 
		super.glViewport(x, y, width, height);
		checkError();
	}

	@Override
	public void glAttachShader (int program, int shader) {
		 
		super.glAttachShader(program, shader);
		checkError();
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		 
		super.glBindAttribLocation(program, index, name);
		checkError();
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		 
		super.glBindBuffer(target, buffer);
		checkError();
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		 
		super.glBindFramebuffer(target, framebuffer);
		checkError();
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		 
		super.glBindRenderbuffer(target, renderbuffer);
		checkError();
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		 
		super.glBlendColor(red, green, blue, alpha);
		checkError();
	}

	@Override
	public void glBlendEquation (int mode) {
		 
		super.glBlendEquation(mode);
		checkError();
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		 
		super.glBlendEquationSeparate(modeRGB, modeAlpha);
		checkError();
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		 
		super.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
		checkError();
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		 
		super.glBufferData(target, size, data, usage);
		checkError();
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		 
		super.glBufferSubData(target, offset, size, data);
		checkError();
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		 
		return super.glCheckFramebufferStatus(target);
	}

	@Override
	public void glCompileShader (int shader) {
		 
		super.glCompileShader(shader);
		checkError();
	}

	@Override
	public int glCreateProgram () {
		 
		int program = super.glCreateProgram();
		checkError();
		return program;
	}

	@Override
	public int glCreateShader (int type) {
		 
		int shader = super.glCreateShader(type);
		checkError();
		return shader;
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		 
		super.glDeleteBuffers(n, buffers);
		checkError();
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		 
		super.glDeleteFramebuffers(n, framebuffers);
		checkError();
	}

	@Override
	public void glDeleteProgram (int program) {
		 
		super.glDeleteProgram(program);
		checkError();
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		 
		super.glDeleteRenderbuffers(n, renderbuffers);
		checkError();
	}

	@Override
	public void glDeleteShader (int shader) {
		 
		super.glDeleteShader(shader);
		checkError();
	}

	@Override
	public void glDetachShader (int program, int shader) {
		 
		super.glDetachShader(program, shader);
		checkError();
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		 
		super.glDisableVertexAttribArray(index);
		checkError();
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		 
		super.glDrawElements(mode, count, type, indices);
		checkError();
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		 
		super.glEnableVertexAttribArray(index);
		checkError();
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		 
		super.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
		checkError();
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		 
		super.glFramebufferTexture2D(target, attachment, textarget, texture, level);
		checkError();
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		 
		super.glGenBuffers(n, buffers);
		checkError();
	}

	@Override
	public void glGenerateMipmap (int target) {
		 
		super.glGenerateMipmap(target);
		checkError();
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		 
		super.glGenFramebuffers(n, framebuffers);
		checkError();
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		 
		super.glGenRenderbuffers(n, renderbuffers);
		checkError();
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		 
		String attrib = super.glGetActiveAttrib(program, index, size, type);
		checkError();
		return attrib;
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		 
		String uniform = super.glGetActiveUniform(program, index, size, type);
		checkError();
		return uniform;
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		 
		super.glGetAttachedShaders(program, maxcount, count, shaders);
		checkError();
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		 
		int loc = super.glGetAttribLocation(program, name);
		checkError();
		return loc;
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		 
		super.glGetBooleanv(pname, params);
		checkError();
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		 
		super.glGetBufferParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		 
		super.glGetFloatv(pname, params);
		checkError();
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		 
		super.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
		checkError();
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		 
		super.glGetProgramiv(program, pname, params);
		checkError();
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		 
		String info = super.glGetProgramInfoLog(program);
		checkError();
		return info;
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		 
		super.glGetRenderbufferParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		 
		super.glGetShaderiv(shader, pname, params);
		checkError();
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		 
		String info = super.glGetShaderInfoLog(shader);
		checkError();
		return info;
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		 
		super.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
		checkError();
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		 
		super.glGetTexParameterfv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		 
		super.glGetTexParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		 
		super.glGetUniformfv(program, location, params);
		checkError();
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		 
		super.glGetUniformiv(program, location, params);
		checkError();
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		 
		int loc = super.glGetUniformLocation(program, name);
		checkError();
		return loc;
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		 
		super.glGetVertexAttribfv(index, pname, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		 
		super.glGetVertexAttribiv(index, pname, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		 
		super.glGetVertexAttribPointerv(index, pname, pointer);
		checkError();
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		 
		boolean res = super.glIsBuffer(buffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsEnabled (int cap) {
		 
		boolean res = super.glIsEnabled(cap);
		checkError();
		return res;
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		 
		boolean res = super.glIsFramebuffer(framebuffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsProgram (int program) {
		 
		boolean res = super.glIsProgram(program);
		checkError();
		return res;
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		 
		boolean res = super.glIsRenderbuffer(renderbuffer);
		checkError();
		return res;
	}

	@Override
	public boolean glIsShader (int shader) {
		 
		boolean res = super.glIsShader(shader);
		checkError();
		return res;
	}

	@Override
	public boolean glIsTexture (int texture) {
		 
		boolean res = super.glIsTexture(texture);
		checkError();
		return res;
	}

	@Override
	public void glLinkProgram (int program) {
		 
		super.glLinkProgram(program);
		checkError();
	}

	@Override
	public void glReleaseShaderCompiler () {
		 
		super.glReleaseShaderCompiler();
		checkError();
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		 
		super.glRenderbufferStorage(target, internalformat, width, height);
		checkError();
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		 
		super.glSampleCoverage(value, invert);
		checkError();
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		 
		super.glShaderBinary(n, shaders, binaryformat, binary, length);
		checkError();
	}

	@Override
	public void glShaderSource (int shader, String source) {
		 
		super.glShaderSource(shader, source);
		checkError();
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		 
		super.glStencilFuncSeparate(face, func, ref, mask);
		checkError();
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		 
		super.glStencilMaskSeparate(face, mask);
		checkError();
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		 
		super.glStencilOpSeparate(face, fail, zfail, zpass);
		checkError();
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		 
		super.glTexParameterfv(target, pname, params);
		checkError();
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		 
		super.glTexParameteri(target, pname, param);
		checkError();
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		 
		super.glTexParameteriv(target, pname, params);
		checkError();
	}

	@Override
	public void glUniform1f (int location, float x) {
		 
		super.glUniform1f(location, x);
		checkError();
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		 
		super.glUniform1fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform1i (int location, int x) {
		 
		super.glUniform1i(location, x);
		checkError();
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		 
		super.glUniform1iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		 
		super.glUniform2f(location, x, y);
		checkError();
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		 
		super.glUniform2fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		 
		super.glUniform2i(location, x, y);
		checkError();
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		 
		super.glUniform2iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		 
		super.glUniform3f(location, x, y, z);
		checkError();
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		 
		super.glUniform3fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		 
		super.glUniform3i(location, x, y, z);
		checkError();
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		 
		super.glUniform3iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		 
		super.glUniform4f(location, x, y, z, w);
		checkError();
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		 
		super.glUniform4fv(location, count, v);
		checkError();
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		 
		super.glUniform4i(location, x, y, z, w);
		checkError();
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		 
		super.glUniform4iv(location, count, v);
		checkError();
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		 
		super.glUniformMatrix2fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		 
		super.glUniformMatrix3fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		 
		super.glUniformMatrix4fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUseProgram (int program) {
		 
		super.glUseProgram(program);
		checkError();
	}

	@Override
	public void glValidateProgram (int program) {
		 
		super.glValidateProgram(program);
		checkError();
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		 
		super.glVertexAttrib1f(indx, x);
		checkError();
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		 
		super.glVertexAttrib1fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		 
		super.glVertexAttrib2f(indx, x, y);
		checkError();
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		 
		super.glVertexAttrib2fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		 
		super.glVertexAttrib3f(indx, x, y, z);
		checkError();
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		 
		super.glVertexAttrib3fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		 
		super.glVertexAttrib4f(indx, x, y, z, w);
		checkError();
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		 
		super.glVertexAttrib4fv(indx, values);
		checkError();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		 
		super.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		checkError();
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		 
		super.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
		checkError();
	}
}
