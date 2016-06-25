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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.FloatCounter;
import com.badlogic.gdx.math.WindowedMean;

/** Class to keep track of the time and load (percentage of total time) a specific task takes. Call {@link #start()} just before
 * starting the task and {@link #stop()} right after. You can do this multiple times if required. Every render or update call
 * {@link #tick()} to update the values. The {@link #time} {@link FloatCounter} provides access to the minimum, maximum, average,
 * total and current time (in seconds) the task takes. Likewise for the {@link #load} value, which is the percentage of the total time.
 * @author xoppa */
public class PerformanceCounter {
	private final static float nano2seconds = 1f / 1000000000.0f;
	private long startTime = 0L;
	private long lastTick = 0L;

	/** The time value of this counter (seconds) */
	public final FloatCounter time;
	/** The load value of this counter */
	public final FloatCounter load;
	/** The name of this counter */
	public final String name;
	/** The current value in seconds, you can manually increase this using your own timing mechanism if needed, if you do so, you also need to
	 * update {@link #valid}. */
	public float current = 0f;
	/** Flag to indicate that the current value is valid, you need to set this to true if using your own timing mechanism */
	public boolean valid = false;

	public PerformanceCounter (final String name) {
		this(name, 5);
	}

	public PerformanceCounter (final String name, final int windowSize) {
		this.name = name;
		this.time = new FloatCounter(windowSize);
		this.load = new FloatCounter(1);
	}

	/** Updates the time and load counters and resets the time. Call {@link #start()} to begin a new count. The values are only
	 * valid after at least two calls to this method. */
	public void tick () {
		final long t = TimeUtils.nanoTime();
		if (lastTick > 0L) tick((t - lastTick) * nano2seconds);
		lastTick = t;
	}

	/** Updates the time and load counters and resets the time. Call {@link #start()} to begin a new count.
	 * @param delta The time since the last call to this method */
	public void tick (final float delta) {
		if (!valid) {
			Gdx.app.error("PerformanceCounter", "Invalid data, check if you called PerformanceCounter#stop()");
			return;
		}

		time.put(current);

		final float currentLoad = delta == 0f ? 0f : current / delta;
		load.put((delta > 1f) ? currentLoad : delta * currentLoad + (1f - delta) * load.latest);

		current = 0f;
		valid = false;
	}

	/** Start counting, call this method just before performing the task you want to keep track of. Call {@link #stop()} when done. */
	public void start () {
		startTime = TimeUtils.nanoTime();
		valid = false;
	}

	/** Stop counting, call this method right after you performed the task you want to keep track of. Call {@link #start()} again
	 * when you perform more of that task. */
	public void stop () {
		if (startTime > 0L) {
			current += (TimeUtils.nanoTime() - startTime) * nano2seconds;
			startTime = 0L;
			valid = true;
		}
	}

	/** Resets this performance counter to its defaults values. */
	public void reset () {
		time.reset();
		load.reset();
		startTime = 0L;
		lastTick = 0L;
		current = 0f;
		valid = false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder();
		return toString(sb).toString();
	}

	/** Creates a string in the form of "name [time: value, load: value]" */
	public StringBuilder toString (final StringBuilder sb) {
		sb.append(name).append(": [time: ").append(time.value).append(", load: ").append(load.value).append("]");
		return sb;
	}
}
