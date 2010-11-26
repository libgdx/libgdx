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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.graphics.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.BitmapFont.HAlignment;

/**
 * A BitmapFontCache caches glyph geometry for a BitmapFont, providing a fast way to render static text. <br>
 * <br>
 * The code is heavily based on Matthias Mann's TWL BitmapFont class. Thanks for sharing, Matthias! :)
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class BitmapFontCache {
	private final BitmapFont font;
	private float[] vertices;
	private int idx;
	int width, height;
	private float x, y;
	private float color;

	public BitmapFontCache (BitmapFont font) {
		this.font = font;
	}

	/**
	 * Sets the position of the text, relative to the position when the cached text was created.
	 * @param x The x coordinate
	 * @param y The y coodinate
	 */
	public void setPosition (float x, float y) {
		translate(x - this.x, y - this.y);
	}

	/**
	 * Sets the position of the text, relative to its current position.
	 * @param xAmount The amount in x to move the text
	 * @param yAmount The amount in y to move the text
	 */
	public void translate (float xAmount, float yAmount) {
		if (xAmount == 0 && yAmount == 0) return;
		x += xAmount;
		y += yAmount;
		float[] vertices = this.vertices;
		for (int i = 0, n = idx; i < n; i += 5) {
			vertices[i] += xAmount;
			vertices[i + 1] += yAmount;
		}
	}

	/**
	 * Sets the tint color of the text.
	 * @param tint The {@link Color}
	 */
	public void setColor (Color tint) {
		final float color = tint.toFloatBits();
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	/**
	 * Sets the tint color of the text.
	 */
	public void setColor (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		float color = Float.intBitsToFloat(intBits);
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	/**
	 * Draws the contents of the cache via a {@link SpriteBatch}. Must be called between a {@link SpriteBatch#begin()}/
	 * {@link SpriteBatch#end()} pair.
	 * @param spriteBatch The SpriteBatch
	 */
	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(font.getSprite().getTexture(), vertices, 0, idx);
	}

	void reset (int glyphCount) {
		x = 0;
		y = 0;
		idx = 0;

		int vertexCount = glyphCount * 20;
		if (vertices == null || vertices.length < vertexCount) vertices = new float[vertexCount];
	}

	private int addToCache (CharSequence str, float x, float y, float color, int start, int end) {
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = font.getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				addGlyph(lastGlyph, x, y, color);
				x += lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = font.getGlyph(ch);
			if (g != null) {
				x += lastGlyph.getKerning(ch);
				lastGlyph = g;
				addGlyph(lastGlyph, x, y, color);
				x += g.xadvance;
			}
		}
		return (int)(x - startX);
	}

	void addGlyph (Glyph glyph, float x, float y, float color) {
		x += glyph.xoffset;
		y += glyph.yoffset;
		final float x2 = x + glyph.width;
		final float y2 = y + glyph.height;
		final float u = glyph.u;
		final float u2 = glyph.u2;
		final float v = glyph.v;
		final float v2 = glyph.v2;

		float[] vertices = this.vertices;
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

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}.
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner
	 * @param tint The color
	 */
	public void setText (CharSequence str, float x, float y, Color tint) {
		setText(str, x, y, tint, 0, str.length());
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}.
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner
	 * @param tint The color
	 * @param start The first character of the string to draw
	 * @param end The last character of the string to draw
	 */
	public void setText (CharSequence str, float x, float y, Color tint, int start, int end) {
		final float color = tint.toFloatBits();
		reset(end - start);
		y += font.yOffset;
		width = addToCache(str, x, y, color, start, end);
		height = font.capHeight;
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}. The position
	 * coincides with the top left corner of the first line's glyph. The method interprets new lines.
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param tint The color
	 */
	public void setMultiLineText (CharSequence str, float x, float y, Color tint) {
		setMultiLineText(str, x, y, tint, 0, HAlignment.LEFT);
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}. The position
	 * coincides with the top left corner of the first line's glyph. The method interprets new lines. <br>
	 * <br>
	 * You can specify the horizontal alignment of the text with the <code>alignmentWidth</code> and <code>alignment</code>
	 * parameters. The first parameter specifies the width of the rectangle the text should be aligned in (x to x +
	 * alignmentWidth). The second parameter specifies the alignment itself.
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param tint The color
	 * @param alignmentWidth The alignment width
	 * @param alignment The horizontal alignment
	 */
	public void setMultiLineText (CharSequence str, float x, float y, Color tint, float alignmentWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		reset(length);

		final float color = tint.toFloatBits();
		y += font.yOffset;
		int down = font.down;

		int maxWidth = 0;
		float startY = y;
		int start = 0;
		int numLines = 0;
		while (start < length) {
			int lineEnd = BitmapFont.indexOf(str, '\n', start);
			float xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = font.getBounds(str, start, lineEnd).width;
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			int lineWidth = addToCache(str, x + xOffset, y, color, start, lineEnd);
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		width = maxWidth;
		height = font.capHeight + (numLines - 1) * font.lineHeight;
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}. The position
	 * coincides with the top left corner of the first line's glyph. This method interprets new lines and causes the text to wrap
	 * at spaces based on the given <code>wrapWidth</code>. The wrapped text is left aligned.
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param tint The color
	 * @param wrapWidth The wrap width
	 */
	public void setWrappedText (CharSequence str, float x, float y, Color tint, float wrapWidth) {
		setWrappedText(str, x, y, tint, wrapWidth, HAlignment.LEFT);
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}. The position
	 * coincides with the top left corner of the first line's glyph. This method interprets new lines and causes the text to wrap
	 * at spaces based on the given <code>wrapWidth</code>. <br>
	 * <br>
	 * You can specify the horizontal alignment of the text within the <code>wrapWidth</code> by using the <code>alignment</code>
	 * parameter.
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param tint The color
	 * @param wrapWidth The wrap width
	 */
	public void setWrappedText (CharSequence str, float x, float y, Color tint, float wrapWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		reset(length);

		final float color = tint.toFloatBits();
		y += font.yOffset;
		int down = font.down;

		int maxWidth = 0;
		int start = 0;
		int numLines = 0;
		while (start < length) {
			int lineEnd = start + font.computeVisibleGlpyhs(str, start, BitmapFont.indexOf(str, '\n', start), wrapWidth);
			if (lineEnd < length) {
				while (lineEnd > start) {
					char ch = str.charAt(lineEnd);
					if (ch == ' ' || ch == '\n') break;
					lineEnd--;
				}
			}
			if (lineEnd == start) lineEnd++;
			float xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = font.getBounds(str, start, lineEnd).width;
				xOffset = wrapWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			int lineWidth = addToCache(str, x + xOffset, y, color, start, lineEnd);
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		width = maxWidth;
		height = font.capHeight + (numLines - 1) * font.lineHeight;
	}

	/**
	 * @return The width of the contained text
	 */
	public int getWidth () {
		return width;
	}

	/**
	 * @return The height of the contained text
	 */
	public int getHeight () {
		return height;
	}

	/**
	 * @return The x coordinate of the contained text, relative to the position when the cached text was created
	 */
	public float getX () {
		return x;
	}

	/**
	 * @return The y coordinate of the contained text, relative to the position when the cached text was created
	 */
	public float getY () {
		return y;
	}
}
