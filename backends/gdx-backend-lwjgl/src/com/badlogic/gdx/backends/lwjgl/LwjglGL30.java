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

package com.badlogic.gdx.backends.lwjgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLSync;

import com.badlogic.gdx.utils.GdxRuntimeException;

final class LwjglGL30 extends LwjglGL20 implements com.badlogic.gdx.graphics.GL30 {

	public void glBindBufferBase (int target, int index, int buffer) {
		GL30.glBindBufferBase(target, index, buffer);
	}

	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		GL31.glGetActiveUniformBlock(program, uniformBlockIndex, pname, params);
	}

	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, 256);
	}

	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		GL31.glGetActiveUniforms(program, uniformIndices, pname, params);
	}

	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return GL31.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	public void glDrawArraysInstanced (int mode, int first, int count, int instancecount) {
		GL31.glDrawArraysInstanced(mode, first, instancecount, instancecount);
	}

	public void glDrawBuffers (int n, IntBuffer bufs) {
		GL20.glDrawBuffers(bufs);
	}

	public void glDrawElementsInstanced (int mode, int count, int type, long indices_buffer_offset, int instancecount) {
		GL31.glDrawElementsInstanced(mode, count, type, indices_buffer_offset, instancecount);
	}

	public void glReadBuffer (int mode) {
		GL11.glReadBuffer(mode);
	}

	public void glDrawRangeElements (int mode, int start, int end, int count, int type, IntBuffer indices) {
		GL12.glDrawRangeElements(mode, start, end, indices);
	}

	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels) {
		if (pixels == null)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, Buffer pixels) {
		if (pixels == null)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ByteBuffer)null);
		else if (pixels instanceof ByteBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ByteBuffer)pixels);
		else if (pixels instanceof ShortBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (ShortBuffer)pixels);
		else if (pixels instanceof IntBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (IntBuffer)pixels);
		else if (pixels instanceof FloatBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (FloatBuffer)pixels);
		else if (pixels instanceof DoubleBuffer)
			GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (DoubleBuffer)pixels);
		else
			throw new GdxRuntimeException("Can't use " + pixels.getClass().getName()
				+ " with this method. Use ByteBuffer, ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL");
	}

	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	public void glCompressedTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int imageSize, ByteBuffer data) {
		GL13.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, data);
	}

	public void glCompressedTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int imageSize, ByteBuffer data) {
		GL13.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data);
	}

	public void glGenQueries (int n, IntBuffer ids) {
		GL15.glGenQueries(ids);
	}

	public void glDeleteQueries (int n, IntBuffer ids) {
		GL15.glDeleteQueries(ids);
	}

	public boolean glIsQuery (int id) {
		return GL15.glIsQuery(id);
	}

	public void glBeginQuery (int target, int id) {
		GL15.glBeginQuery(target, id);
	}

	public void glEndQuery (int target) {
		GL15.glEndQuery(target);
	}

	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		GL15.glGetQuery(target, pname, params);
	}

	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		GL15.glGetQueryObjectu(id, pname, params);
	}

	public boolean glUnmapBuffer (int target) {
		return GL15.glUnmapBuffer(target);
	}

	public ByteBuffer glGetBufferPointerv (int target, int pname) {
		return GL15.glGetBufferPointer(target, pname);
	}

	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x3(location, transpose, value);
	}

	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x2(location, transpose, value);
	}

	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x4(location, transpose, value);
	}

	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x2(location, transpose, value);
	}

	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x4(location, transpose, value);
	}

	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x3(location, transpose, value);
	}

	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
	}

	public ByteBuffer glMapBufferRange (int target, long offset, long length, int access, ByteBuffer previousBuffer) {
		return GL30.glMapBufferRange(target, offset, length, access, previousBuffer);
	}

	public void glFlushMappedBufferRange (int target, long offset, long length) {
		GL30.glFlushMappedBufferRange(target, offset, length);
	}

	public void glBindVertexArray (int array) {
		GL30.glBindVertexArray(array);
	}

	public void glDeleteVertexArrays (int n, IntBuffer arrays) {
		GL30.glDeleteVertexArrays(arrays);
	}

	public void glGenVertexArrays (int n, IntBuffer arrays) {
		GL30.glGenVertexArrays(arrays);
	}

	public boolean glIsVertexArray (int array) {
		return GL30.glIsVertexArray(array);
	}

	public void glGetIntegeri_v (int target, int index, IntBuffer data) {
		GL30.glGetInteger(target, index, data);
	}

	public void glBeginTransformFeedback (int primitiveMode) {
		GL30.glBeginTransformFeedback(primitiveMode);
	}

	public void glEndTransformFeedback () {
		GL30.glEndTransformFeedback();
	}

	public void glBindBufferRange (int target, int index, int buffer, long offset, long size) {
		GL30.glBindBufferRange(target, index, buffer, offset, size);
	}

	public void glTransformFeedbackVaryings (int program, int count, CharSequence[] varyings, int bufferMode) {
		GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
	}

	public String glGetTransformFeedbackVarying (int program, int index, int bufSize, IntBuffer length, IntBuffer size,
		IntBuffer type) {
		return GL30.glGetTransformFeedbackVarying(program, index, bufSize, size, type);
	}

	public void glVertexAttribIPointer (int index, int size, int type, int stride, ByteBuffer buffer) {
		GL30.glVertexAttribIPointer(index, size, type, stride, buffer);
	}

	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribI(index, pname, params);
	}

	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribIu(index, pname, params);
	}

	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4i(index, x, y, z, w);
	}

	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4ui(index, x, y, z, w);
	}

	public void glVertexAttribI4iv (int index, IntBuffer v) {
		GL30.glVertexAttribI4(index, v);
	}

	public void glVertexAttribI4uiv (int index, IntBuffer v) {
		GL30.glVertexAttribI4u(index, v);
	}

	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		GL30.glGetUniformu(program, location, params);
	}

	public int glGetFragDataLocation (int program, String name) {
		return GL30.glGetFragDataLocation(program, name);
	}

	public void glUniform1ui (int location, int v0) {
		GL30.glUniform1ui(location, v0);
	}

	public void glUniform2ui (int location, int v0, int v1) {
		GL30.glUniform2ui(location, v0, v1);
	}

	public void glUniform3ui (int location, int v0, int v1, int v2) {
		GL30.glUniform3ui(location, v0, v1, v2);
	}

	public void glUniform4ui (int location, int v0, int v1, int v2, int v3) {
		GL30.glUniform4ui(location, v0, v1, v2, v3);
	}

	public void glUniform1uiv (int location, int count, IntBuffer value) {
		GL30.glUniform1u(location, value);
	}

	public void glUniform2uiv (int location, int count, IntBuffer value) {
		GL30.glUniform2u(location, value);
	}

	public void glUniform3uiv (int location, int count, IntBuffer value) {
		GL30.glUniform3u(location, value);
	}

	public void glUniform4uiv (int location, int count, IntBuffer value) {
		GL30.glUniform4u(location, value);
	}

	public void glClearBufferiv (int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBuffer(buffer, drawbuffer, value);
	}

	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBufferu(buffer, drawbuffer, value);
	}

	public void glClearBufferfv (int buffer, int drawbuffer, FloatBuffer value) {
		GL30.glClearBuffer(buffer, drawbuffer, value);
	}

	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		GL30.glClearBufferfi(drawbuffer, drawbuffer, depth, stencil);
	}

	public String glGetStringi (int name, int index) {
		return GL30.glGetStringi(name, index);
	}

	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	public void glGetUniformIndices (int program, int uniformCount, String[] uniformNames, IntBuffer uniformIndices) {
		GL31.glGetUniformIndices(program, uniformNames, uniformIndices);
	}

	public void glGetInteger64v (int pname, LongBuffer data) {
		GL32.glGetInteger64(pname, data);
	}

	public void glGetInteger64i_v (int target, int index, LongBuffer data) {
		GL32.glGetInteger64(target, index, data);
	}

	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		GL32.glGetBufferParameter(target, pname, params);
	}

	public void glGenSamplers (int count, IntBuffer samplers) {
		GL33.glGenSamplers(samplers);
	}

	public void glDeleteSamplers (int count, IntBuffer samplers) {
		GL33.glDeleteSamplers(samplers);
	}

	public boolean glIsSampler (int sampler) {
		return GL33.glIsSampler(sampler);
	}

	public void glBindSampler (int unit, int sampler) {
		GL33.glBindSampler(unit, sampler);
	}

	public void glSamplerParameteri (int sampler, int pname, int param) {
		GL33.glSamplerParameteri(sampler, pname, param);
	}

	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		GL33.glSamplerParameter(sampler, pname, param);
	}

	public void glSamplerParameterf (int sampler, int pname, float param) {
		GL33.glSamplerParameterf(sampler, pname, param);
	}

	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		GL33.glSamplerParameter(sampler, pname, param);
	}

	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		GL33.glGetSamplerParameter(sampler, pname, params);
	}

	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		GL33.glGetSamplerParameter(sampler, pname, params);
	}

	public void glVertexAttribDivisor (int index, int divisor) {
		GL33.glVertexAttribDivisor(index, divisor);
	}

	public void glBindTransformFeedback (int target, int id) {
		GL40.glBindTransformFeedback(target, id);
	}

	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glDeleteTransformFeedbacks(ids);
	}

	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glGenTransformFeedbacks(ids);
	}

	public boolean glIsTransformFeedback (int id) {
		return GL40.glIsTransformFeedback(id);
	}

	public void glPauseTransformFeedback () {
		GL40.glPauseTransformFeedback();
	}

	public void glResumeTransformFeedback () {
		GL40.glResumeTransformFeedback();
	}

	public void glGetProgramBinary (int program, int bufSize, IntBuffer length, IntBuffer binaryFormat, ByteBuffer binary) {
		GL41.glGetProgramBinary(program, length, binaryFormat, binary);
	}

	public void glProgramBinary (int program, int binaryFormat, ByteBuffer binary, int length) {
		GL41.glProgramBinary(program, binaryFormat, binary);
	}

	public void glProgramParameteri (int program, int pname, int value) {
		GL41.glProgramParameteri(program, pname, value);
	}

	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		GL43.glInvalidateFramebuffer(target, attachments);
	}

	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
	}

	public void glTexStorage2D (int target, int levels, int internalformat, int width, int height) {
		GL42.glTexStorage2D(target, levels, internalformat, width, height);
	}

	public void glTexStorage3D (int target, int levels, int internalformat, int width, int height, int depth) {
		GL42.glTexStorage3D(target, levels, internalformat, width, height, depth);
	}

	public void glGetInternalformativ (int target, int internalformat, int pname, int bufSize, IntBuffer params) {
		GL42.glGetInternalformat(target, internalformat, pname, params);
	}

}
