/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.lang.reflect.Method;

public abstract class Enum<E extends Enum<E>> implements Comparable<E> {
  private final String name;
  protected final int ordinal;

  public Enum(String name, int ordinal) {
    this.name = name;
    this.ordinal = ordinal;
  }

  public int compareTo(E other) {
    if (getDeclaringClass() != other.getDeclaringClass()) {
      throw new ClassCastException();
    }

    return ordinal - other.ordinal;
  }

  public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
    if (name == null) throw new NullPointerException();

    try {
      Method method = enumType.getMethod("values");
      Enum values[] = (Enum[]) method.invoke(null);
      for (Enum value: values) {
        if (name.equals(value.name)) {
          return (T) value;
        }
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    throw new IllegalArgumentException(name);
  }

  public int ordinal() {
    return ordinal;
  }

  public final String name() {
    return name;
  }

  public String toString() {
    return name;
  }

  public Class<E> getDeclaringClass() {
    Class c = getClass();
    while (c.getSuperclass() != Enum.class) {
      c = c.getSuperclass();
    }
    return c;
  }
}
