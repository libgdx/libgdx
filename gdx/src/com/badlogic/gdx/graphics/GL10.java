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
import java.nio.FloatBuffer;

/** Interface wrapping all standard OpenGL ES 1.0 methods. Note that this excludes and fixed point OpenGL ES 1.0 methods!
 * 
 * @author mzechner */
public interface GL10 extends GLCommon {
	public static final int GL_OES_VERSION_1_0 = 1;
	public static final int GL_OES_read_format = 1;
	public static final int GL_OES_compressed_paletted_texture = 1;
	public static final int GL_DEPTH_BUFFER_BIT = 0x00000100;
	public static final int GL_STENCIL_BUFFER_BIT = 0x00000400;
	public static final int GL_COLOR_BUFFER_BIT = 0x00004000;
	public static final int GL_FALSE = 0;
	public static final int GL_TRUE = 1;
	public static final int GL_POINTS = 0x0000;
	public static final int GL_LINES = 0x0001;
	public static final int GL_LINE_LOOP = 0x0002;
	public static final int GL_LINE_STRIP = 0x0003;
	public static final int GL_TRIANGLES = 0x0004;
	public static final int GL_TRIANGLE_STRIP = 0x0005;
	public static final int GL_TRIANGLE_FAN = 0x0006;
	public static final int GL_NEVER = 0x0200;
	public static final int GL_LESS = 0x0201;
	public static final int GL_EQUAL = 0x0202;
	public static final int GL_LEQUAL = 0x0203;
	public static final int GL_GREATER = 0x0204;
	public static final int GL_NOTEQUAL = 0x0205;
	public static final int GL_GEQUAL = 0x0206;
	public static final int GL_ALWAYS = 0x0207;
	public static final int GL_ZERO = 0;
	public static final int GL_ONE = 1;
	public static final int GL_SRC_COLOR = 0x0300;
	public static final int GL_ONE_MINUS_SRC_COLOR = 0x0301;
	public static final int GL_SRC_ALPHA = 0x0302;
	public static final int GL_ONE_MINUS_SRC_ALPHA = 0x0303;
	public static final int GL_DST_ALPHA = 0x0304;
	public static final int GL_ONE_MINUS_DST_ALPHA = 0x0305;
	public static final int GL_DST_COLOR = 0x0306;
	public static final int GL_ONE_MINUS_DST_COLOR = 0x0307;
	public static final int GL_SRC_ALPHA_SATURATE = 0x0308;
	public static final int GL_FRONT = 0x0404;
	public static final int GL_BACK = 0x0405;
	public static final int GL_FRONT_AND_BACK = 0x0408;
	public static final int GL_FOG = 0x0B60;
	public static final int GL_LIGHTING = 0x0B50;
	public static final int GL_TEXTURE_2D = 0x0DE1;
	public static final int GL_CULL_FACE = 0x0B44;
	public static final int GL_ALPHA_TEST = 0x0BC0;
	public static final int GL_BLEND = 0x0BE2;
	public static final int GL_COLOR_LOGIC_OP = 0x0BF2;
	public static final int GL_DITHER = 0x0BD0;
	public static final int GL_STENCIL_TEST = 0x0B90;
	public static final int GL_DEPTH_TEST = 0x0B71;
	public static final int GL_POINT_SMOOTH = 0x0B10;
	public static final int GL_LINE_SMOOTH = 0x0B20;
	public static final int GL_SCISSOR_TEST = 0x0C11;
	public static final int GL_COLOR_MATERIAL = 0x0B57;
	public static final int GL_NORMALIZE = 0x0BA1;
	public static final int GL_RESCALE_NORMAL = 0x803A;
	public static final int GL_POLYGON_OFFSET_FILL = 0x8037;
	public static final int GL_VERTEX_ARRAY = 0x8074;
	public static final int GL_NORMAL_ARRAY = 0x8075;
	public static final int GL_COLOR_ARRAY = 0x8076;
	public static final int GL_TEXTURE_COORD_ARRAY = 0x8078;
	public static final int GL_MULTISAMPLE = 0x809D;
	public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
	public static final int GL_SAMPLE_ALPHA_TO_ONE = 0x809F;
	public static final int GL_SAMPLE_COVERAGE = 0x80A0;
	public static final int GL_NO_ERROR = 0;
	public static final int GL_INVALID_ENUM = 0x0500;
	public static final int GL_INVALID_VALUE = 0x0501;
	public static final int GL_INVALID_OPERATION = 0x0502;
	public static final int GL_STACK_OVERFLOW = 0x0503;
	public static final int GL_STACK_UNDERFLOW = 0x0504;
	public static final int GL_OUT_OF_MEMORY = 0x0505;
	public static final int GL_EXP = 0x0800;
	public static final int GL_EXP2 = 0x0801;
	public static final int GL_FOG_DENSITY = 0x0B62;
	public static final int GL_FOG_START = 0x0B63;
	public static final int GL_FOG_END = 0x0B64;
	public static final int GL_FOG_MODE = 0x0B65;
	public static final int GL_FOG_COLOR = 0x0B66;
	public static final int GL_CW = 0x0900;
	public static final int GL_CCW = 0x0901;
	public static final int GL_SMOOTH_POINT_SIZE_RANGE = 0x0B12;
	public static final int GL_SMOOTH_LINE_WIDTH_RANGE = 0x0B22;
	public static final int GL_ALIASED_POINT_SIZE_RANGE = 0x846D;
	public static final int GL_ALIASED_LINE_WIDTH_RANGE = 0x846E;
	public static final int GL_IMPLEMENTATION_COLOR_READ_TYPE_OES = 0x8B9A;
	public static final int GL_IMPLEMENTATION_COLOR_READ_FORMAT_OES = 0x8B9B;
	public static final int GL_MAX_LIGHTS = 0x0D31;
	public static final int GL_MAX_TEXTURE_SIZE = 0x0D33;
	public static final int GL_MAX_MODELVIEW_STACK_DEPTH = 0x0D36;
	public static final int GL_MAX_PROJECTION_STACK_DEPTH = 0x0D38;
	public static final int GL_MAX_TEXTURE_STACK_DEPTH = 0x0D39;
	public static final int GL_MAX_VIEWPORT_DIMS = 0x0D3A;
	public static final int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
	public static final int GL_MAX_ELEMENTS_INDICES = 0x80E9;
	public static final int GL_MAX_TEXTURE_UNITS = 0x84E2;
	public static final int GL_NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2;
	public static final int GL_COMPRESSED_TEXTURE_FORMATS = 0x86A3;
	public static final int GL_SUBPIXEL_BITS = 0x0D50;
	public static final int GL_RED_BITS = 0x0D52;
	public static final int GL_GREEN_BITS = 0x0D53;
	public static final int GL_BLUE_BITS = 0x0D54;
	public static final int GL_ALPHA_BITS = 0x0D55;
	public static final int GL_DEPTH_BITS = 0x0D56;
	public static final int GL_STENCIL_BITS = 0x0D57;
	public static final int GL_DONT_CARE = 0x1100;
	public static final int GL_FASTEST = 0x1101;
	public static final int GL_NICEST = 0x1102;
	public static final int GL_PERSPECTIVE_CORRECTION_HINT = 0x0C50;
	public static final int GL_POINT_SMOOTH_HINT = 0x0C51;
	public static final int GL_LINE_SMOOTH_HINT = 0x0C52;
	public static final int GL_POLYGON_SMOOTH_HINT = 0x0C53;
	public static final int GL_FOG_HINT = 0x0C54;
	public static final int GL_LIGHT_MODEL_AMBIENT = 0x0B53;
	public static final int GL_LIGHT_MODEL_TWO_SIDE = 0x0B52;
	public static final int GL_AMBIENT = 0x1200;
	public static final int GL_DIFFUSE = 0x1201;
	public static final int GL_SPECULAR = 0x1202;
	public static final int GL_POSITION = 0x1203;
	public static final int GL_SPOT_DIRECTION = 0x1204;
	public static final int GL_SPOT_EXPONENT = 0x1205;
	public static final int GL_SPOT_CUTOFF = 0x1206;
	public static final int GL_CONSTANT_ATTENUATION = 0x1207;
	public static final int GL_LINEAR_ATTENUATION = 0x1208;
	public static final int GL_QUADRATIC_ATTENUATION = 0x1209;
	public static final int GL_BYTE = 0x1400;
	public static final int GL_UNSIGNED_BYTE = 0x1401;
	public static final int GL_SHORT = 0x1402;
	public static final int GL_UNSIGNED_SHORT = 0x1403;
	public static final int GL_FLOAT = 0x1406;
	public static final int GL_CLEAR = 0x1500;
	public static final int GL_AND = 0x1501;
	public static final int GL_AND_REVERSE = 0x1502;
	public static final int GL_COPY = 0x1503;
	public static final int GL_AND_INVERTED = 0x1504;
	public static final int GL_NOOP = 0x1505;
	public static final int GL_XOR = 0x1506;
	public static final int GL_OR = 0x1507;
	public static final int GL_NOR = 0x1508;
	public static final int GL_EQUIV = 0x1509;
	public static final int GL_INVERT = 0x150A;
	public static final int GL_OR_REVERSE = 0x150B;
	public static final int GL_COPY_INVERTED = 0x150C;
	public static final int GL_OR_INVERTED = 0x150D;
	public static final int GL_NAND = 0x150E;
	public static final int GL_SET = 0x150F;
	public static final int GL_EMISSION = 0x1600;
	public static final int GL_SHININESS = 0x1601;
	public static final int GL_AMBIENT_AND_DIFFUSE = 0x1602;
	public static final int GL_MODELVIEW = 0x1700;
	public static final int GL_PROJECTION = 0x1701;
	public static final int GL_TEXTURE = 0x1702;
	public static final int GL_ALPHA = 0x1906;
	public static final int GL_RGB = 0x1907;
	public static final int GL_RGBA = 0x1908;
	public static final int GL_LUMINANCE = 0x1909;
	public static final int GL_LUMINANCE_ALPHA = 0x190A;
	public static final int GL_UNPACK_ALIGNMENT = 0x0CF5;
	public static final int GL_PACK_ALIGNMENT = 0x0D05;
	public static final int GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033;
	public static final int GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034;
	public static final int GL_UNSIGNED_SHORT_5_6_5 = 0x8363;
	public static final int GL_FLAT = 0x1D00;
	public static final int GL_SMOOTH = 0x1D01;
	public static final int GL_KEEP = 0x1E00;
	public static final int GL_REPLACE = 0x1E01;
	public static final int GL_INCR = 0x1E02;
	public static final int GL_DECR = 0x1E03;
	public static final int GL_VENDOR = 0x1F00;
	public static final int GL_RENDERER = 0x1F01;
	public static final int GL_VERSION = 0x1F02;
	public static final int GL_EXTENSIONS = 0x1F03;
	public static final int GL_MODULATE = 0x2100;
	public static final int GL_DECAL = 0x2101;
	public static final int GL_ADD = 0x0104;
	public static final int GL_TEXTURE_ENV_MODE = 0x2200;
	public static final int GL_TEXTURE_ENV_COLOR = 0x2201;
	public static final int GL_TEXTURE_ENV = 0x2300;
	public static final int GL_NEAREST = 0x2600;
	public static final int GL_LINEAR = 0x2601;
	public static final int GL_NEAREST_MIPMAP_NEAREST = 0x2700;
	public static final int GL_LINEAR_MIPMAP_NEAREST = 0x2701;
	public static final int GL_NEAREST_MIPMAP_LINEAR = 0x2702;
	public static final int GL_LINEAR_MIPMAP_LINEAR = 0x2703;
	public static final int GL_TEXTURE_MAG_FILTER = 0x2800;
	public static final int GL_TEXTURE_MIN_FILTER = 0x2801;
	public static final int GL_TEXTURE_WRAP_S = 0x2802;
	public static final int GL_TEXTURE_WRAP_T = 0x2803;
	public static final int GL_TEXTURE0 = 0x84C0;
	public static final int GL_TEXTURE1 = 0x84C1;
	public static final int GL_TEXTURE2 = 0x84C2;
	public static final int GL_TEXTURE3 = 0x84C3;
	public static final int GL_TEXTURE4 = 0x84C4;
	public static final int GL_TEXTURE5 = 0x84C5;
	public static final int GL_TEXTURE6 = 0x84C6;
	public static final int GL_TEXTURE7 = 0x84C7;
	public static final int GL_TEXTURE8 = 0x84C8;
	public static final int GL_TEXTURE9 = 0x84C9;
	public static final int GL_TEXTURE10 = 0x84CA;
	public static final int GL_TEXTURE11 = 0x84CB;
	public static final int GL_TEXTURE12 = 0x84CC;
	public static final int GL_TEXTURE13 = 0x84CD;
	public static final int GL_TEXTURE14 = 0x84CE;
	public static final int GL_TEXTURE15 = 0x84CF;
	public static final int GL_TEXTURE16 = 0x84D0;
	public static final int GL_TEXTURE17 = 0x84D1;
	public static final int GL_TEXTURE18 = 0x84D2;
	public static final int GL_TEXTURE19 = 0x84D3;
	public static final int GL_TEXTURE20 = 0x84D4;
	public static final int GL_TEXTURE21 = 0x84D5;
	public static final int GL_TEXTURE22 = 0x84D6;
	public static final int GL_TEXTURE23 = 0x84D7;
	public static final int GL_TEXTURE24 = 0x84D8;
	public static final int GL_TEXTURE25 = 0x84D9;
	public static final int GL_TEXTURE26 = 0x84DA;
	public static final int GL_TEXTURE27 = 0x84DB;
	public static final int GL_TEXTURE28 = 0x84DC;
	public static final int GL_TEXTURE29 = 0x84DD;
	public static final int GL_TEXTURE30 = 0x84DE;
	public static final int GL_TEXTURE31 = 0x84DF;
	public static final int GL_REPEAT = 0x2901;
	public static final int GL_CLAMP_TO_EDGE = 0x812F;
	public static final int GL_PALETTE4_RGB8_OES = 0x8B90;
	public static final int GL_PALETTE4_RGBA8_OES = 0x8B91;
	public static final int GL_PALETTE4_R5_G6_B5_OES = 0x8B92;
	public static final int GL_PALETTE4_RGBA4_OES = 0x8B93;
	public static final int GL_PALETTE4_RGB5_A1_OES = 0x8B94;
	public static final int GL_PALETTE8_RGB8_OES = 0x8B95;
	public static final int GL_PALETTE8_RGBA8_OES = 0x8B96;
	public static final int GL_PALETTE8_R5_G6_B5_OES = 0x8B97;
	public static final int GL_PALETTE8_RGBA4_OES = 0x8B98;
	public static final int GL_PALETTE8_RGB5_A1_OES = 0x8B99;
	public static final int GL_LIGHT0 = 0x4000;
	public static final int GL_LIGHT1 = 0x4001;
	public static final int GL_LIGHT2 = 0x4002;
	public static final int GL_LIGHT3 = 0x4003;
	public static final int GL_LIGHT4 = 0x4004;
	public static final int GL_LIGHT5 = 0x4005;
	public static final int GL_LIGHT6 = 0x4006;
	public static final int GL_LIGHT7 = 0x4007;

	public static final int GL_POINT = 0x1B00;
	public static final int GL_LINE = 0x1B01;
	public static final int GL_FILL = 0x1B02;

	public void glAlphaFunc (int func, float ref);

	public void glClientActiveTexture (int texture);

	public void glColor4f (float red, float green, float blue, float alpha);

	public void glColorPointer (int size, int type, int stride, Buffer pointer);

	public void glDeleteTextures (int n, int[] textures, int offset);

	public void glDisableClientState (int array);

	/**
	 * glEnableClientState — enable or disable client-side capability
	 * @param array Specifies the capability to enable. Symbolic constants GL_COLOR_ARRAY, GL_EDGE_FLAG_ARRAY, GL_FOG_COORD_ARRAY, GL_INDEX_ARRAY, GL_NORMAL_ARRAY, GL_SECONDARY_COLOR_ARRAY, GL_TEXTURE_COORD_ARRAY, and GL_VERTEX_ARRAY are accepted.
	 */
	public void glEnableClientState (int array);

	public void glFogf (int pname, float param);

	public void glFogfv (int pname, float[] params, int offset);

	public void glFogfv (int pname, FloatBuffer params);

	/**
	 * glFrustum — multiply the current matrix by a perspective matrix
	 * @param left Specify the coordinates for the left and right vertical clipping planes.
	 * @param right Specify the coordinates for the left and right vertical clipping planes.
	 * @param bottom Specify the coordinates for the bottom and top horizontal clipping planes.
	 * @param top Specify the coordinates for the bottom and top horizontal clipping planes.
	 * @param zNear Specify the distances to the near and far depth clipping planes. Both distances must be positive.
	 * @param zFar Specify the distances to the near and far depth clipping planes. Both distances must be positive.
	 */
	public void glFrustumf (float left, float right, float bottom, float top, float zNear, float zFar);

	public void glGenTextures (int n, int[] textures, int offset);

	public void glGetIntegerv (int pname, int[] params, int offset);

	public void glLightModelf (int pname, float param);

	public void glLightModelfv (int pname, float[] params, int offset);

	public void glLightModelfv (int pname, FloatBuffer params);

	/**
	 * Set light source parameters
	 * @param light Specifies a light. The number of lights depends on the implementation, but at least eight lights are supported. They are identified by symbolic names of the form GL_LIGHT i, where i ranges from 0 to the value of GL_MAX_LIGHTS - 1.
	 * @param pname Specifies a single-valued light source parameter for light. GL_SPOT_EXPONENT, GL_SPOT_CUTOFF, GL_CONSTANT_ATTENUATION, GL_LINEAR_ATTENUATION, and GL_QUADRATIC_ATTENUATION are accepted.
	 * @param param Specifies the value that parameter pname of light source light will be set to.
	 */
	public void glLightf (int light, int pname, float param);

	/**
	 * Set light source parameters
	 * @param light Specifies a light. The number of lights depends on the implementation, but at least eight lights are supported. They are identified by symbolic names of the form GL_LIGHT i, where i ranges from 0 to the value of GL_MAX_LIGHTS - 1
	 * @param pname Specifies a light source parameter for light. GL_AMBIENT, GL_DIFFUSE, GL_SPECULAR, GL_POSITION, GL_SPOT_CUTOFF, GL_SPOT_DIRECTION, GL_SPOT_EXPONENT, GL_CONSTANT_ATTENUATION, GL_LINEAR_ATTENUATION, and GL_QUADRATIC_ATTENUATION are accepted
	 * @param params Specifies a pointer to the value or values that parameter pname of light source light will be set to.
	 * @param offset 
	 */
	public void glLightfv (int light, int pname, float[] params, int offset);

	public void glLightfv (int light, int pname, FloatBuffer params);

	public void glLoadIdentity ();

	public void glLoadMatrixf (float[] m, int offset);

	public void glLoadMatrixf (FloatBuffer m);

	public void glLogicOp (int opcode);

	/**
	 * The glMaterialf function specifies material parameters for the lighting model.<p>
	 * GL_AMBIENT<p>
	 * params contains four integer or floating-point values that specify the ambient RGBA reflectance of the material. Integer values are mapped linearly such that the most positive representable value maps to 1.0, and the most negative representable value maps to -1.0 . Floating-point values are mapped directly. Neither integer nor floating-point values are clamped. The initial ambient reflectance for both front- and back-facing materials is (0.2, 0.2, 0.2, 1.0).<p>
	 * GL_DIFFUSE<p>
	 * params contains four integer or floating-point values that specify the diffuse RGBA reflectance of the material. Integer values are mapped linearly such that the most positive representable value maps to 1.0, and the most negative representable value maps to -1.0 . Floating-point values are mapped directly. Neither integer nor floating-point values are clamped. The initial diffuse reflectance for both front- and back-facing materials is (0.8, 0.8, 0.8, 1.0).<p>
	 * GL_SPECULAR<p>
	 * params contains four integer or floating-point values that specify the specular RGBA reflectance of the material. Integer values are mapped linearly such that the most positive representable value maps to 1.0, and the most negative representable value maps to -1.0 . Floating-point values are mapped directly. Neither integer nor floating-point values are clamped. The initial specular reflectance for both front- and back-facing materials is (0, 0, 0, 1).<p>
	 * GL_EMISSION<p>
	 * params contains four integer or floating-point values that specify the RGBA emitted light intensity of the material. Integer values are mapped linearly such that the most positive representable value maps to 1.0, and the most negative representable value maps to -1.0 . Floating-point values are mapped directly. Neither integer nor floating-point values are clamped. The initial emission intensity for both front- and back-facing materials is (0, 0, 0, 1).<p>
	 * GL_SHININESS<p>
	 * params is a single integer or floating-point value that specifies the RGBA specular exponent of the material. Integer and floating-point values are mapped directly. Only values in the range 0 128 are accepted. The initial specular exponent for both front- and back-facing materials is 0.<p>
	 * GL_AMBIENT_AND_DIFFUSE<p>
	 * Equivalent to calling glMaterial twice with the same parameter values, once with GL_AMBIENT and once with GL_DIFFUSE.<p>
	 * GL_COLOR_INDEXES<p>
	 * params contains three integer or floating-point values specifying the color indices for ambient, diffuse, and specular lighting. These three values, and GL_SHININESS, are the only material values used by the color index mode lighting equation. Refer to the glLightModel reference page for a discussion of color index lighting.<p>
	 * @param face The face or faces that are being updated. Must be one of the following: GL_FRONT, GL_BACK, or GL_FRONT and GL_BACK.
	 * @param pname Specifies the material parameter of the face or faces that is being updated. Must be one of GL_AMBIENT, GL_DIFFUSE, GL_SPECULAR, GL_EMISSION, GL_SHININESS, GL_AMBIENT_AND_DIFFUSE, or GL_COLOR_INDEXES.
	 * @param param The value to which parameter GL_SHININESS will be set.
	 */
	public void glMaterialf (int face, int pname, float param);

	public void glMaterialfv (int face, int pname, float[] params, int offset);

	public void glMaterialfv (int face, int pname, FloatBuffer params);

	public void glMatrixMode (int mode);

	public void glMultMatrixf (float[] m, int offset);

	public void glMultMatrixf (FloatBuffer m);

	public void glMultiTexCoord4f (int target, float s, float t, float r, float q);

	public void glNormal3f (float nx, float ny, float nz);

	public void glNormalPointer (int type, int stride, Buffer pointer);

	public void glOrthof (float left, float right, float bottom, float top, float zNear, float zFar);

	public void glPointSize (float size);

	public void glPopMatrix ();

	public void glPushMatrix ();

	public void glRotatef (float angle, float x, float y, float z);

	public void glSampleCoverage (float value, boolean invert);

	public void glScalef (float x, float y, float z);

	/**
	 * Select flat or smooth shading <p>
	 *  <b>Smooth shading </b>, the default, causes the computed colors of vertices to be interpolated as the primitive is rasterized, typically assigning different colors to each resulting pixel fragment.<p> 
	 *  <b>Flat shading</b> selects the computed color of just one vertex and assigns it to all the pixel fragments generated by rasterizing a single primitive
	 * @param mode Specifies a symbolic value representing a shading technique. Accepted values are GL_FLAT and GL_SMOOTH. The initial value is GL_SMOOTH.
	 */
	public void glShadeModel (int mode);

	public void glTexCoordPointer (int size, int type, int stride, Buffer pointer);

	/**
	 * glTexEnv — set texture environment parameters
	 * @param target Specifies a texture environment. May be GL_TEXTURE_ENV, GL_TEXTURE_FILTER_CONTROL or GL_POINT_SPRITE.
	 * @param pname Specifies the symbolic name of a single-valued texture environment parameter. May be either GL_TEXTURE_ENV_MODE, GL_TEXTURE_LOD_BIAS, GL_COMBINE_RGB, GL_COMBINE_ALPHA, GL_SRC0_RGB, GL_SRC1_RGB, GL_SRC2_RGB, GL_SRC0_ALPHA, GL_SRC1_ALPHA, GL_SRC2_ALPHA, GL_OPERAND0_RGB, GL_OPERAND1_RGB, GL_OPERAND2_RGB, GL_OPERAND0_ALPHA, GL_OPERAND1_ALPHA, GL_OPERAND2_ALPHA, GL_RGB_SCALE, GL_ALPHA_SCALE, or GL_COORD_REPLACE.
	 * @param param Specifies a single symbolic constant, one of GL_ADD, GL_ADD_SIGNED, GL_INTERPOLATE, GL_MODULATE, GL_DECAL, GL_BLEND, GL_REPLACE, GL_SUBTRACT, GL_COMBINE, GL_TEXTURE, GL_CONSTANT, GL_PRIMARY_COLOR, GL_PREVIOUS, GL_SRC_COLOR, GL_ONE_MINUS_SRC_COLOR, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, a single boolean value for the point sprite texture coordinate replacement, a single floating-point value for the texture level-of-detail bias, or 1.0, 2.0, or 4.0 when specifying the GL_RGB_SCALE or GL_ALPHA_SCALE.
	 */
	public void glTexEnvf (int target, int pname, float param);

	/**
	 * glTexEnv — set texture environment parameters
	 * @param target target Specifies a texture environment. May be either GL_TEXTURE_ENV, or GL_TEXTURE_FILTER_CONTROL.
	 * @param pname pname Specifies the symbolic name of a texture environment parameter. Accepted values are GL_TEXTURE_ENV_MODE, GL_TEXTURE_ENV_COLOR, or GL_TEXTURE_LOD_BIAS.
	 * @param params Specifies a pointer to a parameter array that contains either a single symbolic constant, single floating-point number, or an RGBA color.
	 * @param offset
	 */
	public void glTexEnvfv (int target, int pname, float[] params, int offset);

	/**
	 * glTexEnv — set texture environment parameters
	 * @param target Specifies a texture environment. May be either GL_TEXTURE_ENV, or GL_TEXTURE_FILTER_CONTROL.
	 * @param pname Specifies the symbolic name of a texture environment parameter. Accepted values are GL_TEXTURE_ENV_MODE, GL_TEXTURE_ENV_COLOR, or GL_TEXTURE_LOD_BIAS.
	 * @param params Specifies a pointer to a parameter array that contains either a single symbolic constant, single floating-point number, or an RGBA color.
	 */
	public void glTexEnvfv (int target, int pname, FloatBuffer params);

	public void glTranslatef (float x, float y, float z);

	public void glVertexPointer (int size, int type, int stride, Buffer pointer);

	public void glPolygonMode (int face, int mode);
}
