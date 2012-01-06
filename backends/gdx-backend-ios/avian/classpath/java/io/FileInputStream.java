/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class FileInputStream extends InputStream {
  //   static {
  //     System.loadLibrary("natives");
  //   }

  private int fd;

  public FileInputStream(FileDescriptor fd) {
    this.fd = fd.value;
  }

  public FileInputStream(String path) throws IOException {
    fd = open(path);
  }

  public FileInputStream(File file) throws IOException {
    this(file.getPath());
  }

  private static native int open(String path) throws IOException;

  private static native int read(int fd) throws IOException;

  private static native int read(int fd, byte[] b, int offset, int length)
    throws IOException;

  public static native void close(int fd) throws IOException;

  public int read() throws IOException {
    return read(fd);
  }

  public int read(byte[] b, int offset, int length) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }

    if (offset < 0 || offset + length > b.length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    return read(fd, b, offset, length);
  }

  public void close() throws IOException {
    if (fd != -1) {
      close(fd);
      fd = -1;
    }
  }
}
