package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

public class SoundLoader implements SynchronousAssetLoader<Sound, SoundParameter> {
	@Override
	public Sound load (AssetManager assetManager, String fileName, SoundParameter parameter) {
		return Gdx.audio.newSound(Gdx.files.internal(fileName));
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, SoundParameter parameter) {
		return null;
	}
}
