/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class IllegalStateException extends RuntimeException {
  public IllegalStateException(String message, Throwable cause) {
    super(message, cause);
  }

  public IllegalStateException(String message) {
    this(message, null);
  }

  public IllegalStateException(Throwable cause) {
    this(null, cause);
  }

  public IllegalStateException() {
    this(null, null);
  }
}
