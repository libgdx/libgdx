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
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap.Entry;

/** Reads/writes Java objects to/from JSON, automatically.
 * @author Nathan Sweet */
public class Json {
	private static final boolean debug = false;

	private JsonWriter writer;
	private String typeName = "class";
	private boolean usePrototypes = true;
	private OutputType outputType;
	private final ObjectMap<Class, ObjectMap<String, FieldMetadata>> typeToFields = new ObjectMap();
	private final ObjectMap<String, Class> tagToClass = new ObjectMap();
	private final ObjectMap<Class, String> classToTag = new ObjectMap();
	private final ObjectMap<Class, Serializer> classToSerializer = new ObjectMap();
	private final ObjectMap<Class, Object[]> classToDefaultValues = new ObjectMap();
	private boolean ignoreUnknownFields;

	public Json () {
		outputType = OutputType.minimal;
	}

	public Json (OutputType outputType) {
		this.outputType = outputType;
	}

	public void setIgnoreUnknownFields (boolean ignoreUnknownFields) {
		this.ignoreUnknownFields = ignoreUnknownFields;
	}

	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	public void addClassTag (String tag, Class type) {
		tagToClass.put(tag, type);
		classToTag.put(type, tag);
	}

	public Class getClass (String tag) {
		Class type = tagToClass.get(tag);
		if (type != null) return type;
		try {
			return Class.forName(tag);
		} catch (ClassNotFoundException ex) {
			throw new SerializationException(ex);
		}
	}

	public String getTag (Class type) {
		String tag = classToTag.get(type);
		if (tag != null) return tag;
		return type.getName();
	}

	/** Sets the name of the JSON field to store the Java class name or class tag when required to avoid ambiguity during
	 * deserialization. Set to null to never output this information, but be warned that deserialization may fail. */
	public void setTypeName (String typeName) {
		this.typeName = typeName;
	}

	public <T> void setSerializer (Class<T> type, Serializer<T> serializer) {
		classToSerializer.put(type, serializer);
	}

	public void setUsePrototypes (boolean usePrototypes) {
		this.usePrototypes = usePrototypes;
	}

	public void setElementType (Class type, String fieldName, Class elementType) {
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		metadata.elementType = elementType;
	}

	private ObjectMap<String, FieldMetadata> cacheFields (Class type) {
		ArrayList<Field> allFields = new ArrayList();
		Class nextClass = type;
		while (nextClass != Object.class) {
			Collections.addAll(allFields, nextClass.getDeclaredFields());
			nextClass = nextClass.getSuperclass();
		}

		ObjectMap<String, FieldMetadata> nameToField = new ObjectMap();
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

			nameToField.put(field.getName(), new FieldMetadata(field));
		}
		typeToFields.put(type, nameToField);
		return nameToField;
	}

	public String toJson (Object object) {
		return toJson(object, object == null ? null : object.getClass(), (Class)null);
	}

	public String toJson (Object object, Class knownType) {
		return toJson(object, knownType, (Class)null);
	}

	public String toJson (Object object, Class knownType, Class elementType) {
		StringWriter buffer = new StringWriter();
		toJson(object, knownType, elementType, buffer);
		return buffer.toString();
	}

	public void toJson (Object object, FileHandle file) {
		toJson(object, object == null ? null : object.getClass(), null, file);
	}

	public void toJson (Object object, Class knownType, FileHandle file) {
		toJson(object, knownType, null, file);
	}

	public void toJson (Object object, Class knownType, Class elementType, FileHandle file) {
		Writer writer = null;
		try {
			writer = file.writer(false);
			toJson(object, knownType, elementType, writer);
		} catch (Exception ex) {
			throw new SerializationException("Error writing file: " + file, ex);
		} finally {
			try {
				if (writer != null) writer.close();
			} catch (IOException ignored) {
			}
		}
	}

	public void toJson (Object object, Writer writer) {
		toJson(object, object == null ? null : object.getClass(), null, writer);
	}

	public void toJson (Object object, Class knownType, Writer writer) {
		toJson(object, knownType, null, writer);
	}

	public void toJson (Object object, Class knownType, Class elementType, Writer writer) {
		if (!(writer instanceof JsonWriter)) {
			this.writer = new JsonWriter(writer);
			((JsonWriter)this.writer).setOutputType(outputType);
		}
		writeValue(object, knownType, elementType);
	}

	public void writeFields (Object object) {
		Class type = object.getClass();

		Object[] defaultValues = getDefaultValues(type);

		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		int i = 0;
		for (FieldMetadata metadata : fields.values()) {
			Field field = metadata.field;
			try {
				Object value = field.get(object);

				if (defaultValues != null) {
					Object defaultValue = defaultValues[i++];
					if (value == null && defaultValue == null) continue;
					if (value != null && defaultValue != null && value.equals(defaultValue)) continue;
				}

				if (debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
				writer.name(field.getName());
				writeValue(value, field.getType(), metadata.elementType);
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			} catch (Exception runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field + " (" + type.getName() + ")");
				throw ex;
			}
		}
	}

	private Object[] getDefaultValues (Class type) {
		if (!usePrototypes) return null;
		if (classToDefaultValues.containsKey(type)) return classToDefaultValues.get(type);
		Object object;
		try {
			object = newInstance(type);
		} catch (Exception ex) {
			classToDefaultValues.put(type, null);
			return null;
		}

		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);

		Object[] values = new Object[fields.size];
		classToDefaultValues.put(type, values);

		int i = 0;
		for (FieldMetadata metadata : fields.values()) {
			Field field = metadata.field;
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
		return values;
	}

	public void writeField (Object object, String name) {
		writeField(object, name, name, null);
	}

	public void writeField (Object object, String name, Class elementType) {
		writeField(object, name, name, elementType);
	}

	public void writeField (Object object, String fieldName, String jsonName) {
		writeField(object, fieldName, jsonName, null);
	}

	public void writeField (Object object, String fieldName, String jsonName, Class elementType) {
		Class type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		Field field = metadata.field;
		if (elementType == null) elementType = metadata.elementType;
		try {
			if (debug) System.out.println("Writing field: " + field.getName() + " (" + type.getName() + ")");
			writer.name(jsonName);
			writeValue(field.get(object), field.getType(), elementType);
		} catch (IllegalAccessException ex) {
			throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
		} catch (SerializationException ex) {
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		} catch (Exception runtimeEx) {
			SerializationException ex = new SerializationException(runtimeEx);
			ex.addTrace(field + " (" + type.getName() + ")");
			throw ex;
		}
	}

	public void writeValue (String name, Object value) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, value.getClass(), null);
	}

	public void writeValue (String name, Object value, Class knownType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, null);
	}

	public void writeValue (String name, Object value, Class knownType, Class elementType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, elementType);
	}

	public void writeValue (Object value) {
		writeValue(value, value.getClass(), null);
	}

	public void writeValue (Object value, Class knownType) {
		writeValue(value, knownType, null);
	}

	public void writeValue (Object value, Class knownType, Class elementType) {
		try {
			if (value == null) {
				writer.value(null);
				return;
			}

			Class actualType = value.getClass();

			if (actualType.isPrimitive() || actualType == String.class || actualType == Integer.class || actualType == Boolean.class
				|| actualType == Float.class || actualType == Long.class || actualType == Double.class || actualType == Short.class
				|| actualType == Byte.class || actualType == Character.class) {
				writer.value(value);
				return;
			}

			if (value instanceof Serializable) {
				writeObjectStart(actualType, knownType);
				((Serializable)value).write(this);
				writeObjectEnd();
				return;
			}

			Serializer serializer = classToSerializer.get(actualType);
			if (serializer != null) {
				serializer.write(this, value, knownType);
				return;
			}

			if (value instanceof Array) {
				if (knownType != null && actualType != knownType)
					throw new SerializationException("Serialization of an Array other than the known type is not supported.\n"
						+ "Known type: " + knownType + "\nActual type: " + actualType);
				writeArrayStart();
				Array array = (Array)value;
				for (int i = 0, n = array.size; i < n; i++)
					writeValue(array.get(i), elementType, null);
				writeArrayEnd();
				return;
			}

			if (value instanceof Collection) {
				if (knownType != null && actualType != knownType)
					throw new SerializationException("Serialization of a Collection other than the known type is not supported.\n"
						+ "Known type: " + knownType + "\nActual type: " + actualType);
				writeArrayStart();
				for (Object item : (Collection)value)
					writeValue(item, elementType, null);
				writeArrayEnd();
				return;
			}

			if (actualType.isArray()) {
				if (elementType == null) elementType = actualType.getComponentType();
				int length = java.lang.reflect.Array.getLength(value);
				writeArrayStart();
				for (int i = 0; i < length; i++)
					writeValue(java.lang.reflect.Array.get(value, i), elementType, null);
				writeArrayEnd();
				return;
			}

			if (value instanceof ObjectMap) {
				if (knownType == null) knownType = ObjectMap.class;
				writeObjectStart(actualType, knownType);
				for (Entry entry : ((ObjectMap<?, ?>)value).entries()) {
					writer.name(convertToString(entry.key));
					writeValue(entry.value, elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof Map) {
				if (knownType == null) knownType = ObjectMap.class;
				writeObjectStart(actualType, knownType);
				for (Map.Entry entry : ((Map<?, ?>)value).entrySet()) {
					writer.name(convertToString(entry.getKey()));
					writeValue(entry.getValue(), elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (actualType.isEnum()) {
				writer.value(value);
				return;
			}

			writeObjectStart(actualType, knownType);
			writeFields(value);
			writeObjectEnd();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeObjectStart (String name) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeObjectStart();
	}

	public void writeObjectStart (String name, Class actualType, Class knownType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeObjectStart(actualType, knownType);
	}

	public void writeObjectStart () {
		try {
			writer.object();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeObjectStart (Class actualType, Class knownType) {
		try {
			writer.object();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (knownType == null || knownType != actualType) writeType(actualType);
	}

	public void writeObjectEnd () {
		try {
			writer.pop();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayStart (String name) {
		try {
			writer.name(name);
			writer.array();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayStart () {
		try {
			writer.array();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeArrayEnd () {
		try {
			writer.pop();
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
	}

	public void writeType (Class type) {
		if (typeName == null) return;
		String className = classToTag.get(type);
		if (className == null) className = type.getName();
		try {
			writer.set(typeName, className);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (debug) System.out.println("Writing type: " + type.getName());
	}

	public <T> T fromJson (Class<T> type, Reader reader) {
		return (T)readValue(type, null, new JsonReader().parse(reader));
	}

	public <T> T fromJson (Class<T> type, Class elementType, Reader reader) {
		return (T)readValue(type, elementType, new JsonReader().parse(reader));
	}

	public <T> T fromJson (Class<T> type, InputStream input) {
		return (T)readValue(type, null, new JsonReader().parse(input));
	}

	public <T> T fromJson (Class<T> type, Class elementType, InputStream input) {
		return (T)readValue(type, elementType, new JsonReader().parse(input));
	}

	public <T> T fromJson (Class<T> type, FileHandle file) {
		try {
			return (T)readValue(type, null, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	public <T> T fromJson (Class<T> type, Class elementType, FileHandle file) {
		try {
			return (T)readValue(type, elementType, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	public <T> T fromJson (Class<T> type, char[] data, int offset, int length) {
		return (T)readValue(type, null, new JsonReader().parse(data, offset, length));
	}

	public <T> T fromJson (Class<T> type, Class elementType, char[] data, int offset, int length) {
		return (T)readValue(type, elementType, new JsonReader().parse(data, offset, length));
	}

	public <T> T fromJson (Class<T> type, String json) {
		return (T)readValue(type, null, new JsonReader().parse(json));
	}

	public <T> T fromJson (Class<T> type, Class elementType, String json) {
		return (T)readValue(type, elementType, new JsonReader().parse(json));
	}

	public void readField (Object object, String name, Object jsonData) {
		readField(object, name, name, null, jsonData);
	}

	public void readField (Object object, String name, Class elementType, Object jsonData) {
		readField(object, name, name, elementType, jsonData);
	}

	public void readField (Object object, String fieldName, String jsonName, Object jsonData) {
		readField(object, fieldName, jsonName, null, jsonData);
	}

	public void readField (Object object, String fieldName, String jsonName, Class elementType, Object jsonData) {
		ObjectMap jsonMap = (ObjectMap)jsonData;
		Class type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		Field field = metadata.field;
		Object jsonValue = jsonMap.get(jsonName);
		if (jsonValue == null) return;
		if (elementType == null) elementType = metadata.elementType;
		try {
			field.set(object, readValue(field.getType(), elementType, jsonValue));
		} catch (IllegalAccessException ex) {
			throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
		} catch (SerializationException ex) {
			ex.addTrace(field.getName() + " (" + type.getName() + ")");
			throw ex;
		} catch (RuntimeException runtimeEx) {
			SerializationException ex = new SerializationException(runtimeEx);
			ex.addTrace(field.getName() + " (" + type.getName() + ")");
			throw ex;
		}
	}

	public void readFields (Object object, Object jsonData) {
		ObjectMap<String, Object> jsonMap = (ObjectMap)jsonData;
		Class type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		for (Entry<String, Object> entry : jsonMap.entries()) {
			FieldMetadata metadata = fields.get(entry.key);
			if (metadata == null) {
				if (ignoreUnknownFields) {
					if (debug) System.out.println("Ignoring unknown field: " + entry.key + " (" + type.getName() + ")");
					continue;
				} else
					throw new SerializationException("Field not found: " + entry.key + " (" + type.getName() + ")");
			}
			Field field = metadata.field;
			if (entry.value == null) continue;
			try {
				field.set(object, readValue(field.getType(), metadata.elementType, entry.value));
			} catch (IllegalAccessException ex) {
				throw new SerializationException("Error accessing field: " + field.getName() + " (" + type.getName() + ")", ex);
			} catch (SerializationException ex) {
				ex.addTrace(field.getName() + " (" + type.getName() + ")");
				throw ex;
			} catch (RuntimeException runtimeEx) {
				SerializationException ex = new SerializationException(runtimeEx);
				ex.addTrace(field.getName() + " (" + type.getName() + ")");
				throw ex;
			}
		}
	}

	public <T> T readValue (String name, Class<T> type, Object jsonData) {
		ObjectMap jsonMap = (ObjectMap)jsonData;
		return (T)readValue(type, null, jsonMap.get(name));
	}

	public <T> T readValue (String name, Class<T> type, T defaultValue, Object jsonData) {
		ObjectMap jsonMap = (ObjectMap)jsonData;
		Object jsonValue = jsonMap.get(name);
		if (jsonValue == null) return defaultValue;
		return (T)readValue(type, null, jsonValue);
	}

	public <T> T readValue (String name, Class<T> type, Class elementType, Object jsonData) {
		ObjectMap jsonMap = (ObjectMap)jsonData;
		return (T)readValue(type, elementType, jsonMap.get(name));
	}

	public <T> T readValue (String name, Class<T> type, Class elementType, T defaultValue, Object jsonData) {
		ObjectMap jsonMap = (ObjectMap)jsonData;
		Object jsonValue = jsonMap.get(name);
		if (jsonValue == null) return defaultValue;
		return (T)readValue(type, elementType, jsonValue);
	}

	public <T> T readValue (Class<T> type, Class elementType, T defaultValue, Object jsonData) {
		return (T)readValue(type, elementType, jsonData);
	}

	public <T> T readValue (Class<T> type, Object jsonData) {
		return (T)readValue(type, null, jsonData);
	}

	public <T> T readValue (Class<T> type, Class elementType, Object jsonData) {
		if (jsonData == null) return null;

		if (jsonData instanceof ObjectMap) {
			ObjectMap<String, Object> jsonMap = (ObjectMap)jsonData;

			String className = typeName == null ? null : (String)jsonMap.remove(typeName);
			if (className != null) {
				try {
					type = (Class<T>)Class.forName(className);
				} catch (ClassNotFoundException ex) {
					type = tagToClass.get(className);
					if (type == null) throw new SerializationException(ex);
				}
			}

			Object object;
			if (type != null) {
				Serializer serializer = classToSerializer.get(type);
				if (serializer != null) return (T)serializer.read(this, jsonMap, type);

				object = newInstance(type);

				if (object instanceof Serializable) {
					((Serializable)object).read(this, jsonMap);
					return (T)object;
				}

				if (object instanceof HashMap) {
					HashMap result = (HashMap)object;
					for (Entry entry : jsonMap.entries())
						result.put(entry.key, readValue(elementType, null, entry.value));
					return (T)result;
				}
			} else
				object = new ObjectMap();

			if (object instanceof ObjectMap) {
				ObjectMap result = (ObjectMap)object;
				for (Entry entry : jsonMap.entries())
					result.put(entry.key, readValue(elementType, null, entry.value));
				return (T)result;
			}

			readFields(object, jsonMap);
			return (T)object;
		}

		if (type != null) {
			Serializer serializer = classToSerializer.get(type);
			if (serializer != null) return (T)serializer.read(this, jsonData, type);
		}

		if (jsonData instanceof Array) {
			Array array = (Array)jsonData;
			if (type == null || type.isAssignableFrom(Array.class)) {
				Array newArray = new Array(array.size);
				for (int i = 0, n = array.size; i < n; i++)
					newArray.add(readValue(elementType, null, array.get(i)));
				return (T)newArray;
			}
			if (type.isAssignableFrom(ArrayList.class)) {
				ArrayList newArray = new ArrayList(array.size);
				for (int i = 0, n = array.size; i < n; i++)
					newArray.add(readValue(elementType, null, array.get(i)));
				return (T)newArray;
			}
			if (type.isArray()) {
				Class componentType = type.getComponentType();
				if (elementType == null) elementType = componentType;
				Object newArray = java.lang.reflect.Array.newInstance(componentType, array.size);
				for (int i = 0, n = array.size; i < n; i++)
					java.lang.reflect.Array.set(newArray, i, readValue(elementType, null, array.get(i)));
				return (T)newArray;
			}
			throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
		}

		if (jsonData instanceof Float) {
			Float floatValue = (Float)jsonData;
			try {
				if (type == null || type == float.class || type == Float.class) return (T)(Float)floatValue;
				if (type == int.class || type == Integer.class) return (T)(Integer)floatValue.intValue();
				if (type == long.class || type == Long.class) return (T)(Long)floatValue.longValue();
				if (type == double.class || type == Double.class) return (T)(Double)floatValue.doubleValue();
				if (type == short.class || type == Short.class) return (T)(Short)floatValue.shortValue();
				if (type == byte.class || type == Byte.class) return (T)(Byte)floatValue.byteValue();
			} catch (NumberFormatException ignored) {
			}
			jsonData = String.valueOf(jsonData);
		}

		if (jsonData instanceof Boolean) jsonData = String.valueOf(jsonData);

		if (jsonData instanceof String) {
			String string = (String)jsonData;
			if (type == null || type == String.class) return (T)jsonData;
			try {
				if (type == int.class || type == Integer.class) return (T)Integer.valueOf(string);
				if (type == float.class || type == Float.class) return (T)Float.valueOf(string);
				if (type == long.class || type == Long.class) return (T)Long.valueOf(string);
				if (type == double.class || type == Double.class) return (T)Double.valueOf(string);
				if (type == short.class || type == Short.class) return (T)Short.valueOf(string);
				if (type == byte.class || type == Byte.class) return (T)Byte.valueOf(string);
			} catch (NumberFormatException ignored) {
			}
			if (type == boolean.class || type == Boolean.class) return (T)Boolean.valueOf(string);
			if (type == char.class || type == Character.class) return (T)(Character)string.charAt(0);
			if (type.isEnum()) {
				Object[] constants = type.getEnumConstants();
				for (int i = 0, n = constants.length; i < n; i++)
					if (string.equals(constants[i].toString())) return (T)constants[i];
			}
			if (type == CharSequence.class) return (T)string;
			throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
		}

		return null;
	}

	private String convertToString (Object object) {
		if (object instanceof Class) return ((Class)object).getName();
		return String.valueOf(object);
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

	public String prettyPrint (Object object) {
		return prettyPrint(object, false);
	}

	public String prettyPrint (String json) {
		return prettyPrint(json, false);
	}

	public String prettyPrint (Object object, boolean fieldsOnSameLine) {
		return prettyPrint(toJson(object), fieldsOnSameLine);
	}

	public String prettyPrint (String json, boolean fieldsOnSameLine) {
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(new JsonReader().parse(json), buffer, 0, fieldsOnSameLine);
		return buffer.toString();
	}

	private void prettyPrint (Object object, StringBuilder buffer, int indent, boolean fieldsOnSameLine) {
		if (object instanceof ObjectMap) {
			ObjectMap<?, ?> map = (ObjectMap)object;
			if (map.size == 0) {
				buffer.append("{}");
			} else {
				boolean newLines = !fieldsOnSameLine || !isFlat(map) || map.size > 4;
				buffer.append(newLines ? "{\n" : "{ ");
				int i = 0;
				for (Entry entry : map.entries()) {
					if (newLines) indent(indent, buffer);
					buffer.append(outputType.quoteName((String)entry.key));
					buffer.append(": ");
					prettyPrint(entry.value, buffer, indent + 1, fieldsOnSameLine);
					if (i++ < map.size - 1) buffer.append(",");
					buffer.append(newLines ? '\n' : ' ');
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append('}');
			}
		} else if (object instanceof Array) {
			Array array = (Array)object;
			if (array.size == 0) {
				buffer.append("[]");
			} else {
				boolean newLines = !fieldsOnSameLine || !isFlat(array) || array.size > 4;
				buffer.append(newLines ? "[\n" : "[ ");
				for (int i = 0, n = array.size; i < n; i++) {
					if (newLines) indent(indent, buffer);
					prettyPrint(array.get(i), buffer, indent + 1, fieldsOnSameLine);
					if (i < array.size - 1) buffer.append(",");
					buffer.append(newLines ? '\n' : ' ');
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append(']');
			}
		} else if (object instanceof String) {
			buffer.append(outputType.quoteValue((String)object));
		} else if (object instanceof Float) {
			Float floatValue = (Float)object;
			int intValue = floatValue.intValue();
			buffer.append(floatValue - intValue == 0 ? intValue : object);
		} else if (object instanceof Boolean) {
			buffer.append(object);
		} else if (object == null) {
			buffer.append("null");
		} else
			throw new SerializationException("Unknown object type: " + object.getClass());
	}

	static private boolean isFlat (ObjectMap<?, ?> map) {
		for (Entry entry : map.entries()) {
			if (entry.value instanceof ObjectMap) return false;
			if (entry.value instanceof Array) return false;
		}
		return true;
	}

	static private boolean isFlat (Array array) {
		for (int i = 0, n = array.size; i < n; i++) {
			Object value = array.get(i);
			if (value instanceof ObjectMap) return false;
			if (value instanceof Array) return false;
		}
		return true;
	}

	static private void indent (int count, StringBuilder buffer) {
		for (int i = 0; i < count; i++)
			buffer.append('\t');
	}

	static private class FieldMetadata {
		public Field field;
		public Class elementType;

		public FieldMetadata (Field field) {
			this.field = field;
		}
	}

	static public interface Serializer<T> {
		public void write (Json json, T object, Class knownType);

		public T read (Json json, Object jsonData, Class type);
	}

	static public interface Serializable {
		public void write (Json json);

		public void read (Json json, ObjectMap<String, Object> jsonData);
	}
}
