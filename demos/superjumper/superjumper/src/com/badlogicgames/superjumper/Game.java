package com.badlogicgames.superjumper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;

public abstract class Game implements ApplicationListener {
	Screen screen;
	
	public void setScreen (Screen helpScreen2) {
		screen.pause();
		screen.dispose();
		screen = helpScreen2;
	}
	
	public abstract Screen getStartScreen();
	
	@Override public void create () {
		screen = getStartScreen();
	}

	@Override public void resume () {
		screen.resume();
	}

	@Override public void render () {
		screen.update(Gdx.graphics.getDeltaTime());
		screen.present(Gdx.graphics.getDeltaTime());
	}

	@Override public void resize (int width, int height) {
		
	}

	@Override public void pause () {
		screen.pause();
	}

	@Override public void dispose () {
		screen.dispose();
	}
}
