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
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/** Stores {@link GlyphRun runs} of glyphs for a piece of text. The text may contain newlines and color markup tags.
 * <p>
 * Where wrapping occurs is determined by {@link BitmapFontData#getWrapIndex(Array, int)}. Additionally, when
 * {@link BitmapFontData#markupEnabled} is true wrapping can occur at color start or end tags.
 * <p>
 * When wrapping occurs, whitespace is removed before and after the wrap position. Whitespace is determined by
 * {@link BitmapFontData#isWhitespace(char)}.
 * <p>
 * Glyphs positions are determined by {@link BitmapFontData#getGlyphs(GlyphRun, CharSequence, int, int, Glyph)}.
 * <p>
 * This class is not thread safe, even if synchronized externally, and must only be used from the game thread.
 * @author Nathan Sweet
 * @author davebaol
 * @author Alexander Dorokhov */
public class GlyphLayout implements Poolable {
	static private final Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class);
	static private final Pool<Color> colorPool = Pools.get(Color.class);
	static private final Array<Color> colorStack = new Array(4);
	static private final float epsilon = 0.0001f;

	public final Array<GlyphRun> runs = new Array(1);
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
		boolean wrap, @Null String truncate) {

		Array<GlyphRun> runs = this.runs;
		glyphRunPool.freeAll(runs);
		runs.clear();

		BitmapFontData fontData = font.data;
		if (start == end) { // Empty string.
			width = 0;
			height = fontData.capHeight;
			return;
		}

		if (truncate != null)
			wrap = true; // Ensures truncate code runs, doesn't actually cause wrapping.
		else if (targetWidth <= fontData.spaceXadvance * 3) //
			wrap = false; // Avoid one line per character, which is very inefficient.

		Color nextColor = color;
		boolean markupEnabled = fontData.markupEnabled;
		if (markupEnabled) {
			for (int i = 1, n = colorStack.size; i < n; i++)
				colorPool.free(colorStack.get(i));
			colorStack.clear();
			colorStack.add(color);
		}

		float x = 0, y = 0, down = fontData.down;
		Glyph lastGlyph = null;
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
						} else if (length == -2) {
							start++; // Skip first of "[[" escape sequence.
							continue outer;
						}
					}
					break;
				}
			}

			if (runEnd != -1) {
				runEnded:
				if (runEnd != runStart) { // Eg, when a color tag is at text start or a line is "\n".
					// Store the run that has ended.
					GlyphRun run = glyphRunPool.obtain();
					run.color.set(color);
					fontData.getGlyphs(run, str, runStart, runEnd, lastGlyph);
					if (run.glyphs.size == 0) {
						glyphRunPool.free(run);
						break runEnded;
					}
					if (lastGlyph != null) { // Move back the width of the last glyph from the previous run.
						x -= lastGlyph.fixedWidth ? lastGlyph.xadvance * fontData.scaleX
							: (lastGlyph.width + lastGlyph.xoffset) * fontData.scaleX - fontData.padRight;
					}
					lastGlyph = run.glyphs.peek();
					run.x = x;
					run.y = y;
					if (newline || runEnd == end) adjustLastGlyph(fontData, run);
					runs.add(run);

					int n = run.xAdvances.size;
					float[] xAdvances = run.xAdvances.items;
					if (!wrap || n == 0) { // No wrap or truncate, or no glyphs.
						if (markupEnabled) { // If disabled any subsequent run is sure to be on the next line.
							for (int i = 0; i < n; i++)
								x += xAdvances[i];
						}
						break runEnded;
					}

					// Wrap or truncate.
					x += xAdvances[0] + xAdvances[1]; // X offset relative to the drawing position + first xAdvance.
					for (int i = 2; i < n; i++) {
						Glyph glyph = run.glyphs.get(i - 1);
						float glyphWidth = (glyph.width + glyph.xoffset) * fontData.scaleX - fontData.padRight;
						if (x + glyphWidth - epsilon <= targetWidth) {
							// Glyph fits.
							x += xAdvances[i];
							continue;
						}

						if (truncate != null) {
							// Truncate.
							truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
							break outer;
						}

						// Wrap.
						y += down;
						lastGlyph = null;
						int wrapIndex = fontData.getWrapIndex(run.glyphs, i);
						if ((wrapIndex == 0 && run.x == 0) // Require at least one glyph per line.
							|| wrapIndex >= run.glyphs.size) { // Wrap at least the glyph that didn't fit.
							wrapIndex = i - 1;
						}
						GlyphRun next;
						if (wrapIndex == 0) { // Move entire run to next line.
							next = run;

							// Remove leading whitespace.
							for (int glyphCount = run.glyphs.size; wrapIndex < glyphCount; wrapIndex++)
								if (!fontData.isWhitespace((char)run.glyphs.get(wrapIndex).id)) break;
							if (wrapIndex > 0) {
								run.glyphs.removeRange(0, wrapIndex - 1);
								run.xAdvances.removeRange(1, wrapIndex);
							}
							xAdvances[0] = -run.glyphs.first().xoffset * fontData.scaleX - fontData.padLeft;

							if (runs.size > 1) { // Previous run is now at the end of a line.
								// Remove trailing whitespace and adjust last glyph.
								GlyphRun previous = runs.get(runs.size - 2);
								int lastIndex = previous.glyphs.size - 1;
								for (; lastIndex > 0; lastIndex--)
									if (!fontData.isWhitespace((char)previous.glyphs.get(lastIndex).id)) break;
								previous.glyphs.truncate(lastIndex + 1);
								previous.xAdvances.truncate(lastIndex + 2);
								adjustLastGlyph(fontData, previous);
							}
						} else {
							next = wrap(fontData, run, wrapIndex, i);
							if (next == null) { // All wrapped glyphs were whitespace.
								x = 0;
								break;
							}
							runs.add(next);
						}

						// Start the loop over with the new run on the next line.
						n = next.xAdvances.size;
						xAdvances = next.xAdvances.items;
						x = xAdvances[0];
						if (n > 1) x += xAdvances[1];
						next.x = 0;
						next.y = y;
						i = 1;
						run = next;
					}
				}

				if (newline) {
					// Next run will be on the next line.
					x = 0;
					if (runEnd == runStart) // Blank line.
						y += down * fontData.blankLineScale;
					else
						y += down;
					lastGlyph = null;
				}

				runStart = start;
				color = nextColor;
			}
		}
		height = fontData.capHeight + Math.abs(y);

		// Calculate run widths and the entire layout width.
		float width = 0;
		Object[] runsItems = runs.items;
		int runsSize = runs.size;
		for (int i = 0; i < runsSize; i++) {
			GlyphRun run = (GlyphRun)runsItems[i];
			float[] xAdvances = run.xAdvances.items;
			float runWidth = xAdvances[0], max = 0;
			Object[] glyphs = run.glyphs.items;
			for (int ii = 0, nn = run.glyphs.size; ii < nn;) {
				Glyph glyph = (Glyph)glyphs[ii];
				float glyphWidth = (glyph.width + glyph.xoffset) * fontData.scaleX - fontData.padRight;
				max = Math.max(max, runWidth + glyphWidth); // A glyph can extend past the right edge of subsequent glyphs.
				ii++;
				runWidth += xAdvances[ii];
			}
			run.width = Math.max(runWidth, max);
			width = Math.max(width, run.x + run.width);
		}
		this.width = width;

		// Align runs to center or right of targetWidth.
		if ((halign & Align.left) == 0) { // Not left aligned, so must be center or right aligned.
			boolean center = (halign & Align.center) != 0;
			float lineWidth = 0, lineY = Integer.MIN_VALUE;
			int lineStart = 0;
			for (int i = 0; i < runsSize; i++) {
				GlyphRun run = (GlyphRun)runsItems[i];
				if (run.y != lineY) {
					lineY = run.y;
					float shift = targetWidth - lineWidth;
					if (center) shift /= 2;
					while (lineStart < i)
						((GlyphRun)runsItems[lineStart++]).x += shift;
					lineWidth = run.x + run.width;
				} else
					lineWidth = Math.max(lineWidth, run.x + run.width);
			}
			float shift = targetWidth - lineWidth;
			if (center) shift /= 2;
			while (lineStart < runsSize)
				((GlyphRun)runsItems[lineStart++]).x += shift;
		}
	}

	/** @param truncate May be empty string. */
	private void truncate (BitmapFontData fontData, GlyphRun run, float targetWidth, String truncate, int widthIndex,
		Pool<GlyphRun> glyphRunPool) {

		// Determine truncate string size.
		GlyphRun truncateRun = glyphRunPool.obtain();
		fontData.getGlyphs(truncateRun, truncate, 0, truncate.length(), null);
		float truncateWidth = 0;
		if (truncateRun.xAdvances.size > 0) {
			adjustLastGlyph(fontData, truncateRun);
			float[] xAdvances = truncateRun.xAdvances.items;
			for (int i = 1, n = truncateRun.xAdvances.size; i < n; i++) // Skip first for tight bounds.
				truncateWidth += xAdvances[i];
		}
		targetWidth -= truncateWidth;

		// Determine visible glyphs.
		int count = 0;
		float width = run.x;
		float[] xAdvances = run.xAdvances.items;
		while (count < run.xAdvances.size) {
			float xAdvance = xAdvances[count];
			width += xAdvance;
			if (width > targetWidth) break;
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
		}
		run.glyphs.addAll(truncateRun.glyphs);

		glyphRunPool.free(truncateRun);
	}

	/** Breaks a run into two runs at the specified wrapIndex.
	 * @return May be null if second run is all whitespace. */
	private GlyphRun wrap (BitmapFontData fontData, GlyphRun first, int wrapIndex, int widthIndex) {
		Array<Glyph> glyphs2 = first.glyphs; // Starts with all the glyphs.
		int glyphCount = first.glyphs.size;
		FloatArray xAdvances2 = first.xAdvances; // Starts with all the xAdvances.

		// Skip whitespace before the wrap index.
		int firstEnd = wrapIndex;
		for (; firstEnd > 0; firstEnd--)
			if (!fontData.isWhitespace((char)glyphs2.get(firstEnd - 1).id)) break;

		// Skip whitespace after the wrap index.
		int secondStart = wrapIndex;
		for (; secondStart < glyphCount; secondStart++)
			if (!fontData.isWhitespace((char)glyphs2.get(secondStart).id)) break;

		// Copy wrapped glyphs and xAdvances to second run.
		// The second run will contain the remaining glyph data, so swap instances rather than copying.
		GlyphRun second = null;
		if (secondStart < glyphCount) {
			second = glyphRunPool.obtain();
			second.color.set(first.color);

			Array<Glyph> glyphs1 = second.glyphs; // Starts empty.
			glyphs1.addAll(glyphs2, 0, firstEnd);
			glyphs2.removeRange(0, secondStart - 1);
			first.glyphs = glyphs1;
			second.glyphs = glyphs2;

			FloatArray xAdvances1 = second.xAdvances; // Starts empty.
			xAdvances1.addAll(xAdvances2, 0, firstEnd + 1);
			xAdvances2.removeRange(1, secondStart); // Leave first entry to be overwritten by next line.
			xAdvances2.items[0] = -glyphs2.first().xoffset * fontData.scaleX - fontData.padLeft;
			first.xAdvances = xAdvances1;
			second.xAdvances = xAdvances2;
		} else {
			// Second run is empty, just trim whitespace glyphs from end of first run.
			glyphs2.truncate(firstEnd);
			xAdvances2.truncate(firstEnd + 1);
		}

		if (firstEnd == 0) {
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
		if (last.fixedWidth) return;
		float width = (last.width + last.xoffset) * fontData.scaleX - fontData.padRight;
		run.xAdvances.items[run.xAdvances.size - 1] = width;
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
			return -2;
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

	/** Stores glyphs and positions for a piece of text which is a single color and does not span multiple lines.
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
			StringBuilder buffer = new StringBuilder(glyphs.size + 32);
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
