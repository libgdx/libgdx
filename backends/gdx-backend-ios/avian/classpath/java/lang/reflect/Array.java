/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

public final class Array {
  private Array() { }

  public static Object get(Object array, int index) {
    String className = array.getClass().getName();
    if (! className.startsWith("[")) {
      throw new IllegalArgumentException();
    }

    switch (className.charAt(1)) {
    case 'B':
      return Byte.valueOf(((byte[]) array)[index]);
    case 'C':
      return Character.valueOf(((char[]) array)[index]);
    case 'D':
      return Double.valueOf(((double[]) array)[index]);
    case 'F':
      return Float.valueOf(((float[]) array)[index]);
    case 'I':
      return Integer.valueOf(((int[]) array)[index]);
    case 'J':
      return Long.valueOf(((long[]) array)[index]);
    case 'S':
      return Short.valueOf(((short[]) array)[index]);
    case 'Z':
      return Boolean.valueOf(((boolean[]) array)[index]);
    case 'L':
    case '[':
      return ((Object[]) array)[index];

    default:
      throw new Error();
    }
  }

  public static void set(Object array, int index, Object value) {
    String className = array.getClass().getName();
    if (! className.startsWith("[")) {
      throw new IllegalArgumentException();
    }

    switch (className.charAt(1)) {
    case 'B':
      ((byte[]) array)[index] = (Byte) value;
      break;
    case 'C':
      ((char[]) array)[index] = (Character) value;
      break;
    case 'D':
      ((double[]) array)[index] = (Double) value;
      break;
    case 'F':
      ((float[]) array)[index] = (Float) value;
      break;
    case 'I':
      ((int[]) array)[index] = (Integer) value;
      break;
    case 'J':
      ((long[]) array)[index] = (Long) value;
      break;
    case 'S':
      ((short[]) array)[index] = (Short) value;
      break;
    case 'Z':
      ((boolean[]) array)[index] = (Boolean) value;
      break;
    case 'L':
    case '[':
      if (value == null
          || array.getClass().getComponentType().isInstance(value))
      {
        ((Object[]) array)[index] = value;
      } else {
        throw new IllegalArgumentException
          ("need " + array.getClass().getComponentType() +
           ", got " + value.getClass().getName());
      }
      break;

    default:
      throw new Error();
    }    
  }

  public static native int getLength(Object array);

  private static native Object makeObjectArray(Class elementType, int length);

  public static Object newInstance(Class elementType, int length) {
    if (length < 0) {
      throw new NegativeArraySizeException();
    }

    if (elementType.isPrimitive()) {
      if (elementType.equals(boolean.class)) {
        return new boolean[length];
      } else if (elementType.equals(byte.class)) {
        return new byte[length];
      } else if (elementType.equals(char.class)) {
        return new char[length];
      } else if (elementType.equals(short.class)) {
        return new short[length];
      } else if (elementType.equals(int.class)) {
        return new int[length];
      } else if (elementType.equals(long.class)) {
        return new long[length];
      } else if (elementType.equals(float.class)) {
        return new float[length];
      } else if (elementType.equals(double.class)) {
        return new double[length];
      } else {
        throw new IllegalArgumentException();
      }
    } else {
      return makeObjectArray(elementType, length);
    }
  }
}
