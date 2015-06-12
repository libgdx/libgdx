/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;

/** A color fading transition
 * @author iXeption */
public class ColorFadeTransition implements ScreenTransition {

	private Color color;
	private Interpolation interpolation;
	private Texture texture;

	/** @param color the {@link Color} to fade to
	 * @param interpolation the {@link Interpolation} method */
	public ColorFadeTransition (Color color, Interpolation interpolation) {
		this.color = color;
		this.interpolation = interpolation;

		texture = new Texture(1, 1, Format.RGBA8888);
		Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);
		pixmap.setColor(color);
		pixmap.fillRectangle(0, 0, 1, 1);
		texture.draw(pixmap, 0, 0);
	}

	@Override
	public void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float percent) {
		float width = currentScreenTexture.getWidth();
		float height = currentScreenTexture.getHeight();
		float x = 0;
		float y = 0;

		if (interpolation != null) percent = interpolation.apply(percent);

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();

		float fade = percent * 2;

		if (fade > 1.0f) {
			fade = 1.0f - (percent * 2 - 1.0f);
			Color fadeIn = new Color(Color.WHITE);
			fadeIn.a = 1.0f - fade;
			batch.setColor(fadeIn);

			batch.draw(nextScreenTexture, 0, 0, width / 2, height / 2, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(),
				1, 1, 0, 0, 0, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), false, true);

		} else {

			Color fadeOut = new Color(Color.WHITE);
			fadeOut.a = 1.0f - fade;
			batch.setColor(fadeOut);

			batch.draw(currentScreenTexture, 0, 0, width / 2, height / 2, width, height, 1, 1, 0, 0, 0, (int)width, (int)height,
				false, true);

		}

		Color c = new Color(Color.WHITE);
		c.a = fade;

		batch.setColor(c);
		batch.draw(texture, 0, 0, width, height);

		batch.end();

		batch.setColor(Color.WHITE);

	}

}
