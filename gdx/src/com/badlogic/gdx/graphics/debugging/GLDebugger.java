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

package com.badlogic.gdx.graphics.debugging;

import static com.badlogic.gdx.graphics.GL20.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Enabling this will wrap Gdx.gl20 and Gdx.gl30 instances into GLDebugger subclasses that check for GL errors after each GL call,
 * using glGetError. When error happens, {@link GLDebugger#listener}.onError(int) is called inside GL call, so the stack trace may
 * be inspected.
 *
 * @see GL30Debugger
 * @see GL20Debugger
 * @see com.badlogic.gdx.graphics.profiling.GLProfiler
 * @author Jan Polák */
public abstract class GLDebugger {

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

	/** This listener will be called when GLDebugger is enabled and any GL call sets error number (retrievable by glGetError call).
	 *
	 * Default is {@link GLDebuggerErrorListener#LOGGING_LISTENER}. */
	public static GLDebuggerErrorListener listener = GLDebuggerErrorListener.LOGGING_LISTENER;

	/** Enables error checking by replacing the {@code GL20} and {@code GL30} instances with debugging ones. */
	public static void enable () {
		if (!isEnabled()) {
			Gdx.gl30 = Gdx.gl30 == null ? null : new GL30Debugger(Gdx.gl30);
			Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : new GL20Debugger(Gdx.gl20);
			Gdx.gl = Gdx.gl20;
		}
	}

	/** Disables error checking by resetting the {@code GL20} and {@code GL30} instances with the original ones. */
	public static void disable () {
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GL30Debugger) Gdx.gl30 = ((GL30Debugger)Gdx.gl30).gl30;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GL20Debugger) Gdx.gl20 = ((GL20Debugger)Gdx.gl).gl20;
		if (Gdx.gl != null && Gdx.gl instanceof GL20Debugger) Gdx.gl = ((GL20Debugger)Gdx.gl).gl20;
	}

	/** @return Whether error checking is currently enabled */
	public static boolean isEnabled () {
		return Gdx.gl30 instanceof GL30Debugger || Gdx.gl20 instanceof GL20Debugger;
	}
}
