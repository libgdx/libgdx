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
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import cli.OpenTK.Graphics.ES20.ActiveAttribType;
import cli.OpenTK.Graphics.ES20.ActiveUniformType;
import cli.OpenTK.Graphics.ES20.BeginMode;
import cli.OpenTK.Graphics.ES20.BlendEquationMode;
import cli.OpenTK.Graphics.ES20.BlendingFactorDest;
import cli.OpenTK.Graphics.ES20.BlendingFactorSrc;
import cli.OpenTK.Graphics.ES20.BufferParameterName;
import cli.OpenTK.Graphics.ES20.BufferTarget;
import cli.OpenTK.Graphics.ES20.BufferUsage;
import cli.OpenTK.Graphics.ES20.ClearBufferMask;
import cli.OpenTK.Graphics.ES20.CullFaceMode;
import cli.OpenTK.Graphics.ES20.DepthFunction;
import cli.OpenTK.Graphics.ES20.DrawElementsType;
import cli.OpenTK.Graphics.ES20.EnableCap;
import cli.OpenTK.Graphics.ES20.FramebufferParameterName;
import cli.OpenTK.Graphics.ES20.FramebufferSlot;
import cli.OpenTK.Graphics.ES20.FramebufferTarget;
import cli.OpenTK.Graphics.ES20.FrontFaceDirection;
import cli.OpenTK.Graphics.ES20.GL;
import cli.OpenTK.Graphics.ES20.GetPName;
import cli.OpenTK.Graphics.ES20.GetTextureParameter;
import cli.OpenTK.Graphics.ES20.HintMode;
import cli.OpenTK.Graphics.ES20.HintTarget;
import cli.OpenTK.Graphics.ES20.PixelFormat;
import cli.OpenTK.Graphics.ES20.PixelInternalFormat;
import cli.OpenTK.Graphics.ES20.PixelStoreParameter;
import cli.OpenTK.Graphics.ES20.PixelType;
import cli.OpenTK.Graphics.ES20.ProgramParameter;
import cli.OpenTK.Graphics.ES20.RenderbufferInternalFormat;
import cli.OpenTK.Graphics.ES20.RenderbufferParameterName;
import cli.OpenTK.Graphics.ES20.RenderbufferTarget;
import cli.OpenTK.Graphics.ES20.ShaderBinaryFormat;
import cli.OpenTK.Graphics.ES20.ShaderParameter;
import cli.OpenTK.Graphics.ES20.ShaderPrecision;
import cli.OpenTK.Graphics.ES20.ShaderType;
import cli.OpenTK.Graphics.ES20.StencilFunction;
import cli.OpenTK.Graphics.ES20.StencilOp;
import cli.OpenTK.Graphics.ES20.StringName;
import cli.OpenTK.Graphics.ES20.TextureParameterName;
import cli.OpenTK.Graphics.ES20.TextureTarget;
import cli.OpenTK.Graphics.ES20.TextureUnit;
import cli.OpenTK.Graphics.ES20.VertexAttribParameter;
import cli.OpenTK.Graphics.ES20.VertexAttribPointerParameter;
import cli.OpenTK.Graphics.ES20.VertexAttribPointerType;
import cli.System.IntPtr;
import cli.System.Text.StringBuilder;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.utils.BufferUtils;

public class IOSMonotouchGLES20 implements GL20, GLCommon {

	private final int[] intArray = new int[64];
	private final float[] floatArray = new float[64];

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
		buffer.position(0);
		buffer.get(intArray, 0, n);
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
		textures.position(0);
		textures.put(intArray, 0, n);
	}

	@Override
	public int glGetError () {
		return GL.GetError().Value;
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer buffer) {
		GL.GetInteger(GetPName.wrap(pname), intArray);
		buffer.position(0);
		buffer.put(intArray, 0, buffer.remaining());
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
		// pixels can be null by OpenGL definition if an blank image is to be created (e.g. for buffering)
		IntPtr pixelsPointer = pixels != null ? IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(pixels)) : null;
		GL.TexImage2D(TextureTarget.wrap(target), level, PixelInternalFormat.wrap(internalformat), width, height, border,
			PixelFormat.wrap(format), PixelType.wrap(type), pixelsPointer);
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
		GL.BufferData(BufferTarget.wrap(target), IntPtr.op_Explicit(size),
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(data)), BufferUsage.wrap(usage));
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		GL.BufferSubData(BufferTarget.wrap(target), IntPtr.op_Explicit(offset), IntPtr.op_Explicit(size),
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(data)));
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
		buffer.position(0);
		buffer.get(intArray, 0, n);
		GL.DeleteBuffers(n, intArray);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer buffer) {
		buffer.position(0);
		buffer.get(intArray, 0, n);
		GL.DeleteFramebuffers(n, intArray);
	}

	@Override
	public void glDeleteProgram (int program) {
		GL.DeleteProgram(program);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer buffer) {
		buffer.position(0);
		buffer.get(intArray, 0, n);
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
		buffers.position(0);
		buffers.put(intArray, 0, n);
	}

	@Override
	public void glGenerateMipmap (int target) {
		GL.GenerateMipmap(TextureTarget.wrap(target));
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer buffers) {
		GL.GenFramebuffers(n, intArray);
		buffers.position(0);
		buffers.put(intArray, 0, n);
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer buffers) {
		GL.GenRenderbuffers(n, intArray);
		buffers.position(0);
		buffers.put(intArray, 0, n);
	}

	private final ActiveAttribType[] activeAttribTypes = new ActiveAttribType[1];

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		String activeAttrib = GL.GetActiveAttrib(program, index, intArray, activeAttribTypes);
		size.position(0);
		size.put(intArray, 0, size.remaining());
		if (type instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer)type;
			intBuffer.position(0);
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
		size.position(0);
		size.put(intArray, 0, size.remaining());
		if (type instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer)type;
			intBuffer.position(0);
			for (int i = 0; i < activeUniformTypes.length; i++)
				intBuffer.put(activeUniformTypes[i].Value);
			return activeUniform;
		} else {
			throw new UnsupportedOperationException("expected IntBuffer for type parameter");
		}
	}

	private final int[] shadersIntArray = new int[64];

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		GL.GetAttachedShaders(program, maxcount, intArray, shadersIntArray);
		shaders.position(0);
		shaders.put(shadersIntArray, 0, shaders.remaining());

		if (count instanceof IntBuffer) {
			IntBuffer intBuffer = (IntBuffer)count;
			intBuffer.position(0);
			intBuffer.put(intArray, 0, intBuffer.remaining());
		} else {
			throw new UnsupportedOperationException("expected IntBuffer for count parameter");
		}
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		return GL.GetAttribLocation(program, name);
	}

	private final boolean[] booleanArray = new boolean[64];
	private final byte FALSE = 0x00;
	private final byte TRUE = 0x01;

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		GL.GetBoolean(GetPName.wrap(pname), booleanArray);
		if (params instanceof ByteBuffer) {
			ByteBuffer byteBuffer = (ByteBuffer)params;
			byteBuffer.position(0);
			for (int i = 0; i < booleanArray.length; i++)
				byteBuffer.put(booleanArray[i] ? TRUE : FALSE);
		} else {
			throw new UnsupportedOperationException("expected ByteBuffer for params buffer");
		}
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		GL.GetBufferParameter(BufferTarget.wrap(target), BufferParameterName.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		GL.GetFloat(GetPName.wrap(pname), floatArray);
		params.position(0);
		params.put(floatArray, 0, params.remaining());
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		GL.GetFramebufferAttachmentParameter(FramebufferTarget.wrap(target), FramebufferSlot.wrap(attachment),
			FramebufferParameterName.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		GL.GetProgram(program, ProgramParameter.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		return GL.GetProgramInfoLog(program);
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GL.GetRenderbufferParameter(RenderbufferTarget.wrap(target), RenderbufferParameterName.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		GL.GetShader(shader, ShaderParameter.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		return GL.GetShaderInfoLog(shader);
	}

	private final int[] rangeIntArray = new int[2];

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		GL.GetShaderPrecisionFormat(ShaderType.wrap(shadertype), ShaderPrecision.wrap(precisiontype), rangeIntArray, intArray);
		range.position(0);
		range.put(rangeIntArray, 0, range.remaining());
		precision.position(0);
		precision.put(intArray, 0, precision.remaining());
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		GL.GetTexParameter(TextureTarget.wrap(target), GetTextureParameter.wrap(pname), floatArray);
		params.position(0);
		params.put(floatArray, 0, params.remaining());
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		GL.GetTexParameter(TextureTarget.wrap(target), GetTextureParameter.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		GL.GetUniform(program, location, floatArray);
		params.position(0);
		params.put(floatArray, 0, params.remaining());
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		GL.GetUniform(program, location, intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		return GL.GetUniformLocation(program, name);
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		GL.GetVertexAttrib(index, VertexAttribParameter.wrap(pname), floatArray);
		params.position(0);
		params.put(floatArray, 0, params.remaining());
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		GL.GetVertexAttrib(index, VertexAttribParameter.wrap(pname), intArray);
		params.position(0);
		params.put(intArray, 0, params.remaining());
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		GL.GetVertexAttribPointer(index, VertexAttribPointerParameter.wrap(pname),
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(pointer)));
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
		return GL.IsProgram(program);
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
		shaders.position(0);
		shaders.get(intArray, 0, shaders.remaining());
		GL.ShaderBinary(n, intArray, ShaderBinaryFormat.wrap(binaryformat),
			IntPtr.op_Explicit(BufferUtils.getUnsafeBufferAddress(binary)), length);
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
		params.position(0);
		params.get(floatArray, 0, params.remaining());
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), floatArray);
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), param);
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		params.position(0);
		params.get(intArray, 0, params.remaining());
		GL.TexParameter(TextureTarget.wrap(target), TextureParameterName.wrap(pname), intArray);
	}

	@Override
	public void glUniform1f (int location, float x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.Uniform1(location, count, floatArray);
	}

	@Override
	public void glUniform1i (int location, int x) {
		GL.Uniform1(location, x);
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		v.position(0);
		v.get(intArray, 0, v.remaining());
		GL.Uniform1(location, count, intArray);
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.Uniform2(location, count, floatArray);
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		GL.Uniform2(location, x, y);
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		v.position(0);
		v.get(intArray, 0, v.remaining());
		GL.Uniform2(location, count, intArray);
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.Uniform3(location, count, floatArray);
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		GL.Uniform3(location, x, y, z);
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		v.position(0);
		v.get(intArray, 0, v.remaining());
		GL.Uniform3(location, count, intArray);
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.Uniform4(location, count, floatArray);
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		GL.Uniform4(location, x, y, z, w);
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		v.position(0);
		v.get(intArray, 0, v.remaining());
		GL.Uniform4(location, count, intArray);
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.UniformMatrix2(location, count, transpose, floatArray);
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.UniformMatrix3(location, count, transpose, floatArray);
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer v) {
		v.position(0);
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
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib1(indx, floatArray);
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		GL.VertexAttrib2(indx, x, y);
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib2(indx, floatArray);
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		GL.VertexAttrib3(indx, x, y, z);
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer v) {
		v.position(0);
		v.get(floatArray, 0, v.remaining());
		GL.VertexAttrib3(indx, floatArray);
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		GL.VertexAttrib4(indx, x, y, z, w);
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer v) {
		v.position(0);
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
