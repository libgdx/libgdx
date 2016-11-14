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

/** Guarantees that array entries provided by {@link #begin()} between indexes 0 and {@link #size} at the time begin was called
 * will not be modified until {@link #end()} is called. If modification of the SnapshotArray occurs between begin/end, the backing
 * array is copied prior to the modification, ensuring that the backing array that was returned by {@link #begin()} is unaffected.
 * To avoid allocation, an attempt is made to reuse any extra array created as a result of this copy on subsequent copies.
 * <p>
 * It is suggested iteration be done in this specific way:
 * 
 * <pre>
 * SnapshotArray array = new SnapshotArray();
 * // ...
 * Object[] items = array.begin();
 * for (int i = 0, n = array.size; i &lt; n; i++) {
 * 	Object item = items[i];
 * 	// ...
 * }
 * array.end();
 * </pre>
 * 
 * @author Nathan Sweet */
public class SnapshotArray<T> extends Array<T> {
	private T[] snapshot, recycled;
	private int snapshots;

	public SnapshotArray () {
		super();
	}

	public SnapshotArray (Array array) {
		super(array);
	}

	public SnapshotArray (boolean ordered, int capacity, Class arrayType) {
		super(ordered, capacity, arrayType);
	}

	public SnapshotArray (boolean ordered, int capacity) {
		super(ordered, capacity);
	}

	public SnapshotArray (boolean ordered, T[] array, int startIndex, int count) {
		super(ordered, array, startIndex, count);
	}

	public SnapshotArray (Class arrayType) {
		super(arrayType);
	}

	public SnapshotArray (int capacity) {
		super(capacity);
	}

	public SnapshotArray (T[] array) {
		super(array);
	}

	/** Returns the backing array, which is guaranteed to not be modified before {@link #end()}. */
	public T[] begin () {
		modified();
		snapshot = items;
		snapshots++;
		return items;
	}

	/** Releases the guarantee that the array returned by {@link #begin()} won't be modified. */
	public void end () {
		snapshots = Math.max(0, snapshots - 1);
		if (snapshot == null) return;
		if (snapshot != items && snapshots == 0) {
			// The backing array was copied, keep around the old array.
			recycled = snapshot;
			for (int i = 0, n = recycled.length; i < n; i++)
				recycled[i] = null;
		}
		snapshot = null;
	}

	private void modified () {
		if (snapshot == null || snapshot != items) return;
		// Snapshot is in use, copy backing array to recycled array or create new backing array.
		if (recycled != null && recycled.length >= size) {
			System.arraycopy(items, 0, recycled, 0, size);
			items = recycled;
			recycled = null;
		} else
			resize(items.length);
	}

	public void set (int index, T value) {
		modified();
		super.set(index, value);
	}

	public void insert (int index, T value) {
		modified();
		super.insert(index, value);
	}

	public void swap (int first, int second) {
		modified();
		super.swap(first, second);
	}

	public boolean removeValue (T value, boolean identity) {
		modified();
		return super.removeValue(value, identity);
	}

	public T removeIndex (int index) {
		modified();
		return super.removeIndex(index);
	}

	public void removeRange (int start, int end) {
		modified();
		super.removeRange(start, end);
	}

	public boolean removeAll (Array<? extends T> array, boolean identity) {
		modified();
		return super.removeAll(array, identity);
	}

	public T pop () {
		modified();
		return super.pop();
	}

	public void clear () {
		modified();
		super.clear();
	}

	public void sort () {
		modified();
		super.sort();
	}

	public void sort (Comparator<? super T> comparator) {
		modified();
		super.sort(comparator);
	}

	public void reverse () {
		modified();
		super.reverse();
	}

	public void shuffle () {
		modified();
		super.shuffle();
	}

	public void truncate (int newSize) {
		modified();
		super.truncate(newSize);
	}

	public T[] setSize (int newSize) {
		modified();
		return super.setSize(newSize);
	}

	/** @see #SnapshotArray(Object[]) */
	static public <T> SnapshotArray<T> with (T... array) {
		return new SnapshotArray(array);
	}
}
