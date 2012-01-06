/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.ref;

public class ReferenceQueue<T> {
  private Reference<? extends T> front;

  public Reference<? extends T> poll() {
    Reference<? extends T> r = front;
    if (front != null) {
      if (front == front.jNext) {
        front = null;
      } else {
        front = front.jNext;
      }
    }
    return r;
  }

  void add(Reference<? extends T> r) {
    if (front == null) {
      r.jNext = r;
    } else {
      r.jNext = front;
    }
    front = r;
  }
}
