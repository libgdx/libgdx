/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.tests.desktop;

import javax.sound.sampled.LineUnavailableException;

import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.backends.desktop.JoglAudioDevice;
import com.badlogic.gdx.backends.desktop.JoglAudioRecorder;

public class SimpleAudioRecorder {
	public static void main (String[] argv) throws LineUnavailableException {
		AudioDevice device = new JoglAudioDevice(true);
		AudioRecorder recorder = new JoglAudioRecorder(44100, true);

		short[] samples = new short[1024];

		while (true) {
			recorder.read(samples, 0, samples.length);
			device.writeSamples(samples, 0, samples.length);
		}
	}
}
