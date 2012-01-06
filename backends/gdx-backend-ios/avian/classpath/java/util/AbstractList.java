/* Copyright (c) 2009-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public abstract class AbstractList<T> extends AbstractCollection<T>
  implements List<T>
{
  protected int modCount;

  public Iterator<T> iterator() {
    return listIterator();
  }

  public ListIterator<T> listIterator() {
    return new Collections.ArrayListIterator(this);
  }

  public int indexOf(Object o) {
    int i = 0;
    for (T v: this) {
      if (o == null) {
        if (v == null) {
          return i;
        }
      } else if (o.equals(v)) {
        return i;
      }

      ++ i;
    }
    return -1;
  }
}
