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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont.RenderType;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.ColorEffect;
import com.badlogic.gdx.tools.hiero.unicodefont.effects.Effect;

/** Stores a number of glyphs on a single texture.
 * @author Nathan Sweet */
public class GlyphPage {
	private final UnicodeFont unicodeFont;
	private final int pageWidth, pageHeight;
	private final Texture texture;
	private int pageX, pageY, rowHeight;
	private boolean orderAscending;
	private final List<Glyph> pageGlyphs = new ArrayList(32);
	private final List<String> hashes = new ArrayList(32);

	/** @param pageWidth The width of the backing texture.
	 * @param pageHeight The height of the backing texture. */
	GlyphPage (UnicodeFont unicodeFont, int pageWidth, int pageHeight) {
		this.unicodeFont = unicodeFont;
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;

		texture = new Texture(pageWidth, pageHeight, Format.RGBA8888);
	}

	/** Loads glyphs to the backing texture and sets the image on each loaded glyph. Loaded glyphs are removed from the list.
	 * 
	 * If this page already has glyphs and maxGlyphsToLoad is -1, then this method will return 0 if all the new glyphs don't fit.
	 * This reduces texture binds when drawing since glyphs loaded at once are typically displayed together.
	 * @param glyphs The glyphs to load.
	 * @param maxGlyphsToLoad This is the maximum number of glyphs to load from the list. Set to -1 to attempt to load all the
	 *           glyphs.
	 * @return The number of glyphs that were actually loaded. */
	int loadGlyphs (List glyphs, int maxGlyphsToLoad) {
		if (rowHeight != 0 && maxGlyphsToLoad == -1) {
			// If this page has glyphs and we are not loading incrementally, return zero if any of the glyphs don't fit.
			int testX = pageX;
			int testY = pageY;
			int testRowHeight = rowHeight;
			for (Iterator iter = getIterator(glyphs); iter.hasNext();) {
				Glyph glyph = (Glyph)iter.next();
				int width = glyph.getWidth();
				int height = glyph.getHeight();
				if (testX + width >= pageWidth) {
					testX = 0;
					testY += testRowHeight;
					testRowHeight = height;
				} else if (height > testRowHeight) {
					testRowHeight = height;
				}
				if (testY + testRowHeight >= pageWidth) return 0;
				testX += width;
			}
		}

		GL11.glColor4f(1, 1, 1, 1);
		texture.bind();

		int i = 0;
		for (Iterator iter = getIterator(glyphs); iter.hasNext();) {
			Glyph glyph = (Glyph)iter.next();
			int width = Math.min(MAX_GLYPH_SIZE, glyph.getWidth());
			int height = Math.min(MAX_GLYPH_SIZE, glyph.getHeight());

			if (rowHeight == 0) {
				// The first glyph always fits.
				rowHeight = height;
			} else {
				// Wrap to the next line if needed, or break if no more fit.
				if (pageX + width >= pageWidth) {
					if (pageY + rowHeight + height >= pageHeight) break;
					pageX = 0;
					pageY += rowHeight;
					rowHeight = height;
				} else if (height > rowHeight) {
					if (pageY + height >= pageHeight) break;
					rowHeight = height;
				}
			}

			if (renderGlyph(glyph, width, height)) pageX += width;

			iter.remove();
			i++;
			if (i == maxGlyphsToLoad) {
				// If loading incrementally, flip orderAscending so it won't change, since we'll probably load the rest next time.
				orderAscending = !orderAscending;
				break;
			}
		}

		// Every other batch of glyphs added to a page are sorted the opposite way to attempt to keep same size glyps together.
		orderAscending = !orderAscending;

		return i;
	}

	/** Loads a single glyph to the backing texture, if it fits. */
	private boolean renderGlyph (Glyph glyph, int width, int height) {
		scratchGraphics.setComposite(AlphaComposite.Clear);
		scratchGraphics.fillRect(0, 0, MAX_GLYPH_SIZE, MAX_GLYPH_SIZE);
		scratchGraphics.setComposite(AlphaComposite.SrcOver);

		ByteBuffer glyphPixels = scratchByteBuffer;
		int format;
		if (unicodeFont.getRenderType() == RenderType.FreeType && unicodeFont.bitmapFont != null) {
			BitmapFontData data = unicodeFont.bitmapFont.getData();
			BitmapFont.Glyph g = data.getGlyph((char)glyph.getCodePoint());
			Pixmap fontPixmap = unicodeFont.bitmapFont.getRegions().get(g.page).getTexture().getTextureData().consumePixmap();

			int fontWidth = fontPixmap.getWidth();
			int padTop = unicodeFont.getPaddingTop(), padBottom = unicodeFont.getPaddingBottom();
			int padLeftBytes = unicodeFont.getPaddingLeft() * 4;
			int padXBytes = padLeftBytes + unicodeFont.getPaddingRight() * 4;
			int glyphRowBytes = width * 4, fontRowBytes = g.width * 4;

			ByteBuffer fontPixels = fontPixmap.getPixels();
			byte[] row = new byte[glyphRowBytes];
			glyphPixels.position(0);
			for (int i = 0; i < padTop; i++)
				glyphPixels.put(row);
			glyphPixels.position((height - padBottom) * glyphRowBytes);
			for (int i = 0; i < padBottom; i++)
				glyphPixels.put(row);
			glyphPixels.position(padTop * glyphRowBytes);
			for (int y = 0, n = g.height; y < n; y++) {
				fontPixels.position(((g.srcY + y) * fontWidth + g.srcX) * 4);
				fontPixels.get(row, padLeftBytes, fontRowBytes);
				glyphPixels.put(row);
			}
			fontPixels.position(0);
			glyphPixels.position(height * glyphRowBytes);
			glyphPixels.flip();
			format = GL11.GL_RGBA;
		} else {
			// Draw the glyph to the scratch image using Java2D.
			if (unicodeFont.getRenderType() == RenderType.Native) {
				for (Iterator iter = unicodeFont.getEffects().iterator(); iter.hasNext();) {
					Effect effect = (Effect)iter.next();
					if (effect instanceof ColorEffect) scratchGraphics.setColor(((ColorEffect)effect).getColor());
				}
				scratchGraphics.setColor(java.awt.Color.white);
				scratchGraphics.setFont(unicodeFont.getFont());
				scratchGraphics.drawString("" + (char)glyph.getCodePoint(), 0, unicodeFont.getAscent());
			} else if (unicodeFont.getRenderType() == RenderType.Java) {
				scratchGraphics.setColor(java.awt.Color.white);
				for (Iterator iter = unicodeFont.getEffects().iterator(); iter.hasNext();)
					((Effect)iter.next()).draw(scratchImage, scratchGraphics, unicodeFont, glyph);
				glyph.setShape(null); // The shape will never be needed again.
			}

			width = Math.min(width, texture.getWidth());
			height = Math.min(height, texture.getHeight());

			WritableRaster raster = scratchImage.getRaster();
			int[] row = new int[width];
			for (int y = 0; y < height; y++) {
				raster.getDataElements(0, y, width, 1, row);
				scratchIntBuffer.put(row);
			}
			format = GL12.GL_BGRA;
		}

		// Simple deduplication, doesn't work across pages of course.
		String hash = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(glyphPixels);
			glyphPixels.clear();
			BigInteger bigInt = new BigInteger(1, md.digest());
			hash = bigInt.toString(16);
		} catch (NoSuchAlgorithmException ex) {
		}

		try {
			for (int i = 0, n = hashes.size(); i < n; i++) {
				String other = hashes.get(i);
				if (other.equals(hash)) {
					Glyph dupe = pageGlyphs.get(i);
					glyph.setTexture(dupe.texture, dupe.u, dupe.v, dupe.u2, dupe.v2);
					System.out.println((char)glyph.getCodePoint() + " dupe of " + (char)dupe.getCodePoint());
					return false;
				}
			}
		} finally {
			hashes.add(hash);
			pageGlyphs.add(glyph);
		}

		Gdx.gl.glTexSubImage2D(texture.glTarget, 0, pageX, pageY, width, height, format, GL11.GL_UNSIGNED_BYTE, glyphPixels);

		float u = pageX / (float)texture.getWidth();
		float v = pageY / (float)texture.getHeight();
		float u2 = (pageX + width) / (float)texture.getWidth();
		float v2 = (pageY + height) / (float)texture.getHeight();
		glyph.setTexture(texture, u, v, u2, v2);

		return true;
	}

	/** Returns an iterator for the specified glyphs, sorted either ascending or descending. */
	private Iterator getIterator (List glyphs) {
		if (orderAscending) return glyphs.iterator();
		final ListIterator iter = glyphs.listIterator(glyphs.size());
		return new Iterator() {
			public boolean hasNext () {
				return iter.hasPrevious();
			}

			public Object next () {
				return iter.previous();
			}

			public void remove () {
				iter.remove();
			}
		};
	}

	/** Returns the glyphs stored on this page. */
	public List<Glyph> getGlyphs () {
		return pageGlyphs;
	}

	/** Returns the backing texture for this page. */
	public Texture getTexture () {
		return texture;
	}

	static public final int MAX_GLYPH_SIZE = 256;

	static private ByteBuffer scratchByteBuffer = ByteBuffer.allocateDirect(MAX_GLYPH_SIZE * MAX_GLYPH_SIZE * 4);

	static {
		scratchByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	static private IntBuffer scratchIntBuffer = scratchByteBuffer.asIntBuffer();

	static private BufferedImage scratchImage = new BufferedImage(MAX_GLYPH_SIZE, MAX_GLYPH_SIZE, BufferedImage.TYPE_INT_ARGB);
	static Graphics2D scratchGraphics = (Graphics2D)scratchImage.getGraphics();

	static {
		scratchGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		scratchGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	static public FontRenderContext renderContext = scratchGraphics.getFontRenderContext();
}
