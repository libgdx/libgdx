/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.gwt;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Music.OnCompletionListener;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound.SMSoundCallback;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSoundOptions;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;

public class GwtMusic implements Music, SMSoundCallback {
	private boolean isPlaying = false;
	private boolean isLooping = false;
	private SMSound sound;
	private float volume = 1f;
	private float pan = 0f;
	private SMSoundOptions soundOptions;
	private OnCompletionListener onCompletionListener;

	public GwtMusic (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		sound = SoundManager.createSound(url);
		soundOptions = new SMSoundOptions();
		soundOptions.callback = this;
	}

	@Override
	public void play () {
		if (isPlaying()) return;
		if (sound.getPaused()) {
			resume();
			return;
		}
		soundOptions.volume = (int)(volume * 100);
		soundOptions.pan = (int)(pan * 100);
		soundOptions.loops = 1;
		soundOptions.from = 0;
		sound.play(soundOptions);
		isPlaying = true;
	}
	
	public void resume () {
		sound.resume();
	}

	@Override
	public void pause () {
		sound.pause();
		isPlaying = false;
	}

	@Override
	public void stop () {
		sound.stop();
		isPlaying = false;
	}

	@Override
	public boolean isPlaying () {
		isPlaying = !sound.getPaused() && sound.getPlayState() == 1;
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
		sound.setVolume((int)(volume * 100));
		this.volume = volume;
	}
	
	@Override
	public float getVolume () {
		return volume;
	}
	
	@Override
	public void setPan (float pan, float volume) {
		sound.setPan((int)(pan * 100));
		sound.setVolume((int)(volume * 100));
		this.pan = pan;
		this.volume = volume;
	}

	@Override
	public void setPosition (float position) {
		sound.setPosition((int)(position * 1000f));
	}
	
	@Override
	public float getPosition () {
		return sound.getPosition() / 1000f;
	}

	@Override
	public void dispose () {
		sound.destruct();
	}
	
	@Override
	public void setOnCompletionListener (OnCompletionListener listener) {
		onCompletionListener = listener;
	}

	@Override
	public void onfinish () {
		if (isLooping)
			play();
		else if (onCompletionListener != null)
			onCompletionListener.onCompletion(this);
	}
}