/*******************************************************************************
 * Copyright 2023 See AUTHORS file.
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

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.webgl.client.WebGL2RenderingContext;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/** Does not extend from GwtGL20Debug as we would not have GL30 methods to override meaning we would need to implement all of GL30
 * here as well.
 *
 * Instead, GL30 override methods are first, then GL20 override methods are beneath which were copied from
 * {@link GwtGL20Debug}. */
public class GwtGL30Debug extends GwtGL30 {

	protected GwtGL30Debug (WebGL2RenderingContext gl) {
		super(gl);
	}

	private void checkError () {
		int error = 0;
		if ((error = gl.getError()) != GL_NO_ERROR) {
			throw new GdxRuntimeException("GL error: " + error + ", " + Integer.toHexString(error));
		}
	}

	@Override
	public void glBeginQuery (int target, int id) {
		super.glBeginQuery(target, id);
		checkError();
	}

	@Override
	public void glBeginTransformFeedback (int primitiveMode) {
		super.glBeginTransformFeedback(primitiveMode);
		checkError();
	}

	@Override
	public void glBindBufferBase (int target, int index, int buffer) {
		super.glBindBufferBase(target, index, buffer);
		checkError();
	}

	@Override
	public void glBindBufferRange (int target, int index, int buffer, int offset, int size) {
		super.glBindBufferRange(target, index, buffer, offset, size);
		checkError();
	}

	@Override
	public void glBindSampler (int unit, int sampler) {
		super.glBindSampler(unit, sampler);
		checkError();
	}

	@Override
	public void glBindTransformFeedback (int target, int id) {
		super.glBindTransformFeedback(target, id);
		checkError();
	}

	@Override
	public void glBindVertexArray (int array) {
		super.glBindVertexArray(array);
		checkError();
	}

	@Override
	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		super.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
		checkError();
	}

	@Override
	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		super.glClearBufferfi(buffer, drawbuffer, depth, stencil);
		checkError();
	}

	@Override
	public void glClearBufferfv (int buffer, int drawbuffer, FloatBuffer value) {
		super.glClearBufferfv(buffer, drawbuffer, value);
		checkError();
	}

	@Override
	public void glClearBufferiv (int buffer, int drawbuffer, IntBuffer value) {
		super.glClearBufferiv(buffer, drawbuffer, value);
		checkError();
	}

	@Override
	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		super.glClearBufferuiv(buffer, drawbuffer, value);
		checkError();
	}

	@Override
	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		super.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
		checkError();
	}

	@Override
	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		super.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
		checkError();
	}

	@Override
	public void glDeleteQueries (int n, int[] ids, int offset) {
		super.glDeleteQueries(n, ids, offset);
		checkError();
	}

	@Override
	public void glDeleteQueries (int n, IntBuffer ids) {
		super.glDeleteQueries(n, ids);
		checkError();
	}

	@Override
	public void glDeleteSamplers (int count, int[] samplers, int offset) {
		super.glDeleteSamplers(count, samplers, offset);
		checkError();
	}

	@Override
	public void glDeleteSamplers (int n, IntBuffer ids) {
		super.glDeleteSamplers(n, ids);
		checkError();
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, int[] ids, int offset) {
		super.glDeleteTransformFeedbacks(n, ids, offset);
		checkError();
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		super.glDeleteTransformFeedbacks(n, ids);
		checkError();
	}

	@Override
	public void glDeleteVertexArrays (int n, int[] arrays, int offset) {
		super.glDeleteVertexArrays(n, arrays, offset);
		checkError();
	}

	@Override
	public void glDeleteVertexArrays (int n, IntBuffer ids) {
		super.glDeleteVertexArrays(n, ids);
		checkError();
	}

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instanceCount) {
		super.glDrawArraysInstanced(mode, first, count, instanceCount);
		checkError();
	}

	@Override
	public void glDrawBuffers (int n, IntBuffer bufs) {
		super.glDrawBuffers(n, bufs);
		checkError();
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		super.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
		checkError();
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		super.glDrawRangeElements(mode, start, end, count, type, indices);
		checkError();
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		super.glDrawRangeElements(mode, start, end, count, type, offset);
		checkError();
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		int offset) {
		super.glTexImage2D(target, level, internalformat, width, height, border, format, type, offset);
		checkError();
	}

	@Override
	public void glEndQuery (int target) {
		super.glEndQuery(target);
		checkError();
	}

	@Override
	public void glEndTransformFeedback () {
		super.glEndTransformFeedback();
		checkError();
	}

	@Override
	public void glFlushMappedBufferRange (int target, int offset, int length) {
		super.glFlushMappedBufferRange(target, offset, length);
		checkError();
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		super.glFramebufferTextureLayer(target, attachment, texture, level, layer);
		checkError();
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		super.glGenQueries(n, ids, offset);
		checkError();
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		super.glGenQueries(n, ids);
		checkError();
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		super.glGenSamplers(count, samplers, offset);
		checkError();
	}

	@Override
	public void glGenSamplers (int n, IntBuffer ids) {
		super.glGenSamplers(n, ids);
		checkError();
	}

	@Override
	public void glGenTransformFeedbacks (int n, int[] ids, int offset) {
		super.glGenTransformFeedbacks(n, ids, offset);
		checkError();
	}

	@Override
	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		super.glGenTransformFeedbacks(n, ids);
		checkError();
	}

	@Override
	public void glGenVertexArrays (int n, int[] arrays, int offset) {
		super.glGenVertexArrays(n, arrays, offset);
		checkError();
	}

	@Override
	public void glGenVertexArrays (int n, IntBuffer ids) {
		super.glGenVertexArrays(n, ids);
		checkError();
	}

	@Override
	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		super.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
		checkError();
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return super.glGetActiveUniformBlockName(program, uniformBlockIndex);
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		super.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
		checkError();
	}

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		super.glGetActiveUniformsiv(program, uniformCount, uniformIndices, pname, params);
		checkError();
	}

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		super.glGetBufferParameteri64v(target, pname, params);
		checkError();
	}

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		return super.glGetBufferPointerv(target, pname);
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		return super.glGetFragDataLocation(program, name);
	}

	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		super.glGetInteger64v(pname, params);
		checkError();
	}

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		super.glGetQueryiv(target, pname, params);
		checkError();
	}

	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		super.glGetQueryObjectuiv(id, pname, params);
		checkError();
	}

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		super.glGetSamplerParameterfv(sampler, pname, params);
		checkError();
	}

	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		super.glGetSamplerParameteriv(sampler, pname, params);
		checkError();
	}

	@Override
	public String glGetStringi (int name, int index) {
		return super.glGetStringi(name, index);
	}

	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return super.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		super.glGetUniformIndices(program, uniformNames, uniformIndices);
		checkError();
	}

	@Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		super.glGetUniformuiv(program, location, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		super.glGetVertexAttribIiv(index, pname, params);
		checkError();
	}

	@Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		super.glGetVertexAttribIuiv(index, pname, params);
		checkError();
	}

	@Override
	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		super.glInvalidateFramebuffer(target, numAttachments, attachments);
		checkError();
	}

	@Override
	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		super.glInvalidateSubFramebuffer(target, numAttachments, attachments, x, y, width, height);
		checkError();
	}

	@Override
	public boolean glIsQuery (int id) {
		return super.glIsQuery(id);
	}

	@Override
	public boolean glIsSampler (int id) {
		return super.glIsSampler(id);
	}

	@Override
	public boolean glIsTransformFeedback (int id) {
		return super.glIsTransformFeedback(id);
	}

	@Override
	public boolean glIsVertexArray (int id) {
		return super.glIsVertexArray(id);
	}

	@Override
	public Buffer glMapBufferRange (int target, int offset, int length, int access) {
		return super.glMapBufferRange(target, offset, length, access);
	}

	@Override
	public void glPauseTransformFeedback () {
		super.glPauseTransformFeedback();
		checkError();
	}

	@Override
	public void glProgramParameteri (int program, int pname, int value) {
		super.glProgramParameteri(program, pname, value);
		checkError();
	}

	@Override
	public void glReadBuffer (int mode) {
		super.glReadBuffer(mode);
		checkError();
	}

	@Override
	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		super.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
		checkError();
	}

	@Override
	public void glResumeTransformFeedback () {
		super.glResumeTransformFeedback();
		checkError();
	}

	@Override
	public void glSamplerParameterf (int sampler, int pname, float param) {
		super.glSamplerParameterf(sampler, pname, param);
		checkError();
	}

	@Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		super.glSamplerParameterfv(sampler, pname, param);
		checkError();
	}

	@Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		super.glSamplerParameteri(sampler, pname, param);
		checkError();
	}

	@Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		super.glSamplerParameteriv(sampler, pname, param);
		checkError();
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels) {
		super.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
		checkError();
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		super.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
		checkError();
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		int offset) {
		super.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, offset);
		checkError();
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, Buffer pixels) {
		super.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
		checkError();
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		super.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
		checkError();
	}

	@Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		super.glTransformFeedbackVaryings(program, varyings, bufferMode);
		checkError();
	}

	@Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		super.glUniform1uiv(location, count, value);
		checkError();
	}

	@Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		super.glUniform3uiv(location, count, value);
		checkError();
	}

	@Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		super.glUniform4uiv(location, count, value);
		checkError();
	}

	@Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		super.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
		checkError();
	}

	@Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix2x3fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix2x4fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix3x2fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix3x4fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix4x2fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		super.glUniformMatrix4x3fv(location, count, transpose, value);
		checkError();
	}

	@Override
	public boolean glUnmapBuffer (int target) {
		return super.glUnmapBuffer(target);
	}

	@Override
	public void glVertexAttribDivisor (int index, int divisor) {
		super.glVertexAttribDivisor(index, divisor);
		checkError();
	}

	@Override
	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		super.glVertexAttribI4i(index, x, y, z, w);
		checkError();
	}

	@Override
	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		super.glVertexAttribI4ui(index, x, y, z, w);
		checkError();
	}

	@Override
	public void glVertexAttribIPointer (int index, int size, int type, int stride, int offset) {
		super.glVertexAttribIPointer(index, size, type, stride, offset);
		checkError();
	}

	/** Begin GL20 methods **/

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
	public String glGetActiveAttrib (int program, int index, IntBuffer size, IntBuffer type) {

		String attrib = super.glGetActiveAttrib(program, index, size, type);
		checkError();
		return attrib;
	}

	@Override
	public String glGetActiveUniform (int program, int index, IntBuffer size, IntBuffer type) {

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
