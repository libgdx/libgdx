/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
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

import java.util.Arrays;

/**
 * An ordered, resizable float array. Avoids the boxing that occurs with ArrayList<Float>.
 * @author Matthias Mann
 * @author Nathan Sweet
 */
public class FloatArray {
	public float[] items;
	public int size;

	public FloatArray () {
		this(16);
	}

	public FloatArray (int capacity) {
		this.items = new float[capacity];
	}

	public FloatArray (FloatArray array) {
		size = array.size;
		System.arraycopy(array.items, 0, items, 0, size);
	}

	public FloatArray (FloatBag bag) {
		size = bag.size;
		System.arraycopy(bag.items, 0, items, 0, size);
	}

	public void add (float value) {
		float[] items = this.items;
		if (size == items.length) items = resize((int)(size * 1.75f), false);
		items[size++] = value;
	}

	public void addAll (FloatArray array) {
		float[] items = this.items;
		int sizeNeeded = size + array.size;
		if (sizeNeeded >= items.length) items = resize((int)(sizeNeeded * 1.75f), false);
		System.arraycopy(array.items, 0, items, size, array.size);
		size = sizeNeeded;
	}

	public void addAll (FloatBag bag) {
		float[] items = this.items;
		int sizeNeeded = size + bag.size;
		if (sizeNeeded >= items.length) items = resize((int)(sizeNeeded * 1.75f), false);
		System.arraycopy(bag.items, 0, items, size, bag.size);
		size = sizeNeeded;
	}

	public void set (int index, float value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, float value) {
		float[] items = this.items;
		if (size == items.length) {
			resize((int)(size * 1.75f), false)[size++] = value;
			return;
		}
		System.arraycopy(items, index, items, index + 1, size - index);
		size++;
		items[index] = value;
	}

	public float get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public boolean contains (float value) {
		float[] items = this.items;
		int i = size - 1;
		while (i >= 0)
			if (items[i--] == value) return true;
		return false;
	}

	public int indexOf (float value) {
		float[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			if (items[i] == value) return i;
		return -1;
	}

	public boolean removeValue (float value) {
		float[] items = this.items;
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
		float[] items = this.items;
		System.arraycopy(items, index + 1, items, index, size - index);
	}

	/**
	 * Removes and returns the last item.
	 */
	public float pop () {
		return items[--size];
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

	private float[] resize (int newSize, boolean exact) {
		if (!exact && newSize < 8) newSize = 8;
		float[] items = this.items;
		float[] newItems = new float[newSize];
		System.arraycopy(items, 0, newItems, 0, Math.min(items.length, newItems.length));
		this.items = newItems;
		return newItems;
	}

	public void sort () {
		Arrays.sort(items, 0, size);
	}

	public String toString () {
		if (size == 0) return "[]";
		float[] items = this.items;
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
