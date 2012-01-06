/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class StringBuffer implements CharSequence {
  private final StringBuilder sb;

  public StringBuffer(String s) {
    sb = new StringBuilder(s);
  }

  public StringBuffer(int capacity) {
    sb = new StringBuilder(capacity);
  }

  public StringBuffer() {
    this(0);
  }

  public synchronized StringBuffer append(String s) {
    sb.append(s);
    return this;
  }

  public synchronized StringBuffer append(CharSequence s) {
    sb.append(s);
    return this;
  }

  public synchronized StringBuffer append(StringBuffer s) {
    sb.append(s);
    return this;
  }

  public synchronized StringBuffer append(Object o) {
    sb.append(o);
    return this;
  }

  public synchronized StringBuffer append(char v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(boolean v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(int v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(long v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(float v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(double v) {
    sb.append(v);
    return this;
  }

  public synchronized StringBuffer append(char[] b, int offset, int length) {
    sb.append(b, offset, length);
    return this;
  }

  public synchronized StringBuffer append(char[] b) {
    sb.append(b, 0, b.length);
    return this;
  }

  public synchronized int indexOf(String s) {
    return sb.indexOf(s);
  }

  public synchronized int indexOf(String s, int fromIndex) {
    return sb.indexOf(s, fromIndex);
  }

  public synchronized StringBuffer insert(int i, String s) {
    sb.insert(i, s);
    return this;
  }

  public synchronized StringBuffer insert(int i, char c) {
    sb.insert(i, c);
    return this;
  }

  public synchronized StringBuffer insert(int i, int v) {
    sb.insert(i, v);
    return this;
  }

  public synchronized StringBuffer delete(int start, int end) {
    sb.delete(start, end);
    return this;
  }

  public synchronized StringBuffer deleteCharAt(int i) {
    sb.deleteCharAt(i);
    return this;
  }

  public synchronized char charAt(int i) {
    return sb.charAt(i);
  }

  public synchronized int length() {
    return sb.length();
  }

  public synchronized StringBuffer replace(int start, int end, String str) {
    sb.replace(start, end, str);
    return this;
  }

  public synchronized void setLength(int v) {
    sb.setLength(v);
  }

  public synchronized void setCharAt(int index, char ch) {
    sb.setCharAt(index, ch);
  }

  public synchronized void getChars(int srcStart, int srcEnd, char[] dst,
                                    int dstStart)
  {
    sb.getChars(srcStart, srcEnd, dst, dstStart);
  }

  public synchronized String toString() {
    return sb.toString();
  }
  
  public String substring(int start, int end) {
    return sb.substring(start, end);
  }
  
  public CharSequence subSequence(int start, int end) {
    return sb.subSequence(start, end);
  }
}
