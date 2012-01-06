/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class BufferedOutputStream extends OutputStream {
  private final OutputStream out;
  private final byte[] buffer;
  private int position;

  public BufferedOutputStream(OutputStream out, int size) {
    this.out = out;
    this.buffer = new byte[size];
  }
  
  public BufferedOutputStream(OutputStream out) {
    this(out, 4096);
  }
  
  private void drain() throws IOException {
    if (position > 0) {
      out.write(buffer, 0, position);
      position = 0;
    }
  }

  public void write(int c) throws IOException {
    if (position >= buffer.length) {
      drain();
    }

    buffer[position++] = (byte) (c & 0xFF);
  }

  public void write(byte[] b, int offset, int length) throws IOException {
    if (length > buffer.length - position) {
      drain();
      out.write(b, offset, length);      
    } else {
      System.arraycopy(b, offset, buffer, position, length);
      position += length;
    }
  }

  public void flush() throws IOException {
    drain();
    out.flush();
  }

  public void close() throws IOException {
    flush();
    out.close();
  }
}
