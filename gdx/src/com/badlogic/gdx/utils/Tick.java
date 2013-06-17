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

/** @author semtiko */
public class Tick {
	private long delay;
	private long time;

	/**
	 * @param delay Delay time in milliseconds
	 */
	public Tick(long delay) {
		this.delay = delay;
		setCurrentTime();
	}

	/**
	 * Reset timer to current time
	 */
	public void setCurrentTime() {
		this.time = TimeUtils.millis();
	}

	/**
	 * @param delay Sets new delay value in milliseconds
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	public boolean ready() {
		if (TimeUtils.millis() - time >= delay) {
			setCurrentTime();

			return true;
		}

		return false;
	}
}
