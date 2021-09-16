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

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.google.gwt.core.client.JavaScriptObject;

/** Implementation of the {@link Sound} interface for GWT, using the Web Audio API (
 * <a href="https://www.w3.org/TR/webaudio/">https://www.w3.org/TR/webaudio/</a>).
 * 
 * @author barkholt */
public class WebAudioAPISound implements Sound {
	// JavaScript object that is the base object of the Web Audio API
	private final JavaScriptObject audioContext;

	// JavaScript AudioNode representing the final destination of the sound. Typically the speakers of whatever device we are
	// running on.
	private final JavaScriptObject destinationNode;

	// Maps from integer keys to active sounds. Both the AudioBufferSourceNode and the associated AudioControlGraph are stored for
	// quick access
	private final IntMap<JavaScriptObject> activeSounds;
	private final IntMap<AudioControlGraph> activeAudioControlGraphs;

	// The raw sound data of this sound, which will be fed into the audio nodes
	private JavaScriptObject audioBuffer;

	// Key generator for sound objects.
	private int nextKey = 0;

	// We use a pool of AudioControlGraphs in order to minimize object creation
	private AudioControlGraphPool audioGraphPool;

	/** @param audioContext The JavaScript AudioContext object that servers as the base object of the Web Audio API
	 * @param destinationNode The JavaScript AudioNode to route all the sound output to
	 * @param audioGraphPool A Pool that allows us to create AudioControlGraphs efficiently */
	public WebAudioAPISound (JavaScriptObject audioContext, JavaScriptObject destinationNode,
		AudioControlGraphPool audioGraphPool) {
		this.audioContext = audioContext;
		this.destinationNode = destinationNode;
		this.audioGraphPool = audioGraphPool;
		this.activeSounds = new IntMap<JavaScriptObject>();
		this.activeAudioControlGraphs = new IntMap<AudioControlGraph>();
	}

	/** Set the buffer containing the actual sound data
	 * @param audioBuffer */
	public void setAudioBuffer (JavaScriptObject audioBuffer) {
		this.audioBuffer = audioBuffer;

		// If play-back of sounds have been requested before we were ready, do a pause/resume to get sound flowing
		Keys keys = activeSounds.keys();
		while (keys.hasNext) {
			int key = keys.next();
			pause(key);
			resume(key, 0f);
		}
	}

	protected long play (float volume, float pitch, float pan, boolean loop) {
		// if the sound system is not yet unlocked, skip playing the sound.
		// otherwise, it is played when the user makes his first input
		if (!WebAudioAPIManager.isSoundUnlocked() && WebAudioAPIManager.isAudioContextLocked(audioContext)) return -1;

		// Get ourselves a fresh audio graph
		AudioControlGraph audioControlGraph = audioGraphPool.obtain();

		// Create the source node that will be feeding the audio graph
		JavaScriptObject audioBufferSourceNode = createBufferSourceNode(loop, pitch);

		// Configure the audio graph
		audioControlGraph.setSource(audioBufferSourceNode);
		audioControlGraph.setPan(pan);
		audioControlGraph.setVolume(volume);

		int myKey = nextKey++;

		// Start the playback
		playJSNI(audioBufferSourceNode, myKey, 0f);

		// Remember that we are playing
		activeSounds.put(myKey, audioBufferSourceNode);
		activeAudioControlGraphs.put(myKey, audioControlGraph);

		return myKey;
	}

	private void soundDone (int key) {
		// The sound might have been removed by an explicit stop, before the sound reached its end
		if (activeSounds.containsKey(key)) {
			activeSounds.remove(key);
			audioGraphPool.free(activeAudioControlGraphs.remove(key));
		}
	}

	public native JavaScriptObject createBufferSourceNode (boolean loop, float pitch) /*-{
		// Get the Java values here, for readability
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::audioContext;
		var audioBuffer = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::audioBuffer;

		if (audioBuffer == null) {
			// If there is no audio buffer yet, we presume it is still loading, and instead play a short silent clip.
			audioBuffer = audioContext.createBuffer(2, 22050, 44100);
		}

		// Setup the basic audio source
		var source = audioContext.createBufferSource();
		source.buffer = audioBuffer;
		source.loop = loop;

		// Pitch change can cause resampling, so only do it if necessary
		if (pitch !== 1.0) {
			source.playbackRate.value = pitch;
		}

		return source;
	}-*/;

	public native JavaScriptObject playJSNI (JavaScriptObject source, int key, float startOffset) /*-{
		// Get the Java values here, for readability
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::audioContext;

		// Remember when we started playing this. This is needed for pause/resume.
		source.startTime = audioContext.currentTime;

		var self = this;
		// Listen for the end, in order to clean up
		source.onended = function() {
			self.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::soundDone(I)(key);
		};

		// First try the standards defined version of starting the sound
		if (typeof (source.start) !== "undefined")
			source.start(audioContext.currentTime, startOffset);
		else
			// Then try the older webkit (iOS) way
			source.noteOn(audioContext.currentTime, startOffset);

		return source;
	}-*/;

	public static native JavaScriptObject stopJSNI (JavaScriptObject audioBufferSourceNode) /*-{
		// First try the standards defined version of stopping the sound
		if (typeof (audioBufferSourceNode.stop) !== "undefined")
			audioBufferSourceNode.stop();
		else
			// Then try the older webkit (iOS) way
			audioBufferSourceNode.noteOff();
	}-*/;

	@Override
	public long play () {
		return play(1f);
	}

	@Override
	public long play (float volume) {
		return play(volume, 1f, 0f);
	}

	@Override
	public long play (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, false);
	}

	@Override
	public long loop () {
		return loop(1f);
	}

	@Override
	public long loop (float volume) {
		return loop(volume, 1f, 0f);
	}

	@Override
	public long loop (float volume, float pitch, float pan) {
		return play(volume, pitch, pan, true);
	}

	@Override
	public void stop () {
		Keys keys = activeSounds.keys();
		while (keys.hasNext) {
			int next = keys.next();
			stop(next);
		}
	}

	@Override
	public void pause () {
		Keys keys = activeSounds.keys();
		while (keys.hasNext) {
			pause(keys.next());
		}
	}

	@Override
	public void resume () {
		Keys keys = activeSounds.keys();
		while (keys.hasNext) {
			resume(keys.next());
		}
	}

	@Override
	public void dispose () {
		stop();
		activeSounds.clear();
	}

	@Override
	public void stop (long soundId) {
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			JavaScriptObject audioBufferSourceNode = activeSounds.remove(soundKey);
			stopJSNI(audioBufferSourceNode);

			audioGraphPool.free(activeAudioControlGraphs.remove(soundKey));
		}
	}

	@Override
	public void pause (long soundId) {
		// Record our current position, and then stop
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			JavaScriptObject audioBufferSourceNode = activeSounds.get(soundKey);

			// The API has no concept of pause/resume, so we do it by recording a pause time stamp, and then stopping the sound. On
			// resume we play the
			// sound again, starting from a calculated offset.
			pauseJSNI(audioBufferSourceNode);
			stopJSNI(audioBufferSourceNode);
		}
	}

	@Override
	public void resume (long soundId) {
		resume(soundId, null);
	}

	private void resume (long soundId, Float from) {
		// Start from previous paused position
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			JavaScriptObject audioBufferSourceNode = activeSounds.remove(soundKey);
			AudioControlGraph audioControlGraph = activeAudioControlGraphs.get(soundKey);

			boolean loop = getLoopingJSNI(audioBufferSourceNode);
			float pitch = getPitchJSNI(audioBufferSourceNode);
			float resumeOffset = getResumeOffsetJSNI(audioBufferSourceNode);

			if (from != null) resumeOffset = from;

			// These things can not be re-used. One play only, as dictated by the Web Audio API
			JavaScriptObject newAudioBufferSourceNode = createBufferSourceNode(loop, pitch);
			audioControlGraph.setSource(newAudioBufferSourceNode);
			activeSounds.put(soundKey, newAudioBufferSourceNode);

			playJSNI(newAudioBufferSourceNode, soundKey, resumeOffset);
		}
	}

	@Override
	public void setLooping (long soundId, boolean looping) {
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			JavaScriptObject sound = activeSounds.get(soundKey);
			setLoopingJSNI(sound, looping);
		}
	}

	@Override
	public void setPitch (long soundId, float pitch) {
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			JavaScriptObject sound = activeSounds.get(soundKey);
			setPitchJSNI(sound, pitch);
		}
	}

	@Override
	public void setVolume (long soundId, float volume) {
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			AudioControlGraph audioControlGraph = activeAudioControlGraphs.get(soundKey);
			audioControlGraph.setVolume(volume);
		}
	}

	@Override
	public void setPan (long soundId, float pan, float volume) {
		int soundKey = (int)soundId;
		if (activeSounds.containsKey(soundKey)) {
			AudioControlGraph audioControlGraph = activeAudioControlGraphs.get(soundKey);
			audioControlGraph.setPan(pan);
			audioControlGraph.setVolume(volume);
		}
	}

	public static native JavaScriptObject setPitchJSNI (JavaScriptObject audioBufferSourceNode, float pitch) /*-{
		audioBufferSourceNode.playbackRate.value = pitch;
	}-*/;

	public static native float getPitchJSNI (JavaScriptObject audioBufferSourceNode) /*-{
		return audioBufferSourceNode.playbackRate.value;
	}-*/;

	public static native JavaScriptObject setLoopingJSNI (JavaScriptObject audioBufferSourceNode, boolean looping) /*-{
		audioBufferSourceNode.loop = looping;
	}-*/;

	public static native boolean getLoopingJSNI (JavaScriptObject audioBufferSourceNode) /*-{
		return audioBufferSourceNode.loop;
	}-*/;

	public native JavaScriptObject pauseJSNI (JavaScriptObject audioBufferSourceNode) /*-{
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPISound::audioContext;
		audioBufferSourceNode.pauseTime = audioContext.currentTime;
	}-*/;

	public static native float getResumeOffsetJSNI (JavaScriptObject audioBufferSourceNode) /*-{
		return audioBufferSourceNode.pauseTime
				- audioBufferSourceNode.startTime;
	}-*/;
}
