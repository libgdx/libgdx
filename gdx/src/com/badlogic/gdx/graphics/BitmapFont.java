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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads and renders AngleCode BMFont files. The bitmap font consists of 2
 * files: the .fnt file which must be saved with text encoding (not xml or
 * binary!) and the bitmap file holding the glyphs, usually in .png format. <br>
 * <br>
 * This implementation currently only supports a single glyph page. <br>
 * <br>
 * To draw text with this class you need to call one of the draw() methods
 * together with a {@link SpriteBatch}. The SpriteBatch must be in rendering
 * mode, that is, {@link SpriteBatch#begin()} must have been called before
 * drawing <br>
 * <br>
 * Additionally you can cache text in a {@link BitmapFontCache} for faster
 * rendering of static text. <br>
 * <br>
 * A BitmapFont is managed. You need to call the {@link #dispose()} method when
 * you no longer need it. <br>
 * <br>
 * The code is heavily based on Matthias Mann's TWL BitmapFont class. Thanks for
 * sharing, Matthias! :)
 * 
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class BitmapFont {
	private static final int LOG2_PAGE_SIZE = 9;
	private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
	private static final int PAGES = 0x10000 / PAGE_SIZE;

	final Texture texture;
	final int lineHeight;
	final int yOffset;
	final int down;

	private final Glyph[][] glyphs = new Glyph[PAGES][];
	private final int baseLine;
	private final int spaceWidth;
	private final int xHeight;
	private final int capHeight;

	/**
	 * Creates a new BitmapFont using the default 14pt Arial font included in
	 * the gdx jar file. This is here to get you up and running quickly.
	 */
	public BitmapFont() {
		
		this(new FileHandle() {
			@Override
			public String getFileName() {
				return "/com/badlogic/gdx/utils/arial-15.fnt";
			}

			@Override
			public InputStream getInputStream() {
				return BitmapFont.class.getResourceAsStream(getFileName());
			}
		}, new FileHandle() {
			@Override
			public String getFileName() {
				return "/com/badlogic/gdx/utils/arial-15.png";
			}

			@Override
			public InputStream getInputStream() {
				return BitmapFont.class.getResourceAsStream(getFileName());
			}
		}, false);
	}

	/**
	 * Creates a new BitmapFont instance based on a .fnt file and an image file
	 * holding the page with glyphs. Currently only supports single page
	 * AngleCode fonts.
	 * 
	 * @param fontFile
	 *            The font file
	 * @param imageFile
	 *            The image file
	 * @param flip
	 *            If true, the glyphs will be flipped for use with a perspective
	 *            where 0,0 is the upper left corner.
	 */
	public BitmapFont(FileHandle fontFile, FileHandle imageFile, boolean flip) {
		texture = Gdx.graphics.newTexture(imageFile, TextureFilter.Linear,
				TextureFilter.Linear, TextureWrap.ClampToEdge,
				TextureWrap.ClampToEdge);
		float invTexWidth = 1.0f / texture.getWidth();
		float invTexHeight = 1.0f / texture.getHeight();

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				fontFile.getInputStream()), 512);
		try {
			reader.readLine(); // info

			String[] common = reader.readLine().split(" ", 4);
			if (common.length < 4)
				throw new GdxRuntimeException("Invalid font file: " + fontFile);

			if (!common[1].startsWith("lineHeight="))
				throw new GdxRuntimeException("Invalid font file: " + fontFile);
			lineHeight = Integer.parseInt(common[1].substring(11));

			if (!common[2].startsWith("base="))
				throw new GdxRuntimeException("Invalid font file: " + fontFile);
			baseLine = Integer.parseInt(common[2].substring(5));

			reader.readLine(); // page
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (line.startsWith("kernings "))
					break;
				if (!line.startsWith("char "))
					continue;

				Glyph glyph = new Glyph();

				StringTokenizer tokens = new StringTokenizer(line, " =");
				tokens.nextToken();
				tokens.nextToken();
				int ch = Integer.parseInt(tokens.nextToken());
				if (ch <= Character.MAX_VALUE) {
					Glyph[] page = glyphs[ch / PAGE_SIZE];
					if (page == null)
						glyphs[ch / PAGE_SIZE] = page = new Glyph[PAGE_SIZE];
					page[ch & (PAGE_SIZE - 1)] = glyph;
				} else
					continue;
				tokens.nextToken();
				float srcX = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				float srcY = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.width = (Integer.parseInt(tokens.nextToken()));
				tokens.nextToken();
				glyph.height = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.xoffset = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				if (flip)
					glyph.yoffset = Integer.parseInt(tokens.nextToken());
				else
					glyph.yoffset = -(glyph.height + Integer.parseInt(tokens
							.nextToken()));
				tokens.nextToken();
				glyph.xadvance = Integer.parseInt(tokens.nextToken());

				glyph.u = srcX * invTexWidth;
				glyph.u2 = (srcX + glyph.width) * invTexWidth;
				if (flip) {
					glyph.v = srcY * invTexHeight;
					glyph.v2 = (srcY + glyph.height) * invTexHeight;
				} else {
					glyph.v2 = srcY * invTexHeight;
					glyph.v = (srcY + glyph.height) * invTexHeight;
				}
			}

			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (!line.startsWith("kerning "))
					break;

				StringTokenizer tokens = new StringTokenizer(line, " =");
				tokens.nextToken();
				tokens.nextToken();
				int first = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				int second = Integer.parseInt(tokens.nextToken());
				if (first < 0 || first > Character.MAX_VALUE || second < 0
						|| second > Character.MAX_VALUE)
					continue;
				Glyph glyph = getGlyph((char) first);
				tokens.nextToken();
				int amount = Integer.parseInt(tokens.nextToken());
				glyph.setKerning(second, amount);
			}

			Glyph g = getGlyph(' ');
			spaceWidth = (g != null) ? g.xadvance + g.width : 1;

			g = getGlyph('x');
			xHeight = g != null ? g.height : 1;

			g = getGlyph('M');
			capHeight = g != null ? g.height : 1;

			yOffset = flip ? -baseLine : baseLine;
			down = flip ? lineHeight : -lineHeight;
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error loading font file: "
					+ fontFile, ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

	Glyph getGlyph(char ch) {
		Glyph[] page = glyphs[ch / PAGE_SIZE];
		if (page != null)
			return page[ch & (PAGE_SIZE - 1)];
		return null;
	}

	/**
	 * Draws the given string at the given position with the given color. You
	 * can only call this between {@link SpriteBatch#begin()}/
	 * {@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character
	 * @param y
	 *            The y position of the left most character's top left corner
	 * @param color
	 *            The color
	 * @return the width of the rendered string
	 */
	public int draw(SpriteBatch spriteBatch, CharSequence str, int x, int y,
			Color color) {
		return draw(spriteBatch, str, x, y, color, 0, str.length());
	}

	/**
	 * Draws the given string at the given position with the given color. You
	 * can only call this between {@link SpriteBatch#begin()}/
	 * {@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character
	 * @param y
	 *            The y position of the left most character's top left corner
	 * @param tint
	 *            The color
	 * @param start
	 *            the first character of the string to draw
	 * @param end
	 *            the last character of the string to draw
	 * @return the width of the rendered string
	 */
	public int draw(SpriteBatch spriteBatch, CharSequence str, int x, int y,
			Color tint, int start, int end) {
		final float color = tint.toFloatBits();
		y += yOffset;
		int startX = x;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				spriteBatch.draw(texture, x + lastGlyph.xoffset, y
						+ lastGlyph.yoffset, lastGlyph.width, lastGlyph.height,
						lastGlyph.u, lastGlyph.v, lastGlyph.u2, lastGlyph.v2,
						color);
				x += lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = getGlyph(ch);
			if (g == null)
				continue;
			x += lastGlyph.getKerning(ch);
			lastGlyph = g;
			spriteBatch
					.draw(texture, x + lastGlyph.xoffset,
							y + lastGlyph.yoffset, lastGlyph.width,
							lastGlyph.height, lastGlyph.u, lastGlyph.v,
							lastGlyph.u2, lastGlyph.v2, color);
			x += g.xadvance;
		}
		return x - startX;
	}

	/**
	 * Draws the given string at the given position with the given color. The
	 * position coincides with the top left corner of the first line's glyph.
	 * This method interprets new lines. You can only call this between
	 * {@link SpriteBatch#begin()}/ {@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character of the first line
	 * @param y
	 *            The y position of the left most character's top left corner of
	 *            the first line
	 * @param color
	 *            The color
	 * @return The height of the rendered string
	 */
	public int drawMultiLineText(SpriteBatch spriteBatch, CharSequence str,
			int x, int y, Color color) {
		return drawMultiLineText(spriteBatch, str, x, y, color, 0,
				HAlignment.LEFT);
	}

	/**
	 * Draws the given string at the given position with the given color. The
	 * position coincides with the top left corner of the first line's glyph.
	 * The method interprets new lines. You can only call this between
	 * {@link SpriteBatch#begin()}/ {@link SpriteBatch#end()}. <br>
	 * <br>
	 * You can specify the horizontal alignment of the text with the
	 * <code>alignmentWidth</code> and <code>alignment</code> parameters. The
	 * first parameter specifies the width of the rectangle the text should be
	 * aligned in (x to x + alignmentWidth). The second parameter specifies the
	 * alignment itself.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character of the first line
	 * @param y
	 *            The y position of the left most character's top left corner of
	 *            the first line
	 * @param color
	 *            The color
	 * @param alignmentWidth
	 *            The alignment width
	 * @param alignment
	 *            The horizontal alignment
	 * @return The height of the multiline text
	 */
	public int drawMultiLineText(SpriteBatch spriteBatch, CharSequence str,
			int x, int y, Color color, int alignmentWidth, HAlignment alignment) {
		int down = this.down;
		int start = 0;
		int numLines = 0;
		int length = str.length();
		while (start < length) {
			int lineEnd = indexOf(str, '\n', start);
			int xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER)
					xOffset /= 2;
			}
			draw(spriteBatch, str, x + xOffset, y, color, start, lineEnd);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		return numLines * lineHeight;
	}

	/**
	 * Draws the given string at the given position with the given color. The
	 * position coincides with the top left corner of the first line's glyph.
	 * This method interprets new lines and causes the text to wrap at spaces
	 * based on the given <code>wrapWidth</code>. You can only call this between
	 * {@link SpriteBatch#begin()}/ {@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character of the first line
	 * @param y
	 *            The y position of the left most character's top left corner of
	 *            the first line
	 * @param color
	 *            The color
	 * @param wrapWidth
	 *            The wrap width
	 * @return the height of the rendered string
	 */
	public int drawWrappedText(SpriteBatch spriteBatch, CharSequence str,
			int x, int y, Color color, int wrapWidth) {
		return drawWrappedText(spriteBatch, str, x, y, color, wrapWidth,
				HAlignment.LEFT);
	}

	/**
	 * Draws the given string at the given position with the given color. The
	 * position coincides with the top left corner of the first line's glyph.
	 * This method interprets new lines and causes the text to wrap at spaces
	 * based on the given <code>wrapWidth</code>. You can only call this between
	 * {@link SpriteBatch#begin()}/ {@link SpriteBatch#end()}.<br>
	 * <br>
	 * You can specify the horizontal alignment of the text within the
	 * <code>wrapWidth</code> by using the <code>alignment</code> parameter.
	 * 
	 * @param spriteBatch
	 *            The {@link SpriteBatch} to use
	 * @param str
	 *            The string
	 * @param x
	 *            The x position of the left most character of the first line
	 * @param y
	 *            The y position of the left most character's top left corner of
	 *            the first line
	 * @param color
	 *            The color
	 * @param wrapWidth
	 *            The wrap width
	 * @return the height of the rendered string
	 */
	public int drawWrappedText(SpriteBatch spriteBatch, CharSequence str,
			int x, int y, Color color, int wrapWidth, HAlignment alignment) {
		int down = this.down;
		int start = 0;
		int numLines = 0;
		int length = str.length();
		while (start < length) {
			int lineEnd = start
					+ computeVisibleGlpyhs(str, start,
							indexOf(str, '\n', start), wrapWidth);
			if (lineEnd < length) {
				while (lineEnd > start) {
					char ch = str.charAt(lineEnd);
					if (ch == ' ' || ch == '\n')
						break;
					lineEnd--;
				}
			}
			if (lineEnd == start)
				lineEnd++;
			int xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xOffset = wrapWidth - lineWidth;
				if (alignment == HAlignment.CENTER)
					xOffset /= 2;
			}
			draw(spriteBatch, str, x + xOffset, y, color, start, lineEnd);
			start = lineEnd + 1;
			y += down;
			numLines++;
		}
		return numLines * lineHeight;
	}

	/**
	 * Computes the width of the string.
	 * 
	 * @param str
	 *            The string
	 * @return the width
	 */
	public int computeTextWidth(CharSequence str) {
		return computeTextWidth(str, 0, str.length());
	}

	/**
	 * Computes the width of the string.
	 * 
	 * @param str
	 *            the string
	 * @param start
	 *            The first character index
	 * @param end
	 *            The last character index (exclusive)
	 * @return The string width
	 */
	public int computeTextWidth(CharSequence str, int start, int end) {
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
		return width;
	}

	/**
	 * Returns the number of characters that can be rendered given the available
	 * width.
	 * 
	 * @param str
	 *            The string
	 * @param start
	 *            The start index of the first character
	 * @param end
	 *            The index of the last character (exclusive)
	 * @param availableWidth
	 *            the available width
	 * @return The number of characters that fit into availableWdith
	 */
	public int computeVisibleGlpyhs(CharSequence str, int start, int end,
			int availableWidth) {
		int index = start;
		int width = 0;
		Glyph lastGlyph = null;
		for (; index < end; index++) {
			char ch = str.charAt(index);
			Glyph g = getGlyph(ch);
			if (g != null) {
				if (lastGlyph != null)
					width += lastGlyph.getKerning(ch);
				lastGlyph = g;
				if (width + g.width + g.xoffset > availableWidth)
					break;
				width += g.xadvance;
			}
		}
		return index - start;
	}

	/**
	 * Computes the maximum width of the string, respecting newlines.
	 * 
	 * @param str
	 *            The string
	 * @return The maximum width
	 */
	public int computeMultiLineTextWidth(CharSequence str) {
		int start = 0;
		int width = 0;
		int length = str.length();
		while (start < length) {
			int lineEnd = indexOf(str, '\n', start);
			int lineWidth = computeTextWidth(str, start, lineEnd);
			width = Math.max(width, lineWidth);
			start = lineEnd + 1;
		}
		return width;
	}

	/**
	 * @return The glyph texture
	 */
	public Texture getTexture() {
		return texture;
	}

	/**
	 * @return The baseline offset, which is the distance from the drawing
	 *         position to the line that most glyphs sit on
	 */
	public int getBaseLine() {
		return baseLine;
	}

	/**
	 * @return The line height, which is the distance from one line of text to
	 *         the next
	 */
	public int getLineHeight() {
		return lineHeight;
	}

	/**
	 * @return The width of the space character
	 */
	public int getSpaceWidth() {
		return spaceWidth;
	}

	/**
	 * @return The x-height, which is the typical height of lowercase characters
	 */
	public int getXHeight() {
		return xHeight;
	}

	/**
	 * @return The cap height, which is the typical height of uppercase
	 *         characters
	 */
	public int getCapHeight() {
		return capHeight;
	}

	/**
	 * Frees all resources of this font.
	 */
	public void dispose() {
		texture.dispose();
	}

	static class Glyph {
		int width, height;
		float u, v, u2, v2;
		int xoffset, yoffset;
		int xadvance;
		byte[][] kerning;

		int getKerning(char ch) {
			if (kerning != null) {
				byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
				if (page != null)
					return page[ch & (PAGE_SIZE - 1)];
			}
			return 0;
		}

		void setKerning(int ch, int value) {
			if (kerning == null)
				kerning = new byte[PAGES][];
			byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
			if (page == null)
				kerning[ch >>> LOG2_PAGE_SIZE] = page = new byte[PAGE_SIZE];
			page[ch & (PAGE_SIZE - 1)] = (byte) value;
		}
	}

	static int indexOf(CharSequence text, char ch, int start) {
		final int n = text.length();
		for (; start < n; start++)
			if (text.charAt(start) == ch)
				return start;
		return n;
	}

	static public enum HAlignment {
		LEFT, CENTER, RIGHT
	}
}
