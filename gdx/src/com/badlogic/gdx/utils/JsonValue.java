
package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.JsonWriter.OutputType;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Container for a JSON object, array, string, double, long, boolean, or null.
 * <p>
 * JsonValue children are a linked list. Iteration of arrays or objects is easily done using a for loop, either with the enhanced
 * for loop syntactic sugar or like the example below. This is much more efficient than accessing children by index when there are
 * many children.<br>
 * 
 * <pre>
 * JsonValue map = ...;
 * for (JsonValue entry = map.child(); entry != null; entry = entry.next())
 * 	System.out.println(entry.name() + " = " + entry.asString());
 * </pre>
 * @author Nathan Sweet */
public class JsonValue implements Iterable<JsonValue> {
	private ValueType type;

	private String stringValue;
	private double doubleValue;
	private long longValue;

	public String name;
	/** May be null. */
	public JsonValue child, next, prev;
	public int size;

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
		size--;
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
		size--;
		return child;
	}

	/** @deprecated Use the size property instead. Returns this number of children in the array or object. */
	public int size () {
		return size;
	}

	/** Returns this value as a string.
	 * @return May be null if this value is null.
	 * @throws IllegalStateException if this an array or object. */
	public String asString () {
		switch (type) {
		case stringValue:
			return stringValue;
		case doubleValue:
			return Double.toString(doubleValue);
		case longValue:
			return Long.toString(longValue);
		case booleanValue:
			return longValue != 0 ? "true" : "false";
		case nullValue:
			return null;
		}
		throw new IllegalStateException("Value cannot be converted to string: " + type);
	}

	/** Returns this value as a float.
	 * @throws IllegalStateException if this an array or object. */
	public float asFloat () {
		switch (type) {
		case stringValue:
			return Float.parseFloat(stringValue);
		case doubleValue:
			return (float)doubleValue;
		case longValue:
			return (float)longValue;
		case booleanValue:
			return longValue != 0 ? 1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to float: " + type);
	}

	/** Returns this value as a double.
	 * @throws IllegalStateException if this an array or object. */
	public double asDouble () {
		switch (type) {
		case stringValue:
			return Double.parseDouble(stringValue);
		case doubleValue:
			return doubleValue;
		case longValue:
			return (double)longValue;
		case booleanValue:
			return longValue != 0 ? 1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to double: " + type);
	}

	/** Returns this value as a long.
	 * @throws IllegalStateException if this an array or object. */
	public long asLong () {
		switch (type) {
		case stringValue:
			return Long.parseLong(stringValue);
		case doubleValue:
			return (long)doubleValue;
		case longValue:
			return longValue;
		case booleanValue:
			return longValue != 0 ? 1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to long: " + type);
	}

	/** Returns this value as an int.
	 * @throws IllegalStateException if this an array or object. */
	public int asInt () {
		switch (type) {
		case stringValue:
			return Integer.parseInt(stringValue);
		case doubleValue:
			return (int)doubleValue;
		case longValue:
			return (int)longValue;
		case booleanValue:
			return longValue != 0 ? 1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to int: " + type);
	}

	/** Returns this value as a boolean.
	 * @throws IllegalStateException if this an array or object. */
	public boolean asBoolean () {
		switch (type) {
		case stringValue:
			return stringValue.equalsIgnoreCase("true");
		case doubleValue:
			return doubleValue == 0;
		case longValue:
			return longValue == 0;
		case booleanValue:
			return longValue != 0;
		}
		throw new IllegalStateException("Value cannot be converted to boolean: " + type);
	}

	/** Returns true if a child with the specified name exists and has a child. */
	public boolean hasChild (String name) {
		return getChild(name) != null;
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
		longValue = value ? 1 : 0;
		type = ValueType.booleanValue;
	}

	public String toString () {
		if (isValue())
			return name == null ? asString() : name + ": " + asString();
		else
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
			long longValue = object.asLong();
			buffer.append(doubleValue == longValue ? longValue : doubleValue);
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

	static private void indent (int count, StringBuilder buffer) {
		for (int i = 0; i < count; i++)
			buffer.append('\t');
	}

	public enum ValueType {
		object, array, stringValue, doubleValue, longValue, booleanValue, nullValue
	}

	public JsonIterator iterator () {
		return new JsonIterator();
	}

	public class JsonIterator implements Iterator<JsonValue>, Iterable<JsonValue> {
		JsonValue entry = child;
		JsonValue current;

		public boolean hasNext () {
			return entry != null;
		}

		public JsonValue next () {
			current = entry;
			if (current == null) throw new NoSuchElementException();
			entry = current.next;
			return current;
		}

		public void remove () {
			if (current.prev == null) {
				child = current.next;
				if (child != null) child.prev = null;
			} else {
				current.prev.next = current.next;
				if (current.next != null) current.next.prev = current.prev;
			}
			size--;
		}

		public Iterator<JsonValue> iterator () {
			return this;
		}
	}
}
