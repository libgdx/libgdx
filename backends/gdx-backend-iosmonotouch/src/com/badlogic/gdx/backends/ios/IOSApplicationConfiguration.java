package com.badlogic.gdx.backends.ios;

public class IOSApplicationConfiguration {
	// FIXME add compass, framebuffer bit depth, stencil, just like on Android if possible
	/** whether or not portrait orientation is supported. */
	public boolean orientationPortrait = true;
	/** whether or not landscape orientation is supported. */
	public boolean orientationLandscape = true;
	/** whether to use the accelerometer **/
	public boolean useAccelerometer = true;
	/** the update interval to poll the accelerometer with, in seconds **/
	public float accelerometerUpdate = 0.05f;
}
