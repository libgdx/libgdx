
package com.badlogic.gdx;

/**
 * Represents one of many application screens.<br>
 * <br>
 * Note that {@link #dispose()} is not called automatically.
 * @see Game
 */
public interface Screen {
	/**
	 * Called when the screen should render itself.
	 * @param delta The time in seconds since the last render.
	 */
	public void render (float delta);

	/**
	 * @see ApplicationListener#resize(int, int)
	 */
	public void resize (int width, int height);

	/**
	 * Called when this screen becomes the current screen for a {@link Game}.
	 */
	public void show ();

	/**
	 * Called when this screen is no longer the current screen for a {@link Game}.
	 */
	public void hide ();

	/**
	 * @see ApplicationListener#pause()
	 */
	public void pause ();

	/**
	 * @see ApplicationListener#resume()
	 */
	public void resume ();

	/**
	 * Called when this screen should release all resources.
	 */
	public void dispose ();
}
