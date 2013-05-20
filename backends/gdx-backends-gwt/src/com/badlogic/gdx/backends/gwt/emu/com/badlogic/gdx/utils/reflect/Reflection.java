package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;

import com.badlogic.gwtref.client.ReflectionCache;
import com.badlogic.gwtref.client.Type;

public class Reflection {

	static public Class forName(String name) throws ReflectionException {
		return ReflectionCache.instance.forName(name).getClassOfType();
	}
	
	static public boolean isInstance(Class c, Object obj) {
		return isAssignableFrom(c, obj);
	}

	static public boolean isAssignableFrom(Class c, Object obj) {
		Type cType = ReflectionCache.getType(c);
		Type objType = ReflectionCache.getType(obj.getClass());
		return cType.isAssignableFrom(objType);
	}

	static public boolean isMemberClass(Class c) {
		return ReflectionCache.getType(c).isMemberClass();		
	}
	
	static public boolean isStaticClass(Class c) {
		return ReflectionCache.getType(c).isStatic();		
	}
	
	static public Object newArray(Class c, int size) {
		return ReflectionCache.instance.newArray(c, size);		
	}
	
}
