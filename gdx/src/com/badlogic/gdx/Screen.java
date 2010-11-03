
package com.badlogic.gdx;

public abstract class Screen {
	public final Game game;

	public Screen (Game game) {
		this.game = game;
	}

	public abstract void render (float delta);

	public void resize (int width, int height) {
	}

	public void show () {
	}

	public void hide () {
	}

	public void pause () {
	}

	public void resume () {
	}

	public void dispose () {
	}
}
