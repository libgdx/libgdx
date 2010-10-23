
package com.badlogic.gdx.graphics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.GdxRuntimeException;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

/**
 * <p>A BitmapFont is used to load and render AngleCode bitmap font files. The AngleCode
 * bitmap font consists of 2 files, the .fnt file which must be saved with text encoding
 * (not xml or binary!) and the bitmap file holding the glyphs, usually in .png format.</p>
 * 
 * <p>This implementation currently only supports a single glyph page.</p>
 * 
 * <p>To draw text with this class you need to call one of the draw() methods together with
 * a {@link SpriteBatch}. The SpriteBatch must be in rendering mode, that is, {@link SpriteBatch#begin()}
 * must have been called before drawing</p>
 * 
 * <p>Additionally you can cache text in a {@link BitmapFontCache} for faster rendering of static text</p>
 * 
 * <p>A BitmapFont is managed. You need to call the {@link #dispose()} method when you no longer need it</p>
 * 
 * <p>The code is heavily based on Matthias Mann's TWL BitmapFont class. Thanks for sharing
 * Matthias :)</p>
 * 
 * @author nathan.sweet
 *
 */
public class BitmapFont {
	private static final int LOG2_PAGE_SIZE = 9;
	private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
	private static final int PAGES = 0x10000 / PAGE_SIZE;

	public final Texture texture;
	private final Glyph[][] glyphs = new Glyph[PAGES][];
	private int lineHeight;
	private int baseLine;
	private int spaceWidth;
	private int ex;
	private int capHeight;

	/** 
	 * Creates a new BitmapFont instance based on a .fnt file and
	 * an image file holding the page with glyphs. Currently only 
	 * supports single page AngleCode fonts.
	 * 
	 * @param fontFile The font file
	 * @param imageFile the image file
	 */
	public BitmapFont (FileHandle fontFile, FileHandle imageFile) {
		texture = Gdx.graphics.newTexture(imageFile, TextureFilter.Linear, TextureFilter.Linear, TextureWrap.ClampToEdge,
			TextureWrap.ClampToEdge);

		BufferedReader reader = new BufferedReader(new InputStreamReader(fontFile.getInputStream()), 512);
		try {
			reader.readLine(); // info

			String[] common = reader.readLine().split(" ", 4);
			if (common.length < 4) throw new GdxRuntimeException("Invalid font file: " + fontFile);

			if (!common[1].startsWith("lineHeight=")) throw new GdxRuntimeException("Invalid font file: " + fontFile);
			lineHeight = Integer.parseInt(common[1].substring(11));

			if (!common[2].startsWith("base=")) throw new GdxRuntimeException("Invalid font file: " + fontFile);
			baseLine = Integer.parseInt(common[2].substring(5));

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
					page[ch & (PAGE_SIZE - 1)] = glyph;
				} else
					continue;
				tokens.nextToken();
				glyph.x = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.y = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.width = (Integer.parseInt(tokens.nextToken()));
				tokens.nextToken();
				glyph.height = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.xoffset = Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.yoffset = glyph.height + Integer.parseInt(tokens.nextToken());
				tokens.nextToken();
				glyph.xadvance = Integer.parseInt(tokens.nextToken());
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
			spaceWidth = (g != null) ? g.xadvance + g.width : 1;

			Glyph gx = getGlyph('x');
			ex = gx != null ? gx.height : 1;
		} catch (Exception ex) {
			throw new GdxRuntimeException("Error loading font file: " + fontFile, ex);
		} finally {
			try {
				reader.close();
			} catch (IOException ignored) {
			}
		}
	}

	private Glyph getGlyph (char ch) {
		Glyph[] page = glyphs[ch / PAGE_SIZE];
		if (page != null) return page[ch & (PAGE_SIZE - 1)];
		return null;
	}

	/**
	 * Draws the given string at the given position with the given color. You
	 * can only call this between {@link SpriteBatch#begin()}/{@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch The {@link SpriteBatch} to use
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner.
	 * @param color The color
	 * @return the width of the rendered string
	 */
	public int draw (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color) {
		return draw(spriteBatch, str, x, y, color, 0, str.length());
	}

	/**
	 * Draws the given string at the given position with the given color. You
	 * can only call this between {@link SpriteBatch#begin()}/{@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch The {@link SpriteBatch} to use
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner.
	 * @param color The color
	 * @param start the first character of the string to draw
	 * @param the last character of the string to draw
	 * @return the width of the rendered string
	 */
	public int draw (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color, int start, int end) {
		y += capHeight;
		int startX = x;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				spriteBatch.draw(texture, x + lastGlyph.xoffset, y - lastGlyph.yoffset, lastGlyph.x, lastGlyph.y, lastGlyph.width,
					lastGlyph.height, color);
				x += lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = getGlyph(ch);
			if (g != null) {
				x += lastGlyph.getKerning(ch);
				lastGlyph = g;
				spriteBatch.draw(texture, x + lastGlyph.xoffset, y - lastGlyph.yoffset, lastGlyph.x, lastGlyph.y, lastGlyph.width,
					lastGlyph.height, color);
				x += g.xadvance;
			}
		}
		return x - startX;
	}

	/**
	 * Draws the given string at the given position with the given color. The position coincides
	 * with the top left corner of the first line's glyph. The method interprets new lines.
	 * You can only call this between {@link SpriteBatch#begin()}/{@link SpriteBatch#end()}.
	 * 
	 * @param spriteBatch The {@link SpriteBatch} to use
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param color The color
	 * @return
	 */
	public int drawMultiLineText (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color) {
		return drawMultiLineText(spriteBatch, str, x, y, color, 0, HAlignment.LEFT);
	}

	/**
	 * <p>Draws the given string at the given position with the given color. The position coincides
	 * with the top left corner of the first line's glyph. The method interprets new lines.
	 * You can only call this between {@link SpriteBatch#begin()}/{@link SpriteBatch#end()}.</p>
	 * <p>You can specify the horizontal alignment of the text with the <code>alignmentWidth</code> and <code>alignment</code>
	 * parameters. The first parameter specifies the width of the rectangle the text should be aligned in (x to x + alignmentWidth).
	 * The second parameter specifies the alignment itself.
	 * 
	 * @param spriteBatch The {@link SpriteBatch} to use
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param color The color
	 * @param alignmentWidth The alignment width
	 * @param alignment The horizontal alignment
	 * @return the height of the multiline text
	 */
	public int drawMultiLineText (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color, int alignmentWidth,
		HAlignment alignment) {
		int start = 0;
		int numLines = 0;
		while (start < str.length()) {
			int lineEnd = indexOf(str, '\n', start);
			int xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			draw(spriteBatch, str, x + xOffset, y, color, start, lineEnd);
			start = lineEnd + 1;
			y -= lineHeight;
			numLines++;
		}
		return numLines * lineHeight;
	}

	private void addToCache (BitmapFontCache cache, CharSequence str, int x, int y, Color color, int start, int end) {
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				cache.addGlyph(x + lastGlyph.xoffset, y - lastGlyph.yoffset, lastGlyph.x, lastGlyph.y, lastGlyph.width,
					lastGlyph.height, color);
				x += lastGlyph.xadvance;
				break;
			}
		}
		while (start < end) {
			char ch = str.charAt(start++);
			Glyph g = getGlyph(ch);
			if (g != null) {
				x += lastGlyph.getKerning(ch);
				lastGlyph = g;
				cache.addGlyph(x + lastGlyph.xoffset, y - lastGlyph.yoffset, lastGlyph.x, lastGlyph.y, lastGlyph.width,
					lastGlyph.height, color);
				x += g.xadvance;
			}
		}
	}

	/**
	 * Creates a new {@link BitmapFontCache} to be used with {@link #cacheText()}.
	 * @return The cache
	 */
	public BitmapFontCache newCache( )
	{
		return new BitmapFontCache( this.texture );
	}
	
	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}.
	 * 
	 * @param cache The cache
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner.
	 * @param color The color
	 */
	public void cacheText (BitmapFontCache cache, CharSequence str, int x, int y, Color color) {
		cacheText(cache, str, x, y, color, 0, str.length());
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}.
	 * 
	 * @param cache The cache
	 * @param str The string
	 * @param x The x position of the left most character
	 * @param y The y position of the left most character's top left corner.
	 * @param color The color
	 * @param start the first character of the string to draw
	 * @param the last character of the string to draw
	 */
	public void cacheText (BitmapFontCache cache, CharSequence str, int x, int y, Color color, int start, int end) {
		cache.reset(end - start);
		y += capHeight;
		addToCache(cache, str, x, y, color, start, end);
		cache.width = x;
		cache.height = lineHeight;
	}

	/**
	 * Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}.
	 * The position coincides with the top left corner of the first line's glyph. The method interprets new lines.
	 * 
	 * @param spriteBatch The {@link SpriteBatch} to use
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param color The color
	 */
	public void cacheMultiLineText (BitmapFontCache cache, CharSequence str, int x, int y, Color color) {
		cacheMultiLineText(cache, str, x, y, color, 0, HAlignment.LEFT);
	}

	/**
	 * <p>Caches the given string at the given position with the given color in the provided {@link BitmapFontCache}. The position coincides
	 * with the top left corner of the first line's glyph. The method interprets new lines.</p>
	 * <p>You can specify the horizontal alignment of the text with the <code>alignmentWidth</code> and <code>alignment</code>
	 * parameters. The first parameter specifies the width of the rectangle the text should be aligned in (x to x + alignmentWidth).
	 * The second parameter specifies the alignment itself.
	 * 
	 * @param cache The cache
	 * @param str The string
	 * @param x The x position of the left most character of the first line
	 * @param y The y position of the left most character's top left corner of the first line
	 * @param color The color
	 * @param alignmentWidth The alignment width
	 * @param alignment The horizontal alignment
	 */
	public BitmapFontCache cacheMultiLineText (BitmapFontCache cache, CharSequence str, int x, int y, Color color,
		int alignmentWidth, HAlignment alignment) {
		int length = str.length();
		cache.reset(length);
		int start = 0;
		int numLines = 0;
		while (start < length) {
			int lineEnd = indexOf(str, '\n', start);
			int xOffset = 0;
			if (alignment != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xOffset = alignmentWidth - lineWidth;
				if (alignment == HAlignment.CENTER) xOffset /= 2;
			}
			addToCache(cache, str, x + xOffset, y, color, start, lineEnd);
			start = lineEnd + 1;
			y -= lineHeight;
			numLines++;
		}
		cache.width = alignmentWidth;
		cache.height = start - y;
		return cache;
	}

	/**
	 * Computes the strings width 
	 * @param str The string 
	 * @return the width
	 */
	public int computeTextWidth (CharSequence str) {
		return computeTextWidth(str, 0, str.length());
	}

	/**
	 * Computes the string with
	 * @param str the string 
	 * @param start the first character index
	 * @param end the last character index (exclusive)
	 * @return the string width
	 */
	public int computeTextWidth (CharSequence str, int start, int end) {
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
	 * Returns the number of characters that can be rendered given the available width.
	 * @param str The string
	 * @param start the start index of the first character
	 * @param end the index of the last character exclusive
	 * @param availableWidth the available width
	 * @return the number of characters that fit into availableWdith
	 */
	public int computeVisibleGlpyhs (CharSequence str, int start, int end, int availableWidth) {
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

	/**
	 * Computes the maximum width of the multiline string
	 * @param str the string
	 * @return the maximum width
	 */
	public int computeMultiLineTextWidth (CharSequence str) {
		int start = 0;
		int width = 0;
		while (start < str.length()) {
			int lineEnd = indexOf(str, '\n', start);
			int lineWidth = computeTextWidth(str, start, lineEnd);
			width = Math.max(width, lineWidth);
			start = lineEnd + 1;
		}
		return width;
	}

	/**
	 * @return the glyph texture
	 */
	public Texture getTexture () {
		return texture;
	}

	/**
	 * @return the base line offset
	 */
	public int getBaseLine () {
		return baseLine;
	}

	/**
	 * @return the line height
	 */
	public int getLineHeight () {
		return lineHeight;
	}

	/**
	 * @return the width of the space character
	 */
	public int getSpaceWidth () {
		return spaceWidth;
	}

	public int getEM () {
		return lineHeight;
	}

	public int getEX () {
		return ex;
	}

	public int getCapHeight () {
		return capHeight;
	}

	/**
	 * Frees all resources of this font. 
	 */
	public void dispose () {
		texture.dispose();
	}

	static class Glyph {
		int x, y;
		int width, height;
		int xoffset, yoffset;
		int xadvance;
		byte[][] kerning;

		int getKerning (char ch) {
			if (kerning != null) {
				byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
				if (page != null) return page[ch & (PAGE_SIZE - 1)];
			}
			return 0;
		}

		void setKerning (int ch, int value) {
			if (kerning == null) kerning = new byte[PAGES][];
			byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
			if (page == null) kerning[ch >>> LOG2_PAGE_SIZE] = page = new byte[PAGE_SIZE];
			page[ch & (PAGE_SIZE - 1)] = (byte)value;
		}
	}

	static private int indexOf (CharSequence text, char ch, int start) {
		final int n = text.length();
		for (; start < n; start++)
			if (text.charAt(start) == ch) return start;
		return n;
	}

	static public enum HAlignment {
		LEFT, CENTER, RIGHT
	}
}
