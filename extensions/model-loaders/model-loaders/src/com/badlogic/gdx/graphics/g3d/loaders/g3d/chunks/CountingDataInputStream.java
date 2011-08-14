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
package com.badlogic.gdx.graphics.g3d.loaders.g3d.chunks;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CountingDataInputStream implements DataInput {
	int readBytes = 0;
	DataInputStream in;

	public CountingDataInputStream (InputStream in) {
		this.in = new DataInputStream(in);
	}

	public int getReadBytes () {
		return readBytes;
	}

	@Override
	public void readFully (byte[] b) throws IOException {
		readBytes += b.length;
		in.readFully(b);
	}

	@Override
	public void readFully (byte[] b, int off, int len) throws IOException {
		readBytes += len;
		in.readFully(b, off, len);
	}

	@Override
	public int skipBytes (int n) throws IOException {
		int skipped = in.skipBytes(n);
		readBytes += skipped;
		return skipped;
	}

	@Override
	public boolean readBoolean () throws IOException {
		readBytes += 1;
		return in.readBoolean();
	}

	@Override
	public byte readByte () throws IOException {
		readBytes += 1;
		return in.readByte();
	}

	@Override
	public int readUnsignedByte () throws IOException {
		readBytes += 1;
		return in.readUnsignedByte();
	}

	@Override
	public short readShort () throws IOException {
		readBytes += 2;
		return in.readShort();
	}

	@Override
	public int readUnsignedShort () throws IOException {
		readBytes += 2;
		return in.readUnsignedShort();
	}

	@Override
	public char readChar () throws IOException {
		readBytes += 2;
		return in.readChar();
	}

	@Override
	public int readInt () throws IOException {
		readBytes += 4;
		return in.readInt();
	}

	@Override
	public long readLong () throws IOException {
		readBytes += 8;
		return in.readLong();
	}

	@Override
	public float readFloat () throws IOException {
		readBytes += 4;
		return in.readFloat();
	}

	@Override
	public double readDouble () throws IOException {
		readBytes += 8;
		return in.readDouble();
	}

	@Override
	public String readLine () throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public String readUTF () throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}
}