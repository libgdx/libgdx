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

package com.badlogic.gdx.tools.bmfont;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.tools.hiero.Hiero;
import com.badlogic.gdx.utils.Array;

/** A utility to output BitmapFontData to a FNT file. This can be useful for caching the result from TrueTypeFont, for faster load
 * times.
 * <p>
 * The font file format is from the AngelCodeFont BMFont tool.
 * <p>
 * Output is nearly identical to the FreeType settting in the {@link Hiero} tool. BitmapFontWriter gives more flexibility, eg
 * borders and shadows can be used. Hiero is able to avoid outputting the same glyph image more than once if multiple character
 * codes have the exact same glyph.
 * @author mattdesl AKA davedes */
public class BitmapFontWriter {

	/** The output format. */
	public static enum OutputFormat {

		/** AngelCodeFont text format */
		Text,
		/** AngelCodeFont XML format */
		XML;
	}

	/** The output format */
	private static OutputFormat format = OutputFormat.Text;

	/** Sets the AngelCodeFont output format for subsequent writes; can be text (for LibGDX) or XML (for other engines, like
	 * Pixi.js).
	 * 
	 * @param fmt the output format to use */
	public static void setOutputFormat (OutputFormat fmt) {
		if (fmt == null) throw new NullPointerException("format cannot be null");
		format = fmt;
	}

	/** Returns the currently used output format.
	 * @return the output format */
	public static OutputFormat getOutputFormat () {
		return format;
	}

	/** The Padding parameter for FontInfo. */
	public static class Padding {
		public int up, down, left, right;

		public Padding () {
		}

		public Padding (int up, int down, int left, int right) {
			this.up = up;
			this.down = down;
			this.left = left;
			this.right = right;
		}
	}

	/** The spacing parameter for FontInfo. */
	public static class Spacing {
		public int horizontal, vertical;
	}

	/** The font "info" line; everything except padding is ignored by LibGDX's BitmapFont reader, it is otherwise just useful for
	 * clean and organized output. */
	public static class FontInfo {
		/** Face name */
		public String face;
		/** Font size (pt) */
		public int size = 12;
		/** Whether the font is bold */
		public boolean bold;
		/** Whether the font is italic */
		public boolean italic;
		/** The charset; or null/empty for default */
		public String charset;
		/** Whether the font uses unicode glyphs */
		public boolean unicode = true;
		/** Stretch for height; default to 100% */
		public int stretchH = 100;
		/** Whether smoothing is applied */
		public boolean smooth = true;
		/** Amount of anti-aliasing that was applied to the font */
		public int aa = 2;
		/** Padding that was applied to the font */
		public Padding padding = new Padding();
		/** Horizontal/vertical spacing that was applied to font */
		public Spacing spacing = new Spacing();
		public int outline = 0;

		public FontInfo () {
		}

		public FontInfo (String face, int size) {
			this.face = face;
			this.size = size;
		}
	}

	private static String quote (Object params) {
		return quote(params, false);
	}

	private static String quote (Object params, boolean spaceAfter) {
		if (BitmapFontWriter.getOutputFormat() == OutputFormat.XML)
			return "\"" + params.toString().trim() + "\"" + (spaceAfter ? " " : "");
		else
			return params.toString();
	}

	/** Writes the given BitmapFontData to a file, using the specified <tt>pageRefs</tt> strings as the image paths for each
	 * texture page. The glyphs in BitmapFontData have a "page" id, which references the index of the pageRef you specify here.
	 * 
	 * The FontInfo parameter is useful for cleaner output; such as including a size and font face name hint. However, it can be
	 * null to use default values. LibGDX ignores most of the "info" line when reading back fonts, only padding is used. Padding
	 * also affects the size, location, and offset of the glyphs that are output.
	 * 
	 * Likewise, the scaleW and scaleH are only for cleaner output. They are currently ignored by LibGDX's reader. For maximum
	 * compatibility with other BMFont tools, you should use the width and height of your texture pages (each page should be the
	 * same size).
	 * 
	 * @param fontData the bitmap font
	 * @param pageRefs the references to each texture page image file, generally in the same folder as outFntFile
	 * @param outFntFile the font file to save to (typically ends with '.fnt')
	 * @param info the optional info for the file header; can be null
	 * @param scaleW the width of your texture pages
	 * @param scaleH the height of your texture pages */
	public static void writeFont (BitmapFontData fontData, String[] pageRefs, FileHandle outFntFile, FontInfo info, int scaleW,
		int scaleH) {
		if (info == null) {
			info = new FontInfo();
			info.face = outFntFile.nameWithoutExtension();
		}

		int lineHeight = (int)fontData.lineHeight;
		int pages = pageRefs.length;
		int packed = 0;
		int base = (int)((fontData.capHeight) + (fontData.flipped ? -fontData.ascent : fontData.ascent));
		OutputFormat fmt = BitmapFontWriter.getOutputFormat();
		boolean xml = fmt == OutputFormat.XML;

		StringBuilder buf = new StringBuilder();

		if (xml) {
			buf.append("<font>\n");
		}
		String xmlOpen = xml ? "\t<" : "";
		String xmlCloseSelf = xml ? "/>" : "";
		String xmlTab = xml ? "\t" : "";
		String xmlClose = xml ? ">" : "";

		String xmlQuote = xml ? "\"" : "";
		String alphaChnlParams = xml ? " alphaChnl=\"0\" redChnl=\"0\" greenChnl=\"0\" blueChnl=\"0\""
			: " alphaChnl=0 redChnl=0 greenChnl=0 blueChnl=0";

		// INFO LINE
		buf.append(xmlOpen).append("info face=\"").append(info.face == null ? "" : info.face.replaceAll("\"", "'"))
			.append("\" size=").append(quote(info.size)).append(" bold=").append(quote(info.bold ? 1 : 0)).append(" italic=")
			.append(quote(info.italic ? 1 : 0)).append(" charset=\"").append(info.charset == null ? "" : info.charset)
			.append("\" unicode=").append(quote(info.unicode ? 1 : 0)).append(" stretchH=").append(quote(info.stretchH))
			.append(" smooth=").append(quote(info.smooth ? 1 : 0)).append(" aa=").append(quote(info.aa)).append(" padding=")
			.append(xmlQuote).append(info.padding.up).append(",").append(info.padding.down).append(",").append(info.padding.left)
			.append(",").append(info.padding.right).append(xmlQuote).append(" spacing=").append(xmlQuote)
			.append(info.spacing.horizontal).append(",").append(info.spacing.vertical).append(xmlQuote).append(xmlCloseSelf)
			.append("\n");

		// COMMON line
		buf.append(xmlOpen).append("common lineHeight=").append(quote(lineHeight)).append(" base=").append(quote(base))
			.append(" scaleW=").append(quote(scaleW)).append(" scaleH=").append(quote(scaleH)).append(" pages=").append(quote(pages))
			.append(" packed=").append(quote(packed)).append(alphaChnlParams).append(xmlCloseSelf).append("\n");

		if (xml) buf.append("\t<pages>\n");

		// PAGES
		for (int i = 0; i < pageRefs.length; i++) {
			buf.append(xmlTab).append(xmlOpen).append("page id=").append(quote(i)).append(" file=\"").append(pageRefs[i])
				.append("\"").append(xmlCloseSelf).append("\n");
		}

		if (xml) buf.append("\t</pages>\n");

		// CHARS
		Array<Glyph> glyphs = new Array<Glyph>(256);
		for (int i = 0; i < fontData.glyphs.length; i++) {
			if (fontData.glyphs[i] == null) continue;

			for (int j = 0; j < fontData.glyphs[i].length; j++) {
				if (fontData.glyphs[i][j] != null) {
					glyphs.add(fontData.glyphs[i][j]);
				}
			}
		}

		buf.append(xmlOpen).append("chars count=").append(quote(glyphs.size)).append(xmlClose).append("\n");

		int padLeft = 0, padRight = 0, padTop = 0, padX = 0, padY = 0;
		if (info != null) {
			padTop = info.padding.up;
			padLeft = info.padding.left;
			padRight = info.padding.right;
			padX = padLeft + padRight;
			padY = info.padding.up + info.padding.down;
		}

		// CHAR definitions
		for (int i = 0; i < glyphs.size; i++) {
			Glyph g = glyphs.get(i);
			boolean empty = g.width == 0 || g.height == 0;
			buf.append(xmlTab).append(xmlOpen).append("char id=").append(quote(String.format("%-6s", g.id), true)).append("x=")
				.append(quote(String.format("%-5s", empty ? 0 : g.srcX - padLeft), true)).append("y=")
				.append(quote(String.format("%-5s", empty ? 0 : g.srcY - padRight), true)).append("width=")
				.append(quote(String.format("%-5s", empty ? 0 : g.width + padX), true)).append("height=")
				.append(quote(String.format("%-5s", empty ? 0 : g.height + padY), true)).append("xoffset=")
				.append(quote(String.format("%-5s", g.xoffset - padLeft), true)).append("yoffset=")
				.append(
					quote(String.format("%-5s", fontData.flipped ? g.yoffset + padTop : -(g.height + (g.yoffset + padTop))), true))
				.append("xadvance=").append(quote(String.format("%-5s", g.xadvance), true)).append("page=")
				.append(quote(String.format("%-5s", g.page), true)).append("chnl=").append(quote(0, true)).append(xmlCloseSelf)
				.append("\n");
		}

		if (xml) buf.append("\t</chars>\n");

		// KERNINGS
		int kernCount = 0;
		StringBuilder kernBuf = new StringBuilder();
		for (int i = 0; i < glyphs.size; i++) {
			for (int j = 0; j < glyphs.size; j++) {
				Glyph first = glyphs.get(i);
				Glyph second = glyphs.get(j);
				int kern = first.getKerning((char)second.id);
				if (kern != 0) {
					kernCount++;
					kernBuf.append(xmlTab).append(xmlOpen).append("kerning first=").append(quote(first.id)).append(" second=")
						.append(quote(second.id)).append(" amount=").append(quote(kern, true)).append(xmlCloseSelf).append("\n");
				}
			}
		}

		// KERN info
		buf.append(xmlOpen).append("kernings count=").append(quote(kernCount)).append(xmlClose).append("\n");
		buf.append(kernBuf);

		if (xml) {
			buf.append("\t</kernings>\n");
			buf.append("</font>");
		}

		String charset = info.charset;
		if (charset != null && charset.length() == 0) charset = null;

		outFntFile.writeString(buf.toString(), false, charset);
	}

	/** A utility method which writes the given font data to a file.
	 * 
	 * The specified pixmaps are written to the parent directory of <tt>outFntFile</tt>, using that file's name without an
	 * extension for the PNG file name(s).
	 * 
	 * The specified FontInfo is optional, and can be null.
	 * 
	 * Typical usage looks like this:
	 * 
	 * <pre>
	 * BitmapFontWriter.writeFont(myFontData, myFontPixmaps, Gdx.files.external(&quot;fonts/output.fnt&quot;), new FontInfo(&quot;Arial&quot;, 16));
	 * </pre>
	 * 
	 * @param fontData the font data
	 * @param pages the pixmaps to write as PNGs
	 * @param outFntFile the output file for the font definition
	 * @param info the optional font info for the header file, can be null */
	public static void writeFont (BitmapFontData fontData, Pixmap[] pages, FileHandle outFntFile, FontInfo info) {
		String[] pageRefs = writePixmaps(pages, outFntFile.parent(), outFntFile.nameWithoutExtension());

		// write the font data
		writeFont(fontData, pageRefs, outFntFile, info, pages[0].getWidth(), pages[0].getHeight());
	}

	/** A utility method to write the given array of pixmaps to the given output directory, with the specified file name. If the
	 * pages array is of length 1, then the resulting file ref will look like: "fileName.png".
	 * 
	 * If the pages array is greater than length 1, the resulting file refs will be appended with "_N", such as "fileName_0.png",
	 * "fileName_1.png", "fileName_2.png" etc.
	 * 
	 * The returned string array can then be passed to the <tt>writeFont</tt> method.
	 * 
	 * Note: None of the pixmaps will be disposed.
	 * 
	 * @param pages the pages of pixmap data to write
	 * @param outputDir the output directory
	 * @param fileName the file names for the output images
	 * @return the array of string references to be used with <tt>writeFont</tt> */
	public static String[] writePixmaps (Pixmap[] pages, FileHandle outputDir, String fileName) {
		if (pages == null || pages.length == 0) throw new IllegalArgumentException("no pixmaps supplied to BitmapFontWriter.write");

		String[] pageRefs = new String[pages.length];

		for (int i = 0; i < pages.length; i++) {
			String ref = pages.length == 1 ? (fileName + ".png") : (fileName + "_" + i + ".png");

			// the ref for this image
			pageRefs[i] = ref;

			// write the PNG in that directory
			PixmapIO.writePNG(outputDir.child(ref), pages[i]);
		}
		return pageRefs;
	}

	/** A convenience method to write pixmaps by page; typically returned from a PixmapPacker when used alongside
	 * FreeTypeFontGenerator.
	 * 
	 * @param pages the pages containing the Pixmaps
	 * @param outputDir the output directory
	 * @param fileName the file name
	 * @return the file refs */
	public static String[] writePixmaps (Array<Page> pages, FileHandle outputDir, String fileName) {
		Pixmap[] pix = new Pixmap[pages.size];
		for (int i = 0; i < pages.size; i++) {
			pix[i] = pages.get(i).getPixmap();
		}
		return writePixmaps(pix, outputDir, fileName);
	}
}
