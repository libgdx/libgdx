/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public final class Character implements Comparable<Character> {
  public static final int MIN_RADIX = 2;
  public static final int MAX_RADIX = 36;

  public static final Class TYPE = Class.forCanonicalName("C");

  private final char value;

  public Character(char value) {
    this.value = value;
  }

  public static Character valueOf(char value) {
    return new Character(value);
  }

  public int compareTo(Character o) {
    return value - o.value;
  }

  public boolean equals(Object o) {
    return o instanceof Character && ((Character) o).value == value;
  }

  public int hashCode() {
    return (int) value;
  }

  public String toString() {
    return toString(value);
  }

  public static String toString(char v) {
    return new String(new char[] { v });
  }

  public char charValue() {
    return value;
  }

  public static char toLowerCase(char c) {
    if (c >= 'A' && c <= 'Z') {
      return (char) ((c - 'A') + 'a');
    } else {
      return c;
    }
  }

  public static int toLowerCase(int codePoint) {
    if (isSupplementaryCodePoint(codePoint)) {
      return codePoint;
    } else {
      return toLowerCase((char) codePoint);
    }
  }

  public static char toUpperCase(char c) {
    if (c >= 'a' && c <= 'z') {
      return (char) ((c - 'a') + 'A');
    } else {
      return c;
    }
  }

  public static int toUpperCase(int codePoint) {
    if (isSupplementaryCodePoint(codePoint)) {
      return codePoint;
    } else {
      return toUpperCase((char) codePoint);
    }
  }

  public static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  public static boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }

  public static int digit(char c, int radix) {
    int digit = 0;
    if ((c >= '0') && (c <= '9')) {
      digit = c - '0';
    } else if ((c >= 'a') && (c <= 'z')) {
      digit = c - 'a' + 10;
    } else if ((c >= 'A') && (c <= 'Z')) {
      digit = c - 'A' + 10;
    } else {
      return -1;
    }

    if (digit < radix) {
      return digit;
    } else {
      return -1;
    }
  }

  public static char forDigit(int digit, int radix) {
    if (MIN_RADIX <= radix && radix <= MAX_RADIX) {
      if (0 <= digit && digit < radix) {
        return (char) (digit < 10 ? digit + '0' : digit + 'a' - 10);
      }
    }
    return 0;
  }

  public static boolean isLetter(int c) {
    return canCastToChar(c) && isLetter((char) c);
  }

  public static boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
  }

  public static boolean isLetterOrDigit(char c) {
    return isDigit(c) || isLetter(c);
  }

  public static boolean isLetterOrDigit(int c) {
    return canCastToChar(c) && (isDigit((char) c) || isLetter((char) c));
  }

  public static boolean isLowerCase(int c) {
    return canCastToChar(c) && isLowerCase((char) c);
  }

  public static boolean isLowerCase(char c) {
    return (c >= 'a' && c <= 'z');
  }

  public static boolean isUpperCase(char c) {
    return (c >= 'A' && c <= 'Z');
  }

  public static boolean isUpperCase(int c) {
    return canCastToChar(c) && isUpperCase((char) c);
  }

  public static boolean isWhitespace(int c) {
    return canCastToChar(c) && isWhitespace((char) c);
  }

  public static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
  }

  public static boolean isSpaceChar(char c) {
    return isWhitespace(c);
  }

  public static boolean isHighSurrogate(char ch) {
    return ch >= '\uD800' && ch <= '\uDBFF';
  }

  public static boolean isLowSurrogate(char ch) {
    return ch >= '\uDC00' && ch <= '\uDFFF';
  }

  public static int toCodePoint(char high, char low) {
    return (((high & 0x3FF) << 10) | (low & 0x3FF)) + 0x10000;
  }

  public static boolean isSupplementaryCodePoint(int codePoint) {
    return codePoint >= 0x10000 && codePoint <= 0x10FFFF;
  }

  private static boolean canCastToChar(int codePoint) {
    return (codePoint >= 0 && codePoint <= 0xFFFF);
  }

  public static char[] toChars(int codePoint) {
    if (isSupplementaryCodePoint(codePoint)) {
      int cpPrime = codePoint - 0x10000;
      int high = 0xD800 | ((cpPrime >> 10) & 0x3FF);
      int low = 0xDC00 | (cpPrime & 0x3FF);
      return new char[] { (char) high, (char) low };
    }
    return new char[] { (char) codePoint };
  }

  public static boolean isSurrogatePair(char high, char low) {
    return isHighSurrogate(high) && isLowSurrogate(low);
  }

  public static int codePointAt(CharSequence sequence, int offset) {
    int length = sequence.length();
    if (offset < 0 || offset >= length) {
      throw new IndexOutOfBoundsException();
    }

    char high = sequence.charAt(offset);
    if (! isHighSurrogate(high) || offset >= length) {
      return high;
    }
    char low = sequence.charAt(offset + 1);
    if (! isLowSurrogate(low)) {
      return high;
    }

    return toCodePoint(high, low);
  }

  public static int codePointCount(CharSequence sequence, int start, int end) {
    int length = sequence.length();
    if (start < 0 || start > end || end >= length) {
      throw new IndexOutOfBoundsException();
    }

    int count = 0;
    for (int i = start; i < end; ++i) {
      if (isHighSurrogate(sequence.charAt(i))
          && (i + 1) < end
          && isLowSurrogate(sequence.charAt(i + 1)))
      {
        ++ i;
      }
      ++ count;
    }
    return count;
  }
}
