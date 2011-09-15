
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Image extends Widget {
	private TextureRegion region;
	private final Scaling scaling;
	private int align = Align.CENTER;
	private float imageX, imageY, imageWidth, imageHeight;

	public Image (TextureRegion region) {
		this(region, Scaling.none, null);
	}

	public Image (TextureRegion region, Scaling scaling) {
		this(region, scaling, null);
	}

	public Image (TextureRegion region, Scaling scaling, int align) {
		this(region, scaling, align, null);
	}

	public Image (TextureRegion region, Scaling scaling, String name) {
		this(region, scaling, Align.CENTER, null);
	}

	public Image (TextureRegion region, Scaling scaling, int align, String name) {
		super(region.getRegionWidth(), region.getRegionHeight(), name);
		this.region = region;
		this.scaling = scaling;
		this.align = align;
	}

	public void layout () {
		if (!invalidated) return;
		invalidated = false;
		float regionWidth = region.getRegionWidth();
		float regionHeight = region.getRegionHeight();
		switch (scaling) {
		case fill: {
			float widgetRatio = height / width;
			float regionRatio = regionHeight / regionWidth;
			float scale = regionRatio > widgetRatio ? width / regionWidth : height / regionHeight;
			imageWidth = regionWidth * scale;
			imageHeight = regionHeight * scale;
			break;
		}
		case fit: {
			float widgetRatio = height / width;
			float regionRatio = regionHeight / regionWidth;
			float scale = regionRatio < widgetRatio ? width / regionWidth : height / regionHeight;
			imageWidth = regionWidth * scale;
			imageHeight = regionHeight * scale;
			break;
		}
		case stretch:
			imageWidth = width;
			imageHeight = height;
			break;
		case stretchX:
			imageWidth = width;
			imageHeight = regionHeight;
			break;
		case stretchY:
			imageWidth = regionWidth;
			imageHeight = height;
			break;
		case none:
			imageWidth = regionWidth;
			imageHeight = regionHeight;
			break;
		}

		if ((align & Align.LEFT) != 0)
			imageX = 0;
		else if ((align & Align.RIGHT) != 0)
			imageX = (int)(width - imageWidth);
		else
			imageX = (int)(width / 2 - imageWidth / 2);

		if ((align & Align.TOP) != 0)
			imageY = (int)(height - imageHeight);
		else if ((align & Align.BOTTOM) != 0)
			imageY = 0;
		else
			imageY = (int)(height / 2 - imageHeight / 2);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		if (invalidated) layout();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		batch.draw(region, x + imageX, y + imageY, imageWidth, imageHeight);
	}

	public void setRegion (TextureRegion region) {
		this.region = region;
		invalidate();
	}

	public boolean touchDown (float x, float y, int pointer) {
		return false;
	}

	public void touchUp (float x, float y, int pointer) {
	}

	public void touchDragged (float x, float y, int pointer) {
	}

	static public enum Scaling {
		fill, fit, stretch, stretchX, stretchY, none
	}
}
