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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Renders bitmap fonts. The font consists of 2 files: an image file (or {@link Sprite}) containing the glyphs and a file in the
 * AngleCode BMFont text format that describes where each glyph is on the image. Currently only a single image of glyphs is
 * supported.<br>
 * <br>
 * Text is drawn using a {@link SpriteBatch}. Text can be cached in a {@link BitmapFontCache} for faster rendering of static text,
 * which saves needing to compute the location of each glyph each frame.<br>
 * <br>
 * * The texture for a BitmapFont loaded from a file is managed. {@link #dispose()} must be called to free the texture when no
 * longer needed. A BitmapFont loaded using a sprite is managed if the sprite's texture is managed. Disposing the BitmapFont
 * disposes the sprite's texture, which may not be desirable if the texture is still being used elsewhere.<br>
 * <br>
 * The code is based on Matthias Mann's TWL BitmapFont class. Thanks for sharing, Matthias! :)
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class BitmapFont {
	static private final int LOG2_PAGE_SIZE = 9;
	static private final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
	static private final int PAGES = 0x10000 / PAGE_SIZE;

	Sprite sprite;
	int lineHeight;
	int capHeight;
	int ascent;
	int down;

	private final Glyph[][] glyphs = new Glyph[PAGES][];
	private int spaceWidth;
	private int xHeight;
	private final TextBounds textBounds = new TextBounds();
	private float color = Color.WHITE.toFloatBits();

	/**
	 * Creates a new BitmapFont using the default 15pt Arial font included in the libgdx jar file. This is convenient to easy
	 * display some text without bothering with generating a bitmap font.
	 */
	public BitmapFont () {
		this(Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"),
			Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png"), false);
	}

	/**
	 * Creates a new BitmapFont with the glyphs relative to the specified sprite.
	 * @param sprite The sprite containing the glyphs. The glyphs must be relative to the lower left corner (ie, the sprite should
	 *           not be flipped).
	 * @param flip If true, the glyphs will be flipped for use with a perspective where 0,0 is the upper left corner.
	 */
	public BitmapFont (FileHandle fontFile, Sprite sprite, boolean flip) {
		init(fontFile, sprite, flip);
	}

	/**
	 * Creates a new BitmapFont instance based on a BMFont file and an image file holding the page with glyphs.
	 * @param flip If true, the glyphs will be flipped for use with a perspective where 0,0 is the upper left corner.
	 */
	public BitmapFont (FileHandle fontFile, FileHandle imageFile, boolean flip) {
		sprite = new Sprite(Gdx.graphics.newTexture(imageFile, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge));
		init(fontFile, sprite, flip);
	}

	private void init (FileHandle fontFile, Sprite sprite, boolean flip) {
		this.sprite = sprite;
		float invTexWidth = 1.0f / sprite.getTexture().getWidth();
		float invTexHeight = 1.0f / sprite.getTexture().getHeight();
		float uSprite = sprite.getTextureRegionX();
		float vSprite = sprite.getTextureRegionY();
		BufferedReader reader = new BufferedReader(new InputStreamReader(fontFile.read()), 512);
		try {
			reader.readLine(); // info

			String[] common = reader.readLine().split(" ", 4);
			if (common.length < 4) throw new GdxRuntimeException("Invalid font file: " + fontFile);

			if (!common[1].startsWith("lineHeight=")) throw new GdxRuntimeException("Invalid font file: " + fontFile);
			lineHeight = Integer.parseInt(common[1].substring(11));

			if (!common[2].startsWith("base=")) throw new GdxRuntimeException("Invalid font file: " + fontFile);
			int baseLine = Integer.parseInt(common[2].substring(5));

			reader.readLine(); // page
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("kernings ")) break;
				if (!line.startsWith("char ")) continue;

				Glyph glyph = new Glyph();

				StringTokenizer tokens = new StringTokenizer(line, " =");
				tokens.nextToken();
				tokens.nextToken();
				int ch = Integer.parseInt(tokens.nextToken());
				if (ch <= Character.MAX_VALUE) {
					Glyph[] page = glyphs[ch / PAGE_SIZE];
					if (page == null) glyphs[ch / PAGE_SIZE] = page = new Glyph[PAGE_SIZE];
					page[ch & PAGE_SIZE - 1] = glyph;
				} else
					continue;
				tokens.nextToken();
				float srcX = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				float srcY = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.width = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.height = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.xoffset = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				if (flip)
					glyph.yoffset = Integer.parseInt(tokens.nextToken());
				else
					glyph.yoffset = -(glyph.height + Integer.parseInt(tokens.nextToken()));
				tokens.nextToken();
				glyph.xadvance = Integer.parseInt(tokens.nextToken());

				glyph.u = uSprite + srcX * invTexWidth;
				glyph.u2 = uSprite + (srcX + glyph.width) * invTexWidth;
				if (flip) {
					glyph.v = vSprite + srcY * invTexHeight;
					glyph.v2 = vSprite + (srcY + glyph.height) * invTexHeight;
				} else {
					glyph.v2 = vSprite + srcY * invTexHeight;
					glyph.v = vSprite + (srcY + glyph.height) * invTexHeight;
				}
			}

			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				if (!line.startsWith("kerning ")) break;

				StringTokenizer tokens = new StringTokenizer(line, " =");
				tokens.nextToken();
				tokens.nextToken();
				int first = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				int second = Integer.parseInt(tokens.nextToken());
				if (first < 0 || first > Character.MAX_VALUE || second < 0 || second > Character.MAX_VALUE) continue;
				Glyph glyph = getGlyph((char)first);
				tokens.nextToken();
				int amount = Integer.parseInt(tokens.nextToken());
				glyph.setKerning(second, amount);
			}

			Glyph g = getGlyph(' ');
			spaceWidth = g != null ? g.xadvance + g.width : 1;

			g = getGlyph('x');
			xHeight = g != null ? g.height : 1;

			g = getGlyph('M');
			capHeight = g != null ? g.height : 1;

			ascent = baseLine - capHeight;
			down = -lineHeight;
			if (flip) {
				ascent = -ascent;
				down = -down;
			}
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error loading font file: " + fontFile, ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

	Glyph getGlyph (char ch) {
		Glyph[] page = glyphs[ch / PAGE_SIZE];
		if (page != null) return page[ch & PAGE_SIZE - 1];
		return null;
	}

	/**
	 * Draws a string at the specified position and color.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline). Note the same TextBounds
	 *         instance is used for all methods that return TextBounds.
	 */
	public TextBounds draw (SpriteBatch spriteBatch, CharSequence str, float x, float y) {
		return draw(spriteBatch, str, x, y, 0, str.length());
	}

	/**
	 * Draws a substring at the specified position.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @param start The first character of the string to draw.
	 * @param end The last character of the string to draw (exclusive).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline). Note the same TextBounds
	 *         instance is used for all methods that return TextBounds.
	 */
	public TextBounds draw (SpriteBatch spriteBatch, CharSequence str, float x, float y, int start, int end) {
		final Texture texture = sprite.getTexture();
		y += ascent;
		float startX = x;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				spriteBatch.draw(texture, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height,
					lastGlyph.u, lastGlyph.v, lastGlyph.u2, lastGlyph.v2, color);
				x += lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = getGlyph(ch);
			if (g == null) continue;
			x += lastGlyph.getKerning(ch);
			lastGlyph = g;
			spriteBatch.draw(texture, x + lastGlyph.xoffset, y + lastGlyph.yoffset, lastGlyph.width, lastGlyph.height, lastGlyph.u,
				lastGlyph.v, lastGlyph.u2, lastGlyph.v2, color);
			x += g.xadvance;
		}
		textBounds.width = (int)(x - startX);
		textBounds.height = capHeight;
		return textBounds;
	}

	/**
	 * Draws a string, which may contain newlines (\n), at the specified position.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline of the last line). Note the
	 *         same TextBounds instance is used for all methods that return TextBounds.
	 */
	public TextBounds drawMultiLine (SpriteBatch spriteBatch, CharSequence str, float x, float y) {
		return drawMultiLine(spriteBatch, str, x, y, 0, HAlignment.LEFT);
	}

	/**
	 * Draws a string, which may contain newlines (\n), at the specified position and alignment. Each line is aligned horizontally
	 * within a rectangle of the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline of the last line). Note the
	 *         same TextBounds instance is used for all methods that return TextBounds.
	 */
	public TextBounds drawMultiLine (SpriteBatch spriteBatch, CharSequence str, float x, float y, float alignmentWidth,
		HAlignment alignment) {
		int down = this.down;
		int start = 0;
		int numLines = 0;
		int length = str.length();
		int maxWidth = 0;
		while (start < length) {
			int lineEnd = indexOf(str, '\n', start);
			float xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = getBounds(str, start, lineEnd).width;
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			int lineWidth = draw(spriteBatch, str, x + xOffset, y, start, lineEnd).width;
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = capHeight + (numLines - 1) * lineHeight;
		return textBounds;
	}

	/**
	 * Draws a string, which may contain newlines (\n), with the specified position and color. Each line is automatically wrapped
	 * to keep it within a rectangle of the specified width.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline of the last line). Note the
	 *         same TextBounds instance is used for all methods that return TextBounds.
	 */
	public TextBounds drawWrapped (SpriteBatch spriteBatch, CharSequence str, float x, float y, float wrapWidth) {
		return drawWrapped(spriteBatch, str, x, y, wrapWidth, HAlignment.LEFT);
	}

	/**
	 * Draws a string, which may contain newlines (\n), with the specified position and color. Each line is automatically wrapped
	 * to keep it within a rectangle of the specified width, and aligned horizontally within that rectangle.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link #getCapHeight() cap height}).
	 * @return The bounds of the rendered string (the height is the distance from y to the baseline of the last line). Note the
	 *         same TextBounds instance is used for all methods that return TextBounds.
	 */
	public TextBounds drawWrapped (SpriteBatch spriteBatch, CharSequence str, float x, float y, float wrapWidth,
		HAlignment alignment) {
		int down = this.down;
		int start = 0;
		int numLines = 0;
		int length = str.length();
		int maxWidth = 0;
		while (start < length) {
			int lineEnd = start + computeVisibleGlpyhs(str, start, indexOf(str, '\n', start), wrapWidth);
			int nextLineStart;
			if (lineEnd < length) {
				int originalLineEnd = lineEnd;
				while (lineEnd > start) {
					char ch = str.charAt(lineEnd);
					if (ch == ' ' || ch == '\n') break;
					lineEnd--;
				}
				if (lineEnd == start) {
					lineEnd = originalLineEnd;
					if (lineEnd == start) lineEnd++;
					nextLineStart = lineEnd;
				} else
					nextLineStart = lineEnd + 1; // Eat space or newline.
			} else {
				if (lineEnd == start) lineEnd++;
				nextLineStart = length;
			}
			float xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = getBounds(str, start, lineEnd).width;
				xOffset = wrapWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			int lineWidth = draw(spriteBatch, str, x + xOffset, y, start, lineEnd).width;
			maxWidth = Math.max(maxWidth, lineWidth);
			start = nextLineStart;
			y += down;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = capHeight + (numLines - 1) * lineHeight;
		return textBounds;
	}

	/**
	 * Returns the size of the specified string. The height is the distance from the top of most capital letters in the font (the
	 * {@link #getCapHeight() cap height}) to the baseline. Note the same TextBounds instance is used for all methods that return
	 * TextBounds.
	 */
	public TextBounds getBounds (CharSequence str) {
		return getBounds(str, 0, str.length());
	}

	/**
	 * Returns the size of the specified substring. The height is the distance from the top of most capital letters in the font
	 * (the {@link #getCapHeight() cap height}) to the baseline. Note the same TextBounds instance is used for all methods that
	 * return TextBounds.
	 * @param start The first character of the string.
	 * @param end The last character of the string (exclusive).
	 */
	public TextBounds getBounds (CharSequence str, int start, int end) {
		int width = 0;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				width = lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = getGlyph(ch);
			if (g != null) {
				width += lastGlyph.getKerning(ch);
				lastGlyph = g;
				width += g.xadvance;
			}
		}
		textBounds.width = width;
		textBounds.height = capHeight;
		return textBounds;
	}

	/**
	 * Returns the size of the specified string, which may contain newlines. The height is the distance from the top of most
	 * capital letters in the font (the {@link #getCapHeight() cap height}) to the baseline of the last line of text. Note the same
	 * TextBounds instance is used for all methods that return TextBounds.
	 */
	public TextBounds getMultiLineBounds (CharSequence str) {
		int start = 0;
		int maxWidth = 0;
		int numLines = 0;
		int length = str.length();
		while (start < length) {
			int lineEnd = indexOf(str, '\n', start);
			int lineWidth = getBounds(str, start, lineEnd).width;
			maxWidth = Math.max(maxWidth, lineWidth);
			start = lineEnd + 1;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = capHeight + (numLines - 1) * lineHeight;
		return textBounds;
	}

	/**
	 * Returns the size of the specified string, which may contain newlines and is wrapped to keep it within a rectangle of the
	 * specified width. The height is the distance from the top of most capital letters in the font (the {@link #getCapHeight() cap
	 * height}) to the baseline of the last line of text. Note the same TextBounds instance is used for all methods that return
	 * TextBounds.
	 */
	public TextBounds getWrappedBounds (CharSequence str, float wrapWidth) {
		int start = 0;
		int numLines = 0;
		int length = str.length();
		int maxWidth = 0;
		while (start < length) {
			int lineEnd = start + computeVisibleGlpyhs(str, start, indexOf(str, '\n', start), wrapWidth);
			int nextLineStart;
			if (lineEnd < length) {
				int originalLineEnd = lineEnd;
				while (lineEnd > start) {
					char ch = str.charAt(lineEnd);
					if (ch == ' ' || ch == '\n') break;
					lineEnd--;
				}
				if (lineEnd == start) {
					lineEnd = originalLineEnd;
					if (lineEnd == start) lineEnd++;
					nextLineStart = lineEnd;
				} else
					nextLineStart = lineEnd + 1; // Eat space or newline.
			} else {
				if (lineEnd == start) lineEnd++;
				nextLineStart = length;
			}
			float xOffset = 0;
			int lineWidth = getBounds(str, start, lineEnd).width;
			maxWidth = Math.max(maxWidth, lineWidth);
			start = nextLineStart;
			numLines++;
		}
		textBounds.width = maxWidth;
		textBounds.height = capHeight + (numLines - 1) * lineHeight;
		return textBounds;
	}

	/**
	 * Returns the number of glyphs from the substring that can be rendered in the specified width.
	 * @param start The first character of the string.
	 * @param end The last character of the string (exclusive).
	 */
	public int computeVisibleGlpyhs (CharSequence str, int start, int end, float availableWidth) {
		int index = start;
		int width = 0;
		Glyph lastGlyph = null;
		for (; index < end; index++) {
			char ch = str.charAt(index);
			Glyph g = getGlyph(ch);
			if (g != null) {
				if (lastGlyph != null) width += lastGlyph.getKerning(ch);
				lastGlyph = g;
				if (width + g.width + g.xoffset > availableWidth) break;
				width += g.xadvance;
			}
		}
		return index - start;
	}

	public void setColor (Color tint) {
		this.color = tint.toFloatBits();
	}

	public void setColor (float r, float g, float b, float a) {
		int intBits = (int)(255 * a) << 24 | (int)(255 * b) << 16 | (int)(255 * g) << 8 | (int)(255 * r);
		color = Float.intBitsToFloat(intBits);
	}

	public Sprite getSprite () {
		return sprite;
	}

	/**
	 * Returns the line height, which is the distance from one line of text to the next.
	 */
	public int getLineHeight () {
		return lineHeight;
	}

	/**
	 * Returns the width of the space character.
	 */
	public int getSpaceWidth () {
		return spaceWidth;
	}

	/**
	 * Returns the x-height, which is the distance from the top of most lowercase characters to the basline.
	 */
	public int getXHeight () {
		return xHeight;
	}

	/**
	 * Returns the cap height, which is the distance from the top of most uppercase characters to the basline. Since the drawing
	 * position is the cap height of the first line, the cap height can be used to get the location of the baseline.
	 */
	public int getCapHeight () {
		return capHeight;
	}

	/**
	 * Returns the ascent, which is the distance from the cap height to the top of the tallest glyph.
	 */
	public int getAscent () {
		return ascent;
	}

	/**
	 * Disposes the texture used by this BitmapFont's sprite.
	 */
	public void dispose () {
		sprite.getTexture().dispose();
	}

	static class Glyph {
		int width, height;
		float u, v, u2, v2;
		int xoffset, yoffset;
		int xadvance;
		byte[][] kerning;

		int getKerning (char ch) {
			if (kerning != null) {
				byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
				if (page != null) return page[ch & PAGE_SIZE - 1];
			}
			return 0;
		}

		void setKerning (int ch, int value) {
			if (kerning == null) kerning = new byte[PAGES][];
			byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
			if (page == null) kerning[ch >>> LOG2_PAGE_SIZE] = page = new byte[PAGE_SIZE];
			page[ch & PAGE_SIZE - 1] = (byte)value;
		}
	}

	static int indexOf (CharSequence text, char ch, int start) {
		final int n = text.length();
		for (; start < n; start++)
			if (text.charAt(start) == ch) return start;
		return n;
	}

	static public class TextBounds {
		public int width;
		public int height;
	}

	static public enum HAlignment {
		LEFT, CENTER, RIGHT
	}
}
