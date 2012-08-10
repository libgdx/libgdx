
package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Texture;
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
		Texture texture = region.getTexture();
		float u = region.getU();
		float v2 = region.getV2();
		if (remainingX > 0) {
			// Right edge.
			float u2 = u + remainingX / texture.getWidth();
			float v = region.getV();
			y = startY;
			while (y < endY) {
				batch.draw(texture, x, y, remainingX, regionHeight, u, v2, u2, v);
				y += regionHeight;
			}
			// Upper right corner.
			if (remainingY > 0) {
				v = v2 - remainingY / texture.getHeight();
				batch.draw(texture, x, y, remainingX, remainingY, u, v2, u2, v);
			}
		}
		if (remainingY > 0) {
			// Top edge.
			float u2 = region.getU2();
			float v = v2 - remainingY / texture.getHeight();
			x = startX;
			while (x < endX) {
				batch.draw(texture, x, y, regionWidth, remainingY, u, v2, u2, v);
				x += regionWidth;
			}
		}
	}
}
