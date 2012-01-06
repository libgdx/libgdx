/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio;

public abstract class Buffer {
  protected int capacity;
  protected int position;
  protected int limit;

  public final int limit() {
    return limit;
  }

  public final int remaining() {
    return limit-position;
  }

  public final int position() {
    return position;
  }

  public final int capacity() {
    return capacity;
  }

  public final Buffer limit(int newLimit) {
    limit = newLimit;
    return this;
  }

  public final Buffer position(int newPosition) {
    position = newPosition;
    return this;
  }

  public final boolean hasRemaining() {
    return remaining() > 0;
  }

  public final Buffer clear() {
    position = 0;
    limit = capacity;
    return this;
  }

  public final Buffer flip() {
    limit = position;
    position = 0;
    return this;
  }

  public final Buffer rewind() {
    position = 0;
    return this;
  }
}
