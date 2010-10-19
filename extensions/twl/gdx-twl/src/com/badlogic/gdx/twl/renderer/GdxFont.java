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

import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.HAlignment;
import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.Font;
import de.matthiasmann.twl.renderer.FontCache;
import de.matthiasmann.twl.renderer.FontParameter;
import de.matthiasmann.twl.utils.StateExpression;

/**
 * 
 * @author Matthias Mann
 */
class GdxFont implements Font {
	final GdxRenderer renderer;
	final BitmapFont bitmapFont;
	private final FontState[] fontStates;
	private int[] multiLineInfo;

	GdxFont (GdxRenderer renderer, BitmapFont font, Map<String, String> params, Collection<FontParameter> condParams) {
		this.renderer = renderer;
		this.bitmapFont = font;
		font.font = this;

		ArrayList<FontState> states = new ArrayList<FontState>();
		for (FontParameter p : condParams) {
			HashMap<String, String> effective = new HashMap<String, String>(params);
			effective.putAll(p.getParams());
			states.add(createFontState(p.getCondition(), effective));
		}
		states.add(createFontState(null, params));
		this.fontStates = states.toArray(new FontState[states.size()]);
	}

	private FontState createFontState (StateExpression cond, Map<String, String> params) {
		String colorStr = params.get("color");
		if (colorStr == null) {
			throw new IllegalArgumentException("color needs to be defined");
		}
		int offsetX = parseInt(params.get("offsetX"), 0);
		int offsetY = parseInt(params.get("offsetY"), 0);
		int underlineOffset = parseInt(params.get("underlineOffset"), 0);
		Color color = Color.parserColor(colorStr);
		if (color == null) {
			throw new IllegalArgumentException("unknown color name");
		}
		FontState p = new FontState(cond, color, offsetX, offsetY, underlineOffset);
		return p;
	}

	private static int parseInt (String valueStr, int defaultValue) {
		if (valueStr == null) {
			return defaultValue;
		}
		return Integer.parseInt(valueStr);
	}

	private static boolean parseBoolean (String valueStr) {
		if (valueStr == null) {
			return false;
		}
		return Boolean.parseBoolean(valueStr);
	}

	FontState evalFontState (AnimationState as) {
		int i = 0;
		for (int n = fontStates.length - 1; i < n; i++) {
			if (fontStates[i].condition.evaluate(as)) {
				break;
			}
		}
		return fontStates[i];
	}

	private int[] getMultiLineInfo (int numLines) {
		if (multiLineInfo == null || multiLineInfo.length < numLines) {
			multiLineInfo = new int[numLines];
		}
		return multiLineInfo;
	}

	public int drawText (AnimationState as, int x, int y, CharSequence str) {
		return drawText(as, x, y, str, 0, str.length());
	}

	public int drawText (AnimationState as, int x, int y, CharSequence str, int start, int end) {
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY;
		int width;
		if (!bitmapFont.prepare()) {
			return 0;
		}
		try {
			renderer.tintStack.setColor(fontState.color);
			width = bitmapFont.drawText(x, y, str, start, end);
		} finally {
			bitmapFont.cleanup();
		}
		return width;
	}

	public int drawMultiLineText (AnimationState as, int x, int y, CharSequence str, int width, HAlignment align) {
		FontState fontState = evalFontState(as);
		x += fontState.offsetX;
		y += fontState.offsetY;
		int numLines;
		if (!bitmapFont.prepare()) {
			return 0;
		}
		try {
			renderer.tintStack.setColor(fontState.color);
			numLines = bitmapFont.drawMultiLineText(x, y, str, width, align);
		} finally {
			bitmapFont.cleanup();
		}
		return numLines * bitmapFont.getLineHeight();
	}

	public int computeVisibleGlpyhs (CharSequence str, int start, int end, int availWidth) {
		return bitmapFont.computeVisibleGlpyhs(str, start, end, availWidth);
	}

	public int computeTextWidth (CharSequence str) {
		return bitmapFont.computeTextWidth(str, 0, str.length());
	}

	public int computeTextWidth (CharSequence str, int start, int end) {
		return bitmapFont.computeTextWidth(str, start, end);
	}

	public int computeMultiLineTextWidth (CharSequence str) {
		return bitmapFont.computeMultiLineTextWidth(str);
	}

	public FontCache cacheText (FontCache prevCache, CharSequence str) {
		return cacheText(prevCache, str, 0, str.length());
	}

	public FontCache cacheText (FontCache prevCache, CharSequence str, int start, int end) {
		return bitmapFont.cacheText(this, prevCache, str, start, end);
	}

	public FontCache cacheMultiLineText (FontCache prevCache, CharSequence str, int width, HAlignment align) {
		return bitmapFont.cacheMultiLineText(this, prevCache, str, width, align);
	}

	public int getSpaceWidth () {
		return bitmapFont.getSpaceWidth();
	}

	public int getLineHeight () {
		return bitmapFont.getLineHeight();
	}

	public int getBaseLine () {
		return bitmapFont.getBaseLine();
	}

	public int getEM () {
		return bitmapFont.getEM();
	}

	public int getEX () {
		return bitmapFont.getEX();
	}

	public void destroy () {
		bitmapFont.destroy();
	}

	static class FontState {
		final StateExpression condition;
		final Color color;
		final int offsetX;
		final int offsetY;
		final int underlineOffset;

		public FontState (StateExpression condition, Color color, int offsetX, int offsetY, int underlineOffset) {
			this.condition = condition;
			this.color = color;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.underlineOffset = underlineOffset;
		}
	}
}
