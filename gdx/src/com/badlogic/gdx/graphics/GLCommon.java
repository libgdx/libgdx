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

	public void glActiveTexture (int texture);

	public void glBindTexture (int target, int texture);

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

	public void glEnable (int cap);

	public void glFinish ();

	public void glFlush ();

	public void glFrontFace (int mode);

	public void glGenTextures (int n, IntBuffer textures);

	public int glGetError ();

	public void glGetIntegerv (int pname, IntBuffer params);

	public String glGetString (int name);

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
