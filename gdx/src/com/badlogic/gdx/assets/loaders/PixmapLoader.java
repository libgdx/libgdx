package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;

public class PixmapLoader implements AsynchronousAssetLoader<Pixmap, PixmapParameter> {
	Pixmap pixmap;
	
	@Override
	public void loadAsync (AssetManager manager, String fileName, PixmapParameter parameter) {
		pixmap = null;
		pixmap = new Pixmap(Gdx.files.internal(fileName));		
	}

	@Override
	public Pixmap loadSync () {
		return pixmap;
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, PixmapParameter parameter) {
		return null;
	}
}
