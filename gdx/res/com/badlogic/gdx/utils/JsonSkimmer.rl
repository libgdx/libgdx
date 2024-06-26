// Do not edit this file! Generated by Ragel.
// Ragel.exe -J -o ../../../../../src/com/badlogic/gdx/utils/JsonSkimmer.java JsonSkimmer.rl
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;

import com.badlogic.gdx.files.FileHandle;

/** Lightweight event-based JSON parser. All values are provided as strings to reduce work when many values are ignored.
 * @author Nathan Sweet */
public class JsonSkimmer {
	public void parse (String json) {
		char[] data = json.toCharArray();
		parse(data, 0, data.length);
	}

	public void parse (Reader reader) {
		char[] data = new char[1024];
		int offset = 0;
		try {
			while (true) {
				int length = reader.read(data, offset, data.length - offset);
				if (length == -1) break;
				if (length == 0) {
					char[] newData = new char[data.length * 2];
					System.arraycopy(data, 0, newData, 0, data.length);
					data = newData;
				} else
					offset += length;
			}
		} catch (IOException ex) {
			throw new SerializationException("Error reading input.", ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
		parse(data, 0, offset);
	}

	public void parse (InputStream input) {
		Reader reader;
		try {
			reader = new InputStreamReader(input, "UTF-8");
		} catch (Exception ex) {
			throw new SerializationException("Error reading stream.", ex);
		}
		parse(reader);
	}

	public void parse (FileHandle file) {
		Reader reader;
		try {
			reader = file.reader("UTF-8");
		} catch (Exception ex) {
			throw new SerializationException("Error reading file: " + file, ex);
		}
		try {
			parse(reader);
		} catch (Exception ex) {
			throw new SerializationException("Error parsing file: " + file, ex);
		}
	}

	public void parse (char[] data, int offset, int length) {
		stop = false;
		int cs, p = offset, pe = length, eof = pe, top = 0;
		int[] stack = new int[4];

		int s = 0;
		String name = null;
		boolean needsUnescape = false, stringIsName = false, stringIsUnquoted = false;
		RuntimeException parseRuntimeEx = null;

		boolean debug = false;
		if (debug) System.out.println();

		try {
		%%{
			machine json;

			prepush {
				if (top == stack.length) stack = Arrays.copyOf(stack, stack.length * 2);
			}

			action name {
				stringIsName = true;
			}
			action string {
				String value = new String(data, s, p - s);
				if (needsUnescape) value = unescape(value);
				if (stringIsName) {
					stringIsName = false;
					if (debug) System.out.println("name: " + value);
					name = value;
				} else {
					if (debug) System.out.println("value: " + name + "=" + value);
					value(name, value, stringIsUnquoted);
					name = null;
				}
				if (stop) return;
				stringIsUnquoted = false;
				s = p;
			}
			action startObject {
				if (debug) System.out.println("startObject: " + name);
				startObject(name);
				if (stop) return;
				name = null;
				fcall object;
			}
			action endObject {
				if (debug) System.out.println("endObject");
				pop();
				if (stop) return;
				fret;
			}
			action startArray {
				if (debug) System.out.println("startArray: " + name);
				startArray(name);
				if (stop) return;
				name = null;
				fcall array;
			}
			action endArray {
				if (debug) System.out.println("endArray");
				pop();
				if (stop) return;
				fret;
			}
			action comment {
				int start = p - 1;
				if (data[p++] == '/') {
					while (p != eof && data[p] != '\n')
						p++;
					p--;
				} else {
					while (p + 1 < eof && data[p] != '*' || data[p + 1] != '/')
						p++;
					p++;
				}
				if (debug) System.out.println("comment " + new String(data, start, p - start));
			}
			action unquotedChars {
				if (debug) System.out.println("unquotedChars");
				s = p;
				needsUnescape = false;
				stringIsUnquoted = true;
				if (stringIsName) {
					outer:
					while (true) {
						switch (data[p]) {
						case '\\':
							needsUnescape = true;
							break;
						case '/':
							if (p + 1 == eof) break;
							char c = data[p + 1];
							if (c == '/' || c == '*') break outer;
							break;
						case ':':
						case '\r':
						case '\n':
							break outer;
						}
						if (debug) System.out.println("unquotedChar (name): '" + data[p] + "'");
						p++;
						if (p == eof) break;
					}
				} else {
					outer:
					while (true) {
						switch (data[p]) {
						case '\\':
							needsUnescape = true;
							break;
						case '/':
							if (p + 1 == eof) break;
							char c = data[p + 1];
							if (c == '/' || c == '*') break outer;
							break;
						case '}':
						case ']':
						case ',':
						case '\r':
						case '\n':
							break outer;
						}
						if (debug) System.out.println("unquotedChar (value): '" + data[p] + "'");
						p++;
						if (p == eof) break;
					}
				}
				p--;
				while (Character.isSpace(data[p]))
					p--;
			}
			action quotedChars {
				if (debug) System.out.println("quotedChars");
				s = ++p;
				needsUnescape = false;
				outer:
				while (true) {
					switch (data[p]) {
					case '\\':
						needsUnescape = true;
						p++;
						break;
					case '"':
						break outer;
					}
					if (debug) System.out.println("quotedChar: '" + data[p] + "'");
					p++;
					if (p == eof) break;
				}
				p--;
			}

			comment = ('//' | '/*') @comment;
			ws = [\r\n\t ] | comment;
			ws2 = [\t ] | comment;
			comma = ',' | ([\r\n] ws* ','?);
			quotedString = '"' @quotedChars %string '"';
			nameString = quotedString | ^[":,}/\r\n\t ] >unquotedChars %string;
			valueString = quotedString | ^[":,{[\]/\r\n\t ] >unquotedChars %string;
			value = '{' @startObject | '[' @startArray | valueString;
			nameValue = nameString >name ws* ':' ws* value;
			object := ws* nameValue? ws2* <: (comma ws* nameValue ws2*)** :>> (','? ws* '}' @endObject);
			array := ws* value? ws2* <: (comma ws* value ws2*)** :>> (','? ws* ']' @endArray);
			main := ws* value ws*;

			write init;
			write exec;
		}%%
		} catch (RuntimeException ex) {
			parseRuntimeEx = ex;
		}

		if (p < pe) {
			int lineNumber = 1;
			for (int i = 0; i < p; i++)
				if (data[i] == '\n') lineNumber++;
			int start = Math.max(0, p - 32);
			throw new SerializationException("Error parsing JSON on line " + lineNumber + " near: "
				+ new String(data, start, p - start) + "*ERROR*" + new String(data, p, Math.min(64, pe - p)), parseRuntimeEx);
		}
		if (parseRuntimeEx != null) throw new SerializationException("Error parsing JSON: " + new String(data), parseRuntimeEx);
	}

	%% write data;

	private boolean stop;

	/** Causes parsing to stop after the current or next object, array, or value. */
	public void stop () {
		stop = true;
	}

	public boolean isStopped () {
		return stop;
	}

	private String unescape (String value) {
		int length = value.length();
		StringBuilder buffer = new StringBuilder(length + 16);
		for (int i = 0; i < length;) {
			char c = value.charAt(i++);
			if (c != '\\') {
				buffer.append(c);
				continue;
			}
			if (i == length) break;
			c = value.charAt(i++);
			if (c == 'u') {
				buffer.append(Character.toChars(Integer.parseInt(value.substring(i, i + 4), 16)));
				i += 4;
				continue;
			}
			switch (c) {
			case '"':
			case '\\':
			case '/':
				break;
			case 'b':
				c = '\b';
				break;
			case 'f':
				c = '\f';
				break;
			case 'n':
				c = '\n';
				break;
			case 'r':
				c = '\r';
				break;
			case 't':
				c = '\t';
				break;
			default:
				throw new SerializationException("Illegal escaped character: \\" + c);
			}
			buffer.append(c);
		}
		return buffer.toString();
	}

	protected void startObject (@Null String name) {
	}

	protected void startArray (@Null String name) {
	}

	protected void pop () {
	}

	protected void value (@Null String name, String value, boolean unquoted) {
	}
}
