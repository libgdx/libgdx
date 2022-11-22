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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.webgl.client.WebGL2RenderingContext;
import com.google.gwt.webgl.client.WebGLQuery;
import com.google.gwt.webgl.client.WebGLSampler;
import com.google.gwt.webgl.client.WebGLTransformFeedback;
import com.google.gwt.webgl.client.WebGLVertexArrayObject;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.HasArrayBufferView;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/** @author Simon Gerst */
public class GwtGL30 extends GwtGL20 implements GL30 {
	final IntMap<WebGLQuery> queries = IntMap.create();
	int nextQueryId = 1;
	final IntMap<WebGLSampler> samplers = IntMap.create();
	int nextSamplerId = 1;
	final IntMap<WebGLTransformFeedback> feedbacks = IntMap.create();
	int nextFeedbackId = 1;
	final IntMap<WebGLVertexArrayObject> vertexArrays = IntMap.create();
	int nextVertexArrayId = 1;

	final protected WebGL2RenderingContext gl;

	protected GwtGL30 (WebGL2RenderingContext gl) {
		super(gl);
		this.gl = gl;
	}

	private int allocateQueryId (WebGLQuery query) {
		int id = nextQueryId++;
		queries.put(id, query);
		return id;
	}

	private void deallocateQueryId (int id) {
		queries.remove(id);
	}

	private int allocateSamplerId (WebGLSampler query) {
		int id = nextSamplerId++;
		samplers.put(id, query);
		return id;
	}

	private void deallocateFeedbackId (int id) {
		feedbacks.remove(id);
	}

	private int allocateFeedbackId (WebGLTransformFeedback feedback) {
		int id = nextFeedbackId++;
		feedbacks.put(id, feedback);
		return id;
	}

	private void deallocateSamplerId (int id) {
		samplers.remove(id);
	}

	private int allocateVertexArrayId (WebGLVertexArrayObject vArray) {
		int id = nextVertexArrayId++;
		vertexArrays.put(id, vArray);
		return id;
	}

	private void deallocateVertexArrayId (int id) {
		vertexArrays.remove(id);
	}

	// private Int32Array intBufferToNativeJsArray (int length, IntBuffer bufs) {
	// Int32Array array = TypedArrays.createInt32Array(length);
	// array.set((Int32Array)((HasArrayBufferView)bufs).getTypedArray().buffer());
	// return array;
	// }

	@Override
	public void glBeginQuery (int target, int id) {
		gl.beginQuery(target, queries.get(id));
	}

	@Override
	public void glBeginTransformFeedback (int primitiveMode) {
		gl.beginTransformFeedback(primitiveMode);
	}

	@Override
	public void glBindBufferBase (int target, int index, int buffer) {
		gl.bindBufferBase(target, index, buffers.get(buffer));
	}

	@Override
	public void glBindBufferRange (int target, int index, int buffer, int offset, int size) {
		gl.bindBufferRange(target, index, buffers.get(buffer), offset, size);
	}

	@Override
	public void glBindSampler (int unit, int sampler) {
		gl.bindSampler(unit, samplers.get(sampler));
	}

	@Override
	public void glBindTransformFeedback (int target, int id) {
		gl.bindTransformFeedback(target, feedbacks.get(id));
	}

	@Override
	public void glBindVertexArray (int array) {
		gl.bindVertexArray(vertexArrays.get(array));
	}

	@Override
	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter) {
		gl.blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
	}

	@Override
	public void glClearBufferfi (int buffer, int drawbuffer, float depth, int stencil) {
		gl.clearBufferfi(buffer, drawbuffer, depth, stencil);
	}

	@Override
	public void glClearBufferfv (int buffer, int drawbuffer, FloatBuffer value) {
		gl.clearBufferfv(buffer, drawbuffer,  copy(value));
	}

	@Override
	public void glClearBufferiv (int buffer, int drawbuffer, IntBuffer value) {
		gl.clearBufferiv(buffer, drawbuffer, copy(value));
	}

	@Override
	public void glClearBufferuiv (int buffer, int drawbuffer, IntBuffer value) {
		gl.clearBufferuiv(buffer, drawbuffer, copy(value));
	}

	@Override
	public void glCopyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size) {
		gl.copyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
	}

	@Override
	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height) {
		gl.copyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}

	@Override
	public void glDeleteQueries (int n, int[] ids, int offset) {
		for (int i = offset; i < offset + n; i++) {
			throw new UnsupportedOperationException();
			// FIXMEgl.deleteQueries(ids[i]);
		}
	}

	@Override
	public void glDeleteQueries (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			int id = ids.get();
			WebGLQuery query = queries.get(id);
			deallocateQueryId(id);
			gl.deleteQuery(query);
		}
		ids.position(startPosition);
	}

	@Override
	public void glDeleteSamplers (int count, int[] samplers, int offset) {
		for (int i = offset; i < offset + count; i++) {
			throw new UnsupportedOperationException();
			// FIXMEgl.deleteSamplers(samplers[i]);
		}
	}

	@Override
	public void glDeleteSamplers (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			int id = ids.get();
			WebGLSampler sampler = samplers.get(id);
			deallocateSamplerId(id);
			gl.deleteSampler(sampler);
		}
		ids.position(startPosition);
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, int[] ids, int offset) {
		for (int i = offset; i < offset + n; i++) {
			throw new UnsupportedOperationException();
			// FIXMEgl.deleteTransformFeedbacks(ids[i]);
		}
	}

	@Override
	public void glDeleteTransformFeedbacks (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			int id = ids.get();
			WebGLTransformFeedback feedback = feedbacks.get(id);
			deallocateFeedbackId(id);
			gl.deleteTransformFeedback(feedback);
		}
		ids.position(startPosition);
	}

	@Override
	public void glDeleteVertexArrays (int n, int[] arrays, int offset) {
		for (int i = offset; i < offset + n; i++) {
			throw new UnsupportedOperationException();
			// FIXMEgl.deleteVertexArrays(arrays[i]);
		}
	}

	@Override
	public void glDeleteVertexArrays (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			int id = ids.get();
			WebGLVertexArrayObject vArray = vertexArrays.get(id);
			deallocateVertexArrayId(id);
			gl.deleteVertexArray(vArray);
		}
		ids.position(startPosition);
	}

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instanceCount) {
		gl.drawArraysInstanced(mode, first, count, instanceCount);
	}

	@Override
	public void glDrawBuffers (int n, IntBuffer bufs) {
		int startPosition = bufs.position();
		Gdx.app.log("GwtGL30", "Args: n: " + n + " bufs: " + bufs);
		for (int i = 0; i < bufs.capacity(); i++) {
			Gdx.app.log("GwtGL30", "Value: i: " + i + " :: " + bufs.get(i));
		}
		gl.drawBuffers(copy((IntBuffer)bufs).subarray(0, n));
		bufs.position(startPosition);
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		gl.drawElementsInstanced(mode, count, type, indicesOffset, instanceCount);

	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		throw new UnsupportedOperationException();
// if (indices instanceof ByteBuffer)
// gl.drawRangeElements(mode, start, end, (ByteBuffer)indices);
// else if (indices instanceof ShortBuffer)
// gl.drawRangeElements(mode, start, end, (ShortBuffer)indices);
// else if (indices instanceof IntBuffer)
// gl.drawRangeElements(mode, start, end, (IntBuffer)indices);
// else
// throw new GdxRuntimeException("indices must be byte, short or int buffer");
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		gl.drawRangeElements(mode, start, end, count, type, offset);
	}

	@Override
	public void glEndQuery (int target) {
		gl.endQuery(target);
	}

	@Override
	public void glEndTransformFeedback () {
		gl.endTransformFeedback();
	}

	@Override
	public void glFlushMappedBufferRange (int target, int offset, int length) {
		throw new UnsupportedOperationException("glFlushMappedBufferRange not available on WebGL2 ");
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		gl.framebufferTextureLayer(target, attachment, textures.get(texture), level, layer);
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		for (int i = offset; i < offset + n; i++) {
			// ids[i] = gl.genQueries();
			throw new UnsupportedOperationException("glGenQueries (int n, int[] ids, int offset) not supported yet");
		}
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			WebGLQuery buffer = gl.createQuery();
			int id = allocateQueryId(buffer);
			ids.put(id);
		}
		ids.position(startPosition);
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		for (int i = offset; i < offset + count; i++) {
			throw new UnsupportedOperationException();
			// FIXME samplers[i] = gl.genSamplers();
		}
	}

	@Override
	public void glGenSamplers (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			WebGLSampler sampler = gl.createSampler();
			int id = allocateSamplerId(sampler);
			ids.put(id);
		}
		ids.position(startPosition);
	}

	@Override
	public void glGenTransformFeedbacks (int n, int[] ids, int offset) {
		for (int i = offset; i < offset + n; i++) {
			throw new UnsupportedOperationException();
			// FIXME ids[i] = gl.genTransformFeedbacks();
		}
	}

	@Override
	public void glGenTransformFeedbacks (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			WebGLTransformFeedback feedback = gl.createTransformFeedback();
			int id = allocateFeedbackId(feedback);
			ids.put(id);
		}
		ids.position(startPosition);
	}

	@Override
	public void glGenVertexArrays (int n, int[] arrays, int offset) {
		for (int i = offset; i < offset + n; i++) {
			throw new UnsupportedOperationException();
			// FIXME arrays[i] = gl.genVertexArrays();
		}
	}

	@Override
	public void glGenVertexArrays (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			WebGLVertexArrayObject vArray = gl.createVertexArray();
			int id = allocateVertexArrayId(vArray);
			ids.put(id);
		}
		ids.position(startPosition);
	}

	@Override
	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// params.put(gl.getActiveUniformBlocki(program, uniformBlockIndex, pname));
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return gl.getActiveUniformBlockName(programs.get(program), uniformBlockIndex);
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		throw new UnsupportedOperationException();
		// gl.getActiveUniformBlockName(program, uniformBlockIndex, (IntBuffer)length, (ByteBuffer)uniformBlockName);
	}

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getActiveUniforms(program, uniformIndices, pname, params);
	}

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		throw new UnsupportedOperationException();
		// params.put(gl.getBufferParameteri64(target, pname));
	}

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		throw new UnsupportedOperationException();
		// return gl.getBufferPointer(target, pname);
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		return gl.getFragDataLocation(programs.get(program), name);
	}

	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getInteger64v(pname, params);
	}

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// params.put(queries.gl.getQuery(target, pname));
	}

	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getQueryObjectuiv(id, pname, params);
	}

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getSamplerParameter(sampler, pname, params);

	}

	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getSamplerParameterI(sampler, pname, params);
	}

	@Override
	public String glGetStringi (int name, int index) {
		throw new UnsupportedOperationException();
		// return gl.getStringi(name, index);
	}

	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return gl.getUniformBlockIndex(programs.get(program), uniformBlockName);
	}

	@Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		throw new UnsupportedOperationException();
		// gl.getUniformIndices(programs.get(program), uniformNames, uniformIndices);
	}

	@Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getUniformuiv(program, location, params);
	}

	@Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getVertexAttribI(index, pname, params);
	}

	@Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		throw new UnsupportedOperationException();
		// gl.getVertexAttribIu(index, pname, params);
	}

	@Override
	public void glInvalidateFramebuffer (int target, int numAttachments, IntBuffer attachments) {
		int startPosition = attachments.position();
		gl.invalidateFramebuffer(target, copy((IntBuffer)attachments).subarray(0, numAttachments));
		attachments.position(startPosition);
	}

	@Override
	public void glInvalidateSubFramebuffer (int target, int numAttachments, IntBuffer attachments, int x, int y, int width,
		int height) {
		int startPosition = attachments.position();
		gl.invalidateSubFramebuffer(target, copy((IntBuffer)attachments).subarray(0, numAttachments), x, y, width, height);
		attachments.position(startPosition);
	}

	@Override
	public boolean glIsQuery (int id) {
		return gl.isQuery(queries.get(id));
	}

	@Override
	public boolean glIsSampler (int id) {
		return gl.isSampler(samplers.get(id));
	}

	@Override
	public boolean glIsTransformFeedback (int id) {
		return gl.isTransformFeedback(feedbacks.get(id));
	}

	@Override
	public boolean glIsVertexArray (int id) {
		return gl.isVertexArray(vertexArrays.get(id));
	}

	@Override
	public Buffer glMapBufferRange(int target, int offset, int length, int access) {
		return null;
	}

	@Override
	public void glPauseTransformFeedback () {
		gl.pauseTransformFeedback();
	}

	@Override
	public void glProgramParameteri (int program, int pname, int value) {
		throw new UnsupportedOperationException("Not supported on WebGL2");
	}

	@Override
	public void glReadBuffer (int mode) {
		gl.readBuffer(mode);
	}

	@Override
	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height) {
		gl.renderbufferStorageMultisample(target, samples, internalformat, width, height);
	}

	@Override
	public void glResumeTransformFeedback () {
		gl.resumeTransformFeedback();
	}

	@Override
	public void glSamplerParameterf (int sampler, int pname, float param) {
		throw new UnsupportedOperationException();
		// gl.samplerParameterf(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		throw new UnsupportedOperationException();
		// gl.samplerParameterfv(samplers.get(sampler), pname, param);
	}

	@Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		throw new UnsupportedOperationException();
		// gl.samplerParameteri(samplers.get(sampler), pname, param);
	}

	@Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		throw new UnsupportedOperationException();
		// gl.samplerParameter(sampler, pname, param);
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels) {
		// Taken from glTexImage2D
		if (pixels == null) {
			gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, (ArrayBufferView) null);
		} else {
			if (pixels.limit() > 1) {
				HasArrayBufferView arrayHolder = (HasArrayBufferView)pixels;
				ArrayBufferView webGLArray = arrayHolder.getTypedArray();
				ArrayBufferView buffer;
				if (pixels instanceof FloatBuffer) {
					buffer = webGLArray;
				} else {
					int length = pixels.remaining();
					int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;
					buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, length);
				}
				gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, buffer);
			} else {
				Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer)pixels).get(0));
				// Prefer to use the HTMLImageElement when possible, since reading from the CanvasElement can be lossy.
				if (pixmap.canUseImageElement()) {
					gl.texImage3D(target, level, internalformat, width, height, depth, border,format, type, pixmap.getImageElement());
				} else if (pixmap.canUseVideoElement()) {
					gl.texImage3D(target, level, internalformat, width, height, depth, border,format, type, pixmap.getVideoElement());
				} else {
					gl.texImage3D(target, level, internalformat, width, height, depth, border,format, type, pixmap.getCanvasElement());
				}
			}
		}
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, Buffer pixels) {
		// Taken from glTexSubImage2D
		if (pixels.limit() > 1) {
			HasArrayBufferView arrayHolder = (HasArrayBufferView)pixels;
			ArrayBufferView webGLArray = arrayHolder.getTypedArray();
			ArrayBufferView buffer;
			if (pixels instanceof FloatBuffer) {
				buffer = webGLArray;
			} else {
				int remainingBytes = pixels.remaining() * 4;
				int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;
				buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, remainingBytes);
			}
			gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, buffer);
		} else {
			Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer)pixels).get(0));
			gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixmap.getCanvasElement());
		}
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

	@Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		throw new UnsupportedOperationException();
		// gl.transformFeedbackVaryings(program, varyings, bufferMode);
	}

	@Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniform1uiv(location, value);
	}

	@Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniform3uiv(location, value);
	}

	@Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniform4uiv(location, value);
	}

	@Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		gl.uniformBlockBinding(programs.get(program), uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix2x3fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix2x4fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix3x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix3x4fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix4x2fv(location, transpose, value);
	}

	@Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		throw new UnsupportedOperationException();
		// gl.uniformMatrix4x3fv(location, transpose, value);
	}

	@Override
	public boolean glUnmapBuffer (int target) {
		throw new UnsupportedOperationException("Not supported in WebGL2");
	}

	@Override
	public void glVertexAttribDivisor (int index, int divisor) {
		gl.vertexAttribDivisor(index, divisor);
	}

	@Override
	public void glVertexAttribI4i (int index, int x, int y, int z, int w) {
		gl.vertexAttribI4i(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribI4ui (int index, int x, int y, int z, int w) {
		gl.vertexAttribI4ui(index, x, y, z, w);
	}

	@Override
	public void glVertexAttribIPointer (int index, int size, int type, int stride, int offset) {
		gl.vertexAttribIPointer(index, size, type, stride, offset);
	}

}
