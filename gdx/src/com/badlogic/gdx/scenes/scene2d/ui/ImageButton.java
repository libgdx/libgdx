
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Scaling;
import com.esotericsoftware.tablelayout.Cell;

/** A button with a child {@link Image} to display an image. This is useful when the button must be larger than the image and the
 * image centered on the button. If the image is the size of the button, a {@link Button} without any children can be used, where
 * the {@link Button.ButtonStyle#up}, {@link Button.ButtonStyle#down}, and {@link Button.ButtonStyle#checked} nine patches define
 * the image.
 * @author Nathan Sweet */
public class ImageButton extends Button {
	private final Image image;
	private ImageButtonStyle style;

	public ImageButton (Skin skin) {
		this(skin.getStyle("default", ImageButtonStyle.class));
	}

	public ImageButton (ImageButtonStyle style) {
		this(style, null);
	}

	public ImageButton (ImageButtonStyle style, String name) {
		super(style, name);
		image = new Image();
		image.setScaling(Scaling.fit);
		add(image);
		setStyle(style);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public ImageButton (TextureRegion region) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, region, null, null));
	}

	public ImageButton (TextureRegion regionUp, TextureRegion regionDown) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, regionUp, regionDown, null));
	}

	public ImageButton (TextureRegion regionUp, TextureRegion regionDown, TextureRegion regionChecked) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, regionUp, regionDown, regionChecked));
	}

	public ImageButton (NinePatch patch) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, patch, null, null));
	}

	public ImageButton (NinePatch patchUp, NinePatch patchDown) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, patchUp, patchDown, null));
	}

	public ImageButton (NinePatch patchUp, NinePatch patchDown, NinePatch patchChecked) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, patchUp, patchDown, patchChecked));
	}

	public void setStyle (ButtonStyle style) {
		if (!(style instanceof ImageButtonStyle)) throw new IllegalArgumentException("style must be an ImageButtonStyle.");
		super.setStyle(style);
		this.style = (ImageButtonStyle)style;
		if (image != null) updateImage();
	}

	public ImageButtonStyle getStyle () {
		return style;
	}

	private void updateImage () {
		if (isPressed && style.regionDown != null)
			image.setRegion(style.regionDown);
		else if (isPressed && style.patchDown != null)
			image.setPatch(style.patchDown);
		else if (isChecked && style.regionChecked != null)
			image.setRegion(style.regionChecked);
		else if (isChecked && style.patchChecked != null)
			image.setPatch(style.patchChecked);
		else if (style.regionUp != null)
			image.setRegion(style.regionUp);
		else if (style.patchUp != null) //
			image.setPatch(style.patchUp);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		updateImage();
		super.draw(batch, parentAlpha);
	}

	public Image getImage () {
		return image;
	}

	public Cell getImageCell () {
		return getCell(image);
	}

	/** The style for an image button, see {@link ImageButton}.
	 * @author Nathan Sweet */
	static public class ImageButtonStyle extends ButtonStyle {
		/** Optional. */
		public TextureRegion regionUp, regionDown, regionChecked;
		/** Optional. */
		public NinePatch patchUp, patchDown, patchChecked;

		public ImageButtonStyle () {
		}

		public ImageButtonStyle (NinePatch down, NinePatch up, NinePatch checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY, TextureRegion regionUp, TextureRegion regionDown,
			TextureRegion regionChecked) {
			super(down, up, checked, pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY);
			this.regionUp = regionUp;
			this.regionDown = regionDown;
			this.regionChecked = regionChecked;
		}

		public ImageButtonStyle (NinePatch down, NinePatch up, NinePatch checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY, NinePatch patchUp, NinePatch patchDown, NinePatch patchChecked) {
			super(down, up, checked, pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY);
			this.patchUp = patchUp;
			this.patchDown = patchDown;
			this.patchChecked = patchChecked;
		}
	}
}
