package com.badlogic.gdx.math;

import java.util.Random;

public class RandomXS128 extends Random{
	long[] s = new long[2];
	
	public RandomXS128(long seed0, long seed1){
		s[0] = seed0;
		s[1] = seed1;
	}
	
	public RandomXS128(){
		s[0] = System.nanoTime();
		s[1] = System.nanoTime();
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
