/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public abstract class InputStream {
  public abstract int read() throws IOException;

  public int read(byte[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte[] buffer, int offset, int length) throws IOException {
    for (int i = 0; i < length; ++i) {
      int c = read();
      if (c == -1) {
        if (i == 0) {
          return -1;
        } else {
          return i;
        }
      } else {
        buffer[offset + i] = (byte) (c & 0xFF);
      }
    }
    return length;
  }

  public long skip(long count) throws IOException {
    final long Max = 8 * 1024;
    int size = (int) (count < Max ? count : Max);
    byte[] buffer = new byte[size];
    long remaining = count;
    int c;
    while ((c = read(buffer, 0, (int) (size < remaining ? size : remaining)))
           >= 0
           && remaining > 0) {
      remaining -= c;
    }
    return count - remaining;    
  }

  public int available() throws IOException {
    return 0;
  }

  public void mark(int limit) {
    // ignore
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }

  public void close() throws IOException { }
}
