package com.badlogic.gdx.utils.reflect;

public final class ArrayReflection {

	static public Object newInstance (Class c, int size) {
		return java.lang.reflect.Array.newInstance(c, size);
	}

	static public int getLength (Object array) {
		return java.lang.reflect.Array.getLength(array);
	}

	static public Object get (Object array, int index) {
		return java.lang.reflect.Array.get(array, index);
	}

	static public void set (Object array, int index, Object value) {
		java.lang.reflect.Array.set(array, index, value);
	}
}