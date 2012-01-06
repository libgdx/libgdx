/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class Object {
  protected Object clone() throws CloneNotSupportedException {
    if ((this instanceof Cloneable) || getClass().isArray()) {
      return clone(this);
    } else {
      throw new CloneNotSupportedException(getClass().getName());
    }
  }

  private static native Object clone(Object o);

  public boolean equals(Object o) {
    return this == o;
  }

  protected void finalize() throws Throwable { }

  public final Class<? extends Object> getClass() {
    return avian.SystemClassLoader.getClass(getVMClass(this));
  }

  private static native avian.VMClass getVMClass(Object o);

  public native int hashCode();

  public native final void notify();

  public native final void notifyAll();

  public native String toString();

  public final void wait() throws InterruptedException {
    wait(0);
  }

  public native final void wait(long milliseconds) throws InterruptedException;

  public final void wait(long milliseconds, int nanoseconds)
    throws InterruptedException
  {
    if (nanoseconds != 0) {
      ++ milliseconds;
    }
    wait(milliseconds);
  }
}
