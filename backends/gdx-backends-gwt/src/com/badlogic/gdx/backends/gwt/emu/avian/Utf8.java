/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.io.ByteArrayOutputStream;

public class Utf8 {
	public static boolean test (Object data) {
		if (!(data instanceof byte[])) return false;
		byte[] b = (byte[])data;
		for (int i = 0; i < b.length; ++i) {
			if (((int)b[i] & 0x080) != 0) return true;
		}
		return false;
	}

	public static byte[] encode (char[] s16, int offset, int length) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		for (int i = offset; i < offset + length; ++i) {
			char c = s16[i];
			if (c == '\u0000') { // null char
				buf.write(0);
				buf.write(0);
			} else if (c < 0x080) { // 1 byte char
				buf.write(c);
			} else if (c < 0x0800) { // 2 byte char
				buf.write(0x0c0 | (c >>> 6));
				buf.write(0x080 | (c & 0x03f));
			} else { // 3 byte char
				buf.write(0x0e0 | ((c >>> 12) & 0x0f));
				buf.write(0x080 | ((c >>> 6) & 0x03f));
				buf.write(0x080 | (c & 0x03f));
			}
		}
		return buf.toByteArray();
	}

	public static Object decode (byte[] s8, int offset, int length) {
		Object buf = new byte[length];
		boolean isMultiByte = false;
		int i = offset, j = 0;
		while (i < offset + length) {
			int x = s8[i++];
			if ((x & 0x080) == 0x0) { // 1 byte char
				if (x == 0) ++i; // 2 byte null char
				cram(buf, j++, x);
			} else if ((x & 0x0e0) == 0x0c0) { // 2 byte char
				if (!isMultiByte) {
					buf = widen(buf, j, length - 1);
					isMultiByte = true;
				}
				int y = s8[i++];
				cram(buf, j++, ((x & 0x1f) << 6) | (y & 0x3f));
			} else if ((x & 0x0f0) == 0x0e0) { // 3 byte char
				if (!isMultiByte) {
					buf = widen(buf, j, length - 2);
					isMultiByte = true;
				}
				int y = s8[i++];
				int z = s8[i++];
				cram(buf, j++, ((x & 0xf) << 12) | ((y & 0x3f) << 6) | (z & 0x3f));
			}
		}

		return trim(buf, j);
	}

	public static char[] decode16 (byte[] s8, int offset, int length) {
		Object decoded = decode(s8, offset, length);
		if (decoded instanceof char[]) return (char[])decoded;
		return (char[])widen(decoded, length, length);
	}

	private static void cram (Object data, int index, int val) {
		if (data instanceof byte[])
			((byte[])data)[index] = (byte)val;
		else
			((char[])data)[index] = (char)val;
	}

	private static Object widen (Object data, int length, int capacity) {
		byte[] src = (byte[])data;
		char[] result = new char[capacity];
		for (int i = 0; i < length; ++i)
			result[i] = (char)((int)src[i] & 0x0ff);
		return result;
	}

	private static Object trim (Object data, int length) {
		if (data instanceof byte[]) return data;
		if (((char[])data).length == length) return data;
		char[] result = new char[length];
		System.arraycopy(data, 0, result, 0, length);
		return result;
	}
}
