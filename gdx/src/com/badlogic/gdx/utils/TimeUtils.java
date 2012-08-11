/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

/** Wrapper around System.nanoTime() and System.currentTimeMillis(). Use this if you want to be compatible across all platforms!
 * @author mzechner */
public class TimeUtils {
	/** @return The current value of the system timer, in nanoseconds. */
	public static long nanoTime () {
		return System.nanoTime();
	}

	/** @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC. */
	public static long millis () {
		return System.currentTimeMillis();
	}
}
