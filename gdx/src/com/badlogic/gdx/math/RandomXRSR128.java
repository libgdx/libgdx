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

/** This class implements the xoroshiro128++ pseudo random number generator. Adapted from Sebastiano Vigna's
 * <a href="https://github.com/vigna/dsiutils">disutils</a>. Note that xoroshiro128++ is a part of the Java API from version 17
 * and above.
 * <p>
 * Instances of RandomXRSR128 are not thread-safe.
 *
 * @author trietng */
public class RandomXRSR128 extends Random {
	/** Normalization constant for double. */
	private static final double NORM_DOUBLE = 1.0 / (1L << 53);

	/** Normalization constant for float. */
	private static final float NORM_FLOAT = (float)(1.0 / (1L << 24));

	/** 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
	private static final long PHI = 0x9E3779B97F4A7C15L;

	/** The first half of the internal state of this pseudo-random number generator. */
	private long seed0;

	/** The second half of the internal state of this pseudo-random number generator. */
	private long seed1;

	/** Creates a new random number generator. This constructor sets the seed of the random number generator to a value very likely
	 * to be distinct from any other invocation of this constructor.
	 * <p>
	 * This implementation creates a {@link Random} instance to generate the initial seed. */
	public RandomXRSR128 () {
		setSeed(new Random().nextLong());
	}

	/** Creates a new random number generator using a single {@code long} seed.
	 * @param seed the initial seed */
	public RandomXRSR128 (final long seed) {
		setSeed(seed);
	}

	/** Creates a new random number generator using two {@code long} seeds.
	 * @param seed0 the first part of the initial seed
	 * @param seed1 the second part of the initial seed */
	public RandomXRSR128 (final long seed0, final long seed1) {
		setState(seed0, seed1);
	}

	/** Returns the next pseudo-random, uniformly distributed {@code long} value from this random number generator's sequence.
	 * <p>
	 * Subclasses should override this, as this is used by all other methods. */
	@Override
	public long nextLong () {
		final long s0 = this.seed0;
		long s1 = this.seed1;

		final long result = Long.rotateLeft(s0 + s1, 17) + s0;

		s1 ^= s0;
		this.seed0 = Long.rotateLeft(s0, 49) ^ s1 ^ s1 << 21;
		this.seed1 = Long.rotateLeft(s1, 28);
		return result;
	}

	/** This protected method is final because, contrary to the superclass, it's not used anymore by the other methods. */
	@Override
	protected final int next (final int bits) {
		return (int)(nextLong() & ((1L << bits) - 1));
	}

	/** Returns the next pseudo-random, uniformly distributed {@code int} value from this random number generator's sequence.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally. */
	@Override
	public int nextInt () {
		return (int)nextLong();
	}

	/** Returns a pseudo-random, uniformly distributed {@code int} value between 0 (inclusive) and the specified value (exclusive),
	 * drawn from this random number generator's sequence.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally.
	 * @param n the positive bound on the random number to be returned.
	 * @return the next pseudo-random {@code int} value between {@code 0} (inclusive) and {@code n} (exclusive). */
	@Override
	public int nextInt (final int n) {
		return (int)nextLong(n);
	}

	/** Returns a pseudorandom uniformly distributed {@code long} value between 0 (inclusive) and the specified value (exclusive),
	 * drawn from this random number generator's sequence. The algorithm used to generate the value guarantees that the result is
	 * uniform, provided that the sequence of 64-bit values produced by this generator is.
	 * @param n the positive bound on the random number to be returned.
	 * @return the next pseudorandom {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive). */
	public long nextLong (final long n) {
		if (n <= 0) throw new IllegalArgumentException("illegal bound " + n + " (must be positive)");
		long t = nextLong();
		final long nMinus1 = n - 1;
		// Rejection-based algorithm to get uniform integers in the general case
		for (long u = t >>> 1; u + nMinus1 - (t = u % n) < 0; u = nextLong() >>> 1)
			;
		return t;
	}

	/** Returns a pseudo-random, uniformly distributed {@code double} value between 0.0 and 1.0 from this random number generator's
	 * sequence.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally. */
	@Override
	public double nextDouble () {
		return (nextLong() >>> 11) * NORM_DOUBLE;
	}

	/** Returns a pseudo-random, uniformly distributed {@code float} value between 0.0 and 1.0 from this random number generator's
	 * sequence.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally. */
	@Override
	public float nextFloat () {
		return (nextLong() >>> 40) * NORM_FLOAT;
	}

	/** Returns a pseudo-random, uniformly distributed {@code boolean } value from this random number generator's sequence.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally. */
	@Override
	public boolean nextBoolean () {
		return nextLong() < 0;
	}

	/** Generates random bytes and places them into a user-supplied byte array. The number of random bytes produced is equal to the
	 * length of the byte array.
	 * <p>
	 * This implementation uses {@link #nextLong()} internally. */
	@Override
	public void nextBytes (final byte[] bytes) {
		int i = bytes.length, n;
		while (i != 0) {
			n = Math.min(i, 8);
			for (long bits = nextLong(); n-- != 0; bits >>= 8)
				bytes[--i] = (byte)bits;
		}
	}

	/** Sets the internal seed of this generator based on the given {@code long} value.
	 * @param seed a seed for this generator. */
	@Override
	public void setSeed (final long seed) {
		long h = murmurHash3(seed);
		this.seed0 = staffordMix13(h += PHI);
		this.seed1 = staffordMix13(h + PHI);
	}

	/** Sets the internal state of this generator.
	 * @param seed0 the first part of the internal state
	 * @param seed1 the second part of the internal state */
	public void setState (final long seed0, final long seed1) {
		this.seed0 = seed0;
		this.seed1 = seed1;
	}

	/** Returns the internal seeds to allow state saving.
	 * @param seed must be 0 or 1, designating which of the 2 long seeds to return
	 * @return the internal seed that can be used in setState */
	public long getState (int seed) {
		return seed == 0 ? seed0 : seed1;
	}

	private static long murmurHash3 (long x) {
		x ^= x >>> 33;
		x *= 0xff51afd7ed558ccdL;
		x ^= x >>> 33;
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= x >>> 33;
		return x;
	}

	/** Adapted from Sebastiano Vigna's <a href="https://github.com/vigna/fastutil">fastutil</a> */
	private static long staffordMix13 (long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}
}
