/*
 * Copyright 2011 Rod Hyde (rod@badlydrawngames.com)
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

package com.badlydrawngames.general;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/** Pool helper functions.
 * @author Rod */
public class Pools {
	private Pools () {
	};

	/** Frees the items in an array to a pool.
	 * @param <T> the type of item allocated in the array.
	 * @param array the array of items to free.
	 * @param pool the pool that the items are to be released to. */
	public static <T> void freeArrayToPool (Array<T> array, Pool<T> pool) {
		pool.free(array);
		array.clear();
	}

	/** Creates an array from a pool, freeing its items if it already exists.
	 * @param <T> the type of item allocated in the array.
	 * @param array the array of items to (re)create.
	 * @param pool the pool that the items are to be allocated from / released to.
	 * @param size the array's capacity.
	 * @return */
	@SuppressWarnings("unchecked")
	public static <T> Array<T> makeArrayFromPool (Array<T> array, Pool<T> pool, int size) {
		if (array == null) {
			// Do this so that array.items can be used.
			T t = pool.obtain();
			array = new Array<T>(false, size, (Class<T>)t.getClass());
			pool.free(t);
		} else {
			freeArrayToPool(array, pool);
		}
		return array;
	}
}
