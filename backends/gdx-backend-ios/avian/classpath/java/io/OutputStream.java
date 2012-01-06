/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public abstract class OutputStream {
  public abstract void write(int c) throws IOException;

  public void write(byte[] buffer) throws IOException {
    write(buffer, 0, buffer.length);
  }

  public void write(byte[] buffer, int offset, int length) throws IOException {
    for (int i = 0; i < length; ++i) {
      write(buffer[offset + i]);
    }
  }

  public void flush() throws IOException { }

  public void close() throws IOException { }
}
