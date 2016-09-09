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

package com.google.gwt.corp.compatibility;

import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;

public class Numbers {

	static final double LN2 = Math.log(2);

	public static final int floatToIntBits (float f) {
		wfa.set(0, f);
		return wia.get(0);

// if (Float.isNaN(f)) {
// return 0x7f800001;
// }
// int signBit;
// if (f == 0) {
// return (1/f == Float.NEGATIVE_INFINITY) ? 0x80000000 : 0;
// } else if (f < 0) {
// f = -f;
// signBit = 0x80000000;
// } else {
// signBit = 0;
// }
// if (f == Float.POSITIVE_INFINITY) {
// return signBit | 0x7f800000;
// }
//
// int exponent = (int) (Math.log(f) / LN2);
// if (exponent < -126) {
// exponent = -126;
// }
// int significand = (int) (0.5 + f * Math.exp(-(exponent - 23) * LN2));
//
// // Handle exponent rounding issues & denorm
// if ((significand & 0x01000000) != 0) {
// significand >>= 1;
// exponent++;
// } else if ((significand & 0x00800000) == 0) {
// if (exponent == -126) {
// return signBit | significand;
// } else {
// significand <<= 1;
// exponent--;
// }
// }
//
// return signBit | ((exponent + 127) << 23) | (significand & 0x007fffff);
	}

	static Int8Array wba = Int8ArrayNative.create(4);
	static Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 1);
	static Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 1);

	public static final float intBitsToFloat (int i) {
// wba.set(0, (byte) (i >> 24));
// wba.set(1, (byte) (i >> 16));
// wba.set(2, (byte) (i >> 8));
// wba.set(3, (byte) (i));
		wia.set(0, i);
		return wfa.get(0);
//
//
// int exponent = (i >>> 23) & 255;
// int significand = i & 0x007fffff;
// float result;
// if (exponent == 0) {
// result = (float) (Math.exp((-126 - 23) * LN2) * significand);
// } else if (exponent == 255) {
// result = significand == 0 ? Float.POSITIVE_INFINITY : Float.NaN;
// } else {
// result = (float) (Math.exp((exponent - 127 - 23) * LN2) * (0x00800000 | significand));
// }
//
// return (i & 0x80000000) == 0 ? result : -result;
	}

	public static final long doubleToLongBits (Double d) {
		throw new RuntimeException("NYI");
	}

	public static final double longBitsToDouble (long l) {
		throw new RuntimeException("NYI");
	}

	public static long doubleToRawLongBits (double value) {
		throw new RuntimeException("NYI: Numbers.doubleToRawLongBits");
	}
}
