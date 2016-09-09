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

package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;

/** Provides information about, and access to, a single constructor for a Class.
 * @author nexsoftware */
public final class Constructor {

	private final java.lang.reflect.Constructor constructor;

	Constructor (java.lang.reflect.Constructor constructor) {
		this.constructor = constructor;
	}

	/** Returns an array of Class objects that represent the formal parameter types, in declaration order, of the constructor. */
	public Class[] getParameterTypes () {
		return constructor.getParameterTypes();
	}

	/** Returns the Class object representing the class or interface that declares the constructor. */
	public Class getDeclaringClass () {
		return constructor.getDeclaringClass();
	}

	public boolean isAccessible () {
		return constructor.isAccessible();
	}

	public void setAccessible (boolean accessible) {
		constructor.setAccessible(accessible);
	}

	/** Uses the constructor to create and initialize a new instance of the constructor's declaring class, with the supplied
	 * initialization parameters. */
	public Object newInstance (Object... args) throws ReflectionException {
		try {
			return constructor.newInstance(args);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Illegal argument(s) supplied to constructor for class: " + getDeclaringClass().getName(),
				e);
		} catch (InstantiationException e) {
			throw new ReflectionException("Could not instantiate instance of class: " + getDeclaringClass().getName(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Could not instantiate instance of class: " + getDeclaringClass().getName(), e);
		} catch (InvocationTargetException e) {
			throw new ReflectionException("Exception occurred in constructor for class: " + getDeclaringClass().getName(), e);
		}
	}

}
