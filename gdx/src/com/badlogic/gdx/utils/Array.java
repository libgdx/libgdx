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

import java.util.ArrayList;
import java.util.Comparator;

/**
 * An ordered, resizable array of objects. This class is very slightly more efficient than {@link ArrayList} (which does an extra
 * method call for get/add and increments a "modCount" on add) and exposes the size and underlying {@link #items} array. The items
 * array can be typed, which facilitates System.arraycopy.
 * @author Nathan Sweet
 */
public class Array<T> extends Bag<T> {
	public Array () {
	}

	public Array (Array array) {
		super(array);
	}

	public Array (Bag bag) {
		super(bag);
	}

	public Array (Class<T> arrayType, int capacity) {
		super(arrayType, capacity);
	}

	public Array (Class<T> arrayType) {
		super(arrayType);
	}

	public Array (int capacity) {
		super(capacity);
	}

	public void insert (int index, T value) {
		T[] items = this.items;
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
		Object[] items = this.items;
		System.arraycopy(items, index + 1, items, index, size - index);
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public T pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		Object[] items = this.items;
		T value = (T)items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
		return value;
	}

	/**
	 * Sorts this array. The array elements must implement {@link Comparable}. This method is not thread safe (uses
	 * {@link Sort#instance()}).
	 */
	public void sort () {
		Sort.instance().sort(items, 0, size);
	}

	/**
	 * Sorts the array. This method is not thread safe (uses {@link Sort#instance()}).
	 */
	public void sort (Comparator<T> comparator) {
		Sort.instance().sort(items, comparator, 0, size);
	}
}
