/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class AssertionError extends Error {
  public AssertionError() {
    super("", null);
  }

  public AssertionError(boolean detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(char detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(double detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(float detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(int detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(long detailMessage) {
    super(""+detailMessage, null);
  }

  public AssertionError(Object detailMessage) {
    super(""+detailMessage, null);
  }
}
