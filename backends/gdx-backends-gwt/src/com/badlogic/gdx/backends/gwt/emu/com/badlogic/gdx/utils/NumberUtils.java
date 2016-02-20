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

import com.google.gwt.corp.compatibility.Numbers;

public final class NumberUtils {

	public static int floatToIntBits (float value) {
		return Numbers.floatToIntBits(value);
	}

	public static int floatToRawIntBits (float value) {
		return Numbers.floatToIntBits(value);
	}

	public static int floatToIntColor (float value) {
		return Numbers.floatToIntBits(value);
	}

	public static float intToFloatColor (int value) {
		// This mask avoids using bits in the NaN range. See Float.intBitsToFloat javadocs.
		// This unfortunately means we don't get the full range of alpha.
		return Numbers.intBitsToFloat(value & 0xfeffffff);
	}

	public static float intBitsToFloat (int value) {
		return Numbers.intBitsToFloat(value);
	}

	public static long doubleToLongBits (double value) {
		return 0; // FIXME
	}

	public static double longBitsToDouble (long value) {
		return 0; // FIXME
	}
}
