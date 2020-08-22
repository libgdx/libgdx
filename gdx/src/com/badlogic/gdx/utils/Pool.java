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

/** A pool of objects that can be reused to avoid allocation.
 * @see Pools
 * @author Nathan Sweet */
abstract public class Pool<T> {
	/** The maximum number of objects that will be pooled. */
	public final int max;
	/** The highest number of free objects. Can be reset any time. */
	public int peak;

	private final Array<T> freeObjects;

	/** Creates a pool with an initial capacity of 16 and no maximum. */
	public Pool () {
		this(16, Integer.MAX_VALUE, false);
	}

	/** Creates a pool with the specified initial capacity and no maximum. */
	public Pool (int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE, false);
	}

	/** @param max The maximum number of free objects to store in this pool. */
	public Pool (int initialCapacity, int max) {
		this(initialCapacity, max, false);
	}

	/** @param initialCapacity The initial size of the array supporting the pool. No objects are created unless preFill is true.
	 * @param max The maximum number of free objects to store in this pool.
	 * @param preFill Whether to pre-fill the pool with objects. The number of pre-filled objects will be equal to the initial
	 *           capacity. */
	public Pool (int initialCapacity, int max, boolean preFill) {
		if (initialCapacity > max && preFill)
			throw new IllegalArgumentException("max must be larger than initialCapacity if preFill is set to true.");
		freeObjects = new Array(false, initialCapacity);
		this.max = max;
		if (preFill) {
			for (int i = 0; i < initialCapacity; i++)
				freeObjects.add(newObject());
			peak = freeObjects.size;
		}
	}

	abstract protected T newObject ();

	/** Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
	 * {@link #free(Object) freed}). */
	public T obtain () {
		return freeObjects.size == 0 ? newObject() : freeObjects.pop();
	}

	/** Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
	 * {@link #max} free objects, the specified object is reset but not added to the pool.
	 * <p>
	 * The pool does not check if an object is already freed, so the same object must not be freed multiple times. */
	public void free (T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		if (freeObjects.size < max) {
			freeObjects.add(object);
			peak = Math.max(peak, freeObjects.size);
		}
		reset(object);
	}

	/** Adds the specified number of new free objects to the pool. Usually called early on as a pre-allocation mechanism but can be
	 * used at any time.
	 *
	 * @param size the number of objects to be added */
	public void fill (int size) {
		for (int i = 0; i < size; i++)
			if (freeObjects.size < max) freeObjects.add(newObject());
		peak = Math.max(peak, freeObjects.size);
	}

	/** Called when an object is freed to clear the state of the object for possible later reuse. The default implementation calls
	 * {@link Poolable#reset()} if the object is {@link Poolable}. */
	protected void reset (T object) {
		if (object instanceof Poolable) ((Poolable)object).reset();
	}

	/** Puts the specified objects in the pool. Null objects within the array are silently ignored.
	 * <p>
	 * The pool does not check if an object is already freed, so the same object must not be freed multiple times.
	 * @see #free(Object) */
	public void freeAll (Array<T> objects) {
		if (objects == null) throw new IllegalArgumentException("objects cannot be null.");
		Array<T> freeObjects = this.freeObjects;
		int max = this.max;
		for (int i = 0; i < objects.size; i++) {
			T object = objects.get(i);
			if (object == null) continue;
			if (freeObjects.size < max) freeObjects.add(object);
			reset(object);
		}
		peak = Math.max(peak, freeObjects.size);
	}

	/** Removes all free objects from this pool. */
	public void clear () {
		freeObjects.clear();
	}

	/** The number of objects available to be obtained. */
	public int getFree () {
		return freeObjects.size;
	}

	/** Objects implementing this interface will have {@link #reset()} called when passed to {@link Pool#free(Object)}. */
	static public interface Poolable {
		/** Resets the object for reuse. Object references should be nulled and fields may be set to default values. */
		public void reset ();
	}
}
