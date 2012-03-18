
package com.dozingcatsoftware.bouncy.util;

/*
 Copyright (c) 2002 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/** A JSONArray is an ordered sequence of values. Its external text form is a string wrapped in square brackets with commas
 * separating the values. The internal form is an object having <code>get</code> and <code>opt</code> methods for accessing the
 * values by index, and <code>put</code> methods for adding or replacing values. The values can be any of these types:
 * <code>Boolean</code>, <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>, <code>String</code>, or the
 * <code>JSONObject.NULL object</code>.
 * <p>
 * The constructor can convert a JSON text into a Java object. The <code>toString</code> method converts to JSON text.
 * <p>
 * A <code>get</code> method returns a value if one can be found, and throws an exception if one cannot be found. An
 * <code>opt</code> method returns a default value instead of throwing an exception, and so is useful for obtaining optional
 * values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an object which you can cast or query for type. There are
 * also typed <code>get</code> and <code>opt</code> methods that do type checking and type coercion for you.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to JSON syntax rules. The constructors are more
 * forgiving in the texts they will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just before the closing bracket.</li>
 * <li>The <code>null</code> value will be inserted when there is <code>,</code>&nbsp;<small>(comma)</small> elision.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote or single quote, and if they do not contain
 * leading or trailing spaces, and if they do not contain any of these characters: <code>{ } [ ] / \ : , = ; #</code> and if they
 * do not look like numbers and if they are not the reserved words <code>true</code>, <code>false</code>, or <code>null</code>.</li>
 * <li>Values can be separated by <code>;</code> <small>(semicolon)</small> as well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author JSON.org
 * @version 2010-12-28 */
public class JSONArray {

	/** The arrayList where the JSONArray's properties are kept. */
	private ArrayList myArrayList;

	/** Construct an empty JSONArray. */
	public JSONArray () {
		this.myArrayList = new ArrayList();
	}

	/** Construct a JSONArray from a JSONTokener.
	 * @param x A JSONTokener
	 * @throws JSONException If there is a syntax error. */
	public JSONArray (JSONTokener x) throws JSONException {
		this();
		if (x.nextClean() != '[') {
			throw x.syntaxError("A JSONArray text must start with '['");
		}
		if (x.nextClean() != ']') {
			x.back();
			for (;;) {
				if (x.nextClean() == ',') {
					x.back();
					this.myArrayList.add(JSONObject.NULL);
				} else {
					x.back();
					this.myArrayList.add(x.nextValue());
				}
				switch (x.nextClean()) {
				case ';':
				case ',':
					if (x.nextClean() == ']') {
						return;
					}
					x.back();
					break;
				case ']':
					return;
				default:
					throw x.syntaxError("Expected a ',' or ']'");
				}
			}
		}
	}

	/** Construct a JSONArray from a source JSON text.
	 * @param source A string that begins with <code>[</code>&nbsp;<small>(left bracket)</small> and ends with <code>]</code>
	 *           &nbsp;<small>(right bracket)</small>.
	 * @throws JSONException If there is a syntax error. */
	public JSONArray (String source) throws JSONException {
		this(new JSONTokener(source));
	}

	/** Construct a JSONArray from a Collection.
	 * @param collection A Collection. */
	public JSONArray (Collection collection) {
		this.myArrayList = new ArrayList();
		if (collection != null) {
			Iterator iter = collection.iterator();
			while (iter.hasNext()) {
				this.myArrayList.add(JSONObject.wrap(iter.next()));
			}
		}
	}

	/** Construct a JSONArray from an array
	 * @throws JSONException If not an array. */
	public JSONArray (Object array) throws JSONException {
		this();
		if (array.getClass().isArray()) {
//			int length = Array.getLength(array);
//			for (int i = 0; i < length; i += 1) {
//				this.put(JSONObject.wrap(Array.get(array, i)));
//			}
		} else {
			throw new JSONException("JSONArray initial value should be a string or collection or array.");
		}
	}

	/** Get the object value associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return An object value.
	 * @throws JSONException If there is no value for the index. */
	public Object get (int index) throws JSONException {
		Object object = opt(index);
		if (object == null) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		return object;
	}

	/** Get the boolean value associated with an index. The string values "true" and "false" are converted to boolean.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The truth.
	 * @throws JSONException If there is no value for the index or if the value is not convertible to boolean. */
	public boolean getBoolean (int index) throws JSONException {
		Object object = get(index);
		if (object.equals(Boolean.FALSE) || (object instanceof String && ((String)object).equalsIgnoreCase("false"))) {
			return false;
		} else if (object.equals(Boolean.TRUE) || (object instanceof String && ((String)object).equalsIgnoreCase("true"))) {
			return true;
		}
		throw new JSONException("JSONArray[" + index + "] is not a boolean.");
	}

	/** Get the double value associated with an index.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException If the key is not found or if the value cannot be converted to a number. */
	public double getDouble (int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/** Get the int value associated with an index.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException If the key is not found or if the value is not a number. */
	public int getInt (int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/** Get the JSONArray associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return A JSONArray value.
	 * @throws JSONException If there is no value for the index. or if the value is not a JSONArray */
	public JSONArray getJSONArray (int index) throws JSONException {
		Object object = get(index);
		if (object instanceof JSONArray) {
			return (JSONArray)object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
	}

	/** Get the JSONObject associated with an index.
	 * @param index subscript
	 * @return A JSONObject value.
	 * @throws JSONException If there is no value for the index or if the value is not a JSONObject */
	public JSONObject getJSONObject (int index) throws JSONException {
		Object object = get(index);
		if (object instanceof JSONObject) {
			return (JSONObject)object;
		}
		throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
	}

	/** Get the long value associated with an index.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value.
	 * @throws JSONException If the key is not found or if the value cannot be converted to a number. */
	public long getLong (int index) throws JSONException {
		Object object = get(index);
		try {
			return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
		} catch (Exception e) {
			throw new JSONException("JSONArray[" + index + "] is not a number.");
		}
	}

	/** Get the string associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return A string value.
	 * @throws JSONException If there is no value for the index. */
	public String getString (int index) throws JSONException {
		Object object = get(index);
		return object == JSONObject.NULL ? null : object.toString();
	}

	/** Determine if the value is null.
	 * @param index The index must be between 0 and length() - 1.
	 * @return true if the value at the index is null, or if there is no value. */
	public boolean isNull (int index) {
		return JSONObject.NULL.equals(opt(index));
	}

	/** Make a string from the contents of this JSONArray. The <code>separator</code> string is inserted between each element.
	 * Warning: This method assumes that the data structure is acyclical.
	 * @param separator A string that will be inserted between the elements.
	 * @return a string.
	 * @throws JSONException If the array contains an invalid number. */
	public String join (String separator) throws JSONException {
		int len = length();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < len; i += 1) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
		}
		return sb.toString();
	}

	/** Get the number of elements in the JSONArray, included nulls.
	 * 
	 * @return The length (or size). */
	public int length () {
		return this.myArrayList.size();
	}

	/** Get the optional object value associated with an index.
	 * @param index The index must be between 0 and length() - 1.
	 * @return An object value, or null if there is no object at that index. */
	public Object opt (int index) {
		return (index < 0 || index >= length()) ? null : this.myArrayList.get(index);
	}

	/** Get the optional boolean value associated with an index. It returns false if there is no value at that index, or if the
	 * value is not Boolean.TRUE or the String "true".
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The truth. */
	public boolean optBoolean (int index) {
		return optBoolean(index, false);
	}

	/** Get the optional boolean value associated with an index. It returns the defaultValue if there is no value at that index or
	 * if it is not a Boolean or the String "true" or "false" (case insensitive).
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue A boolean default.
	 * @return The truth. */
	public boolean optBoolean (int index, boolean defaultValue) {
		try {
			return getBoolean(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** Get the optional double value associated with an index. NaN is returned if there is no value for the index, or if the value
	 * is not a number and cannot be converted to a number.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value. */
	public double optDouble (int index) {
		return optDouble(index, Double.NaN);
	}

	/** Get the optional double value associated with an index. The defaultValue is returned if there is no value for the index, or
	 * if the value is not a number and cannot be converted to a number.
	 * 
	 * @param index subscript
	 * @param defaultValue The default value.
	 * @return The value. */
	public double optDouble (int index, double defaultValue) {
		try {
			return getDouble(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** Get the optional int value associated with an index. Zero is returned if there is no value for the index, or if the value is
	 * not a number and cannot be converted to a number.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value. */
	public int optInt (int index) {
		return optInt(index, 0);
	}

	/** Get the optional int value associated with an index. The defaultValue is returned if there is no value for the index, or if
	 * the value is not a number and cannot be converted to a number.
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue The default value.
	 * @return The value. */
	public int optInt (int index, int defaultValue) {
		try {
			return getInt(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** Get the optional JSONArray associated with an index.
	 * @param index subscript
	 * @return A JSONArray value, or null if the index has no value, or if the value is not a JSONArray. */
	public JSONArray optJSONArray (int index) {
		Object o = opt(index);
		return o instanceof JSONArray ? (JSONArray)o : null;
	}

	/** Get the optional JSONObject associated with an index. Null is returned if the key is not found, or null if the index has no
	 * value, or if the value is not a JSONObject.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return A JSONObject value. */
	public JSONObject optJSONObject (int index) {
		Object o = opt(index);
		return o instanceof JSONObject ? (JSONObject)o : null;
	}

	/** Get the optional long value associated with an index. Zero is returned if there is no value for the index, or if the value
	 * is not a number and cannot be converted to a number.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return The value. */
	public long optLong (int index) {
		return optLong(index, 0);
	}

	/** Get the optional long value associated with an index. The defaultValue is returned if there is no value for the index, or if
	 * the value is not a number and cannot be converted to a number.
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue The default value.
	 * @return The value. */
	public long optLong (int index, long defaultValue) {
		try {
			return getLong(index);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/** Get the optional string value associated with an index. It returns an empty string if there is no value at that index. If
	 * the value is not a string and is not null, then it is coverted to a string.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @return A String value. */
	public String optString (int index) {
		return optString(index, "");
	}

	/** Get the optional string associated with an index. The defaultValue is returned if the key is not found.
	 * 
	 * @param index The index must be between 0 and length() - 1.
	 * @param defaultValue The default value.
	 * @return A String value. */
	public String optString (int index, String defaultValue) {
		Object object = opt(index);
		return object != null ? object.toString() : defaultValue;
	}

	/** Append a boolean value. This increases the array's length by one.
	 * 
	 * @param value A boolean value.
	 * @return this. */
	public JSONArray put (boolean value) {
		put(value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/** Put a value in the JSONArray, where the value will be a JSONArray which is produced from a Collection.
	 * @param value A Collection value.
	 * @return this. */
	public JSONArray put (Collection value) {
		put(new JSONArray(value));
		return this;
	}

	/** Append a double value. This increases the array's length by one.
	 * 
	 * @param value A double value.
	 * @throws JSONException if the value is not finite.
	 * @return this. */
	public JSONArray put (double value) throws JSONException {
		Double d = new Double(value);
		JSONObject.testValidity(d);
		put(d);
		return this;
	}

	/** Append an int value. This increases the array's length by one.
	 * 
	 * @param value An int value.
	 * @return this. */
	public JSONArray put (int value) {
		put(new Integer(value));
		return this;
	}

	/** Append an long value. This increases the array's length by one.
	 * 
	 * @param value A long value.
	 * @return this. */
	public JSONArray put (long value) {
		put(new Long(value));
		return this;
	}

	/** Put a value in the JSONArray, where the value will be a JSONObject which is produced from a Map.
	 * @param value A Map value.
	 * @return this. */
	public JSONArray put (Map value) {
		put(new JSONObject(value));
		return this;
	}

	/** Append an object value. This increases the array's length by one.
	 * @param value An object value. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
	 *           JSONObject.NULL object.
	 * @return this. */
	public JSONArray put (Object value) {
		this.myArrayList.add(value);
		return this;
	}

	/** Put or replace a boolean value in the JSONArray. If the index is greater than the length of the JSONArray, then null
	 * elements will be added as necessary to pad it out.
	 * @param index The subscript.
	 * @param value A boolean value.
	 * @return this.
	 * @throws JSONException If the index is negative. */
	public JSONArray put (int index, boolean value) throws JSONException {
		put(index, value ? Boolean.TRUE : Boolean.FALSE);
		return this;
	}

	/** Put a value in the JSONArray, where the value will be a JSONArray which is produced from a Collection.
	 * @param index The subscript.
	 * @param value A Collection value.
	 * @return this.
	 * @throws JSONException If the index is negative or if the value is not finite. */
	public JSONArray put (int index, Collection value) throws JSONException {
		put(index, new JSONArray(value));
		return this;
	}

	/** Put or replace a double value. If the index is greater than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * @param index The subscript.
	 * @param value A double value.
	 * @return this.
	 * @throws JSONException If the index is negative or if the value is not finite. */
	public JSONArray put (int index, double value) throws JSONException {
		put(index, new Double(value));
		return this;
	}

	/** Put or replace an int value. If the index is greater than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * @param index The subscript.
	 * @param value An int value.
	 * @return this.
	 * @throws JSONException If the index is negative. */
	public JSONArray put (int index, int value) throws JSONException {
		put(index, new Integer(value));
		return this;
	}

	/** Put or replace a long value. If the index is greater than the length of the JSONArray, then null elements will be added as
	 * necessary to pad it out.
	 * @param index The subscript.
	 * @param value A long value.
	 * @return this.
	 * @throws JSONException If the index is negative. */
	public JSONArray put (int index, long value) throws JSONException {
		put(index, new Long(value));
		return this;
	}

	/** Put a value in the JSONArray, where the value will be a JSONObject which is produced from a Map.
	 * @param index The subscript.
	 * @param value The Map value.
	 * @return this.
	 * @throws JSONException If the index is negative or if the the value is an invalid number. */
	public JSONArray put (int index, Map value) throws JSONException {
		put(index, new JSONObject(value));
		return this;
	}

	/** Put or replace an object value in the JSONArray. If the index is greater than the length of the JSONArray, then null
	 * elements will be added as necessary to pad it out.
	 * @param index The subscript.
	 * @param value The value to put into the array. The value should be a Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 *           or String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the index is negative or if the the value is an invalid number. */
	public JSONArray put (int index, Object value) throws JSONException {
		JSONObject.testValidity(value);
		if (index < 0) {
			throw new JSONException("JSONArray[" + index + "] not found.");
		}
		if (index < length()) {
			this.myArrayList.set(index, value);
		} else {
			while (index != length()) {
				put(JSONObject.NULL);
			}
			put(value);
		}
		return this;
	}

	/** Remove an index and close the hole.
	 * @param index The index of the element to be removed.
	 * @return The value that was associated with the index, or null if there was no value. */
	public Object remove (int index) {
		Object o = opt(index);
		this.myArrayList.remove(index);
		return o;
	}

	/** Produce a JSONObject by combining a JSONArray of names with the values of this JSONArray.
	 * @param names A JSONArray containing a list of key strings. These will be paired with the values.
	 * @return A JSONObject, or null if there are no names or if this JSONArray has no values.
	 * @throws JSONException If any of the names are null. */
	public JSONObject toJSONObject (JSONArray names) throws JSONException {
		if (names == null || names.length() == 0 || length() == 0) {
			return null;
		}
		JSONObject jo = new JSONObject();
		for (int i = 0; i < names.length(); i += 1) {
			jo.put(names.getString(i), this.opt(i));
		}
		return jo;
	}

	/** Make a JSON text of this JSONArray. For compactness, no unnecessary whitespace is added. If it is not possible to produce a
	 * syntactically correct JSON text then null will be returned instead. This could occur if the array contains an invalid
	 * number.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, transmittable representation of the array. */
	public String toString () {
		try {
			return '[' + join(",") + ']';
		} catch (Exception e) {
			return null;
		}
	}

	/** Make a prettyprinted JSON text of this JSONArray. Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of indentation.
	 * @return a printable, displayable, transmittable representation of the object, beginning with <code>[</code>
	 *         &nbsp;<small>(left bracket)</small> and ending with <code>]</code>&nbsp;<small>(right bracket)</small>.
	 * @throws JSONException */
	public String toString (int indentFactor) throws JSONException {
		return toString(indentFactor, 0);
	}

	/** Make a prettyprinted JSON text of this JSONArray. Warning: This method assumes that the data structure is acyclical.
	 * @param indentFactor The number of spaces to add to each level of indentation.
	 * @param indent The indention of the top level.
	 * @return a printable, displayable, transmittable representation of the array.
	 * @throws JSONException */
	String toString (int indentFactor, int indent) throws JSONException {
		int len = length();
		if (len == 0) {
			return "[]";
		}
		int i;
		StringBuffer sb = new StringBuffer("[");
		if (len == 1) {
			sb.append(JSONObject.valueToString(this.myArrayList.get(0), indentFactor, indent));
		} else {
			int newindent = indent + indentFactor;
			sb.append('\n');
			for (i = 0; i < len; i += 1) {
				if (i > 0) {
					sb.append(",\n");
				}
				for (int j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JSONObject.valueToString(this.myArrayList.get(i), indentFactor, newindent));
			}
			sb.append('\n');
			for (i = 0; i < indent; i += 1) {
				sb.append(' ');
			}
		}
		sb.append(']');
		return sb.toString();
	}

	/** Write the contents of the JSONArray as JSON text to a writer. For compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 * @throws JSONException */
	public Writer write (Writer writer) throws JSONException {
		try {
			boolean b = false;
			int len = length();

			writer.write('[');

			for (int i = 0; i < len; i += 1) {
				if (b) {
					writer.write(',');
				}
				Object v = this.myArrayList.get(i);
				if (v instanceof JSONObject) {
					((JSONObject)v).write(writer);
				} else if (v instanceof JSONArray) {
					((JSONArray)v).write(writer);
				} else {
					writer.write(JSONObject.valueToString(v));
				}
				b = true;
			}
			writer.write(']');
			return writer;
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
}
