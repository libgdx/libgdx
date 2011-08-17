/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/** Reads/writes Java objects to/from JSON, automatically.
 * @author Nathan Sweet */
public class Json {
	private static final boolean debug = false;

	private final ObjectMap<Class, ObjectMap<String, Field>> typeToFields = new ObjectMap();
	private final ObjectMap<String, Class> tagToClass = new ObjectMap();
	private final ObjectMap<Class, String> classToTag = new ObjectMap();
	private String typeName = "type";

	public void addClassTag (String tag, Class type) {
		tagToClass.put(tag, type);
		classToTag.put(type, tag);
	}

	public void setTypeName (String typeName) {
		this.typeName = typeName;
	}

	private ObjectMap<String, Field> cacheFields (Class type) {
		ArrayList<Field> allFields = new ArrayList();
		Class nextClass = type;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}

		ObjectMap<String, Field> nameToField = new ObjectMap();
		for (int i = 0, n = allFields.size(); i < n; i++) {
			Field field = allFields.get(i);

			int modifiers = field.getModifiers();
			if (Modifier.isTransient(modifiers)) continue;
			if (Modifier.isStatic(modifiers)) continue;
			if (field.isSynthetic()) continue;

			if (!field.isAccessible()) {
				try {
					field.setAccessible(true);
				} catch (AccessControlException ex) {
					continue;
				}
			}

			nameToField.put(field.getName(), field);
		}
		typeToFields.put(type, nameToField);
		return nameToField;
	}

	public void write (Object object, Writer writer) throws IOException {
		if (!(writer instanceof JsonWriter)) writer = new JsonWriter(writer);
		write(object.getClass(), null, object, (JsonWriter)writer);
	}

	private void write (Class valueType, String name, Object value, JsonWriter writer) throws IOException {
		if (value == null) {
			if (name == null)
				writer.add(value);
			else
				writer.set(name, value);
			return;
		}

		Class c = value.getClass();
		if (c.isPrimitive() || c == String.class || c == Integer.class || c == Boolean.class || c == Float.class || c == Long.class
			|| c == Double.class || c == Short.class || c == Byte.class || c == Character.class) {
			if (name == null)
				writer.add(value);
			else
				writer.set(name, value);
			return;
		}

		if (value instanceof Collection) {
			if (name == null)
				writer.array();
			else
				writer.array(name);
			for (Object item : (Collection)value)
				write(null, null, item, writer);
			writer.pop();
			return;
		}

		if (value instanceof Array) {
			if (name == null)
				writer.array();
			else
				writer.array(name);
			for (Object item : (Array)value)
				write(null, null, item, writer);
			writer.pop();
			return;
		}

		if (value.getClass().isArray()) {
			if (name == null)
				writer.array();
			else
				writer.array(name);

			int length = java.lang.reflect.Array.getLength(value);
			for (int i = 0; i < length; i++)
				write(null, null, java.lang.reflect.Array.get(value, i), writer);

			writer.pop();
			return;
		}

		if (name == null)
			writer.object();
		else
			writer.object(name);

		Class type = value.getClass();

		if (valueType == null || valueType != type) {
			String className = classToTag.get(type);
			if (className == null) className = type.getName();
			writer.set(typeName, className);
			if (debug) System.out.println("Writing type: " + type.getName());
		}

		ObjectMap<String, Field> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		for (Field valueField : fields.values()) {
			try {
				if (debug) System.out.println("Writing field: " + valueField.getName() + " (" + value.getClass().getName() + ")");
				write(valueField.getType(), valueField.getName(), valueField.get(value), writer);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + valueField.getName() + " (" + value.getClass().getName()
					+ ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(valueField + " (" + value.getClass().getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(valueField + " (" + value.getClass().getName() + ")");
				throw ex;
			}
		}

		writer.pop();
	}

	public <T> T parse (Class<T> type, Reader reader) throws IOException {
		return (T)read(new JsonReader().parse(reader), type);
	}

	public <T> T parse (Class<T> type, InputStream input) throws IOException {
		return (T)read(new JsonReader().parse(input), type);
	}

	public <T> T parse (Class<T> type, FileHandle file) throws IOException {
		return (T)read(new JsonReader().parse(file), type);
	}

	public <T> T parse (Class<T> type, char[] data, int offset, int length) {
		return (T)read(new JsonReader().parse(data, offset, length), type);
	}

	public <T> T read (Class<T> type, String json) {
		return (T)read(new JsonReader().parse(json), type);
	}

	private Object read (Object value, Class type) {
		if (value instanceof ObjectMap) {
			ObjectMap<String, Object> map = (ObjectMap)value;
			String className = (String)map.remove(typeName);
			if (className != null) {
				try {
					type = Class.forName(className);
				} catch (ClassNotFoundException ex) {
					type = tagToClass.get(className);
					if (type == null) throw new SerializationException(ex);
				}
			}
			Object object;
			try {
				object = type.newInstance();
			} catch (Exception ex) {
				throw new SerializationException("Error creating instance of class: " + type.getName(), ex);
			}
			ObjectMap<String, Field> fields = typeToFields.get(type);
			if (fields == null) fields = cacheFields(type);
			for (Entry<String, Object> entry : map.entries()) {
				Field field = fields.get(entry.key);
				if (field == null)
					throw new SerializationException("Unable to find field: " + entry.key + " (" + type.getName() + ")");
				try {
					field.set(object, read(entry.value, field.getType()));
				} catch (Exception ex) {
					throw new SerializationException("Error setting field: " + field.getName() + " (" + type.getName() + ")", ex);
				}
			}
			return object;
		}

		if (value instanceof Array) {
			Array array = (Array)value;
			if (type.isAssignableFrom(Array.class)) {
				Array newArray = new Array(array.size);
				for (int i = 0, n = array.size; i < n; i++)
					newArray.add(read(array.get(i), String.class));
				return newArray;
			}
			if (type.isAssignableFrom(ArrayList.class)) {
				ArrayList newArray = new ArrayList(array.size);
				for (int i = 0, n = array.size; i < n; i++)
					newArray.add(read(array.get(i), String.class));
				return newArray;
			}
			if (type.isArray()) {
				Class componentType = type.getComponentType();
				Object newArray = java.lang.reflect.Array.newInstance(componentType, array.size);
				for (int i = 0, n = array.size; i < n; i++)
					java.lang.reflect.Array.set(newArray, i, read(array.get(i), componentType));
				return newArray;
			}
			throw new SerializationException("Unable to convert value to required type: " + value + " (" + type.getName() + ")");
		}

		if (value instanceof String) {
			String string = (String)value;
			if (type == String.class || value == null) return value;
			try {
				if (type == int.class || type == Integer.class) return Integer.valueOf(string);
				if (type == float.class || type == Float.class) return Float.valueOf(string);
				if (type == long.class || type == Long.class) return Long.valueOf(string);
				if (type == double.class || type == Double.class) return Double.valueOf(string);
				if (type == short.class || type == Short.class) return Short.valueOf(string);
				if (type == byte.class || type == Byte.class) return Byte.valueOf(string);
			} catch (NumberFormatException ignored) {
			}
			if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(string);
			if (type == char.class || type == Character.class) return string.charAt(0);
			if (type.isEnum()) {
				Object[] constants = type.getEnumConstants();
				for (int i = 0, n = constants.length; i < n; i++)
					if (string.equals(constants[i].toString())) return constants[i];
			}
			if (type == CharSequence.class) return string;
			throw new SerializationException("Unable to convert value to required type: " + value + " (" + type.getName() + ")");
		}

		return null;
	}
}
