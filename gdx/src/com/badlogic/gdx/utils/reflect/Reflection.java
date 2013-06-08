
package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.Modifier;

public class Reflection {

	static public final class ArrayReflection {

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

	static public final class ClassReflection {

		static public Class forName (String name) throws ReflectionException {
			try {
				return Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public boolean isInstance (Class c, Object obj) {
			return c.isInstance(obj);
		}

		static public boolean isAssignableFrom (Class c, Object obj) {
			return c.isAssignableFrom(obj.getClass());
		}

		static public boolean isMemberClass (Class c) {
			return c.isMemberClass();
		}

		static public boolean isStaticClass (Class c) {
			return Modifier.isStatic(c.getModifiers());
		}

		static public Object newInstance (Class c) throws ReflectionException {
			try {
				return c.newInstance();
			} catch (InstantiationException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (IllegalAccessException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Constructor[] getConstructors (Class c) {
			java.lang.reflect.Constructor[] constructors = c.getConstructors();
			Constructor[] result = new Constructor[constructors.length];
			for (int i = 0, j = constructors.length; i < j; i++) {
				result[i] = new Constructor(constructors[i]);
			}
			return result;
		}

		static public Constructor getConstructor (Class c, Class... parameterTypes) throws ReflectionException {
			try {
				return new Constructor(c.getConstructor(parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Constructor getDeclaredConstructor (Class c, Class... parameterTypes) throws ReflectionException {
			try {
				return new Constructor(c.getDeclaredConstructor(parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Method[] getMethods (Class c) {
			java.lang.reflect.Method[] methods = c.getMethods();
			Method[] result = new Method[methods.length];
			for (int i = 0, j = methods.length; i < j; i++) {
				result[i] = new Method(methods[i]);
			}
			return result;
		}

		static public Method getMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
			try {
				return new Method(c.getMethod(name, parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Method[] getDeclaredMethods (Class c) {
			java.lang.reflect.Method[] methods = c.getDeclaredMethods();
			Method[] result = new Method[methods.length];
			for (int i = 0, j = methods.length; i < j; i++) {
				result[i] = new Method(methods[i]);
			}
			return result;
		}

		static public Method getDeclaredMethod (Class c, String name, Class... parameterTypes) throws ReflectionException {
			try {
				return new Method(c.getDeclaredMethod(name, parameterTypes));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchMethodException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Field[] getFields (Class c) {
			java.lang.reflect.Field[] fields = c.getFields();
			Field[] result = new Field[fields.length];
			for (int i = 0, j = fields.length; i < j; i++) {
				result[i] = new Field(fields[i]);
			}
			return result;
		}

		static public Field getField (Class c, String name) throws ReflectionException {
			try {
				return new Field(c.getField(name));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchFieldException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}

		static public Field[] getDeclaredFields (Class c) {
			java.lang.reflect.Field[] fields = c.getDeclaredFields();
			Field[] result = new Field[fields.length];
			for (int i = 0, j = fields.length; i < j; i++) {
				result[i] = new Field(fields[i]);
			}
			return result;
		}

		static public Field getDeclaredField (Class c, String name) throws ReflectionException {
			try {
				return new Field(c.getDeclaredField(name));
			} catch (SecurityException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			} catch (NoSuchFieldException e) {
				throw new ReflectionException("", e); // TODO: Real Message
			}
		}
		
		static public String getSimpleName(Class c) {
			return c.getSimpleName();
		}

	}

}
