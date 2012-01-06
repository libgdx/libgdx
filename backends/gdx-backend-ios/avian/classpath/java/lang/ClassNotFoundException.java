/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class ClassNotFoundException extends Exception {
  private final Throwable cause2;

  public ClassNotFoundException(String message, Throwable cause) {
    super(message, cause);
    cause2 = cause;
  }

  public ClassNotFoundException(String message) {
    this(message, null);
  }

  public ClassNotFoundException() {
    this(null, null);
  }
}
