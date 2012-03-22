package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class GwtSound implements Sound {
	
	public GwtSound (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
	}
	
	@Override
	public long play () {
		return 0;
	}

	@Override
	public long play (float volume) {
		return 0;
	}

	@Override
	public long loop () {
		return 0;
	}

	@Override
	public long loop (float volume) {
		return 0;
	}

	@Override
	public void stop () {
	}

	@Override
	public void dispose () {
	}

	@Override
	public void stop (long soundId) {
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
	}
		
	@Override
	public void setPitch (long soundId, float pitch) {
	}

	@Override
	public void setVolume (long soundId, float volume) {
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
	}
}
