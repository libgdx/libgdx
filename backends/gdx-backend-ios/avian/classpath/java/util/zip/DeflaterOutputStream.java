/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.util.zip;

import java.io.OutputStream;
import java.io.IOException;

public class DeflaterOutputStream extends OutputStream {
  private final OutputStream out;
  private final Deflater deflater;
  private final byte[] buffer;

  public DeflaterOutputStream(OutputStream out, Deflater deflater, int bufferSize)
  {
    this.out = out;
    this.deflater = deflater;
    this.buffer = new byte[bufferSize];
  }

  public DeflaterOutputStream(OutputStream out, Deflater deflater) {
    this(out, deflater, 4 * 1024);
  }

  public DeflaterOutputStream(OutputStream out) {
    this(out, new Deflater());
  }

  public void write(int b) throws IOException {
    byte[] buffer = new byte[1];
    buffer[0] = (byte)(b & 0xff);
    write(buffer, 0, 1);
  }

  public void write(byte[] b, int offset, int length) throws IOException {
    // error condition checking
    if (deflater.finished()) {
      throw new IOException("Already at end of stream");
    } else if (offset < 0) {
      throw new IndexOutOfBoundsException("Offset can't be less than zero");
    } else if (length < 0) {
      throw new IndexOutOfBoundsException("Length can't be less than zero");
    } else if (b.length - (offset + length) < 0) {
      throw new IndexOutOfBoundsException("Offset + Length is larger than the input byte array");
    } else if (length == 0) {
      return;
    }

    deflater.setInput(b, offset, length);
    while (deflater.getRemaining() > 0) {
      deflate();
    }
  }

  private void deflate() throws IOException {
    int len = deflater.deflate(buffer, 0, buffer.length);
    if (len > 0) {
      out.write(buffer, 0, len);
    }
  }

  public void close() throws IOException {
    deflater.finish();
    while (! deflater.finished()) {
      deflate();
    }
    out.close();
    deflater.dispose();
  }
}
