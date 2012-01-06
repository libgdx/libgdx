/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

import java.net.URL;
import java.security.cert.Certificate;

public class CodeSource {
  private final URL url;

  public CodeSource(URL url, Certificate[] certificates) {
    this.url = url;
  }

  public URL getLocation() {
    return url;
  }
}
