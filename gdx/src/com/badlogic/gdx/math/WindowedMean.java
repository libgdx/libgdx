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

/** A simple class keeping track of the mean of a stream of values within a certain window. the WindowedMean will only return a
 * value in case enough data has been sampled. After enough data has been sampled the oldest sample will be replaced by the newest
 * in case a new sample is added.
 * 
 * @author badlogicgames@gmail.com */
public final class WindowedMean {
	float values[];
	int added_values = 0;
	int last_value;
	float mean = 0;
	boolean dirty = true;

	/** constructor, window_size specifies the number of samples we will continuously get the mean and variance from. the class
	 * will only return meaning full values if at least window_size values have been added.
	 * 
	 * @param window_size size of the sample window */
	public WindowedMean (int window_size) {
		values = new float[window_size];
	}

	/** @return whether the value returned will be meaningful */
	public boolean hasEnoughData () {
		return added_values >= values.length;
	}

	/** clears this WindowedMean. The class will only return meaningful values after enough data has been added again. */
	public void clear () {
		added_values = 0;
		last_value = 0;
		for (int i = 0; i < values.length; i++)
			values[i] = 0;
		dirty = true;
	}

	/** adds a new sample to this mean. In case the window is full the oldest value will be replaced by this new value.
	 * 
	 * @param value The value to add */
	public void addValue (float value) {
		if (added_values < values.length) added_values++;
		values[last_value++] = value;
		if (last_value > values.length - 1) last_value = 0;
		dirty = true;
	}

	/** returns the mean of the samples added to this instance. Only returns meaningful results when at least window_size samples
	 * as specified in the constructor have been added.
	 * @return the mean */
	public float getMean () {
		if (hasEnoughData()) {
			if (dirty) {
				float mean = 0;
				for (int i = 0; i < values.length; i++)
					mean += values[i];

				this.mean = mean / values.length;
				dirty = false;
			}
			return this.mean;
		} else
			return 0;
	}

	/** @return the oldest value in the window */
	public float getOldest () {
		return added_values < values.length ? values[0] : values[last_value];
	}

	/** @return the value last added */
	public float getLatest () {
		return values[last_value - 1 == -1 ? values.length - 1 : last_value - 1];
	}

	/** @return The standard deviation */
	public float standardDeviation () {
		if (!hasEnoughData()) return 0;

		float mean = getMean();
		float sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += (values[i] - mean) * (values[i] - mean);
		}

		return (float)Math.sqrt(sum / values.length);
	}

	public float getLowest () {
		float lowest = Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
			lowest = Math.min(lowest, values[i]);
		return lowest;
	}
	
	public float getHighest () {
		float lowest = Float.MIN_NORMAL;
		for (int i = 0; i < values.length; i++)
			lowest = Math.max(lowest, values[i]);
		return lowest;
	}

	public int getValueCount () {
		return added_values;
	}

	public int getWindowSize () {
		return values.length;
	}

	/** @return A new <code>float[]</code> containing all values currently in the window of the stream, in order from oldest to
	 *         latest. The length of the array is smaller than the window size if not enough data has been added. */
	public float[] getWindowValues () {
		float[] windowValues = new float[added_values];
		if (hasEnoughData()) {
			for (int i = 0; i < windowValues.length; i++) {
				windowValues[i] = values[(i + last_value) % values.length];
			}
		} else {
			System.arraycopy(values, 0, windowValues, 0, added_values);
		}
		return windowValues;
	}
}
