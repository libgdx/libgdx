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

package com.badlogic.gdx.graphics.benchmark;

/** This class will collect statistics about the GL calls. All calls to it will get counted and delegated to the actual GL20 or
 * GL30 instance.
 * @author Daniel Holderbaum */
public class GLBenchmark {

	/** All calls to any GL function since the last reset. */
	public int calls;

	/** The amount of times a texture binding has happened since the last reset. */
	public int textureBinds;

	/** The amount of draw calls that happened since the last reset. */
	public int drawCalls;

	/** The amount of times a shader was switched since the last reset. */
	public int shaderSwitches;

	/** Will reset the statistical information which has been collected so far. This should be called after every frame. */
	public void reset () {
		calls = 0;
		textureBinds = 0;
		drawCalls = 0;
		shaderSwitches = 0;
	}

}
