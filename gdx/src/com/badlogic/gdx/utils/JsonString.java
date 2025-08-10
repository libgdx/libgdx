/*******************************************************************************
 * Copyright 2024 See AUTHORS file.
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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.badlogic.gdx.utils.JsonWriter.OutputType;

/** Builder API for emitting JSON to a string.
 * @author Nathan Sweet */
public class JsonString {
	static private final int none = 0, needsComma = 1, object = '}' << 1, array = ']' << 1, isObject = 0b1000000;

	final StringBuilder buffer;
	private final IntArray stack = new IntArray();
	private int current;
	private boolean named;
	private OutputType outputType = OutputType.json;
	private boolean quoteLongValues;

	public JsonString () {
		this(64);
	}

	public JsonString (int initialBufferSize) {
		buffer = new StringBuilder(initialBufferSize);
	}

	public StringBuilder getBuffer () {
		return buffer;
	}

	/** Sets the type of JSON output. Default is {@link OutputType#minimal}. */
	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	/** When true, long, double, BigInteger, BigDecimal types are output as strings to prevent truncation in languages like
	 * JavaScript and PHP. This is not necessary when using libgdx, which handles these types without truncation. Default is
	 * false. */
	public void setQuoteLongValues (boolean quoteLongValues) {
		this.quoteLongValues = quoteLongValues;
	}

	public JsonString object () {
		requireCommaOrName();
		buffer.append('{');
		stack.add(current);
		current = object;
		return this;
	}

	public JsonString array () {
		requireCommaOrName();
		buffer.append('[');
		stack.add(current);
		current = array;
		return this;
	}

	/** Prefer calling the more specific value() methods. */
	public JsonString value (@Null Object value) {
		if (quoteLongValues
			&& (value instanceof Long || value instanceof Double || value instanceof BigDecimal || value instanceof BigInteger)) {
			value = value.toString();
		} else if (value instanceof Number) {
			Number number = (Number)value;
			long longValue = number.longValue();
			if (number.doubleValue() == longValue) value = longValue;
		}
		requireCommaOrName();
		buffer.append(outputType.quoteValue(value));
		return this;
	}

	public JsonString value (String value) {
		requireCommaOrName();
		buffer.append(outputType.quoteValue(value));
		return this;
	}

	public JsonString value (boolean value) {
		requireCommaOrName();
		buffer.append(value);
		return this;
	}

	public JsonString value (int value) {
		requireCommaOrName();
		buffer.append(value);
		return this;
	}

	public JsonString value (long value) {
		if (quoteLongValues)
			value(Long.toString(value));
		else {
			requireCommaOrName();
			buffer.append(value);
		}
		return this;
	}

	public JsonString value (float value) {
		requireCommaOrName();
		buffer.append(value);
		return this;
	}

	public JsonString value (double value) {
		if (quoteLongValues)
			value(Double.toString(value));
		else {
			requireCommaOrName();
			buffer.append(value);
		}
		return this;
	}

	/** Writes the specified JSON string, without quoting or escaping. */
	public JsonString json (String json) {
		requireCommaOrName();
		buffer.append(json);
		return this;
	}

	private void requireCommaOrName () {
		if ((current & isObject) != 0) {
			if (!named) throw new IllegalStateException("Name must be set.");
			named = false;
		} else {
			if ((current & needsComma) != 0)
				buffer.append(',');
			else if (current != none) //
				current |= needsComma;
		}
	}

	public JsonString name (String name) {
		nameValue(name);
		named = true;
		return this;
	}

	private void nameValue (String name) {
		if ((current & isObject) == 0) throw new IllegalStateException("Current item must be an object.");
		if ((current & needsComma) != 0)
			buffer.append(',');
		else
			current |= needsComma;
		buffer.append(outputType.quoteName(name));
		buffer.append(':');
	}

	public JsonString object (String name) {
		nameValue(name);
		buffer.append('{');
		stack.add(current);
		current = object;
		return this;
	}

	public JsonString array (String name) {
		nameValue(name);
		buffer.append('[');
		stack.add(current);
		current = array;
		return this;
	}

	/** Prefer calling the more specific set() methods. */
	public JsonString set (String name, Object value) {
		name(name);
		value(value);
		return this;
	}

	public JsonString set (String name, String value) {
		nameValue(name);
		buffer.append(outputType.quoteValue(value));
		return this;
	}

	public JsonString set (String name, boolean value) {
		nameValue(name);
		buffer.append(value);
		return this;
	}

	public JsonString set (String name, int value) {
		nameValue(name);
		buffer.append(value);
		return this;
	}

	public JsonString set (String name, long value) {
		if (quoteLongValues)
			set(name, Long.toString(value));
		else {
			nameValue(name);
			buffer.append(value);
		}
		return this;
	}

	public JsonString set (String name, float value) {
		nameValue(name);
		buffer.append(value);
		return this;
	}

	public JsonString set (String name, double value) {
		if (quoteLongValues)
			set(name, Double.toString(value));
		else {
			nameValue(name);
			buffer.append(value);
		}
		return this;
	}

	/** Writes the specified JSON string, without quoting or escaping. */
	public JsonString json (String name, String json) {
		nameValue(name);
		buffer.append(json);
		return this;
	}

	public JsonString pop () {
		if (named) throw new IllegalStateException("Expected an object, array, or value since a name was set.");
		buffer.append((char)(current >> 1));
		current = stack.size == 0 ? none : stack.items[--stack.size];
		return this;
	}

	public JsonString close () {
		while (stack.size > 0)
			pop();
		return this;
	}

	public void reset () {
		buffer.setLength(0);
		stack.size = 0;
		current = none;
		named = false;
	}

	public String toString () {
		return buffer.toString();
	}
}
