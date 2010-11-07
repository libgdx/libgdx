package com.badlogic.gdx;

/**
 * <p>
 * An {@link ApplicationListener} that delegates to a {@link Screen}. This
 * allows an application to easily have multiple screens.
 * </p>
 * <p>
 * Screens are not disposed automatically. You must handle whether you want to
 * keep screens around or dispose of them when another screen is set.
 * </p>
 */
public abstract class Game implements ApplicationListener {
	private Screen screen;

	@Override
	public void dispose() {
		if (screen != null)
			screen.hide();
	}

	@Override
	public void pause() {
		if (screen != null)
			screen.pause();
	}

	@Override
	public void resume() {
		if (screen != null)
			screen.resume();
	}

	@Override
	public void render() {
		if (screen != null)
			screen.render(Gdx.graphics.getDeltaTime());
	}

	@Override
	public void resize(int width, int height) {
		if (screen != null)
			screen.resize(width, height);
	}

	/**
	 * Sets the current screen. {@link Screen#hide()} is called on any old
	 * screen, and {@link Screen#show()} is called on the new screen.
	 */
	public void setScreen(Screen screen) {
		if (this.screen != null)
			this.screen.hide();
		this.screen = screen;
		screen.show();
		screen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	/**
	 * @return the currently active {@link Screen}.
	 */
	public Screen getScreen() {
		return screen;
	}
}
