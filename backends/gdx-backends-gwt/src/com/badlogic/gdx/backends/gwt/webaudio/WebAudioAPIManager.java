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

package com.badlogic.gdx.backends.gwt.webaudio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.gwt.GwtFileHandle;
import com.badlogic.gdx.backends.gwt.preloader.AssetDownloader;
import com.badlogic.gdx.files.FileHandle;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.media.client.Audio;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.google.gwt.xhr.client.XMLHttpRequest.ResponseType;

public class WebAudioAPIManager implements LifecycleListener {
	private final JavaScriptObject audioContext;
	private final JavaScriptObject globalVolumeNode;
	private final AssetDownloader assetDownloader;
	private final AudioControlGraphPool audioControlGraphPool;
	private static boolean soundUnlocked;

	public WebAudioAPIManager () {
		this.assetDownloader = new AssetDownloader();
		this.audioContext = createAudioContextJSNI();
		this.globalVolumeNode = createGlobalVolumeNodeJSNI();
		this.audioControlGraphPool = new AudioControlGraphPool(audioContext, globalVolumeNode);

		// for automatically muting/unmuting on pause/resume
		Gdx.app.addLifecycleListener(this);

		/*
		 * The Web Audio API is blocked on many platforms until the developer triggers the first sound playback using the API. But
		 * it MUST happen as a direct result of a few specific input events. This is a major point of confusion for developers new
		 * to the platform. Here we attach event listeners to the graphics canvas in order to unlock the sound system on the first
		 * input event. On the event, we play a silent sample, which should unlock the sound - on platforms where it is not
		 * necessary the effect should not be noticeable (i.e. we play silence). As soon as the attempt to unlock has been
		 * performed, we remove all the event listeners.
		 */
		if (isAudioContextLocked(audioContext))
			hookUpSoundUnlockers();
		else
			setUnlocked();
	}

	public native void hookUpSoundUnlockers () /*-{
		var self = this;
		var audioContext = self.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::audioContext;
		
		// An array of various user interaction events we should listen for
		var userInputEventNames = [
			'click', 'contextmenu', 'auxclick', 'dblclick', 'mousedown',
			'mouseup', 'pointerup', 'touchend', 'keydown', 'keyup', 'touchstart'
		];

		var unlock = function(e) {
			
			// resume audio context if it was suspended. It's only required for musics since sounds automatically resume
			// audio context when started.
			audioContext.resume();
			
			self.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::setUnlocked()();

			userInputEventNames.forEach(function (eventName) {
				$doc.removeEventListener(eventName, unlock);
			});

		};

		userInputEventNames.forEach(function (eventName) {
			$doc.addEventListener(eventName, unlock);
		});
	}-*/;

	public void setUnlocked () {
		Gdx.app.log("Webaudio", "Audiocontext unlocked");
		soundUnlocked = true;
	}

	public static boolean isSoundUnlocked () {
		return soundUnlocked;
	}

	static native boolean isAudioContextLocked (JavaScriptObject audioContext) /*-{
		return audioContext.state !== 'running';
	}-*/;

	/** Older browsers do not support the Web Audio API. This is where we find out.
	 * 
	 * @return is the WebAudioAPI available in this browser? */
	public static native boolean isSupported () /*-{
		return typeof (window.AudioContext || window.webkitAudioContext) != "undefined";
	}-*/;

	private static native JavaScriptObject createAudioContextJSNI () /*-{
		var AudioContext = window.AudioContext || window.webkitAudioContext;
		if (AudioContext) {
			var audioContext = new AudioContext();
			return audioContext;
		}
		return null;
	}-*/;

	private native JavaScriptObject createGlobalVolumeNodeJSNI () /*-{
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::audioContext;

		var gainNode = null;
		if (audioContext.createGain)
			// Standard compliant
			gainNode = audioContext.createGain();
		else
			// Old WebKit/iOS
			gainNode = audioContext.createGainNode();

		// Default to full, unmuted volume
		gainNode.gain.value = 1.0;

		// Connect the global volume to the speakers. This will be the last part of our audio graph.
		gainNode.connect(audioContext.destination);

		return gainNode;
	}-*/;

	private native void disconnectJSNI () /*-{
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::audioContext;
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::globalVolumeNode;

		gainNode.disconnect(audioContext.destination);
	}-*/;

	private native void connectJSNI () /*-{
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::audioContext;
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::globalVolumeNode;

		gainNode.connect(audioContext.destination);
	}-*/;

	public JavaScriptObject getAudioContext () {
		return audioContext;
	}

	public Sound createSound (FileHandle fileHandle) {
		final WebAudioAPISound newSound = new WebAudioAPISound(audioContext, globalVolumeNode, audioControlGraphPool);

		String url = ((GwtFileHandle)fileHandle).getAssetUrl();

		XMLHttpRequest request = XMLHttpRequest.create();
		request.setOnReadyStateChange(new ReadyStateChangeHandler() {
			@Override
			public void onReadyStateChange (XMLHttpRequest xhr) {
				if (xhr.getReadyState() == XMLHttpRequest.DONE) {
					if (xhr.getStatus() != 200) {
					} else {
						Int8Array data = TypedArrays.createInt8Array(xhr.getResponseArrayBuffer());

						/*
						 * Start decoding the sound data. This is an asynchronous process, which is a bad fit for the libGDX API, which
						 * expects sound creation to be synchronous. The result is that sound won't actually start playing until the
						 * decoding is done.
						 */
						decodeAudioData(getAudioContext(), data.buffer(), newSound);
					}
				}
			}
		});
		request.open("GET", url);
		request.setResponseType(ResponseType.ArrayBuffer);
		request.send();

		return newSound;
	}

	public Music createMusic (FileHandle fileHandle) {
		String url = ((GwtFileHandle)fileHandle).getAssetUrl();

		Audio audio = Audio.createIfSupported();
		audio.setSrc(url);

		WebAudioAPIMusic music = new WebAudioAPIMusic(audioContext, audio, audioControlGraphPool);

		return music;
	}

	public static native void decodeAudioData (JavaScriptObject audioContextIn, ArrayBuffer audioData,
		WebAudioAPISound targetSound) /*-{
		audioContextIn
				.decodeAudioData(
						audioData,
						function(buffer) {
							targetSound.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::setAudioBuffer(Lcom/google/gwt/core/client/JavaScriptObject;)(buffer);
						}, function() {
							console.log("Error: decodeAudioData");
						});
	}-*/;

	@Override
	public void pause () {
		// As the web application looses focus, we mute the sound
		disconnectJSNI();
	}

	@Override
	public void resume () {
		// As the web application regains focus, we unmute the sound
		connectJSNI();
	}

	public void setGlobalVolume (float volume) {
		setGlobalVolumeJSNI(volume);
	}

	public native JavaScriptObject setGlobalVolumeJSNI (float volume) /*-{
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIManager::globalVolumeNode;
		gainNode.gain.value = volume;
	}-*/;

	@Override
	public void dispose () {
	}
}
