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

import java.util.ArrayList;
import java.util.List;

/**
 * A pool implementation used for object instances that should get reused instead of being collected by the garbage collector.
 * Note that you should not hold on to the references of objects from this Pool once they've been marked as free by calling
 * {@link #free(Object)}.
 * 
 * @author mzechner
 * 
 * @param <T> the type
 */
@SuppressWarnings("unchecked") public class Pool<T> {
	/**
	 * Interface for an Object Factory to be used with this Pool
	 * 
	 * @author mzechner
	 * 
	 * @param <T> the type
	 */
	public interface PoolObjectFactory<T> {
		public T createObject ();
	}

	/** the list of free objects **/
	private final List<T> freeObjects = new ArrayList();
	/** the factory **/
	private final PoolObjectFactory<T> factory;
	/** maximum size of pool **/
	private final int maxSize;

	public Pool (PoolObjectFactory<T> factory, int maxSize) {
		this.factory = factory;
		this.maxSize = maxSize;
	}

	/**
	 * Creates a new object either by taking it from the free object pool or by creating a new one if there are no free objects
	 * yet.
	 * 
	 * @return the object
	 */
	public T newObject () {
		T object = null;

		if (freeObjects.size() == 0)
			object = factory.createObject();
		else
			object = freeObjects.remove(freeObjects.size() - 1);

		return object;
	}

	/**
	 * Frees the given object and returns it to the pool.
	 * @param object the object
	 */
	public void free (T object) {
		if (freeObjects.size() < maxSize) freeObjects.add(object);
	}
}
