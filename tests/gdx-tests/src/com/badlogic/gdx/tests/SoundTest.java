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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Slider.ValueChangedListener;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SoundTest extends GdxTest {
	Sound sound;
	float volume = 0.5f;
	long soundId = 0;
	Stage ui;

	BitmapFont font;
	SpriteBatch batch;

	@Override
	public void create () {
		sound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/shotgun.wav", FileType.Internal));

		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"), Gdx.files.internal("data/uiskin.png"));
		ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		Button play = new Button("Play", skin);
		Button stop = new Button("Stop", skin);
		final Slider pitch = new Slider(0.1f, 4, 0.1f, skin);
		pitch.setValue(1);
		final Label pitchValue = new Label("1.0", skin);
		final Slider volume = new Slider(0.1f, 1, 0.1f, skin);
		volume.setValue(1);
		final Label volumeValue = new Label("1.0", skin);
		Table table = new Table();
		final Slider pan = new Slider(-1f, 1f, 0.1f, skin);
		pan.setValue(0);
		final Label panValue = new Label("0.0", skin);
		table.width = Gdx.graphics.getWidth();
		table.height = Gdx.graphics.getHeight();

		table.align(Align.CENTER | Align.TOP);
		table.add(play);
		table.add(stop);
		table.row();
		table.add(new Label("Pitch", skin));
		table.add(pitch);
		table.add(pitchValue);
		table.row();
		table.add(new Label("Volume", skin));
		table.add(volume);
		table.add(volumeValue);
		table.row();
		table.add(new Label("Pan", skin));
		table.add(pan);
		table.add(panValue);
		ui.addActor(table);

		play.setClickListener(new ClickListener() {
			@Override
			public void click (Actor actor, float x, float y) {
				soundId = sound.play(volume.getValue());
				sound.setPitch(soundId, pitch.getValue());
				sound.setPan(soundId, pan.getValue(), volume.getValue());
			}
		});

		stop.setClickListener(new ClickListener() {
			@Override
			public void click (Actor actor, float x, float y) {
				sound.stop(soundId);
			}
		});
		pitch.setValueChangedListener(new ValueChangedListener() {
			@Override
			public void changed (Slider slider, float value) {
				sound.setPitch(soundId, value);
				pitchValue.setText("" + value);
			}
		});
		volume.setValueChangedListener(new ValueChangedListener() {
			@Override
			public void changed (Slider slider, float value) {
				sound.setVolume(soundId, value);
				volumeValue.setText("" + value);
			}
		});
		pan.setValueChangedListener(new ValueChangedListener() {
			@Override
			public void changed (Slider slider, float value) {
				sound.setPan(soundId, value, volume.getValue());
				panValue.setText("" + value);
			}
		});
		Gdx.input.setInputProcessor(ui);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		ui.act(Gdx.graphics.getDeltaTime());
		ui.draw();
	}

	@Override
	public boolean needsGL20 () {
		return false;
	}
}
