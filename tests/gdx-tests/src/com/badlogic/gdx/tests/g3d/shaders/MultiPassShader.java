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

package com.badlogic.gdx.tests.g3d.shaders;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

public class MultiPassShader extends DefaultShader {
	public static int passes = 10;

	protected final int u_pass = register(new Uniform("u_pass"));

	public MultiPassShader (final Renderable renderable, final Config config) {
		super(renderable, config);
	}

	@Override
	public void render (Renderable renderable) {
		set(u_pass, 0f);
		super.render(renderable);
		context.setDepthTest(GL20.GL_LESS);
		if (has(u_pass)) {
			context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			for (int i = 1; i < passes; ++i) {
				set(u_pass, (float)i / (float)passes);
				renderable.meshPart.render(program, false);
			}
		}
	}
}
