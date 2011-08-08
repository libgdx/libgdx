package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;

public class PixmapLoader implements AsynchronousAssetLoader<Pixmap, PixmapParameter> {
	Pixmap pixmap;
	
	@Override
	public void loadAsync (String fileName, PixmapParameter parameter) {
		pixmap = null;
		pixmap = new Pixmap(Gdx.files.internal(fileName));		
	}

	@Override
	public Pixmap loadSync () {
		return pixmap;
	}
}
