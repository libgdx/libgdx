
package com.badlogic.gdx.scenes.scene2d.ui;

import com.esotericsoftware.tablelayout.Cell;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Scaling;

/** A button with a child {@link Image} and {@link Label}.
 * @see ImageButton
 * @see TextButton
 * @see Button
 * @author Nathan Sweet */
public class ImageTextButton extends Button {
	private final Image image;
	private final Label label;
	private ImageTextButtonStyle style;

	public ImageTextButton (String text, Skin skin) {
		this(text, skin.get(ImageTextButtonStyle.class));
		setSkin(skin);
	}

	public ImageTextButton (String text, Skin skin, String styleName) {
		this(text, skin.get(styleName, ImageTextButtonStyle.class));
		setSkin(skin);
	}

	public ImageTextButton (String text, ImageTextButtonStyle style) {
		super(style);
		this.style = style;

		defaults().space(3);

		image = new Image();
		image.setScaling(Scaling.fit);
		add(image);

		label = new Label(text, new LabelStyle(style.font, style.fontColor));
		label.setAlignment(Align.center);
		add(label);

		setWidth(getPrefWidth());
		setHeight(getPrefHeight());
	}

	public void setStyle (ButtonStyle style) {
		if (!(style instanceof ImageTextButtonStyle)) throw new IllegalArgumentException("style must be a ImageTextButtonStyle.");
		super.setStyle(style);
		this.style = (ImageTextButtonStyle)style;
		if (image != null) updateImage();
		if (label != null) {
			ImageTextButtonStyle textButtonStyle = (ImageTextButtonStyle)style;
			LabelStyle labelStyle = label.getStyle();
			labelStyle.font = textButtonStyle.font;
			labelStyle.fontColor = textButtonStyle.fontColor;
			label.setStyle(labelStyle);
		}
	}

	public ImageTextButtonStyle getStyle () {
		return style;
	}

	private void updateImage () {
		boolean isPressed = isPressed();
		if (isDisabled && style.imageDisabled != null)
			image.setDrawable(style.imageDisabled);
		else if (isPressed && style.imageDown != null)
			image.setDrawable(style.imageDown);
		else if (isChecked && style.imageChecked != null)
			image.setDrawable((style.imageCheckedOver != null && isOver()) ? style.imageCheckedOver : style.imageChecked);
		else if (isOver() && style.imageOver != null)
			image.setDrawable(style.imageOver);
		else if (style.imageUp != null) //
			image.setDrawable(style.imageUp);
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		updateImage();
		Color fontColor;
		if (isDisabled && style.disabledFontColor != null)
			fontColor = style.disabledFontColor;
		else if (isPressed() && style.downFontColor != null)
			fontColor = style.downFontColor;
		else if (isChecked && style.checkedFontColor != null)
			fontColor = (isOver() && style.checkedOverFontColor != null) ? style.checkedOverFontColor : style.checkedFontColor;
		else if (isOver() && style.overFontColor != null)
			fontColor = style.overFontColor;
		else
			fontColor = style.fontColor;
		if (fontColor != null) label.getStyle().fontColor = fontColor;
		super.draw(batch, parentAlpha);
	}

	public Image getImage () {
		return image;
	}

	public Cell getImageCell () {
		return getCell(image);
	}

	public Label getLabel () {
		return label;
	}

	public Cell getLabelCell () {
		return getCell(label);
	}

	public void setText (String text) {
		label.setText(text);
	}

	public CharSequence getText () {
		return label.getText();
	}

	/** The style for an image text button, see {@link ImageTextButton}.
	 * @author Nathan Sweet */
	static public class ImageTextButtonStyle extends TextButtonStyle {
		/** Optional. */
		public Drawable imageUp, imageDown, imageOver, imageChecked, imageCheckedOver, imageDisabled;

		public ImageTextButtonStyle () {
		}

		public ImageTextButtonStyle (Drawable up, Drawable down, Drawable checked) {
			super(up, down, checked);
		}

		public ImageTextButtonStyle (ImageTextButtonStyle style) {
			super(style);
			this.font = style.font;
			if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
			if (style.downFontColor != null) this.downFontColor = new Color(style.downFontColor);
			if (style.overFontColor != null) this.overFontColor = new Color(style.overFontColor);
			if (style.checkedFontColor != null) this.checkedFontColor = new Color(style.checkedFontColor);
			if (style.checkedOverFontColor != null) this.checkedOverFontColor = new Color(style.checkedOverFontColor);
			if (style.disabledFontColor != null) this.disabledFontColor = new Color(style.disabledFontColor);
			if (style.imageUp != null) this.imageUp = style.imageUp;
			if (style.imageDown != null) this.imageDown = style.imageDown;
			if (style.imageOver != null) this.imageOver = style.imageOver;
			if (style.imageChecked != null) this.imageChecked = style.imageChecked;
			if (style.imageCheckedOver != null) this.imageCheckedOver = style.imageCheckedOver;
			if (style.imageDisabled != null) this.imageDisabled = style.imageDisabled;
		}
	}
}
