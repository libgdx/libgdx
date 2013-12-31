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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** Interface wrapping some functions of OpenGL ES 3.0
 * @author mattijs driel */
public interface GL30 extends GL20 {
	public static final int GL_READ_BUFFER = 0x0C02;
	public static final int GL_UNPACK_ROW_LENGTH = 0x0CF2;
	public static final int GL_UNPACK_SKIP_ROWS = 0x0CF3;
	public static final int GL_UNPACK_SKIP_PIXELS = 0x0CF4;
	public static final int GL_PACK_ROW_LENGTH = 0x0D02;
	public static final int GL_PACK_SKIP_ROWS = 0x0D03;
	public static final int GL_PACK_SKIP_PIXELS = 0x0D04;
	public static final int GL_COLOR = 0x1800;
	public static final int GL_DEPTH = 0x1801;
	public static final int GL_STENCIL = 0x1802;
	public static final int GL_RED = 0x1903;
	public static final int GL_RGB8 = 0x8051;
	public static final int GL_RGBA8 = 0x8058;
	public static final int GL_RGB10_A2 = 0x8059;
	public static final int GL_TEXTURE_BINDING_3D = 0x806A;
	public static final int GL_UNPACK_SKIP_IMAGES = 0x806D;
	public static final int GL_UNPACK_IMAGE_HEIGHT = 0x806E;
	public static final int GL_TEXTURE_3D = 0x806F;
	public static final int GL_TEXTURE_WRAP_R = 0x8072;
	public static final int GL_MAX_3D_TEXTURE_SIZE = 0x8073;
	public static final int GL_UNSIGNED_INT_2_10_10_10_REV = 0x8368;
	public static final int GL_MAX_ELEMENTS_VERTICES = 0x80E8;
	public static final int GL_MAX_ELEMENTS_INDICES = 0x80E9;
	public static final int GL_TEXTURE_MIN_LOD = 0x813A;
	public static final int GL_TEXTURE_MAX_LOD = 0x813B;
	public static final int GL_TEXTURE_BASE_LEVEL = 0x813C;
	public static final int GL_TEXTURE_MAX_LEVEL = 0x813D;
	public static final int GL_MIN = 0x8007;
	public static final int GL_MAX = 0x8008;
	public static final int GL_DEPTH_COMPONENT24 = 0x81A6;
	public static final int GL_MAX_TEXTURE_LOD_BIAS = 0x84FD;
	public static final int GL_TEXTURE_COMPARE_MODE = 0x884C;
	public static final int GL_TEXTURE_COMPARE_FUNC = 0x884D;
	public static final int GL_CURRENT_QUERY = 0x8865;
	public static final int GL_QUERY_RESULT = 0x8866;
	public static final int GL_QUERY_RESULT_AVAILABLE = 0x8867;
	public static final int GL_BUFFER_MAPPED = 0x88BC;
	public static final int GL_BUFFER_MAP_POINTER = 0x88BD;
	public static final int GL_STREAM_READ = 0x88E1;
	public static final int GL_STREAM_COPY = 0x88E2;
	public static final int GL_STATIC_READ = 0x88E5;
	public static final int GL_STATIC_COPY = 0x88E6;
	public static final int GL_DYNAMIC_READ = 0x88E9;
	public static final int GL_DYNAMIC_COPY = 0x88EA;
	public static final int GL_MAX_DRAW_BUFFERS = 0x8824;
	public static final int GL_DRAW_BUFFER0 = 0x8825;
	public static final int GL_DRAW_BUFFER1 = 0x8826;
	public static final int GL_DRAW_BUFFER2 = 0x8827;
	public static final int GL_DRAW_BUFFER3 = 0x8828;
	public static final int GL_DRAW_BUFFER4 = 0x8829;
	public static final int GL_DRAW_BUFFER5 = 0x882A;
	public static final int GL_DRAW_BUFFER6 = 0x882B;
	public static final int GL_DRAW_BUFFER7 = 0x882C;
	public static final int GL_DRAW_BUFFER8 = 0x882D;
	public static final int GL_DRAW_BUFFER9 = 0x882E;
	public static final int GL_DRAW_BUFFER10 = 0x882F;
	public static final int GL_DRAW_BUFFER11 = 0x8830;
	public static final int GL_DRAW_BUFFER12 = 0x8831;
	public static final int GL_DRAW_BUFFER13 = 0x8832;
	public static final int GL_DRAW_BUFFER14 = 0x8833;
	public static final int GL_DRAW_BUFFER15 = 0x8834;
	public static final int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 0x8B49;
	public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 0x8B4A;
	public static final int GL_SAMPLER_3D = 0x8B5F;
	public static final int GL_SAMPLER_2D_SHADOW = 0x8B62;
	public static final int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 0x8B8B;
	public static final int GL_PIXEL_PACK_BUFFER = 0x88EB;
	public static final int GL_PIXEL_UNPACK_BUFFER = 0x88EC;
	public static final int GL_PIXEL_PACK_BUFFER_BINDING = 0x88ED;
	public static final int GL_PIXEL_UNPACK_BUFFER_BINDING = 0x88EF;
	public static final int GL_FLOAT_MAT2x3 = 0x8B65;
	public static final int GL_FLOAT_MAT2x4 = 0x8B66;
	public static final int GL_FLOAT_MAT3x2 = 0x8B67;
	public static final int GL_FLOAT_MAT3x4 = 0x8B68;
	public static final int GL_FLOAT_MAT4x2 = 0x8B69;
	public static final int GL_FLOAT_MAT4x3 = 0x8B6A;
	public static final int GL_SRGB = 0x8C40;
	public static final int GL_SRGB8 = 0x8C41;
	public static final int GL_SRGB8_ALPHA8 = 0x8C43;
	public static final int GL_COMPARE_REF_TO_TEXTURE = 0x884E;
	public static final int GL_MAJOR_VERSION = 0x821B;
	public static final int GL_MINOR_VERSION = 0x821C;
	public static final int GL_NUM_EXTENSIONS = 0x821D;
	public static final int GL_RGBA32F = 0x8814;
	public static final int GL_RGB32F = 0x8815;
	public static final int GL_RGBA16F = 0x881A;
	public static final int GL_RGB16F = 0x881B;
	public static final int GL_VERTEX_ATTRIB_ARRAY_INTEGER = 0x88FD;
	public static final int GL_MAX_ARRAY_TEXTURE_LAYERS = 0x88FF;
	public static final int GL_MIN_PROGRAM_TEXEL_OFFSET = 0x8904;
	public static final int GL_MAX_PROGRAM_TEXEL_OFFSET = 0x8905;
	public static final int GL_MAX_VARYING_COMPONENTS = 0x8B4B;
	public static final int GL_TEXTURE_2D_ARRAY = 0x8C1A;
	public static final int GL_TEXTURE_BINDING_2D_ARRAY = 0x8C1D;
	public static final int GL_R11F_G11F_B10F = 0x8C3A;
	public static final int GL_UNSIGNED_INT_10F_11F_11F_REV = 0x8C3B;
	public static final int GL_RGB9_E5 = 0x8C3D;
	public static final int GL_UNSIGNED_INT_5_9_9_9_REV = 0x8C3E;
	public static final int GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH = 0x8C76;
	public static final int GL_TRANSFORM_FEEDBACK_BUFFER_MODE = 0x8C7F;
	public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS = 0x8C80;
	public static final int GL_TRANSFORM_FEEDBACK_VARYINGS = 0x8C83;
	public static final int GL_TRANSFORM_FEEDBACK_BUFFER_START = 0x8C84;
	public static final int GL_TRANSFORM_FEEDBACK_BUFFER_SIZE = 0x8C85;
	public static final int GL_TRANSFORM_FEEDBACK_PRIMITIVES_WRITTEN = 0x8C88;
	public static final int GL_RASTERIZER_DISCARD = 0x8C89;
	public static final int GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS = 0x8C8A;
	public static final int GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS = 0x8C8B;
	public static final int GL_INTERLEAVED_ATTRIBS = 0x8C8C;
	public static final int GL_SEPARATE_ATTRIBS = 0x8C8D;
	public static final int GL_TRANSFORM_FEEDBACK_BUFFER = 0x8C8E;
	public static final int GL_TRANSFORM_FEEDBACK_BUFFER_BINDING = 0x8C8F;
	public static final int GL_RGBA32UI = 0x8D70;
	public static final int GL_RGB32UI = 0x8D71;
	public static final int GL_RGBA16UI = 0x8D76;
	public static final int GL_RGB16UI = 0x8D77;
	public static final int GL_RGBA8UI = 0x8D7C;
	public static final int GL_RGB8UI = 0x8D7D;
	public static final int GL_RGBA32I = 0x8D82;
	public static final int GL_RGB32I = 0x8D83;
	public static final int GL_RGBA16I = 0x8D88;
	public static final int GL_RGB16I = 0x8D89;
	public static final int GL_RGBA8I = 0x8D8E;
	public static final int GL_RGB8I = 0x8D8F;
	public static final int GL_RED_INTEGER = 0x8D94;
	public static final int GL_RGB_INTEGER = 0x8D98;
	public static final int GL_RGBA_INTEGER = 0x8D99;
	public static final int GL_SAMPLER_2D_ARRAY = 0x8DC1;
	public static final int GL_SAMPLER_2D_ARRAY_SHADOW = 0x8DC4;
	public static final int GL_SAMPLER_CUBE_SHADOW = 0x8DC5;
	public static final int GL_UNSIGNED_INT_VEC2 = 0x8DC6;
	public static final int GL_UNSIGNED_INT_VEC3 = 0x8DC7;
	public static final int GL_UNSIGNED_INT_VEC4 = 0x8DC8;
	public static final int GL_INT_SAMPLER_2D = 0x8DCA;
	public static final int GL_INT_SAMPLER_3D = 0x8DCB;
	public static final int GL_INT_SAMPLER_CUBE = 0x8DCC;
	public static final int GL_INT_SAMPLER_2D_ARRAY = 0x8DCF;
	public static final int GL_UNSIGNED_INT_SAMPLER_2D = 0x8DD2;
	public static final int GL_UNSIGNED_INT_SAMPLER_3D = 0x8DD3;
	public static final int GL_UNSIGNED_INT_SAMPLER_CUBE = 0x8DD4;
	public static final int GL_UNSIGNED_INT_SAMPLER_2D_ARRAY = 0x8DD7;
	public static final int GL_BUFFER_ACCESS_FLAGS = 0x911F;
	public static final int GL_BUFFER_MAP_LENGTH = 0x9120;
	public static final int GL_BUFFER_MAP_OFFSET = 0x9121;
	public static final int GL_DEPTH_COMPONENT32F = 0x8CAC;
	public static final int GL_DEPTH32F_STENCIL8 = 0x8CAD;
	public static final int GL_FLOAT_32_UNSIGNED_INT_24_8_REV = 0x8DAD;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING = 0x8210;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE = 0x8211;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE = 0x8212;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE = 0x8213;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE = 0x8214;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE = 0x8215;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE = 0x8216;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE = 0x8217;
	public static final int GL_FRAMEBUFFER_DEFAULT = 0x8218;
	public static final int GL_FRAMEBUFFER_UNDEFINED = 0x8219;
	public static final int GL_DEPTH_STENCIL_ATTACHMENT = 0x821A;
	public static final int GL_DEPTH_STENCIL = 0x84F9;
	public static final int GL_UNSIGNED_INT_24_8 = 0x84FA;
	public static final int GL_DEPTH24_STENCIL8 = 0x88F0;
	public static final int GL_UNSIGNED_NORMALIZED = 0x8C17;
	public static final int GL_DRAW_FRAMEBUFFER_BINDING = 0x8CA6;
	public static final int GL_READ_FRAMEBUFFER = 0x8CA8;
	public static final int GL_DRAW_FRAMEBUFFER = 0x8CA9;
	public static final int GL_READ_FRAMEBUFFER_BINDING = 0x8CAA;
	public static final int GL_RENDERBUFFER_SAMPLES = 0x8CAB;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER = 0x8CD4;
	public static final int GL_MAX_COLOR_ATTACHMENTS = 0x8CDF;
	public static final int GL_COLOR_ATTACHMENT1 = 0x8CE1;
	public static final int GL_COLOR_ATTACHMENT2 = 0x8CE2;
	public static final int GL_COLOR_ATTACHMENT3 = 0x8CE3;
	public static final int GL_COLOR_ATTACHMENT4 = 0x8CE4;
	public static final int GL_COLOR_ATTACHMENT5 = 0x8CE5;
	public static final int GL_COLOR_ATTACHMENT6 = 0x8CE6;
	public static final int GL_COLOR_ATTACHMENT7 = 0x8CE7;
	public static final int GL_COLOR_ATTACHMENT8 = 0x8CE8;
	public static final int GL_COLOR_ATTACHMENT9 = 0x8CE9;
	public static final int GL_COLOR_ATTACHMENT10 = 0x8CEA;
	public static final int GL_COLOR_ATTACHMENT11 = 0x8CEB;
	public static final int GL_COLOR_ATTACHMENT12 = 0x8CEC;
	public static final int GL_COLOR_ATTACHMENT13 = 0x8CED;
	public static final int GL_COLOR_ATTACHMENT14 = 0x8CEE;
	public static final int GL_COLOR_ATTACHMENT15 = 0x8CEF;
	public static final int GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE = 0x8D56;
	public static final int GL_MAX_SAMPLES = 0x8D57;
	public static final int GL_HALF_FLOAT = 0x140B;
	public static final int GL_MAP_READ_BIT = 0x0001;
	public static final int GL_MAP_WRITE_BIT = 0x0002;
	public static final int GL_MAP_INVALIDATE_RANGE_BIT = 0x0004;
	public static final int GL_MAP_INVALIDATE_BUFFER_BIT = 0x0008;
	public static final int GL_MAP_FLUSH_EXPLICIT_BIT = 0x0010;
	public static final int GL_MAP_UNSYNCHRONIZED_BIT = 0x0020;
	public static final int GL_RG = 0x8227;
	public static final int GL_RG_INTEGER = 0x8228;
	public static final int GL_R8 = 0x8229;
	public static final int GL_RG8 = 0x822B;
	public static final int GL_R16F = 0x822D;
	public static final int GL_R32F = 0x822E;
	public static final int GL_RG16F = 0x822F;
	public static final int GL_RG32F = 0x8230;
	public static final int GL_R8I = 0x8231;
	public static final int GL_R8UI = 0x8232;
	public static final int GL_R16I = 0x8233;
	public static final int GL_R16UI = 0x8234;
	public static final int GL_R32I = 0x8235;
	public static final int GL_R32UI = 0x8236;
	public static final int GL_RG8I = 0x8237;
	public static final int GL_RG8UI = 0x8238;
	public static final int GL_RG16I = 0x8239;
	public static final int GL_RG16UI = 0x823A;
	public static final int GL_RG32I = 0x823B;
	public static final int GL_RG32UI = 0x823C;
	public static final int GL_VERTEX_ARRAY_BINDING = 0x85B5;
	public static final int GL_R8_SNORM = 0x8F94;
	public static final int GL_RG8_SNORM = 0x8F95;
	public static final int GL_RGB8_SNORM = 0x8F96;
	public static final int GL_RGBA8_SNORM = 0x8F97;
	public static final int GL_SIGNED_NORMALIZED = 0x8F9C;
	public static final int GL_PRIMITIVE_RESTART_FIXED_INDEX = 0x8D69;
	public static final int GL_COPY_READ_BUFFER = 0x8F36;
	public static final int GL_COPY_WRITE_BUFFER = 0x8F37;
	public static final int GL_COPY_READ_BUFFER_BINDING = 0x8F36;
	public static final int GL_COPY_WRITE_BUFFER_BINDING = 0x8F37;
	public static final int GL_UNIFORM_BUFFER = 0x8A11;
	public static final int GL_UNIFORM_BUFFER_BINDING = 0x8A28;
	public static final int GL_UNIFORM_BUFFER_START = 0x8A29;
	public static final int GL_UNIFORM_BUFFER_SIZE = 0x8A2A;
	public static final int GL_MAX_VERTEX_UNIFORM_BLOCKS = 0x8A2B;
	public static final int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = 0x8A2D;
	public static final int GL_MAX_COMBINED_UNIFORM_BLOCKS = 0x8A2E;
	public static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = 0x8A2F;
	public static final int GL_MAX_UNIFORM_BLOCK_SIZE = 0x8A30;
	public static final int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = 0x8A31;
	public static final int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = 0x8A33;
	public static final int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = 0x8A34;
	public static final int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = 0x8A35;
	public static final int GL_ACTIVE_UNIFORM_BLOCKS = 0x8A36;
	public static final int GL_UNIFORM_TYPE = 0x8A37;
	public static final int GL_UNIFORM_SIZE = 0x8A38;
	public static final int GL_UNIFORM_NAME_LENGTH = 0x8A39;
	public static final int GL_UNIFORM_BLOCK_INDEX = 0x8A3A;
	public static final int GL_UNIFORM_OFFSET = 0x8A3B;
	public static final int GL_UNIFORM_ARRAY_STRIDE = 0x8A3C;
	public static final int GL_UNIFORM_MATRIX_STRIDE = 0x8A3D;
	public static final int GL_UNIFORM_IS_ROW_MAJOR = 0x8A3E;
	public static final int GL_UNIFORM_BLOCK_BINDING = 0x8A3F;
	public static final int GL_UNIFORM_BLOCK_DATA_SIZE = 0x8A40;
	public static final int GL_UNIFORM_BLOCK_NAME_LENGTH = 0x8A41;
	public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = 0x8A42;
	public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = 0x8A43;
	public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = 0x8A44;
	public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = 0x8A46;
	public static final int GL_INVALID_INDEX = 0xFFFFFFFF;
	public static final int GL_MAX_VERTEX_OUTPUT_COMPONENTS = 0x9122;
	public static final int GL_MAX_FRAGMENT_INPUT_COMPONENTS = 0x9125;
	public static final int GL_MAX_SERVER_WAIT_TIMEOUT = 0x9111;
	public static final int GL_OBJECT_TYPE = 0x9112;
	public static final int GL_SYNC_CONDITION = 0x9113;
	public static final int GL_SYNC_STATUS = 0x9114;
	public static final int GL_SYNC_FLAGS = 0x9115;
	public static final int GL_SYNC_FENCE = 0x9116;
	public static final int GL_SYNC_GPU_COMMANDS_COMPLETE = 0x9117;
	public static final int GL_UNSIGNALED = 0x9118;
	public static final int GL_SIGNALED = 0x9119;
	public static final int GL_ALREADY_SIGNALED = 0x911A;
	public static final int GL_TIMEOUT_EXPIRED = 0x911B;
	public static final int GL_CONDITION_SATISFIED = 0x911C;
	public static final int GL_WAIT_FAILED = 0x911D;
	public static final int GL_SYNC_FLUSH_COMMANDS_BIT = 0x00000001;
	public static final long GL_TIMEOUT_IGNORED = -1; // 0xFFFFFFFFFFFFFFFFull
	public static final int GL_VERTEX_ATTRIB_ARRAY_DIVISOR = 0x88FE;
	public static final int GL_ANY_SAMPLES_PASSED = 0x8C2F;
	public static final int GL_ANY_SAMPLES_PASSED_CONSERVATIVE = 0x8D6A;
	public static final int GL_SAMPLER_BINDING = 0x8919;
	public static final int GL_RGB10_A2UI = 0x906F;
	public static final int GL_TEXTURE_SWIZZLE_R = 0x8E42;
	public static final int GL_TEXTURE_SWIZZLE_G = 0x8E43;
	public static final int GL_TEXTURE_SWIZZLE_B = 0x8E44;
	public static final int GL_TEXTURE_SWIZZLE_A = 0x8E45;
	public static final int GL_GREEN = 0x1904;
	public static final int GL_BLUE = 0x1905;
	public static final int GL_INT_2_10_10_10_REV = 0x8D9F;
	public static final int GL_TRANSFORM_FEEDBACK = 0x8E22;
	public static final int GL_TRANSFORM_FEEDBACK_PAUSED = 0x8E23;
	public static final int GL_TRANSFORM_FEEDBACK_ACTIVE = 0x8E24;
	public static final int GL_TRANSFORM_FEEDBACK_BINDING = 0x8E25;
	public static final int GL_PROGRAM_BINARY_RETRIEVABLE_HINT = 0x8257;
	public static final int GL_PROGRAM_BINARY_LENGTH = 0x8741;
	public static final int GL_NUM_PROGRAM_BINARY_FORMATS = 0x87FE;
	public static final int GL_PROGRAM_BINARY_FORMATS = 0x87FF;
	public static final int GL_COMPRESSED_R11_EAC = 0x9270;
	public static final int GL_COMPRESSED_SIGNED_R11_EAC = 0x9271;
	public static final int GL_COMPRESSED_RG11_EAC = 0x9272;
	public static final int GL_COMPRESSED_SIGNED_RG11_EAC = 0x9273;
	public static final int GL_COMPRESSED_RGB8_ETC2 = 0x9274;
	public static final int GL_COMPRESSED_SRGB8_ETC2 = 0x9275;
	public static final int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9276;
	public static final int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x9277;
	public static final int GL_COMPRESSED_RGBA8_ETC2_EAC = 0x9278;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = 0x9279;
	public static final int GL_TEXTURE_IMMUTABLE_FORMAT = 0x912F;
	public static final int GL_MAX_ELEMENT_INDEX = 0x8D6B;
	public static final int GL_NUM_SAMPLE_COUNTS = 0x9380;
	public static final int GL_TEXTURE_IMMUTABLE_LEVELS = 0x82DF;

	public void glBindBufferBase (int target, int index, int buffer);

	public void glDrawArraysInstanced (int mode, int first, int count, int instancecount);

	public void glDrawBuffers (int n, IntBuffer bufs);

	public void glDrawElementsInstanced (int mode, int count, int type, long indices, int instancecount);

	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params);

	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex);

	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params);

	public int glGetUniformBlockIndex (int program, String uniformBlockName);

	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding);

	// All functions below this comment might need an extra test

	public void glBeginQuery (int target, int id);

	public void glCopyTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width,
		int height);

	public void glCompressedTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border,
		int imageSize, ByteBuffer data);

	public void glCompressedTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height,
		int depth, int format, int imageSize, ByteBuffer data);

	public void glDeleteQueries (int n, IntBuffer ids);

	public void glDrawRangeElements (int mode, int start, int end, int count, int type, IntBuffer indices);

	public void glEndQuery (int target);

	public void glGenQueries (int n, IntBuffer ids);

	// deviates
	public ByteBuffer glGetBufferPointerv (int target, int pname);

	public void glGetQueryiv (int target, int pname, IntBuffer params);

	public void glGetQueryObjectuiv (int id, int pname, IntBuffer params);

	public boolean glIsQuery (int id);

	public void glReadBuffer (int mode);

	public void glTexImage3D (int target, int level, int internalformat, int width, int height, int depth, int border, int format,
		int type, Buffer pixels);

	public void glTexSubImage3D (int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth,
		int format, int type, Buffer pixels);

	public void glUniformMatrix2x3fv (int location, int count, boolean transpose, FloatBuffer value);

	public void glUniformMatrix3x2fv (int location, int count, boolean transpose, FloatBuffer value);

	public void glUniformMatrix2x4fv (int location, int count, boolean transpose, FloatBuffer value);

	public void glUniformMatrix4x2fv (int location, int count, boolean transpose, FloatBuffer value);

	public void glUniformMatrix3x4fv (int location, int count, boolean transpose, FloatBuffer value);

	public void glUniformMatrix4x3fv (int location, int count, boolean transpose, FloatBuffer value);

	public boolean glUnmapBuffer (int target);

	public void glBlitFramebuffer (int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1,
		int mask, int filter);

	public void glRenderbufferStorageMultisample (int target, int samples, int internalformat, int width, int height);

	public void glFramebufferTextureLayer (int target, int attachment, int texture, int level, int layer);

	/** FIXME: Deviates from spec: only provide a buffer returned by a previous call of this function, preferably of the same buffer
	 * object. This is Lwjgl's solution, might need to change for other backends. */
	public ByteBuffer glMapBufferRange (int target, long offset, long length, int access, ByteBuffer previousBuffer);

	public void glFlushMappedBufferRange (int target, long offset, long length);

	public void glBindVertexArray (int array);

	public void glDeleteVertexArrays (int n, IntBuffer arrays);

	public void glGenVertexArrays (int n, IntBuffer arrays);

	public boolean glIsVertexArray (int array);

	public void glGetIntegeri_v (int target, int index, IntBuffer data);

	public void glBeginTransformFeedback (int primitiveMode);

	public void glEndTransformFeedback ();

	public void glBindBufferRange (int target, int index, int buffer, long offset, long size);

	public void glTransformFeedbackVaryings (int program, int count, CharSequence[] varyings, int bufferMode);

	public String glGetTransformFeedbackVarying (int program, int index, int bufSize, IntBuffer length, IntBuffer size,
		IntBuffer type);

// public void glVertexAttribIPointer (int index, int size, int type, int stride, const void *pointer);
// public void glGetVertexAttribIiv (int index, int pname, int *params);
// public void glGetVertexAttribIuiv (int index, int pname, int *params);
// public void glVertexAttribI4i (int index, int x, int y, int z, int w);
// public void glVertexAttribI4ui (int index, int x, int y, int z, int w);
// public void glVertexAttribI4iv (int index, const int *v);
// public void glVertexAttribI4uiv (int index, const int *v);
// public void glGetUniformuiv (int program, int location, int *params);
// public int glGetFragDataLocation (int program, const GLchar *name);
// public void glUniform1ui (int location, int v0);
// public void glUniform2ui (int location, int v0, int v1);
// public void glUniform3ui (int location, int v0, int v1, int v2);
// public void glUniform4ui (int location, int v0, int v1, int v2, int v3);
// public void glUniform1uiv (int location, int count, const int *value);
// public void glUniform2uiv (int location, int count, const int *value);
// public void glUniform3uiv (int location, int count, const int *value);
// public void glUniform4uiv (int location, int count, const int *value);
// public void glClearBufferiv (int buffer, int drawbuffer, const int *value);
// public void glClearBufferuiv (int buffer, int drawbuffer, const int *value);
// public void glClearBufferfv (int buffer, int drawbuffer, const GLfloat *value);
// public void glClearBufferfi (int buffer, int drawbuffer, GLfloat depth, int stencil);
// public const GLubyte *glGetStringi (int name, int index);
// public void glCopyBufferSubData (int readTarget, int writeTarget, GLintptr readOffset, GLintptr writeOffset, GLsizeiptr
// size);
// public void glGetUniformIndices (int program, int uniformCount, const GLchar *const*uniformNames, GLuint
// *uniformIndices);
// public GLsync glFenceSync (int condition, GLbitfield flags);
// public GLboolean glIsSync (GLsync sync);
// public void glDeleteSync (GLsync sync);
// public int glClientWaitSync (GLsync sync, GLbitfield flags, GLuint64 timeout);
// public void glWaitSync (GLsync sync, GLbitfield flags, GLuint64 timeout);
// public void glGetInteger64v (int pname, GLint64 *data);
// public void glGetSynciv (GLsync sync, int pname, int bufSize, int *length, int *values);
// public void glGetInteger64i_v (int target, int index, GLint64 *data);
// public void glGetBufferParameteri64v (int target, int pname, GLint64 *params);
// public void glGenSamplers (int count, int *samplers);
// public void glDeleteSamplers (int count, const int *samplers);
// public GLboolean glIsSampler (int sampler);
// public void glBindSampler (int unit, int sampler);
// public void glSamplerParameteri (int sampler, int pname, int param);
// public void glSamplerParameteriv (int sampler, int pname, const int *param);
// public void glSamplerParameterf (int sampler, int pname, GLfloat param);
// public void glSamplerParameterfv (int sampler, int pname, const GLfloat *param);
// public void glGetSamplerParameteriv (int sampler, int pname, int *params);
// public void glGetSamplerParameterfv (int sampler, int pname, GLfloat *params);
// public void glVertexAttribDivisor (int index, int divisor);
// public void glBindTransformFeedback (int target, int id);
// public void glDeleteTransformFeedbacks (int n, const int *ids);
// public void glGenTransformFeedbacks (int n, int *ids);
// public GLboolean glIsTransformFeedback (int id);
// public void glPauseTransformFeedback (void);
// public void glResumeTransformFeedback (void);
// public void glGetProgramBinary (int program, int bufSize, int *length, int *binaryFormat, void *binary);
// public void glProgramBinary (int program, int binaryFormat, const void *binary, int length);
// public void glProgramParameteri (int program, int pname, int value);
// public void glInvalidateFramebuffer (int target, int numAttachments, const int *attachments);
// public void glInvalidateSubFramebuffer (int target, int numAttachments, const int *attachments, int x, int y,
// int width, int height);
// public void glTexStorage2D (int target, int levels, int internalformat, int width, int height);
// public void glTexStorage3D (int target, int levels, int internalformat, int width, int height, GLsizei
// depth);
// public void glGetInternalformativ (int target, int internalformat, int pname, int bufSize, int *params);
}
