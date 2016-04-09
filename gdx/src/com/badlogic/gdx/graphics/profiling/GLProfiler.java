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

package com.badlogic.gdx.graphics.profiling;

import static com.badlogic.gdx.graphics.GL20.GL_INVALID_ENUM;
import static com.badlogic.gdx.graphics.GL20.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.badlogic.gdx.graphics.GL20.GL_INVALID_OPERATION;
import static com.badlogic.gdx.graphics.GL20.GL_INVALID_VALUE;
import static com.badlogic.gdx.graphics.GL20.GL_OUT_OF_MEMORY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.FloatCounter;

/** When enabled, collects statistics about GL calls and checks for GL errors.
 * Enabling will wrap Gdx.gl* instances with delegate classes which provide described functionality
 * and route GL calls to the actual GL instances.
 * 
 * @see GL20Profiler
 * @see GL30Profiler
 * 
 * @author Daniel Holderbaum
 * @author Jan Polák */
public abstract class GLProfiler {

	/** All calls to any GL function since the last reset. */
	public static int calls;

	/** The amount of times a texture binding has happened since the last reset. */
	public static int textureBindings;

	/** The amount of draw calls that happened since the last reset. */
	public static int drawCalls;

	/** The amount of times a shader was switched since the last reset. */
	public static int shaderSwitches;

	/** The amount rendered vertices since the last reset. */
	public static final FloatCounter vertexCount = new FloatCounter(0);

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

	/** This listener will be called when GLProfiler is enabled and any GL call sets an error number (retrievable by glGetError call).
	 *
	 * Default is {@link GLErrorListener#LOGGING_LISTENER}. */
	public static GLErrorListener listener = GLErrorListener.LOGGING_LISTENER;
	
	/** Enables profiling by replacing the {@code GL20} and {@code GL30} instances with profiling ones. */
	public static void enable () {
		if (!isEnabled()) {
			Gdx.gl30 = Gdx.gl30 == null ? null : new GL30Profiler(Gdx.gl30);
			Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : new GL20Profiler(Gdx.gl20);
			Gdx.gl = Gdx.gl20;
		}
	}

	/** Disables profiling by resetting the {@code GL20} and {@code GL30} instances with the original ones. */
	public static void disable () {
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GL30Profiler) Gdx.gl30 = ((GL30Profiler)Gdx.gl30).gl30;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GL20Profiler) Gdx.gl20 = ((GL20Profiler)Gdx.gl).gl20;
		if (Gdx.gl != null && Gdx.gl instanceof GL20Profiler) Gdx.gl = ((GL20Profiler)Gdx.gl).gl20;
	}
	
	/** @return Whether profiling is currently enabled */
	public static boolean isEnabled() {
		return Gdx.gl30 instanceof GL30Profiler || Gdx.gl20 instanceof GL20Profiler;
	}

	/** Will reset the statistical information which has been collected so far. This should be called after every frame.
	 * Error listener is kept as it is. */
	public static void reset () {
		calls = 0;
		textureBindings = 0;
		drawCalls = 0;
		shaderSwitches = 0;
		vertexCount.reset();
	}

}
