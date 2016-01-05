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

package com.badlogic.gdx.tools.hiero;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tools.hiero.unicodefont.Glyph;
import com.badlogic.gdx.tools.hiero.unicodefont.GlyphPage;
import com.badlogic.gdx.tools.hiero.unicodefont.UnicodeFont;
import com.badlogic.gdx.utils.IntIntMap;

/** @author Nathan Sweet */
public class BMFontUtil {
	private final UnicodeFont unicodeFont;

	public BMFontUtil (UnicodeFont unicodeFont) {
		this.unicodeFont = unicodeFont;
	}

	public void save (File outputBMFontFile) throws IOException {
		File outputDir = outputBMFontFile.getParentFile();
		String outputName = outputBMFontFile.getName();
		if (outputName.endsWith(".fnt")) outputName = outputName.substring(0, outputName.length() - 4);

		// Always include space and the missing gyph.
		getGlyph(' ');
		getGlyph('\u0000');
		unicodeFont.loadGlyphs();

		PrintStream out = new PrintStream(new FileOutputStream(new File(outputDir, outputName + ".fnt")));
		Font font = unicodeFont.getFont();
		int pageWidth = unicodeFont.getGlyphPageWidth();
		int pageHeight = unicodeFont.getGlyphPageHeight();
		out.println("info face=\"" + font.getFontName() + "\" size=" + font.getSize() + " bold=" + (font.isBold() ? 1 : 0)
			+ " italic=" + (font.isItalic() ? 1 : 0) + " charset=\"\" unicode=0 stretchH=100 smooth=1 aa=1 padding="
			+ unicodeFont.getPaddingTop() + "," + unicodeFont.getPaddingLeft() + "," + unicodeFont.getPaddingBottom() + ","
			+ unicodeFont.getPaddingRight() + " spacing=" + unicodeFont.getPaddingAdvanceX() + ","
			+ unicodeFont.getPaddingAdvanceY());
		out.println("common lineHeight=" + unicodeFont.getLineHeight() + " base=" + unicodeFont.getAscent() + " scaleW=" + pageWidth
			+ " scaleH=" + pageHeight + " pages=" + unicodeFont.getGlyphPages().size() + " packed=0");

		int pageIndex = 0, glyphCount = 0;
		for (Iterator pageIter = unicodeFont.getGlyphPages().iterator(); pageIter.hasNext();) {
			GlyphPage page = (GlyphPage)pageIter.next();
			String fileName;
			if (pageIndex == 0 && !pageIter.hasNext())
				fileName = outputName + ".png";
			else
				fileName = outputName + (pageIndex + 1) + ".png";
			out.println("page id=" + pageIndex + " file=\"" + fileName + "\"");
			glyphCount += page.getGlyphs().size();
			pageIndex++;
		}

		out.println("chars count=" + glyphCount);

		pageIndex = 0;
		List allGlyphs = new ArrayList(512);
		for (Iterator pageIter = unicodeFont.getGlyphPages().iterator(); pageIter.hasNext();) {
			GlyphPage page = (GlyphPage)pageIter.next();
			List<Glyph> glyphs = page.getGlyphs();
			Collections.sort(glyphs, new Comparator<Glyph>() {
				public int compare (Glyph o1, Glyph o2) {
					return o1.getCodePoint() - o2.getCodePoint();
				}
			});
			for (Iterator glyphIter = page.getGlyphs().iterator(); glyphIter.hasNext();) {
				Glyph glyph = (Glyph)glyphIter.next();
				writeGlyph(out, pageWidth, pageHeight, pageIndex, glyph);
			}
			allGlyphs.addAll(page.getGlyphs());
			pageIndex++;
		}

		String ttfFileRef = unicodeFont.getFontFile();
		if (ttfFileRef == null)
			System.out.println("Kerning information could not be output because a TTF font file was not specified.");
		else {
			Kerning kerning = new Kerning();
			try {
				kerning.load(Gdx.files.internal(ttfFileRef).read(), font.getSize());
			} catch (IOException ex) {
				System.out.println("Unable to read kerning information from font: " + ttfFileRef);
				ex.printStackTrace();
			}

			IntIntMap glyphCodeToCodePoint = new IntIntMap();
			for (Iterator iter = allGlyphs.iterator(); iter.hasNext();) {
				Glyph glyph = (Glyph)iter.next();
				glyphCodeToCodePoint.put(new Integer(getGlyphCode(font, glyph.getCodePoint())), new Integer(glyph.getCodePoint()));
			}

			List kernings = new ArrayList(256);
			class KerningPair {
				public int firstCodePoint, secondCodePoint, offset;
			}
			for (IntIntMap.Entry entry : kerning.getKernings()) {
				int firstGlyphCode = entry.key >> 16;
				int secondGlyphCode = entry.key & 0xffff;
				int offset = entry.value;
				int firstCodePoint = glyphCodeToCodePoint.get(firstGlyphCode, -1);
				int secondCodePoint = glyphCodeToCodePoint.get(secondGlyphCode, -1);

				if (firstCodePoint == -1 || secondCodePoint == -1 || offset == 0) {
					// We are not outputting one or both of these glyphs, or the offset is zero anyway.
					continue;
				}

				KerningPair pair = new KerningPair();
				pair.firstCodePoint = firstCodePoint;
				pair.secondCodePoint = secondCodePoint;
				pair.offset = offset;
				kernings.add(pair);
			}
			out.println("kernings count=" + kernings.size());
			for (Iterator iter = kernings.iterator(); iter.hasNext();) {
				KerningPair pair = (KerningPair)iter.next();
				out.println("kerning first=" + pair.firstCodePoint + " second=" + pair.secondCodePoint + " amount=" + pair.offset);
			}
		}
		out.close();

		int width = unicodeFont.getGlyphPageWidth();
		int height = unicodeFont.getGlyphPageHeight();
		IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
		BufferedImage pageImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		int[] row = new int[width];

		pageIndex = 0;
		for (Iterator pageIter = unicodeFont.getGlyphPages().iterator(); pageIter.hasNext();) {
			GlyphPage page = (GlyphPage)pageIter.next();
			String fileName;
			if (pageIndex == 0 && !pageIter.hasNext())
				fileName = outputName + ".png";
			else
				fileName = outputName + (pageIndex + 1) + ".png";

			page.getTexture().bind();
			buffer.clear();
			GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);
			WritableRaster raster = pageImage.getRaster();
			for (int y = 0; y < height; y++) {
				buffer.get(row);
				raster.setDataElements(0, y, width, 1, row);
			}
			File imageOutputFile = new File(outputDir, fileName);
			ImageIO.write(pageImage, "png", imageOutputFile);

			pageIndex++;
		}
	}

	/** @return May be null. */
	private Glyph getGlyph (char c) {
		char[] chars = {c};
		GlyphVector vector = unicodeFont.getFont().layoutGlyphVector(GlyphPage.renderContext, chars, 0, chars.length,
			Font.LAYOUT_LEFT_TO_RIGHT);
		Rectangle bounds = vector.getGlyphPixelBounds(0, GlyphPage.renderContext, 0, 0);
		return unicodeFont.getGlyph(vector.getGlyphCode(0), c, bounds, vector, 0);
	}

	void writeGlyph (PrintStream out, int pageWidth, int pageHeight, int pageIndex, Glyph glyph) {
		out.println("char id=" + String.format("%-6s", glyph.getCodePoint()) //
			+ "x=" + String.format("%-5s", (int)(glyph.getU() * pageWidth)) //
			+ "y=" + String.format("%-5s", (int)(glyph.getV() * pageHeight)) //
			+ "width=" + String.format("%-5s", glyph.getWidth()) //
			+ "height=" + String.format("%-5s", glyph.getHeight()) //
			+ "xoffset=" + String.format("%-5s", glyph.getXOffset()) //
			+ "yoffset=" + String.format("%-5s", glyph.getYOffset()) //
			+ "xadvance=" + String.format("%-5s", glyph.getXAdvance()) //
			+ "page=" + String.format("%-5s", pageIndex) //
			+ "chnl=0 ");
	}

	private int getGlyphCode (Font font, int codePoint) {
		char[] chars = Character.toChars(codePoint);
		GlyphVector vector = font.layoutGlyphVector(GlyphPage.renderContext, chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
		return vector.getGlyphCode(0);
	}
}
