/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// copied from https://cs.android.com/android/platform/superproject/+/master:frameworks/base/opengl/java/android/opengl/GLES32.java

package com.badlogic.gdx.graphics;

public interface GL32 extends GL31 {

	public static final int GL_CONTEXT_FLAG_DEBUG_BIT = 0x00000002;

	public static final int GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT = 0x00000004;

	public static final int GL_GEOMETRY_SHADER_BIT = 0x00000004;
	public static final int GL_TESS_CONTROL_SHADER_BIT = 0x00000008;
	public static final int GL_TESS_EVALUATION_SHADER_BIT = 0x00000010;

	public static final int GL_QUADS = 0x0007;
	public static final int GL_LINES_ADJACENCY = 0x000A;
	public static final int GL_LINE_STRIP_ADJACENCY = 0x000B;
	public static final int GL_TRIANGLES_ADJACENCY = 0x000C;
	public static final int GL_TRIANGLE_STRIP_ADJACENCY = 0x000D;
	public static final int GL_PATCHES = 0x000E;
	public static final int GL_STACK_OVERFLOW = 0x0503;
	public static final int GL_STACK_UNDERFLOW = 0x0504;
	public static final int GL_CONTEXT_LOST = 0x0507;
	public static final int GL_TEXTURE_BORDER_COLOR = 0x1004;
	public static final int GL_VERTEX_ARRAY = 0x8074;
	public static final int GL_CLAMP_TO_BORDER = 0x812D;
	public static final int GL_CONTEXT_FLAGS = 0x821E;
	public static final int GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED = 0x8221;
	public static final int GL_DEBUG_OUTPUT_SYNCHRONOUS = 0x8242;
	public static final int GL_DEBUG_NEXT_LOGGED_MESSAGE_LENGTH = 0x8243;
	public static final int GL_DEBUG_CALLBACK_FUNCTION = 0x8244;
	public static final int GL_DEBUG_CALLBACK_USER_PARAM = 0x8245;
	public static final int GL_DEBUG_SOURCE_API = 0x8246;
	public static final int GL_DEBUG_SOURCE_WINDOW_SYSTEM = 0x8247;
	public static final int GL_DEBUG_SOURCE_SHADER_COMPILER = 0x8248;
	public static final int GL_DEBUG_SOURCE_THIRD_PARTY = 0x8249;
	public static final int GL_DEBUG_SOURCE_APPLICATION = 0x824A;
	public static final int GL_DEBUG_SOURCE_OTHER = 0x824B;
	public static final int GL_DEBUG_TYPE_ERROR = 0x824C;
	public static final int GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR = 0x824D;
	public static final int GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR = 0x824E;
	public static final int GL_DEBUG_TYPE_PORTABILITY = 0x824F;
	public static final int GL_DEBUG_TYPE_PERFORMANCE = 0x8250;
	public static final int GL_DEBUG_TYPE_OTHER = 0x8251;
	public static final int GL_LOSE_CONTEXT_ON_RESET = 0x8252;
	public static final int GL_GUILTY_CONTEXT_RESET = 0x8253;
	public static final int GL_INNOCENT_CONTEXT_RESET = 0x8254;
	public static final int GL_UNKNOWN_CONTEXT_RESET = 0x8255;
	public static final int GL_RESET_NOTIFICATION_STRATEGY = 0x8256;
	public static final int GL_LAYER_PROVOKING_VERTEX = 0x825E;
	public static final int GL_UNDEFINED_VERTEX = 0x8260;
	public static final int GL_NO_RESET_NOTIFICATION = 0x8261;
	public static final int GL_DEBUG_TYPE_MARKER = 0x8268;
	public static final int GL_DEBUG_TYPE_PUSH_GROUP = 0x8269;
	public static final int GL_DEBUG_TYPE_POP_GROUP = 0x826A;
	public static final int GL_DEBUG_SEVERITY_NOTIFICATION = 0x826B;
	public static final int GL_MAX_DEBUG_GROUP_STACK_DEPTH = 0x826C;
	public static final int GL_DEBUG_GROUP_STACK_DEPTH = 0x826D;
	public static final int GL_BUFFER = 0x82E0;
	public static final int GL_SHADER = 0x82E1;
	public static final int GL_PROGRAM = 0x82E2;
	public static final int GL_QUERY = 0x82E3;
	public static final int GL_PROGRAM_PIPELINE = 0x82E4;
	public static final int GL_SAMPLER = 0x82E6;
	public static final int GL_MAX_LABEL_LENGTH = 0x82E8;
	public static final int GL_MAX_TESS_CONTROL_INPUT_COMPONENTS = 0x886C;
	public static final int GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS = 0x886D;
	public static final int GL_GEOMETRY_SHADER_INVOCATIONS = 0x887F;
	public static final int GL_GEOMETRY_VERTICES_OUT = 0x8916;
	public static final int GL_GEOMETRY_INPUT_TYPE = 0x8917;
	public static final int GL_GEOMETRY_OUTPUT_TYPE = 0x8918;
	public static final int GL_MAX_GEOMETRY_UNIFORM_BLOCKS = 0x8A2C;
	public static final int GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS = 0x8A32;
	public static final int GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS = 0x8C29;
	public static final int GL_TEXTURE_BUFFER = 0x8C2A;
	public static final int GL_TEXTURE_BUFFER_BINDING = 0x8C2A;
	public static final int GL_MAX_TEXTURE_BUFFER_SIZE = 0x8C2B;
	public static final int GL_TEXTURE_BINDING_BUFFER = 0x8C2C;
	public static final int GL_TEXTURE_BUFFER_DATA_STORE_BINDING = 0x8C2D;
	public static final int GL_SAMPLE_SHADING = 0x8C36;
	public static final int GL_MIN_SAMPLE_SHADING_VALUE = 0x8C37;
	public static final int GL_PRIMITIVES_GENERATED = 0x8C87;
	public static final int GL_FRAMEBUFFER_ATTACHMENT_LAYERED = 0x8DA7;
	public static final int GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS = 0x8DA8;
	public static final int GL_SAMPLER_BUFFER = 0x8DC2;
	public static final int GL_INT_SAMPLER_BUFFER = 0x8DD0;
	public static final int GL_UNSIGNED_INT_SAMPLER_BUFFER = 0x8DD8;
	public static final int GL_GEOMETRY_SHADER = 0x8DD9;
	public static final int GL_MAX_GEOMETRY_UNIFORM_COMPONENTS = 0x8DDF;
	public static final int GL_MAX_GEOMETRY_OUTPUT_VERTICES = 0x8DE0;
	public static final int GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS = 0x8DE1;
	public static final int GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E1E;
	public static final int GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E1F;
	public static final int GL_FIRST_VERTEX_CONVENTION = 0x8E4D;
	public static final int GL_LAST_VERTEX_CONVENTION = 0x8E4E;
	public static final int GL_MAX_GEOMETRY_SHADER_INVOCATIONS = 0x8E5A;
	public static final int GL_MIN_FRAGMENT_INTERPOLATION_OFFSET = 0x8E5B;
	public static final int GL_MAX_FRAGMENT_INTERPOLATION_OFFSET = 0x8E5C;
	public static final int GL_FRAGMENT_INTERPOLATION_OFFSET_BITS = 0x8E5D;
	public static final int GL_PATCH_VERTICES = 0x8E72;
	public static final int GL_TESS_CONTROL_OUTPUT_VERTICES = 0x8E75;
	public static final int GL_TESS_GEN_MODE = 0x8E76;
	public static final int GL_TESS_GEN_SPACING = 0x8E77;
	public static final int GL_TESS_GEN_VERTEX_ORDER = 0x8E78;
	public static final int GL_TESS_GEN_POINT_MODE = 0x8E79;
	public static final int GL_ISOLINES = 0x8E7A;
	public static final int GL_FRACTIONAL_ODD = 0x8E7B;
	public static final int GL_FRACTIONAL_EVEN = 0x8E7C;
	public static final int GL_MAX_PATCH_VERTICES = 0x8E7D;
	public static final int GL_MAX_TESS_GEN_LEVEL = 0x8E7E;
	public static final int GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS = 0x8E7F;
	public static final int GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS = 0x8E80;
	public static final int GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS = 0x8E81;
	public static final int GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS = 0x8E82;
	public static final int GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS = 0x8E83;
	public static final int GL_MAX_TESS_PATCH_COMPONENTS = 0x8E84;
	public static final int GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS = 0x8E85;
	public static final int GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS = 0x8E86;
	public static final int GL_TESS_EVALUATION_SHADER = 0x8E87;
	public static final int GL_TESS_CONTROL_SHADER = 0x8E88;
	public static final int GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS = 0x8E89;
	public static final int GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS = 0x8E8A;
	public static final int GL_TEXTURE_CUBE_MAP_ARRAY = 0x9009;
	public static final int GL_TEXTURE_BINDING_CUBE_MAP_ARRAY = 0x900A;
	public static final int GL_SAMPLER_CUBE_MAP_ARRAY = 0x900C;
	public static final int GL_SAMPLER_CUBE_MAP_ARRAY_SHADOW = 0x900D;
	public static final int GL_INT_SAMPLER_CUBE_MAP_ARRAY = 0x900E;
	public static final int GL_UNSIGNED_INT_SAMPLER_CUBE_MAP_ARRAY = 0x900F;
	public static final int GL_IMAGE_BUFFER = 0x9051;
	public static final int GL_IMAGE_CUBE_MAP_ARRAY = 0x9054;
	public static final int GL_INT_IMAGE_BUFFER = 0x905C;
	public static final int GL_INT_IMAGE_CUBE_MAP_ARRAY = 0x905F;
	public static final int GL_UNSIGNED_INT_IMAGE_BUFFER = 0x9067;
	public static final int GL_UNSIGNED_INT_IMAGE_CUBE_MAP_ARRAY = 0x906A;
	public static final int GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS = 0x90CB;
	public static final int GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS = 0x90CC;
	public static final int GL_MAX_GEOMETRY_IMAGE_UNIFORMS = 0x90CD;
	public static final int GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS = 0x90D7;
	public static final int GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS = 0x90D8;
	public static final int GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS = 0x90D9;
	public static final int GL_TEXTURE_2D_MULTISAMPLE_ARRAY = 0x9102;
	public static final int GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY = 0x9105;
	public static final int GL_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910B;
	public static final int GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910C;
	public static final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = 0x910D;
	public static final int GL_MAX_GEOMETRY_INPUT_COMPONENTS = 0x9123;
	public static final int GL_MAX_GEOMETRY_OUTPUT_COMPONENTS = 0x9124;
	public static final int GL_MAX_DEBUG_MESSAGE_LENGTH = 0x9143;
	public static final int GL_MAX_DEBUG_LOGGED_MESSAGES = 0x9144;
	public static final int GL_DEBUG_LOGGED_MESSAGES = 0x9145;
	public static final int GL_DEBUG_SEVERITY_HIGH = 0x9146;
	public static final int GL_DEBUG_SEVERITY_MEDIUM = 0x9147;
	public static final int GL_DEBUG_SEVERITY_LOW = 0x9148;
	public static final int GL_TEXTURE_BUFFER_OFFSET = 0x919D;
	public static final int GL_TEXTURE_BUFFER_SIZE = 0x919E;
	public static final int GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT = 0x919F;
	public static final int GL_MULTIPLY = 0x9294;
	public static final int GL_SCREEN = 0x9295;
	public static final int GL_OVERLAY = 0x9296;
	public static final int GL_DARKEN = 0x9297;
	public static final int GL_LIGHTEN = 0x9298;
	public static final int GL_COLORDODGE = 0x9299;
	public static final int GL_COLORBURN = 0x929A;
	public static final int GL_HARDLIGHT = 0x929B;
	public static final int GL_SOFTLIGHT = 0x929C;
	public static final int GL_DIFFERENCE = 0x929E;
	public static final int GL_EXCLUSION = 0x92A0;
	public static final int GL_HSL_HUE = 0x92AD;
	public static final int GL_HSL_SATURATION = 0x92AE;
	public static final int GL_HSL_COLOR = 0x92AF;
	public static final int GL_HSL_LUMINOSITY = 0x92B0;
	public static final int GL_PRIMITIVE_BOUNDING_BOX = 0x92BE;
	public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS = 0x92CD;
	public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS = 0x92CE;
	public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS = 0x92CF;
	public static final int GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS = 0x92D3;
	public static final int GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS = 0x92D4;
	public static final int GL_MAX_GEOMETRY_ATOMIC_COUNTERS = 0x92D5;
	public static final int GL_DEBUG_OUTPUT = 0x92E0;
	public static final int GL_IS_PER_PATCH = 0x92E7;
	public static final int GL_REFERENCED_BY_TESS_CONTROL_SHADER = 0x9307;
	public static final int GL_REFERENCED_BY_TESS_EVALUATION_SHADER = 0x9308;
	public static final int GL_REFERENCED_BY_GEOMETRY_SHADER = 0x9309;
	public static final int GL_FRAMEBUFFER_DEFAULT_LAYERS = 0x9312;
	public static final int GL_MAX_FRAMEBUFFER_LAYERS = 0x9317;
	public static final int GL_MULTISAMPLE_LINE_WIDTH_RANGE = 0x9381;
	public static final int GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY = 0x9382;
	public static final int GL_COMPRESSED_RGBA_ASTC_4x4 = 0x93B0;
	public static final int GL_COMPRESSED_RGBA_ASTC_5x4 = 0x93B1;
	public static final int GL_COMPRESSED_RGBA_ASTC_5x5 = 0x93B2;
	public static final int GL_COMPRESSED_RGBA_ASTC_6x5 = 0x93B3;
	public static final int GL_COMPRESSED_RGBA_ASTC_6x6 = 0x93B4;
	public static final int GL_COMPRESSED_RGBA_ASTC_8x5 = 0x93B5;
	public static final int GL_COMPRESSED_RGBA_ASTC_8x6 = 0x93B6;
	public static final int GL_COMPRESSED_RGBA_ASTC_8x8 = 0x93B7;
	public static final int GL_COMPRESSED_RGBA_ASTC_10x5 = 0x93B8;
	public static final int GL_COMPRESSED_RGBA_ASTC_10x6 = 0x93B9;
	public static final int GL_COMPRESSED_RGBA_ASTC_10x8 = 0x93BA;
	public static final int GL_COMPRESSED_RGBA_ASTC_10x10 = 0x93BB;
	public static final int GL_COMPRESSED_RGBA_ASTC_12x10 = 0x93BC;
	public static final int GL_COMPRESSED_RGBA_ASTC_12x12 = 0x93BD;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4 = 0x93D0;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4 = 0x93D1;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5 = 0x93D2;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5 = 0x93D3;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6 = 0x93D4;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5 = 0x93D5;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6 = 0x93D6;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8 = 0x93D7;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5 = 0x93D8;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6 = 0x93D9;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8 = 0x93DA;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10 = 0x93DB;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10 = 0x93DC;
	public static final int GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12 = 0x93DD;

	// C function void glBlendBarrier ( void )

	void glBlendBarrier ();

	// C function void glCopyImageSubData ( GLuint srcName, GLenum srcTarget, GLint srcLevel, GLint srcX, GLint srcY, GLint srcZ,
	// GLuint dstName, GLenum dstTarget, GLint dstLevel, GLint dstX, GLint dstY, GLint dstZ, GLsizei srcWidth, GLsizei srcHeight,
	// GLsizei srcDepth )

	void glCopyImageSubData (int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget,
		int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth);

	// C function void glDebugMessageControl ( GLenum source, GLenum type, GLenum severity, GLsizei count, const GLuint *ids,
	// GLboolean enabled )

// void glDebugMessageControl(
// int source,
// int type,
// int severity,
// int count,
// int[] ids,
// int offset,
// boolean enabled
// );

	// C function void glDebugMessageControl ( GLenum source, GLenum type, GLenum severity, GLsizei count, const GLuint *ids,
	// GLboolean enabled )

	void glDebugMessageControl (int source, int type, int severity,
// int count,
		java.nio.IntBuffer ids, boolean enabled);

	// C function void glDebugMessageInsert ( GLenum source, GLenum type, GLuint id, GLenum severity, GLsizei length, const GLchar
	// *buf )

	void glDebugMessageInsert (int source, int type, int id, int severity,
// int length
		String buf);

	// C function void glDebugMessageCallback ( GLDEBUGPROC callback, const void *userParam )

	public interface DebugProc {
		void onMessage (int source, int type, int id, int severity, String message);
	}

	void glDebugMessageCallback (DebugProc callback);

	// C function GLuint glGetDebugMessageLog ( GLuint count, GLsizei bufSize, GLenum *sources, GLenum *types, GLuint *ids, GLenum
	// *severities, GLsizei *lengths, GLchar *messageLog )

// int glGetDebugMessageLog(
// int count,
// int bufSize,
// int[] sources,
// int sourcesOffset,
// int[] types,
// int typesOffset,
// int[] ids,
// int idsOffset,
// int[] severities,
// int severitiesOffset,
// int[] lengths,
// int lengthsOffset,
// byte[] messageLog,
// int messageLogOffset);

	// C function GLuint glGetDebugMessageLog ( GLuint count, GLsizei bufSize, GLenum *sources, GLenum *types, GLuint *ids, GLenum
	// *severities, GLsizei *lengths, GLchar *messageLog )

	int glGetDebugMessageLog (int count, java.nio.IntBuffer sources, java.nio.IntBuffer types, java.nio.IntBuffer ids,
		java.nio.IntBuffer severities, java.nio.IntBuffer lengths, java.nio.ByteBuffer messageLog);

	// C function GLuint glGetDebugMessageLog ( GLuint count, GLsizei bufSize, GLenum *sources, GLenum *types, GLuint *ids, GLenum
	// *severities, GLsizei *lengths, GLchar *messageLog )

// String[] glGetDebugMessageLog(
// int count,
// int[] sources,
// int sourcesOffset,
// int[] types,
// int typesOffset,
// int[] ids,
// int idsOffset,
// int[] severities,
// int severitiesOffset);

	// C function GLuint glGetDebugMessageLog ( GLuint count, GLsizei bufSize, GLenum *sources, GLenum *types, GLuint *ids, GLenum
	// *severities, GLsizei *lengths, GLchar *messageLog )

// String[] glGetDebugMessageLog(
// int count,
// java.nio.IntBuffer sources,
// java.nio.IntBuffer types,
// java.nio.IntBuffer ids,
// java.nio.IntBuffer severities);

	// C function void glPushDebugGroup ( GLenum source, GLuint id, GLsizei length, const GLchar *message )

	void glPushDebugGroup (int source, int id,
// int length,
		String message);

	// C function void glPopDebugGroup ( void )

	void glPopDebugGroup ();

	// C function void glObjectLabel ( GLenum identifier, GLuint name, GLsizei length, const GLchar *label )

	void glObjectLabel (int identifier, int name,
// int length,
		String label);

	// C function void glGetObjectLabel ( GLenum identifier, GLuint name, GLsizei bufSize, GLsizei *length, GLchar *label )

	String glGetObjectLabel (int identifier, int name);

	// C function void glObjectPtrLabel ( const void *ptr, GLsizei length, const GLchar *label )

// void glObjectPtrLabel (long ptr, String label);

	// C function void glGetObjectPtrLabel ( const void *ptr, GLsizei bufSize, GLsizei *length, GLchar *label )

// String glGetObjectPtrLabel (long ptr);

	// C function void glGetPointerv ( GLenum pname, void **params )

	long glGetPointerv (int pname);

	// C function void glEnablei ( GLenum target, GLuint index )

	void glEnablei (int target, int index);

	// C function void glDisablei ( GLenum target, GLuint index )

	void glDisablei (int target, int index);

	// C function void glBlendEquationi ( GLuint buf, GLenum mode )

	void glBlendEquationi (int buf, int mode);

	// C function void glBlendEquationSeparatei ( GLuint buf, GLenum modeRGB, GLenum modeAlpha )

	void glBlendEquationSeparatei (int buf, int modeRGB, int modeAlpha);

	// C function void glBlendFunci ( GLuint buf, GLenum src, GLenum dst )

	void glBlendFunci (int buf, int src, int dst);

	// C function void glBlendFuncSeparatei ( GLuint buf, GLenum srcRGB, GLenum dstRGB, GLenum srcAlpha, GLenum dstAlpha )

	void glBlendFuncSeparatei (int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha);

	// C function void glColorMaski ( GLuint index, GLboolean r, GLboolean g, GLboolean b, GLboolean a )

	void glColorMaski (int index, boolean r, boolean g, boolean b, boolean a);

	// C function GLboolean glIsEnabledi ( GLenum target, GLuint index )

	boolean glIsEnabledi (int target, int index);

	// C function void glDrawElementsBaseVertex ( GLenum mode, GLsizei count, GLenum type, const void *indices, GLint basevertex )

	void glDrawElementsBaseVertex (int mode, int count, int type, java.nio.Buffer indices, int basevertex);

	// C function void glDrawRangeElementsBaseVertex ( GLenum mode, GLuint start, GLuint end, GLsizei count, GLenum type, const
	// void *indices, GLint basevertex )

	void glDrawRangeElementsBaseVertex (int mode, int start, int end, int count, int type, java.nio.Buffer indices,
		int basevertex);

	// C function void glDrawElementsInstancedBaseVertex ( GLenum mode, GLsizei count, GLenum type, const void *indices, GLsizei
	// instanceCount, GLint basevertex )

	void glDrawElementsInstancedBaseVertex (int mode, int count, int type, java.nio.Buffer indices, int instanceCount,
		int basevertex);

	// C function void glDrawElementsInstancedBaseVertex ( GLenum mode, GLsizei count, GLenum type, const void *indices, GLsizei
	// instanceCount, GLint basevertex )

	void glDrawElementsInstancedBaseVertex (int mode, int count, int type, int indicesOffset, int instanceCount, int basevertex);

	// C function void glFramebufferTexture ( GLenum target, GLenum attachment, GLuint texture, GLint level )

	void glFramebufferTexture (int target, int attachment, int texture, int level);

	// C function void glPrimitiveBoundingBox ( GLfloat minX, GLfloat minY, GLfloat minZ, GLfloat minW, GLfloat maxX, GLfloat maxY,
	// GLfloat maxZ, GLfloat maxW )

// void glPrimitiveBoundingBox(
// float minX,
// float minY,
// float minZ,
// float minW,
// float maxX,
// float maxY,
// float maxZ,
// float maxW
// );

	// C function GLenum glGetGraphicsResetStatus ( void )

	int glGetGraphicsResetStatus ();

	// C function void glReadnPixels ( GLint x, GLint y, GLsizei width, GLsizei height, GLenum format, GLenum type, GLsizei
	// bufSize, void *data )

	void glReadnPixels (int x, int y, int width, int height, int format, int type, int bufSize, java.nio.Buffer data);

	// C function void glGetnUniformfv ( GLuint program, GLint location, GLsizei bufSize, GLfloat *params )

// void glGetnUniformfv(
// int program,
// int location,
// int bufSize,
// float[] params,
// int offset
// );

	// C function void glGetnUniformfv ( GLuint program, GLint location, GLsizei bufSize, GLfloat *params )

	void glGetnUniformfv (int program, int location,
// int bufSize,
		java.nio.FloatBuffer params);

	// C function void glGetnUniformiv ( GLuint program, GLint location, GLsizei bufSize, GLint *params )

// void glGetnUniformiv(
// int program,
// int location,
// int bufSize,
// int[] params,
// int offset
// );

	// C function void glGetnUniformiv ( GLuint program, GLint location, GLsizei bufSize, GLint *params )

	void glGetnUniformiv (int program, int location,
// int bufSize,
		java.nio.IntBuffer params);

	// C function void glGetnUniformuiv ( GLuint program, GLint location, GLsizei bufSize, GLuint *params )

// void glGetnUniformuiv(
// int program,
// int location,
// int bufSize,
// int[] params,
// int offset
// );

	// C function void glGetnUniformuiv ( GLuint program, GLint location, GLsizei bufSize, GLuint *params )

	void glGetnUniformuiv (int program, int location,
// int bufSize,
		java.nio.IntBuffer params);

	// C function void glMinSampleShading ( GLfloat value )

	void glMinSampleShading (float value);

	// C function void glPatchParameteri ( GLenum pname, GLint value )

	void glPatchParameteri (int pname, int value);

	// C function void glTexParameterIiv ( GLenum target, GLenum pname, const GLint *params )

// void glTexParameterIiv(
// int target,
// int pname,
// int[] params,
// int offset
// );

	// C function void glTexParameterIiv ( GLenum target, GLenum pname, const GLint *params )

	void glTexParameterIiv (int target, int pname, java.nio.IntBuffer params);

	// C function void glTexParameterIuiv ( GLenum target, GLenum pname, const GLuint *params )

// void glTexParameterIuiv(
// int target,
// int pname,
// int[] params,
// int offset
// );

	// C function void glTexParameterIuiv ( GLenum target, GLenum pname, const GLuint *params )

	void glTexParameterIuiv (int target, int pname, java.nio.IntBuffer params);

	// C function void glGetTexParameterIiv ( GLenum target, GLenum pname, GLint *params )

// void glGetTexParameterIiv(
// int target,
// int pname,
// int[] params,
// int offset
// );

	// C function void glGetTexParameterIiv ( GLenum target, GLenum pname, GLint *params )

	void glGetTexParameterIiv (int target, int pname, java.nio.IntBuffer params);

	// C function void glGetTexParameterIuiv ( GLenum target, GLenum pname, GLuint *params )

// void glGetTexParameterIuiv(
// int target,
// int pname,
// int[] params,
// int offset
// );

	// C function void glGetTexParameterIuiv ( GLenum target, GLenum pname, GLuint *params )

	void glGetTexParameterIuiv (int target, int pname, java.nio.IntBuffer params);

	// C function void glSamplerParameterIiv ( GLuint sampler, GLenum pname, const GLint *param )

// void glSamplerParameterIiv(
// int sampler,
// int pname,
// int[] param,
// int offset
// );

	// C function void glSamplerParameterIiv ( GLuint sampler, GLenum pname, const GLint *param )

	void glSamplerParameterIiv (int sampler, int pname, java.nio.IntBuffer param);

	// C function void glSamplerParameterIuiv ( GLuint sampler, GLenum pname, const GLuint *param )

// void glSamplerParameterIuiv(
// int sampler,
// int pname,
// int[] param,
// int offset
// );

	// C function void glSamplerParameterIuiv ( GLuint sampler, GLenum pname, const GLuint *param )

	void glSamplerParameterIuiv (int sampler, int pname, java.nio.IntBuffer param);

	// C function void glGetSamplerParameterIiv ( GLuint sampler, GLenum pname, GLint *params )

// void glGetSamplerParameterIiv(
// int sampler,
// int pname,
// int[] params,
// int offset
// );

	// C function void glGetSamplerParameterIiv ( GLuint sampler, GLenum pname, GLint *params )

	void glGetSamplerParameterIiv (int sampler, int pname, java.nio.IntBuffer params);

	// C function void glGetSamplerParameterIuiv ( GLuint sampler, GLenum pname, GLuint *params )

// void glGetSamplerParameterIuiv(
// int sampler,
// int pname,
// int[] params,
// int offset
// );

	// C function void glGetSamplerParameterIuiv ( GLuint sampler, GLenum pname, GLuint *params )

	void glGetSamplerParameterIuiv (int sampler, int pname, java.nio.IntBuffer params);

	// C function void glTexBuffer ( GLenum target, GLenum internalformat, GLuint buffer )

	void glTexBuffer (int target, int internalformat, int buffer);

	// C function void glTexBufferRange ( GLenum target, GLenum internalformat, GLuint buffer, GLintptr offset, GLsizeiptr size )

	void glTexBufferRange (int target, int internalformat, int buffer, int offset, int size);

	// C function void glTexStorage3DMultisample ( GLenum target, GLsizei samples, GLenum internalformat, GLsizei width, GLsizei
	// height, GLsizei depth, GLboolean fixedsamplelocations )

	void glTexStorage3DMultisample (int target, int samples, int internalformat, int width, int height, int depth,
		boolean fixedsamplelocations);

}
