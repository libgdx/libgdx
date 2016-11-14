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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Extends {@link DataInputStream} with additional convenience methods.
 * @author Nathan Sweet */
public class DataInput extends DataInputStream {
	private char[] chars = new char[32];

	public DataInput (InputStream in) {
		super(in);
	}

	/** Reads a 1-5 byte int. */
	public int readInt (boolean optimizePositive) throws IOException {
		int b = read();
		int result = b & 0x7F;
		if ((b & 0x80) != 0) {
			b = read();
			result |= (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				b = read();
				result |= (b & 0x7F) << 14;
				if ((b & 0x80) != 0) {
					b = read();
					result |= (b & 0x7F) << 21;
					if ((b & 0x80) != 0) {
						b = read();
						result |= (b & 0x7F) << 28;
					}
				}
			}
		}
		return optimizePositive ? result : ((result >>> 1) ^ -(result & 1));
	}

	/** Reads the length and string of UTF8 characters, or null.
	 * @return May be null. */
	public String readString () throws IOException {
		int charCount = readInt(true);
		switch (charCount) {
		case 0:
			return null;
		case 1:
			return "";
		}
		charCount--;
		if (chars.length < charCount) chars = new char[charCount];
		char[] chars = this.chars;
		// Try to read 7 bit ASCII chars.
		int charIndex = 0;
		int b = 0;
		while (charIndex < charCount) {
			b = read();
			if (b > 127) break;
			chars[charIndex++] = (char)b;
		}
		// If a char was not ASCII, finish with slow path.
		if (charIndex < charCount) readUtf8_slow(charCount, charIndex, b);
		return new String(chars, 0, charCount);
	}

	private void readUtf8_slow (int charCount, int charIndex, int b) throws IOException {
		char[] chars = this.chars;
		while (true) {
			switch (b >> 4) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				chars[charIndex] = (char)b;
				break;
			case 12:
			case 13:
				chars[charIndex] = (char)((b & 0x1F) << 6 | read() & 0x3F);
				break;
			case 14:
				chars[charIndex] = (char)((b & 0x0F) << 12 | (read() & 0x3F) << 6 | read() & 0x3F);
				break;
			}
			if (++charIndex >= charCount) break;
			b = read() & 0xFF;
		}
	}
}
