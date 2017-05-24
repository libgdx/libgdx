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
import com.badlogic.gdx.utils.Array;

/** A Slicing transition
 * @author iXeption */
public class SlicingTransition implements ScreenTransition {

	public enum Direction {
		UP, DOWN, UPDOWN
	}

	private Direction direction;
	private Interpolation interpolation;
	private Array<Integer> slices = new Array<Integer>();

	/** @param direction the {@link Direction} of the transition
	 * @param numSlices the number of slices
	 * @param interpolation the {@link Interpolation} method */
	public SlicingTransition (Direction direction, int numSlices, Interpolation interpolation) {
		this.direction = direction;
		this.interpolation = interpolation;

		slices.clear();
		for (int i = 0; i < numSlices; i++)
			slices.add(i);
		slices.shuffle();

	}

	@Override
	public void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float percent) {
		float width = currentScreenTexture.getWidth();
		float height = currentScreenTexture.getHeight();
		float x = 0;
		float y = 0;
		int sliceWidth = (int)(width / slices.size);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(currentScreenTexture, 0, 0, 0, 0, width, height, 1, 1, 0, 0, 0, (int)width, (int)height, false, true);
		if (interpolation != null) percent = interpolation.apply(percent);
		for (int i = 0; i < slices.size; i++) {

			x = i * sliceWidth;

			float offsetY = height * (1 + slices.get(i) / (float)slices.size);
			switch (direction) {
			case UP:
				y = -offsetY + offsetY * percent;
				break;
			case DOWN:
				y = offsetY - offsetY * percent;
				break;
			case UPDOWN:
				if (i % 2 == 0) {
					y = -offsetY + offsetY * percent;
				} else {
					y = offsetY - offsetY * percent;
				}
				break;
			}
			batch.draw(nextScreenTexture, x, y, 0, 0, sliceWidth, nextScreenTexture.getHeight(), 1, 1, 0, i * sliceWidth, 0,
				sliceWidth, nextScreenTexture.getHeight(), false, true);
		}
		batch.end();
	}

}
