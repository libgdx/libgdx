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

package com.badlogic.gdx.backends.ios;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import cli.OpenTK.Graphics.ES20.BeginMode;
import cli.OpenTK.Graphics.ES20.BlendingFactorDest;
import cli.OpenTK.Graphics.ES20.BlendingFactorSrc;
import cli.OpenTK.Graphics.ES20.BufferTarget;
import cli.OpenTK.Graphics.ES20.ClearBufferMask;
import cli.OpenTK.Graphics.ES20.CullFaceMode;
import cli.OpenTK.Graphics.ES20.DepthFunction;
import cli.OpenTK.Graphics.ES20.DrawElementsType;
import cli.OpenTK.Graphics.ES20.EnableCap;
import cli.OpenTK.Graphics.ES20.FrontFaceDirection;
import cli.OpenTK.Graphics.ES20.GL;
import cli.OpenTK.Graphics.ES20.GetPName;
import cli.OpenTK.Graphics.ES20.HintMode;
import cli.OpenTK.Graphics.ES20.HintTarget;
import cli.OpenTK.Graphics.ES20.PixelFormat;
import cli.OpenTK.Graphics.ES20.PixelInternalFormat;
import cli.OpenTK.Graphics.ES20.PixelStoreParameter;
import cli.OpenTK.Graphics.ES20.PixelType;
import cli.OpenTK.Graphics.ES20.ProgramParameter;
import cli.OpenTK.Graphics.ES20.ShaderParameter;
import cli.OpenTK.Graphics.ES20.ShaderType;
import cli.OpenTK.Graphics.ES20.StringName;
import cli.OpenTK.Graphics.ES20.TextureTarget;
import cli.OpenTK.Graphics.ES20.TextureUnit;
import cli.System.IntPtr;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.utils.BufferUtils;

public class IOSMonotouchGLES20 implements GL20, GLCommon {
	
	private final int[] buffer1 = new int[1];

	@Override
	public void glActiveTexture (int texture) {
		GL.ActiveTexture(TextureUnit.wrap(texture));
	}

	@Override
	public void glBindTexture (int target, int texture) {
		GL.BindTexture(TextureTarget.wrap(target), texture);
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		GL.BlendFunc(BlendingFactorSrc.wrap(sfactor), BlendingFactorDest.wrap(dfactor));
	}

	@Override
	public void glClear (int mask) {
		GL.Clear(ClearBufferMask.wrap(mask));
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		GL.ClearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf (float depth) {
		GL.ClearDepth(depth);
	}

	@Override
	public void glClearStencil (int s) {
		GL.ClearStencil(s);
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		GL.ColorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		GL.CompressedTexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat), //
			width, height, border, imageSize, IntPtr.op_Explicit(BufferUtils.getUnsafeByteBufferAddress((ByteBuffer)data)));
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GL.CompressedTexSubImage2D(TextureTarget.wrap(target), level, xoffset, yoffset, width, height, PixelFormat.wrap(format),
			imageSize, IntPtr.op_Explicit(BufferUtils.getUnsafeByteBufferAddress((ByteBuffer)data)));
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		GL.CopyTexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat), x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		GL.CopyTexSubImage2D(TextureTarget.wrap(target), level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace (int mode) {
		GL.CullFace(CullFaceMode.wrap(mode));
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		GL.DeleteTextures(n, textures.array());
	}

	@Override
	public void glDepthFunc (int func) {
		GL.DepthFunc(DepthFunction.wrap(func));
	}

	@Override
	public void glDepthMask (boolean flag) {
		GL.DepthMask(flag);
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		GL.DepthRange(zNear, zFar);
	}

	@Override
	public void glDisable (int cap) {
		GL.Disable(EnableCap.wrap(cap));
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		GL.DrawArrays(BeginMode.wrap(mode), first, count);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		GL.DrawElements(BeginMode.wrap(mode), count, DrawElementsType.wrap(type), //
			IntPtr.op_Explicit(BufferUtils.getUnsafeByteBufferAddress((ByteBuffer)indices)));
	}

	@Override
	public void glEnable (int cap) {
		GL.Enable(EnableCap.wrap(cap));
	}

	@Override
	public void glFinish () {
		GL.Finish();
	}

	@Override
	public void glFlush () {
		GL.Flush();
	}

	@Override
	public void glFrontFace (int mode) {
		GL.FrontFace(FrontFaceDirection.wrap(mode));
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		GL.GenTextures(n, buffer1);
		textures.put(buffer1, 0, 1);
	}

	@Override
	public int glGetError () {
		return GL.GetError().Value;
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		GL.GetInteger(GetPName.wrap(pname), params.array());
	}

	@Override
	public String glGetString (int name) {
		return GL.GetString(StringName.wrap(name));
	}

	@Override
	public void glHint (int target, int mode) {
		GL.Hint(HintTarget.wrap(target), HintMode.wrap(mode));
	}

	@Override
	public void glLineWidth (float width) {
		GL.LineWidth(width);
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		GL.PixelStore(PixelStoreParameter.wrap(pname), param);
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		GL.PolygonOffset(factor, units);
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		GL.ReadPixels(x, y, width, height, PixelFormat.wrap(format), PixelType.wrap(type), //
			IntPtr.op_Explicit(BufferUtils.getUnsafeByteBufferAddress((ByteBuffer)pixels)));
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glStencilMask (int mask) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		GL.TexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat), width, height, border,
			PixelFormat.wrap(format), PixelType.wrap(type),
			IntPtr.op_Explicit(BufferUtils.getUnsafeByteBufferAddress((ByteBuffer)pixels)));
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		GL.Viewport(x, y, width, height);
	}

	@Override
	public void glAttachShader (int program, int shader) {
		GL.AttachShader(program, shader);
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		GL.BindAttribLocation(program, index, name);
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		GL.BindBuffer(BufferTarget.wrap(target), buffer);
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		// TODO Auto-generated function stub
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		// TODO Auto-generated function stub
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glBlendEquation (int mode) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		// TODO Auto-generated function stub

	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		// TODO Auto-generated function stub
		return 0;

	}

	@Override
	public void glCompileShader (int shader) {
		GL.CompileShader(shader);
	}

	@Override
	public int glCreateProgram () {
		return GL.CreateProgram();
	}

	@Override
	public int glCreateShader (int type) {
		return GL.CreateShader(ShaderType.wrap(type));
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDeleteProgram (int program) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDeleteShader (int shader) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDetachShader (int program, int shader) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGenerateMipmap (int target) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		// TODO Auto-generated function stub

	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated function stub
		return null;

	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		// TODO Auto-generated function stub
		return null;

	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		// TODO Auto-generated function stub

	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		// TODO Auto-generated function stub
		return 0;

	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL.GetProgram(program, ProgramParameter.wrap(pname), buffer1);
		params.put(buffer1, 0, buffer1.length);
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		return GL.GetProgramInfoLog(program);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}
	
	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL.GetShader(shader, ShaderParameter.wrap(pname), buffer1);
		params.put(buffer1, 0, buffer1.length);
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		return GL.GetShaderInfoLog(shader);
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		return GL.GetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		// TODO Auto-generated function stub

	}

	@Override
	public boolean glIsBuffer (int buffer) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsEnabled (int cap) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsProgram (int program) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsShader (int shader) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsTexture (int texture) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public void glLinkProgram (int program) {
		GL.LinkProgram(program);
	}

	@Override
	public void glReleaseShaderCompiler () {
		// TODO Auto-generated function stub

	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glShaderSource (int shader, String string) {
		GL.ShaderSource(shader, string);
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glUniform1f (int location, float x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		GL.Uniform1(location, count, v.array());
	}

	@Override
	public void glUniform1i (int location, int x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		GL.Uniform1(location, count, v.array());
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		GL.Uniform2(location, count, v.array());
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		GL.Uniform2(location, count, v.array());
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		GL.Uniform3(location, count, v.array());
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		GL.Uniform3(location, count, v.array());
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		GL.Uniform4(location, count, v.array());
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		GL.Uniform4(location, count, v.array());
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.UniformMatrix2(location, count, transpose, value.array());
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.UniformMatrix3(location, count, transpose, value.array());
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL.UniformMatrix4(location, count, transpose, value.array());
	}

	@Override
	public void glUseProgram (int program) {
		GL.UseProgram(program);
	}

	@Override
	public void glValidateProgram (int program) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		// TODO Auto-generated function stub

	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		// TODO Auto-generated function stub

	}

}
