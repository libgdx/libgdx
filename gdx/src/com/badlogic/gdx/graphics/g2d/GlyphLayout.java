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
	public final Array<GlyphRun> runs = new Array();
	public float width, height;

	private final Array<Color> colorStack = new Array(4);

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

	/** Calls {@link #setText(BitmapFont, CharSequence, int, int, Color, float, int, boolean, String) setText} with the whole
	 * string and no truncation. */
	public void setText (BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
		setText(font, str, 0, str.length(), color, targetWidth, halign, wrap, null);
	}

	/** @param color The default color to use for the text (the BitmapFont {@link BitmapFont#getColor() color} is not used). If
	 *           {@link BitmapFontData#markupEnabled} is true, color markup tags in the specified string may change the color for
	 *           portions of the text.
	 * @param halign Horizontal alignment of the text, see {@link Align}.
	 * @param targetWidth The width used for alignment, line wrapping, and truncation. May be zero if those features are not used.
	 * @param truncate If not null and the width of the glyphs exceed targetWidth, the glyphs are truncated and the glyphs for the
	 *           specified truncate string are placed at the end. Empty string can be used to truncate without adding glyphs.
	 *           Truncate should not be used with text that contains multiple lines. Wrap is ignored if truncate is not null. */
	public void setText (BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign,
		boolean wrap, String truncate) {
		if (truncate != null)
			wrap = true; // Causes truncate code to run, doesn't actually cause wrapping.
		else if (targetWidth <= font.data.spaceWidth) //
			wrap = false; // Avoid one line per character, which is very inefficient.

		BitmapFontData fontData = font.data;
		boolean markupEnabled = fontData.markupEnabled;

		Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class);
		Array<GlyphRun> runs = this.runs;
		glyphRunPool.freeAll(runs);
		runs.clear();

		float x = 0, y = 0, width = 0;
		int lines = 0;

		Array<Color> colorStack = this.colorStack;
		Color nextColor = color;
		colorStack.add(color);
		Pool<Color> colorPool = Pools.get(Color.class);

		int runStart = start;
		outer:
		while (true) {
			// Each run is delimited by newline or left square bracket.
			int runEnd = -1;
			boolean newline = false, colorRun = false;
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
							colorRun = true;
						}
					}
					break;
				}
			}

			if (runEnd != -1) {
				if (runEnd != runStart) { // Can happen (eg) when a color tag is at text start.
					// Store the run that has ended.
					GlyphRun run = glyphRunPool.obtain();
					run.color.set(color);
					run.x = x;
					run.y = y;
					fontData.getGlyphs(run, str, runStart, runEnd, colorRun);
					if (run.glyphs.size == 0)
						glyphRunPool.free(run);
					else {
						runs.add(run);

						// Compute the run width, wrap if necessary, and position the run.
						float[] xAdvances = run.xAdvances.items;
						for (int i = 0, n = run.xAdvances.size; i < n; i++) {
							float xAdvance = xAdvances[i];
							x += xAdvance;

							// Don't wrap if the glyph would fit with just its width (no xadvance or kerning).
							if (wrap && x > targetWidth && i > 1
								&& x - xAdvance + (run.glyphs.get(i - 1).xoffset + run.glyphs.get(i - 1).width) * fontData.scaleX
									- 0.0001f > targetWidth) {

								if (truncate != null) {
									truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
									x = run.x + run.width;
									break outer;
								}

								int wrapIndex = fontData.getWrapIndex(run.glyphs, i);
								if ((run.x == 0 && wrapIndex == 0) // Require at least one glyph per line.
									|| wrapIndex >= run.glyphs.size) { // Wrap at least the glyph that didn't fit.
									wrapIndex = i - 1;
								}
								GlyphRun next;
								if (wrapIndex == 0)
									next = run; // No wrap index, move entire run to next line.
								else {
									next = wrap(fontData, run, glyphRunPool, wrapIndex, i);
									runs.add(next);
								}

								// Start the loop over with the new run on the next line.
								width = Math.max(width, run.x + run.width);
								x = 0;
								y += fontData.down;
								lines++;
								next.x = 0;
								next.y = y;
								i = -1;
								n = next.xAdvances.size;
								xAdvances = next.xAdvances.items;
								run = next;
							} else
								run.width += xAdvance;
						}
					}
				}

				if (newline) {
					// Next run will be on the next line.
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

		// Determine truncate string size.
		GlyphRun truncateRun = glyphRunPool.obtain();
		fontData.getGlyphs(truncateRun, truncate, 0, truncate.length(), true);
		float truncateWidth = 0;
		for (int i = 1, n = truncateRun.xAdvances.size; i < n; i++)
			truncateWidth += truncateRun.xAdvances.get(i);
		targetWidth -= truncateWidth;

		// Determine visible glyphs.
		int count = 0;
		float width = run.x;
		while (count < run.xAdvances.size) {
			float xAdvance = run.xAdvances.get(count);
			width += xAdvance;
			if (width > targetWidth) {
				run.width = width - run.x - xAdvance;
				break;
			}
			count++;
		}

		if (count > 1) {
			// Some run glyphs fit, append truncate glyphs.
			run.glyphs.truncate(count - 1);
			run.xAdvances.truncate(count);
			adjustLastGlyph(fontData, run);
			if (truncateRun.xAdvances.size > 0) run.xAdvances.addAll(truncateRun.xAdvances, 1, truncateRun.xAdvances.size - 1);
		} else {
			// No run glyphs fit, use only truncate glyphs.
			run.glyphs.clear();
			run.xAdvances.clear();
			run.xAdvances.addAll(truncateRun.xAdvances);
			if (truncateRun.xAdvances.size > 0) run.width += truncateRun.xAdvances.get(0);
		}
		run.glyphs.addAll(truncateRun.glyphs);
		run.width += truncateWidth;

		glyphRunPool.free(truncateRun);
	}

	private GlyphRun wrap (BitmapFontData fontData, GlyphRun first, Pool<GlyphRun> glyphRunPool, int wrapIndex, int widthIndex) {
		GlyphRun second = glyphRunPool.obtain();
		second.color.set(first.color);
		int glyphCount = first.glyphs.size;

		// Increase first run width up to the end index.
		while (widthIndex < wrapIndex)
			first.width += first.xAdvances.get(widthIndex++);

		// Reduce first run width by the wrapped glyphs that have contributed to the width.
		while (widthIndex > wrapIndex + 1)
			first.width -= first.xAdvances.get(--widthIndex);

		// Copy wrapped glyphs and xAdvances to second run.
		// The second run will contain the remaining glyph data, so swap instances rather than copying to reduce large allocations.
		if (wrapIndex < glyphCount) {
			Array<Glyph> glyphs1 = second.glyphs; // Starts empty.
			Array<Glyph> glyphs2 = first.glyphs; // Starts with all the glyphs.
			glyphs1.addAll(glyphs2, 0, wrapIndex);
			glyphs2.removeRange(0, wrapIndex - 1);
			first.glyphs = glyphs1;
			second.glyphs = glyphs2;
			// Equivalent to:
			// second.glyphs.addAll(first.glyphs, wrapIndex, glyphCount - wrapIndex);
			// first.glyphs.truncate(wrapIndex);

			FloatArray xAdvances1 = second.xAdvances; // Starts empty.
			FloatArray xAdvances2 = first.xAdvances; // Starts with all the xAdvances.
			xAdvances1.addAll(xAdvances2, 0, wrapIndex + 1);
			xAdvances2.removeRange(1, wrapIndex); // Leave first entry to be overwritten by next line.
			xAdvances2.set(0, -glyphs2.first().xoffset * fontData.scaleX - fontData.padLeft);
			first.xAdvances = xAdvances1;
			second.xAdvances = xAdvances2;
			// Equivalent to:
			// second.xAdvances.add(-second.glyphs.first().xoffset * fontData.scaleX - fontData.padLeft);
			// second.xAdvances.addAll(first.xAdvances, wrapIndex + 1, first.xAdvances.size - (wrapIndex + 1));
			// first.xAdvances.truncate(wrapIndex + 1);
		}

		if (wrapIndex == 0) {
			// If the first run is now empty, remove it.
			glyphRunPool.free(first);
			runs.pop();
		} else
			adjustLastGlyph(fontData, first);

		return second;
	}

	/** Adjusts the xadvance of the last glyph to use its width instead of xadvance. */
	private void adjustLastGlyph (BitmapFontData fontData, GlyphRun run) {
		Glyph last = run.glyphs.peek();
		if (fontData.isWhitespace((char)last.id)) return; // Can happen when doing truncate.
		float width = (last.xoffset + last.width) * fontData.scaleX - fontData.padRight;
		run.width += width - run.xAdvances.peek(); // Can cause the run width to be > targetWidth, but the problem is minimal.
		run.xAdvances.set(run.xAdvances.size - 1, width);
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
					if (i - start <= 7) { // RRGGBB or fewer chars.
						for (int ii = 0, nn = 9 - (i - start); ii < nn; ii++)
							colorInt = colorInt << 4;
						colorInt |= 0xff;
					}
					Color color = colorPool.obtain();
					colorStack.add(color);
					Color.rgba8888ToColor(color, colorInt);
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

	public void reset () {
		Pools.get(GlyphRun.class).freeAll(runs);
		runs.clear();

		width = 0;
		height = 0;
	}

	public String toString () {
		if (runs.size == 0) return "";
		StringBuilder buffer = new StringBuilder(128);
		buffer.append(width);
		buffer.append('x');
		buffer.append(height);
		buffer.append('\n');
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
		public Array<Glyph> glyphs = new Array();
		/** Contains glyphs.size+1 entries: First entry is X offset relative to the drawing position. Subsequent entries are the X
		 * advance relative to previous glyph position. Last entry is the width of the last glyph. */
		public FloatArray xAdvances = new FloatArray();
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
