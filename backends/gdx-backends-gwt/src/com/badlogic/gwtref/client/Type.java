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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Describes a type (equivalent to {@link Class}), providing methods to retrieve fields, constructors, methods and super
 * interfaces of the type. Only types that are visible (public) can be described by this class.
 * @author mzechner */
public class Type {
	private static final Field[] EMPTY_FIELDS = new Field[0];
	private static final Method[] EMPTY_METHODS = new Method[0];
	private static final Constructor[] EMPTY_CONSTRUCTORS = new Constructor[0];
	private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
	private static final Set<Class> EMPTY_ASSIGNABLES = Collections.unmodifiableSet(new HashSet<Class>());
	private static final Set<Class> EMPTY_INTERFACES = Collections.unmodifiableSet(new HashSet<Class>());
	
	final String name;
	final int id;
	final Class clazz;
	final CachedTypeLookup superClass;
	final Set<Class> assignables;
	final Set<Class> interfaces;
	boolean isAbstract;
	boolean isInterface;
	boolean isPrimitive;
	boolean isEnum;
	boolean isArray;
	boolean isMemberClass;
	boolean isStatic;
	boolean isAnnotation;

	Field[] fields = EMPTY_FIELDS;
	Method[] methods = EMPTY_METHODS;
	Constructor[] constructors = EMPTY_CONSTRUCTORS;
	Annotation[] annotations = EMPTY_ANNOTATIONS;

	Class componentType;
	Object[] enumConstants;

	private Field[] allFields;
	private Method[] allMethods;

	public Type (String name, int id, Class clazz, Class superClass, Set<Class> assignables, Set<Class> interfaces) {
		this.name = name;
		this.id = id;
		this.clazz = clazz;
		this.superClass = new CachedTypeLookup(superClass);
		this.assignables = assignables != null ? assignables : EMPTY_ASSIGNABLES;
		this.interfaces = interfaces != null ? interfaces : EMPTY_INTERFACES;
	}

	/** @return a new instance of this type created via the default constructor which must be public. */
	public Object newInstance () throws NoSuchMethodException {
		return getConstructor().newInstance();
	}

	/** @return the fully qualified name of this type. */
	public String getName () {
		return name;
	}

	/** @return the {@link Class} of this type. */
	public Class getClassOfType () {
		return clazz;
	}

	/** @return the super class of this type or null */
	public Type getSuperclass () {
		return superClass.getType();
	}

	/** @param otherType the other type
	 * @return whether this type is assignable to the other type. */
	public boolean isAssignableFrom (Type otherType) {
		return clazz == otherType.clazz || (clazz == Object.class && !otherType.isPrimitive)
			|| otherType.assignables.contains(clazz);
	}
	
	public Class[] getInterfaces() {
		return interfaces.toArray(new Class[this.interfaces.size()]);
	}

	/** @param name the name of the field
	 * @return the public field of this type or one of its super interfaces with the given name. See
	 *         {@link Class#getField(String)}.
	 * @throws NoSuchFieldException */
	public Field getField (String name) throws NoSuchFieldException {
		for (Field f : getFields()) {
			if (f.name.equals(name)) return f;
		}
		throw new NoSuchFieldException();
	}

	/** @return an array containing all the public fields of this class and its super classes. See {@link Class#getFields()}. */
	public Field[] getFields () {
		if (allFields == null) {
			ArrayList<Field> allFieldsList = new ArrayList<Field>();
			Type t = this;
			while (t != null) {
				for (Field f : t.fields) {
					if (f.isPublic) allFieldsList.add(f);
				}
				t = t.getSuperclass();
			}
			allFields = allFieldsList.toArray(new Field[allFieldsList.size()]);
		}
		return allFields;
	}

	/** @param name the name of the field
	 * @return the declared field of this type. See {@link Class#getDeclaredField(String)}.
	 * @throws NoSuchFieldException */
	public Field getDeclaredField (String name) throws NoSuchFieldException {
		for (Field f : getDeclaredFields()) {
			if (f.name.equals(name)) return f;
		}
		throw new NoSuchFieldException();
	}
	
	/** @return an array containing all the fields of this class, including private and protected fields. See
	 *         {@link Class#getDeclaredFields()}. */
	public Field[] getDeclaredFields () {
		return fields;
	}
	
	/** @param name the name of the method
	 * @param parameterTypes the types of the parameters of the method
	 * @return the public method that matches the name and parameter types of this type or one of its super interfaces.
	 * @throws NoSuchMethodException */
	public Method getMethod (String name, Class... parameterTypes) throws NoSuchMethodException {
		for (Method m : getMethods()) {
			if (m.match(name, parameterTypes)) return m;
		}
		throw new NoSuchMethodException();
	}

	/** s * @return an array containing all public methods of this class and its super classes. See {@link Class#getMethods()}. */
	public Method[] getMethods () {
		if (allMethods == null) {
			ArrayList<Method> allMethodsList = new ArrayList<Method>();
			Type t = this;
			while (t != null) {
				for (Method m : t.methods) {
					if (m.isPublic()) allMethodsList.add(m);
				}
				t = t.getSuperclass();
			}
			allMethods = allMethodsList.toArray(new Method[allMethodsList.size()]);
		}
		return allMethods;
	}

	/** @param name the name of the method
	 * @param parameterTypes the types of the parameters of the method
	 * @return the declared method that matches the name and parameter types of this type.
	 * @throws NoSuchMethodException */
	public Method getDeclaredMethod (String name, Class... parameterTypes) throws NoSuchMethodException {
		for (Method m : getDeclaredMethods()) {
			if (m.match(name, parameterTypes)) return m;
		}
		throw new NoSuchMethodException();
	}
	
	/** @return an array containing all methods of this class, including abstract, private and protected methods. See
	 *         {@link Class#getDeclaredMethods()}. */
	public Method[] getDeclaredMethods () {
		return methods;
	}

	public Constructor[] getConstructors () {
		return constructors;
	}

	public Constructor getDeclaredConstructor (Class... parameterTypes) throws NoSuchMethodException {
		return getConstructor(parameterTypes);
	}

	public Constructor getConstructor (Class... parameterTypes) throws NoSuchMethodException {
		for (Constructor c : constructors) {
			if (c.isPublic() && c.match(parameterTypes)) return c;
		}
		throw new NoSuchMethodException();
	}

	public boolean isAbstract () {
		return isAbstract;
	}

	public boolean isInterface () {
		return isInterface;
	}

	public boolean isPrimitive () {
		return isPrimitive;
	}

	public boolean isEnum () {
		return isEnum;
	}

	public boolean isArray () {
		return isArray;
	}

	public boolean isMemberClass () {
		return isMemberClass;
	}

	public boolean isStatic () {
		return isStatic;
	}

	public boolean isAnnotation () {
		return isAnnotation;
	}

	/** @return the class of the components if this is an array type or null. */
	public Class getComponentType () {
		return componentType;
	}

	/** @param obj an array object of this type.
	 * @return the length of the given array object. */
	public int getArrayLength (Object obj) {
		return ReflectionCache.getArrayLength(this, obj);
	}

	/** @param obj an array object of this type.
	 * @param i the index of the element to retrieve.
	 * @return the element at position i in the array. */
	public Object getArrayElement (Object obj, int i) {
		return ReflectionCache.getArrayElement(this, obj, i);
	}

	/** Sets the element i in the array object to value.
	 * @param obj an array object of this type.
	 * @param i the index of the element to set.
	 * @param value the element value. */
	public void setArrayElement (Object obj, int i, Object value) {
		ReflectionCache.setArrayElement(this, obj, i, value);
	}

	/** @return the enumeration constants if this type is an enumeration or null. */
	public Object[] getEnumConstants () {
		return enumConstants;
	}

	/** @return an array of annotation instances, if this type has any. */
	public Annotation[] getDeclaredAnnotations () {
		return annotations;
	}

	/** @return annotation of specified type, or null if not found. */
	public Annotation getDeclaredAnnotation (Class<? extends java.lang.annotation.Annotation> annotationType) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationType)) return annotation;
		}
		return null;
	}
	
	@Override
	public String toString () {
		return "Type [name=" + name + ",\n clazz=" + clazz + ",\n superClass=" + superClass + ",\n assignables=" + assignables
			+ ",\n isAbstract=" + isAbstract + ",\n isInterface=" + isInterface + ",\n isPrimitive=" + isPrimitive + ",\n isEnum="
			+ isEnum + ",\n isArray=" + isArray + ",\n isMemberClass=" + isMemberClass + ",\n isStatic=" + isStatic
			+ ",\n isAnnotation=" + isAnnotation + ",\n fields=" + Arrays.toString(fields) + ",\n methods="
			+ Arrays.toString(methods) + ",\n constructors=" + Arrays.toString(constructors) + ",\n annotations="
			+ Arrays.toString(annotations) + ",\n componentType=" + componentType + ",\n enumConstants="
			+ Arrays.toString(enumConstants) + "]";
	}
}
