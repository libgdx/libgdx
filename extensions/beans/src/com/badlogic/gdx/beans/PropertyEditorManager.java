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

import java.util.HashMap;
import java.util.Map;

public class PropertyEditorManager {

	private static String[] path = {"org.apache.harmony.beans.editors"}; //$NON-NLS-1$

	private static final Map<Class<?>, Class<?>> registeredEditors = new HashMap<Class<?>, Class<?>>();

	public PropertyEditorManager () {
		// expected
	}

	public static void registerEditor (Class<?> targetType, Class<?> editorClass) {
		if (targetType == null) {
			throw new NullPointerException();
		}
		SecurityManager sm = System.getSecurityManager();

		if (sm != null) {
			sm.checkPropertiesAccess();
		}

		if (editorClass != null) {
			registeredEditors.put(targetType, editorClass);
		} else {
			registeredEditors.remove(targetType);
		}
	}

	private static PropertyEditor loadEditor (Class<?> targetType, String className) throws ClassNotFoundException,
		IllegalAccessException, InstantiationException {
		ClassLoader loader = targetType.getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		try {
			return (PropertyEditor)loader.loadClass(className).newInstance();
		} catch (ClassNotFoundException e) {
			// Ignored
		}

		return (PropertyEditor)Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
	}

	public static synchronized PropertyEditor findEditor (Class<?> targetType) {
		if (targetType == null) {
			throw new NullPointerException();
		}
		Class<?> editorClass = registeredEditors.get(targetType);

		if (editorClass != null) {
			try {
				return (PropertyEditor)editorClass.newInstance();
			} catch (Exception e) {
				// expected
			}
		}
		String editorClassName = targetType.getName() + "Editor"; //$NON-NLS-1$

		try {
			return loadEditor(targetType, editorClassName);
		} catch (Exception exception) {
			// expected
		}
		String shortEditorClassName = (targetType.isPrimitive() ? (editorClassName.substring(0, 1).toUpperCase() + editorClassName
			.substring(1)) : editorClassName.substring(editorClassName.lastIndexOf('.') + 1));

		for (String element : path) {
			if (element == null) {
				continue;
			}
			try {
				return loadEditor(targetType, element + '.' + shortEditorClassName);
			} catch (Exception e) {
				// expected
			}
		}
		return null;
	}

	public static void setEditorSearchPath (String[] apath) {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPropertiesAccess();
		}
		synchronized (PropertyEditorManager.class) {
			path = (apath == null) ? new String[0] : apath;
		}
	}

	public static synchronized String[] getEditorSearchPath () {
		return path.clone();
	}
}
