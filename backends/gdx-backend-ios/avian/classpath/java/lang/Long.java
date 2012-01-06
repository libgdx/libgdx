/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public final class Long extends Number implements Comparable<Long> {
  public static final long MIN_VALUE = -9223372036854775808l;
  public static final long MAX_VALUE =  9223372036854775807l;

  public static final Class TYPE = Class.forCanonicalName("J");

  private final long value;

  public Long(long value) {
    this.value = value;
  }

  public Long(String s) {
    this.value = parseLong(s);
  }

  public static Long valueOf(String value) {
    return new Long(value);
  }

  public static Long valueOf(long value) {
    return new Long(value);
  }

  public int compareTo(Long o) {
    return value > o.value ? 1 : (value < o.value ? -1 : 0);
  }

  public boolean equals(Object o) {
    return o instanceof Long && ((Long) o).value == value;
  }

  public int hashCode() {
    return (int) ((value >> 32) ^ (value & 0xFF));
  }

  public String toString() {
    return String.valueOf(value);
  }

  public static String toString(long v, int radix) {
    if (radix < 1 || radix > 36) {
      throw new IllegalArgumentException("radix " + radix + " not in [1,36]");
    }

    if (v == 0) {
      return "0";
    }

    boolean negative = v < 0;

    int size = (negative ? 1 : 0);
    for (long n = v; n != 0; n /= radix) ++size;

    char[] array = new char[size];

    int i = size - 1;
    for (long n = v; n != 0; n /= radix) {
      long digit = n % radix;
      if (negative) digit = -digit;

      if (digit >= 0 && digit <= 9) {
        array[i] = (char) ('0' + digit);
      } else {
        array[i] = (char) ('a' + (digit - 10));
      }
      --i;
    }

    if (negative) {
      array[i] = '-';
    }

    return new String(array, 0, size, false);
  }

  public static String toString(long v) {
    return toString(v, 10);
  }

  public static String toHexString(long v) {
    return toString(v, 16);
  }

  public byte byteValue() {
    return (byte) value;
  }

  public short shortValue() {
    return (short) value;
  }

  public int intValue() {
    return (int) value;
  }

  public long longValue() {
    return value;
  }

  public float floatValue() {
    return (float) value;
  }

  public double doubleValue() {
    return (double) value;
  }

  private static long pow(long a, long b) {
    long c = 1;
    for (int i = 0; i < b; ++i) c *= a;
    return c;
  }

  public static long parseLong(String s) {
    return parseLong(s, 10);
  } 

  public static long parseLong(String s, int radix) {
    int i = 0;
    long number = 0;
    boolean negative = s.startsWith("-");
    int length = s.length();
    if (negative) {
      i = 1;
      -- length;
    }

    long factor = pow(radix, length - 1);
    for (; i < s.length(); ++i) {
      char c = s.charAt(i);
      int digit = Character.digit(c, radix);
      if (digit >= 0) {
        number += digit * factor;
        factor /= radix;
      } else {
        throw new NumberFormatException("invalid character " + c + " code " +
                                        (int) c);
      }
    }

    if (negative) {
      number = -number;
    }

    return number;
  }
}
