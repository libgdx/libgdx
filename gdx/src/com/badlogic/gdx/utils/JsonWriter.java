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

	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
	}

	/** When true, quotes Long, BigDecimal and BigInteger types to prevent truncation in languages like JavaScript and PHP. */
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
		if (current != null) {
			if (current.array) {
				if (!current.needsComma)
					current.needsComma = true;
				else
					writer.write(',');
			} else {
				if (!named && !current.array) throw new IllegalStateException("Name must be set.");
				named = false;
			}
		}
		stack.add(current = new JsonObject(false));
		return this;
	}

	public JsonWriter array () throws IOException {
		if (current != null) {
			if (current.array) {
				if (!current.needsComma)
					current.needsComma = true;
				else
					writer.write(',');
			} else {
				if (!named && !current.array) throw new IllegalStateException("Name must be set.");
				named = false;
			}
		}
		stack.add(current = new JsonObject(true));
		return this;
	}

	public JsonWriter value (Object value) throws IOException {
		// quote long values; convert integer Doubles to Long
		if (quoteLongValues && (value instanceof Long || value instanceof BigDecimal || value instanceof BigInteger)) {
			value = String.valueOf(value);
		} else if (value instanceof Number) {
			Number number = (Number)value;
			long longValue = number.longValue();
			if (number.doubleValue() == longValue) value = longValue;
		}
		if (current != null) {
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
		writer.write(outputType.quoteValue(value));
		return this;
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
		/** Normal JSON, with all its quotes. */
		json,
		/** Like JSON, but names are only quoted if necessary. */
		javascript,
		/** Like JSON, but names and values are only quoted if they don't contain <code>\r\n\t</code> or <code>space</code> and don't
		 * begin with <code>/{}[]:,"</code>. Additionally, names cannot contain <code>:</code> and values cannot contain
		 * <code>}],</code>. */
		minimal;

		static private Pattern javascriptPattern = Pattern.compile("[a-zA-Z_$][a-zA-Z_$0-9]*");
		static private Pattern minimalNamePattern = Pattern.compile("[^/{}\\[\\],\":\\r\\n\\t ][^:\\r\\n\\t ]*");
		static private Pattern minimalValuePattern = Pattern.compile("[^/{}\\[\\],\":\\r\\n\\t ][^}\\],\\r\\n\\t ]*");

		public String quoteValue (Object value) {
			if (value == null || value instanceof Number || value instanceof Boolean) return String.valueOf(value);
			String string = String.valueOf(value).replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")
				.replace("\t", "\\t");
			if (this == OutputType.minimal && !string.equals("true") && !string.equals("false") && !string.equals("null")
				&& minimalValuePattern.matcher(string).matches()) return string;
			return '"' + string.replace("\"", "\\\"") + '"';
		}

		public String quoteName (String value) {
			value = value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t");
			switch (this) {
			case minimal:
				if (minimalNamePattern.matcher(value).matches()) return value;
			case javascript:
				if (javascriptPattern.matcher(value).matches()) return value;
			}
			return '"' + value.replace("\"", "\\\"") + '"';
		}
	}
}
