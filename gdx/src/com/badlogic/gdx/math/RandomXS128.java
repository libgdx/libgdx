package com.badlogic.gdx.math;

import java.util.Random;

/** This class implements the xorshift 128+ algorithm known as the fastest among the PRNG, 
 * the comparisons between this and the java default random show an average difference of 10 ms on an input
 * of 1 millions of floating point numbers.
 * To use it inside the code one can simply assign an instance of this class to {@link MathUtils#random} member
 * at application setup and generate the random numbers using the methods of {@link MathUtils} class. 
 * More information and algorithms can be found <a href="http://xorshift.di.unimi.it/">here</a> */ 
public class RandomXS128 extends Random{
	long[] s = new long[2];
	
	public RandomXS128(long seed0, long seed1){
		s[0] = seed0;
		s[1] = seed1;
	}
	
	/** It will use {@link System#nanoTime()} as seeds*/
	public RandomXS128(){
		this(System.nanoTime(), System.nanoTime());
	}
	
	@Override
	protected int next (int bits) {
		long s1 = s[ 0 ];
		long s0 = s[ 1 ];
		s[ 0 ] = s0;
		s1 ^= s1 << 23; // a
		s[ 1 ] = ( s1 ^ s0 ^ ( s1 >> 17 ) ^ ( s0 >> 26 ) )  + s0; // b, c
		return  (int)(s[ 1 ] &((1L << bits) - 1));
	}

}
