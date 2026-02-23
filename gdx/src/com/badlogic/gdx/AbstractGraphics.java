
package com.badlogic.gdx;

import com.badlogic.gdx.graphics.GL20;

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

	/** Clears the color buffers, optionally the depth buffer and whether to apply antialiasing (requires to set number of samples
	 * in the launcher class).
	 *
	 * @param clearDepth Clears the depth buffer if true.
	 * @param applyAntialiasing applies multi-sampling for antialiasing if true. */
	public void clear (float r, float g, float b, float a, boolean clearDepth, boolean applyAntialiasing) {
		getGL20().glClearColor(r, g, b, a);
		int mask = GL20.GL_COLOR_BUFFER_BIT;
		if (clearDepth) mask = mask | GL20.GL_DEPTH_BUFFER_BIT;
		if (applyAntialiasing && getBufferFormat().coverageSampling) mask = mask | GL20.GL_COVERAGE_BUFFER_BIT_NV;
		getGL20().glClear(mask);
	}
}
