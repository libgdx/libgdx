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
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSound;
import com.badlogic.gdx.backends.gwt.soundmanager2.SMSoundOptions;
import com.badlogic.gdx.backends.gwt.soundmanager2.SoundManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BooleanArray;

public class GwtSound implements Sound {
	
	/** The maximum number of sound instances to create to support simultaneous playback. */
	private static final int MAX_SOUNDS = 8;
	
	/** Our sounds. */
	private GwtMusic[] sounds;
	/** The next player we think should be available for play - we circle through them to find a free one. */
	private int soundIndex;
	/** The path to the sound file. */
	private FileHandle soundFile;
	
	public GwtSound (FileHandle file) {
		soundFile = file;
		sounds = new GwtMusic[MAX_SOUNDS];
		sounds[0] = new GwtMusic(file);
		soundIndex = 0;
	}
	
	/** Let's find a sound that isn't currently playing.
	 * @return  The index of the sound or -1 if none is available. */
	private int findAvailableSound() {
		for (int i = 0; i < sounds.length; i++) {
			int index = (soundIndex + i) % sounds.length;
			if (sounds[index] == null || !sounds[index].isPlaying()) {
				// point to the next likely free player
				soundIndex = (index + 1) % sounds.length; 
				
				// return the free player
				return index;
			}
		}
		
		// all are busy playing, stop the next sound in the queue and reuse it
		int index = soundIndex % sounds.length;
		soundIndex = (index + 1) % sounds.length;
		return index;
	}

	@Override
	public long play () {
		return play(1.0f, 1.0f, 0.0f, false);
	}

	@Override
	public long play (float volume) {
		return play(volume, 1.0f, 0.0f, false);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, false);
	}
	
	private long play (float volume, float pitch, float pan, boolean loop) {
		int soundId = findAvailableSound();
		if (soundId >= 0) {
			GwtMusic sound;
			if (sounds[soundId] == null) {
				sounds[soundId] = new GwtMusic(soundFile);
			}
			sound = sounds[soundId];
			sound.stop();
			sound.setPan(pan, volume);
			sound.setLooping(loop);
			sound.play();
		}
		return soundId;
	}

	@Override
	public long loop () {
		return play(1.0f, 1.0f, 0.0f, true);
	}

	@Override
	public long loop (float volume) {
		return play(volume, 1.0f, 0.0f, true);
	}
	
	@Override
	public long loop (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, true);
	}
	
	@Override
	public void stop () {
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
				sounds[i].stop();
		}
	}

	@Override
	public void dispose () {
		stop();
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
				sounds[i].dispose();
		}
		sounds = null;
	}

	@Override
	public void stop (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].stop();
	}

	@Override
	public void pause () {
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
				sounds[i].pause();
		}
	}
	
	@Override
	public void pause (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].pause();
	}
	
	@Override
	public void resume () {
		for (int i = 0; i < sounds.length; i++) {
			if (sounds[i] != null)
				sounds[i].resume();
		}
	}
	
	@Override
	public void resume (long soundId) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].resume();
	}
	
	@Override
	public void setLooping (long soundId, boolean looping) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].setLooping(looping);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		// FIXME - Not possible?
	}

	@Override
	public void setVolume (long soundId, float volume) {
		if (soundId >= 0 && sounds[(int)soundId] != null)
			sounds[(int)soundId].setVolume(volume);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		if (soundId >= 0 && sounds[(int)soundId] != null) {
			sounds[(int)soundId].setPan(pan, volume);
		}
	}
}