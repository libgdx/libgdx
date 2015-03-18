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

package com.badlogic.gdx.backends.headless.mock.audio;

import com.badlogic.gdx.audio.AudioDevice;

/** The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockAudioDevice implements AudioDevice {

	@Override
	public boolean isMono() {
		return false;
	}

	@Override
	public void writeSamples(short[] samples, int offset, int numSamples) {

	}

	@Override
	public void writeSamples(float[] samples, int offset, int numSamples) {

	}

	@Override
	public int getLatency() {
		return 0;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void setVolume(float volume) {

	}
}
