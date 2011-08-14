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

import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.enums.BeginMode;
import gwt.g3d.client.gl2.enums.BlendEquationMode;
import gwt.g3d.client.gl2.enums.BlendingFactorDest;
import gwt.g3d.client.gl2.enums.BlendingFactorSrc;
import gwt.g3d.client.gl2.enums.ClearBufferMask;
import gwt.g3d.client.gl2.enums.CullFaceMode;
import gwt.g3d.client.gl2.enums.DepthFunction;
import gwt.g3d.client.gl2.enums.DrawElementsType;
import gwt.g3d.client.gl2.enums.EnableCap;
import gwt.g3d.client.gl2.enums.FramebufferTarget;
import gwt.g3d.client.gl2.enums.FrontFaceDirection;
import gwt.g3d.client.gl2.enums.HintMode;
import gwt.g3d.client.gl2.enums.HintTarget;
import gwt.g3d.client.gl2.enums.PixelInternalFormat;
import gwt.g3d.client.gl2.enums.PixelStoreParameter;
import gwt.g3d.client.gl2.enums.RenderbufferInternalFormat;
import gwt.g3d.client.gl2.enums.RenderbufferTarget;
import gwt.g3d.client.gl2.enums.StencilFunction;
import gwt.g3d.client.gl2.enums.StencilOp;
import gwt.g3d.client.gl2.enums.StringName;
import gwt.g3d.client.gl2.enums.TextureParameterName;
import gwt.g3d.client.gl2.enums.TextureTarget;
import gwt.g3d.client.gl2.enums.TextureUnit;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GwtGL20 implements GL20 {
	final Surface3D surface;
	final GL2 gl;

	protected GwtGL20 (Surface3D surface) {
		this.surface = surface;
		this.gl = surface.getGL();
	}

	@Override
	public void glActiveTexture (int texture) {
		gl.activeTexture(TextureUnit.parseTextureUnit(texture));
	}

	@Override
	public void glBindTexture (int target, int texture) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
// gl.bindTexture(TextureTarget.parseTextureTarget(target), );
	}

	@Override
	public void glBlendFunc (int sfactor, int dfactor) {
		gl.blendFunc(BlendingFactorSrc.parseBlendingFactorSrc(sfactor), BlendingFactorDest.parseBlendingFactorDest(dfactor));
	}

	@Override
	public void glClear (int mask) {
		if ((mask & GL20.GL_COLOR_BUFFER_BIT) != 0 && (mask & GL20.GL_DEPTH_BUFFER_BIT) != 0
			&& (mask & GL20.GL_STENCIL_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.COLOR_BUFFER_BIT, ClearBufferMask.DEPTH_BUFFER_BIT, ClearBufferMask.STENCIL_BUFFER_BIT);
		}

		if ((mask & GL20.GL_COLOR_BUFFER_BIT) != 0 && (mask & GL20.GL_DEPTH_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.COLOR_BUFFER_BIT, ClearBufferMask.DEPTH_BUFFER_BIT);
		}

		if ((mask & GL20.GL_DEPTH_BUFFER_BIT) != 0 && (mask & GL20.GL_STENCIL_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT, ClearBufferMask.STENCIL_BUFFER_BIT);
		}

		if ((mask & GL20.GL_COLOR_BUFFER_BIT) != 0 && (mask & GL20.GL_STENCIL_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.COLOR_BUFFER_BIT, ClearBufferMask.STENCIL_BUFFER_BIT);
		}

		if ((mask & GL20.GL_COLOR_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.COLOR_BUFFER_BIT);
		}

		if ((mask & GL20.GL_DEPTH_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.DEPTH_BUFFER_BIT);
		}

		if ((mask & GL20.GL_STENCIL_BUFFER_BIT) != 0) {
			gl.clear(ClearBufferMask.STENCIL_BUFFER_BIT);
		}
	}

	@Override
	public void glClearColor (float red, float green, float blue, float alpha) {
		gl.clearColor(red, green, blue, alpha);
	}

	@Override
	public void glClearDepthf (float depth) {
		gl.clearDepth(depth);
	}

	@Override
	public void glClearStencil (int s) {
		gl.clearStencil(s);
	}

	@Override
	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha) {
		gl.colorMask(red, green, blue, alpha);
	}

	@Override
	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("compressed textures not supported by GWT WebGL backend");
	}

	@Override
	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data) {
		throw new GdxRuntimeException("compressed textures not supported by GWT WebGL backend");
	}

	@Override
	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border) {
		gl.copyTexImage2D(TextureTarget.parseTextureTarget(target), level,
			PixelInternalFormat.parsePixelInternalFormat(internalformat), x, y, width, height, border);
	}

	@Override
	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
		gl.copyTexSubImage2D(TextureTarget.parseTextureTarget(target), level, xoffset, yoffset, x, y, width, height);
	}

	@Override
	public void glCullFace (int mode) {
		gl.cullFace(CullFaceMode.parseCullFaceMode(mode));
	}

	@Override
	public void glDeleteTextures (int n, IntBuffer textures) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDepthFunc (int func) {
		gl.depthFunc(DepthFunction.parseDepthFunction(func));
	}

	@Override
	public void glDepthMask (boolean flag) {
		gl.depthMask(flag);
	}

	@Override
	public void glDepthRangef (float zNear, float zFar) {
		gl.depthRange(zNear, zFar);
	}

	@Override
	public void glDisable (int cap) {
		gl.disable(EnableCap.parseEnableCap(cap));
	}

	@Override
	public void glDrawArrays (int mode, int first, int count) {
		gl.drawArrays(BeginMode.parseBeginMode(mode), first, count);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, Buffer indices) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
// gl.drawElements(BeginMode.parseBeginMode(mode), count, type, offset)
	}

	@Override
	public void glEnable (int cap) {
		gl.enable(EnableCap.parseEnableCap(cap));
	}

	@Override
	public void glFinish () {
		gl.finish();
	}

	@Override
	public void glFlush () {
		gl.flush();
	}

	@Override
	public void glFrontFace (int mode) {
		gl.frontFace(FrontFaceDirection.parseFrontFaceDirection(mode));
	}

	@Override
	public void glGenTextures (int n, IntBuffer textures) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glGetError () {
		return gl.getError().getValue();
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		throw new GdxRuntimeException("glGetInteger not supported by GWT WebGL backend");
	}

	@Override
	public String glGetString (int name) {
		return gl.getString(StringName.parseStringName(name));
	}

	@Override
	public void glHint (int target, int mode) {
		gl.hint(HintTarget.parseHintTarget(target), HintMode.parseHintMode(mode));
	}

	@Override
	public void glLineWidth (float width) {
		gl.lineWidth(width);
	}

	@Override
	public void glPixelStorei (int pname, int param) {
		gl.pixelStorei(PixelStoreParameter.parsePixelStoreParameter(pname), param);
	}

	@Override
	public void glPolygonOffset (float factor, float units) {
		gl.polygonOffset(factor, units);
	}

	@Override
	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glScissor (int x, int y, int width, int height) {
		gl.scissor(x, y, width, height);
	}

	@Override
	public void glStencilFunc (int func, int ref, int mask) {
		gl.stencilFunc(StencilFunction.parseStencilFunction(func), ref, mask);
	}

	@Override
	public void glStencilMask (int mask) {
		gl.stencilMask(mask);
	}

	@Override
	public void glStencilOp (int fail, int zfail, int zpass) {
		gl.stencilOp(StencilOp.parseStencilOp(fail), StencilOp.parseStencilOp(zfail), StencilOp.parseStencilOp(zpass));
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glTexParameterf (int target, int pname, float param) {
		gl.texParameterf(TextureTarget.parseTextureTarget(target), TextureParameterName.parseTextureParameterName(pname), param);
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glViewport (int x, int y, int width, int height) {
		gl.viewport(x, y, width, height);
	}

	@Override
	public void glAttachShader (int program, int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBindAttribLocation (int program, int index, String name) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBindBuffer (int target, int buffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBlendColor (float red, float green, float blue, float alpha) {
		gl.blendColor(red, green, blue, alpha);
	}

	@Override
	public void glBlendEquation (int mode) {
		gl.blendEquation(BlendEquationMode.parseBlendEquationMode(mode));
	}

	@Override
	public void glBlendEquationSeparate (int modeRGB, int modeAlpha) {
		gl.blendEquationSeparate(BlendEquationMode.parseBlendEquationMode(modeRGB),
			BlendEquationMode.parseBlendEquationMode(modeAlpha));
	}

	@Override
	public void glBlendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		gl.blendFuncSeparate(BlendingFactorSrc.parseBlendingFactorSrc(srcRGB), BlendingFactorDest.parseBlendingFactorDest(dstRGB),
			BlendingFactorSrc.parseBlendingFactorSrc(srcAlpha), BlendingFactorDest.parseBlendingFactorDest(dstAlpha));
	}

	@Override
	public void glBufferData (int target, int size, Buffer data, int usage) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glBufferSubData (int target, int offset, int size, Buffer data) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		return gl.checkFramebufferStatus(FramebufferTarget.parseFramebufferTarget(target)).getValue();
	}

	@Override
	public void glCompileShader (int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glCreateProgram () {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glCreateShader (int type) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDeleteBuffers (int n, IntBuffer buffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDeleteProgram (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDeleteShader (int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDetachShader (int program, int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glDisableVertexAttribArray (int index) {
		gl.disableVertexAttribArray(index);
	}

	@Override
	public void glDrawElements (int mode, int count, int type, int indices) {
		gl.drawElements(BeginMode.parseBeginMode(mode), count, DrawElementsType.parseDataType(type), indices);
	}

	@Override
	public void glEnableVertexAttribArray (int index) {
		gl.enableVertexAttribArray(index);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGenBuffers (int n, IntBuffer buffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGenerateMipmap (int target) {
		gl.generateMipmap(TextureTarget.parseTextureTarget(target));
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public String glGetActiveAttrib (int program, int index, IntBuffer size, Buffer type) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, Buffer type) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetAttachedShaders (int program, int maxcount, Buffer count, IntBuffer shaders) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glGetAttribLocation (int program, String name) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetBooleanv (int pname, Buffer params) {
		throw new GdxRuntimeException("glGetBoolean not supported by GWT WebGL backend");
	}

	@Override
	public void glGetBufferParameteriv (int target, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		throw new GdxRuntimeException("glGetFloat not supported by GWT WebGL backend");
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetProgramiv (int program, int pname, IntBuffer params) {
		throw new GdxRuntimeException("glGetProgram not supported by GWT WebGL backend");
	}

	@Override
	public String glGetProgramInfoLog (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetShaderiv (int shader, int pname, IntBuffer params) {
		throw new GdxRuntimeException("glGetShader not supported by GWT WebGL backend");
	}

	@Override
	public String glGetShaderInfoLog (int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetShaderPrecisionFormat (int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
		throw new GdxRuntimeException("glGetShaderPrecisionFormat not supported by GWT WebGL backend");
	}

	@Override
	public void glGetShaderSource (int shader, int bufsize, Buffer length, String source) {
		throw new GdxRuntimeException("glGetShaderSource not supported by GWT WebGL backend");
	}

	@Override
	public void glGetTexParameterfv (int target, int pname, FloatBuffer params) {
		throw new GdxRuntimeException("glGetTexParameter not supported by GWT WebGL backend");
	}

	@Override
	public void glGetTexParameteriv (int target, int pname, IntBuffer params) {
		throw new GdxRuntimeException("glGetTexParameter not supported by GWT WebGL backend");
	}

	@Override
	public void glGetUniformfv (int program, int location, FloatBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetUniformiv (int program, int location, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public int glGetUniformLocation (int program, String name) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetVertexAttribfv (int index, int pname, FloatBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetVertexAttribiv (int index, int pname, IntBuffer params) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glGetVertexAttribPointerv (int index, int pname, Buffer pointer) {
		throw new GdxRuntimeException("glGetVertexAttribPointer not supported by GWT WebGL backend");
	}

	@Override
	public boolean glIsBuffer (int buffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public boolean glIsEnabled (int cap) {
		return gl.isEnabled(EnableCap.parseEnableCap(cap));
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public boolean glIsProgram (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public boolean glIsShader (int shader) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public boolean glIsTexture (int texture) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glLinkProgram (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glReleaseShaderCompiler () {
		throw new GdxRuntimeException("glReleaseShaderCompiler not supported by GWT WebGL backend");
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		gl.renderbufferStorage(RenderbufferTarget.parseRenderbufferTarget(target),
			RenderbufferInternalFormat.parseRenderbufferInternalFormat(internalformat), width, height);
	}

	@Override
	public void glSampleCoverage (float value, boolean invert) {
		gl.sampleCoverage(value, invert);
	}

	@Override
	public void glShaderBinary (int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
		throw new GdxRuntimeException("glShaderBinary not supported by GWT WebGL backend");
	}

	@Override
	public void glShaderSource (int shader, String string) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glStencilFuncSeparate (int face, int func, int ref, int mask) {
		gl.stencilFuncSeparate(CullFaceMode.parseCullFaceMode(face), StencilFunction.parseStencilFunction(func), ref, mask);
	}

	@Override
	public void glStencilMaskSeparate (int face, int mask) {
		gl.stencilMaskSeparate(CullFaceMode.parseCullFaceMode(face), mask);
	}

	@Override
	public void glStencilOpSeparate (int face, int fail, int zfail, int zpass) {
		gl.stencilOpSeparate(CullFaceMode.parseCullFaceMode(face), StencilOp.parseStencilOp(fail), StencilOp.parseStencilOp(zfail),
			StencilOp.parseStencilOp(zpass));
	}

	@Override
	public void glTexParameterfv (int target, int pname, FloatBuffer params) {
		gl.texParameterf(TextureTarget.parseTextureTarget(target), TextureParameterName.parseTextureParameterName(pname),
			params.get());
	}

	@Override
	public void glTexParameteri (int target, int pname, int param) {
		gl.texParameterf(TextureTarget.parseTextureTarget(target), TextureParameterName.parseTextureParameterName(pname), param);
	}

	@Override
	public void glTexParameteriv (int target, int pname, IntBuffer params) {
		gl.texParameterf(TextureTarget.parseTextureTarget(target), TextureParameterName.parseTextureParameterName(pname),
			params.get());
	}

	@Override
	public void glUniform1f (int location, float x) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform1fv (int location, int count, FloatBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform1i (int location, int x) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform1iv (int location, int count, IntBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform2f (int location, float x, float y) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform2fv (int location, int count, FloatBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform2i (int location, int x, int y) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform2iv (int location, int count, IntBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform3f (int location, float x, float y, float z) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform3fv (int location, int count, FloatBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform3i (int location, int x, int y, int z) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform3iv (int location, int count, IntBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform4f (int location, float x, float y, float z, float w) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform4fv (int location, int count, FloatBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform4i (int location, int x, int y, int z, int w) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniform4iv (int location, int count, IntBuffer v) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniformMatrix2fv (int location, int count, boolean transpose, FloatBuffer value) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniformMatrix3fv (int location, int count, boolean transpose, FloatBuffer value) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUniformMatrix4fv (int location, int count, boolean transpose, FloatBuffer value) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glUseProgram (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glValidateProgram (int program) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib1f (int indx, float x) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib1fv (int indx, FloatBuffer values) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib2f (int indx, float x, float y) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib2fv (int indx, FloatBuffer values) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib3f (int indx, float x, float y, float z) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib3fv (int indx, FloatBuffer values) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib4f (int indx, float x, float y, float z, float w) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttrib4fv (int indx, FloatBuffer values) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
		throw new GdxRuntimeException("not implemented");
	}

	@Override
	public void glVertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int ptr) {
		// FIXME
		throw new GdxRuntimeException("not implemented");
	}
}