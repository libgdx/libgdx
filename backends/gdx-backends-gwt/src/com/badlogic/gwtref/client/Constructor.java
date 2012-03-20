package com.badlogic.gwtref.client;

/**
 * The default constructor for the enclosing type.
 * @author mzechner
 *
 */
public class Constructor {
	final Class enclosingType;
	boolean isAccessible = false;
	
	Constructor(Class enclosingType) {
		this.enclosingType = enclosingType;
	}
	
	/**
	 * @return a new instance of the enclosing type of this constructor.
	 */
	public Object newInstance() {
		return ReflectionCache.instance.newInstance(ReflectionCache.instance.forName(enclosingType.getName()));
	}
	
	/**
	 * @return the enclosing type this constructor belongs to.
	 */
	public Type getEnclosingType() {
		return ReflectionCache.instance.forName(enclosingType.getName());
	}
	
	public boolean isAccessible() {
		return isAccessible;
	}
	
	public void setAccessible(boolean accessible) throws SecurityException {
		isAccessible = accessible;
	}
}
