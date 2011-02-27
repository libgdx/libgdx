package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;

/**
 * A simple helper class to log the frames per seconds
 * achieved. Just invoke the {@link #log()} method in
 * your rendering method. The output will be logged
 * once per second.
 * 
 * @author mzechner
 *
 */
public class FPSLogger {
	long startTime;
	
	public FPSLogger() {
		startTime = System.nanoTime();
	}
	
	/**
	 * Logs the current frames per second to the console.
	 */
	public void log() {
		if(System.nanoTime()-startTime>1000000000) {
			Gdx.app.log("FPSLogger", "fps: " + Gdx.graphics.getFramesPerSecond());
			startTime = System.nanoTime();
		}
	}
}
