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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** Provides information about, and access to, a single field of a class or interface.
 * @author nexsoftware */
public final class Field {

	private final java.lang.reflect.Field field;

	Field (java.lang.reflect.Field field) {
		this.field = field;
	}

	/** Returns the name of the field. */
	public String getName () {
		return field.getName();
	}

	/** Returns a Class object that identifies the declared type for the field. */
	public Class getType () {
		return field.getType();
	}

	/** Returns the Class object representing the class or interface that declares the field. */
	public Class getDeclaringClass () {
		return field.getDeclaringClass();
	}

	public boolean isAccessible () {
		return field.isAccessible();
	}

	public void setAccessible (boolean accessible) {
		field.setAccessible(accessible);
	}

	/** Return true if the field does not include any of the {@code private}, {@code protected}, or {@code public} modifiers. */
	public boolean isDefaultAccess () {
		return !isPrivate() && !isProtected() && !isPublic();
	}

	/** Return true if the field includes the {@code final} modifier. */
	public boolean isFinal () {
		return Modifier.isFinal(field.getModifiers());
	}

	/** Return true if the field includes the {@code private} modifier. */
	public boolean isPrivate () {
		return Modifier.isPrivate(field.getModifiers());
	}

	/** Return true if the field includes the {@code protected} modifier. */
	public boolean isProtected () {
		return Modifier.isProtected(field.getModifiers());
	}

	/** Return true if the field includes the {@code public} modifier. */
	public boolean isPublic () {
		return Modifier.isPublic(field.getModifiers());
	}

	/** Return true if the field includes the {@code static} modifier. */
	public boolean isStatic () {
		return Modifier.isStatic(field.getModifiers());
	}

	/** Return true if the field includes the {@code transient} modifier. */
	public boolean isTransient () {
		return Modifier.isTransient(field.getModifiers());
	}

	/** Return true if the field includes the {@code volatile} modifier. */
	public boolean isVolatile () {
		return Modifier.isVolatile(field.getModifiers());
	}

	/** Return true if the field is a synthetic field. */
	public boolean isSynthetic () {
		return field.isSynthetic();
	}

	/** If the type of the field is parameterized, returns the Class object representing the parameter type at the specified index,
	 * null otherwise. */
	public Class getElementType (int index) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
			if (actualTypes.length - 1 >= index) {
				Type actualType = actualTypes[index];
				if (actualType instanceof Class)
					return (Class)actualType;
				else if (actualType instanceof ParameterizedType)
					return (Class)((ParameterizedType)actualType).getRawType();
				else if (actualType instanceof GenericArrayType) {
					Type componentType = ((GenericArrayType)actualType).getGenericComponentType();
					if (componentType instanceof Class) return ArrayReflection.newInstance((Class)componentType, 0).getClass();
				}
			}
		}
		return null;
	}

	/** Returns true if the field includes an annotation of the provided class type. */
	public boolean isAnnotationPresent (Class<? extends java.lang.annotation.Annotation> annotationType) {
		return field.isAnnotationPresent(annotationType);
	}

	/** Returns an array of {@link Annotation} objects reflecting all annotations declared by this field,
	 * or an empty array if there are none. Does not include inherited annotations. */
	public Annotation[] getDeclaredAnnotations () {
		java.lang.annotation.Annotation[] annotations = field.getDeclaredAnnotations();
		Annotation[] result = new Annotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			result[i] = new Annotation(annotations[i]);
		}
		return result;
	}

	/** Returns an {@link Annotation} object reflecting the annotation provided, or null of this field doesn't
	 * have such an annotation. This is a convenience function if the caller knows already which annotation
	 * type he's looking for. */
	public Annotation getDeclaredAnnotation (Class<? extends java.lang.annotation.Annotation> annotationType) {
		java.lang.annotation.Annotation[] annotations = field.getDeclaredAnnotations();
		if (annotations == null) {
			return null;
		}
		for (java.lang.annotation.Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationType)) {
				return new Annotation(annotation);
			}
		}
		return null;
	}

	/** Returns the value of the field on the supplied object. */
	public Object get (Object obj) throws ReflectionException {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Object is not an instance of " + getDeclaringClass(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Illegal access to field: " + getName(), e);
		}
	}

	/** Sets the value of the field on the supplied object. */
	public void set (Object obj, Object value) throws ReflectionException {
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Argument not valid for field: " + getName(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Illegal access to field: " + getName(), e);
		}
	}

}
