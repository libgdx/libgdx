
package com.badlogic.gdx.math;

import java.util.Random;

/** This class implements the xorshift 128+ algorithm known as the fastest among the PRNG, the comparisons between this and the
 * java default random show an average difference of 10 ms on an input of 1 millions of floating point numbers. More information
 * and algorithms can be found <a href="http://xorshift.di.unimi.it/">here</a> */
public class RandomXS128 extends Random {
	long seed0, seed1;

	public RandomXS128 (long seed0, long seed1) {
		this.seed0 = seed0;
		this.seed1 = seed1;
	}

	/** It will allocate a {@link Random} to generate the two long seeds */
	public RandomXS128 () {
		Random random = new Random();
		seed0 = random.nextLong();
		seed1 = random.nextLong();
	}

	@Override
	protected int next (int bits) {
		long s1 = seed0;
		long s0 = seed1;
		seed0 = s0;
		s1 ^= s1 << 23; // a
		s1 = (s1 ^ s0 ^ (s1 >> 17) ^ (s0 >> 26)) + s0; // b, c
		seed1 = s1;
		return (int)(s1 & ((1L << bits) - 1));
	}
}
