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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.FloatCounter;

/** This class will collect statistics about the GL calls. All calls to it will get counted and delegated to the actual GL20 or
 * GL30 instance.
 * 
 * @see GL20Profiler
 * @see GL30Profiler
 * 
 * @author Daniel Holderbaum */
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
	public static FloatCounter vertexCount = new FloatCounter(0);

	/** Enables profiling by replacing the {@code GL20} and {@code GL30} instances with profiling ones. */
	public static void enable () {
		Gdx.gl30 = Gdx.gl30 == null ? null : new GL30Profiler(Gdx.gl30);
		Gdx.gl20 = Gdx.gl30 != null ? Gdx.gl30 : new GL20Profiler(Gdx.gl20);
		Gdx.gl = Gdx.gl20;
	}

	/** Disables profiling by resetting the {@code GL20} and {@code GL30} instances with the original ones. */
	public static void disable () {
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GL30Profiler) Gdx.gl30 = ((GL30Profiler)Gdx.gl30).gl30;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GL20Profiler) Gdx.gl20 = ((GL20Profiler)Gdx.gl).gl20;
		if (Gdx.gl != null && Gdx.gl instanceof GL20Profiler) Gdx.gl = ((GL20Profiler)Gdx.gl).gl20;
	}

	/** Will reset the statistical information which has been collected so far. This should be called after every frame. */
	public static void reset () {
		calls = 0;
		textureBindings = 0;
		drawCalls = 0;
		shaderSwitches = 0;
		vertexCount.reset();
	}

}
