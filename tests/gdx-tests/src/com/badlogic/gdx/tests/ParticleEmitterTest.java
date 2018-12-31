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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.Array;

public class ParticleEmitterTest extends GdxTest {
	private SpriteBatch spriteBatch;
	ParticleEffect effect;
	int emitterIndex;
	Array<ParticleEmitter> emitters;
	int particleCount = 10;
	float fpsCounter;
	InputProcessor inputProcessor;

	@Override
	public void create () {
		spriteBatch = new SpriteBatch();

		effect = new ParticleEffect();
		effect.load(Gdx.files.internal("data/test.p"), Gdx.files.internal("data"));
		effect.setPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		// Of course, a ParticleEffect is normally just used, without messing around with its emitters.
		emitters = new Array(effect.getEmitters());
		effect.getEmitters().clear();
		effect.getEmitters().add(emitters.get(0));

		inputProcessor = new InputProcessor() {
			public boolean touchUp (int x, int y, int pointer, int button) {
				return false;
			}

			public boolean touchDragged (int x, int y, int pointer) {
				effect.setPosition(x, Gdx.graphics.getHeight() - y);
				return false;
			}

			public boolean touchDown (int x, int y, int pointer, int newParam) {
				// effect.setPosition(x, Gdx.graphics.getHeight() - y);
				ParticleEmitter emitter = emitters.get(emitterIndex);
				particleCount += 100;
				System.out.println(particleCount);
				particleCount = Math.max(0, particleCount);
				if (particleCount > emitter.getMaxParticleCount()) emitter.setMaxParticleCount(particleCount * 2);
				emitter.getEmission().setHigh(particleCount / emitter.getLife().getHighMax() * 1000);
				effect.getEmitters().clear();
				effect.getEmitters().add(emitter);
				return false;
			}

			public boolean keyUp (int keycode) {
				return false;
			}

			public boolean keyTyped (char character) {
				return false;
			}

			public boolean keyDown (int keycode) {
				ParticleEmitter emitter = emitters.get(emitterIndex);
				if (keycode == Input.Keys.DPAD_UP)
					particleCount += 5;
				else if (keycode == Input.Keys.PLUS) {
					emitter = new ParticleEmitter(emitter);
				}
				else if (keycode == Input.Keys.DPAD_DOWN)
					particleCount -= 5;
				else if (keycode == Input.Keys.SPACE) {
					emitterIndex = (emitterIndex + 1) % emitters.size;
					emitter = emitters.get(emitterIndex);

					// if we've previously stopped the emitter reset it
					if (emitter.isComplete()) emitter.reset();
					particleCount = (int)(emitter.getEmission().getHighMax() * emitter.getLife().getHighMax() / 1000f);
				} else if (keycode == Input.Keys.ENTER) {
					emitter = emitters.get(emitterIndex);
					if (emitter.isComplete())
						emitter.reset();
					else
						emitter.allowCompletion();
				} else
					return false;
				particleCount = Math.max(0, particleCount);
				if (particleCount > emitter.getMaxParticleCount()) emitter.setMaxParticleCount(particleCount * 2);
				emitter.getEmission().setHigh(particleCount / emitter.getLife().getHighMax() * 1000);
				effect.getEmitters().clear();
				effect.getEmitters().add(emitter);
				return false;
			}

			@Override
			public boolean mouseMoved (int x, int y) {
				return false;
			}

			@Override
			public boolean scrolled (int amount) {
				return false;
			}
		};

		Gdx.input.setInputProcessor(inputProcessor);
	}

	@Override
	public void dispose () {
		spriteBatch.dispose();
		effect.dispose();
	}

	public void render () {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		float delta = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		spriteBatch.begin();
		effect.draw(spriteBatch, delta);
		spriteBatch.end();
		fpsCounter += delta;
		if (fpsCounter > 3) {
			fpsCounter = 0;
			int activeCount = emitters.get(emitterIndex).getActiveCount();
			Gdx.app.log("libgdx", activeCount + "/" + particleCount + " particles, FPS: " + Gdx.graphics.getFramesPerSecond());
		}
	}

	public boolean needsGL20 () {
		return false;
	}
}
