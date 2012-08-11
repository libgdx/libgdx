/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;

/*** The base class for all readers. A reader is a means of reading data from a source in a character-wise manner. Some readers also
 * support marking a position in the input and returning to this position later.
 * <p>
 * This abstract class does not provide a fully working implementation, so it needs to be subclassed, and at least the
 * {@link #read(char[], int, int)} and {@link #close()} methods needs to be overridden. Overriding some of the non-abstract
 * methods is also often advised, since it might result in higher efficiency.
 * <p>
 * Many specialized readers for purposes like reading from a file already exist in this package.
 * 
 * @see Writer */
public abstract class Reader implements Readable, Closeable {
	/*** The object used to synchronize access to the reader. */
	protected Object lock;

	/*** Constructs a new {@code Reader} with {@code this} as the object used to synchronize critical sections. */
	protected Reader () {
		super();
		lock = this;
	}

	/*** Constructs a new {@code Reader} with {@code lock} used to synchronize critical sections.
	 * 
	 * @param lock the {@code Object} used to synchronize critical sections.
	 * @throws NullPointerException if {@code lock} is {@code null}. */
	protected Reader (Object lock) {
		if (lock == null) {
			throw new NullPointerException();
		}
		this.lock = lock;
	}

	/*** Closes this reader. Implementations of this method should free any resources associated with the reader.
	 * 
	 * @throws IOException if an error occurs while closing this reader. */
	public abstract void close () throws IOException;

	/*** Sets a mark position in this reader. The parameter {@code readLimit} indicates how many characters can be read before the
	 * mark is invalidated. Calling {@code reset()} will reposition the reader back to the marked position if {@code readLimit} has
	 * not been surpassed.
	 * <p>
	 * This default implementation simply throws an {@code IOException}; subclasses must provide their own implementation.
	 * 
	 * @param readLimit the number of characters that can be read before the mark is invalidated.
	 * @throws IllegalArgumentException if {@code readLimit < 0}.
	 * @throws IOException if an error occurs while setting a mark in this reader.
	 * @see #markSupported()
	 * @see #reset() */
	public void mark (int readLimit) throws IOException {
		throw new IOException();
	}

	/*** Indicates whether this reader supports the {@code mark()} and {@code reset()} methods. This default implementation returns
	 * {@code false}.
	 * 
	 * @return always {@code false}. */
	public boolean markSupported () {
		return false;
	}

	/*** Reads a single character from this reader and returns it as an integer with the two higher-order bytes set to 0. Returns -1
	 * if the end of the reader has been reached.
	 * 
	 * @return the character read or -1 if the end of the reader has been reached.
	 * @throws IOException if this reader is closed or some other I/O error occurs. */
	public int read () throws IOException {
		synchronized (lock) {
			char charArray[] = new char[1];
			if (read(charArray, 0, 1) != -1) {
				return charArray[0];
			}
			return -1;
		}
	}

	/*** Reads characters from this reader and stores them in the character array {@code buf} starting at offset 0. Returns the
	 * number of characters actually read or -1 if the end of the reader has been reached.
	 * 
	 * @param buf character array to store the characters read.
	 * @return the number of characters read or -1 if the end of the reader has been reached.
	 * @throws IOException if this reader is closed or some other I/O error occurs. */
	public int read (char buf[]) throws IOException {
		return read(buf, 0, buf.length);
	}

	/*** Reads at most {@code count} characters from this reader and stores them at {@code offset} in the character array {@code buf}
	 * . Returns the number of characters actually read or -1 if the end of the reader has been reached.
	 * 
	 * @param buf the character array to store the characters read.
	 * @param offset the initial position in {@code buffer} to store the characters read from this reader.
	 * @param count the maximum number of characters to read.
	 * @return the number of characters read or -1 if the end of the reader has been reached.
	 * @throws IOException if this reader is closed or some other I/O error occurs. */
	public abstract int read (char buf[], int offset, int count) throws IOException;

	/*** Indicates whether this reader is ready to be read without blocking. Returns {@code true} if this reader will not block when
	 * {@code read} is called, {@code false} if unknown or blocking will occur. This default implementation always returns
	 * {@code false}.
	 * 
	 * @return always {@code false}.
	 * @throws IOException if this reader is closed or some other I/O error occurs.
	 * @see #read()
	 * @see #read(char[])
	 * @see #read(char[], int, int) */
	public boolean ready () throws IOException {
		return false;
	}

	/*** Resets this reader's position to the last {@code mark()} location. Invocations of {@code read()} and {@code skip()} will
	 * occur from this new location. If this reader has not been marked, the behavior of {@code reset()} is implementation
	 * specific. This default implementation throws an {@code IOException}.
	 * 
	 * @throws IOException always thrown in this default implementation.
	 * @see #mark(int)
	 * @see #markSupported() */
	public void reset () throws IOException {
		throw new IOException();
	}

	/*** Skips {@code amount} characters in this reader. Subsequent calls of {@code read} methods will not return these characters
	 * unless {@code reset()} is used. This method may perform multiple reads to read {@code count} characters.
	 * 
	 * @param count the maximum number of characters to skip.
	 * @return the number of characters actually skipped.
	 * @throws IllegalArgumentException if {@code amount < 0}.
	 * @throws IOException if this reader is closed or some other I/O error occurs.
	 * @see #mark(int)
	 * @see #markSupported()
	 * @see #reset() */
	public long skip (long count) throws IOException {
		if (count < 0) {
			throw new IllegalArgumentException();
		}
		synchronized (lock) {
			long skipped = 0;
			int toRead = count < 512 ? (int)count : 512;
			char charsSkipped[] = new char[toRead];
			while (skipped < count) {
				int read = read(charsSkipped, 0, toRead);
				if (read == -1) {
					return skipped;
				}
				skipped += read;
				if (read < toRead) {
					return skipped;
				}
				if (count - skipped < toRead) {
					toRead = (int)(count - skipped);
				}
			}
			return skipped;
		}
	}

	/*** Reads characters and puts them into the {@code target} character buffer.
	 * 
	 * @param target the destination character buffer.
	 * @return the number of characters put into {@code target} or -1 if the end of this reader has been reached before a character
	 *         has been read.
	 * @throws IOException if any I/O error occurs while reading from this reader.
	 * @throws NullPointerException if {@code target} is {@code null}.
	 * @throws ReadOnlyBufferException if {@code target} is read-only. */
	public int read (CharBuffer target) throws IOException {
		if (null == target) {
			throw new NullPointerException();
		}
		int length = target.length();
		char[] buf = new char[length];
		length = Math.min(length, read(buf));
		if (length > 0) {
			target.put(buf, 0, length);
		}
		return length;
	}
}
