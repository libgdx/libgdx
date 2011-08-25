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
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayDeque;

/** Builder style API for emitting JSON.
 * @author Nathan Sweet */
public class JsonWriter extends Writer {
	Writer writer;
	private final ArrayDeque<JsonObject> stack = new ArrayDeque();
	private JsonObject current;

	public JsonWriter (Writer writer) {
		this.writer = writer;
	}

	public JsonWriter object () throws IOException {
		if (current != null) {
			if (!current.array) throw new RuntimeException("Current item must be an array.");
			if (!current.needsComma)
				current.needsComma = true;
			else
				writer.write(",");
		}
		stack.push(current = new JsonObject(false));
		return this;
	}

	public JsonWriter array () throws IOException {
		if (current != null) {
			if (!current.array) throw new RuntimeException("Current item must be an array.");
			if (!current.needsComma)
				current.needsComma = true;
			else
				writer.write(",");
		}
		stack.push(current = new JsonObject(true));
		return this;
	}

	public JsonWriter add (Object value) throws IOException {
		if (!current.array) throw new RuntimeException("Current item must be an array.");
		if (!current.needsComma)
			current.needsComma = true;
		else
			writer.write(",");
		if (value == null || value instanceof Number || value instanceof Boolean)
			writer.write(String.valueOf(value));
		else {
			writer.write("\"");
			writer.write(value.toString());
			writer.write("\"");
		}
		return this;
	}

	public JsonWriter object (String name) throws IOException {
		if (current == null || current.array) throw new RuntimeException("Current item must be an object.");
		if (!current.needsComma)
			current.needsComma = true;
		else
			writer.write(",");
		writer.write("\"");
		writer.write(name);
		writer.write("\":");
		stack.push(current = new JsonObject(false));
		return this;
	}

	public JsonWriter array (String name) throws IOException {
		if (current == null || current.array) throw new RuntimeException("Current item must be an object.");
		if (!current.needsComma)
			current.needsComma = true;
		else
			writer.write(",");
		writer.write("\"");
		writer.write(name);
		writer.write("\":");
		stack.push(current = new JsonObject(true));
		return this;
	}

	public JsonWriter set (String name, Object value) throws IOException {
		if (current == null || current.array) throw new RuntimeException("Current item must be an object.");
		if (!current.needsComma)
			current.needsComma = true;
		else
			writer.write(",");
		writer.write("\"");
		writer.write(name);
		if (value == null || value instanceof Number || value instanceof Boolean) {
			writer.write("\":");
			writer.write(String.valueOf(value));
		} else {
			writer.write("\":\"");
			writer.write(value.toString().replace("\\", "\\\\"));
			writer.write("\"");
		}
		return this;
	}

	public JsonWriter pop () throws IOException {
		stack.pop().close();
		current = stack.peek();
		return this;
	}

	public void write (char[] cbuf, int off, int len) throws IOException {
		writer.write(cbuf, off, len);
	}

	public void flush () throws IOException {
		writer.flush();
	}

	public void close () throws IOException {
		while (!stack.isEmpty())
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
}
