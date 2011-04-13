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

/**
 * A pool of objects that can be resused to avoid allocation.
 * @author Nathan Sweet
 */
abstract public class Pool<T> {
	public final int max;

	private final Array<T> freeObjects;

	/**
	 * Creates a pool with an initial capacity of 16 and no maximum.
	 */
	public Pool () {
		this(16, Integer.MAX_VALUE);
	}

	/**
	 * Creates a pool with the specified initial capacity and no maximum.
	 */
	public Pool (int initialCapacity) {
		this(initialCapacity, Integer.MAX_VALUE);
	}

	/**
	 * @param max The maximum number of free objects to store in this pool.
	 */
	public Pool (int initialCapacity, int max) {
		freeObjects = new Array(false, initialCapacity);
		this.max = max;
	}

	abstract protected T newObject ();

	/**
	 * Returns an object from this pool. The object may be new (from {@link #newObject()}) or reused (previously
	 * {@link #free(Object) freed}).
	 */
	public T obtain () {
		return freeObjects.size == 0 ? newObject() : freeObjects.pop();
	}

	/**
	 * Puts the specified object in the pool, making it eligible to be returned by {@link #obtain()}. If the pool already contains
	 * {@link #max} free objects, the specified object is ignored.
	 */
	public void free (T object) {
		if (object == null) throw new IllegalArgumentException("object cannot be null.");
		if (freeObjects.size < max) freeObjects.add(object);
	}

	/**
	 * Puts the specified objects in the pool.
	 * @see #free(Object)
	 */
	public void free (Array<T> objects) {
		for (int i = 0, n = Math.min(objects.size, max - freeObjects.size); i < n; i++)
			freeObjects.add(objects.get(i));
	}
	
	/**
	 * Removes all free objects from this pool.
	 */
	public void clear() {
		freeObjects.clear();
	}
}
