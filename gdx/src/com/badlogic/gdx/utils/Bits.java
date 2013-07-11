
package com.badlogic.gdx.utils;

import java.util.Arrays;

/** A bitset, without size limitation, allows comparison via bitwise operators to other bitfields.
 * @author mzechner */
public class Bits {
	long[] bits = { 0 };

	/**
	 * @param index the index of the bit
	 * @return whether the bit is set
	 * @throws ArrayIndexOutOfBoundsException if index < 0
	 */
	public boolean get (int index) {
		final int word = index >>> 6;
		if(word >= bits.length) return false;
		return (bits[word] & (1L << (index & 0x3F))) != 0L;
	}

	/**
	 * @param index the index of the bit to set
	 * @throws ArrayIndexOutOfBoundsException if index < 0
	 */
	public void set (int index) {
		final int word = index >>> 6;
		checkCapacity(word);
		bits[word] |= 1L << (index & 0x3F);
	}
	
	/**
	 * @param index the index of the bit to flip
	 */
	public void flip(int index) {
		final int word = index >>> 6;
		checkCapacity(word);
		bits[word] ^= 1L << (index & 0x3F);
	}
	
	private void checkCapacity(int len) {
		if(len>=bits.length) {
			long[] newBits = new long[len+1];
			System.arraycopy(bits, 0, newBits, 0, bits.length);
			bits = newBits;
		}
	}

	/**
	 * @param index the index of the bit to clear
	 * @throws ArrayIndexOutOfBoundsException if index < 0
	 */
	public void clear (int index) {
		final int word = index >>> 6;
		if(word >= bits.length) return;
		bits[word] &= ~(1L << (index & 0x3F));
	}

	/**
	 * Clears the entire bitset
	 */
	public void clear () {
		int length = bits.length;
		for (int i = 0; i < length; i++) {
			bits[i] = 0L;
		}
	}
	
	/**
	 * @return the number of bits currently stored, <b>not</b> the highset set bit!
	 */
	public int numBits() {
		return bits.length << 6;
	}
}
