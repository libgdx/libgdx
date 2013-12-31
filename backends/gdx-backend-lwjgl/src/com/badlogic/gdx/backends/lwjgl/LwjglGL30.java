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
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;

import com.badlogic.gdx.graphics.GL10;
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

}
