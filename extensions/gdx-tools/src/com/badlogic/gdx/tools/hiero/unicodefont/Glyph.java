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

import java.awt.*;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;

import com.badlogic.gdx.graphics.Texture;

/** Represents the glyph in a font for a unicode codepoint.
 * @author Nathan Sweet */
public class Glyph {
	private int codePoint;
	private short width, height;
	private short yOffset;
	private boolean isMissing;
	private Shape shape;
	private float u, v, u2, v2;
	private Texture texture;

	Glyph (int codePoint, Rectangle bounds, GlyphVector vector, int index, UnicodeFont unicodeFont) {
		this.codePoint = codePoint;

		GlyphMetrics metrics = vector.getGlyphMetrics(index);
		int lsb = (int)metrics.getLSB();
		if (lsb > 0) lsb = 0;
		int rsb = (int)metrics.getRSB();
		if (rsb > 0) rsb = 0;

		int glyphWidth = bounds.width - lsb - rsb;
		int glyphHeight = bounds.height;
		if (glyphWidth > 0 && glyphHeight > 0) {
			int padTop = unicodeFont.getPaddingTop();
			int padRight = unicodeFont.getPaddingRight();
			int padBottom = unicodeFont.getPaddingBottom();
			int padLeft = unicodeFont.getPaddingLeft();
			int glyphSpacing = 1; // Needed to prevent filtering problems.
			width = (short)(glyphWidth + padLeft + padRight + glyphSpacing);
			height = (short)(glyphHeight + padTop + padBottom + glyphSpacing);
			yOffset = (short)(unicodeFont.getAscent() + bounds.y - padTop);
		}

		shape = vector.getGlyphOutline(index, -bounds.x + unicodeFont.getPaddingLeft(), -bounds.y + unicodeFont.getPaddingTop());

		isMissing = !unicodeFont.getFont().canDisplay((char)codePoint);
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
}
