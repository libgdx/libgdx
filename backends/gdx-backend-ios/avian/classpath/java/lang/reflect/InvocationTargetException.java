/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

public class InvocationTargetException extends Exception {
  public InvocationTargetException(Throwable targetException, String message) {
    super(message, targetException);
  }

  public InvocationTargetException(Throwable targetException) {
    this(targetException, null);
  }

  public InvocationTargetException() {
    this(null, null);
  }

  public Throwable getTargetException() {
    return getCause();
  }
}
