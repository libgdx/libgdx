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

public class OutputStreamWriter extends Writer {
	private final OutputStream out;

	public OutputStreamWriter (OutputStream out, String encoding) {
		this(out);
	}

	public OutputStreamWriter (OutputStream out) {
		this.out = out;
	}

	public void write (char[] b, int offset, int length) throws IOException {
		out.write(Utf8.encode(b, offset, length));
	}

	public void flush () throws IOException {
		out.flush();
	}

	public void close () throws IOException {
		out.close();
	}
}
