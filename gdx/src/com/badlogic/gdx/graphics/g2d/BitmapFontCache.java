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
import com.badlogic.gdx.utils.NumberUtils;

/** Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the location
 * of each glyph each frame.
 * @author Nathan Sweet
 * @author Matthias Mann */
public class BitmapFontCache {
	private final BitmapFont font;
	private float[] vertices = new float[0];
	private int idx;
	private float x, y;
	private float color = Color.WHITE.toFloatBits();
	private final Color tempColor = new Color(Color.WHITE);
	private final TextBounds textBounds = new TextBounds();
	private boolean integer = true;

	public BitmapFontCache (BitmapFont font) {
		this(font, font.usesIntegerPositions());
	}

	/** Creates a new BitmapFontCache
	 * @param font the font to use
	 * @param integer whether to use integer positions and sizes. */
	public BitmapFontCache (BitmapFont font, boolean integer) {
		this.font = font;
		this.integer = integer;
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
		float[] vertices = this.vertices;
		for (int i = 0, n = idx; i < n; i += 5) {
			vertices[i] += xAmount;
			vertices[i + 1] += yAmount;
		}
	}

	public void setColor (float color) {
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	public void setColor (Color tint) {
		final float color = tint.toFloatBits();
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = NumberUtils.intToFloatColor(intBits);
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	/** Sets the color of the specified characters. This may only be called after {@link #setText(CharSequence, float, float)} and
	 * is reset every time setText is called. */
	public void setColor (Color tint, int start, int end) {
		final float color = tint.toFloatBits();
		float[] vertices = this.vertices;
		for (int i = start * 20 + 2, n = end * 20; i < n; i += 5)
			vertices[i] = color;
	}

	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(font.getRegion().getTexture(), vertices, 0, idx);
	}

	public void draw (SpriteBatch spriteBatch, float alphaModulation) {
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
		x = 0;
		y = 0;
		idx = 0;
	}

	private void require (int glyphCount) {
		int vertexCount = idx + glyphCount * 20;
		if (vertices == null || vertices.length < vertexCount) {
			float[] newVertices = new float[vertexCount];
			System.arraycopy(vertices, 0, newVertices, 0, idx);
			vertices = newVertices;
		}
	}

	private float addToCache (CharSequence str, float x, float y, int start, int end) {
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		BitmapFontData data = font.data;
		if (data.scaleX == 1 && data.scaleY == 1) {
			while (start < end) {
				lastGlyph = data.getGlyph(str.charAt(start++));
				if (lastGlyph != null) {
					addGlyph(lastGlyph, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height);
					x += lastGlyph.xadvance;
					break;
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
				}
			}
		} else {
			float scaleX = data.scaleX, scaleY = data.scaleY;
			while (start < end) {
				lastGlyph = data.getGlyph(str.charAt(start++));
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

		final float[] vertices = this.vertices;

		if (integer) {
			x = Math.round(x);
			y = Math.round(y);
			x2 = Math.round(x2);
			y2 = Math.round(y2);
		}

		int idx = this.idx;
		this.idx += 20;

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
		vertices[idx++] = v;
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
		require(end - start);
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
		require(length);

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
		require(length);

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
		return vertices;
	}
}
