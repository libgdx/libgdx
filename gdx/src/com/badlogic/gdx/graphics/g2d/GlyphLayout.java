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
import com.badlogic.gdx.utils.IntArray;
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

		// Avoid wrapping one line per character, which is very inefficient.
		boolean wrapOrTruncate = (wrap && targetWidth > fontData.spaceXadvance * 3) || truncate != null;

		Color nextColor = color;
		boolean markupEnabled = fontData.markupEnabled;
		if (markupEnabled) colorStack.add(color);

		boolean isLastRun = false;
		float y = 0, down = fontData.down;
		GlyphRun lineRun = null; // Collects glyphs for the current line.
		Glyph lastGlyph = null; // Last glyph of the previous run on the same line, used for kerning between runs.
		int runStart = start;
		outer:
		while (true) {
			int runEnd = -1;
			boolean newline = false;
			if (start == end) { // End of text.
				if (runStart == end) break; // No run to process, we're done.
				runEnd = end; // Process the final run.
				isLastRun = true;
			} else {
				// Each run is delimited by newline or left square bracket.
				switch (str.charAt(start++)) {
				case '\n': // End of line.
					runEnd = start - 1;
					newline = true;
					break;
				case '[': // Possible color tag.
					if (markupEnabled) {
						int length = parseColorMarkup(str, start, end);
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
				if (true) { // Can occur eg when a color tag is at text start or a line is "\n".
					// Store the newRun that has ended.
					GlyphRun newRun = glyphRunPool.obtain();
					fontData.getGlyphs(newRun, str, runStart, runEnd, lastGlyph);
					newRun.x = 0;
					newRun.y = y;
					newRun.colorChangeIndices.add(0);
					newRun.colors.add(Color.rgba8888(color));
					color = nextColor;
					if (newRun.glyphs.size == 0) {
						glyphRunPool.free(newRun);
						if (lineRun == null) break runEnded; // else wrap and truncate must still be processed for lineRun.
					} else if (lineRun == null) {
						lineRun = newRun;
						runs.add(lineRun);
					} else {
						lineRun.appendRun(newRun, markupEnabled);
						glyphRunPool.free(newRun);
					}

					if (newline || isLastRun) {
						adjustLastGlyph(fontData, lineRun);
						lastGlyph = null;
					} else
						lastGlyph = lineRun.glyphs.peek();

					if (!wrapOrTruncate || lineRun.glyphs.size == 0) // No wrap or truncate, or no glyphs.
						break runEnded;

					if (newline || isLastRun) {
						// Wrap or truncate. First xadvance is the first glyph's X offset relative to the drawing position.
						float runWidth = lineRun.xAdvances.first();
						for (int i = 1; i < lineRun.xAdvances.size; i++) {
							Glyph glyph = lineRun.glyphs.get(i - 1);
							float glyphWidth = getGlyphWidth(glyph, fontData);
							if (runWidth + glyphWidth - epsilon <= targetWidth) {
								// Glyph fits.
								runWidth += lineRun.xAdvances.items[i];
								continue;
							}

							if (truncate != null) {
								// Truncate.
								truncate(fontData, runs.peek(), targetWidth, truncate);
								break outer;
							}

							// Wrap.
							int wrapIndex = fontData.getWrapIndex(lineRun.glyphs, i);
							if ((wrapIndex == 0 && lineRun.x == 0) // Require at least one glyph per line.
								|| wrapIndex >= lineRun.glyphs.size) { // Wrap at least the glyph that didn't fit.
								wrapIndex = i - 1;
							}
							GlyphRun next = wrap(fontData, lineRun, wrapIndex);
							lineRun = next;
							if (next == null) break runEnded; // All wrapped glyphs were whitespace.
							runs.add(next);

							y += down;
							lineRun.x = 0;
							lineRun.y = y;

							// Start the wrap-loop again, another wrap might be necessary.
							runWidth = lineRun.xAdvances.first();
							if (lineRun.xAdvances.size > 1) runWidth += lineRun.xAdvances.get(1);
							i = 1;
						}
					}
				}

				if (newline) {
					lineRun = null;
					lastGlyph = null;

					// Next run will be on the next line.
					if (runEnd == runStart) // Blank line.
						y += down * fontData.blankLineScale;
					else
						y += down;
				}

				runStart = start;
			}
		}

		height = fontData.capHeight + Math.abs(y);

		calculateAndSetWidths(fontData);

		alignRuns(targetWidth, halign);

		// Free the color stack.
		if (markupEnabled) {
			for (int i = 1, n = colorStack.size; i < n; i++) // Skip the first color, which was passed in.
				colorPool.free(colorStack.get(i));
			colorStack.clear();
		}
	}

	/** Calculate run widths and the entire layout width. */
	private void calculateAndSetWidths (BitmapFontData fontData) {
		float width = 0;
		Object[] runsItems = runs.items;
		for (int i = 0, n = runs.size; i < n; i++) {
			GlyphRun run = (GlyphRun)runsItems[i];
			float[] xAdvances = run.xAdvances.items;
			float runWidth = run.x + xAdvances[0], max = 0; // run.x is needed to ensure floats are rounded same as above.
			Object[] glyphs = run.glyphs.items;
			for (int ii = 0, nn = run.glyphs.size; ii < nn;) {
				Glyph glyph = (Glyph)glyphs[ii];
				float glyphWidth = getGlyphWidth(glyph, fontData);
				max = Math.max(max, runWidth + glyphWidth); // A glyph can extend past the right edge of subsequent glyphs.
				ii++;
				runWidth += xAdvances[ii];
			}
			run.width = Math.max(runWidth, max) - run.x;
			width = Math.max(width, run.x + run.width);
		}
		this.width = width;
	}

	/** Align runs to center or right of targetWidth. Requires run.width of runs to be already set */
	private void alignRuns (float targetWidth, int halign) {
		if ((halign & Align.left) == 0) { // Not left aligned, so must be center or right aligned.
			boolean center = (halign & Align.center) != 0;
			Object[] runsItems = runs.items;
			for (int i = 0, n = runs.size; i < n; i++) {
				GlyphRun run = (GlyphRun)runsItems[i];
				run.x += center ? 0.5f * (targetWidth - run.width) : targetWidth - run.width;
			}
		}
	}

	/** @param truncate May be empty string. */
	private void truncate (BitmapFontData fontData, GlyphRun run, float targetWidth, String truncate) {
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

		if (fontData.markupEnabled) {
			// Remove all color changes for indices > run.glyphs.size.
			for (int i = run.colorChangeIndices.size - 1; i > 0; i--) { // i > 0 because first value is never dropped
				if (run.colorChangeIndices.get(i) < run.glyphs.size) break;
				run.colorChangeIndices.pop();
				run.colors.pop();
			}
		}

		glyphRunPool.free(truncateRun);
	}

	/** Breaks a run into two runs at the specified wrapIndex.
	 * @return May be null if second run is all whitespace. */
	private GlyphRun wrap (BitmapFontData fontData, GlyphRun first, int wrapIndex) {
		Array<Glyph> glyphs2 = first.glyphs; // Starts with all the glyphs.
		int glyphCount = first.glyphs.size;
		FloatArray xAdvances2 = first.xAdvances; // Starts with all the xadvances.

		// Skip whitespace before the wrap index.
		int firstEnd = wrapIndex;
		for (; firstEnd > 0; firstEnd--)
			if (!fontData.isWhitespace((char)glyphs2.get(firstEnd - 1).id)) break;

		// Skip whitespace after the wrap index.
		int secondStart = wrapIndex;
		for (; secondStart < glyphCount; secondStart++)
			if (!fontData.isWhitespace((char)glyphs2.get(secondStart).id)) break;

		// Copy wrapped glyphs and xadvances to second run.
		// The second run will contain the remaining glyph data, so swap instances rather than copying.
		GlyphRun second = null;
		if (secondStart < glyphCount) {
			second = glyphRunPool.obtain();

			Array<Glyph> glyphs1 = second.glyphs; // Starts empty.
			glyphs1.addAll(glyphs2, 0, firstEnd);
			glyphs2.removeRange(0, secondStart - 1);
			first.glyphs = glyphs1;
			second.glyphs = glyphs2;

			FloatArray xAdvances1 = second.xAdvances; // Starts empty.
			xAdvances1.addAll(xAdvances2, 0, firstEnd + 1);
			xAdvances2.removeRange(1, secondStart); // Leave first entry to be overwritten by next line.
			xAdvances2.items[0] = getLineOffset(glyphs2, fontData);
			first.xAdvances = xAdvances1;
			second.xAdvances = xAdvances2;

			if (!fontData.markupEnabled) {
				second.colorChangeIndices.add(0); // Set run color.
				second.colors.add(first.colors.peek());
			} else {
				int firstGlyphCount = first.glyphs.size; // After wrapping it.
				int secondGlyphCount = second.glyphs.size;

				// Adjust colorChangeIndices according to droppedGlyphCount, which is the number of glyphs dropped by wrapping.
				int droppedGlyphCount = glyphCount - firstGlyphCount - secondGlyphCount;
				if (droppedGlyphCount > 0) {
					for (int i = first.colorChangeIndices.size - 1; i > 0; i--) { // i > 0 because first the value is never adjusted.
						int colorChangeIndex = first.colorChangeIndices.get(i);
						if (colorChangeIndex <= firstGlyphCount) break;
						first.colorChangeIndices.set(i, colorChangeIndex - droppedGlyphCount);
					}
				}

				for (int i = 0, n = first.colorChangeIndices.size; i < n; i++) {
					int firstColorChangeIndex = first.colorChangeIndices.get(i);
					if (firstColorChangeIndex < firstGlyphCount) {
						if (second.colorChangeIndices.isEmpty()) {
							second.colorChangeIndices.add(0); // Set run start color.
							second.colors.add(first.colors.get(i));
						} else
							second.colors.set(0, first.colors.get(i)); // Update run start color.
					} else {
						first.colorChangeIndices.removeIndex(i);
						int color = first.colors.removeIndex(i);
						n--;
						i--;
						int index = firstColorChangeIndex - firstGlyphCount;
						if (index == 0)
							second.colors.set(0, color); // Update run start color.
						else {
							second.colorChangeIndices.add(index);
							second.colors.add(color);
						}
					}
				}
			}
		} else {
			// Second run is empty, just trim whitespace glyphs from end of first run.
			glyphs2.truncate(firstEnd);
			xAdvances2.truncate(firstEnd + 1);

			if (fontData.markupEnabled) {
				// Remove all color changes for indices >= firstEnd.
				for (int i = first.colorChangeIndices.size - 1; i > 0; i--) { // i > 0 because the first value is never dropped.
					if (first.colorChangeIndices.get(i) < firstEnd) break;
					first.colorChangeIndices.pop();
					first.colors.pop();
				}
			}
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
		if (!last.fixedWidth) run.xAdvances.items[run.xAdvances.size - 1] = getGlyphWidth(last, fontData);
	}

	/** Returns the distance from the glyph's drawing position to the right edge of the glyph. */
	private float getGlyphWidth (Glyph glyph, BitmapFontData fontData) {
		return (glyph.width + glyph.xoffset) * fontData.scaleX - fontData.padRight;
	}

	/** Returns an X offset for the first glyph so when drawn, none of it is left of the line's drawing position. */
	private float getLineOffset (Array<Glyph> glyphs, BitmapFontData fontData) {
		return -glyphs.first().xoffset * fontData.scaleX - fontData.padLeft;
	}

	private int parseColorMarkup (CharSequence str, int start, int end) {
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
		for (int i = start + 1; i < end; i++) {
			char ch = str.charAt(i);
			if (ch != ']') continue;
			Color namedColor = Colors.get(str.subSequence(start, i).toString());
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

	/** Stores glyphs and positions for a piece of text and indices and colors of color changes and does not span multiple lines.
	 * @author Nathan Sweet */
	static public class GlyphRun implements Poolable {
		public Array<Glyph> glyphs = new Array();
		/** Contains glyphs.size+1 entries: First entry is X offset relative to the drawing position (the offset of the line).
		 * Subsequent entries are the X advance relative to previous glyph position. Last entry is the width of the last glyph. */
		public FloatArray xAdvances = new FloatArray();
		public float x, y, width;

		/** The indexes at which the color of the glyphs changes. The new color can be retrieved from colors. The first entry (0)
		 * must be set and will normally be 0 (start to use the first color of colors from the first glyph on) */
		public IntArray colorChangeIndices = new IntArray(4);
		/** The change colors for colorChangeIndices. The first entry (0) must be set and states the base color for this run */
		public IntArray colors = new IntArray(4); // rgba8888

		void appendRun (GlyphRun run, boolean markupEnabled) {
			int glyphsCount = glyphs.size;
			glyphs.addAll(run.glyphs);
			// xAdvances[0] is the offset of the whole line so it it should only be added to an empty run
			int xadvanceStartIndex = xAdvances.isEmpty() ? 0 : 1;
			for (int i = xadvanceStartIndex, n = run.xAdvances.size; i < n; i++)
				xAdvances.add(run.xAdvances.get(i));

			if (colorChangeIndices.isEmpty()) {
				colorChangeIndices.add(0);
				colors.add(run.colors.peek());
			} else if (markupEnabled) {
				// First color is always set but only needs to be added if different from last color
				int runColor = run.colors.first();
				if (runColor != colors.peek()) {
					colorChangeIndices.add(glyphsCount);
					colors.add(runColor);
				}
				// Append other color changes
				for (int i = 1, n = run.colorChangeIndices.size; i < n; i++) {
					colorChangeIndices.add(run.colorChangeIndices.get(i) + glyphsCount);
					colors.add(run.colors.get(i));
				}
			}
		}

		public void reset () {
			glyphs.clear();
			xAdvances.clear();
			width = 0;
			colorChangeIndices.clear();
			colors.clear();
		}

		public String toString () {
			StringBuilder buffer = new StringBuilder(glyphs.size + 32);
			Array<Glyph> glyphs = this.glyphs;
			for (int i = 0, n = glyphs.size; i < n; i++) {
				Glyph g = glyphs.get(i);
				buffer.append((char)g.id);
			}
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
