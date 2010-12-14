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

/**
 * An unordered, resizable array. Avoids a memory copy when removing elements (the last element is moved to the removed element's
 * position).
 * @author Riven
 * @author Nathan Sweet <misc@n4te.com>
 */
public class Bag<T> {
	public Object[] items;
	public int size;

	public Bag () {
		this(16);
	}

	public Bag (int capacity) {
		items = new Object[capacity];
	}

	public Bag (Bag bag) {
		size = bag.size;
		items = new Object[size];
		System.arraycopy(bag.items, 0, items, 0, size);
	}

	// BOZO
// public Bag (LongArray array) {
// size = array.size;
// items = new Object[size];
// System.arraycopy(array.items, 0, items, 0, size);
// }

	public void add (T value) {
		if (size == items.length) resize((int)(size * 1.75f));
		items[size++] = value;
	}

	public void addAll (Bag bag) {
		int sizeNeeded = size + bag.size;
		if (sizeNeeded >= items.length) resize((int)(sizeNeeded * 1.75f));
		System.arraycopy(bag.items, 0, items, size, bag.size);
		size += bag.size;
	}

	// BOZO
// public void addAll (LongArray array) {
// int sizeNeeded = size + array.size;
// if (sizeNeeded >= items.length) resize((int)(sizeNeeded * 1.75f));
// System.arraycopy(array.items, 0, items, size, array.size);
// size += array.size;
// }

	public T get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return (T)items[index];
	}

	public boolean contains (T value) {
		int i = size - 1;
		Object[] items = this.items;
		while (i >= 0)
			if (items[i--] == value) return true;
		return false;
	}

	public int indexOf (T value) {
		Object[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i] == value) return i;
		return -1;
	}

	public boolean removeValue (T value) {
		Object[] items = this.items;
		for (int i = 0, n = size; i < n; i++) {
			if (items[i] == value) {
				removeIndex(i);
				return true;
			}
		}
		return false;
	}

	public void removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		Object[] items = this.items;
		items[index] = items[size];
	}

	/**
	 * Removes and returns the last item.
	 */
	public T pop () {
		return (T)items[--size];
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public T pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		Object[] items = this.items;
		T value = (T)items[index];
		size--;
		items[index] = items[size];
		return value;
	}

	public void clear () {
		size = 0;
	}

	/**
	 * Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items have
	 * been removed, or if it is known the more items will not be added.
	 */
	public void shrink () {
		if (items.length <= 8) return;
		resize(size);
	}

	/**
	 * Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(sizeNeeded);
	}

	private void resize (int newSize) {
		Object[] newItems = new Object[Math.max(newSize, 8)];
		Object[] items = this.items;
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
	}

	public String toString () {
		if (size == 0) return "[]";
		Object[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append('[');
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(", ");
			buffer.append(items[i]);
		}
		buffer.append(']');
		return buffer.toString();
	}
}
