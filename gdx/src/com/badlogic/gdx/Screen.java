package com.badlogic.gdx;

public abstract class Screen {
	final Game game;
	
	public Screen(Game game) {
		this.game = game;
	}
	public abstract void resize(int width, int height);
	
	public abstract void render(float delta);
	
	public abstract void pause();
	
	public abstract void resume();
	
	public abstract void dispose();
}
