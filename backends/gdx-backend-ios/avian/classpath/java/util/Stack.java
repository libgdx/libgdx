/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class Stack<T> extends Vector<T> {
  public boolean empty() {
    return size() == 0;
  }

  public T peek() {
    return get(size() - 1);
  }

  public T pop() {
    return remove(size() - 1);
  }

  public T push(T element) {
    add(element);
    return element;
  }
}
