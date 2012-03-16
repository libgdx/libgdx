package com.badlogic.gdx.utils;

/**
 * Wrapper around System.nanoTime() and System.currentTimeMillis(). Use this
 * if you want to be compatible across all platforms!
 * @author mzechner
 *
 */
public class TimeUtils {
	/**
	 * @return The current value of the system timer, in nanoseconds.
	 */
	public static long nanoTime() {
		return System.nanoTime();
	}
	
	/**
	 * @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
	 */
	public static long millis() {
		return System.currentTimeMillis();
	}
}
