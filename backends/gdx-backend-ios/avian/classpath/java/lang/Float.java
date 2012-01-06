/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public final class Float extends Number {
  public static final Class TYPE = Class.forCanonicalName("F");
  private static final int EXP_BIT_MASK = 0x7F800000;
  private static final int SIGNIF_BIT_MASK = 0x007FFFFF;
  
  private final float value;  

  public Float(String value) {
    this.value = parseFloat(value);
  }

  public Float(float value) {
    this.value = value;
  }

  public static Float valueOf(float value) {
    return new Float(value);
  }

  public static Float valueOf(String s) {
    return new Float(s);
  }

  public boolean equals(Object o) {
    return o instanceof Float && ((Float) o).value == value;
  }

  public int hashCode() {
    return floatToRawIntBits(value);
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(float v) {
    return Double.toString(v);
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
    return (long) value;
  }

  public float floatValue() {
    return value;
  }

  public double doubleValue() {
    return (double) value;
  }

  public boolean isInfinite() {
    return isInfinite(value);
  }

  public boolean isNaN() {
    return isNaN(value);
  }

  public static float parseFloat(String s) {
    int[] numRead = new int[1];
    float f = floatFromString(s, numRead);
    if (numRead[0] == 1) {
      return f;
    } else {
      throw new NumberFormatException(s);
    }
  }
  
  public static int floatToIntBits(float value) {
    int result = floatToRawIntBits(value);
    
    // Check for NaN based on values of bit fields, maximum
    // exponent and nonzero significand.
    if (((result & EXP_BIT_MASK) == EXP_BIT_MASK) && (result & SIGNIF_BIT_MASK) != 0) {
      result = 0x7fc00000;
    }
    return result;
  }

  public static native int floatToRawIntBits(float value);

  public static native float intBitsToFloat(int bits);

  public static native boolean isInfinite(float value);

  public static native boolean isNaN(float value);

  public static native float floatFromString(String s, int[] numRead);
}
