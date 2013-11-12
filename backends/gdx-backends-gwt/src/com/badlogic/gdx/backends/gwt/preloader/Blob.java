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

package com.badlogic.gdx.backends.gwt.preloader;

import java.io.IOException;
import java.io.InputStream;

import com.google.gwt.typedarrays.shared.Int8Array;

public final class Blob {

	public Blob (Int8Array data) {
		this.data = data;
	}

	public int length () {
		return data.length();
	}

	public byte get (int i) {
		return data.get(i);
	}

	public InputStream read () {
		return new InputStream() {

			@Override
			public int read () throws IOException {
				if (pos == length()) return -1;
				return get(pos++) & 0xff;
			}
			
			@Override
			public int available () {
				return length() - pos;
			}

			int pos;
		};
	}

	public String toBase64 () {
		int length = data.length();
		String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
		StringBuilder encoded = new StringBuilder(length * 4 / 3 + 2);
		for (int i = 0; i < length; i += 3) {
			if (length - i >= 3) {
				int j = ((data.get(i) & 0xff) << 16) + ((data.get(i + 1) & 0xff) << 8) + (data.get(i + 2) & 0xff);
				encoded.append(base64code.charAt((j >> 18) & 0x3f));
				encoded.append(base64code.charAt((j >> 12) & 0x3f));
				encoded.append(base64code.charAt((j >> 6) & 0x3f));
				encoded.append(base64code.charAt(j & 0x3f));
			} else if (length - i >= 2) {
				int j = ((data.get(i) & 0xff) << 16) + ((data.get(i + 1) & 0xff) << 8);
				encoded.append(base64code.charAt((j >> 18) & 0x3f));
				encoded.append(base64code.charAt((j >> 12) & 0x3f));
				encoded.append(base64code.charAt((j >> 6) & 0x3f));
				encoded.append("=");
			} else {
				int j = ((data.get(i) & 0xff) << 16);
				encoded.append(base64code.charAt((j >> 18) & 0x3f));
				encoded.append(base64code.charAt((j >> 12) & 0x3f));
				encoded.append("==");
			}
		}
		return encoded.toString();
	}

	private final Int8Array data;

}
