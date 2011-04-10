/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

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
	public VAlignment valignment = VAlignment.BOTTOM;
	public String text;
	public TextBounds bounds;

	private WrapType wrapType;
	private HAlignment halignment;
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
		bounds = cache.setText(text, 0, cache.getFont().isFlipped() ? 0 : cache.getFont().getCapHeight());
		width = bounds.width;
		height = bounds.height;
	}

	public void setMultiLineText (String text) {
		this.text = text;
		wrapType = WrapType.multiLine;
		bounds = cache.getFont().getMultiLineBounds(text);
		cache.setMultiLineText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height);
		width = bounds.width;
		height = bounds.height;
	}

	public void setWrappedText (String text, HAlignment alignment) {
		this.text = text;
		this.halignment = alignment;
		wrapType = WrapType.wrapped;
		bounds = cache.getFont().getWrappedBounds(text, width);
		cache.setWrappedText(text, 0, cache.getFont().isFlipped() ? 0 : bounds.height, width, alignment);
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
			setWrappedText(text, halignment);
			break;
		}
	}

	@Override protected void draw (SpriteBatch batch, float parentAlpha) {
		cache.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		switch (valignment) {
		case TOP:
			cache.setPosition(x, y + height - bounds.height);
			break;
		case CENTER:
			cache.setPosition(x, y + (height - bounds.height) / 2);
			break;
		case BOTTOM:
			cache.setPosition(x, y);
			break;
		}
		cache.draw(batch);
	}

	@Override protected boolean touchDown (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchUp (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override protected boolean touchDragged (float x, float y, int pointer) {
		if (!touchable) return false;
		return x > 0 && y > 0 && x < width && y < height;
	}

	@Override public Actor hit (float x, float y) {
		return x > 0 && y > 0 && x < width && y < height ? this : null;
	}

	public void layout () {
		if (wrapType == WrapType.wrapped && lastWidth != width) setWrappedText(text, halignment);
		lastWidth = width;
	}

	public float getPrefWidth () {
		switch (wrapType) {
		case singleLine:
			return cache.getFont().getBounds(text).width;
		case multiLine:
			return cache.getFont().getMultiLineBounds(text).width;
		case wrapped:
		}
		return 0;
	}

	public float getPrefHeight () {
		switch (wrapType) {
		case singleLine:
			return cache.getFont().getBounds(text).height;
		case multiLine:
			return cache.getFont().getMultiLineBounds(text).width;
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
