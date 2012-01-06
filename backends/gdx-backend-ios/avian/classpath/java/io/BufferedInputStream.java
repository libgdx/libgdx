/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class BufferedInputStream extends InputStream {
  private final InputStream in;
  private final byte[] buffer;
  private int position;
  private int limit;

  public BufferedInputStream(InputStream in, int size) {
    this.in = in;
    this.buffer = new byte[size];
  }
  
  public BufferedInputStream(InputStream in) {
    this(in, 4096);
  }

  private void fill() throws IOException {
    position = 0;
    limit = in.read(buffer);
  }

  public int read() throws IOException {
    if (position >= limit) {
      fill();
      if (limit == -1) {
        return -1;
      }
    }

    return buffer[position++] & 0xFF;
  }

  public int read(byte[] b, int offset, int length) throws IOException {
    int count = 0;

    if (position < limit) {
      int remaining = limit - position;
      if (remaining > length) {
        remaining = length;
      }

      System.arraycopy(buffer, position, b, offset, remaining);

      count += remaining;
      position += remaining;
      offset += remaining;
      length -= remaining;
    }

    while (length > 0) {
      int c = in.read(b, offset, length);
      if (c == -1) {
        if (count == 0) {
          count = -1;
        }
        break;
      } else {
        offset += c;
        count += c;
        length -= c;

        if (in.available() <= 0) {
          break;
        }
      }
    }

    return count;
  }

  public int available() throws IOException {
    return in.available() + (limit - position);
  }

  public void close() throws IOException {
    in.close();
  }
}
