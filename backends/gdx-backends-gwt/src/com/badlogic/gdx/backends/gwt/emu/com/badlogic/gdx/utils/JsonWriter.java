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

/** Builder style API for emitting JSON.
 * @author Nathan Sweet */
public class JsonWriter extends Writer {
	final Writer writer;
	private final Array<JsonObject> stack = new Array();
	private JsonObject current;
	private boolean named;
	private OutputType outputType = OutputType.json;

	public JsonWriter (Writer writer) {
		this.writer = writer;
	}

	public Writer getWriter () {
		return writer;
	}

	public void setOutputType (OutputType outputType) {
		this.outputType = outputType;
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
		if (value == null || value instanceof Number || value instanceof Boolean) {
			writer.write(String.valueOf(value));
		} else {
			writer.write(outputType.quoteValue(value.toString()));
		}
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
		/** Like JSON, but names and values are only quoted if necessary. */
		minimal;

		// FIXME Avian regex matcher can't do that...
// static private Pattern javascriptPattern = Pattern.compile("[a-zA-Z_$][a-zA-Z_$0-9]*");
// static private Pattern minimalPattern = Pattern.compile("[a-zA-Z_$][^:}\\], ]*");

		public String quoteValue (String value) {
			value = value.replace("\\", "\\\\");
// if (this == OutputType.minimal && !value.equals("true") && !value.equals("false") && !value.equals("null")
// && minimalPattern.matcher(value).matches()) return value;
			return '"' + value.replace("\"", "\\\"") + '"';
		}

		public String quoteName (String value) {
			value = value.replace("\\", "\\\\");
// switch (this) {
// case minimal:
// if (minimalPattern.matcher(value).matches()) return value;
// return '"' + value.replace("\"", "\\\"") + '"';
// case javascript:
// if (javascriptPattern.matcher(value).matches()) return value;
// return '"' + value.replace("\"", "\\\"") + '"';
// default:
			return '"' + value.replace("\"", "\\\"") + '"';
// }
		}
	}
}
