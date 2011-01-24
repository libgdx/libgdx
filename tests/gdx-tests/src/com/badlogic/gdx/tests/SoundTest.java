/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.tests;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.tests.utils.GdxTest;

public class SoundTest extends GdxTest implements InputProcessor {
	Sound sound;
	Music music;
	float volume = 0.5f;

	@Override public void render () {
	}

	@Override public void create () {
//		sound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/shotgun.wav", FileType.Internal));
		sound = Gdx.audio.newSound(Gdx.files.getFileHandle("data/sell_buy_item.wav", FileType.Internal));

		music = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/threeofaperfectpair.mp3", FileType.Internal));
		music.setVolume(volume);
		music.play();
		music.setLooping(true);
		Gdx.input.setInputProcessor(this);
	}

	@Override public boolean keyDown (int keycode) {
		return false;
	}

	@Override public boolean keyTyped (char character) {
		if (character == '+') volume += 0.1f;
		if (character == '-') volume -= 0.1f;
		music.setVolume(volume);

		return false;
	}

	@Override public boolean keyUp (int keycode) {

		return false;
	}

	@Override public boolean touchDown (int x, int y, int pointer, int newParam) {
		sound.play(1f);
		return false;
	}

	@Override public boolean touchDragged (int x, int y, int pointer) {
		return false;
	}

	@Override public boolean touchUp (int x, int y, int pointer, int button) {
		return false;
	}

	@Override public boolean needsGL20 () {
		return false;
	}

	@Override public boolean touchMoved (int x, int y) {
		return false;
	}

	@Override public boolean scrolled (int amount) {
		return false;
	}
}
