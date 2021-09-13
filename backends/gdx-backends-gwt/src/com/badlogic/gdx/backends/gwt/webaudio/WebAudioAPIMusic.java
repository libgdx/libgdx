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

import com.badlogic.gdx.audio.Music;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.media.client.Audio;

public class WebAudioAPIMusic implements Music {
	// The Audio element to be streamed
	private final Audio audio;

	// Pool from which we will draw, and eventually return to, our AudioControlGraph from
	private final AudioControlGraphPool audioControlGraphPool;

	// The audio graph used to control pan and volume for this piece of music
	private final AudioControlGraph audioControlGraph;

	private boolean isPlaying;

	private OnCompletionListener onCompletionListener;

	public WebAudioAPIMusic (JavaScriptObject audioContext, Audio audio, AudioControlGraphPool audioControlGraphPool) {
		this.audio = audio;
		this.audioControlGraphPool = audioControlGraphPool;

		// Create AudioSourceNode from Audio element
		JavaScriptObject audioSourceNode = createMediaElementAudioSourceNode(audioContext, audio.getAudioElement());

		// Setup the sound graph to control pan and volume
		audioControlGraph = audioControlGraphPool.obtain();
		audioControlGraph.setSource(audioSourceNode);
	}

	public void ended () {
		if (this.onCompletionListener != null) this.onCompletionListener.onCompletion(this);
	}

	public native JavaScriptObject createMediaElementAudioSourceNode (JavaScriptObject audioContext, JavaScriptObject audioElement) /*-{
		var source = audioContext.createMediaElementSource(audioElement);
		var self = this;
		audioElement.addEventListener("ended", self.@com.badlogic.gdx.backends.gwt.webaudio.WebAudioAPIMusic::ended());
		return source;
	}-*/;

	@Override
	public void play () {
		audio.play();
		isPlaying = true;
	}

	@Override
	public void pause () {
		audio.pause();
		isPlaying = false;
	}

	@Override
	public void stop () {
		pause();
		audio.setCurrentTime(0);
	}

	@Override
	public boolean isPlaying () {
		return isPlaying;
	}

	@Override
	public void setLooping (boolean isLooping) {
		audio.setLoop(isLooping);
	}

	@Override
	public boolean isLooping () {
		return audio.isLoop();
	}

	@Override
	public void setVolume (float volume) {
		// Volume can be controlled on the Audio element, or as part of the audio graph. We do it as part of the graph to ensure we
		// use as much common
		// code as possible with the sound effect code.
		audioControlGraph.setVolume(volume);
	}

	@Override
	public float getVolume () {
		return audioControlGraph.getVolume();
	}

	@Override
	public void setPan (float pan, float volume) {
		audioControlGraph.setPan(pan);
		audioControlGraph.setVolume(volume);
	}

	@Override
	public void setPosition (float position) {
		audio.setCurrentTime(position);
	}

	@Override
	public float getPosition () {
		return (float)audio.getCurrentTime();
	}

	@Override
	public void dispose () {
		// Stop the music
		pause();

		// Tear down the audio graph
		audioControlGraphPool.free(audioControlGraph);
	}

	@Override
	public void setOnCompletionListener (OnCompletionListener listener) {
		this.onCompletionListener = listener;
	}
}
