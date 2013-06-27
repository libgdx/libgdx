
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
		public Padding padding;
		public Spacing spacing;
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
		
		System.out.println(info.face);
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
