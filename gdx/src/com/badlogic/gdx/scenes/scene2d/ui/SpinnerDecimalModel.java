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

import java.math.BigDecimal;

import com.badlogic.gdx.scenes.scene2d.ui.Spinner.SpinnerModel;

/** Spinner model used to display decimal numbers
 * @author Jeremy Gillespie-Cloutier */
public class SpinnerDecimalModel extends SpinnerModel<BigDecimal> {

	private BigDecimal min;
	private BigDecimal max;
	private BigDecimal step;
	private BigDecimal value;

	/** Constructor
	 * @param min The min value the spinner can take
	 * @param max The max value the spinner can take
	 * @param step The absolute value for "next" and "previous" increments
	 * @param value The current value the spinner takes */
	public SpinnerDecimalModel (BigDecimal min, BigDecimal max, BigDecimal step, BigDecimal value) {
		if (min.compareTo(max) > 0) throw new IllegalArgumentException("The maximum cannot be smaller than the minimum");
		if (value.compareTo(min) < 0 || value.compareTo(max) > 0)
			throw new IllegalArgumentException("The current value must be between the minimum and the maximum (inclusive)");

		this.min = min;
		this.max = max;
		if (step.compareTo(BigDecimal.ZERO) < 0) step = step.negate();
		this.step = step;
		this.value = value;
	}

	@Override
	public boolean next () {
		BigDecimal next = value.add(step);
		if (next.compareTo(max) <= 0) {
			value = next;
			return true;
		}
		return false;
	}

	@Override
	public boolean previous () {
		BigDecimal previous = value.subtract(step);
		if (previous.compareTo(min) >= 0) {
			value = previous;
			return true;
		}
		return false;
	}

	@Override
	public BigDecimal getValue () {
		return value;
	}

	@Override
	public CharSequence getDisplayValue () {
		return value + "";
	}

}
