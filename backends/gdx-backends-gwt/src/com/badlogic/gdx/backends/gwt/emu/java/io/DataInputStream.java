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

import com.google.gwt.corp.compatibility.Numbers;

public class DataInputStream extends InputStream implements DataInput {

	private final InputStream is;

	public DataInputStream (final InputStream is) {
		this.is = is;
	}

	@Override
	public int read () throws IOException {
		return is.read();
	}

	public boolean readBoolean () throws IOException {
		return readByte() != 0;
	}

	public byte readByte () throws IOException {
		int i = read();
		if (i == -1) {
			throw new EOFException();
		}
		return (byte)i;
	}

	public char readChar () throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return (char)((a << 8) | b);
	}

	public double readDouble () throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public float readFloat () throws IOException {
		return Numbers.intBitsToFloat(readInt());
	}

	public void readFully (byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public void readFully (byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int count = is.read(b, off, len);
			if (count <= 0) {
				throw new EOFException();
			}
			off += count;
			len -= count;
		}
	}

	public int readInt () throws IOException {
		int a = is.read();
		int b = is.read();
		int c = is.read();
		int d = readUnsignedByte();
		return (a << 24) | (b << 16) | (c << 8) | d;
	}

	public String readLine () throws IOException {
		throw new RuntimeException("readline NYI");
	}

	public long readLong () throws IOException {
		long a = readInt();
		long b = readInt() & 0x0ffffffff;
		return (a << 32) | b;
	}

	public short readShort () throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return (short)((a << 8) | b);
	}

	public String readUTF () throws IOException {
		int bytes = readUnsignedShort();
		StringBuilder sb = new StringBuilder();

		while (bytes > 0) {
			bytes -= readUtfChar(sb);
		}

		return sb.toString();
	}

	private int readUtfChar (StringBuilder sb) throws IOException {
		int a = readUnsignedByte();
		if ((a & 0x80) == 0) {
			sb.append((char)a);
			return 1;
		}
		if ((a & 0xe0) == 0xc0) {
			int b = readUnsignedByte();
			sb.append((char)(((a & 0x1F) << 6) | (b & 0x3F)));
			return 2;
		}
		if ((a & 0xf0) == 0xe0) {
			int b = readUnsignedByte();
			int c = readUnsignedByte();
			sb.append((char)(((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F)));
			return 3;
		}
		throw new UTFDataFormatException();
	}

	public int readUnsignedByte () throws IOException {
		int i = read();
		if (i == -1) {
			throw new EOFException();
		}
		return i;
	}

	public int readUnsignedShort () throws IOException {
		int a = is.read();
		int b = readUnsignedByte();
		return ((a << 8) | b);
	}

	public int skipBytes (int n) throws IOException {
		// note: This is actually a valid implementation of this method, rendering it quite useless...
		return 0;
	}

	@Override
	public int available () {
		return is.available();
	}
	
}
