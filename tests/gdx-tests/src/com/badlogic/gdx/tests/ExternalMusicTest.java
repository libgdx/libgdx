package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Tests playing back audio from the external storage. 
 * @author mzechner
 *
 */
public class ExternalMusicTest extends GdxTest {

	@Override
	public void create () {
		// copy an internal mp3 to the external storage
		FileHandle src = Gdx.files.internal("data/8.12.mp3");
		FileHandle dst = Gdx.files.external("8.12.mp3");
		src.copyTo(dst);
		
		// create a music instance and start playback
		Music music = Gdx.audio.newMusic(dst);
		music.play();
	}

	@Override
	public void dispose () {
		// delete the copy on the external storage
		Gdx.files.external("8.12.mp3").delete();
	}
}
