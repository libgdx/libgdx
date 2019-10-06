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
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MusicTest extends GdxTest {

	Music music;
	float songDuration;
	float currentPosition;

	TextureRegion buttons;
	SpriteBatch batch;
	BitmapFont font;

	Stage stage;
	Slider slider;
	boolean sliderUpdating = false;
	SelectBox<Song> musicBox;
	TextButton btLoop;

	enum Song {
		MP3, OGG, WAV, MP3_CLOCK
	}

	private float time;
	
	@Override
	public void create () {

		buttons = new TextureRegion(new Texture(Gdx.files.internal("data/playback.png")));
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);

		stage = new Stage();
		Skin skin = new Skin(Gdx.files.internal("data/uiskin.json"));
		slider = new Slider(0, 100, 0.1f, false, skin);
		slider.setPosition(200, 20);
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
				if(music != null) music.setLooping(btLoop.isChecked());
			}
		});
		
		setSong(musicBox.getSelected());

		
		Table table = new Table(skin);
		table.add(musicBox);
		table.add(btLoop);
		table.setFillParent(true);
		stage.addActor(table);
		
		stage.addActor(slider);

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
			music = Gdx.audio.newMusic(Gdx.files.internal("data/cloudconnected.ogg"));
			songDuration = 22;
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
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void resume () {
		System.out.println(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void render () {
		currentPosition = music.getPosition();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(buttons, 0, 0);
		font.draw(batch, (int)currentPosition / 60 + ":" + (int)currentPosition % 60, 365, 35);
		batch.end();

		sliderUpdating = true;
		slider.setValue((currentPosition / songDuration) * 100f);
		sliderUpdating = false;
		stage.act();
		stage.draw();

		if (Gdx.input.justTouched()) {
			if (Gdx.input.getY() > Gdx.graphics.getHeight() - 64) {
				if (Gdx.input.getX() < 64) {
					music.play();
					time = 0;
				}
				if (Gdx.input.getX() > 64 && Gdx.input.getX() < 128) {
					music.stop();
				}
				if (Gdx.input.getX() > 128 && Gdx.input.getX() < 192) {
					music.pause();
				}
			}
		}
		if(music.isPlaying()){
			time += Gdx.graphics.getDeltaTime();
			System.out.println("realtime: " + time + " music time: " + currentPosition);
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		buttons.getTexture().dispose();
		music.dispose();
	}
}
