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
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.NumberUtils;

/** Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the location
 * of each glyph each frame.
 * @author Nathan Sweet
 * @author Matthias Mann */
public class BitmapFontCache {
	/** Unused unicode space, which we're using for color change codes */
	private static final char UNUSED_UNICODE_START = 0xE000;
	private static final char UNUSED_UNICODE_END = 0xF8FF;

	/** Used to reset color back to default */
	public static final char COLOR_RESET = UNUSED_UNICODE_START;
	/** Start of custom color code space */
	private static final char COLOR_CODES_START = UNUSED_UNICODE_START + 1;
	/** End of custom color code space */
	private static final char COLOR_CODES_END = UNUSED_UNICODE_END;

	/** Each index is an offset from COLOR_CODES_START. Float is the color for that char */
	private static final FloatArray COLOR_CHARS = new FloatArray(true, 4);
	/** End of allocated custom color codes */
	private static int colorCodesEnd = COLOR_CODES_START;

	/** Add a new color to the custom color codes. The returned char can be placed inline with any text rendered by
	 * BitmapFontCache to change the color of the following text. To reset to the default color, specify {@link #COLOR_RESET}.
	 * @return The char that will change to the given color for the following text. */
	public static synchronized char addColorChar(float color) {
		int newChar = COLOR_CODES_START + COLOR_CHARS.size;
		if (newChar >= COLOR_CODES_END) {
			throw new GdxRuntimeException("Custom color code overflow");
		}
		colorCodesEnd++;
		COLOR_CHARS.add(color);
		return (char) newChar;
	}

	/** Add a new color to the custom color codes.
	 * @return The char that will change to the given color for the following text. */
	public static char addColorChar(Color color) {
		return addColorChar(color.toFloatBits());
	}

	/** Clear all custom color codes. */
	public static synchronized void clearColorChars() {
		colorCodesEnd = COLOR_CODES_START;
		COLOR_CHARS.clear();
	}

	private final BitmapFont font;

	private float[][] vertexData;

	private int[] idx;
	/** Used internally to ensure a correct capacity for multi-page font vertex data. */
	private int[] tmpGlyphCount;

	private float x, y;
	private float color = Color.WHITE.toFloatBits();
	/** Used internally to track the current glyph color (different from this.color if color-codes are in use) */
	private float glyphColor = Color.WHITE.toFloatBits();
	private final Color tempColor = new Color(Color.WHITE);
	private final TextBounds textBounds = new TextBounds();
	private boolean integer = true;
	private boolean hasColorSpans = false;

	private int glyphCount = 0;

	// For multi-page fonts, the vertices are not laid out in order.
	// This means that we have no frame of reference for setColor(Color, int, int)
	// since the "start" and "end" indices will not work here.
	// So for multi-page fonts, we need to store the INDEX of the glyph in terms of the full string
	private IntArray[] glyphIndices;

	public BitmapFontCache (BitmapFont font) {
		this(font, font.usesIntegerPositions());
	}

	/** Creates a new BitmapFontCache
	 * @param font the font to use
	 * @param integer whether to use integer positions and sizes. */
	public BitmapFontCache (BitmapFont font, boolean integer) {
		this.font = font;
		this.integer = integer;
		
		int regionsLength = font.regions.length;
		if (regionsLength == 0)
			throw new IllegalArgumentException("The specified font must contain at least 1 texture page");
		
		this.vertexData = new float[regionsLength][];
		
		this.idx = new int[regionsLength];
		int vertexDataLength = vertexData.length;
		if (vertexDataLength > 1) { // if we have multiple pages...
			// contains the indices of the glyph in the Cache as they are added
			glyphIndices = new IntArray[vertexDataLength];
			for (int i = 0, n = glyphIndices.length; i < n; i++) {
				glyphIndices[i] = new IntArray();
			}

			tmpGlyphCount = new int[vertexDataLength];
		}
	}

	/** Gets the current cached glyph count */
	public int getGlyphCount() {
		return glyphCount;
	}

	/** Sets the position of the text, relative to the position when the cached text was created.
	 * @param x The x coordinate
	 * @param y The y coodinate */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/** Sets the position of the text, relative to its current position.
	 * @param xAmount The amount in x to move the text
	 * @param yAmount The amount in y to move the text */
	public void translate (float xAmount, float yAmount) {
		if (xAmount == 0 && yAmount == 0) return;
		if (integer) {
			xAmount = Math.round(xAmount);
			yAmount = Math.round(yAmount);
		}
		x += xAmount;
		y += yAmount;

		for (int j = 0, length = vertexData.length; j < length; j++) {
			float[] vertices = vertexData[j];
			for (int i = 0, n = idx[j]; i < n; i += 5) {
				vertices[i] += xAmount;
				vertices[i + 1] += yAmount;
			}
		}
	}

	private void setAllVertexColors(float color) {
		for (int j = 0, length = vertexData.length; j < length; j++) {
			float[] vertices = vertexData[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = color;
		}
	}

	/** Set the default color of the text. Does not affect color sub-ranges set by {@link #setColor (Color, int, int)} or
	 * color-codes from {@link #addColorChar()} as long as the sub-ranges are different from the default color. */
	public void setColor (float color) {
		if (color == this.color) return;
		float oldColor = this.color;
		this.color = color;
		if (!hasColorSpans) {
			setAllVertexColors(color);
		} else {
			int newColorA = NumberUtils.floatToIntColor(color) & 0xFF000000;
			for (int j = 0, length = vertexData.length; j < length; j++) {
				float[] vertices = vertexData[j];
				for (int i = 2, n = idx[j]; i < n; i += 5) {
					if (vertices[i] == oldColor) {
						vertices[i] = color;
					} else {
						// Replace alpha channel in color-spans with main color alpha.
						int glyphColorRGB = NumberUtils.floatToIntColor(vertices[i]) & 0xFFFFFF;
						vertices[i] = NumberUtils.intToFloatColor(glyphColorRGB | newColorA);
					}
				}
			}
		}
	}

	/** Set the default color of the text. Does not affect color sub-ranges set by {@link #setColor (Color, int, int)} or
	 * color-codes from {@link #addColorChar()} as long as the sub-ranges are different from the default color. */
	public void setColor (Color tint) {
		setColor(tint.toFloatBits());
	}

	/** Set the default color of the text. Does not affect color sub-ranges set by {@link #setColor (Color, int, int)} or
	 * color-codes from {@link #addColorChar()} as long as the sub-ranges are different from the default color. */
	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		setColor(NumberUtils.intToFloatColor(intBits));
	}

	/** Sets the color of the specified sub-range of characters. This may only be called after
	 * {@link #setText(CharSequence, float, float)} and is reset every time setText is called. */
	public void setColor (Color tint, int start, int end) {
		hasColorSpans = true;
		final float color = tint.toFloatBits();

		if (vertexData.length == 1) { // only one page...
			float[] vertices = vertexData[0];
			for (int i = start * 20 + 2, n = end * 20; i < n; i += 5)
				vertices[i] = color;
		} else {
			int pageCount = vertexData.length;

			// for each page...
			for (int i = 0; i < pageCount; i++) {
				float[] vertices = vertexData[i];

				// we need to loop through the indices and determine whether the glyph is inside begin/end
				for (int j = 0, n = glyphIndices[i].size; j < n; j++) {
					int gInd = glyphIndices[i].items[j];

					// break early if the glyph is outside our bounds
					if (gInd >= end) break;

					// if the glyph is inside start and end, then change it's colour
					if (gInd >= start) { // && gInd < end
						// modify color index
						for (int off = 0; off < 20; off += 5)
							vertices[off + (j * 20 + 2)] = color;
					}
				}
			}

		}
	}

	/** Clear all sub-range colors and set to the default color */
	public void setDefaultColor () {
		hasColorSpans = false;
		setAllVertexColors(this.color);
	}

	public void draw (Batch spriteBatch) {
		TextureRegion[] regions = font.getRegions();
		for (int j = 0, n = vertexData.length; j < n; j++) {
			if (idx[j] >= 0) { //ignore if this texture has no glyphs
				float[] vertices = vertexData[j];
				spriteBatch.draw(regions[j].getTexture(), vertices, 0, idx[j]);
			}
		}
	}

	public void draw (Batch spriteBatch, int start, int end) {
		if (vertexData.length == 1) { // i.e. 1 page
			spriteBatch.draw(font.getRegion().getTexture(), vertexData[0], start * 20, (end - start) * 20);
		} else { // i.e. multiple pages
			// TODO: bounds check?

			// We basically need offset and len for each page
			// Different pages might have different offsets and lengths
			// Some pages might not need to be rendered at all..

			TextureRegion[] regions = font.getRegions();

			// for each page...
			for (int i = 0, pageCount = vertexData.length; i < pageCount; i++) {

				int offset = -1;
				int count = 0;

				// we need to loop through the indices and determine where we begin within the start/end bounds
				IntArray currentGlyphIndices = glyphIndices[i];
				for (int j = 0, n = currentGlyphIndices.size; j < n; j++) {
					int glyphIndex = currentGlyphIndices.items[j];

					// break early if the glyph is outside our bounds
					if (glyphIndex >= end) break;

					// determine if this glyph is "inside" our start/end bounds
					// if so; use the first match of that for the offset
					if (offset == -1 && glyphIndex >= start) offset = j;

					// we also need to determine the length of our vertices array...
					// we do so by counting the glyphs within our bounds
					if (glyphIndex >= start) // && gInd < end
						count++;
				}

				// this page isn't necessary to be rendered
				if (offset == -1 || count == 0) continue;

				// render the page vertex data with our determined offset and length
				spriteBatch.draw(regions[i].getTexture(), vertexData[i], offset * 20, count * 20);
			}
		}
	}

	public void draw (Batch spriteBatch, float alphaModulation) {
		if (alphaModulation == 1) {
			draw(spriteBatch);
			return;
		}
		Color color = getColor();
		float oldAlpha = color.a;
		color.a *= alphaModulation;
		setColor(color);
		draw(spriteBatch);
		color.a = oldAlpha;
		setColor(color);
	}

	public Color getColor () {
		float floatBits = color;
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/** Removes all glyphs in the cache. */
	public void clear () {
		hasColorSpans = false;
		glyphColor = this.color;
		x = 0;
		y = 0;
		glyphCount = 0;
		for (int i = 0, n = idx.length; i < n; i++) {
			if (glyphIndices != null) glyphIndices[i].clear();
			idx[i] = 0;
		}
	}

	private void requireSequence (CharSequence seq, int start, int end) {
		int newGlyphCount = end - start;
		if (vertexData.length == 1) {
			require(0, newGlyphCount); // don't scan sequence if we just have one page
		} else {
			for (int i = 0, n = tmpGlyphCount.length; i < n; i++)
				tmpGlyphCount[i] = 0;

			// determine # of glyphs in each page
			while (start < end) {
				Glyph g = font.data.getGlyph(seq.charAt(start++));
				if (g == null) continue;
				tmpGlyphCount[g.page]++;
			}
			// require that many for each page
			for (int i = 0, n = tmpGlyphCount.length; i < n; i++)
				require(i, tmpGlyphCount[i]);
		}
	}

	private void require (int page, int glyphCount) {
		if (glyphIndices != null) {
			if (glyphCount > glyphIndices[page].items.length)
				glyphIndices[page].ensureCapacity(glyphCount - glyphIndices[page].items.length);
		}

		int vertexCount = idx[page] + glyphCount * 20;
		float[] vertices = vertexData[page];
		if (vertices == null) {
			vertexData[page] = new float[vertexCount];
		} else if (vertices.length < vertexCount) {
			float[] newVertices = new float[vertexCount];
			System.arraycopy(vertices, 0, newVertices, 0, idx[page]);
			vertexData[page] = newVertices;
		}
	}

	private void updateGlyphColor(char ch) {
		if (ch == COLOR_RESET) {
			glyphColor = this.color;
		} else if (ch >= COLOR_CODES_START && ch < colorCodesEnd) {
			hasColorSpans = true;
			glyphColor = COLOR_CHARS.get(ch - COLOR_CODES_START);
		}
	}

	private float addToCache (CharSequence str, float x, float y, int start, int end) {
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		BitmapFontData data = font.data;
		if (data.scaleX == 1 && data.scaleY == 1) {
			while (start < end) {
				char ch = str.charAt(start++);
				lastGlyph = data.getGlyph(ch);
				if (lastGlyph != null) {
					addGlyph(lastGlyph, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height);
					x += lastGlyph.xadvance;
					break;
				} else {
					updateGlyphColor(ch);
				}
			}
			while (start < end) {
				char ch = str.charAt(start++);
				Glyph g = data.getGlyph(ch);
				if (g != null) {
					x += lastGlyph.getKerning(ch);
					lastGlyph = g;
					addGlyph(lastGlyph, x + g.xoffset, y + g.yoffset, g.width, g.height);
					x += g.xadvance;
				} else {
					updateGlyphColor(ch);
				}
			}
		} else {
			float scaleX = data.scaleX, scaleY = data.scaleY;
			while (start < end) {
				char ch = str.charAt(start++);
				lastGlyph = data.getGlyph(ch);
				if (lastGlyph != null) {
					addGlyph(lastGlyph, //
						x + lastGlyph.xoffset * scaleX, //
						y + lastGlyph.yoffset * scaleY, //
						lastGlyph.width * scaleX, //
						lastGlyph.height * scaleY);
					x += lastGlyph.xadvance * scaleX;
					break;
				} else {
					updateGlyphColor(ch);
				}
			}
			while (start < end) {
				char ch = str.charAt(start++);
				Glyph g = data.getGlyph(ch);
				if (g != null) {
					x += lastGlyph.getKerning(ch) * scaleX;
					lastGlyph = g;
					addGlyph(lastGlyph, //
						x + g.xoffset * scaleX, //
						y + g.yoffset * scaleY, //
						g.width * scaleX, //
						g.height * scaleY);
					x += g.xadvance * scaleX;
				} else {
					updateGlyphColor(ch);
				}
			}
		}
		return x - startX;
	}

	private void addGlyph (Glyph glyph, float x, float y, float width, float height) {
		float x2 = x + width;
		float y2 = y + height;
		final float u = glyph.u;
		final float u2 = glyph.u2;
		final float v = glyph.v;
		final float v2 = glyph.v2;

		final int page = glyph.page;

		if (glyphIndices != null) {
			glyphIndices[page].add(glyphCount++);
		}

		final float[] vertices = vertexData[page];

		if (integer) {
			x = Math.round(x);
			y = Math.round(y);
			x2 = Math.round(x2);
			y2 = Math.round(y2);
		}

		int idx = this.idx[page];
		this.idx[page] += 20;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = glyphColor;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = y2;
		vertices[idx++] = glyphColor;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = glyphColor;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y;
		vertices[idx++] = glyphColor;
		vertices[idx++] = u2;
		vertices[idx] = v;
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int) */
	public TextBounds setText (CharSequence str, float x, float y) {
		clear();
		return addText(str, x, y, 0, str.length());
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int) */
	public TextBounds setText (CharSequence str, float x, float y, int start, int end) {
		clear();
		return addText(str, x, y, start, end);
	}

	/** Adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int) */
	public TextBounds addText (CharSequence str, float x, float y) {
		return addText(str, x, y, 0, str.length());
	}

	/** Adds glyphs for the the specified text.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @param start The first character of the string to draw.
	 * @param end The last character of the string to draw (exclusive).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline). */
	public TextBounds addText (CharSequence str, float x, float y, int start, int end) {
		requireSequence(str, start, end);
		y += font.data.ascent;
		textBounds.width = addToCache(str, x, y, start, end);
		textBounds.height = font.data.capHeight;
		return textBounds;
	}

	/** Clears any cached glyphs and adds glyphs for the specified text, which may contain newlines (\n).
	 * @see #addMultiLineText(CharSequence, float, float, float, HAlignment) */
	public TextBounds setMultiLineText (CharSequence str, float x, float y) {
		clear();
		return addMultiLineText(str, x, y, 0, HAlignment.LEFT);
	}

	/** Clears any cached glyphs and adds glyphs for the specified text, which may contain newlines (\n).
	 * @see #addMultiLineText(CharSequence, float, float, float, HAlignment) */
	public TextBounds setMultiLineText (CharSequence str, float x, float y, float alignmentWidth, HAlignment alignment) {
		clear();
		return addMultiLineText(str, x, y, alignmentWidth, alignment);
	}

	/** Adds glyphs for the specified text, which may contain newlines (\n).
	 * @see #addMultiLineText(CharSequence, float, float, float, HAlignment) */
	public TextBounds addMultiLineText (CharSequence str, float x, float y) {
		return addMultiLineText(str, x, y, 0, HAlignment.LEFT);
	}

	/** Adds glyphs for the specified text, which may contain newlines (\n). Each line is aligned horizontally within a rectangle of
	 * the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @param alignment The horizontal alignment of wrapped line.
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line). */
	public TextBounds addMultiLineText (CharSequence str, float x, float y, float alignmentWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		requireSequence(str, 0, length);

		y += font.data.ascent;
		float down = font.data.down;

		float maxWidth = 0;
		float startY = y;
		int start = 0;
		int numLines = 0;
		while (start < length) {
			int lineEnd = BitmapFont.indexOf(str, '\n', start);
			float xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				float lineWidth = font.getBounds(str, start, lineEnd).width;
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			float lineWidth = addToCache(str, x + xOffset, y, start, lineEnd);
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = font.data.capHeight + (numLines - 1) * font.data.lineHeight;
		return textBounds;
	}

	/** Clears any cached glyphs and adds glyphs for the specified text, which may contain newlines (\n) and is automatically
	 * wrapped within the specified width.
	 * @see #addWrappedText(CharSequence, float, float, float, HAlignment) */
	public TextBounds setWrappedText (CharSequence str, float x, float y, float wrapWidth) {
		clear();
		return addWrappedText(str, x, y, wrapWidth, HAlignment.LEFT);
	}

	/** Clears any cached glyphs and adds glyphs for the specified text, which may contain newlines (\n) and is automatically
	 * wrapped within the specified width.
	 * @see #addWrappedText(CharSequence, float, float, float, HAlignment) */
	public TextBounds setWrappedText (CharSequence str, float x, float y, float wrapWidth, HAlignment alignment) {
		clear();
		return addWrappedText(str, x, y, wrapWidth, alignment);
	}

	/** Adds glyphs for the specified text, which may contain newlines (\n) and is automatically wrapped within the specified width.
	 * @see #addWrappedText(CharSequence, float, float, float, HAlignment) */
	public TextBounds addWrappedText (CharSequence str, float x, float y, float wrapWidth) {
		return addWrappedText(str, x, y, wrapWidth, HAlignment.LEFT);
	}

	/** Adds glyphs for the specified text, which may contain newlines (\n) and is automatically wrapped within the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @param alignment The horizontal alignment of wrapped line.
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line). */
	public TextBounds addWrappedText (CharSequence str, float x, float y, float wrapWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		requireSequence(str, 0, length);

		y += font.data.ascent;
		float down = font.data.down;

		if (wrapWidth <= 0) wrapWidth = Integer.MAX_VALUE;
		float maxWidth = 0;
		int start = 0;
		int numLines = 0;
		while (start < length) {
			int newLine = BitmapFont.indexOf(str, '\n', start);
			// Eat whitespace at start of line.
			while (start < newLine) {
				if (!BitmapFont.isWhitespace(str.charAt(start))) break;
				start++;
			}
			int lineEnd = start + font.computeVisibleGlyphs(str, start, newLine, wrapWidth);
			int nextStart = lineEnd + 1;
			if (lineEnd < newLine) {
				// Find char to break on.
				while (lineEnd > start) {
					if (BitmapFont.isWhitespace(str.charAt(lineEnd))) break;
					lineEnd--;
				}
				if (lineEnd == start) {
					if (nextStart > start + 1) nextStart--;
					lineEnd = nextStart; // If no characters to break, show all.
				} else {
					nextStart = lineEnd;
					// Eat whitespace at end of line.
					while (lineEnd > start) {
						if (!BitmapFont.isWhitespace(str.charAt(lineEnd - 1))) break;
						lineEnd--;
					}
				}
			}
			if (lineEnd > start) {
				float xOffset = 0;
				if (alignment != HAlignment.LEFT) {
					float lineWidth = font.getBounds(str, start, lineEnd).width;
					xOffset = wrapWidth - lineWidth;
					if (alignment == HAlignment.CENTER) xOffset /= 2;
				}
				float lineWidth = addToCache(str, x + xOffset, y, start, lineEnd);
				maxWidth = Math.max(maxWidth, lineWidth);
			}
			start = nextStart;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = font.data.capHeight + (numLines - 1) * font.data.lineHeight;
		return textBounds;
	}

	/** Returns the size of the cached string. The height is the distance from the top of most capital letters in the font (the
	 * {@link BitmapFont#getCapHeight() cap height}) to the baseline of the last line of text. */
	public TextBounds getBounds () {
		return textBounds;
	}

	/** Returns the x position of the cached string, relative to the position when the string was cached. */
	public float getX () {
		return x;
	}

	/** Returns the y position of the cached string, relative to the position when the string was cached. */
	public float getY () {
		return y;
	}

	public BitmapFont getFont () {
		return font;
	}

	/** Specifies whether to use integer positions or not. Default is to use them so filtering doesn't kick in as badly.
	 * @param use */
	public void setUseIntegerPositions (boolean use) {
		this.integer = use;
	}

	/** @return whether this font uses integer positions for drawing. */
	public boolean usesIntegerPositions () {
		return integer;
	}

	public float[] getVertices () {
		return getVertices(0);
	}

	public float[] getVertices (int page) {
		return vertexData[page];
	}
}
