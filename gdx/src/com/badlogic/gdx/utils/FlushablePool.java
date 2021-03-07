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
 * A {@link Pool} which keeps track of the obtained items (see {@link #obtain()}), which can be free'd all at once using the
 * {@link #flush()} method.
 *
 * @author Xoppa
 */
public abstract class FlushablePool<T> extends Pool<T> {
    protected Array<T> obtained = new Array<T>();

    /**
     * Constructor of FlushablePool to create a new FlushablePool with
     * an initial capacity of 16 and a max of INTEGER.MAX_VALUE.
     */
    public FlushablePool () {
        super();
    }

    /**
     * Constructor of FlushablePool to create a new FlushablePool with
     * a custom capacity and a max of INTEGER.MAX_VALUE.
     *
     * @param initialCapacity The initialCapacity you want to give the FlushablePool
     */
    public FlushablePool (int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor of FlushablePool to create a new FlushablePool with
     * a custom capacity and max of INTEGER.MAX_VALUE.
     *
     * @param initialCapacity The initialCapacity you want to give the FlushablePool
     * @param max             The max size of the FlushablePool
     */
    public FlushablePool (int initialCapacity, int max) {
        super(initialCapacity, max);
    }

    /**
     * Method that overrides the default obtain, after obtaining
     * it keeps track of the obtained elements inside the obtained array.
     *
     * @return The obtained value.
     */
    @Override
    public T obtain () {
        T result = super.obtain();
        obtained.add(result);
        return result;
    }

    /**
     * Frees all obtained instances.
     */
    public void flush () {
        super.freeAll(obtained);
        obtained.clear();
    }

    /**
     * Method to free a single object from the obtained array.
     *
     * @param object The object you want to free from the obtained array.
     */
    @Override
    public void free (T object) {
        obtained.removeValue(object, true);
        super.free(object);
    }

    /**
     * Method to free an array objects from the obtained array.
     *
     * @param objects The objects you want to free from the obtained array.
     */
    @Override
    public void freeAll (Array<T> objects) {
        obtained.removeAll(objects, true);
        super.freeAll(objects);
    }
}
