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

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.google.gwt.user.client.Timer;

public class DefaultGwtAudio implements GwtAudio {
	private WebAudioAPIManager webAudioAPIManager = null;

	private String[] availableOutputDevices = new String[] {};

	public DefaultGwtAudio () {
		webAudioAPIManager = new WebAudioAPIManager();

		getUserMedia();
		Timer observer = new Timer() {
			@Override
			public void run () {
				fetchAvailableOutputDevices(new DeviceListener() {
					@Override
					public void onDevicesChanged (String[] devices) {
						availableOutputDevices = devices;
					}
				});
			}
		};
		observer.scheduleRepeating(1000);
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
		return webAudioAPIManager.createSound(fileHandle);
	}

	@Override
	public Music newMusic (FileHandle file) {
		return webAudioAPIManager.createMusic(file);
	}

	@Override
	public boolean switchOutputDevice (String deviceIdentifier) {
		String[] features = GwtFeaturePolicy.features();
		if (features == null || !Array.with(features).contains("speaker-selection", false)
			|| GwtFeaturePolicy.allowsFeature("speaker-selection")) {
			if (deviceIdentifier == null || deviceIdentifier.equals("default")) {
				deviceIdentifier = ""; // Empty = default
			}
			webAudioAPIManager.setSinkId(deviceIdentifier);
			return true;
		}
		return false;
	}

	@Override
	public String[] getAvailableOutputDevices () {
		return availableOutputDevices;
	}

	private native void getUserMedia () /*-{
		navigator.mediaDevices.getUserMedia({ audio: true });
	}-*/;

	private native void fetchAvailableOutputDevices (DeviceListener listener) /*-{
		navigator.mediaDevices
			.enumerateDevices()
			.then(function(devices) {
				var dev = devices.filter(function(device) {
					return device.deviceId && device.kind === 'audiooutput' && device.deviceId !== 'default';
				})
					.map(function(device) {
						return device.deviceId;
					});
                listener.@com.badlogic.gdx.backends.gwt.DefaultGwtAudio.DeviceListener::onDevicesChanged([Ljava/lang/String;)(@com.badlogic.gdx.backends.gwt.GwtUtils::toStringArray(Lcom/google/gwt/core/client/JsArrayString;)(dev));
			});
	}-*/;

	private interface DeviceListener {
		void onDevicesChanged (String[] devices);
	}
}
