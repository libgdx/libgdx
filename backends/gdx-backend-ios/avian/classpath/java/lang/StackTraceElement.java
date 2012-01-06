/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

public class StackTraceElement {
  private static int NativeLine = -1;

  private String class_;
  private String method;
  private String file;
  private int line;

  public StackTraceElement(String class_, String method, String file,
                           int line)
  {
    this.class_ = class_;
    this.method = method;
    this.file = file;
    this.line = line;
  }

  public int hashCode() {
    return class_.hashCode() ^ method.hashCode() ^ line;
  }

  public boolean equals(Object o) {
    if (o instanceof StackTraceElement) {
      StackTraceElement e = (StackTraceElement) o;
      return class_.equals(e.class_)
        && method.equals(e.method)
        && line == e.line;
    } else {
      return false;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(class_).append(".").append(method);

    if (line == NativeLine) {
      sb.append(" (native)");
    } else if (line >= 0) {
      sb.append(" (line ").append(line).append(")");
    }

    return sb.toString();
  }

  public String getClassName() {
    return class_;
  }

  public String getMethodName() {
    return method;
  }

  public String getFileName() {
    return file;
  }

  public int getLineNumber() {
    return line;
  }

  public boolean isNativeMethod() {
    return line == NativeLine;
  }
}
