/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.regex;

/**
 * This is a work in progress.
 * 
 * @author zsombor and others
 */
public class Matcher {
  private final Pattern pattern;
  private CharSequence input;
  private int start;
  private int end;

  Matcher(Pattern pattern, CharSequence input) {
    this.pattern = pattern;
    this.input = input;
  }

  public boolean matches() {
    if (pattern.pattern().equals(input.toString())) {
      start = 0;
      end = input.length();
      return true;
    } else {
      return false;
    }
  }

  public Matcher reset() {
    return reset(input);
  }

  public Matcher reset(CharSequence input) {
    this.input = input;
    start = 0;
    end = 0;
    return this;
  }

  public int start() {
    return start;
  }

  public String replaceAll(String replacement) {
    return replace(replacement, Integer.MAX_VALUE);
  }

  public String replaceFirst(String replacement) {
    return replace(replacement, 1);
  }

  private String replace(String replacement, int limit) {
    reset();

    StringBuilder sb = null;
    int index = 0;
    int count = 0;
    while (count < limit && index < input.length()) {
      if (find(index)) {
        if (sb == null) {
          sb = new StringBuilder();
        }
        if (start > index) {
          sb.append(input.subSequence(index, start));
        }
        sb.append(replacement);
        index = end;
        ++ count;
      } else if (index == 0) {
        return input.toString();
      } else {
        break;
      }
    }
    if (index < input.length()) {
      sb.append(input.subSequence(index, input.length()));
    }
    return sb.toString();
  }

  public int end() {
    return end;
  }

  public boolean find() {
    return find(end);
  }

  public boolean find(int start) {
    String p = pattern.pattern();
    int i = Pattern.indexOf(input, p, start);
    if (i >= 0) {
      this.start = i;
      this.end = i + p.length();
      return true;
    } else {
      return false;
    }
  }
}
