/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import java.lang.annotation.Annotation;

public interface AnnotatedElement {
  public boolean isAnnotationPresent(Class<? extends Annotation> class_);

  public <T extends Annotation> T getAnnotation(Class<T> class_);

  public Annotation[] getAnnotations();

  public Annotation[] getDeclaredAnnotations();
}
