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
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class MusicTest extends GdxTest {

	Music music;
	float songDuration;
	float currentPosition;

	SpriteBatch batch;

	Stage stage;
	Label label;
	Slider slider;
	boolean sliderUpdating = false;
	SelectBox<Song> musicBox;
	TextButton btLoop;

	enum Song {
		MP3, OGG, WAV, MP3_CLOCK
	}

	float time;

	@Override
	public void create () {

		batch = new SpriteBatch();

		stage = new Stage(new ExtendViewport(600, 480));
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		Table sliderTable = new Table();
		label = new Label("", skin);
		slider = new Slider(0, 100, 0.1f, false, skin);
		sliderTable.add(slider).expand();
		sliderTable.add(label).left().width(60f);
		slider.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (!sliderUpdating && slider.isDragging()) music.setPosition((slider.getValue() / 100f) * songDuration);
			}
		});

		musicBox = new SelectBox<Song>(skin);
		musicBox.setItems(Song.values());
		musicBox.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				setSong(musicBox.getSelected());
			}
		});

		btLoop = new TextButton("loop", skin, "toggle");
		btLoop.setChecked(true);
		btLoop.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				if (music != null) music.setLooping(btLoop.isChecked());
			}
		});

		// Build buttons
		Table controlsTable = new Table();
		controlsTable.setSize(200f, 80f);
		Button playButton = new ImageButton(getDrawable("data/player_play.png"));
		Button pauseButton = new ImageButton(getDrawable("data/player_pause.png"));
		Button stopButton = new ImageButton(getDrawable("data/player_stop.png"));
		float buttonSize = 64f;
		controlsTable.add(playButton).size(buttonSize);
		controlsTable.add(pauseButton).size(buttonSize);
		controlsTable.add(stopButton).size(buttonSize);
		playButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				music.play();
				time = 0;
			}
		});
		pauseButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				music.pause();
			}
		});
		stopButton.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				music.stop();
			}
		});

		Table footerTable = new Table();
		footerTable.setSize(500f, 120f);
		footerTable.add(controlsTable);
		footerTable.add(sliderTable).width(250f);

		setSong(musicBox.getSelected());

		Table table = new Table(skin);
		table.add(musicBox);
		table.add(btLoop);
		table.setFillParent(true);
		stage.addActor(table);
		stage.addActor(footerTable);

		Gdx.input.setInputProcessor(stage);
	}

	void setSong (Song song) {
		if (music != null) {
			music.dispose();
		}
		switch (song) {
		default:
		case MP3_CLOCK:
			music = Gdx.audio.newMusic(Gdx.files.internal("data/60bpm.mp3"));
			songDuration = 5 * 60 + 4;
			break;
		case MP3:
			music = Gdx.audio.newMusic(Gdx.files.internal("data/8.12.mp3"));
			songDuration = 183;
			break;
		case OGG:
			music = Gdx.audio.newMusic(Gdx.files.internal("data/8.12.ogg"));
			songDuration = 183;
			break;
		case WAV:
			music = Gdx.audio.newMusic(Gdx.files.internal("data/8.12.loop.wav"));
			songDuration = 4;
			break;
		}
		music.setLooping(btLoop.isChecked());
		music.play();
		time = 0;
	}

	@Override
	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void resume () {
		System.out.println(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void render () {
		ScreenUtils.clear(Color.BLACK);
		currentPosition = music.getPosition();
		label.setText((int)currentPosition / 60 + ":" + (int)currentPosition % 60);

		sliderUpdating = true;
		slider.setValue((currentPosition / songDuration) * 100f);
		sliderUpdating = false;
		stage.act();
		stage.draw();

// if(music.isPlaying()){
// time += Gdx.graphics.getDeltaTime();
// System.out.println("realtime: " + time + " music time: " + currentPosition);
// }
	}

	@Override
	public void dispose () {
		batch.dispose();
		music.dispose();
	}

	private Drawable getDrawable (String path) {
		return new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal(path))));
	}
}
