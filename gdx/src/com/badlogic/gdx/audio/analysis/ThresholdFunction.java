/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.audio.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates a threshold function based on the spectral flux.
 * 
 * @author badlogicgames@gmail.com
 * 
 */
public class ThresholdFunction {
	/** the history size **/
	private final int historySize;

	/** the average multiplier **/
	private final float multiplier;

	/**
	 * Consturctor, sets the history size in number of spectra to take into account to calculate the average spectral flux at a
	 * specific position. Also sets the multiplier to multiply the average with.
	 * 
	 * @param historySize The history size.
	 * @param multiplier The average multiplier.
	 */
	public ThresholdFunction (int historySize, float multiplier) {
		this.historySize = historySize;
		this.multiplier = multiplier;
	}

	/**
	 * Returns the threshold function for a given spectral flux function.
	 * 
	 * @return The threshold function.
	 */
	public List<Float> calculate (List<Float> spectralFlux) {
		ArrayList<Float> thresholds = new ArrayList<Float>(spectralFlux.size());

		for (int i = 0; i < spectralFlux.size(); i++) {
			float sum = 0;
			int start = Math.max(0, i - historySize / 2);
			int end = Math.min(spectralFlux.size() - 1, i + historySize / 2);
			for (int j = start; j <= end; j++)
				sum += spectralFlux.get(j);
			sum /= (end - start);
			sum *= multiplier;
			thresholds.add(sum);
		}

		return thresholds;
	}
}
