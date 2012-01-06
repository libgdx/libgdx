/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class MissingResourceException extends RuntimeException {
  private final String class_;
  private final String key;

  public MissingResourceException(String message, String class_, String key) {
    super(message);
    this.class_ = class_;
    this.key = key;
  }

  public String getClassName() {
    return class_;
  }

  public String getKey() {
    return key;
  }
}
