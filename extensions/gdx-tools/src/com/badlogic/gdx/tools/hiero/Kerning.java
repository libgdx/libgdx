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

package com.badlogic.gdx.tools.hiero;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/** Reads a TTF font file and provides access to kerning information.
 * 
 * Thanks to the Apache FOP project for their inspiring work!
 * 
 * @author Nathan Sweet */
public class Kerning {
	private TTFInputStream input;
	private float scale;
	private int headOffset = -1;
	private int kernOffset = -1;
	private int gposOffset = -1;
	private IntIntMap kernings = new IntIntMap();

	/** @param inputStream The data for the TTF font.
	 * @param fontSize The font size to use to determine kerning pixel offsets.
	 * @throws IOException If the font could not be read. */
	public void load (InputStream inputStream, int fontSize) throws IOException {
		if (inputStream == null) throw new IllegalArgumentException("inputStream cannot be null.");
		input = new TTFInputStream(inputStream);
		inputStream.close();

		readTableDirectory();
		if (headOffset == -1) throw new IOException("HEAD table not found.");
		readHEAD(fontSize);

		// By reading the 'kern' table last, it takes precedence over the 'GPOS' table. We are more likely to interpret
		// the GPOS table incorrectly because we ignore most of it, since BMFont doesn't support its features.
		if (gposOffset != -1) {
			input.seek(gposOffset);
			readGPOS();
		}
		if (kernOffset != -1) {
			input.seek(kernOffset);
			readKERN();
		}
		input.close();
		input = null;
	}

	/** @return A map from pairs of glyph codes to their kerning in pixels. Each map key encodes two glyph codes:
	 * the high 16 bits form the first glyph code, and the low 16 bits form the second. */
	public IntIntMap getKernings () {
		return kernings;
	}

	private void storeKerningOffset (int firstGlyphCode, int secondGlyphCode, int offset) {
		// Scale the offset values using the font size.
		int value = Math.round(offset * scale);
		if (value == 0) {
			return;
		}
		int key = (firstGlyphCode << 16) | secondGlyphCode;
		kernings.put(key, value);
	}

	private void readTableDirectory () throws IOException {
		input.skip(4);
		int tableCount = input.readUnsignedShort();
		input.skip(6);

		byte[] tagBytes = new byte[4];
		for (int i = 0; i < tableCount; i++) {
			tagBytes[0] = input.readByte();
			tagBytes[1] = input.readByte();
			tagBytes[2] = input.readByte();
			tagBytes[3] = input.readByte();
			input.skip(4);
			int offset = (int) input.readUnsignedLong();
			input.skip(4);

			String tag = new String(tagBytes, "ISO-8859-1");
			if (tag.equals("head")) {
				headOffset = offset;
			} else if (tag.equals("kern")) {
				kernOffset = offset;
			} else if (tag.equals("GPOS")) {
				gposOffset = offset;
			}
		}
	}

	private void readHEAD (int fontSize) throws IOException {
		input.seek(headOffset + 2 * 4 + 2 * 4 + 2);
		int unitsPerEm = input.readUnsignedShort();
		scale = (float)fontSize / unitsPerEm;
	}

	private void readKERN () throws IOException {
		input.seek(kernOffset + 2);
		for (int subTableCount = input.readUnsignedShort(); subTableCount > 0; subTableCount--) {
			input.skip(2 * 2);
			int tupleIndex = input.readUnsignedShort();
			if (!((tupleIndex & 1) != 0) || (tupleIndex & 2) != 0 || (tupleIndex & 4) != 0) return;
			if (tupleIndex >> 8 != 0) continue;

			int kerningCount = input.readUnsignedShort();
			input.skip(3 * 2);
			while (kerningCount-- > 0) {
				int firstGlyphCode = input.readUnsignedShort();
				int secondGlyphCode = input.readUnsignedShort();
				int offset = (int) input.readShort();
				storeKerningOffset(firstGlyphCode, secondGlyphCode, offset);
			}
		}
	}

	private void readGPOS () throws IOException {
		// See https://www.microsoft.com/typography/otspec/gpos.htm for the format and semantics.
		// Useful tools are ttfdump and showttf.
		input.seek(gposOffset + 4 + 2 + 2);
		int lookupListOffset = input.readUnsignedShort();
		input.seek(gposOffset + lookupListOffset);

		int lookupListPosition = input.getPosition();
		int lookupCount = input.readUnsignedShort();
		int[] lookupOffsets = input.readUnsignedShortArray(lookupCount);

		for (int i = 0; i < lookupCount; i++) {
			int lookupPosition = lookupListPosition + lookupOffsets[i];
			input.seek(lookupPosition);
			int type = input.readUnsignedShort();
			readSubtables(type, lookupPosition);
		}
	}

	private void readSubtables ( int type, int lookupPosition) throws IOException {
		input.skip(2);
		int subTableCount = input.readUnsignedShort();
		int[] subTableOffsets = input.readUnsignedShortArray(subTableCount);

		for (int i = 0; i < subTableCount; i++) {
			int subTablePosition = lookupPosition + subTableOffsets[i];
			readSubtable(type, subTablePosition);
		}
	}

	private void readSubtable (int type, int subTablePosition) throws IOException {
		input.seek(subTablePosition);
		if (type == 2) {
			readPairAdjustmentSubtable(subTablePosition);
		} else if (type == 9) {
			readExtensionPositioningSubtable(subTablePosition);
		}
	}

	private void readPairAdjustmentSubtable(int subTablePosition) throws IOException {
		int type = input.readUnsignedShort();
		if (type == 1) {
			readPairPositioningAdjustmentFormat1(subTablePosition);
		} else if (type == 2) {
			readPairPositioningAdjustmentFormat2(subTablePosition);
		}
	}

	private void readExtensionPositioningSubtable (int subTablePosition) throws IOException {
		int type = input.readUnsignedShort();
		if (type == 1) {
			readExtensionPositioningFormat1(subTablePosition);
		}
	}

	private void readPairPositioningAdjustmentFormat1 (long subTablePosition) throws IOException {
		int coverageOffset = input.readUnsignedShort();
		int valueFormat1 = input.readUnsignedShort();
		int valueFormat2 = input.readUnsignedShort();
		int pairSetCount = input.readUnsignedShort();
		int[] pairSetOffsets = input.readUnsignedShortArray(pairSetCount);

		input.seek((int) (subTablePosition + coverageOffset));
		int[] coverage = readCoverageTable();

		// The two should be equal, but just in case they're not, we can still do something sensible.
		pairSetCount = Math.min(pairSetCount, coverage.length);

		for (int i = 0; i < pairSetCount; i++) {
			int firstGlyph = coverage[i];
			input.seek((int) (subTablePosition + pairSetOffsets[i]));
			int pairValueCount = input.readUnsignedShort();
			for (int j = 0; j < pairValueCount; j++) {
				int secondGlyph = input.readUnsignedShort();
				int xAdvance1 = readXAdvanceFromValueRecord(valueFormat1);
				readXAdvanceFromValueRecord(valueFormat2); // Value2
				if (xAdvance1 != 0) {
					storeKerningOffset(firstGlyph, secondGlyph, xAdvance1);
				}
			}
		}
	}

	private void readPairPositioningAdjustmentFormat2 (int subTablePosition) throws IOException {
		int coverageOffset = input.readUnsignedShort();
		int valueFormat1 = input.readUnsignedShort();
		int valueFormat2 = input.readUnsignedShort();
		int classDefOffset1 = input.readUnsignedShort();
		int classDefOffset2 = input.readUnsignedShort();
		int class1Count = input.readUnsignedShort();
		int class2Count = input.readUnsignedShort();

		int position = input.getPosition();

		input.seek((int) (subTablePosition + coverageOffset));
		int[] coverage = readCoverageTable();

		input.seek(position);
		IntArray[] glyphsByClass1 = readClassDefinition(subTablePosition + classDefOffset1, class1Count);
		IntArray[] glyphsByClass2 = readClassDefinition(subTablePosition + classDefOffset2, class2Count);
		input.seek(position);

		for (int i = 0; i < coverage.length; i++) {
			int glyph = coverage[i];
			boolean found = false;
			for (int j = 1; j < class1Count && !found; j++) {
				found = glyphsByClass1[j].contains(glyph);
			}
			if (!found) {
				glyphsByClass1[0].add(glyph);
			}
		}

		for (int i = 0; i < class1Count; i++) {
			for (int j = 0; j < class2Count; j++) {
				int xAdvance1 = readXAdvanceFromValueRecord(valueFormat1);
				readXAdvanceFromValueRecord(valueFormat2); // Value2
				if (xAdvance1 == 0) continue;
				for (int k = 0; k < glyphsByClass1[i].size; k++) {
					int glyph1 = glyphsByClass1[i].items[k];
					for (int l = 0; l < glyphsByClass2[j].size; l++) {
						int glyph2 = glyphsByClass2[j].items[l];
						storeKerningOffset(glyph1, glyph2, xAdvance1);
					}
				}
			}
		}
	}

	private void readExtensionPositioningFormat1 (int subTablePosition) throws IOException {
		int lookupType = input.readUnsignedShort();
		int lookupPosition = subTablePosition + (int) input.readUnsignedLong();
		readSubtable(lookupType, lookupPosition);
	}

	private IntArray[] readClassDefinition (int position, int classCount) throws IOException {
		input.seek(position);

		IntArray[] glyphsByClass = new IntArray[classCount];
		for (int i = 0; i < classCount; i++) {
			glyphsByClass[i] = new IntArray();
		}

		int classFormat = input.readUnsignedShort();
		if (classFormat == 1) {
			readClassDefinitionFormat1(glyphsByClass);
		} else if (classFormat == 2) {
			readClassDefinitionFormat2(glyphsByClass);
		} else {
			throw new IOException("Unknown class definition table type " + classFormat);
		}
		return glyphsByClass;
	}

	private void readClassDefinitionFormat1 (IntArray[] glyphsByClass) throws IOException {
		int startGlyph = input.readUnsignedShort();
		int glyphCount = input.readUnsignedShort();
		int[] classValueArray = input.readUnsignedShortArray(glyphCount);
		for (int i = 0; i < glyphCount; i++) {
			int glyph = startGlyph + i;
			int glyphClass = classValueArray[i];
			if (glyphClass < glyphsByClass.length) {
				glyphsByClass[glyphClass].add(glyph);
			}
		}
	}

	private void readClassDefinitionFormat2 (IntArray[] glyphsByClass) throws IOException {
		int classRangeCount = input.readUnsignedShort();
		for (int i = 0; i < classRangeCount; i++) {
			int start = input.readUnsignedShort();
			int end = input.readUnsignedShort();
			int glyphClass = input.readUnsignedShort();
			if (glyphClass < glyphsByClass.length) {
				for (int glyph = start; glyph <= end; glyph++) {
					glyphsByClass[glyphClass].add(glyph);
				}
			}
		}
	}

	private int[] readCoverageTable () throws IOException {
		int format = input.readUnsignedShort();
		if (format == 1) {
			int glyphCount = input.readUnsignedShort();
			int[] glyphArray = input.readUnsignedShortArray(glyphCount);
			return glyphArray;
		} else if (format == 2) {
			int rangeCount = input.readUnsignedShort();
			IntArray glyphArray = new IntArray();
			for (int i = 0; i < rangeCount; i++) {
				int start = input.readUnsignedShort();
				int end = input.readUnsignedShort();
				input.skip(2);
				for (int glyph = start; glyph <= end; glyph++) {
					glyphArray.add(glyph);
				}
			}
			return glyphArray.shrink();
		}
		throw new IOException("Unknown coverage table format " + format);
	}

	private int readXAdvanceFromValueRecord (int valueFormat) throws IOException {
		int xAdvance = 0;
		for (int mask = 1; mask <= 0x8000 && mask <= valueFormat; mask <<= 1) {
			if ((valueFormat & mask) != 0) {
				int value = (int) input.readShort();
				if (mask == 0x0004) {
					xAdvance = value;
				}
			}
		}
		return xAdvance;
	}

	private static class TTFInputStream extends ByteArrayInputStream {
		public TTFInputStream (InputStream input) throws IOException {
			super(readAllBytes(input));
		}

		private static byte[] readAllBytes(InputStream input) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int numRead;
			byte[] buffer = new byte[16384];
			while ((numRead = input.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, numRead);
			}
			return out.toByteArray();
		}

		public int getPosition () {
			return pos;
		}

		public void seek (int position) {
			pos = position;
		}

		public int readUnsignedByte () throws IOException {
			int b = read();
			if (b == -1) throw new EOFException("Unexpected end of file.");
			return b;
		}

		public byte readByte () throws IOException {
			return (byte) readUnsignedByte();
		}

		public int readUnsignedShort () throws IOException {
			return (readUnsignedByte() << 8) + readUnsignedByte();
		}

		public short readShort () throws IOException {
			return (short)readUnsignedShort();
		}

		public long readUnsignedLong () throws IOException {
			long value = readUnsignedByte();
			value = (value << 8) + readUnsignedByte();
			value = (value << 8) + readUnsignedByte();
			value = (value << 8) + readUnsignedByte();
			return value;
		}

		public int[] readUnsignedShortArray (int count) throws IOException {
			int[] shorts = new int[count];
			for (int i = 0; i < count; i++) {
				shorts[i] = readUnsignedShort();
			}
			return shorts;
		}
	}
}
