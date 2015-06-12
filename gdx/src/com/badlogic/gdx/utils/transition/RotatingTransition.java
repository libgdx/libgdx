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

/** A Rotating transition
 * @author iXeption */
public class RotatingTransition implements ScreenTransition {

	private Interpolation interpolation;
	private float angle;
	private TransitionScaling scaling;

	public enum TransitionScaling {
		NONE, IN, OUT
	}

	/** @param interpolation the {@link Interpolation} method
	 * @param angle the amount of rotation
	 * @param scaling apply {@link TransitionScaling} */
	public RotatingTransition (Interpolation interpolation, float angle, TransitionScaling scaling) {
		this.interpolation = interpolation;
		this.angle = angle;
		this.scaling = scaling;
	}

	@Override
	public void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float percent) {
		float width = currentScreenTexture.getWidth();
		float height = currentScreenTexture.getHeight();
		float x = 0;
		float y = 0;

		float scalefactor;

		switch (scaling) {
		case IN:
			scalefactor = percent;
			break;
		case OUT:
			scalefactor = 1.0f - percent;
			break;
		case NONE:
		default:
			scalefactor = 1.0f;
			break;
		}

		float rotation = 1;
		if (interpolation != null) rotation = interpolation.apply(percent);

		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(currentScreenTexture, 0, 0, width / 2, height / 2, width, height, 1, 1, 0, 0, 0, (int)width, (int)height, false,
			true);
		batch.draw(nextScreenTexture, 0, 0, width / 2, height / 2, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(),
			scalefactor, scalefactor, rotation * angle, 0, 0, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), false,
			true);
		batch.end();

	}

}
