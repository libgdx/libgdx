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

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Container for a JSON object, array, string, double, long, boolean, or null.
 * <p>
 * JsonValue children are a linked list. Iteration of arrays or objects is easily done using a for loop, either with the enhanced
 * for loop syntactic sugar or like the example below. This is much more efficient than accessing children by index when there are
 * many children.<br>
 * 
 * <pre>
 * JsonValue map = ...;
 * for (JsonValue entry = map.child; entry != null; entry = entry.next)
 * 	System.out.println(entry.name + " = " + entry.asString());
 * </pre>
 * 
 * @author Nathan Sweet */
public class JsonValue implements Iterable<JsonValue> {
	private ValueType type;

	/** May be null. */
	private String stringValue;
	private double doubleValue;
	private long longValue;

	public String name;
	/** May be null. */
	public JsonValue child, next, prev, parent;
	public int size;

	public JsonValue (ValueType type) {
		this.type = type;
	}

	/** @param value May be null. */
	public JsonValue (String value) {
		set(value);
	}

	public JsonValue (double value) {
		set(value, null);
	}

	public JsonValue (long value) {
		set(value, null);
	}

	public JsonValue (double value, String stringValue) {
		set(value, stringValue);
	}

	public JsonValue (long value, String stringValue) {
		set(value, stringValue);
	}

	public JsonValue (boolean value) {
		set(value);
	}

	/** Returns the child at the specified index. This requires walking the linked list to the specified entry, see
	 * {@link JsonValue} for how to iterate efficiently.
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

	/** Returns true if a child with the specified name exists. */
	public boolean has (String name) {
		return get(name) != null;
	}

	/** Returns the child at the specified index. This requires walking the linked list to the specified entry, see
	 * {@link JsonValue} for how to iterate efficiently.
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

	/** Removes the child with the specified index. This requires walking the linked list to the specified entry, see
	 * {@link JsonValue} for how to iterate efficiently.
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
	@Deprecated
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
			return stringValue != null ? stringValue : Double.toString(doubleValue);
		case longValue:
			return stringValue != null ? stringValue : Long.toString(longValue);
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
			return doubleValue != 0;
		case longValue:
			return longValue != 0;
		case booleanValue:
			return longValue != 0;
		}
		throw new IllegalStateException("Value cannot be converted to boolean: " + type);
	}

	/** Returns this value as a byte.
	 * @throws IllegalStateException if this an array or object. */
	public byte asByte () {
		switch (type) {
		case stringValue:
			return Byte.parseByte(stringValue);
		case doubleValue:
			return (byte)doubleValue;
		case longValue:
			return (byte)longValue;
		case booleanValue:
			return longValue != 0 ? (byte)1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to byte: " + type);
	}

	/** Returns this value as a short.
	 * @throws IllegalStateException if this an array or object. */
	public short asShort () {
		switch (type) {
		case stringValue:
			return Short.parseShort(stringValue);
		case doubleValue:
			return (short)doubleValue;
		case longValue:
			return (short)longValue;
		case booleanValue:
			return longValue != 0 ? (short)1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to short: " + type);
	}

	/** Returns this value as a char.
	 * @throws IllegalStateException if this an array or object. */
	public char asChar () {
		switch (type) {
		case stringValue:
			return stringValue.length() == 0 ? 0 : stringValue.charAt(0);
		case doubleValue:
			return (char)doubleValue;
		case longValue:
			return (char)longValue;
		case booleanValue:
			return longValue != 0 ? (char)1 : 0;
		}
		throw new IllegalStateException("Value cannot be converted to char: " + type);
	}

	/** Returns the children of this value as a newly allocated String array.
	 * @throws IllegalStateException if this is not an array. */
	public String[] asStringArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		String[] array = new String[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			String v;
			switch (value.type) {
			case stringValue:
				v = value.stringValue;
				break;
			case doubleValue:
				v = stringValue != null ? stringValue : Double.toString(value.doubleValue);
				break;
			case longValue:
				v = stringValue != null ? stringValue : Long.toString(value.longValue);
				break;
			case booleanValue:
				v = value.longValue != 0 ? "true" : "false";
				break;
			case nullValue:
				v = null;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to string: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated float array.
	 * @throws IllegalStateException if this is not an array. */
	public float[] asFloatArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		float[] array = new float[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			float v;
			switch (value.type) {
			case stringValue:
				v = Float.parseFloat(value.stringValue);
				break;
			case doubleValue:
				v = (float)value.doubleValue;
				break;
			case longValue:
				v = (float)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? 1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to float: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated double array.
	 * @throws IllegalStateException if this is not an array. */
	public double[] asDoubleArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		double[] array = new double[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			double v;
			switch (value.type) {
			case stringValue:
				v = Double.parseDouble(value.stringValue);
				break;
			case doubleValue:
				v = value.doubleValue;
				break;
			case longValue:
				v = (double)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? 1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to double: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated long array.
	 * @throws IllegalStateException if this is not an array. */
	public long[] asLongArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		long[] array = new long[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			long v;
			switch (value.type) {
			case stringValue:
				v = Long.parseLong(value.stringValue);
				break;
			case doubleValue:
				v = (long)value.doubleValue;
				break;
			case longValue:
				v = value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? 1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to long: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated int array.
	 * @throws IllegalStateException if this is not an array. */
	public int[] asIntArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		int[] array = new int[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			int v;
			switch (value.type) {
			case stringValue:
				v = Integer.parseInt(value.stringValue);
				break;
			case doubleValue:
				v = (int)value.doubleValue;
				break;
			case longValue:
				v = (int)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? 1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to int: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated boolean array.
	 * @throws IllegalStateException if this is not an array. */
	public boolean[] asBooleanArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		boolean[] array = new boolean[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			boolean v;
			switch (value.type) {
			case stringValue:
				v = Boolean.parseBoolean(value.stringValue);
				break;
			case doubleValue:
				v = value.doubleValue == 0;
				break;
			case longValue:
				v = value.longValue == 0;
				break;
			case booleanValue:
				v = value.longValue != 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to boolean: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated byte array.
	 * @throws IllegalStateException if this is not an array. */
	public byte[] asByteArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		byte[] array = new byte[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			byte v;
			switch (value.type) {
			case stringValue:
				v = Byte.parseByte(value.stringValue);
				break;
			case doubleValue:
				v = (byte)value.doubleValue;
				break;
			case longValue:
				v = (byte)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? (byte)1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to byte: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated short array.
	 * @throws IllegalStateException if this is not an array. */
	public short[] asShortArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		short[] array = new short[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			short v;
			switch (value.type) {
			case stringValue:
				v = Short.parseShort(value.stringValue);
				break;
			case doubleValue:
				v = (short)value.doubleValue;
				break;
			case longValue:
				v = (short)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? (short)1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to short: " + value.type);
			}
			array[i] = v;
		}
		return array;
	}

	/** Returns the children of this value as a newly allocated char array.
	 * @throws IllegalStateException if this is not an array. */
	public char[] asCharArray () {
		if (type != ValueType.array) throw new IllegalStateException("Value is not an array: " + type);
		char[] array = new char[size];
		int i = 0;
		for (JsonValue value = child; value != null; value = value.next, i++) {
			char v;
			switch (value.type) {
			case stringValue:
				v = value.stringValue.length() == 0 ? 0 : value.stringValue.charAt(0);
				break;
			case doubleValue:
				v = (char)value.doubleValue;
				break;
			case longValue:
				v = (char)value.longValue;
				break;
			case booleanValue:
				v = value.longValue != 0 ? (char)1 : 0;
				break;
			default:
				throw new IllegalStateException("Value cannot be converted to char: " + value.type);
			}
			array[i] = v;
		}
		return array;
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

	/** Finds the child with the specified name and returns it as a byte. Returns defaultValue if not found. */
	public byte getByte (String name, byte defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asByte();
	}

	/** Finds the child with the specified name and returns it as a short. Returns defaultValue if not found. */
	public short getShort (String name, short defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asShort();
	}

	/** Finds the child with the specified name and returns it as a char. Returns defaultValue if not found. */
	public char getChar (String name, char defaultValue) {
		JsonValue child = get(name);
		return (child == null || !child.isValue()) ? defaultValue : child.asChar();
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

	/** Finds the child with the specified name and returns it as a byte.
	 * @throws IllegalArgumentException if the child was not found. */
	public byte getByte (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asByte();
	}

	/** Finds the child with the specified name and returns it as a short.
	 * @throws IllegalArgumentException if the child was not found. */
	public short getShort (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asShort();
	}

	/** Finds the child with the specified name and returns it as a char.
	 * @throws IllegalArgumentException if the child was not found. */
	public char getChar (String name) {
		JsonValue child = get(name);
		if (child == null) throw new IllegalArgumentException("Named value not found: " + name);
		return child.asChar();
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

	/** Finds the child with the specified index and returns it as a byte.
	 * @throws IllegalArgumentException if the child was not found. */
	public byte getByte (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asByte();
	}

	/** Finds the child with the specified index and returns it as a short.
	 * @throws IllegalArgumentException if the child was not found. */
	public short getShort (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asShort();
	}

	/** Finds the child with the specified index and returns it as a char.
	 * @throws IllegalArgumentException if the child was not found. */
	public char getChar (int index) {
		JsonValue child = get(index);
		if (child == null) throw new IllegalArgumentException("Indexed value not found: " + name);
		return child.asChar();
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

	/** Returns the parent for this value.
	 * @return May be null. */
	public JsonValue parent () {
		return parent;
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

	/** @param stringValue May be null if the string representation is the string value of the double (eg, no leading zeros). */
	public void set (double value, String stringValue) {
		doubleValue = value;
		longValue = (long)value;
		this.stringValue = stringValue;
		type = ValueType.doubleValue;
	}

	/** @param stringValue May be null if the string representation is the string value of the long (eg, no leading zeros). */
	public void set (long value, String stringValue) {
		longValue = value;
		doubleValue = (double)value;
		this.stringValue = stringValue;
		type = ValueType.longValue;
	}

	public void set (boolean value) {
		longValue = value ? 1 : 0;
		type = ValueType.booleanValue;
	}

	public String toJson (OutputType outputType) {
		if (isValue()) return asString();
		StringBuilder buffer = new StringBuilder(512);
		json(this, buffer, outputType);
		return buffer.toString();
	}

	private void json (JsonValue object, StringBuilder buffer, OutputType outputType) {
		if (object.isObject()) {
			if (object.child == null)
				buffer.append("{}");
			else {
				int start = buffer.length();
				while (true) {
					buffer.append('{');
					int i = 0;
					for (JsonValue child = object.child; child != null; child = child.next) {
						buffer.append(outputType.quoteName(child.name));
						buffer.append(':');
						json(child, buffer, outputType);
						if (child.next != null) buffer.append(',');
					}
					break;
				}
				buffer.append('}');
			}
		} else if (object.isArray()) {
			if (object.child == null)
				buffer.append("[]");
			else {
				int start = buffer.length();
				while (true) {
					buffer.append('[');
					for (JsonValue child = object.child; child != null; child = child.next) {
						json(child, buffer, outputType);
						if (child.next != null) buffer.append(',');
					}
					break;
				}
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

	public String toString () {
		if (isValue()) return name == null ? asString() : name + ": " + asString();
		return (name == null ? "" : name + ": ") + prettyPrint(OutputType.minimal, 0);
	}

	public String prettyPrint (OutputType outputType, int singleLineColumns) {
		PrettyPrintSettings settings = new PrettyPrintSettings();
		settings.outputType = outputType;
		settings.singleLineColumns = singleLineColumns;
		return prettyPrint(settings);
	}

	public String prettyPrint (PrettyPrintSettings settings) {
		StringBuilder buffer = new StringBuilder(512);
		prettyPrint(this, buffer, 0, settings);
		return buffer.toString();
	}

	private void prettyPrint (JsonValue object, StringBuilder buffer, int indent, PrettyPrintSettings settings) {
		OutputType outputType = settings.outputType;
		if (object.isObject()) {
			if (object.child == null)
				buffer.append("{}");
			else {
				boolean newLines = !isFlat(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "{\n" : "{ ");
					int i = 0;
					for (JsonValue child = object.child; child != null; child = child.next) {
						if (newLines) indent(indent, buffer);
						buffer.append(outputType.quoteName(child.name));
						buffer.append(": ");
						prettyPrint(child, buffer, indent + 1, settings);
						if ((!newLines || outputType != OutputType.minimal) && child.next != null) buffer.append(',');
						buffer.append(newLines ? '\n' : ' ');
						if (!newLines && buffer.length() - start > settings.singleLineColumns) {
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
			if (object.child == null)
				buffer.append("[]");
			else {
				boolean newLines = !isFlat(object);
				boolean wrap = settings.wrapNumericArrays || !isNumeric(object);
				int start = buffer.length();
				outer:
				while (true) {
					buffer.append(newLines ? "[\n" : "[ ");
					for (JsonValue child = object.child; child != null; child = child.next) {
						if (newLines) indent(indent, buffer);
						prettyPrint(child, buffer, indent + 1, settings);
						if ((!newLines || outputType != OutputType.minimal) && child.next != null) buffer.append(',');
						buffer.append(newLines ? '\n' : ' ');
						if (wrap && !newLines && buffer.length() - start > settings.singleLineColumns) {
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
		for (JsonValue child = object.child; child != null; child = child.next)
			if (child.isObject() || child.isArray()) return false;
		return true;
	}

	static private boolean isNumeric (JsonValue object) {
		for (JsonValue child = object.child; child != null; child = child.next)
			if (!child.isNumber()) return false;
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

	/** Returns a human readable string representing the path from the root of the JSON object graph to this value. */
	public String trace () {
		if (parent == null) {
			if (type == ValueType.array) return "[]";
			if (type == ValueType.object) return "{}";
			return "";
		}
		String trace;
		if (parent.type == ValueType.array) {
			trace = "[]";
			int i = 0;
			for (JsonValue child = parent.child; child != null; child = child.next, i++) {
				if (child == this) {
					trace = "[" + i + "]";
					break;
				}
			}
		} else if (name.indexOf('.') != -1)
			trace = ".\"" + name.replace("\"", "\\\"") + "\"";
		else
			trace = '.' + name;
		return parent.trace() + trace;
	}

	static public class PrettyPrintSettings {
		public OutputType outputType;

		/** If an object on a single line fits this many columns, it won't wrap. */
		public int singleLineColumns;

		/** Arrays of floats won't wrap. */
		public boolean wrapNumericArrays;
	}
}
