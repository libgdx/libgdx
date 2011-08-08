package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundLoader implements SynchronousAssetLoader<Sound, SoundParameter> {
	@Override
	public Sound load (String fileName, SoundParameter parameter) {
		return Gdx.audio.newSound(Gdx.files.internal(fileName));
	}
}
