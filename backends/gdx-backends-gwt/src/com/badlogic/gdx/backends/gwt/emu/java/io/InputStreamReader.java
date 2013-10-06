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

 /* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

import avian.Utf8;

public class InputStreamReader extends Reader {
	private final InputStream in;

	public InputStreamReader (InputStream in) {
		this.in = in;
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

		if (c <= 0) return c;

		char[] buffer16 = Utf8.decode16(buffer, 0, c);

		System.arraycopy(buffer16, 0, b, offset, buffer16.length);

		return buffer16.length;
	}

	public void close () throws IOException {
		in.close();
	}
}
