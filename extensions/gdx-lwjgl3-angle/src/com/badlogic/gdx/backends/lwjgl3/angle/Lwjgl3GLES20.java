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

package com.badlogic.gdx.backends.lwjgl3.angle;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.opengles.GLES20;

import java.nio.*;

public class Lwjgl3GLES20 implements GL20 {
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
		((Buffer)floatBuffer).clear();
		((Buffer)floatBuffer).limit(count);
		floatBuffer.put(v, offset, count);
		((Buffer)floatBuffer).position(0);
		return floatBuffer;
	}

	private IntBuffer toIntBuffer (int v[], int offset, int count) {
		ensureBufferCapacity(count << 2);
		((Buffer)intBuffer).clear();
		((Buffer)intBuffer).limit(count);
		intBuffer.put(v, offset, count);
		((Buffer)intBuffer).position(0);
		return intBuffer;
	}

	public void glActiveTexture (int texture) {
		GLES20.glActiveTexture(texture);
	}

	public void glAttachShader (int program, int shader) {
		GLES20.glAttachShader(program, shader);
	}

	public void glBindAttribLocation (int program, int index, String name) {
		GLES20.glBindAttribLocation(program, index, name);
	}

	public void glBindBuffer (int target, int buffer) {
		GLES20.glBindBuffer(target, buffer);
	}

	public void glBindFramebuffer (int target, int framebuffer) {
		GLES20.glBindFramebuffer(target, framebuffer);
	}

	public void glBindRenderbuffer (int target, int renderbuffer) {
		GLES20.glBindRenderbuffer(target, renderbuffer);
	}

	public void glBindTexture (int target, int texture) {
		GLES20.glBindTexture(target, texture);
	}

	public void glBlendColor (float red, float green, float blue, float alpha) {
		GLES20.glBlendColor(red, green, blue, alpha);
	}

	public void glBlendEquation (int mode) {
		GLES20.glBlendEquation(mode);
	}

	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GLES20.glBlendEquationSeparate(modeRGB, modeAlpha);
	}

	public void glBlendFunc (int sfactor, int dfactor) {
		GLES20.glBlendFunc(sfactor, dfactor);
	}

	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLES20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	public void glBufferData (int target, int size, Buffer data, int usage) {
		if (data == null)
			GLES20.glBufferData(target, size, usage);
		else if (data instanceof ByteBuffer)
			GLES20.glBufferData(target, (ByteBuffer)data, usage);
		else if (data instanceof IntBuffer)
			GLES20.glBufferData(target, (IntBuffer)data, usage);
		else if (data instanceof FloatBuffer)
			GLES20.glBufferData(target, (FloatBuffer)data, usage);
		else if (data instanceof ShortBuffer) //
			GLES20.glBufferData(target, (ShortBuffer)data, usage);
		else
			throw new GdxRuntimeException("Buffer data of type " + data.getClass().getName() + " not supported in GLES20.");
	}

	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		if (data == null)
			throw new GdxRuntimeException("Using null for the data not possible, ");
		else if (data instanceof ByteBuffer)
			GLES20.glBufferSubData(target, offset, (ByteBuffer)data);
		else if (data instanceof IntBuffer)
			GLES20.glBufferSubData(target, offset, (IntBuffer)data);
		else if (data instanceof FloatBuffer)
			GLES20.glBufferSubData(target, offset, (FloatBuffer)data);
		else if (data instanceof ShortBuffer) //
			GLES20.glBufferSubData(target, offset, (ShortBuffer)data);
		else
			throw new GdxRuntimeException("Buffer data of type " + data.getClass().getName() + " not supported in GLES20.");
	}

	public int glCheckFramebufferStatus (int target) {
		return GLES20.glCheckFramebufferStatus(target);
	}

	public void glClear (int mask) {
		GLES20.glClear(mask);
	}

	public void glClearColor (float red, float green, float blue, float alpha) {
		GLES20.glClearColor(red, green, blue, alpha);
	}

	public void glClearDepthf (float depth) {
		GLES20.glClearDepthf(depth);
	}

	public void glClearStencil (int s) {
		GLES20.glClearStencil(s);
	}

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GLES20.glColorMask(red, green, blue, alpha);
	}

	public void glCompileShader (int shader) {
		GLES20.glCompileShader(shader);
	}

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		if (data instanceof ByteBuffer) {
			GLES20.glCompressedTexImage2D(target, level, internalformat, width, height, border, (ByteBuffer)data);
		} else {
			throw new GdxRuntimeException("Can't use " + data.getClass().getName() + " with this method. Use ByteBuffer instead.");
		}
	}

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("not implemented");
	}

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GLES20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
	}

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GLES20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
	}

	public int glCreateProgram () {
		return GLES20.glCreateProgram();
	}

	public int glCreateShader (int type) {
		return GLES20.glCreateShader(type);
	}

	public void glCullFace (int mode) {
		GLES20.glCullFace(mode);
	}

	public void glDeleteBuffers (int n, IntBuffer buffers) {
		GLES20.glDeleteBuffers(buffers);
	}

	@Override
	public void glDeleteBuffer (int buffer) {
		GLES20.glDeleteBuffers(buffer);
	}

	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GLES20.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		GLES20.glDeleteFramebuffers(framebuffer);
	}

	public void glDeleteProgram (int program) {
		GLES20.glDeleteProgram(program);
	}

	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GLES20.glDeleteRenderbuffers(renderbuffers);
	}

	public void glDeleteRenderbuffer (int renderbuffer) {
		GLES20.glDeleteRenderbuffers(renderbuffer);
	}

	public void glDeleteShader (int shader) {
		GLES20.glDeleteShader(shader);
	}

	public void glDeleteTextures (int n, IntBuffer textures) {
		GLES20.glDeleteTextures(textures);
	}

	@Override
	public void glDeleteTexture (int texture) {
		GLES20.glDeleteTextures(texture);
	}

	public void glDepthFunc (int func) {
		GLES20.glDepthFunc(func);
	}

	public void glDepthMask (boolean flag) {
		GLES20.glDepthMask(flag);
	}

	public void glDepthRangef (float zNear, float zFar) {
		GLES20.glDepthRangef(zNear, zFar);
	}

	public void glDetachShader (int program, int shader) {
		GLES20.glDetachShader(program, shader);
	}

	public void glDisable (int cap) {
		GLES20.glDisable(cap);
	}

	public void glDisableVertexAttribArray (int index) {
		GLES20.glDisableVertexAttribArray(index);
	}

	public void glDrawArrays (int mode, int first, int count) {
		GLES20.glDrawArrays(mode, first, count);
	}

	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GLES20.glDrawElements(mode, (ShortBuffer)indices);
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT)
			GLES20.glDrawElements(mode, ((ByteBuffer)indices).asShortBuffer());
		else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE)
			GLES20.glDrawElements(mode, (ByteBuffer)indices);
		else
			throw new GdxRuntimeException(
				"Can't use " + indices.getClass().getName() + " with this method. Use ShortBuffer or ByteBuffer instead.");
	}

	public void glEnable (int cap) {
		GLES20.glEnable(cap);
	}

	public void glEnableVertexAttribArray (int index) {
		GLES20.glEnableVertexAttribArray(index);
	}

	public void glFinish () {
		GLES20.glFinish();
	}

	public void glFlush () {
		GLES20.glFlush();
	}

	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GLES20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GLES20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	public void glFrontFace (int mode) {
		GLES20.glFrontFace(mode);
	}

	public void glGenBuffers (int n, IntBuffer buffers) {
		GLES20.glGenBuffers(buffers);
	}

	public int glGenBuffer () {
		return GLES20.glGenBuffers();
	}

	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GLES20.glGenFramebuffers(framebuffers);
	}

	public int glGenFramebuffer () {
		return GLES20.glGenFramebuffers();
	}

	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GLES20.glGenRenderbuffers(renderbuffers);
	}

	public int glGenRenderbuffer () {
		return GLES20.glGenRenderbuffers();
	}

	public void glGenTextures (int n, IntBuffer textures) {
		GLES20.glGenTextures(textures);
	}

	public int glGenTexture () {
		return GLES20.glGenTextures();
	}

	public void glGenerateMipmap (int target) {
		GLES20.glGenerateMipmap(target);
	}

	public String glGetActiveAttrib (int program, int index, IntBuffer size, IntBuffer type) {
		return GLES20.glGetActiveAttrib(program, index, 256, size, type);
	}

	public String glGetActiveUniform (int program, int index, IntBuffer size, IntBuffer type) {
		return GLES20.glGetActiveUniform(program, index, 256, size, type);
	}

	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GLES20.glGetAttachedShaders(program, (IntBuffer)count, shaders);
	}

	public int glGetAttribLocation (int program, String name) {
		return GLES20.glGetAttribLocation(program, name);
	}

	public void glGetBooleanv (int pname, Buffer params) {
		GLES20.glGetBooleanv(pname, (ByteBuffer)params);
	}

	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glGetBufferParameteriv(target, pname, params);
	}

	public int glGetError () {
		return GLES20.glGetError();
	}

	public void glGetFloatv (int pname, FloatBuffer params) {
		GLES20.glGetFloatv(pname, params);
	}

	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		GLES20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
	}

	public void glGetIntegerv (int pname, IntBuffer params) {
		GLES20.glGetIntegerv(pname, params);
	}

	public String glGetProgramInfoLog (int program) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLES20.glGetProgramInfoLog(program, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GLES20.glGetProgramiv(program, pname, params);
	}

	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glGetRenderbufferParameteriv(target, pname, params);
	}

	public String glGetShaderInfoLog (int shader) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 10);
		buffer.order(ByteOrder.nativeOrder());
		ByteBuffer tmp = ByteBuffer.allocateDirect(4);
		tmp.order(ByteOrder.nativeOrder());
		IntBuffer intBuffer = tmp.asIntBuffer();

		GLES20.glGetShaderInfoLog(shader, intBuffer, buffer);
		int numBytes = intBuffer.get(0);
		byte[] bytes = new byte[numBytes];
		buffer.get(bytes);
		return new String(bytes);
	}

	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GLES20.glGetShaderiv(shader, pname, params);
	}

	public String glGetString (int name) {
		return GLES20.glGetString(name);
	}

	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GLES20.glGetTexParameterfv(target, pname, params);
	}

	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glGetTexParameteriv(target, pname, params);
	}

	public int glGetUniformLocation (int program, String name) {
		return GLES20.glGetUniformLocation(program, name);
	}

	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GLES20.glGetUniformfv(program, location, params);
	}

	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GLES20.glGetUniformiv(program, location, params);
	}

	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GLES20.glGetVertexAttribfv(index, pname, params);
	}

	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GLES20.glGetVertexAttribiv(index, pname, params);
	}

	public void glHint (int target, int mode) {
		GLES20.glHint(target, mode);
	}

	public boolean glIsBuffer (int buffer) {
		return GLES20.glIsBuffer(buffer);
	}

	public boolean glIsEnabled (int cap) {
		return GLES20.glIsEnabled(cap);
	}

	public boolean glIsFramebuffer (int framebuffer) {
		return GLES20.glIsFramebuffer(framebuffer);
	}

	public boolean glIsProgram (int program) {
		return GLES20.glIsProgram(program);
	}

	public boolean glIsRenderbuffer (int renderbuffer) {
		return GLES20.glIsRenderbuffer(renderbuffer);
	}

	public boolean glIsShader (int shader) {
		return GLES20.glIsShader(shader);
	}

	public boolean glIsTexture (int texture) {
		return GLES20.glIsTexture(texture);
	}

	public void glLineWidth (float width) {
		GLES20.glLineWidth(width);
	}

	public void glLinkProgram (int program) {
		GLES20.glLinkProgram(program);
	}

	public void glPixelStorei (int pname, int param) {
		GLES20.glPixelStorei(pname, param);
	}

	public void glPolygonOffset (float factor, float units) {
		GLES20.glPolygonOffset(factor, units);
	}

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GLES20.glReadPixels(x, y, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GLES20.glReadPixels(x, y, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GLES20.glReadPixels(x, y, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GLES20.glReadPixels(x, y, width, height, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer or FloatBuffer instead.");
	}

	public void glReleaseShaderCompiler () {
		// nothing to do here
	}

	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GLES20.glRenderbufferStorage(target, internalformat, width, height);
	}

	public void glSampleCoverage (float value, boolean invert) {
		GLES20.glSampleCoverage(value, invert);
	}

	public void glScissor (int x, int y, int width, int height) {
		GLES20.glScissor(x, y, width, height);
	}

	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new UnsupportedOperationException("unsupported, won't implement");
	}

	public void glShaderSource (int shader, String string) {
		GLES20.glShaderSource(shader, string);
	}

	public void glStencilFunc (int func, int ref, int mask) {
		GLES20.glStencilFunc(func, ref, mask);
	}

	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GLES20.glStencilFuncSeparate(face, func, ref, mask);
	}

	public void glStencilMask (int mask) {
		GLES20.glStencilMask(mask);
	}

	public void glStencilMaskSeparate (int face, int mask) {
		GLES20.glStencilMaskSeparate(face, mask);
	}

	public void glStencilOp (int fail, int zfail, int zpass) {
		GLES20.glStencilOp(fail, zfail, zpass);
	}

	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GLES20.glStencilOpSeparate(face, fail, zfail, zpass);
	}

	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		if (pixels == null)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GLES20.glTexImage2D(target, level, internalformat, width, height, border, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead.");
	}

	public void glTexParameterf (int target, int pname, float param) {
		GLES20.glTexParameterf(target, pname, param);
	}

	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		GLES20.glTexParameterfv(target, pname, params);
	}

	public void glTexParameteri (int target, int pname, int param) {
		GLES20.glTexParameteri(target, pname, param);
	}

	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		GLES20.glTexParameteriv(target, pname, params);
	}

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		if (pixels instanceof ByteBuffer)
			GLES20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GLES20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GLES20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GLES20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, (FloatBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead.");
	}

	public void glUniform1f (int location, float x) {
		GLES20.glUniform1f(location, x);
	}

	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GLES20.glUniform1fv(location, v);
	}

	public void glUniform1fv (int location, int count, float[] v, int offset) {
		GLES20.glUniform1fv(location, toFloatBuffer(v, offset, count));
	}

	public void glUniform1i (int location, int x) {
		GLES20.glUniform1i(location, x);
	}

	public void glUniform1iv (int location, int count, IntBuffer v) {
		GLES20.glUniform1iv(location, v);
	}

	@Override
	public void glUniform1iv (int location, int count, int[] v, int offset) {
		GLES20.glUniform1iv(location, toIntBuffer(v, offset, count));
	}

	public void glUniform2f (int location, float x, float y) {
		GLES20.glUniform2f(location, x, y);
	}

	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GLES20.glUniform2fv(location, v);
	}

	public void glUniform2fv (int location, int count, float[] v, int offset) {
		GLES20.glUniform2fv(location, toFloatBuffer(v, offset, count << 1));
	}

	public void glUniform2i (int location, int x, int y) {
		GLES20.glUniform2i(location, x, y);
	}

	public void glUniform2iv (int location, int count, IntBuffer v) {
		GLES20.glUniform2iv(location, v);
	}

	public void glUniform2iv (int location, int count, int[] v, int offset) {
		GLES20.glUniform2iv(location, toIntBuffer(v, offset, count << 1));
	}

	public void glUniform3f (int location, float x, float y, float z) {
		GLES20.glUniform3f(location, x, y, z);
	}

	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GLES20.glUniform3fv(location, v);
	}

	public void glUniform3fv (int location, int count, float[] v, int offset) {
		GLES20.glUniform3fv(location, toFloatBuffer(v, offset, count * 3));
	}

	public void glUniform3i (int location, int x, int y, int z) {
		GLES20.glUniform3i(location, x, y, z);
	}

	public void glUniform3iv (int location, int count, IntBuffer v) {
		GLES20.glUniform3iv(location, v);
	}

	public void glUniform3iv (int location, int count, int[] v, int offset) {
		GLES20.glUniform3iv(location, toIntBuffer(v, offset, count * 3));
	}

	public void glUniform4f (int location, float x, float y, float z, float w) {
		GLES20.glUniform4f(location, x, y, z, w);
	}

	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GLES20.glUniform4fv(location, v);
	}

	public void glUniform4fv (int location, int count, float[] v, int offset) {
		GLES20.glUniform4fv(location, toFloatBuffer(v, offset, count << 2));
	}

	public void glUniform4i (int location, int x, int y, int z, int w) {
		GLES20.glUniform4i(location, x, y, z, w);
	}

	public void glUniform4iv (int location, int count, IntBuffer v) {
		GLES20.glUniform4iv(location, v);
	}

	public void glUniform4iv (int location, int count, int[] v, int offset) {
		GLES20.glUniform4iv(location, toIntBuffer(v, offset, count << 2));
	}

	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLES20.glUniformMatrix2fv(location, transpose, value);
	}

	public void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset) {
		GLES20.glUniformMatrix2fv(location, transpose, toFloatBuffer(value, offset, count << 2));
	}

	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLES20.glUniformMatrix3fv(location, transpose, value);
	}

	public void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset) {
		GLES20.glUniformMatrix3fv(location, transpose, toFloatBuffer(value, offset, count * 9));
	}

	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GLES20.glUniformMatrix4fv(location, transpose, value);
	}

	public void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset) {
		GLES20.glUniformMatrix4fv(location, transpose, toFloatBuffer(value, offset, count << 4));
	}

	public void glUseProgram (int program) {
		GLES20.glUseProgram(program);
	}

	public void glValidateProgram (int program) {
		GLES20.glValidateProgram(program);
	}

	public void glVertexAttrib1f (int indx, float x) {
		GLES20.glVertexAttrib1f(indx, x);
	}

	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		GLES20.glVertexAttrib1f(indx, values.get());
	}

	public void glVertexAttrib2f (int indx, float x, float y) {
		GLES20.glVertexAttrib2f(indx, x, y);
	}

	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		GLES20.glVertexAttrib2f(indx, values.get(), values.get());
	}

	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GLES20.glVertexAttrib3f(indx, x, y, z);
	}

	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		GLES20.glVertexAttrib3f(indx, values.get(), values.get(), values.get());
	}

	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GLES20.glVertexAttrib4f(indx, x, y, z, w);
	}

	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		GLES20.glVertexAttrib4f(indx, values.get(), values.get(), values.get(), values.get());
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer buffer) {
		if (buffer instanceof ByteBuffer) {
			if (type == GL_BYTE)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer)buffer);
			else if (type == GL_UNSIGNED_BYTE)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, (ByteBuffer)buffer);
			else if (type == GL_SHORT)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asShortBuffer());
			else if (type == GL_UNSIGNED_SHORT)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asShortBuffer());
			else if (type == GL_FLOAT)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ((ByteBuffer)buffer).asFloatBuffer());
			else
				throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with type " + type
					+ " with this method. Use ByteBuffer and one of GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT or GL_FLOAT for type.");
		} else if (buffer instanceof FloatBuffer) {
			if (type == GL_FLOAT)
				GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, (FloatBuffer)buffer);
			else
				throw new GdxRuntimeException(
					"Can't use " + buffer.getClass().getName() + " with type " + type + " with this method.");
		} else
			throw new GdxRuntimeException("Can't use " + buffer.getClass().getName() + " with this method. Use ByteBuffer instead.");
	}

	public void glViewport (int x, int y, int width, int height) {
		GLES20.glViewport(x, y, width, height);
	}

	public void glDrawElements (int mode, int count, int type, int indices) {
		GLES20.glDrawElements(mode, count, type, indices);
	}

	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GLES20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
	}
}
