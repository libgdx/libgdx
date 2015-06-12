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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;

/** A sliding transition
 * @author iXeption */
public class SlidingTransition implements ScreenTransition {

	public enum Direction {
		LEFT, RIGHT, UP, DOWN
	}

	private Direction direction;
	private boolean slideOut;
	private Interpolation interpolation;

	/** @param direction the {@link Direction} of the transition
	 * @param interpolation the {@link Interpolation} method
	 * @param slideOut slide out or slide in */
	public SlidingTransition (Direction direction, Interpolation interpolation, boolean slideOut) {
		this.direction = direction;
		this.interpolation = interpolation;
		this.slideOut = slideOut;
	}

	@Override
	public void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float percent) {
		float width = currentScreenTexture.getWidth();
		float height = currentScreenTexture.getHeight();
		float x = 0;
		float y = 0;
		if (interpolation != null) percent = interpolation.apply(percent);

		switch (direction) {
		case LEFT:
			x = -width * percent;
			if (!slideOut) x += width;
			break;
		case RIGHT:
			x = width * percent;
			if (!slideOut) x -= width;
			break;
		case UP:
			y = height * percent;
			if (!slideOut) y -= height;
			break;
		case DOWN:
			y = -height * percent;
			if (!slideOut) y += height;
			break;
		}
		Texture texBottom = slideOut ? nextScreenTexture : currentScreenTexture;
		Texture texTop = slideOut ? currentScreenTexture : nextScreenTexture;

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(texBottom, 0, 0, 0, 0, width, height, 1, 1, 0, 0, 0, (int)width, (int)height, false, true);
		batch.draw(texTop, x, y, 0, 0, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), 1, 1, 0, 0, 0,
			nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), false, true);
		batch.end();

	}

}
