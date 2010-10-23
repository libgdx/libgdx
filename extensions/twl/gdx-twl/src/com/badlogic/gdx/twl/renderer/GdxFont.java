/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.twl.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.BitmapFont;
import com.badlogic.gdx.graphics.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.BitmapFontCache;
import com.badlogic.gdx.graphics.Texture;

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontCache;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.utils.StateExpression;

/**
 * @author Nathan Sweet <misc@n4te.com>
 */
class GdxFont implements Font {
	final TwlRenderer renderer;
	final BitmapFont bitmapFont;
	private final FontState[] fontStates;

	public GdxFont (TwlRenderer renderer, BitmapFont bitmapFont, Map<String, String> params, Collection<FontParameter> condParams) {
		this.bitmapFont = bitmapFont;
		this.renderer = renderer;
		ArrayList<FontState> states = new ArrayList<FontState>();
		for (FontParameter p : condParams) {
			HashMap<String, String> effective = new HashMap<String, String>(params);
			effective.putAll(p.getParams());
			states.add(new FontState(p.getCondition(), effective));
		}
		states.add(new FontState(null, params));
		this.fontStates = states.toArray(new FontState[states.size()]);
	}

	public int drawText (AnimationState as, int x, int y, CharSequence str) {
		return drawText(as, x, y, str, 0, str.length());
	}

	public int drawText (AnimationState as, int x, int y, CharSequence str, int start, int end) {
		y = Gdx.graphics.getHeight() - y;
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY;
		com.badlogic.gdx.graphics.Color color = renderer.getColor(fontState.color);
		return bitmapFont.draw(renderer.spriteBatch, str, x, y, color);
	}

	public int drawMultiLineText (AnimationState as, int x, int y, CharSequence str, int width,
		de.matthiasmann.twl.HAlignment align) {
		y = Gdx.graphics.getHeight() - y;
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY;
		com.badlogic.gdx.graphics.Color color = renderer.getColor(fontState.color);
		return bitmapFont.drawMultiLineText(renderer.spriteBatch, str, x, y, color, width, HAlignment.values()[align.ordinal()]);
	}

	public FontCache cacheText (FontCache cache, CharSequence str) {
		return cacheText(cache, str, 0, str.length());
	}

	public FontCache cacheText (FontCache cache, CharSequence str, int start, int end) {
		if (cache == null) cache = new GdxFontCache(this, str.length());
		return (GdxFontCache)bitmapFont
			.cacheText((GdxFontCache)cache, str, 0, 0, com.badlogic.gdx.graphics.Color.WHITE, start, end);
	}

	public FontCache cacheMultiLineText (FontCache cache, CharSequence str, int width, de.matthiasmann.twl.HAlignment align) {
		if (cache == null) cache = new GdxFontCache(this, str.length());
		return (GdxFontCache)bitmapFont.cacheMultiLineText((GdxFontCache)cache, str, 0, 0, com.badlogic.gdx.graphics.Color.WHITE,
			width, HAlignment.values()[align.ordinal()]);
	}

	public void destroy () {
		bitmapFont.dispose();
	}

	public int getBaseLine () {
		return bitmapFont.getBaseLine();
	}

	public int getLineHeight () {
		return bitmapFont.getLineHeight();
	}

	public int getSpaceWidth () {
		return bitmapFont.getSpaceWidth();
	}

	public int getEM () {
		return bitmapFont.getEM();
	}

	public int getEX () {
		return bitmapFont.getEX();
	}

	public int computeMultiLineTextWidth (CharSequence str) {
		return bitmapFont.computeMultiLineTextWidth(str);
	}

	public int computeTextWidth (CharSequence str) {
		return bitmapFont.computeTextWidth(str);
	}

	public int computeTextWidth (CharSequence str, int start, int end) {
		return bitmapFont.computeTextWidth(str, start, end);
	}

	public int computeVisibleGlpyhs (CharSequence str, int start, int end, int width) {
		return bitmapFont.computeVisibleGlpyhs(str, start, end, width);
	}

	FontState evalFontState (AnimationState animationState) {
		int i = 0;
		for (int n = fontStates.length - 1; i < n; i++)
			if (fontStates[i].condition.evaluate(animationState)) break;
		return fontStates[i];
	}

	static private class FontState {
		final StateExpression condition;
		final Color color;
		final int offsetX;
		final int offsetY;

		public FontState (StateExpression condition, Map<String, String> params) {
			this.condition = condition;
			String colorStr = params.get("color");
			if (colorStr == null) throw new IllegalArgumentException("Color must be defined.");
			color = Color.parserColor(colorStr);
			if (color == null) throw new IllegalArgumentException("Unknown color name: " + colorStr);
			String value = params.get("offsetX");
			offsetX = value == null ? 0 : Integer.parseInt(value);
			value = params.get("offsetY");
			offsetY = value == null ? 0 : Integer.parseInt(value);
		}
	}

	static private class GdxFontCache extends BitmapFontCache implements FontCache {
		private final GdxFont font;

		public GdxFontCache (GdxFont font, int glyphCount) {
			super(font.bitmapFont.getTexture(), glyphCount);
			this.font = font;
		}

		public void draw (AnimationState as, int x, int y) {
			y = Gdx.graphics.getHeight() - y;
			GdxFont.FontState fontState = font.evalFontState(as);
			TwlRenderer renderer = font.renderer;
			setColor(renderer.getColor(fontState.color));
			setPosition(x + fontState.offsetX, y + fontState.offsetY);
			draw(renderer.spriteBatch);
		}

		public void destroy () {
		}
	}
}
