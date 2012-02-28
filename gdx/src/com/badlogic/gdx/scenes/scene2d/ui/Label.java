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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Layout;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;

/** A text label, with optional word wrapping.
 * <p>
 * The preferred size of the label is determined by the actual text bounds, unless {@link #setWrap(boolean) word wrap} is enabled.
 * @author Nathan Sweet */
public class Label extends Widget {
	private LabelStyle style;
	private final TextBounds bounds = new TextBounds();
	private CharSequence text;
	private BitmapFontCache cache;
	private float prefWidth, prefHeight;
	private int labelAlign = Align.LEFT;
	private HAlignment lineAlign = HAlignment.LEFT;
	private boolean wrap;
	private float lastPrefHeight;

	public Label (Skin skin) {
		this("", skin);
	}

	public Label (CharSequence text, Skin skin) {
		this(text, skin.getStyle(LabelStyle.class), null);
	}

	/** Creates a label, using a {@link LabelStyle} that has a BitmapFont with the specified name from the skin and the specified
	 * color. */
	public Label (CharSequence text, String fontName, Color color, Skin skin) {
		this(text, new LabelStyle(skin.getFont(fontName), color), null);
	}

	/** Creates a label, using a {@link LabelStyle} that has a BitmapFont with the specified name and the specified color from the
	 * skin. */
	public Label (CharSequence text, String fontName, String colorName, Skin skin) {
		this(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName)), null);
	}

	public Label (CharSequence text, LabelStyle style) {
		this(text, style, null);
	}

	public Label (CharSequence text, LabelStyle style, String name) {
		super(name);
		if (text == null) text = "";
		this.text = text;
		setStyle(style);
		width = getPrefWidth();
		height = getPrefHeight();
	}

	public void setStyle (LabelStyle style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		if (style.font == null) throw new IllegalArgumentException("Missing LabelStyle font.");
		this.style = style;
		cache = new BitmapFontCache(style.font);
		if (style.fontColor != null) cache.setColor(style.fontColor);
		computeBounds();
		invalidateHierarchy();
	}

	/** Returns the label's style. Modifying the returned style may not have an effect until {@link #setStyle(LabelStyle)} is
	 * called. */
	public LabelStyle getStyle () {
		return style;
	}

	public void setText (CharSequence text) {
		if (text == null) throw new IllegalArgumentException("text cannot be null.");
		if (text.equals(this.text)) return;
		this.text = text;
		computeBounds();
		invalidateHierarchy();
	}

	public CharSequence getText () {
		return text;
	}

	public TextBounds getTextBounds () {
		return bounds;
	}

	/** If false, the text will only wrap where it contains newlines (\n). The preferred size of the label will be the text bounds.
	 * If true, the text will word wrap using the width of the label. The preferred width of the label will be 0, it is expected
	 * that the something external will set the width of the label. Default is false. */
	public void setWrap (boolean wrap) {
		this.wrap = wrap;
		computeBounds();
		invalidateHierarchy();
	}

	/** @param wrapAlign Aligns each line of text horizontally and all the text vertically.
	 * @see Align */
	public void setAlignment (int wrapAlign) {
		setAlignment(wrapAlign, wrapAlign);
	}

	/** @param labelAlign Aligns all the text with the label widget.
	 * @param lineAlign Aligns each line of text (left, right, or center).
	 * @see Align */
	public void setAlignment (int labelAlign, int lineAlign) {
		this.labelAlign = labelAlign;

		if ((lineAlign & Align.LEFT) != 0)
			this.lineAlign = HAlignment.LEFT;
		else if ((lineAlign & Align.RIGHT) != 0)
			this.lineAlign = HAlignment.RIGHT;
		else
			this.lineAlign = HAlignment.CENTER;

		invalidate();
	}

	public void setColor (float color) {
		cache.setColor(color);
	}

	public void setColor (Color tint) {
		cache.setColor(tint);
	}

	public void setColor (float r, float g, float b, float a) {
		cache.setColor(r, g, b, a);
	}

	public Color getColor () {
		return cache.getColor();
	}

	private void computeBounds () {
		if (wrap)
			bounds.set(cache.getFont().getWrappedBounds(text, width));
		else
			bounds.set(cache.getFont().getMultiLineBounds(text));
	}

	@Override
	public void layout () {
		computeBounds();

		if (wrap) {
			float prefHeight = getPrefHeight();
			if (prefHeight != lastPrefHeight) {
				lastPrefHeight = prefHeight;
				invalidateHierarchy();
			}
		}

		float y;
		if ((labelAlign & Align.TOP) != 0) {
			y = cache.getFont().isFlipped() ? 0 : height - bounds.height;
			y += style.font.getDescent();
		} else if ((labelAlign & Align.BOTTOM) != 0) {
			y = cache.getFont().isFlipped() ? height - bounds.height : 0;
			y -= style.font.getDescent();
		} else
			y = (height - bounds.height) / 2;
		if (!cache.getFont().isFlipped()) y += bounds.height;

		float x;
		if ((labelAlign & Align.LEFT) != 0)
			x = 0;
		else if ((labelAlign & Align.RIGHT) != 0) {
			x = width - bounds.width;
		} else
			x = (width - bounds.width) / 2;

		if (wrap)
			cache.setWrappedText(text, x, y, bounds.width, lineAlign);
		else
			cache.setMultiLineText(text, x, y, bounds.width, lineAlign);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		validate();
		cache.setPosition(x, y);
		cache.draw(batch, color.a * parentAlpha);
	}

	public float getPrefWidth () {
		if (wrap) return 0;
		return bounds.width;
	}

	public float getPrefHeight () {
		return bounds.height - style.font.getDescent() * 2;
	}

	/** The style for a label, see {@link Label}.
	 * @author Nathan Sweet */
	static public class LabelStyle {
		public BitmapFont font;
		/** Optional. */
		public Color fontColor;

		public LabelStyle () {
		}

		public LabelStyle (BitmapFont font, Color fontColor) {
			this.font = font;
			this.fontColor = fontColor;
		}
	}
}
