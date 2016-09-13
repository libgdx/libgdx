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

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class GwtAudio implements Audio {
	private WebAudioAPIManager webAudioAPIManager = null;

	public GwtAudio (GwtApplicationConfiguration config) {
		webAudioAPIManager = new WebAudioAPIManager(config);
	}

	@Override
	public AudioDevice newAudioDevice (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("AudioDevice not supported by GWT backend");
	}

	@Override
	public AudioRecorder newAudioRecorder (int samplingRate, boolean isMono) {
		throw new GdxRuntimeException("AudioRecorder not supported by GWT backend");
	}

	@Override
	public Sound newSound (FileHandle fileHandle) {
		return WebAudioAPIManager.isSupported() ? webAudioAPIManager.createSound(fileHandle) : new Sound() {
			@Override
			public long play() {
				return 0;
			}

			@Override
			public long play(float volume) {
				return 0;
			}

			@Override
			public long play(float volume, float pitch, float pan) {
				return 0;
			}

			@Override
			public long loop() {
				return 0;
			}

			@Override
			public long loop(float volume) {
				return 0;
			}

			@Override
			public long loop(float volume, float pitch, float pan) {
				return 0;
			}

			@Override
			public void stop() {

			}

			@Override
			public void pause() {

			}

			@Override
			public void resume() {

			}

			@Override
			public void dispose() {

			}

			@Override
			public void stop(long soundId) {

			}

			@Override
			public void pause(long soundId) {

			}

			@Override
			public void resume(long soundId) {

			}

			@Override
			public void setLooping(long soundId, boolean looping) {

			}

			@Override
			public void setPitch(long soundId, float pitch) {

			}

			@Override
			public void setVolume(long soundId, float volume) {

			}

			@Override
			public void setPan(long soundId, float pan, float volume) {

			}
		};
	}

	@Override
	public Music newMusic (FileHandle file) {
		return WebAudioAPIManager.isSupported() ? webAudioAPIManager.createMusic(file) : new Music() {
			@Override
			public void play() {

			}

			@Override
			public void pause() {

			}

			@Override
			public void stop() {

			}

			@Override
			public boolean isPlaying() {
				return false;
			}

			@Override
			public void setLooping(boolean isLooping) {

			}

			@Override
			public boolean isLooping() {
				return false;
			}

			@Override
			public void setVolume(float volume) {

			}

			@Override
			public float getVolume() {
				return 0;
			}

			@Override
			public void setPan(float pan, float volume) {

			}

			@Override
			public void setPosition(float position) {

			}

			@Override
			public float getPosition() {
				return 0;
			}

			@Override
			public void dispose() {

			}

			@Override
			public void setOnCompletionListener(OnCompletionListener listener) {

			}
		};
	}
}
