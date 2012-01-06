/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

public abstract class ClassLoader {
  private final ClassLoader parent;

  protected ClassLoader(ClassLoader parent) {
    if (parent == null) {
      this.parent = getSystemClassLoader();
    } else {
      this.parent = parent;
    }
  }

  protected ClassLoader() {
    this(getSystemClassLoader());
  }

  public static ClassLoader getSystemClassLoader() {
    return ClassLoader.class.getClassLoader();
  }

  protected Class defineClass(String name, byte[] b, int offset, int length) {
    if (b == null) {
      throw new NullPointerException();
    }

    if (offset < 0 || offset > length || offset + length > b.length) {
      throw new IndexOutOfBoundsException();
    }

    return avian.SystemClassLoader.getClass
      (avian.Classes.defineVMClass(this, b, offset, length));
  }

  protected Class findClass(String name) throws ClassNotFoundException {
    throw new ClassNotFoundException();
  }

  protected Class reallyFindLoadedClass(String name) {
    return null;
  }

  protected final Class findLoadedClass(String name) {
    return reallyFindLoadedClass(name);
  }

  public Class loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, false);
  }

  protected Class loadClass(String name, boolean resolve)
    throws ClassNotFoundException
  {
    Class c = findLoadedClass(name);
    if (c == null) {
      if (parent != null) {
        try {
          c = parent.loadClass(name);
        } catch (ClassNotFoundException ok) { }
      }

      if (c == null) {
        c = findClass(name);
      }
    }

    if (resolve) {
      resolveClass(c);
    }

    return c;
  }

  protected void resolveClass(Class c) {
    avian.Classes.link(c.vmClass, this);
  }

  public final ClassLoader getParent() {
    return parent;
  }
  
  protected URL findResource(String path) {
    return null;
  }

  protected Enumeration<URL> findResources(String name) throws IOException {
    return Collections.enumeration(new ArrayList<URL>(0));
  }

  public URL getResource(String path) {
    URL url = null;
    if (parent != null) {
      url = parent.getResource(path);
    }

    if (url == null) {
      url = findResource(path);
    }

    return url;
  }

  public InputStream getResourceAsStream(String path) {
    URL url = getResource(path);
    try {
      return (url == null ? null : url.openStream());
    } catch (IOException e) {
      return null;
    }
  }

  public static URL getSystemResource(String path) {
    return getSystemClassLoader().getResource(path);
  }

  public static InputStream getSystemResourceAsStream(String path) {
    return getSystemClassLoader().getResourceAsStream(path);
  }
  
  public static Enumeration<URL> getSystemResources(String name) throws IOException {
    return getSystemClassLoader().getResources(name);
  }
  
  public Enumeration<URL> getResources(String name)
    throws IOException {
    Collection<URL> resources = collectResources(name);
    return Collections.enumeration(resources);
  }

  private Collection<URL> collectResources(String name) {
    Collection<URL> urls = parent != null ? parent.collectResources(name) : new ArrayList<URL>(5);
    URL url = findResource(name);
    if (url != null) {
      urls.add(url);
    }
    return urls;
  }
  
}
