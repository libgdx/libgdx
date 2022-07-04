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

import android.annotation.TargetApi;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AndroidMusic implements Music, MediaPlayer.OnCompletionListener {
	private final AndroidAudio audio;
	private MediaPlayer player, player2;
	private MediaPlayer currentPlayer, nextPlayer;
	private boolean isPrepared = true;
	protected boolean wasPlaying = false;
	private float volume = 1f, pan = 0;
	protected OnCompletionListener onCompletionListener;
	private AssetFileDescriptor descriptor;
	private final FileHandle file;
	private boolean isLooping;

	AndroidMusic (AndroidAudio audio, FileHandle file) {
		this.audio = audio;
		this.file = file;

		this.player = createMediaPlayer();
		this.currentPlayer = player;

		this.onCompletionListener = null;
	}

	@Override
	public void dispose () {
		if (player == null) return;
		try {
			player.release();
			if (player2 != null) player2.release();
		} catch (Throwable t) {
			Gdx.app.log("AndroidMusic", "error while disposing AndroidMusic instance, non-fatal");
		} finally {
			player = null;
			player2 = null;
			onCompletionListener = null;
			audio.notifyMusicDisposed(this);
		}
	}

	@Override
	public boolean isLooping () {
		return isLooping;
	}

	@Override
	public boolean isPlaying () {
		if (player == null) return false;
		try {
			return player.isPlaying() || player2.isPlaying();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void pause () {
		if (player == null) return;
		try {
			currentPlayer.pause();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		wasPlaying = false;
	}

	@Override
	public void play () {
		if (player == null) return;
		try {
			if (!isPrepared) {
				currentPlayer.prepare();
				isPrepared = true;
			}
			currentPlayer.start();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setLooping (boolean isLooping) {
		this.isLooping = isLooping;
		if (player == null) return;
		if (Build.VERSION.SDK_INT >= 16) {
			if (isLooping) {
				if (player2 == null) {
					player2 = createMediaPlayer();
					nextPlayer = player2;
					setPan(pan, volume);
				}
				currentPlayer.setOnCompletionListener(createOnCompletionListener(nextPlayer));
				nextPlayer.setOnCompletionListener(createOnCompletionListener(currentPlayer));
				currentPlayer.setNextMediaPlayer(nextPlayer);
			} else if (player2 != null) {
				currentPlayer.setNextMediaPlayer(null);
				currentPlayer.setOnCompletionListener(null);
			}
		} else {
			player.setLooping(isLooping);
		}
	}

	@Override
	public void setVolume (float volume) {
		if (player == null) return;
		player.setVolume(volume, volume);
		if (player2 != null) player2.setVolume(volume, volume);
		this.volume = volume;
	}

	@Override
	public float getVolume () {
		return volume;
	}

	@Override
	public void setPan (float pan, float volume) {
		if (player == null) return;
		this.pan = pan;
		float leftVolume = volume;
		float rightVolume = volume;

		if (pan < 0) {
			rightVolume *= (1 - Math.abs(pan));
		} else if (pan > 0) {
			leftVolume *= (1 - Math.abs(pan));
		}

		player.setVolume(leftVolume, rightVolume);
		if (player2 != null) player2.setVolume(volume, volume);
		this.volume = volume;
	}

	@Override
	public void stop () {
		if (player == null) return;
		currentPlayer.stop();
		isPrepared = false;
	}

	public void setPosition (float position) {
		if (player == null) return;
		try {
			if (!isPrepared) {
				currentPlayer.prepare();
				isPrepared = true;
			}
			currentPlayer.seekTo((int)(position * 1000));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public float getPosition () {
		if (player == null) return 0.0f;
		return currentPlayer.getCurrentPosition() / 1000f;
	}

	public float getDuration () {
		if (player == null) return 0.0f;
		return currentPlayer.getDuration() / 1000f;
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
					if (onCompletionListener != null) {
						onCompletionListener.onCompletion(AndroidMusic.this);
					}
				}
			});
		}
	};

	@TargetApi(16)
	private MediaPlayer.OnCompletionListener createOnCompletionListener(final MediaPlayer next) {
		return new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mediaPlayer.reset();
				try {
					mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
					mediaPlayer.prepare();
				} catch (IOException e) {
					e.printStackTrace();
				}
				next.setNextMediaPlayer(mediaPlayer);
				currentPlayer = next;
				nextPlayer = mediaPlayer;
			}
		};
	}

	private MediaPlayer createMediaPlayer () {
		MediaPlayer mediaPlayer = new MediaPlayer();
		if (Build.VERSION.SDK_INT <= 21) {
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		} else {
			mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
				.setUsage(AudioAttributes.USAGE_GAME).build());
		}

		AndroidFileHandle aHandle = (AndroidFileHandle) file;
		if (aHandle.type() == Files.FileType.Internal) {
			try {
				this.descriptor = aHandle.getAssetFileDescriptor();
				mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
				mediaPlayer.prepare();
			} catch (Exception ex) {
				throw new GdxRuntimeException(
					"Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				mediaPlayer.setDataSource(aHandle.file().getPath());
				mediaPlayer.prepare();
			} catch (Exception ex) {
				throw new GdxRuntimeException(
					"Error loading audio file: " + file + "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		}

		return mediaPlayer;
	}
}
