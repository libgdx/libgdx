/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class Date {
  public final long when;

  public Date() {
    when = System.currentTimeMillis();
  }

  public Date(long when) {
    this.when = when;
  }

  public long getTime() {
    return when;
  }

  public String toString() {
    return toString(when);
  }

  private static native String toString(long when);
}
