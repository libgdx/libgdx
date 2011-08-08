package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class TextureAtlasLoader implements SynchronousAssetLoader<TextureAtlas, TextureAtlasParameter>{
	@Override
	public TextureAtlas load (AssetManager assetManager, String fileName, TextureAtlasParameter parameter) {
		if(parameter != null) return new TextureAtlas(Gdx.files.internal(fileName), parameter.flip);
		else return new TextureAtlas(Gdx.files.internal(fileName), false);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, TextureAtlasParameter parameter) {
		return null;
	}
}
