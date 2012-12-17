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

import cli.OpenTK.Graphics.ES20.ActiveAttribType;
import cli.OpenTK.Graphics.ES20.ActiveUniformType;
import cli.OpenTK.Graphics.ES20.BeginMode;
import cli.OpenTK.Graphics.ES20.BlendEquationMode;
import cli.OpenTK.Graphics.ES20.BlendingFactorDest;
import cli.OpenTK.Graphics.ES20.BlendingFactorSrc;
import cli.OpenTK.Graphics.ES20.BufferTarget;
import cli.OpenTK.Graphics.ES20.ClearBufferMask;
import cli.OpenTK.Graphics.ES20.CullFaceMode;
import cli.OpenTK.Graphics.ES20.DepthFunction;
import cli.OpenTK.Graphics.ES20.DrawElementsType;
import cli.OpenTK.Graphics.ES20.EnableCap;
import cli.OpenTK.Graphics.ES20.FramebufferSlot;
import cli.OpenTK.Graphics.ES20.FramebufferTarget;
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
import cli.OpenTK.Graphics.ES20.RenderbufferInternalFormat;
import cli.OpenTK.Graphics.ES20.RenderbufferTarget;
import cli.OpenTK.Graphics.ES20.ShaderParameter;
import cli.OpenTK.Graphics.ES20.ShaderType;
import cli.OpenTK.Graphics.ES20.StencilFunction;
import cli.OpenTK.Graphics.ES20.StencilOp;
import cli.OpenTK.Graphics.ES20.StringName;
import cli.OpenTK.Graphics.ES20.TextureParameterName;
import cli.OpenTK.Graphics.ES20.TextureTarget;
import cli.OpenTK.Graphics.ES20.TextureUnit;
import cli.OpenTK.Graphics.ES20.VertexAttribPointerType;
import cli.System.IntPtr;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.utils.BufferUtils;

public class IOSMonotouchGLES20 implements GL20, GLCommon {

	private final int[] intArray = new int[32];
	private final float[] floatArray = new float[32];

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
			width, height, border, imageSize, IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(data)));
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		GL.CompressedTexSubImage2D(TextureTarget.wrap(target), level, xoffset, yoffset, width, height, PixelFormat.wrap(format),
			imageSize, IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(data)));
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
	public void glDeleteTextures (int n, IntBuffer buffer) {
		buffer.get(intArray, 0, buffer.remaining());
		GL.DeleteTextures(n, intArray);
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
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(indices)));
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
		GL.GenTextures(n, intArray);
		textures.put(intArray, 0, textures.remaining());
	}

	@Override
	public int glGetError () {
		return GL.GetError().Value;
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		// uses it directly since it should be a direct buffer?...
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
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(pixels)));
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		GL.Scissor(x, y, width, height);
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		GL.StencilFunc(StencilFunction.wrap(func), ref, mask);
	}

	@Override
	public void glStencilMask (int mask) {
		GL.StencilMask(mask);
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		GL.StencilOp(StencilOp.wrap(fail), StencilOp.wrap(zfail), StencilOp.wrap(zpass));
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		GL.TexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat), width, height, border,
			PixelFormat.wrap(format), PixelType.wrap(type), IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(pixels)));
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), param);
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		GL.TexSubImage2D(TextureTarget.wrap(target), level, xoffset, yoffset, width, height, PixelFormat.wrap(format),
			PixelType.wrap(type), IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(pixels)));
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
		GL.BindFramebuffer(FramebufferTarget.wrap(target), framebuffer);
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		GL.BindRenderbuffer(RenderbufferTarget.wrap(target), renderbuffer);
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		GL.BlendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation (int mode) {
		GL.BlendEquation(BlendEquationMode.wrap(mode));
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		GL.BlendEquationSeparate(BlendEquationMode.wrap(modeRGB), BlendEquationMode.wrap(modeAlpha));
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL.BlendFuncSeparate(BlendingFactorSrc.wrap(srcRGB), BlendingFactorDest.wrap(dstRGB), BlendingFactorSrc.wrap(srcAlpha),
			BlendingFactorDest.wrap(dstAlpha));
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("OpenTK API expects an IntPtr for the size which seems to be wrong");
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("OpenTK API expects an IntPtr for the size and offset which seems to be wrong");
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		return GL.CheckFramebufferStatus(FramebufferTarget.wrap(target)).Value;
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
	public void glDeleteBuffers (int n, IntBuffer buffer) {
		buffer.get(intArray, 0, buffer.remaining());
		GL.DeleteBuffers(n, intArray);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer buffer) {
		buffer.get(intArray, 0, buffer.remaining());
		GL.DeleteFramebuffers(n, intArray);
	}

	@Override
	public void glDeleteProgram (int program) {
		GL.DeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer buffer) {
		buffer.get(intArray, 0, buffer.remaining());
		GL.DeleteRenderbuffers(n, intArray);
	}

	@Override
	public void glDeleteShader (int shader) {
		GL.DeleteShader(shader);
	}

	@Override
	public void glDetachShader (int program, int shader) {
		GL.DetachShader(program, shader);
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		GL.DisableVertexAttribArray(index);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		GL.DrawElements(BeginMode.wrap(mode), count, DrawElementsType.wrap(type), indices);
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		GL.EnableVertexAttribArray(index);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GL.FramebufferRenderbuffer(FramebufferTarget.wrap(target), FramebufferSlot.wrap(attachment),
			RenderbufferTarget.wrap(renderbuffertarget), renderbuffer);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GL.FramebufferTexture2D(FramebufferTarget.wrap(target), FramebufferSlot.wrap(attachment), TextureTarget.wrap(textarget),
			texture, level);
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		GL.GenBuffers(n, intArray);
		buffers.put(intArray, 0, n);
	}

	@Override
	public void glGenerateMipmap (int target) {
		GL.GenerateMipmap(TextureTarget.wrap(target));
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer buffers) {
		GL.GenFramebuffers(n, intArray);
		buffers.put(intArray, 0, n);
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer buffers) {
		GL.GenRenderbuffers(n, intArray);
		buffers.put(intArray, 0, n);
	}

	private final ActiveAttribType[] activeAttribTypes = new ActiveAttribType[1];

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		String activeAttrib = GL.GetActiveAttrib(program, index, intArray, activeAttribTypes);
		size.put(intArray, 0, size.remaining());
		if (type instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer)type;
			for (int i = 0; i < activeAttribTypes.length; i++)
				intBuffer.put(activeAttribTypes[i].Value);
			return activeAttrib;
		} else {
			throw new UnsupportedOperationException("expected IntBuffer for type parameter");
		}
	}

	private final ActiveUniformType[] activeUniformTypes = new ActiveUniformType[1];

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		String activeUniform = GL.GetActiveUniform(program, index, intArray, activeUniformTypes);
		size.put(intArray, 0, size.remaining());
		if (type instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer)type;
			for (int i = 0; i < activeUniformTypes.length; i++)
				intBuffer.put(activeUniformTypes[i].Value);
			return activeUniform;
		} else {
			throw new UnsupportedOperationException("expected IntBuffer for type parameter");
		}
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		return GL.GetAttribLocation(program, name);
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL.GetProgram(program, ProgramParameter.wrap(pname), intArray);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		return GL.GetProgramInfoLog(program);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL.GetShader(shader, ShaderParameter.wrap(pname), intArray);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		return GL.GetShaderInfoLog(shader);
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		return GL.GetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		return GL.IsBuffer(buffer);
	}

	@Override
	public boolean glIsEnabled (int cap) {
		return GL.IsEnabled(EnableCap.wrap(cap));
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		return GL.IsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsProgram (int program) {
		// TODO Auto-generated function stub
		return false;

	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return GL.IsRenderbuffer(renderbuffer);
	}

	@Override
	public boolean glIsShader (int shader) {
		return GL.IsShader(shader);
	}

	@Override
	public boolean glIsTexture (int texture) {
		return GL.IsTexture(texture);
	}

	@Override
	public void glLinkProgram (int program) {
		GL.LinkProgram(program);
	}

	@Override
	public void glReleaseShaderCompiler () {
		GL.ReleaseShaderCompiler();
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GL.RenderbufferStorage(RenderbufferTarget.wrap(target), RenderbufferInternalFormat.wrap(internalformat), width, height);
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		GL.SampleCoverage(value, invert);
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		// TODO Auto-generated function stub
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void glShaderSource (int shader, String string) {
		GL.ShaderSource(shader, string);
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		GL.StencilFuncSeparate(CullFaceMode.wrap(face), StencilFunction.wrap(func), ref, mask);
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		GL.StencilMaskSeparate(CullFaceMode.wrap(face), mask);
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		GL.StencilOpSeparate(CullFaceMode.wrap(face), StencilOp.wrap(fail), StencilOp.wrap(zfail), StencilOp.wrap(zpass));
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		params.get(floatArray, 0, params.remaining());
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), floatArray);
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), param);
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		params.get(intArray, 0, params.remaining());
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), intArray);
	}

	@Override
	public void glUniform1f (int location, float x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.Uniform1(location, count, floatArray);
	}

	@Override
	public void glUniform1i (int location, int x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		v.get(intArray, 0, v.remaining());
		GL.Uniform1(location, count, intArray);
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.Uniform2(location, count, floatArray);
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		v.get(intArray, 0, v.remaining());
		GL.Uniform2(location, count, intArray);
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.Uniform3(location, count, floatArray);
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		v.get(intArray, 0, v.remaining());
		GL.Uniform3(location, count, intArray);
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.Uniform4(location, count, floatArray);
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		v.get(intArray, 0, v.remaining());
		GL.Uniform4(location, count, intArray);
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.UniformMatrix2(location, count, transpose, floatArray);
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.UniformMatrix3(location, count, transpose, floatArray);
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.UniformMatrix4(location, count, transpose, floatArray);
	}

	@Override
	public void glUseProgram (int program) {
		GL.UseProgram(program);
	}

	@Override
	public void glValidateProgram (int program) {
		GL.ValidateProgram(program);
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		GL.VertexAttrib1(indx, x);
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib1(indx, floatArray);
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		GL.VertexAttrib2(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib2(indx, floatArray);
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GL.VertexAttrib3(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib3(indx, floatArray);
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GL.VertexAttrib4(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer v) {
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib4(indx, floatArray);
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		GL.VertexAttribPointer(indx, size, VertexAttribPointerType.wrap(type), normalized, stride,
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(ptr)));
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		GL.VertexAttribPointer(indx, size, VertexAttribPointerType.wrap(type), normalized, stride, ptr);
	}

}
