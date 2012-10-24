package com.badlogic.gdx.physics.bullet;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputAdapter;

public abstract class GdxTest extends InputAdapter implements ApplicationListener {
	public boolean needsGL20 () {
		return false;
	}

	public void create () {
	}

	public void resume () {
	}

	public void render () {
	}

	public void resize (int width, int height) {
	}

	public void pause () {
	}

	public void dispose () {
	}
}