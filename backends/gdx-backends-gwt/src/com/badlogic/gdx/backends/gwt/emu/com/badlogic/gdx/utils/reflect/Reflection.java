
package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gwtref.client.ReflectionCache;
import com.badlogic.gwtref.client.Type;

public class Reflection {

	static public final class ArrayReflection {

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

	static public final class ClassReflection {

		static public Class forName (String name) throws ReflectionException {
			return ReflectionCache.instance.forName(name).getClassOfType();
		}

		static public boolean isInstance (Class c, Object obj) {
			return isAssignableFrom(c, obj);
		}

		static public boolean isAssignableFrom (Class c, Object obj) {
			Type cType = ReflectionCache.getType(c);
			Type objType = ReflectionCache.getType(obj.getClass());
			return cType.isAssignableFrom(objType);
		}

		static public boolean isMemberClass (Class c) {
			return ReflectionCache.getType(c).isMemberClass();
		}

		static public boolean isStaticClass (Class c) {
			return ReflectionCache.getType(c).isStatic();
		}

		static public Object newInstance (Class c) throws ReflectionException {
			return ReflectionCache.getType(c).newInstance();
		}

		static public Constructor[] getConstructors (Class c) {
			throw new GdxRuntimeException("Not implemented.");
		}

		static public Constructor getConstructor (Class c, Class... parameterTypes) throws ReflectionException {
			throw new GdxRuntimeException("Not implemented.");
		}

		static public Constructor getDeclaredConstructor (Class c, Class... parameterTypes) throws ReflectionException {
			throw new GdxRuntimeException("Not implemented.");
		}

		static public Method[] getMethods (Class c) {
			com.badlogic.gwtref.client.Method[] methods = ReflectionCache.getType(c).getMethods();
			Method[] result = new Method[methods.length];
			for (int i = 0, j = methods.length; i < j; i++) {
				result[i] = new Method(methods[i]);
			}
			return result;
		}

		static public Method getMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
			try {
				return new Method(ReflectionCache.getType(c).getMethod(name, parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Method[] getDeclaredMethods (Class c) {
			com.badlogic.gwtref.client.Method[] methods = ReflectionCache.getType(c).getDeclaredMethods();
			Method[] result = new Method[methods.length];
			for (int i = 0, j = methods.length; i < j; i++) {
				result[i] = new Method(methods[i]);
			}
			return result;
		}

		static public Method getDeclaredMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
			try {
				return new Method(ReflectionCache.getType(c).getMethod(name, parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Field[] getFields (Class c) {
			com.badlogic.gwtref.client.Field[] fields = ReflectionCache.getType(c).getFields();
			Field[] result = new Field[fields.length];
			for (int i = 0, j = fields.length; i < j; i++) {
				result[i] = new Field(fields[i]);
			}
			return result;
		}

		static public Field getField (Class c, String name) throws ReflectionException {
			try {
				return new Field(ReflectionCache.getType(c).getField(name));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Field[] getDeclaredFields (Class c) {
			com.badlogic.gwtref.client.Field[] fields = ReflectionCache.getType(c).getDeclaredFields();
			Field[] result = new Field[fields.length];
			for (int i = 0, j = fields.length; i < j; i++) {
				result[i] = new Field(fields[i]);
			}
			return result;
		}

		static public Field getDeclaredField (Class c, String name) throws ReflectionException {
			try {
				return new Field(ReflectionCache.getType(c).getField(name));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

	}

}
