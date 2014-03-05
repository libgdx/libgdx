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
import com.badlogic.gdx.tests.utils.GdxTest;

public class MusicTest extends GdxTest {

	static final int NUM_STREAMS = 1;
	Music[] music = new Music[NUM_STREAMS];
	TextureRegion buttons;
	SpriteBatch batch;
	BitmapFont font;

	@Override
	public void create () {
		for (int i = 0; i < music.length; i++) {
			music[i] = Gdx.audio.newMusic(Gdx.files.internal("data/cloudconnected.ogg"));
			music[i].setLooping(true);
			music[i].setLooping(false);
		}

		buttons = new TextureRegion(new Texture(Gdx.files.internal("data/playback.png")));
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("data/arial-15.fnt"), false);
	}

	@Override
	public void resize (int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(buttons, 0, 0);
// font.draw(batch, "\"Three of a perfect pair: " + music[0].getPosition(), 10, Gdx.graphics.getHeight() - 20);
		batch.end();

		if (Gdx.input.justTouched()) {
			if (Gdx.input.getY() > Gdx.graphics.getHeight() - 64) {
				if (Gdx.input.getX() < 64) {
					for (int i = 0; i < music.length; i++)
						music[i].play();
				}
				if (Gdx.input.getX() > 64 && Gdx.input.getX() < 128) {
					for (int i = 0; i < music.length; i++)
						music[i].stop();
				}
				if (Gdx.input.getX() > 128 && Gdx.input.getX() < 192) {
					for (int i = 0; i < music.length; i++)
						music[i].pause();
				}
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		buttons.getTexture().dispose();
		for (Music m : music)
			m.dispose();
	}
}
