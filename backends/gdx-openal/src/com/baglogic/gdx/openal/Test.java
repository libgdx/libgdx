
package com.baglogic.gdx.openal;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public class Test implements ApplicationListener {
	public void create () {
		final OpenALAudio audio = new OpenALAudio(8);

		Thread thread = new Thread() {
			public void run () {
				while (true) {
					audio.update();
					try {
						Thread.sleep(100);
					} catch (InterruptedException ignored) {
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();

		// String path = "C:/Dev/libgdx/tests/gdx-tests-lwjgl/data/cloudconnected.ogg";
		String path = "C:/Users/Nate/Desktop/reggae/10 - Rainbow In The Sky.mp3";

		OpenALSound sound = audio.newSound(Gdx.files.absolute(path));
		sound.play();

		// OpenALMusic music = audio.newMusic(Gdx.files.absolute(path));
		// music.play();
	}

	public void resize (int width, int height) {
	}

	public void render () {
	}

	public void resume () {
	}

	public void pause () {
	}

	public void dispose () {
	}

	public static void main (String[] args) throws Exception {
		//new LwjglApplication(new Test(), "Test", 480, 320, false);
	}
}
