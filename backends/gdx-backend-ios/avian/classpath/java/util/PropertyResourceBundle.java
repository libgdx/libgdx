/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

import java.io.InputStream;
import java.io.IOException;

public class PropertyResourceBundle extends ResourceBundle {
  private final Properties map = new Properties();

  public PropertyResourceBundle(InputStream in) throws IOException {
    map.load(in);
  }

  public Object handleGetObject(String key) {
    return map.get(key);
  }

  public Enumeration<String> getKeys() {
    return map.keys();
  }
}
