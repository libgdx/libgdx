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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;

/** Provides utility methods to copy streams. */
public final class StreamUtils {
	public static final int DEFAULT_BUFFER_SIZE = 4096;
	public static final byte[] EMPTY_BYTES = new byte[0];

	/** Allocates a {@value #DEFAULT_BUFFER_SIZE} byte[] for use as a temporary buffer and calls
	 * {@link #copyStream(InputStream, OutputStream, byte[])}. */
	public static void copyStream (InputStream input, OutputStream output) throws IOException {
		copyStream(input, output, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/** Allocates a byte[] of the specified size for use as a temporary buffer and calls
	 * {@link #copyStream(InputStream, OutputStream, byte[])}. */
	public static void copyStream (InputStream input, OutputStream output, int bufferSize) throws IOException {
		copyStream(input, output, new byte[bufferSize]);
	}

	/** Copy the data from an {@link InputStream} to an {@link OutputStream}, using the specified byte[] as a temporary buffer. The
	 * stream is not closed. */
	public static void copyStream (InputStream input, OutputStream output, byte[] buffer) throws IOException {
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	/** Allocates a {@value #DEFAULT_BUFFER_SIZE} byte[] for use as a temporary buffer and calls
	 * {@link #copyStream(InputStream, OutputStream, byte[])}. */
	public static void copyStream (InputStream input, ByteBuffer output) throws IOException {
		copyStream(input, output, new byte[DEFAULT_BUFFER_SIZE]);
	}

	/** Allocates a byte[] of the specified size for use as a temporary buffer and calls
	 * {@link #copyStream(InputStream, ByteBuffer, byte[])}. */
	public static void copyStream (InputStream input, ByteBuffer output, int bufferSize) throws IOException {
		copyStream(input, output, new byte[bufferSize]);
	}

	/** Copy the data from an {@link InputStream} to a {@link ByteBuffer}, using the specified byte[] as a temporary buffer. The
	 * buffer's limit is increased by the number of bytes copied, the position is left unchanged. The stream is not closed.
	 * @param output Must be a direct Buffer with native byte order and the buffer MUST be large enough to hold all the bytes in
	 *           the stream. No error checking is performed.
	 * @return the number of bytes copied. */
	public static int copyStream (InputStream input, ByteBuffer output, byte[] buffer) throws IOException {
		int startPosition = output.position(), total = 0, bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			BufferUtils.copy(buffer, 0, output, bytesRead);
			total += bytesRead;
			output.position(startPosition + total);
		}
		output.position(startPosition);
		return total;
	}

	/** Copy the data from an {@link InputStream} to a byte array. The stream is not closed. */
	public static byte[] copyStreamToByteArray (InputStream input) throws IOException {
		return copyStreamToByteArray(input, input.available());
	}

	/** Copy the data from an {@link InputStream} to a byte array. The stream is not closed.
	 * @param estimatedSize Used to allocate the output byte[] to possibly avoid an array copy. */
	public static byte[] copyStreamToByteArray (InputStream input, int estimatedSize) throws IOException {
		ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(Math.max(0, estimatedSize));
		copyStream(input, baos);
		return baos.toByteArray();
	}

	/** Calls {@link #copyStreamToString(InputStream, int, String)} using the input's {@link InputStream#available() available} size
	 * and the platform's default charset. */
	public static String copyStreamToString (InputStream input) throws IOException {
		return copyStreamToString(input, input.available(), null);
	}

	/** Calls {@link #copyStreamToString(InputStream, int, String)} using the platform's default charset. */
	public static String copyStreamToString (InputStream input, int estimatedSize) throws IOException {
		return copyStreamToString(input, estimatedSize, null);
	}

	/** Copy the data from an {@link InputStream} to a string using the specified charset.
	 * @param estimatedSize Used to allocate the output buffer to possibly avoid an array copy.
	 * @param charset May be null to use the platform's default charset. */
	public static String copyStreamToString (InputStream input, int estimatedSize, String charset) throws IOException {
		InputStreamReader reader = charset == null ? new InputStreamReader(input) : new InputStreamReader(input, charset);
		StringWriter writer = new StringWriter(Math.max(0, estimatedSize));
		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		int charsRead;
		while ((charsRead = reader.read(buffer)) != -1) {
			writer.write(buffer, 0, charsRead);
		}
		return writer.toString();
	}

	/** Close and ignore all errors. */
	public static void closeQuietly (Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (Exception ignored) {
			}
		}
	}

	/** A ByteArrayOutputStream which avoids copying of the byte array if possible. */
	static public class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
		public OptimizedByteArrayOutputStream (int initialSize) {
			super(initialSize);
		}

		@Override
		public synchronized byte[] toByteArray () {
			if (count == buf.length) return buf;
			return super.toByteArray();
		}

		public byte[] getBuffer () {
			return buf;
		}
	}
}
