/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.Serializable;

/**
 * @author zsombor
 * 
 */
public class BitSet implements Serializable, Cloneable {

  final static int  BITS_PER_LONG       = 64;
  final static int  BITS_PER_LONG_SHIFT = 6;
  final static long MASK                = 0xFFFFFFFFFFFFFFFFL;

  private long[]    bits;

  private static int longPosition(int index) {
    return index >> BITS_PER_LONG_SHIFT;
  }

  private static long bitPosition(int index) {
    return 1L << (index % BITS_PER_LONG);
  }

  public BitSet(int bitLength) {
    if (bitLength % BITS_PER_LONG == 0) {
      enlarge(longPosition(bitLength));
    } else {
      enlarge(longPosition(bitLength) + 1);
    }
  }

  public BitSet() {
    enlarge(1);
  }

  public void and(BitSet otherBits) {
    int min = Math.min(bits.length, otherBits.bits.length);
    for (int i = 0; i < min; i++) {
      bits[i] &= otherBits.bits[i];
    }
    for (int i = min; i < bits.length; i++) { 
      bits[i] = 0;
    }
  }

  public void andNot(BitSet otherBits) {
    int max = Math.max(bits.length, otherBits.bits.length);
    enlarge(max);
    int min = Math.min(bits.length, otherBits.bits.length);
    for (int i = 0; i < min; i++) {
      bits[i] &= ~otherBits.bits[i];
    }
  }

  public void or(BitSet otherBits) {
    int max = Math.max(bits.length, otherBits.bits.length);
    enlarge(max);
    int min = Math.min(bits.length, otherBits.bits.length);
    for (int i = 0; i < min; i++) {
      bits[i] |= otherBits.bits[i];
    }
  }

  public void xor(BitSet otherBits) {
    int max = Math.max(bits.length, otherBits.bits.length);
    enlarge(max);
    int min = Math.min(bits.length, otherBits.bits.length);
    for (int i = 0; i < min; i++) {
      bits[i] ^= otherBits.bits[i];
    }
  }

  private void enlarge(int newSize) {
    if (bits == null || bits.length < newSize) {
      long[] newBits = new long[newSize + 1];
      if (bits != null) {
        System.arraycopy(bits, 0, newBits, 0, bits.length);
      }
      bits = newBits;
    }
  }

  public void clear(int index) {
    int pos = longPosition(index);
    if (pos < bits.length) {
      bits[pos] &= (MASK ^ bitPosition(index));
    }
  }

  public boolean get(int index) {
    int pos = longPosition(index);
    if (pos < bits.length) {
      return (bits[pos] & bitPosition(index)) != 0;
    }
    return false;
  }

  public void set(int index) {
    int pos = longPosition(index);
    enlarge(pos);
    bits[pos] |= bitPosition(index);
  }

  public void set(int start, int end) {
    for (int i = start; i < end; i++) {
      set(i);
    }
  }

  public void clear(int start, int end) {
    for (int i = start; i < end; i++) {
      clear(i);
    }
  }

  public boolean isEmpty() {
    for (int i = 0; i < bits.length; i++) {
      if (bits[i] != 0) {
        return false;
      }
    }
    return true;
  }

  public boolean intersects(BitSet otherBits) {
    int max = Math.max(bits.length, otherBits.bits.length);
    for (int i = 0; i < max; i++) {
      if ((bits[i] & otherBits.bits[i]) != 0) {
        return true;
      }
    }
    return false;
  }

  public int length() {
    return bits.length << BITS_PER_LONG_SHIFT;
  }

  public int nextSetBit(int fromIndex) {
    return nextBit(fromIndex, false);
  }

  private int nextBit(int fromIndex, boolean bitClear) {
    int pos = longPosition(fromIndex);
    if (pos >= bits.length) {
      return -1;
    }
    int current = fromIndex;
    do {
      long currValue = bits[pos];
      if (currValue == 0) {
        pos++;
        current = pos << BITS_PER_LONG_SHIFT;
      } else {
        do {
          long bitPos = bitPosition(current);
          if (((currValue & bitPos) != 0) ^ bitClear) {
            return current;
          } else {
            current++;
          }
        } while (current % BITS_PER_LONG != 0);
      }
      pos++;
    } while (pos < bits.length);

    return -1;
  }

  public int nextClearBit(int fromIndex) {
    return nextBit(fromIndex, true);
  }

}
