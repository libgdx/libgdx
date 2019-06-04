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
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pools;

/** Caches glyph geometry for a BitmapFont, providing a fast way to render static text. This saves needing to compute the glyph
 * geometry each frame.
 * @author Nathan Sweet
 * @author davebaol
 * @author Alexander Dorokhov */
public class BitmapFontCache {
	static private final Color tempColor = new Color(1, 1, 1, 1);

	private final BitmapFont font;
	private boolean integer;
	private final Array<GlyphLayout> layouts = new Array();
	private final Array<GlyphLayout> pooledLayouts = new Array();
	private int glyphCount;
	private float x, y;
	private final Color color = new Color(1, 1, 1, 1);
	private float currentTint;

	/** Vertex data per page. */
	private float[][] pageVertices;
	/** Number of vertex data entries per page. */
	private int[] idx;
	/** For each page, an array with a value for each glyph from that page, where the value is the index of the character in the
	 * full text being cached. */
	private IntArray[] pageGlyphIndices;
	/** Used internally to ensure a correct capacity for multi-page font vertex data. */
	private int[] tempGlyphCount;

	public BitmapFontCache (BitmapFont font) {
		this(font, font.usesIntegerPositions());
	}

	/** @param integer If true, rendering positions will be at integer values to avoid filtering artifacts. */
	public BitmapFontCache (BitmapFont font, boolean integer) {
		this.font = font;
		this.integer = integer;

		int pageCount = font.regions.size;
		if (pageCount == 0) throw new IllegalArgumentException("The specified font must contain at least one texture page.");

		pageVertices = new float[pageCount][];
		idx = new int[pageCount];
		if (pageCount > 1) {
			// Contains the indices of the glyph in the cache as they are added.
			pageGlyphIndices = new IntArray[pageCount];
			for (int i = 0, n = pageGlyphIndices.length; i < n; i++)
				pageGlyphIndices[i] = new IntArray();
		}
		tempGlyphCount = new int[pageCount];
	}

	/** Sets the position of the text, relative to the position when the cached text was created.
	 * @param x The x coordinate
	 * @param y The y coordinate */
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

		float[][] pageVertices = this.pageVertices;
		for (int i = 0, n = pageVertices.length; i < n; i++) {
			float[] vertices = pageVertices[i];
			for (int ii = 0, nn = idx[i]; ii < nn; ii += 5) {
				vertices[ii] += xAmount;
				vertices[ii + 1] += yAmount;
			}
		}
	}

	/** Tints all text currently in the cache. Does not affect subsequently added text. */
	public void tint (Color tint) {
		float newTint = tint.toFloatBits();
		if (currentTint == newTint) return;
		currentTint = newTint;

		int[] tempGlyphCount = this.tempGlyphCount;
		for (int i = 0, n = tempGlyphCount.length; i < n; i++)
			tempGlyphCount[i] = 0;

		for (int i = 0, n = layouts.size; i < n; i++) {
			GlyphLayout layout = layouts.get(i);
			for (int ii = 0, nn = layout.runs.size; ii < nn; ii++) {
				GlyphRun run = layout.runs.get(ii);
				Array<Glyph> glyphs = run.glyphs;
				float colorFloat = tempColor.set(run.color).mul(tint).toFloatBits();
				for (int iii = 0, nnn = glyphs.size; iii < nnn; iii++) {
					Glyph glyph = glyphs.get(iii);
					int page = glyph.page;
					int offset = tempGlyphCount[page] * 20 + 2;
					tempGlyphCount[page]++;
					float[] vertices = pageVertices[page];
					for (int v = 0; v < 20; v += 5)
						vertices[offset + v] = colorFloat;
				}
			}
		}
	}

	/** Sets the alpha component of all text currently in the cache. Does not affect subsequently added text. */
	public void setAlphas (float alpha) {
		int alphaBits = ((int)(254 * alpha)) << 24;
		float prev = 0, newColor = 0;
		for (int j = 0, length = pageVertices.length; j < length; j++) {
			float[] vertices = pageVertices[j];
			for (int i = 2, n = idx[j]; i < n; i += 5) {
				float c = vertices[i];
				if (c == prev && i != 2) {
					vertices[i] = newColor;
				} else {
					prev = c;
					int rgba = NumberUtils.floatToIntColor(c);
					rgba = (rgba & 0x00FFFFFF) | alphaBits;
					newColor = NumberUtils.intToFloatColor(rgba);
					vertices[i] = newColor;
				}
			}
		}
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (float color) {
		for (int j = 0, length = pageVertices.length; j < length; j++) {
			float[] vertices = pageVertices[j];
			for (int i = 2, n = idx[j]; i < n; i += 5)
				vertices[i] = color;
		}
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (Color tint) {
		setColors(tint.toFloatBits());
	}

	/** Sets the color of all text currently in the cache. Does not affect subsequently added text. */
	public void setColors (float r, float g, float b, float a) {
		int intBits = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		setColors(NumberUtils.intToFloatColor(intBits));
	}

	/** Sets the color of the specified characters. This may only be called after {@link #setText(CharSequence, float, float)} and
	 * is reset every time setText is called. */
	public void setColors (Color tint, int start, int end) {
		setColors(tint.toFloatBits(), start, end);
	}

	/** Sets the color of the specified characters. This may only be called after {@link #setText(CharSequence, float, float)} and
	 * is reset every time setText is called. */
	public void setColors (float color, int start, int end) {
		if (pageVertices.length == 1) { // One page.
			float[] vertices = pageVertices[0];
			for (int i = start * 20 + 2, n = end * 20; i < n; i += 5)
				vertices[i] = color;
			return;
		}

		int pageCount = pageVertices.length;
		for (int i = 0; i < pageCount; i++) {
			float[] vertices = pageVertices[i];
			IntArray glyphIndices = pageGlyphIndices[i];
			// Loop through the indices and determine whether the glyph is inside begin/end.
			for (int j = 0, n = glyphIndices.size; j < n; j++) {
				int glyphIndex = glyphIndices.items[j];

				// Break early if the glyph is out of bounds.
				if (glyphIndex >= end) break;

				// If inside start and end, change its colour.
				if (glyphIndex >= start) { // && glyphIndex < end
					for (int off = 0; off < 20; off += 5)
						vertices[off + (j * 20 + 2)] = color;
				}
			}
		}
	}

	/** Returns the color used for subsequently added text. Modifying the color affects text subsequently added to the cache, but
	 * does not affect existing text currently in the cache. */
	public Color getColor () {
		return color;
	}

	/** A convenience method for setting the cache color. The color can also be set by modifying {@link #getColor()}. */
	public void setColor (Color color) {
		this.color.set(color);
	}

	/** A convenience method for setting the cache color. The color can also be set by modifying {@link #getColor()}. */
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
	}

	public void draw (Batch spriteBatch) {
		Array<TextureRegion> regions = font.getRegions();
		for (int j = 0, n = pageVertices.length; j < n; j++) {
			if (idx[j] > 0) { // ignore if this texture has no glyphs
				float[] vertices = pageVertices[j];
				spriteBatch.draw(regions.get(j).getTexture(), vertices, 0, idx[j]);
			}
		}
	}

	public void draw (Batch spriteBatch, int start, int end) {
		if (pageVertices.length == 1) { // 1 page.
			spriteBatch.draw(font.getRegion().getTexture(), pageVertices[0], start * 20, (end - start) * 20);
			return;
		}

		// Determine vertex offset and count to render for each page. Some pages might not need to be rendered at all.
		Array<TextureRegion> regions = font.getRegions();
		for (int i = 0, pageCount = pageVertices.length; i < pageCount; i++) {
			int offset = -1, count = 0;

			// For each set of glyph indices, determine where to begin within the start/end bounds.
			IntArray glyphIndices = pageGlyphIndices[i];
			for (int ii = 0, n = glyphIndices.size; ii < n; ii++) {
				int glyphIndex = glyphIndices.get(ii);

				// Break early if the glyph is out of bounds.
				if (glyphIndex >= end) break;

				// Determine if this glyph is within bounds. Use the first match of that for the offset.
				if (offset == -1 && glyphIndex >= start) offset = ii;

				// Determine the vertex count by counting glyphs within bounds.
				if (glyphIndex >= start) // && gInd < end
					count++;
			}

			// Page doesn't need to be rendered.
			if (offset == -1 || count == 0) continue;

			// Render the page vertex data with the offset and count.
			spriteBatch.draw(regions.get(i).getTexture(), pageVertices[i], offset * 20, count * 20);
		}
	}

	public void draw (Batch spriteBatch, float alphaModulation) {
		if (alphaModulation == 1) {
			draw(spriteBatch);
			return;
		}
		Color color = getColor();
		float oldAlpha = color.a;
		color.a *= alphaModulation;
		setColors(color);
		draw(spriteBatch);
		color.a = oldAlpha;
		setColors(color);
	}

	/** Removes all glyphs in the cache. */
	public void clear () {
		x = 0;
		y = 0;
		Pools.freeAll(pooledLayouts, true);
		pooledLayouts.clear();
		layouts.clear();
		for (int i = 0, n = idx.length; i < n; i++) {
			if (pageGlyphIndices != null) pageGlyphIndices[i].clear();
			idx[i] = 0;
		}
	}

	private void requireGlyphs (GlyphLayout layout) {
		if (pageVertices.length == 1) {
			// Simpler counting if we just have one page.
			int newGlyphCount = 0;
			for (int i = 0, n = layout.runs.size; i < n; i++)
				newGlyphCount += layout.runs.get(i).glyphs.size;
			requirePageGlyphs(0, newGlyphCount);
		} else {
			int[] tempGlyphCount = this.tempGlyphCount;
			for (int i = 0, n = tempGlyphCount.length; i < n; i++)
				tempGlyphCount[i] = 0;
			// Determine # of glyphs in each page.
			for (int i = 0, n = layout.runs.size; i < n; i++) {
				Array<Glyph> glyphs = layout.runs.get(i).glyphs;
				for (int ii = 0, nn = glyphs.size; ii < nn; ii++)
					tempGlyphCount[glyphs.get(ii).page]++;
			}
			// Require that many for each page.
			for (int i = 0, n = tempGlyphCount.length; i < n; i++)
				requirePageGlyphs(i, tempGlyphCount[i]);
		}
	}

	private void requirePageGlyphs (int page, int glyphCount) {
		if (pageGlyphIndices != null) {
			if (glyphCount > pageGlyphIndices[page].items.length)
				pageGlyphIndices[page].ensureCapacity(glyphCount - pageGlyphIndices[page].items.length);
		}

		int vertexCount = idx[page] + glyphCount * 20;
		float[] vertices = pageVertices[page];
		if (vertices == null) {
			pageVertices[page] = new float[vertexCount];
		} else if (vertices.length < vertexCount) {
			float[] newVertices = new float[vertexCount];
			System.arraycopy(vertices, 0, newVertices, 0, idx[page]);
			pageVertices[page] = newVertices;
		}
	}

	private void addToCache (GlyphLayout layout, float x, float y) {
		// Check if the number of font pages has changed.
		int pageCount = font.regions.size;
		if (pageVertices.length < pageCount) {
			float[][] newPageVertices = new float[pageCount][];
			System.arraycopy(pageVertices, 0, newPageVertices, 0, pageVertices.length);
			pageVertices = newPageVertices;

			int[] newIdx = new int[pageCount];
			System.arraycopy(idx, 0, newIdx, 0, idx.length);
			idx = newIdx;

			IntArray[] newPageGlyphIndices = new IntArray[pageCount];
			int pageGlyphIndicesLength = 0;
			if (pageGlyphIndices != null) {
				pageGlyphIndicesLength = pageGlyphIndices.length;
				System.arraycopy(pageGlyphIndices, 0, newPageGlyphIndices, 0, pageGlyphIndices.length);
			}
			for (int i = pageGlyphIndicesLength; i < pageCount; i++)
				newPageGlyphIndices[i] = new IntArray();
			pageGlyphIndices = newPageGlyphIndices;

			tempGlyphCount = new int[pageCount];
		}

		layouts.add(layout);
		requireGlyphs(layout);
		for (int i = 0, n = layout.runs.size; i < n; i++) {
			GlyphRun run = layout.runs.get(i);
			Array<Glyph> glyphs = run.glyphs;
			FloatArray xAdvances = run.xAdvances;
			float color = run.color.toFloatBits();
			float gx = x + run.x, gy = y + run.y;
			for (int ii = 0, nn = glyphs.size; ii < nn; ii++) {
				Glyph glyph = glyphs.get(ii);
				gx += xAdvances.get(ii);
				addGlyph(glyph, gx, gy, color);
			}
		}

		currentTint = Color.WHITE_FLOAT_BITS; // Cached glyphs have changed, reset the current tint.
	}

	private void addGlyph (Glyph glyph, float x, float y, float color) {
		final float scaleX = font.data.scaleX, scaleY = font.data.scaleY;
		x += glyph.xoffset * scaleX;
		y += glyph.yoffset * scaleY;
		float width = glyph.width * scaleX, height = glyph.height * scaleY;
		final float u = glyph.u, u2 = glyph.u2, v = glyph.v, v2 = glyph.v2;

		if (integer) {
			x = Math.round(x);
			y = Math.round(y);
			width = Math.round(width);
			height = Math.round(height);
		}
		final float x2 = x + width, y2 = y + height;

		final int page = glyph.page;
		int idx = this.idx[page];
		this.idx[page] += 20;

		if (pageGlyphIndices != null) pageGlyphIndices[page].add(glyphCount++);

		final float[] vertices = pageVertices[page];
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
		vertices[idx] = v;
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout setText (CharSequence str, float x, float y) {
		clear();
		return addText(str, x, y, 0, str.length(), 0, Align.left, false);
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout setText (CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
		clear();
		return addText(str, x, y, 0, str.length(), targetWidth, halign, wrap);
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout setText (CharSequence str, float x, float y, int start, int end, float targetWidth, int halign,
		boolean wrap) {
		clear();
		return addText(str, x, y, start, end, targetWidth, halign, wrap);
	}

	/** Clears any cached glyphs and adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout setText (CharSequence str, float x, float y, int start, int end, float targetWidth, int halign,
		boolean wrap, String truncate) {
		clear();
		return addText(str, x, y, start, end, targetWidth, halign, wrap, truncate);
	}

	/** Clears any cached glyphs and adds the specified glyphs.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public void setText (GlyphLayout layout, float x, float y) {
		clear();
		addText(layout, x, y);
	}

	/** Adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout addText (CharSequence str, float x, float y) {
		return addText(str, x, y, 0, str.length(), 0, Align.left, false, null);
	}

	/** Adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout addText (CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
		return addText(str, x, y, 0, str.length(), targetWidth, halign, wrap, null);
	}

	/** Adds glyphs for the specified text.
	 * @see #addText(CharSequence, float, float, int, int, float, int, boolean, String) */
	public GlyphLayout addText (CharSequence str, float x, float y, int start, int end, float targetWidth, int halign,
		boolean wrap) {
		return addText(str, x, y, start, end, targetWidth, halign, wrap, null);
	}

	/** Adds glyphs for the the specified text.
	 * @param x The x position for the left most character.
	 * @param y The y position for the top of most capital letters in the font (the {@link BitmapFontData#capHeight cap height}).
	 * @param start The first character of the string to draw.
	 * @param end The last character of the string to draw (exclusive).
	 * @param targetWidth The width of the area the text will be drawn, for wrapping or truncation.
	 * @param halign Horizontal alignment of the text, see {@link Align}.
	 * @param wrap If true, the text will be wrapped within targetWidth.
	 * @param truncate If not null, the text will be truncated within targetWidth with this string appended. May be an empty
	 *           string.
	 * @return The glyph layout for the cached string (the layout's height is the distance from y to the baseline). */
	public GlyphLayout addText (CharSequence str, float x, float y, int start, int end, float targetWidth, int halign,
		boolean wrap, String truncate) {
		GlyphLayout layout = Pools.obtain(GlyphLayout.class);
		pooledLayouts.add(layout);
		layout.setText(font, str, start, end, color, targetWidth, halign, wrap, truncate);
		addText(layout, x, y);
		return layout;
	}

	/** Adds the specified glyphs. */
	public void addText (GlyphLayout layout, float x, float y) {
		addToCache(layout, x, y + font.data.ascent);
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
		return getVertices(0);
	}

	public float[] getVertices (int page) {
		return pageVertices[page];
	}

	public int getVertexCount (int page) {
		return idx[page];
	}

	public Array<GlyphLayout> getLayouts () {
		return layouts;
	}
}
