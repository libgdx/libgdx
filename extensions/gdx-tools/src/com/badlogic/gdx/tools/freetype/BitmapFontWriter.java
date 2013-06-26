package com.badlogic.gdx.tools.freetype;

import java.io.OutputStream;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;

public class BitmapFontWriter {
	
	/**
	 * The font info line; ignored by LibGDX's BitmapFont reader
	 * @author Matt
	 *
	 */
	static class FontInfo {
		String face;
		int size;
		boolean bold;
		boolean italic;
		String charset;
		boolean unicode = true;
		int stretchH = 100;
		boolean smooth = true;
		int aa = 2;
		Rectangle padding = new Rectangle();
		int spacing_left = 0;
		int spacing_right = 0;
		
		int outline = 0;
		
		//stretchH=100 smooth=1 aa=2 padding=0,1,1,0 spacing=1,1 outline=0
	}
	
	public static void write(BitmapFont font, String faceName, int faceSize, OutputStream out) {
		font.getData().getFontFile();
		
	}
}
