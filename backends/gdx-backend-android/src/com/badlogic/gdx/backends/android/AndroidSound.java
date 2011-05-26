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

	@Override public void dispose () {
		soundPool.unload(soundId);
	}

	@Override public void play () {
		play(1);
	}

	@Override public void play (float volume) {
		if (streamIds.size == 8) streamIds.pop();
		streamIds.add(soundPool.play(soundId, volume, volume, 1, 0, 1));
	}

	public void stop () {
		for (int i = 0, n = streamIds.size; i < n; i++)
			soundPool.stop(streamIds.get(i));
	}
}
