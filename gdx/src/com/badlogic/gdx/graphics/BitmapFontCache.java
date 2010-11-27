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
import com.badlogic.gdx.graphics.BitmapFont.TextBounds;

/**
 * Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the location
 * of each glyph each frame.
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class BitmapFontCache {
	private final BitmapFont font;
	private float[] vertices = new float[0];
	private int idx;
	private float x, y;
	private float color = Color.WHITE.toFloatBits();
	private final TextBounds textBounds = new TextBounds();

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
		float color = Float.intBitsToFloat(intBits);
		if (color == this.color) return;
		this.color = color;
		float[] vertices = this.vertices;
		for (int i = 2, n = idx; i < n; i += 5)
			vertices[i] = color;
	}

	public void draw (SpriteBatch spriteBatch) {
		spriteBatch.draw(font.getSprite().getTexture(), vertices, 0, idx);
	}

	private void reset (int glyphCount) {
		x = 0;
		y = 0;
		idx = 0;

		int vertexCount = glyphCount * 20;
		if (vertices == null || vertices.length < vertexCount) vertices = new float[vertexCount];
	}

	private int addToCache (CharSequence str, float x, float y, int start, int end) {
		float startX = x;
		BitmapFont font = this.font;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = font.getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				addGlyph(lastGlyph, x, y);
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
				addGlyph(lastGlyph, x, y);
				x += g.xadvance;
			}
		}
		return (int)(x - startX);
	}

	private void addGlyph (Glyph glyph, float x, float y) {
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
	 * Caches a string with the specified position.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline).
	 */
	public TextBounds setText (CharSequence str, float x, float y) {
		return setText(str, x, y, 0, str.length());
	}

	/**
	 * Caches a substring with the specified position.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @param start The first character of the string to draw.
	 * @param end The last character of the string to draw (exclusive).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline).
	 */
	public TextBounds setText (CharSequence str, float x, float y, int start, int end) {
		reset(end - start);
		y += font.yOffset;
		textBounds.width = addToCache(str, x, y, start, end);
		textBounds.height = font.capHeight;
		return textBounds;
	}

	/**
	 * Caches a string, which may contain newlines (\n), with the specified position.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line).
	 */
	public TextBounds setMultiLineText (CharSequence str, float x, float y) {
		return setMultiLineText(str, x, y, 0, HAlignment.LEFT);
	}

	/**
	 * Caches a string, which may contain newlines (\n), with the specified position and alignment. Each line is aligned
	 * horizontally within a rectangle of the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line).
	 */
	public TextBounds setMultiLineText (CharSequence str, float x, float y, float alignmentWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		reset(length);

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
			int lineWidth = addToCache(str, x + xOffset, y, start, lineEnd);
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = font.capHeight + (numLines - 1) * font.lineHeight;
		return textBounds;
	}

	/**
	 * Caches a string, which may contain newlines (\n), with the specified position. Each line is automatically wrapped to keep it
	 * within a rectangle of the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line).
	 */
	public TextBounds setWrappedText (CharSequence str, float x, float y, float wrapWidth) {
		return setWrappedText(str, x, y, wrapWidth, HAlignment.LEFT);
	}

	/**
	 * Caches a string, which may contain newlines (\n), with the specified position. Each line is automatically wrapped to keep it
	 * within a rectangle of the specified width, and aligned horizontally within that rectangle.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFont#getCapHeight() cap height}).
	 * @return The bounds of the cached string (the height is the distance from y to the baseline of the last line).
	 */
	public TextBounds setWrappedText (CharSequence str, float x, float y, float wrapWidth, HAlignment alignment) {
		BitmapFont font = this.font;

		int length = str.length();
		reset(length);

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
			int lineWidth = addToCache(str, x + xOffset, y, start, lineEnd);
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = font.capHeight + (numLines - 1) * font.lineHeight;
		return textBounds;
	}

	/**
	 * Returns the size of the cached string. The height is the distance from the top of most capital letters in the font (the
	 * {@link BitmapFont#getCapHeight() cap height}) to the baseline of the last line of text.
	 */
	public TextBounds getBounds () {
		return textBounds;
	}

	/**
	 * Returns the x position of the cached string, relative to the position when the string was cached.
	 */
	public float getX () {
		return x;
	}

	/**
	 * Returns the y position of the cached string, relative to the position when the string was cached.
	 */
	public float getY () {
		return y;
	}
}
