
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Container for a JSON object, array, string, double, long, boolean, or null.
 * <p>
 * Iteration of arrays or objects is easily done using a for loop:<br>
 * 
 * <pre>
 * JsonValue map = ...;
 * for (JsonValue entry = map.child(); entry != null; entry = entry.next())
 * 	System.out.println(entry.name() + " = " + entry.asString());
 * </pre>
 * @author Nathan Sweet */
public class JsonValue {
	private String name;
	private ValueType type;

	private String stringValue;
	private Boolean booleanValue;
	private Double doubleValue;
	private long longValue;

	private JsonValue child, next, prev;

	public JsonValue (ValueType type) {
		this.type = type;
	}

	/** @param value May be null. */
	public JsonValue (String value) {
		set(value);
	}

	public JsonValue (double value) {
		set(value);
	}

	public JsonValue (long value) {
		set(value);
	}

	public JsonValue (boolean value) {
		set(value);
	}

	/** Returns the child at the specified index.
	 * @return May be null. */
	public JsonValue get (int index) {
		JsonValue current = child;
		while (current != null && index > 0) {
			index--;
			current = current.next;
		}
		return current;
	}

	/** Returns the child with the specified name.
	 * @return May be null. */
	public JsonValue get (String name) {
		JsonValue current = child;
		while (current != null && !current.name.equalsIgnoreCase(name))
			current = current.next;
		return current;
	}

	/** Returns the child at the specified index.
	 * @throws IllegalArgumentException if the child was not found. */
	public JsonValue require (int index) {
		JsonValue current = child;
		while (current != null && index > 0) {
			index--;
			current = current.next;
		}
		if (current == null) throw new IllegalArgumentException("Child not found with index: " + index);
		return current;
	}

	/** Returns the child with the specified name.
	 * @throws IllegalArgumentException if the child was not found. */
	public JsonValue require (String name) {
		JsonValue current = child;
		while (current != null && !current.name.equalsIgnoreCase(name))
			current = current.next;
		if (current == null) throw new IllegalArgumentException("Child not found with name: " + name);
		return current;
	}

	/** Removes the child with the specified name.
	 * @return May be null. */
	public JsonValue remove (int index) {
		JsonValue child = get(index);
		if (child == null) return null;
		if (child.prev == null) {
			this.child = child.next;
			if (this.child != null) this.child.prev = null;
		} else {
			child.prev.next = child.next;
			if (child.next != null) child.next.prev = child.prev;
		}
		return child;
	}

	/** Removes the child with the specified name.
	 * @return May be null. */
	public JsonValue remove (String name) {
		JsonValue child = get(name);
		if (child == null) return null;
		if (child.prev == null) {
			this.child = child.next;
			if (this.child != null) this.child.prev = null;
		} else {
			child.prev.next = child.next;
			if (child.next != null) child.next.prev = child.prev;
		}
		return child;
	}

	/** Returns this number of children in the array or object. */
	public int size () {
		JsonValue current = child;
		int size = 0;
		while (current != null) {
			size++;
			current = current.next;
		}
		return size;
	}

	/** Returns this value as a string.
	 * @return May be null if this value is null.
	 * @throws IllegalStateException if this an array or object. */
	public String asString () {
		if (stringValue != null) return stringValue;
		if (doubleValue != null) {
			if (doubleValue == longValue) return Long.toString(longValue);
			return Double.toString(doubleValue);
		}
		if (booleanValue != null) return Boolean.toString(booleanValue);
		if (type == ValueType.nullValue) return null;
		throw new IllegalStateException("Value cannot be converted to string: " + type);
	}

	/** Returns this value as a float.
	 * @throws IllegalStateException if this an array or object. */
	public float asFloat () {
		if (doubleValue != null) return doubleValue.floatValue();
		if (stringValue != null) {
			try {
				return Float.parseFloat(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to float: " + type);
	}

	/** Returns this value as a double.
	 * @throws IllegalStateException if this an array or object. */
	public double asDouble () {
		if (doubleValue != null) return doubleValue;
		if (stringValue != null) {
			try {
				return Double.parseDouble(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to double: " + type);
	}

	/** Returns this value as a long.
	 * @throws IllegalStateException if this an array or object. */
	public long asLong () {
		if (doubleValue != null) return longValue;
		if (stringValue != null) {
			try {
				return Long.parseLong(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to long: " + type);
	}

	/** Returns this value as an int.
	 * @throws IllegalStateException if this an array or object. */
	public int asInt () {
		if (doubleValue != null) return (int)longValue;
		if (stringValue != null) {
			try {
				return Integer.parseInt(stringValue);
			} catch (NumberFormatException ignored) {
			}
		}
		if (booleanValue != null) return booleanValue ? 1 : 0;
		throw new IllegalStateException("Value cannot be converted to int: " + type);
	}

	/** Returns this value as a boolean.
	 * @throws IllegalStateException if this an array or object. */
	public boolean asBoolean () {
		if (booleanValue != null) return booleanValue;
		if (doubleValue != null) return longValue == 0;
		if (stringValue != null) return stringValue.equalsIgnoreCase("true");
		throw new IllegalStateException("Value cannot be converted to boolean: " + type);
	}

	/** Finds the child with the specified name and returns its first child.
	 * @return May be null. */
	public JsonValue getChild (String name) {
		JsonValue child = get(name);
		return child == null ? null : child.child;
	}

	/** Finds the child with the specified name and returns it as a string. Returns defaultValue if not found.
	 * @param defaultValue May be null. */
	public String getString (String name, String defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue() || child.isNull()) ? defaultValue : child.asString();
	}

	/** Finds the child with the specified name and returns it as a float. Returns defaultValue if not found. */
	public float getFloat (String name, float defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asFloat();
	}

	/** Finds the child with the specified name and returns it as a double. Returns defaultValue if not found. */
	public double getDouble (String name, double defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asDouble();
	}

	/** Finds the child with the specified name and returns it as a long. Returns defaultValue if not found. */
	public long getLong (String name, long defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asLong();
	}

	/** Finds the child with the specified name and returns it as an int. Returns defaultValue if not found. */
	public int getInt (String name, int defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asInt();
	}

	/** Finds the child with the specified name and returns it as a boolean. Returns defaultValue if not found. */
	public boolean getBoolean (String name, boolean defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asBoolean();
	}

	/** Finds the child with the specified name and returns it as a string.
	 * @throws IllegalArgumentException if the child was not found. */
	public String getString (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asString();
	}

	/** Finds the child with the specified name and returns it as a float.
	 * @throws IllegalArgumentException if the child was not found. */
	public float getFloat (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asFloat();
	}

	/** Finds the child with the specified name and returns it as a double.
	 * @throws IllegalArgumentException if the child was not found. */
	public double getDouble (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asDouble();
	}

	/** Finds the child with the specified name and returns it as a long.
	 * @throws IllegalArgumentException if the child was not found. */
	public long getLong (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asLong();
	}

	/** Finds the child with the specified name and returns it as an int.
	 * @throws IllegalArgumentException if the child was not found. */
	public int getInt (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asInt();
	}

	/** Finds the child with the specified name and returns it as a boolean.
	 * @throws IllegalArgumentException if the child was not found. */
	public boolean getBoolean (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asBoolean();
	}

	/** Finds the child with the specified index and returns it as a string.
	 * @throws IllegalArgumentException if the child was not found. */
	public String getString (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asString();
	}

	/** Finds the child with the specified index and returns it as a float.
	 * @throws IllegalArgumentException if the child was not found. */
	public float getFloat (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asFloat();
	}

	/** Finds the child with the specified index and returns it as a double.
	 * @throws IllegalArgumentException if the child was not found. */
	public double getDouble (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asDouble();
	}

	/** Finds the child with the specified index and returns it as a long.
	 * @throws IllegalArgumentException if the child was not found. */
	public long getLong (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asLong();
	}

	/** Finds the child with the specified index and returns it as an int.
	 * @throws IllegalArgumentException if the child was not found. */
	public int getInt (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asInt();
	}

	/** Finds the child with the specified index and returns it as a boolean.
	 * @throws IllegalArgumentException if the child was not found. */
	public boolean getBoolean (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asBoolean();
	}

	public ValueType type () {
		return type;
	}

	public void setType (ValueType type) {
		if (type == null) throw new IllegalArgumentException("type cannot be null.");
		this.type = type;
	}

	public boolean isArray () {
		return type == ValueType.array;
	}

	public boolean isObject () {
		return type == ValueType.object;
	}

	public boolean isString () {
		return type == ValueType.stringValue;
	}

	/** Returns true if this is a double or long value. */
	public boolean isNumber () {
		return type == ValueType.doubleValue || type == ValueType.longValue;
	}

	public boolean isDouble () {
		return type == ValueType.doubleValue;
	}

	public boolean isLong () {
		return type == ValueType.longValue;
	}

	public boolean isBoolean () {
		return type == ValueType.booleanValue;
	}

	public boolean isNull () {
		return type == ValueType.nullValue;
	}

	/** Returns true if this is not an array or object. */
	public boolean isValue () {
		switch (type) {
		case stringValue:
		case doubleValue:
		case longValue:
		case booleanValue:
		case nullValue:
			return true;
		}
		return false;
	}

	/** Returns the name for this object value.
	 * @return May be null. */
	public String name () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}

	/** Returns the first child for this object or array.
	 * @return May be null. */
	public JsonValue child () {
		return child;
	}

	public void addChild (JsonValue newChild) {
		JsonValue current = child;
		if (current == null) {
			child = newChild;
			return;
		}
		while (true) {
			if (current.next == null) {
				current.next = newChild;
				newChild.prev = current;
				return;
			}
			current = current.next;
		}
	}

	/** Returns the next sibling of this value.
	 * @return May be null. */
	public JsonValue next () {
		return next;
	}

	public void setNext (JsonValue next) {
		this.next = next;
	}

	/** Returns the previous sibling of this value.
	 * @return May be null. */
	public JsonValue prev () {
		return prev;
	}

	public void setPrev (JsonValue prev) {
		this.prev = prev;
	}

	/** @param value May be null. */
	public void set (String value) {
		stringValue = value;
		type = value == null ? ValueType.nullValue : ValueType.stringValue;
	}

	public void set (double value) {
		doubleValue = value;
		longValue = (long)value;
		type = ValueType.doubleValue;
	}

	public void set (long value) {
		longValue = value;
		doubleValue = (double)value;
		type = ValueType.longValue;
	}

	public void set (boolean value) {
		booleanValue = value;
		type = ValueType.booleanValue;
	}

	public String toString () {
		return prettyPrint(OutputType.minimal, 0);
	}

	public String prettyPrint (OutputType outputType, int singleLineColumns) {
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(this, buffer, outputType, 0, singleLineColumns);
		return buffer.toString();
	}

	private void prettyPrint (JsonValue object, StringBuilder buffer, OutputType outputType, int indent, int singleLineColumns) {
		if (object.isObject()) {
			if (object.child() == null) {
				buffer.append("{}");
			} else {
				boolean newLines = !isFlat(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "{\n" : "{ ");
					int i = 0;
					for (JsonValue child = object.child(); child != null; child = child.next()) {
						if (newLines) indent(indent, buffer);
						buffer.append(outputType.quoteName(child.name()));
						buffer.append(": ");
						prettyPrint(child, buffer, outputType, indent + 1, singleLineColumns);
						if (child.next() != null) buffer.append(",");
						buffer.append(newLines ? '\n' : ' ');
						if (!newLines && buffer.length() - start > singleLineColumns) {
							buffer.setLength(start);
							newLines = true;
							continue outer;
						}
					}
					break;
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append('}');
			}
		} else if (object.isArray()) {
			if (object.child() == null) {
				buffer.append("[]");
			} else {
				boolean newLines = !isFlat(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "[\n" : "[ ");
					for (JsonValue child = object.child(); child != null; child = child.next()) {
						if (newLines) indent(indent, buffer);
						prettyPrint(child, buffer, outputType, indent + 1, singleLineColumns);
						if (child.next() != null) buffer.append(",");
						buffer.append(newLines ? '\n' : ' ');
						if (!newLines && buffer.length() - start > singleLineColumns) {
							buffer.setLength(start);
							newLines = true;
							continue outer;
						}
					}
					break;
				}
				if (newLines) indent(indent - 1, buffer);
				buffer.append(']');
			}
		} else if (object.isString()) {
			buffer.append(outputType.quoteValue(object.asString()));
		} else if (object.isDouble()) {
			double doubleValue = object.asDouble();
			long longValue = (int)doubleValue;
			buffer.append(doubleValue - longValue == 0 ? longValue : object);
		} else if (object.isLong()) {
			buffer.append(object.asLong());
		} else if (object.isBoolean()) {
			buffer.append(object.asBoolean());
		} else if (object.isNull()) {
			buffer.append("null");
		} else
			throw new SerializationException("Unknown object type: " + object);
	}

	static private boolean isFlat (JsonValue object) {
		for (JsonValue child = object.child(); child != null; child = child.next())
			if (child.isObject() || child.isArray()) return false;
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

	public enum ValueType {
		object, array, stringValue, doubleValue, longValue, booleanValue, nullValue
	}
}
