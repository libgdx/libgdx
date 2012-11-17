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

package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/** A resizable, ordered or unordered array of objects. If unordered, this class avoids a memory copy when removing elements (the
 * last element is moved to the removed element's position).
 * @author Nathan Sweet */
public class Array<T> implements Iterable<T> {
	/** Provides direct access to the underlying array. If the Array's generic type is not Object, this field may only be accessed
	 * if the {@link Array#Array(boolean, int, Class)} constructor was used. */
	public T[] items;

	public int size;
	public boolean ordered;

	private ArrayIterator iterator;

	/** Creates an ordered array with a capacity of 16. */
	public Array () {
		this(true, 16);
	}

	/** Creates an ordered array with the specified capacity. */
	public Array (int capacity) {
		this(true, capacity);
	}

	/** @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy.
	 * @param capacity Any elements added beyond this will cause the backing array to be grown. */
	public Array (boolean ordered, int capacity) {
		this.ordered = ordered;
		items = (T[])new Object[capacity];
	}

	/** Creates a new array with {@link #items} of the specified type.
	 * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy.
	 * @param capacity Any elements added beyond this will cause the backing array to be grown. */
	public Array (boolean ordered, int capacity, Class<T> arrayType) {
		this.ordered = ordered;
		items = (T[])java.lang.reflect.Array.newInstance(arrayType, capacity);
	}

	/** Creates an ordered array with {@link #items} of the specified type and a capacity of 16. */
	public Array (Class<T> arrayType) {
		this(true, 16, arrayType);
	}

	/** Creates a new array containing the elements in the specified array. The new array will have the same type of backing array
	 * and will be ordered if the specified array is ordered. The capacity is set to the number of elements, so any subsequent
	 * elements added will cause the backing array to be grown. */
	public Array (Array array) {
		this(array.ordered, array.size, (Class<T>)array.items.getClass().getComponentType());
		size = array.size;
		System.arraycopy(array.items, 0, items, 0, size);
	}

	/** Creates a new ordered array containing the elements in the specified array. The new array will have the same type of backing
	 * array. The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be
	 * grown. */
	public Array (T[] array) {
		this(true, array);
	}

	/** Creates a new array containing the elements in the specified array. The new array will have the same type of backing array.
	 * The capacity is set to the number of elements, so any subsequent elements added will cause the backing array to be grown.
	 * @param ordered If false, methods that remove elements may change the order of other elements in the array, which avoids a
	 *           memory copy. */
	public Array (boolean ordered, T[] array) {
		this(ordered, array.length, (Class)array.getClass().getComponentType());
		size = array.length;
		System.arraycopy(array, 0, items, 0, size);
	}

	public void add (T value) {
		T[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		items[size++] = value;
	}

	public void addAll (Array array) {
		addAll(array, 0, array.size);
	}

	public void addAll (Array array, int offset, int length) {
		if (offset + length > array.size)
			throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
		addAll((T[])array.items, offset, length);
	}

	public void addAll (T[] array) {
		addAll(array, 0, array.length);
	}

	public void addAll (T[] array, int offset, int length) {
		T[] items = this.items;
		int sizeNeeded = size + length;
		if (sizeNeeded > items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
		System.arraycopy(array, offset, items, size, length);
		size += length;
	}

	public T get (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		return items[index];
	}

	public void set (int index, T value) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		items[index] = value;
	}

	public void insert (int index, T value) {
		T[] items = this.items;
		if (size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
		if (ordered)
			System.arraycopy(items, index, items, index + 1, size - index);
		else
			items[size] = items[index];
		size++;
		items[index] = value;
	}

	public void swap (int first, int second) {
		if (first >= size) throw new IndexOutOfBoundsException(String.valueOf(first));
		if (second >= size) throw new IndexOutOfBoundsException(String.valueOf(second));
		T[] items = this.items;
		T firstValue = items[first];
		items[first] = items[second];
		items[second] = firstValue;
	}

	/** @param identity If true, == comparison will be used. If false, .equals() comaparison will be used. */
	public boolean contains (T value, boolean identity) {
		T[] items = this.items;
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
		T[] items = this.items;
		if (identity || value == null) {
			for (int i = 0, n = size; i < n; i++)
				if (items[i] == value) return i;
		} else {
			for (int i = 0, n = size; i < n; i++)
				if (value.equals(items[i])) return i;
		}
		return -1;
	}

	public int lastIndexOf (T value, boolean identity) {
		T[] items = this.items;
		if (identity || value == null) {
			for (int i = size - 1; i >= 0; i--)
				if (items[i] == value) return i;
		} else {
			for (int i = size - 1; i >= 0; i--)
				if (value.equals(items[i])) return i;
		}
		return -1;
	}

	public boolean removeValue (T value, boolean identity) {
		T[] items = this.items;
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

	/** Removes and returns the item at the specified index. */
	public T removeIndex (int index) {
		if (index >= size) throw new IndexOutOfBoundsException(String.valueOf(index));
		T[] items = this.items;
		T value = (T)items[index];
		size--;
		if (ordered)
			System.arraycopy(items, index + 1, items, index, size - index);
		else
			items[index] = items[size];
		items[size] = null;
		return value;
	}

	/** Removes and returns the last item. */
	public T pop () {
		--size;
		T item = items[size];
		items[size] = null;
		return item;
	}

	/** Returns the last item. */
	public T peek () {
		return items[size - 1];
	}

	/** Returns the first item. */
	public T first () {
		return items[0];
	}

	public void clear () {
		T[] items = this.items;
		for (int i = 0, n = size; i < n; i++)
			items[i] = null;
		size = 0;
	}

	/** Reduces the size of the backing array to the size of the actual items. This is useful to release memory when many items have
	 * been removed, or if it is known that more items will not be added. */
	public void shrink () {
		resize(size);
	}

	/** Increases the size of the backing array to acommodate the specified number of additional items. Useful before adding many
	 * items to avoid multiple backing array resizes.
	 * @return {@link #items} */
	public T[] ensureCapacity (int additionalCapacity) {
		int sizeNeeded = size + additionalCapacity;
		if (sizeNeeded >= items.length) resize(Math.max(8, sizeNeeded));
		return items;
	}

	/** Creates a new backing array with the specified size containing the current items. */
	protected T[] resize (int newSize) {
		T[] items = this.items;
		T[] newItems = (T[])java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), newSize);
		System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
		this.items = newItems;
		return newItems;
	}

	/** Sorts this array. The array elements must implement {@link Comparable}. This method is not thread safe (uses
	 * {@link Sort#instance()}). */
	public void sort () {
		Sort.instance().sort(items, 0, size);
	}

	/** Sorts the array. This method is not thread safe (uses {@link Sort#instance()}). */
	public void sort (Comparator<T> comparator) {
		Sort.instance().sort(items, comparator, 0, size);
	}

	public void reverse () {
		for (int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++) {
			int ii = lastIndex - i;
			T temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	public void shuffle () {
		for (int i = size - 1; i >= 0; i--) {
			int ii = MathUtils.random(i);
			T temp = items[i];
			items[i] = items[ii];
			items[ii] = temp;
		}
	}

	/** Returns an iterator for the items in the array. Remove is supported. Note that the same iterator instance is returned each
	 * time this method is called. Use the {@link ArrayIterator} constructor for nested or multithreaded iteration. */
	public Iterator<T> iterator () {
		if (iterator == null)
			iterator = new ArrayIterator(this);
		else
			iterator.index = 0;
		return iterator;
	}

	/** Reduces the size of the array to the specified size. If the array is already smaller than the specified size, no action is
	 * taken. */
	public void truncate (int newSize) {
		if (size <= newSize) return;
		for (int i = newSize; i < size; i++)
			items[i] = null;
		size = newSize;
	}

	/** Returns a random item from the array, or null if the array is empty. */
	public T random () {
		if (size == 0) return null;
		return items[MathUtils.random(0, size - 1)];
	}

	public T[] toArray () {
		return (T[])toArray(items.getClass().getComponentType());
	}

	public <V> V[] toArray (Class<V> type) {
		V[] result = (V[])java.lang.reflect.Array.newInstance(type, size);
		System.arraycopy(items, 0, result, 0, size);
		return result;
	}

	public boolean equals (Object object) {
		if (object == this) return true;
		if (!(object instanceof Array)) return false;
		Array array = (Array)object;
		int n = size;
		if (n != array.size) return false;
		Object[] items1 = this.items;
		Object[] items2 = array.items;
		for (int i = 0; i < n; i++) {
			Object o1 = items1[i];
			Object o2 = items2[i];
			if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
		}
		return true;
	}

	public String toString () {
		if (size == 0) return "[]";
		T[] items = this.items;
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

	public String toString (String separator) {
		if (size == 0) return "";
		T[] items = this.items;
		StringBuilder buffer = new StringBuilder(32);
		buffer.append(items[0]);
		for (int i = 1; i < size; i++) {
			buffer.append(separator);
			buffer.append(items[i]);
		}
		return buffer.toString();
	}

	static public class ArrayIterator<T> implements Iterator<T> {
		private final Array<T> array;
		int index;

		public ArrayIterator (Array<T> array) {
			this.array = array;
		}

		public boolean hasNext () {
			return index < array.size;
		}

		public T next () {
			if (index >= array.size) throw new NoSuchElementException(String.valueOf(index));
			return array.items[index++];
		}

		public void remove () {
			index--;
			array.removeIndex(index);
		}

		public void reset () {
			index = 0;
		}
	}

	static public class ArrayIterable<T> implements Iterable<T> {
		private ArrayIterator<T> iterator;

		public ArrayIterable (Array<T> array) {
			iterator = new ArrayIterator<T>(array);
		}

		@Override
		public Iterator<T> iterator () {
			iterator.reset();
			return iterator;
		}
	}
}
