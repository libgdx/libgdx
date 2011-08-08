package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class MusicLoader implements SynchronousAssetLoader<Music, MusicParameter> {
	@Override
	public Music load (String fileName, MusicParameter parameter) {
		return Gdx.audio.newMusic(Gdx.files.internal(fileName));
	}
}
