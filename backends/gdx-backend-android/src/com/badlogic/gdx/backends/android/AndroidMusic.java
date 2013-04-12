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

package com.badlogic.gdx.backends.android;

import java.io.IOException;

import android.media.MediaPlayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

public class AndroidMusic implements Music {
	private final AndroidAudio audio;
	private MediaPlayer player;
	private boolean isPrepared = true;
	protected boolean wasPlaying = false;
	private float volume = 1f;

	AndroidMusic (AndroidAudio audio, MediaPlayer player) {
		this.audio = audio;
		this.player = player;
	}

	@Override
	public void dispose () {
		if (player == null) return;
		try {
			if (player.isPlaying()) player.stop();
			player.release();
		} catch (Throwable t) {
			Gdx.app.log("AndroidMusic", "error while disposing AndroidMusic instance, non-fatal");
		} finally {
			player = null;
			synchronized (audio.musics) {
				audio.musics.remove(this);
			}
		}
	}

	@Override
	public boolean isLooping () {
		return player.isLooping();
	}

	@Override
	public boolean isPlaying () {
		return player.isPlaying();
	}

	@Override
	public void pause () {
		if (player.isPlaying()) player.pause();
	}

	@Override
	public void play () {
		if (player.isPlaying()) return;

		try {
			if (!isPrepared) {
				player.prepare();
				isPrepared = true;
			}
			player.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setLooping (boolean isLooping) {
		player.setLooping(isLooping);
	}

	@Override
	public void setVolume (float volume) {
		player.setVolume(volume, volume);
		this.volume = volume;
	}
	
	@Override
	public float getVolume () {
		return volume;
	}

	@Override
	public void stop () {
		if (isPrepared) {
			player.seekTo(0);
		}
		player.stop();
		isPrepared = false;
	}

	public float getPosition () {
		return player.getCurrentPosition() / 1000f;
	}
}
