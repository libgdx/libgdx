
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
