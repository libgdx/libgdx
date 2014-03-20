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

package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;

/** A simple helper class to log the frames per seconds achieved. Just invoke the {@link #log()} method in your rendering method.
 * The output will be logged once per second.
 * 
 * @author mzechner */
public class FPSLogger {
	long startTime;

	public FPSLogger () {
		startTime = TimeUtils.nanoTime();
	}

	/** Logs the current frames per second to the console. */
	public void log () {
		if (TimeUtils.nanoTime() - startTime > 1000000000) /* 1,000,000,000ns == one second */{
			Gdx.app.log("FPSLogger", "fps: " + Gdx.graphics.getFramesPerSecond());
			startTime = TimeUtils.nanoTime();
		}
	}
}
