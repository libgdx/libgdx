/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.utils;

import java.util.Arrays;

/**
 * An ordered, resizable float array. Avoids the boxing that occurs with ArrayList<Float>.
 * @author Nathan Sweet
 */
public class FloatArray extends FloatBag {
	public FloatArray () {
	}

	public FloatArray (FloatArray array) {
		super(array);
	}

	public FloatArray (FloatBag bag) {
		super(bag);
	}

	public FloatArray (int capacity) {
		super(capacity);
	}

	public void insert (int index, float value) {
		float[] items = this.items;
		if (size == items.length) {
			resize(Math.max(8, (int)(size * 1.75f)))[size++] = value;
			return;
		}
		System.arraycopy(items, index, items, index + 1, size - index);
		size++;
		items[index] = value;
	}

	public void removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		float[] items = this.items;
		System.arraycopy(items, index + 1, items, index, size - index);
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public float pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		float[] items = this.items;
		float value = items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		return value;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}
}
