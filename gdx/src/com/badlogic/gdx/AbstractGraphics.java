package com.badlogic.gdx;

public abstract class AbstractGraphics implements Graphics {

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
