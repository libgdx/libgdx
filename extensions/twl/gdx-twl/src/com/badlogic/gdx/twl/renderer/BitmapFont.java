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

package com.badlogic.gdx.twl.renderer;

import java.io.IOException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.matthiasmann.twl.HAlignment;
import de.matthiasmann.twl.renderer.FontCache;
import de.matthiasmann.twl.utils.TextUtil;
import de.matthiasmann.twl.utils.XMLParser;

/**
 * A Bitmap Font class. Parses the output of AngelCode's BMFont tool.
 * 
 * @author Matthias Mann
 */
class BitmapFont {
	private static final int LOG2_PAGE_SIZE = 9;
	private static final int PAGE_SIZE = 1 << LOG2_PAGE_SIZE;
	private static final int PAGES = 0x10000 / PAGE_SIZE;

	static class Glyph extends TextureAreaBase {
		short xoffset;
		short yoffset;
		short xadvance;
		byte[][] kerning;

		public Glyph (GdxRenderer renderer, int x, int y, int width, int height, int texWidth, int texHeight) {
			super(renderer, x, y, width, height, texWidth, texHeight);
		}

		void draw (int x, int y) {
			// BOZO - This is terrible. See drawText below.
			drawQuad(x + xoffset, y + yoffset, width, height);
		}

		int getKerning (char ch) {
			if (kerning != null) {
				byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
				if (page != null) {
					return page[ch & (PAGE_SIZE - 1)];
				}
			}
			return 0;
		}

		void setKerning (int ch, int value) {
			if (kerning == null) {
				kerning = new byte[PAGES][];
			}
			byte[] page = kerning[ch >>> LOG2_PAGE_SIZE];
			if (page == null) {
				kerning[ch >>> LOG2_PAGE_SIZE] = page = new byte[PAGE_SIZE];
			}
			page[ch & (PAGE_SIZE - 1)] = (byte)value;
		}
	}

	final GdxTexture texture;
	private final Glyph[][] glyphs;
	private final int lineHeight;
	private final int baseLine;
	private final int spaceWidth;
	private final int ex;
	private float[] temp = new float[1024];
	private int tempIndex = 0;
	GdxFont font;

	public BitmapFont (GdxRenderer renderer, XMLParser xmlp, URL baseUrl) throws XmlPullParserException, IOException {
		xmlp.require(XmlPullParser.START_TAG, null, "font");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.START_TAG, null, "info");
		xmlp.ignoreOtherAttributes();
		xmlp.nextTag();
		xmlp.require(XmlPullParser.END_TAG, null, "info");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.START_TAG, null, "common");
		lineHeight = xmlp.parseIntFromAttribute("lineHeight");
		baseLine = xmlp.parseIntFromAttribute("base");
		if (xmlp.parseIntFromAttribute("pages", 1) != 1) {
			throw new UnsupportedOperationException("multi page fonts not supported");
		}
		if (xmlp.parseIntFromAttribute("packed", 0) != 0) {
			throw new UnsupportedOperationException("packed fonts not supported");
		}
		xmlp.ignoreOtherAttributes();
		xmlp.nextTag();
		xmlp.require(XmlPullParser.END_TAG, null, "common");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.START_TAG, null, "pages");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.START_TAG, null, "page");
		int pageId = Integer.parseInt(xmlp.getAttributeValue(null, "id"));
		if (pageId != 0) {
			throw new UnsupportedOperationException("only page id 0 supported");
		}
		String textureName = xmlp.getAttributeValue(null, "file");
		this.texture = renderer.load(new URL(baseUrl, textureName));
		xmlp.nextTag();
		xmlp.require(XmlPullParser.END_TAG, null, "page");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.END_TAG, null, "pages");
		xmlp.nextTag();
		xmlp.require(XmlPullParser.START_TAG, null, "chars");
		xmlp.ignoreOtherAttributes();
		xmlp.nextTag();

		glyphs = new Glyph[PAGES][];
		while (!xmlp.isEndTag()) {
			xmlp.require(XmlPullParser.START_TAG, null, "char");
			int idx = xmlp.parseIntFromAttribute("id");
			int x = xmlp.parseIntFromAttribute("x");
			int y = xmlp.parseIntFromAttribute("y");
			int w = xmlp.parseIntFromAttribute("width");
			int h = xmlp.parseIntFromAttribute("height");
			if (xmlp.parseIntFromAttribute("page", 0) != 0) {
				throw xmlp.error("Multiple pages not supported");
			}
			int chnl = xmlp.parseIntFromAttribute("chnl", 0);
			Glyph g = new Glyph(renderer, x, y, w, h, texture.getWidth(), texture.getHeight());
			g.xoffset = Short.parseShort(xmlp.getAttributeNotNull("xoffset"));
			g.yoffset = Short.parseShort(xmlp.getAttributeNotNull("yoffset"));
			g.xadvance = Short.parseShort(xmlp.getAttributeNotNull("xadvance"));
			if (idx <= Character.MAX_VALUE) {
				Glyph[] page = glyphs[idx / PAGE_SIZE];
				if (page == null) {
					glyphs[idx / PAGE_SIZE] = page = new Glyph[PAGE_SIZE];
				}
				page[idx & (PAGE_SIZE - 1)] = g;
			}
			xmlp.nextTag();
			xmlp.require(XmlPullParser.END_TAG, null, "char");
			xmlp.nextTag();
		}

		xmlp.require(XmlPullParser.END_TAG, null, "chars");
		xmlp.nextTag();
		if (xmlp.isStartTag()) {
			xmlp.require(XmlPullParser.START_TAG, null, "kernings");
			xmlp.ignoreOtherAttributes();
			xmlp.nextTag();
			while (!xmlp.isEndTag()) {
				xmlp.require(XmlPullParser.START_TAG, null, "kerning");
				int first = xmlp.parseIntFromAttribute("first");
				int second = xmlp.parseIntFromAttribute("second");
				int amount = xmlp.parseIntFromAttribute("amount");
				if (first >= 0 && first <= Character.MAX_VALUE && second >= 0 && second <= Character.MAX_VALUE) {
					Glyph g = getGlyph((char)first);
					if (g != null) {
						g.setKerning(second, amount);
					}
				}
				xmlp.nextTag();
				xmlp.require(XmlPullParser.END_TAG, null, "kerning");
				xmlp.nextTag();
			}
			xmlp.require(XmlPullParser.END_TAG, null, "kernings");
			xmlp.nextTag();
		}
		xmlp.require(XmlPullParser.END_TAG, null, "font");

		Glyph g = getGlyph(' ');
		spaceWidth = (g != null) ? g.xadvance + g.width : 1;

		Glyph gx = getGlyph('x');
		ex = (gx != null) ? gx.height : 1;
	}

	public static BitmapFont loadFont (GdxRenderer renderer, URL url) throws IOException {
		try {
			XMLParser xmlp = new XMLParser(url);
			try {
				xmlp.require(XmlPullParser.START_DOCUMENT, null, null);
				xmlp.nextTag();
				return new BitmapFont(renderer, xmlp, url);
			} finally {
				xmlp.close();
			}
		} catch (XmlPullParserException ex) {
			throw (IOException)(new IOException().initCause(ex));
		}
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

	public void destroy () {
		texture.destroy();
	}

	private Glyph getGlyph (char ch) {
		Glyph[] page = glyphs[ch / PAGE_SIZE];
		if (page != null) {
			return page[ch & (PAGE_SIZE - 1)];
		}
		return null;
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

	public int computeVisibleGlpyhs (CharSequence str, int start, int end, int availWidth) {
		int index = start;
		int width = 0;
		Glyph lastGlyph = null;
		for (; index < end; index++) {
			char ch = str.charAt(index);
			Glyph g = getGlyph(ch);
			if (g != null) {
				if (lastGlyph != null) {
					width += lastGlyph.getKerning(ch);
				}
				lastGlyph = g;
				if (width + g.width + g.xoffset > availWidth) {
					break;
				}
				width += g.xadvance;
			}
		}
		return index - start;
	}

	protected int drawText (int x, int y, CharSequence str, int start, int end) {
		// BOZO - This is terrible. Cached text is used most of the time, but this still should be fixed.
		// Maybe with same solution as caching? Eg:
		// cacheText(font, cache, str, start, end).draw(...);
		
		int startX = x;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				lastGlyph.draw(x, y);
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
				g.draw(x, y);
				x += g.xadvance;
			}
		}
		return x - startX;
	}

	protected int drawMultiLineText (int x, int y, CharSequence str, int width, HAlignment align) {
		int start = 0;
		int numLines = 0;
		while (start < str.length()) {
			int lineEnd = TextUtil.indexOf(str, '\n', start);
			int xoff = 0;
			if (align != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xoff = width - lineWidth;
				if (align == HAlignment.CENTER) {
					xoff /= 2;
				}
			}
			drawText(x + xoff, y, str, start, lineEnd);
			start = lineEnd + 1;
			y += lineHeight;
			numLines++;
		}
		return numLines;
	}

	public void computeMultiLineInfo (CharSequence str, int width, HAlignment align, int[] multiLineInfo) {
		int start = 0;
		int idx = 0;
		while (start < str.length()) {
			int lineEnd = TextUtil.indexOf(str, '\n', start);
			int lineWidth = computeTextWidth(str, start, lineEnd);
			int xoff = width - lineWidth;
			if (align == HAlignment.LEFT) {
				xoff = 0;
			} else if (align == HAlignment.CENTER) {
				xoff /= 2;
			}
			multiLineInfo[idx++] = (lineWidth << 16) | (xoff & 0xFFFF);
			start = lineEnd + 1;
		}
	}

	public int computeMultiLineTextWidth (CharSequence str) {
		int start = 0;
		int width = 0;
		while (start < str.length()) {
			int lineEnd = TextUtil.indexOf(str, '\n', start);
			int lineWidth = computeTextWidth(str, start, lineEnd);
			width = Math.max(width, lineWidth);
			start = lineEnd + 1;
		}
		return width;
	}

	public FontCache cacheMultiLineText (GdxFont font, FontCache c, CharSequence str, int width, HAlignment align) {
		// System.out.println("multi line: " + str + "?");
		int length = str.length();
		GdxFontCache cache = (GdxFontCache)c;
		if (cache == null)
			cache = new GdxFontCache(font, length * 6);
		else
			cache.ensureCapacity(length * 6);
		if (length * 24 > temp.length) temp = new float[length * 24];
		tempIndex = 0;
		int start = 0, y = 0;
		int numLines = 0;
		while (start < str.length()) {
			int lineEnd = TextUtil.indexOf(str, '\n', start);
			int xoff = 0;
			if (align != HAlignment.LEFT) {
				int lineWidth = computeTextWidth(str, start, lineEnd);
				xoff = width - lineWidth;
				if (align == HAlignment.CENTER) {
					xoff /= 2;
				}
			}
			addToTemp(cache, str, start, lineEnd);
			start = lineEnd + 1;
			y += lineHeight;
			numLines++;
		}
		cache.width = width;
		cache.height = y;
		cache.mesh.setVertices(temp, 0, tempIndex);
		return cache;
	}

	public FontCache cacheText (GdxFont font, FontCache c, CharSequence str, int start, int end) {
		// System.out.println("single line: " + str.subSequence(start, end) + "?");
		int length = end - start;
		GdxFontCache cache = (GdxFontCache)c;
		if (cache == null)
			cache = new GdxFontCache(font, length * 6);
		else
			cache.ensureCapacity(length * 6);
		if (length * 24 > temp.length) temp = new float[length * 24];
		tempIndex = 0;
		cache.width = addToTemp(cache, str, start, end);
		cache.height = lineHeight;
		cache.mesh.setVertices(temp, 0, tempIndex);
		return cache;
	}

	private int addToTemp (GdxFontCache cache, CharSequence str, int start, int end) {
		int startX = 0, x = 0;
		Glyph lastGlyph = null;
		while (start < end) {
			lastGlyph = getGlyph(str.charAt(start++));
			if (lastGlyph != null) {
				int left = x + lastGlyph.xoffset;
				int top = lastGlyph.yoffset;
				int right = left + lastGlyph.width;
				int bottom = top + lastGlyph.height;
				temp[tempIndex++] = left;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = left;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = right;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = left;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty1;

				temp[tempIndex++] = right;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty1;

				temp[tempIndex++] = right;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty1;
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
				int left = x + lastGlyph.xoffset;
				int top = lastGlyph.yoffset;
				int right = left + lastGlyph.width;
				int bottom = top + lastGlyph.height;
				temp[tempIndex++] = left;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = left;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = right;
				temp[tempIndex++] = top;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty0;

				temp[tempIndex++] = left;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx0;
				temp[tempIndex++] = lastGlyph.ty1;

				temp[tempIndex++] = right;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty1;

				temp[tempIndex++] = right;
				temp[tempIndex++] = bottom;
				temp[tempIndex++] = lastGlyph.tx1;
				temp[tempIndex++] = lastGlyph.ty1;
				x += g.xadvance;
			}
		}
		return x;
	}

	protected boolean prepare () {
		return texture.bind();
	}

	protected void cleanup () {
	}
}
