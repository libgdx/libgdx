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

import java.util.Comparator;

/** An array that queues removal during iteration until the iteration has completed. Queues any removals done after
 * {@link #begin()} is called to occur once {@link #end()} is called. This can allow code out of your control to remove items
 * without affecting iteration. Between begin and end, most mutator methods will throw IllegalStateException. Only
 * {@link #removeIndex(int)}, {@link #removeValue(Object, boolean)}, {@link #removeRange(int, int)}, {@link #clear()}, and add
 * methods are allowed.
 * <p>
 * Note that DelayedRemovalArray is not for thread safety, only for removal during iteration.
 * <p>
 * Code using this class must not rely on items being removed immediately. Consider using {@link SnapshotArray} if this is a
 * problem.
 * @author Nathan Sweet */
public class DelayedRemovalArray<T> extends Array<T> {
	private int iterating;
	private IntArray remove = new IntArray(0);
	private int clear;

	public DelayedRemovalArray () {
		super();
	}

	public DelayedRemovalArray (Array array) {
		super(array);
	}

	public DelayedRemovalArray (boolean ordered, int capacity, Class arrayType) {
		super(ordered, capacity, arrayType);
	}

	public DelayedRemovalArray (boolean ordered, int capacity) {
		super(ordered, capacity);
	}

	public DelayedRemovalArray (boolean ordered, T[] array, int startIndex, int count) {
		super(ordered, array, startIndex, count);
	}

	public DelayedRemovalArray (Class arrayType) {
		super(arrayType);
	}

	public DelayedRemovalArray (int capacity) {
		super(capacity);
	}

	public DelayedRemovalArray (T[] array) {
		super(array);
	}

	public void begin () {
		iterating++;
	}

	public void end () {
		if (iterating == 0) throw new IllegalStateException("begin must be called before end.");
		iterating--;
		if (iterating == 0) {
			if (clear > 0 && clear == size) {
				remove.clear();
				clear();
			} else {
				for (int i = 0, n = remove.size; i < n; i++) {
					int index = remove.pop();
					if (index >= clear) removeIndex(index);
				}
				for (int i = clear - 1; i >= 0; i--)
					removeIndex(i);
			}
			clear = 0;
		}
	}

	private void remove (int index) {
		if (index < clear) return;
		for (int i = 0, n = remove.size; i < n; i++) {
			int removeIndex = remove.get(i);
			if (index == removeIndex) return;
			if (index < removeIndex) {
				remove.insert(i, index);
				return;
			}
		}
		remove.add(index);
	}

	public boolean removeValue (T value, boolean identity) {
		if (iterating > 0) {
			int index = indexOf(value, identity);
			if (index == -1) return false;
			remove(index);
			return true;
		}
		return super.removeValue(value, identity);
	}

	public T removeIndex (int index) {
		if (iterating > 0) {
			remove(index);
			return get(index);
		}
		return super.removeIndex(index);
	}

	public void removeRange (int start, int end) {
		if (iterating > 0) {
			for (int i = end; i >= start; i--)
				remove(i);
		} else
			super.removeRange(start, end);
	}

	public void clear () {
		if (iterating > 0) {
			clear = size;
			return;
		}
		super.clear();
	}

	public void set (int index, T value) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.set(index, value);
	}

	public void insert (int index, T value) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.insert(index, value);
	}

	public void swap (int first, int second) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.swap(first, second);
	}

	public T pop () {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		return super.pop();
	}

	public void sort () {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.sort();
	}

	public void sort (Comparator<? super T> comparator) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.sort(comparator);
	}

	public void reverse () {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.reverse();
	}

	public void shuffle () {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.shuffle();
	}

	public void truncate (int newSize) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		super.truncate(newSize);
	}

	public T[] setSize (int newSize) {
		if (iterating > 0) throw new IllegalStateException("Invalid between begin/end.");
		return super.setSize(newSize);
	}

	/** @see #DelayedRemovalArray(Object[]) */
	static public <T> DelayedRemovalArray<T> with (T... array) {
		return new DelayedRemovalArray(array);
	}
}
