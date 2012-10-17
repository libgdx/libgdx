package com.badlogic.gdx.backends.ios;

public class IOSApplicationConfiguration {
	// FIXME add compass, framebuffer bit depth, stencil, just like on Android if possible
	/** wheter to enable screen dimming. */
	public boolean preventScreenDimming = true;
	/** whether or not portrait orientation is supported. */
	public boolean orientationPortrait = true;
	/** whether or not landscape orientation is supported. */
	public boolean orientationLandscape = true;
	/**
	 * Scale factor to use on large screens (i.e. iPad) with retina (has no effect on non-retina screens).
	 * <ul>
	 *   <li>1.0 = use same screen dimensions as you would on non retina-devices
	 *   <li>2.0 = use retina screen dimensions (you will have double the pixels)
	 *   <li>any other value between 1.0 and 2.0: scales the screens according to your scale factor. A scale factor
	 *       of 1.5 or 1.75 for example works very well without any artifacts! NOTE: using a scale factor less than
	 *       2.0 will prevent buttons/items to become too tiny on retina devices.
	 * </ul>
    */
	public float displayScaleLargeScreenIfRetina = 1.0f;
	/**
	 * Scale factor to use on small screens (i.e. iPhone, iPod) with retina (has no effect on non-retina screens).
	 * <ul>
	 *   <li>1.0 = use same screen dimensions as you would on non retina-devices
	 *   <li>2.0 = use retina screen dimensions (you will have double the pixels)
	 *   <li>any other value between 1.0 and 2.0: scales the screens according to your scale factor. A scale factor
	 *       of 1.5 or 1.75 for example works very well without any artifacts! NOTE: using a scale factor less than
	 *       2.0 will prevent buttons/items to become too tiny on retina devices.
	 * </ul>
    */
	public float displayScaleSmallScreenIfRetina = 1.0f;
	/** whether to use the accelerometer **/
	public boolean useAccelerometer = true;
	/** the update interval to poll the accelerometer with, in seconds **/
	public float accelerometerUpdate = 0.05f;
}
