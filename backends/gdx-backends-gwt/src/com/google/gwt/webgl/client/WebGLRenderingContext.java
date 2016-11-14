/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.webgl.client;

import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.VideoElement;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.TypedArrays;

/** TODO: Lots more documentation needed here.
 * 
 * Taken from the WebGL Draft Spec as of Aug 30, 2010:
 * https://cvs.khronos.org/svn/repos/registry/trunk/public/webgl/doc/spec/WebGL-spec.html TODO: Update this to the Feb 11 version. */
public class WebGLRenderingContext extends JavaScriptObject {

	/* ClearBufferMask */
	public static final int DEPTH_BUFFER_BIT = 0x00000100;
	public static final int STENCIL_BUFFER_BIT = 0x00000400;
	public static final int COLOR_BUFFER_BIT = 0x00004000;

	/* BeginMode */
	public static final int POINTS = 0x0000;
	public static final int LINES = 0x0001;
	public static final int LINE_LOOP = 0x0002;
	public static final int LINE_STRIP = 0x0003;
	public static final int TRIANGLES = 0x0004;
	public static final int TRIANGLE_STRIP = 0x0005;
	public static final int TRIANGLE_FAN = 0x0006;

	/* AlphaFunction (not supported in ES20) */
	/* NEVER */
	/* LESS */
	/* EQUAL */
	/* LEQUAL */
	/* GREATER */
	/* NOTEQUAL */
	/* GEQUAL */
	/* ALWAYS */

	/* BlendingFactorDest */
	public static final int ZERO = 0;
	public static final int ONE = 1;
	public static final int SRC_COLOR = 0x0300;
	public static final int ONE_MINUS_SRC_COLOR = 0x0301;
	public static final int SRC_ALPHA = 0x0302;
	public static final int ONE_MINUS_SRC_ALPHA = 0x0303;
	public static final int DST_ALPHA = 0x0304;
	public static final int ONE_MINUS_DST_ALPHA = 0x0305;

	/* BlendingFactorSrc */
	/* ZERO */
	/* ONE */
	public static final int DST_COLOR = 0x0306;
	public static final int ONE_MINUS_DST_COLOR = 0x0307;
	public static final int SRC_ALPHA_SATURATE = 0x0308;
	/* SRC_ALPHA */
	/* ONE_MINUS_SRC_ALPHA */
	/* DST_ALPHA */
	/* ONE_MINUS_DST_ALPHA */

	/* BlendEquationSeparate */
	public static final int FUNC_ADD = 0x8006;
	public static final int BLEND_EQUATION = 0x8009;
	public static final int BLEND_EQUATION_RGB = 0x8009; /* same as BLEND_EQUATION */
	public static final int BLEND_EQUATION_ALPHA = 0x883D;

	/* BlendSubtract */
	public static final int FUNC_SUBTRACT = 0x800A;
	public static final int FUNC_REVERSE_SUBTRACT = 0x800B;

	/* Separate Blend Functions */
	public static final int BLEND_DST_RGB = 0x80C8;
	public static final int BLEND_SRC_RGB = 0x80C9;
	public static final int BLEND_DST_ALPHA = 0x80CA;
	public static final int BLEND_SRC_ALPHA = 0x80CB;
	public static final int CONSTANT_COLOR = 0x8001;
	public static final int ONE_MINUS_CONSTANT_COLOR = 0x8002;
	public static final int CONSTANT_ALPHA = 0x8003;
	public static final int ONE_MINUS_CONSTANT_ALPHA = 0x8004;
	public static final int BLEND_COLOR = 0x8005;

	/* Buffer Objects */
	public static final int ARRAY_BUFFER = 0x8892;
	public static final int ELEMENT_ARRAY_BUFFER = 0x8893;
	public static final int ARRAY_BUFFER_BINDING = 0x8894;
	public static final int ELEMENT_ARRAY_BUFFER_BINDING = 0x8895;

	public static final int STREAM_DRAW = 0x88E0;
	public static final int STATIC_DRAW = 0x88E4;
	public static final int DYNAMIC_DRAW = 0x88E8;

	public static final int BUFFER_SIZE = 0x8764;
	public static final int BUFFER_USAGE = 0x8765;

	public static final int CURRENT_VERTEX_ATTRIB = 0x8626;

	/* CullFaceMode */
	public static final int FRONT = 0x0404;
	public static final int BACK = 0x0405;
	public static final int FRONT_AND_BACK = 0x0408;

	/* DepthFunction */
	/* NEVER */
	/* LESS */
	/* EQUAL */
	/* LEQUAL */
	/* GREATER */
	/* NOTEQUAL */
	/* GEQUAL */
	/* ALWAYS */

	/* EnableCap */
	public static final int TEXTURE_2D = 0x0DE1;
	public static final int CULL_FACE = 0x0B44;
	public static final int BLEND = 0x0BE2;
	public static final int DITHER = 0x0BD0;
	public static final int STENCIL_TEST = 0x0B90;
	public static final int DEPTH_TEST = 0x0B71;
	public static final int SCISSOR_TEST = 0x0C11;
	public static final int POLYGON_OFFSET_FILL = 0x8037;
	public static final int SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
	public static final int SAMPLE_COVERAGE = 0x80A0;

	/* ErrorCode */
	public static final int NO_ERROR = 0;
	public static final int INVALID_ENUM = 0x0500;
	public static final int INVALID_VALUE = 0x0501;
	public static final int INVALID_OPERATION = 0x0502;
	public static final int OUT_OF_MEMORY = 0x0505;

	/* FrontFaceDirection */
	public static final int CW = 0x0900;
	public static final int CCW = 0x0901;

	/* GetPName */
	public static final int LINE_WIDTH = 0x0B21;
	public static final int ALIASED_POINT_SIZE_RANGE = 0x846D;
	public static final int ALIASED_LINE_WIDTH_RANGE = 0x846E;
	public static final int CULL_FACE_MODE = 0x0B45;
	public static final int FRONT_FACE = 0x0B46;
	public static final int DEPTH_RANGE = 0x0B70;
	public static final int DEPTH_WRITEMASK = 0x0B72;
	public static final int DEPTH_CLEAR_VALUE = 0x0B73;
	public static final int DEPTH_FUNC = 0x0B74;
	public static final int STENCIL_CLEAR_VALUE = 0x0B91;
	public static final int STENCIL_FUNC = 0x0B92;
	public static final int STENCIL_FAIL = 0x0B94;
	public static final int STENCIL_PASS_DEPTH_FAIL = 0x0B95;
	public static final int STENCIL_PASS_DEPTH_PASS = 0x0B96;
	public static final int STENCIL_REF = 0x0B97;
	public static final int STENCIL_VALUE_MASK = 0x0B93;
	public static final int STENCIL_WRITEMASK = 0x0B98;
	public static final int STENCIL_BACK_FUNC = 0x8800;
	public static final int STENCIL_BACK_FAIL = 0x8801;
	public static final int STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802;
	public static final int STENCIL_BACK_PASS_DEPTH_PASS = 0x8803;
	public static final int STENCIL_BACK_REF = 0x8CA3;
	public static final int STENCIL_BACK_VALUE_MASK = 0x8CA4;
	public static final int STENCIL_BACK_WRITEMASK = 0x8CA5;
	public static final int VIEWPORT = 0x0BA2;
	public static final int SCISSOR_BOX = 0x0C10;
	/* SCISSOR_TEST */
	public static final int COLOR_CLEAR_VALUE = 0x0C22;
	public static final int COLOR_WRITEMASK = 0x0C23;
	public static final int UNPACK_ALIGNMENT = 0x0CF5;
	public static final int PACK_ALIGNMENT = 0x0D05;
	public static final int MAX_TEXTURE_SIZE = 0x0D33;
	public static final int MAX_VIEWPORT_DIMS = 0x0D3A;
	public static final int SUBPIXEL_BITS = 0x0D50;
	public static final int RED_BITS = 0x0D52;
	public static final int GREEN_BITS = 0x0D53;
	public static final int BLUE_BITS = 0x0D54;
	public static final int ALPHA_BITS = 0x0D55;
	public static final int DEPTH_BITS = 0x0D56;
	public static final int STENCIL_BITS = 0x0D57;
	public static final int POLYGON_OFFSET_UNITS = 0x2A00;
	/* POLYGON_OFFSET_FILL */
	public static final int POLYGON_OFFSET_FACTOR = 0x8038;
	public static final int TEXTURE_BINDING_2D = 0x8069;
	public static final int SAMPLE_BUFFERS = 0x80A8;
	public static final int SAMPLES = 0x80A9;
	public static final int SAMPLE_COVERAGE_VALUE = 0x80AA;
	public static final int SAMPLE_COVERAGE_INVERT = 0x80AB;

	/* GetTextureParameter */
	/* TEXTURE_MAG_FILTER */
	/* TEXTURE_MIN_FILTER */
	/* TEXTURE_WRAP_S */
	/* TEXTURE_WRAP_T */

	public static final int NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2;
	public static final int COMPRESSED_TEXTURE_FORMATS = 0x86A3;

	/* HintMode */
	public static final int DONT_CARE = 0x1100;
	public static final int FASTEST = 0x1101;
	public static final int NICEST = 0x1102;

	/* HintTarget */
	public static final int GENERATE_MIPMAP_HINT = 0x8192;

	/* DataType */
	public static final int BYTE = 0x1400;
	public static final int UNSIGNED_BYTE = 0x1401;
	public static final int SHORT = 0x1402;
	public static final int UNSIGNED_SHORT = 0x1403;
	public static final int INT = 0x1404;
	public static final int UNSIGNED_INT = 0x1405;
	public static final int FLOAT = 0x1406;

	/* PixelFormat */
	public static final int DEPTH_COMPONENT = 0x1902;
	public static final int ALPHA = 0x1906;
	public static final int RGB = 0x1907;
	public static final int RGBA = 0x1908;
	public static final int LUMINANCE = 0x1909;
	public static final int LUMINANCE_ALPHA = 0x190A;

	/* PixelType */
	/* UNSIGNED_BYTE */
	public static final int UNSIGNED_SHORT_4_4_4_4 = 0x8033;
	public static final int UNSIGNED_SHORT_5_5_5_1 = 0x8034;
	public static final int UNSIGNED_SHORT_5_6_5 = 0x8363;

	/* Shaders */
	public static final int FRAGMENT_SHADER = 0x8B30;
	public static final int VERTEX_SHADER = 0x8B31;
	public static final int MAX_VERTEX_ATTRIBS = 0x8869;
	public static final int MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB;
	public static final int MAX_VARYING_VECTORS = 0x8DFC;
	public static final int MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D;
	public static final int MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C;
	public static final int MAX_TEXTURE_IMAGE_UNITS = 0x8872;
	public static final int MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD;
	public static final int SHADER_TYPE = 0x8B4F;
	public static final int DELETE_STATUS = 0x8B80;
	public static final int LINK_STATUS = 0x8B82;
	public static final int VALIDATE_STATUS = 0x8B83;
	public static final int ATTACHED_SHADERS = 0x8B85;
	public static final int ACTIVE_UNIFORMS = 0x8B86;
	public static final int ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87;
	public static final int ACTIVE_ATTRIBUTES = 0x8B89;
	public static final int ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A;
	public static final int SHADING_LANGUAGE_VERSION = 0x8B8C;
	public static final int CURRENT_PROGRAM = 0x8B8D;

	/* StencilFunction */
	public static final int NEVER = 0x0200;
	public static final int LESS = 0x0201;
	public static final int EQUAL = 0x0202;
	public static final int LEQUAL = 0x0203;
	public static final int GREATER = 0x0204;
	public static final int NOTEQUAL = 0x0205;
	public static final int GEQUAL = 0x0206;
	public static final int ALWAYS = 0x0207;

	/* StencilOp */
	/* ZERO */
	public static final int KEEP = 0x1E00;
	public static final int REPLACE = 0x1E01;
	public static final int INCR = 0x1E02;
	public static final int DECR = 0x1E03;
	public static final int INVERT = 0x150A;
	public static final int INCR_WRAP = 0x8507;
	public static final int DECR_WRAP = 0x8508;

	/* StringName */
	public static final int VENDOR = 0x1F00;
	public static final int RENDERER = 0x1F01;
	public static final int VERSION = 0x1F02;

	/* TextureMagFilter */
	public static final int NEAREST = 0x2600;
	public static final int LINEAR = 0x2601;

	/* TextureMinFilter */
	/* NEAREST */
	/* LINEAR */
	public static final int NEAREST_MIPMAP_NEAREST = 0x2700;
	public static final int LINEAR_MIPMAP_NEAREST = 0x2701;
	public static final int NEAREST_MIPMAP_LINEAR = 0x2702;
	public static final int LINEAR_MIPMAP_LINEAR = 0x2703;

	/* TextureParameterName */
	public static final int TEXTURE_MAG_FILTER = 0x2800;
	public static final int TEXTURE_MIN_FILTER = 0x2801;
	public static final int TEXTURE_WRAP_S = 0x2802;
	public static final int TEXTURE_WRAP_T = 0x2803;

	/* TextureTarget */
	/* TEXTURE_2D */
	public static final int TEXTURE = 0x1702;

	public static final int TEXTURE_CUBE_MAP = 0x8513;
	public static final int TEXTURE_BINDING_CUBE_MAP = 0x8514;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518;
	public static final int TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519;
	public static final int TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
	public static final int MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C;

	/* TextureUnit */
	public static final int TEXTURE0 = 0x84C0;
	public static final int TEXTURE1 = 0x84C1;
	public static final int TEXTURE2 = 0x84C2;
	public static final int TEXTURE3 = 0x84C3;
	public static final int TEXTURE4 = 0x84C4;
	public static final int TEXTURE5 = 0x84C5;
	public static final int TEXTURE6 = 0x84C6;
	public static final int TEXTURE7 = 0x84C7;
	public static final int TEXTURE8 = 0x84C8;
	public static final int TEXTURE9 = 0x84C9;
	public static final int TEXTURE10 = 0x84CA;
	public static final int TEXTURE11 = 0x84CB;
	public static final int TEXTURE12 = 0x84CC;
	public static final int TEXTURE13 = 0x84CD;
	public static final int TEXTURE14 = 0x84CE;
	public static final int TEXTURE15 = 0x84CF;
	public static final int TEXTURE16 = 0x84D0;
	public static final int TEXTURE17 = 0x84D1;
	public static final int TEXTURE18 = 0x84D2;
	public static final int TEXTURE19 = 0x84D3;
	public static final int TEXTURE20 = 0x84D4;
	public static final int TEXTURE21 = 0x84D5;
	public static final int TEXTURE22 = 0x84D6;
	public static final int TEXTURE23 = 0x84D7;
	public static final int TEXTURE24 = 0x84D8;
	public static final int TEXTURE25 = 0x84D9;
	public static final int TEXTURE26 = 0x84DA;
	public static final int TEXTURE27 = 0x84DB;
	public static final int TEXTURE28 = 0x84DC;
	public static final int TEXTURE29 = 0x84DD;
	public static final int TEXTURE30 = 0x84DE;
	public static final int TEXTURE31 = 0x84DF;
	public static final int ACTIVE_TEXTURE = 0x84E0;

	/* TextureWrapMode */
	public static final int REPEAT = 0x2901;
	public static final int CLAMP_TO_EDGE = 0x812F;
	public static final int MIRRORED_REPEAT = 0x8370;

	/* Uniform Types */
	public static final int FLOAT_VEC2 = 0x8B50;
	public static final int FLOAT_VEC3 = 0x8B51;
	public static final int FLOAT_VEC4 = 0x8B52;
	public static final int INT_VEC2 = 0x8B53;
	public static final int INT_VEC3 = 0x8B54;
	public static final int INT_VEC4 = 0x8B55;
	public static final int BOOL = 0x8B56;
	public static final int BOOL_VEC2 = 0x8B57;
	public static final int BOOL_VEC3 = 0x8B58;
	public static final int BOOL_VEC4 = 0x8B59;
	public static final int FLOAT_MAT2 = 0x8B5A;
	public static final int FLOAT_MAT3 = 0x8B5B;
	public static final int FLOAT_MAT4 = 0x8B5C;
	public static final int SAMPLER_2D = 0x8B5E;
	public static final int SAMPLER_CUBE = 0x8B60;

	/* Vertex Arrays */
	public static final int VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622;
	public static final int VERTEX_ATTRIB_ARRAY_SIZE = 0x8623;
	public static final int VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624;
	public static final int VERTEX_ATTRIB_ARRAY_TYPE = 0x8625;
	public static final int VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A;
	public static final int VERTEX_ATTRIB_ARRAY_POINTER = 0x8645;
	public static final int VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F;

	/* Read Format */
	public static final int IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A;
	public static final int IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B;

	/* Shader Source */
	public static final int COMPILE_STATUS = 0x8B81;
	public static final int INFO_LOG_LENGTH = 0x8B84;
	public static final int SHADER_SOURCE_LENGTH = 0x8B88;

	/* Shader Precision-Specified Types */
	public static final int LOW_FLOAT = 0x8DF0;
	public static final int MEDIUM_FLOAT = 0x8DF1;
	public static final int HIGH_FLOAT = 0x8DF2;
	public static final int LOW_INT = 0x8DF3;
	public static final int MEDIUM_INT = 0x8DF4;
	public static final int HIGH_INT = 0x8DF5;

	/* Framebuffer Object. */
	public static final int FRAMEBUFFER = 0x8D40;
	public static final int RENDERBUFFER = 0x8D41;

	public static final int RGBA4 = 0x8056;
	public static final int RGB5_A1 = 0x8057;
	public static final int RGB565 = 0x8D62;
	public static final int DEPTH_COMPONENT16 = 0x81A5;
	public static final int STENCIL_INDEX = 0x1901;
	public static final int STENCIL_INDEX8 = 0x8D48;
	public static final int DEPTH_STENCIL = 0x84F9;

	public static final int RENDERBUFFER_WIDTH = 0x8D42;
	public static final int RENDERBUFFER_HEIGHT = 0x8D43;
	public static final int RENDERBUFFER_INTERNAL_FORMAT = 0x8D44;
	public static final int RENDERBUFFER_RED_SIZE = 0x8D50;
	public static final int RENDERBUFFER_GREEN_SIZE = 0x8D51;
	public static final int RENDERBUFFER_BLUE_SIZE = 0x8D52;
	public static final int RENDERBUFFER_ALPHA_SIZE = 0x8D53;
	public static final int RENDERBUFFER_DEPTH_SIZE = 0x8D54;
	public static final int RENDERBUFFER_STENCIL_SIZE = 0x8D55;

	public static final int FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0;
	public static final int FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1;
	public static final int FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2;
	public static final int FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3;

	public static final int COLOR_ATTACHMENT0 = 0x8CE0;
	public static final int DEPTH_ATTACHMENT = 0x8D00;
	public static final int STENCIL_ATTACHMENT = 0x8D20;
	public static final int DEPTH_STENCIL_ATTACHMENT = 0x821A;

	public static final int NONE = 0;

	public static final int FRAMEBUFFER_COMPLETE = 0x8CD5;
	public static final int FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6;
	public static final int FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7;
	public static final int FRAMEBUFFER_INCOMPLETE_DIMENSIONS = 0x8CD9;
	public static final int FRAMEBUFFER_UNSUPPORTED = 0x8CDD;

	public static final int FRAMEBUFFER_BINDING = 0x8CA6;
	public static final int RENDERBUFFER_BINDING = 0x8CA7;
	public static final int MAX_RENDERBUFFER_SIZE = 0x84E8;

	public static final int INVALID_FRAMEBUFFER_OPERATION = 0x0506;

	/* WebGL-specific enums */
	public static final int UNPACK_FLIP_Y_WEBGL = 0x9240;
	public static final int UNPACK_PREMULTIPLY_ALPHA_WEBGL = 0x9241;
	public static final int CONTEXT_LOST_WEBGL = 0x9242;

	/** Returns a WebGL context for the given canvas element. Returns null if no 3d context is available. */
	public static WebGLRenderingContext getContext (CanvasElement canvas) {
		return getContext(canvas, WebGLContextAttributes.create());
	}

	/** Returns a WebGL context for the given canvas element. Returns null if no 3d context is available. */
	public static native WebGLRenderingContext getContext (CanvasElement canvas, WebGLContextAttributes attributes) /*-{
		var names = [ "experimental-webgl", "webgl", "moz-webgl",
				"webkit-webgl", "webkit-3d" ];

		for ( var i = 0; i < names.length; i++) {
			try {
				var ctx = canvas.getContext(names[i], attributes);

				if (ctx != null) {
					// Hook for the semi-standard WebGLDebugUtils script.
					if ($wnd.WebGLDebugUtils) {
						if ($wnd.console && $wnd.console.log)
							console.log('WebGL debugging enabled');
						return $wnd.WebGLDebugUtils.makeDebugContext(ctx);
					}
					return ctx;
				}
			} catch (e) {
			}
		}

		return null;
	}-*/;

	protected WebGLRenderingContext () {
	}

	public final native CanvasElement getCanvas () /*-{
		return this.canvas;
	}-*/;

	public final native WebGLContextAttributes getContextAttributes () /*-{
		return this.getContextAttributes();
	}-*/;

	public final native boolean isContextLost () /*-{
		return this.isContextLost();
	}-*/;

	public final native JsArrayString getSupportedExtensions () /*-{
		return this.getSupportedExtensions();
	}-*/;

	public final native JavaScriptObject getExtension (String name) /*-{
		return this.getExtension(name);
	}-*/;

	public final native void activeTexture (int texture) /*-{
		this.activeTexture(texture);
	}-*/;

	public final native void attachShader (WebGLProgram program, WebGLShader shader) /*-{
		this.attachShader(program, shader);
	}-*/;

	public final native void bindAttribLocation (WebGLProgram program, int index, String name) /*-{
		this.bindAttribLocation(program, index, name);
	}-*/;

	public final native void bindBuffer (int target, WebGLBuffer buffer) /*-{
		this.bindBuffer(target, buffer);
	}-*/;

	public final native void bindFramebuffer (int target, WebGLFramebuffer framebuffer) /*-{
		this.bindFramebuffer(target, framebuffer);
	}-*/;

	public final native void bindRenderbuffer (int target, WebGLRenderbuffer renderbuffer) /*-{
		this.bindRenderbuffer(target, renderbuffer);
	}-*/;

	public final native void bindTexture (int target, WebGLTexture texture) /*-{
		this.bindTexture(target, texture);
	}-*/;

	public final native void blendColor (float red, float green, float blue, float alpha) /*-{
		this.blendColor(red, green, blue, alpha);
	}-*/;

	public final native void blendEquation (int mode) /*-{
		this.blendEquation(mode);
	}-*/;

	public final native void blendEquationSeparate (int modeRGB, int modeAlpha) /*-{
		this.blendEquationSeparate(modeRGB, modeAlpha);
	}-*/;

	public final native void blendFunc (int sfactor, int dfactor) /*-{
		this.blendFunc(sfactor, dfactor);
	}-*/;

	public final native void blendFuncSeparate (int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) /*-{
		this.blendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
	}-*/;

	public final native void bufferData (int target, int size, int usage) /*-{
		this.bufferData(target, size, usage);
	}-*/;

	public final native void bufferData (int target, ArrayBufferView data, int usage) /*-{
		this.bufferData(target, data, usage);
	}-*/;

	public final native void bufferData (int target, ArrayBuffer data, int usage) /*-{
		this.bufferData(target, data, usage);
	}-*/;

	public final native void bufferSubData (int target, int offset, ArrayBufferView data) /*-{
		this.bufferSubData(target, offset, data);
	}-*/;

	public final native void bufferSubData (int target, int offset, ArrayBuffer data) /*-{
		this.bufferSubData(target, offset, data);
	}-*/;

	public final native int checkFramebufferStatus (int target) /*-{
		return this.checkFramebufferStatus(target);
	}-*/;

	public final native void clear (int mask) /*-{
		this.clear(mask);
	}-*/;

	public final native void clearColor (float red, float green, float blue, float alpha) /*-{
		this.clearColor(red, green, blue, alpha);
	}-*/;

	public final native void clearDepth (float depth) /*-{
		this.clearDepth(depth);
	}-*/;

	public final native void clearStencil (int s) /*-{
		this.clearStencil(s);
	}-*/;

	public final native void colorMask (boolean red, boolean green, boolean blue, boolean alpha) /*-{
		this.colorMask(red, green, blue, alpha);
	}-*/;

	public final native void compileShader (WebGLShader shader) /*-{
		this.compileShader(shader);
	}-*/;

	public final native void copyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height,
		int border) /*-{
		this.copyTexImage2D(target, level, internalformat, x, y, width, height,
				border);
	}-*/;

	public final native void copyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width,
		int height) /*-{
		this.copyTexSubImage2D(target, level, xoffset, yoffset, x, y, width,
				height);
	}-*/;

	public final native WebGLBuffer createBuffer () /*-{
		return this.createBuffer();
	}-*/;

	public final native WebGLFramebuffer createFramebuffer () /*-{
		return this.createFramebuffer();
	}-*/;

	public final native WebGLProgram createProgram () /*-{
		return this.createProgram();
	}-*/;

	public final native WebGLRenderbuffer createRenderbuffer () /*-{
		return this.createRenderbuffer();
	}-*/;

	public final native WebGLShader createShader (int type) /*-{
		return this.createShader(type);
	}-*/;

	public final native WebGLTexture createTexture () /*-{
		return this.createTexture();
	}-*/;

	public final native void cullFace (int mode) /*-{
		this.cullFace(mode);
	}-*/;

	public final native void deleteBuffer (WebGLBuffer buffer) /*-{
		this.deleteBuffer(buffer);
	}-*/;

	public final native void deleteFramebuffer (WebGLFramebuffer framebuffer) /*-{
		this.deleteFramebuffer(framebuffer);
	}-*/;

	public final native void deleteProgram (WebGLProgram program) /*-{
		this.deleteProgram(program);
	}-*/;

	public final native void deleteRenderbuffer (WebGLRenderbuffer renderbuffer) /*-{
		this.deleteRenderbuffer(renderbuffer);
	}-*/;

	public final native void deleteShader (WebGLShader shader) /*-{
		this.deleteShader(shader);
	}-*/;

	public final native void deleteTexture (WebGLTexture texture) /*-{
		this.deleteTexture(texture);
	}-*/;

	public final native void depthFunc (int func) /*-{
		this.depthFunc(func);
	}-*/;

	public final native void depthMask (boolean flag) /*-{
		this.depthMask(flag);
	}-*/;

	public final native void depthRange (float zNear, float zFar) /*-{
		this.depthRange(zNear, zFar);
	}-*/;

	public final native void detachShader (WebGLProgram program, WebGLShader shader) /*-{
		this.detachShader(program, shader);
	}-*/;

	public final native void disable (int cap) /*-{
		this.disable(cap);
	}-*/;

	public final native void disableVertexAttribArray (int index) /*-{
		this.disableVertexAttribArray(index);
	}-*/;

	public final native void drawArrays (int mode, int first, int count) /*-{
		this.drawArrays(mode, first, count);
	}-*/;

	public final native void drawElements (int mode, int count, int type, int offset) /*-{
		this.drawElements(mode, count, type, offset);
	}-*/;

	public final native void enable (int cap) /*-{
		this.enable(cap);
	}-*/;

	public final native void enableVertexAttribArray (int index) /*-{
		this.enableVertexAttribArray(index);
	}-*/;

	public final native void finish () /*-{
		this.finish();
	}-*/;

	public final native void flush () /*-{
		this.flush();
	}-*/;

	public final native void framebufferRenderbuffer (int target, int attachment, int renderbuffertarget,
		WebGLRenderbuffer renderbuffer) /*-{
		this.framebufferRenderbuffer(target, attachment, renderbuffertarget,
				renderbuffer);
	}-*/;

	public final native void framebufferTexture2D (int target, int attachment, int textarget, WebGLTexture texture, int level) /*-{
		this
				.framebufferTexture2D(target, attachment, textarget, texture,
						level);
	}-*/;

	public final native void frontFace (int mode) /*-{
		this.frontFace(mode);
	}-*/;

	public final native void generateMipmap (int target) /*-{
		this.generateMipmap(target);
	}-*/;

	public final native WebGLActiveInfo getActiveAttrib (WebGLProgram program, int index) /*-{
		return this.getActiveAttrib(program, index);
	}-*/;

	public final native WebGLActiveInfo getActiveUniform (WebGLProgram program, int index) /*-{
		return this.getActiveUniform(program, index);
	}-*/;

	public final native JsArray<WebGLShader> getAttachedShaders (WebGLProgram program) /*-{
		return this.getAttachedShaders(program);
	}-*/;

	public final native int getAttribLocation (WebGLProgram program, String name) /*-{
		return this.getAttribLocation(program, name);
	}-*/;

	public final native int getError () /*-{
		return this.getError();
	}-*/;

	public final native String getProgramInfoLog (WebGLProgram program) /*-{
		return this.getProgramInfoLog(program);
	}-*/;

	public final native String getShaderInfoLog (WebGLShader shader) /*-{
		return this.getShaderInfoLog(shader);
	}-*/;

	public final native String getShaderSource (WebGLShader shader) /*-{
		return this.getShaderSource(shader);
	}-*/;

	public final native WebGLUniformLocation getUniformLocation (WebGLProgram program, String name) /*-{
		return this.getUniformLocation(program, name);
	}-*/;

	public final native int getVertexAttribOffset (int index, int pname) /*-{
		return this.getVertexAttribOffset(index, pname);
	}-*/;

	public final native void hint (int target, int mode) /*-{
		this.hint(target, mode);
	}-*/;

	public final native boolean isBuffer (WebGLBuffer buffer) /*-{
		return this.isBuffer(buffer);
	}-*/;

	public final native boolean isEnabled (int cap) /*-{
		return this.isEnabled(cap);
	}-*/;

	public final native boolean isFramebuffer (WebGLFramebuffer framebuffer) /*-{
		return this.isFramebuffer(framebuffer);
	}-*/;

	public final native boolean isProgram (WebGLProgram program) /*-{
		return this.isProgram(program);
	}-*/;

	public final native boolean isRenderbuffer (WebGLRenderbuffer renderbuffer) /*-{
		return this.isRenderbuffer(renderbuffer);
	}-*/;

	public final native boolean isShader (WebGLShader shader) /*-{
		return this.isShader(shader);
	}-*/;

	public final native boolean isTexture (WebGLTexture texture) /*-{
		return this.isTexture(texture);
	}-*/;

	public final native void lineWidth (float width) /*-{
		this.lineWidth(width);
	}-*/;

	public final native void linkProgram (WebGLProgram program) /*-{
		this.linkProgram(program);
	}-*/;

	public final native void pixelStorei (int pname, int param) /*-{
		this.pixelStorei(pname, param);
	}-*/;

	public final native void polygonOffset (float factor, float units) /*-{
		this.polygonOffset(factor, units);
	}-*/;

	public final native void readPixels (int x, int y, int width, int height, int format, int type, ArrayBufferView pixels) /*-{
		this.readPixels(x, y, width, height, format, type, pixels);
	}-*/;

	public final native void renderbufferStorage (int target, int internalformat, int width, int height) /*-{
		this.renderbufferStorage(target, internalformat, width, height);
	}-*/;

	public final native void sampleCoverage (float value, boolean invert) /*-{
		this.sampleCoverage(value, invert);
	}-*/;

	public final native void scissor (int x, int y, int width, int height) /*-{
		this.scissor(x, y, width, height);
	}-*/;

	public final native void shaderSource (WebGLShader shader, String source) /*-{
		this.shaderSource(shader, source);
	}-*/;

	public final native void stencilFunc (int func, int ref, int mask) /*-{
		this.stencilFunc(func, ref, mask);
	}-*/;

	public final native void stencilFuncSeparate (int face, int func, int ref, int mask) /*-{
		this.stencilFuncSeparate(face, func, ref, mask);
	}-*/;

	public final native void stencilMask (int mask) /*-{
		this.stencilMask(mask);
	}-*/;

	public final native void stencilMaskSeparate (int face, int mask) /*-{
		this.stencilMaskSeparate(face, mask);
	}-*/;

	public final native void stencilOp (int fail, int zfail, int zpass) /*-{
		this.stencilOp(fail, zfail, zpass);
	}-*/;

	public final native void stencilOpSeparate (int face, int fail, int zfail, int zpass) /*-{
		this.stencilOpSeparate(face, fail, zfail, zpass);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int width, int height, int border, int format,
		int type, ArrayBufferView pixels) /*-{
		this.texImage2D(target, level, internalformat, width, height, border,
				format, type, pixels);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int format, int type, ImageData pixels) /*-{
		this.texImage2D(target, level, internalformat, format, type, pixels);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int format, int type, ImageElement image) /*-{
		this.texImage2D(target, level, internalformat, format, type, image);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int format, int type, CanvasElement canvas) /*-{
		this.texImage2D(target, level, internalformat, format, type, canvas);
	}-*/;

	public final native void texImage2D (int target, int level, int internalformat, int format, int type, VideoElement video) /*-{
		this.texImage2D(target, level, internalformat, format, type, video);
	}-*/;

	public final native void texParameterf (int target, int pname, float param) /*-{
		this.texParameterf(target, pname, param);
	}-*/;

	public final native void texParameteri (int target, int pname, int param) /*-{
		this.texParameteri(target, pname, param);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int type, ArrayBufferView pixels) /*-{
		this.texSubImage2D(target, level, xoffset, yoffset, width, height,
				format, type, pixels);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int format, int type, ImageData pixels) /*-{
		this.texSubImage2D(target, level, xoffset, yoffset, format, type,
				pixels);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int format, int type,
		ImageElement image) /*-{
		this
				.texSubImage2D(target, level, xoffset, yoffset, format, type,
						image);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int format, int type,
		CanvasElement canvas) /*-{
		this.texSubImage2D(target, level, xoffset, yoffset, format, type,
				canvas);
	}-*/;

	public final native void texSubImage2D (int target, int level, int xoffset, int yoffset, int format, int type,
		VideoElement video) /*-{
		this
				.texSubImage2D(target, level, xoffset, yoffset, format, type,
						video);
	}-*/;

	public final native void uniform1f (WebGLUniformLocation location, float x) /*-{
		this.uniform1f(location, x);
	}-*/;

	public final void uniform1fv (WebGLUniformLocation location, Float32Array v) {
		this.uniform1fv(location, (JavaScriptObject)v);
	}

	public final void uniform1fv (WebGLUniformLocation location, float[] v) {
		this.uniform1fv(location, toJsArray(v));
	}

	private final native void uniform1fv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform1fv(location, v);
	}-*/;

	public final native void uniform1i (WebGLUniformLocation location, int x) /*-{
		this.uniform1i(location, x);
	}-*/;

	public final void uniform1iv (WebGLUniformLocation location, Int32Array v) {
		this.uniform1iv(location, (JavaScriptObject)v);
	}

	public final void uniform1iv (WebGLUniformLocation location, int[] v) {
		this.uniform1iv(location, toJsArray(v));
	}

	private final native void uniform1iv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform1iv(location, v);
	}-*/;

	public final native void uniform2f (WebGLUniformLocation location, float x, float y) /*-{
		this.uniform2f(location, x, y);
	}-*/;

	public final void uniform2fv (WebGLUniformLocation location, Float32Array v) {
		this.uniform2fv(location, (JavaScriptObject)v);
	}

	public final void uniform2fv (WebGLUniformLocation location, float[] v) {
		this.uniform2fv(location, toJsArray(v));
	}

	private final native void uniform2fv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform2fv(location, v);
	}-*/;

	public final native void uniform2i (WebGLUniformLocation location, int x, int y) /*-{
		this.uniform2i(location, x, y);
	}-*/;

	public final void uniform2iv (WebGLUniformLocation location, Int32Array v) {
		this.uniform2iv(location, (JavaScriptObject)v);
	}

	public final void uniform2iv (WebGLUniformLocation location, int[] v) {
		this.uniform2iv(location, toJsArray(v));
	}

	private final native void uniform2iv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform2iv(location, v);
	}-*/;

	public final native void uniform3f (WebGLUniformLocation location, float x, float y, float z) /*-{
		this.uniform3f(location, x, y, z);
	}-*/;

	public final void uniform3fv (WebGLUniformLocation location, Float32Array v) {
		this.uniform3fv(location, (JavaScriptObject)v);
	}

	public final void uniform3fv (WebGLUniformLocation location, float[] v) {
		this.uniform3fv(location, toJsArray(v));
	}

	private final native void uniform3fv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform3fv(location, v);
	}-*/;

	public final native void uniform3i (WebGLUniformLocation location, int x, int y, int z) /*-{
		this.uniform3i(location, x, y, z);
	}-*/;

	public final void uniform3iv (WebGLUniformLocation location, Int32Array v) {
		this.uniform3iv(location, (JavaScriptObject)v);
	}

	public final void uniform3iv (WebGLUniformLocation location, int[] v) {

		this.uniform3iv(location, toJsArray(v));
	}

	private final native void uniform3iv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform3iv(location, v);
	}-*/;

	public final native void uniform4f (WebGLUniformLocation location, float x, float y, float z, float w) /*-{
		this.uniform4f(location, x, y, z, w);
	}-*/;

	public final void uniform4fv (WebGLUniformLocation location, Float32Array v) {
		this.uniform4fv(location, (JavaScriptObject)v);
	}

	public final void uniform4fv (WebGLUniformLocation location, float[] v) {
		this.uniform4fv(location, toJsArray(v));
	}

	private final native void uniform4fv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform4fv(location, v);
	}-*/;

	public final native void uniform4i (WebGLUniformLocation location, int x, int y, int z, int w) /*-{
		this.uniform4i(location, x, y, z, w);
	}-*/;

	public final void uniform4iv (WebGLUniformLocation location, Int32Array v) {
		this.uniform4iv(location, (JavaScriptObject)v);
	}

	public final void uniform4iv (WebGLUniformLocation location, int[] v) {
		this.uniform4iv(location, toJsArray(v));
	}

	private final native void uniform4iv (WebGLUniformLocation location, JavaScriptObject v) /*-{
		this.uniform4iv(location, v);
	}-*/;

	public final void uniformMatrix2fv (WebGLUniformLocation location, boolean transpose, Float32Array value) {
		this.uniformMatrix2fv(location, transpose, (JavaScriptObject)value);
	}

	public final void uniformMatrix2fv (WebGLUniformLocation location, boolean transpose, float[] value) {
		this.uniformMatrix2fv(location, transpose, toJsArray(value));
	}

	private final native void uniformMatrix2fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value) /*-{
		this.uniformMatrix2fv(location, transpose, value);
	}-*/;

	public final void uniformMatrix3fv (WebGLUniformLocation location, boolean transpose, Float32Array value) {
		this.uniformMatrix3fv(location, transpose, (JavaScriptObject)value);
	}

	public final void uniformMatrix3fv (WebGLUniformLocation location, boolean transpose, float[] value) {
		this.uniformMatrix3fv(location, transpose, toJsArray(value));
	}

	private final native void uniformMatrix3fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value) /*-{
		this.uniformMatrix3fv(location, transpose, value);
	}-*/;

	public final void uniformMatrix4fv (WebGLUniformLocation location, boolean transpose, Float32Array value) {
		this.uniformMatrix4fv(location, transpose, (JavaScriptObject)value);
	}

	public final void uniformMatrix4fv (WebGLUniformLocation location, boolean transpose, float[] value) {
		uniformMatrix4fv(location, transpose, toJsArray(value));
	}

	private final native void uniformMatrix4fv (WebGLUniformLocation location, boolean transpose, JavaScriptObject value) /*-{
		this.uniformMatrix4fv(location, transpose, value);
	}-*/;

	public final native void useProgram (WebGLProgram program) /*-{
		this.useProgram(program);
	}-*/;

	public final native void validateProgram (WebGLProgram program) /*-{
		this.validateProgram(program);
	}-*/;

	public final native void vertexAttrib1f (int indx, float x) /*-{
		this.vertexAttrib1f(indx, x);
	}-*/;

	public final void vertexAttrib1fv (int indx, Float32Array values) {
		this.vertexAttrib1fv(indx, (JavaScriptObject)values);
	}

	public final void vertexAttrib1fv (int indx, float[] values) {
		this.vertexAttrib1fv(indx, toJsArray(values));
	}

	private final native void vertexAttrib1fv (int indx, JavaScriptObject values) /*-{
		this.vertexAttrib1fv(indx, values);
	}-*/;

	public final native void vertexAttrib2f (int indx, float x, float y) /*-{
		this.vertexAttrib2f(indx, x, y);
	}-*/;

	public final void vertexAttrib2fv (int indx, Float32Array values) {
		this.vertexAttrib2fv(indx, (JavaScriptObject)values);
	}

	public final void vertexAttrib2fv (int indx, float[] values) {
		this.vertexAttrib2fv(indx, toJsArray(values));
	}

	private final native void vertexAttrib2fv (int indx, JavaScriptObject values) /*-{
		this.vertexAttrib2fv(indx, values);
	}-*/;

	public final native void vertexAttrib3f (int indx, float x, float y, float z) /*-{
		this.vertexAttrib3f(indx, x, y, z);
	}-*/;

	public final void vertexAttrib3fv (int indx, Float32Array values) {
		this.vertexAttrib3fv(indx, (JavaScriptObject)values);
	}

	public final void vertexAttrib3fv (int indx, float[] values) {
		this.vertexAttrib3fv(indx, toJsArray(values));
	}

	private final native void vertexAttrib3fv (int indx, JavaScriptObject values) /*-{
		this.vertexAttrib3fv(indx, values);
	}-*/;

	public final native void vertexAttrib4f (int indx, float x, float y, float z, float w) /*-{
		this.vertexAttrib4f(indx, x, y, z, w);
	}-*/;

	public final void vertexAttrib4fv (int indx, Float32Array values) {
		this.vertexAttrib4fv(indx, (JavaScriptObject)values);
	}

	public final void vertexAttrib4fv (int indx, float[] values) {
		this.vertexAttrib4fv(indx, toJsArray(values));
	}

	public final Float32Array toJsArray (float[] values) {
		Float32Array array = TypedArrays.createFloat32Array(values.length);
		array.set(values);
		return array;
	}

	public final Int32Array toJsArray (int[] values) {
		Int32Array array = TypedArrays.createInt32Array(values.length);
		array.set(values);
		return array;
	}

	private final native void vertexAttrib4fv (int indx, JavaScriptObject values) /*-{
		this.vertexAttrib4fv(indx, values);
	}-*/;

	public final native void vertexAttribPointer (int indx, int size, int type, boolean normalized, int stride, int offset) /*-{
		this.vertexAttribPointer(indx, size, type, normalized, stride, offset);
	}-*/;

	public final native void viewport (int x, int y, int width, int height) /*-{
		this.viewport(x, y, width, height);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of RENDERER, SHADING_LANGUAGE_VERSION, VENDOR, VERSION */
	public final native String getParameterString (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of ACTIVE_TEXTURE, ALPHA_BITS, BLEND_DST_ALPHA, BLEND_DST_RGB, BLEND_EQUATION_ALPHA, BLEND_EQUATION_RGB,
	 *           BLEND_SRC_ALPHA, BLEND_SRC_RGB, BLUE_BITS, CULL_FACE_MODE, DEPTH_BITS, DEPTH_FUNC, FRONT_FACE,
	 *           GENERATE_MIPMAP_HINT, GREEN_BITS, IMPLEMENTATION_COLOR_READ_FORMAT, IMPLEMENTATION_COLOR_READ_TYPE,
	 *           MAX_COMBINED_TEXTURE_IMAGE_UNITS, MAX_CUBE_MAP_TEXTURE_SIZE, MAX_FRAGMENT_UNIFORM_VECTORS, MAX_RENDERBUFFER_SIZE,
	 *           MAX_TEXTURE_IMAGE_UNITS, MAX_TEXTURE_SIZE, MAX_VARYING_VECTORS, MAX_VERTEX_ATTRIBS,
	 *           MAX_VERTEX_TEXTURE_IMAGE_UNITS, MAX_VERTEX_UNIFORM_VECTORS, NUM_COMPRESSED_TEXTURE_FORMATS, PACK_ALIGNMENT,
	 *           RED_BITS, SAMPLE_BUFFERS, SAMPLES, STENCIL_BACK_FAIL, STENCIL_BACK_FUNC, STENCIL_BACK_PASS_DEPTH_FAIL,
	 *           STENCIL_BACK_PASS_DEPTH_PASS, STENCIL_BACK_REF, STENCIL_BACK_VALUE_MASK, STENCIL_BACK_WRITEMASK, STENCIL_BITS,
	 *           STENCIL_CLEAR_VALUE, STENCIL_FAIL, STENCIL_FUNC, STENCIL_PASS_DEPTH_FAIL, STENCIL_PASS_DEPTH_PASS, STENCIL_REF,
	 *           STENCIL_VALUE_MASK, STENCIL_WRITEMASK, SUBPIXEL_BITS, UNPACK_ALIGNMENT */
	public final native int getParameteri (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of BLEND, CULL_FACE, DEPTH_TEST, DEPTH_WRITEMASK, DITHER, POLYGON_OFFSET_FILL, SAMPLE_COVERAGE_INVERT,
	 *           SCISSOR_TEST, STENCIL_TEST, UNPACK_FLIP_Y_WEBGL, UNPACK_PREMULTIPLY_ALPHA_WEBGL */
	public final native boolean getParameterb (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of DEPTH_CLEAR_VALUE, LINE_WIDTH, POLYGON_OFFSET_FACTOR, POLYGON_OFFSET_UNITS, SAMPLE_COVERAGE_VALUE */
	public final native float getParameterf (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of ARRAY_BUFFER_BINDING, COMPRESSED_TEXTURE_FORMATS, CURRENT_PROGRAM, ELEMENT_ARRAY_BUFFER_BINDING,
	 *           FRAMEBUFFER_BINDING, RENDERBUFFER_BINDING, TEXTURE_BINDING_2D, TEXTURE_BINDING_CUBE_MAP */
	public final native <T extends WebGLObject> T getParametero (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of ALIASED_LINE_WIDTH_RANGE, ALIASED_POINT_SIZE_RANGE, BLEND_COLOR, COLOR_CLEAR_VALUE, COLOR_WRITEMASK,
	 *           DEPTH_RANGE, MAX_VIEWPORT_DIMS, SCISSOR_BOX, VIEWPORT */
	public final native <T extends ArrayBufferView> T getParameterv (int pname) /*-{
		return this.getParameter(pname);
	}-*/;

	/** Return the uniform value at the passed location in the passed program. */
	public final native boolean getUniformb (WebGLProgram program, WebGLUniformLocation location) /*-{
		return this.getUniform(program, location);
	}-*/;

	/** Return the uniform value at the passed location in the passed program. */
	public final native int getUniformi (WebGLProgram program, WebGLUniformLocation location) /*-{
		return this.getUniform(program, location);
	}-*/;

	/** Return the uniform value at the passed location in the passed program. */
	public final native float getUniformf (WebGLProgram program, WebGLUniformLocation location) /*-{
		return this.getUniform(program, location);
	}-*/;

	/** Return the uniform value at the passed location in the passed program. */
	public final native <T extends ArrayBufferView> T getUniformv (WebGLProgram program, WebGLUniformLocation location) /*-{
		return this.getUniform(program, location);
	}-*/;

	/** Return the information requested in pname about the vertex attribute at the passed index.
	 * 
	 * @param pname one of VERTEX_ATTRIB_ARRAY_SIZE, VERTEX_ATTRIB_ARRAY_STRIDE, VERTEX_ATTRIB_ARRAY_TYPE */
	public final native int getVertexAttribi (int index, int pname) /*-{
		return this.getVertexAttrib(index, pname);
	}-*/;

	/** Return the information requested in pname about the vertex attribute at the passed index.
	 * 
	 * @param pname one of VERTEX_ATTRIB_ARRAY_ENABLED, VERTEX_ATTRIB_ARRAY_NORMALIZED */
	public final native boolean getVertexAttribb (int index, int pname) /*-{
		return this.getVertexAttrib(index, pname);
	}-*/;

	/** Return the information requested in pname about the vertex attribute at the passed index.
	 * 
	 * @param pname VERTEX_ATTRIB_ARRAY_BUFFER_BINDING
	 * @return {@link WebGLBuffer} */
	public final native <T extends WebGLObject> T getVertexAttribo (int index, int pname) /*-{
		return this.getVertexAttrib(index, pname);
	}-*/;

	/** Return the information requested in pname about the vertex attribute at the passed index.
	 * 
	 * @param pname CURRENT_VERTEX_ATTRIB
	 * @return a {@link Float32Array} with 4 elements */
	public final native Float32Array getVertexAttribv (int index, int pname) /*-{
		return this.getVertexAttrib(index, pname);
	}-*/;

	/** Return the value for the passed pname given the passed target. The type returned is the natural type for the requested
	 * pname, as given in the following table: If an attempt is made to call this function with no WebGLTexture bound (see above),
	 * an INVALID_OPERATION error is generated.
	 * 
	 * @param pname one of TEXTURE_MAG_FILTER, TEXTURE_MIN_FILTER, TEXTURE_WRAP_S, TEXTURE_WRAP_T */
	public final native int getTexParameter (int target, int pname) /*-{
		return this.getTexParameter(target, pname);
	}-*/;

	/** Return the value for the passed pname given the passed shader.
	 * 
	 * @param pname one of DELETE_STATUS, COMPILE_STATUS */
	public final native boolean getShaderParameterb (WebGLShader shader, int pname) /*-{
		return this.getShaderParameter(shader, pname);
	}-*/;

	/** Return the value for the passed pname given the passed shader.
	 * 
	 * @param pname one of SHADER_TYPE, INFO_LOG_LENGTH, SHADER_SOURCE_LENGTH */
	public final native int getShaderParameteri (WebGLShader shader, int pname) /*-{
		return this.getShaderParameter(shader, pname);
	}-*/;

	/** Return the value for the passed pname given the passed target.
	 * 
	 * @param pname one of RENDERBUFFER_WIDTH, RENDERBUFFER_HEIGHT, RENDERBUFFER_INTERNAL_FORMAT, RENDERBUFFER_RED_SIZE,
	 *           RENDERBUFFER_GREEN_SIZE, RENDERBUFFER_BLUE_SIZE, RENDERBUFFER_ALPHA_SIZE, RENDERBUFFER_DEPTH_SIZE,
	 *           RENDERBUFFER_STENCIL_SIZE */
	public final native int getRenderbufferParameter (int target, int pname) /*-{
		return this.getRenderbufferParameter(target, pname);
	}-*/;

	/** Return the value for the passed pname given the passed program.
	 * 
	 * @param pname one of DELETE_STATUS, LINK_STATUS, VALIDATE_STATUS */
	public final native boolean getProgramParameterb (WebGLProgram program, int pname) /*-{
		return this.getProgramParameter(program, pname);
	}-*/;

	/** Return the value for the passed pname given the passed program.
	 * 
	 * @param pname one of INFO_LOG_LENGTH, ATTACHED_SHADERS, ACTIVE_ATTRIBUTES, ACTIVE_ATTRIBUTE_MAX_LENGTH, ACTIVE_UNIFORMS,
	 *           ACTIVE_UNIFORM_MAX_LENGTH */
	public final native int getProgramParameteri (WebGLProgram program, int pname) /*-{
		return this.getProgramParameter(program, pname);
	}-*/;

	/** Return the value for the passed pname.
	 * 
	 * @param pname one of BUFFER_SIZE, BUFFER_USAGE */
	public final native int getBufferParameter (int target, int pname) /*-{
		return this.getBufferParameter(target, pname);
	}-*/;

	/** Return the value for the passed pname given the passed target and attachment.
	 * 
	 * @param pname one of FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL,
	 *           FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE */
	public final native int getFramebufferAttachmentParameteri (int target, int attachment, int pname) /*-{
		return this
				.getFramebufferAttachmentParameter(target, attachment, pname);
	}-*/;

	/** Return the value for the passed pname given the passed target and attachment.
	 * 
	 * @param pname FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
	 * @return {@link WebGLRenderbuffer} or {@link WebGLTexture} */
	public final native <T extends WebGLObject> T getFramebufferAttachmentParametero (int target, int attachment, int pname) /*-{
		return this
				.getFramebufferAttachmentParameter(target, attachment, pname);
	}-*/;
}
