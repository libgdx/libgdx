
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;

/** Displays a {@link TextureRegion} or {@link NinePatch}, scaled various way within the widgets bounds. The preferred size is the
 * actual size of the region or ninepatch.
 * @author Nathan Sweet */
public class Image extends Widget {
	private TextureRegion region;
	private NinePatch patch;
	private Scaling scaling;
	private int align = Align.CENTER;
	private float imageX, imageY, imageWidth, imageHeight;

	/** Creates an image with no region or patch, stretched, and aligned center. */
	public Image () {
		this((TextureRegion)null);
	}

	/** Creates an image stretched, and aligned center. */
	public Image (Texture texture) {
		this(new TextureRegion(texture));
	}

	/** Creates an image aligned center. */
	public Image (Texture texture, Scaling scaling) {
		this(new TextureRegion(texture), scaling);
	}

	public Image (Texture texture, Scaling scaling, int align) {
		this(new TextureRegion(texture), scaling, align);
	}

	public Image (Texture texture, Scaling scaling, int align, String name) {
		this(new TextureRegion(texture), scaling, align, name);
	}

	/** Creates an image stretched, and aligned center.
	 * @param region May be null. */
	public Image (TextureRegion region) {
		this(region, Scaling.stretch, Align.CENTER, null);
	}

	/** Creates an image aligned center.
	 * @param region May be null. */
	public Image (TextureRegion region, Scaling scaling) {
		this(region, scaling, Align.CENTER, null);
	}

	/** @param region May be null. */
	public Image (TextureRegion region, Scaling scaling, int align) {
		this(region, scaling, align, null);
	}

	/** @param region May be null. */
	public Image (TextureRegion region, Scaling scaling, int align, String name) {
		super(name);
		setRegion(region);
		this.scaling = scaling;
		this.align = align;
		pack();
	}

	/** Creates an image stretched, and aligned center.
	 * @param patch May be null. */
	public Image (NinePatch patch) {
		this(patch, Scaling.stretch, Align.CENTER, null);
	}

	/** Creates an image aligned center.
	 * @param patch May be null. */
	public Image (NinePatch patch, Scaling scaling) {
		this(patch, scaling, Align.CENTER, null);
	}

	/** @param patch May be null. */
	public Image (NinePatch patch, Scaling scaling, int align) {
		this(patch, scaling, align, null);
	}

	/** @param patch May be null. */
	public Image (NinePatch patch, Scaling scaling, int align, String name) {
		super(name);
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
			patch.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
		else if (region != null) {
			if (scaleX == 1 && scaleY == 1 && rotation == 0)
				batch.draw(region, x + imageX, y + imageY, imageWidth, imageHeight);
			else
				batch.draw(region, x + imageX, y + imageY, originX, originY, imageWidth, imageHeight, scaleX, scaleY, rotation);
		}
	}

	/** @param region May be null. */
	public void setRegion (TextureRegion region) {
		if (region != null) {
			if (this.region == region) return;
			if (getPrefWidth() != region.getRegionWidth() || getPrefHeight() != region.getRegionHeight()) invalidateHierarchy();
		} else {
			if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
		}
		this.region = region;
		patch = null;
	}

	public TextureRegion getRegion () {
		return region;
	}

	/** @param patch May be null. */
	public void setPatch (NinePatch patch) {
		if (patch != null) {
			if (this.patch == patch) return;
			if (getPrefWidth() != patch.getTotalWidth() || getPrefHeight() != patch.getTotalHeight()) invalidateHierarchy();
		} else {
			if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
		}
		this.patch = patch;
		region = null;
	}

	public NinePatch getPatch () {
		return patch;
	}

	public void setScaling (Scaling scaling) {
		if (scaling == null) throw new IllegalArgumentException("scaling cannot be null.");
		this.scaling = scaling;
	}

	public void setAlign (int align) {
		this.align = align;
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
