
package com.badlogic.gdx.tests.extensions;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.utils.Array;

public class BitmapFontWriter {

	/** The Padding parameter for FontInfo. */
	static class Padding {
		public int up, down, left, right;
	}

	/** The spacing parameter for FontInfo. */
	static class Spacing {
		public int horizontal, vertical;
	}

	/** The font "info" line; ignored by LibGDX's BitmapFont reader */
	static class FontInfo {
		public String face;
		public int size = 12;
		public boolean bold;
		public boolean italic;
		public String charset;
		public boolean unicode = true;
		public int stretchH = 100;
		public boolean smooth = true;
		public int aa = 2;
		public Padding padding = new Padding();
		public Spacing spacing = new Spacing();
		public int outline = 0;
		
		public FontInfo() {
		}
		
		public FontInfo(String face, int size) {
			this.face = face;
			this.size = size;
		}
	}
	
	
	public static void write (BitmapFontData fontData, String[] pageRefs, FileHandle outFntFile, FontInfo info, int scaleW, int scaleH) {
		if (info==null) {
			info = new FontInfo();
			info.face = outFntFile.nameWithoutExtension();
		}
		
		int lineHeight = (int)fontData.lineHeight;
		int pages = pageRefs.length;
		int packed = 0;
		int base = (int)((fontData.capHeight) + (fontData.flipped ? -fontData.ascent : fontData.ascent));
		
		StringBuilder buf = new StringBuilder();
		//INFO LINE
		buf.append("info face=\"")
			.append(info.face==null ? "" : info.face.replaceAll("\"", "'"))
			.append("\" size=").append(info.size)
			.append(" bold=").append(info.bold ? 1 : 0)
			.append(" italic=").append(info.italic ? 1 : 0)
			.append(" charset=\"").append(info.charset==null ? "" : info.charset)
			.append("\" unicode=").append(info.unicode ? 1 : 0)
			.append(" stretchH=").append(info.stretchH)
			.append(" smooth=").append(info.smooth ? 1 : 0)
			.append(" aa=").append(info.aa)
			.append(" padding=")
				.append(info.padding.up).append(",")
				.append(info.padding.down).append(",")
				.append(info.padding.left).append(",")
				.append(info.padding.right)
			.append(" spacing=")
				.append(info.spacing.horizontal).append(",")
				.append(info.spacing.vertical)
			.append("\n");
		
		//COMMON line
		buf.append("common lineHeight=")
			.append(lineHeight)
			.append(" base=").append(base)
			.append(" scaleW=").append(scaleW)
			.append(" scaleH=").append(scaleH)
			.append(" pages=").append(pages)
			.append(" packed=").append(packed)
			.append(" alphaChnl=0 redChnl=0 greenChnl=0 blueChnl=0")
			.append("\n");
		
		//PAGES
		for (int i=0; i<pageRefs.length; i++) {
			buf.append("page id=")
				.append(i)
				.append(" file=\"")
				.append(pageRefs[i])
				.append("\"\n");
		}
		
		//CHARS
		Array<Glyph> glyphs = new Array<Glyph>(256);
		for (int i=0; i<fontData.glyphs.length; i++) {
			if (fontData.glyphs[i]==null)
				continue;
			
			for (int j=0; j<fontData.glyphs[i].length; j++) {
				if (fontData.glyphs[i][j]!=null) {
					glyphs.add(fontData.glyphs[i][j]);
				}
			}
		}
		
		buf.append("chars count=").append(glyphs.size).append("\n");
		
		//CHAR definitions
		for (int i=0; i<glyphs.size; i++) {
			Glyph g = glyphs.get(i);
			buf.append("char id=")
				.append(String.format("%-5s", g.id))
				.append("x=").append(String.format("%-5s", g.srcX))
				.append("y=").append(String.format("%-5s", g.srcY))
				.append("width=").append(String.format("%-5s", g.width))
				.append("height=").append(String.format("%-5s", g.height))
				.append("xoffset=").append(String.format("%-5s", g.xoffset))
				.append("yoffset=").append(String.format("%-5s", fontData.flipped ? g.yoffset : -(g.height + g.yoffset) ))
				.append("xadvance=").append(String.format("%-5s", g.xadvance))
				.append("page=").append(String.format("%-5s", g.page))
				.append("chnl=0")
				.append("\n");
		}
		
		//KERNINGS
		int kernCount = 0;
		StringBuilder kernBuf = new StringBuilder(); 
		for (int i = 0; i < glyphs.size; i++) {
			for (int j = 0; j < glyphs.size; j++) {
				Glyph first = glyphs.get(i);
				Glyph second = glyphs.get(j);
				int kern = first.getKerning((char)second.id);
				if (kern!=0) {
					kernCount++;
					kernBuf.append("kerning first=").append(first.id)
							 .append(" second=").append(second.id)
							 .append(" amount=").append(kern)
							 .append("\n");
				}
			}
		}

		//KERN info
		buf.append("kernings count=").append(kernCount).append("\n");
		buf.append(kernBuf);
		
		String charset = info.charset;
		if (charset!=null&&charset.length()==0)
			charset = null;
		
		outFntFile.writeString(buf.toString(), false, charset);
	}
	
	public static String[] writePixmaps (Pixmap[] pages, FileHandle outputDir, String fileName) {
		if (pages==null || pages.length==0)
			throw new IllegalArgumentException("no pixmaps supplied to BitmapFontWriter.write");
		
		String[] pageRefs = new String[pages.length];
		
		for (int i=0; i<pages.length; i++) {
			String ref = pages.length==1 ? (fileName+".png") : (fileName+"_"+i+".png");
			
			//the ref for this image
			pageRefs[i] = ref;
						
			//write the PNG in that directory
			PixmapIO.writePNG(outputDir.child(ref), pages[i]);
		}
		return pageRefs;
	}
	
	public static void write (BitmapFontData fontData, Pixmap[] pages, FileHandle outFntFile, FontInfo info) {
		String[] pageRefs = writePixmaps(pages, outFntFile.parent(), outFntFile.nameWithoutExtension());
		
		//write the font data
		write(fontData, pageRefs, outFntFile, info, pages[0].getWidth(), pages[0].getHeight());
	}
	
}
