package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** Provides information about, and access to, a single field of a class or interface.
 * @author nexsoftware */
public final class Field {
	
	private final com.badlogic.gwtref.client.Field field;
	
	Field(com.badlogic.gwtref.client.Field field) {
		this.field = field;
	}
	
	/** Returns the name of the field. */
	public String getName() {
		return field.getName();
	}
	
	/** Returns a Class object that identifies the declared type for the field. */
	public Class getType() {
		return field.getType().getClassOfType();
	}
	
	/** Returns the Class object representing the class or interface that declares the field. */
	public Class getDeclaringClass() {
		return field.getEnclosingType().getClassOfType();
	}
	
	public boolean isAccessible() {
		return field.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		field.setAccessible(accessible);
	}

	/** Return true if the field does not include any of the {@code private}, {@code protected}, or {@code public} modifiers. */
	public boolean isDefaultAccess() {
		return !isPrivate() && ! isProtected() && ! isPublic();
	}
	
	/** Return true if the field includes the {@code final} modifier. */
	public boolean isFinal() {
		return field.isFinal();
	}
	
	/** Return true if the field includes the {@code private} modifier. */
	public boolean isPrivate() {
		return field.isPrivate();
	}
	
	/** Return true if the field includes the {@code protected} modifier. */
	public boolean isProtected() {
		return field.isProtected();
	}
	
	/** Return true if the field includes the {@code public} modifier. */
	public boolean isPublic() {
		return field.isPublic();
	}
	
	/** Return true if the field includes the {@code static} modifier. */
	public boolean isStatic() {
		return field.isStatic();
	}

	/** Return true if the field includes the {@code transient} modifier. */
	public boolean isTransient() {
		return field.isTransient();
	}
	
	/** Return true if the field includes the {@code volatile} modifier. */
	public boolean isVolatile() {
		return field.isVolatile();
	}

	/** Return true if the field is a synthetic field. */
	public boolean isSynthetic() {
		return field.isSynthetic();
	}
	
	/** If the type of the field is parameterized, returns the Class object representing the parameter type, null otherwise. */
	public Class getElementType() {
		return null;
	}
	
	/** Returns the value of the field on the supplied object. */
	public Object get(Object obj) throws ReflectionException {
		try {
			return field.get(obj);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Object is not an instance of " + getDeclaringClass(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Illegal access to field: " + getName(), e);
		}	
	}
	
	/** Sets the value of the field on the supplied object. */
	public void set(Object obj, Object value) throws ReflectionException {
		try {
			field.set(obj, value);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Argument not valid for field: " + getName(), e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException("Illegal access to field: " + getName(), e);
		}
	}
	
}
