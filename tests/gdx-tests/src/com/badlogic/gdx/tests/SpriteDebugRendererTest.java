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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteDebugRenderer;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SpriteDebugRendererTest extends GdxTest {
	private final Color[] colors = {Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.PINK,
		Color.MAROON, Color.MAGENTA, Color.YELLOW, Color.RED};

	private SpriteDebugRenderer debugger;
	private Texture texture;
	private SpriteBatch batch;
	private Sprite sprite;
	private int index;
	private float scale = 1;
	private float rotation = 0;
	private float rotationSpeed = -25;
	private float scaleSpeed = -1;

	@Override
	public void create () {

		batch = new SpriteBatch();
		debugger = new SpriteDebugRenderer();

		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));

		sprite = new Sprite(texture);

		sprite.setX((Gdx.graphics.getWidth() / 2) - (texture.getWidth() / 2));
		sprite.setY((Gdx.graphics.getHeight() / 2) - (texture.getHeight() / 2));

		Gdx.input.setInputProcessor(new InputAdapter() {

			@Override
			public boolean touchDown (int screenX, int screenY, int pointer, int button) {
				debugger.enableFillMode(!debugger.isFillModeEnabled());

				return super.touchDown(screenX, screenY, pointer, button);
			}

		});
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float sx = sprite.getScaleX();
		float sy = sprite.getScaleY();

		scale += scaleSpeed * Gdx.graphics.getDeltaTime();
		rotation += rotationSpeed * Gdx.graphics.getDeltaTime();

		if (scale < 0.5f) {
			scale = 0.5f;
			scaleSpeed = 1;
			index = (index + 1) % colors.length;
		}

		if (scale > 1.0f) {
			scale = 1.0f;
			scaleSpeed = -1;
			index = (index + 1) % colors.length;
		}

		if (rotation < 180) {
			rotation = 180;
			rotationSpeed = 25;
		}

		if (rotation > 360) {
			rotation = 360;
			rotationSpeed = -25;
		}

		sprite.setRotation(rotation);
		sprite.setScale(scale);

		batch.begin();
		sprite.draw(batch);
		batch.end();

		debugger.begin();
		debugger.debugRender(sprite, colors[index]);
		debugger.end();
	}

	@Override
	public void dispose () {
		debugger.dispose();
		batch.dispose();
		texture.dispose();
	}
}
