package com.badlogic.gdx.tests.extensions;

import java.util.EnumMap;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;

public class FontMap<K extends Enum<K>> {

	PixmapPacker packer;
	EnumMap<K, BitmapFont> fontMap;
	EnumMap<K, BitmapFontData> fontDataMap;
	
	public FontMap(int width, int height, Format format, int spacing) {
		packer = new PixmapPacker(width, height, format, spacing, false);
	}
	
	public FontMap(int width, int height) {
		this(width, height, Format.RGBA8888, 2);
	}
	
	public void pack(K key, FileHandle fnt) {
		
	}		
}

/*
FontLoader loader = new FontLoader(Gdx.files.internal("data/fonts.json"));
final int[] FONT_SIZES = new int[] { 16, 18, 24 };
FontMap map = new FontMap(Gdx.files.internal("data/fonts.json"));

if (!map.cached()) {
	map.pack(Fonts.Arial, Gdx.files.internal("data/arial.ttf"), FONT_SIZES);
	map.pack(Fonts.ArialBold, Gdx.files.internal("data/arial-bold.ttf"), FONT_SIZES);
	map.cache();
}*/


//FontMap<FontKey> map = new FontMap<FontKey>();

//map.generate(FontKey.Arial, Gdx.files.internal("arial.ttf"), )


