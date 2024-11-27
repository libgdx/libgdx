
package com.badlogic.gdx;

/** Abstract implementation of the Graphics interface, providing common methods for graphics-related functionalities such as time
 * and density calculations. */
public abstract class AbstractGraphics implements Graphics {

	@Override
	public float getRawDeltaTime () {
		return getDeltaTime();
	}

	@Override
	public float getDensity () {
		float ppiX = getPpiX();
		return (ppiX > 0 && ppiX <= Float.MAX_VALUE) ? ppiX / 160f : 1f;
	}

	@Override
	public float getBackBufferScale () {
		return getBackBufferWidth() / (float)getWidth();
	}
}
