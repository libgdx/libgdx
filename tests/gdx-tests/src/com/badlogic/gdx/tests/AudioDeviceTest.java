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

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.tests.utils.GdxTest;

public class AudioDeviceTest extends GdxTest {
	Thread thread;
	boolean stop = false;

	@Override
	public void create () {
		if (thread == null) {
			final AudioDevice device = Gdx.app.getAudio().newAudioDevice(44100, false);
			thread = new Thread(new Runnable() {
				@Override
				public void run () {
					final float frequency = 440;
					float increment = (float)(2 * Math.PI) * frequency / 44100; // angular increment for each sample
					float angle = 0;
					float samples[] = new float[1024];

					while (!stop) {
						for (int i = 0; i < samples.length; i += 2) {
							samples[i] = 0.5f * (float)Math.sin(angle);
							samples[i + 1] = 2 * samples[i];
							angle += increment;
						}

						device.writeSamples(samples, 0, samples.length);
					}

					device.dispose();
				}
			});
			thread.start();
		}
	}

	@Override
	public void dispose () {
		stop = true;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
