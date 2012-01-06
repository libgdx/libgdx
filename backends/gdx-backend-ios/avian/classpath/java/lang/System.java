/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.PrintStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileDescriptor;
import java.util.Properties;

public abstract class System {
  private static final long NanoTimeBaseInMillis = currentTimeMillis();
  
  private static Property properties;
  
  private static SecurityManager securityManager;
  //   static {
  //     loadLibrary("natives");
  //   }

  public static final PrintStream out = new PrintStream
    (new BufferedOutputStream(new FileOutputStream(FileDescriptor.out)), true);

  public static final PrintStream err = new PrintStream
    (new BufferedOutputStream(new FileOutputStream(FileDescriptor.err)), true);

  public static final InputStream in
    = new BufferedInputStream(new FileInputStream(FileDescriptor.in));

  public static native void arraycopy(Object src, int srcOffset, Object dst,
                                      int dstOffset, int length);

  public static String getProperty(String name) {
    for (Property p = properties; p != null; p = p.next) {
      if (p.name.equals(name)) {
        return p.value;
      }
    }

    boolean[] found = new boolean[1];
    String value = getProperty(name, found);
    if (found[0]) return value;

    value = getVMProperty(name, found);
    if (found[0]) return value;

    return null;
  }
  
  public static String getProperty(String name, String defaultValue) {
    String result = getProperty(name);
    if (result==null) {
      return defaultValue;
    }
    return result;
  }
  

  public static String setProperty(String name, String value) {
    for (Property p = properties; p != null; p = p.next) {
      if (p.name.equals(name)) {
        String oldValue = p.value;
        p.value = value;
        return oldValue;
      }
    }

    properties = new Property(name, value, properties);
    return null;
  }

  public static Properties getProperties () {
    Properties prop = new Properties();
    for (Property p = properties; p != null; p = p.next) {
      prop.put(p.name, p.value);
    }
    return prop;
  }
  
  private static native String getProperty(String name, boolean[] found);

  private static native String getVMProperty(String name, boolean[] found);

  public static native long currentTimeMillis();

  public static native int identityHashCode(Object o);

  public static long nanoTime() {
    return (currentTimeMillis() - NanoTimeBaseInMillis) * 1000000;
  }

  public static String mapLibraryName(String name) {
    if (name != null) {
      return doMapLibraryName(name);
    } else {
      throw new NullPointerException();
    }
  }

  private static native String doMapLibraryName(String name);

  public static void load(String path) {
    Runtime.getRuntime().load(path);
  }

  public static void loadLibrary(String name) {
    Runtime.getRuntime().loadLibrary(name);
  }

  public static void gc() {
    Runtime.getRuntime().gc();
  }

  public static void exit(int code) {
    Runtime.getRuntime().exit(code);
  }
  
  public static SecurityManager getSecurityManager() {
    return securityManager;
  }
  
  public static void setSecurityManager(SecurityManager securityManager) {
    System.securityManager = securityManager;
  }

  private static class Property {
    public final String name;
    public String value;
    public final Property next;

    public Property(String name, String value, Property next) {
      this.name = name;
      this.value = value;
      this.next = next;
    }
  }
}
