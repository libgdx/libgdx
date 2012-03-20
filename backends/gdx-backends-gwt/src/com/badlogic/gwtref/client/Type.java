package com.badlogic.gwtref.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes a type (equivalent to {@link Class}), providing methods to retrieve
 * fields, constructors, methods and super interfaces of the type. Only types
 * that are visible (public) can be described by this class.
 * @author mzechner
 *
 */
public class Type {
	String name;
	Class clazz;
	Class superClass;
	Set<Class> assignables = new HashSet<Class>();
	boolean isAbstract;
	boolean isInterface;
	boolean isPrimitive;
	boolean isEnum;
	boolean isArray;
	boolean isMemberClass;
	boolean isStatic;

	Field[] fields;
	Method[] methods;
	Constructor constructor;
	Class componentType;
	Object[] enumConstants;
	
	/**
	 * @return a new instance of this type created via the default constructor which must be public.
	 */
	public Object newInstance () {
		return ReflectionCache.instance.newInstance(this);
	}

	/**
	 * @return the fully qualified name of this type.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the {@link Class} of this type.
	 */
	public Class getClassOfType () {
		return clazz;
	}
	
	/**
	 * @return the super class of this type or null
	 */
	public Type getSuperclass() {
		try {
			return ReflectionCache.forName(superClass.getName());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * @param otherType the other type
	 * @return whether this type is assignable to the other type.
	 */
	public boolean isAssignableFrom (Type otherType) {
		return assignables.contains(otherType.getClassOfType());
	}
	
	/**
	 * @param name the name of the field
	 * @return the public field of this type or one of its super interfaces with the given name or null. See {@link Class#getField(String)}.
	 */
	public Field getField(String name) {
		Type t = this;
		while(t != null) {
			Field[] declFields = t.getDeclaredFields();
			if(declFields != null) {
				for(Field f: declFields) {
					if(f.isPublic && f.name.equals(name)) return f;
				}
			}
			t = t.getSuperclass();
		}
		return null;
	}
	
	/**
	 * @return an array containing all the public fields of this class and its super classes. See {@link Class#getFields()}.
	 */
	public Field[] getFields() {
		ArrayList<Field> allFields = new ArrayList<Field>();
		Type t = this;
		while(t != null) {
			Field[] declFields = t.getDeclaredFields();
			if(declFields != null) {
				for(Field f: declFields) {
					if(f.isPublic) allFields.add(f);
				}
			}
			t = t.getSuperclass();
		}
		return allFields.toArray(new Field[allFields.size()]);
	}
	
	/**
	 * @return an array containing all the fields of this class, including private and protected fields. See {@link Class#getDeclaredFields()}.
	 */
	public Field[] getDeclaredFields() {
		return fields;
	}

	/**
	 * @param name the name of the method
	 * @param parameterTypes the types of the parameters of the method
	 * @return the public method that matches the name and parameter types of this type or one of its super interfaces.
	 * @throws NoSuchMethodException 
	 */
	public Method getMethod(String name, Class ... parameterTypes) throws NoSuchMethodException {
		ArrayList<Method> allMethods = new ArrayList<Method>();
		Type t = this;
		while(t != null) {
			Method[] declMethods = t.getDeclaredMethods();
			if(declMethods != null) {
				for(Method m: declMethods) {
					if(m.isPublic() && m.match(name, parameterTypes)) return m;
				}
			}
			t = t.getSuperclass();
		}
		throw new NoSuchMethodException();
	}

	/**
s	 * @return an array containing all public methods of this class and its super classes. See {@link Class#getMethods()}.
	 */
	public Method[] getMethods() {
		ArrayList<Method> allMethods = new ArrayList<Method>();
		Type t = this;
		while(t != null) {
			Method[] declMethods = t.getDeclaredMethods();
			if(declMethods != null) {
				for(Method m: declMethods) {
					if(m.isPublic()) allMethods.add(m);
				}
			}
			t = t.getSuperclass();
		}
		return allMethods.toArray(new Method[allMethods.size()]);
	}
	
	/**
	 * @return an array containing all methods of this class, including abstract, private and protected methods. See {@link Class#getDeclaredMethods()}.
	 */
	public Method[] getDeclaredMethods() {
		return methods;
	}
	
	public Constructor getDeclaredConstructor() throws NoSuchMethodException {
		return constructor;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean isInterface() {
		return isInterface;
	}
	
	public boolean isPrimitive() {
		return isPrimitive;
	}
	
	public boolean isEnum () {
		return isEnum;
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	public boolean isMemberClass() {
		return isMemberClass;
	}
	
	public boolean isStatic() {
		return isStatic;
	}
	
	/**
	 * @return the class of the components if this is an array type or null.
	 */
	public Class getComponentType () {
		return componentType;
	}
	
	/**
	 * @param obj an array object of this type.
	 * @return the length of the given array object.
	 */
	public int getArrayLength(Object obj) {
		return ReflectionCache.instance.getArrayLength(this, obj);
	}
	
	/**
	 * @param obj an array object of this type.
	 * @param i the index of the element to retrieve.
	 * @return the element at position i in the array.
	 */
	public Object getArrayElement(Object obj, int i) {
		return ReflectionCache.instance.getArrayElement(this, obj, i);
	}
	
	/**
	 * Sets the element i in the array object to value.
	 * @param obj an array object of this type.
	 * @param i the index of the element to set.
	 * @param value the element value.
	 */
	public void setArrayElement(Object obj, int i, Object value) {
		ReflectionCache.instance.setArrayElement(this, obj, i, value);
	}
	
	/**
	 * @return the enumeration constants if this type is an enumeration or null.
	 */
	public Object[] getEnumConstants () {
		return enumConstants;
	}

	@Override
	public String toString () {
		return "Type [name=" + name + ",\n clazz=" + clazz + ",\n superClass=" + superClass + ",\n assignables=" + assignables
			+ ",\n isAbstract=" + isAbstract + ",\n isInterface=" + isInterface + ",\n isPrimitive=" + isPrimitive + ",\n isEnum=" + isEnum
			+ ",\n isArray=" + isArray + ",\n isMemberClass=" + isMemberClass + ",\n isStatic=" + isStatic + ",\n fields="
			+ Arrays.toString(fields) + ",\n methods=" + Arrays.toString(methods) + ",\n constructor=" + constructor
			+ ",\n componentType=" + componentType + ",\n enumConstants=" + Arrays.toString(enumConstants) + "]";
	}
}
