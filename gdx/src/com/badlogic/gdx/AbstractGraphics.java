
package com.badlogic.gdx;

/**
 * Abstract implementation of the Graphics interface, providing common methods
 * for graphics-related functionalities such as time and density calculations.
 */
public abstract class AbstractGraphics implements Graphics {

	/** 
	 * Delegates to {@link #getDeltaTime()} for the actual time calculation.
	 * @return the time span between the current frame and the last frame in seconds.
	 * */
	@Override
	public float getRawDeltaTime () {
		return getDeltaTime();
	}

	/**
	 * Calculates and returns the screen density based on the pixel-per-inch value
	 * along the X-axis. The density is derived by dividing the PPI by a standard 160 PPI,
	 * and defaults to 1 if PPI is invalid.
	 * @return the Density Independent Pixel factor of the display.
	 */
	@Override
	public float getDensity () {
		float ppiX = getPpiX();
		return (ppiX > 0 && ppiX <= Float.MAX_VALUE) ? ppiX / 160f : 1f;
	}

	/**
	 * Computes and returns the scaling factor for the back buffer compared to the display
	 * width. This is the ratio of the back buffer width to the display width.
	 * @return amount of pixels per logical pixel (point)
	 */
	@Override
	public float getBackBufferScale () {
		return getBackBufferWidth() / (float)getWidth();
	}
}
