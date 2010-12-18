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
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An unordered, resizable array that reuses element instances
 * @see Bag
 * @author Riven
 * @author Nathan Sweet
 */
abstract public class BagPool<T> {
	public T[] items;
	public int size;

	private ItemIterator iterator;

	/**
	 * Creates a new bag with an initial capacity of 16.
	 */
	public BagPool () {
		this(16);
	}

	public BagPool (int capacity) {
		items = (T[])new Object[capacity];
	}

	/**
	 * Creates a new bag with an initial capacity of 16 and {@link #items} of the specified type.
	 */
	public BagPool (Class<T> arrayType) {
		this(arrayType, 16);
	}

	/**
	 * Creates a new bag with {@link #items} of the specified type.
	 */
	public BagPool (Class<T> arrayType, int capacity) {
		items = (T[])java.lang.reflect.Array.newInstance(arrayType, capacity);
	}

	abstract protected T newObject ();

	public T add () {
		if (size == items.length) {
			T item = newObject();
			resize((int)(size * 1.75f), false)[size++] = item;
			return item;
		}
		T item = items[size];
		if (item == null) item = newObject();
		items[size++] = item;
		return item;
	}

	public T get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public boolean contains (T value, boolean identity) {
		Object[] items = this.items;
		int i = size - 1;
		if (identity || value == null) {
			while (i >= 0)
				if (items[i--] == value) return true;
		} else {
			while (i >= 0)
				if (value.equals(items[i--])) return true;
		}
		return false;
	}

	public int indexOf (T value, boolean identity) {
		Object[] items = this.items;
		if (identity || value == null) {
			for (int i = 0, n = size; i < n; i++)
				if (items[i] == value) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (value.equals(items[i])) return i;
		}
		return -1;
	}

	public boolean removeValue (T value, boolean identity) {
		Object[] items = this.items;
		if (identity || value == null) {
			for (int i = 0, n = size; i < n; i++) {
				if (items[i] == value) {
					removeIndex(i);
					return true;
				}
			}
		} else {
			for (int i = 0, n = size; i < n; i++) {
				if (value.equals(items[i])) {
					removeIndex(i);
					return true;
				}
			}
		}
		return false;
	}

	public void removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		Object[] items = this.items;
		T old = (T)items[index];
		items[index] = items[size];
		items[size] = old;
	}

	/**
	 * Removes and returns the last item.
	 */
	public T pop () {
		return items[--size];
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public T pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		Object[] items = this.items;
		T value = (T)items[index];
		items[index] = items[size];
		items[size] = value;
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
		resize(size, true);
	}

	/**
	 * Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 */
	public void ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(sizeNeeded, false);
	}

	private T[] resize (int newSize, boolean exact) {
		if (!exact && newSize < 8) newSize = 8;
		T[] items = this.items;
		T[] newItems = (T[])java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), newSize);
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
		return newItems;
	}

	/**
	 * Sorts the bag, which will stay ordered until an element is removed.
	 */
	public void sort (Comparator<T> comparator) {
		Arrays.sort(items, 0, size, comparator);
	}

	/**
	 * Sorts the bag, which will stay ordered until an element is removed.
	 */
	public void sort () {
		Arrays.sort(items, 0, size);
	}

	/**
	 * Returns an iterator for the items in the bag. Remove is supported. Note that the same iterator instance is reused each time
	 * this method is called.
	 */
	public Iterator<T> iterator () {
		if (iterator == null) iterator = new ItemIterator();
		iterator.index = 0;
		return iterator;
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

	class ItemIterator implements Iterator<T> {
		int index;

		public boolean hasNext () {
			return index < size;
		}

		public T next () {
			if (index >= size) throw new NoSuchElementException(String.valueOf(index));
			return items[index++];
		}

		public void remove () {
			index--;
			removeIndex(index);
		}
	}
}
