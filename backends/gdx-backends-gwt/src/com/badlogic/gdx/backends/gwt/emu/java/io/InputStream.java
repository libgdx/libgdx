/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.io;

public abstract class InputStream implements Closeable {
	public abstract int read () throws IOException;

	public int read (byte[] buf, int start, int len) throws IOException {

		int end = start + len;
		for (int i = start; i < end; i++) {
			int r = read();
			if (r == -1) {
				return i == start ? -1 : i - start;
			}
			buf[i] = (byte)r;
		}
		return len;
	}

	public int read (byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	public void close () throws IOException {

	}

	public int available () {
		return 0;
	}
}
