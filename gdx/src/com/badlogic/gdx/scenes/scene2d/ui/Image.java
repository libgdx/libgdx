
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

/** Displays a {@link Drawable}, scaled various way within the widgets bounds. The preferred size is the min size of the drawable.
 * Only when using a {@link TextureRegionDrawable} will the actor's scale, rotation, and origin be used when drawing.
 * @author Nathan Sweet */
public class Image extends Widget {
	private Scaling scaling;
	private int align = Align.CENTER;
	private float imageX, imageY, imageWidth, imageHeight;
	private Drawable drawable;

	/** Creates an image with no region or patch, stretched, and aligned center. */
	public Image () {
		this((Drawable)null);
	}

	/** Creates an image stretched, and aligned center.
	 * @param patch May be null. */
	public Image (NinePatch patch) {
		this(new NinePatchDrawable(patch), Scaling.stretch, Align.CENTER);
	}

	/** Creates an image stretched, and aligned center.
	 * @param region May be null. */
	public Image (TextureRegion region) {
		this(new TextureRegionDrawable(region), Scaling.stretch, Align.CENTER);
	}

	/** Creates an image stretched, and aligned center. */
	public Image (Texture texture) {
		this(new TextureDrawable(texture));
	}

	/** Creates an image stretched, and aligned center.
	 * @param drawable May be null. */
	public Image (Drawable drawable) {
		this(drawable, Scaling.stretch, Align.CENTER);
	}

	/** Creates an image aligned center.
	 * @param drawable May be null. */
	public Image (Drawable drawable, Scaling scaling) {
		this(drawable, scaling, Align.CENTER);
	}

	/** @param drawable May be null. */
	public Image (Drawable drawable, Scaling scaling, int align) {
		setDrawable(drawable);
		this.scaling = scaling;
		this.align = align;
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public void layout () {
		float regionWidth, regionHeight;
		if (drawable != null) {
			regionWidth = drawable.getMinWidth();
			regionHeight = drawable.getMinHeight();
		} else
			return;

		float width = getWidth();
		float height = getHeight();

		Vector2 size = scaling.apply(regionWidth, regionHeight, width, height);
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

		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

		float x = getX();
		float y = getY();
		float scaleX = getScaleX();
		float scaleY = getScaleY();

		if (drawable != null) {
			if (drawable.getClass() == TextureRegionDrawable.class) {
				TextureRegion region = ((TextureRegionDrawable)drawable).getRegion();
				float rotation = getRotation();
				if (scaleX == 1 && scaleY == 1 && rotation == 0)
					batch.draw(region, x + imageX, y + imageY, imageWidth, imageHeight);
				else {
					batch.draw(region, x + imageX, y + imageY, getOriginX() - imageX, getOriginY() - imageY, imageWidth, imageHeight,
						scaleX, scaleY, rotation);
				}
			} else
				drawable.draw(batch, x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
		}
	}

	public void setDrawable (Drawable drawable) {
		if (drawable != null) {
			if (this.drawable == drawable) return;
			if (getPrefWidth() != drawable.getMinWidth() || getPrefHeight() != drawable.getMinHeight()) invalidateHierarchy();
		} else {
			if (getPrefWidth() != 0 || getPrefHeight() != 0) invalidateHierarchy();
		}
		this.drawable = drawable;
	}

	public Drawable getDrawable () {
		return drawable;
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
		if (drawable != null) return drawable.getMinWidth();
		return 0;
	}

	public float getPrefHeight () {
		if (drawable != null) return drawable.getMinHeight();
		return 0;
	}

	public float getImageX () {
		return imageX;
	}

	public float getImageY () {
		return imageY;
	}

	public float getImageWidth () {
		return imageWidth;
	}

	public float getImageHeight () {
		return imageHeight;
	}
}
