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

/** Describes a method of a {@link Type}.
 * @author mzechner */
public class Method {
	private static final Parameter[] EMPTY_PARAMS = new Parameter[0];
	final String name;
	final CachedTypeLookup enclosingType;
	final CachedTypeLookup returnType;
	final boolean isAbstract;
	final boolean isFinal;
	final boolean isStatic;
	final boolean isNative;
	final boolean isDefaultAccess;
	final boolean isPrivate;
	final boolean isProtected;
	final boolean isPublic;
	final boolean isVarArgs;
	final boolean isMethod;
	final boolean isConstructor;
	final Parameter[] parameters;
	final int methodId;
	final Annotation[] annotations;
	
	public Method (String name, Class enclosingType, Class returnType, Parameter[] parameters, boolean isAbstract,
		boolean isFinal, boolean isStatic, boolean isDefaultAccess, boolean isPrivate, boolean isProtected, boolean isPublic,
		boolean isNative, boolean isVarArgs, boolean isMethod, boolean isConstructor, int methodId, Annotation[] annotations) {
		this.name = name;
		this.enclosingType = new CachedTypeLookup(enclosingType);
		this.parameters = parameters != null ? parameters : EMPTY_PARAMS;
		this.returnType = new CachedTypeLookup(returnType);
		this.isAbstract = isAbstract;
		this.isFinal = isFinal;
		this.isStatic = isStatic;
		this.isNative = isNative;
		this.isDefaultAccess = isDefaultAccess;
		this.isPrivate = isPrivate;
		this.isProtected = isProtected;
		this.isPublic = isPublic;
		this.isVarArgs = isVarArgs;
		this.isMethod = isMethod;
		this.isConstructor = isConstructor;
		this.methodId = methodId;
		this.annotations = annotations;
	}

	/** @return the {@link Class} of the enclosing type. */
	public Class getEnclosingType () {
		return enclosingType.clazz;
	}

	/** @return the {@link Class} of the return type or null. */
	public Class getReturnType () {
		return returnType.clazz;
	}

	/** @return the list of parameters, can be a zero size array. */
	public Parameter[] getParameters () {
		return parameters;
	}

	/** @return the name of the method. */
	public String getName () {
		return name;
	}

	public boolean isAbstract () {
		return isAbstract;
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

	public boolean isNative () {
		return isNative;
	}

	public boolean isVarArgs () {
		return isVarArgs;
	}

	public boolean isStatic () {
		return isStatic;
	}

	public boolean isMethod () {
		return isMethod;
	}

	public boolean isConstructor () {
		return isConstructor;
	}
	
	public Annotation[] getDeclaredAnnotations () {
		return annotations;
	}

	/** Invokes the method on the given object. Ignores the object if this is a static method. Throws an IllegalArgumentException if
	 * the parameters do not match.
	 * @param obj the object to invoke the method on or null.
	 * @param params the parameters to pass to the method or null.
	 * @return the return value or null if the method does not return anything. */
	public Object invoke (Object obj, Object... params) {
		if (parameters.length != (params != null ? params.length : 0)) throw new IllegalArgumentException("Parameter mismatch");

		return ReflectionCache.invoke(this, obj, params);
	}

	boolean match (String name, Class... types) {
		return this.name.equals(name) && match(types);
	}

	boolean match (Class... types) {
		if (types == null) return parameters.length == 0;
		if (types.length != parameters.length) return false;
		for (int i = 0; i < types.length; i++) {
			Type t1 = parameters[i].getType();
			Type t2 = ReflectionCache.getType(types[i]);
			if (t1 != t2 && !t1.isAssignableFrom(t2)) return false;
		}
		return true;
	}

	@Override
	public String toString () {
		return "Method [name=" + name + ", enclosingType=" + enclosingType + ", returnType=" + returnType + ", isAbstract="
			+ isAbstract + ", isFinal=" + isFinal + ", isStatic=" + isStatic + ", isNative=" + isNative + ", isDefaultAccess="
			+ isDefaultAccess + ", isPrivate=" + isPrivate + ", isProtected=" + isProtected + ", isPublic=" + isPublic
			+ ", isVarArgs=" + isVarArgs + ", isMethod=" + isMethod + ", isConstructor=" + isConstructor + ", parameters="
			+ Arrays.toString(parameters) + "]";
	}
}
