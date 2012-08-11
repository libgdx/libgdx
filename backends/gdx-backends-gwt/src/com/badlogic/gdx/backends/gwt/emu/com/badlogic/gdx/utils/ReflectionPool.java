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

import java.lang.reflect.Constructor;

import com.badlogic.gwtref.client.ReflectionCache;
import com.badlogic.gwtref.client.Type;

/** Pool that creates new instances of a type using reflection. The type must have a zero argument constructor.
 * {@link Constructor#setAccessible(boolean)} will be used if the class and/or constructor is not visible.
 * @author Nathan Sweet */
public class ReflectionPool<T> extends Pool<T> {
	private final Class<T> type;

	public ReflectionPool (Class<T> type) {
		this.type = type;
	}

	public ReflectionPool (Class<T> type, int initialCapacity, int max) {
		super(initialCapacity, max);
		this.type = type;
	}

	public ReflectionPool (Class<T> type, int initialCapacity) {
		super(initialCapacity);
		this.type = type;
	}

	protected T newObject () {
		Type t = ReflectionCache.getType(type);
		try {
			return (T)t.newInstance();
		} catch (Exception ex) {
			throw new GdxRuntimeException("Unable to create new instance: " + type.getName(), ex);
		}
	}
}
