/*
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.badlogic.gdx.hiero;

import java.awt.font.GlyphVector;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Reads a TTF font file and provides access to kerning information.
 * 
 * Thanks to the Apache FOP project for their inspiring work!
 * 
 * @author Nathan Sweet
 */
class Kerning {
	private Map values = Collections.EMPTY_MAP;
	private int size = -1;
	private int kerningPairCount = -1;
	private float scale;
	private long bytePosition;
	private long headOffset = -1;
	private long kernOffset = -1;

	/**
	 * @param input The data for the TTF font.
	 * @param size The font size to use to determine kerning pixel offsets.
	 * @throws IOException If the font could not be read.
	 */
	public void load (InputStream input, int size) throws IOException {
		this.size = size;
		if (input == null) throw new IllegalArgumentException("input cannot be null.");
		readTableDirectory(input);
		if (headOffset == -1) throw new IOException("HEAD table not found.");
		if (kernOffset == -1) {
			values = Collections.EMPTY_MAP;
			return;
		}
		values = new HashMap(256);
		if (headOffset < kernOffset) {
			readHEAD(input);
			readKERN(input);
		} else {
			readKERN(input);
			readHEAD(input);
		}
		input.close();

		for (Iterator entryIter = values.entrySet().iterator(); entryIter.hasNext();) {
			Entry entry = (Entry)entryIter.next();
			// Scale the offset values using the font size.
			List valueList = (List)entry.getValue();
			for (ListIterator valueIter = valueList.listIterator(); valueIter.hasNext();) {
				int value = ((Integer)valueIter.next()).intValue();
				int glyphCode = value & 0xffff;
				int offset = value >> 16;
				offset = Math.round(offset * scale);
				if (offset == 0)
					valueIter.remove();
				else
					valueIter.set(new Integer((offset << 16) | glyphCode));
			}
			if (valueList.isEmpty()) {
				entryIter.remove();
			} else {
				// Replace ArrayList with int[].
				int[] valueArray = new int[valueList.size()];
				int i = 0;
				for (Iterator valueIter = valueList.iterator(); valueIter.hasNext(); i++)
					valueArray[i] = ((Integer)valueIter.next()).intValue();
				entry.setValue(valueArray);
				kerningPairCount += valueArray.length;
			}
		}
	}

	/**
	 * Returns the encoded kerning value for the specified glyph. The glyph code for a Unicode codepoint can be retrieved with
	 * {@link GlyphVector#getGlyphCode(int)}.
	 */
	public int[] getValues (int firstGlyphCode) {
		return (int[])values.get(new Integer(firstGlyphCode));
	}

	public int getKerning (int[] values, int otherGlyphCode) {
		int low = 0;
		int high = values.length - 1;
		while (low <= high) {
			int midIndex = (low + high) >>> 1;
			int value = values[midIndex];
			int foundGlyphCode = value & 0xffff;
			if (foundGlyphCode < otherGlyphCode)
				low = midIndex + 1;
			else if (foundGlyphCode > otherGlyphCode)
				high = midIndex - 1;
			else
				return value >> 16;
		}
		return 0;
	}

	public int getCount () {
		return kerningPairCount;
	}

	private void readTableDirectory (InputStream input) throws IOException {
		skip(input, 4);
		int tableCount = readUnsignedShort(input);
		skip(input, 6);

		byte[] tagBytes = new byte[4];
		for (int i = 0; i < tableCount; i++) {
			tagBytes[0] = readByte(input);
			tagBytes[1] = readByte(input);
			tagBytes[2] = readByte(input);
			tagBytes[3] = readByte(input);
			skip(input, 4);
			long offset = readUnsignedLong(input);
			skip(input, 4);

			String tag = new String(tagBytes, "ISO-8859-1");
			if (tag.equals("head")) {
				headOffset = offset;
				if (kernOffset != -1) break;
			} else if (tag.equals("kern")) {
				kernOffset = offset;
				if (headOffset != -1) break;
			}
		}
	}

	private void readHEAD (InputStream input) throws IOException {
		seek(input, headOffset + 2 * 4 + 2 * 4 + 2);
		int unitsPerEm = readUnsignedShort(input);
		scale = (float)size / unitsPerEm;
	}

	private void readKERN (InputStream input) throws IOException {
		seek(input, kernOffset + 2);
		for (int subTableCount = readUnsignedShort(input); subTableCount > 0; subTableCount--) {
			skip(input, 2 * 2);
			int tupleIndex = readUnsignedShort(input);
			if (!((tupleIndex & 1) != 0) || (tupleIndex & 2) != 0 || (tupleIndex & 4) != 0) return;
			if (tupleIndex >> 8 != 0) continue;

			int kerningCount = readUnsignedShort(input);
			skip(input, 3 * 2);
			while (kerningCount-- > 0) {
				int firstGlyphCode = readUnsignedShort(input);
				int secondGlyphCode = readUnsignedShort(input);
				int offset = readShort(input);
				int value = (offset << 16) | secondGlyphCode;

				List firstGlyphValues = (List)values.get(new Integer(firstGlyphCode));
				if (firstGlyphValues == null) {
					firstGlyphValues = new ArrayList(256);
					values.put(new Integer(firstGlyphCode), firstGlyphValues);
				}
				firstGlyphValues.add(new Integer(value));
			}
		}
	}

	private int readUnsignedByte (InputStream input) throws IOException {
		bytePosition++;
		int b = input.read();
		if (b == -1) throw new EOFException("Unexpected end of file.");
		return b;
	}

	private byte readByte (InputStream input) throws IOException {
		return (byte)readUnsignedByte(input);
	}

	private int readUnsignedShort (InputStream input) throws IOException {
		return (readUnsignedByte(input) << 8) + readUnsignedByte(input);
	}

	private short readShort (InputStream input) throws IOException {
		return (short)readUnsignedShort(input);
	}

	private long readUnsignedLong (InputStream input) throws IOException {
		long value = readUnsignedByte(input);
		value = (value << 8) + readUnsignedByte(input);
		value = (value << 8) + readUnsignedByte(input);
		value = (value << 8) + readUnsignedByte(input);
		return value;
	}

	private void skip (InputStream input, long skip) throws IOException {
		while (skip > 0) {
			long skipped = input.skip(skip);
			if (skipped <= 0) break;
			bytePosition += skipped;
			skip -= skipped;
		}
	}

	private void seek (InputStream input, long position) throws IOException {
		skip(input, position - bytePosition);
	}
}
