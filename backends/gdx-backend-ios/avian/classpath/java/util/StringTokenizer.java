/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util;

public class StringTokenizer implements Enumeration {
  private final String in;
  private String delimiters;
  private final boolean includeDelimiters;
  private int position;

  public StringTokenizer(String in, String delimiters,
                         boolean includeDelimiters)
  {
    this.in = in;
    this.delimiters = delimiters;
    this.includeDelimiters = includeDelimiters;
  }

  public StringTokenizer(String in, String delimiters) {
    this(in, delimiters, false);
  }

  public StringTokenizer(String in) {
    this(in, " \t\r\n\f");
  }

  private boolean isDelimiter(char c) {
    return delimiters.indexOf(c) >= 0;
  }

  public int countTokens() {
    int count = 0;
    boolean sawNonDelimiter = false;
    for (int i = position; i < in.length(); ++i) {
      if (isDelimiter(in.charAt(i))) {
        if (sawNonDelimiter) {
          sawNonDelimiter = false;
          ++ count;
        }

        if (includeDelimiters) {
          ++ count;
        }
      } else {
        sawNonDelimiter = true;
      }
    }

    if (sawNonDelimiter) {
      ++ count;
    }

    return count;
  }

  public boolean hasMoreTokens() {
    for (int i = position; i < in.length(); ++i) {
      if (isDelimiter(in.charAt(i))) {
        if (includeDelimiters) {
          return true;
        }
      } else {
        position = i;
        return true;
      }
    }
    return false;
  }

  public String nextToken() {
    for (int i = position; i < in.length(); ++i) {
      if (isDelimiter(in.charAt(i))) {
        if (includeDelimiters) {
          return in.substring(i, i + 1);
        }
      } else {
        position = i;
        while (position < in.length() && ! isDelimiter(in.charAt(position))) {
          ++ position;
        }
        return in.substring(i, position);
      }
    }
    throw new NoSuchElementException();
  }

  public String nextToken(String delimiters) {
    this.delimiters = delimiters;
    return nextToken();
  }

  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  public Object nextElement() {
    return nextToken();
  }

}
