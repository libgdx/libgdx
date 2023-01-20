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

package com.google.gwt.webgl.client;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayBoolean;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Uint32Array;

/** @author Simon Gerst
 * @author JamesTKhan */
public class WebGL2RenderingContext extends WebGLRenderingContext {

	/** Returns a WebGL2 context for the given canvas element. Returns null if no 3d context is available. */
	public static WebGL2RenderingContext getContext (CanvasElement canvas) {
		return getContext(canvas, WebGLContextAttributes.create());
	}

	/** Returns a WebGL2 context for the given canvas element. Returns null if no 3d context is available. */
	public static native WebGL2RenderingContext getContext (CanvasElement canvas, WebGLContextAttributes attributes) /*-{
		try {
			var ctx = canvas.getContext("webgl2", attributes);
			if (ctx != null) {
				console.log('WebGL2 Enabled');
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
		return null;
	}-*/;

	protected WebGL2RenderingContext () {
	}

	/** Converts a Java primitive string array to JsArrayString
	 *
	 * @param input primitive string array
	 * @return converted JsArrayString */
	private static JsArrayString toJsArray (String[] input) {
		JsArrayString jsArrayString = JsArrayString.createArray().cast();
		for (String s : input) {
			jsArrayString.push(s);
		}
		return jsArrayString;
	}

	public final native void beginQuery (int target, WebGLQuery query)/*-{
		this.beginQuery(target, query);
	}-*/;

	public final native void beginTransformFeedback (int primitiveMode)/*-{
		this.beginTransformFeedback(primitiveMode);
	}-*/;

	public final native void bindBufferBase (int target, int index, WebGLBuffer buffer)/*-{
		this.bindBufferBase(target, index, buffer);
	}-*/;

	public final native void bindBufferRange (int target, int index, WebGLBuffer buffer, int offset, int size)/*-{
		this.bindBufferRange(target, index, buffer, offset, size);
	}-*/;

	public final native void bindSampler (int unit, WebGLSampler sampler)/*-{
		this.bindSampler(unit, sampler);
	}-*/;

	public final native void bindTransformFeedback (int target, WebGLTransformFeedback id)/*-{
		this.bindTransformFeedback(target, id);

	}-*/;

	public final native void bindVertexArray (WebGLVertexArrayObject array)/*-{
		this.bindVertexArray(array);
	}-*/;

	public final native void blitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1,
		int dstY1, int mask, int filter)/*-{
		this.blitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1,
				dstY1, mask, filter);
	}-*/;

	public final native void clearBufferfi (int buffer, int drawbuffer, float depth, int stencil)/*-{
		this.clearBufferfi(buffer, drawbuffer, depth, stencil);
	}-*/;

	public final void clearBufferfv (int buffer, int drawbuffer, Float32Array value) {
		this.clearBufferfv(buffer, drawbuffer, (JavaScriptObject)value);
	}

	public final native void clearBufferfv (int buffer, int drawbuffer, JavaScriptObject value)/*-{
		this.clearBufferfv(buffer, drawbuffer, value);
	}-*/;

	public final void clearBufferiv (int buffer, int drawbuffer, Int32Array value) {
		this.clearBufferiv(buffer, drawbuffer, (JavaScriptObject)value);
	}

	public final native void clearBufferiv (int buffer, int drawbuffer, JavaScriptObject value)/*-{
		this.clearBufferiv(buffer, drawbuffer, value);
	}-*/;

	public final void clearBufferuiv (int buffer, int drawbuffer, Int32Array value) {
		this.clearBufferuiv(buffer, drawbuffer, (JavaScriptObject)value);
	}

	public final native void clearBufferuiv (int buffer, int drawbuffer, JavaScriptObject value)/*-{
		this.clearBufferuiv(buffer, drawbuffer, value);
	}-*/;

// Commented out in GL30 interface
// public final native int clientWaitSync (WebGLSync sync, int flags, /* GLint64 */int timeout)/*-{
// throw "UnsupportedOperation";
// }-*/;

// Commented out in GL30 interface
// public final native void compressedTexImage3D (int target, int level, int internalformat, int width, int height, int depth,
// int border, ArrayBufferView data)/*-{
// throw "UnsupportedOperation";
// }-*/;

// Commented out in GL30 interface
// public final native void compressedTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width,
// int height, int depth, int format, ArrayBufferView data)/*-{
// throw "UnsupportedOperation";
//
// }-*/;

	public final native void copyBufferSubData (int readTarget, int writeTarget, int readOffset, int writeOffset, int size)/*-{
		this.copyBufferSubData(readTarget, writeTarget, readOffset,
				writeOffset, size);
	}-*/;

	public final native void copyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
		int width, int height)/*-{
    	this.copyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
	}-*/;

	public final native WebGLQuery createQuery ()/*-{
		return this.createQuery();
	}-*/;

	public final native WebGLSampler createSampler ()/*-{
		return this.createSampler();
	}-*/;

	public final native WebGLTransformFeedback createTransformFeedback ()/*-{
		return this.createTransformFeedback();
	}-*/;

	public final native WebGLVertexArrayObject createVertexArray ()/*-{
		return this.createVertexArray();
	}-*/;

	public final native void deleteQuery (WebGLQuery query)/*-{
		this.deleteQuery(query);
	}-*/;

	public final native void deleteSampler (WebGLSampler sampler)/*-{
		this.deleteSampler(sampler);
	}-*/;

// Commented out in GL30 interface
// public final native void deleteSync (WebGLSync sync)/*-{
// this.deleteSync(sync);
// }-*/;

	public final native void deleteTransformFeedback (WebGLTransformFeedback transformFeedback)/*-{
		this.deleteTransformFeedback(transformFeedback);
	}-*/;

	public final native void deleteVertexArray (WebGLVertexArrayObject vertexArray)/*-{
		this.deleteVertexArray(vertexArray);
	}-*/;

	public final native void drawArraysInstanced (int mode, int first, int count, int instanceCount)/*-{
		this.drawArraysInstanced(mode, first, count, instanceCount);
	}-*/;

	public final native void drawBuffers (Int32Array buffers)/*-{
		this.drawBuffers(buffers);
	}-*/;

	public final native void drawElementsInstanced (int mode, int count, int type, int offset, int instanceCount)/*-{
		this.drawElementsInstanced(mode, count, type, offset, instanceCount);
	}-*/;

	public final native void drawRangeElements (int mode, int start, int end, int count, int type, int offset)/*-{
		this.drawRangeElements(mode, start, end, count, type, offset);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, int offset)/*-{
		this.texImage2D(target, level, internalformat, width, height, border, format, type, offset);
	}-*/;

	public final native void endQuery (int target)/*-{
		this.endQuery(target);
	}-*/;

	public final native void endTransformFeedback ()/*-{
		this.endTransformFeedback();
	}-*/;

// Commented out in GL30 interface
// public final native WebGLSync fenceSync (int condition, int flags)/*-{
// throw "UnsupportedOperation";
//
// }-*/;

	public final native void framebufferTextureLayer (int target, int attachment, WebGLTexture texture, int level, int layer)/*-{
		this.framebufferTextureLayer(target, attachment, texture, level, layer);
	}-*/;

	public final native String getActiveUniformBlockName (WebGLProgram program, int uniformBlockIndex)/*-{
        return this.getActiveUniformBlockName(program, uniformBlockIndex);
	}-*/;

	public final native int getActiveUniformBlockParameteri (WebGLProgram program, int uniformBlockIndex, int pname)/*-{
		return this.getActiveUniformBlockParameter(program, uniformBlockIndex, pname);
	}-*/;

	public final native <T extends ArrayBufferView> T getActiveUniformBlockParameterv (WebGLProgram program, int uniformBlockIndex,
		int pname)/*-{
		return this.getActiveUniformBlockParameter(program, uniformBlockIndex, pname);
	}-*/;

	public final native boolean getActiveUniformBlockParameterb (WebGLProgram program, int uniformBlockIndex, int pname)/*-{
		return this.getActiveUniformBlockParameter(program, uniformBlockIndex, pname);
	}-*/;

	public final native JsArrayInteger getActiveUniformsi (WebGLProgram program, Int32Array uniformIndices, int pname)/*-{
    	return this.getActiveUniforms(program, uniformIndices, pname);
	}-*/;

	public final native JsArrayBoolean getActiveUniformsb (WebGLProgram program, Int32Array uniformIndices, int pname)/*-{
		return this.getActiveUniforms(program, uniformIndices, pname);
	}-*/;

	public final native int getFragDataLocation (WebGLProgram program, String name)/*-{
        return this.getFragDataLocation(program, name);
	}-*/;

	// Returning an int but GL type is GLint64 and GL30 interface uses LongBuffer. JS does not support long
	// so we return an int, not sure how else to preserve the long values at this time.
	public final native int getParameteri64 (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	public final native WebGLQuery getQuery (int target, int pname)/*-{
		return this.getQuery(target, pname);
	}-*/;

	public final native boolean getQueryParameterb (WebGLQuery query, int pname)/*-{
		return this.getQueryParameter(query, pname);
	}-*/;

	public final native int getQueryParameteri (WebGLQuery query, int pname)/*-{
		return this.getQueryParameter(query, pname);
	}-*/;

	public final native float getSamplerParameterf (WebGLSampler sampler, int pname)/*-{
		return this.getSamplerParameter(sampler, pname);
	}-*/;

	public final native int getSamplerParameteri (WebGLSampler sampler, int pname)/*-{
		return this.getSamplerParameter(sampler, pname);
	}-*/;

// Commented out in GL30 interface
// public final native WebGLActiveInfo getTransformFeedbackVarying (WebGLProgram program, int index)/*-{
// throw "UnsupportedOperation";
// }-*/;

	public final native int getUniformBlockIndex (WebGLProgram program, String uniformBlockName)/*-{
        return this.getUniformBlockIndex(program, uniformBlockName);
	}-*/;

	public final JsArrayInteger getUniformIndices (WebGLProgram program, String[] uniformNames) {
		return this.getUniformIndices(program, toJsArray(uniformNames));
	}

	public final native JsArrayInteger getUniformIndices (WebGLProgram program, JsArrayString uniformNames)/*-{
		return this.getUniformIndices(program, uniformNames);
	}-*/;

	public final native void invalidateFramebuffer (int target, Int32Array attachments)/*-{
		this.invalidateFramebuffer(target, attachments);
	}-*/;

	public final native void invalidateSubFramebuffer (int target, Int32Array attachments, int x, int y, int width, int height)/*-{
		this.invalidateSubFramebuffer(target, attachments, x, y, width, height);
	}-*/;

	public final native boolean isQuery (WebGLQuery query)/*-{
        return this.isQuery(query);
	}-*/;

	public final native boolean isSampler (WebGLSampler sampler)/*-{
		return this.isSampler(sampler);
	}-*/;

// Commented out in GL30 interface
// public final native boolean isSync (WebGLSync sync)/*-{
// return this.isSync(sync);
// }-*/;

	public final native boolean isTransformFeedback (WebGLTransformFeedback transformFeedback)/*-{
		return this.isTransformFeedback(transformFeedback);
	}-*/;

	public final native boolean isVertexArray (WebGLVertexArrayObject vertexArray)/*-{
		return this.isVertexArray(vertexArray);
	}-*/;

	public final native void pauseTransformFeedback ()/*-{
        this.pauseTransformFeedback();
	}-*/;

	public final native void readBuffer (int src)/*-{
		this.readBuffer(src);
	}-*/;

	public final native void renderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height)/*-{
		this.renderbufferStorageMultisample(target, samples, internalformat, width, height);
	}-*/;

	public final native void resumeTransformFeedback ()/*-{
		this.resumeTransformFeedback();
	}-*/;

	public final native void samplerParameterf (WebGLSampler sampler, int pname, float param)/*-{
		this.samplerParameterf(sampler, pname, param);
	}-*/;

	public final native void samplerParameteri (WebGLSampler sampler, int pname, int param)/*-{
		this.samplerParameteri(sampler, pname, param);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, int offset)/*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border,
				format, type, offset);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, ArrayBufferView pixels)/*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border,
				format, type, pixels);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, ImageData pixels) /*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, ImageElement image) /*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border, format, type, image);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, CanvasElement canvas) /*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border, format, type, canvas);
	}-*/;

	public final native void texImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int format, int type, VideoElement video) /*-{
		this.texImage3D(target, level, internalformat, width, height, depth, border, format, type, video);
	}-*/;

// Commented out in GL30 interface
// public final native void texStorage2D (int target, int levels, int internalformat, int width, int height)/*-{
// this.texStorage2D(target, levels, internalformat, width, height)
// }-*/;

// Commented out in GL30 interface
// public final native void texStorage3D (int target, int levels, int internalformat, int width, int height, int depth)/*-{
// throw "UnsupportedOperation";
// }-*/;

	public final native void texSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int type, ArrayBufferView pixels)/*-{
    	this.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
	}-*/;

	public final native void texSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int type, CanvasElement canvas)/*-{
		this.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, canvas);
	}-*/;

	public final native void texSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int type, int offset)/*-{
		this.texSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, offset);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, int offset)/*-{
		this.texSubImage2D(target, level, xoffset, yoffset, width, height, format, type, offset);
	}-*/;

	public final void transformFeedbackVaryings (WebGLProgram program, String[] varyings, int bufferMode) {
		this.transformFeedbackVaryings(program, toJsArray(varyings), bufferMode);
	}

	public final native void transformFeedbackVaryings (WebGLProgram program, JsArrayString varyings, int bufferMode)/*-{
		this.transformFeedbackVaryings(program, varyings, bufferMode);
	}-*/;

// Commented out in GL30 interface
// public final native void uniform1ui (WebGLUniformLocation location, int v0)/*-{
// this.uniform1ui(location, v0);
// }-*/;

	public final void uniform1uiv (WebGLUniformLocation location, Uint32Array value, int srcOffset, int srcLength) {
		this.uniform1uiv(location, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniform1uiv (WebGLUniformLocation location, JavaScriptObject value, int srcOffset, int srcLength)/*-{
    	this.uniform1uiv(location, value, srcOffset, srcLength);
	}-*/;

	public final void uniform3uiv (WebGLUniformLocation location, Uint32Array value, int srcOffset, int srcLength) {
		this.uniform3uiv(location, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniform3uiv (WebGLUniformLocation location, JavaScriptObject value, int srcOffset, int srcLength)/*-{
		this.uniform3uiv(location, value, srcOffset, srcLength)
	}-*/;

	public final void uniform4uiv (WebGLUniformLocation location, Uint32Array value, int srcOffset, int srcLength) {
		this.uniform4uiv(location, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniform4uiv (WebGLUniformLocation location, JavaScriptObject value, int srcOffset, int srcLength)/*-{
		this.uniform4uiv(location, value, srcOffset, srcLength)
	}-*/;

	public final native void uniformBlockBinding (WebGLProgram program, int uniformBlockIndex, int uniformBlockBinding)/*-{
		this.uniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}-*/;

	public final void uniformMatrix2x3fv (WebGLUniformLocation location, boolean transpose, Float32Array value) {
		this.uniformMatrix2x3fv(location, transpose, (JavaScriptObject)value);
	}

	public final native void uniformMatrix2x3fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value)/*-{
		this.uniformMatrix2x3fv(location, transpose, value);
	}-*/;

	public final void uniformMatrix2x4fv (WebGLUniformLocation location, boolean transpose, Float32Array value, int srcOffset,
		int srcLength) {
		this.uniformMatrix2x4fv(location, transpose, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniformMatrix2x4fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value,
		int srcOffset, int srcLength)/*-{
		this.uniformMatrix2x4fv(location, transpose, value, srcOffset, srcLength);
	}-*/;

	public final void uniformMatrix3x2fv (WebGLUniformLocation location, boolean transpose, Float32Array value, int srcOffset,
		int srcLength) {
		this.uniformMatrix3x2fv(location, transpose, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniformMatrix3x2fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value,
		int srcOffset, int srcLength)/*-{
		this.uniformMatrix3x2fv(location, transpose, value, srcOffset, srcLength);
	}-*/;

	public final void uniformMatrix3x4fv (WebGLUniformLocation location, boolean transpose, Float32Array value, int srcOffset,
		int srcLength) {
		this.uniformMatrix3x4fv(location, transpose, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniformMatrix3x4fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value,
		int srcOffset, int srcLength)/*-{
		this.uniformMatrix3x4fv(location, transpose, value, srcOffset, srcLength);
	}-*/;

	public final void uniformMatrix4x2fv (WebGLUniformLocation location, boolean transpose, Float32Array value, int srcOffset,
		int srcLength) {
		this.uniformMatrix4x2fv(location, transpose, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniformMatrix4x2fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value,
		int srcOffset, int srcLength)/*-{
		this.uniformMatrix4x2fv(location, transpose, value, srcOffset, srcLength);
	}-*/;

	public final void uniformMatrix4x3fv (WebGLUniformLocation location, boolean transpose, Float32Array value, int srcOffset,
		int srcLength) {
		this.uniformMatrix4x3fv(location, transpose, (JavaScriptObject)value, srcOffset, srcLength);
	}

	public final native void uniformMatrix4x3fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value,
		int srcOffset, int srcLength)/*-{
		this.uniformMatrix4x3fv(location, transpose, value, srcOffset, srcLength);
	}-*/;

	public final native void vertexAttribDivisor (int index, int divisor)/*-{
		this.vertexAttribDivisor(index, divisor);
	}-*/;

	public final native void vertexAttribI4i (int index, int x, int y, int z, int w)/*-{
		this.vertexAttribI4i(index, x, y, z, w);
	}-*/;

// Commented out in GL30 interface
// public final native void vertexAttribI4iv (int index, VertexAttribIVSource values)/*-{
// throw "UnsupportedOperation";
// }-*/;

	public final native void vertexAttribI4ui (int index, int x, int y, int z, int w)/*-{
		this.vertexAttribI4ui(index, x, y, z, w);
	}-*/;

// Commented out in GL30 interface
// public final native void vertexAttribI4uiv (int index, VertexAttribUIVSource values)/*-{
// throw "UnsupportedOperation";
// }-*/;

	public final native void vertexAttribIPointer (int index, int size, int type, int stride, int offset)/*-{
		this.vertexAttribIPointer(index, size, type, stride, offset);
	}-*/;

// Commented out in GL30 interface
// public final native void waitSync (WebGLSync sync, int flags, /* GLint64 */int timeout)/*-{
// throw "UnsupportedOperation";
// }-*/;
};
