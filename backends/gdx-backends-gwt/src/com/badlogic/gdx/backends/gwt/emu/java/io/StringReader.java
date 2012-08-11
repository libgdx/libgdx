/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class StringReader extends Reader {
	private final String in;
	private int position = 0;

	public StringReader (String in) {
		this.in = in;
	}

	public int read (char[] b, int offset, int length) throws IOException {
		if (length > in.length() - position) {
			length = in.length() - position;
			if (length <= 0) {
				return -1;
			}
		}
		in.getChars(position, position + length, b, offset);
		position += length;
		return length;
	}

	public void close () throws IOException {
	}
}
