/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

public class AnnotationInvocationHandler implements InvocationHandler {
  private Object[] data;

  public AnnotationInvocationHandler(Object[] data) {
    this.data = data;
  }
    
  public Object invoke(Object proxy, Method method, Object[] arguments) {
    for (int i = 2; i < data.length; i += 2) {
      if (method.getName().equals(data[i])) {
        return data[i + 1];
      }
    }
    throw new IllegalArgumentException();
  }
}
