package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;

public final class Constructor {
	
	private final java.lang.reflect.Constructor constructor;
	
	Constructor(java.lang.reflect.Constructor constructor) {
		this.constructor = constructor;
	}

	public Class getDeclaringClass() {
		return constructor.getDeclaringClass();
	}
	
	public boolean isAccessible() {
		return constructor.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		constructor.setAccessible(accessible);
	}
	
	public Object newInstance(Object... args) throws ReflectionException {
		try {
			return constructor.newInstance(args);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (InstantiationException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (IllegalAccessException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (InvocationTargetException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}		
	}
	
}
