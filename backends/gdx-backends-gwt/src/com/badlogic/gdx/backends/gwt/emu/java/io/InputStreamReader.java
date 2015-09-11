/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import com.badlogic.gdx.utils.Utf8Decoder;

public class InputStreamReader extends Reader {
	private final InputStream in;

	private final Utf8Decoder utf8Decoder;

	public InputStreamReader (InputStream in) {
		this.in = in;
		this.utf8Decoder = new Utf8Decoder();
	}

	public InputStreamReader (InputStream in, String encoding) throws UnsupportedEncodingException {
		this(in);

		// FIXME this is bad, but some APIs seem to use "ISO-8859-1", fuckers...
// if (! encoding.equals("UTF-8")) {
// throw new UnsupportedEncodingException(encoding);
// }
	}

	public int read (char[] b, int offset, int length) throws IOException {
		byte[] buffer = new byte[length];
		int c = in.read(buffer);
		return c <= 0 ? c : utf8Decoder.decode(buffer, 0, c, b, offset);
	}

	public void close () throws IOException {
		in.close();
	}
}
