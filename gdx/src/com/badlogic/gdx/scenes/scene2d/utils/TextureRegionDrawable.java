
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureRegionDrawable extends BaseDrawable {
	private TextureRegion region;

	public TextureRegionDrawable () {
	}

	public TextureRegionDrawable (TextureRegion region) {
		setRegion(region);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		batch.draw(region, x, y, width, height);
	}

	public void setRegion (TextureRegion region) {
		this.region = region;
		setMinWidth(region.getRegionWidth());
		setMinHeight(region.getRegionHeight());
	}

	public TextureRegion getRegion () {
		return region;
	}
}
