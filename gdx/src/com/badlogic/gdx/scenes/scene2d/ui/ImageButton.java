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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Scaling;

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
		setSkin(skin);
	}

	public ImageButton (Skin skin, String styleName) {
		this(skin.get(styleName, ImageButtonStyle.class));
		setSkin(skin);
	}

	public ImageButton (ImageButtonStyle style) {
		super(style);
		image = new Image();
		image.setScaling(Scaling.fit);
		add(image);
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());
	}

	public ImageButton (@Null Drawable imageUp) {
		this(new ImageButtonStyle(null, null, null, imageUp, null, null));
	}

	public ImageButton (@Null Drawable imageUp, @Null Drawable imageDown) {
		this(new ImageButtonStyle(null, null, null, imageUp, imageDown, null));
	}

	public ImageButton (@Null Drawable imageUp, @Null Drawable imageDown, @Null Drawable imageChecked) {
		this(new ImageButtonStyle(null, null, null, imageUp, imageDown, imageChecked));
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

	/** Updates the Image with the appropriate Drawable from the style before it is drawn. */
	protected void updateImage () {
		Drawable drawable = null;
		if (isDisabled() && style.imageDisabled != null)
			drawable = style.imageDisabled;
		else if (isPressed() && style.imageDown != null)
			drawable = style.imageDown;
		else if (isChecked && style.imageChecked != null)
			drawable = (style.imageCheckedOver != null && isOver()) ? style.imageCheckedOver : style.imageChecked;
		else if (isOver() && style.imageOver != null)
			drawable = style.imageOver;
		else if (style.imageUp != null) //
			drawable = style.imageUp;
		image.setDrawable(drawable);
	}

	public void draw (Batch batch, float parentAlpha) {
		updateImage();
		super.draw(batch, parentAlpha);
	}

	public Image getImage () {
		return image;
	}

	public Cell getImageCell () {
		return getCell(image);
	}

	public String toString () {
		String name = getName();
		if (name != null) return name;
		String className = getClass().getName();
		int dotIndex = className.lastIndexOf('.');
		if (dotIndex != -1) className = className.substring(dotIndex + 1);
		return (className.indexOf('$') != -1 ? "ImageButton " : "") + className + ": " + image.getDrawable();
	}

	/** The style for an image button, see {@link ImageButton}.
	 * @author Nathan Sweet */
	static public class ImageButtonStyle extends ButtonStyle {
		/** Optional. */
		@Null public Drawable imageUp, imageDown, imageOver, imageChecked, imageCheckedOver, imageDisabled;

		public ImageButtonStyle () {
		}

		public ImageButtonStyle (@Null Drawable up, @Null Drawable down, @Null Drawable checked,
			@Null Drawable imageUp, @Null Drawable imageDown, @Null Drawable imageChecked) {
			super(up, down, checked);
			this.imageUp = imageUp;
			this.imageDown = imageDown;
			this.imageChecked = imageChecked;
		}

		public ImageButtonStyle (ImageButtonStyle style) {
			super(style);
			this.imageUp = style.imageUp;
			this.imageDown = style.imageDown;
			this.imageOver = style.imageOver;
			this.imageChecked = style.imageChecked;
			this.imageCheckedOver = style.imageCheckedOver;
			this.imageDisabled = style.imageDisabled;
		}

		public ImageButtonStyle (ButtonStyle style) {
			super(style);
		}
	}
}
