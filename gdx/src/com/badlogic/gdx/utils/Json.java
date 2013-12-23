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

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.ObjectMap.Values;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Reads/writes Java objects to/from JSON, automatically. See the wiki for usage:
 * https://github.com/libgdx/libgdx/wiki/Reading-%26-writing-JSON
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
	private Serializer defaultSerializer;
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
			return ClassReflection.forName(tag);
		} catch (ReflectionException ex) {
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

	public void setDefaultSerializer (Serializer defaultSerializer) {
		this.defaultSerializer = defaultSerializer;
	}

	public <T> void setSerializer (Class<T> type, Serializer<T> serializer) {
		classToSerializer.put(type, serializer);
	}

	public <T> Serializer<T> getSerializer (Class<T> type) {
		return classToSerializer.get(type);
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
			Collections.addAll(allFields, ClassReflection.getDeclaredFields(nextClass));
			nextClass = nextClass.getSuperclass();
		}

		ObjectMap<String, FieldMetadata> nameToField = new ObjectMap();
		for (int i = 0, n = allFields.size(); i < n; i++) {
			Field field = allFields.get(i);

			if (field.isTransient()) continue;
			if (field.isStatic()) continue;
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

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public String toJson (Object object, Class knownType, Class elementType) {
		StringWriter buffer = new StringWriter();
		toJson(object, knownType, elementType, buffer);
		return buffer.toString();
	}

	public void toJson (Object object, FileHandle file) {
		toJson(object, object == null ? null : object.getClass(), null, file);
	}

	/** @param knownType May be null if the type is unknown. */
	public void toJson (Object object, Class knownType, FileHandle file) {
		toJson(object, knownType, null, file);
	}

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void toJson (Object object, Class knownType, Class elementType, FileHandle file) {
		Writer writer = null;
		try {
			writer = file.writer(false, "UTF-8");
			toJson(object, knownType, elementType, writer);
		} catch (Exception ex) {
			throw new SerializationException("Error writing file: " + file, ex);
		} finally {
			StreamUtils.closeQuietly(writer);
		}
	}

	public void toJson (Object object, Writer writer) {
		toJson(object, object == null ? null : object.getClass(), null, writer);
	}

	/** @param knownType May be null if the type is unknown. */
	public void toJson (Object object, Class knownType, Writer writer) {
		toJson(object, knownType, null, writer);
	}

	/** @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void toJson (Object object, Class knownType, Class elementType, Writer writer) {
		setWriter(writer);
		try {
			writeValue(object, knownType, elementType);
		} finally {
			StreamUtils.closeQuietly(this.writer);
			this.writer = null;
		}
	}

	/** Sets the writer where JSON output will go. This is only necessary when not using the toJson methods. */
	public void setWriter (Writer writer) {
		if (!(writer instanceof JsonWriter)) writer = new JsonWriter(writer);
		this.writer = (JsonWriter)writer;
		this.writer.setOutputType(outputType);
	}

	public JsonWriter getWriter () {
		return writer;
	}

	/** Writes all fields of the specified object to the current JSON object. */
	public void writeFields (Object object) {
		Class type = object.getClass();

		Object[] defaultValues = getDefaultValues(type);

		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		int i = 0;
		for (FieldMetadata metadata : new Values<FieldMetadata>(fields)) {
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
			} catch (ReflectionException ex) {
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
			} catch (ReflectionException ex) {
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

	/** @see #writeField(Object, String, String, Class) */
	public void writeField (Object object, String name) {
		writeField(object, name, name, null);
	}

	/** @param elementType May be null if the type is unknown.
	 * @see #writeField(Object, String, String, Class) */
	public void writeField (Object object, String name, Class elementType) {
		writeField(object, name, name, elementType);
	}

	/** @see #writeField(Object, String, String, Class) */
	public void writeField (Object object, String fieldName, String jsonName) {
		writeField(object, fieldName, jsonName, null);
	}

	/** Writes the specified field to the current JSON object.
	 * @param elementType May be null if the type is unknown. */
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
		} catch (ReflectionException ex) {
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

	/** Writes the value as a field on the current JSON object, without writing the actual class.
	 * @param value May be null.
	 * @see #writeValue(String, Object, Class, Class) */
	public void writeValue (String name, Object value) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		if (value == null)
			writeValue(value, null, null);
		else
			writeValue(value, value.getClass(), null);
	}

	/** Writes the value as a field on the current JSON object, writing the class of the object if it differs from the specified
	 * known type.
	 * @param value May be null.
	 * @param knownType May be null if the type is unknown.
	 * @see #writeValue(String, Object, Class, Class) */
	public void writeValue (String name, Object value, Class knownType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, null);
	}

	/** Writes the value as a field on the current JSON object, writing the class of the object if it differs from the specified
	 * known type. The specified element type is used as the default type for collections.
	 * @param value May be null.
	 * @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void writeValue (String name, Object value, Class knownType, Class elementType) {
		try {
			writer.name(name);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		}
		writeValue(value, knownType, elementType);
	}

	/** Writes the value, without writing the class of the object.
	 * @param value May be null. */
	public void writeValue (Object value) {
		if (value == null)
			writeValue(value, null, null);
		else
			writeValue(value, value.getClass(), null);
	}

	/** Writes the value, writing the class of the object if it differs from the specified known type.
	 * @param value May be null.
	 * @param knownType May be null if the type is unknown. */
	public void writeValue (Object value, Class knownType) {
		writeValue(value, knownType, null);
	}

	/** Writes the value, writing the class of the object if it differs from the specified known type. The specified element type is
	 * used as the default type for collections.
	 * @param value May be null.
	 * @param knownType May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown. */
	public void writeValue (Object value, Class knownType, Class elementType) {
		try {
			if (value == null) {
				writer.value(null);
				return;
			}

			if ((knownType != null && knownType.isPrimitive()) || knownType == String.class || knownType == Integer.class
				|| knownType == Boolean.class || knownType == Float.class || knownType == Long.class || knownType == Double.class
				|| knownType == Short.class || knownType == Byte.class || knownType == Character.class) {
				writer.value(value);
				return;
			}

			Class actualType = value.getClass();

			if (actualType.isPrimitive() || actualType == String.class || actualType == Integer.class || actualType == Boolean.class
				|| actualType == Float.class || actualType == Long.class || actualType == Double.class || actualType == Short.class
				|| actualType == Byte.class || actualType == Character.class) {
				writeObjectStart(actualType, null);
				writeValue("value", value);
				writeObjectEnd();
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
				if (knownType != null && actualType != knownType && actualType != Array.class)
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
				if (knownType != null && actualType != knownType && actualType != ArrayList.class)
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
				int length = ArrayReflection.getLength(value);
				writeArrayStart();
				for (int i = 0; i < length; i++)
					writeValue(ArrayReflection.get(value, i), elementType, null);
				writeArrayEnd();
				return;
			}

			if (value instanceof OrderedMap) {
				if (knownType == null) knownType = OrderedMap.class;
				writeObjectStart(actualType, knownType);
				OrderedMap map = (OrderedMap)value;
				for (Object key : map.orderedKeys()) {
					writer.name(convertToString(key));
					writeValue(map.get(key), elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof ArrayMap) {
				if (knownType == null) knownType = ArrayMap.class;
				writeObjectStart(actualType, knownType);
				ArrayMap map = (ArrayMap)value;
				for (int i = 0, n = map.size; i < n; i++) {
					writer.name(convertToString(map.keys[i]));
					writeValue(map.values[i], elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof ObjectMap) {
				if (knownType == null) knownType = OrderedMap.class;
				writeObjectStart(actualType, knownType);
				for (Entry entry : ((ObjectMap<?, ?>)value).entries()) {
					writer.name(convertToString(entry.key));
					writeValue(entry.value, elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (value instanceof Map) {
				if (knownType == null) knownType = HashMap.class;
				writeObjectStart(actualType, knownType);
				for (Map.Entry entry : ((Map<?, ?>)value).entrySet()) {
					writer.name(convertToString(entry.getKey()));
					writeValue(entry.getValue(), elementType, null);
				}
				writeObjectEnd();
				return;
			}

			if (ClassReflection.isAssignableFrom(Enum.class, actualType)) {
				if (knownType == null || !knownType.equals(actualType)) {
					writeObjectStart(actualType, null);
					writer.name("value");
					writer.value(value);
					writeObjectEnd();
				} else {
					writer.value(value);
				}
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

	/** @param knownType May be null if the type is unknown. */
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

	/** Starts writing an object, writing the actualType to a field if needed.
	 * @param knownType May be null if the type is unknown. */
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

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Reader reader) {
		return (T)readValue(type, null, new JsonReader().parse(reader));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class elementType, Reader reader) {
		return (T)readValue(type, elementType, new JsonReader().parse(reader));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, InputStream input) {
		return (T)readValue(type, null, new JsonReader().parse(input));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class elementType, InputStream input) {
		return (T)readValue(type, elementType, new JsonReader().parse(input));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, FileHandle file) {
		try {
			return (T)readValue(type, null, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class elementType, FileHandle file) {
		try {
			return (T)readValue(type, elementType, new JsonReader().parse(file));
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, char[] data, int offset, int length) {
		return (T)readValue(type, null, new JsonReader().parse(data, offset, length));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class elementType, char[] data, int offset, int length) {
		return (T)readValue(type, elementType, new JsonReader().parse(data, offset, length));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, String json) {
		return (T)readValue(type, null, new JsonReader().parse(json));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T fromJson (Class<T> type, Class elementType, String json) {
		return (T)readValue(type, elementType, new JsonReader().parse(json));
	}

	public void readField (Object object, String name, JsonValue jsonData) {
		readField(object, name, name, null, jsonData);
	}

	public void readField (Object object, String name, Class elementType, JsonValue jsonData) {
		readField(object, name, name, elementType, jsonData);
	}

	public void readField (Object object, String fieldName, String jsonName, JsonValue jsonData) {
		readField(object, fieldName, jsonName, null, jsonData);
	}

	/** @param elementType May be null if the type is unknown. */
	public void readField (Object object, String fieldName, String jsonName, Class elementType, JsonValue jsonMap) {
		Class type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		FieldMetadata metadata = fields.get(fieldName);
		if (metadata == null) throw new SerializationException("Field not found: " + fieldName + " (" + type.getName() + ")");
		Field field = metadata.field;
		JsonValue jsonValue = jsonMap.get(jsonName);
		if (jsonValue == null) return;
		if (elementType == null) elementType = metadata.elementType;
		try {
			field.set(object, readValue(field.getType(), elementType, jsonValue));
		} catch (ReflectionException ex) {
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

	public void readFields (Object object, JsonValue jsonMap) {
		Class type = object.getClass();
		ObjectMap<String, FieldMetadata> fields = typeToFields.get(type);
		if (fields == null) fields = cacheFields(type);
		for (JsonValue child = jsonMap.child(); child != null; child = child.next()) {
			FieldMetadata metadata = fields.get(child.name());
			if (metadata == null) {
				if (ignoreUnknownFields) {
					if (debug) System.out.println("Ignoring unknown field: " + child.name() + " (" + type.getName() + ")");
					continue;
				} else
					throw new SerializationException("Field not found: " + child.name() + " (" + type.getName() + ")");
			}
			Field field = metadata.field;
			// if (entry.value == null) continue; // I don't remember what this did. :(
			try {
				field.set(object, readValue(field.getType(), metadata.elementType, child));
			} catch (ReflectionException ex) {
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

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (String name, Class<T> type, JsonValue jsonMap) {
		return (T)readValue(type, null, jsonMap.get(name));
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (String name, Class<T> type, T defaultValue, JsonValue jsonMap) {
		JsonValue jsonValue = jsonMap.get(name);
		if (jsonValue == null) return defaultValue;
		return (T)readValue(type, null, jsonValue);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (String name, Class<T> type, Class elementType, JsonValue jsonMap) {
		return (T)readValue(type, elementType, jsonMap.get(name));
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (String name, Class<T> type, Class elementType, T defaultValue, JsonValue jsonMap) {
		JsonValue jsonValue = jsonMap.get(name);
		if (jsonValue == null) return defaultValue;
		return (T)readValue(type, elementType, jsonValue);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (Class<T> type, Class elementType, T defaultValue, JsonValue jsonData) {
		return (T)readValue(type, elementType, jsonData);
	}

	/** @param type May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (Class<T> type, JsonValue jsonData) {
		return (T)readValue(type, null, jsonData);
	}

	/** @param type May be null if the type is unknown.
	 * @param elementType May be null if the type is unknown.
	 * @return May be null. */
	public <T> T readValue (Class<T> type, Class elementType, JsonValue jsonData) {
		if (jsonData == null) return null;

		if (jsonData.isObject()) {
			String className = typeName == null ? null : jsonData.getString(typeName, null);
			if (className != null) {
				jsonData.remove(typeName);
				try {
					type = (Class<T>)ClassReflection.forName(className);
				} catch (ReflectionException ex) {
					type = tagToClass.get(className);
					if (type == null) throw new SerializationException(ex);
				}
			}

			Object object;
			if (type != null) {
				if (type == String.class || type == Integer.class || type == Boolean.class || type == Float.class
					|| type == Long.class || type == Double.class || type == Short.class || type == Byte.class
					|| type == Character.class || type.isEnum()) {
					return readValue("value", type, jsonData);
				}

				Serializer serializer = classToSerializer.get(type);
				if (serializer != null) return (T)serializer.read(this, jsonData, type);

				object = newInstance(type);

				if (object instanceof Serializable) {
					((Serializable)object).read(this, jsonData);
					return (T)object;
				}

				if (object instanceof HashMap) {
					HashMap result = (HashMap)object;
					for (JsonValue child = jsonData.child(); child != null; child = child.next())
						result.put(child.name(), readValue(elementType, null, child));
					return (T)result;
				}
			} else if (defaultSerializer != null) {
				return (T)defaultSerializer.read(this, jsonData, type);
			} else
				return (T)jsonData;

			if (object instanceof ObjectMap) {
				ObjectMap result = (ObjectMap)object;
				for (JsonValue child = jsonData.child(); child != null; child = child.next())
					result.put(child.name(), readValue(elementType, null, child));
				return (T)result;
			}
			readFields(object, jsonData);
			return (T)object;
		}

		if (type != null) {
			Serializer serializer = classToSerializer.get(type);
			if (serializer != null) return (T)serializer.read(this, jsonData, type);
		}

		if (jsonData.isArray()) {
			if ((type == null || type == Object.class) || ClassReflection.isAssignableFrom(Array.class, type)) {
				Array newArray = (type == null || type == Object.class) ? new Array() : (Array)newInstance(type);
				for (JsonValue child = jsonData.child(); child != null; child = child.next())
					newArray.add(readValue(elementType, null, child));
				return (T)newArray;
			}
			if (ClassReflection.isAssignableFrom(List.class, type)) {
				List newArray = (type == null || type.isInterface()) ? new ArrayList() : (List)newInstance(type);
				for (JsonValue child = jsonData.child(); child != null; child = child.next())
					newArray.add(readValue(elementType, null, child));
				return (T)newArray;
			}
			if (type.isArray()) {
				Class componentType = type.getComponentType();
				if (elementType == null) elementType = componentType;
				Object newArray = ArrayReflection.newInstance(componentType, jsonData.size);
				int i = 0;
				for (JsonValue child = jsonData.child(); child != null; child = child.next())
					ArrayReflection.set(newArray, i++, readValue(elementType, null, child));
				return (T)newArray;
			}
			throw new SerializationException("Unable to convert value to required type: " + jsonData + " (" + type.getName() + ")");
		}

		if (jsonData.isNumber()) {
			try {
				if (type == null || type == float.class || type == Float.class) return (T)(Float)jsonData.asFloat();
				if (type == int.class || type == Integer.class) return (T)(Integer)jsonData.asInt();
				if (type == long.class || type == Long.class) return (T)(Long)jsonData.asLong();
				if (type == double.class || type == Double.class) return (T)(Double)(double)jsonData.asFloat();
				if (type == String.class) return (T)Float.toString(jsonData.asFloat());
				if (type == short.class || type == Short.class) return (T)(Short)(short)jsonData.asInt();
				if (type == byte.class || type == Byte.class) return (T)(Byte)(byte)jsonData.asInt();
			} catch (NumberFormatException ignored) {
			}
			jsonData = new JsonValue(jsonData.asString());
		}

		if (jsonData.isBoolean()) {
			try {
				if (type == null || type == boolean.class || type == Boolean.class) return (T)(Boolean)jsonData.asBoolean();
			} catch (NumberFormatException ignored) {
			}
			jsonData = new JsonValue(jsonData.asString());
		}

		if (jsonData.isString()) {
			String string = jsonData.asString();
			if (type == null || type == String.class) return (T)string;
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
			if (ClassReflection.isAssignableFrom(Enum.class, type)) {
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

	protected Object newInstance (Class type) {
		try {
			return ClassReflection.newInstance(type);
		} catch (Exception ex) {
			try {
				// Try a private constructor.
				Constructor constructor = ClassReflection.getDeclaredConstructor(type);
				constructor.setAccessible(true);
				return constructor.newInstance();
			} catch (SecurityException ignored) {
			} catch (ReflectionException ignored) {
				if (type.isEnum()) {
					return type.getEnumConstants()[0];
				}
				if (type.isArray())
					throw new SerializationException("Encountered JSON object when expected array of type: " + type.getName(), ex);
				else if (ClassReflection.isMemberClass(type) && !ClassReflection.isStaticClass(type))
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
		return prettyPrint(object, 0);
	}

	public String prettyPrint (String json) {
		return prettyPrint(json, 0);
	}

	public String prettyPrint (Object object, int singleLineColumns) {
		return prettyPrint(toJson(object), singleLineColumns);
	}

	public String prettyPrint (String json, int singleLineColumns) {
		return new JsonReader().parse(json).prettyPrint(outputType, singleLineColumns);
	}

	public String prettyPrint (Object object, PrettyPrintSettings settings) {
		return prettyPrint(toJson(object), settings);
	}

	public String prettyPrint (String json, PrettyPrintSettings settings) {
		return new JsonReader().parse(json).prettyPrint(settings);
	}

	static private class FieldMetadata {
		Field field;
		Class elementType;

		public FieldMetadata (Field field) {
			this.field = field;
			int index = (ClassReflection.isAssignableFrom(ObjectMap.class, field.getType()) || ClassReflection.isAssignableFrom(
				Map.class, field.getType())) ? 1 : 0;
			this.elementType = field.getElementType(index);
		}
	}

	static public interface Serializer<T> {
		public void write (Json json, T object, Class knownType);

		public T read (Json json, JsonValue jsonData, Class type);
	}

	static abstract public class ReadOnlySerializer<T> implements Serializer<T> {
		public void write (Json json, T object, Class knownType) {
		}

		abstract public T read (Json json, JsonValue jsonData, Class type);
	}

	static public interface Serializable {
		public void write (Json json);

		public void read (Json json, JsonValue jsonData);
	}
}
