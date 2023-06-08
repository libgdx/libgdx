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

package com.badlogic.gdx.math;

import java.util.Random;

/** Utility and fast math functions.
 * <p>
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/floor/ceil.
 * @author Nathan Sweet */
public final class MathUtils {

	private MathUtils () {
	}

	static public final float nanoToSec = 1 / 1000000000f;

	// ---
	static public final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits
	static public final float PI = (float)Math.PI;
	static public final float PI2 = PI * 2;
	static public final float HALF_PI = PI / 2;

	static public final float E = (float)Math.E;

	static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
	static private final int SIN_MASK = ~(-1 << SIN_BITS);
	static private final int SIN_COUNT = SIN_MASK + 1;

	static private final float radFull = PI2;
	static private final float degFull = 360;
	static private final float radToIndex = SIN_COUNT / radFull;
	static private final float degToIndex = SIN_COUNT / degFull;

	/** multiply by this to convert from radians to degrees */
	static public final float radiansToDegrees = 180f / PI;
	static public final float radDeg = radiansToDegrees;
	/** multiply by this to convert from degrees to radians */
	static public final float degreesToRadians = PI / 180;
	static public final float degRad = degreesToRadians;

	static private class Sin {
		static final float[] table = new float[SIN_COUNT];

		static {
			for (int i = 0; i < SIN_COUNT; i++)
				table[i] = (float)Math.sin((i + 0.5f) / SIN_COUNT * radFull);
			// The four right angles get extra-precise values, because they are
			// the most likely to need to be correct.
			table[0] = 0f;
			table[(int)(90 * degToIndex) & SIN_MASK] = 1f;
			table[(int)(180 * degToIndex) & SIN_MASK] = 0f;
			table[(int)(270 * degToIndex) & SIN_MASK] = -1f;
		}
	}

	/** Returns the sine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
	 * inclusive). */
	static public float sin (float radians) {
		return Sin.table[(int)(radians * radToIndex) & SIN_MASK];
	}

	/** Returns the cosine in radians from a lookup table. For optimal precision, use radians between -PI2 and PI2 (both
	 * inclusive). */
	static public float cos (float radians) {
		return Sin.table[(int)((radians + HALF_PI) * radToIndex) & SIN_MASK];
	}

	/** Returns the sine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
	 * inclusive). */
	static public float sinDeg (float degrees) {
		return Sin.table[(int)(degrees * degToIndex) & SIN_MASK];
	}

	/** Returns the cosine in degrees from a lookup table. For optimal precision, use degrees between -360 and 360 (both
	 * inclusive). */
	static public float cosDeg (float degrees) {
		return Sin.table[(int)((degrees + 90) * degToIndex) & SIN_MASK];
	}

	/** Returns the tangent given an input in radians, using a Padé approximant. <br>
	 * Padé approximants tend to be most accurate when they aren't producing results of extreme magnitude; in the tan() function,
	 * those results occur on and near odd multiples of {@code PI/2}, and this method is least accurate when given inputs near
	 * those multiples. <br>
	 * For inputs between -1.57 to 1.57 (just inside half-pi), separated by 0x1p-20f, absolute error is 0.00890192, relative error
	 * is 0.00000090, and the maximum error is 17.98901367 when given 1.56999838. The maximum error might seem concerning, but it's
	 * the difference between the correct 1253.22167969 and the 1235.23266602 this returns, so for many purposes the difference
	 * won't be noticeable. <br>
	 * For inputs between -1.55 to 1.55 (getting less close to half-pi), separated by 0x1p-20f, absolute error is 0.00023368,
	 * relative error is -0.00000009, and the maximum error is 0.02355957 when given -1.54996467. The maximum error is the
	 * difference between the correct -47.99691010 and the -47.97335052 this returns. <br>
	 * While you don't have to use a dedicated method for tan(), and you can use {@code sin(x)/cos(x)}, approximating tan() in that
	 * way is very susceptible to error building up from any of sin(), cos() or the division. Where this tan() has a maximum error
	 * in the -1.55 to 1.55 range of 0.02355957, that simpler division technique on the same range has a maximum error of
	 * 1.25724030 (about 50 times worse), as well as larger absolute and relative errors. Casting the double result of
	 * {@link Math#tan(double)} to float will get the highest precision, but can be anywhere from 2.5x to nearly 4x slower than
	 * this, depending on JVM. <br>
	 * Based on <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer by Soonts</a>.
	 *
	 * @param radians a float angle in radians, where 0 to {@link #PI2} is one rotation
	 * @return a float approximation of tan() */
	public static float tan (float radians) {
		radians /= PI;
		radians += 0.5f;
		radians -= Math.floor(radians);
		radians -= 0.5f;
		radians *= PI;
		final float x2 = radians * radians, x4 = x2 * x2;
		return radians * ((0.0010582010582010583f) * x4 - (0.1111111111111111f) * x2 + 1f)
			/ ((0.015873015873015872f) * x4 - (0.4444444444444444f) * x2 + 1f);
		// How we calculated those long constants above (from Stack Exchange, by Soonts):
// return x * ((1.0/945.0) * x4 - (1.0/9.0) * x2 + 1.0) / ((1.0/63.0) * x4 - (4.0/9.0) * x2 + 1.0);
// Normally, it would be best to show the division steps, but if GWT isn't computing mathematical constants at
// compile-time, which I don't know if it does, that would make the shown-division way slower by 4 divisions.
	}

	/** Returns the tangent given an input in degrees, using a Padé approximant. Based on
	 * <a href="https://math.stackexchange.com/a/4453027">this Stack Exchange answer</a>.
	 *
	 * @param degrees an angle in degrees, where 0 to 360 is one rotation
	 * @return a float approximation of tan() */
	public static float tanDeg (float degrees) {
		degrees *= (1f / 180f);
		degrees += 0.5f;
		degrees -= Math.floor(degrees);
		degrees -= 0.5f;
		degrees *= PI;
		final float x2 = degrees * degrees, x4 = x2 * x2;
		return degrees * ((0.0010582010582010583f) * x4 - (0.1111111111111111f) * x2 + 1f)
			/ ((0.015873015873015872f) * x4 - (0.4444444444444444f) * x2 + 1f);
	}

	// ---

	/** A variant on {@link #atan(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
	 * parameter, but is otherwise the same as atan(float), and returns a float like that method. It uses the same approximation,
	 * from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
	 * {@link #atan2(float, float)}, but it may be a tiny bit faster than atan(float) in other code.
	 * @param i any finite double or float, but more commonly a float
	 * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive */
	public static float atanUnchecked (double i) {
		// We use double precision internally, because some constants need double precision.
		double n = Math.abs(i);
		// c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
		double c = (n - 1.0) / (n + 1.0);
		// The approximation needs 6 odd powers of c.
		double c2 = c * c;
		double c3 = c * c2;
		double c5 = c3 * c2;
		double c7 = c5 * c2;
		double c9 = c7 * c2;
		double c11 = c9 * c2;
		return (float)(Math.signum(i) * ((Math.PI * 0.25)
			+ (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11)));
	}

	/** Close approximation of the frequently-used trigonometric method atan2. Average error is 1.057E-6 radians; maximum error is
	 * 1.922E-6. Takes y and x (in that unusual order) as floats, and returns the angle from the origin to that point in radians.
	 * It is about 4 times faster than {@link Math#atan2(double, double)} (roughly 15 ns instead of roughly 60 ns for Math, on Java
	 * 8 HotSpot). <br>
	 * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
	 * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
	 * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
	 * translates to atan2().
	 * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @return the angle to the given point, in radians as a float; ranges from {@code -PI} to {@code PI} */
	public static float atan2 (final float y, float x) {
		float n = y / x;
		if (n != n)
			n = (y == x ? 1f : -1f); // if both y and x are infinite, n would be NaN
		else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
		if (x > 0)
			return atanUnchecked(n);
		else if (x < 0) {
			if (y >= 0) return atanUnchecked(n) + PI;
			return atanUnchecked(n) - PI;
		} else if (y > 0)
			return x + HALF_PI;
		else if (y < 0) return x - HALF_PI;
		return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
	}

	/** A variant on {@link #atanDeg(float)} that does not tolerate infinite inputs for speed reasons. This can be given a double
	 * parameter, but is otherwise the same as atanDeg(float), and returns a float like that method. It uses the same
	 * approximation, from sheet 11 of "Approximations for Digital Computers." This is mostly meant to be used inside
	 * {@link #atan2(float, float)}, but it may be a tiny bit faster than atanDeg(float) in other code.
	 * @param i any finite double or float, but more commonly a float
	 * @return an output from the inverse tangent function in degrees, from {@code -90} to {@code 90} inclusive */
	public static double atanUncheckedDeg (double i) {
		// We use double precision internally, because some constants need double precision.
		double n = Math.abs(i);
		// c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
		double c = (n - 1.0) / (n + 1.0);
		// The approximation needs 6 odd powers of c.
		double c2 = c * c;
		double c3 = c * c2;
		double c5 = c3 * c2;
		double c7 = c5 * c2;
		double c9 = c7 * c2;
		double c11 = c9 * c2;
		return (Math.signum(i) * (45.0 + (57.2944766070562 * c - 19.05792099799635 * c3 + 11.089223410359068 * c5
			- 6.6711120475953765 * c7 + 3.016813013351768 * c9 - 0.6715752908287405 * c11)));
	}

	/** Close approximation of the frequently-used trigonometric method atan2, using positive or negative degrees. Average absolute
	 * error is 0.00006037 degrees; relative error is 0 degrees, maximum error is 0.00010396 degrees. Takes y and x (in that
	 * unusual order) as floats, and returns the angle from the origin to that point in degrees. <br>
	 * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
	 * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
	 * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
	 * translates to atan2().
	 * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @return the angle to the given point, in degrees as a float; ranges from {@code -180} to {@code 180} */
	public static float atan2Deg (final float y, float x) {
		float n = y / x;
		if (n != n)
			n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
		else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
		if (x > 0)
			return (float)atanUncheckedDeg(n);
		else if (x < 0) {
			if (y >= 0) return (float)(atanUncheckedDeg(n) + 180.0);
			return (float)(atanUncheckedDeg(n) - 180.0);
		} else if (y > 0)
			return x + 90f;
		else if (y < 0) return x - 90f;
		return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
	}

	/** Close approximation of the frequently-used trigonometric method atan2, using non-negative degrees only. Average absolute
	 * error is 0.00006045 degrees; relative error is 0 degrees; maximum error is 0.00011178 degrees. Takes y and x (in that
	 * unusual order) as floats, and returns the angle from the origin to that point in degrees. <br>
	 * This can be useful when a negative result from atan() would require extra work to handle. <br>
	 * Credit for this goes to the 1955 research study "Approximations for Digital Computers," by RAND Corporation. This is sheet
	 * 11's algorithm, which is the fourth-fastest and fourth-least precise. The algorithms on sheets 8-10 are faster, but only by
	 * a very small degree, and are considerably less precise. That study provides an {@link #atan(float)} method, and that cleanly
	 * translates to atan2Deg360().
	 * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
	 * @return the angle to the given point, in degrees as a float; ranges from {@code 0} to {@code 360} */
	public static float atan2Deg360 (final float y, float x) {
		float n = y / x;
		if (n != n)
			n = (y == x ? 1f : -1.0f); // if both y and x are infinite, n would be NaN
		else if (n - n != n - n) x = 0f; // if n is infinite, y is infinitely larger than x.
		if (x > 0) {
			if (y >= 0)
				return (float)atanUncheckedDeg(n);
			else
				return (float)(atanUncheckedDeg(n) + 360.0);
		} else if (x < 0) {
			return (float)(atanUncheckedDeg(n) + 180.0);
		} else if (y > 0)
			return x + 90f;
		else if (y < 0) return x + 270f;
		return x + y; // returns 0 for 0,0 or NaN if either y or x is NaN
	}

	/** Returns acos in radians; less accurate than Math.acos but may be faster. Average error of 0.00002845 radians (0.0016300649
	 * degrees), largest error of 0.000067548 radians (0.0038702153 degrees). This implementation does not return NaN if given an
	 * out-of-range input (Math.acos does return NaN), unless the input is NaN.
	 * @param a acos is defined only when a is between -1f and 1f, inclusive
	 * @return between {@code 0} and {@code PI} when a is in the defined range */
	static public float acos (float a) {
		float a2 = a * a; // a squared
		float a3 = a * a2; // a cubed
		if (a >= 0f) {
			return (float)Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
		}
		return 3.14159265358979323846f
			- (float)Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3);
	}

	/** Returns asin in radians; less accurate than Math.asin but may be faster. Average error of 0.000028447 radians (0.0016298931
	 * degrees), largest error of 0.000067592 radians (0.0038727364 degrees). This implementation does not return NaN if given an
	 * out-of-range input (Math.asin does return NaN), unless the input is NaN.
	 * @param a asin is defined only when a is between -1f and 1f, inclusive
	 * @return between {@code -HALF_PI} and {@code HALF_PI} when a is in the defined range */
	static public float asin (float a) {
		float a2 = a * a; // a squared
		float a3 = a * a2; // a cubed
		if (a >= 0f) {
			return 1.5707963267948966f
				- (float)Math.sqrt(1f - a) * (1.5707288f - 0.2121144f * a + 0.0742610f * a2 - 0.0187293f * a3);
		}
		return -1.5707963267948966f + (float)Math.sqrt(1f + a) * (1.5707288f + 0.2121144f * a + 0.0742610f * a2 + 0.0187293f * a3);
	}

	/** Arc tangent approximation with very low error, using an algorithm from the 1955 research study "Approximations for Digital
	 * Computers," by RAND Corporation (this is sheet 11's algorithm, which is the fourth-fastest and fourth-least precise). This
	 * method is usually about 4x faster than {@link Math#atan(double)}, but is somewhat less precise than Math's implementation.
	 * For finite inputs only, you may get a tiny speedup by using {@link #atanUnchecked(double)}, but this method will be correct
	 * enough for infinite inputs, and atanUnchecked() will not be.
	 * @param i an input to the inverse tangent function; any float is accepted
	 * @return an output from the inverse tangent function, from {@code -HALF_PI} to {@code HALF_PI} inclusive
	 * @see #atanUnchecked(double) If you know the input will be finite, you can use atanUnchecked() instead. */
	public static float atan (float i) {
		// We use double precision internally, because some constants need double precision.
		// This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
		// again when converted back to float.
		double n = Math.min(Math.abs(i), Double.MAX_VALUE);
		// c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
		double c = (n - 1.0) / (n + 1.0);
		// The approximation needs 6 odd powers of c.
		double c2 = c * c;
		double c3 = c * c2;
		double c5 = c3 * c2;
		double c7 = c5 * c2;
		double c9 = c7 * c2;
		double c11 = c9 * c2;
		return Math.signum(i) * (float)((Math.PI * 0.25)
			+ (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11));
	}

	/** Returns arcsine in degrees. This implementation does not return NaN if given an out-of-range input (Math.asin does return
	 * NaN), unless the input is NaN.
	 * @param a asin is defined only when a is between -1f and 1f, inclusive
	 * @return between {@code -90} and {@code 90} when a is in the defined range */
	public static float asinDeg (float a) {
		float a2 = a * a; // a squared
		float a3 = a * a2; // a cubed
		if (a >= 0f) {
			return 90f - (float)Math.sqrt(1f - a)
				* (89.99613099964837f - 12.153259893949748f * a + 4.2548418824210055f * a2 - 1.0731098432343729f * a3);
		}
		return (float)Math.sqrt(1f + a)
			* (89.99613099964837f + 12.153259893949748f * a + 4.2548418824210055f * a2 + 1.0731098432343729f * a3) - 90f;
	}

	/** Returns arccosine in degrees. This implementation does not return NaN if given an out-of-range input (Math.acos does return
	 * NaN), unless the input is NaN.
	 * @param a acos is defined only when a is between -1f and 1f, inclusive
	 * @return between {@code 0} and {@code 180} when a is in the defined range */
	public static float acosDeg (float a) {
		float a2 = a * a; // a squared
		float a3 = a * a2; // a cubed
		if (a >= 0f) {
			return (float)Math.sqrt(1f - a)
				* (89.99613099964837f - 12.153259533621753f * a + 4.254842010910525f * a2 - 1.0731098035209208f * a3);
		}
		return 180f - (float)Math.sqrt(1f + a)
			* (89.99613099964837f + 12.153259533621753f * a + 4.254842010910525f * a2 + 1.0731098035209208f * a3);
	}

	/** Arc tangent approximation returning a value measured in positive or negative degrees, using an algorithm from the 1955
	 * research study "Approximations for Digital Computers," by RAND Corporation (this is sheet 11's algorithm, which is the
	 * fourth-fastest and fourth-least precise). For finite inputs only, you may get a tiny speedup by using
	 * {@link #atanUncheckedDeg(double)}, but this method will be correct enough for infinite inputs, and atanUnchecked() will not
	 * be.
	 * @param i an input to the inverse tangent function; any float is accepted
	 * @return an output from the inverse tangent function in degrees, from {@code -90} to {@code 90} inclusive
	 * @see #atanUncheckedDeg(double) If you know the input will be finite, you can use atanUncheckedDeg() instead. */
	public static float atanDeg (float i) {
		// We use double precision internally, because some constants need double precision.
		// This clips infinite inputs at Double.MAX_VALUE, which still probably becomes infinite
		// again when converted back to float.
		double n = Math.min(Math.abs(i), Double.MAX_VALUE);
		// c uses the "equally-good" formulation that permits n to be from 0 to almost infinity.
		double c = (n - 1.0) / (n + 1.0);
		// The approximation needs 6 odd powers of c.
		double c2 = c * c;
		double c3 = c * c2;
		double c5 = c3 * c2;
		double c7 = c5 * c2;
		double c9 = c7 * c2;
		double c11 = c9 * c2;
		return (float)(Math.signum(i) * (45.0 + (57.2944766070562 * c - 19.05792099799635 * c3 + 11.089223410359068 * c5
			- 6.6711120475953765 * c7 + 3.016813013351768 * c9 - 0.6715752908287405 * c11)));
	}

	// ---

	static public Random random = new RandomXS128();

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public int random (int range) {
		return random.nextInt(range + 1);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public int random (int start, int end) {
		return start + random.nextInt(end - start + 1);
	}

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public long random (long range) {
		// Uses the lower-bounded overload defined below, which is simpler and doesn't lose much optimization.
		return random(0L, range);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public long random (long start, long end) {
		final long rand = random.nextLong();
		// In order to get the range to go from start to end, instead of overflowing after end and going
		// back around to start, start must be less than end.
		if (end < start) {
			long t = end;
			end = start;
			start = t;
		}
		long bound = end - start + 1L; // inclusive on end
		// Credit to https://oroboro.com/large-random-in-range/ for the following technique
		// It's a 128-bit-product where only the upper 64 of 128 bits are used.
		final long randLow = rand & 0xFFFFFFFFL;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long randHigh = (rand >>> 32);
		final long boundHigh = (bound >>> 32);
		return start + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	/** Returns a random boolean value. */
	static public boolean randomBoolean () {
		return random.nextBoolean();
	}

	/** Returns true if a random value between 0 and 1 is less than the specified value. */
	static public boolean randomBoolean (float chance) {
		return MathUtils.random() < chance;
	}

	/** Returns random number between 0.0 (inclusive) and 1.0 (exclusive). */
	static public float random () {
		return random.nextFloat();
	}

	/** Returns a random number between 0 (inclusive) and the specified value (exclusive). */
	static public float random (float range) {
		return random.nextFloat() * range;
	}

	/** Returns a random number between start (inclusive) and end (exclusive). */
	static public float random (float start, float end) {
		return start + random.nextFloat() * (end - start);
	}

	/** Returns -1 or 1, randomly. */
	static public int randomSign () {
		return 1 | (random.nextInt() >> 31);
	}

	/** Returns a triangularly distributed random number between -1.0 (exclusive) and 1.0 (exclusive), where values around zero are
	 * more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(float, float, float) randomTriangular(-1, 1, 0)} */
	public static float randomTriangular () {
		return random.nextFloat() - random.nextFloat();
	}

	/** Returns a triangularly distributed random number between {@code -max} (exclusive) and {@code max} (exclusive), where values
	 * around zero are more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(float, float, float) randomTriangular(-max, max, 0)}
	 * @param max the upper limit */
	public static float randomTriangular (float max) {
		return (random.nextFloat() - random.nextFloat()) * max;
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where the
	 * {@code mode} argument defaults to the midpoint between the bounds, giving a symmetric distribution.
	 * <p>
	 * This method is equivalent of {@link #randomTriangular(float, float, float) randomTriangular(min, max, (min + max) * 0.5f)}
	 * @param min the lower limit
	 * @param max the upper limit */
	public static float randomTriangular (float min, float max) {
		return randomTriangular(min, max, (min + max) * 0.5f);
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where values
	 * around {@code mode} are more likely.
	 * @param min the lower limit
	 * @param max the upper limit
	 * @param mode the point around which the values are more likely */
	public static float randomTriangular (float min, float max, float mode) {
		float u = random.nextFloat();
		float d = max - min;
		if (u <= (mode - min) / d) return min + (float)Math.sqrt(u * d * (mode - min));
		return max - (float)Math.sqrt((1 - u) * d * (max - mode));
	}

	// ---

	/** Returns the next power of two. Returns the specified value if the value is already a power of two. */
	static public int nextPowerOfTwo (int value) {
		if (value == 0) return 1;
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}

	static public boolean isPowerOfTwo (int value) {
		return value != 0 && (value & value - 1) == 0;
	}

	// ---

	static public short clamp (short value, short min, short max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public int clamp (int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public long clamp (long value, long min, long max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public float clamp (float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public double clamp (double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	// ---

	/** Linearly interpolates between fromValue to toValue on progress position. */
	static public float lerp (float fromValue, float toValue, float progress) {
		return fromValue + (toValue - fromValue) * progress;
	}

	/** Linearly normalizes value from a range. Range must not be empty. This is the inverse of {@link #lerp(float, float, float)}.
	 * @param rangeStart Range start normalized to 0
	 * @param rangeEnd Range end normalized to 1
	 * @param value Value to normalize
	 * @return Normalized value. Values outside of the range are not clamped to 0 and 1 */
	static public float norm (float rangeStart, float rangeEnd, float value) {
		return (value - rangeStart) / (rangeEnd - rangeStart);
	}

	/** Linearly map a value from one range to another. Input range must not be empty. This is the same as chaining
	 * {@link #norm(float, float, float)} from input range and {@link #lerp(float, float, float)} to output range.
	 * @param inRangeStart Input range start
	 * @param inRangeEnd Input range end
	 * @param outRangeStart Output range start
	 * @param outRangeEnd Output range end
	 * @param value Value to map
	 * @return Mapped value. Values outside of the input range are not clamped to output range */
	static public float map (float inRangeStart, float inRangeEnd, float outRangeStart, float outRangeEnd, float value) {
		return outRangeStart + (value - inRangeStart) * (outRangeEnd - outRangeStart) / (inRangeEnd - inRangeStart);
	}

	/** Linearly interpolates between two angles in radians. Takes into account that angles wrap at two pi and always takes the
	 * direction with the smallest delta angle.
	 * 
	 * @param fromRadians start angle in radians
	 * @param toRadians target angle in radians
	 * @param progress interpolation value in the range [0, 1]
	 * @return the interpolated angle in the range [0, PI2[ */
	public static float lerpAngle (float fromRadians, float toRadians, float progress) {
		float delta = (((toRadians - fromRadians) % PI2 + PI2 + PI) % PI2) - PI;
		return ((fromRadians + delta * progress) % PI2 + PI2) % PI2;
	}

	/** Linearly interpolates between two angles in degrees. Takes into account that angles wrap at 360 degrees and always takes
	 * the direction with the smallest delta angle.
	 * 
	 * @param fromDegrees start angle in degrees
	 * @param toDegrees target angle in degrees
	 * @param progress interpolation value in the range [0, 1]
	 * @return the interpolated angle in the range [0, 360[ */
	public static float lerpAngleDeg (float fromDegrees, float toDegrees, float progress) {
		float delta = (((toDegrees - fromDegrees) % 360f + 360f + 180f) % 360f) - 180f;
		return ((fromDegrees + delta * progress) % 360f + 360f) % 360f;
	}

	// ---

	static private final int BIG_ENOUGH_INT = 16 * 1024;
	static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	static private final double CEIL = 0.9999999;
	static private final double BIG_ENOUGH_CEIL = 16384.999999999996;
	static private final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

	/** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
	 * -(2^14) to (Float.MAX_VALUE - 2^14). */
	static public int floor (float value) {
		return (int)(value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	/** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats that are
	 * positive. Note this method simply casts the float to int. */
	static public int floorPositive (float value) {
		return (int)value;
	}

	/** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
	 * -(2^14) to (Float.MAX_VALUE - 2^14). */
	static public int ceil (float value) {
		return BIG_ENOUGH_INT - (int)(BIG_ENOUGH_FLOOR - value);
	}

	/** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats that
	 * are positive. */
	static public int ceilPositive (float value) {
		return (int)(value + CEIL);
	}

	/** Returns the closest integer to the specified float. This method will only properly round floats from -(2^14) to
	 * (Float.MAX_VALUE - 2^14). */
	static public int round (float value) {
		return (int)(value + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	/** Returns the closest integer to the specified float. This method will only properly round floats that are positive. */
	static public int roundPositive (float value) {
		return (int)(value + 0.5f);
	}

	/** Returns true if the value is zero (using the default tolerance as upper bound) */
	static public boolean isZero (float value) {
		return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
	}

	/** Returns true if the value is zero.
	 * @param tolerance represent an upper bound below which the value is considered zero. */
	static public boolean isZero (float value, float tolerance) {
		return Math.abs(value) <= tolerance;
	}

	/** Returns true if a is nearly equal to b. The function uses the default floating error tolerance.
	 * @param a the first value.
	 * @param b the second value. */
	static public boolean isEqual (float a, float b) {
		return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
	}

	/** Returns true if a is nearly equal to b.
	 * @param a the first value.
	 * @param b the second value.
	 * @param tolerance represent an upper bound below which the two values are considered equal. */
	static public boolean isEqual (float a, float b, float tolerance) {
		return Math.abs(a - b) <= tolerance;
	}

	/** @return the logarithm of value with base a */
	static public float log (float a, float value) {
		return (float)(Math.log(value) / Math.log(a));
	}

	/** @return the logarithm of value with base 2 */
	static public float log2 (float value) {
		return log(2, value);
	}
}
