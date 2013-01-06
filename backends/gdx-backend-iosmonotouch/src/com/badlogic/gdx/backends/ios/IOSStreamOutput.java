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
import java.io.OutputStream;

import cli.System.IO.Stream;

public class IOSStreamOutput extends OutputStream {

	/** The stream for I/O operation. */
	private Stream stream;
	/** True if we can close the stream here. */
	private boolean closeable;
	
	
	public IOSStreamOutput(Stream stream) {
		this(stream, true);
	}
	
	public IOSStreamOutput(Stream stream, boolean closeable) {
		this.stream = stream;
		this.closeable = closeable;
	}

	@Override
	public void write (byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write (byte[] b, int off, int len) throws IOException {
		stream.Write(b, off, len);
	}

	@Override
	public void write (int b) throws IOException {
		stream.WriteByte((byte)(b & 0xff));
	}
	
   @Override
	public void flush () throws IOException {
   	// NOTE: has no effect on iOS!
		stream.Flush();
	}

	@Override
	public void close () throws IOException {
		if (closeable) {
			stream.Close();
			stream = null;
		}
	}
}