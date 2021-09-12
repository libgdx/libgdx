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

package com.badlogic.gdx.tools.flame;

import com.badlogic.gdx.graphics.GL20;

enum BlendFunction {
	ZERO(GL20.GL_ZERO), ONE(GL20.GL_ONE), SRC_ALPHA(GL20.GL_SRC_ALPHA), SRC_COLOR(GL20.GL_SRC_COLOR), DST_ALPHA(
		GL20.GL_DST_ALPHA), DST_COLOR(GL20.GL_DST_COLOR), ONE_MINUS_SRC_COLOR(GL20.GL_ONE_MINUS_SRC_COLOR), ONE_MINUS_SRC_ALPHA(
			GL20.GL_ONE_MINUS_SRC_ALPHA), ONE_MINUS_DST_COLOR(
				GL20.GL_ONE_MINUS_DST_COLOR), ONE_MINUS_DST_ALPHA(GL20.GL_ONE_MINUS_DST_ALPHA);

	public int blend;

	private BlendFunction (int blend) {
		this.blend = blend;
	}

	public static BlendFunction find (int function) {
		for (BlendFunction func : values()) {
			if (func.blend == function) return func;
		}
		return null;
	}
}
