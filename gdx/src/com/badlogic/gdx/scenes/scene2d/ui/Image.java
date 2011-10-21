
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

/** @author Nathan Sweet */
public class Image extends Widget {
	protected TextureRegion region;
	protected NinePatch patch;
	protected Scaling scaling;
	protected int align = Align.CENTER;
	protected float imageX, imageY, imageWidth, imageHeight;

	public Image (TextureRegion region) {
		this(region, Scaling.stretch, null);
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
		setRegion(region);
		this.scaling = scaling;
		this.align = align;
		pack();
	}

	public Image (NinePatch patch) {
		this(patch, Scaling.stretch, null);
	}

	public Image (NinePatch patch, Scaling scaling) {
		this(patch, scaling, null);
	}

	public Image (NinePatch patch, Scaling scaling, int align) {
		this(patch, scaling, align, null);
	}

	public Image (NinePatch patch, Scaling scaling, String name) {
		this(patch, scaling, Align.CENTER, null);
	}

	public Image (NinePatch patch, Scaling scaling, int align, String name) {
		setPatch(patch);
		this.scaling = scaling;
		this.align = align;
		pack();
	}

	public void layout () {
		float regionWidth, regionHeight;
		if (patch != null) {
			regionWidth = patch.getTotalWidth();
			regionHeight = patch.getTotalHeight();
		} else if (region != null) {
			regionWidth = region.getRegionWidth();
			regionHeight = region.getRegionHeight();
		} else
			return;

		Vector2 size = scaling.apply(regionWidth, regionHeight, width * scaleX, height * scaleY);
		imageWidth = size.x;
		imageHeight = size.y;

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
		validate();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (patch != null)
			patch.draw(batch, x, y, imageWidth * scaleX, imageHeight * scaleY);
		else if (region != null) {
			if (scaleX == 1 && scaleY == 1 && rotation == 0)
				batch.draw(region, x, y + imageY, imageWidth, imageHeight);
			else
				batch.draw(region, x, y + imageY, originX, originY, imageWidth, imageHeight, scaleX, scaleY, rotation);
		}
	}

	/** @param region May be null. */
	public void setRegion (TextureRegion region) {
		if (region != null) {
			if (getPrefWidth() != region.getRegionWidth() || getPrefHeight() != region.getRegionHeight()) invalidateHierarchy();
		} else {
			if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
		}
		this.region = region;
		patch = null;
	}

	/** @param patch May be null. */
	public void setPatch (NinePatch patch) {
		if (patch != null) {
			if (getPrefWidth() != patch.getTotalWidth() || getPrefHeight() != patch.getTotalHeight()) invalidateHierarchy();
		} else {
			if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
		}
		this.patch = patch;
		region = null;
	}

	public void setScaling (Scaling scaling) {
		this.scaling = scaling;
	}

	public float getMinWidth () {
		return 0;
	}

	public float getMinHeight () {
		return 0;
	}

	public float getPrefWidth () {
		if (region != null) return region.getRegionWidth();
		if (patch != null) return patch.getTotalWidth();
		return 0;
	}

	public float getPrefHeight () {
		if (region != null) return region.getRegionHeight();
		if (patch != null) return patch.getTotalHeight();
		return 0;
	}

	public boolean touchDown (float x, float y, int pointer) {
		return false;
	}

	public void touchUp (float x, float y, int pointer) {
	}

	public void touchDragged (float x, float y, int pointer) {
	}
}
