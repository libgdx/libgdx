/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.text;

import java.util.Locale;

public class MessageFormat extends Format {
  private String pattern;
  private final Locale locale;

  public MessageFormat(String pattern, Locale locale) {
    this.pattern = pattern;
    this.locale = locale;
  }

  public MessageFormat(String pattern) {
    this(pattern, Locale.getDefault());
  }

  public StringBuffer format(Object[] args, StringBuffer target,
                             FieldPosition p)
  {
    // todo
    return target.append(pattern);
  }

  public StringBuffer format(Object args, StringBuffer target, FieldPosition p)
  {
    return format((Object[]) args, target, p);
  }

  public static String format(String pattern, Object ... args) {
    return new MessageFormat
      (pattern).format(args, new StringBuffer(), new FieldPosition(0))
      .toString();
  }

  public void applyPattern(String pattern) {
    this.pattern = pattern;
  }
}
