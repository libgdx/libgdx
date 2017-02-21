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

package com.badlogic.gdx.backends.jglfw;

import static com.badlogic.jglfw.utils.Memory.getPosition;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.jglfw.gl.GL;

public class JglfwGL20 implements GL20 {
	private ByteBuffer buffer = null;
	private FloatBuffer floatBuffer = null;
	private IntBuffer intBuffer = null;

	private void ensureBufferCapacity (int numBytes) {
		if (buffer == null || buffer.capacity() < numBytes) {
			buffer = com.badlogic.gdx.utils.BufferUtils.newByteBuffer(numBytes);
			floatBuffer = buffer.asFloatBuffer();
			intBuffer = buffer.asIntBuffer();
		}
	}

	private FloatBuffer toFloatBuffer (float v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		floatBuffer.clear();
		com.badlogic.gdx.utils.BufferUtils.copy(v, floatBuffer, count, offset);
		return floatBuffer;
	}

	private IntBuffer toIntBuffer (int v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		intBuffer.clear();
		com.badlogic.gdx.utils.BufferUtils.copy(v, count, offset, intBuffer);
		return intBuffer;
	}
	
	private IntBuffer toIntBuffer (int v) {
		ensureBufferCapacity(4);
		intBuffer.put(0, v);
		intBuffer.position(0);
		intBuffer.limit(1);
		return intBuffer;
	}
	
	public void glActiveTexture (int texture) {
		GL.glActiveTexture(texture);
	}

	public void glBindTexture (int target, int texture) {
		GL.glBindTexture(target, texture);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		GL.glBlendFunc(sfactor, dfactor);
	}

	public void glClear (int mask) {
		GL.glClear(mask);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		GL.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf (float depth) {
		GL.glClearDepthf(depth);
	}

	public void glClearStencil (int s) {
		GL.glClearStencil(s);
	}

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GL.glColorMask(red, green, blue, alpha);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GL.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data, getPosition(data));
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GL.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data, getPosition(data));
	}

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public void glCullFace (int mode) {
		GL.glCullFace(mode);
	}

	public void glDeleteTextures (int n, IntBuffer textures) {
		GL.glDeleteTextures(n, textures, getPosition(textures));
	}
	
	public void glDeleteTexture (int texture) {
		glDeleteTextures(1, toIntBuffer(texture));
	}

	public void glDepthFunc (int func) {
		GL.glDepthFunc(func);
	}

	public void glDepthMask (boolean flag) {
		GL.glDepthMask(flag);
	}

	public void glDepthRangef (float zNear, float zFar) {
		GL.glDepthRangef(zNear, zFar);
	}

	public void glDisable (int cap) {
		GL.glDisable(cap);
	}

	public void glDrawArrays (int mode, int first, int count) {
		GL.glDrawArrays(mode, first, count);
	}

	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		GL.glDrawElements(mode, count, type, indices, getPosition(indices));
	}

	public void glEnable (int cap) {
		GL.glEnable(cap);
	}

	public void glFinish () {
		GL.glFinish();
	}

	public void glFlush () {
		GL.glFlush();
	}

	public void glFrontFace (int mode) {
		GL.glFrontFace(mode);
	}

	public void glGenTextures (int n, IntBuffer textures) {
		GL.glGenTextures(n, textures, getPosition(textures));
	}
	
	public int glGenTexture () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenTextures(1, intBuffer);
		return intBuffer.get(0);
	}

	public int glGetError () {
		return GL.glGetError();
	}

	public void glGetIntegerv (int pname, IntBuffer params) {
		GL.glGetIntegerv(pname, params, getPosition(params));
	}

	public String glGetString (int name) {
		return GL.glGetString(name);
	}

	public void glHint (int target, int mode) {
		GL.glHint(target, mode);
	}

	public void glLineWidth (float width) {
		GL.glLineWidth(width);
	}

	public void glPixelStorei (int pname, int param) {
		GL.glPixelStorei(pname, param);
	}

	public void glPolygonOffset (float factor, float units) {
		GL.glPolygonOffset(factor, units);
	}

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GL.glReadPixels(x, y, width, height, format, type, pixels, getPosition(pixels));
	}

	public void glScissor (int x, int y, int width, int height) {
		GL.glScissor(x, y, width, height);
	}

	public void glStencilFunc (int func, int ref, int mask) {
		GL.glStencilFunc(func, ref, mask);
	}

	public void glStencilMask (int mask) {
		GL.glStencilMask(mask);
	}

	public void glStencilOp (int fail, int zfail, int zpass) {
		GL.glStencilOp(fail, zfail, zpass);
	}

	public void glTexImage2D (int target, int level, int internalFormat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		GL.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels, getPosition(pixels));
	}

	public void glTexParameterf (int target, int pname, float param) {
		GL.glTexParameterf(target, pname, param);
	}

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		GL.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels, getPosition(pixels));
	}

	public void glViewport (int x, int y, int width, int height) {
		GL.glViewport(x, y, width, height);
	}

	public void glGetFloatv (int pname, FloatBuffer params) {
		GL.glGetFloatv(pname, params, getPosition(params));
	}

	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL.glGetTexParameterfv(target, pname, params, getPosition(params));
	}

	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GL.glTexParameterfv(target, pname, params, getPosition(params));
	}

	public void glBindBuffer (int target, int buffer) {
		GL.glBindBuffer(target, buffer);
	}

	public void glBufferData (int target, int size, Buffer data, int usage) {
		GL.glBufferData(target, size, data, getPosition(data), usage);
	}

	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		GL.glBufferSubData(target, offset, size, data, getPosition(data));
	}

	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GL.glDeleteBuffers(n, buffers, getPosition(buffers));
	}
	
	public void glDeleteBuffer (int buffer) {
		glDeleteBuffers(1, toIntBuffer(buffer));
	}

	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL.glGetBufferParameteriv(target, pname, params, getPosition(params));
	}

	public void glGenBuffers (int n, IntBuffer buffers) {
		GL.glGenBuffers(n, buffers, getPosition(buffers));
	}
	
	public int glGenBuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenBuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL.glGetTexParameteriv(target, pname, params, getPosition(params));
	}

	public boolean glIsBuffer (int buffer) {
		return GL.glIsBuffer(buffer);
	}

	public boolean glIsEnabled (int cap) {
		return GL.glIsEnabled(cap);
	}

	public boolean glIsTexture (int texture) {
		return GL.glIsTexture(texture);
	}

	public void glTexParameteri (int target, int pname, int param) {
		GL.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GL.glTexParameteriv(target, pname, params, getPosition(params));
	}

	public void glDrawElements (int mode, int count, int type, int indices) {
		GL.glDrawElements(mode, count, type, indices);
	}

	public void glAttachShader (int program, int shader) {
		GL.glAttachShader(program, shader);
	}

	public void glBindAttribLocation (int program, int index, String name) {
		GL.glBindAttribLocation(program, index, name);
	}

	public void glBindFramebuffer (int target, int framebuffer) {
		GL.glBindFramebufferEXT(target, framebuffer);
	}

	public void glBindRenderbuffer (int target, int renderbuffer) {
		GL.glBindRenderbufferEXT(target, renderbuffer);
	}

	public void glBlendColor (float red, float green, float blue, float alpha) {
		GL.glBlendColor(red, green, blue, alpha);
	}

	public void glBlendEquation (int mode) {
		GL.glBlendEquation(mode);
	}

	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GL.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public int glCheckFramebufferStatus (int target) {
		return GL.glCheckFramebufferStatusEXT(target);
	}

	public void glCompileShader (int shader) {
		GL.glCompileShader(shader);
	}

	public int glCreateProgram () {
		return GL.glCreateProgram();
	}

	public int glCreateShader (int type) {
		return GL.glCreateShader(type);
	}

	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GL.glDeleteFramebuffersEXT(n, framebuffers, getPosition(framebuffers));
	}
	
	public void glDeleteFramebuffer (int framebuffer) {
		glDeleteFramebuffers(1, toIntBuffer(framebuffer));
	}

	public void glDeleteProgram (int program) {
		GL.glDeleteProgram(program);
	}

	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GL.glDeleteRenderbuffersEXT(n, renderbuffers, getPosition(renderbuffers));
	}
	
	public void glDeleteRenderbuffer (int renderbuffer) {
		glDeleteRenderbuffers(1, toIntBuffer(renderbuffer));
	}

	public void glDeleteShader (int shader) {
		GL.glDeleteShader(shader);
	}

	public void glDetachShader (int program, int shader) {
		GL.glDetachShader(program, shader);
	}

	public void glDisableVertexAttribArray (int index) {
		GL.glDisableVertexAttribArray(index);
	}

	public void glEnableVertexAttribArray (int index) {
		GL.glEnableVertexAttribArray(index);
	}

	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GL.glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
	}

	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GL.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
	}

	public void glGenerateMipmap (int target) {
		GL.glGenerateMipmapEXT(target);
	}

	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GL.glGenFramebuffersEXT(n, framebuffers, getPosition(framebuffers));
	}
	
	public int glGenFramebuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenFramebuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GL.glGenRenderbuffersEXT(n, renderbuffers, getPosition(renderbuffers));
	}
	
	public int glGenRenderbuffer () {
		ensureBufferCapacity(4);
		intBuffer.position(0);
		intBuffer.limit(1);
		glGenRenderbuffers(1, intBuffer);
		return intBuffer.get(0);
	}

	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		return GL.glGetActiveAttrib(program, index, size, getPosition(size), type, getPosition(type));
	}

	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		return GL.glGetActiveUniform(program, index, size, getPosition(size), type, getPosition(type));
	}

	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GL.glGetAttachedShaders(program, maxcount, count, getPosition(count), shaders, getPosition(shaders));
	}

	public int glGetAttribLocation (int program, String name) {
		return GL.glGetAttribLocation(program, name);
	}

	public void glGetBooleanv (int pname, Buffer params) {
		GL.glGetBooleanv(pname, params, getPosition(params));
	}

	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		GL.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params, getPosition(params));
	}

	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL.glGetProgramiv(program, pname, params, getPosition(params));
	}

	public String glGetProgramInfoLog (int program) {
		return GL.glGetProgramInfoLog(program);
	}

	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GL.glGetRenderbufferParameterivEXT(target, pname, params, getPosition(params));
	}

	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL.glGetShaderiv(shader, pname, params, getPosition(params));
	}

	public String glGetShaderInfoLog (int shader) {
		return GL.glGetShaderInfoLog(shader);
	}

	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		GL.glGetShaderPrecisionFormat(shadertype, precisiontype, range, getPosition(range), precision, getPosition(precision));
	}

	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GL.glGetUniformfv(program, location, params, getPosition(params));
	}

	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GL.glGetUniformiv(program, location, params, getPosition(params));
	}

	public int glGetUniformLocation (int program, String name) {
		return GL.glGetUniformLocation(program, name);
	}

	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GL.glGetVertexAttribfv(index, pname, params, getPosition(params));
	}

	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GL.glGetVertexAttribiv(index, pname, params, getPosition(params));
	}

	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		GL.glGetVertexAttribPointerv(index, pname, pointer, getPosition(pointer));
	}

	public boolean glIsFramebuffer (int framebuffer) {
		return GL.glIsFramebufferEXT(framebuffer);
	}

	public boolean glIsProgram (int program) {
		return GL.glIsProgram(program);
	}

	public boolean glIsRenderbuffer (int renderbuffer) {
		return GL.glIsRenderbufferEXT(renderbuffer);
	}

	public boolean glIsShader (int shader) {
		return GL.glIsShader(shader);
	}

	public void glLinkProgram (int program) {
		GL.glLinkProgram(program);
	}

	public void glReleaseShaderCompiler () {
		GL.glReleaseShaderCompiler();
	}

	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GL.glRenderbufferStorageEXT(target, internalformat, width, height);
	}

	public void glSampleCoverage (float value, boolean invert) {
		GL.glSampleCoverage(value, invert);
	}

	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		GL.glShaderBinary(n, shaders, getPosition(shaders), binaryformat, binary, getPosition(binary), length);
	}

	public void glShaderSource (int shader, String string) {
		GL.glShaderSource(shader, string);
	}

	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GL.glStencilFuncSeparate(face, func, ref, mask);
	}

	public void glStencilMaskSeparate (int face, int mask) {
		GL.glStencilMaskSeparate(face, mask);
	}

	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GL.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	public void glUniform1f (int location, float x) {
		GL.glUniform1f(location, x);
	}

	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GL.glUniform1fv(location, count, v, getPosition(v));
	}

	public void glUniform1fv (int location, int count, float[] v, int offset) {
		glUniform1fv(location, count, toFloatBuffer(v, offset, count));
	}
	
	public void glUniform1i (int location, int x) {
		GL.glUniform1i(location, x);
	}

	public void glUniform1iv (int location, int count, IntBuffer v) {
		GL.glUniform1iv(location, count, v, getPosition(v));
	}
	
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		glUniform1iv(location, count, toIntBuffer(v, offset, count));
	}

	public void glUniform2f (int location, float x, float y) {
		GL.glUniform2f(location, x, y);
	}

	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GL.glUniform2fv(location, count, v, getPosition(v));
	}
	
	public void glUniform2fv (int location, int count, float[] v, int offset) {
		glUniform2fv(location, count, toFloatBuffer(v, offset, count << 1));
	}

	public void glUniform2i (int location, int x, int y) {
		GL.glUniform2i(location, x, y);
	}

	public void glUniform2iv (int location, int count, IntBuffer v) {
		GL.glUniform2iv(location, count, v, getPosition(v));
	}
	
	public void glUniform2iv (int location, int count, int[] v, int offset) {
		glUniform2iv(location, count, toIntBuffer(v, offset, count<<1));
	}

	public void glUniform3f (int location, float x, float y, float z) {
		GL.glUniform3f(location, x, y, z);
	}

	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GL.glUniform3fv(location, count, v, getPosition(v));
	}
	
	public void glUniform3fv (int location, int count, float[] v, int offset) {
		glUniform3fv(location, count, toFloatBuffer(v, offset, count*3));
	}

	public void glUniform3i (int location, int x, int y, int z) {
		GL.glUniform3i(location, x, y, z);
	}

	public void glUniform3iv (int location, int count, IntBuffer v) {
		GL.glUniform3iv(location, count, v, getPosition(v));
	}
	
	public void glUniform3iv (int location, int count, int[] v, int offset) {
		glUniform3iv(location, count, toIntBuffer(v, offset, count*3));
	}

	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL.glUniform4f(location, x, y, z, w);
	}

	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GL.glUniform4fv(location, count, v, getPosition(v));
	}
	
	public void glUniform4fv (int location, int count, float[] v, int offset) {
		glUniform4fv(location, count, toFloatBuffer(v, offset, count << 2));
	}

	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL.glUniform4i(location, x, y, z, w);
	}

	public void glUniform4iv (int location, int count, IntBuffer v) {
		GL.glUniform4iv(location, count, v, getPosition(v));
	}
	
	public void glUniform4iv (int location, int count, int[] v, int offset) {
		glUniform4iv(location, count, toIntBuffer(v, offset, count << 2));
	}

	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.glUniformMatrix2fv(location, count, transpose, value, getPosition(value));
	}
	
	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix2fv(location, count, transpose, toFloatBuffer(value, offset, count << 2));
	}

	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.glUniformMatrix3fv(location, count, transpose, value, getPosition(value));
	}
	
	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix3fv(location, count, transpose, toFloatBuffer(value, offset, count * 9));
	}

	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.glUniformMatrix4fv(location, count, transpose, value, getPosition(value));
	}
	
	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		glUniformMatrix4fv(location, count, transpose, toFloatBuffer(value, offset, count << 4));
	}

	public void glUseProgram (int program) {
		GL.glUseProgram(program);
	}

	public void glValidateProgram (int program) {
		GL.glValidateProgram(program);
	}

	public void glVertexAttrib1f (int indx, float x) {
		GL.glVertexAttrib1f(indx, x);
	}

	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		GL.glVertexAttrib1fv(indx, values, getPosition(values));
	}

	public void glVertexAttrib2f (int indx, float x, float y) {
		GL.glVertexAttrib2f(indx, x, y);
	}

	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		GL.glVertexAttrib2fv(indx, values, getPosition(values));
	}

	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GL.glVertexAttrib3f(indx, x, y, z);
	}

	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		GL.glVertexAttrib3fv(indx, values, getPosition(values));
	}

	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GL.glVertexAttrib4f(indx, x, y, z, w);
	}

	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		GL.glVertexAttrib4fv(indx, values, getPosition(values));
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		GL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr, getPosition(ptr));
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GL.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
}
