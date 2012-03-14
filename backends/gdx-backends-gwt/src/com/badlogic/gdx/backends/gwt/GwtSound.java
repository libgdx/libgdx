package com.badlogic.gdx.backends.gwt;

import com.allen_sauer.gwt.voices.client.SoundController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public class GwtSound implements Sound {
	com.allen_sauer.gwt.voices.client.Sound sound;
	
	public GwtSound (SoundController soundController, FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		String mimeType = getMimeType(file.extension());
		sound = soundController.createSound(mimeType, url, false, false);
	}
	
	private String getMimeType(String extension) {
		if(extension.toLowerCase().equals("wav")) return com.allen_sauer.gwt.voices.client.Sound.MIME_TYPE_AUDIO_WAV_PCM;
		if(extension.toLowerCase().equals("ogg")) return com.allen_sauer.gwt.voices.client.Sound.MIME_TYPE_AUDIO_OGG_VORBIS;
		if(extension.toLowerCase().equals("mp3")) return com.allen_sauer.gwt.voices.client.Sound.MIME_TYPE_AUDIO_MPEG_MP3;
		return  com.allen_sauer.gwt.voices.client.Sound.MIME_TYPE_AUDIO_WAV_UNKNOWN; // FIXME not really possible...
	}

	@Override
	public long play () {
		sound.play();
		return 0;
	}

	@Override
	public long play (float volume) {
		sound.play();
		sound.setVolume(((int)volume * 100));
		return 0;
	}

	@Override
	public long loop () {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long loop (float volume) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void stop () {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose () {
	}

	@Override
	public void stop (long soundId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVolume (long soundId, float volume) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		// TODO Auto-generated method stub
	}
}
