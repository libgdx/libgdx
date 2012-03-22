package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class GwtMusic implements Music {
	boolean isPlaying = false;
	boolean isLooping = false;
	
	public GwtMusic (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
	}
	
	@Override
	public void play () {
		if(isPlaying) return;
		isPlaying = true;
	}

	@Override
	public void pause () {
		isPlaying = false;
	}

	@Override
	public void stop () {
		isPlaying = false;
	}

	@Override
	public boolean isPlaying () {
		return isPlaying;
	}

	@Override
	public void setLooping (boolean isLooping) {
		this.isLooping = isLooping;
	}

	@Override
	public boolean isLooping () {
		return isLooping;
	}

	@Override
	public void setVolume (float volume) {
	}

	@Override
	public float getPosition () {
		return 0;
	}

	@Override
	public void dispose () {
	}
}
