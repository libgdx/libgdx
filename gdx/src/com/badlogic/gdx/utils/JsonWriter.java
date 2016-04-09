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

/** Builder style API for emitting JSON.
 * @author Nathan Sweet */
public class JsonWriter extends Writer {
	final Writer writer;
	private final Array<JsonObject> stack = new Array();
	private JsonObject current;
	private boolean named;
	private OutputType outputType = OutputType.json;
	private boolean quoteLongValues = false;

	public JsonWriter (Writer writer) {
		this.writer = writer;
	}

	public Writer getWriter () {
		return writer;
	}

	/** Sets the type of JSON output. Default is {@link OutputType#minimal}. */
	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	/** When true, quotes long, double, BigInteger, BigDecimal types to prevent truncation in languages like JavaScript and PHP.
	 * This is not necessary when using libgdx, which handles these types without truncation. Default is false. */
	public void setQuoteLongValues (boolean quoteLongValues) {
		this.quoteLongValues = quoteLongValues;
	}

	public JsonWriter name (String name) throws IOException {
		if (current == null || current.array) throw new IllegalStateException("Current item must be an object.");
		if (!current.needsComma)
			current.needsComma = true;
		else
			writer.write(',');
		writer.write(outputType.quoteName(name));
		writer.write(':');
		named = true;
		return this;
	}

	public JsonWriter object () throws IOException {
		requireCommaOrName();
		stack.add(current = new JsonObject(false));
		return this;
	}

	public JsonWriter array () throws IOException {
		requireCommaOrName();
		stack.add(current = new JsonObject(true));
		return this;
	}

	public JsonWriter value (Object value) throws IOException {
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

	/** Writes the specified JSON value, without quoting or escaping. */
	public JsonWriter json (String json) throws IOException {
		requireCommaOrName();
		writer.write(json);
		return this;
	}

	private void requireCommaOrName () throws IOException {
		if (current == null) return;
		if (current.array) {
			if (!current.needsComma)
				current.needsComma = true;
			else
				writer.write(',');
		} else {
			if (!named) throw new IllegalStateException("Name must be set.");
			named = false;
		}
	}

	public JsonWriter object (String name) throws IOException {
		return name(name).object();
	}

	public JsonWriter array (String name) throws IOException {
		return name(name).array();
	}

	public JsonWriter set (String name, Object value) throws IOException {
		return name(name).value(value);
	}

	/** Writes the specified JSON value, without quoting or escaping. */
	public JsonWriter json (String name, String json) throws IOException {
		return name(name).json(json);
	}

	public JsonWriter pop () throws IOException {
		if (named) throw new IllegalStateException("Expected an object, array, or value since a name was set.");
		stack.pop().close();
		current = stack.size == 0 ? null : stack.peek();
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

	private class JsonObject {
		final boolean array;
		boolean needsComma;

		JsonObject (boolean array) throws IOException {
			this.array = array;
			writer.write(array ? '[' : '{');
		}

		void close () throws IOException {
			writer.write(array ? ']' : '}');
		}
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
		 * <code>false</code> , or <code>null</code>.
		 * <li>Newlines are treated as commas, making commas optional in many cases.
		 * <li>C style comments may be used: <code>//...</code> or <code>/*...*<b></b>/</code>
		 * </ul> */
		minimal;

		static private Pattern javascriptPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z_$0-9]*$");
		static private Pattern minimalNamePattern = Pattern.compile("^[^\":,}/ ][^:]*$");
		static private Pattern minimalValuePattern = Pattern.compile("^[^\":,{\\[\\]/ ][^}\\],]*$");

		public String quoteValue (Object value) {
			if (value == null) return "null";
			String string = value.toString();
			if (value instanceof Number || value instanceof Boolean) return string;
			StringBuilder buffer = new StringBuilder(string);
			buffer.replace('\\', "\\\\").replace('\r', "\\r").replace('\n', "\\n").replace('\t', "\\t");
			if (this == OutputType.minimal && !string.equals("true") && !string.equals("false") && !string.equals("null")
				&& !string.contains("//") && !string.contains("/*")) {
				int length = buffer.length();
				if (length > 0 && buffer.charAt(length - 1) != ' ' && minimalValuePattern.matcher(buffer).matches())
					return buffer.toString();
			}
			return '"' + buffer.replace('"', "\\\"").toString() + '"';
		}

		public String quoteName (String value) {
			StringBuilder buffer = new StringBuilder(value);
			buffer.replace('\\', "\\\\").replace('\r', "\\r").replace('\n', "\\n").replace('\t', "\\t");
			switch (this) {
			case minimal:
				if (!value.contains("//") && !value.contains("/*") && minimalNamePattern.matcher(buffer).matches())
					return buffer.toString();
			case javascript:
				if (javascriptPattern.matcher(buffer).matches()) return buffer.toString();
			}
			return '"' + buffer.replace('"', "\\\"").toString() + '"';
		}
	}
}
