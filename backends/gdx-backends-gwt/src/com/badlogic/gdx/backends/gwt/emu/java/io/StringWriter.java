/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class StringWriter extends Writer {
	private final StringBuilder out;

	public StringWriter() {
		out = new StringBuilder();
	}
	
	public StringWriter(int initialCapacity) {
		out = new StringBuilder(initialCapacity);
	}
	
	public void write (char[] b, int offset, int length) throws IOException {
		out.append(b, offset, length);
	}

	public String toString () {
		return out.toString();
	}

	public void flush () throws IOException {
	}

	public void close () throws IOException {
	}
}
