/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

/*******************************************************************************
 * Copyright (c) 2008-2009 Bjoern Hoehrmann <bjoern@hoehrmann.de>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 ******************************************************************************/

package com.badlogic.gdx.utils;

/** Utf8Decoder converts UTF-8 encoded bytes into characters properly handling buffer boundaries.
 *
 * This class is stateful and up to 4 calls to {@link #decode(byte)} may be needed before a character is appended to the char
 * buffer.
 *
 * The UTF-8 decoding is done by this class and no additional buffers are created. The UTF-8 code was inspired by
 * http://bjoern.hoehrmann.de/utf-8/decoder/dfa/
 * 
 * @author davebaol */
public class Utf8Decoder {

	private static final char REPLACEMENT = '\ufffd';
	private static final int UTF8_ACCEPT = 0;
	private static final int UTF8_REJECT = 12;

	// This table maps bytes to character classes to reduce
	// the size of the transition table and create bitmasks.
	private static final byte[] BYTE_TABLE = {
		// @off - disable libgdx formatter
		 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,  9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,
		 7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
		 8,8,2,2,2,2,2,2,2,2,2,2,2,2,2,2,  2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
		10,3,3,3,3,3,3,3,3,3,3,3,3,4,3,3, 11,6,6,6,5,8,8,8,8,8,8,8,8,8,8,8
		// @on - enable libgdx formatter
	};

	// This is a transition table that maps a combination of a
	// state of the automaton and a character class to a state.
	private static final byte[] TRANSITION_TABLE = {
		// @off - disable libgdx formatter
		 0,12,24,36,60,96,84,12,12,12,48,72, 12,12,12,12,12,12,12,12,12,12,12,12,
		12, 0,12,12,12,12,12, 0,12, 0,12,12, 12,24,12,12,12,12,12,24,12,24,12,12,
		12,12,12,12,12,12,12,24,12,12,12,12, 12,24,12,12,12,12,12,12,12,24,12,12,
		12,12,12,12,12,12,12,36,12,36,12,12, 12,36,12,12,12,12,12,36,12,36,12,12,
		12,36,12,12,12,12,12,12,12,12,12,12
		// @on - enable libgdx formatter
	};

	private int codePoint;
	private int state;
	private final char[] utf16Char = new char[2];
	private char[] charBuffer;
	private int charOffset;

	public Utf8Decoder () {
		this.state = UTF8_ACCEPT;
	}

	protected void reset () {
		state = UTF8_ACCEPT;
	}

	public int decode (byte[] b, int offset, int length, char[] charBuffer, int charOffset) {
		this.charBuffer = charBuffer;
		this.charOffset = charOffset;
		int end = offset + length;
		for (int i = offset; i < end; i++)
			decode(b[i]);
		return this.charOffset - charOffset;
	}

	private void decode (byte b) {

		if (b > 0 && state == UTF8_ACCEPT) {
			charBuffer[charOffset++] = (char)(b & 0xFF);
		} else {
			int i = b & 0xFF;
			int type = BYTE_TABLE[i];
			codePoint = state == UTF8_ACCEPT ? (0xFF >> type) & i : (i & 0x3F) | (codePoint << 6);
			int next = TRANSITION_TABLE[state + type];

			switch (next) {
			case UTF8_ACCEPT:
				state = next;
				if (codePoint < Character.MIN_HIGH_SURROGATE) {
					charBuffer[charOffset++] = (char)codePoint;
				} else {
					// The code below is equivalent to
					// for (char c : Character.toChars(codePoint)) charBuffer[charOffset++] = c;
					// but does not allocate a char array.
					int codePointLength = Character.toChars(codePoint, utf16Char, 0);
					charBuffer[charOffset++] = utf16Char[0];
					if (codePointLength == 2) charBuffer[charOffset++] = utf16Char[1];
				}
				break;

			case UTF8_REJECT:
				codePoint = 0;
				state = UTF8_ACCEPT;
				charBuffer[charOffset++] = REPLACEMENT;
				break;

			default:
				state = next;
				break;
			}
		}
	}
}
