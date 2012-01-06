/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public interface Collection<T> extends Iterable<T> {
  public int size();

  public boolean isEmpty();

  public boolean contains(Object element);

  public boolean add(T element);

  public boolean addAll(Collection<? extends T> collection);

  public boolean remove(Object element);

  public Object[] toArray();

  public <S> S[] toArray(S[] array);

  public void clear();
}
