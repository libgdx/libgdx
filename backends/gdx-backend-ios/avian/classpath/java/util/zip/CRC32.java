/* Copyright (c) 2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.zip;

public class CRC32 {
  private static final int Polynomial = 0x04C11DB7;
  private static final int Width = 32;
  private static final int Top = 1 << (Width - 1);
  private static final int InitialRemainder = 0xFFFFFFFF;
  private static final long ResultXor = 0xFFFFFFFFL;

  private static final int[] table = new int[256];

  static {
    for (int dividend = 0; dividend < 256; ++ dividend) {
      int remainder = dividend << (Width - 8);
      for (int bit = 8; bit > 0; --bit) {
        remainder = ((remainder & Top) != 0)
          ? (remainder << 1) ^ Polynomial
          : (remainder << 1);
      }
      table[dividend] = remainder;
    }
  }

  private int remainder = InitialRemainder;

  public void reset() {
    remainder = InitialRemainder;
  }

  public void update(int b) {
    remainder = table[reflect(b, 8) ^ (remainder >>> (Width - 8))]
      ^ (remainder << 8);
  }

  public void update(byte[] array, int offset, int length) {
    for (int i = 0; i < length; ++i) {
      update(array[offset + i] & 0xFF);
    }
  }

  public void update(byte[] array) {
    update(array, 0, array.length);
  }

  public long getValue() {
    return (reflect(remainder, Width) ^ ResultXor) & 0xFFFFFFFFL;
  }

  private static int reflect(int x, int n) {
    int reflection = 0;
    for (int i = 0; i < n; ++i) {
      if ((x & 1) != 0) {
        reflection |= (1 << ((n - 1) - i));
      }
      x = (x >>> 1);
    }
    return reflection;
  }
}
