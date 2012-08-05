
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Draws a {@link TextureRegion} repeatedly to fill the area, instead of stretching it.
 * @author Nathan Sweet */
public class TiledDrawable extends TextureRegionDrawable {
	public TiledDrawable () {
		super();
	}

	public TiledDrawable (TextureRegion region) {
		super(region);
	}

	public TiledDrawable (TextureRegionDrawable drawable) {
		super(drawable);
	}

	public void draw (SpriteBatch batch, float x, float y, float width, float height) {
		TextureRegion region = getRegion();
		float regionWidth = region.getRegionWidth(), regionHeight = region.getRegionWidth();
		float remainingX = width % regionWidth, remainingY = height % regionHeight;
		float startX = x, startY = y;
		float endX = x + width - remainingX, endY = y + height - remainingY;
		while (x < endX) {
			y = startY;
			while (y < endY) {
				batch.draw(region, x, y, regionWidth, regionHeight);
				y += regionHeight;
			}
			x += regionWidth;
		}
		if (remainingX > 0) {
			y = startY;
			while (y < endY) {
				batch.draw(region, x, y, remainingX, regionHeight);
				y += regionHeight;
			}
			if (remainingY > 0) batch.draw(region, x, y, remainingX, remainingY);
		}
		if (remainingY > 0) {
			x = startX;
			while (x < endX) {
				batch.draw(region, x, y, regionWidth, remainingY);
				x += regionWidth;
			}
		}
	}
}
