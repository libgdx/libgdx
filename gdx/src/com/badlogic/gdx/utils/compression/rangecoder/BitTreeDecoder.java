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

package com.badlogic.gdx.utils.compression.rangecoder;

public class BitTreeDecoder {
	short[] Models;
	int NumBitLevels;

	public BitTreeDecoder (int numBitLevels) {
		NumBitLevels = numBitLevels;
		Models = new short[1 << numBitLevels];
	}

	public void Init () {
		Decoder.InitBitModels(Models);
	}

	public int Decode (Decoder rangeDecoder) throws java.io.IOException {
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0; bitIndex--)
			m = (m << 1) + rangeDecoder.DecodeBit(Models, m);
		return m - (1 << NumBitLevels);
	}

	public int ReverseDecode (Decoder rangeDecoder) throws java.io.IOException {
		int m = 1;
		int symbol = 0;
		for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
			int bit = rangeDecoder.DecodeBit(Models, m);
			m <<= 1;
			m += bit;
			symbol |= (bit << bitIndex);
		}
		return symbol;
	}

	public static int ReverseDecode (short[] Models, int startIndex, Decoder rangeDecoder, int NumBitLevels)
		throws java.io.IOException {
		int m = 1;
		int symbol = 0;
		for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
			int bit = rangeDecoder.DecodeBit(Models, startIndex + m);
			m <<= 1;
			m += bit;
			symbol |= (bit << bitIndex);
		}
		return symbol;
	}
}
