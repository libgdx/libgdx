/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class ByteArrayOutputStream extends OutputStream {
  private static final int BufferSize = 32;

  private Cell chain;
  private int length;
  private byte[] buffer;
  private int position;

  public ByteArrayOutputStream(int capacity) { }

  public ByteArrayOutputStream() {
    this(0);
  }

  public void reset() {
    chain = null;
    length = 0;
    buffer = null;
    position = 0;
  }

  public int size() {
    return length;
  }

  public void write(int c) {
    if (buffer == null) {
      buffer = new byte[BufferSize];
    } else if (position >= buffer.length) {
      flushBuffer();
      buffer = new byte[BufferSize];
    }

    buffer[position++] = (byte) (c & 0xFF);
    ++ length;
  }

  private byte[] copy(byte[] b, int offset, int length) {
    byte[] array = new byte[length];
    System.arraycopy(b, offset, array, 0, length);
    return array;
  }

  public void write(byte[] b, int offset, int length) {
    if (b == null) {
      throw new NullPointerException();
    }

    if (offset < 0 || offset + length > b.length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    if (length == 0) return;

    if (buffer != null && length <= buffer.length - position) {
      System.arraycopy(b, offset, buffer, position, length);
      position += length;
    } else {
      flushBuffer();
      chain = new Cell(copy(b, offset, length), 0, length, chain);
    }

    this.length += length;
  }

  private void flushBuffer() {
    if (position > 0) {
      byte[] b = buffer;
      int p = position;
      buffer = null;
      position = 0;
      chain = new Cell(b, 0, p, chain);
    }    
  }

  public byte[] toByteArray() {
    flushBuffer();
    
    byte[] array = new byte[length];
    int index = length;
    for (Cell c = chain; c != null; c = c.next) {
      int start = index - c.length;
      System.arraycopy(c.array, c.offset, array, start, c.length);
      index = start;
    }
    return array;
  }

  public String toString(String encoding) throws UnsupportedEncodingException {
    return new String(toByteArray(), encoding);
  }

  private static class Cell {
    public byte[] array;
    public int offset;
    public int length;
    public Cell next;

    public Cell(byte[] array, int offset, int length, Cell next) {
      this.array = array;
      this.offset = offset;
      this.length = length;
      this.next = next;
    }
  }
}
