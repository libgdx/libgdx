/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class UnsupportedOperationException extends RuntimeException {
  public UnsupportedOperationException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnsupportedOperationException(String message) {
    this(message, null);
  }

  public UnsupportedOperationException(Throwable cause) {
    this(null, cause);
  }

  public UnsupportedOperationException() {
    this(null, null);
  }
}
