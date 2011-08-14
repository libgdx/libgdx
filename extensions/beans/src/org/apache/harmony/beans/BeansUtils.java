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

package org.apache.harmony.beans;

import java.lang.reflect.Method;
import java.util.Arrays;

public class BeansUtils {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	public static final String NEW = "new"; //$NON-NLS-1$

	public static final String NEWINSTANCE = "newInstance"; //$NON-NLS-1$

	public static final String NEWARRAY = "newArray"; //$NON-NLS-1$

	public static final String FORNAME = "forName"; //$NON-NLS-1$

	public static final String GET = "get"; //$NON-NLS-1$

	public static final String IS = "is"; //$NON-NLS-1$

	public static final String SET = "set"; //$NON-NLS-1$

	public static final String ADD = "add"; //$NON-NLS-1$

	public static final String PUT = "put"; //$NON-NLS-1$

	public static final String NULL = "null"; //$NON-NLS-1$

	public static final String QUOTE = "\"\""; //$NON-NLS-1$

	public static final int getHashCode (Object obj) {
		return obj != null ? obj.hashCode() : 0;
	}

	public static final int getHashCode (boolean bool) {
		return bool ? 1 : 0;
	}

	public static String toASCIILowerCase (String string) {
		char[] charArray = string.toCharArray();
		StringBuilder sb = new StringBuilder(charArray.length);
		for (int index = 0; index < charArray.length; index++) {
			if ('A' <= charArray[index] && charArray[index] <= 'Z') {
				sb.append((char)(charArray[index] + ('a' - 'A')));
			} else {
				sb.append(charArray[index]);
			}
		}
		return sb.toString();
	}

	public static String toASCIIUpperCase (String string) {
		char[] charArray = string.toCharArray();
		StringBuilder sb = new StringBuilder(charArray.length);
		for (int index = 0; index < charArray.length; index++) {
			if ('a' <= charArray[index] && charArray[index] <= 'z') {
				sb.append((char)(charArray[index] - ('a' - 'A')));
			} else {
				sb.append(charArray[index]);
			}
		}
		return sb.toString();
	}

	public static boolean isPrimitiveWrapper (Class<?> wrapper, Class<?> base) {
		return (base == boolean.class) && (wrapper == Boolean.class) || (base == byte.class) && (wrapper == Byte.class)
			|| (base == char.class) && (wrapper == Character.class) || (base == short.class) && (wrapper == Short.class)
			|| (base == int.class) && (wrapper == Integer.class) || (base == long.class) && (wrapper == Long.class)
			|| (base == float.class) && (wrapper == Float.class) || (base == double.class) && (wrapper == Double.class);
	}

	private static final String EQUALS_METHOD = "equals";

	private static final Class<?>[] EQUALS_PARAMETERS = new Class<?>[] {Object.class};

	public static boolean declaredEquals (Class<?> clazz) {
		for (Method declaredMethod : clazz.getDeclaredMethods()) {
			if (EQUALS_METHOD.equals(declaredMethod.getName())
				&& Arrays.equals(declaredMethod.getParameterTypes(), EQUALS_PARAMETERS)) {
				return true;
			}
		}
		return false;
	}

	public static String idOfClass (Class<?> clazz) {
		Class<?> theClass = clazz;
		StringBuilder sb = new StringBuilder();
		if (theClass.isArray()) {
			do {
				sb.append("Array"); //$NON-NLS-1$
				theClass = theClass.getComponentType();
			} while (theClass.isArray());
		}
		String clazzName = theClass.getName();
		clazzName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
		return clazzName + sb.toString();
	}
}
