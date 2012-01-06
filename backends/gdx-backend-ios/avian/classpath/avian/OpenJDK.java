/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.net.URL;
import java.net.MalformedURLException;
import java.security.CodeSource;
import java.security.AllPermission;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

public class OpenJDK {  
  public static ProtectionDomain getProtectionDomain(VMClass c) {
    CodeSource source = null;
    if (c.source != null) {
      try {
        source = new CodeSource
          (new URL(new String(c.source, 0, c.source.length - 1)),
           (Certificate[]) null);
      } catch (MalformedURLException ignored) { }
    }

    Permissions p = new Permissions();
    p.add(new AllPermission());

    return new ProtectionDomain(source, p);
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

  public static Class getDeclaringClass(VMClass c) {
    try {
      String name = new String
        (replace('/', '.', c.name, 0, c.name.length - 1), 0,
         c.name.length - 1);
      int index = name.lastIndexOf("$");
      if (index == -1) {
        return null;
      } else {
        return c.loader.loadClass(name.substring(0, index));
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
