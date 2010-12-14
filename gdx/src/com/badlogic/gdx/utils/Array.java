/*
 * Copyright (c) 2008-2010, Matthias Mann
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. * Neither the name of Matthias Mann nor
 * the names of its contributors may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.badlogic.gdx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * An ordered, resizable array. This class is very slightly more efficient than {@link ArrayList} (which does an extra method call
 * for get/add and increments a "modCount" on add) and exposes the size and underlying items array, which can be typed rather than
 * Object[].
 * @author Nathan Sweet <misc@n4te.com>
 * @author Matthias Mann
 */
public class Array<T> {
	public T[] items;
	public int size;

	/**
	 * Creates a new array with an initial capacity of 16.
	 */
	public Array () {
		this(16);
	}

	public Array (int capacity) {
		this.items = (T[])new Object[capacity];
	}

	/**
	 * Creates a new array with an initial capacity of 16 and {@link #items} of the specified type.
	 */
	public Array (Class<T> arrayType) {
		this(arrayType, 16);
	}

	/**
	 * Creates a new array with {@link #items} of the specified type.
	 */
	public Array (Class<T> arrayType, int capacity) {
		items = (T[])java.lang.reflect.Array.newInstance(arrayType, capacity);
	}

	public Array (Array<T> array) {
		this((Class<T>)array.items.getClass().getComponentType(), array.size);
		size = array.size;
		System.arraycopy(array.items, 0, items, 0, size);
	}

	public Array (Bag<T> bag) {
		this((Class<T>)bag.items.getClass().getComponentType(), bag.size);
		size = bag.size;
		System.arraycopy(bag.items, 0, items, 0, size);
	}

	public void add (T value) {
		if (size == items.length) resize((int)(size * 1.75f));
		items[size++] = value;
	}

	public void addAll (Array array) {
		int sizeNeeded = size + array.size;
		if (sizeNeeded >= items.length) resize((int)(sizeNeeded * 1.75f));
		System.arraycopy(array.items, 0, items, size, array.size);
		size = sizeNeeded;
	}

	public void addAll (Bag bag) {
		int sizeNeeded = size + bag.size;
		if (sizeNeeded >= items.length) resize((int)(sizeNeeded * 1.75f));
		System.arraycopy(bag.items, 0, items, size, bag.size);
		size = sizeNeeded;
	}

	public void set (int index, T value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, T value) {
		if (size == items.length) {
			resize((int)(size * 1.75f));
			items[size++] = value;
			return;
		}
		System.arraycopy(items, index, items, index + 1, size - index);
		size++;
		items[index] = value;
	}

	public T get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public boolean contains (T value) {
		Object[] items = this.items;
		int i = size - 1;
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
		System.arraycopy(items, index + 1, items, index, size - index);
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
		Object[] items = this.items;
		T value = (T)items[index];
		size--;
		System.arraycopy(items, index + 1, items, index, size - index);
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
		T[] newItems = (T[])java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), Math.max(newSize, 8));
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		items = newItems;
	}

	public void sort (Comparator<T> comparator) {
		Arrays.sort(items, 0, size, comparator);
	}

	public void sort () {
		Arrays.sort(items, 0, size);
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
