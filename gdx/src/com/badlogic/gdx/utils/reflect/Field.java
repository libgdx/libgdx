package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;

public final class Field {
	
	private final java.lang.reflect.Field field;
	
	Field(java.lang.reflect.Field field) {
		this.field = field;
	}
	
	public Class getDeclaringClass() {
		return field.getDeclaringClass();
	}
	
	public boolean isAccessible() {
		return field.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		field.setAccessible(accessible);
	}

	public boolean isDefaultAccess() {
		return !isPrivate() && ! isProtected() && ! isPublic();
	}
	
	public boolean isFinal() {
		return Modifier.isFinal(field.getModifiers());
	}
	
	public boolean isPrivate() {
		return Modifier.isPrivate(field.getModifiers());
	}
	
	public boolean isProtected() {
		return Modifier.isProtected(field.getModifiers());
	}
	
	public boolean isPublic() {
		return Modifier.isPublic(field.getModifiers());
	}
	
	public boolean isStatic() {
		return Modifier.isStatic(field.getModifiers());
	}

	public boolean isTransient() {
		return Modifier.isTransient(field.getModifiers());
	}
	
	public boolean isVolatile() {
		return Modifier.isVolatile(field.getModifiers());
	}

	public boolean isSynthetic() {
		return field.isSynthetic();
	}
	
	public Object get(Object obj) throws ReflectionException {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (IllegalAccessException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}	
	}
	
	public void set(Object obj, Object value) throws ReflectionException {
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (IllegalAccessException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}
	}
	
}
