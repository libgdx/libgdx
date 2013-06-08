package com.badlogic.gdx.utils.reflect;

import com.badlogic.gwtref.client.ReflectionCache;

public final class ArrayReflection {

	static public Object newInstance (Class c, int size) {
		return ReflectionCache.instance.newArray(c, size);
	}

	static public int getLength (Object array) {
		return ReflectionCache.instance.getArrayLength(ReflectionCache.getType(array.getClass()), array);
	}

	static public Object get (Object array, int index) {
		ReflectionCache.instance.getArrayElement(ReflectionCache.getType(array.getClass()), array, index);
		return array;
	}

	static public void set (Object array, int index, Object value) {
		ReflectionCache.instance.setArrayElement(ReflectionCache.getType(array.getClass()), array, index, value);
	}
}