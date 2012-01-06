/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Serializable;

public class Throwable implements Serializable {
  private String message;
  private Object trace;
  private Throwable cause;

  public Throwable(String message, Throwable cause) {
    this.message = message;
    this.trace = trace(1);
    this.cause = cause;
  }

  public Throwable(String message) {
    this(message, null);
  }

  public Throwable(Throwable cause) {
    this(null, cause);
  }

  public Throwable() {
    this(null, null);
  }

  public Throwable getCause() {
    return cause;
  }

  public Throwable initCause(Throwable e) {
    if (cause == null) {
      cause = e;
      return this;
    } else {
      throw new IllegalStateException();
    }
  }

  public String getMessage() {
    return message;
  }

  public String getLocalizedMessage() {
    return getMessage();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getName());
    if (message != null) {
      sb.append(": ").append(message);
    }
    return sb.toString();
  }

  private static native Object trace(int skipCount);

  static native StackTraceElement[] resolveTrace(Object trace);

  private StackTraceElement[] resolveTrace() {
    if (! (trace instanceof StackTraceElement[])) {
      trace = resolveTrace(trace);
    }
    return (StackTraceElement[]) trace;
  }

  public StackTraceElement[] getStackTrace() {
    return resolveTrace();
  }

  public void setStackTrace(StackTraceElement[] trace) {
    this.trace = trace;
  }

  public void printStackTrace(PrintStream out) {
    StringBuilder sb = new StringBuilder();
    printStackTrace(sb, System.getProperty("line.separator"));
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace(PrintWriter out) {
    StringBuilder sb = new StringBuilder();
    printStackTrace(sb, System.getProperty("line.separator"));
    out.print(sb.toString());
    out.flush();
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  private void printStackTrace(StringBuilder sb, String nl) {
    sb.append(toString()).append(nl);

    StackTraceElement[] trace = resolveTrace();
    for (int i = 0; i < trace.length; ++i) {
      sb.append("  at ").append(trace[i].toString()).append(nl);
    }

    if (cause != null) {
      sb.append("caused by: ");
      cause.printStackTrace(sb, nl);
    }
  }

  public Throwable fillInStackTrace() {
    trace = trace(0);
    return this;
  }
}
