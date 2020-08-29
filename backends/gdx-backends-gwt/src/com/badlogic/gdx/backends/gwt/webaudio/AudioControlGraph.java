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

import com.badlogic.gdx.utils.Pool.Poolable;
import com.google.gwt.core.client.JavaScriptObject;

public class AudioControlGraph {
	private final JavaScriptObject audioContext;
	private JavaScriptObject destinationNode;

	private JavaScriptObject gainNode;
	private JavaScriptObject panNode;

	public AudioControlGraph (JavaScriptObject audioContext, JavaScriptObject destinationNode) {
		this.audioContext = audioContext;
		this.destinationNode = destinationNode;

		setupAudoGraph();
	}

	public native JavaScriptObject setupAudoGraph () /*-{
		// Get the Java values here, for readability
		var audioContext = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::audioContext;

		// Not all browser engines are created equal, stereo panning is not always available.
		var panNode = null;
		if (audioContext.createStereoPanner) {
			panNode = audioContext.createStereoPanner();
			panNode.pan.value = 0;
			this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::panNode = panNode;
		}

		// Setup a volume node for the sound. Enables volume change during playback.
		var gainNode = null;
		if (audioContext.createGain)
			// Standard compliant
			gainNode = audioContext.createGain();
		else
			// Old WebKit/iOS
			gainNode = audioContext.createGainNode();
		this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::gainNode = gainNode;
		gainNode.gain.value = 1;

		if (panNode) {
			panNode.connect(gainNode);
		}

		gainNode
				.connect(this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::destinationNode);

	}-*/;

	public native JavaScriptObject setSourceJSNI (JavaScriptObject sourceNode) /*-{
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::gainNode;
		var panNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::panNode;

		// Setup the audio graph, based on the supported features
		if (panNode) {
			sourceNode.connect(panNode);
		} else {
			sourceNode.connect(gainNode);
		}
	}-*/;

	public void setVolume (float volume) {
		setVolumeJSNI(volume);
	}

	public float getVolume () {
		return getVolumeJSNI();
	}

	public native JavaScriptObject setVolumeJSNI (float volume) /*-{
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::gainNode;
		gainNode.gain.value = volume;
	}-*/;

	public native float getVolumeJSNI () /*-{
		var gainNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::gainNode;
		return gainNode.gain.value;
	}-*/;

	public void setPan (float pan) {
		setPanJSNI(pan);
	}

	public float getPan () {
		return getPanJSNI();
	}

	public native JavaScriptObject setPanJSNI (float pan) /*-{
		var panNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::panNode;
		if (panNode)
			panNode.pan.value = pan;
	}-*/;

	public native float getPanJSNI () /*-{
		var panNode = this.@com.badlogic.gdx.backends.gwt.webaudio.AudioControlGraph::panNode;
		if (panNode)
			return panNode.pan.value;
		return 0;
	}-*/;

	public void setSource (JavaScriptObject sourceNode) {
		setSourceJSNI(sourceNode);
	}
}
