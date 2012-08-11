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
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;

public class GwtMusic implements Music {
	boolean isPlaying = false;
	boolean isLooping = false;
	SMSound sound;

	public GwtMusic (FileHandle file) {
		String url = ((GwtApplication)Gdx.app).getBaseUrl() + file.path();
		sound = SoundManager.createSound(url, url);
		sound.loops(0);
	}

	@Override
	public void play () {
		if (isPlaying()) return;
		sound.play();
		isPlaying = true;
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
		isPlaying &= sound.playState() == 1;
		return isPlaying;
	}

	@Override
	public void setLooping (boolean isLooping) {
		sound.loops(isLooping ? 999 : 0);
		this.isLooping = isLooping;
	}

	@Override
	public boolean isLooping () {
		return isLooping;
	}

	@Override
	public void setVolume (float volume) {
		sound.setVolume((int)(volume * 100));
	}

	@Override
	public float getPosition () {
		return sound.getPosition() / 1000f;
	}

	@Override
	public void dispose () {
		sound.destruct();
	}
}
