
package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class Field {

	private final java.lang.reflect.Field field;

	Field (java.lang.reflect.Field field) {
		this.field = field;
	}

	public String getName () {
		return field.getName();
	}

	public Class getType () {
		return field.getType();
	}

	public Class getDeclaringClass () {
		return field.getDeclaringClass();
	}

	public boolean isAccessible () {
		return field.isAccessible();
	}

	public void setAccessible (boolean accessible) {
		field.setAccessible(accessible);
	}

	public boolean isDefaultAccess () {
		return !isPrivate() && !isProtected() && !isPublic();
	}

	public boolean isFinal () {
		return Modifier.isFinal(field.getModifiers());
	}

	public boolean isPrivate () {
		return Modifier.isPrivate(field.getModifiers());
	}

	public boolean isProtected () {
		return Modifier.isProtected(field.getModifiers());
	}

	public boolean isPublic () {
		return Modifier.isPublic(field.getModifiers());
	}

	public boolean isStatic () {
		return Modifier.isStatic(field.getModifiers());
	}

	public boolean isTransient () {
		return Modifier.isTransient(field.getModifiers());
	}

	public boolean isVolatile () {
		return Modifier.isVolatile(field.getModifiers());
	}

	public boolean isSynthetic () {
		return field.isSynthetic();
	}

	public Class getElementType () {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType) {
			Type[] actualTypes = ((ParameterizedType)genericType).getActualTypeArguments();
			if (actualTypes.length == 1) {
				Type actualType = actualTypes[0];
				if (actualType instanceof Class)
					return (Class)actualType;
				else if (actualType instanceof ParameterizedType) return (Class)((ParameterizedType)actualType).getRawType();
			}
		}
		return null;
	}

	public Object get (Object obj) throws ReflectionException {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (IllegalAccessException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}
	}

	public void set (Object obj, Object value) throws ReflectionException {
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		} catch (IllegalAccessException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}
	}

}
