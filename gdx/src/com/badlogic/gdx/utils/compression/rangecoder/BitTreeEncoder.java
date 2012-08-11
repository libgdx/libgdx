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

import java.io.IOException;

public class BitTreeEncoder {
	short[] Models;
	int NumBitLevels;

	public BitTreeEncoder (int numBitLevels) {
		NumBitLevels = numBitLevels;
		Models = new short[1 << numBitLevels];
	}

	public void Init () {
		Decoder.InitBitModels(Models);
	}

	public void Encode (Encoder rangeEncoder, int symbol) throws IOException {
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0;) {
			bitIndex--;
			int bit = (symbol >>> bitIndex) & 1;
			rangeEncoder.Encode(Models, m, bit);
			m = (m << 1) | bit;
		}
	}

	public void ReverseEncode (Encoder rangeEncoder, int symbol) throws IOException {
		int m = 1;
		for (int i = 0; i < NumBitLevels; i++) {
			int bit = symbol & 1;
			rangeEncoder.Encode(Models, m, bit);
			m = (m << 1) | bit;
			symbol >>= 1;
		}
	}

	public int GetPrice (int symbol) {
		int price = 0;
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0;) {
			bitIndex--;
			int bit = (symbol >>> bitIndex) & 1;
			price += Encoder.GetPrice(Models[m], bit);
			m = (m << 1) + bit;
		}
		return price;
	}

	public int ReverseGetPrice (int symbol) {
		int price = 0;
		int m = 1;
		for (int i = NumBitLevels; i != 0; i--) {
			int bit = symbol & 1;
			symbol >>>= 1;
			price += Encoder.GetPrice(Models[m], bit);
			m = (m << 1) | bit;
		}
		return price;
	}

	public static int ReverseGetPrice (short[] Models, int startIndex, int NumBitLevels, int symbol) {
		int price = 0;
		int m = 1;
		for (int i = NumBitLevels; i != 0; i--) {
			int bit = symbol & 1;
			symbol >>>= 1;
			price += Encoder.GetPrice(Models[startIndex + m], bit);
			m = (m << 1) | bit;
		}
		return price;
	}

	public static void ReverseEncode (short[] Models, int startIndex, Encoder rangeEncoder, int NumBitLevels, int symbol)
		throws IOException {
		int m = 1;
		for (int i = 0; i < NumBitLevels; i++) {
			int bit = symbol & 1;
			rangeEncoder.Encode(Models, startIndex + m, bit);
			m = (m << 1) | bit;
			symbol >>= 1;
		}
	}
}
