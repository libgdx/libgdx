/*
 **
 ** Copyright, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.badlogic.gdx.graphics;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** OpenGL ES 3.1 */
public interface GL31 extends GL30 {
	public final int GL_COMPUTE_SHADER                             = 0x91B9;
	public final int GL_MAX_COMPUTE_UNIFORM_BLOCKS                 = 0x91BB;
	public final int GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS            = 0x91BC;
	public final int GL_MAX_COMPUTE_IMAGE_UNIFORMS                 = 0x91BD;
	public final int GL_MAX_COMPUTE_SHARED_MEMORY_SIZE             = 0x8262;
	public final int GL_MAX_COMPUTE_UNIFORM_COMPONENTS             = 0x8263;
	public final int GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS         = 0x8264;
	public final int GL_MAX_COMPUTE_ATOMIC_COUNTERS                = 0x8265;
	public final int GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS    = 0x8266;
	public final int GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS         = 0x90EB;
	public final int GL_MAX_COMPUTE_WORK_GROUP_COUNT               = 0x91BE;
	public final int GL_MAX_COMPUTE_WORK_GROUP_SIZE                = 0x91BF;
	public final int GL_COMPUTE_WORK_GROUP_SIZE                    = 0x8267;
	public final int GL_DISPATCH_INDIRECT_BUFFER                   = 0x90EE;
	public final int GL_DISPATCH_INDIRECT_BUFFER_BINDING           = 0x90EF;
	public final int GL_COMPUTE_SHADER_BIT                         = 0x20;
	public final int GL_DRAW_INDIRECT_BUFFER                       = 0x8F3F;
	public final int GL_DRAW_INDIRECT_BUFFER_BINDING               = 0x8F43;
	public final int GL_MAX_UNIFORM_LOCATIONS                      = 0x826E;
	public final int GL_FRAMEBUFFER_DEFAULT_WIDTH                  = 0x9310;
	public final int GL_FRAMEBUFFER_DEFAULT_HEIGHT                 = 0x9311;
	public final int GL_FRAMEBUFFER_DEFAULT_SAMPLES                = 0x9313;
	public final int GL_FRAMEBUFFER_DEFAULT_FIXED_SAMPLE_LOCATIONS = 0x9314;
	public final int GL_MAX_FRAMEBUFFER_WIDTH                      = 0x9315;
	public final int GL_MAX_FRAMEBUFFER_HEIGHT                     = 0x9316;
	public final int GL_MAX_FRAMEBUFFER_SAMPLES                    = 0x9318;
	public final int GL_UNIFORM                                    = 0x92E1;
	public final int GL_UNIFORM_BLOCK                              = 0x92E2;
	public final int GL_PROGRAM_INPUT                              = 0x92E3;
	public final int GL_PROGRAM_OUTPUT                             = 0x92E4;
	public final int GL_BUFFER_VARIABLE                            = 0x92E5;
	public final int GL_SHADER_STORAGE_BLOCK                       = 0x92E6;
	public final int GL_ATOMIC_COUNTER_BUFFER                      = 0x92C0;
	public final int GL_TRANSFORM_FEEDBACK_VARYING                 = 0x92F4;
	public final int GL_ACTIVE_RESOURCES                           = 0x92F5;
	public final int GL_MAX_NAME_LENGTH                            = 0x92F6;
	public final int GL_MAX_NUM_ACTIVE_VARIABLES                   = 0x92F7;
	public final int GL_NAME_LENGTH                                = 0x92F9;
	public final int GL_TYPE                                       = 0x92FA;
	public final int GL_ARRAY_SIZE                                 = 0x92FB;
	public final int GL_OFFSET                                     = 0x92FC;
	public final int GL_BLOCK_INDEX                                = 0x92FD;
	public final int GL_ARRAY_STRIDE                               = 0x92FE;
	public final int GL_MATRIX_STRIDE                              = 0x92FF;
	public final int GL_IS_ROW_MAJOR                               = 0x9300;
	public final int GL_ATOMIC_COUNTER_BUFFER_INDEX                = 0x9301;
	public final int GL_BUFFER_BINDING                             = 0x9302;
	public final int GL_BUFFER_DATA_SIZE                           = 0x9303;
	public final int GL_NUM_ACTIVE_VARIABLES                       = 0x9304;
	public final int GL_ACTIVE_VARIABLES                           = 0x9305;
	public final int GL_REFERENCED_BY_VERTEX_SHADER                = 0x9306;
	public final int GL_REFERENCED_BY_FRAGMENT_SHADER              = 0x930A;
	public final int GL_REFERENCED_BY_COMPUTE_SHADER               = 0x930B;
	public final int GL_TOP_LEVEL_ARRAY_SIZE                       = 0x930C;
	public final int GL_TOP_LEVEL_ARRAY_STRIDE                     = 0x930D;
	public final int GL_LOCATION                                   = 0x930E;
	public final int GL_VERTEX_SHADER_BIT                          = 0x1;
	public final int GL_FRAGMENT_SHADER_BIT                        = 0x2;
	public final int GL_ALL_SHADER_BITS                            = 0xFFFFFFFF;
	public final int GL_PROGRAM_SEPARABLE                          = 0x8258;
	public final int GL_ACTIVE_PROGRAM                             = 0x8259;
	public final int GL_PROGRAM_PIPELINE_BINDING                   = 0x825A;
	public final int GL_ATOMIC_COUNTER_BUFFER_BINDING              = 0x92C1;
	public final int GL_ATOMIC_COUNTER_BUFFER_START                = 0x92C2;
	public final int GL_ATOMIC_COUNTER_BUFFER_SIZE                 = 0x92C3;
	public final int GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS          = 0x92CC;
	public final int GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS        = 0x92D0;
	public final int GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS        = 0x92D1;
	public final int GL_MAX_VERTEX_ATOMIC_COUNTERS                 = 0x92D2;
	public final int GL_MAX_FRAGMENT_ATOMIC_COUNTERS               = 0x92D6;
	public final int GL_MAX_COMBINED_ATOMIC_COUNTERS               = 0x92D7;
	public final int GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE             = 0x92D8;
	public final int GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS         = 0x92DC;
	public final int GL_ACTIVE_ATOMIC_COUNTER_BUFFERS              = 0x92D9;
	public final int GL_UNSIGNED_INT_ATOMIC_COUNTER                = 0x92DB;
	public final int GL_MAX_IMAGE_UNITS                            = 0x8F38;
	public final int GL_MAX_VERTEX_IMAGE_UNIFORMS                  = 0x90CA;
	public final int GL_MAX_FRAGMENT_IMAGE_UNIFORMS                = 0x90CE;
	public final int GL_MAX_COMBINED_IMAGE_UNIFORMS                = 0x90CF;
	public final int GL_IMAGE_BINDING_NAME                         = 0x8F3A;
	public final int GL_IMAGE_BINDING_LEVEL                        = 0x8F3B;
	public final int GL_IMAGE_BINDING_LAYERED                      = 0x8F3C;
	public final int GL_IMAGE_BINDING_LAYER                        = 0x8F3D;
	public final int GL_IMAGE_BINDING_ACCESS                       = 0x8F3E;
	public final int GL_IMAGE_BINDING_FORMAT                       = 0x906E;
	public final int GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT            = 0x1;
	public final int GL_ELEMENT_ARRAY_BARRIER_BIT                  = 0x2;
	public final int GL_UNIFORM_BARRIER_BIT                        = 0x4;
	public final int GL_TEXTURE_FETCH_BARRIER_BIT                  = 0x8;
	public final int GL_SHADER_IMAGE_ACCESS_BARRIER_BIT            = 0x20;
	public final int GL_COMMAND_BARRIER_BIT                        = 0x40;
	public final int GL_PIXEL_BUFFER_BARRIER_BIT                   = 0x80;
	public final int GL_TEXTURE_UPDATE_BARRIER_BIT                 = 0x100;
	public final int GL_BUFFER_UPDATE_BARRIER_BIT                  = 0x200;
	public final int GL_FRAMEBUFFER_BARRIER_BIT                    = 0x400;
	public final int GL_TRANSFORM_FEEDBACK_BARRIER_BIT             = 0x800;
	public final int GL_ATOMIC_COUNTER_BARRIER_BIT                 = 0x1000;
	public final int GL_ALL_BARRIER_BITS                           = 0xFFFFFFFF;
	public final int GL_IMAGE_2D                                   = 0x904D;
	public final int GL_IMAGE_3D                                   = 0x904E;
	public final int GL_IMAGE_CUBE                                 = 0x9050;
	public final int GL_IMAGE_2D_ARRAY                             = 0x9053;
	public final int GL_INT_IMAGE_2D                               = 0x9058;
	public final int GL_INT_IMAGE_3D                               = 0x9059;
	public final int GL_INT_IMAGE_CUBE                             = 0x905B;
	public final int GL_INT_IMAGE_2D_ARRAY                         = 0x905E;
	public final int GL_UNSIGNED_INT_IMAGE_2D                      = 0x9063;
	public final int GL_UNSIGNED_INT_IMAGE_3D                      = 0x9064;
	public final int GL_UNSIGNED_INT_IMAGE_CUBE                    = 0x9066;
	public final int GL_UNSIGNED_INT_IMAGE_2D_ARRAY                = 0x9069;
	public final int GL_IMAGE_FORMAT_COMPATIBILITY_TYPE            = 0x90C7;
	public final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_SIZE         = 0x90C8;
	public final int GL_IMAGE_FORMAT_COMPATIBILITY_BY_CLASS        = 0x90C9;
	public final int GL_READ_ONLY                                  = 0x88B8;
	public final int GL_WRITE_ONLY                                 = 0x88B9;
	public final int GL_READ_WRITE                                 = 0x88BA;
	public final int GL_SHADER_STORAGE_BUFFER                      = 0x90D2;
	public final int GL_SHADER_STORAGE_BUFFER_BINDING              = 0x90D3;
	public final int GL_SHADER_STORAGE_BUFFER_START                = 0x90D4;
	public final int GL_SHADER_STORAGE_BUFFER_SIZE                 = 0x90D5;
	public final int GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS           = 0x90D6;
	public final int GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS         = 0x90DA;
	public final int GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS          = 0x90DB;
	public final int GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS         = 0x90DC;
	public final int GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS         = 0x90DD;
	public final int GL_MAX_SHADER_STORAGE_BLOCK_SIZE              = 0x90DE;
	public final int GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT     = 0x90DF;
	public final int GL_SHADER_STORAGE_BARRIER_BIT                 = 0x2000;
	public final int GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES       = 0x8F39;
	public final int GL_DEPTH_STENCIL_TEXTURE_MODE                 = 0x90EA;
	public final int GL_STENCIL_INDEX                              = 0x1901;
	public final int GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET          = 0x8E5E;
	public final int GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET          = 0x8E5F;
	public final int GL_SAMPLE_POSITION                            = 0x8E50;
	public final int GL_SAMPLE_MASK                                = 0x8E51;
	public final int GL_SAMPLE_MASK_VALUE                          = 0x8E52;
	public final int GL_TEXTURE_2D_MULTISAMPLE                     = 0x9100;
	public final int GL_MAX_SAMPLE_MASK_WORDS                      = 0x8E59;
	public final int GL_MAX_COLOR_TEXTURE_SAMPLES                  = 0x910E;
	public final int GL_MAX_DEPTH_TEXTURE_SAMPLES                  = 0x910F;
	public final int GL_MAX_INTEGER_SAMPLES                        = 0x9110;
	public final int GL_TEXTURE_BINDING_2D_MULTISAMPLE             = 0x9104;
	public final int GL_TEXTURE_SAMPLES                            = 0x9106;
	public final int GL_TEXTURE_FIXED_SAMPLE_LOCATIONS             = 0x9107;
	public final int GL_TEXTURE_WIDTH                              = 0x1000;
	public final int GL_TEXTURE_HEIGHT                             = 0x1001;
	public final int GL_TEXTURE_DEPTH                              = 0x8071;
	public final int GL_TEXTURE_INTERNAL_FORMAT                    = 0x1003;
	public final int GL_TEXTURE_RED_SIZE                           = 0x805C;
	public final int GL_TEXTURE_GREEN_SIZE                         = 0x805D;
	public final int GL_TEXTURE_BLUE_SIZE                          = 0x805E;
	public final int GL_TEXTURE_ALPHA_SIZE                         = 0x805F;
	public final int GL_TEXTURE_DEPTH_SIZE                         = 0x884A;
	public final int GL_TEXTURE_STENCIL_SIZE                       = 0x88F1;
	public final int GL_TEXTURE_SHARED_SIZE                        = 0x8C3F;
	public final int GL_TEXTURE_RED_TYPE                           = 0x8C10;
	public final int GL_TEXTURE_GREEN_TYPE                         = 0x8C11;
	public final int GL_TEXTURE_BLUE_TYPE                          = 0x8C12;
	public final int GL_TEXTURE_ALPHA_TYPE                         = 0x8C13;
	public final int GL_TEXTURE_DEPTH_TYPE                         = 0x8C16;
	public final int GL_TEXTURE_COMPRESSED                         = 0x86A1;
	public final int GL_SAMPLER_2D_MULTISAMPLE                     = 0x9108;
	public final int GL_INT_SAMPLER_2D_MULTISAMPLE                 = 0x9109;
	public final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE        = 0x910A;
	public final int GL_VERTEX_ATTRIB_BINDING                      = 0x82D4;
	public final int GL_VERTEX_ATTRIB_RELATIVE_OFFSET              = 0x82D5;
	public final int GL_VERTEX_BINDING_DIVISOR                     = 0x82D6;
	public final int GL_VERTEX_BINDING_OFFSET                      = 0x82D7;
	public final int GL_VERTEX_BINDING_STRIDE                      = 0x82D8;
	public final int GL_VERTEX_BINDING_BUFFER                      = 0x8F4F;
	public final int GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET          = 0x82D9;
	public final int GL_MAX_VERTEX_ATTRIB_BINDINGS                 = 0x82DA;
	public final int GL_MAX_VERTEX_ATTRIB_STRIDE                   = 0x82E5; 	

	public void glActiveShaderProgram (int pipeline, int program);

	public void glBindImageTexture (int unit, int texture, int level, boolean layered, int layer, int access, int format);

	public void glBindProgramPipeline (int pipeline);

	public void glBindVertexBuffer (int bindingindex, int buffer, long offset, int stride);

	public int glCreateShaderProgramv (int type, String[] strings);

	// void glDeleteProgramPipelines(int n, int[] pipelines, int offset)
	public void glDeleteProgramPipelines (int n, IntBuffer pipelines);

	public void glDispatchCompute (int num_groups_x, int num_groups_y, int num_groups_z);

 	public void glDispatchComputeIndirect (long indirect);

 	public void glDrawArraysIndirect (int mode, long indirect);

 	public void glDrawElementsIndirect (int mode, int type, long indirect);
 	
 	public void glFramebufferParameteri (int target, int pname, int param);

	// void glGenProgramPipelines(int n, int[] pipelines, int offset)
 	public void glGenProgramPipelines (int n, IntBuffer pipelines);

	// void	glGetBooleani_v(int target, int index, boolean[] data, int offset)
	public void glGetBooleani_v (int target, int index, IntBuffer data);

	// void	glGetFramebufferParameteriv(int target, int pname, int[] params, int offset)
	public void glGetFramebufferParameteriv (int target, int pname, IntBuffer params);

 	// void	glGetMultisamplefv(int pname, int index, float[] val, int offset)
 	public void glGetMultisamplefv (int pname, int index, FloatBuffer val);

	// void	glGetProgramInterfaceiv(int program, int programInterface, int pname, int[] params, int offset)
	public void glGetProgramInterfaceiv (int program, int programInterface, int pname, IntBuffer params);

	public String glGetProgramPipelineInfoLog (int program);

	// void	glGetProgramPipelineiv(int pipeline, int pname, int[] params, int offset)
	public void glGetProgramPipelineiv (int pipeline, int pname, IntBuffer params);

	public int glGetProgramResourceIndex (int program, int programInterface, String name);

	public int glGetProgramResourceLocation (int program, int programInterface, String name);

	public String glGetProgramResourceName(int program, int programInterface, int index);

 	// void	glGetProgramResourceiv(int program, int programInterface, int index, int propCount, int[] props, int propsOffset, int bufSize, int[] length, int lengthOffset, int[] params, int paramsOffset)	
	public void glGetProgramResourceiv (int program, int programInterface, int index, int propCount, IntBuffer props, int bufSize, IntBuffer length, IntBuffer params);

	// void	glGetTexLevelParameterfv(int target, int level, int pname, float[] params, int offset)
 	public void glGetTexLevelParameterfv (int target, int level, int pname, FloatBuffer params);
 
	// void	glGetTexLevelParameteriv(int target, int level, int pname, int[] params, int offset)
	public void glGetTexLevelParameteriv (int target, int level, int pname, IntBuffer params);

 	public boolean glIsProgramPipeline (int pipeline);

	public void glMemoryBarrier (int barriers);

	public void glMemoryBarrierByRegion (int barriers);

	public void glProgramUniform1f (int program, int location, float v0);

	// void glProgramUniform1fv(int program, int location, int count, FloatBuffer value)
	// void glProgramUniform1fv(int program, int location, int count, float[] value, int offset)
	public void glProgramUniform1i (int program, int location, int v0);

	// void glProgramUniform1iv(int program, int location, int count, int[] value, int offset)
	// void glProgramUniform1iv(int program, int location, int count, IntBuffer value)
	public void glProgramUniform1ui (int program, int location, int v0);

	// void glProgramUniform1uiv(int program, int location, int count, int[] value, int offset)
	// void glProgramUniform1uiv(int program, int location, int count, IntBuffer value)
	public void glProgramUniform2f (int program, int location, float v0, float v1);

	// void glProgramUniform2fv(int program, int location, int count, FloatBuffer value)
	// void glProgramUniform2fv(int program, int location, int count, float[] value, int offset)
	public void glProgramUniform2i (int program, int location, int v0, int v1);

	// void glProgramUniform2iv(int program, int location, int count, IntBuffer value)
	// void glProgramUniform2iv(int program, int location, int count, int[] value, int offset)
	public void glProgramUniform2ui (int program, int location, int v0, int v1);

	// void glProgramUniform2uiv(int program, int location, int count, IntBuffer value)
	// void glProgramUniform2uiv(int program, int location, int count, int[] value, int offset)
	public void glProgramUniform3f (int program, int location, float v0, float v1, float v2);

	// void glProgramUniform3fv(int program, int location, int count, FloatBuffer value)
	// void glProgramUniform3fv(int program, int location, int count, float[] value, int offset)
	public void glProgramUniform3i (int program, int location, int v0, int v1, int v2);

	// void glProgramUniform3iv(int program, int location, int count, int[] value, int offset)
	// void glProgramUniform3iv(int program, int location, int count, IntBuffer value)
	public void glProgramUniform3ui (int program, int location, int v0, int v1, int v2);

	// void glProgramUniform3uiv(int program, int location, int count, IntBuffer value)
	// void glProgramUniform3uiv(int program, int location, int count, int[] value, int offset)
	public void glProgramUniform4f (int program, int location, float v0, float v1, float v2, float v3);

	// void glProgramUniform4fv(int program, int location, int count, FloatBuffer value)
	// void glProgramUniform4fv(int program, int location, int count, float[] value, int offset)
	public void glProgramUniform4i (int program, int location, int v0, int v1, int v2, int v3);

	// void glProgramUniform4iv(int program, int location, int count, int[] value, int offset)
	// void glProgramUniform4iv(int program, int location, int count, IntBuffer value)
	public void glProgramUniform4ui (int program, int location, int v0, int v1, int v2, int v3);

	// void glProgramUniform4uiv(int program, int location, int count, int[] value, int offset)
	public void glProgramUniform4uiv (int program, int location, int count, IntBuffer value);

	// void glProgramUniformMatrix2fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix2fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix2x3fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix2x3fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix2x4fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix2x4fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix3fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix3fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix3x2fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix3x2fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix3x4fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix3x4fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix4fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix4fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix4x2fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix4x2fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	// void glProgramUniformMatrix4x3fv(int program, int location, int count, boolean transpose, float[] value, int offset)
	public void glProgramUniformMatrix4x3fv (int program, int location, int count, boolean transpose, FloatBuffer value);

	public void glSampleMaski (int maskNumber, int mask);

	public void glTexStorage2DMultisample (int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations);

	public void glUseProgramStages (int pipeline, int stages, int program);

	public void glValidateProgramPipeline (int pipeline);

	public void glVertexAttribBinding (int attribindex, int bindingindex);

	public void glVertexAttribFormat (int attribindex, int size, int type, boolean normalized, int relativeoffset);

	public void glVertexAttribIFormat (int attribindex, int size, int type, int relativeoffset);	

	public void glVertexBindingDivisor(int bindingindex, int divisor);
}
