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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.tests.utils.GdxTest;

public class MusicTest extends GdxTest {

	Music music;
	float songDuration = 183;
	float currentPosition;

	TextureRegion buttons;
	SpriteBatch batch;
	BitmapFont font;

	Stage stage;
	Slider slider;
	boolean sliderUpdating = false;

	@Override
	public void create () {
		music = Gdx.audio.newMusic(Gdx.files.internal("data/8.12.mp3"));
		music.play();

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
		stage.addActor(slider);

		Gdx.input.setInputProcessor(stage);
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
				}
				if (Gdx.input.getX() > 64 && Gdx.input.getX() < 128) {
					music.stop();
				}
				if (Gdx.input.getX() > 128 && Gdx.input.getX() < 192) {
					music.pause();
				}
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		buttons.getTexture().dispose();
		music.dispose();
	}
}
