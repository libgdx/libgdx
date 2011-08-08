package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;


public class BitmapFontLoader implements SynchronousAssetLoader<BitmapFont, BitmapFontParameter>{
	@Override
	public BitmapFont load (String fileName, BitmapFontParameter parameter) {
		FileHandle handle = Gdx.files.internal(fileName);
		
		if(parameter != null) return new BitmapFont(handle, parameter.flip);
		else return new BitmapFont(handle, false);
	}
}
