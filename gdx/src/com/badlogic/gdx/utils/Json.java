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
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/** Reads/writes Java objects to/from JSON, automatically.
 * @author Nathan Sweet */
public class Json {
	private static final boolean debug = false;

	private final ObjectMap<Class, ObjectMap<String, Field>> typeToFields = new ObjectMap();
	private final ObjectMap<String, Class> tagToClass = new ObjectMap();
	private final ObjectMap<Class, String> classToTag = new ObjectMap();
	private final ObjectMap<Class, Serializer> classToSerializer = new ObjectMap();
	private final ObjectMap<Class, Object[]> classToDefaultValues = new ObjectMap();
	private String typeName = "type";
	private boolean usePrototypes = true;

	public void addClassTag (String tag, Class type) {
		tagToClass.put(tag, type);
		classToTag.put(type, tag);
	}

	/** Sets the name of the JSON field to store the Java class name or class tag when required to avoid ambiguity during
	 * deserialization. Set to null to never output this information, but be warned that deserialization may fail. */
	public void setTypeName (String typeName) {
		this.typeName = typeName;
	}

	public void setSerializer (Class type, Serializer serializer) {
		classToSerializer.put(type, serializer);
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

	public String write (Object object) {
		StringWriter buffer = new StringWriter();
		try {
			write(object, buffer);
		} catch (IOException ex) {
			throw new SerializationException("Error writing JSON.", ex);
		}
		return buffer.toString();
	}

	public void write (Object object, Writer writer) throws IOException {
		if (!(writer instanceof JsonWriter)) writer = new JsonWriter(writer);
		writeValue(null, object, object.getClass(), (JsonWriter)writer);
	}

	public void writeFields (Object object, JsonWriter writer) throws IOException {
		Class type = object.getClass();

		Object[] defaultValues = getDefaultValues(type);

		ObjectMap<String, Field> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		int i = 0;
		for (Field field : fields.values()) {
			try {
				Object value = field.get(object);

				if (defaultValues != null) {
					Object defaultValue = defaultValues[i++];
					if (value == null && defaultValue == null) continue;
					if (value != null && defaultValue != null && value.equals(defaultValue)) continue;
				}

				if (debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
				writeValue(field.getName(), value, field.getType(), writer);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			}
		}
	}

	private Object[] getDefaultValues (Class type) {
		if (!usePrototypes) return null;
		Object[] values = classToDefaultValues.get(type);
		if (values == null) {
			Object object = newInstance(type);

			ObjectMap<String, Field> fields = typeToFields.get(type);
			if (fields == null) fields = cacheFields(type);

			values = new Object[fields.size];
			classToDefaultValues.put(type, values);

			int i = 0;
			for (Field field : fields.values()) {
				try {
					values[i++] = field.get(object);
				} catch (IllegalAccessException ex) {
					throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
				} catch (SerializationException ex) {
					ex.addTrace(field + " (" + type.getName() + ")");
					throw ex;
				} catch (RuntimeException runtimeEx) {
					SerializationException ex = new SerializationException(runtimeEx);
					ex.addTrace(field + " (" + type.getName() + ")");
					throw ex;
				}
			}
		}
		return values;
	}

	public void writeField (Object object, String name, JsonWriter writer) throws IOException {
		Class type = object.getClass();
		ObjectMap<String, Field> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		Field field = fields.get(name);
		if (field == null) throw new SerializationException("Field not found: " + name + " (" + type.getName() + ")");
		try {
			if (debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
			writeValue(field.getName(), field.get(object), field.getType(), writer);
		} catch (IllegalAccessException ex) {
			throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
		} catch (SerializationException ex) {
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		} catch (RuntimeException runtimeEx) {
			SerializationException ex = new SerializationException(runtimeEx);
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		}
	}

	public void writeValue (String name, Object value, Class valueType, JsonWriter writer) throws IOException {
		if (value == null) {
			if (name == null)
				writer.add(value);
			else
				writer.set(name, value);
			return;
		}

		Class actualType = value.getClass();

		if (actualType.isPrimitive() || actualType == String.class || actualType == Integer.class || actualType == Boolean.class
			|| actualType == Float.class || actualType == Long.class || actualType == Double.class || actualType == Short.class
			|| actualType == Byte.class || actualType == Character.class) {
			if (name == null)
				writer.add(value);
			else
				writer.set(name, value);
			return;
		}

		if (value instanceof Serializable) {
			startObject(name, valueType, actualType, writer);
			((Serializable)value).write(this, writer);
			writer.pop();
			return;
		}

		Serializer serializer = classToSerializer.get(actualType);
		if (serializer != null) {
			startObject(name, valueType, actualType, writer);
			serializer.write(this, writer, value);
			writer.pop();
			return;
		}

		if (value instanceof Collection) {
			if (name == null)
				writer.array();
			else
				writer.array(name);
			for (Object item : (Collection)value)
				writeValue(null, item, null, writer);
			writer.pop();
			return;
		}

		if (value instanceof Array) {
			if (name == null)
				writer.array();
			else
				writer.array(name);
			for (Object item : (Array)value)
				writeValue(null, item, null, writer);
			writer.pop();
			return;
		}

		if (actualType.isArray()) {
			if (name == null)
				writer.array();
			else
				writer.array(name);

			Class componentType = actualType.getComponentType();
			int length = java.lang.reflect.Array.getLength(value);
			for (int i = 0; i < length; i++)
				writeValue(null, java.lang.reflect.Array.get(value, i), componentType, writer);

			writer.pop();
			return;
		}

		if (value instanceof ObjectMap) {
			startObject(name, valueType, actualType, writer);
			for (Entry entry : ((ObjectMap<?, ?>)value).entries())
				writer.set((String)entry.key, entry.value);
			writer.pop();
			return;
		}

		if (value instanceof Map) {
			startObject(name, valueType, actualType, writer);
			for (Map.Entry entry : ((Map<?, ?>)value).entrySet())
				writer.set((String)entry.getKey(), entry.getValue());
			writer.pop();
			return;
		}

		if (actualType.isEnum()) {
			writer.set(name, value);
			return;
		}

		startObject(name, valueType, actualType, writer);
		writeFields(value, writer);
		writer.pop();
	}

	private void startObject (String name, Class valueType, Class actualType, JsonWriter writer) throws IOException {
		if (name == null)
			writer.object();
		else
			writer.object(name);

		if ((valueType == null || valueType != actualType) && typeName != null) {
			String className = classToTag.get(actualType);
			if (className == null) className = actualType.getName();
			writer.set(typeName, className);
			if (debug) System.out.println("Writing type: " + actualType.getName());
		}
	}

	public <T> T read (Class<T> type, Reader reader) throws IOException {
		return (T)readValue(type, new JsonReader().parse(reader));
	}

	public <T> T read (Class<T> type, InputStream input) throws IOException {
		return (T)readValue(type, new JsonReader().parse(input));
	}

	public <T> T read (Class<T> type, FileHandle file) {
		return (T)readValue(type, new JsonReader().parse(file));
	}

	public <T> T read (Class<T> type, char[] data, int offset, int length) {
		return (T)readValue(type, new JsonReader().parse(data, offset, length));
	}

	public <T> T read (Class<T> type, String json) {
		return (T)readValue(type, new JsonReader().parse(json));
	}

	public void readField (Object object, String name, ObjectMap map) {
		Class type = object.getClass();
		ObjectMap<String, Field> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		Field field = fields.get(name);
		if (field == null) throw new SerializationException("Unable to find field: " + name + " (" + type.getName() + ")");
		Object value = map.get(name);
		if (value == null) return;
		try {
			field.set(object, readValue(field.getType(), value));
		} catch (Exception ex) {
			throw new SerializationException("Error setting field: " + field.getName() + " (" + type.getName() + ")", ex);
		}
	}

	public void readFields (Object object, ObjectMap<String, Object> map) {
		Class type = object.getClass();
		ObjectMap<String, Field> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		for (Entry<String, Object> entry : map.entries()) {
			Field field = fields.get(entry.key);
			if (field == null) throw new SerializationException("Unable to find field: " + entry.key + " (" + type.getName() + ")");
			if (entry.value == null) continue;
			try {
				field.set(object, readValue(field.getType(), entry.value));
			} catch (Exception ex) {
				throw new SerializationException("Error setting field: " + field.getName() + " (" + type.getName() + ")", ex);
			}
		}
	}

	public <T> T readValue (Class<T> type, String name, ObjectMap map) {
		return (T)readValue(type, map.get(name));
	}

	public <T> T readValue (Class<T> type, String name, ObjectMap map, T defaultValue) {
		Object value = map.get(name);
		if (value == null) return defaultValue;
		return (T)readValue(type, value);
	}

	private Object newInstance (Class type) {
		try {
			return type.newInstance();
		} catch (Exception ex) {
			try {
				// Try a private constructor.
				Constructor constructor = type.getDeclaredConstructor();
				constructor.setAccessible(true);
				return constructor.newInstance();
			} catch (SecurityException ignored) {
			} catch (NoSuchMethodException ignored) {
				if (type.isMemberClass() && !Modifier.isStatic(type.getModifiers()))
					throw new SerializationException("Class cannot be created (non-static member class): " + type.getName(), ex);
				else
					throw new SerializationException("Class cannot be created (missing no-arg constructor): " + type.getName(), ex);
			} catch (Exception privateConstructorException) {
				ex = privateConstructorException;
			}
			throw new SerializationException("Error constructing instance of class: " + type.getName(), ex);
		}
	}

	private Object readValue (Class type, Object value) {
		if (value instanceof ObjectMap) {
			ObjectMap<String, Object> map = (ObjectMap)value;
			String className = typeName == null ? null : (String)map.remove(typeName);
			if (className != null) {
				try {
					type = Class.forName(className);
				} catch (ClassNotFoundException ex) {
					type = tagToClass.get(className);
					if (type == null) throw new SerializationException(ex);
				}
			}

			Serializer serializer = classToSerializer.get(type);
			if (serializer != null) return serializer.read(this, map, type);

			Object object = newInstance(type);

			if (object instanceof Serializable) {
				((Serializable)object).read(this, map);
				return object;
			}

			if (object instanceof ObjectMap) {
				ObjectMap result = (ObjectMap)object;
				for (Entry entry : map.entries())
					result.put(entry.key, readValue(String.class, entry.value));
				return result;
			}

			if (object instanceof HashMap) {
				HashMap result = (HashMap)object;
				for (Entry entry : map.entries())
					result.put(entry.key, readValue(String.class, entry.value));
				return result;
			}

			ObjectMap<String, Field> fields = typeToFields.get(type);
			if (fields == null) fields = cacheFields(type);
			for (Entry<String, Object> entry : map.entries()) {
				Field field = fields.get(entry.key);
				if (field == null)
					throw new SerializationException("Unable to find field: " + entry.key + " (" + type.getName() + ")");
				try {
					field.set(object, readValue(field.getType(), entry.value));
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
					newArray.add(readValue(String.class, array.get(i)));
				return newArray;
			}
			if (type.isAssignableFrom(ArrayList.class)) {
				ArrayList newArray = new ArrayList(array.size);
				for (int i = 0, n = array.size; i < n; i++)
					newArray.add(readValue(String.class, array.get(i)));
				return newArray;
			}
			if (type.isArray()) {
				Class componentType = type.getComponentType();
				Object newArray = java.lang.reflect.Array.newInstance(componentType, array.size);
				for (int i = 0, n = array.size; i < n; i++)
					java.lang.reflect.Array.set(newArray, i, readValue(componentType, array.get(i)));
				return newArray;
			}
			throw new SerializationException("Unable to convert value to required type: " + value + " (" + type.getName() + ")");
		}

		if (value instanceof Float) {
			Float floatValue = (Float)value;
			try {
				if (type == int.class || type == Integer.class) return floatValue.intValue();
				if (type == float.class || type == Float.class) return floatValue;
				if (type == long.class || type == Long.class) return floatValue.longValue();
				if (type == double.class || type == Double.class) return floatValue.doubleValue();
				if (type == short.class || type == Short.class) return floatValue.shortValue();
				if (type == byte.class || type == Byte.class) return floatValue.byteValue();
			} catch (NumberFormatException ignored) {
			}
			value = String.valueOf(value);
		}

		if (value instanceof Boolean) value = String.valueOf(value);

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

	public String prettyPrint (Object object) {
		return prettyPrint(write(object));
	}

	static public String prettyPrint (String json) {
		JsonReader reader = new JsonReader();
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(reader.parse(json), buffer, 0);
		return buffer.toString();
	}

	static private void prettyPrint (Object object, StringBuilder buffer, int indent) {
		if (object instanceof ObjectMap) {
			ObjectMap<?, ?> map = (ObjectMap)object;
			if (map.size == 0) {
				buffer.append("{}");
			} else if (map.size == 1) {
				buffer.append("{ ");
				Entry entry = map.entries().next();
				buffer.append('"');
				buffer.append(entry.key);
				buffer.append("\": ");
				prettyPrint(entry.value, buffer, indent + 1);
				buffer.append(" }");
			} else {
				buffer.append("{\n");
				int i = 0;
				for (Entry entry : map.entries()) {
					indent(indent, buffer);
					buffer.append('"');
					buffer.append(entry.key);
					buffer.append("\": ");
					prettyPrint(entry.value, buffer, indent + 1);
					if (i++ < map.size - 1) buffer.append(",");
					buffer.append('\n');
				}
				indent(indent - 1, buffer);
				buffer.append('}');
			}
		} else if (object instanceof Array) {
			Array array = (Array)object;
			if (array.size == 0) {
				buffer.append("[]");
			} else if (array.size == 1) {
				buffer.append("[ ");
				prettyPrint(array.get(0), buffer, indent + 1);
				buffer.append(" ]");
			} else {
				buffer.append("[\n");
				for (int i = 0, n = array.size; i < n; i++) {
					indent(indent, buffer);
					prettyPrint(array.get(i), buffer, indent + 1);
					if (i < array.size - 1) buffer.append(",");
					buffer.append('\n');
				}
				indent(indent - 1, buffer);
				buffer.append(']');
			}
		} else if (object instanceof String) {
			buffer.append('"');
			buffer.append(object);
			buffer.append('"');
		} else if (object instanceof Float) {
			Float floatValue = (Float)object;
			int intValue = floatValue.intValue();
			buffer.append(floatValue - intValue == 0 ? intValue : object);
		} else if (object instanceof Boolean) {
			buffer.append(object);
		} else if (object == null) {
			buffer.append("null");
		} else
			throw new IllegalArgumentException("Unknown object type: " + object.getClass());
	}

	static private void indent (int count, StringBuilder buffer) {
		for (int i = 0; i < count; i++)
			buffer.append('\t');
	}

	static public interface Serializer<T> {
		public void write (Json json, JsonWriter writer, T object) throws IOException;

		public T read (Json json, ObjectMap map, Class type);
	}

	static public interface Serializable {
		public void write (Json json, JsonWriter writer) throws IOException;

		public void read (Json json, ObjectMap map);
	}
}
