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
package com.badlogic.gdx.backends.ios;

import java.io.IOException;
import java.io.InputStream;

import cli.System.IO.Stream;

public class IOSStreamInput extends InputStream {

	/** The stream for I/O operation. */
	private Stream stream;
	/** True if we can close the stream here. */
	private boolean closeable;
	
	
	public IOSStreamInput(Stream stream) {
		this(stream, true);
	}
	
	public IOSStreamInput(Stream stream, boolean closeable) {
		this.stream = stream;
		this.closeable = closeable;
	}
	
	@Override
	public int available () throws IOException {
		// NOTE: not supported!
		return 0;
	}

	@Override
	public synchronized void mark (int readLimit) {
		// not implemented
	}

	@Override
	public boolean markSupported () {
		return false;
	}

	@Override
	public synchronized void reset () throws IOException {
		// not implemented
	}

	@Override
	public int read () throws IOException {
		return stream.ReadByte();
	}

	@Override
	public int read (byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read (byte[] b, int off, int len) throws IOException {
		return stream.Read(b, off, len);
	}

	@Override
	public void close () throws IOException {
		if (closeable) {
			stream.Close();
			stream = null;
		}
	}
}