package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;


public class BitmapFontLoader implements AsynchronousAssetLoader<BitmapFont, BitmapFontParameter>{
	BitmapFontData data;
	AssetManager manager;
	String fileName;
	BitmapFontParameter parameter;

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, BitmapFontParameter parameter) {
		FileHandle handle = Gdx.files.internal(fileName);
		data = new BitmapFontData(handle, parameter != null? parameter.flip: false);
		
		Array<AssetDescriptor> deps = new Array<AssetDescriptor>();
		deps.add(new AssetDescriptor(data.getImageFile(), Texture.class, null));
		return deps;
	}

	@Override
	public void loadAsync (AssetManager manager, String fileName, BitmapFontParameter parameter) {
		this.manager = manager;
		this.fileName = fileName;
		this.parameter = parameter;
	}

	@Override
	public BitmapFont loadSync () {
		FileHandle handle = Gdx.files.internal(fileName);
		TextureRegion region = new TextureRegion(manager.get(data.getImageFile(), Texture.class));		
		return new BitmapFont(data, region, true);
	}

}
