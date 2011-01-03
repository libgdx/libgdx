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
 * An ordered, resizable long array. Avoids the boxing that occurs with ArrayList<Long>.
 * @author Nathan Sweet
 */
public class LongArray extends LongBag {
	public LongArray () {
	}

	public LongArray (int capacity) {
		super(capacity);
	}

	public LongArray (LongArray array) {
		super(array);
	}

	public LongArray (LongBag bag) {
		super(bag);
	}

	public void insert (int index, long value) {
		long[] items = this.items;
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
		long[] items = this.items;
		System.arraycopy(items, index + 1, items, index, size - index);
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public long pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		long[] items = this.items;
		long value = items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		return value;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}
}
