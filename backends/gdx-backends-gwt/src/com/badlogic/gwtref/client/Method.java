package com.badlogic.gwtref.client;

import java.util.Arrays;

/**
 * Describes a method of a {@link Type}.
 * @author mzechner
 *
 */
public class Method {
	final String name;
	final Class enclosingType;
	final Class returnType;
	final boolean isAbstract;
	final boolean isFinal;
	final boolean isStatic;
	final boolean isNative;
	final boolean isDefaultAccess;
	final boolean isPrivate;
	final boolean isProtected;
	final boolean isPublic;
	final boolean isVarArgs;
	final boolean isMethod;
	final boolean isConstructor;
	final Parameter[] parameters;
	final String methodId;
	boolean accessible;
	
	
	public Method(String name, Class enclosingType, Class returnType, Parameter[] parameters, boolean isAbstract, boolean isFinal, boolean isStatic,
					  boolean isDefaultAccess, boolean isPrivate, boolean isProtected, boolean isPublic, boolean isNative,
					  boolean isVarArgs, boolean isMethod, boolean isConstructor, String methodId) {
		this.name = name;
		this.enclosingType = enclosingType;
		this.parameters = parameters;
		this.returnType = returnType;
		this.isAbstract = isAbstract;
		this.isFinal = isFinal;
		this.isStatic = isStatic;
		this.isNative = isNative;
		this.isDefaultAccess = isDefaultAccess;
		this.isPrivate = isPrivate;
		this.isProtected = isProtected;
		this.isPublic = isPublic;
		this.isVarArgs = isVarArgs;
		this.isMethod = isMethod;
		this.isConstructor = isConstructor;
		this.methodId = methodId;
	}
	
	public boolean isAccessible () {
		return accessible;
	}
	public void setAccessible (boolean accessible) {
		this.accessible = accessible;
	}
	
	/**
	 * @return the {@link Class} of the enclosing type.
	 */
	public Class getEnclosingType () {
		return enclosingType;
	}
	
	/**
	 * @return the {@link Class} of the return type or null.
	 */
	public Class getReturnType () {
		return returnType;
	}
	
	/**
	 * @return the list of parameters, can be a zero size array. 
	 */
	public Parameter[] getParameters() {
		return parameters;
	}
	
	/**
	 * @return the name of the method.
	 */
	public String getName () {
		return name;
	}
	
	public boolean isAbstract () {
		return isAbstract;
	}
	
	public boolean isFinal () {
		return isFinal;
	}
	
	public boolean isDefaultAccess () {
		return isDefaultAccess;
	}
	
	public boolean isPrivate () {
		return isPrivate;
	}
	
	public boolean isProtected () {
		return isProtected;
	}
	
	public boolean isPublic () {
		return isPublic;
	}
	
	public boolean isNative () {
		return isNative;
	}
	
	public boolean isVarArgs () {
		return isVarArgs;
	}
	
	public boolean isStatic () {
		return isStatic;
	}
	
	public boolean isMethod () {
		return isMethod;
	}
	
	public boolean isConstructor () {
		return isConstructor;
	}

	/**
	 * Invokes the method on the given object. Ignores the object if this is
	 * a static method. Throws an IllegalArgumentException if the parameters
	 * do not match.
	 * @param obj the object to invoke the method on or null. 
	 * @param params the parameters to pass to the method or null.
	 * @return the return value or null if the method does not return anything.
	 */
	public Object invoke(Object obj, Object ... params) {
		if(parameters != null && (params == null || params.length != parameters.length))
			throw new IllegalArgumentException("Parameter mismatch");
		if(parameters == null && params != null && params.length > 0) {
			throw new IllegalArgumentException("Parameter mismatch");
		}
		return ReflectionCache.instance.invoke(this, obj, params);
	}
	
	boolean match(String name, Class ... types) {
		if(!name.equals(name)) return false;
		if(types.length != parameters.length) return false;
		for(int i = 0; i < types.length; i++) {
			Type t1 = ReflectionCache.instance.forName(parameters[i].getType().getName());
			Type t2 = ReflectionCache.instance.forName(types[i].getName());
			if(t1 != t2 && !t1.isAssignableFrom(t2)) return false;
		}
		return true;
	}
	
	@Override
	public String toString () {
		return "Method [name=" + name + ", enclosingType=" + enclosingType + ", returnType=" + returnType + ", isAbstract="
			+ isAbstract + ", isFinal=" + isFinal + ", isStatic=" + isStatic + ", isNative=" + isNative + ", isDefaultAccess="
			+ isDefaultAccess + ", isPrivate=" + isPrivate + ", isProtected=" + isProtected + ", isPublic=" + isPublic
			+ ", isVarArgs=" + isVarArgs + ", isMethod=" + isMethod + ", isConstructor=" + isConstructor + ", parameters="
			+ Arrays.toString(parameters) + ", accessible=" + accessible + "]";
	}
}