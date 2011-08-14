/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.beans;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.harmony.beans.BeansUtils;
import org.apache.harmony.beans.internal.nls.Messages;

public class Statement {

	private Object target;

	private String methodName;

	private Object[] arguments;

	// cache used methods of specified target class to accelerate method search
	private static WeakHashMap<Class<?>, Method[]> classMethodsCache = new WeakHashMap<Class<?>, Method[]>();

	public Statement (Object target, String methodName, Object[] arguments) {
		this.target = target;
		this.methodName = methodName;
		this.arguments = arguments == null ? BeansUtils.EMPTY_OBJECT_ARRAY : arguments;
	}

	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder();
		if (target == null) {
			sb.append(BeansUtils.NULL);
		} else {
			Class<?> clazz = target.getClass();
			sb.append(clazz == String.class ? BeansUtils.QUOTE : BeansUtils.idOfClass(clazz));
		}
		sb.append('.' + methodName + '(');
		if (arguments != null) {
			Class<?> clazz;
			for (int index = 0; index < arguments.length; index++) {
				if (index > 0) {
					sb.append(", "); //$NON-NLS-1$
				}
				if (arguments[index] == null) {
					sb.append(BeansUtils.NULL);
				} else {
					clazz = arguments[index].getClass();
					sb.append(clazz == String.class ? '"' + (String)arguments[index] + '"' : BeansUtils.idOfClass(clazz));
				}
			}
		}
		sb.append(')');
		sb.append(';');
		return sb.toString();
	}

	public String getMethodName () {
		return methodName;
	}

	public Object[] getArguments () {
		return arguments;
	}

	public Object getTarget () {
		return target;
	}

	public void execute () throws Exception {
		invokeMethod();
	}

	Object invokeMethod () throws Exception {
		Object result = null;
		try {
			Object target = getTarget();
			String methodName = getMethodName();
			Object[] arguments = getArguments();
			Class<?> targetClass = target.getClass();
			if (targetClass.isArray()) {
				Method method = findArrayMethod(methodName, arguments);
				Object[] copy = new Object[arguments.length + 1];
				copy[0] = target;
				System.arraycopy(arguments, 0, copy, 1, arguments.length);
				result = method.invoke(null, copy);
			} else if (BeansUtils.NEWINSTANCE.equals(methodName) && target == Array.class) {
				result = Array.newInstance((Class<?>)arguments[0], ((Integer)arguments[1]).intValue());
			} else if (BeansUtils.NEW.equals(methodName) || BeansUtils.NEWINSTANCE.equals(methodName)) {
				if (target instanceof Class<?>) {
					Constructor<?> constructor = findConstructor((Class<?>)target, arguments);
					result = constructor.newInstance(arguments);
				} else {
					if (BeansUtils.NEW.equals(methodName)) {
						throw new NoSuchMethodException(this.toString());
					}
					// target class declares a public named "newInstance" method
					Method method = findMethod(targetClass, methodName, arguments, false);
					result = method.invoke(target, arguments);
				}
			} else if (methodName.equals(BeansUtils.NEWARRAY)) {
				// create a new array instance without length attribute
				Class<?> clazz = (Class<?>)target, argClass;

				// check the element types of array
				for (int index = 0; index < arguments.length; index++) {
					argClass = arguments[index] == null ? null : arguments[index].getClass();
					if (argClass != null && !clazz.isAssignableFrom(argClass) && !BeansUtils.isPrimitiveWrapper(argClass, clazz)) {
						throw new IllegalArgumentException(Messages.getString("beans.63")); //$NON-NLS-1$
					}
				}
				result = Array.newInstance(clazz, arguments.length);
				if (clazz.isPrimitive()) {
					// Copy element according to primitive types
					arrayCopy(clazz, arguments, result, arguments.length);
				} else {
					// Copy element of Objects
					System.arraycopy(arguments, 0, result, 0, arguments.length);
				}
				return result;
			} else if (target instanceof Class<?>) {
				Method method = null;
				try {
					/*
					 * Try to look for a static method of class described by the given Class object at first process only if the class
					 * differs from Class itself
					 */
					if (target != Class.class) {
						method = findMethod((Class<?>)target, methodName, arguments, true);
						result = method.invoke(null, arguments);
					}
				} catch (NoSuchMethodException e) {
					// expected
				}
				if (method == null) {
					// static method was not found
					// try to invoke method of Class object
					if (BeansUtils.FORNAME.equals(methodName) && arguments.length == 1 && arguments[0] instanceof String) {
						// special handling of Class.forName(String)
						try {
							result = Class.forName((String)arguments[0]);
						} catch (ClassNotFoundException e2) {
							result = Class.forName((String)arguments[0], true, Thread.currentThread().getContextClassLoader());
						}
					} else {
						method = findMethod(targetClass, methodName, arguments, false);
						result = method.invoke(target, arguments);
					}
				}
			} else if (target instanceof Iterator<?>) {
				final Iterator<?> iterator = (Iterator<?>)target;
				final Method method = findMethod(targetClass, methodName, arguments, false);
				if (iterator.hasNext()) {
					result = new PrivilegedAction<Object>() {
						public Object run () {
							try {
								method.setAccessible(true);
								return (method.invoke(iterator, new Object[0]));
							} catch (Exception e) {
								// ignore
							}
							return null;
						}

					}.run();
				}
			} else {
				Method method = findMethod(targetClass, methodName, arguments, false);
				method.setAccessible(true);
				result = method.invoke(target, arguments);
			}
		} catch (InvocationTargetException ite) {
			Throwable t = ite.getCause();
			throw (t != null) && (t instanceof Exception) ? (Exception)t : ite;
		}
		return result;
	}

	private void arrayCopy (Class<?> type, Object[] src, Object dest, int length) {
		if (type == boolean.class) {
			boolean[] destination = (boolean[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Boolean)src[index]).booleanValue();
			}
		} else if (type == short.class) {
			short[] destination = (short[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Short)src[index]).shortValue();
			}
		} else if (type == byte.class) {
			byte[] destination = (byte[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Byte)src[index]).byteValue();
			}
		} else if (type == char.class) {
			char[] destination = (char[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Character)src[index]).charValue();
			}
		} else if (type == int.class) {
			int[] destination = (int[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Integer)src[index]).intValue();
			}
		} else if (type == long.class) {
			long[] destination = (long[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Long)src[index]).longValue();
			}
		} else if (type == float.class) {
			float[] destination = (float[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Float)src[index]).floatValue();
			}
		} else if (type == double.class) {
			double[] destination = (double[])dest;
			for (int index = 0; index < length; index++) {
				destination[index] = ((Double)src[index]).doubleValue();
			}
		}
	}

	private Method findArrayMethod (String methodName, Object[] args) throws NoSuchMethodException {
		// the code below reproduces exact RI exception throwing behavior
		boolean isGet = BeansUtils.GET.equals(methodName); //$NON-NLS-1$
		boolean isSet = BeansUtils.SET.equals(methodName); //$NON-NLS-1$
		if (!isGet && !isSet) {
			throw new NoSuchMethodException(Messages.getString("beans.3C")); //$NON-NLS-1$
		} else if (args.length > 0 && args[0].getClass() != Integer.class) {
			throw new ClassCastException(Messages.getString("beans.3D")); //$NON-NLS-1$
		} else if (isGet && args.length != 1) {
			throw new ArrayIndexOutOfBoundsException(Messages.getString("beans.3E")); //$NON-NLS-1$
		} else if (isSet && args.length != 2) {
			throw new ArrayIndexOutOfBoundsException(Messages.getString("beans.3F")); //$NON-NLS-1$
		}

		Class<?>[] paraTypes = isGet ? new Class<?>[] {Object.class, int.class} : new Class<?>[] {Object.class, int.class,
			Object.class};
		return Array.class.getMethod(methodName, paraTypes);
	}

	private Constructor<?> findConstructor (Class<?> clazz, Object[] args) throws NoSuchMethodException {
		Class<?>[] argTypes = getTypes(args), paraTypes, resultParaTypes;
		Constructor<?> result = null;
		boolean isAssignable;
		for (Constructor<?> constructor : clazz.getConstructors()) {
			paraTypes = constructor.getParameterTypes();
			if (match(argTypes, paraTypes)) {
				if (result == null) {
					// first time, set constructor
					result = constructor;
					continue;
				}
				// find out more suitable constructor
				resultParaTypes = result.getParameterTypes();
				isAssignable = true;
				for (int index = 0; index < paraTypes.length; index++) {
					if (argTypes[index] != null && !(isAssignable &= resultParaTypes[index].isAssignableFrom(paraTypes[index]))) {
						break;
					}
					if (argTypes[index] == null && !(isAssignable &= paraTypes[index].isAssignableFrom(resultParaTypes[index]))) {
						break;
					}
				}
				if (isAssignable) {
					result = constructor;
				}
			}
		}
		if (result == null) {
			throw new NoSuchMethodException(Messages.getString("beans.40", clazz.getName())); //$NON-NLS-1$
		}
		return result;
	}

	/** Searches for best matching method for given name and argument types. */
	static Method findMethod (Class<?> clazz, String methodName, Object[] args, boolean isStatic) throws NoSuchMethodException {
		Class<?>[] argTypes = getTypes(args);

		Method[] methods = null;
		if (classMethodsCache.containsKey(clazz)) {
			methods = classMethodsCache.get(clazz);
		} else {
			methods = clazz.getMethods();
			classMethodsCache.put(clazz, methods);
		}

		ArrayList<Method> fitMethods = new ArrayList<Method>();
		for (Method method : methods) {
			if (methodName.equals(method.getName())) {
				if (!isStatic || Modifier.isStatic(method.getModifiers())) {
					if (match(argTypes, method.getParameterTypes())) {
						fitMethods.add(method);
					}
				}
			}
		}
		int fitSize = fitMethods.size();
		if (fitSize == 0) {
			throw new NoSuchMethodException(Messages.getString("beans.41", methodName)); //$NON-NLS-1$
		}
		if (fitSize == 1) {
			return fitMethods.get(0);
		}
		// find the most relevant one
		MethodComparator comparator = new MethodComparator(methodName, argTypes);
		Method[] fitMethodArray = fitMethods.toArray(new Method[fitSize]);
		Method onlyMethod = fitMethodArray[0];
		Class<?> onlyReturnType, fitReturnType;
		int difference;
		for (int i = 1; i < fitMethodArray.length; i++) {
			// if 2 methods have same relevance, check their return type
			if ((difference = comparator.compare(onlyMethod, fitMethodArray[i])) == 0) {
				// if 2 methods have the same signature, check their return type
				onlyReturnType = onlyMethod.getReturnType();
				fitReturnType = fitMethodArray[i].getReturnType();
				if (onlyReturnType == fitReturnType) {
					// if 2 methods have the same relevance and return type
					throw new NoSuchMethodException(Messages.getString("beans.62", methodName)); //$NON-NLS-1$
				}

				if (onlyReturnType.isAssignableFrom(fitReturnType)) {
					// if onlyReturnType is super class or interface of
					// fitReturnType, set onlyMethod to fitMethodArray[i]
					onlyMethod = fitMethodArray[i];
				}
			}
			if (difference > 0) {
				onlyMethod = fitMethodArray[i];
			}
		}
		return onlyMethod;
	}

	private static boolean match (Class<?>[] argTypes, Class<?>[] paraTypes) {
		if (paraTypes.length != argTypes.length) {
			return false;
		}
		for (int index = 0; index < paraTypes.length; index++) {
			if (argTypes[index] != null && !paraTypes[index].isAssignableFrom(argTypes[index])
				&& !BeansUtils.isPrimitiveWrapper(argTypes[index], paraTypes[index])) {
				return false;
			}
		}
		return true;
	}

	static boolean isStaticMethodCall (Statement stmt) {
		Object target = stmt.getTarget();
		String methodName = stmt.getMethodName();
		if (!(target instanceof Class<?>)) {
			return false;
		}
		try {
			Statement.findMethod((Class<?>)target, methodName, stmt.getArguments(), true);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	/*
	 * The list of "method signatures" used by persistence delegates to create objects. Not necessary reflects to real methods.
	 */
	private static final String[][] pdConstructorSignatures = { {"java.lang.Class", "new", "java.lang.Boolean", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Byte", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Character", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Double", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Float", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Integer", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Long", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.Short", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "new", "java.lang.String", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "forName", "java.lang.String", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "newInstance", "java.lang.Class", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"java.lang.Integer", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"java.lang.reflect.Field", "get", "null", "", "", ""}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		{"java.lang.Class", "forName", "java.lang.String", "", "", ""} //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	};

	static boolean isPDConstructor (Statement stmt) {
		Object target = stmt.getTarget();
		String methodName = stmt.getMethodName();
		Object[] args = stmt.getArguments();
		String[] sig = new String[pdConstructorSignatures[0].length];
		if (target == null || methodName == null || args == null || args.length == 0) {
			// not a constructor for sure
			return false;
		}
		sig[0] = target.getClass().getName();
		sig[1] = methodName;
		for (int i = 2; i < sig.length; i++) {
			if (args.length > i - 2) {
				sig[i] = args[i - 2] != null ? args[i - 2].getClass().getName() : "null"; //$NON-NLS-1$
			} else {
				sig[i] = ""; //$NON-NLS-1$
			}
		}
		for (String[] element : pdConstructorSignatures) {
			if (Arrays.equals(sig, element)) {
				return true;
			}
		}
		return false;
	}

	private static Class<?> getPrimitiveWrapper (Class<?> base) {
		Class<?> res = null;
		if (base == boolean.class) {
			res = Boolean.class;
		} else if (base == byte.class) {
			res = Byte.class;
		} else if (base == char.class) {
			res = Character.class;
		} else if (base == short.class) {
			res = Short.class;
		} else if (base == int.class) {
			res = Integer.class;
		} else if (base == long.class) {
			res = Long.class;
		} else if (base == float.class) {
			res = Float.class;
		} else if (base == double.class) {
			res = Double.class;
		}
		return res;
	}

	private static Class<?>[] getTypes (Object[] arguments) {
		Class<?>[] types = new Class[arguments.length];
		for (int index = 0; index < arguments.length; ++index) {
			types[index] = (arguments[index] == null) ? null : arguments[index].getClass();
		}
		return types;
	}

	/** Comparator to determine which of two methods is "closer" to the reference method. */
	static class MethodComparator implements Comparator<Method> {
		static int INFINITY = Integer.MAX_VALUE;

		private String referenceMethodName;

		private Class<?>[] referenceMethodArgumentTypes;

		private final Map<Method, Integer> cache;

		public MethodComparator (String refMethodName, Class<?>[] refArgumentTypes) {
			this.referenceMethodName = refMethodName;
			this.referenceMethodArgumentTypes = refArgumentTypes;
			cache = new HashMap<Method, Integer>();
		}

		public int compare (Method m1, Method m2) {
			Integer norm1 = cache.get(m1);
			Integer norm2 = cache.get(m2);
			if (norm1 == null) {
				norm1 = Integer.valueOf(getNorm(m1));
				cache.put(m1, norm1);
			}
			if (norm2 == null) {
				norm2 = Integer.valueOf(getNorm(m2));
				cache.put(m2, norm2);
			}
			return (norm1.intValue() - norm2.intValue());
		}

		/** Returns the norm for given method. The norm is the "distance" from the reference method to the given method.
		 * 
		 * @param m the method to calculate the norm for
		 * @return norm of given method */
		private int getNorm (Method m) {
			String methodName = m.getName();
			Class<?>[] argumentTypes = m.getParameterTypes();
			int totalNorm = 0;
			if (!referenceMethodName.equals(methodName) || referenceMethodArgumentTypes.length != argumentTypes.length) {
				return INFINITY;
			}
			for (int i = 0; i < referenceMethodArgumentTypes.length; i++) {
				if (referenceMethodArgumentTypes[i] == null) {
					// doesn't affect the norm calculation if null
					continue;
				}
				if (referenceMethodArgumentTypes[i].isPrimitive()) {
					referenceMethodArgumentTypes[i] = getPrimitiveWrapper(referenceMethodArgumentTypes[i]);
				}
				if (argumentTypes[i].isPrimitive()) {
					argumentTypes[i] = getPrimitiveWrapper(argumentTypes[i]);
				}
				totalNorm += getDistance(referenceMethodArgumentTypes[i], argumentTypes[i]);
			}
			return totalNorm;
		}

		/** Returns a "hierarchy distance" between two classes.
		 * 
		 * @param clz1
		 * @param clz2 should be superclass or superinterface of clz1
		 * @return hierarchy distance from clz1 to clz2, Integer.MAX_VALUE if clz2 is not assignable from clz1. */
		private static int getDistance (Class<?> clz1, Class<?> clz2) {
			Class<?> superClz;
			int superDist = INFINITY;
			if (!clz2.isAssignableFrom(clz1)) {
				return INFINITY;
			}
			if (clz1.getName().equals(clz2.getName())) {
				return 0;
			}
			superClz = clz1.getSuperclass();
			if (superClz != null) {
				superDist = getDistance(superClz, clz2);
			}
			if (clz2.isInterface()) {
				Class<?>[] interfaces = clz1.getInterfaces();
				int bestDist = INFINITY;
				for (Class<?> element : interfaces) {
					int curDist = getDistance(element, clz2);
					if (curDist < bestDist) {
						bestDist = curDist;
					}
				}
				if (superDist < bestDist) {
					bestDist = superDist;
				}
				return (bestDist != INFINITY ? bestDist + 1 : INFINITY);
			}
			return (superDist != INFINITY ? superDist + 2 : INFINITY);
		}
	}
}
