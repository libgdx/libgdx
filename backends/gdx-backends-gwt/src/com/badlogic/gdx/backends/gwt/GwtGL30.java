/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayBoolean;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.typedarrays.client.Uint8ArrayNative;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint32Array;
import com.google.gwt.webgl.client.WebGL2RenderingContext;
import com.google.gwt.webgl.client.WebGLFramebuffer;
import com.google.gwt.webgl.client.WebGLQuery;
import com.google.gwt.webgl.client.WebGLSampler;
import com.google.gwt.webgl.client.WebGLTexture;
import com.google.gwt.webgl.client.WebGLTransformFeedback;
import com.google.gwt.webgl.client.WebGLUniformLocation;
import com.google.gwt.webgl.client.WebGLVertexArrayObject;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.HasArrayBufferView;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/** @author Simon Gerst
 * @author JamesTKhan */
public class GwtGL30 extends GwtGL20 implements GL30 {
	private final IntMap<WebGLQuery> queries = IntMap.create();
	private int nextQueryId = 1;
	private final IntMap<WebGLSampler> samplers = IntMap.create();
	private int nextSamplerId = 1;
	private final IntMap<WebGLTransformFeedback> feedbacks = IntMap.create();
	private int nextFeedbackId = 1;
	private final IntMap<WebGLVertexArrayObject> vertexArrays = IntMap.create();
	private int nextVertexArrayId = 1;
	private final Uint32Array uIntBuffer = TypedArrays.createUint32Array(2000 * 6);

	final protected WebGL2RenderingContext gl;

	protected GwtGL30 (WebGL2RenderingContext gl) {
		super(gl);
		this.gl = gl;
	}

	private Uint32Array copyUnsigned (IntBuffer buffer) {
		if (GWT.isProdMode()) {
			return ((Uint32Array)((HasArrayBufferView)buffer).getTypedArray()).subarray(buffer.position(), buffer.remaining());
		} else {
			ensureCapacity(buffer);
			for (int i = buffer.position(), j = 0; i < buffer.limit(); i++, j++) {
				uIntBuffer.set(j, buffer.get(i));
			}
			return uIntBuffer.subarray(0, buffer.remaining());
		}
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
		gl.clearBufferfv(buffer, drawbuffer, copy(value));
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
			int id = ids[i];
			WebGLQuery query = queries.get(id);
			deallocateQueryId(id);
			gl.deleteQuery(query);
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
			int id = samplers[i];
			WebGLSampler sampler = this.samplers.get(id);
			deallocateSamplerId(id);
			gl.deleteSampler(sampler);
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
			int id = ids[i];
			WebGLTransformFeedback feedback = feedbacks.get(id);
			deallocateFeedbackId(id);
			gl.deleteTransformFeedback(feedback);
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
			int id = arrays[i];
			WebGLVertexArrayObject vArray = vertexArrays.get(id);
			deallocateVertexArrayId(id);
			gl.deleteVertexArray(vArray);
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
		gl.drawBuffers(copy((IntBuffer)bufs).subarray(0, n));
		bufs.position(startPosition);
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, int indicesOffset, int instanceCount) {
		gl.drawElementsInstanced(mode, count, type, indicesOffset, instanceCount);
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, Buffer indices) {
		gl.drawRangeElements(mode, start, end, count, type, indices.position());
	}

	@Override
	public void glDrawRangeElements (int mode, int start, int end, int count, int type, int offset) {
		gl.drawRangeElements(mode, start, end, count, type, offset);
	}

	@Override
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		int offset) {
		gl.texImage2D(target, level, internalformat, width, height, border, format, type, offset);
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
		throw new UnsupportedOperationException("glFlushMappedBufferRange not supported on WebGL2");
	}

	@Override
	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer) {
		gl.framebufferTextureLayer(target, attachment, textures.get(texture), level, layer);
	}

	@Override
	public void glGenQueries (int n, int[] ids, int offset) {
		for (int i = offset; i < offset + n; i++) {
			WebGLQuery query = gl.createQuery();
			int id = allocateQueryId(query);
			ids[i] = id;
		}
	}

	@Override
	public void glGenQueries (int n, IntBuffer ids) {
		int startPosition = ids.position();
		for (int i = 0; i < n; i++) {
			WebGLQuery query = gl.createQuery();
			int id = allocateQueryId(query);
			ids.put(id);
		}
		ids.position(startPosition);
	}

	@Override
	public void glGenSamplers (int count, int[] samplers, int offset) {
		for (int i = offset; i < offset + count; i++) {
			WebGLSampler sampler = gl.createSampler();
			int id = allocateSamplerId(sampler);
			samplers[i] = id;
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
			WebGLTransformFeedback feedback = gl.createTransformFeedback();
			int id = allocateFeedbackId(feedback);
			ids[i] = id;
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
			WebGLVertexArrayObject vArray = gl.createVertexArray();
			int id = allocateVertexArrayId(vArray);
			arrays[i] = id;
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
		if (pname == GL30.GL_UNIFORM_BLOCK_BINDING || pname == GL30.GL_UNIFORM_BLOCK_DATA_SIZE
			|| pname == GL30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS) {
			params.put(gl.getActiveUniformBlockParameteri(programs.get(program), uniformBlockIndex, pname));
		} else if (pname == GL30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES) {
			Uint32Array array = gl.getActiveUniformBlockParameterv(programs.get(program), uniformBlockIndex, pname);
			for (int i = 0; i < array.length(); i++) {
				params.put(i, (int)array.get(i));
			}
		} else if (pname == GL30.GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER
			|| pname == GL30.GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER) {
			boolean result = gl.getActiveUniformBlockParameterb(programs.get(program), uniformBlockIndex, pname);
			params.put(result ? GL20.GL_TRUE : GL20.GL_FALSE);
		} else {
			throw new GdxRuntimeException("Unsupported pname passed to glGetActiveUniformBlockiv");
		}
		params.flip();
	}

	@Override
	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return gl.getActiveUniformBlockName(programs.get(program), uniformBlockIndex);
	}

	@Override
	public void glGetActiveUniformBlockName (int program, int uniformBlockIndex, Buffer length, Buffer uniformBlockName) {
		throw new UnsupportedOperationException("glGetActiveUniformBlockName with Buffer parameters not supported on WebGL2");
	}

	@Override
	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		if (pname == GL30.GL_UNIFORM_IS_ROW_MAJOR) {
			JsArrayBoolean arr = gl.getActiveUniformsb(programs.get(program), copy(uniformIndices).subarray(0, uniformCount), pname);
			for (int i = 0; i < uniformCount; i++) {
				params.put(i, arr.get(i) ? GL20.GL_TRUE : GL20.GL_FALSE);
			}
		} else {
			JsArrayInteger arr = gl.getActiveUniformsi(programs.get(program), copy(uniformIndices).subarray(0, uniformCount), pname);
			for (int i = 0; i < uniformCount; i++) {
				params.put(i, arr.get(i));
			}
		}
		params.flip();
	}

	@Override
	public void glGetBufferParameteri64v (int target, int pname, LongBuffer params) {
		throw new UnsupportedOperationException("glGetBufferParameteri64v not supported on WebGL2");
	}

	@Override
	public Buffer glGetBufferPointerv (int target, int pname) {
		throw new UnsupportedOperationException("glGetBufferPointerv not supported on WebGL2");
	}

	@Override
	public void glGetFloatv (int pname, FloatBuffer params) {
		// Override GwtGL20 method to check if it's a pname introduced with GL30.
		if (pname == GL30.GL_MAX_TEXTURE_LOD_BIAS) {
			params.put(0, gl.getParameterf(pname));
			params.flip();
		} else {
			super.glGetFloatv(pname, params);
		}
	}

	@Override
	public int glGetFragDataLocation (int program, String name) {
		return gl.getFragDataLocation(programs.get(program), name);
	}

	@Override
	public void glGetIntegerv (int pname, IntBuffer params) {
		// Override GwtGL20 method to check if it's a pname introduced with GL30.
		switch (pname) {
		case GL30.GL_DRAW_BUFFER0:
		case GL30.GL_DRAW_BUFFER1:
		case GL30.GL_DRAW_BUFFER2:
		case GL30.GL_DRAW_BUFFER3:
		case GL30.GL_DRAW_BUFFER4:
		case GL30.GL_DRAW_BUFFER5:
		case GL30.GL_DRAW_BUFFER6:
		case GL30.GL_DRAW_BUFFER7:
		case GL30.GL_DRAW_BUFFER8:
		case GL30.GL_DRAW_BUFFER9:
		case GL30.GL_DRAW_BUFFER10:
		case GL30.GL_FRAGMENT_SHADER_DERIVATIVE_HINT:
		case GL30.GL_MAX_3D_TEXTURE_SIZE:
		case GL30.GL_MAX_ARRAY_TEXTURE_LAYERS:
		case GL30.GL_MAX_COLOR_ATTACHMENTS:
		case GL30.GL_MAX_DRAW_BUFFERS:
		case GL30.GL_MAX_ELEMENTS_INDICES:
		case GL30.GL_MAX_ELEMENTS_VERTICES:
		case GL30.GL_MAX_FRAGMENT_INPUT_COMPONENTS:
		case GL30.GL_MAX_FRAGMENT_UNIFORM_BLOCKS:
		case GL30.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS:
		case GL30.GL_MAX_PROGRAM_TEXEL_OFFSET:
		case GL30.GL_MAX_SAMPLES:
		case GL30.GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS:
		case GL30.GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS:
		case GL30.GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS:
		case GL30.GL_MAX_UNIFORM_BUFFER_BINDINGS:
		case GL30.GL_MAX_VARYING_COMPONENTS:
		case GL30.GL_MAX_VERTEX_OUTPUT_COMPONENTS:
		case GL30.GL_MAX_VERTEX_UNIFORM_BLOCKS:
		case GL30.GL_MAX_VERTEX_UNIFORM_COMPONENTS:
		case GL30.GL_MIN_PROGRAM_TEXEL_OFFSET:
		case GL30.GL_PACK_ROW_LENGTH:
		case GL30.GL_PACK_SKIP_PIXELS:
		case GL30.GL_PACK_SKIP_ROWS:
		case GL30.GL_READ_BUFFER:
		case GL30.GL_UNPACK_IMAGE_HEIGHT:
		case GL30.GL_UNPACK_ROW_LENGTH:
		case GL30.GL_UNPACK_SKIP_IMAGES:
		case GL30.GL_UNPACK_SKIP_PIXELS:
		case GL30.GL_UNPACK_SKIP_ROWS:
			params.put(0, gl.getParameteri(pname));
			params.flip();
			return;
		case GL30.GL_DRAW_FRAMEBUFFER_BINDING:
		case GL30.GL_READ_FRAMEBUFFER_BINDING:
			WebGLFramebuffer fbo = gl.getParametero(pname);
			if (fbo == null) {
				params.put(0);
			} else {
				params.put(frameBuffers.getKey(fbo));
			}
			params.flip();
			return;
		case GL30.GL_TEXTURE_BINDING_2D_ARRAY:
		case GL30.GL_TEXTURE_BINDING_3D:
			WebGLTexture tex = gl.getParametero(pname);
			if (tex == null) {
				params.put(0);
			} else {
				params.put(textures.getKey(tex));
			}
			params.flip();
			return;
		case GL30.GL_VERTEX_ARRAY_BINDING:
			WebGLVertexArrayObject obj = gl.getParametero(pname);
			if (obj == null) {
				params.put(0);
			} else {
				params.put(vertexArrays.getKey(obj));
			}
			params.flip();
			return;
		default:
			// Assume it is a GL20 pname
			super.glGetIntegerv(pname, params);
		}
	}

	@Override
	public void glGetInteger64v (int pname, LongBuffer params) {
		switch (pname) {
		case GL30.GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS:
		case GL30.GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS:
		case GL30.GL_MAX_ELEMENT_INDEX:
		case GL30.GL_MAX_SERVER_WAIT_TIMEOUT:
		case GL30.GL_MAX_UNIFORM_BLOCK_SIZE:
			params.put(gl.getParameteri64(pname));
			params.flip();
			return;
		default:
			throw new UnsupportedOperationException("Given glGetInteger64v enum not supported on WebGL2");
		}
	}

	@Override
	public void glGetFramebufferAttachmentParameteriv (int target, int attachment, int pname, IntBuffer params) {
		switch (pname) {
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE:
		case GL30.GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER:
			params.put(0, gl.getFramebufferAttachmentParameteri(target, attachment, pname));
			params.flip();
			break;
		default:
			// Assume it is a GL20 pname
			super.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
		}
	}

	@Override
	public void glGetQueryiv (int target, int pname, IntBuffer params) {
		// Not 100% clear on this one. Returning the integer key for the query.
		// Similar to how GwtGL20 handles FBO in glGetIntegerv
		WebGLQuery query = gl.getQuery(target, pname);
		if (query == null) {
			params.put(0);
		} else {
			params.put(queries.getKey(query));
		}
		params.flip();
	}

	@Override
	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params) {
		// In WebGL2 getQueryObject was renamed to getQueryParameter
		if (pname == GL30.GL_QUERY_RESULT) {
			params.put(gl.getQueryParameteri(queries.get(id), pname));
		} else if (pname == GL30.GL_QUERY_RESULT_AVAILABLE) {
			boolean result = gl.getQueryParameterb(queries.get(id), pname);
			params.put(result ? GL20.GL_TRUE : GL20.GL_FALSE);
		} else {
			throw new GdxRuntimeException("Unsupported pname passed to glGetQueryObjectuiv");
		}
		params.flip();
	}

	@Override
	public void glGetSamplerParameterfv (int sampler, int pname, FloatBuffer params) {
		params.put(gl.getSamplerParameterf(samplers.get(sampler), pname));
		params.flip();
	}

	@Override
	public void glGetSamplerParameteriv (int sampler, int pname, IntBuffer params) {
		params.put(gl.getSamplerParameteri(samplers.get(sampler), pname));
		params.flip();
	}

	@Override
	public String glGetStringi (int name, int index) {
		throw new UnsupportedOperationException("glGetStringi not supported on WebGL2");
	}

	@Override
	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return gl.getUniformBlockIndex(programs.get(program), uniformBlockName);
	}

	@Override
	public void glGetUniformIndices (int program, String[] uniformNames, IntBuffer uniformIndices) {
		JsArrayInteger array = gl.getUniformIndices(programs.get(program), uniformNames);
		for (int i = 0; i < array.length(); i++) {
			uniformIndices.put(i, array.get(i));
		}
		uniformIndices.flip();
	}

	@Override
	public void glGetUniformuiv (int program, int location, IntBuffer params) {
		// fv and iv also not implemented in GwtGL20
		throw new UnsupportedOperationException("glGetUniformuiv not implemented on WebGL2");
	}

	@Override
	public void glGetVertexAttribIiv (int index, int pname, IntBuffer params) {
		// fv and iv also not implemented in GwtGL20
		throw new UnsupportedOperationException("glGetVertexAttribIiv not implemented on WebGL2");
	}

	@Override
	public void glGetVertexAttribIuiv (int index, int pname, IntBuffer params) {
		// fv and iv also not implemented in GwtGL20
		throw new UnsupportedOperationException("glGetVertexAttribIuiv not implemented on WebGL2");
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
	public Buffer glMapBufferRange (int target, int offset, int length, int access) {
		throw new UnsupportedOperationException("glMapBufferRange not supported on WebGL2");
	}

	@Override
	public void glPauseTransformFeedback () {
		gl.pauseTransformFeedback();
	}

	@Override
	public void glProgramParameteri (int program, int pname, int value) {
		// Per WebGL2 spec: Accessing binary representations of compiled shader programs is not supported in the WebGL 2.0 API.
		// This includes OpenGL ES 3.0 GetProgramBinary, ProgramBinary, and ProgramParameteri entry points
		throw new UnsupportedOperationException("glProgramParameteri not supported on WebGL2");
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
		gl.samplerParameterf(samplers.get(sampler), pname, param);
	}

	@Override
	public void glSamplerParameterfv (int sampler, int pname, FloatBuffer param) {
		gl.samplerParameterf(samplers.get(sampler), pname, param.get());
	}

	@Override
	public void glSamplerParameteri (int sampler, int pname, int param) {
		gl.samplerParameteri(samplers.get(sampler), pname, param);
	}

	@Override
	public void glSamplerParameteriv (int sampler, int pname, IntBuffer param) {
		gl.samplerParameterf(samplers.get(sampler), pname, param.get());
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels) {
		// Taken from glTexImage2D
		if (pixels == null) {
			gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, (ArrayBufferView)null);
			return;
		}

		if (pixels.limit() > 1) {
			HasArrayBufferView arrayHolder = (HasArrayBufferView)pixels;
			ArrayBufferView webGLArray = arrayHolder.getTypedArray();
			ArrayBufferView buffer;
			if (pixels instanceof FloatBuffer) {
				buffer = webGLArray;
			} else {
				int length = pixels.remaining();
				if (!(pixels instanceof ByteBuffer)) {
					// It seems for ByteBuffer we don't need this byte conversion
					length *= 4;
				}
				int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;
				buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, length);
			}
			gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, buffer);
		} else {
			Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer)pixels).get(0));
			// Prefer to use the HTMLImageElement when possible, since reading from the CanvasElement can be lossy.
			if (pixmap.canUseImageElement()) {
				gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, pixmap.getImageElement());
			} else if (pixmap.canUseVideoElement()) {
				gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, pixmap.getVideoElement());
			} else {
				gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, pixmap.getCanvasElement());
			}
		}
	}

	@Override
	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, int offset) {
		gl.texImage3D(target, level, internalformat, width, height, depth, border, format, type, offset);
	}

	@Override
	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		int offset) {
		gl.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, offset);
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
				int length = pixels.remaining();
				if (!(pixels instanceof ByteBuffer)) {
					// It seems for ByteBuffer we don't need this byte conversion
					length *= 4;
				}
				int byteOffset = webGLArray.byteOffset() + pixels.position() * 4;
				buffer = Uint8ArrayNative.create(webGLArray.buffer(), byteOffset, length);
			}
			gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, buffer);
		} else {
			Pixmap pixmap = Pixmap.pixmaps.get(((IntBuffer)pixels).get(0));
			gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type,
				pixmap.getCanvasElement());
		}
	}

	@Override
	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, int offset) {
		gl.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}

	@Override
	public void glTransformFeedbackVaryings (int program, String[] varyings, int bufferMode) {
		gl.transformFeedbackVaryings(programs.get(program), varyings, bufferMode);
	}

	@Override
	public void glUniform1uiv (int location, int count, IntBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform1uiv(loc, copyUnsigned(value), 0, count);
	}

	@Override
	public void glUniform3uiv (int location, int count, IntBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform3uiv(loc, copyUnsigned(value), 0, count);
	}

	@Override
	public void glUniform4uiv (int location, int count, IntBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniform4uiv(loc, copyUnsigned(value), 0, count);
	}

	@Override
	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		gl.uniformBlockBinding(programs.get(program), uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix2x3fv(loc, transpose, copy(value));
	}

	@Override
	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix2x4fv(loc, transpose, copy(value), 0, count);
	}

	@Override
	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix3x2fv(loc, transpose, copy(value), 0, count);
	}

	@Override
	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix3x4fv(loc, transpose, copy(value), 0, count);
	}

	@Override
	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix4x2fv(loc, transpose, copy(value), 0, count);
	}

	@Override
	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value) {
		WebGLUniformLocation loc = getUniformLocation(location);
		gl.uniformMatrix4x3fv(loc, transpose, copy(value), 0, count);
	}

	@Override
	public boolean glUnmapBuffer (int target) {
		throw new UnsupportedOperationException("glUnmapBuffer not supported on WebGL2");
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
