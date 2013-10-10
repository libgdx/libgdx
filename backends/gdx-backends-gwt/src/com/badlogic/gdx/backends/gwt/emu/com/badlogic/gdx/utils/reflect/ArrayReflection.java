package com.badlogic.gdx.utils.reflect;

import com.badlogic.gwtref.client.ReflectionCache;

/** Utilities for Array reflection.
 * @author nexsoftware */
public final class ArrayReflection {

	/** Creates a new array with the specified component type and length. */
	static public Object newInstance (Class c, int size) {
		return ReflectionCache.instance.newArray(c, size);
	}

	/** Returns the length of the supplied array. */
	static public int getLength (Object array) {
		return ReflectionCache.instance.getArrayLength(ReflectionCache.getType(array.getClass()), array);
	}

	/** Returns the value of the indexed component in the supplied array. */
	static public Object get (Object array, int index) {
		ReflectionCache.instance.getArrayElement(ReflectionCache.getType(array.getClass()), array, index);
		return array;
	}

	/** Sets the value of the indexed component in the supplied array to the supplied value. */
	static public void set (Object array, int index, Object value) {
		ReflectionCache.instance.setArrayElement(ReflectionCache.getType(array.getClass()), array, index, value);
	}
}