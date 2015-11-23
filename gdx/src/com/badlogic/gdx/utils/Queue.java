/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

import com.badlogic.gdx.utils.reflect.ArrayReflection;

import java.util.NoSuchElementException;

/** Automatically resizing queue. Values in backing queue rotate around so add and remove to/from ends are O(1) (unless resizing in
 * add). Also provides deque functionality via {@link #removeLast()} and {@link #addFirst(Object)}. This collection is not
 * thread-safe. */
public class Queue<T> {

	/** Contains values waiting in this queue. Head and tail indices go in circle around this array, wrapping at the end. */
	protected T[] values;

	/** Index of first element. Logically smaller than tail. Unless empty, it points to a valid element inside queue. NOTE: Do not
	 * manipulate this value directly. */
	protected int head = 0;

	/** Index of last element. Logically bigger than head. Usually points to an empty position, but points to the head when full
	 * (size == values.length). NOTE: Do not manipulate this value directly. */
	protected int tail = 0;

	/** Amount of elements currently in the queue. NOTE: Do not manipulate this value directly. */
	public int size = 0;

	/** Create a new Queue which can hold 16 values without needing to resize backing array. */
	public Queue () {
		this(16);
	}

	/** Create a new Queue which can hold `initialSize` values without needing to resize backing array. */
	public Queue (int initialSize) {
		// noinspection unchecked
		this.values = (T[])new Object[initialSize];
	}

	/** Create a new Queue which can hold `initialSize` values without needing to resize backing array. This creates backing array
	 * of correct type via reflection. Necessary only if you want to access the backing array directly. */
	public Queue (int initialSize, Class<T> type) {
		// noinspection unchecked
		this.values = (T[])ArrayReflection.newInstance(type, initialSize);
	}

	/** Append given object to the tail. (enqueue to tail) Unless backing array needs resizing, operates in O(1) time.
	 * @param object can be null */
	public void addLast (T object) {
		T[] values = this.values;

		if (size == values.length) {
			resize(values.length << 1);// * 2
			values = this.values;
		}

		values[tail++] = object;
		if (tail == values.length) {
			tail = 0;
		}
		size++;
	}

	/** Prepend given object to the head. (enqueue to head)
	 * @see #addLast(Object)
	 * @param object can be null */
	public void addFirst (T object) {
		T[] values = this.values;

		if (size == values.length) {
			resize(values.length << 1);// * 2
			values = this.values;
		}

		int head = this.head;
		head--;
		if (head == -1) {
			head = values.length - 1;
		}
		values[head] = object;

		this.head = head;
		this.size++;
	}

	/** Optionally resize backing array so adding `additional` amount of entries won't cause resize. */
	public void ensureCapacity (int additional) {
		final int needed = size + additional;
		if (values.length < needed) {
			resize(needed);
		}
	}

	/** Resize backing array. newSize must be bigger than current size. */
	protected void resize (int newSize) {
		final T[] values = this.values;
		final int head = this.head;
		final int tail = this.tail;

		@SuppressWarnings("unchecked")
		final T[] newArray = (T[])ArrayReflection.newInstance(values.getClass().getComponentType(), newSize);
		if (head < tail) {
			// Continuous
			System.arraycopy(values, head, newArray, 0, tail - head);
		} else if (size > 0) {
			// Wrapped
			final int rest = values.length - head;
			System.arraycopy(values, head, newArray, 0, rest);
			System.arraycopy(values, 0, newArray, rest, tail);
		}
		this.values = newArray;
		this.head = 0;
		this.tail = size;
	}

	/** Remove the first item from the queue. (dequeue from head) Always O(1).
	 * @return removed object
	 * @throws NoSuchElementException when queue is empty */
	public T removeFirst () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty");
		}

		final T[] values = this.values;

		final T result = values[head];
		values[head] = null;
		head++;
		if (head == values.length) {
			head = 0;
		}
		size--;

		return result;
	}

	/** Remove the last item from the queue. (dequeue from tail)
	 * @see #removeFirst()
	 * @return removed object
	 * @throws NoSuchElementException when queue is empty */
	public T removeLast () {
		if (size == 0) {
			throw new NoSuchElementException("Deque is empty");
		}

		final T[] values = this.values;
		int tail = this.tail;
		tail--;
		if (tail == -1) {
			tail = values.length - 1;
		}
		final T result = values[tail];
		values[tail] = null;
		this.tail = tail;
		size--;

		return result;
	}

	/** Returns the first (head) item in the queue (without removing it).
	 * @see #addFirst(Object)
	 * @see #removeFirst()
	 * @throws NoSuchElementException when queue is empty */
	public T first () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty");
		}
		return values[head];
	}

	/** Returns the last (tail) item in the queue (without removing it).
	 * @see #addLast(Object)
	 * @see #removeLast()
	 * @throws NoSuchElementException when queue is empty */
	public T last () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Deque is empty");
		}
		final T[] values = this.values;
		int tail = this.tail;
		tail--;
		if (tail == -1) {
			tail = values.length - 1;
		}
		return values[tail];
	}

	/** Retrieves the value in Queue without removing it. Indexing is from the front to back, zero based. Therefore get(0) is the
	 * same as {@link #first()}.
	 * @throws NoSuchElementException when the index is negative or >= size */
	public T get (int index) {
		if (index < 0 || index >= size) {
			throw new NoSuchElementException("Index " + index + " does not exist, size is " + size);
		}
		final T[] values = this.values;

		int i = head + index;
		if (i >= values.length) {
			i -= values.length;
		}
		return values[i];
	}

	/** Removes all values from this queue. (Values in backing array are set to null to prevent memory leak, so this operates in
	 * O(n).) */
	public void clear () {
		if (size == 0) return;
		final T[] values = this.values;
		final int head = this.head;
		final int tail = this.tail;

		if (head < tail) {
			// Continuous
			for (int i = head; i < tail; i++) {
				values[i] = null;
			}
		} else {
			// Wrapped
			for (int i = head; i < values.length; i++) {
				values[i] = null;
			}
			for (int i = 0; i < tail; i++) {
				values[i] = null;
			}
		}
		this.head = 0;
		this.tail = 0;
		this.size = 0;
	}

	public String toString () {
		if (size == 0) {
			return "[]";
		}
		final T[] values = this.values;
		final int head = this.head;
		final int tail = this.tail;

		StringBuilder sb = new StringBuilder(64);
		sb.append('[');
		sb.append(values[head]);
		for (int i = (head + 1) % values.length; i != tail; i = (i + 1) % values.length) {
			sb.append(", ").append(values[i]);
		}
		sb.append(']');
		return sb.toString();
	}
}
