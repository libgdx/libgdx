/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class PrintWriter extends Writer {
  private static final char[] newline
    = System.getProperty("line.separator").toCharArray();

  private final Writer out;
  private final boolean autoFlush;

  public PrintWriter(Writer out, boolean autoFlush) {
    this.out = out;
    this.autoFlush = autoFlush;
  }

  public PrintWriter(Writer out) {
    this(out, false);
  }

  public PrintWriter(OutputStream out, boolean autoFlush) {
    this(new OutputStreamWriter(out), autoFlush);
  }

  public PrintWriter(OutputStream out) {
    this(out, false);
  }

  public synchronized void print(String s) {
    try {
      out.write(s.toCharArray());
    } catch (IOException e) { }
  }

  public void print(Object o) {
    print(o.toString());
  }

  public void print(char c) {
    print(String.valueOf(c));
  }

  public synchronized void println(String s) {
    try {
      out.write(s.toCharArray());    
      out.write(newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public synchronized void println() {
    try {
      out.write(newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void println(Object o) {
    println(o.toString());
  }

  public void println(char c) {
    println(String.valueOf(c));
  }

  public void write(char[] buffer, int offset, int length) throws IOException {
    out.write(buffer, offset, length);
    if (autoFlush) flush();
  }

  public void flush() {
    try {
      out.flush();
    } catch (IOException e) { }
  }

  public void close() {
    try {
      out.close();
    } catch (IOException e) { }
  }
}
