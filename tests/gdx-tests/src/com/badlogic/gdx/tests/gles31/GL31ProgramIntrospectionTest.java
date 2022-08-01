/*******************************************************************************
 * Copyright 2022 See AUTHORS file.
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

package com.badlogic.gdx.tests.gles31;

import java.nio.IntBuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL31;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;

/** see https://www.khronos.org/opengl/wiki/Program_Introspection#Interface_query
 * 
 * @author mgsx */
@GdxTestConfig(requireGL31 = true)
public class GL31ProgramIntrospectionTest extends GdxTest {
	@Override
	public void create () {
		ShaderProgram shader = SpriteBatch.createDefaultShader();
		int program = shader.getHandle();

		IntBuffer int1 = BufferUtils.newIntBuffer(1);

		Gdx.gl31.glGetProgramInterfaceiv(program, GL31.GL_UNIFORM, GL31.GL_ACTIVE_RESOURCES, int1);
		int nbRes = int1.get();

		for (int i = 0; i < nbRes; i++) {
			String name = Gdx.gl31.glGetProgramResourceName(program, GL31.GL_UNIFORM, i);
			Gdx.app.log("Gdx", name);
		}

		int u_texture_index = Gdx.gl31.glGetProgramResourceIndex(program, GL31.GL_UNIFORM, "u_texture");
		Gdx.app.log("Gdx", "u_texture: index:" + u_texture_index);

		int resLoc = Gdx.gl31.glGetProgramResourceLocation(program, GL31.GL_UNIFORM, "u_texture");
		Gdx.app.log("Gdx", "u_texture: loc:" + resLoc);

		IntBuffer props = BufferUtils.newIntBuffer(16);
		props.put(GL31.GL_TYPE);
		props.put(GL31.GL_LOCATION);
		props.put(GL31.GL_REFERENCED_BY_VERTEX_SHADER);
		props.put(GL31.GL_REFERENCED_BY_FRAGMENT_SHADER);
		props.flip();

		int1.clear();

		IntBuffer results = BufferUtils.newIntBuffer(16);

		Gdx.gl31.glGetProgramResourceiv(program, GL31.GL_UNIFORM, u_texture_index, props, int1, results);
		int count = int1.get();
		if (count == 4) {
			int type = results.get();
			int loc = results.get();
			boolean vs = results.get() == GL20.GL_TRUE;
			boolean fs = results.get() == GL20.GL_TRUE;
			boolean sampler2D = type == GL20.GL_SAMPLER_2D;
			Gdx.app.log("Gdx", "u_texture: sampler2D:" + sampler2D + " " + loc + " vertex:" + vs + " fragment:" + fs);
		} else {
			Gdx.app.error("Gdx", "result count mismatch");
		}

		shader.dispose();
	}
}
