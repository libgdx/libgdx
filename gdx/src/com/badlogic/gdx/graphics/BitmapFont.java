
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

	public int draw (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color) {
		return draw(spriteBatch, str, x, y, color, 0, str.length());
	}

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

	public int drawMultiLineText (SpriteBatch spriteBatch, CharSequence str, int x, int y, Color color) {
		return drawMultiLineText(spriteBatch, str, x, y, color, 0, HAlignment.LEFT);
	}

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
		int startX = x;
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

	public BitmapFontCache cacheText (BitmapFontCache cache, CharSequence str, int x, int y, Color color) {
		return cacheText(cache, str, x, y, color, 0, str.length());
	}

	public BitmapFontCache cacheText (BitmapFontCache cache, CharSequence str, int x, int y, Color color, int start, int end) {
		if (cache == null)
			cache = new BitmapFontCache(texture, end - start);
		else
			cache.reset(end - start);
		y += capHeight;
		addToCache(cache, str, x, y, color, start, end);
		cache.width = x;
		cache.height = lineHeight;
		return cache;
	}

	public BitmapFontCache cacheMultiLineText (BitmapFontCache cache, CharSequence str, int x, int y, Color color) {
		return cacheMultiLineText(cache, str, x, y, color, 0, HAlignment.LEFT);
	}

	public BitmapFontCache cacheMultiLineText (BitmapFontCache cache, CharSequence str, int x, int y, Color color,
		int alignmentWidth, HAlignment alignment) {
		int length = str.length();
		if (cache == null)
			cache = new BitmapFontCache(texture, length);
		else
			cache.reset(length);
		int startY = y;
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

	public int computeTextWidth (CharSequence str) {
		return computeTextWidth(str, 0, str.length());
	}

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

	public Texture getTexture () {
		return texture;
	}

	public int getBaseLine () {
		return baseLine;
	}

	public int getLineHeight () {
		return lineHeight;
	}

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
