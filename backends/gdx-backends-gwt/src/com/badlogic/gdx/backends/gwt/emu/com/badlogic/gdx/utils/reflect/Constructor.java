package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;

/** Provides information about, and access to, a single constructor for a Class.
 * @author nexsoftware */
public final class Constructor {
	
	private final com.badlogic.gwtref.client.Constructor constructor;
	
	Constructor(com.badlogic.gwtref.client.Constructor constructor) {
		this.constructor = constructor;
	}

	/** Returns an array of Class objects that represent the formal parameter types, in declaration order, of the constructor. */
	public Class[] getParameterTypes() {
		return null;
	}
	
	/** Returns the Class object representing the class or interface that declares the constructor. */
	public Class getDeclaringClass() {
		return constructor.getEnclosingType().getClassOfType();
	}
	
	public boolean isAccessible() {
		return constructor.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		constructor.setAccessible(accessible);
	}
	
	/** Uses the constructor to create and initialize a new instance of the constructor's declaring class, with the supplied initialization parameters. */
	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return constructor.newInstance();
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Illegal argument(s) supplied to constructor for class: " + getDeclaringClass().getName(), e);
		}		
	}
	
}
