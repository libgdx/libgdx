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
import com.badlogic.gdx.graphics.GL20;

/** This class will collect statistics about the GL calls. All calls to it will get counted and delegated to the actual GL20 or
 * GL30 instance.
 * 
 * @see GL20Profiler
 * @see GL30Profiler
 * 
 * @author Daniel Holderbaum */
public abstract class GLProfiler {

	/** All calls to any GL function since the last reset. */
	protected int calls;

	/** The amount of times a texture binding has happened since the last reset. */
	protected int textureBindings;

	/** The amount of draw calls that happened since the last reset. */
	protected int drawCalls;

	/** The amount of times a shader was switched since the last reset. */
	protected int shaderSwitches;

	/** The amount rendered vertices since the last reset. */
	protected int vertexCount;

	/** The amount rendered primitives like {@code GL_POINTS, GL_LINES, GL_TRIANGLES, GL_LINE_STRIP, ...} since the last reset. */
	protected int primitiveCount;

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
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) ((GLProfiler)Gdx.gl30).resetVariables();
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) ((GLProfiler)Gdx.gl).resetVariables();
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) ((GLProfiler)Gdx.gl).resetVariables();
	}

	public static int getGLCalls () {
		int calls = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) calls += ((GLProfiler)Gdx.gl).calls;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) calls += ((GLProfiler)Gdx.gl20).calls;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) calls += ((GLProfiler)Gdx.gl30).calls;
		return calls;
	}

	public static int getShaderSwitches () {
		int shaderSwitches = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) shaderSwitches += ((GLProfiler)Gdx.gl).shaderSwitches;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) shaderSwitches += ((GLProfiler)Gdx.gl20).shaderSwitches;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) shaderSwitches += ((GLProfiler)Gdx.gl30).shaderSwitches;
		return shaderSwitches;
	}

	public static int getTextureBindings () {
		int textureBindings = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) textureBindings += ((GLProfiler)Gdx.gl).textureBindings;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) textureBindings += ((GLProfiler)Gdx.gl20).textureBindings;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) textureBindings += ((GLProfiler)Gdx.gl30).textureBindings;
		return textureBindings;
	}

	public static int getVertexCount () {
		int triCount = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl).vertexCount;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl20).vertexCount;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl30).vertexCount;
		return triCount;
	}

	public static int getPrimitiveCount () {
		int triCount = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl).primitiveCount;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl20).primitiveCount;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) triCount += ((GLProfiler)Gdx.gl30).primitiveCount;
		return triCount;
	}

	public static int getDrawCalls () {
		int drawCalls = 0;
		if (Gdx.gl != null && Gdx.gl instanceof GLProfiler) drawCalls += ((GLProfiler)Gdx.gl).drawCalls;
		if (Gdx.gl20 != null && Gdx.gl20 instanceof GLProfiler) drawCalls += ((GLProfiler)Gdx.gl20).drawCalls;
		if (Gdx.gl30 != null && Gdx.gl30 instanceof GLProfiler) drawCalls += ((GLProfiler)Gdx.gl30).drawCalls;
		return drawCalls;
	}

	protected void resetVariables () {
		calls = 0;
		textureBindings = 0;
		drawCalls = 0;
		shaderSwitches = 0;
		vertexCount = 0;
		primitiveCount = 0;
	}

	protected int calculatePrimitiveCount (int mode, int count) {
		switch (mode) {
		case GL20.GL_POINTS:
			return count;
		case GL20.GL_LINES:
			return count / 2;
		case GL20.GL_LINE_STRIP:
			return count - 1;
		case GL20.GL_LINE_LOOP:
			return count;
		case GL20.GL_TRIANGLE_STRIP:
			return count - 2;
		case GL20.GL_TRIANGLE_FAN:
			return count - 2;
		case GL20.GL_TRIANGLES:
			return count / 3;
// case GL20.GL_QUAD_STRIP:
// return count / 2;
// case GL20.GL_QUADS:
// return count / 4;
// case GL20.GL_POLYGON:
// return 1;
		default:
			throw new IllegalArgumentException("Unknown primitive mode '" + mode + "'.");
		}
	}
}
