/*
 * Copyright (c) 2002 JSON.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * The Software shall be used for Good, not Evil.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.badlogic.gdx.utils.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * A JSONTokener takes a source string and extracts characters and tokens from it. It is used by the JSONObject and JSONArray
 * constructors to parse JSON source strings.
 * @author JSON.org
 * @version 2010-02-02
 */
public class JSONTokener {

	private int character;
	private boolean eof;
	private int index;
	private int line;
	private char previous;
	private Reader reader;
	private boolean usePrevious;

	/**
	 * Construct a JSONTokener from a reader.
	 * 
	 * @param reader A reader.
	 */
	public JSONTokener (Reader reader) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
		this.character = 1;
		this.line = 1;
	}

	/**
	 * Construct a JSONTokener from a string.
	 * 
	 * @param s A source string.
	 */
	public JSONTokener (String s) {
		this(new StringReader(s));
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability, so that you can test for a digit or letter before
	 * attempting to parse the next number or identifier.
	 */
	public void back () throws JSONException {
		if (usePrevious || index <= 0) {
			throw new JSONException("Stepping back two steps is not supported");
		}
		this.index -= 1;
		this.character -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Get the hex value of a character (base16).
	 * @param c A character between '0' and '9' or between 'A' and 'F' or between 'a' and 'f'.
	 * @return An int between 0 and 15, or -1 if c was not a hex digit.
	 */
	public static int dehexchar (char c) {
		if (c >= '0' && c <= '9') {
			return c - '0';
		}
		if (c >= 'A' && c <= 'F') {
			return c - ('A' - 10);
		}
		if (c >= 'a' && c <= 'f') {
			return c - ('a' - 10);
		}
		return -1;
	}

	public boolean end () {
		return eof && !usePrevious;
	}

	/**
	 * Determine if the source string still contains characters that next() can consume.
	 * @return true if not yet at the end of the source.
	 */
	public boolean more () throws JSONException {
		next();
		if (end()) {
			return false;
		}
		back();
		return true;
	}

	/**
	 * Get the next character in the source string.
	 * 
	 * @return The next character, or 0 if past the end of the source string.
	 */
	public char next () throws JSONException {
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new JSONException(exception);
			}

			if (c <= 0) { // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
		if (this.previous == '\r') {
			this.line += 1;
			this.character = c == '\n' ? 0 : 1;
		} else if (c == '\n') {
			this.line += 1;
			this.character = 0;
		} else {
			this.character += 1;
		}
		this.previous = (char)c;
		return this.previous;
	}

	/**
	 * Consume the next character, and check that it matches a specified character.
	 * @param c The character to match.
	 * @return The character.
	 * @throws JSONException if the character does not match.
	 */
	public char next (char c) throws JSONException {
		char n = next();
		if (n != c) {
			throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
		}
		return n;
	}

	/**
	 * Get the next n characters.
	 * 
	 * @param n The number of characters to take.
	 * @return A string of n characters.
	 * @throws JSONException Substring bounds error if there are not n characters remaining in the source string.
	 */
	public String next (int n) throws JSONException {
		if (n == 0) {
			return "";
		}

		char[] buffer = new char[n];
		int pos = 0;

		while (pos < n) {
			buffer[pos] = next();
			if (end()) {
				throw syntaxError("Substring bounds error");
			}
			pos += 1;
		}
		return new String(buffer);
	}

	/**
	 * Get the next char in the string, skipping whitespace.
	 * @throws JSONException
	 * @return A character, or 0 if there are no more characters.
	 */
	public char nextClean () throws JSONException {
		for (;;) {
			char c = next();
			if (c == 0 || c > ' ') {
				return c;
			}
		}
	}

	/**
	 * Return the characters up to the next close quote character. Backslash processing is done. The formal JSON format does not
	 * allow strings in single quotes, but an implementation is allowed to accept them.
	 * @param quote The quoting character, either <code>"</code>&nbsp;<small>(double quote)</small> or <code>'</code>
	 *           &nbsp;<small>(single quote)</small>.
	 * @return A String.
	 * @throws JSONException Unterminated string.
	 */
	public String nextString (char quote) throws JSONException {
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = next();
			switch (c) {
			case 0:
			case '\n':
			case '\r':
				throw syntaxError("Unterminated string");
			case '\\':
				c = next();
				switch (c) {
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				case 'u':
					sb.append((char)Integer.parseInt(next(4), 16));
					break;
				case '"':
				case '\'':
				case '\\':
				case '/':
					sb.append(c);
					break;
				default:
					throw syntaxError("Illegal escape.");
				}
				break;
			default:
				if (c == quote) {
					return sb.toString();
				}
				sb.append(c);
			}
		}
	}

	/**
	 * Get the text up but not including the specified character or the end of line, whichever comes first.
	 * @param d A delimiter character.
	 * @return A string.
	 */
	public String nextTo (char d) throws JSONException {
		StringBuffer sb = new StringBuffer();
		for (;;) {
			char c = next();
			if (c == d || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the text up but not including one of the specified delimiter characters or the end of line, whichever comes first.
	 * @param delimiters A set of delimiter characters.
	 * @return A string, trimmed.
	 */
	public String nextTo (String delimiters) throws JSONException {
		char c;
		StringBuffer sb = new StringBuffer();
		for (;;) {
			c = next();
			if (delimiters.indexOf(c) >= 0 || c == 0 || c == '\n' || c == '\r') {
				if (c != 0) {
					back();
				}
				return sb.toString().trim();
			}
			sb.append(c);
		}
	}

	/**
	 * Get the next value. The value can be a Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
	 * JSONObject.NULL object.
	 * @throws JSONException If syntax error.
	 * 
	 * @return An object.
	 */
	public Object nextValue () throws JSONException {
		char c = nextClean();
		String s;

		switch (c) {
		case '"':
		case '\'':
			return nextString(c);
		case '{':
			back();
			return new JSONObject(this);
		case '[':
		case '(':
			back();
			return new JSONArray(this);
		}

		/*
		 * Handle unquoted text. This could be the values true, false, or null, or it can be a number. An implementation (such as
		 * this one) is allowed to also accept non-standard forms.
		 * 
		 * Accumulate characters until we reach the end of the text or a formatting character.
		 */

		StringBuffer sb = new StringBuffer();
		while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
			sb.append(c);
			c = next();
		}
		back();

		s = sb.toString().trim();
		if (s.equals("")) {
			throw syntaxError("Missing value");
		}
		return JSONObject.stringToValue(s);
	}

	/**
	 * Skip characters until the next character is the requested character. If the requested character is not found, no characters
	 * are skipped.
	 * @param to A character to skip to.
	 * @return The requested character, or zero if the requested character is not found.
	 */
	public char skipTo (char to) throws JSONException {
		char c;
		try {
			int startIndex = this.index;
			int startCharacter = this.character;
			int startLine = this.line;
			reader.mark(Integer.MAX_VALUE);
			do {
				c = next();
				if (c == 0) {
					reader.reset();
					this.index = startIndex;
					this.character = startCharacter;
					this.line = startLine;
					return c;
				}
			} while (c != to);
		} catch (IOException exc) {
			throw new JSONException(exc);
		}

		back();
		return c;
	}

	/**
	 * Make a JSONException to signal a syntax error.
	 * 
	 * @param message The error message.
	 * @return A JSONException object, suitable for throwing
	 */
	public JSONException syntaxError (String message) {
		return new JSONException(message + toString());
	}

	/**
	 * Make a printable string of this JSONTokener.
	 * 
	 * @return " at {index} [character {character} line {line}]"
	 */
	public String toString () {
		return " at " + index + " [character " + this.character + " line " + this.line + "]";
	}
}
