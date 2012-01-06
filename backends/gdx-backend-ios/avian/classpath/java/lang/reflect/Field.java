/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang.reflect;

import avian.VMField;
import avian.AnnotationInvocationHandler;
import avian.SystemClassLoader;
import avian.Classes;

import java.lang.annotation.Annotation;

public class Field<T> extends AccessibleObject {
  private static final int VoidField = 0;
  private static final int ByteField = 1;
  private static final int CharField = 2;
  private static final int DoubleField = 3;
  private static final int FloatField = 4;
  private static final int IntField = 5;
  private static final int LongField = 6;
  private static final int ShortField = 7;
  private static final int BooleanField = 8;
  private static final int ObjectField = 9;

  private final VMField vmField;
  private boolean accessible = true;

  public Field(VMField vmField) {
    this.vmField = vmField;
  }

  public boolean isAccessible() {
    return accessible;
  }

  public void setAccessible(boolean v) {
    accessible = v;
  }

  public Class<T> getDeclaringClass() {
    return SystemClassLoader.getClass(vmField.class_);
  }

  public int getModifiers() {
    return vmField.flags;
  }

  public String getName() {
    return getName(vmField);
  }

  public static String getName(VMField vmField) {
    return new String(vmField.name, 0, vmField.name.length - 1, false);
  }

  public Class getType() {
    return Class.forCanonicalName
      (vmField.class_.loader,
       new String(vmField.spec, 0, vmField.spec.length - 1, false));
  }

  public Object get(Object instance) throws IllegalAccessException {
    Object target;
    if ((vmField.flags & Modifier.STATIC) != 0) {
      target = vmField.class_.staticTable;
    } else if (Class.isInstance(vmField.class_, instance)) {
      target = instance;
    } else {
      throw new IllegalArgumentException();
    }

    switch (vmField.code) {
    case ByteField:
      return Byte.valueOf
        ((byte) getPrimitive(target, vmField.code, vmField.offset));

    case BooleanField:
      return Boolean.valueOf
        (getPrimitive(target, vmField.code, vmField.offset) != 0);

    case CharField:
      return Character.valueOf
        ((char) getPrimitive(target, vmField.code, vmField.offset));

    case ShortField:
      return Short.valueOf
        ((short) getPrimitive(target, vmField.code, vmField.offset));

    case IntField:
      return Integer.valueOf
        ((int) getPrimitive(target, vmField.code, vmField.offset));

    case LongField:
      return Long.valueOf
        ((int) getPrimitive(target, vmField.code, vmField.offset));

    case FloatField:
      return Float.valueOf
        (Float.intBitsToFloat
         ((int) getPrimitive(target, vmField.code, vmField.offset)));

    case DoubleField:
      return Double.valueOf
        (Double.longBitsToDouble
         (getPrimitive(target, vmField.code, vmField.offset)));

    case ObjectField:
      return getObject(target, vmField.offset);

    default:
      throw new Error();
    }
  }

  public boolean getBoolean(Object instance) throws IllegalAccessException {
    return ((Boolean) get(instance)).booleanValue();
  }

  public byte getByte(Object instance) throws IllegalAccessException {
    return ((Byte) get(instance)).byteValue();
  }

  public short getShort(Object instance) throws IllegalAccessException {
    return ((Short) get(instance)).shortValue();
  }

  public char getChar(Object instance) throws IllegalAccessException {
    return ((Character) get(instance)).charValue();
  }

  public int getInt(Object instance) throws IllegalAccessException {
    return ((Integer) get(instance)).intValue();
  }

  public float getFloat(Object instance) throws IllegalAccessException {
    return ((Float) get(instance)).floatValue();
  }

  public long getLong(Object instance) throws IllegalAccessException {
    return ((Long) get(instance)).longValue();
  }

  public double getDouble(Object instance) throws IllegalAccessException {
    return ((Double) get(instance)).doubleValue();
  }

  public void set(Object instance, Object value)
    throws IllegalAccessException
  {
    Object target;
    if ((vmField.flags & Modifier.STATIC) != 0) {
      target = vmField.class_.staticTable;
    } else if (Class.isInstance(vmField.class_, instance)) {
      target = instance;
    } else {
      throw new IllegalArgumentException();
    }

    switch (vmField.code) {
    case ByteField:
      setPrimitive(target, vmField.code, vmField.offset, (Byte) value);
      break;

    case BooleanField:
      setPrimitive
        (target, vmField.code, vmField.offset, ((Boolean) value) ? 1 : 0);
      break;

    case CharField:
      setPrimitive(target, vmField.code, vmField.offset, (Character) value);
      break;

    case ShortField:
      setPrimitive(target, vmField.code, vmField.offset, (Short) value);
      break;

    case IntField:
      setPrimitive(target, vmField.code, vmField.offset, (Integer) value);
      break;

    case LongField:
      setPrimitive(target, vmField.code, vmField.offset, (Long) value);
      break;

    case FloatField:
      setPrimitive(target, vmField.code, vmField.offset,
                   Float.floatToRawIntBits((Float) value));
      break;

    case DoubleField:
      setPrimitive(target, vmField.code, vmField.offset,
                   Double.doubleToRawLongBits((Double) value));
      break;

    case ObjectField:
      if (value == null || getType().isInstance(value)) {
        setObject(target, vmField.offset, value);
      } else {
        throw new IllegalArgumentException
          ("needed " + getType() + ", got "
           + value.getClass().getName() +
           " when setting " + Class.getName(vmField.class_) + "." + getName());
      }
      break;

    default:
      throw new Error();
    }
  }

  private Annotation getAnnotation(Object[] a) {
    if (a[0] == null) {
      a[0] = Proxy.newProxyInstance
        (vmField.class_.loader, new Class[] { (Class) a[1] },
         new AnnotationInvocationHandler(a));
    }
    return (Annotation) a[0];
  }

  public <T extends Annotation> T getAnnotation(Class<T> class_) {
    if (vmField.addendum.annotationTable != null) {
      Object[] table = (Object[]) vmField.addendum.annotationTable;
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
    if (vmField.addendum.annotationTable != null) {
      Object[] table = (Object[]) vmField.addendum.annotationTable;
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

  private static native long getPrimitive
    (Object instance, int code, int offset);

  private static native Object getObject
    (Object instance, int offset);

  private static native void setPrimitive
    (Object instance, int code, int offset, long value);

  private static native void setObject
    (Object instance, int offset, Object value);
}
