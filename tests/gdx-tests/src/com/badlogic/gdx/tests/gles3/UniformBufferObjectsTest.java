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

package com.badlogic.gdx.tests.gles3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.tests.utils.GdxTestConfig;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/** Added during WebGL2 implementation but also applicable to Desktop. UBO's were added in WebGL2, this test uses a UBO to send
 * color and position data to the shader using a buffer.
 * @author JamesTKhan */
@GdxTestConfig(requireGL30 = true)
public class UniformBufferObjectsTest extends GdxTest {

	Skin skin;
	Stage stage;
	Table table;

	RandomXS128 random;
	SpriteBatch batch;
	Texture texture;
	ShaderProgram shaderProgram;
	FloatBuffer uniformBuffer = BufferUtils.newFloatBuffer(16);

	float lerpToR = 1.0f;
	float lerpToG = 1.0f;
	float lerpToB = 1.0f;
	float elapsedTime = 0;

	@Override
	public void create () {
		random = new RandomXS128();
		batch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
		texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

		shaderProgram = new ShaderProgram(Gdx.files.internal("data/shaders/ubo.vert"), Gdx.files.internal("data/shaders/ubo.frag"));

		Gdx.app.log("UniformBufferObjectsTest", shaderProgram.getLog());
		if (shaderProgram.isCompiled()) {
			Gdx.app.log("UniformBufferObjectsTest", "Shader compiled");
			batch.setShader(shaderProgram);
		}

		IntBuffer tmpBuffer = BufferUtils.newIntBuffer(16);

		// Get the block index for the uniform block
		int blockIndex = Gdx.gl30.glGetUniformBlockIndex(shaderProgram.getHandle(), "u_bufferBlock");

		// Use the index to get the active block uniform count
		Gdx.gl30.glGetActiveUniformBlockiv(shaderProgram.getHandle(), blockIndex, GL30.GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, tmpBuffer);
		int activeUniforms = tmpBuffer.get(0);

		tmpBuffer.clear();
		Gdx.gl30.glGenBuffers(1, tmpBuffer);
		int bufferHandle = tmpBuffer.get(0);

		Gdx.gl.glBindBuffer(GL30.GL_UNIFORM_BUFFER, bufferHandle);
		Gdx.gl.glBufferData(GL30.GL_UNIFORM_BUFFER, 16, uniformBuffer, GL30.GL_STATIC_DRAW);

		int bindingPoint = 0;
		// Use the index to bind to a binding point, then bind the buffer
		Gdx.gl30.glUniformBlockBinding(shaderProgram.getHandle(), blockIndex, bindingPoint);
		Gdx.gl30.glBindBufferBase(GL30.GL_UNIFORM_BUFFER, bindingPoint, bufferHandle);

		// UI
		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		table = new Table();
		table.add(new Label("Block Uniforms (2 is expected):" + activeUniforms, skin)).row();
		table.add(new Label("Block Index (-1 is invalid): " + blockIndex, skin));
		stage.addActor(table);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		elapsedTime += Gdx.graphics.getDeltaTime();

		if (elapsedTime > 2f) {
			elapsedTime = 0;
			lerpToR = random.nextFloat();
			lerpToG = random.nextFloat();
			lerpToB = random.nextFloat();
		}

		// Update the colors
		uniformBuffer.put(0, Interpolation.smooth.apply(uniformBuffer.get(0), lerpToR, Gdx.graphics.getDeltaTime() * 2));// ColorBuffer.R
		uniformBuffer.put(1, Interpolation.smooth.apply(uniformBuffer.get(1), lerpToG, Gdx.graphics.getDeltaTime() * 2));// ColorBuffer.G
		uniformBuffer.put(2, Interpolation.smooth.apply(uniformBuffer.get(2), lerpToB, Gdx.graphics.getDeltaTime() * 2));// ColorBuffer.B

		// Update the positions
		uniformBuffer.put(4, Interpolation.smooth.apply(uniformBuffer.get(4), lerpToR, Gdx.graphics.getDeltaTime() * 2));// Position.X
		uniformBuffer.put(5, Interpolation.smooth.apply(uniformBuffer.get(5), lerpToG, Gdx.graphics.getDeltaTime() * 2));// Position.Y

		// Update the buffer data store
		Gdx.gl30.glBufferSubData(GL30.GL_UNIFORM_BUFFER, 0, uniformBuffer.capacity() * 4, uniformBuffer);

		batch.begin();
		batch.draw(texture, 0, 0, Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
		batch.end();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);

		table.setPosition(stage.getViewport().getScreenWidth() * .25f, stage.getViewport().getScreenHeight() * .95f);
	}

	@Override
	public void dispose () {
		texture.dispose();
		batch.dispose();
		shaderProgram.dispose();
	}
}
