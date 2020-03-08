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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** Extends {@link DataOutputStream} with additional convenience methods.
 * @author Nathan Sweet */
public class DataOutput extends DataOutputStream {
	public DataOutput (OutputStream out) {
		super(out);
	}

	/** Writes a 1-5 byte int.
	 * @param optimizePositive If true, small positive numbers will be more efficient (1 byte) and small negative numbers will be
	 *           inefficient (5 bytes). */
	public int writeInt (int value, boolean optimizePositive) throws IOException {
		if (!optimizePositive) value = (value << 1) ^ (value >> 31);
		if (value >>> 7 == 0) {
			write((byte)value);
			return 1;
		}
		write((byte)((value & 0x7F) | 0x80));
		if (value >>> 14 == 0) {
			write((byte)(value >>> 7));
			return 2;
		}
		write((byte)(value >>> 7 | 0x80));
		if (value >>> 21 == 0) {
			write((byte)(value >>> 14));
			return 3;
		}
		write((byte)(value >>> 14 | 0x80));
		if (value >>> 28 == 0) {
			write((byte)(value >>> 21));
			return 4;
		}
		write((byte)(value >>> 21 | 0x80));
		write((byte)(value >>> 28));
		return 5;
	}

	/** Writes a length and then the string as UTF8.
	 * @param value May be null. */
	public void writeString (@Null String value) throws IOException {
		if (value == null) {
			write(0);
			return;
		}
		int charCount = value.length();
		if (charCount == 0) {
			writeByte(1);
			return;
		}
		writeInt(charCount + 1, true);
		// Try to write 8 bit chars.
		int charIndex = 0;
		for (; charIndex < charCount; charIndex++) {
			int c = value.charAt(charIndex);
			if (c > 127) break;
			write((byte)c);
		}
		if (charIndex < charCount) writeString_slow(value, charCount, charIndex);
	}

	private void writeString_slow (String value, int charCount, int charIndex) throws IOException {
		for (; charIndex < charCount; charIndex++) {
			int c = value.charAt(charIndex);
			if (c <= 0x007F) {
				write((byte)c);
			} else if (c > 0x07FF) {
				write((byte)(0xE0 | c >> 12 & 0x0F));
				write((byte)(0x80 | c >> 6 & 0x3F));
				write((byte)(0x80 | c & 0x3F));
			} else {
				write((byte)(0xC0 | c >> 6 & 0x1F));
				write((byte)(0x80 | c & 0x3F));
			}
		}
	}
}
