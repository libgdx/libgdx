package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class TextureAtlasLoader implements SynchronousAssetLoader<TextureAtlas, TextureAtlasParameter>{
	@Override
	public TextureAtlas load (String fileName, TextureAtlasParameter parameter) {
		if(parameter != null) return new TextureAtlas(Gdx.files.internal(fileName), parameter.flip);
		else return new TextureAtlas(Gdx.files.internal(fileName), false);
	}
}
