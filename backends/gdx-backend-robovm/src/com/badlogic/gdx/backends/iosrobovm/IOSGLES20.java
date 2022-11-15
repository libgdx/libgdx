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

import java.nio.*;

import com.badlogic.gdx.graphics.GL20;
import org.robovm.apple.foundation.NSProcessInfo;

public class IOSGLES20 implements GL20 {

	final boolean shouldConvert16bit = IOSApplication.IS_METALANGLE
		&& NSProcessInfo.getSharedProcessInfo().getEnvironment().containsKey("SIMULATOR_DEVICE_NAME");

	public IOSGLES20 () {
		init();
	}

	/** last viewport set, needed because GLKView resets the viewport on each call to render... amazing **/
	public static int x, y, width, height;

	private static native void init ();

	public native void glActiveTexture (int texture);

	public native void glAttachShader (int program, int shader);

	public native void glBindAttribLocation (int program, int index, String name);

	public native void glBindBuffer (int target, int buffer);

	public native void glBindFramebuffer (int target, int framebuffer);

	public native void glBindRenderbuffer (int target, int renderbuffer);

	public native void glBindTexture (int target, int texture);

	public native void glBlendColor (float red, float green, float blue, float alpha);

	public native void glBlendEquation (int mode);

	public native void glBlendEquationSeparate (int modeRGB, int modeAlpha);

	public native void glBlendFunc (int sfactor, int dfactor);

	public native void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha);

	public native void glBufferData (int target, int size, Buffer data, int usage);

	public native void glBufferSubData (int target, int offset, int size, Buffer data);

	public native int glCheckFramebufferStatus (int target);

	public native void glClear (int mask);

	public native void glClearColor (float red, float green, float blue, float alpha);

	public native void glClearDepthf (float depth);

	public native void glClearStencil (int s);

	public native void glColorMask (boolean red, boolean green, boolean blue, boolean alpha);

	public native void glCompileShader (int shader);

	public native void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data);

	public native void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height,
		int format, int imageSize, Buffer data);

	public native void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height,
		int border);

	public native void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);

	public native int glCreateProgram ();

	public native int glCreateShader (int type);

	public native void glCullFace (int mode);

	public native void glDeleteBuffers (int n, IntBuffer buffers);

	public native void glDeleteBuffer (int buffer);

	public native void glDeleteFramebuffers (int n, IntBuffer framebuffers);

	public native void glDeleteFramebuffer (int framebuffer);

	public native void glDeleteProgram (int program);

	public native void glDeleteRenderbuffers (int n, IntBuffer renderbuffers);

	public native void glDeleteRenderbuffer (int renderbuffer);

	public native void glDeleteShader (int shader);

	public native void glDeleteTextures (int n, IntBuffer textures);

	public native void glDeleteTexture (int texture);

	public native void glDepthFunc (int func);

	public native void glDepthMask (boolean flag);

	public native void glDepthRangef (float zNear, float zFar);

	public native void glDetachShader (int program, int shader);

	public native void glDisable (int cap);

	public native void glDisableVertexAttribArray (int index);

	public native void glDrawArrays (int mode, int first, int count);

	public native void glDrawElements (int mode, int count, int type, Buffer indices);

	public native void glDrawElements (int mode, int count, int type, int indices);

	public native void glEnable (int cap);

	public native void glEnableVertexAttribArray (int index);

	public native void glFinish ();

	public native void glFlush ();

	public native void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer);

	public native void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level);

	public native void glFrontFace (int mode);

	public native void glGenBuffers (int n, IntBuffer buffers);

	public native int glGenBuffer ();

	public native void glGenerateMipmap (int target);

	public native void glGenFramebuffers (int n, IntBuffer framebuffers);

	public native int glGenFramebuffer ();

	public native void glGenRenderbuffers (int n, IntBuffer renderbuffers);

	public native int glGenRenderbuffer ();

	public native void glGenTextures (int n, IntBuffer textures);

	public native int glGenTexture ();

	public native String glGetActiveAttrib (int program, int index, IntBuffer size, IntBuffer type);

	public native String glGetActiveUniform (int program, int index, IntBuffer size, IntBuffer type);

	public native void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders);

	public native int glGetAttribLocation (int program, String name);

	public native void glGetBooleanv (int pname, Buffer params);

	public native void glGetBufferParameteriv (int target, int pname, IntBuffer params);

	public native int glGetError ();

	public native void glGetFloatv (int pname, FloatBuffer params);

	public native void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params);

	public native void glGetIntegerv (int pname, IntBuffer params);

	public native void glGetProgramiv (int program, int pname, IntBuffer params);

	public native String glGetProgramInfoLog (int program);

	public native void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params);

	public native void glGetShaderiv (int shader, int pname, IntBuffer params);

	public native String glGetShaderInfoLog (int shader);

	public native void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision);

	public native void glGetShaderSource (int shader, int bufsize, Buffer length, String source);

	public native String glGetString (int name);

	public native void glGetTexParameterfv (int target, int pname, FloatBuffer params);

	public native void glGetTexParameteriv (int target, int pname, IntBuffer params);

	public native void glGetUniformfv (int program, int location, FloatBuffer params);

	public native void glGetUniformiv (int program, int location, IntBuffer params);

	public native int glGetUniformLocation (int program, String name);

	public native void glGetVertexAttribfv (int index, int pname, FloatBuffer params);

	public native void glGetVertexAttribiv (int index, int pname, IntBuffer params);

	public native void glGetVertexAttribPointerv (int index, int pname, Buffer pointer);

	public native void glHint (int target, int mode);

	public native boolean glIsBuffer (int buffer);

	public native boolean glIsEnabled (int cap);

	public native boolean glIsFramebuffer (int framebuffer);

	public native boolean glIsProgram (int program);

	public native boolean glIsRenderbuffer (int renderbuffer);

	public native boolean glIsShader (int shader);

	public native boolean glIsTexture (int texture);

	public native void glLineWidth (float width);

	public native void glLinkProgram (int program);

	public native void glPixelStorei (int pname, int param);

	public native void glPolygonOffset (float factor, float units);

	public native void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels);

	public native void glReleaseShaderCompiler ();

	public native void glRenderbufferStorage (int target, int internalformat, int width, int height);

	public native void glSampleCoverage (float value, boolean invert);

	public native void glScissor (int x, int y, int width, int height);

	public native void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length);

	public native void glShaderSource (int shader, String string);

	public native void glStencilFunc (int func, int ref, int mask);

	public native void glStencilFuncSeparate (int face, int func, int ref, int mask);

	public native void glStencilMask (int mask);

	public native void glStencilMaskSeparate (int face, int mask);

	public native void glStencilOp (int fail, int zfail, int zpass);

	public native void glStencilOpSeparate (int face, int fail, int zfail, int zpass);

	static Buffer convert16bitBufferToRGBA8888 (Buffer buffer, int type) {
		// TODO: 20.10.22 Can it be a different buffer type?
		ByteBuffer byteBuffer = (ByteBuffer)buffer;
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer converted = ByteBuffer.allocateDirect(byteBuffer.limit() * 2);
		while (buffer.remaining() != 0) {
			short color = byteBuffer.getShort();
			int rgba8888;

			if (type == GL_UNSIGNED_SHORT_4_4_4_4) {
				byte r = (byte)((color >> 12) & 0x0F);
				byte g = (byte)((color >> 8) & 0x0F);
				byte b = (byte)((color >> 4) & 0x0F);
				byte a = (byte)((color) & 0x0F);
				rgba8888 = (r << 4 | r) << 24 | (g << 4 | g) << 16 | (b << 4 | b) << 8 | (a << 4 | a);
			} else if (type == GL_UNSIGNED_SHORT_5_6_5) {
				byte r = (byte)((color >> 11) & 0x1F);
				byte g = (byte)((color >> 5) & 0x3F);
				byte b = (byte)((color) & 0x1F);
				rgba8888 = (r << 3 | r >> 2) << 24 | (g << 2 | g >> 4) << 16 | (b << 3 | b >> 2) << 8 | 0xFF;
			} else {
				byte r = (byte)((color >> 11) & 0x1F);
				byte g = (byte)((color >> 6) & 0x1F);
				byte b = (byte)((color >> 5) & 0x1F);
				byte a = (byte)((color) & 0x1);
				rgba8888 = (r << 3 | r >> 2) << 24 | (g << 3 | g >> 2) << 16 | (b << 3 | b >> 2) << 8 | a * 255;
			}
			converted.putInt(rgba8888);
		}
		converted.position(0);
		return converted;
	}

	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		if (!shouldConvert16bit) {
			glTexImage2DJNI(target, level, internalformat, width, height, border, format, type, pixels);
			return;
		}
		if (type != GL_UNSIGNED_SHORT_5_6_5 && type != GL_UNSIGNED_SHORT_5_5_5_1 && type != GL_UNSIGNED_SHORT_4_4_4_4) {
			glTexImage2DJNI(target, level, internalformat, width, height, border, format, type, pixels);
			return;
		}
		Buffer converted = convert16bitBufferToRGBA8888(pixels, type);
		glTexImage2DJNI(target, level, GL_RGBA, width, height, border, GL_RGBA, GL_UNSIGNED_BYTE, converted);
	}

	public native void glTexImage2DJNI (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, Buffer pixels);

	public native void glTexParameterf (int target, int pname, float param);

	public native void glTexParameterfv (int target, int pname, FloatBuffer params);

	public native void glTexParameteri (int target, int pname, int param);

	public native void glTexParameteriv (int target, int pname, IntBuffer params);

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		if (!shouldConvert16bit) {
			glTexSubImage2DJNI(target, level, xoffset, yoffset, width, height, format, type, pixels);
			return;
		}
		if (type != GL_UNSIGNED_SHORT_5_6_5 && type != GL_UNSIGNED_SHORT_5_5_5_1 && type != GL_UNSIGNED_SHORT_4_4_4_4) {
			glTexSubImage2DJNI(target, level, xoffset, yoffset, width, height, format, type, pixels);
			return;
		}
		Buffer converted = convert16bitBufferToRGBA8888(pixels, type);
		glTexSubImage2DJNI(target, level, xoffset, yoffset, width, height, GL_RGBA, GL_UNSIGNED_BYTE, converted);
	}

	public native void glTexSubImage2DJNI (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, Buffer pixels);

	public native void glUniform1f (int location, float x);

	public native void glUniform1fv (int location, int count, FloatBuffer v);

	public native void glUniform1fv (int location, int count, float[] v, int offset);

	public native void glUniform1i (int location, int x);

	public native void glUniform1iv (int location, int count, IntBuffer v);

	public native void glUniform1iv (int location, int count, int[] v, int offset);

	public native void glUniform2f (int location, float x, float y);

	public native void glUniform2fv (int location, int count, FloatBuffer v);

	public native void glUniform2fv (int location, int count, float[] v, int offset);

	public native void glUniform2i (int location, int x, int y);

	public native void glUniform2iv (int location, int count, IntBuffer v);

	public native void glUniform2iv (int location, int count, int[] v, int offset);

	public native void glUniform3f (int location, float x, float y, float z);

	public native void glUniform3fv (int location, int count, FloatBuffer v);

	public native void glUniform3fv (int location, int count, float[] v, int offset);

	public native void glUniform3i (int location, int x, int y, int z);

	public native void glUniform3iv (int location, int count, IntBuffer v);

	public native void glUniform3iv (int location, int count, int[] v, int offset);

	public native void glUniform4f (int location, float x, float y, float z, float w);

	public native void glUniform4fv (int location, int count, FloatBuffer v);

	public native void glUniform4fv (int location, int count, float[] v, int offset);

	public native void glUniform4i (int location, int x, int y, int z, int w);

	public native void glUniform4iv (int location, int count, IntBuffer v);

	public native void glUniform4iv (int location, int count, int[] v, int offset);

	public native void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value);

	public native void glUniformMatrix2fv (int location, int count, boolean transpose, float[] value, int offset);

	public native void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value);

	public native void glUniformMatrix3fv (int location, int count, boolean transpose, float[] value, int offset);

	public native void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value);

	public native void glUniformMatrix4fv (int location, int count, boolean transpose, float[] value, int offset);

	public native void glUseProgram (int program);

	public native void glValidateProgram (int program);

	public native void glVertexAttrib1f (int indx, float x);

	public native void glVertexAttrib1fv (int indx, FloatBuffer values);

	public native void glVertexAttrib2f (int indx, float x, float y);

	public native void glVertexAttrib2fv (int indx, FloatBuffer values);

	public native void glVertexAttrib3f (int indx, float x, float y, float z);

	public native void glVertexAttrib3fv (int indx, FloatBuffer values);

	public native void glVertexAttrib4f (int indx, float x, float y, float z, float w);

	public native void glVertexAttrib4fv (int indx, FloatBuffer values);

	public native void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr);

	public native void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr);

	public void glViewport (int x, int y, int width, int height) {
		IOSGLES20.x = x;
		IOSGLES20.y = y;
		IOSGLES20.width = width;
		IOSGLES20.height = height;
		glViewportJni(x, y, width, height);
	}

	public native void glViewportJni (int x, int y, int width, int height);
}
