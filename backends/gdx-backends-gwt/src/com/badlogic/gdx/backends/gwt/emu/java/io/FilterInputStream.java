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

package java.io;

public class FilterInputStream extends InputStream {
	protected InputStream in;

	protected FilterInputStream (InputStream in) {
		this.in = in;
	}

	public int read () throws IOException {
		return in.read();
	}

	public int read (byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read (byte b[], int off, int len) throws IOException {
		return in.read(b, off, len);
	}

	public long skip (long n) throws IOException {
		return 0;
	}

	public int available () {
		return in.available();
	}

	public void close () throws IOException {
		in.close();
	}

	public synchronized void mark (int readlimit) {
	}

	public synchronized void reset () throws IOException {
	}

	public boolean markSupported () {
		return false;
	}
}
