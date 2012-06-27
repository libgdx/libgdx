
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Drawable for a {@link Sprite}.
 * @author Nathan Sweet */
public class SpriteDrawable extends EmptyDrawable {
	private Sprite sprite;

	public SpriteDrawable () {
	}

	public SpriteDrawable (Sprite sprite) {
		setSprite(sprite);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		sprite.setBounds(x, y, width, height);
		sprite.draw(batch);
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
