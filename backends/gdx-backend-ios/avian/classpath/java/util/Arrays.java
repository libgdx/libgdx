/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class Arrays {
  private Arrays() { }

  public static String toString(Object[] a) {
    return asList(a).toString();
  }

  private static boolean equal(Object a, Object b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  public static void sort(Object[] array) {
    sort(array, new Comparator() {
        public int compare(Object a, Object b) {
          return ((Comparable) a).compareTo(b);
        }
      });
  }

  public static <T> void sort(T[] array, Comparator<? super T> comparator) {
    // insertion sort
    for (int j = 1; j < array.length; ++j) {
      T t = array[j];
      int i = j - 1;
      while (i >= 0 && comparator.compare(array[i], t) > 0) {
        array[i + 1] = array[i];
        i = i - 1;
      }
      array[i + 1] = t;
    }
  }

  public static <T> List<T> asList(final T ... array) {
    return new List<T>() {
      public String toString() {
        return Collections.toString(this);
      }

      public int size() {
        return array.length;
      }

      public boolean add(T element) {
        throw new UnsupportedOperationException();
      }

      public boolean addAll(Collection<? extends T> collection) {
        throw new UnsupportedOperationException();      
      }

      public void add(int index, T element) {
        throw new UnsupportedOperationException();
      }

      public boolean contains(Object element) {
        for (int i = 0; i < array.length; ++i) {
          if (equal(element, array[i])) {
            return true;
          }
        }
        return false;
      }

      public int indexOf(Object element) {
        for (int i = 0; i < array.length; ++i) {
          if (equal(element, array[i])) {
            return i;
          }
        }
        return -1;
      }

      public int lastIndexOf(Object element) {
        for (int i = array.length - 1; i >= 0; --i) {
          if (equal(element, array[i])) {
            return i;
          }
        }
        return -1;
      }

      public T get(int index) {
        return array[index];
      }

      public T set(int index, T value) {
        throw new UnsupportedOperationException();
      }

      public Object[] toArray() {
        return toArray(new Object[size()]);      
      }

      public <S> S[] toArray(S[] a) {
        return (S[])array;
      }

      public boolean isEmpty() {
        return size() == 0;
      }

      public T remove(int index) {
        throw new UnsupportedOperationException();        
      }

      public boolean remove(Object element) {
        throw new UnsupportedOperationException();
      }

      public void clear() {
        throw new UnsupportedOperationException();
      }

      public Iterator<T> iterator() {
        return listIterator();
      }

      public ListIterator<T> listIterator(int index) {
        return new Collections.ArrayListIterator(this, index);
      }

      public ListIterator<T> listIterator() {
        return listIterator(0);
      }
    };
  }

  public static void fill(int[] array, int value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

  public static void fill(char[] array, char value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }
  
  public static <T> void fill(T[] array, T value) {
    for (int i=0;i<array.length;i++) {
      array[i] = value;
    }
  }

}
