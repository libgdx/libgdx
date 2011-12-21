
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** A checkbox is a button that contains an image indicating the checked or unchecked state and a label.
 * @author Nathan Sweet */
public class CheckBox extends TextButton {
	private Image image;
	private CheckBoxStyle style;

	public CheckBox (Skin skin) {
		this("", skin);
	}

	public CheckBox (String text, CheckBoxStyle style) {
		this(text, style, null);
	}

	public CheckBox (String text, Skin skin) {
		this(text, skin.getStyle(CheckBoxStyle.class), null);
	}

	public CheckBox (String text, CheckBoxStyle style, String name) {
		super(text, style, name);
		clear();
		add(image = new Image(style.checkboxOff));
		Label label = getLabel();
		add(label);
		label.setAlignment(Align.LEFT);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (ButtonStyle style) {
		if (!(style instanceof CheckBoxStyle)) throw new IllegalArgumentException("style must be a CheckBoxStyle.");
		super.setStyle(style);
		this.style = (CheckBoxStyle)style;
	}

	/** Returns the checkbox's style. Modifying the returned style may not have an effect until {@link #setStyle(ButtonStyle)} is
	 * called. */
	public CheckBoxStyle getStyle () {
		return style;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		image.setRegion(isChecked ? style.checkboxOn : style.checkboxOff);
		super.draw(batch, parentAlpha);
	}

	public Image getImage () {
		return image;
	}

	/** The style for a select box, see {@link CheckBox}.
	 * @author Nathan Sweet */
	static public class CheckBoxStyle extends TextButtonStyle {
		public TextureRegion checkboxOn, checkboxOff;

		public CheckBoxStyle () {
		}

		public CheckBoxStyle (TextureRegion checkboxOff, TextureRegion checkboxOn, BitmapFont font, Color fontColor) {
			this.checkboxOff = checkboxOff;
			this.checkboxOn = checkboxOn;
			this.font = font;
			this.fontColor = fontColor;
		}
	}
}
