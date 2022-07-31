/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

package com.badlogic.gdx.backends.lwjgl3;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.opengl.KHRBlendEquationAdvanced;
import org.lwjgl.system.MemoryUtil;

import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.GL32.DebugProc;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Lwjgl3GL32 extends Lwjgl3GL31 implements GL32 {

	private static final PointerBuffer pb = PointerBuffer.allocateDirect(16);

	@Override
	public void glBlendBarrier () {
		// when available, this extension is enabled by default.
		// see https://registry.khronos.org/OpenGL/extensions/KHR/KHR_blend_equation_advanced.txt
		KHRBlendEquationAdvanced.glBlendBarrierKHR();
	}

	@Override
	public void glCopyImageSubData (int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName,
		int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
		GL43.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ,
			srcWidth, srcHeight, srcDepth);
	}

	@Override
	public void glDebugMessageControl (int source, int type, int severity, IntBuffer ids, boolean enabled) {
		GL43.glDebugMessageControl(source, type, severity, ids, enabled);
	}

	@Override
	public void glDebugMessageInsert (int source, int type, int id, int severity, String buf) {
		GL43.glDebugMessageInsert(source, type, id, severity, buf);
	}

	@Override
	public void glDebugMessageCallback (DebugProc callback) {
		if (callback != null) {
			GL43.glDebugMessageCallback(new GLDebugMessageCallbackI() {
				@Override
				public void invoke (int source, int type, int id, int severity, int length, long message, long userParam) {
					callback.onMessage(source, type, id, severity, MemoryUtil.memUTF8(message, length));
				}
			}, 0);
		} else {
			GL43.glDebugMessageCallback(null, 0);
		}
	}

	@Override
	public int glGetDebugMessageLog (int count, IntBuffer sources, IntBuffer types, IntBuffer ids, IntBuffer severities,
		IntBuffer lengths, ByteBuffer messageLog) {
		return GL43.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
	}

	@Override
	public void glPushDebugGroup (int source, int id, String message) {
		GL43.glPushDebugGroup(source, id, message);
	}

	@Override
	public void glPopDebugGroup () {
		GL43.glPopDebugGroup();
	}

	@Override
	public void glObjectLabel (int identifier, int name, String label) {
		GL43.glObjectLabel(identifier, name, label);
	}

	@Override
	public String glGetObjectLabel (int identifier, int name) {
		return GL43.glGetObjectLabel(identifier, name);
	}

	@Override
	public long glGetPointerv (int pname) {
		pb.reset();
		GL43.glGetPointerv(pname, pb);
		return pb.get();
	}

	@Override
	public void glEnablei (int target, int index) {
		GL30.glEnablei(target, index);
	}

	@Override
	public void glDisablei (int target, int index) {
		GL30.glDisablei(target, index);
	}

	@Override
	public void glBlendEquationi (int buf, int mode) {
		GL40.glBlendEquationi(buf, mode);
	}

	@Override
	public void glBlendEquationSeparatei (int buf, int modeRGB, int modeAlpha) {
		GL40.glBlendEquationSeparatei(buf, modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFunci (int buf, int src, int dst) {
		GL40.glBlendFunci(buf, src, dst);
	}

	@Override
	public void glBlendFuncSeparatei (int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GL40.glBlendFuncSeparatei(buf, srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glColorMaski (int index, boolean r, boolean g, boolean b, boolean a) {
		GL30.glColorMaski(index, r, g, b, a);
	}

	@Override
	public boolean glIsEnabledi (int target, int index) {
		return GL30.glIsEnabledi(target, index);
	}

	@Override
	public void glDrawElementsBaseVertex (int mode, int count, int type, Buffer indices, int basevertex) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = (ShortBuffer)indices;
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsBaseVertex(mode, sb, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = ((ByteBuffer)indices).asShortBuffer();
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsBaseVertex(mode, sb, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE) {
			ByteBuffer bb = (ByteBuffer)indices;
			int position = bb.position();
			int oldLimit = bb.limit();
			bb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsBaseVertex(mode, bb, basevertex);
			bb.limit(oldLimit);
		} else
			throw new GdxRuntimeException(
				"Can't use " + indices.getClass().getName() + " with this method. Use ShortBuffer or ByteBuffer instead.");
	}

	@Override
	public void glDrawRangeElementsBaseVertex (int mode, int start, int end, int count, int type, Buffer indices, int basevertex) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = (ShortBuffer)indices;
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawRangeElementsBaseVertex(mode, start, end, sb, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = ((ByteBuffer)indices).asShortBuffer();
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawRangeElementsBaseVertex(mode, start, end, sb, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE) {
			ByteBuffer bb = (ByteBuffer)indices;
			int position = bb.position();
			int oldLimit = bb.limit();
			bb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawRangeElementsBaseVertex(mode, start, end, bb, basevertex);
			bb.limit(oldLimit);
		} else
			throw new GdxRuntimeException(
				"Can't use " + indices.getClass().getName() + " with this method. Use ShortBuffer or ByteBuffer instead.");
	}

	@Override
	public void glDrawElementsInstancedBaseVertex (int mode, int count, int type, Buffer indices, int instanceCount,
		int basevertex) {
		if (indices instanceof ShortBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = (ShortBuffer)indices;
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsInstancedBaseVertex(mode, sb, instanceCount, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_SHORT) {
			ShortBuffer sb = ((ByteBuffer)indices).asShortBuffer();
			int position = sb.position();
			int oldLimit = sb.limit();
			sb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsInstancedBaseVertex(mode, sb, instanceCount, basevertex);
			sb.limit(oldLimit);
		} else if (indices instanceof ByteBuffer && type == com.badlogic.gdx.graphics.GL20.GL_UNSIGNED_BYTE) {
			ByteBuffer bb = (ByteBuffer)indices;
			int position = bb.position();
			int oldLimit = bb.limit();
			bb.limit(position + count);
			org.lwjgl.opengl.GL32.glDrawElementsInstancedBaseVertex(mode, bb, instanceCount, basevertex);
			bb.limit(oldLimit);
		} else
			throw new GdxRuntimeException(
				"Can't use " + indices.getClass().getName() + " with this method. Use ShortBuffer or ByteBuffer instead.");
	}

	@Override
	public void glDrawElementsInstancedBaseVertex (int mode, int count, int type, int indicesOffset, int instanceCount,
		int basevertex) {
		org.lwjgl.opengl.GL32.glDrawElementsInstancedBaseVertex(mode, count, type, indicesOffset, instanceCount, basevertex);
	}

	@Override
	public void glFramebufferTexture (int target, int attachment, int texture, int level) {
		org.lwjgl.opengl.GL32.glFramebufferTexture(target, attachment, texture, level);
	}

	@Override
	public int glGetGraphicsResetStatus () {
		return GL45.glGetGraphicsResetStatus();
	}

	@Override
	public void glReadnPixels (int x, int y, int width, int height, int format, int type, int bufSize, Buffer data) {
		if (data == null) {
			GL45.glReadnPixels(x, y, width, height, format, type, bufSize, 0L);
		} else {
			int oldLimit = data.limit();
			data.limit(bufSize);
			if (data instanceof ByteBuffer) {
				GL45.glReadnPixels(x, y, width, height, format, type, (ByteBuffer)data);
			} else if (data instanceof IntBuffer) {
				GL45.glReadnPixels(x, y, width, height, format, type, (IntBuffer)data);
			} else if (data instanceof ShortBuffer) {
				GL45.glReadnPixels(x, y, width, height, format, type, (ShortBuffer)data);
			} else if (data instanceof FloatBuffer) {
				GL45.glReadnPixels(x, y, width, height, format, type, (FloatBuffer)data);
			} else {
				throw new GdxRuntimeException("buffer type not supported");
			}
			data.limit(oldLimit);
		}
	}

	@Override
	public void glGetnUniformfv (int program, int location, FloatBuffer params) {
		GL45.glGetnUniformfv(program, location, params);
	}

	@Override
	public void glGetnUniformiv (int program, int location, IntBuffer params) {
		GL45.glGetnUniformiv(program, location, params);
	}

	@Override
	public void glGetnUniformuiv (int program, int location, IntBuffer params) {
		GL45.glGetnUniformuiv(program, location, params);
	}

	@Override
	public void glMinSampleShading (float value) {
		GL40.glMinSampleShading(value);
	}

	@Override
	public void glPatchParameteri (int pname, int value) {
		GL40.glPatchParameteri(pname, value);
	}

	@Override
	public void glTexParameterIiv (int target, int pname, IntBuffer params) {
		GL30.glTexParameterIiv(target, pname, params);
	}

	@Override
	public void glTexParameterIuiv (int target, int pname, IntBuffer params) {
		GL30.glTexParameterIuiv(target, pname, params);
	}

	@Override
	public void glGetTexParameterIiv (int target, int pname, IntBuffer params) {
		GL30.glGetTexParameterIiv(target, pname, params);
	}

	@Override
	public void glGetTexParameterIuiv (int target, int pname, IntBuffer params) {
		GL30.glGetTexParameterIuiv(target, pname, params);
	}

	@Override
	public void glSamplerParameterIiv (int sampler, int pname, IntBuffer param) {
		GL33.glSamplerParameterIiv(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterIuiv (int sampler, int pname, IntBuffer param) {
		GL33.glSamplerParameterIuiv(sampler, pname, param);
	}

	@Override
	public void glGetSamplerParameterIiv (int sampler, int pname, IntBuffer params) {
		GL33.glGetSamplerParameterIiv(sampler, pname, params);
	}

	@Override
	public void glGetSamplerParameterIuiv (int sampler, int pname, IntBuffer params) {
		GL33.glGetSamplerParameterIuiv(sampler, pname, params);
	}

	@Override
	public void glTexBuffer (int target, int internalformat, int buffer) {
		GL31.glTexBuffer(target, internalformat, buffer);
	}

	@Override
	public void glTexBufferRange (int target, int internalformat, int buffer, int offset, int size) {
		GL43.glTexBufferRange(target, internalformat, buffer, offset, size);
	}

	@Override
	public void glTexStorage3DMultisample (int target, int samples, int internalformat, int width, int height, int depth,
		boolean fixedsamplelocations) {
		GL43.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
	}

}
