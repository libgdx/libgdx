package com.badlogic.gdx.backends.ios;

public class IOSApplicationConfiguration {
	
	/** The orientation the application/game is supporting. */
	public enum SupportedOrientation {
		/** Portrait only. */
		PORTRAIT,
		/** Landscape only. */
		LANDSCAPE,
		/** Both landscape & portrait. */
		BOTH;
	}
	
	/** The screen orientations the application supports. */
	public SupportedOrientation supportedOrientation = SupportedOrientation.BOTH;
	/** whether to use the accelerometer **/
	public boolean useAccelerometer = true;
	/** the update interval to poll the accelerometer with, in seconds **/
	public float accelerometerUpdate = 0.05f;
}
