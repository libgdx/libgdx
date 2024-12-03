
package com.badlogic.gdx.backends.android;

import android.opengl.GLES31;

import com.badlogic.gdx.graphics.GL31;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AndroidGL31 extends AndroidGL30 implements GL31 {

	@Override
	public void glDispatchCompute (int num_groups_x, int num_groups_y, int num_groups_z) {
		GLES31.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
	}

	@Override
	public void glDispatchComputeIndirect (long indirect) {
		GLES31.glDispatchComputeIndirect(indirect);
	}

	@Override
	public void glDrawArraysIndirect (int mode, long indirect) {
		GLES31.glDrawArraysIndirect(mode, indirect);
	}

	@Override
	public void glDrawElementsIndirect (int mode, int type, long indirect) {
		GLES31.glDrawElementsIndirect(mode, type, indirect);
	}

	@Override
	public void glFramebufferParameteri (int target, int pname, int param) {
		GLES31.glDrawElementsIndirect(target, pname, param);
	}

	@Override
	public void glGetFramebufferParameteriv (int target, int pname, IntBuffer params) {
		GLES31.glGetFramebufferParameteriv(target, pname, params);
	}

	@Override
	public void glGetProgramInterfaceiv (int program, int programInterface, int pname, IntBuffer params) {
		GLES31.glGetProgramInterfaceiv(program, programInterface, pname, params);
	}

	@Override
	public int glGetProgramResourceIndex (int program, int programInterface, String name) {
		return GLES31.glGetProgramResourceIndex(program, programInterface, name);
	}

	@Override
	public String glGetProgramResourceName (int program, int programInterface, int index) {
		return GLES31.glGetProgramResourceName(program, programInterface, index);
	}

	@Override
	public void glGetProgramResourceiv (int program, int programInterface, int index, IntBuffer props, IntBuffer length,
		IntBuffer params) {
		GLES31.glGetProgramResourceiv(program, programInterface, index, props.remaining(), props, params.remaining(), length,
			params);
	}

	@Override
	public int glGetProgramResourceLocation (int program, int programInterface, String name) {
		return GLES31.glGetProgramResourceLocation(program, programInterface, name);
	}

	@Override
	public void glUseProgramStages (int pipeline, int stages, int program) {
		GLES31.glUseProgramStages(pipeline, stages, program);
	}

	@Override
	public void glActiveShaderProgram (int pipeline, int program) {
		GLES31.glActiveShaderProgram(pipeline, program);
	}

	@Override
	public int glCreateShaderProgramv (int type, String[] strings) {
		return GLES31.glCreateShaderProgramv(type, strings);
	}

	@Override
	public void glBindProgramPipeline (int pipeline) {
		GLES31.glBindProgramPipeline(pipeline);
	}

	@Override
	public void glDeleteProgramPipelines (int n, IntBuffer pipelines) {
		GLES31.glDeleteProgramPipelines(n, pipelines);
	}

	@Override
	public void glGenProgramPipelines (int n, IntBuffer pipelines) {
		GLES31.glGenProgramPipelines(n, pipelines);
	}

	@Override
	public boolean glIsProgramPipeline (int pipeline) {
		return GLES31.glIsProgramPipeline(pipeline);
	}

	@Override
	public void glGetProgramPipelineiv (int pipeline, int pname, IntBuffer params) {
		GLES31.glGetProgramPipelineiv(pipeline, pname, params);
	}

	@Override
	public void glProgramUniform1i (int program, int location, int v0) {
		GLES31.glProgramUniform1i(program, location, v0);
	}

	@Override
	public void glProgramUniform2i (int program, int location, int v0, int v1) {
		GLES31.glProgramUniform2i(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3i (int program, int location, int v0, int v1, int v2) {
		GLES31.glProgramUniform3i(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4i (int program, int location, int v0, int v1, int v2, int v3) {
		GLES31.glProgramUniform4i(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1ui (int program, int location, int v0) {
		GLES31.glProgramUniform1ui(program, location, v0);
	}

	@Override
	public void glProgramUniform2ui (int program, int location, int v0, int v1) {
		GLES31.glProgramUniform2ui(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3ui (int program, int location, int v0, int v1, int v2) {
		GLES31.glProgramUniform3ui(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4ui (int program, int location, int v0, int v1, int v2, int v3) {
		GLES31.glProgramUniform4ui(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1f (int program, int location, float v0) {
		GLES31.glProgramUniform1f(program, location, v0);
	}

	@Override
	public void glProgramUniform2f (int program, int location, float v0, float v1) {
		GLES31.glProgramUniform2f(program, location, v0, v1);
	}

	@Override
	public void glProgramUniform3f (int program, int location, float v0, float v1, float v2) {
		GLES31.glProgramUniform3f(program, location, v0, v1, v2);
	}

	@Override
	public void glProgramUniform4f (int program, int location, float v0, float v1, float v2, float v3) {
		GLES31.glProgramUniform4f(program, location, v0, v1, v2, v3);
	}

	@Override
	public void glProgramUniform1iv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform1iv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform2iv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform2iv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform3iv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform3iv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform4iv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform4iv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform1uiv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform1uiv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform2uiv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform2uiv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform3uiv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform3uiv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform4uiv (int program, int location, IntBuffer value) {
		GLES31.glProgramUniform4uiv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform1fv (int program, int location, FloatBuffer value) {
		GLES31.glProgramUniform1fv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform2fv (int program, int location, FloatBuffer value) {
		GLES31.glProgramUniform2fv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform3fv (int program, int location, FloatBuffer value) {
		GLES31.glProgramUniform3fv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniform4fv (int program, int location, FloatBuffer value) {
		GLES31.glProgramUniform4fv(program, location, value.remaining(), value);
	}

	@Override
	public void glProgramUniformMatrix2fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix2fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix3fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix4fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix2x3fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix2x3fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3x2fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix3x2fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix2x4fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix2x4fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4x2fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix4x2fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix3x4fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix3x4fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glProgramUniformMatrix4x3fv (int program, int location, boolean transpose, FloatBuffer value) {
		GLES31.glProgramUniformMatrix4x3fv(program, location, value.remaining(), transpose, value);
	}

	@Override
	public void glValidateProgramPipeline (int pipeline) {
		GLES31.glValidateProgramPipeline(pipeline);
	}

	@Override
	public String glGetProgramPipelineInfoLog (int program) {
		return GLES31.glGetProgramPipelineInfoLog(program);
	}

	@Override
	public void glBindImageTexture (int unit, int texture, int level, boolean layered, int layer, int access, int format) {
		GLES31.glBindImageTexture(unit, texture, level, layered, layer, access, format);
	}

	@Override
	public void glGetBooleani_v (int target, int index, IntBuffer data) {
		GLES31.glGetBooleani_v(target, index, data);
	}

	@Override
	public void glMemoryBarrier (int barriers) {
		GLES31.glMemoryBarrier(barriers);
	}

	@Override
	public void glMemoryBarrierByRegion (int barriers) {
		GLES31.glMemoryBarrierByRegion(barriers);
	}

	@Override
	public void glTexStorage2DMultisample (int target, int samples, int internalformat, int width, int height,
		boolean fixedsamplelocations) {
		GLES31.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
	}

	@Override
	public void glGetMultisamplefv (int pname, int index, FloatBuffer val) {
		GLES31.glGetMultisamplefv(pname, index, val);
	}

	@Override
	public void glSampleMaski (int maskNumber, int mask) {
		GLES31.glSampleMaski(maskNumber, mask);
	}

	@Override
	public void glGetTexLevelParameteriv (int target, int level, int pname, IntBuffer params) {
		GLES31.glGetTexLevelParameteriv(target, level, pname, params);
	}

	@Override
	public void glGetTexLevelParameterfv (int target, int level, int pname, FloatBuffer params) {
		GLES31.glGetTexLevelParameterfv(target, level, pname, params);
	}

	@Override
	public void glBindVertexBuffer (int bindingindex, int buffer, long offset, int stride) {
		GLES31.glBindVertexBuffer(bindingindex, buffer, offset, stride);
	}

	@Override
	public void glVertexAttribFormat (int attribindex, int size, int type, boolean normalized, int relativeoffset) {
		GLES31.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
	}

	@Override
	public void glVertexAttribIFormat (int attribindex, int size, int type, int relativeoffset) {
		GLES31.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
	}

	@Override
	public void glVertexAttribBinding (int attribindex, int bindingindex) {
		GLES31.glVertexAttribBinding(attribindex, bindingindex);
	}

	@Override
	public void glVertexBindingDivisor (int bindingindex, int divisor) {
		GLES31.glVertexBindingDivisor(bindingindex, divisor);
	}
}
