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
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Drawable for a {@link Texture}.
 * @author Nathan Sweet */
public class TextureDrawable extends EmptyDrawable {
	private Texture texture;

	public TextureDrawable () {
	}

	public TextureDrawable (Texture texture) {
		setTexture(texture);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		batch.draw(texture, x, y, width, height);
	}

	public void setTexture (Texture texture) {
		this.texture = texture;
		setMinWidth(texture.getWidth());
		setMinHeight(texture.getHeight());
	}

	public Texture getTexture () {
		return texture;
	}
}