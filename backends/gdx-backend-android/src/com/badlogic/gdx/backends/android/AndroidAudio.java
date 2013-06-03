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
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implementation of the {@link Audio} interface for Android.
 * 
 * @author mzechner */
public final class AndroidAudio implements Audio {
	private final AudioManager manager;
	protected final List<AndroidMusic> musics = new ArrayList<AndroidMusic>();
	private final List<SoundPoolContainer> containers = new ArrayList<SoundPoolContainer>();
	int MAX_STREAMS_PER_POOL;

	public AndroidAudio (Context context, AndroidApplicationConfiguration config) {
		manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		MAX_STREAMS_PER_POOL = config.maxSimultaneousSounds;
		if (context instanceof Activity) {
			((Activity)context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}
	}

	protected void pause () {
		synchronized (musics) {
			for (AndroidMusic music : musics) {
				if (music.isPlaying()) {
					music.wasPlaying = true;
					music.pause();

				} else
					music.wasPlaying = false;
			}
		}
	}

	protected void resume () {
		synchronized (musics) {
			for (int i = 0; i < musics.size(); i++) {
				if (musics.get(i).wasPlaying == true) musics.get(i).play();
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		return new AndroidAudioDevice(samplingRate, isMono);
	}

	/** {@inheritDoc} */
	@Override
	public Music newMusic (FileHandle file) {
		AndroidFileHandle aHandle = (AndroidFileHandle)file;

		MediaPlayer mediaPlayer = new MediaPlayer();

		if (aHandle.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = aHandle.assets.openFd(aHandle.path());
				mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
				descriptor.close();
				mediaPlayer.prepare();
				AndroidMusic music = new AndroidMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error loading audio file: " + file
					+ "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {
				mediaPlayer.setDataSource(aHandle.file().getPath());
				mediaPlayer.prepare();
				AndroidMusic music = new AndroidMusic(this, mediaPlayer);
				synchronized (musics) {
					musics.add(music);
				}
				return music;
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error loading audio file: " + file, ex);
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public Sound newSound (FileHandle file) {
		AndroidFileHandle aHandle = (AndroidFileHandle)file;
		if (aHandle.type() == FileType.Internal) {
			try {
				AssetFileDescriptor descriptor = aHandle.assets.openFd(aHandle.path());

				for (SoundPoolContainer container : containers) {
					if (!container.isFull()) {
						return container.loadSound(manager, container.soundPool.load(descriptor, 1));
					}
				}
				SoundPoolContainer container = new SoundPoolContainer();
				containers.add(container);
				AndroidSound sound = container.loadSound(manager, container.soundPool.load(descriptor, 1));

				descriptor.close();
				return sound;
			} catch (IOException ex) {
				throw new GdxRuntimeException("Error loading audio file: " + file
					+ "\nNote: Internal audio files must be placed in the assets directory.", ex);
			}
		} else {
			try {

				for (SoundPoolContainer container : containers) {
					if (!container.isFull()) {
						return container.loadSound(manager, container.soundPool.load(aHandle.file().getPath(), 1));
					}
				}
				SoundPoolContainer container = new SoundPoolContainer();
				containers.add(container);
				return container.loadSound(manager, container.soundPool.load(aHandle.file().getPath(), 1));
			} catch (Exception ex) {
				throw new GdxRuntimeException("Error loading audio file: " + file, ex);
			}
		}
	}

	private Sound loadSound () {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		return new AndroidAudioRecorder(samplingRate, isMono);
	}

	/** Kills the soundpool and all other resources */
	public void dispose () {
		synchronized (musics) {
			// gah i hate myself.... music.dispose() removes the music from the list...
			ArrayList<AndroidMusic> musicsCopy = new ArrayList<AndroidMusic>(musics);
			for (AndroidMusic music : musicsCopy) {
				music.dispose();
			}
		}
		for (SoundPoolContainer container : containers) {
			container.dispose();
		}
	}

	/** A private class that holds the <code>SoundPool</code>. */
	private class SoundPoolContainer {
		public SoundPool soundPool;
		private int size;

		public SoundPoolContainer () {
			soundPool = new SoundPool(MAX_STREAMS_PER_POOL, AudioManager.STREAM_MUSIC, 100);
			size = 0;
		}

		/** Creates a new AndroidSound and put it in the map.
		 * @param manager Android AudioManager
		 * @param soundId The id of the sound isntance.
		 * @return The sound that just got created. */
		public AndroidSound loadSound (AudioManager manager, int soundId) {
			size++;
			return new AndroidSound(soundPool, manager, soundId);
		}

		/** Check if the SoundPool is full or not.
		 * @return Whether the SoundPool is full or not. */
		public boolean isFull () {
			return size >= MAX_STREAMS_PER_POOL;
		}

		/** Dispose the sound pool and clear the soundmap. */
		public void dispose () {
			soundPool.release();
		}
	}
}