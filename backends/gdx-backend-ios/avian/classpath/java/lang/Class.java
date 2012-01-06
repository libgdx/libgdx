/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import avian.VMClass;
import avian.ClassAddendum;
import avian.AnnotationInvocationHandler;
import avian.SystemClassLoader;
import avian.Classes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.AnnotatedElement;
import java.lang.annotation.Annotation;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.security.ProtectionDomain;
import java.security.Permissions;
import java.security.AllPermission;

public final class Class <T> implements Type, AnnotatedElement {
  private static final int PrimitiveFlag = 1 << 5;

  public final VMClass vmClass;

  public Class(VMClass vmClass) {
    this.vmClass = vmClass;
  }

  public String toString() {
    return getName();
  }

  private static byte[] replace(int a, int b, byte[] s, int offset,
                                int length)
  {
    byte[] array = new byte[length];
    for (int i = 0; i < length; ++i) {
      byte c = s[i];
      array[i] = (byte) (c == a ? b : c);
    }
    return array;
  }

  public String getName() {
    return getName(vmClass);
  }

  public static String getName(VMClass c) {
    if (c.name == null) {
      if ((c.vmFlags & PrimitiveFlag) != 0) {
        if (c == Classes.primitiveClass('V')) {
          c.name = "void\0".getBytes();
        } else if (c == Classes.primitiveClass('Z')) {
          c.name = "boolean\0".getBytes();
        } else if (c == Classes.primitiveClass('B')) {
          c.name = "byte\0".getBytes();
        } else if (c == Classes.primitiveClass('C')) {
          c.name = "char\0".getBytes();
        } else if (c == Classes.primitiveClass('S')) {
          c.name = "short\0".getBytes();
        } else if (c == Classes.primitiveClass('I')) {
          c.name = "int\0".getBytes();
        } else if (c == Classes.primitiveClass('F')) {
          c.name = "float\0".getBytes();
        } else if (c == Classes.primitiveClass('J')) {
          c.name = "long\0".getBytes();
        } else if (c == Classes.primitiveClass('D')) {
          c.name = "double\0".getBytes();
        } else {
          throw new AssertionError();
        }
      } else {
        throw new AssertionError();
      }
    }

    return new String
      (replace('/', '.', c.name, 0, c.name.length - 1), 0, c.name.length - 1,
       false);
  }

  public String getCanonicalName() {
    if ((vmClass.vmFlags & PrimitiveFlag) != 0) {
      return getName();
    } else if (isArray()) {
      return getComponentType().getCanonicalName() + "[]";
    } else {
      return getName().replace('$', '.');
    }
  }

  public String getSimpleName() {
    if ((vmClass.vmFlags & PrimitiveFlag) != 0) {
      return getName();
    } else if (isArray()) {
      return getComponentType().getSimpleName() + "[]";
    } else {
      String name = getCanonicalName();
      int index = name.lastIndexOf('.');
      if (index >= 0) {
        return name.substring(index + 1);
      } else {
        return name;
      }
    }
  }

  public T newInstance()
    throws IllegalAccessException, InstantiationException
  {
    try {
      return (T) getConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static Class forName(String name) throws ClassNotFoundException {
    return forName(name, true, Method.getCaller().class_.loader);
  }

  public static Class forName(String name, boolean initialize,
                              ClassLoader loader)
    throws ClassNotFoundException
  {
    if (loader == null) {
      loader = Class.class.vmClass.loader;
    }
    Class c = loader.loadClass(name);
    Classes.link(c.vmClass, loader);
    if (initialize) {
      Classes.initialize(c.vmClass);
    }
    return c;
  }

  public static Class forCanonicalName(String name) {
    return forCanonicalName(null, name);
  }

  public static Class forCanonicalName(ClassLoader loader, String name) {
    try {
      if (name.startsWith("[")) {
        return forName(name, true, loader);
      } else if (name.startsWith("L")) {
        return forName(name.substring(1, name.length() - 1), true, loader);
      } else {
        if (name.length() == 1) {
          return SystemClassLoader.getClass
            (Classes.primitiveClass(name.charAt(0)));
        } else {
          throw new ClassNotFoundException(name);
        }
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public Class getComponentType() {
    if (isArray()) {
      String n = getName();
      if ("[Z".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('Z'));
      } else if ("[B".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('B'));
      } else if ("[S".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('S'));
      } else if ("[C".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('C'));
      } else if ("[I".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('I'));
      } else if ("[F".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('F'));
      } else if ("[J".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('J'));
      } else if ("[D".equals(n)) {
        return SystemClassLoader.getClass(Classes.primitiveClass('D'));
      }

      if (vmClass.staticTable == null) throw new AssertionError();
      return SystemClassLoader.getClass((VMClass) vmClass.staticTable);
    } else {
      return null;
    }
  }

  public boolean isAssignableFrom(Class c) {
    return Classes.isAssignableFrom(vmClass, c.vmClass);
  }

  private static Field findField(VMClass vmClass, String name) {
    if (vmClass.fieldTable != null) {
      Classes.link(vmClass);

      for (int i = 0; i < vmClass.fieldTable.length; ++i) {
        if (Field.getName(vmClass.fieldTable[i]).equals(name)) {
          return new Field(vmClass.fieldTable[i]);
        }
      }
    }
    return null;
  }

  public Field getDeclaredField(String name) throws NoSuchFieldException {
    Field f = findField(vmClass, name);
    if (f == null) {
      throw new NoSuchFieldException(name);
    } else {
      return f;
    }
  }

  public Field getField(String name) throws NoSuchFieldException {
    for (VMClass c = vmClass; c != null; c = c.super_) {
      Field f = findField(c, name);
      if (f != null) {
        return f;
      }
    }
    throw new NoSuchFieldException(name);
  }

  private static boolean match(Class[] a, Class[] b) {
    if (a.length == b.length) {
      for (int i = 0; i < a.length; ++i) {
        if (! a[i].isAssignableFrom(b[i])) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private static Method findMethod(VMClass vmClass, String name,
                                   Class[] parameterTypes)
  {
    if (vmClass.methodTable != null) {
      Classes.link(vmClass);

      if (parameterTypes == null) {
        parameterTypes = new Class[0];
      }

      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (Method.getName(vmClass.methodTable[i]).equals(name)
            && match(parameterTypes,
                     Method.getParameterTypes(vmClass.methodTable[i])))
        {
          return new Method(vmClass.methodTable[i]);
        }
      }
    }
    return null;
  }

  public Method getDeclaredMethod(String name, Class ... parameterTypes)
    throws NoSuchMethodException
  {
    if (name.startsWith("<")) {
      throw new NoSuchMethodException(name);
    }
    Method m = findMethod(vmClass, name, parameterTypes);
    if (m == null) {
      throw new NoSuchMethodException(name);
    } else {
      return m;
    }
  }

  public Method getMethod(String name, Class ... parameterTypes)
    throws NoSuchMethodException
  {
    if (name.startsWith("<")) {
      throw new NoSuchMethodException(name);
    }
    for (VMClass c = vmClass; c != null; c = c.super_) {
      Method m = findMethod(c, name, parameterTypes);
      if (m != null) {
        return m;
      }
    }
    throw new NoSuchMethodException(name);
  }

  public Constructor getConstructor(Class ... parameterTypes)
    throws NoSuchMethodException
  {
    Method m = findMethod(vmClass, "<init>", parameterTypes);
    if (m == null) {
      throw new NoSuchMethodException();
    } else {
      return new Constructor(m);
    }
  }

  public Constructor getDeclaredConstructor(Class ... parameterTypes)
    throws NoSuchMethodException
  {
    Constructor c = null;
    Constructor[] constructors = getDeclaredConstructors();

    for (int i = 0; i < constructors.length; ++i) {
      if (match(parameterTypes, constructors[i].getParameterTypes())) {
        c = constructors[i];
      }
    }

    if (c == null) {
      throw new NoSuchMethodException();
    } else {
      return c;
    }
  }

  private int countConstructors(boolean publicOnly) {
    int count = 0;
    if (vmClass.methodTable != null) {
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (((! publicOnly)
             || ((vmClass.methodTable[i].flags & Modifier.PUBLIC))
             != 0)
            && Method.getName(vmClass.methodTable[i]).equals("<init>"))
        {
          ++ count;
        }
      }
    }
    return count;
  }

  public Constructor[] getDeclaredConstructors() {
    Constructor[] array = new Constructor[countConstructors(false)];
    if (vmClass.methodTable != null) {
      Classes.link(vmClass);

      int index = 0;
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (Method.getName(vmClass.methodTable[i]).equals("<init>")) {
          array[index++] = new Constructor(new Method(vmClass.methodTable[i]));
        }
      }
    }

    return array;
  }

  public Constructor[] getConstructors() {
    Constructor[] array = new Constructor[countConstructors(true)];
    if (vmClass.methodTable != null) {
      Classes.link(vmClass);

      int index = 0;
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (((vmClass.methodTable[i].flags & Modifier.PUBLIC) != 0)
            && Method.getName(vmClass.methodTable[i]).equals("<init>"))
        {
          array[index++] = new Constructor(new Method(vmClass.methodTable[i]));
        }
      }
    }

    return array;
  }

  public Field[] getDeclaredFields() {
    if (vmClass.fieldTable != null) {
      Field[] array = new Field[vmClass.fieldTable.length];
      for (int i = 0; i < vmClass.fieldTable.length; ++i) {
        array[i] = new Field(vmClass.fieldTable[i]);
      }
      return array;
    } else {
      return new Field[0];
    }
  }

  private int countPublicFields() {
    int count = 0;
    if (vmClass.fieldTable != null) {
      for (int i = 0; i < vmClass.fieldTable.length; ++i) {
        if (((vmClass.fieldTable[i].flags & Modifier.PUBLIC)) != 0) {
          ++ count;
        }
      }
    }
    return count;
  }

  public Field[] getFields() {
    Field[] array = new Field[countPublicFields()];
    if (vmClass.fieldTable != null) {
      Classes.link(vmClass);

      int ai = 0;
      for (int i = 0; i < vmClass.fieldTable.length; ++i) {
        if (((vmClass.fieldTable[i].flags & Modifier.PUBLIC)) != 0) {
          array[ai++] = new Field(vmClass.fieldTable[i]);
        }
      }
    }
    return array;
  }

  private static void getAllFields(VMClass vmClass, ArrayList<Field> fields) {
    if (vmClass.super_ != null) {
      getAllFields(vmClass.super_, fields);
    }
    if (vmClass.fieldTable != null) {
      Classes.link(vmClass);

      for (int i = 0; i < vmClass.fieldTable.length; ++i) {
        fields.add(new Field(vmClass.fieldTable[i]));
      }
    }
  }

  public Field[] getAllFields() {
    ArrayList<Field> fields = new ArrayList<Field>();
    getAllFields(vmClass, fields);
    return fields.toArray(new Field[fields.size()]);
  }

  private int countMethods(boolean publicOnly) {
    int count = 0;
    if (vmClass.methodTable != null) {
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (((! publicOnly)
             || ((vmClass.methodTable[i].flags & Modifier.PUBLIC))
             != 0)
            && (! Method.getName(vmClass.methodTable[i]).startsWith("<")))
        {
          ++ count;
        }
      }
    }
    return count;
  }

  public Method[] getDeclaredMethods() {
    Method[] array = new Method[countMethods(false)];
    if (vmClass.methodTable != null) {
      Classes.link(vmClass);

      int ai = 0;
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (! Method.getName(vmClass.methodTable[i]).startsWith("<")) {
          array[ai++] = new Method(vmClass.methodTable[i]);
        }
      }
    }

    return array;
  }

  public Method[] getMethods() {
    Method[] array = new Method[countMethods(true)];
    if (vmClass.methodTable != null) {
      Classes.link(vmClass);

      int index = 0;
      for (int i = 0; i < vmClass.methodTable.length; ++i) {
        if (((vmClass.methodTable[i].flags & Modifier.PUBLIC) != 0)
            && (! Method.getName(vmClass.methodTable[i]).startsWith("<")))
        {
          array[index++] = new Method(vmClass.methodTable[i]);
        }
      }
    }

    return array;
  }

  public Class[] getInterfaces() {
    if (vmClass.interfaceTable != null) {
      Classes.link(vmClass);

      int stride = (isInterface() ? 1 : 2);
      Class[] array = new Class[vmClass.interfaceTable.length / stride];
      for (int i = 0; i < array.length; ++i) {
        array[i] = SystemClassLoader.getClass
          ((VMClass) vmClass.interfaceTable[i * stride]);
      }
      return array;
    } else {
      return new Class[0];
    }
  }

  public T[] getEnumConstants() {
    if (Enum.class.isAssignableFrom(this)) {
      try {
        return (T[]) getMethod("values").invoke(null);
      } catch (Exception e) {
        throw new Error();
      }
    } else {
      return null;
    }
  }

  public ClassLoader getClassLoader() {
    return vmClass.loader;
  }

  public int getModifiers() {
    return vmClass.flags;
  }

  public boolean isInterface() {
    return (vmClass.flags & Modifier.INTERFACE) != 0;
  }

  public Class getSuperclass() {
    return (vmClass.super_ == null ? null : SystemClassLoader.getClass(vmClass.super_));
  }

  public boolean isArray() {
    return vmClass.arrayDimensions != 0;
  }

  public static boolean isInstance(VMClass c, Object o) {
    return o != null && Classes.isAssignableFrom
      (c, Classes.getVMClass(o));
  }

  public boolean isInstance(Object o) {
    return isInstance(vmClass, o);
  }

  public boolean isPrimitive() {
    return (vmClass.vmFlags & PrimitiveFlag) != 0;
  }

  public URL getResource(String path) {
    if (! path.startsWith("/")) {
      String name = new String
        (vmClass.name, 0, vmClass.name.length - 1, false);
      int index = name.lastIndexOf('/');
      if (index >= 0) {
        path = name.substring(0, index) + "/" + path;
      }
    }
    return getClassLoader().getResource(path);
  }

  public InputStream getResourceAsStream(String path) {
    URL url = getResource(path);
    try {
      return (url == null ? null : url.openStream());
    } catch (IOException e) {
      return null;
    }
  }

  public boolean desiredAssertionStatus() {
    return false;
  }

  public <T> Class<? extends T> asSubclass(Class<T> c) {
    if (! c.isAssignableFrom(this)) {
      throw new ClassCastException();
    }

    return (Class<? extends T>) this;
  }

  public T cast(Object o) {
    return (T) o;
  }

  public Package getPackage() {
    if ((vmClass.vmFlags & PrimitiveFlag) != 0 || isArray()) {
      return null;
    } else {
      String name = getCanonicalName();
      int index = name.lastIndexOf('.');
      if (index >= 0) {
        return new Package(name.substring(0, index),
                           null, null, null, null, null, null, null, null);
      } else {
        return null;
      }
    }
  }

  public boolean isAnnotationPresent
    (Class<? extends Annotation> class_)
  {
    return getAnnotation(class_) != null;
  }

  private static Annotation getAnnotation(VMClass c, Object[] a) {
    if (a[0] == null) {
      a[0] = Proxy.newProxyInstance
        (c.loader, new Class[] { (Class) a[1] },
         new AnnotationInvocationHandler(a));
    }
    return (Annotation) a[0];
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    for (VMClass c = vmClass; c != null; c = c.super_) {
      if (c.addendum != null && c.addendum.annotationTable != null) {
        Classes.link(c, c.loader);
        
        Object[] table = (Object[]) c.addendum.annotationTable;
        for (int i = 0; i < table.length; ++i) {
          Object[] a = (Object[]) table[i];
          if (a[1] == class_) {
            return (T) getAnnotation(c, a);
          }
        }
      }
    }
    return null;
  }

  public Annotation[] getDeclaredAnnotations() {
    if (vmClass.addendum.annotationTable != null) {
      Classes.link(vmClass);

      Object[] table = (Object[]) vmClass.addendum.annotationTable;
      Annotation[] array = new Annotation[table.length];
      for (int i = 0; i < table.length; ++i) {
        array[i] = getAnnotation(vmClass, (Object[]) table[i]);
      }
      return array;
    } else {
      return new Annotation[0];
    }
  }

  private int countAnnotations() {
    int count = 0;
    for (VMClass c = vmClass; c != null; c = c.super_) {
      if (c.addendum != null && c.addendum.annotationTable != null) {
        count += ((Object[]) c.addendum.annotationTable).length;
      }
    }
    return count;
  }

  public Annotation[] getAnnotations() {
    Annotation[] array = new Annotation[countMethods(true)];
    int i = 0;
    for (VMClass c = vmClass; c != null; c = c.super_) {
      if (c.addendum != null && c.addendum.annotationTable != null) {
        Object[] table = (Object[]) c.addendum.annotationTable;
        for (int j = 0; j < table.length; ++j) {
          array[i++] = getAnnotation(vmClass, (Object[]) table[j]);
        }
      }
    }

    return array;
  }

  public ProtectionDomain getProtectionDomain() {
    Permissions p = new Permissions();
    p.add(new AllPermission());
    return new ProtectionDomain(null, p);
  }
}
