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

package com.badlogic.gdx.graphics;

import java.nio.Buffer;
import java.nio.IntBuffer;

/** This interface defines methods common to GL10, GL11 and GL20.
 * @author mzechner */
public interface GLCommon {
	public static final int GL_GENERATE_MIPMAP = 0x8191;
	public static final int GL_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FE;
	public static final int GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT = 0x84FF;
	
	/**
	 * glActiveTexture — select active texture unit
	 * @param texture Specifies which texture unit to make active. The number of texture units is implementation dependent, but must be at least two. texture must be one of GL_TEXTUREi, where i ranges from 0 to the larger of (GL_MAX_TEXTURE_COORDS - 1) and (GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS - 1). The initial value is GL_TEXTURE0.
	 */
	public void glActiveTexture (int texture);

	public void glBindTexture (int target, int texture);

	/**
	 * glBlendFunc — specify pixel arithmetic
	 * @param sfactor Specifies how the red, green, blue, and alpha source blending factors are computed. The following symbolic constants are accepted: GL_ZERO, GL_ONE, GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR, GL_DST_COLOR, GL_ONE_MINUS_DST_COLOR, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA, GL_CONSTANT_COLOR, GL_ONE_MINUS_CONSTANT_COLOR, GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA, and GL_SRC_ALPHA_SATURATE. The initial value is GL_ONE. <p>
	 * @param dfactor Specifies how the red, green, blue, and alpha destination blending factors are computed. The following symbolic constants are accepted: GL_ZERO, GL_ONE, GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR, GL_DST_COLOR, GL_ONE_MINUS_DST_COLOR, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA. GL_CONSTANT_COLOR, GL_ONE_MINUS_CONSTANT_COLOR, GL_CONSTANT_ALPHA, and GL_ONE_MINUS_CONSTANT_ALPHA. The initial value is GL_ZERO.
	 */
	public void glBlendFunc (int sfactor, int dfactor);

	public void glClear (int mask);

	public void glClearColor (float red, float green, float blue, float alpha);

	public void glClearDepthf (float depth);

	public void glClearStencil (int s);

	public void glColorMask (boolean red, boolean green, boolean blue, boolean alpha);

	public void glCompressedTexImage2D (int target, int level, int internalformat, int width, int height, int border,
		int imageSize, Buffer data);

	public void glCompressedTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format,
		int imageSize, Buffer data);

	public void glCopyTexImage2D (int target, int level, int internalformat, int x, int y, int width, int height, int border);

	public void glCopyTexSubImage2D (int target, int level, int xoffset, int yoffset, int x, int y, int width, int height);

	public void glCullFace (int mode);

	public void glDeleteTextures (int n, IntBuffer textures);

	public void glDepthFunc (int func);

	public void glDepthMask (boolean flag);

	public void glDepthRangef (float zNear, float zFar);

	public void glDisable (int cap);

	public void glDrawArrays (int mode, int first, int count);

	public void glDrawElements (int mode, int count, int type, Buffer indices);

	/**
	 * glEnable — enable or disable server-side GL capabilities
	 * @param cap <p>
	 * GL_BLEND<p>
	 * If enabled, blend the computed fragment color values with the values in the color buffers. See {@link GLCommon#glBlendFunc}.<p>
	 * GL_CLIP_DISTANCEi<p>
	 * If enabled, clip geometry against user-defined half space i.<p>
	 * GL_COLOR_LOGIC_OP<p>
	 * If enabled, apply the currently selected logical operation to the computed fragment color and color buffer values. See {@link GL10#glLogicOp}.<p>
	 * GL_CULL_FACE<p>
	 * If enabled, cull polygons based on their winding in window coordinates. See {@link GLCommon#glCullFace}.<p>
	 * GL_DEBUG_OUTPUT<p>
	 * If enabled, debug messages are produced by a debug context. When disabled, the debug message log is silenced. Note that in a non-debug context, very few, if any messages might be produced, even when GL_DEBUG_OUTPUT is enabled.<p>
	 * GL_DEBUG_OUTPUT_SYNCHRONOUS<p>
	 * If enabled, debug messages are produced synchronously by a debug context. If disabled, debug messages may be produced asynchronously. In particular, they may be delayed relative to the execution of GL commands, and the debug callback function may be called from a thread other than that in which the commands are executed. See glDebugMessageCallback.<p>
	 * GL_DEPTH_CLAMP<p>
	 * If enabled, the -wc≤zc≤wc plane equation is ignored by view volume clipping (effectively, there is no near or far plane clipping). See {@link GLCommon#glDepthRangef}.<p>
	 * GL_DEPTH_TEST<p>
	 * If enabled, do depth comparisons and update the depth buffer. Note that even if the depth buffer exists and the depth mask is non-zero, the depth buffer is not updated if the depth test is disabled. See glDepthFunc and {@link GLCommon#glDepthRangef}.<p>
	 * GL_DITHER<p>
	 * If enabled, dither color components or indices before they are written to the color buffer.<p>
	 * GL_FRAMEBUFFER_SRGB<p>
	 * If enabled and the value of GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING for the framebuffer attachment corresponding to the destination buffer is GL_SRGB, the R, G, and B destination color values (after conversion from fixed-point to floating-point) are considered to be encoded for the sRGB color space and hence are linearized prior to their use in blending.<p>
	 * GL_LINE_SMOOTH<p>
	 * If enabled, draw lines with correct filtering. Otherwise, draw aliased lines. See {@link GLCommon#glLineWidth}.<p>
	 * GL_MULTISAMPLE<p>
	 * If enabled, use multiple fragment samples in computing the final color of a pixel. See {@link GL10#glSampleCoverage}.<p>
	 * GL_POLYGON_OFFSET_FILL<p>
	 * If enabled, and if the polygon is rendered in GL_FILL mode, an offset is added to depth values of a polygon's fragments before the depth comparison is performed. See {@link GLCommon#glPolygonOffset}.<p>
	 * GL_POLYGON_OFFSET_LINE<p>
	 * If enabled, and if the polygon is rendered in GL_LINE mode, an offset is added to depth values of a polygon's fragments before the depth comparison is performed. See {@link GLCommon#glPolygonOffset}.<p>
	 * GL_POLYGON_OFFSET_POINT<p>
	 * If enabled, an offset is added to depth values of a polygon's fragments before the depth comparison is performed, if the polygon is rendered in GL_POINT mode. See {@link GLCommon#glPolygonOffset}.<p>
	 * GL_POLYGON_SMOOTH<p>
	 * If enabled, draw polygons with proper filtering. Otherwise, draw aliased polygons. For correct antialiased polygons, an alpha buffer is needed and the polygons must be sorted front to back.<p>
	 * GL_PRIMITIVE_RESTART<p>
	 * Enables primitive restarting. If enabled, any one of the draw commands which transfers a set of generic attribute array elements to the GL will restart the primitive when the index of the vertex is equal to the primitive restart index. See glPrimitiveRestartIndex.<p>
	 * GL_PRIMITIVE_RESTART_FIXED_INDEX<p>
	 * Enables primitive restarting with a fixed index. If enabled, any one of the draw commands which transfers a set of generic attribute array elements to the GL will restart the primitive when the index of the vertex is equal to the fixed primitive index for the specified index type. The fixed index is equal to 2n−1 where n is equal to 8 for GL_UNSIGNED_BYTE, 16 for GL_UNSIGNED_SHORT and 32 for GL_UNSIGNED_INT.<p>
	 * GL_SAMPLE_ALPHA_TO_COVERAGE<p>
	 * If enabled, compute a temporary coverage value where each bit is determined by the alpha value at the corresponding sample location. The temporary coverage value is then ANDed with the fragment coverage value.<p>
	 * GL_SAMPLE_ALPHA_TO_ONE<p>
	 * If enabled, each sample alpha value is replaced by the maximum representable alpha value.<p>
	 * GL_SAMPLE_COVERAGE<p>
	 * If enabled, the fragment's coverage is ANDed with the temporary coverage value. If GL_SAMPLE_COVERAGE_INVERT is set to GL_TRUE, invert the coverage value. See {@link GL10#glSampleCoverage}.<p>
	 * GL_SAMPLE_SHADING<p>
	 * If enabled, the active fragment shader is run once for each covered sample, or at fraction of this rate as determined by the current value of GL_MIN_SAMPLE_SHADING_VALUE. See glMinSampleShading.<p>
	 * GL_SAMPLE_MASK<p>
	 * If enabled, the sample coverage mask generated for a fragment during rasterization will be ANDed with the value of GL_SAMPLE_MASK_VALUE before shading occurs. See glSampleMaski.<p>
	 * GL_SCISSOR_TEST<p>
	 * If enabled, discard fragments that are outside the scissor rectangle. See glScissor.<p>
	 * GL_STENCIL_TEST<p>
	 * If enabled, do stencil testing and update the stencil buffer. See glStencilFunc and glStencilOp.<p>
	 * GL_TEXTURE_CUBE_MAP_SEAMLESS<p>
	 * If enabled, cubemap textures are sampled such that when linearly sampling from the border between two adjacent faces, texels from both faces are used to generate the final sample value. When disabled, texels from only a single face are used to construct the final sample value.<p>
	 * GL_PROGRAM_POINT_SIZE<p>
	 * If enabled and a vertex or geometry shader is active, then the derived point size is taken from the (potentially clipped) shader builtin gl_PointSize and clamped to the implementation-dependent point size range.<p>
	 */
	public void glEnable (int cap);

	public void glFinish ();

	public void glFlush ();

	public void glFrontFace (int mode);

	public void glGenTextures (int n, IntBuffer textures);

	public int glGetError ();

	public void glGetIntegerv (int pname, IntBuffer params);

	public String glGetString (int name);

	/**
	 * glHint — specify implementation-specific hints
	 * @param target Specifies a symbolic constant indicating the behavior to be controlled. GL_FOG_HINT,	 GL_GENERATE_MIPMAP_HINT, GL_LINE_SMOOTH_HINT, GL_PERSPECTIVE_CORRECTION_HINT, GL_POINT_SMOOTH_HINT, GL_POLYGON_SMOOTH_HINT, GL_TEXTURE_COMPRESSION_HINT, and GL_FRAGMENT_SHADER_DERIVATIVE_HINT are accepted.<p>
	 * @param mode Specifies a symbolic constant indicating the desired behavior. GL_FASTEST, GL_NICEST, and GL_DONT_CARE are accepted.
	 */
	public void glHint (int target, int mode);

	public void glLineWidth (float width);

	/**
	 * glPixelStorei — set pixel storage modes, affect the operation of subsequent {@link GLCommon#glReadPixels} as well as the unpacking of {@link GLCommon#glTexImage2D}, and {@link GLCommon#glTexSubImage2D}.
	 * <p>GL_PACK_ALIGNMENT <p>
	 * Specifies the alignment requirements for the start of each pixel row in memory. The allowable values are 1 (byte-alignment), 2 (rows aligned to even-numbered bytes), 4 (word-alignment), and 8 (rows start on double-word boundaries). The initial value is 4.<p>
	 * GL_UNPACK_ALIGNMENT<p>
	 * Specifies the alignment requirements for the start of each pixel row in memory. The allowable values are 1 (byte-alignment), 2 (rows aligned to even-numbered bytes), 4 (word-alignment), and 8 (rows start on double-word boundaries). The initial value is 4.<p>
	 * @param pname Specifies the symbolic name of the parameter to be set. GL_PACK_ALIGNMENT affects the packing of pixel data into memory. GL_UNPACK_ALIGNMENT affects the unpacking of pixel data from memory.
	 * @param param Specifies the value that pname is set to.
	 * @Note Pixel storage modes are client states.
	 * {@link GLCommon#glCompressedTexImage2D} and {@link GLCommon#glCompressedTexSubImage2D} are not affected by glPixelStorei.
	 */
	public void glPixelStorei (int pname, int param);

	public void glPolygonOffset (float factor, float units);

	public void glReadPixels (int x, int y, int width, int height, int format, int type, Buffer pixels);

	public void glScissor (int x, int y, int width, int height);

	public void glStencilFunc (int func, int ref, int mask);

	public void glStencilMask (int mask);

	public void glStencilOp (int fail, int zfail, int zpass);

	/**
	 * glTexImage2D — specify a two-dimensional texture image
	 * @param target Specifies the target texture. Must be GL_TEXTURE_2D, GL_PROXY_TEXTURE_2D, GL_TEXTURE_CUBE_MAP_POSITIVE_X, GL_TEXTURE_CUBE_MAP_NEGATIVE_X, GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, or GL_PROXY_TEXTURE_CUBE_MAP.
	 * @param level Specifies the level-of-detail number. Level 0 is the base image level. Level n is the nth mipmap reduction image.
	 * @param internalformat Specifies the number of color components in the texture. Must be 1, 2, 3, or 4, or one of the following symbolic constants: GL_ALPHA, GL_ALPHA4, GL_ALPHA8, GL_ALPHA12, GL_ALPHA16, GL_COMPRESSED_ALPHA, GL_COMPRESSED_LUMINANCE, GL_COMPRESSED_LUMINANCE_ALPHA, GL_COMPRESSED_INTENSITY, GL_COMPRESSED_RGB, GL_COMPRESSED_RGBA, GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT32, GL_LUMINANCE, GL_LUMINANCE4, GL_LUMINANCE8, GL_LUMINANCE12, GL_LUMINANCE16, GL_LUMINANCE_ALPHA, GL_LUMINANCE4_ALPHA4, GL_LUMINANCE6_ALPHA2, GL_LUMINANCE8_ALPHA8, GL_LUMINANCE12_ALPHA4, GL_LUMINANCE12_ALPHA12, GL_LUMINANCE16_ALPHA16, GL_INTENSITY, GL_INTENSITY4, GL_INTENSITY8, GL_INTENSITY12, GL_INTENSITY16, GL_R3_G3_B2, GL_RGB, GL_RGB4, GL_RGB5, GL_RGB8, GL_RGB10, GL_RGB12, GL_RGB16, GL_RGBA, GL_RGBA2, GL_RGBA4, GL_RGB5_A1, GL_RGBA8, GL_RGB10_A2, GL_RGBA12, GL_RGBA16, GL_SLUMINANCE, GL_SLUMINANCE8, GL_SLUMINANCE_ALPHA, GL_SLUMINANCE8_ALPHA8, GL_SRGB, GL_SRGB8, GL_SRGB_ALPHA, or GL_SRGB8_ALPHA8.
	 * @param width Specifies the width of the texture image including the border if any. If the GL version does not support non-power-of-two sizes, this value must be 2 n + 2 ⁡ border for some integer n. All implementations support texture images that are at least 64 texels wide.
	 * @param height Specifies the height of the texture image including the border if any. If the GL version does not support non-power-of-two sizes, this value must be 2 m + 2 ⁡ border for some integer m. All implementations support texture images that are at least 64 texels high.
	 * @param border Specifies the width of the border. Must be either 0 or 1.
	 * @param format Specifies the format of the pixel data. The following symbolic values are accepted: GL_COLOR_INDEX, GL_RED, GL_GREEN, GL_BLUE, GL_ALPHA, GL_RGB, GL_BGR, GL_RGBA, GL_BGRA, GL_LUMINANCE, and GL_LUMINANCE_ALPHA.
	 * @param type Specifies the data type of the pixel data. The following symbolic values are accepted: GL_UNSIGNED_BYTE, GL_BYTE, GL_BITMAP, GL_UNSIGNED_SHORT, GL_SHORT, GL_UNSIGNED_INT, GL_INT, GL_FLOAT, GL_UNSIGNED_BYTE_3_3_2, GL_UNSIGNED_BYTE_2_3_3_REV, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_5_6_5_REV, GL_UNSIGNED_SHORT_4_4_4_4, GL_UNSIGNED_SHORT_4_4_4_4_REV, GL_UNSIGNED_SHORT_5_5_5_1, GL_UNSIGNED_SHORT_1_5_5_5_REV, GL_UNSIGNED_INT_8_8_8_8, GL_UNSIGNED_INT_8_8_8_8_REV, GL_UNSIGNED_INT_10_10_10_2, and GL_UNSIGNED_INT_2_10_10_10_REV.
	 * @param pixels Specifies a pointer to the image data in memory.
	 */
	public void glTexImage2D (int target, int level, int internalformat, int width, int height, int border, int format, int type,
		Buffer pixels);

	public void glTexParameterf (int target, int pname, float param);

	public void glTexSubImage2D (int target, int level, int xoffset, int yoffset, int width, int height, int format, int type,
		Buffer pixels);

	public void glViewport (int x, int y, int width, int height);
}
