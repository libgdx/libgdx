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

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class Field {
	final String name;
	final CachedTypeLookup enclosingType;
	final CachedTypeLookup type;
	final boolean isFinal;
	final boolean isDefaultAccess;
	final boolean isPrivate;
	final boolean isProtected;
	final boolean isPublic;
	final boolean isStatic;
	final boolean isTransient;
	final boolean isVolatile;
	final int getter;
	final int setter;
	final CachedTypeLookup[] elementTypes;
	final Annotation[] annotations;

	Field (String name, Class enclosingType, Class type, boolean isFinal, boolean isDefaultAccess, boolean isPrivate,
		boolean isProtected, boolean isPublic, boolean isStatic, boolean isTransient, boolean isVolatile, int getter, int setter,
		Class[] elementTypes, Annotation[] annotations) {
		this.name = name;
		this.enclosingType = new CachedTypeLookup(enclosingType);
		this.type = new CachedTypeLookup(type);
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

		CachedTypeLookup[] tmp = null;
		if (elementTypes != null) {
			tmp = new CachedTypeLookup[elementTypes.length];
			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = new CachedTypeLookup(elementTypes[i]);
			}
		}
		this.elementTypes = tmp;

		this.annotations = annotations != null ? annotations : new Annotation[] {};
	}

	public Object get (Object obj) throws IllegalAccessException {
		return ReflectionCache.getFieldValue(this, obj);
	}

	public void set (Object obj, Object value) throws IllegalAccessException {
		ReflectionCache.setFieldValue(this, obj, value);
	}

	public Type getElementType (int index) {
		if (elementTypes != null && index >= 0 && index < elementTypes.length) return elementTypes[index].getType();
		return null;
	}

	public String getName () {
		return name;
	}

	public Type getEnclosingType () {
		return enclosingType.getType();
	}

	public Type getType () {
		return type.getType();
	}

	public boolean isSynthetic () {
		return false;
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

	public Annotation[] getDeclaredAnnotations () {
		return annotations;
	}

	@Override
	public String toString () {
		return "Field [name=" + name + ", enclosingType=" + enclosingType + ", type=" + type + ", isFinal=" + isFinal
			+ ", isDefaultAccess=" + isDefaultAccess + ", isPrivate=" + isPrivate + ", isProtected=" + isProtected + ", isPublic="
			+ isPublic + ", isStatic=" + isStatic + ", isTransient=" + isTransient + ", isVolatile=" + isVolatile + ", getter="
			+ getter + ", setter=" + setter + ", elementTypes=" + Arrays.toString(elementTypes) + ", annotations="
			+ Arrays.toString(annotations) + "]";
	}
}
