/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

/**
 * @author zsombor
 *
 */
public class ConcurrentModificationException extends RuntimeException {

  /**
   * @param message
   * @param cause
   */
  public ConcurrentModificationException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public ConcurrentModificationException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public ConcurrentModificationException(Throwable cause) {
    super(cause);
  }

  /**
   * 
   */
  public ConcurrentModificationException() {
  }

}
