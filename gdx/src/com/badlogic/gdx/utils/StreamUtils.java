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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net.HttpRequest;

/** Provides utility methods to copy streams */
public class StreamUtils {
	public static final byte[] EMPTY_BYTES = new byte[0];

	/** Copy the data from an {@link InputStream} to an {@link OutputStream}.
	 * @throws IOException */
	public static void copyStream (InputStream input, OutputStream output) throws IOException {
		copyStream(input, output, 8192);
	}

	/** Copy the data from an {@link InputStream} to an {@link OutputStream}.
	 * @throws IOException */
	public static void copyStream (InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte[] buffer = new byte[bufferSize];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	/** Copy the data from an {@link InputStream} to a byte array.
	 * @throws IOException */
	public static byte[] copyStreamToByteArray (InputStream input) throws IOException {
		return copyStreamToByteArray(input, input.available());
	}

	/** Copy the data from an {@link InputStream} to a byte array.
	 * @param estimatedSize Used to preallocate a possibly correct sized byte array to avoid an array copy.
	 * @throws IOException */
	public static byte[] copyStreamToByteArray (InputStream input, int estimatedSize) throws IOException {
		ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(Math.max(0, estimatedSize));
		copyStream(input, baos);
		return baos.toByteArray();
	}

	/** Close and ignore all errors. */
	public static void closeQuietly (Closeable c) {
		if (c != null) try {
			c.close();
		} catch (IOException e) {
			// ignore
		}
	}
	
	/** A ByteArrayOutputStream which avoids copying of the byte array if not necessary. */
	private static class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
		OptimizedByteArrayOutputStream (int initialSize) {
			super(initialSize);
		}

		@Override
		public synchronized byte[] toByteArray () {
			if (count == buf.length) return buf;
			return super.toByteArray();
		}
	}
}