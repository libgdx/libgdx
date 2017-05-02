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

public class CRC {
	static public int[] Table = new int[256];

	static {
		for (int i = 0; i < 256; i++) {
			int r = i;
			for (int j = 0; j < 8; j++)
				if ((r & 1) != 0)
					r = (r >>> 1) ^ 0xEDB88320;
				else
					r >>>= 1;
			Table[i] = r;
		}
	}

	int _value = -1;

	public void Init () {
		_value = -1;
	}

	public void Update (byte[] data, int offset, int size) {
		for (int i = 0; i < size; i++)
			_value = Table[(_value ^ data[offset + i]) & 0xFF] ^ (_value >>> 8);
	}

	public void Update (byte[] data) {
		int size = data.length;
		for (int i = 0; i < size; i++)
			_value = Table[(_value ^ data[i]) & 0xFF] ^ (_value >>> 8);
	}

	public void UpdateByte (int b) {
		_value = Table[(_value ^ b) & 0xFF] ^ (_value >>> 8);
	}

	public int GetDigest () {
		return _value ^ (-1);
	}
}
