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
package com.badlogic.gdx.scenes.scene2d.actors;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Layout;

public class Label extends Actor implements Layout {
	public BitmapFontCache cache;
	public VAlignment valign = VAlignment.BOTTOM;
	public String text;
	public final TextBounds bounds = new TextBounds();

	private WrapType wrapType;
	private HAlignment halign;
	private float lastWidth = -1;

	public Label (String name, BitmapFont font) {
		super(name);
		cache = new BitmapFontCache(font);
	}

	public Label (String name, BitmapFont font, String text) {
		this(name, font);
		setText(text);
	}

	public void setText (String text) {
		this.text = text;
		wrapType = WrapType.singleLine;
		bounds.set(cache.setText(text, 0, cache.getFont().isFlipped() ? 0 : cache.getFont().getCapHeight()));
		width = bounds.width;
		height = bounds.height;
	}

	public void setMultiLineText (String text) {
		this.text = text;
		wrapType = WrapType.multiLine;
		bounds.set(cache.getFont().getMultiLineBounds(text));
		cache.setMultiLineText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height);
		width = bounds.width;
		height = bounds.height;
	}

	public void setWrappedText (String text, HAlignment halign) {
		this.text = text;
		this.halign = halign;
		wrapType = WrapType.wrapped;
		bounds.set(cache.getFont().getWrappedBounds(text, width));
		cache.setWrappedText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height, width, halign);
	}

	public void setFont (BitmapFont font) {
		cache = new BitmapFontCache(font);
		switch (wrapType) {
		case singleLine:
			setText(text);
			break;
		case multiLine:
			setMultiLineText(text);
			break;
		case wrapped:
			setWrappedText(text, halign);
			break;
		}
	}

	@Override public void draw (SpriteBatch batch, float parentAlpha) {
		cache.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		switch (valign) {
		case TOP:
			if (cache.getFont().isFlipped())
				cache.setPosition(x, y);
			else
				cache.setPosition(x, y + height - bounds.height);
			break;
		case CENTER:
			cache.setPosition(x, y + (height - bounds.height) / 2);
			break;
		case BOTTOM:
			if (cache.getFont().isFlipped())
				cache.setPosition(x, y + height - bounds.height);
			else
				cache.setPosition(x, y);
			break;
		}
		cache.draw(batch);
	}

	@Override public boolean touchDown (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override public boolean touchUp (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override public boolean touchDragged (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override public Actor hit (float x, float y) {
		return x > 0 && y > 0 && x < width && y < height ? this : null;
	}

	public void layout () {
		if (wrapType == WrapType.wrapped && lastWidth != width) setWrappedText(text, halign);
		lastWidth = width;
	}

	public void invalidate () {
		lastWidth = -1;
	}

	public float getPrefWidth () {
		switch (wrapType) {
		case singleLine:
			return cache.getFont().getBounds(text).width * scaleX;
		case multiLine:
			return cache.getFont().getMultiLineBounds(text).width * scaleX;
		case wrapped:
		}
		return 0;
	}

	public float getPrefHeight () {
		switch (wrapType) {
		case singleLine:
			return cache.getFont().getBounds(text).height * scaleY;
		case multiLine:
			return cache.getFont().getMultiLineBounds(text).height * scaleY;
		case wrapped:
		}
		return 0;
	}

	static public enum VAlignment {
		TOP, CENTER, BOTTOM
	}

	static private enum WrapType {
		singleLine, multiLine, wrapped
	}
}
