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

package com.google.gwt.webgl.client;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.UnsafeNativeLong;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/** @author Simon Gerst */
public class WebGL2RenderingContext extends WebGLRenderingContext {
	private static class ClearBufferFVSource {
		// typedef (Float32Array or sequence<float>) ClearBufferFVSource;
	}

	private static class ClearBufferIVSource {
		// typedef (Int32Array or sequence<int>) ClearBufferIVSource;
	}

	private static class ClearBufferUIVSource {
		// typedef (Uint32Array or sequence<int>) ClearBufferUIVSource;
	}

	// FIXME
	private static class TexImageSource {

	}

	private static class UniformMatrixFVSource {
		// typedef (Float32Array or sequence<float>) UniformMatrixFVSource;
	}

	private static class UniformUIVSource { // FIXME
		// typedef (Uint32Array or sequence<int>) UniformUIVSource;
	}

	private static class VertexAttribIVSource {
		// typedef (Int32Array or sequence<int>) VertexAttribIVSource;
	}

	/*
	 * public final native void invalidateFramebuffer (int target, int[] attachments)/*-{ throw "UnsupportedOperation";
	 * 
	 * }-
	 */;// FIXME

	private static class VertexAttribUIVSource {
		// typedef (Uint32Array or sequence<int>) VertexAttribUIVSource;
	}

	/*
	 * public final native void invalidateSubFramebuffer (int target, int[] attachments, int x, int y, int width, int height)/*-{
	 * throw "UnsupportedOperation";
	 * 
	 * }-
	 */;// FIXME

	/** Returns a WebGL2 context for the given canvas element. Returns null if no 3d context is available. */
	public static WebGL2RenderingContext getContext (CanvasElement canvas) {
		return getContext(canvas, WebGLContextAttributes.create());
	}

	/** Returns a WebGL2 context for the given canvas element. Returns null if no 3d context is available. */
	public static native WebGL2RenderingContext getContext (CanvasElement canvas, WebGLContextAttributes attributes) /*-{
		var names = [ "experimental-webgl2", "webgl2" ];
		for (var i = 0; i < names.length; i++) {
			try {
				var ctx = canvas.getContext(names[i], attributes);
				if (ctx != null) {
					if ($wnd.WebGLDebugUtils) {
						if ($wnd.console && $wnd.console.log) {
							console.log('WebGL debugging enabled');
						}
						return $wnd.WebGLDebugUtils.makeDebugContext(ctx);
					}
					return ctx;
				}
			} catch (e) {
				console.log(e);
			}
		}
		return null;
	}-*/;

	protected WebGL2RenderingContext () {
	}

	public final native void beginQuery (int target, WebGLQuery query)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void beginTransformFeedback (int primitiveMode)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Uniform Buffer Objects and Transform Feedback Buffers */
	public final native void bindBufferBase (int target, int index, WebGLBuffer buffer)/*-{
		this.bindBufferBase(target, index, buffer)
	}-*/;

	public final native void bindBufferRange (int target, int index, WebGLBuffer buffer, int offset, int size)/*-{
		this.bindBufferRange(target, index, buffer, offset, size)
	}-*/;

	public final native void bindSampler (int unit, WebGLSampler sampler)/*-{
		throw "UnsupportedOperation";
	}-*/;

	public final native void bindTransformFeedback (int target, WebGLTransformFeedback id)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void bindVertexArray (WebGLVertexArrayObject array)/*-{
		this.bindVertexArray(array)
	}-*/;

	/* Framebuffer objects */
	public final native void blitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
		int dstY1, int mask, int filter)/*-{
		this.blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1,
				dstY1, mask, filter)
	}-*/;

	public final native void clearBufferfi (int buffer, int drawbuffer, float depth, int stencil)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void clearBufferfv (int buffer, int drawbuffer, ClearBufferFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void clearBufferiv (int buffer, int drawbuffer, ClearBufferIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void clearBufferuiv (int buffer, int drawbuffer, ClearBufferUIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// FIXME
	public final native int clientWaitSync (WebGLSync sync, int flags, /* GLint64 */int timeout)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void compressedTexImage3D (int target, int level, int internalformat, int width, int height, int depth,
		int border, ArrayBufferView data)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void compressedTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width,
		int height, int depth, int format, ArrayBufferView data)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Buffer objects */
	public final native void copyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size)/*-{
		this.copyBufferSubData(readTarget, writeTarget, readOffset,
				writeOffset, size)
	}-*/;

	public final native void copyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
		int width, int height)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Query Objects */
	public final native WebGLQuery createQuery ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Sampler Objects */
	public final native WebGLSampler createSampler ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Transform Feedback */
	public final native WebGLTransformFeedback createTransformFeedback ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Vertex Array Objects */
	public final native WebGLVertexArrayObject createVertexArray ()/*-{
		return this.createVertexArray()
	}-*/;

	public final native void deleteQuery (WebGLQuery query)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void deleteSampler (WebGLSampler sampler)/*-{
		throw "UnsupportedOperation";
	}-*/;

	public final native void deleteSync (WebGLSync sync)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void deleteTransformFeedback (WebGLTransformFeedback transformFeedback)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void deleteVertexArray (WebGLVertexArrayObject vertexArray)/*-{
		this.deleteVertexArray(vertexArray)
	}-*/;

	public final native void drawArraysInstanced (int mode, int first, int count, int instanceCount)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Multiple Render Targets */
	// Official
	public final native void drawBuffers (int[] buffers)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// Inofficial
	/*public final native void drawBuffers (JavaScriptObject buffers)/*-{
		this.drawBuffers(buffers);
	}-*/;

	public final native void drawBuffers (Int32Array buffers)/*-{
		this.drawBuffers(buffers);
	}-*/;

	public final native void drawElementsInstanced (int mode, int count, int type, int offset, int instanceCount)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void drawRangeElements (int mode, int start, int end, int count, int type, int offset)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void endQuery (int target)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void endTransformFeedback ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Sync objects */
	public final native WebGLSync fenceSync (int condition, int flags)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void framebufferTextureLayer (int target, int attachment, WebGLTexture texture, int level, int layer)/*-{
		this.framebufferTextureLayer(target, attachment, texture, level, layer)
	}-*/;

	public final native String getActiveUniformBlockName (WebGLProgram program, int uniformBlockIndex)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// FIXME
	public final native/* any */Object getActiveUniformBlockParameter (WebGLProgram program, int uniformBlockIndex, int pname)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// FIXME
	public final native/* any */Object getActiveUniforms (WebGLProgram program, int[] uniformIndices, int pname)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void getBufferSubData (int target, int offset, ArrayBuffer returnedData)/*-{
		this.getBufferSubData(target, offset, returnedData)
	}-*/;

	/* Programs and shaders */
	public final native int getFragDataLocation (WebGLProgram program, String name)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// FIXME
	public final native/* any */Object getIndexedParameter (int target, int index)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Renderbuffer objects */
	// FIXME
	public final native/* any */Object getInternalformatParameter (int target, int internalformat, int pname)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native WebGLQuery getQuery (int target, int pname)/*-{
		throw "UnsupportedOperation";
	}-*/;

	// FIXME
	public final native/* any */Object getQueryParameter (WebGLQuery query, int pname)/*-{
		throw "UnsupportedOperation";
	}-*/;

	// FIXME
	public final native/* any */Object getSamplerParameter (WebGLSampler sampler, int pname)/*-{
		throw "UnsupportedOperation";
	}-*/;

	// FIXME
	public final native/* any */Object getSyncParameter (WebGLSync sync, int pname)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native WebGLActiveInfo getTransformFeedbackVarying (WebGLProgram program, int index)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native int getUniformBlockIndex (WebGLProgram program, String uniformBlockName)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* FIXME Not sure if this is right */
	public final native JsArrayInteger getUniformIndices (WebGLProgram program, JsArrayString uniformNames)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void invalidateFramebuffer (int target, Int32Array attachments)/*-{
		this.invalidateFramebuffer(target, attachments)
	}-*/;

	public final native void invalidateSubFramebuffer (int target, Int32Array attachments, int x, int y, int width, int height)/*-{
		invalidateSubFramebuffer(target, attachments, x, y, width, height)
	}-*/;

	public final native boolean isQuery (WebGLQuery query)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native boolean isSampler (WebGLSampler sampler)/*-{
		throw "UnsupportedOperation";
	}-*/;

	public final native boolean isSync (WebGLSync sync)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native boolean isTransformFeedback (WebGLTransformFeedback transformFeedback)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native boolean isVertexArray (WebGLVertexArrayObject vertexArray)/*-{
		return this.isVertexArray(vertexArray)
	}-*/;

	public final native void pauseTransformFeedback ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void readBuffer (int src)/*-{
		this.readBuffer(src)
	}-*/;

	public final native void renderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void resumeTransformFeedback ()/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void samplerParameterf (WebGLSampler sampler, int pname, float param)/*-{
		throw "UnsupportedOperation";
	}-*/;

	public final native void samplerParameteri (WebGLSampler sampler, int pname, int param)/*-{
		throw "UnsupportedOperation";
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, ArrayBufferView pixels)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Texture objects */
	public final native void texStorage2D (int target, int levels, int internalformat, int width, int height)/*-{
		this.texStorage2D(target, levels, internalformat, width, height)
	}-*/;

	public final native void texStorage3D (int target, int levels, int internalformat, int width, int height, int depth)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void texSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int type, ArrayBufferView pixels)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void texSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int format, int type,
		TexImageSource source)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void transformFeedbackVaryings (WebGLProgram program, JsArrayString varyings, int bufferMode)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Uniforms and attributes */
	public final native void uniform1ui (WebGLUniformLocation location, int v0)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform1uiv (WebGLUniformLocation location, UniformUIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform2ui (WebGLUniformLocation location, int v0, int v1)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform2uiv (WebGLUniformLocation location, UniformUIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform3ui (WebGLUniformLocation location, int v0, int v1, int v2)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform3uiv (WebGLUniformLocation location, UniformUIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform4ui (WebGLUniformLocation location, int v0, int v1, int v2, int v3)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniform4uiv (WebGLUniformLocation location, UniformUIVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformBlockBinding (WebGLProgram program, int uniformBlockIndex, int uniformBlockBinding)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix2x3fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix2x4fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix3x2fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix3x4fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix4x2fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void uniformMatrix4x3fv (WebGLUniformLocation location, boolean transpose, UniformMatrixFVSource value)/*-{
		throw "UnsupportedOperation";

	}-*/;

	/* Writing to the drawing buffer */
	public final native void vertexAttribDivisor (int index, int divisor)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void vertexAttribI4i (int index, int x, int y, int z, int w)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void vertexAttribI4iv (int index, VertexAttribIVSource values)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void vertexAttribI4ui (int index, int x, int y, int z, int w)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void vertexAttribI4uiv (int index, VertexAttribUIVSource values)/*-{
		throw "UnsupportedOperation";

	}-*/;

	public final native void vertexAttribIPointer (int index, int size, int type, int stride, int offset)/*-{
		throw "UnsupportedOperation";

	}-*/;

	// FIXME
	public final native void waitSync (WebGLSync sync, int flags, /* GLint64 */int timeout)/*-{
		throw "UnsupportedOperation";

	}-*/;
};
