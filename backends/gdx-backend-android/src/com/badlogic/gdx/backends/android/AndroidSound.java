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

import android.media.AudioManager;
import android.media.SoundPool;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.IntArray;

final class AndroidSound implements Sound {
	final SoundPool soundPool;
	final AudioManager manager;
	final int soundId;
	final IntArray streamIds = new IntArray(8);

	AndroidSound (SoundPool pool, AudioManager manager, int soundId) {
		this.soundPool = pool;
		this.manager = manager;
		this.soundId = soundId;
	}

	@Override
	public void dispose () {
		soundPool.unload(soundId);
	}

	@Override
	public long play () {
		return play(1);
	}

	@Override
	public long play (float volume) {
		if (streamIds.size == 8) streamIds.pop();
		int streamId = soundPool.play(soundId, volume, volume, 1, 0, 1);
		// standardise error code with other backends
		if (streamId == 0) return -1;
		streamIds.insert(0, streamId);
		return streamId;
	}

	public void stop () {
		for (int i = 0, n = streamIds.size; i < n; i++)
			soundPool.stop(streamIds.get(i));
	}

	@Override
	public void stop (long soundId) {
		soundPool.stop((int)soundId);
	}

	@Override
	public void pause () {
		soundPool.autoPause();
	}

	@Override
	public void pause (long soundId) {
		soundPool.pause((int)soundId);
	}

	@Override
	public void resume () {
		soundPool.autoResume();
	}

	@Override
	public void resume (long soundId) {
		soundPool.resume((int)soundId);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		soundPool.setRate((int)soundId, pitch);
	}

	@Override
	public void setVolume (long soundId, float volume) {
		soundPool.setVolume((int)soundId, volume, volume);
	}

	@Override
	public long loop () {
		return loop(1);
	}

	@Override
	public long loop (float volume) {
		if (streamIds.size == 8) streamIds.pop();
		int streamId = soundPool.play(soundId, volume, volume, 1, -1, 1);
		// standardise error code with other backends
		if (streamId == 0) return -1;
		streamIds.insert(0, streamId);
		return streamId;
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		int streamId = (int)soundId;

		soundPool.pause(streamId);
		soundPool.setLoop(streamId, looping ? -1 : 0);
		soundPool.resume(streamId);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		float leftVolume = volume;
		float rightVolume = volume;

		if (pan < 0) {
			rightVolume *= (1 - Math.abs(pan));
		} else if (pan > 0) {
			leftVolume *= (1 - Math.abs(pan));
		}

		soundPool.setVolume((int)soundId, leftVolume, rightVolume);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		if (streamIds.size == 8) streamIds.pop();
		float leftVolume = volume;
		float rightVolume = volume;
		if (pan < 0) {
			rightVolume *= (1 - Math.abs(pan));
		} else if (pan > 0) {
			leftVolume *= (1 - Math.abs(pan));
		}
		int streamId = soundPool.play(soundId, leftVolume, rightVolume, 1, 0, pitch);
		// standardise error code with other backends
		if (streamId == 0) return -1;
		streamIds.insert(0, streamId);
		return streamId;
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		if (streamIds.size == 8) streamIds.pop();
		float leftVolume = volume;
		float rightVolume = volume;
		if (pan < 0) {
			rightVolume *= (1 - Math.abs(pan));
		} else if (pan > 0) {
			leftVolume *= (1 - Math.abs(pan));
		}
		int streamId = soundPool.play(soundId, leftVolume, rightVolume, 1, -1, pitch);
		// standardise error code with other backends
		if (streamId == 0) return -1;
		streamIds.insert(0, streamId);
		return streamId;
	}
}
