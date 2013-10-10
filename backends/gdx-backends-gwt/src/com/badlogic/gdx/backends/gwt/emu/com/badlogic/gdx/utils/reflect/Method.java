package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.badlogic.gwtref.client.Parameter;

/** Provides information about, and access to, a single method on a class or interface.
 * @author nexsoftware */
public final class Method {
	
	private final com.badlogic.gwtref.client.Method method;
	
	Method(com.badlogic.gwtref.client.Method method) {
		this.method = method;
	}

	/** Returns the name of the method. */
	public String getName() {
		return method.getName();
	}
	
	/** Returns a Class object that represents the formal return type of the method. */
	public Class getReturnType() {
		return method.getReturnType();
	}
	
	/** Returns an array of Class objects that represent the formal parameter types, in declaration order, of the method. */
	public Class[] getParameterTypes() {
		Parameter[] parameters = method.getParameters();
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0, j = parameters.length; i < j; i++) {
			parameterTypes[i] = parameters[i].getType();
		}
		return parameterTypes;
	}
	
	/** Returns the Class object representing the class or interface that declares the method. */
	public Class getDeclaringClass() {
		return method.getEnclosingType();
	}
	
	public boolean isAccessible() {
		return method.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		method.setAccessible(accessible);
	}

	/** Return true if the method includes the {@code abstract} modifier. */
	public boolean isAbstract() {
		return method.isAbstract();
	}
	
	/** Return true if the method does not include any of the {@code private}, {@code protected}, or {@code public} modifiers. */
	public boolean isDefaultAccess() {
		return !isPrivate() && ! isProtected() && ! isPublic();
	}
	
	/** Return true if the method includes the {@code final} modifier. */
	public boolean isFinal() {
		return method.isFinal();
	}

	/** Return true if the method includes the {@code private} modifier. */
	public boolean isPrivate() {
		return method.isPrivate();
	}
	
	/** Return true if the method includes the {@code protected} modifier. */
	public boolean isProtected() {
		return method.isProtected();
	}
	
	/** Return true if the method includes the {@code public} modifier. */
	public boolean isPublic() {
		return method.isPublic();
	}
	
	/** Return true if the method includes the {@code native} modifier. */
	public boolean isNative() {
		return method.isNative();
	}
	
	/** Return true if the method includes the {@code static} modifier. */
	public boolean isStatic() {
		return method.isStatic();
	}
	
	/** Return true if the method takes a variable number of arguments. */
	public boolean isVarArgs() {
		return method.isVarArgs();
	}

	/** Invokes the underlying method on the supplied object with the supplied parameters. */
	public Object invoke(Object obj, Object... args) throws ReflectionException {
		try {
			return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("Illegal argument(s) supplied to method: " + getName(), e);
		}
	}
	
}
