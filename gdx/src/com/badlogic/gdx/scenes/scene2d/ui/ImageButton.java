/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
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
		this(skin.get(ImageButtonStyle.class));
	}

	public ImageButton (Skin skin, String styleName) {
		this(skin.get(styleName, ImageButtonStyle.class));
	}

	public ImageButton (ImageButtonStyle style) {
		super(style);
		image = new Image();
		image.setScaling(Scaling.fit);
		add(image);
		setStyle(style);
		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public ImageButton (Drawable imageUp) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, imageUp, null, null));
	}

	public ImageButton (Drawable imageUp, Drawable imageDown) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, imageUp, imageDown, null));
	}

	public ImageButton (Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
		this(new ImageButtonStyle(null, null, null, 0f, 0f, 0f, 0f, imageUp, imageDown, imageChecked));
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
		boolean isPressed = isPressed();
		if (isPressed && style.imageDown != null)
			image.setDrawable(style.imageDown);
		else if (isChecked && style.imageChecked != null)
			image.setDrawable(style.imageChecked);
		else if (style.imageUp != null) //
			image.setDrawable(style.imageUp);
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
		public Drawable imageUp, imageDown, imageChecked;

		public ImageButtonStyle () {
		}

		public ImageButtonStyle (Drawable down, Drawable up, Drawable checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY, Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
			super(down, up, checked, pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY);
			this.imageUp = imageUp;
			this.imageDown = imageDown;
			this.imageChecked = imageChecked;
		}

		public ImageButtonStyle (ImageButtonStyle style) {
			super(style);
			this.imageUp = style.imageUp;
			this.imageDown = style.imageDown;
			this.imageChecked = style.imageChecked;
		}
	}
}
