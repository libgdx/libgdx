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

package com.badlogic.gdx.utils;

public class NumberUtils {
	public static int floatToIntBits (float value) {
		return Float.floatToIntBits(value);
	}

	public static int floatToRawIntBits (float value) {
		return Float.floatToRawIntBits(value);
	}

	public static int floatToIntColor (float value) {
		return Float.floatToRawIntBits(value);
	}

	/** Encodes the ABGR int color as a float. The high bits are masked to avoid using floats in the NaN range, which unfortunately
	 * means the full range of alpha cannot be used. See {@link Float#intBitsToFloat(int)} javadocs. */
	public static float intToFloatColor (int value) {
		return Float.intBitsToFloat(value & 0xfeffffff);
	}

	public static float intBitsToFloat (int value) {
		return Float.intBitsToFloat(value);
	}

	public static long doubleToLongBits (double value) {
		return Double.doubleToLongBits(value);
	}

	public static double longBitsToDouble (long value) {
		return Double.longBitsToDouble(value);
	}
	
	private static final char[] chars = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

	/** zero-allocation int to CharSequence conversion. clears the given StringBuilder and appends chars to it that represent the
	 * given int, then returns the same StringBuilder */
	public static StringBuilder intToText (int n, StringBuilder sb) {
		sb.setLength(0);
		if (n == Integer.MIN_VALUE) return sb.append("-2147483648");
		if (n < 0) {
			sb.append('-');
			n = Math.abs(n);
		}
		if (n >= 1000000000) sb.append(chars[(int)((long)n % 10000000000L / 1000000000L)]);
		if (n >= 100000000) sb.append(chars[n % 1000000000 / 100000000]);
		if (n >= 10000000) sb.append(chars[n % 100000000 / 10000000]);
		if (n >= 1000000) sb.append(chars[n % 10000000 / 1000000]);
		if (n >= 100000) sb.append(chars[n % 1000000 / 100000]);
		if (n >= 10000) sb.append(chars[n % 100000 / 10000]);
		if (n >= 1000) sb.append(chars[n % 10000 / 1000]);
		if (n >= 100) sb.append(chars[n % 1000 / 100]);
		if (n >= 10) sb.append(chars[n % 100 / 10]);
		sb.append(chars[n % 10]);
		return sb;
	}
}
