/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.EOFException;

public abstract class Stream {
  public static void write1(OutputStream out, int v) throws IOException {
    out.write(v & 0xFF);
  }

  public static int read1(InputStream in) throws IOException {
    return in.read();
  }

  public static void write2(OutputStream out, int v) throws IOException {
    out.write((v >>> 8) & 0xFF);
    out.write((v      ) & 0xFF);
  }

  public static int read2(InputStream in) throws IOException {
    int b1 = in.read();
    int b2 = in.read();
    if (b2 == -1) throw new EOFException();
    return ((b1 << 8) | (b2 & 0xFF));
  }

  public static void write4(OutputStream out, int v) throws IOException {
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>>  8) & 0xFF);
    out.write((v       ) & 0xFF);
  }

  public static int read4(InputStream in) throws IOException {
    int b1 = in.read();
    int b2 = in.read();
    int b3 = in.read();
    int b4 = in.read();
    if (b4 == -1) throw new EOFException();
    return ((b1 << 24) | (b2 << 16) | (b3 << 8) | (b4));
  }

  public static void write8(OutputStream out, long v) throws IOException {
    write4(out, (int) (v >>> 32) & 0xFFFFFFFF);
    write4(out, (int) (v       ) & 0xFFFFFFFF);
  }

  public static long read8(InputStream in) throws IOException {
    long b1 = in.read();
    long b2 = in.read();
    long b3 = in.read();
    long b4 = in.read();
    long b5 = in.read();
    long b6 = in.read();
    long b7 = in.read();
    long b8 = in.read();
    if (b8 == -1) throw new EOFException();
    return ((b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) |
            (b5 << 24) | (b6 << 16) | (b7 << 8) | (b8));
  }

  public static void set4(byte[] array, int offset, int v) {
    array[offset    ] = (byte) ((v >>> 24) & 0xFF);
    array[offset + 1] = (byte) ((v >>> 16) & 0xFF);
    array[offset + 2] = (byte) ((v >>>  8) & 0xFF);
    array[offset + 3] = (byte) ((v       ) & 0xFF);
  }
}
