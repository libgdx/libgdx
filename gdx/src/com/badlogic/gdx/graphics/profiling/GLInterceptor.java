/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.badlogic.gdx.graphics.profiling;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.FloatCounter;

public abstract class GLInterceptor implements GL20 {

	protected int calls;
	protected int textureBindings;
	protected int drawCalls;
	protected int shaderSwitches;
	protected final FloatCounter vertexCount = new FloatCounter(0);

	protected GLProfiler glProfiler;

	protected GLInterceptor (GLProfiler profiler) {
		this.glProfiler = profiler;
	}

	public static String resolveErrorNumber (int error) {
		switch (error) {
		case GL_INVALID_VALUE:
			return "GL_INVALID_VALUE";
		case GL_INVALID_OPERATION:
			return "GL_INVALID_OPERATION";
		case GL_INVALID_FRAMEBUFFER_OPERATION:
			return "GL_INVALID_FRAMEBUFFER_OPERATION";
		case GL_INVALID_ENUM:
			return "GL_INVALID_ENUM";
		case GL_OUT_OF_MEMORY:
			return "GL_OUT_OF_MEMORY";
		default:
			return "number " + error;
		}
	}

	public int getCalls () {
		return calls;
	}

	public int getTextureBindings () {
		return textureBindings;
	}

	public int getDrawCalls () {
		return drawCalls;
	}

	public int getShaderSwitches () {
		return shaderSwitches;
	}

	public FloatCounter getVertexCount () {
		return vertexCount;
	}

	public void reset () {
		calls = 0;
		textureBindings = 0;
		drawCalls = 0;
		shaderSwitches = 0;
		vertexCount.reset();
	}
}
