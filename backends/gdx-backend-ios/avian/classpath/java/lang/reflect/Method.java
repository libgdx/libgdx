/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import avian.VMMethod;
import avian.AnnotationInvocationHandler;
import avian.SystemClassLoader;

import java.lang.annotation.Annotation;

public class Method<T> extends AccessibleObject implements Member {
  private final VMMethod vmMethod;
  private boolean accessible;

  public Method(VMMethod vmMethod) {
    this.vmMethod = vmMethod;
  }

  public boolean isAccessible() {
    return accessible;
  }

  public void setAccessible(boolean v) {
    accessible = v;
  }

  public static native VMMethod getCaller();

  public Class<T> getDeclaringClass() {
    return SystemClassLoader.getClass(vmMethod.class_);
  }

  public int getModifiers() {
    return vmMethod.flags;
  }

  public String getName() {
    return getName(vmMethod);
  }

  public static String getName(VMMethod vmMethod) {
    return new String(vmMethod.name, 0, vmMethod.name.length - 1, false);
  }

  private String getSpec() {
    return getSpec(vmMethod);
  }

  public static String getSpec(VMMethod vmMethod) {
    return new String(vmMethod.spec, 0, vmMethod.spec.length - 1, false);
  }

  private static int next(char c, String s, int start) {
    for (int i = start; i < s.length(); ++i) {
      if (s.charAt(i) == c) return i;
    }
    throw new RuntimeException();
  }

  public Class[] getParameterTypes() {
    return getParameterTypes(vmMethod);
  }

  public static Class[] getParameterTypes(VMMethod vmMethod) {
    int count = vmMethod.parameterCount;

    Class[] types = new Class[count];
    int index = 0;

    String spec = new String
      (vmMethod.spec, 1, vmMethod.spec.length - 1, false);

    try {
      for (int i = 0; i < spec.length(); ++i) {
        char c = spec.charAt(i);
        if (c == ')') {
          break;
        } else if (c == 'L') {
          int start = i + 1;
          i = next(';', spec, start);
          String name = spec.substring(start, i).replace('/', '.');
          types[index++] = Class.forName(name, true, vmMethod.class_.loader);
        } else if (c == '[') {
          int start = i;
          while (spec.charAt(i) == '[') ++i;

          if (spec.charAt(i) == 'L') {
            i = next(';', spec, i + 1);
            String name = spec.substring(start, i).replace('/', '.');
            types[index++] = Class.forName
              (name, true, vmMethod.class_.loader);
          } else {
            String name = spec.substring(start, i + 1);
            types[index++] = Class.forCanonicalName
              (vmMethod.class_.loader, name);
          }
        } else {
          String name = spec.substring(i, i + 1);
          types[index++] = Class.forCanonicalName
            (vmMethod.class_.loader, name);
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    return types;
  }

  public Object invoke(Object instance, Object ... arguments)
    throws InvocationTargetException, IllegalAccessException
  {
    if ((vmMethod.flags & Modifier.STATIC) != 0
        || Class.isInstance(vmMethod.class_, instance))
    {
      if ((vmMethod.flags & Modifier.STATIC) != 0) {
        instance = null;
      }

      if (arguments == null) {
        if (vmMethod.parameterCount > 0) {
          throw new NullPointerException();
        }
        arguments = new Object[0];
      }

      if (arguments.length == vmMethod.parameterCount) {
        return invoke(vmMethod, instance, arguments);        
      } else {
        throw new ArrayIndexOutOfBoundsException();
      }
    } else {
//       System.out.println
//         (getDeclaringClass() + "." + getName() + " flags: " + vmMethod.flags + " vm flags: " + vmMethod.vmFlags + " return code: " + vmMethod.returnCode);
      throw new IllegalArgumentException();
    }
  }

  private static native Object invoke(VMMethod method, Object instance,
                                      Object ... arguments)
    throws InvocationTargetException, IllegalAccessException;

  public Class getReturnType() {
    for (int i = 0; i < vmMethod.spec.length - 1; ++i) {
      if (vmMethod.spec[i] == ')') {
        return Class.forCanonicalName
          (vmMethod.class_.loader,
           new String
           (vmMethod.spec, i + 1, vmMethod.spec.length - i - 2, false));
      }
    }
    throw new RuntimeException();
  }

  private Annotation getAnnotation(Object[] a) {
    if (a[0] == null) {
      a[0] = Proxy.newProxyInstance
        (vmMethod.class_.loader, new Class[] { (Class) a[1] },
         new AnnotationInvocationHandler(a));
    }
    return (Annotation) a[0];
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    if (vmMethod.addendum.annotationTable != null) {
      Object[] table = (Object[]) vmMethod.addendum.annotationTable;
      for (int i = 0; i < table.length; ++i) {
        Object[] a = (Object[]) table[i];
        if (a[1] == class_) {
          return (T) getAnnotation(a);
        }
      }
    }
    return null;
  }

  public Annotation[] getAnnotations() {
    if (vmMethod.addendum.annotationTable != null) {
      Object[] table = (Object[]) vmMethod.addendum.annotationTable;
      Annotation[] array = new Annotation[table.length];
      for (int i = 0; i < table.length; ++i) {
        array[i] = getAnnotation((Object[]) table[i]);
      }
      return array;
    } else {
      return new Annotation[0];
    }
  }

  public Annotation[] getDeclaredAnnotations() {
    return getAnnotations();
  }
}
