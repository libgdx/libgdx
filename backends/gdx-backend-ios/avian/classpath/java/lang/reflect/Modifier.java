/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

public final class Modifier {
  public static final int PUBLIC       = 1 <<  0;
  public static final int PRIVATE      = 1 <<  1;
  public static final int PROTECTED    = 1 <<  2;
  public static final int STATIC       = 1 <<  3;
  public static final int FINAL        = 1 <<  4;
  public static final int SUPER        = 1 <<  5;
  public static final int SYNCHRONIZED = SUPER;
  public static final int VOLATILE     = 1 <<  6;
  public static final int TRANSIENT    = 1 <<  7;
  public static final int NATIVE       = 1 <<  8;
  public static final int INTERFACE    = 1 <<  9;
  public static final int ABSTRACT     = 1 << 10;
  public static final int STRICT       = 1 << 11;

  private Modifier() { }

  public static boolean isPublic   (int v) { return (v &    PUBLIC) != 0; }
  public static boolean isPrivate  (int v) { return (v &   PRIVATE) != 0; }
  public static boolean isProtected(int v) { return (v & PROTECTED) != 0; }
  public static boolean isStatic   (int v) { return (v &    STATIC) != 0; }
  public static boolean isFinal    (int v) { return (v &     FINAL) != 0; }
  public static boolean isSuper    (int v) { return (v &     SUPER) != 0; }
  public static boolean isNative   (int v) { return (v &    NATIVE) != 0; }
  public static boolean isAbstract (int v) { return (v &  ABSTRACT) != 0; }
  public static boolean isInterface(int v) { return (v & INTERFACE) != 0; }
}
