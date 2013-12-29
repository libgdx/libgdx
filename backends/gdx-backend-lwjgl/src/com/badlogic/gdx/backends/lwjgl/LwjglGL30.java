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

package com.badlogic.gdx.backends.lwjgl;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.utils.GdxRuntimeException;

final class LwjglGL30 extends LwjglGL20 implements com.badlogic.gdx.graphics.GL30 {

	public void glBindBufferBase (int target, int index, int buffer) {
		GL30.glBindBufferBase(target, index, buffer);
	}

	public void glGetActiveUniformBlockiv (int program, int uniformBlockIndex, int pname, IntBuffer params) {
		GL31.glGetActiveUniformBlock(program, uniformBlockIndex, pname, params);
	}

	public String glGetActiveUniformBlockName (int program, int uniformBlockIndex) {
		return GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, 256);
	}

	public void glGetActiveUniformsiv (int program, int uniformCount, IntBuffer uniformIndices, int pname, IntBuffer params) {
		GL31.glGetActiveUniforms(program, uniformIndices, pname, params);
		
	}

	public int glGetUniformBlockIndex (int program, String uniformBlockName) {
		return GL31.glGetUniformBlockIndex(program, uniformBlockName);
	}

	public void glUniformBlockBinding (int program, int uniformBlockIndex, int uniformBlockBinding) {
		GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
	}

	@Override
	public void glDrawArraysInstanced (int mode, int first, int count, int instancecount) {
		GL31.glDrawArraysInstanced(mode, first, instancecount, instancecount);
	}

	@Override
	public void glDrawElementsInstanced (int mode, int count, int type, long indices_buffer_offset, int instancecount) {
		GL31.glDrawElementsInstanced(mode, count, type, indices_buffer_offset, instancecount);
	}
}
