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

package com.badlogic.gdx.ai;

import com.badlogic.gdx.utils.TimeUtils;

public class RealTimeProvider implements TimeProvider {
	
	private static final long START = TimeUtils.nanoTime();

	/** Returns the current time in nanoseconds.
	 * <p>
	 * This implementation returns the value of the system timer minus a constant value determined when this class was loaded the
	 * first time in order to ensure it takes increasing values (for 2 ^ 63 nanoseconds, i.e. 292 years) since the time stamp is
	 * used to order the telegrams in the queue. */
	@Override
	public long getCurrentTime () {
		return TimeUtils.nanoTime() - START;
	}
	
}
