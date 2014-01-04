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

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;

public class FrameBufferObject implements Disposable {

	private static IntBuffer singleInt = BufferUtils.newIntBuffer(1);

	private int glTarget;
	private int glHandle;

	private final IntBuffer colorDrawBuffers;

	public FrameBufferObject (int... colorBindings) {
		this.glTarget = GL30.GL_FRAMEBUFFER;
		singleInt.position(0);
		Gdx.gl20.glGenFramebuffers(1, singleInt);
		glHandle = singleInt.get(0);

		// prepare drawbuffers that will be used on bind
		colorDrawBuffers = BufferUtils.newIntBuffer(colorBindings.length);
		for (int i = 0; i < colorBindings.length; ++i)
			colorDrawBuffers.put(colorBindings[i]);
		colorDrawBuffers.position(0);
	}

	public void unbind () {
		Gdx.gl20.glBindFramebuffer(glTarget, 0);
	}

	public void bind () {
		Gdx.gl20.glBindFramebuffer(glTarget, glHandle);
		Gdx.gl30.glDrawBuffers(colorDrawBuffers.capacity(), colorDrawBuffers);
	}

	@Override
	public void dispose () {
		singleInt.position(0);
		singleInt.put(glHandle);
		singleInt.position(0);
		Gdx.gl20.glDeleteFramebuffers(1, singleInt);
	}
}
