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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Utility class for the relatively large amount of ES 3.0 internal pixel storage formats. Using this class will reduce errors
 * occuring on the opengl level that are usually harder to debug.
 * @author Mattijs Driel */
public class PixelFormatES3 {
	private static final String incompatibleMsg = "Incompatible Type for given InternalFormat.";

	int format;
	int type;
	int internalFormat;

	// Most of this class was created using block selection and macro magic (in notepad++), and the eclipse formatter.

	public enum GLFormat {
		GL_RGB, GL_RGBA, GL_LUMINANCE_ALPHA, GL_LUMINANCE, GL_ALPHA, GL_RED, GL_RED_INTEGER, GL_RG, GL_RG_INTEGER, GL_RGB_INTEGER, GL_RGBA_INTEGER, GL_DEPTH_COMPONENT, GL_DEPTH_STENCIL
	}

	public enum GLType {
		GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT_5_6_5, GL_UNSIGNED_SHORT_4_4_4_4, GL_UNSIGNED_SHORT_5_5_5_1, GL_BYTE, GL_HALF_FLOAT, GL_FLOAT, GL_UNSIGNED_SHORT, GL_SHORT, GL_UNSIGNED_INT, GL_INT, GL_UNSIGNED_INT_10F_11F_11F_REV, GL_UNSIGNED_INT_5_9_9_9_REV, GL_UNSIGNED_INT_2_10_10_10_REV, GL_UNSIGNED_INT_24_8, GL_FLOAT_32_UNSIGNED_INT_24_8_REV
	}

	public enum GLInternalFormat {
		GL_RGB, GL_RGBA, GL_LUMINANCE_ALPHA, GL_LUMINANCE, GL_ALPHA, GL_R8, GL_R8_SNORM, GL_R16F, GL_R32F, GL_R8UI, GL_R8I, GL_R16UI, GL_R16I, GL_R32UI, GL_R32I, GL_RG8, GL_RG8_SNORM, GL_RG16F, GL_RG32F, GL_RG8UI, GL_RG8I, GL_RG16UI, GL_RG16I, GL_RG32UI, GL_RG32I, GL_RGB8, GL_SRGB8, GL_RGB565, GL_RGB8_SNORM, GL_R11F_G11F_B10F, GL_RGB9_E5, GL_RGB16F, GL_RGB32F, GL_RGB8UI, GL_RGB8I, GL_RGB16UI, GL_RGB16I, GL_RGB32UI, GL_RGB32I, GL_RGBA8, GL_SRGB8_ALPHA8, GL_RGBA8_SNORM, GL_RGB5_A1, GL_RGBA4, GL_RGB10_A2, GL_RGBA16F, GL_RGBA32F, GL_RGBA8UI, GL_RGBA8I, GL_RGB10_A2UI, GL_RGBA16UI, GL_RGBA16I, GL_RGBA32I, GL_RGBA32UI, GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT32F, GL_DEPTH24_STENCIL8, GL_DEPTH32F_STENCIL8
	}

	/** See {@link PixelFormatES3#set(GLInternalFormat)} for details. */
	public PixelFormatES3 (GLInternalFormat internalFormat) {
		set(internalFormat, null);
	}

	/** See {@link PixelFormatES3#set(GLInternalFormat, GLType)} for details. */
	public PixelFormatES3 (GLInternalFormat internalFormat, GLType type) {
		set(internalFormat, type);
	}

	/** Sets the InternalFormat to the given value. Format will be set accordingly. Type will be set to a valid default for the
	 * InternalFormat. */
	public void set (GLInternalFormat internalFormat) {
		set(internalFormat, null);
	}

	/** Sets the InternalFormat to the given value. Format will be set accordingly. Type will be set to the given value if it is
	 * valid for the InternalFormat, or an exception is thrown if it is not. */
	public void set (GLInternalFormat internalFormat, GLType type) {
		// first set type if requested
		if (type != null)
			setType(internalFormat, type);
		else
			this.type = -1;

		// now set other fields, and check if valid
		switch (internalFormat) {
		case GL_RGB:
			this.internalFormat = GL30.GL_RGB;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE && this.type != GL30.GL_UNSIGNED_SHORT_5_6_5)
				throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA:
			this.internalFormat = GL30.GL_RGBA;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE && this.type != GL30.GL_UNSIGNED_SHORT_4_4_4_4
				&& this.type != GL30.GL_UNSIGNED_SHORT_5_5_5_1) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_LUMINANCE_ALPHA:
			this.internalFormat = GL30.GL_LUMINANCE_ALPHA;
			this.format = GL30.GL_LUMINANCE_ALPHA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_LUMINANCE:
			this.internalFormat = GL30.GL_LUMINANCE;
			this.format = GL30.GL_LUMINANCE;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_ALPHA:
			this.internalFormat = GL30.GL_ALPHA;
			this.format = GL30.GL_ALPHA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R8:
			this.internalFormat = GL30.GL_R8;
			this.format = GL30.GL_RED;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R8_SNORM:
			this.internalFormat = GL30.GL_R8_SNORM;
			this.format = GL30.GL_RED;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R16F:
			this.internalFormat = GL30.GL_R16F;
			this.format = GL30.GL_RED;
			if (this.type == -1)
				this.type = GL30.GL_HALF_FLOAT;
			else if (this.type != GL30.GL_HALF_FLOAT && this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R32F:
			this.internalFormat = GL30.GL_R32F;
			this.format = GL30.GL_RED;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT;
			else if (this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R8UI:
			this.internalFormat = GL30.GL_R8UI;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R8I:
			this.internalFormat = GL30.GL_R8I;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R16UI:
			this.internalFormat = GL30.GL_R16UI;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_SHORT;
			else if (this.type != GL30.GL_UNSIGNED_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R16I:
			this.internalFormat = GL30.GL_R16I;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_SHORT;
			else if (this.type != GL30.GL_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R32UI:
			this.internalFormat = GL30.GL_R32UI;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT;
			else if (this.type != GL30.GL_UNSIGNED_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R32I:
			this.internalFormat = GL30.GL_R32I;
			this.format = GL30.GL_RED_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_INT;
			else if (this.type != GL30.GL_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG8:
			this.internalFormat = GL30.GL_RG8;
			this.format = GL30.GL_RG;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG8_SNORM:
			this.internalFormat = GL30.GL_RG8_SNORM;
			this.format = GL30.GL_RG;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG16F:
			this.internalFormat = GL30.GL_RG16F;
			this.format = GL30.GL_RG;
			if (this.type == -1)
				this.type = GL30.GL_HALF_FLOAT;
			else if (this.type != GL30.GL_HALF_FLOAT && this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG32F:
			this.internalFormat = GL30.GL_RG32F;
			this.format = GL30.GL_RG;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT;
			else if (this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG8UI:
			this.internalFormat = GL30.GL_RG8UI;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG8I:
			this.internalFormat = GL30.GL_RG8I;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG16UI:
			this.internalFormat = GL30.GL_RG16UI;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_SHORT;
			else if (this.type != GL30.GL_UNSIGNED_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG16I:
			this.internalFormat = GL30.GL_RG16I;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_SHORT;
			else if (this.type != GL30.GL_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG32UI:
			this.internalFormat = GL30.GL_RG32UI;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT;
			else if (this.type != GL30.GL_UNSIGNED_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RG32I:
			this.internalFormat = GL30.GL_RG32I;
			this.format = GL30.GL_RG_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_INT;
			else if (this.type != GL30.GL_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB8:
			this.internalFormat = GL30.GL_RGB8;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_SRGB8:
			this.internalFormat = GL30.GL_SRGB8;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB565:
			this.internalFormat = GL30.GL_RGB565;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE && this.type != GL30.GL_UNSIGNED_SHORT_5_6_5)
				throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB8_SNORM:
			this.internalFormat = GL30.GL_RGB8_SNORM;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_R11F_G11F_B10F:
			this.internalFormat = GL30.GL_R11F_G11F_B10F;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT_10F_11F_11F_REV;
			else if (this.type != GL30.GL_UNSIGNED_INT_10F_11F_11F_REV && this.type != GL30.GL_HALF_FLOAT
				&& this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB9_E5:
			this.internalFormat = GL30.GL_RGB9_E5;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT_5_9_9_9_REV;
			else if (this.type != GL30.GL_UNSIGNED_INT_5_9_9_9_REV && this.type != GL30.GL_HALF_FLOAT && this.type != GL30.GL_FLOAT)
				throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB16F:
			this.internalFormat = GL30.GL_RGB16F;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_HALF_FLOAT;
			else if (this.type != GL30.GL_HALF_FLOAT && this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB32F:
			this.internalFormat = GL30.GL_RGB32F;
			this.format = GL30.GL_RGB;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT;
			else if (this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB8UI:
			this.internalFormat = GL30.GL_RGB8UI;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB8I:
			this.internalFormat = GL30.GL_RGB8I;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB16UI:
			this.internalFormat = GL30.GL_RGB16UI;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_SHORT;
			else if (this.type != GL30.GL_UNSIGNED_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB16I:
			this.internalFormat = GL30.GL_RGB16I;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_SHORT;
			else if (this.type != GL30.GL_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB32UI:
			this.internalFormat = GL30.GL_RGB32UI;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT;
			else if (this.type != GL30.GL_UNSIGNED_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB32I:
			this.internalFormat = GL30.GL_RGB32I;
			this.format = GL30.GL_RGB_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_INT;
			else if (this.type != GL30.GL_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA8:
			this.internalFormat = GL30.GL_RGBA8;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_SRGB8_ALPHA8:
			this.internalFormat = GL30.GL_SRGB8_ALPHA8;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA8_SNORM:
			this.internalFormat = GL30.GL_RGBA8_SNORM;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB5_A1:
			this.internalFormat = GL30.GL_RGB5_A1;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE && this.type != GL30.GL_UNSIGNED_SHORT_5_5_5_1
				&& this.type != GL30.GL_UNSIGNED_INT_2_10_10_10_REV) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA4:
			this.internalFormat = GL30.GL_RGBA4;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE && this.type != GL30.GL_UNSIGNED_SHORT_4_4_4_4)
				throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB10_A2:
			this.internalFormat = GL30.GL_RGB10_A2;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT_2_10_10_10_REV;
			else if (this.type != GL30.GL_UNSIGNED_INT_2_10_10_10_REV) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA16F:
			this.internalFormat = GL30.GL_RGBA16F;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_HALF_FLOAT;
			else if (this.type != GL30.GL_HALF_FLOAT && this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA32F:
			this.internalFormat = GL30.GL_RGBA32F;
			this.format = GL30.GL_RGBA;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT;
			else if (this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA8UI:
			this.internalFormat = GL30.GL_RGBA8UI;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_BYTE;
			else if (this.type != GL30.GL_UNSIGNED_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA8I:
			this.internalFormat = GL30.GL_RGBA8I;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_BYTE;
			else if (this.type != GL30.GL_BYTE) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGB10_A2UI:
			this.internalFormat = GL30.GL_RGB10_A2UI;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT_2_10_10_10_REV;
			else if (this.type != GL30.GL_UNSIGNED_INT_2_10_10_10_REV) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA16UI:
			this.internalFormat = GL30.GL_RGBA16UI;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_SHORT;
			else if (this.type != GL30.GL_UNSIGNED_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA16I:
			this.internalFormat = GL30.GL_RGBA16I;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_SHORT;
			else if (this.type != GL30.GL_SHORT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA32I:
			this.internalFormat = GL30.GL_RGBA32I;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_INT;
			else if (this.type != GL30.GL_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_RGBA32UI:
			this.internalFormat = GL30.GL_RGBA32UI;
			this.format = GL30.GL_RGBA_INTEGER;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT;
			else if (this.type != GL30.GL_UNSIGNED_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_DEPTH_COMPONENT16:
			this.internalFormat = GL30.GL_DEPTH_COMPONENT16;
			this.format = GL30.GL_DEPTH_COMPONENT;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_SHORT;
			else if (this.type != GL30.GL_UNSIGNED_SHORT && this.type != GL30.GL_UNSIGNED_INT)
				throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_DEPTH_COMPONENT24:
			this.internalFormat = GL30.GL_DEPTH_COMPONENT24;
			this.format = GL30.GL_DEPTH_COMPONENT;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT;
			else if (this.type != GL30.GL_UNSIGNED_INT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_DEPTH_COMPONENT32F:
			this.internalFormat = GL30.GL_DEPTH_COMPONENT32F;
			this.format = GL30.GL_DEPTH_COMPONENT;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT;
			else if (this.type != GL30.GL_FLOAT) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_DEPTH24_STENCIL8:
			this.internalFormat = GL30.GL_DEPTH24_STENCIL8;
			this.format = GL30.GL_DEPTH_STENCIL;
			if (this.type == -1)
				this.type = GL30.GL_UNSIGNED_INT_24_8;
			else if (this.type != GL30.GL_UNSIGNED_INT_24_8) throw new GdxRuntimeException(incompatibleMsg);
			break;
		case GL_DEPTH32F_STENCIL8:
			this.internalFormat = GL30.GL_DEPTH32F_STENCIL8;
			this.format = GL30.GL_DEPTH_STENCIL;
			if (this.type == -1)
				this.type = GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
			else if (this.type != GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV) throw new GdxRuntimeException(incompatibleMsg);
			break;
		}
	}

	public int getInternalFormat () {
		return internalFormat;
	}

	public int getFormat () {
		return format;
	}

	public int getType () {
		return type;
	}

	public GLFormat getEnumFormat () {
		switch (format) {
		case GL30.GL_RGB:
			return GLFormat.GL_RGB;
		case GL30.GL_RGBA:
			return GLFormat.GL_RGBA;
		case GL30.GL_LUMINANCE_ALPHA:
			return GLFormat.GL_LUMINANCE_ALPHA;
		case GL30.GL_LUMINANCE:
			return GLFormat.GL_LUMINANCE;
		case GL30.GL_ALPHA:
			return GLFormat.GL_ALPHA;
		case GL30.GL_RED:
			return GLFormat.GL_RED;
		case GL30.GL_RED_INTEGER:
			return GLFormat.GL_RED_INTEGER;
		case GL30.GL_RG:
			return GLFormat.GL_RG;
		case GL30.GL_RG_INTEGER:
			return GLFormat.GL_RG_INTEGER;
		case GL30.GL_RGB_INTEGER:
			return GLFormat.GL_RGB_INTEGER;
		case GL30.GL_RGBA_INTEGER:
			return GLFormat.GL_RGBA_INTEGER;
		case GL30.GL_DEPTH_COMPONENT:
			return GLFormat.GL_DEPTH_COMPONENT;
		case GL30.GL_DEPTH_STENCIL:
			return GLFormat.GL_DEPTH_STENCIL;
		default:
			throw new GdxRuntimeException("Stored Format is invalid.");
		}
	}

	public GLType getEnumType () {
		switch (format) {
		case GL30.GL_UNSIGNED_BYTE:
			return GLType.GL_UNSIGNED_BYTE;
		case GL30.GL_UNSIGNED_SHORT_5_6_5:
			return GLType.GL_UNSIGNED_SHORT_5_6_5;
		case GL30.GL_UNSIGNED_SHORT_4_4_4_4:
			return GLType.GL_UNSIGNED_SHORT_4_4_4_4;
		case GL30.GL_UNSIGNED_SHORT_5_5_5_1:
			return GLType.GL_UNSIGNED_SHORT_5_5_5_1;
		case GL30.GL_BYTE:
			return GLType.GL_BYTE;
		case GL30.GL_HALF_FLOAT:
			return GLType.GL_HALF_FLOAT;
		case GL30.GL_FLOAT:
			return GLType.GL_FLOAT;
		case GL30.GL_UNSIGNED_SHORT:
			return GLType.GL_UNSIGNED_SHORT;
		case GL30.GL_SHORT:
			return GLType.GL_SHORT;
		case GL30.GL_UNSIGNED_INT:
			return GLType.GL_UNSIGNED_INT;
		case GL30.GL_INT:
			return GLType.GL_INT;
		case GL30.GL_UNSIGNED_INT_10F_11F_11F_REV:
			return GLType.GL_UNSIGNED_INT_10F_11F_11F_REV;
		case GL30.GL_UNSIGNED_INT_5_9_9_9_REV:
			return GLType.GL_UNSIGNED_INT_5_9_9_9_REV;
		case GL30.GL_UNSIGNED_INT_2_10_10_10_REV:
			return GLType.GL_UNSIGNED_INT_2_10_10_10_REV;
		case GL30.GL_UNSIGNED_INT_24_8:
			return GLType.GL_UNSIGNED_INT_24_8;
		case GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV:
			return GLType.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
		default:
			throw new GdxRuntimeException("Stored Type is invalid.");
		}
	}

	public GLInternalFormat getEnumInternalFormat () {
		switch (internalFormat) {
		case GL30.GL_RGB:
			return GLInternalFormat.GL_RGB;
		case GL30.GL_RGBA:
			return GLInternalFormat.GL_RGBA;
		case GL30.GL_LUMINANCE_ALPHA:
			return GLInternalFormat.GL_LUMINANCE_ALPHA;
		case GL30.GL_LUMINANCE:
			return GLInternalFormat.GL_LUMINANCE;
		case GL30.GL_ALPHA:
			return GLInternalFormat.GL_ALPHA;
		case GL30.GL_R8:
			return GLInternalFormat.GL_R8;
		case GL30.GL_R8_SNORM:
			return GLInternalFormat.GL_R8_SNORM;
		case GL30.GL_R16F:
			return GLInternalFormat.GL_R16F;
		case GL30.GL_R32F:
			return GLInternalFormat.GL_R32F;
		case GL30.GL_R8UI:
			return GLInternalFormat.GL_R8UI;
		case GL30.GL_R8I:
			return GLInternalFormat.GL_R8I;
		case GL30.GL_R16UI:
			return GLInternalFormat.GL_R16UI;
		case GL30.GL_R16I:
			return GLInternalFormat.GL_R16I;
		case GL30.GL_R32UI:
			return GLInternalFormat.GL_R32UI;
		case GL30.GL_R32I:
			return GLInternalFormat.GL_R32I;
		case GL30.GL_RG8:
			return GLInternalFormat.GL_RG8;
		case GL30.GL_RG8_SNORM:
			return GLInternalFormat.GL_RG8_SNORM;
		case GL30.GL_RG16F:
			return GLInternalFormat.GL_RG16F;
		case GL30.GL_RG32F:
			return GLInternalFormat.GL_RG32F;
		case GL30.GL_RG8UI:
			return GLInternalFormat.GL_RG8UI;
		case GL30.GL_RG8I:
			return GLInternalFormat.GL_RG8I;
		case GL30.GL_RG16UI:
			return GLInternalFormat.GL_RG16UI;
		case GL30.GL_RG16I:
			return GLInternalFormat.GL_RG16I;
		case GL30.GL_RG32UI:
			return GLInternalFormat.GL_RG32UI;
		case GL30.GL_RG32I:
			return GLInternalFormat.GL_RG32I;
		case GL30.GL_RGB8:
			return GLInternalFormat.GL_RGB8;
		case GL30.GL_SRGB8:
			return GLInternalFormat.GL_SRGB8;
		case GL30.GL_RGB565:
			return GLInternalFormat.GL_RGB565;
		case GL30.GL_RGB8_SNORM:
			return GLInternalFormat.GL_RGB8_SNORM;
		case GL30.GL_R11F_G11F_B10F:
			return GLInternalFormat.GL_R11F_G11F_B10F;
		case GL30.GL_RGB9_E5:
			return GLInternalFormat.GL_RGB9_E5;
		case GL30.GL_RGB16F:
			return GLInternalFormat.GL_RGB16F;
		case GL30.GL_RGB32F:
			return GLInternalFormat.GL_RGB32F;
		case GL30.GL_RGB8UI:
			return GLInternalFormat.GL_RGB8UI;
		case GL30.GL_RGB8I:
			return GLInternalFormat.GL_RGB8I;
		case GL30.GL_RGB16UI:
			return GLInternalFormat.GL_RGB16UI;
		case GL30.GL_RGB16I:
			return GLInternalFormat.GL_RGB16I;
		case GL30.GL_RGB32UI:
			return GLInternalFormat.GL_RGB32UI;
		case GL30.GL_RGB32I:
			return GLInternalFormat.GL_RGB32I;
		case GL30.GL_RGBA8:
			return GLInternalFormat.GL_RGBA8;
		case GL30.GL_SRGB8_ALPHA8:
			return GLInternalFormat.GL_SRGB8_ALPHA8;
		case GL30.GL_RGBA8_SNORM:
			return GLInternalFormat.GL_RGBA8_SNORM;
		case GL30.GL_RGB5_A1:
			return GLInternalFormat.GL_RGB5_A1;
		case GL30.GL_RGBA4:
			return GLInternalFormat.GL_RGBA4;
		case GL30.GL_RGB10_A2:
			return GLInternalFormat.GL_RGB10_A2;
		case GL30.GL_RGBA16F:
			return GLInternalFormat.GL_RGBA16F;
		case GL30.GL_RGBA32F:
			return GLInternalFormat.GL_RGBA32F;
		case GL30.GL_RGBA8UI:
			return GLInternalFormat.GL_RGBA8UI;
		case GL30.GL_RGBA8I:
			return GLInternalFormat.GL_RGBA8I;
		case GL30.GL_RGB10_A2UI:
			return GLInternalFormat.GL_RGB10_A2UI;
		case GL30.GL_RGBA16UI:
			return GLInternalFormat.GL_RGBA16UI;
		case GL30.GL_RGBA16I:
			return GLInternalFormat.GL_RGBA16I;
		case GL30.GL_RGBA32I:
			return GLInternalFormat.GL_RGBA32I;
		case GL30.GL_RGBA32UI:
			return GLInternalFormat.GL_RGBA32UI;
		case GL30.GL_DEPTH_COMPONENT16:
			return GLInternalFormat.GL_DEPTH_COMPONENT16;
		case GL30.GL_DEPTH_COMPONENT24:
			return GLInternalFormat.GL_DEPTH_COMPONENT24;
		case GL30.GL_DEPTH_COMPONENT32F:
			return GLInternalFormat.GL_DEPTH_COMPONENT32F;
		case GL30.GL_DEPTH24_STENCIL8:
			return GLInternalFormat.GL_DEPTH24_STENCIL8;
		case GL30.GL_DEPTH32F_STENCIL8:
			return GLInternalFormat.GL_DEPTH32F_STENCIL8;
		default:
			throw new GdxRuntimeException("Stored InternalFormat is invalid.");
		}
	}

	private void setType (GLInternalFormat internalFormat, GLType type) {
		switch (type) {
		case GL_UNSIGNED_BYTE:
			this.type = GL30.GL_UNSIGNED_BYTE;
			break;
		case GL_UNSIGNED_SHORT_5_6_5:
			this.type = GL30.GL_UNSIGNED_SHORT_5_6_5;
			break;
		case GL_UNSIGNED_SHORT_4_4_4_4:
			this.type = GL30.GL_UNSIGNED_SHORT_4_4_4_4;
			break;
		case GL_UNSIGNED_SHORT_5_5_5_1:
			this.type = GL30.GL_UNSIGNED_SHORT_5_5_5_1;
			break;
		case GL_BYTE:
			this.type = GL30.GL_BYTE;
			break;
		case GL_HALF_FLOAT:
			this.type = GL30.GL_HALF_FLOAT;
			break;
		case GL_FLOAT:
			this.type = GL30.GL_FLOAT;
			break;
		case GL_UNSIGNED_SHORT:
			this.type = GL30.GL_UNSIGNED_SHORT;
			break;
		case GL_SHORT:
			this.type = GL30.GL_SHORT;
			break;
		case GL_UNSIGNED_INT:
			this.type = GL30.GL_UNSIGNED_INT;
			break;
		case GL_INT:
			this.type = GL30.GL_INT;
			break;
		case GL_UNSIGNED_INT_10F_11F_11F_REV:
			this.type = GL30.GL_UNSIGNED_INT_10F_11F_11F_REV;
			break;
		case GL_UNSIGNED_INT_5_9_9_9_REV:
			this.type = GL30.GL_UNSIGNED_INT_5_9_9_9_REV;
			break;
		case GL_UNSIGNED_INT_2_10_10_10_REV:
			this.type = GL30.GL_UNSIGNED_INT_2_10_10_10_REV;
			break;
		case GL_UNSIGNED_INT_24_8:
			this.type = GL30.GL_UNSIGNED_INT_24_8;
			break;
		case GL_FLOAT_32_UNSIGNED_INT_24_8_REV:
			this.type = GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
			break;
		}
	}

	/** Used for easy copying. */
	private PixelFormatES3 (int manualInternalFormat, int manualType, int manualFormat) {
		format = manualFormat;
		type = manualType;
		internalFormat = manualInternalFormat;
	}

	public PixelFormatES3 copy () {
		return new PixelFormatES3(internalFormat, type, format);
	}

	@Override
	public boolean equals (Object other) {
		if (other instanceof PixelFormatES3) {
			PixelFormatES3 otherPF = (PixelFormatES3)other;
			return this.internalFormat == otherPF.internalFormat && this.format == otherPF.format && this.type == otherPF.type;
		} else
			return false;
	}
}
