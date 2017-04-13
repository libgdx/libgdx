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
		if (config.preferWebAudioAPI && WebAudioAPIManager.isSupported()) {
			webAudioAPIManager = new WebAudioAPIManager(config);
		}
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
		if (webAudioAPIManager != null) {
			return webAudioAPIManager.createSound(fileHandle);
		} else {
			/* Use the SoundManager2 based implementation that uses the HTML5 Audio element or Flash, based on users preferences */
			return new GwtSound(fileHandle);
		}
	}

	@Override
	public Music newMusic (FileHandle file) {
		if (webAudioAPIManager != null) {
			return webAudioAPIManager.createMusic(file);
		} else {
			/* Use the SoundManager2 based implementation that uses the HTML5 Audio element or Flash, based on users preferences */
			return new GwtMusic(file);
		}
	}
}
