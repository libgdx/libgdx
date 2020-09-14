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

import java.lang.reflect.Modifier;

/** Utilities for Class reflection.
 * @author nexsoftware */
public final class ClassReflection {

	/** Returns the Class object associated with the class or interface with the supplied string name. */
	static public Class forName (String name) throws ReflectionException {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new ReflectionException("Class not found: " + name, e);
		}
	}

	/** Returns the simple name of the underlying class as supplied in the source code. */
	static public String getSimpleName (Class c) {
		return c.getSimpleName();
	}

	/** Determines if the supplied Object is assignment-compatible with the object represented by supplied Class. */
	static public boolean isInstance (Class c, Object obj) {
		return c.isInstance(obj);
	}

	/** Determines if the class or interface represented by first Class parameter is either the same as, or is a superclass or
	 * superinterface of, the class or interface represented by the second Class parameter. */
	static public boolean isAssignableFrom (Class c1, Class c2) {
		return c1.isAssignableFrom(c2);
	}

	/** Returns true if the class or interface represented by the supplied Class is a member class. */
	static public boolean isMemberClass (Class c) {
		return c.isMemberClass();
	}

	/** Returns true if the class or interface represented by the supplied Class is a static class. */
	static public boolean isStaticClass (Class c) {
		return Modifier.isStatic(c.getModifiers());
	}
	
	/** Determines if the supplied Class object represents an array class. */
	static public boolean isArray (Class c) {
		return c.isArray();
	}
	
	/** Determines if the supplied Class object represents a primitive type. */
	static public boolean isPrimitive (Class c) {
		return c.isPrimitive();
	}
	
	/** Determines if the supplied Class object represents an enum type. */
	static public boolean isEnum (Class c) {
		return c.isEnum();
	}
	
	/** Determines if the supplied Class object represents an annotation type. */
	static public boolean isAnnotation (Class c) {
		return c.isAnnotation();
	}
	
	/** Determines if the supplied Class object represents an interface type. */
	static public boolean isInterface (Class c) {
		return c.isInterface();
	}
	
	/** Determines if the supplied Class object represents an abstract type. */
	static public boolean isAbstract (Class c) {
		return Modifier.isAbstract(c.getModifiers());
	}	

	/** Creates a new instance of the class represented by the supplied Class. */
	static public <T> T newInstance (Class<T> c) throws ReflectionException {
		try {
			return c.newInstance();
		} catch (InstantiationException e) {
			throw new ReflectionException("Could not instantiate instance of class: " + c.getName(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Could not instantiate instance of class: " + c.getName(), e);
		}
	}
	
	/** Returns the Class representing the component type of an array. If this class does not represent an array class this method returns null.	 */
	static public Class getComponentType(Class c){
		return c.getComponentType();
	}

	/** Returns an array of {@link Constructor} containing the public constructors of the class represented by the supplied Class. */
	static public Constructor[] getConstructors (Class c) {
		java.lang.reflect.Constructor[] constructors = c.getConstructors();
		Constructor[] result = new Constructor[constructors.length];
		for (int i = 0, j = constructors.length; i < j; i++) {
			result[i] = new Constructor(constructors[i]);
		}
		return result;
	}

	/** Returns a {@link Constructor} that represents the public constructor for the supplied class which takes the supplied
	 * parameter types. */
	static public Constructor getConstructor (Class c, Class... parameterTypes) throws ReflectionException {
		try {
			return new Constructor(c.getConstructor(parameterTypes));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation occurred while getting constructor for class: '" + c.getName() + "'.",
				e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Constructor not found for class: " + c.getName(), e);
		}
	}

	/** Returns a {@link Constructor} that represents the constructor for the supplied class which takes the supplied parameter
	 * types. */
	static public Constructor getDeclaredConstructor (Class c, Class... parameterTypes) throws ReflectionException {
		try {
			return new Constructor(c.getDeclaredConstructor(parameterTypes));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation while getting constructor for class: " + c.getName(), e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Constructor not found for class: " + c.getName(), e);
		}
	}
	
	/** Returns the elements of this enum class or null if this Class object does not represent an enum type. */
	static public Object[] getEnumConstants (Class c) {
		return c.getEnumConstants();
	}

	/** Returns an array of {@link Method} containing the public member methods of the class represented by the supplied Class. */
	static public Method[] getMethods (Class c) {
		java.lang.reflect.Method[] methods = c.getMethods();
		Method[] result = new Method[methods.length];
		for (int i = 0, j = methods.length; i < j; i++) {
			result[i] = new Method(methods[i]);
		}
		return result;
	}

	/** Returns a {@link Method} that represents the public member method for the supplied class which takes the supplied parameter
	 * types. */
	static public Method getMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
		try {
			return new Method(c.getMethod(name, parameterTypes));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation while getting method: " + name + ", for class: " + c.getName(), e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Method not found: " + name + ", for class: " + c.getName(), e);
		}
	}

	/** Returns an array of {@link Method} containing the methods declared by the class represented by the supplied Class. */
	static public Method[] getDeclaredMethods (Class c) {
		java.lang.reflect.Method[] methods = c.getDeclaredMethods();
		Method[] result = new Method[methods.length];
		for (int i = 0, j = methods.length; i < j; i++) {
			result[i] = new Method(methods[i]);
		}
		return result;
	}

	/** Returns a {@link Method} that represents the method declared by the supplied class which takes the supplied parameter types. */
	static public Method getDeclaredMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
		try {
			return new Method(c.getDeclaredMethod(name, parameterTypes));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation while getting method: " + name + ", for class: " + c.getName(), e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException("Method not found: " + name + ", for class: " + c.getName(), e);
		}
	}

	/** Returns an array of {@link Field} containing the public fields of the class represented by the supplied Class. */
	static public Field[] getFields (Class c) {
		java.lang.reflect.Field[] fields = c.getFields();
		Field[] result = new Field[fields.length];
		for (int i = 0, j = fields.length; i < j; i++) {
			result[i] = new Field(fields[i]);
		}
		return result;
	}

	/** Returns a {@link Field} that represents the specified public member field for the supplied class. */
	static public Field getField (Class c, String name) throws ReflectionException {
		try {
			return new Field(c.getField(name));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation while getting field: " + name + ", for class: " + c.getName(), e);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Field not found: " + name + ", for class: " + c.getName(), e);
		}
	}

	/** Returns an array of {@link Field} objects reflecting all the fields declared by the supplied class. */
	static public Field[] getDeclaredFields (Class c) {
		java.lang.reflect.Field[] fields = c.getDeclaredFields();
		Field[] result = new Field[fields.length];
		for (int i = 0, j = fields.length; i < j; i++) {
			result[i] = new Field(fields[i]);
		}
		return result;
	}

	/** Returns a {@link Field} that represents the specified declared field for the supplied class. */
	static public Field getDeclaredField (Class c, String name) throws ReflectionException {
		try {
			return new Field(c.getDeclaredField(name));
		} catch (SecurityException e) {
			throw new ReflectionException("Security violation while getting field: " + name + ", for class: " + c.getName(), e);
		} catch (NoSuchFieldException e) {
			throw new ReflectionException("Field not found: " + name + ", for class: " + c.getName(), e);
		}
	}

	/** Returns true if the supplied class includes an annotation of the given type. */
	static public boolean isAnnotationPresent (Class c, Class<? extends java.lang.annotation.Annotation> annotationType) {
		return c.isAnnotationPresent(annotationType);
	}

	/** Returns an array of {@link Annotation} objects reflecting all annotations declared by the supplied class, and inherited
	 * from its superclass. Returns an empty array if there are none. */
	static public Annotation[] getAnnotations (Class c) {
		java.lang.annotation.Annotation[] annotations = c.getAnnotations();
		Annotation[] result = new Annotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			result[i] = new Annotation(annotations[i]);
		}
		return result;
	}

	/** Returns an {@link Annotation} object reflecting the annotation provided, or null if this class doesn't have such an
	 * annotation. This is a convenience function if the caller knows already which annotation type he's looking for. */
	static public Annotation getAnnotation (Class c, Class<? extends java.lang.annotation.Annotation> annotationType) {
		java.lang.annotation.Annotation annotation = c.getAnnotation(annotationType);
		if (annotation != null) return new Annotation(annotation);
		return null;
	}

	/** Returns an array of {@link Annotation} objects reflecting all annotations declared by the supplied class, or an empty
	 * array if there are none. Does not include inherited annotations. */
	static public Annotation[] getDeclaredAnnotations (Class c) {
		java.lang.annotation.Annotation[] annotations = c.getDeclaredAnnotations();
		Annotation[] result = new Annotation[annotations.length];
		for (int i = 0; i < annotations.length; i++) {
			result[i] = new Annotation(annotations[i]);
		}
		return result;
	}

	/** Returns an {@link Annotation} object reflecting the annotation provided, or null if this class doesn't have such an
	 * annotation. This is a convenience function if the caller knows already which annotation type he's looking for. */
	static public Annotation getDeclaredAnnotation (Class c, Class<? extends java.lang.annotation.Annotation> annotationType) {
		java.lang.annotation.Annotation[] annotations = c.getDeclaredAnnotations();
		for (java.lang.annotation.Annotation annotation : annotations) {
			if (annotation.annotationType().equals(annotationType)) return new Annotation(annotation);
		}
		return null;
	}

	static public Class[] getInterfaces(Class c) {
		return c.getInterfaces();
	}

}
