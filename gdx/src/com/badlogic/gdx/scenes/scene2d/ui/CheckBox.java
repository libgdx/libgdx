
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** @author Nathan Sweet */
public class CheckBox extends Button {
	protected Image image;

	private boolean isCheckedRegion;

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
		super(style, name);
		add(image = new Image(style.checkboxOff));
		Label label = new Label(text, style);
		label.setAlignment(Align.CENTER);
		add(label);
		pack();
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		if (isCheckedRegion != isChecked) {
			isCheckedRegion = isChecked;
			image.setRegion(isChecked ? ((CheckBoxStyle)style).checkboxOn : ((CheckBoxStyle)style).checkboxOff);
		}
		super.draw(batch, parentAlpha);
	}

	static public class CheckBoxStyle extends ButtonStyle {
		public TextureRegion checkboxOn;
		public TextureRegion checkboxOff;

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
