/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.badlogic.gdx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

/** A {@code PropertyMap} is an {@code ObjectMap<String,String>} whose key/value pairs can be loaded from and stored to a stream
 * with the same line-oriented syntax supported by {@code java.util.Properties}.
 * <p>
 * Unlike {@code java.util.Properties}, some features are not supported:
 * <ul>
 * <li>defaults</li>
 * <li>object values (only strings are legal)</li>
 * <li>XML format</li>
 * </ul> */
public class PropertyMap extends ObjectMap<String, String> {

	private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3, KEY_DONE = 4, IGNORE = 5;

	public PropertyMap () {
	}

	/** Reads a property list (key/value pairs) from the input byte stream in a simple line-oriented format compatible with
	 * <code>java.util.Properties</code>. The input stream is assumed to use the ISO 8859-1 character encoding; that is each byte
	 * is one Latin1 character. Characters not in Latin1, and certain special characters, are represented in keys and elements
	 * using <a href="http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3">Unicode escapes</a>.
	 * <p>
	 * The specified stream remains open after this method returns.
	 * 
	 * @param inStream the input stream.
	 * @exception IOException if an error occurred when reading from the input stream.
	 * @throws IllegalArgumentException if the input stream contains a malformed Unicode escape sequence. */
	public synchronized void load (InputStream inStream) throws IOException {
		if (inStream == null) {
			throw new NullPointerException("InputStream cannot be null");
		}
		loadImpl(new InputStreamReader(inStream, "ISO-8859-1"));
	}

	/** Reads a property list (key/value pairs) from the input character stream in a simple line-oriented format compatible with
	 * <code>java.util.Properties</code>.
	 * <p>
	 * The specified stream remains open after this method returns.
	 * 
	 * @param reader the input character stream.
	 * @throws IOException if an error occurred when reading from the input stream.
	 * @throws IllegalArgumentException if a malformed Unicode escape appears in the input. */
	public synchronized void load (Reader reader) throws IOException {
		if (reader == null) {
			throw new NullPointerException("InputStream cannot be null");
		}
		loadImpl(reader);
	}

	@SuppressWarnings("deprecation")
	private void loadImpl (Reader in) throws IOException {
		int mode = NONE, unicode = 0, count = 0;
		char nextChar, buf[] = new char[40];
		int offset = 0, keyLength = -1, intVal;
		boolean firstChar = true;

		BufferedReader br = new BufferedReader(in);

		while (true) {
			intVal = br.read();
			if (intVal == -1) {
				break;
			}
			nextChar = (char)intVal;

			if (offset == buf.length) {
				char[] newBuf = new char[buf.length * 2];
				System.arraycopy(buf, 0, newBuf, 0, offset);
				buf = newBuf;
			}
			if (mode == UNICODE) {
				int digit = Character.digit(nextChar, 16);
				if (digit >= 0) {
					unicode = (unicode << 4) + digit;
					if (++count < 4) {
						continue;
					}
				} else if (count <= 4) {
					throw new IllegalArgumentException("Invalid Unicode sequence: illegal character");
				}
				mode = NONE;
				buf[offset++] = (char)unicode;
				if (nextChar != '\n') {
					continue;
				}
			}
			if (mode == SLASH) {
				mode = NONE;
				switch (nextChar) {
				case '\r':
					mode = CONTINUE; // Look for a following \n
					continue;
				case '\n':
					mode = IGNORE; // Ignore whitespace on the next line
					continue;
				case 'b':
					nextChar = '\b';
					break;
				case 'f':
					nextChar = '\f';
					break;
				case 'n':
					nextChar = '\n';
					break;
				case 'r':
					nextChar = '\r';
					break;
				case 't':
					nextChar = '\t';
					break;
				case 'u':
					mode = UNICODE;
					unicode = count = 0;
					continue;
				}
			} else {
				switch (nextChar) {
				case '#':
				case '!':
					if (firstChar) {
						while (true) {
							intVal = br.read();
							if (intVal == -1) {
								break;
							}
							nextChar = (char)intVal;
							if (nextChar == '\r' || nextChar == '\n') {
								break;
							}
						}
						continue;
					}
					break;
				case '\n':
					if (mode == CONTINUE) { // Part of a \r\n sequence
						mode = IGNORE; // Ignore whitespace on the next line
						continue;
					}
					// fall into the next case
				case '\r':
					mode = NONE;
					firstChar = true;
					if (offset > 0 || (offset == 0 && keyLength == 0)) {
						if (keyLength == -1) {
							keyLength = offset;
						}
						String temp = new String(buf, 0, offset);
						put(temp.substring(0, keyLength), temp.substring(keyLength));
					}
					keyLength = -1;
					offset = 0;
					continue;
				case '\\':
					if (mode == KEY_DONE) {
						keyLength = offset;
					}
					mode = SLASH;
					continue;
				case ':':
				case '=':
					if (keyLength == -1) { // if parsing the key
						mode = NONE;
						keyLength = offset;
						continue;
					}
					break;
				}
// if (Character.isWhitespace(nextChar)) { <-- not supported by GWT; replaced with isSpace.
				if (Character.isSpace(nextChar)) {
					if (mode == CONTINUE) {
						mode = IGNORE;
					}
					// if key length == 0 or value length == 0
					if (offset == 0 || offset == keyLength || mode == IGNORE) {
						continue;
					}
					if (keyLength == -1) { // if parsing the key
						mode = KEY_DONE;
						continue;
					}
				}
				if (mode == IGNORE || mode == CONTINUE) {
					mode = NONE;
				}
			}
			firstChar = false;
			if (mode == KEY_DONE) {
				keyLength = offset;
				mode = NONE;
			}
			buf[offset++] = nextChar;
		}
		if (mode == UNICODE && count <= 4) {
			throw new IllegalArgumentException("Invalid Unicode sequence: expected format \\uxxxx");
		}
		if (keyLength == -1 && offset > 0) {
			keyLength = offset;
		}
		if (keyLength >= 0) {
			String temp = new String(buf, 0, offset);
			String key = temp.substring(0, keyLength);
			String value = temp.substring(keyLength);
			if (mode == SLASH) {
				value += "\u0000";
			}
			put(key, value);
		}
	}

	/** Writes this property list (key/value pairs) in this <code>PropertyMap</code> to the output character stream in a simple
	 * line-oriented format compatible with <code>java.util.Properties</code>. The specified {@code OutputStream} is written using
	 * ISO-8859-1 character encoding.
	 * 
	 * @param out the {@code OutputStream}
	 * @param comment an optional comment to be written, or null.
	 * @exception IOException if writing this property list to the specified output stream throws an <tt>IOException</tt>.
	 * @exception NullPointerException if <code>out</code> is null. */
	public synchronized void store (OutputStream out, String comment) throws IOException {
		store(new OutputStreamWriter(out, "ISO-8859-1"), comment);
	}

	private static final String LINE_SEPARATOR = "\n"; // System.lineSeparator() is not supported by GWT

	/** Writes this property list (key/value pairs) in this <code>PropertyMap</code> to the output character stream in a simple
	 * line-oriented format compatible with <code>java.util.Properties</code>.
	 * 
	 * @param writer an output character stream writer.
	 * @param comment an optional comment to be written, or null.
	 * @exception IOException if writing this property list to the specified output stream throws an <tt>IOException</tt>.
	 * @exception NullPointerException if <code>writer</code> is null. */
	public synchronized void store (Writer writer, String comment) throws IOException {
		if (comment != null) {
			writer.write("#");
			writer.write(comment);
			writer.write(LINE_SEPARATOR);
		}
		writer.write("#");
		writer.write(new Date().toString());
		writer.write(LINE_SEPARATOR);

		StringBuilder sb = new StringBuilder(200);
		for (Entry<String, String> entry : entries()) {
			String key = (String)entry.key;
			dumpString(sb, key, true);
			sb.append('=');
			dumpString(sb, (String)entry.value, false);
			sb.append(LINE_SEPARATOR);
			writer.write(sb.toString());
			sb.setLength(0);
		}
		writer.flush();
	}

	private void dumpString (StringBuilder buffer, String string, boolean key) {
		int i = 0;
		if (!key && i < string.length() && string.charAt(i) == ' ') {
			buffer.append("\\ ");
			i++;
		}

		for (; i < string.length(); i++) {
			char ch = string.charAt(i);
			switch (ch) {
			case '\t':
				buffer.append("\\t");
				break;
			case '\n':
				buffer.append("\\n");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			case '\r':
				buffer.append("\\r");
				break;
			default:
				if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
					buffer.append('\\');
				}
				if (ch >= ' ' && ch <= '~') {
					buffer.append(ch);
				} else {
					String hex = Integer.toHexString(ch);
					buffer.append("\\u");
					for (int j = 0; j < 4 - hex.length(); j++) {
						buffer.append("0");
					}
					buffer.append(hex);
				}
			}
		}
	}
}
