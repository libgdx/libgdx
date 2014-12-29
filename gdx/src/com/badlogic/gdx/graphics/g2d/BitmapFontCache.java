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
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.NumberUtils;

/** Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the location
 * of each glyph each frame.
 * @author Nathan Sweet
 * @author Matthias Mann
 * @author davebaol
 * @author Alexander Dorokhov */
public class BitmapFontCache {

	private final BitmapFont font;

	private float[][] vertexData;

	private int[] idx;
	/** Used internally to ensure a correct capacity for multi-page font vertex data. */
	private int[] tmpGlyphCount;

	private float x, y;
	private float color = Color.WHITE.toFloatBits();
	private final Color tempColor = new Color(1, 1, 1, 1);
	private final TextBounds textBounds = new TextBounds();
	private boolean integer = true;
	private int glyphCount = 0;

	/** An array for each page containing an entry for each glyph from that page, where the entry is the index of the character in
	 * the full text being cached. */
	private IntArray[] glyphIndices;

	private boolean textChanged;
	private float oldTint = 0;

	private TextMarkup markup = new TextMarkup();

	private int charsCount;

	public BitmapFontCache (BitmapFont font) {
		this(font, font.usesIntegerPositions());
	}

	/** @param integer If true, rendering positions will be at integer values to avoid filtering artifacts. */
	public BitmapFontCache (BitmapFont font, boolean integer) {
		this.font = font;
		this.integer = integer;

		int regionsLength = font.regions.length;
		if (regionsLength == 0) throw new IllegalArgumentException("The specified font must contain at least one texture page.");

		this.vertexData = new float[regionsLength][];

		this.idx = new int[regionsLength];
		int vertexDataLength = vertexData.length;
		if (vertexDataLength > 1) { // If we have multiple pages...
			// Contains the indices of the glyph in the Cache as they are added.
			glyphIndices = new IntArray[vertexDataLength];
			for (int i = 0, n = glyphIndices.length; i < n; i++) {
				glyphIndices[i] = new IntArray();
			}

			tmpGlyphCount = new int[vertexDataLength];
		}
	}

	/** Sets the position of the text, relative to the position when the cached text was created.
	 * @param x The x coordinate
	 * @param y The y coordinate */
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

	private Color setColor (Color color, float floatColor) {
		int intBits = NumberUtils.floatToIntColor(floatColor);
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	/** Tints all text currently in the cache. Does not affect subsequently added text. */
	public void tint (Color tint) {
		final float floatTint = tint.toFloatBits();
		if (textChanged || oldTint != floatTint) {
			textChanged = false;
			oldTint = floatTint;
			markup.tint(this, tint);
		}
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (float color) {
		for (int j = 0, length = vertexData.length; j < length; j++) {
			float[] vertices = vertexData[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = color;
		}
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (Color tint) {
		final float color = tint.toFloatBits();
		for (int j = 0, length = vertexData.length; j < length; j++) {
			float[] vertices = vertexData[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = color;
		}
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = NumberUtils.intToFloatColor(intBits);
		for (int j = 0, length = vertexData.length; j < length; j++) {
			float[] vertices = vertexData[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = color;
		}
	}

	/** Sets the color of the specified characters. This may only be called after {@link #setText(CharSequence, float, float)} and
	 * is reset every time setText is called. */
	public void setColors (Color tint, int start, int end) {
		setColors(tint.toFloatBits(), start, end);
	}

	/** Sets the color of the specified characters. This may only be called after {@link #setText(CharSequence, float, float)} and
	 * is reset every time setText is called. */
	public void setColors (float color, int start, int end) {
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

	/** Sets the color of subsequently added text. Does not affect text currently in the cache. */
	public void setColor (Color tint) {
		color = tint.toFloatBits();
		markup.setDefaultChunk(tint, charsCount);
	}

	/** Sets the color of subsequently added text. Does not affect text currently in the cache. */
	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = NumberUtils.intToFloatColor(intBits);
		markup.setDefaultChunk(intBits, charsCount);
	}

	/** Sets the color of subsequently added text. Does not affect text currently in the cache. */
	public void setColor (float color) {
		this.color = color;
		markup.setDefaultChunk(color, charsCount);
	}

	public Color getColor () {
		int intBits = NumberUtils.floatToIntColor(color);
		Color color = tempColor;
		color.r = (intBits & 0xff) / 255f;
		color.g = ((intBits >>> 8) & 0xff) / 255f;
		color.b = ((intBits >>> 16) & 0xff) / 255f;
		color.a = ((intBits >>> 24) & 0xff) / 255f;
		return color;
	}

	public void draw (Batch spriteBatch) {
		TextureRegion[] regions = font.getRegions();
		for (int j = 0, n = vertexData.length; j < n; j++) {
			if (idx[j] > 0) { // ignore if this texture has no glyphs
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
		setColors(color);
		draw(spriteBatch);
		color.a = oldAlpha;
		setColors(color);
	}

	/** Removes all glyphs in the cache. */
	public void clear () {
		x = 0;
		y = 0;
		glyphCount = 0;
		charsCount = 0;
		markup.clear();
		for (int i = 0, n = idx.length; i < n; i++) {
			if (glyphIndices != null) glyphIndices[i].clear();
			idx[i] = 0;
		}
	}

	/** Counts the actual glyphs excluding characters used to markup the text. */
	private int countGlyphs (CharSequence seq, int start, int end) {
		int count = end - start;
		while (start < end) {
			char ch = seq.charAt(start++);
			if (ch == '[') {
				count--;
				if (!(start < end && seq.charAt(start) == '[')) { // non escaped '['
					while (start < end && seq.charAt(start) != ']') {
						start++;
						count--;
					}
					count--;
				}
				start++;
			}
		}
		return count;
	}

	private void requireSequence (CharSequence seq, int start, int end) {
		if (vertexData.length == 1) {
			// don't scan sequence if we just have one page and markup is disabled
			int newGlyphCount = font.markupEnabled ? countGlyphs(seq, start, end) : end - start;
			require(0, newGlyphCount);
		} else {
			for (int i = 0, n = tmpGlyphCount.length; i < n; i++)
				tmpGlyphCount[i] = 0;

			// determine # of glyphs in each page
			while (start < end) {
				char ch = seq.charAt(start++);
				if (ch == '[' && font.markupEnabled) {
					if (!(start < end && seq.charAt(start) == '[')) { // non escaped '['
						while (start < end && seq.charAt(start) != ']')
							start++;
						start++;
						continue;
					}
					start++;
				}
				Glyph g = font.data.getGlyph(ch);
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

	private float addToCache (CharSequence str, float x, float y, int start, int end) {
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		BitmapFontData data = font.data;
		textChanged = start < end;
		if (data.scaleX == 1 && data.scaleY == 1) {
			while (start < end) {
				char ch = str.charAt(start++);
				if (ch == '[' && font.markupEnabled) {
					if (!(start < end && str.charAt(start) == '[')) { // non escaped '['
						start += TextMarkup.parseColorTag(markup, str, charsCount, start, end) + 1;
						color = markup.getLastColor().toFloatBits();
						continue;
					}
					start++;
				}
				lastGlyph = data.getGlyph(ch);
				if (lastGlyph != null) {
					addGlyph(lastGlyph, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height);
					x += lastGlyph.xadvance;
					break;
				}
			}
			while (start < end) {
				char ch = str.charAt(start++);
				if (ch == '[' && font.markupEnabled) {
					if (!(start < end && str.charAt(start) == '[')) { // non escaped '['
						start += TextMarkup.parseColorTag(markup, str, charsCount, start, end) + 1;
						color = markup.getLastColor().toFloatBits();
						continue;
					}
					start++;
				}
				Glyph g = data.getGlyph(ch);
				if (g != null) {
					x += lastGlyph.getKerning(ch);
					lastGlyph = g;
					addGlyph(lastGlyph, x + g.xoffset, y + g.yoffset, g.width, g.height);
					x += g.xadvance;
				}
			}
		} else {
			float scaleX = data.scaleX, scaleY = data.scaleY;
			while (start < end) {
				char ch = str.charAt(start++);
				if (ch == '[' && font.markupEnabled) {
					if (!(start < end && str.charAt(start) == '[')) { // non escaped '['
						start += TextMarkup.parseColorTag(markup, str, charsCount, start, end) + 1;
						color = markup.getLastColor().toFloatBits();
						continue;
					}
					start++;
				}
				lastGlyph = data.getGlyph(ch);
				if (lastGlyph != null) {
					addGlyph(lastGlyph, //
						x + lastGlyph.xoffset * scaleX, //
						y + lastGlyph.yoffset * scaleY, //
						lastGlyph.width * scaleX, //
						lastGlyph.height * scaleY);
					x += lastGlyph.xadvance * scaleX;
					break;
				}
			}
			while (start < end) {
				char ch = str.charAt(start++);
				if (ch == '[' && font.markupEnabled) {
					if (!(start < end && str.charAt(start) == '[')) { // non escaped '['
						start += TextMarkup.parseColorTag(markup, str, charsCount, start, end) + 1;
						color = markup.getLastColor().toFloatBits();
						continue;
					}
					start++;
				}
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
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x2;
		vertices[idx++] = y;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx] = v;

		charsCount++;
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
					else if (font.isBreakChar(str.charAt(lineEnd - 1))) break;
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
	
	/**
	 * Provide any additional characters that should act as break characters when the label is wrapped.
	 * By default, only whitespace characters act as break chars.
	 */
	public void setBreakChars(char[] breakChars) {
		font.setBreakChars(breakChars);
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

	/** Count of characters currently being in cache */
	public int getCharsCount () {
		return charsCount;
	}
}
