package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class Field {
	
	private final com.badlogic.gwtref.client.Field field;
	
	Field(com.badlogic.gwtref.client.Field field) {
		this.field = field;
	}
	
	public String getName() {
		return field.getName();
	}
	
	public Class getType() {
		return field.getType().getClassOfType();
	}
	
	public Class getDeclaringClass() {
		return field.getEnclosingType().getClassOfType();
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
		return field.isFinal();
	}
	
	public boolean isPrivate() {
		return field.isPrivate();
	}
	
	public boolean isProtected() {
		return field.isProtected();
	}
	
	public boolean isPublic() {
		return field.isPublic();
	}
	
	public boolean isStatic() {
		return field.isStatic();
	}

	public boolean isTransient() {
		return field.isTransient();
	}
	
	public boolean isVolatile() {
		return field.isVolatile();
	}

	public boolean isSynthetic() {
		return field.isSynthetic();
	}
	
	public Class getElementType() {
		return null;
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
