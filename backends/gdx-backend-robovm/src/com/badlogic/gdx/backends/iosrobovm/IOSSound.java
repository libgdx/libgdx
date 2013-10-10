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

package com.badlogic.gdx.backends.iosrobovm;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.iosrobovm.objectal.ALBuffer;
import com.badlogic.gdx.backends.iosrobovm.objectal.ALSource;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import com.badlogic.gdx.files.FileHandle;

/** @author tescott
 * 
 *         First pass at implementing OALSimpleAudio support. */
public class IOSSound implements Sound {

	private ALSource soundSource;
	private ALBuffer soundBuffer;
	private String soundPath;

	public IOSSound (FileHandle filePath) {
		soundPath = filePath.file().getPath().replace('\\', '/');
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

	@Override
	public void pause () {
		if (soundSource != null) soundSource.setPaused(true);
	}

	@Override
	public void resume () {
		if (soundSource != null) soundSource.setPaused(false);
	}

	@Override
	public void pause (long soundId) {
		// we should do something to give an id for each sound.
		pause();
	}

	@Override
	public void resume (long soundId) {
		// we should do something to give an id for each sound.
		resume();
	}
}
