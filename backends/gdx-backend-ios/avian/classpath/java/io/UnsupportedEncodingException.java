/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class UnsupportedEncodingException extends IOException {
  public UnsupportedEncodingException(String message) {
    super(message);
  }

  public UnsupportedEncodingException() {
    this(null);
  }
}
