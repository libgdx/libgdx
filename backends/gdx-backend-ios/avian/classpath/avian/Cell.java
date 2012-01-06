/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

public class Cell <T> {
  public T value;
  public Cell<T> next;
  
  public Cell(T value, Cell<T> next) {
    this.value = value;
    this.next = next;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    for (Cell c = this; c != null; c = c.next) {
      sb.append(value);
      if (c.next != null) {
        sb.append(" ");
      }
    }
    sb.append(")");
    return sb.toString();
  }
}
