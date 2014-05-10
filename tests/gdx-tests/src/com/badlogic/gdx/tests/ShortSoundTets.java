package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ShortSoundTets extends GdxTest {

	@Override
	public void create () {
		Gdx.audio.newSound(Gdx.files.internal("data/chirp.wav")).play();
	}

}
