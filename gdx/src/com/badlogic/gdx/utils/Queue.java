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

/** Automatically resizing FIFO queue. Values in backing queue rotate around so push and pop are O(1) (unless resizing in push).
 * Also provides deque functionality via {@link #popLast()} and {@link #pushFront(Object)}. This collection is not thread-safe. */
public class Queue<T> {

	/** Contains values waiting in this queue. Pop and push indices go in circle around this array, wrapping at the end. */
	protected T[] values;

	/** Index to dequeue/remove from. Logically smaller than tail. Unless empty, it points to a valid element inside queue. */
	protected int head = 0;

	/** Index to enqueue/add to. Logically bigger than head. Usually points to an empty values position, but when full (size ==
	 * values.length) points to the head. */
	protected int tail = 0;

	/** Amount of things currently enqueued. */
	protected int size = 0;

	/** Non resizable queues will have limited capacity.
	 * @see Queue#push(Object) */
	public boolean resizable;

	/** Create a new Queue which can hold 15 values without resizing. */
	public Queue () {
		this(16, true);
	}

	/** Create a new Queue which can hold `initialSize` values without resizing. */
	public Queue (int initialSize, boolean resizable) {
		// noinspection unchecked
		this.values = (T[])new Object[initialSize];
		this.resizable = resizable;
	}

	/** Create a new Queue which can hold `initialSize` values without resizing. This creates backing array of correct type via
	 * reflection. Use this only if you are accessing backing array directly.
	 * <p/>
	 * NOTE: Worth using only if you know what are you doing. */
	public Queue (int initialSize, boolean resizable, Class<T> type) {
		// noinspection unchecked
		this.values = (T[])ArrayReflection.newInstance(type, initialSize);
		this.resizable = resizable;
	}

	/** Enqueue given object (append after tail). Unless backing array needs resizing, operates in O(1) time.
	 * @param object can be null
	 * @throws IllegalStateException when full and not resizable */
	public void push (T object) {
		T[] values = this.values;

		if (size == values.length) {
			if (!resizable) {
				throw new IllegalStateException("Queue is full");
			} else {
				resize(values.length << 1);// *2
				values = this.values;
			}
		}

		values[tail++] = object;
		if (tail == values.length) {
			tail = 0;
		}
		size++;
	}

	/** Add given object to the deque, prepending to head. Is very similar to {@link #push(Object)} but the object is added to the
	 * other side. Can be thought of as jumping the queue.
	 * @param object can be null
	 * @throws IllegalStateException when full and not resizable */
	public void pushFront (T object) {
		T[] values = this.values;

		if (size == values.length) {
			if (!resizable) {
				throw new IllegalStateException("Deque is full");
			} else {
				resize(values.length << 1);// *2
				values = this.values;
			}
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

	/** Optionally resize backing array so adding `additional` amount of entries won't cause resize.
	 * <p/>
	 * NOTE: This WILL resize non-resizable arrays. */
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

	/** Dequeue next object in queue
	 * @return next item in queue
	 * @throws NoSuchElementException when queue is empty */
	public T pop () {
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

	/** Remove item which was added last to this deque. Works the same way as {@link #pop()}, but from the other side.
	 * @return last item in queue
	 * @throws NoSuchElementException when queue is empty */
	public T popLast () {
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

	/** Returns the next item in queue.
	 * @throws NoSuchElementException when queue is empty */
	public T peek () {
		if (size == 0) {
			// Underflow
			throw new NoSuchElementException("Queue is empty");
		}
		return values[head];
	}

	/** @return the last item in deque.
	 * @throws NoSuchElementException when the deque is empty */
	public T peekLast () {
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
	 * same as peek().
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

	/** @return true if this queue can't hold any more values (always false when resizable) */
	public boolean isFull () {
		return !resizable && size == values.length;
	}

	/** @return amount of values waiting in this queue */
	public int size () {
		return size;
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
