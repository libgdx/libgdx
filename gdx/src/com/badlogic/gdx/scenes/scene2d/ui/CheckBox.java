
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CheckBox extends Button {
	protected Image image;

	public CheckBox (String text, CheckBoxStyle style) {
		this(text, style, null);
	}

	public CheckBox (String text, Skin skin) {
		this(text, skin.getStyle(CheckBoxStyle.class), null);
	}

	public CheckBox (String text, CheckBoxStyle style, String name) {
		super(style, name);
		add(image = new Image(style.checkboxOff));
		add(new Label(text, style));
		pack();
	}

	public void click () {
		super.click();
		image.setRegion(isChecked ? ((CheckBoxStyle)style).checkboxOn : ((CheckBoxStyle)style).checkboxOff);
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
