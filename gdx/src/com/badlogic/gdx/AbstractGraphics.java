package com.badlogic.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;

public abstract class AbstractGraphics implements Graphics {

	@Override
	public void clear(float r, float g, float b, float a){
		Gdx.gl.glClearColor(r, g, b, a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void clear(Color color){
		clear(color.r, color.g, color.b, color.a);
	}

	@Override
	public float getRawDeltaTime () {
		return getDeltaTime();
	}

	@Override
	public float getDensity () {
		return getPpiX() / 160f;
	}

	@Override
	public float getBackBufferScale () {
		return getBackBufferWidth() / (float) getWidth();
	}
}
