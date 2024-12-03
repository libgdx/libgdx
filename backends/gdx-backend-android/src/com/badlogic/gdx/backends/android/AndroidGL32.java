/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.opengl.GLES32;

import com.badlogic.gdx.graphics.GL32;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@TargetApi(24)
public class AndroidGL32 extends AndroidGL31 implements GL32 {

	@Override
	public void glBlendBarrier () {
		GLES32.glBlendBarrier();
	}

	@Override
	public void glCopyImageSubData (int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName,
		int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
		GLES32.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ,
			srcWidth, srcHeight, srcDepth);
	}

	@Override
	public void glDebugMessageControl (int source, int type, int severity, IntBuffer ids, boolean enabled) {
		GLES32.glDebugMessageControl(source, type, severity, ids.remaining(), ids, enabled);
	}

	@Override
	public void glDebugMessageInsert (int source, int type, int id, int severity, String buf) {
		GLES32.glDebugMessageInsert(source, type, id, severity, buf.length(), buf);
	}

	@Override
	public void glDebugMessageCallback (final DebugProc callback) {
		GLES32.glDebugMessageCallback(new GLES32.DebugProc() {
			@Override
			public void onMessage (int source, int type, int id, int severity, String message) {
				callback.onMessage(source, type, id, severity, message);
			}
		});
	}

	@Override
	public int glGetDebugMessageLog (int count, IntBuffer sources, IntBuffer types, IntBuffer ids, IntBuffer severities,
		IntBuffer lengths, ByteBuffer messageLog) {
		return GLES32.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
	}

	@Override
	public void glPushDebugGroup (int source, int id, String message) {
		GLES32.glPushDebugGroup(source, id, message.length(), message);
	}

	@Override
	public void glPopDebugGroup () {
		GLES32.glPopDebugGroup();
	}

	@Override
	public void glObjectLabel (int identifier, int name, String label) {
		GLES32.glObjectLabel(identifier, name, label.length(), label);
	}

	@Override
	public String glGetObjectLabel (int identifier, int name) {
		return GLES32.glGetObjectLabel(identifier, name);
	}

	@Override
	public long glGetPointerv (int pname) {
		return GLES32.glGetPointerv(pname);
	}

	@Override
	public void glEnablei (int target, int index) {
		GLES32.glEnablei(target, index);
	}

	@Override
	public void glDisablei (int target, int index) {
		GLES32.glDisablei(target, index);
	}

	@Override
	public void glBlendEquationi (int buf, int mode) {
		GLES32.glBlendEquationi(buf, mode);
	}

	@Override
	public void glBlendEquationSeparatei (int buf, int modeRGB, int modeAlpha) {
		GLES32.glBlendEquationSeparatei(buf, modeRGB, modeAlpha);
	}

	@Override
	public void glBlendFunci (int buf, int src, int dst) {
		GLES32.glBlendFunci(buf, src, dst);
	}

	@Override
	public void glBlendFuncSeparatei (int buf, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
		GLES32.glBlendFuncSeparatei(buf, srcRGB, dstRGB, srcAlpha, dstAlpha);
	}

	@Override
	public void glColorMaski (int index, boolean r, boolean g, boolean b, boolean a) {
		GLES32.glColorMaski(index, r, g, b, a);
	}

	@Override
	public boolean glIsEnabledi (int target, int index) {
		return GLES32.glIsEnabledi(target, index);
	}

	@Override
	public void glDrawElementsBaseVertex (int mode, int count, int type, Buffer indices, int basevertex) {
		GLES32.glDrawElementsBaseVertex(mode, count, type, indices, basevertex);
	}

	@Override
	public void glDrawRangeElementsBaseVertex (int mode, int start, int end, int count, int type, Buffer indices, int basevertex) {
		GLES32.glDrawRangeElementsBaseVertex(mode, start, end, count, type, indices, basevertex);
	}

	@Override
	public void glDrawElementsInstancedBaseVertex (int mode, int count, int type, Buffer indices, int instanceCount,
		int basevertex) {
		GLES32.glDrawElementsInstancedBaseVertex(mode, count, type, indices, instanceCount, basevertex);
	}

	@Override
	public void glDrawElementsInstancedBaseVertex (int mode, int count, int type, int indicesOffset, int instanceCount,
		int basevertex) {
		GLES32.glDrawElementsInstancedBaseVertex(mode, count, type, indicesOffset, instanceCount, basevertex);
	}

	@Override
	public void glFramebufferTexture (int target, int attachment, int texture, int level) {
		GLES32.glFramebufferTexture(target, attachment, texture, level);
	}

	@Override
	public int glGetGraphicsResetStatus () {
		return GLES32.glGetGraphicsResetStatus();
	}

	@Override
	public void glReadnPixels (int x, int y, int width, int height, int format, int type, int bufSize, Buffer data) {
		GLES32.glReadnPixels(x, y, width, height, format, type, bufSize, data);
	}

	@Override
	public void glGetnUniformfv (int program, int location, FloatBuffer params) {
		GLES32.glGetnUniformfv(program, location, params.remaining(), params);
	}

	@Override
	public void glGetnUniformiv (int program, int location, IntBuffer params) {
		GLES32.glGetnUniformiv(program, location, params.remaining(), params);
	}

	@Override
	public void glGetnUniformuiv (int program, int location, IntBuffer params) {
		GLES32.glGetnUniformuiv(program, location, params.remaining(), params);
	}

	@Override
	public void glMinSampleShading (float value) {
		GLES32.glMinSampleShading(value);
	}

	@Override
	public void glPatchParameteri (int pname, int value) {
		GLES32.glPatchParameteri(pname, value);
	}

	@Override
	public void glTexParameterIiv (int target, int pname, IntBuffer params) {
		GLES32.glTexParameterIiv(target, pname, params);
	}

	@Override
	public void glTexParameterIuiv (int target, int pname, IntBuffer params) {
		GLES32.glTexParameterIuiv(target, pname, params);
	}

	@Override
	public void glGetTexParameterIiv (int target, int pname, IntBuffer params) {
		GLES32.glGetTexParameterIiv(target, pname, params);
	}

	@Override
	public void glGetTexParameterIuiv (int target, int pname, IntBuffer params) {
		GLES32.glGetTexParameterIuiv(target, pname, params);
	}

	@Override
	public void glSamplerParameterIiv (int sampler, int pname, IntBuffer param) {
		GLES32.glSamplerParameterIiv(sampler, pname, param);
	}

	@Override
	public void glSamplerParameterIuiv (int sampler, int pname, IntBuffer param) {
		GLES32.glSamplerParameterIuiv(sampler, pname, param);
	}

	@Override
	public void glGetSamplerParameterIiv (int sampler, int pname, IntBuffer params) {
		GLES32.glGetSamplerParameterIiv(sampler, pname, params);
	}

	@Override
	public void glGetSamplerParameterIuiv (int sampler, int pname, IntBuffer params) {
		GLES32.glGetSamplerParameterIuiv(sampler, pname, params);
	}

	@Override
	public void glTexBuffer (int target, int internalformat, int buffer) {
		GLES32.glTexBuffer(target, internalformat, buffer);
	}

	@Override
	public void glTexBufferRange (int target, int internalformat, int buffer, int offset, int size) {
		GLES32.glTexBufferRange(target, internalformat, buffer, offset, size);
	}

	@Override
	public void glTexStorage3DMultisample (int target, int samples, int internalformat, int width, int height, int depth,
		boolean fixedsamplelocations) {
		GLES32.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
	}
}
