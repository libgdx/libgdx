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

package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/** Stores runs of glyphs for a piece of text. The text may contain newlines and color markup tags.
 * @author Nathan Sweet
 * @author davebaol
 * @author Alexander Dorokhov */
public class GlyphLayout implements Poolable {
	static private final Array<Color> colorStack = new Array(4);

	public final Array<GlyphRun> runs = new Array();
	public float width, height;

	/** Creates an empty GlyphLayout. */
	public GlyphLayout () {
	}

	/** @see #setText(BitmapFont, CharSequence) */
	public GlyphLayout (BitmapFont font, CharSequence str) {
		setText(font, str);
	}

	/** @see #setText(BitmapFont, CharSequence) */
	public GlyphLayout (BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
		setText(font, str, color, targetWidth, halign, wrap);
	}

	/** @see #setText(BitmapFont, CharSequence) */
	public GlyphLayout (BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign,
		boolean wrap, String truncate) {
		setText(font, str, start, end, color, targetWidth, halign, wrap, truncate);
	}

	/** Calls {@link #setText(BitmapFont, CharSequence, int, int, Color, float, int, boolean, String) setText} with the whole
	 * string, the font's current color, and no alignment or wrapping. */
	public void setText (BitmapFont font, CharSequence str) {
		setText(font, str, 0, str.length(), font.getColor(), 0, Align.left, false, null);
	}

	/** Calls {@link #setText(BitmapFont, CharSequence, int, int, Color, float, int, boolean, String) setText} with the whole string
	 * and no truncation. */
	public void setText (BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
		setText(font, str, 0, str.length(), color, targetWidth, halign, wrap, null);
	}

	/** @param color The default color to use for the text. If {@link BitmapFontData#markupEnabled} is true, color markup tags in the
	 *           specified string may change the color for portions of the text.
	 * @param targetWidth The width used for alignment, line wrapping, and truncation. May be zero if those features are not used.
	 * @param truncate If not null and the width of the glyphs exceed targetWidth, the glyphs are truncated and the glyphs for the
	 *           specified truncate string are placed at the end. Empty string can be used to truncate without adding glyphs. */
	public void setText (BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign,
		boolean wrap, String truncate) {
		if (targetWidth == 0) wrap = false; // Avoid wrapping every character to it's own line/run, which is very inefficient.
		if (truncate != null) wrap = true; // Causes truncate code to run, doesn't actually cause wrapping.

		BitmapFontData fontData = font.data;
		boolean markupEnabled = fontData.markupEnabled;

		Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class);
		glyphRunPool.freeAll(runs);
		runs.clear();

		float x = 0, y = 0, width = 0;
		int lines = 0;

		Color nextColor = color;
		colorStack.add(color);
		Pool<Color> colorPool = Pools.get(Color.class);

		int runStart = start;
		outer:
		while (true) {
			// Each run is delimited by newline or left square bracket.
			int runEnd = -1;
			boolean newline = false;
			if (start == end) {
				if (runStart == end) break; // End of string with no run to process, we're done.
				runEnd = end; // End of string, process last run.
			} else {
				switch (str.charAt(start++)) {
				case '\n':
					// End of line.
					runEnd = start - 1;
					newline = true;
					break;
				case '[':
					// Possible color tag.
					if (markupEnabled) {
						int length = parseColorMarkup(str, start, end, colorPool);
						if (length >= 0) {
							runEnd = start - 1;
							start += length + 1;
							nextColor = colorStack.peek();
						}
					}
					break;
				}
			}

			if (runEnd != -1) {
				// Store the run that has ended.
				GlyphRun run = glyphRunPool.obtain();
				runs.add(run);
				run.color.set(color);
				run.x = x;
				run.y = y;
				fontData.getGlyphs(run, str, runStart, runEnd);

				// Compute the run width, wrap if necessary, and position the run.
				FloatArray xAdvances = run.xAdvances;
				for (int i = 0, n = xAdvances.size; i < n; i++) {
					float xAdvance = xAdvances.get(i);
					x += xAdvance;
					// Don't wrap if the glyph would fit with just its width (no x-advance or kerning).
					if (wrap && x > targetWidth && i > 0 && x - (xAdvance - run.glyphs.get(i).width) > targetWidth) {
						if (truncate != null) {
							truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
							break outer;
						}

						GlyphRun next = glyphRunPool.obtain();
						runs.add(next);
						wrap(fontData, run, next, Math.max(1, fontData.getWrapIndex(run.glyphs, i)), i);

						// Start the loop over with the new run on the next line.
						width = Math.max(width, run.width);
						x = 0;
						y += fontData.down;
						lines++;
						next.x = 0;
						next.y = y;
						i = -1;
						n = next.xAdvances.size;
						xAdvances = next.xAdvances;
						run = next;
					} else
						run.width += xAdvance;
				}

				if (newline) {
					width = Math.max(width, x);
					x = 0;
					y += fontData.down;
					lines++;
				}

				runStart = start;
				color = nextColor;
			}
		}
		width = Math.max(width, x);

		for (int i = 1, n = colorStack.size; i < n; i++)
			colorPool.free(colorStack.get(i));
		colorStack.clear();

		// Align runs to center or right of targetWidth.
		if ((halign & Align.left) == 0) { // Not left aligned, so must be center or right aligned.
			boolean center = (halign & Align.center) != 0;
			float lineWidth = 0, lineY = Integer.MIN_VALUE;
			int lineStart = 0, n = runs.size;
			for (int i = 0; i < n; i++) {
				GlyphRun run = runs.get(i);
				if (run.y != lineY) {
					lineY = run.y;
					float shift = targetWidth - lineWidth;
					if (center) shift /= 2;
					while (lineStart < i)
						runs.get(lineStart++).x += shift;
					lineWidth = 0;
				}
				lineWidth += run.width;
			}
			float shift = targetWidth - lineWidth;
			if (center) shift /= 2;
			while (lineStart < n)
				runs.get(lineStart++).x += shift;
		}

		this.width = width;
		this.height = fontData.capHeight + lines * fontData.lineHeight;
	}

	private void truncate (BitmapFontData fontData, GlyphRun run, float targetWidth, String truncate, int widthIndex,
		Pool<GlyphRun> glyphRunPool) {

		GlyphRun truncateRun = glyphRunPool.obtain();
		fontData.getGlyphs(truncateRun, truncate, 0, truncate.length());

		// Truncate glyphs to make room.
		float truncateWidth = targetWidth;
		int n = truncateRun.glyphs.size;
		for (int i = 0; i < n; i++)
			truncateWidth -= truncateRun.xAdvances.get(i);
		while (run.width > truncateWidth) {
			widthIndex--;
			run.width -= run.xAdvances.get(widthIndex);
		}
		run.glyphs.truncate(widthIndex);
		run.xAdvances.truncate(widthIndex);

		// Append truncate glyphs.
		run.glyphs.addAll(truncateRun.glyphs);
		run.xAdvances.addAll(truncateRun.xAdvances);
		run.width += truncateWidth;

		glyphRunPool.free(truncateRun);
	}

	private int parseColorMarkup (CharSequence str, int start, int end, Pool<Color> colorPool) {
		if (start == end) return -1; // String ended with "[".
		switch (str.charAt(start)) {
		case '#':
			// Parse hex color RRGGBBAA where AA is optional and defaults to 0xFF if less than 6 chars are used.
			int colorInt = 0;
			for (int i = start + 1; i < end; i++) {
				char ch = str.charAt(i);
				if (ch == ']') {
					if (i < start + 2 || i > start + 9) break; // Illegal number of hex digits.
					Color color = colorPool.obtain();
					colorStack.add(color);
					Color.rgb888ToColor(color, colorInt);
					if (i <= start + 7) color.a = 1f; // RRGGBB
					return i - start;
				}
				if (ch >= '0' && ch <= '9')
					colorInt = colorInt * 16 + (ch - '0');
				else if (ch >= 'a' && ch <= 'f')
					colorInt = colorInt * 16 + (ch - ('a' - 10));
				else if (ch >= 'A' && ch <= 'F')
					colorInt = colorInt * 16 + (ch - ('A' - 10));
				else
					break; // Unexpected character in hex color.
			}
			return -1;
		case '[': // "[[" is an escaped left square bracket.
			return -1;
		case ']': // "[]" is a "pop" color tag.
			if (colorStack.size > 1) colorPool.free(colorStack.pop());
			return 0;
		}
		// Parse named color.
		int colorStart = start;
		for (int i = start + 1; i < end; i++) {
			char ch = str.charAt(i);
			if (ch != ']') continue;
			Color namedColor = Colors.get(str.subSequence(colorStart, i).toString());
			if (namedColor == null) return -1; // Unknown color name.
			Color color = colorPool.obtain();
			colorStack.add(color);
			color.set(namedColor);
			return i - start;
		}
		return -1; // Unclosed color tag.
	}

	private void wrap (BitmapFontData fontData, GlyphRun first, GlyphRun second, int wrapIndex, int widthIndex) {
		second.color.set(first.color);

		// Reduce first run width by wrapped glyphs.
		while (widthIndex-- > wrapIndex)
			first.width -= first.xAdvances.get(widthIndex);

		// Wrap glyphs and xAdvances.
		second.glyphs.addAll(first.glyphs, wrapIndex, first.glyphs.size - wrapIndex);
		second.xAdvances.addAll(first.xAdvances, wrapIndex, first.xAdvances.size - wrapIndex);
		first.glyphs.truncate(wrapIndex);
		first.xAdvances.truncate(wrapIndex);

		// Eat whitespace at end of first run.
		for (int i = wrapIndex - 1; i >= 0; i--) {
			char ch = (char)first.glyphs.get(i).id;
			if (fontData.isWhitespace(ch)) {
				first.width -= first.xAdvances.get(i);
				continue;
			}
			if (i > 0) {
				first.glyphs.truncate(i + 1);
				first.xAdvances.truncate(i + 1);
			}
			break;
		}
	}

	public void reset () {
		Pools.get(GlyphRun.class).freeAll(runs);
		runs.clear();

		width = 0;
		height = 0;
	}

	public String toString () {
		if (runs.size == 0) return "";
		StringBuilder buffer = new StringBuilder(128);
		for (int i = 0, n = runs.size; i < n; i++) {
			buffer.append(runs.get(i).toString());
			buffer.append('\n');
		}
		buffer.setLength(buffer.length() - 1);
		return buffer.toString();
	}

	/** Stores glyphs and positions for a piece of text.
	 * @author Nathan Sweet */
	static public class GlyphRun implements Poolable {
		public final Array<Glyph> glyphs = new Array();
		public final FloatArray xAdvances = new FloatArray(); // X advance relative to previous glyph.
		public float x, y, width;
		public final Color color = new Color();

		public void reset () {
			glyphs.clear();
			xAdvances.clear();
			width = 0;
		}

		public String toString () {
			StringBuilder buffer = new StringBuilder(glyphs.size);
			Array<Glyph> glyphs = this.glyphs;
			for (int i = 0, n = glyphs.size; i < n; i++) {
				Glyph g = glyphs.get(i);
				buffer.append((char)g.id);
			}
			buffer.append(", #");
			buffer.append(color);
			buffer.append(", ");
			buffer.append(x);
			buffer.append(", ");
			buffer.append(y);
			buffer.append(", ");
			buffer.append(width);
			return buffer.toString();
		}
	}
}
