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
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

/** Builder API for emitting JSON to a {@link Writer}.
 * @author Nathan Sweet */
public class JsonWriter extends Writer {
	static private final int none = 0, needsComma = 1, object = '}' << 1, array = ']' << 1, isObject = 0b1000000;

	Writer writer;
	private final IntArray stack = new IntArray();
	private int current;
	private boolean named;
	private OutputType outputType = OutputType.json;
	private boolean quoteLongValues;

	public JsonWriter (Writer writer) {
		this.writer = writer;
	}

	/** A writer must be set before use. */
	protected JsonWriter () {
	}

	public void setWriter (Writer writer) {
		this.writer = writer;
	}

	public Writer getWriter () {
		return writer;
	}

	/** Sets the type of JSON output. Default is {@link OutputType#json}. */
	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	/** When true, long, double, BigInteger, BigDecimal types are output as strings to prevent truncation in languages like
	 * JavaScript and PHP. This is not necessary when using libgdx, which handles these types without truncation. Default is
	 * false. */
	public void setQuoteLongValues (boolean quoteLongValues) {
		this.quoteLongValues = quoteLongValues;
	}

	public JsonWriter object () throws IOException {
		requireCommaOrName();
		writer.write('{');
		stack.add(current);
		current = object;
		return this;
	}

	public JsonWriter array () throws IOException {
		requireCommaOrName();
		writer.write('[');
		stack.add(current);
		current = array;
		return this;
	}

	/** Prefer calling the more specific value() methods. */
	public JsonWriter value (@Null Object value) throws IOException {
		if (quoteLongValues
			&& (value instanceof Long || value instanceof Double || value instanceof BigDecimal || value instanceof BigInteger)) {
			value = value.toString();
		} else if (value instanceof Number) {
			Number number = (Number)value;
			long longValue = number.longValue();
			if (number.doubleValue() == longValue) value = longValue;
		}
		requireCommaOrName();
		writer.write(outputType.quoteValue(value));
		return this;
	}

	public JsonWriter value (String value) throws IOException {
		requireCommaOrName();
		writer.write(outputType.quoteValue(value));
		return this;
	}

	public JsonWriter value (boolean value) throws IOException {
		requireCommaOrName();
		writer.write(value ? "true" : "false");
		return this;
	}

	public JsonWriter value (int value) throws IOException {
		requireCommaOrName();
		writer.write(Integer.toString(value));
		return this;
	}

	public JsonWriter value (long value) throws IOException {
		if (quoteLongValues)
			value(Long.toString(value));
		else {
			requireCommaOrName();
			writer.write(Long.toString(value));
		}
		return this;
	}

	public JsonWriter value (float value) throws IOException {
		requireCommaOrName();
		writer.write(Float.toString(value));
		return this;
	}

	public JsonWriter value (double value) throws IOException {
		if (quoteLongValues)
			value(Double.toString(value));
		else {
			requireCommaOrName();
			writer.write(Double.toString(value));
		}
		return this;
	}

	/** Writes the specified JSON string, without quoting or escaping. */
	public JsonWriter json (String json) throws IOException {
		requireCommaOrName();
		writer.write(json);
		return this;
	}

	private void requireCommaOrName () throws IOException {
		if ((current & isObject) != 0) {
			if (!named) throw new IllegalStateException("Name must be set.");
			named = false;
		} else {
			if ((current & needsComma) != 0)
				writer.write(',');
			else if (current != none) //
				current |= needsComma;
		}
	}

	public JsonWriter name (String name) throws IOException {
		nameValue(name);
		named = true;
		return this;
	}

	private void nameValue (String name) throws IOException {
		if ((current & isObject) == 0) throw new IllegalStateException("Current item must be an object.");
		if ((current & needsComma) != 0)
			writer.write(',');
		else
			current |= needsComma;
		writer.write(outputType.quoteName(name));
		writer.write(':');
	}

	public JsonWriter object (String name) throws IOException {
		nameValue(name);
		writer.write('{');
		stack.add(current);
		current = object;
		return this;
	}

	public JsonWriter array (String name) throws IOException {
		nameValue(name);
		writer.write('[');
		stack.add(current);
		current = array;
		return this;
	}

	/** Prefer calling the more specific set() methods. */
	public JsonWriter set (String name, Object value) throws IOException {
		name(name);
		value(value);
		return this;
	}

	public JsonWriter set (String name, String value) throws IOException {
		nameValue(name);
		writer.write(outputType.quoteValue(value));
		return this;
	}

	public JsonWriter set (String name, boolean value) throws IOException {
		nameValue(name);
		writer.write(value ? "true" : "false");
		return this;
	}

	public JsonWriter set (String name, int value) throws IOException {
		nameValue(name);
		writer.write(Integer.toString(value));
		return this;
	}

	public JsonWriter set (String name, long value) throws IOException {
		if (quoteLongValues)
			set(name, Long.toString(value));
		else {
			nameValue(name);
			writer.write(Long.toString(value));
		}
		return this;
	}

	public JsonWriter set (String name, float value) throws IOException {
		nameValue(name);
		writer.write(Float.toString(value));
		return this;
	}

	public JsonWriter set (String name, double value) throws IOException {
		if (quoteLongValues)
			set(name, Double.toString(value));
		else {
			nameValue(name);
			writer.write(Double.toString(value));
		}
		return this;
	}

	/** Writes the specified JSON string, without quoting or escaping. */
	public JsonWriter json (String name, String json) throws IOException {
		nameValue(name);
		writer.write(json);
		return this;
	}

	public JsonWriter pop () throws IOException {
		if (named) throw new IllegalStateException("Expected an object, array, or value since a name was set.");
		writer.write((char)(current >> 1));
		current = stack.size == 0 ? none : stack.items[--stack.size];
		return this;
	}

	public void write (char[] cbuf, int off, int len) throws IOException {
		writer.write(cbuf, off, len);
	}

	public void flush () throws IOException {
		writer.flush();
	}

	public void close () throws IOException {
		while (stack.size > 0)
			pop();
		writer.close();
	}

	static public enum OutputType {
		/** Normal JSON, with all its double quotes. */
		json,
		/** Like JSON, but names are only double quoted if necessary. */
		javascript,
		/** Like JSON, but:
		 * <ul>
		 * <li>Names only require double quotes if they start with <code>space</code> or any of <code>":,}/</code> or they contain
		 * <code>//</code> or <code>/*</code> or <code>:</code>.
		 * <li>Values only require double quotes if they start with <code>space</code> or any of <code>":,{[]/</code> or they
		 * contain <code>//</code> or <code>/*</code> or any of <code>}],</code> or they are equal to <code>true</code>,
		 * <code>false</code>, or <code>null</code>.
		 * <li>Newlines are treated as commas, making commas optional in many cases.
		 * <li>C style comments may be used: <code>//...</code> or <code>/*...*<b></b>/</code>
		 * </ul>
		 */
		minimal;

		static private Pattern javascriptPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$");
		static private Pattern minimalNamePattern = Pattern.compile("^[^\":,}/ ][^:]*$");
		static private Pattern minimalValuePattern = Pattern.compile("^[^\":,{\\[\\]/ ][^}\\],]*$");

		public String quoteValue (@Null Object value) {
			if (value == null) return "null";
			String string = value.toString();
			if (value instanceof Number || value instanceof Boolean) return string;

			boolean quote = false;
			outer:
			for (int i = 0; i < string.length(); i++) {
				switch (string.charAt(i)) {
				case '\\':
				case '\r':
				case '\n':
				case '\t':
					string = escape(string, i);
					quote = true;
					break outer;
				case '"':
					quote = true;
				}
			}

			if (this == OutputType.minimal && !string.equals("true") && !string.equals("false") && !string.equals("null")
				&& !string.contains("//") && !string.contains("/*")) {
				int length = string.length();
				if (length > 0 && string.charAt(length - 1) != ' ' && minimalValuePattern.matcher(string).matches()) return string;
			}
			return quote ? escapeQuote(string) : '"' + string + '"';
		}

		public String quoteName (String value) {
			boolean quote = false;
			outer:
			for (int i = 0; i < value.length(); i++) {
				switch (value.charAt(i)) {
				case '\\':
				case '\r':
				case '\n':
				case '\t':
					value = escape(value, i);
					quote = true;
					break outer;
				case '"':
					quote = true;
				}
			}

			switch (this) {
			case minimal:
				if (!value.contains("//") && !value.contains("/*") && minimalNamePattern.matcher(value).matches()) return value;
			case javascript:
				if (javascriptPattern.matcher(value).matches()) return value;
			}
			return quote ? escapeQuote(value) : '"' + value + '"';
		}

		static private String escape (String value, int i) {
			StringBuilder buffer = new StringBuilder(value.length() + 6);
			buffer.append(value, 0, i);
			for (; i < value.length(); i++) {
				char c = value.charAt(i);
				switch (c) {
				case '\\':
					buffer.append("\\\\");
					break;
				case '\r':
					buffer.append("\\r");
					break;
				case '\n':
					buffer.append("\\n");
					break;
				case '\t':
					buffer.append("\\t");
					break;
				default:
					buffer.append(c);
				}
			}
			return buffer.toString();
		}

		static private String escapeQuote (String value) {
			StringBuilder buffer = new StringBuilder(value.length() + 6);
			buffer.append('"');
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '"')
					buffer.append("\\\"");
				else
					buffer.append(c);
			}
			buffer.append('"');
			return buffer.toString();
		}
	}
}
