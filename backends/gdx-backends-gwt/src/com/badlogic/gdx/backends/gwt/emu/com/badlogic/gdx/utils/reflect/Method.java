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
import java.lang.reflect.Modifier;

import com.badlogic.gwtref.client.Parameter;

/** Provides information about, and access to, a single method on a class or interface.
 * @author nexsoftware */
public final class Method {

	private final com.badlogic.gwtref.client.Method method;

	Method (com.badlogic.gwtref.client.Method method) {
		this.method = method;
	}

	/** Returns the name of the method. */
	public String getName () {
		return method.getName();
	}

	/** Returns a Class object that represents the formal return type of the method. */
	public Class getReturnType () {
		return method.getReturnType();
	}

	/** Returns an array of Class objects that represent the formal parameter types, in declaration order, of the method. */
	public Class[] getParameterTypes () {
		Parameter[] parameters = method.getParameters();
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0, j = parameters.length; i < j; i++) {
			parameterTypes[i] = parameters[i].getClazz();
		}
		return parameterTypes;
	}

	/** Returns the Class object representing the class or interface that declares the method. */
	public Class getDeclaringClass () {
		return method.getEnclosingType();
	}

	public boolean isAccessible () {
		return method.isPublic();
	}

	public void setAccessible (boolean accessible) {
		// NOOP in GWT
	}

	/** Return true if the method includes the {@code abstract} modifier. */
	public boolean isAbstract () {
		return method.isAbstract();
	}

	/** Return true if the method does not include any of the {@code private}, {@code protected}, or {@code public} modifiers. */
	public boolean isDefaultAccess () {
		return !isPrivate() && !isProtected() && !isPublic();
	}

	/** Return true if the method includes the {@code final} modifier. */
	public boolean isFinal () {
		return method.isFinal();
	}

	/** Return true if the method includes the {@code private} modifier. */
	public boolean isPrivate () {
		return method.isPrivate();
	}

	/** Return true if the method includes the {@code protected} modifier. */
	public boolean isProtected () {
		return method.isProtected();
	}

	/** Return true if the method includes the {@code public} modifier. */
	public boolean isPublic () {
		return method.isPublic();
	}

	/** Return true if the method includes the {@code native} modifier. */
	public boolean isNative () {
		return method.isNative();
	}

	/** Return true if the method includes the {@code static} modifier. */
	public boolean isStatic () {
		return method.isStatic();
	}

	/** Return true if the method takes a variable number of arguments. */
	public boolean isVarArgs () {
		return method.isVarArgs();
	}

	/** Invokes the underlying method on the supplied object with the supplied parameters. */
	public Object invoke (Object obj, Object... args) throws ReflectionException {
		try {
			return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Illegal argument(s) supplied to method: " + getName(), e);
		}
	}

	/** Returns true if the method includes an annotation of the provided class type. */
	public boolean isAnnotationPresent (Class<? extends java.lang.annotation.Annotation> annotationType) {
		java.lang.annotation.Annotation[] annotations = method.getDeclaredAnnotations();
		if (annotations != null) {
			for (java.lang.annotation.Annotation annotation : annotations) {
				if (annotation.annotationType().equals(annotationType)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns an array of {@link Annotation} objects reflecting all annotations declared by this method,
	 * or an empty array if there are none. Does not include inherited annotations.
	 * Does not include parameter annotations. */
	public Annotation[] getDeclaredAnnotations () {
		java.lang.annotation.Annotation[] annotations = method.getDeclaredAnnotations();
		if (annotations == null)
			return new Annotation[0];
		Annotation[] result = new Annotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			result[i] = new Annotation(annotations[i]);
		}
		return result;
	}

	/** Returns an {@link Annotation} object reflecting the annotation provided, or null of this method doesn't
	 * have such an annotation. This is a convenience function if the caller knows already which annotation
	 * type he's looking for. */
	public Annotation getDeclaredAnnotation (Class<? extends java.lang.annotation.Annotation> annotationType) {
		java.lang.annotation.Annotation[] annotations = method.getDeclaredAnnotations();
		if (annotations == null)
			return null;
		for (java.lang.annotation.Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationType)) {
				return new Annotation(annotation);
			}
		}
		return null;
	}
	
}
