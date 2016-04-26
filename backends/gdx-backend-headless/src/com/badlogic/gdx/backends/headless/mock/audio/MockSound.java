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

import com.badlogic.gdx.audio.Sound;

/** The headless backend does its best to mock elements. This is intended to make code-sharing between
 * server and client as simple as possible.
 */
public class MockSound implements Sound {
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
}
