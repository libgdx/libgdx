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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;

/** @author iXeption */

/** A simple alpha fade transition
 * @author iXeption */
public class AlphaFadingTransition implements ScreenTransition {

	@Override
	public void render (Batch batch, Texture currentScreenTexture, Texture nextScreenTexture, float alpha) {
		alpha = Interpolation.fade.apply(alpha);
		batch.begin();
		batch.setColor(1, 1, 1, 1);
		batch.draw(currentScreenTexture, 0, 0, 0, 0, currentScreenTexture.getWidth(), currentScreenTexture.getHeight(), 1, 1, 0, 0,
			0, currentScreenTexture.getWidth(), currentScreenTexture.getHeight(), false, true);
		batch.setColor(1, 1, 1, alpha);
		batch.draw(nextScreenTexture, 0, 0, 0, 0, nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), 1, 1, 0, 0, 0,
			nextScreenTexture.getWidth(), nextScreenTexture.getHeight(), false, true);
		batch.end();

	}
}
