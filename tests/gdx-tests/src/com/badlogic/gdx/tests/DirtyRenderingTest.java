package com.badlogic.gdx.tests;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.tests.utils.GdxTest;

/**
 * Demonstrates how to use non-continuous (aka dirty-only) rendering. The
 * application will clear the screen with a random color every frame it
 * renders. Rendering requests are issued automatically if new input
 * events arrive.
 * 
 * @author mzechner
 *
 */
public class DirtyRenderingTest extends GdxTest {
	@Override
	public void create () {
		// disable continuous rendering
		Gdx.graphics.setContinuousRendering(false);
		Gdx.app.log("DirtyRenderingTest", "created");
	}

	@Override
	public boolean needsGL20 () {
		return true;
	}

	@Override
	public void resume () {
		Gdx.app.log("DirtyRenderingTest", "resumed");
	}

	@Override
	public void resize (int width, int height) {
		Gdx.app.log("DirtyRenderingTest", "resized");
	}

	@Override
	public void pause () {
		Gdx.app.log("DirtyRenderingTest", "paused");
	}

	@Override
	public void dispose () {
		Gdx.app.log("DirtyRenderingTest", "disposed");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(MathUtils.random(), MathUtils.random(), MathUtils.random(), MathUtils.random());
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
	}
}
