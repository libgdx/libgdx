package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;

public final class Constructor {
	
	private final com.badlogic.gwtref.client.Constructor constructor;
	
	Constructor(com.badlogic.gwtref.client.Constructor constructor) {
		this.constructor = constructor;
	}

	public Class[] getParameterTypes() {
		return null;
	}
	
	public Class getDeclaringClass() {
		return constructor.getEnclosingType().getClassOfType();
	}
	
	public boolean isAccessible() {
		return constructor.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		constructor.setAccessible(accessible);
	}
	
	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return constructor.newInstance();
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}		
	}
	
}
