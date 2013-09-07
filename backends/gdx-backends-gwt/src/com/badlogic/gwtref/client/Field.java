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

import java.security.AccessControlException;

public class Field {
	final String name;
	final Class enclosingType;
	final Class type;
	final boolean isFinal;
	final boolean isDefaultAccess;
	final boolean isPrivate;
	final boolean isProtected;
	final boolean isPublic;
	final boolean isStatic;
	final boolean isTransient;
	final boolean isVolatile;
	boolean accessible;
	final String getter;
	final String setter;

	Field (String name, Class enclosingType, Class type, boolean isFinal, boolean isDefaultAccess, boolean isPrivate,
		boolean isProtected, boolean isPublic, boolean isStatic, boolean isTransient, boolean isVolatile, String getter,
		String setter) {
		this.name = name;
		this.enclosingType = enclosingType;
		this.type = type;
		this.isFinal = isFinal;
		this.isDefaultAccess = isDefaultAccess;
		this.isPrivate = isPrivate;
		this.isProtected = isProtected;
		this.isPublic = isPublic;
		this.isStatic = isStatic;
		this.isTransient = isTransient;
		this.isVolatile = isVolatile;
		this.getter = getter;
		this.setter = setter;
		accessible = isPublic;
	}

	public Object get (Object obj) throws IllegalAccessException {
		return ReflectionCache.instance.get(this, obj);
	}

	public void set (Object obj, Object value) throws IllegalAccessException {
		ReflectionCache.instance.set(this, obj, value);
	}

	public String getName () {
		return name;
	}

	public Type getEnclosingType () {
		return ReflectionCache.getType(enclosingType);
	}

	public Type getType () {
		return ReflectionCache.getType(type);
	}

	public boolean isSynthetic () {
		return false;
	}

	public boolean isAccessible () {
		return accessible;
	}

	public void setAccessible (boolean accessible) throws AccessControlException {
		this.accessible = accessible;
	}

	public boolean isFinal () {
		return isFinal;
	}

	public boolean isDefaultAccess () {
		return isDefaultAccess;
	}

	public boolean isPrivate () {
		return isPrivate;
	}

	public boolean isProtected () {
		return isProtected;
	}

	public boolean isPublic () {
		return isPublic;
	}

	public boolean isStatic () {
		return isStatic;
	}

	public boolean isTransient () {
		return isTransient;
	}

	public boolean isVolatile () {
		return isVolatile;
	}

	@Override
	public String toString () {
		return "Field [name=" + name + ", enclosingType=" + enclosingType + ", type=" + type + ", isFinal=" + isFinal
			+ ", isDefaultAccess=" + isDefaultAccess + ", isPrivate=" + isPrivate + ", isProtected=" + isProtected + ", isPublic="
			+ isPublic + ", isStatic=" + isStatic + ", isTransient=" + isTransient + ", isVolatile=" + isVolatile + ", accessible="
			+ accessible + ", getter=" + getter + ", setter=" + setter + "]";
	}
}
