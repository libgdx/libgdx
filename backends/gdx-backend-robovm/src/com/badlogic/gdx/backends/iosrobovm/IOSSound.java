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

import org.robovm.apple.foundation.NSArray;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.iosrobovm.objectal.ALBuffer;
import com.badlogic.gdx.backends.iosrobovm.objectal.ALChannelSource;
import com.badlogic.gdx.backends.iosrobovm.objectal.ALSource;
import com.badlogic.gdx.backends.iosrobovm.objectal.OALSimpleAudio;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.IntArray;

/** @author tescott
 *  @author Tomski
 * 
 *         First pass at implementing OALSimpleAudio support. */
public class IOSSound implements Sound {

	private ALBuffer soundBuffer;
	private String soundPath;
	
	private ALChannelSource channel;
	private NSArray<ALSource> sourcePool;
	private IntArray streamIds = new IntArray(8);
	
	public IOSSound (FileHandle filePath) {
		soundPath = filePath.file().getPath().replace('\\', '/');
		soundBuffer = OALSimpleAudio.sharedInstance().preloadEffect(soundPath);
		channel = OALSimpleAudio.sharedInstance().getChannelSource();
		sourcePool = channel.getSourcePool().getSources();
	}

	@Override
	public long play () {
		return play(1, 1, 0, false);
	}

	@Override
	public long play (float volume) {
		return play(volume, 1, 0, false);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, false);
	}

	public long play (float volume, float pitch, float pan, boolean loop) {
		if (streamIds.size == 8) streamIds.pop();
		ALSource soundSource = OALSimpleAudio.sharedInstance().playBuffer(soundBuffer, volume, pitch, pan, loop);
		if (soundSource == null) return -1;
		if (soundSource.getSourceId() == -1) return -1;
		streamIds.insert(0, soundSource.getSourceId());
		return soundSource.getSourceId();
	}

	@Override
	public long loop () {
		return play(1, 1, 0, true);
	}

	@Override
	public long loop (float volume) {
		return play(volume, 1, 0, true);
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, true);
	}

	@Override
	public void stop () {
		ALSource source;
		for (int i = 0; i < streamIds.size; i++) {
			if ((source = getSoundSource(streamIds.get(i))) != null) source.stop();
		}
	}

	@Override
	public void dispose () {
		stop();
		OALSimpleAudio.sharedInstance().unloadEffect(soundPath);
	}

	@Override
	public void stop (long soundId) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.stop();
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.setLooping(looping);
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.setPitch(pitch);
	}

	@Override
	public void setVolume (long soundId, float volume) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.setVolume(volume);
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) {
			source.setPan(pan);
			source.setVolume(volume);
		}
	}

	@Override
	public void pause () {
		ALSource source;
		for (int i = 0; i < streamIds.size; i++) {
			if ((source = getSoundSource(streamIds.get(i))) != null) source.setPaused(true);
		}
	}

	@Override
	public void resume () {
		ALSource source;
		for (int i = 0; i < streamIds.size; i++) {
			if ((source = getSoundSource(streamIds.get(i))) != null) source.setPaused(false);
		}
	}

	@Override
	public void pause (long soundId) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.setPaused(true);
	}

	@Override
	public void resume (long soundId) {
		ALSource source;
		if ((source = getSoundSource(soundId)) != null) source.setPaused(false);
	}
	
	private ALSource getSoundSource (long soundId) {	
		for (ALSource source : sourcePool) {
			if (source.getSourceId() == soundId) return source;			
		}
		return null;
	}
}
