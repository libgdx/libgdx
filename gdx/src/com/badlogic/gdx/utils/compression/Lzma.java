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

package com.badlogic.gdx.utils.compression;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Adapted from LZMA SDK version 9.22.
 * 
 * This was modified to be used directly on streams, rather than via the command line as in the LZMA SDK.
 * 
 * We only currently allow the default LZMA options to be used, as we know it works on for our target usage. */
public class Lzma {
	static class CommandLine {
		public static final int kEncode = 0;
		public static final int kDecode = 1;
		public static final int kBenchmak = 2;

		public int Command = -1;
		public int NumBenchmarkPasses = 10;

		public int DictionarySize = 1 << 23;
		public boolean DictionarySizeIsDefined = false;

		public int Lc = 3;
		public int Lp = 0;
		public int Pb = 2;

		public int Fb = 128;
		public boolean FbIsDefined = false;

		public boolean Eos = false;

		public int Algorithm = 2;
		public int MatchFinder = 1;

		public String InFile;
		public String OutFile;
	}

	/** Compresses the given {@link InputStream} into the given {@link OutputStream}.
	 * 
	 * @param in the {@link InputStream} to compress
	 * @param out the {@link OutputStream} to compress to
	 * @throws IOException */
	static public void compress (InputStream in, OutputStream out) throws IOException {
		CommandLine params = new CommandLine();
		boolean eos = false;
		if (params.Eos) eos = true;
		com.badlogic.gdx.utils.compression.lzma.Encoder encoder = new com.badlogic.gdx.utils.compression.lzma.Encoder();
		if (!encoder.SetAlgorithm(params.Algorithm)) throw new RuntimeException("Incorrect compression mode");
		if (!encoder.SetDictionarySize(params.DictionarySize)) throw new RuntimeException("Incorrect dictionary size");
		if (!encoder.SetNumFastBytes(params.Fb)) throw new RuntimeException("Incorrect -fb value");
		if (!encoder.SetMatchFinder(params.MatchFinder)) throw new RuntimeException("Incorrect -mf value");
		if (!encoder.SetLcLpPb(params.Lc, params.Lp, params.Pb)) throw new RuntimeException("Incorrect -lc or -lp or -pb value");
		encoder.SetEndMarkerMode(eos);
		encoder.WriteCoderProperties(out);
		long fileSize;
		if (eos) {
			fileSize = -1;
		} else {
			if ((fileSize = in.available()) == 0) {
				fileSize = -1;
			}
		}
		for (int i = 0; i < 8; i++) {
			out.write((int)(fileSize >>> (8 * i)) & 0xFF);
		}
		encoder.Code(in, out, -1, -1, null);
	}

	/** Decompresses the given {@link InputStream} into the given {@link OutputStream}.
	 * 
	 * @param in the {@link InputStream} to decompress
	 * @param out the {@link OutputStream} to decompress to
	 * @throws IOException */
	static public void decompress (InputStream in, OutputStream out) throws IOException {
		int propertiesSize = 5;
		byte[] properties = new byte[propertiesSize];
		if (in.read(properties, 0, propertiesSize) != propertiesSize) throw new RuntimeException("input .lzma file is too short");
		com.badlogic.gdx.utils.compression.lzma.Decoder decoder = new com.badlogic.gdx.utils.compression.lzma.Decoder();
		if (!decoder.SetDecoderProperties(properties)) throw new RuntimeException("Incorrect stream properties");
		long outSize = 0;
		for (int i = 0; i < 8; i++) {
			int v = in.read();
			if (v < 0) {
				throw new RuntimeException("Can't read stream size");
			}
			outSize |= ((long)v) << (8 * i);
		}
		if (!decoder.Code(in, out, outSize)) {
			throw new RuntimeException("Error in data stream");
		}
	}
}
