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

/** A {@link Pool} which keeps track of the obtained items (see {@link #obtain()}), which can be free'd all at once using the
 * {@link #flush()} method.
 * @author Xoppa */
public abstract class FlushablePool<T> extends Pool<T> {
	protected Array<T> obtained = new Array<T>();

	public FlushablePool () {
		super();
	}

	public FlushablePool (int initialCapacity) {
		super(initialCapacity);
	}

	public FlushablePool (int initialCapacity, int max) {
		super(initialCapacity, max);
	}

	@Override
	public T obtain () {
		T result = super.obtain();
		obtained.add(result);
		return result;
	}

	/** Frees all obtained instances. */
	public void flush () {
		super.freeAll(obtained);
		obtained.clear();
	}

	@Override
	public void free (T object) {
		obtained.removeValue(object, true);
		super.free(object);
	}

	@Override
	public void freeAll (Array<T> objects) {
		obtained.removeAll(objects, true);
		super.freeAll(objects);
	}
}
