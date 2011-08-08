package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;

public class MusicLoader implements SynchronousAssetLoader<Music, MusicParameter> {
	@Override
	public Music load (AssetManager assetManager, String fileName, MusicParameter parameter) {
		return Gdx.audio.newMusic(Gdx.files.internal(fileName));
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, MusicParameter parameter) {
		return null;
	}
}
