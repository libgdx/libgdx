package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.tests.utils.GdxTest;

public class ShortSoundTest extends GdxTest {

	@Override
	public void create () {
		Gdx.audio.newSound(Gdx.files.internal("data/tic.ogg")).play();
	}

}
