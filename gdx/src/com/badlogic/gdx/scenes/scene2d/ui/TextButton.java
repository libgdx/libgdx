
package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.esotericsoftware.tablelayout.Cell;

/** A button with a child {@link Label} to display text.
 * @author Nathan Sweet */
public class TextButton extends Button {
	private final Label label;
	private TextButtonStyle style;

	// BOZO - Region/patch constructors?

	public TextButton (Skin skin) {
		this("", skin);
	}

	public TextButton (String text, Skin skin) {
		this(text, skin.getStyle("default", TextButtonStyle.class));
	}

	public TextButton (String text, TextButtonStyle style) {
		this(text, style, null);
	}

	public TextButton (String text, TextButtonStyle style, String name) {
		super(style, name);
		this.style = style;
		label = new Label(text, new LabelStyle(style.font, style.fontColor));
		label.setAlignment(Align.CENTER);
		add(label).expand().fill();
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (ButtonStyle style) {
		if (!(style instanceof TextButtonStyle)) throw new IllegalArgumentException("style must be a TextButtonStyle.");
		super.setStyle(style);
		this.style = (TextButtonStyle)style;
		if (label != null) {
			TextButtonStyle textButtonStyle = (TextButtonStyle)style;
			LabelStyle labelStyle = label.getStyle();
			labelStyle.font = textButtonStyle.font;
			labelStyle.fontColor = textButtonStyle.fontColor;
			label.setStyle(labelStyle);
		}
	}

	public TextButtonStyle getStyle () {
		return style;
	}

	public void draw (SpriteBatch batch, float parentAlpha) {
		if (isPressed) {
			if (style.downFontColor != null) label.setColor(style.downFontColor);
		} else {
			if (style.fontColor != null)
				label.setColor((isChecked && style.checkedFontColor != null) ? style.checkedFontColor : style.fontColor);
		}
		super.draw(batch, parentAlpha);
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

	/** The style for a text button, see {@link TextButton}.
	 * @author Nathan Sweet */
	static public class TextButtonStyle extends ButtonStyle {
		public BitmapFont font;
		/** Optional. */
		public Color fontColor, downFontColor, checkedFontColor;

		public TextButtonStyle () {
		}

		public TextButtonStyle (NinePatch down, NinePatch up, NinePatch checked, float pressedOffsetX, float pressedOffsetY,
			float unpressedOffsetX, float unpressedOffsetY, BitmapFont font, Color fontColor, Color downFontColor,
			Color checkedFontColor) {
			super(down, up, checked, pressedOffsetX, pressedOffsetY, unpressedOffsetX, unpressedOffsetY);
			this.font = font;
			this.fontColor = fontColor;
			this.downFontColor = downFontColor;
			this.checkedFontColor = checkedFontColor;
		}
	}
}
