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

/** Default Pool implementation that creates a new instances of a type based on a supplier. */
public class DefaultPool<T> extends Pool<T> {

	private final PoolSupplier<T> poolTypeSupplier;

	public DefaultPool (PoolSupplier<T> supplier) {
		this(supplier, 16, Integer.MAX_VALUE);
	}

	public DefaultPool (PoolSupplier<T> supplier, int initialCapacity) {
		this(supplier, initialCapacity, Integer.MAX_VALUE);
	}

	public DefaultPool (PoolSupplier<T> supplier, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.poolTypeSupplier = supplier;
	}

	@Override
	protected T newObject () {
		return poolTypeSupplier.get();
	}

	/** An interface that is used to create objects for the Pool. Even tho not annotated with "FunctionalInterface", it can act as
	 * one. <br>
	 * You can use a constructor reference or lambda as a PoolSupplier, such as with {@code MyClass::new} or
	 * {@code () -> new MyClass()}. */
	public interface PoolSupplier<T> {
		T get ();
	}
}
