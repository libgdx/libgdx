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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.tests.utils.GdxTest;

public class TextureAtlasTest extends GdxTest {
	SpriteBatch batch;
	Sprite badlogic, badlogicSmall, star;
	TextureAtlas atlas;
	TextureAtlas jumpAtlas;
	Animation jumpAnimation;
	BitmapFont font;
	float time = 0;
	ShapeRenderer renderer;

	public void create () {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();

		atlas = new TextureAtlas(Gdx.files.internal("data/pack"));
		jumpAtlas = new TextureAtlas(Gdx.files.internal("data/jump.txt"));

		jumpAnimation = new Animation(0.25f, jumpAtlas.findRegions("ALIEN_JUMP_"));

		badlogic = atlas.createSprite("badlogicslice");
		badlogic.setPosition(50, 50);

		// badlogicSmall = atlas.createSprite("badlogicsmall");
		badlogicSmall = atlas.createSprite("badlogicsmall-rotated");
		badlogicSmall.setPosition(10, 10);

		AtlasRegion region = atlas.findRegion("badlogicsmall");
		System.out.println("badlogicSmall original size: " + region.originalWidth + ", " + region.originalHeight);
		System.out.println("badlogicSmall packed size: " + region.packedWidth + ", " + region.packedHeight);

		star = atlas.createSprite("particle-star");
		star.setPosition(10, 70);

		font = new BitmapFont(Gdx.files.internal("data/font.fnt"), atlas.findRegion("font"), false);

		Gdx.gl.glClearColor(0, 1, 0, 1);

		Gdx.input.setInputProcessor(new InputAdapter() {
			public boolean keyUp (int keycode) {
				if (keycode == Keys.UP) {
					badlogicSmall.flip(false, true);
				} else if (keycode == Keys.RIGHT) {
					badlogicSmall.flip(true, false);
				} else if (keycode == Keys.LEFT) {
					badlogicSmall.setSize(512, 512);
				} else if (keycode == Keys.DOWN) {
					badlogicSmall.rotate90(true);
				}
				return super.keyUp(keycode);
			}
		});
	}

	public void render () {
		time += Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		renderer.begin(ShapeType.Line);
		renderer.rect(10, 10, 256, 256);
		renderer.end();

		batch.begin();
		// badlogic.draw(batch);
		// star.draw(batch);
		// font.draw(batch, "This font was packed!", 26, 65);
		badlogicSmall.draw(batch);
		// batch.draw(jumpAnimation.getKeyFrame(time, true), 100, 100);
		batch.end();
	}

	public void dispose () {
		atlas.dispose();
		jumpAtlas.dispose();
		batch.dispose();
		font.dispose();
	}
}
