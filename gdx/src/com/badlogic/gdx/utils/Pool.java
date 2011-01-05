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
 * An ordered, resizable array of objects that reuses element instances. The {@link #add()} and {@link #insert(int)} methods add
 * objects to the pool. The objects are created by {@link #newObject()}. Any other methods that would add objects to the pool are
 * not supported. When an object is removed from the pool, a reference to it is retained for reuse.
 * @author Nathan Sweet
 */
abstract public class Pool<T> extends Array<T> {
	public final int max;

	public Pool (boolean ordered, int capacity) {
		this(ordered, capacity, -1);
	}

	/**
	 * @param max The maximum size of this pool. -1 for no max size. See {@link #add()}.
	 */
	public Pool (boolean ordered, int capacity, int max) {
		super(ordered, capacity);
		this.max = max;
	}

	public Pool (boolean ordered, int capacity, Class<T> arrayType) {
		this(ordered, capacity, arrayType, -1);
	}

	/**
	 * @param max The maximum size of this pool. -1 for no max size. See {@link #add()}.
	 */
	public Pool (boolean ordered, int capacity, Class<T> arrayType, int max) {
		super(ordered, capacity, arrayType);
		this.max = max;
	}

	abstract protected T newObject ();

	/**
	 * Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
	 * {@link #removeValue(Object, boolean) removed} from the pool). If this pool already contains {@link #max} objects, a new
	 * object is returned, but it is not added to the pool (it will be garbage collected when removed).
	 */
	public T add () {
		if (size == max) return newObject();
		T[] items = this.items;
		if (size == items.length) {
			T item = newObject();
			resize(Math.max(8, (int)(size * 1.75f)))[size++] = item;
			return item;
		}
		T item = items[size];
		if (item == null) item = newObject();
		items[size++] = item;
		return item;
	}

	public T insert (int index) {
		if (size == items.length) {
			T item = newObject();
			resize(Math.max(8, (int)(size * 1.75f)))[size++] = item;
			return item;
		}
		T item = items[size];
		if (item == null) item = newObject();
		System.arraycopy(items, index, items, index + 1, size - index);
		size++;
		items[index] = item;
		return item;
	}

	public void removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		Object[] items = this.items;
		T old = (T)items[index];
		System.arraycopy(items, index + 1, items, index, size - index);
		items[size] = old;
	}

	/**
	 * Removes and returns the item at the specified index.
	 */
	public T pop (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		size--;
		Object[] items = this.items;
		T old = (T)items[index];
		System.arraycopy(items, index + 1, items, index, size - index);
		items[size] = old;
		return old;
	}

	/**
	 * Not supported for a pool. Use {@link #add()}.
	 */
	public void add (T value) {
		throw new UnsupportedOperationException("Not supported for a pool.");
	}

	/**
	 * Not supported for a pool. Use {@link #add()}.
	 */
	public void addAll (Array array) {
		throw new UnsupportedOperationException("Not supported for a pool.");
	}

	/**
	 * Not supported for a pool. Use {@link #add()}.
	 */
	public void set (int index, T value) {
		throw new UnsupportedOperationException("Not supported for a pool.");
	}

	/**
	 * Not supported for a pool. Use {@link #add()}.
	 */
	public void insert (int index, T value) {
		throw new UnsupportedOperationException("Not supported for a pool.");
	}
}
