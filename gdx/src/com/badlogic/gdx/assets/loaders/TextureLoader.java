package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;

public class TextureLoader implements AsynchronousAssetLoader<Texture, TextureParameter>{
	TextureData data;
	Texture texture;
	
	@Override
	public void loadAsync (String fileName, TextureParameter parameter) {
		FileHandle handle = Gdx.files.internal(fileName);
		Pixmap pixmap = new Pixmap(handle);
		Format format = null;
		boolean genMipMaps = false;
		texture = null;
		
		if(parameter != null) {
			format = parameter.format;
			genMipMaps = parameter.genMipMaps;
			texture = parameter.texture;
		}
		
		data = new FileTextureData(handle, pixmap, format, genMipMaps);
	}

	@Override
	public Texture loadSync () {
		if(texture != null) {
			texture.load(data);
			return texture;
		} else {
			return new Texture(data);
		}		
	}
}
