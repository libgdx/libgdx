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

import com.badlogic.gdx.utils.Array;

/** Spinner model used to select array elements
 * @author Jeremy Gillespie-Cloutier
 *
 * @param <T> Type of elements contained in the array */
public class SpinnerArrayModel<T> extends SpinnerModel<T> {

	private Array<T> array;
	private int index;
	private boolean wrap;

	/** Constructor
	 * @param array The array of elements held by the spinner
	 * @param index The index of the current element in the array
	 * @param wrap If true, calling next() from the last element will move to the first element (instead of doing nothing). Similar
	 *           behavior for previous(). */
	public SpinnerArrayModel (Array<T> array, int index, boolean wrap) {
		if (index < 0 || index >= array.size) throw new IllegalArgumentException("Index of current element out of bounds.");
		this.array = array;
		this.index = index;
		this.wrap = wrap;
	}

	@Override
	public boolean next () {
		if (index < array.size - 1 || wrap) {
			index++;
			if (index >= array.size) index -= array.size;
			return true;
		}
		return false;
	}

	@Override
	public boolean previous () {
		if (index > 0 || wrap) {
			index--;
			if (index < 0) index += array.size;
			return true;
		}
		return false;
	}

	@Override
	public T getValue () {
		return array.get(index);
	}

}
