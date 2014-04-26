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

package com.badlogic.gdx.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Set;

/** {@code PropertyResourceBundle} loads resources from an {@code InputStream}. All resources are Strings. The resources must be of
 * the form {@code key=value}, one resource per line (see Properties).
 * 
 * @see ResourceBundle
 * @see java.util.Properties */
public class PropertyResourceBundle extends ResourceBundle {

	Properties resources;

	/** Constructs a new instance of {@code PropertyResourceBundle} and loads the properties file from the specified
	 * {@code InputStream}.
	 * 
	 * @param stream the {@code InputStream}.
	 * @throws IOException if an error occurs during a read operation on the {@code InputStream}. */
	public PropertyResourceBundle (InputStream stream) throws IOException {
		this(stream, false);
	}

	/** Constructs a new instance of {@code PropertyResourceBundle} and loads the properties from the specified {@code InputStream}
	 * according to the specified format, either key/value pairs or XML document.
	 * 
	 * @param stream the {@code InputStream}.
	 * @param fromXml true if a XML document is expected; false otherwise.
	 * @throws IOException if an error occurs during a read operation on the {@code InputStream}. */
	public PropertyResourceBundle (InputStream stream, boolean fromXml) throws IOException {
		if (stream == null) {
			throw new NullPointerException("stream == null");
		}
		resources = new Properties();
		if (fromXml) {
			resources.loadFromXML(stream);
		} else {
			resources.load(stream);
		}
	}

	/** Constructs a new resource bundle with properties read from {@code reader}.
	 * 
	 * @param reader the {@code Reader}
	 * @throws IOException */
	public PropertyResourceBundle (Reader reader) throws IOException {
		resources = new Properties();
		resources.load(reader);
	}

	@Override
	protected Set<String> handleKeySet () {
		return resources.stringPropertyNames();
	}

	Enumeration<String> getLocalKeys () {
		return (Enumeration<String>)resources.propertyNames();
	}

	@Override
	public Enumeration<String> getKeys () {
		if (parent == null) {
			return getLocalKeys();
		}
		return new Enumeration<String>() {
			Enumeration<String> local = getLocalKeys();

			Enumeration<String> pEnum = parent.getKeys();

			String nextElement;

			private boolean findNext () {
				if (nextElement != null) {
					return true;
				}
				while (pEnum.hasMoreElements()) {
					String next = pEnum.nextElement();
					if (!resources.containsKey(next)) {
						nextElement = next;
						return true;
					}
				}
				return false;
			}

			public boolean hasMoreElements () {
				if (local.hasMoreElements()) {
					return true;
				}
				return findNext();
			}

			public String nextElement () {
				if (local.hasMoreElements()) {
					return local.nextElement();
				}
				if (findNext()) {
					String result = nextElement;
					nextElement = null;
					return result;
				}
				// Cause an exception
				return pEnum.nextElement();
			}
		};
	}

	@Override
	public Object handleGetObject (String key) {
		return resources.get(key);
	}

	/** This class acts as cross-platform support since in Android 2.2 its superclass <code>java.util.Properties</code> has no
	 * method {@link Properties#load(Reader) load(reader)} which allows you to use a specific encoding. */
	private static class Properties extends java.util.Properties {

		private static final long serialVersionUID = 1L;

		private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3, KEY_DONE = 4, IGNORE = 5;

		public Properties () {
		}

		public Properties (Properties defaults) {
			super(defaults);
		}

		@Override
		public synchronized void load (InputStream in) throws IOException {
			if (in == null) {
				throw new NullPointerException("in == null");
			}
			loadImpl(new InputStreamReader(in, "ISO-8859-1"));
		}

		@Override
		public synchronized void load (Reader in) throws IOException {
			if (in == null) {
				throw new NullPointerException("in == null");
			}
			loadImpl(in);
		}

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
					if (Character.isWhitespace(nextChar)) {
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
	}
}
