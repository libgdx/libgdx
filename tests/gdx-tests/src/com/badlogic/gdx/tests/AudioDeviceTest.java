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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class AudioDeviceTest extends GdxTest {
	Thread thread;
	boolean stop = false;
	Stage ui;
	Skin skin;
	float wavePanValue = 0;

	@Override
	public void create () {

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		ui = new Stage(new FitViewport(640, 400));

		Table table = new Table(skin);
		final Slider pan = new Slider(-1f, 1f, 0.1f, false, skin);
		pan.setValue(0);
		final Label panValue = new Label("0.0", skin);
		table.setFillParent(true);
		table.add("Pan");
		table.add(pan);
		table.add(panValue).width(100);

		ui.addActor(table);

		Gdx.input.setInputProcessor(ui);

		pan.addListener(new ChangeListener() {
			public void changed (ChangeEvent event, Actor actor) {
				wavePanValue = pan.getValue();
				panValue.setText("" + pan.getValue());
			}
		});

		if (thread == null) {
			final int samplingFrequency = 44100;
			final AudioDevice device = Gdx.app.getAudio().newAudioDevice(samplingFrequency, false);
			thread = new Thread(new Runnable() {
				@Override
				public void run () {
					final float waveFrequency = 440;
					float samples[] = new float[1024];
					long playedFrames = 0;
					while (!stop) {
						for (int i = 0; i < samples.length; i += 2) {
							float time = (float)playedFrames / (float)samplingFrequency;
							float wave = (float)Math.sin(time * waveFrequency * Math.PI * 2.0);
							float pan = wavePanValue * .5f + .5f;
							samples[i] = wave * (1 - pan);
							samples[i + 1] = wave * pan;
							playedFrames++;
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
	public void resize (int width, int height) {
		ui.getViewport().update(width, height, true);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		ui.act(Gdx.graphics.getDeltaTime());
		ui.draw();
	}

	@Override
	public void dispose () {
		ui.dispose();
		skin.dispose();
		stop = true;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
