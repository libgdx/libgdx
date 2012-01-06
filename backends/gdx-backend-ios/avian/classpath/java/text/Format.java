/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.text;

public abstract class Format {
  public final String format(Object o) {
    return format(o, new StringBuffer(), new FieldPosition(0)).toString();
  }

  public abstract StringBuffer format(Object o, StringBuffer target,
                                      FieldPosition p);
}
