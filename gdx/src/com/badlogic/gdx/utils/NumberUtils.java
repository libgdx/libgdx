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
	/** When false, the mask in {@link #intToFloatColor(int)} will not be used. This must only be used when the resulting floats
	 * will not be used with {@link #floatToIntColor(float)}. */
	static public boolean intToFloatColorMask = true;

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
		return Float.intBitsToFloat(intToFloatColorMask ? (value & 0xfeffffff) : value);
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
}
