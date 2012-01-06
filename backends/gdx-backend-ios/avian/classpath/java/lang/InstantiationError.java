/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

/**
 * TODO : current Avian runtime doesn't check, need to be implemented.
 *
 */
public class InstantiationError extends IncompatibleClassChangeError {

  public InstantiationError(String message) {
    super(message);
  }

  public InstantiationError() {
  }

}
