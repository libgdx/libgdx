package com.badlogic.gdx.backends.gwt;

import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController;
import com.allen_sauer.gwt.voices.client.handler.PlaybackCompleteEvent;
import com.allen_sauer.gwt.voices.client.handler.SoundHandler;
import com.allen_sauer.gwt.voices.client.handler.SoundLoadStateChangeEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public class GwtMusic implements Music {
	Sound sound;
	boolean isPlaying = false;
	boolean isLooping = false;
	
	public GwtMusic (SoundController soundController, FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		String mimeType = getMimeType(file.extension());
		sound = soundController.createSound(mimeType, url, true, false);
		sound.addEventHandler(new SoundHandler() {
			@Override
			public void onSoundLoadStateChange (SoundLoadStateChangeEvent event) {
				if(isPlaying) sound.play();
			}
			
			@Override
			public void onPlaybackComplete (PlaybackCompleteEvent event) {
				isPlaying = false;
			}
		});
	}
	
	private String getMimeType(String extension) {
		if(extension.toLowerCase().equals("wav")) return Sound.MIME_TYPE_AUDIO_WAV_PCM;
		if(extension.toLowerCase().equals("ogg")) return Sound.MIME_TYPE_AUDIO_OGG_VORBIS;
		if(extension.toLowerCase().equals("mp3")) return Sound.MIME_TYPE_AUDIO_MPEG_MP3;
		return Sound.MIME_TYPE_AUDIO_WAV_UNKNOWN; // FIXME not really possible...
	}

	@Override
	public void play () {
		if(isPlaying) return;
		sound.play();
		isPlaying = true;
	}

	@Override
	public void pause () {
		sound.stop();
		isPlaying = false;
	}

	@Override
	public void stop () {
		sound.stop();
		isPlaying = false;
	}

	@Override
	public boolean isPlaying () {
		return isPlaying;
	}

	@Override
	public void setLooping (boolean isLooping) {
		sound.setLooping(isLooping);
		this.isLooping = isLooping;
	}

	@Override
	public boolean isLooping () {
		return isLooping;
	}

	@Override
	public void setVolume (float volume) {
		sound.setVolume((int)(100 * volume));
	}

	@Override
	public float getPosition () {
		return 0;
	}

	@Override
	public void dispose () {
	}
}
