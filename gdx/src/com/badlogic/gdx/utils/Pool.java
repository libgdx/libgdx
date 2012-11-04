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
 * @author Nathan Sweet */
abstract public class Pool<T> {
	public final int max;

	private final Array<T> freeObjects;

	/** Creates a pool with an initial capacity of 16 and no maximum. */
	public Pool () {
		this(16, Integer.MAX_VALUE);
	}

	/** Creates a pool with the specified initial capacity and no maximum. */
	public Pool (int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE);
	}

	/** @param max The maximum number of free objects to store in this pool. */
	public Pool (int initialCapacity, int max) {
		freeObjects = new Array(false, initialCapacity);
		this.max = max;
	}

	abstract protected T newObject ();

	/** Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
	 * {@link #free(Object) freed}). */
	public T obtain () {
		return freeObjects.size == 0 ? newObject() : freeObjects.pop();
	}

	/** Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
	 * {@link #max} free objects, the specified object is reset but not added to the pool. */
	public void free (T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		if (freeObjects.size < max) freeObjects.add(object);
		if (object instanceof Poolable) ((Poolable)object).reset();
	}

	/** Puts the specified objects in the pool. Null objects within the array are silently ignored.
	 * @see #free(Object) */
	public void freeAll (Array<T> objects) {
		if (objects == null) throw new IllegalArgumentException("object cannot be null.");
		for (int i = 0; i < objects.size; i++) {
			T object = objects.get(i);
			if (object == null) continue;
			if (freeObjects.size < max) freeObjects.add(object);
			if (object instanceof Poolable) ((Poolable)object).reset();
		}
	}

	/** Removes all free objects from this pool. */
	public void clear () {
		freeObjects.clear();
	}

	/** Objects implementing this interface will have {@link #reset()} called when passed to {@link #free(Object)}. */
	static public interface Poolable {
		/** Resets the object for reuse. Object references should be nulled and fields may be set to default values. */
		public void reset ();
	}
}
