
package com.badlogic.gdx.utils;

import java.util.Comparator;

/** Queues any removals done after {@link #begin()} is called to occur once {@link #end()} is called. This can be allow code out of
 * your control to remove items without affecting iteration. Between begin and end, most mutator methods will throw
 * IllegalStateException. Only {@link #removeIndex(int)}, {@link #removeValue(Object, boolean)}, and add methods are allowed. */
public class DelayedRemovalArray<T> extends Array<T> {
	private boolean iterating;
	private IntArray remove = new IntArray(0);

	public DelayedRemovalArray () {
		super();
	}

	public DelayedRemovalArray (Array array) {
		super(array);
	}

	public DelayedRemovalArray (boolean ordered, int capacity, Class<T> arrayType) {
		super(ordered, capacity, arrayType);
	}

	public DelayedRemovalArray (boolean ordered, int capacity) {
		super(ordered, capacity);
	}

	public DelayedRemovalArray (boolean ordered, T[] array) {
		super(ordered, array);
	}

	public DelayedRemovalArray (Class<T> arrayType) {
		super(arrayType);
	}

	public DelayedRemovalArray (int capacity) {
		super(capacity);
	}

	public DelayedRemovalArray (T[] array) {
		super(array);
	}

	public void begin () {
		iterating = true;
	}

	public void end () {
		iterating = false;
		for (int i = 0, n = remove.size; i < n; i++)
			removeIndex(remove.pop());
	}

	private void remove (int index) {
		for (int i = 0, n = remove.size; i < n; i++) {
			if (index < remove.get(i)) {
				remove.insert(i, index);
				return;
			}
		}
		remove.add(index);
	}

	public boolean removeValue (T value, boolean identity) {
		if (iterating) {
			int index = indexOf(value, identity);
			if (index == -1) return false;
			remove(index);
			return true;
		}
		return super.removeValue(value, identity);
	}

	public T removeIndex (int index) {
		if (iterating) {
			remove(index);
			return get(index);
		}
		return super.removeIndex(index);
	}

	public void set (int index, T value) {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.set(index, value);
	}

	public void insert (int index, T value) {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.insert(index, value);
	}

	public void swap (int first, int second) {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.swap(first, second);
	}

	public T pop () {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		return super.pop();
	}

	public void clear () {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.clear();
	}

	public void sort () {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.sort();
	}

	public void sort (Comparator<T> comparator) {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.sort(comparator);
	}

	public void reverse () {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.reverse();
	}

	public void shuffle () {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.shuffle();
	}

	public void truncate (int newSize) {
		if (iterating) throw new IllegalStateException("Invalid during snapshot.");
		super.truncate(newSize);
	}
}
