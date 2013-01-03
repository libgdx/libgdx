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

package com.badlogic.gdx.backends.ios;

import cli.objectal.ALBuffer;
import cli.objectal.ALSource;
import cli.objectal.OALSimpleAudio;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

/** @author tescott
 * 
 *         First pass at implementing OALSimpleAudio support. */
public class IOSObjectALSound implements Sound {

	private ALSource soundSource;
	private ALBuffer soundBuffer;
	private String soundPath;

	public IOSObjectALSound (FileHandle filePath) {
		soundPath = filePath.path();
		soundBuffer = OALSimpleAudio.sharedInstance().preloadEffect(soundPath);
	}

	@Override
	public long play () {
		return play(1, 1, 1, false);
	}

	@Override
	public long play (float volume) {
		return play(volume, 1, 1, false);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, false);
	}

	public long play (float volume, float pitch, float pan, boolean loop) {
		soundSource = OALSimpleAudio.sharedInstance().playEffect(soundPath, volume, pitch, pan, loop);
		return 0;
	}

	@Override
	public long loop () {
		return play(1, 1, 1, true);
	}

	@Override
	public long loop (float volume) {
		return play(volume, 1, 1, true);
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, true);
	}

	@Override
	public void stop () {
		if (soundSource != null) soundSource.stop();
	}

	@Override
	public void dispose () {
		OALSimpleAudio.sharedInstance().unloadEffect(soundPath);
	}

	@Override
	public void stop (long soundId) {
		// we should do something to give an id for each sound.
		stop();
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVolume (long soundId, float volume) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPriority (long soundId, int priority) {
		// TODO Auto-generated method stub

	}
}
