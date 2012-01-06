/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

public abstract class Singleton {
  public static native int getInt(Object singleton, int offset);
  public static native long getLong(Object singleton, int offset);
  public static native Object getObject(Object singleton, int offset);
}
