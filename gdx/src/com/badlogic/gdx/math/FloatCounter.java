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

package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.StringBuilder;

/** Track properties of a stream of float values. The properties (total value, minimum, etc) are updated as values are
 * {@link #put(float)} into the stream.
 * 
 * @author xoppa */
public class FloatCounter {
	/** The amount of values added */
	public int count;
	/** The sum of all values */
	public float total;
	/** The smallest value */
	public float min;
	/** The largest value */
	public float max;
	/** The average value (total / count) */
	public float average;
	/** The latest raw value */
	public float latest;
	/** The current windowed mean value */
	public float value;
	/** Provides access to the WindowedMean if any (can be null) */
	public final WindowedMean mean;

	/** Construct a new FloatCounter
	 * @param windowSize The size of the mean window or 1 or below to not use a windowed mean. */
	public FloatCounter (int windowSize) {
		mean = (windowSize > 1) ? new WindowedMean(windowSize) : null;
		reset();
	}

	/** Add a value and update all fields.
	 * @param value The value to add */
	public void put (float value) {
		latest = value;
		total += value;
		count++;
		average = total / count;

		if (mean != null) {
			mean.addValue(value);
			this.value = mean.getMean();
		} else
			this.value = latest;

		if (mean == null || mean.hasEnoughData()) {
			if (this.value < min) min = this.value;
			if (this.value > max) max = this.value;
		}
	}

	/** Reset all values to their default value. */
	public void reset () {
		count = 0;
		total = 0f;
		min = Float.MAX_VALUE;
		max = -Float.MAX_VALUE;
		average = 0f;
		latest = 0f;
		value = 0f;
		if (mean != null) mean.clear();
	}
}
