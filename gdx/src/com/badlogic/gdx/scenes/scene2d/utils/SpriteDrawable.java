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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Drawable for a {@link Sprite}.
 * @author Nathan Sweet */
public class SpriteDrawable extends BaseDrawable {
	private Sprite sprite;

	/** Creates an unitialized SpriteDrawable. The sprite must be set before use. */
	public SpriteDrawable () {
	}

	public SpriteDrawable (Sprite sprite) {
		setSprite(sprite);
	}

	public SpriteDrawable (SpriteDrawable drawable) {
		super(drawable);
		setSprite(drawable.sprite);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		sprite.setBounds(x, y, width, height);
		Color color = sprite.getColor();
		sprite.setColor(Color.tmp.set(color).mul(batch.getColor()));
		sprite.draw(batch);
		sprite.setColor(color);
	}

	public void setSprite (Sprite sprite) {
		this.sprite = sprite;
		setMinWidth(sprite.getWidth());
		setMinHeight(sprite.getHeight());
	}

	public Sprite getSprite () {
		return sprite;
	}
}
