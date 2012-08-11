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

public abstract class OutputStream {
	// not abstract because of some gwt strangeness
	public void write (int b) throws IOException {
	}

	public void write (byte[] ba) throws IOException {
		write(ba, 0, ba.length);
	}

	public void write (byte[] ba, int start, int len) throws IOException {
		int end = start + len;
		for (int i = start; i < end; i++) {
			write(ba[i]);
		}
	}

	public void flush () {
	}

	public void close () throws IOException {
	}
}
