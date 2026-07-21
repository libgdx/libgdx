
package com.badlogic.gdx.tests;

import com.badlogic.gdx.tests.utils.GdxTest;

public class ShortSoundTest extends GdxTest {

	@Override
	public void create () {
		audio.newSound(files.internal("data/tic.ogg")).play();
	}

}
