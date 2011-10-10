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

/** @author Nathan Sweet */
public class Label extends Widget {
	public LabelStyle style;
	public final TextBounds bounds = new TextBounds();

	private String text;
	private BitmapFontCache cache;
	private float prefWidth, prefHeight;
	private boolean wrap;
	private int align = Align.LEFT;

	public Label (String text, Skin skin) {
		this(text, skin.getStyle(LabelStyle.class), null);
	}

	public Label (String text, LabelStyle style) {
		this(text, style, null);
	}

	public Label (String text, LabelStyle style, String name) {
		super(name);
		this.text = text;
		setStyle(style);
		touchable = false;
	}

	public void setStyle (LabelStyle style) {
		this.style = style;
		cache = new BitmapFontCache(style.font);
		cache.setColor(style.fontColor);
		invalidateHierarchy();
	}

	public void setText (String text) {
		if (text == null) throw new IllegalArgumentException("text cannot be null.");
		if (text.equals(this.text)) return;
		this.text = text;
		invalidateHierarchy();
	}

	public String getText () {
		return text;
	}

	public void setWrap (boolean wrap) {
		this.wrap = wrap;
		invalidate();
	}

	public void setAlignment (int align) {
		this.align = align;
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

	@Override
	public void layout () {
		if (!invalidated) return;
		invalidated = false;

		if (wrap)
			bounds.set(cache.getFont().getWrappedBounds(text, width));
		else
			bounds.set(cache.getFont().getMultiLineBounds(text));

		float y;
		if ((align & Align.TOP) != 0) {
			y = cache.getFont().isFlipped() ? 0 : height - bounds.height;
			y += style.font.getDescent();
		} else if ((align & Align.BOTTOM) != 0) {
			y = cache.getFont().isFlipped() ? height - bounds.height : 0;
			y -= style.font.getDescent();
		} else
			y = (height - bounds.height) / 2;
		if (!cache.getFont().isFlipped()) y += bounds.height;

		HAlignment halign;
		if ((align & Align.LEFT) != 0)
			halign = HAlignment.LEFT;
		else if ((align & Align.RIGHT) != 0)
			halign = HAlignment.RIGHT;
		else
			halign = HAlignment.CENTER;

		if (wrap)
			cache.setWrappedText(text, 0, y, width, halign);
		else
			cache.setMultiLineText(text, 0, y, width, halign);
	}

	@Override
	public void draw (SpriteBatch batch, float parentAlpha) {
		if (invalidated) layout();
		cache.setPosition(x, y);
		cache.draw(batch, parentAlpha);
	}

	@Override
	public boolean touchDown (float x, float y, int pointer) {
		return false;
	}

	@Override
	public void touchUp (float x, float y, int pointer) {
	}

	@Override
	public void touchDragged (float x, float y, int pointer) {
	}

	public float getPrefWidth () {
		if (wrap) return 100;
		if (invalidated) layout();
		return bounds.width;
	}

	public float getPrefHeight () {
		if (wrap) return 100;
		if (invalidated) layout();
		return bounds.height - style.font.getDescent() * 2;
	}

	static public class LabelStyle {
		public BitmapFont font;
		public Color fontColor;

		public LabelStyle () {
		}

		public LabelStyle (BitmapFont font, Color fontColor) {
			this.font = font;
			this.fontColor = fontColor;
		}
	}
}
