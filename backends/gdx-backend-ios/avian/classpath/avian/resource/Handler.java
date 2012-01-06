/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian.resource;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Handler extends URLStreamHandler {
  protected URLConnection openConnection(URL url) {
    return new ResourceConnection(url);
  }

  private static class ResourceConnection extends URLConnection {
    public ResourceConnection(URL url) {
      super(url);
    }

    public int getContentLength() {
      return ResourceInputStream.getContentLength(url.getFile());
    }

    public InputStream getInputStream() throws IOException {
      return new ResourceInputStream(url.getFile());
    }

    public void connect() {
      // ignore
    }
  }

  private static class ResourceInputStream extends InputStream {
    private long peer;
    private int position;

    public ResourceInputStream(String path) throws IOException {
      peer = open(path);
      if (peer == 0) {
        throw new FileNotFoundException(path);
      }
    }

    private static native int getContentLength(String path);

    private static native long open(String path) throws IOException;

    private static native int read(long peer, int position) throws IOException;

    private static native int read(long peer, int position,
                                   byte[] b, int offset, int length)
      throws IOException;

    public static native void close(long peer) throws IOException;

    public static native int available(long peer, int position);

    public int available() {
      return available(peer, position);
    }

    public int read() throws IOException {
      if (peer != 0) {
        int c = read(peer, position);
        if (c >= 0) {
          ++ position;
        }
        return c;
      } else {
        throw new IOException();
      }
    }

    public int read(byte[] b, int offset, int length) throws IOException {
      if (peer != 0) {
        if (b == null) {
          throw new NullPointerException();
        }

        if (offset < 0 || offset + length > b.length) {
          throw new ArrayIndexOutOfBoundsException();
        }

        int c = read(peer, position, b, offset, length);
        if (c >= 0) {
          position += c;
        }
        return c;
      } else {
        throw new IOException();
      }
    }

    public void close() throws IOException {
      if (peer != 0) {
        close(peer);
        peer = 0;
      }
    }
  }
}
