package com.badlogic.gdx.tools.grapheditor;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;

public class ShaderGraphEditor implements ApplicationListener {
	@Override
	public void create () {
	}

	@Override
	public void resize (int width, int height) {
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
	
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 640;
		config.height = 480;
		config.useGL20 = true;
		new LwjglApplication(new ShaderGraphEditor(), config);
	}
}
