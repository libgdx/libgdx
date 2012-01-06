/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class PrintStream extends OutputStream {
  private final OutputStream out;
  private final boolean autoFlush;

  private static class Static {
    private static final byte[] newline
      = System.getProperty("line.separator").getBytes();
  }

  public PrintStream(OutputStream out, boolean autoFlush) {
    this.out = out;
    this.autoFlush = autoFlush;
  }

  public PrintStream(OutputStream out) {
    this(out, false);
  }

  public synchronized void print(String s) {
    try {
      out.write(s.getBytes());
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void print(Object o) {
    print(String.valueOf(o));
  }

  public void print(boolean v) {
    print(String.valueOf(v));
  }

  public void print(char c) {
    print(String.valueOf(c));
  }

  public void print(int v) {
    print(String.valueOf(v));
  }

  public void print(long v) {
    print(String.valueOf(v));
  }

  public void print(float v) {
    print(String.valueOf(v));
  }

  public void print(double v) {
    print(String.valueOf(v));
  }

  public void print(char[] s) {
    print(String.valueOf(s));
  }

  public synchronized void println(String s) {
    try {
      out.write(s.getBytes());    
      out.write(Static.newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public synchronized void println() {
    try {
      out.write(Static.newline);
      if (autoFlush) flush();
    } catch (IOException e) { }
  }

  public void println(Object o) {
    println(String.valueOf(o));
  }

  public void println(boolean v) {
    println(String.valueOf(v));
  }

  public void println(char c) {
    println(String.valueOf(c));
  }

  public void println(int v) {
    println(String.valueOf(v));
  }

  public void println(long v) {
    println(String.valueOf(v));
  }

  public void println(float v) {
    println(String.valueOf(v));
  }

  public void println(double v) {
    println(String.valueOf(v));
  }

  public void println(char[] s) {
    println(String.valueOf(s));
  }
  
  public void write(int c) throws IOException {
    out.write(c);
    if (autoFlush && c == '\n') flush();
  }

  public void write(byte[] buffer, int offset, int length) throws IOException {
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
