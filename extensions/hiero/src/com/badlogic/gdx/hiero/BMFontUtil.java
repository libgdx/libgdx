
package com.badlogic.gdx.hiero;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.font.GlyphMetrics;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.hiero.unicodefont.Glyph;
import com.badlogic.gdx.hiero.unicodefont.GlyphPage;
import com.badlogic.gdx.hiero.unicodefont.UnicodeFont;

/**
 * @author Nathan Sweet <misc@n4te.com>
 */
public class BMFontUtil {
	private final UnicodeFont unicodeFont;

	public BMFontUtil (UnicodeFont unicodeFont) {
		this.unicodeFont = unicodeFont;
	}

	public void save (File outputBMFontFile) throws IOException {
		File outputDir = outputBMFontFile.getParentFile();
		String outputName = outputBMFontFile.getName();
		if (outputName.endsWith(".fnt")) outputName = outputName.substring(0, outputName.length() - 4);

		unicodeFont.loadGlyphs();

		PrintStream out = new PrintStream(new FileOutputStream(new File(outputDir, outputName + ".fnt")));
		Font font = unicodeFont.getFont();
		int pageWidth = unicodeFont.getGlyphPageWidth();
		int pageHeight = unicodeFont.getGlyphPageHeight();
		out.println("info face=\"" + font.getFontName() + "\" size=" + font.getSize() + " bold=" + (font.isBold() ? 1 : 0)
			+ " italic=" + (font.isItalic() ? 1 : 0)
			+ " charset=\"\" unicode=0 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=1,1");
		out.println("common lineHeight=" + unicodeFont.getLineHeight() + " base=" + unicodeFont.getAscent() + " scaleW="
			+ pageWidth + " scaleH=" + pageHeight + " pages=" + unicodeFont.getGlyphPages().size() + " packed=0");

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

		// Always output space entry (codepoint 32).
		int[] glyphMetrics = getGlyphMetrics(font, 32);
		int xAdvance = glyphMetrics[1];
		out.println("char id=32   x=0     y=0     width=0     height=0     xoffset=0     yoffset=" + unicodeFont.getAscent()
			+ "    xadvance=" + xAdvance + "     page=0  chnl=0 ");

		pageIndex = 0;
		List allGlyphs = new ArrayList(512);
		for (Iterator pageIter = unicodeFont.getGlyphPages().iterator(); pageIter.hasNext();) {
			GlyphPage page = (GlyphPage)pageIter.next();
			for (Iterator glyphIter = page.getGlyphs().iterator(); glyphIter.hasNext();) {
				Glyph glyph = (Glyph)glyphIter.next();

				glyphMetrics = getGlyphMetrics(font, glyph.getCodePoint());
				int xOffset = glyphMetrics[0];
				xAdvance = glyphMetrics[1];

				out.println("char id=" + glyph.getCodePoint() + "   " + "x=" + (int)(glyph.getU() * pageWidth) + "     y="
					+ (int)(glyph.getV() * pageHeight) + "     width=" + glyph.getWidth() + "     height=" + glyph.getHeight()
					+ "     xoffset=" + xOffset + "     yoffset=" + glyph.getYOffset() + "    xadvance=" + xAdvance + "     page="
					+ pageIndex + "  chnl=0 ");
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
				kerning.load(Gdx.files.readFile(ttfFileRef, FileType.Internal), font.getSize());
			} catch (IOException ex) {
				System.out.println("Unable to read kerning information from font: " + ttfFileRef);
			}

			Map glyphCodeToCodePoint = new HashMap();
			for (Iterator iter = allGlyphs.iterator(); iter.hasNext();) {
				Glyph glyph = (Glyph)iter.next();
				glyphCodeToCodePoint.put(new Integer(getGlyphCode(font, glyph.getCodePoint())), new Integer(glyph.getCodePoint()));
			}

			List kernings = new ArrayList(256);
			class KerningPair {
				public int firstCodePoint, secondCodePoint, offset;
			}
			for (Iterator iter1 = allGlyphs.iterator(); iter1.hasNext();) {
				Glyph firstGlyph = (Glyph)iter1.next();
				int firstGlyphCode = getGlyphCode(font, firstGlyph.getCodePoint());
				int[] values = kerning.getValues(firstGlyphCode);
				if (values == null) continue;
				for (int i = 0; i < values.length; i++) {
					Integer secondCodePoint = (Integer)glyphCodeToCodePoint.get(new Integer(values[i] & 0xffff));
					if (secondCodePoint == null) continue; // We may not be outputting the second character.
					int offset = values[i] >> 16;
					KerningPair pair = new KerningPair();
					pair.firstCodePoint = firstGlyph.getCodePoint();
					pair.secondCodePoint = secondCodePoint.intValue();
					pair.offset = offset;
					kernings.add(pair);
				}
			}
			out.println("kernings count=" + kerning.getCount());
			for (Iterator iter = kernings.iterator(); iter.hasNext();) {
				KerningPair pair = (KerningPair)iter.next();
				out.println("kerning first=" + pair.firstCodePoint + "  second=" + pair.secondCodePoint + "  amount=" + pair.offset);
			}
		}
		out.close();

		pageIndex = 0;
		for (Iterator pageIter = unicodeFont.getGlyphPages().iterator(); pageIter.hasNext();) {
			GlyphPage page = (GlyphPage)pageIter.next();
			String fileName;
			if (pageIndex == 0 && !pageIter.hasNext())
				fileName = outputName + ".png";
			else
				fileName = outputName + (pageIndex + 1) + ".png";
			File imageOutputFile = new File(outputDir, fileName);
			FileOutputStream imageOutput = new FileOutputStream(imageOutputFile);
			try {
				// BOZO - Save texture to PNG.
				// saveImage(page.getTexture(), "png", imageOutput, true);
			} finally {
				imageOutput.close();
			}
			// Flip output image.
			Image image = new ImageIcon(imageOutputFile.getAbsolutePath()).getImage();
			BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = bufferedImage.getGraphics();
			g.drawImage(image, 0, 0, null);
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
			tx.translate(0, -image.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			bufferedImage = op.filter(bufferedImage, null);
			ImageIO.write(bufferedImage, "png", imageOutputFile);

			pageIndex++;
		}
	}

	private int getGlyphCode (Font font, int codePoint) {
		char[] chars = Character.toChars(codePoint);
		GlyphVector vector = font.layoutGlyphVector(GlyphPage.renderContext, chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
		return vector.getGlyphCode(0);
	}

	private int[] getGlyphMetrics (Font font, int codePoint) {
		// xOffset and xAdvance will be incorrect for unicode characters such as combining marks or non-spacing characters
		// (eg Pnujabi's "\u0A1C\u0A47") that require the context of surrounding glyphs to determine spacing, but thisis the
		// best we can do with the BMFont format.
		char[] chars = Character.toChars(codePoint);
		GlyphVector vector = font.layoutGlyphVector(GlyphPage.renderContext, chars, 0, chars.length, Font.LAYOUT_LEFT_TO_RIGHT);
		GlyphMetrics metrics = vector.getGlyphMetrics(0);
		int xOffset = vector.getGlyphPixelBounds(0, GlyphPage.renderContext, 0.5f, 0).x - unicodeFont.getPaddingLeft();
		int xAdvance = (int)(metrics.getAdvanceX() + unicodeFont.getPaddingAdvanceX() + unicodeFont.getPaddingLeft() + unicodeFont
			.getPaddingRight());
		return new int[] {xOffset, xAdvance};
	}
}
