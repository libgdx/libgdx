package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;

public class GwtSound implements Sound {
	SMSound sound;
	
	public GwtSound (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		sound = SoundManager.createSound(url, url);
	}
	
	@Override
	public long play () {
		sound.play();
		return 0;
	}

	@Override
	public long play (float volume) {
		sound.setVolume((int)(volume * 100));
		sound.play();
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
		sound.stop();
	}

	@Override
	public void dispose () {
		sound.destruct();
	}

	@Override
	public void stop (long soundId) {
		sound.stop();
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		// FIXME
	}
		
	@Override
	public void setPitch (long soundId, float pitch) {
		// FIXME
	}

	@Override
	public void setVolume (long soundId, float volume) {
		// FIXME
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		// FIXME
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		// TODO Auto-generated method stub
		return play(volume);
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		return loop(volume);
	}
}
