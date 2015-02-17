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

package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Tween;

/** Creates a sprite and manages its properties using Tween.
 * 
 * Interpolates the sprite position from left to right and fades the sprite in, changing the alpha properties.
 * 
 * Keys: 
 * - P: pauses/resumes the tweens animation 
 * - S: stops the tweens animation  
 * - click: restarts the tweens */
public class TweenTest extends GdxTest {
	SpriteBatch batch;
	Sprite img;
	Tween positionTween;
	Tween alphaTween;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Sprite(new Texture("data/badlogic.jpg"));

		// interpolates from 0 to 1 in 2 seconds using a circle interpolation
		alphaTween = new Tween(Interpolation.circle, 0, 1, 2);
		// interpolates from 0 to 400 in 2 seconds using a pow2Out interpolation
		positionTween = new Tween(Interpolation.pow2Out, 0, 400, 2);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		float delta = Gdx.graphics.getDeltaTime();

		// update the tweens
		alphaTween.update(delta);
		positionTween.update(delta);

		// update the Sprite, using the tweens values
		img.setAlpha(alphaTween.getValue());
		img.setPosition(positionTween.getValue(), 100);

		// handling input
		if (Gdx.input.isKeyJustPressed(Keys.P)) {
			if (alphaTween.isTweenActive()) {
				alphaTween.pause();
				positionTween.pause();
			} else {
				alphaTween.resume();
				positionTween.resume();
			}
		} else if (Gdx.input.isKeyJustPressed(Keys.S)) {
			alphaTween.stop();
			positionTween.stop();
		} else if (Gdx.input.isTouched()) {
			alphaTween.restart();
			positionTween.restart();
		}

		img.draw(batch);

		batch.end();
	}
}
