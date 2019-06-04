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

public final class NumberUtils {
	public static int floatToIntBits (float value) {
		return Float.floatToIntBits(value);
	}

	public static int floatToRawIntBits (float value) {
		return Float.floatToRawIntBits(value);
	}

	/** Converts the color from a float ABGR encoding to an int ABGR encoding. The alpha is expanded from 0-254 in the float
	 * encoding (see {@link #intToFloatColor(int)}) to 0-255, which means converting from int to float and back to int can be
	 * lossy. */
	public static int floatToIntColor (float value) {
		int intBits = Float.floatToRawIntBits(value);
		intBits |= (int)((intBits >>> 24) * (255f / 254f)) << 24;
		return intBits;
	}

	/** Encodes the ABGR int color as a float. The alpha is compressed to 0-254 to avoid using bits in the NaN range (see
	 * {@link Float#intBitsToFloat(int)} javadocs). Rendering which uses colors encoded as floats should expand the 0-254 back to
	 * 0-255. */
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
}
