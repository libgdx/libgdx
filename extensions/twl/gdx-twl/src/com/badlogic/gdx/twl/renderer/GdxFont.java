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

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontCache;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.utils.StateExpression;

/**
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
class GdxFont implements Font {
	static private final HAlignment[] gdxAlignment = HAlignment.values();

	final GdxRenderer renderer;
	final BitmapFont bitmapFont;
	private final FontState[] fontStates;
	private final int yOffset;

	public GdxFont (GdxRenderer renderer, BitmapFont bitmapFont, Map<String, String> params, Collection<FontParameter> condParams) {
		this.bitmapFont = bitmapFont;
		this.renderer = renderer;
		yOffset = -bitmapFont.getAscent();

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
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY + yOffset;
		bitmapFont.setColor(renderer.getColor(fontState.color));
		return bitmapFont.draw(renderer.spriteBatch, str, x, y, start, end).width;
	}

	public int drawMultiLineText (AnimationState as, int x, int y, CharSequence str, int width,
		de.matthiasmann.twl.HAlignment align) {
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY + yOffset;
		bitmapFont.setColor(renderer.getColor(fontState.color));
		return bitmapFont.drawMultiLine(renderer.spriteBatch, str, x, y, width, gdxAlignment[align.ordinal()]).width;
	}

	public FontCache cacheText (FontCache cache, CharSequence str) {
		return cacheText(cache, str, 0, str.length());
	}

	public FontCache cacheText (FontCache cache, CharSequence str, int start, int end) {
		if (cache == null) cache = new GdxFontCache();
		GdxFontCache bitmapCache = (GdxFontCache)cache;
		bitmapFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		bitmapCache.setText(str, 0, yOffset, start, end);
		return cache;
	}

	public FontCache cacheMultiLineText (FontCache cache, CharSequence str, int width, de.matthiasmann.twl.HAlignment align) {
		if (cache == null) cache = new GdxFontCache();
		GdxFontCache bitmapCache = (GdxFontCache)cache;
		bitmapFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		bitmapCache.setMultiLineText(str, 0, yOffset, width, gdxAlignment[align.ordinal()]);
		return cache;
	}

	public void destroy () {
		bitmapFont.dispose();
	}

	public int getBaseLine () {
		return bitmapFont.getCapHeight();
	}

	public int getLineHeight () {
		return bitmapFont.getLineHeight();
	}

	public int getSpaceWidth () {
		return bitmapFont.getSpaceWidth();
	}

	public int getEM () {
		return bitmapFont.getLineHeight();
	}

	public int getEX () {
		return bitmapFont.getXHeight();
	}

	public int computeMultiLineTextWidth (CharSequence str) {
		return bitmapFont.getMultiLineBounds(str).width;
	}

	public int computeTextWidth (CharSequence str) {
		return bitmapFont.getBounds(str).width;
	}

	public int computeTextWidth (CharSequence str, int start, int end) {
		return bitmapFont.getBounds(str, start, end).width;
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

	private class GdxFontCache extends BitmapFontCache implements FontCache {
		public GdxFontCache () {
			super(bitmapFont);
		}

		public void draw (AnimationState as, int x, int y) {
			GdxFont.FontState fontState = evalFontState(as);
			setColor(renderer.getColor(fontState.color));
			setPosition(x + fontState.offsetX, y + fontState.offsetY);
			draw(renderer.spriteBatch);
		}

		public int getWidth () {
			return getBounds().width;
		}
		
		public int getHeight () {
			return getBounds().height;
		}

		public void destroy () {
		}
	}
}
