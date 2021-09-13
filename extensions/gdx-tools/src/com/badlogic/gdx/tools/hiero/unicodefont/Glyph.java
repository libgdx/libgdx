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

package com.badlogic.gdx.tools.hiero.unicodefont;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont.RenderType;

/** Represents the glyph in a font for a unicode codepoint.
 * @author Nathan Sweet */
public class Glyph {
	private int codePoint;
	private short width, height;
	private short yOffset;
	private boolean isMissing;
	private Shape shape;
	float u, v, u2, v2;
	private int xOffset, xAdvance;
	Texture texture;

	Glyph (int codePoint, Rectangle bounds, GlyphVector vector, int index, UnicodeFont unicodeFont) {
		this.codePoint = codePoint;

		int padTop = unicodeFont.getPaddingTop(), padBottom = unicodeFont.getPaddingBottom();
		int padLeft = unicodeFont.getPaddingLeft(), padRight = unicodeFont.getPaddingRight();

		if (unicodeFont.renderType == RenderType.FreeType && unicodeFont.bitmapFont != null) {
			BitmapFont.Glyph g = unicodeFont.bitmapFont.getData().getGlyph((char)codePoint);
			if (g == null)
				isMissing = true;
			else {
				boolean empty = g.width == 0 || g.height == 0;
				width = empty ? 0 : (short)(g.width + padLeft + padRight);
				height = empty ? 0 : (short)(g.height + padTop + padBottom);
				yOffset = (short)(g.yoffset - padTop);
				xOffset = g.xoffset - unicodeFont.getPaddingLeft();
				xAdvance = g.xadvance + unicodeFont.getPaddingAdvanceX() + unicodeFont.getPaddingLeft()
					+ unicodeFont.getPaddingRight();
				isMissing = codePoint == 0;
			}

		} else {
			GlyphMetrics metrics = vector.getGlyphMetrics(index);
			int lsb = (int)metrics.getLSB();
			if (lsb > 0) lsb = 0;
			int rsb = (int)metrics.getRSB();
			if (rsb > 0) rsb = 0;

			int glyphWidth = bounds.width - lsb - rsb;
			int glyphHeight = bounds.height;
			if (glyphWidth > 0 && glyphHeight > 0) {
				width = (short)(glyphWidth + padLeft + padRight);
				height = (short)(glyphHeight + padTop + padBottom);
				yOffset = (short)(unicodeFont.getAscent() + bounds.y - padTop);
			}

			// xOffset and xAdvance will be incorrect for unicode characters such as combining marks or non-spacing characters
			// (eg Pnujabi's "\u0A1C\u0A47") that require the context of surrounding glyphs to determine spacing, but this is the
			// best we can do with the BMFont format.
			char[] chars = Character.toChars(codePoint);
			GlyphVector charVector = unicodeFont.getFont().layoutGlyphVector(GlyphPage.renderContext, chars, 0, chars.length,
				Font.LAYOUT_LEFT_TO_RIGHT);
			GlyphMetrics charMetrics = charVector.getGlyphMetrics(0);
			xOffset = charVector.getGlyphPixelBounds(0, GlyphPage.renderContext, 0, 0).x - unicodeFont.getPaddingLeft();
			xAdvance = (int)(metrics.getAdvanceX() + unicodeFont.getPaddingAdvanceX() + unicodeFont.getPaddingLeft()
				+ unicodeFont.getPaddingRight());

			shape = vector.getGlyphOutline(index, -bounds.x + unicodeFont.getPaddingLeft(), -bounds.y + unicodeFont.getPaddingTop());

			isMissing = !unicodeFont.getFont().canDisplay((char)codePoint);
		}
	}

	/** The unicode codepoint the glyph represents. */
	public int getCodePoint () {
		return codePoint;
	}

	/** Returns true if the font does not have a glyph for this codepoint. */
	public boolean isMissing () {
		return isMissing;
	}

	/** The width of the glyph's image. */
	public int getWidth () {
		return width;
	}

	/** The height of the glyph's image. */
	public int getHeight () {
		return height;
	}

	/** The shape to use to draw this glyph. This is set to null after the glyph is stored in a GlyphPage. */
	public Shape getShape () {
		return shape;
	}

	public void setShape (Shape shape) {
		this.shape = shape;
	}

	public void setTexture (Texture texture, float u, float v, float u2, float v2) {
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.u2 = u2;
		this.v2 = v2;
	}

	public Texture getTexture () {
		return texture;
	}

	public float getU () {
		return u;
	}

	public float getV () {
		return v;
	}

	public float getU2 () {
		return u2;
	}

	public float getV2 () {
		return v2;
	}

	/** The distance from drawing y location to top of this glyph, causing the glyph to sit on the baseline. */
	public int getYOffset () {
		return yOffset;
	}

	public int getXOffset () {
		return xOffset;
	}

	public int getXAdvance () {
		return xAdvance;
	}
}
