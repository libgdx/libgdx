
package com.badlogic.gdx;

public abstract class Game implements ApplicationListener {
	Screen screen;

	@Override public void dispose () {
		if (screen != null) screen.hide();
	}

	@Override public void pause () {
		if (screen != null) screen.pause();
	}

	@Override public void resume () {
		if (screen != null) screen.resume();
	}

	@Override public void render () {
		if (screen != null) screen.render(Gdx.graphics.getDeltaTime());
	}

	@Override public void resize (int width, int height) {
		if (screen != null) screen.resize(width, height);
	}

	public void setScreen (Screen screen) {
		if (this.screen != null) this.screen.hide();
		this.screen = screen;
		screen.show();
	}
}
