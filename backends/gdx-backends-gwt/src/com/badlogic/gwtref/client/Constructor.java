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

package com.badlogic.gwtref.client;

/** The default constructor for the enclosing type.
 * @author mzechner */
public class Constructor {
	final Class enclosingType;
	boolean isAccessible = false;

	Constructor (Class enclosingType) {
		this.enclosingType = enclosingType;
	}

	/** @return a new instance of the enclosing type of this constructor. */
	public Object newInstance () {
		return ReflectionCache.getType(enclosingType).newInstance();
	}

	/** @return the enclosing type this constructor belongs to. */
	public Type getEnclosingType () {
		return ReflectionCache.getType(enclosingType);
	}

	public boolean isAccessible () {
		return isAccessible;
	}

	public void setAccessible (boolean accessible) throws SecurityException {
		isAccessible = accessible;
	}
}
