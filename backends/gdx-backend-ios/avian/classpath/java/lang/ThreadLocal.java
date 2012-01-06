/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.util.Map;

public class ThreadLocal<T> {
  private static final Object Null = new Object();

  protected T initialValue() {
    return null;
  }

  public T get() {
    Map<ThreadLocal, Object> map = Thread.currentThread().locals();
    Object o = map.get(this);
    if (o == null) {
      o = initialValue();
      if (o == null) {
        o = Null;
      }
      map.put(this, o);
    }
    if (o == Null) {
      o = null;
    }
    return (T) o;
  }

  public void set(T value) {
    Map<ThreadLocal, Object> map = Thread.currentThread().locals();
    Object o = value;
    if (o == null) {
      o = Null;
    }
    map.put(this, o);
  }
}
