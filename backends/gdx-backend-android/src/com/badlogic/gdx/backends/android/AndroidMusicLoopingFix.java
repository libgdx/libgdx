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

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import java.io.FileDescriptor;
import java.io.IOException;

public class AndroidMusicLoopingFix extends AndroidMusic {

	// which file is getting played
	public static final int FILE_DESCRIPTOR = 1;
	public static final int INTERNAL = 2;
	public static final int PATH = 3;

	// states of the media player
	public static final int STATE_PLAYING = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_STOP = 3;

	// current state
	private int state = STATE_STOP;

	// current mediaPlayer which is playing
	private int mediaPlayerIndex = -1;


	private final AndroidAudio audio;
	// 3 media players
	private final MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper();
	//	private final MediaPlayer mediaPlayers[] = new MediaPlayer[3];
	private boolean isPrepared = true;
	protected boolean wasPlaying = false;
	private float volume = 1f;
	protected OnCompletionListener onCompletionListener;
	private AndroidFileHandle aHandle;
	private FileDescriptor fd;
	private int type;

	AndroidMusicLoopingFix(AndroidAudio audio, FileDescriptor fd) {
		this(audio, null, fd, FILE_DESCRIPTOR);
	}

	AndroidMusicLoopingFix(AndroidAudio audio, AndroidFileHandle aHandle, int type) {
		this(audio, aHandle, null, type);
	}

	AndroidMusicLoopingFix(AndroidAudio audio, AndroidFileHandle aHandle, FileDescriptor fd, int type) {
		super();
		this.audio = audio;
		this.aHandle = aHandle;
		this.fd = fd;
		this.type = type;
		this.onCompletionListener = null;
	}

	@Override
	public void dispose () {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return;
				try {
					mediaPlayer.release();
				} catch (Throwable t) {
					Gdx.app.log("AndroidMusic", "error while disposing AndroidMusic instance, non-fatal");
				} finally {
					mediaPlayer = null;
					synchronized (audio.musics) {
						audio.musics.remove(this);
					}
				}
			}
		}
	}

	@Override
	public boolean isLooping () {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return false;
				try {
					return mediaPlayer.isLooping();
				} catch (Exception e) {
					// NOTE: isLooping() can potentially throw an exception and crash the application
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
	}

	@Override
	public boolean isPlaying () {
		synchronized (mediaPlayerWrapper) {
			boolean isPlaying = false;
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				//Gdx.app.debug("mediaplayer", "mediaPlayer=" + mediaPlayer);
				if (mediaPlayer == null) continue;
				//Gdx.app.debug("mediaplayer", mediaPlayer + " isPlaying=" + mediaPlayer.isPlaying());
				try {
					isPlaying = isPlaying || mediaPlayer.isPlaying();
				} catch (Exception e) {
					// NOTE: isPlaying() can potentially throw an exception and crash the application
					e.printStackTrace();
				}
			}
			return isPlaying;
		}
	}

	@Override
	public void pause () {
		synchronized (mediaPlayerWrapper) {
			wasPlaying = false;
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				//Gdx.app.debug("mediaplayer", mediaPlayer + " - pause");
				if (mediaPlayer == null) continue;
				try {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
						wasPlaying = true;
					}
				} catch (Exception e) {
					// NOTE: isPlaying() can potentially throw an exception and crash the application
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void play () {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				mediaPlayerWrapper.setValue(i, new MediaPlayer());
				switch (type) {
					case FILE_DESCRIPTOR:
						try {
							mediaPlayerWrapper.getValue(i).setDataSource(fd);
							mediaPlayerWrapper.getValue(i).prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case INTERNAL:
						try {
							AssetFileDescriptor descriptor = aHandle.getAssetFileDescriptor();
							mediaPlayerWrapper.getValue(i).setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
							descriptor.close();
							mediaPlayerWrapper.getValue(i).prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
					case PATH:
						try {
							mediaPlayerWrapper.getValue(i).setDataSource(aHandle.file().getPath());
							mediaPlayerWrapper.getValue(i).prepare();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break;
				}
				mediaPlayerWrapper.getValue(i).setVolume(volume, volume);
				mediaPlayerWrapper.getValue(i).setOnCompletionListener(completionListener);
			}
			// set nextMediaPlayers
			mediaPlayerWrapper.getValue(0).setNextMediaPlayer(mediaPlayerWrapper.getValue(1));
			mediaPlayerWrapper.getValue(1).setNextMediaPlayer(mediaPlayerWrapper.getValue(2));

			mediaPlayerIndex = 0;
			MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(mediaPlayerIndex);
			if (mediaPlayer == null) return;
			try {
				if (mediaPlayer.isPlaying()) return;
			} catch (Exception e) {
				// NOTE: isPlaying() can potentially throw an exception and crash the application
				e.printStackTrace();
				return;
			}

			try {
				if (!isPrepared) {
					mediaPlayer.prepare();
					isPrepared = true;
				}
				//Gdx.app.debug("mediaplayer", "*** now ***");
				mediaPlayer.start();
				state = STATE_PLAYING;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setLooping (boolean isLooping) {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return;
				mediaPlayer.setLooping(isLooping);
			}
		}
	}

	@Override
	public void setVolume (float volume) {
		synchronized (mediaPlayerWrapper) {
			this.volume = volume;
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				//Gdx.app.debug("aMp", "pos=" + i + " - " + mediaPlayer);
				if (mediaPlayer != null) { // && aMp.isPlaying()) {
					//Gdx.app.debug("aMp", "aMp=" + mediaPlayer.isPlaying());
					//Gdx.app.debug("aMp", i + " settingVol=" + volume);
					mediaPlayer.setVolume(volume, volume);
				}
			}
		}
	}

	@Override
	public float getVolume () {
		return volume;
	}

	@Override
	public void setPan (float pan, float volume) {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return;
				float leftVolume = volume;
				float rightVolume = volume;

				if (pan < 0) {
					rightVolume *= (1 - Math.abs(pan));
				} else if (pan > 0) {
					leftVolume *= (1 - Math.abs(pan));
				}

				mediaPlayer.setVolume(leftVolume, rightVolume);
			}
			this.volume = volume;
		}
	}

	//@Override
	public void oldstop () {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return;
				if (isPrepared) {
					mediaPlayer.seekTo(0);
				}
				mediaPlayer.stop();
			}
			isPrepared = false;
		}
	}

	public void setPosition (float position) {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				if (mediaPlayer == null) return;
				try {
					if (!isPrepared) {
						mediaPlayer.prepare();
						isPrepared = true;
					}
					mediaPlayer.seekTo((int) (position * 1000));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public float getPosition () {
		synchronized (mediaPlayerWrapper) {
			if (mediaPlayerWrapper.getValue(mediaPlayerIndex) == null) return 0.0f;
			return mediaPlayerWrapper.getValue(mediaPlayerIndex).getCurrentPosition() / 1000f;
		}
	}

	public float getDuration () {
		synchronized (mediaPlayerWrapper) {
			if (mediaPlayerWrapper.getValue(mediaPlayerIndex) == null) return 0.0f;
			return mediaPlayerWrapper.getValue(mediaPlayerIndex).getDuration() / 1000f;
		}
	}

	public void setOnCompletionListener (OnCompletionListener listener) {
		onCompletionListener = listener;
	}









	/**
	 * internal listener which handles looping thing
	 */
	private MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer curmp) {
			synchronized (mediaPlayerWrapper) {
				//Gdx.app.debug("mediaplayer", curmp + " completed");

				for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
					//Gdx.app.debug("mediaplayer", "mediaPlayers" + i + " isplaying=" + mediaPlayerWrapper.getValue(i).isPlaying());
				}
				int mpEnds = 0;
				int mpPlaying = 0;
				int mpNext = 0;
				if (curmp == mediaPlayerWrapper.getValue(0)) {
					mpEnds = 0;
					mpPlaying = 1;
					mpNext = 2;
				} else if (curmp == mediaPlayerWrapper.getValue(1)) {
					mpEnds = 1;
					mpPlaying = 2;
					mpNext = 0;  // corrected, else index out of range
				} else if (curmp == mediaPlayerWrapper.getValue(2)) {
					mpEnds = 2;
					mpPlaying = 0; // corrected, else index out of range
					mpNext = 1; // corrected, else index out of range
				}

				// as we have set mp2 mp1's next, so index will be 1
				mediaPlayerIndex = mpPlaying;
				//Log.d("BZMediaPlayer", "Media Player " + mpEnds);
				try {
					// mp3 is already playing release it
					if (mediaPlayerWrapper.getValue(mpNext) != null) {
						mediaPlayerWrapper.getValue(mpNext).release();
					}
					// if we are playing uri
					mediaPlayerWrapper.setValue(mpNext, new MediaPlayer());
					//Gdx.app.debug("mediaplayer", "mediaPlayers[" + mpNext + "] created=" + mediaPlayerWrapper.getValue(mpNext));
					AssetFileDescriptor descriptor = aHandle.getAssetFileDescriptor();
					mediaPlayerWrapper.getValue(mpNext).setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
					descriptor.close();
					mediaPlayerWrapper.getValue(mpNext).prepare();
					//Gdx.app.debug("mediaplayer", "mediaPlayers[" + mpNext + "].prepare()=");
					// at listener to mp3
					mediaPlayerWrapper.getValue(mpNext).setOnCompletionListener(this);
					// set volume
					mediaPlayerWrapper.getValue(mpNext).setVolume(volume, volume);
					//Gdx.app.debug("mediaplayer", "mediaPlayers[" + mpNext + "].setVolume");
					// set nextMediaPlayer
					//Gdx.app.debug("mediaplayer", "mediaPlayers[" + mpPlaying + "].setNextMediaPlayer()=" + mediaPlayerWrapper.getValue(mpNext));
					mediaPlayerWrapper.getValue(mpPlaying).setNextMediaPlayer(mediaPlayerWrapper.getValue(mpNext));
					// set nextMediaPlayer volume
					//Gdx.app.debug("mediaplayer", "mediaPlayers[" + mpPlaying + "].setVolume");
					mediaPlayerWrapper.getValue(mpPlaying).setVolume(volume, volume);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * pause current playing session
	 */
	public void newPause() {
		synchronized (mediaPlayerWrapper) {
			if (state == STATE_PLAYING) {
				mediaPlayerWrapper.getValue(mediaPlayerIndex).pause();
				//Log.d("BZMediaPlayer", "pausing");
				state = STATE_PAUSED;
			}
		}
	}

	/**
	 * get current state
	 * @return
	 */
	public int getState() {
		return state;
	}

	/**
	 * stop every mediaplayer
	 */
	@Override
	public void stop() {
		synchronized (mediaPlayerWrapper) {
			for (int i = 0; i < mediaPlayerWrapper.size(); i++) {
				MediaPlayer mediaPlayer = mediaPlayerWrapper.getValue(i);
				mediaPlayer.stop();
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.release();
				}
			}
			state = STATE_STOP;
		}
	}

	class MediaPlayerWrapper {
		private MediaPlayer[] mediaPlayers = new MediaPlayer[3];
		public int size() {
			return mediaPlayers.length;
		}

		public void setValue(int index, MediaPlayer value) {
			synchronized (mediaPlayers) {
				mediaPlayers[index] = value;
			}
		}

		public MediaPlayer getValue(int index) {
			synchronized (mediaPlayers) {
				return mediaPlayers[index];
			}
		}

	}
}
