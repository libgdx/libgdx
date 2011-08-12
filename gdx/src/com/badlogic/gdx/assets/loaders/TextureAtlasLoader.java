package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;

public class TextureAtlasLoader implements SynchronousAssetLoader<TextureAtlas, TextureAtlasParameter>{
	TextureAtlasData data;
	
	@Override
	public TextureAtlas load (AssetManager assetManager, String fileName, TextureAtlasParameter parameter) {
		for(Page page: data.getPages()) {
			Texture texture = assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
			page.texture = texture;
		}
		
		return new TextureAtlas(data);
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, TextureAtlasParameter parameter) {
		if(parameter != null) data = new TextureAtlasData(Gdx.files.internal(fileName), Gdx.files.internal(fileName).parent(), parameter.flip);
		else data = new TextureAtlasData(Gdx.files.internal(fileName), Gdx.files.internal(fileName).parent(), false);
		Array<AssetDescriptor> dependencies = new Array<AssetDescriptor>();
		for(Page page: data.getPages()) {
			FileHandle handle = page.textureFile;
			TextureParameter params = new TextureParameter();
			params.format = page.format;
			params.genMipMaps = page.useMipMaps;
			dependencies.add(new AssetDescriptor(handle.path().replaceAll("\\\\", "/"), Texture.class, params));
		}
		return dependencies;
	}
}
