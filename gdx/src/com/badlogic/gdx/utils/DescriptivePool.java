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

/** A pool of objects that can be reused to avoid allocation. However, this implements
 * {@link #onFree(DescriptivePoolable) and {@link #onObtain(DescriptivePoolable)}}
 * @see Pools
 * @author Carter Gale */
abstract class DescriptivePool<T extends DescriptivePool.DescriptivePoolable> extends Pool<T> {
	/** Creates a pool with an initial capacity of 16 and no maximum. */
	public DescriptivePool () {
                super();
        }

        /** Creates a pool with the specified initial capacity and no maximum. */
        public DescriptivePool (int initialCapacity) {
                super(initialCapacity);
        }

        /** @param max The maximum number of free objects to store in this pool. */
        public DescriptivePool (int initialCapacity, int max) {
                super(initialCapacity, max);
        }

        @Override
        public T obtain () {
		T obj = super.obtain();
		obj.onObtain();
		return obj;
	}

    	@Override
    	public void free (T object){
        	super.free(object);
		object.onFree();
    	}
	
	@Override
	public void freeAll (Array<T> objects) {
		super.freeAll(objects);
		for (int i = 0; i < objects.size; i++) {
			T object = objects.get(i);
			if (object == null) continue;
			object.onFree();
		}
	}


    	/** Objects implementing this interface will have {@link #reset()} called when passed to {@link Pool#free(Object)}. */
    	static public interface DescriptivePoolable extends Poolable {
		/** Called ONLY when an object is added to the pool.
      	   	* Note: Will call after {@link #reset()} in {@link Pool#free(Object)} related methods */
        	public void onFree ();

        	/** Called whenever the object is removed from the pool. */
        	public void onObtain ();
    	}
}
