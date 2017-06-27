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

package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Spinner.SpinnerModel;

/** Spinner model used to select integer values
 * @author Jeremy Gillespie-Cloutier */
public class SpinnerNumberModel extends SpinnerModel<Integer> {

	private int min;
	private int max;
	private int value;
	private int step;

	/** Constructor
	 * @param min The minimum value the spinner can take
	 * @param max The maximum value the spinner can take
	 * @param step The absolute increment when the next or previous button on the spinner is pressed
	 * @param value The current value of the spinner */
	public SpinnerNumberModel (int min, int max, int step, int value) {
		if (max < min) throw new IllegalArgumentException("The maximum cannot be smaller than the minimum");
		if (value < min || value > max)
			throw new IllegalArgumentException("The current value must be between the minimum and the maximum (inclusive)");
		this.min = min;
		this.max = max;
		if (step < 0) step *= -1;
		this.step = step;
		this.value = value;
	}

	@Override
	public boolean next () {
		if (value + step <= max) {
			value += step;
			return true;
		}
		return false;
	}

	@Override
	public boolean previous () {
		if (value - step >= min) {
			value -= step;
			return true;
		}
		return false;
	}

	@Override
	public Integer getValue () {
		return value;
	}

}
