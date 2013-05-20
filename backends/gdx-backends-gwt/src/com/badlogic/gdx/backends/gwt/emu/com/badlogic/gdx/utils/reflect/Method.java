package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.badlogic.gwtref.client.Parameter;

public final class Method {
	
	private final com.badlogic.gwtref.client.Method method;
	
	Method(com.badlogic.gwtref.client.Method method) {
		this.method = method;
	}

	public String getName() {
		return method.getName();
	}
	
	public Class getReturnType() {
		return method.getReturnType();
	}
	
	public Class[] getParameterTypes() {
		Parameter[] parameters = method.getParameters();
		Class[] parameterTypes = new Class[parameters.length];
		for (int i = 0, j = parameters.length; i < j; i++) {
			parameterTypes[i] = parameters[i].getType();
		}
		return parameterTypes;
	}
	
	public Class getDeclaringClass() {
		return method.getEnclosingType();
	}
	
	public boolean isAccessible() {
		return method.isAccessible();		
	}
	
	public void setAccessible(boolean accessible) {
		method.setAccessible(accessible);
	}
	
	public boolean isAbstract() {
		return method.isAbstract();
	}
	
	public boolean isDefaultAccess() {
		return !isPrivate() && ! isProtected() && ! isPublic();
	}
	
	public boolean isFinal() {
		return method.isFinal();
	}

	public boolean isPrivate() {
		return method.isPrivate();
	}
	
	public boolean isProtected() {
		return method.isProtected();
	}
	
	public boolean isPublic() {
		return method.isPublic();
	}
	
	public boolean isNative() {
		return method.isNative();
	}
	
	public boolean isVarArgs() {
		return method.isVarArgs();
	}
	
	public boolean isStatic() {
		return method.isStatic();
	}
	
	public Object invoke(Object obj, Object... args) throws ReflectionException {
		try {
			return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException("", e); // TODO: Real Message
		}
	}
	
}
