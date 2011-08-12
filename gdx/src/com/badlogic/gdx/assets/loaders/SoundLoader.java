package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;

public class SoundLoader extends SynchronousAssetLoader<Sound, SoundParameter> {
	public SoundLoader (FileHandleResolver resolver) {
		super(resolver);
	}

	@Override
	public Sound load (AssetManager assetManager, String fileName, SoundParameter parameter) {
		return Gdx.audio.newSound(resolve(fileName));
	}

	@Override
	public Array<AssetDescriptor> getDependencies (String fileName, SoundParameter parameter) {
		return null;
	}
}
