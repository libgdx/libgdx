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

public class ByteArrayOutputStream extends OutputStream {

	protected int count;
	protected byte[] buf;

	public ByteArrayOutputStream () {
		this(16);
	}

	public ByteArrayOutputStream (int initialSize) {
		buf = new byte[initialSize];
	}

	@Override
	public void write (int b) {
		if (buf.length == count) {
			byte[] newBuf = new byte[buf.length * 3 / 2];
			System.arraycopy(buf, 0, newBuf, 0, count);
			buf = newBuf;
		}

		buf[count++] = (byte)b;
	}

	public byte[] toByteArray () {
		byte[] result = new byte[count];
		System.arraycopy(buf, 0, result, 0, count);
		return result;
	}

	public int size () {
		return count;
	}

	public String toString () {
		return new String(buf, 0, count);
	}

	public String toString (String enc) throws UnsupportedEncodingException {
		return new String(buf, 0, count, enc);
	}
}
