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
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;

public class AudioRecorderTest extends GdxTest {
	short[] samples = new short[1024 * 4];
	AudioDevice device;
	AudioRecorder recorder;

	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create () {
		device = Gdx.audio.newAudioDevice(44100, true);
		recorder = Gdx.audio.newAudioRecorder(44100, true, false);

		Thread t = new Thread(new Runnable() {

			@Override
			public void run () {
				while (true) {
					recorder.read(samples, 0, samples.length);
					device.writeSamples(samples, 0, samples.length);
				}
			}
		});
		t.setDaemon(true);
		t.start();

		batch = new SpriteBatch();
		font = new BitmapFont();
		font.getData().setScale(Gdx.graphics.getDensity() * 2);
	}
boolean touched = false;
	@Override
	public void render () {
		if (Gdx.input.isTouched() && !touched) {
			recorder.requestPermission();
			touched = true;
		}

		ScreenUtils.clear(Color.BLACK);
		batch.begin();
		font.draw(batch, recorder.hasPermission().toString(), 50, Gdx.graphics.getBackBufferHeight() - 50);
		batch.end();
	}

	@Override
	public void pause () {
		device.dispose();
		recorder.dispose();
	}

	@Override
	public void resume () {
		device = Gdx.audio.newAudioDevice(44100, true);
		recorder = Gdx.audio.newAudioRecorder(44100, true);
	}
}
