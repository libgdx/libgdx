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

public class AndroidMusic implements Music, MediaPlayer.OnCompletionListener {
	private final AndroidAudio audio;
	private MediaPlayer player;
	private boolean isPrepared = true;
	protected boolean wasPlaying = false;
	private float volume = 1f;
	protected OnCompletionListener onCompletionListener;

	AndroidMusic (AndroidAudio audio, MediaPlayer player) {
		this.audio = audio;
		this.player = player;
		this.onCompletionListener = null;
		this.player.setOnCompletionListener(this);
	}

	@Override
	public void dispose () {
		if (player == null) return;
		try {
			player.release();
		} catch (Throwable t) {
			Gdx.app.log("AndroidMusic", "error while disposing AndroidMusic instance, non-fatal");
		} finally {
			player = null;
			onCompletionListener = null;
			audio.notifyMusicDisposed(this);
		}
	}

	@Override
	public boolean isLooping () {
		if (player == null) return false;
		try {
			return player.isLooping();
		} catch (Exception e) {
			// NOTE: isLooping() can potentially throw an exception and crash the application
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isPlaying () {
		if (player == null) return false;
		try {
			return player.isPlaying();
		} catch (Exception e) {
			// NOTE: isPlaying() can potentially throw an exception and crash the application
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void pause () { 
		if (player == null) return;
		try {
			if (player.isPlaying()) {			
				player.pause();
			}
		} catch (Exception e) {
			// NOTE: isPlaying() can potentially throw an exception and crash the application
			e.printStackTrace();
		}
		wasPlaying = false;
	}

	@Override
	public void play () {
		if (player == null) return;
		try {
			if (player.isPlaying()) return;
		} catch (Exception e) {
			// NOTE: isPlaying() can potentially throw an exception and crash the application
			e.printStackTrace();
			return;
		}

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
		if (player == null) return;
		player.setLooping(isLooping);
	}

	@Override
	public void setVolume (float volume) {
		if (player == null) return;
		player.setVolume(volume, volume);
		this.volume = volume;
	}

	@Override
	public float getVolume () {
		return volume;
	}

	@Override
	public void setPan (float pan, float volume) {
		if (player == null) return;
		float leftVolume = volume;
		float rightVolume = volume;

		if (pan < 0) {
			rightVolume *= (1 - Math.abs(pan));
		} else if (pan > 0) {
			leftVolume *= (1 - Math.abs(pan));
		}

		player.setVolume(leftVolume, rightVolume);
		this.volume = volume;
	}

	@Override
	public void stop () {
		if (player == null) return;
		if (isPrepared) {
			player.seekTo(0);
		}
		player.stop();
		isPrepared = false;
	}

	public void setPosition (float position) {
		if (player == null) return;
		try {
			if (!isPrepared) {
				player.prepare();
				isPrepared = true;
			}
			player.seekTo((int)(position * 1000));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float getPosition () {
		if (player == null) return 0.0f;
		return player.getCurrentPosition() / 1000f;
	}

	public float getDuration () {
		if (player == null) return 0.0f;
		return player.getDuration() / 1000f;
	}

	@Override
	public void setOnCompletionListener (OnCompletionListener listener) {
		onCompletionListener = listener;
	}

	@Override
	public void onCompletion (MediaPlayer mp) {
		if (onCompletionListener != null) {
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run () {
					onCompletionListener.onCompletion(AndroidMusic.this);
				}
			});
		}
	};
}
