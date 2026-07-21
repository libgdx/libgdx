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

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class ParticleEmitterChangeSpriteTest extends GdxTest {
	private SpriteBatch spriteBatch;
	ParticleEffect effect;
	int emitterIndex = 0;
	Array<ParticleEmitter> emitters;
	TextureAtlas atlas;
	Array<Sprite> sprites;
	int currentSprite = 0;
	float fpsCounter;
	InputProcessor inputProcessor;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();

		atlas = new TextureAtlas("data/particles.atlas");

		int spriteCount = atlas.getRegions().size;
		sprites = new Array<Sprite>(spriteCount);
		for (TextureRegion region : atlas.getRegions()) {
			sprites.add(new Sprite(region));
		}

		effect = new ParticleEffect();
		effect.load(files.internal("data/test.p"), files.internal("data"));
		effect.setPosition(graphics.getWidth() / 2, graphics.getHeight() / 2);
		// Of course, a ParticleEffect is normally just used, without messing around with its emitters.
		emitters = new Array(effect.getEmitters());
		effect.getEmitters().clear();
		effect.getEmitters().add(emitters.get(0));

		inputProcessor = new InputAdapter() {

			public boolean touchDragged (int x, int y, int pointer) {
				effect.setPosition(x, graphics.getHeight() - y);
				return false;
			}

			public boolean touchDown (int x, int y, int pointer, int newParam) {
				ParticleEmitter emitter = emitters.get(emitterIndex);
				currentSprite = (currentSprite + 1) % sprites.size;
				emitter.setSprites(new Array<Sprite>(new Sprite[] {sprites.get(currentSprite)}));
				return false;
			}
		};

		input.setInputProcessor(inputProcessor);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		effect.dispose();
		atlas.dispose();
	}

	public void render () {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, graphics.getWidth(), graphics.getHeight());
		float delta = graphics.getDeltaTime();
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		effect.draw(spriteBatch, delta);
		spriteBatch.end();
		fpsCounter += delta;
		if (fpsCounter > 3) {
			fpsCounter = 0;
			app.log("libgdx", "current sprite: " + currentSprite + ", FPS: " + graphics.getFramesPerSecond());
		}
	}

	public boolean needsGL20 () {
		return false;
	}
}
