
package com.badlogic.gdx.tests.extensions;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;

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
	}
	
	/** The "common" line parameters. */
	static class Common {
		public int lineHeight;
		public int base;
		public int scaleW;
		public int scaleH;
		public int pages;
		public int packed = 0;
	}
	
	public static void write (BitmapFontData fontData, String[] pageRefs, FileHandle outFntFile, FontInfo info) {
		Common common = new Common();
		
		if (info==null) {
			info = new FontInfo();
			info.face = outFntFile.nameWithoutExtension();
			
		}
		
		int lineHeight = (int)fontData.lineHeight;
		
//		int base = fontData.get
		
		StringBuffer buf = new StringBuffer();
		//INFO LINE
		buf.append("info face=\"")
			.append(info.face==null ? "" : info.face.replaceAll("\"", "'"))
			.append("\" size=").append(info.size)
			.append(" bold=").append(info.bold ? 1 : 0)
			.append(" italic=").append(info.italic ? 1 : 0)
			.append(" charset=\"").append(info.charset==null ? "" : info.charset)
			.append("\" unicode=").append(info.unicode ? 1 : 0)
			.append(" stretchH=").append(info.stretchH)
			.append(" smooth=").append(info.smooth)
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
			.append(common.lineHeight)
			.append(" base=").append(common.base)
			.append(" scaleW=").append(common.scaleW)
			.append(" scaleH=").append(common.scaleH)
			.append(" pages=").append(common.pages)
			.append(" packed=").append(common.packed)
			.append("\n");
		
		//common lineHeight=37 base=29 scaleW=256 scaleH=256 pages=2 packed=0
		
		String charset = info.charset;
		if (charset!=null&&charset.length()==0)
			charset = null;
		outFntFile.writeString(buf.toString(), false, charset);
	}
	
	public static void write (BitmapFontData fontData, Pixmap[] pages, FileHandle outFntFile, FontInfo info) {
		FileHandle parent = outFntFile.parent();
		String name = outFntFile.nameWithoutExtension();
		String[] pageRefs = new String[pages.length];
		for (int i=0; i<pages.length; i++) {
			String ref = pages.length==1 ? (name+".png") : (name+"_"+i+".png");
			
			//the ref for this image
			pageRefs[i] = ref;
			
			//write the PNG in that directory
			PixmapIO.writePNG(parent.child(ref), pages[i]);
		}
		
		//write the font data
		write(fontData, pageRefs, outFntFile, info);
	}
	
	public static void main(String[] args) {
		
		
		//Pixmap pix = new Pixmap(new FileHandle(""));
	}
}
