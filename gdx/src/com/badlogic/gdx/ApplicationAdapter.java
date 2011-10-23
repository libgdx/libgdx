
package com.badlogic.gdx;

/** Convenience implementation of {@link ApplicationListener}. Derrive from this and only override what you need.
 * @author mzechner */
public abstract class ApplicationAdapter implements ApplicationListener {
	@Override
	public void create () {
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void render () {
	}

	@Override
	public void pause () {
	}

	@Override
	public void resume () {
	}

	@Override
	public void dispose () {
	}
}
