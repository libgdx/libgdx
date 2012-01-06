/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import java.lang.annotation.Annotation;

public abstract class AccessibleObject implements AnnotatedElement {
  protected static final int Accessible = 1 << 0;

  public boolean isAnnotationPresent
    (Class<? extends Annotation> class_)
  {
    return getAnnotation(class_) != null;
  }

  public abstract boolean isAccessible();

  public abstract void setAccessible(boolean v);

  public static void setAccessible(AccessibleObject[] array, boolean v) {
    for (AccessibleObject o: array) o.setAccessible(v);
  }
}
