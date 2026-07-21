
package com.badlogic.gdx.tests.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

public class GdxTestWrapper implements ApplicationListener {
	private ApplicationListener app;
	private boolean logGLErrors;

	public GdxTestWrapper (ApplicationListener delegates, boolean logGLErrors) {
		super();
		this.app = delegates;
		this.logGLErrors = logGLErrors;
	}

	@Override
	public void create () {
		create(Gdx.app);
	}

	@Override
	public void create (Application application) {
		if (logGLErrors) {
			application.log("GLProfiler", "profiler enabled");
			GLProfiler profiler = new GLProfiler(application.getGraphics());
			profiler.setListener(new GLErrorListener() {
				@Override
				public void onError (int error) {
					application.error("GLProfiler", "error " + error);
				}
			});
			profiler.enable();
		}
		app.create(application);
	}

	@Override
	public void resize (int width, int height) {
		app.resize(width, height);
	}

	@Override
	public void render () {
		app.render();
	}

	@Override
	public void pause () {
		app.pause();
	}

	@Override
	public void resume () {
		app.resume();
	}

	@Override
	public void dispose () {
		app.dispose();
	}

}
