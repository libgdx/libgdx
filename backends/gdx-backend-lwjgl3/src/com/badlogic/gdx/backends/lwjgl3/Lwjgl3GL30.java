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

package com.badlogic.gdx.backends.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL43;

import com.badlogic.gdx.utils.GdxRuntimeException;

class Lwjgl3GL30 extends Lwjgl3GL20 implements com.badlogic.gdx.graphics.GL30 {
	@Override
	public void glReadBuffer (int mode) {		
		GL11.glReadBuffer(mode);
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		if(indices instanceof ByteBuffer) GL12.glDrawRangeElements(mode, start, end, (ByteBuffer)indices);
		else if(indices instanceof ShortBuffer) GL12.glDrawRangeElements(mode, start, end, (ShortBuffer)indices);
		else if(indices instanceof IntBuffer) GL12.glDrawRangeElements(mode, start, end, (IntBuffer)indices);
		else throw new GdxRuntimeException("indices must be byte, short or int buffer");
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		GL12.glDrawRangeElements(mode, start, end, count, type, offset);
	}

	@Override
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

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		GL12.glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
			int format, int type, Buffer pixels) {
		if (pixels instanceof ByteBuffer)
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

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

	@Override
	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GL15.glGenQueries();
		}
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			ids.put(GL15.glGenQueries());
		}
	}

	@Override
	public void glDeleteQueries (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL15.glDeleteQueries(ids[i]);
		}
	}

	@Override
	public void glDeleteQueries (int n, IntBuffer ids) {
		for(int i = 0; i < n; i++) {
			GL15.glDeleteQueries(ids.get());
		}
	}

	@Override
	public boolean glIsQuery (int id) {
		return GL15.glIsQuery(id);
	}

	@Override
	public void glBeginQuery (int target, int id) {
		GL15.glBeginQuery(target, id);
	}

	@Override
	public void glEndQuery (int target) {
		GL15.glEndQuery(target);
	}

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		GL15.glGetQueryiv(target, pname, params);
	}

	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		GL15.glGetQueryObjectuiv(id, pname, params);
	}

	@Override
	public boolean glUnmapBuffer (int target) {
		return GL15.glUnmapBuffer(target);
	}

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		// FIXME glGetBufferPointerv needs a proper translation
		// return GL15.glGetBufferPointer(target, pname);
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void glDrawBuffers (int n, IntBuffer bufs) {
		GL20.glDrawBuffers(bufs);
	}

	@Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x3fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix2x4fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix3x4fv(location, transpose, value);
	}


	@Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		GL21.glUniformMatrix4x3fv(location, transpose, value);
	}

	@Override
	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public void glBindFramebuffer (int target, int framebuffer) {
		GL30.glBindFramebuffer(target, framebuffer);
	}

	@Override
	public void glBindRenderbuffer (int target, int renderbuffer) {
		GL30.glBindRenderbuffer(target, renderbuffer);
	}

	@Override
	public int glCheckFramebufferStatus (int target) {
		return GL30.glCheckFramebufferStatus(target);
	}

	@Override
	public void glDeleteFramebuffers (int n, IntBuffer framebuffers) {
		GL30.glDeleteFramebuffers(framebuffers);
	}

	@Override
	public void glDeleteFramebuffer (int framebuffer) {
		GL30.glDeleteFramebuffers(framebuffer);
	}

	@Override
	public void glDeleteRenderbuffers (int n, IntBuffer renderbuffers) {
		GL30.glDeleteRenderbuffers(renderbuffers);
	}

	@Override
	public void glDeleteRenderbuffer (int renderbuffer) {
		GL30.glDeleteRenderbuffers(renderbuffer);
	}

	@Override
	public void glGenerateMipmap (int target) {
		GL30.glGenerateMipmap(target);
	}

	@Override
	public void glGenFramebuffers (int n, IntBuffer framebuffers) {
		GL30.glGenFramebuffers(framebuffers);
	}

	@Override
	public int glGenFramebuffer () {
		return GL30.glGenFramebuffers();
	}

	@Override
	public void glGenRenderbuffers (int n, IntBuffer renderbuffers) {
		GL30.glGenRenderbuffers(renderbuffers);
	}

	@Override
	public int glGenRenderbuffer () {
		return GL30.glGenRenderbuffers();
	}

	@Override
	public void glGetRenderbufferParameteriv (int target, int pname, IntBuffer params) {
		GL30.glGetRenderbufferParameteriv(target, pname, params);
	}

	@Override
	public boolean glIsFramebuffer (int framebuffer) {
		return GL30.glIsFramebuffer(framebuffer);
	}

	@Override
	public boolean glIsRenderbuffer (int renderbuffer) {
		return GL30.glIsRenderbuffer(renderbuffer);
	}

	@Override
	public void glRenderbufferStorage (int target, int internalformat, int width, int height) {
		GL30.glRenderbufferStorage(target, internalformat, width, height);
	}

	@Override
	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	@Override
	public void glFramebufferTexture2D (int target, int attachment, int textarget, int texture, int level) {
		GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
	}

	@Override
	public void glFramebufferRenderbuffer (int target, int attachment, int renderbuffertarget, int renderbuffer) {
		GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
	}

	@Override
	public void glFlushMappedBufferRange (int target, int offset, int length) {
		GL30.glFlushMappedBufferRange(target, offset, length);
	}

	@Override
	public void glBindVertexArray (int array) {
		GL30.glBindVertexArray(array);
	}

	@Override
	public void glDeleteVertexArrays (int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL30.glDeleteVertexArrays(arrays[i]);
		}
	}

	@Override
	public void glDeleteVertexArrays (int n, IntBuffer arrays) {
		GL30.glDeleteVertexArrays(arrays);
	}

	@Override
	public void glGenVertexArrays (int n, int[] arrays, int offset) {
		for(int i = offset; i < offset + n; i++) {
			arrays[i] = GL30.glGenVertexArrays();
		}
	}

	@Override
	public void glGenVertexArrays (int n, IntBuffer arrays) {
		GL30.glGenVertexArrays(arrays);
	}

	@Override
	public boolean glIsVertexArray (int array) {
		return GL30.glIsVertexArray(array);
	}

	@Override
	public void glBeginTransformFeedback (int primitiveMode) {
		GL30.glBeginTransformFeedback(primitiveMode);
	}

	@Override
	public void glEndTransformFeedback () {
		GL30.glEndTransformFeedback();
	}

	@Override
	public void glBindBufferRange (int target, int index, int buffer, int offset, int size) {
		GL30.glBindBufferRange(target, index, buffer, offset, size);
	}

	@Override
	public void glBindBufferBase (int target, int index, int buffer) {
		GL30.glBindBufferBase(target, index, buffer);
	}

	@Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
	}

	@Override
	public void glVertexAttribIPointer (int index, int size, int type, int stride, int offset) {
		GL30.glVertexAttribIPointer(index, size, type, stride, offset);
	}

	@Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribIiv(index, pname, params);
	}

	@Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		GL30.glGetVertexAttribIuiv(index, pname, params);
	}

	@Override
	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4i(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		GL30.glVertexAttribI4ui(index, x, y, z, w);
	}
	
	@Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		GL30.glGetUniformuiv(program, location, params);
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		return GL30.glGetFragDataLocation(program, name);
	}

	@Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		GL30.glUniform1uiv(location, value);
	}

	@Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		GL30.glUniform3uiv(location, value);
	}

	@Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		GL30.glUniform4uiv(location, value);
	}

	@Override
	public void glClearBufferiv (int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBufferiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		GL30.glClearBufferuiv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfv (int buffer, int drawbuffer, FloatBuffer value) {
		GL30.glClearBufferfv(buffer, drawbuffer, value);
	}

	@Override
	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		GL30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
	}

	@Override
	public String glGetStringi (int name, int index) {
		return GL30.glGetStringi(name, index);
	}

	@Override
	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		GL31.glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	@Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		GL31.glGetUniformIndices(program, uniformNames, uniformIndices);
	}

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		GL31.glGetActiveUniformsiv(program, uniformIndices, pname, params);
	}

	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return GL31.glGetUniformBlockIndex(program, uniformBlockName);
	}

	@Override
	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		params.put(GL31.glGetActiveUniformBlocki(program, uniformBlockIndex, pname));
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, (IntBuffer)length, (ByteBuffer)uniformBlockName);
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, 1024);
	}

	@Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instanceCount) {
		GL31.glDrawArraysInstanced(mode, first, count, instanceCount);
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		GL31.glDrawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
		
	}

	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		GL32.glGetInteger64v(pname, params);
	}

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		params.put(GL32.glGetBufferParameteri64(target, pname));
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			samplers[i] = GL33.glGenSamplers();
		}
	}

	@Override
	public void glGenSamplers (int count, IntBuffer samplers) {
		GL33.glGenSamplers(samplers);
	}

	@Override
	public void glDeleteSamplers (int count, int[] samplers, int offset) {
		for(int i = offset; i < offset + count; i++) {
			GL33.glDeleteSamplers(samplers[i]);
		}
	}

	@Override
	public void glDeleteSamplers (int count, IntBuffer samplers) {
		GL33.glDeleteSamplers(samplers);
	}

	@Override
	public boolean glIsSampler (int sampler) {
		return GL33.glIsSampler(sampler);
	}

	@Override
	public void glBindSampler (int unit, int sampler) {
		GL33.glBindSampler(unit, sampler);
	}

	@Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		GL33.glSamplerParameteri(sampler, pname, param);
	}

	@Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		GL33.glSamplerParameteriv(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterf (int sampler, int pname, float param) {
		GL33.glSamplerParameterf(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		GL33.glSamplerParameterfv(sampler, pname, param);
	}

	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		GL33.glGetSamplerParameterIiv(sampler, pname, params);
	}

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		GL33.glGetSamplerParameterfv(sampler, pname, params);		
	}

	@Override
	public void glVertexAttribDivisor (int index, int divisor) {
		GL33.glVertexAttribDivisor(index, divisor);
	}

	@Override
	public void glBindTransformFeedback (int target, int id) {
		GL40.glBindTransformFeedback(target, id);
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			GL40.glDeleteTransformFeedbacks(ids[i]);
		}
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glDeleteTransformFeedbacks(ids);
	}

	@Override
	public void glGenTransformFeedbacks (int n, int[] ids, int offset) {
		for(int i = offset; i < offset + n; i++) {
			ids[i] = GL40.glGenTransformFeedbacks();
		}
	}

	@Override
	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		GL40.glGenTransformFeedbacks(ids);
	}

	@Override
	public boolean glIsTransformFeedback (int id) {
		return GL40.glIsTransformFeedback(id);
	}

	@Override
	public void glPauseTransformFeedback () {
		GL40.glPauseTransformFeedback();
	}

	@Override
	public void glResumeTransformFeedback () {
		GL40.glResumeTransformFeedback();
	}

	@Override
	public void glProgramParameteri (int program, int pname, int value) {
		GL41.glProgramParameteri(program, pname, value);
	}

	@Override
	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		GL43.glInvalidateFramebuffer(target, attachments);
	}

	@Override
	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
	}
}
