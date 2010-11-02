package com.badlogic.gdx;

public abstract class Game implements ApplicationListener {
	Screen screen;
	
	/**
	 * Starts the game and returns the start {@link Screen}. This
	 * will be called in the rendering thread so you can do anything
	 * you like.
	 * 
	 * @return the start screen. 
	 */
	public abstract Screen start();

	@Override
	public void create() {
		screen = start();
	}

	@Override
	public void dispose() {		
		screen.dispose();
	}

	@Override
	public void pause() {
		screen.pause();
	}

	@Override
	public void render() {
		screen.render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		screen.resize(width,height);
	}

	@Override
	public void resume() {
		screen.resume();		
	}
	
	public void setScreen(Screen screen) {
		this.screen.dispose();
		this.screen = screen;
	}
}
