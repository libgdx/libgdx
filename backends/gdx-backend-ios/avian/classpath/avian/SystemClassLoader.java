/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Enumeration;

public class SystemClassLoader extends ClassLoader {
  private native VMClass findVMClass(String name)
    throws ClassNotFoundException;

  protected Class findClass(String name) throws ClassNotFoundException {
    return getClass(findVMClass(name));
  }

  public static native Class getClass(VMClass vmClass);

  private native VMClass findLoadedVMClass(String name);

  protected Class reallyFindLoadedClass(String name){
    VMClass c = findLoadedVMClass(name);
    return c == null ? null : getClass(c);
  }

  private native String resourceURLPrefix(String name);

  protected URL findResource(String name) {
    String prefix = resourceURLPrefix(name);
    if (prefix != null) {
      try {
        return new URL(prefix + name);
      } catch (MalformedURLException ignored) { }
    }
    return null;
  }

  // OpenJDK's java.lang.ClassLoader.getResource makes use of
  // sun.misc.Launcher to load bootstrap resources, which is not
  // appropriate for the Avian build, so we override it to ensure we
  // get the behavior we want.  This implementation is the same as
  // that of Avian's java.lang.ClassLoader.getResource.
  public URL getResource(String path) {
    URL url = null;
    ClassLoader parent = getParent();
    if (parent != null) {
      url = parent.getResource(path);
    }

    if (url == null) {
      url = findResource(path);
    }

    return url;
  }

  // As above, we override this method to avoid inappropriate behavior
  // in OpenJDK's java.lang.ClassLoader.getResources.
  public Enumeration<URL> getResources(String name) throws IOException {
    Collection<URL> urls = new ArrayList<URL>(5);

    ClassLoader parent = getParent();
    if (parent != null) {
      for (Enumeration<URL> e = parent.getResources(name);
           e.hasMoreElements();)
      {
        urls.add(e.nextElement());
      }
    }

    URL url = findResource(name);
    if (url != null) {
      urls.add(url);
    }

    return Collections.enumeration(urls);
  }

  protected Enumeration<URL> findResources(String name) {
    Collection<URL> urls = new ArrayList(1);
    URL url = findResource(name);
    if (url != null) {
      urls.add(url);
    }
    return Collections.enumeration(urls);
  }
}
