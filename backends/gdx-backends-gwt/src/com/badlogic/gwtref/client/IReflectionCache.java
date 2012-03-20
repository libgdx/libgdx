package com.badlogic.gwtref.client;

import java.util.Collection;


public interface IReflectionCache {
	// Class level methods
	public Collection<Type> getKnownTypes();
	public Type forName(String name); 
	public Object newArray (Class componentType, int size);
	
	// Type methods
	public Object newInstance (Type type);
	public int getArrayLength(Type type, Object obj);
	public Object getArrayElement(Type type, Object obj, int i);
	public void setArrayElement(Type type, Object obj, int i, Object value);
	
	// Field Methods
	public Object get(Field field, Object obj) throws IllegalAccessException;
	public void set(Field field, Object obj, Object value) throws IllegalAccessException;
	
	// Method Methods :p
	public Object invoke (Method m, Object obj, Object[] params);
}
