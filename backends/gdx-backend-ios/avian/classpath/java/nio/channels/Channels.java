/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Channels {
  public static InputStream newInputStream(ReadableByteChannel channel) {
    return new MyInputStream(channel);
  }

  public static OutputStream newOutputStream(WritableByteChannel channel) {
    return new MyOutputStream(channel);
  }

  public static ReadableByteChannel newChannel(InputStream stream) {
    return new InputStreamChannel(stream);
  }

  public static WritableByteChannel newChannel(OutputStream stream) {
    return new OutputStreamChannel(stream);
  }

  private static class MyInputStream extends InputStream {
    private final ReadableByteChannel channel;

    public MyInputStream(ReadableByteChannel channel) {
      this.channel = channel;
    }

    public int read() throws IOException {
      byte[] buffer = new byte[1];
      int r = read(buffer);
      if (r == -1) {
        return -1;
      } else {
        return buffer[0] & 0xFF;
      }
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
      return channel.read(ByteBuffer.wrap(buffer, offset, length));
    }

    public void close() throws IOException {
      channel.close();
    }
  }

  private static class MyOutputStream extends OutputStream {
    private final WritableByteChannel channel;

    public MyOutputStream(WritableByteChannel channel) {
      this.channel = channel;
    }

    public void write(int v) throws IOException {
      byte[] buffer = new byte[] { (byte) (v & 0xFF) };
      write(buffer);
    }

    public void write(byte[] buffer, int offset, int length)
      throws IOException
    {
      channel.write(ByteBuffer.wrap(buffer, offset, length));
    }

    public void close() throws IOException {
      channel.close();
    }
  }

  private static class InputStreamChannel implements ReadableByteChannel {
    private InputStream stream;

    public InputStreamChannel(InputStream stream) {
      this.stream = stream;
    }

    public void close() throws IOException {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    public boolean isOpen() {
      return stream != null;
    }

    public int read(ByteBuffer b) throws IOException {
      int c = stream.read
        (b.array(), b.arrayOffset() + b.position(), b.remaining());
      
      if (c > 0) {
        b.position(b.position() + c);
      }

      return c;
    }
  }

  private static class OutputStreamChannel implements WritableByteChannel {
    private OutputStream stream;

    public OutputStreamChannel(OutputStream stream) {
      this.stream = stream;
    }

    public void close() throws IOException {
      if (stream != null) {
        stream.close();
        stream = null;
      }
    }

    public boolean isOpen() {
      return stream != null;
    }

    public int write(ByteBuffer b) throws IOException {
      stream.write(b.array(), b.arrayOffset() + b.position(), b.remaining());

      int c = b.remaining();

      b.position(b.limit());
      
      return c;
    }
  }
}
