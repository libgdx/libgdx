/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.net.URL;

public class Package {
  private final String name;
  private final String implementationTitle;
  private final String implementationVendor;
  private final String implementationVersion;
  private final String specementationTitle;
  private final String specementationVendor;
  private final String specementationVersion;
  private final URL sealed;
  private final ClassLoader loader;

  Package(String name,
          String implementationTitle, 
          String implementationVendor, 
          String implementationVersion, 
          String specementationTitle, 
          String specementationVendor, 
          String specementationVersion, 
          URL sealed, 
          ClassLoader loader)
  {
    this.name = name;
    this.implementationTitle = implementationTitle;
    this.implementationVendor = implementationVendor;
    this.implementationVersion = implementationVersion;
    this.specementationTitle = specementationTitle;
    this.specementationVendor = specementationVendor;
    this.specementationVersion = specementationVersion;
    this.sealed = sealed;
    this.loader = loader;
  }

  public String getName() {
    return name;
  }
}
