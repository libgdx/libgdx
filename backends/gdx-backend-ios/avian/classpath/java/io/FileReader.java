/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class FileReader extends Reader {
  private final Reader in;

  public FileReader(FileInputStream in) {
    this.in = new InputStreamReader(in);
  }
  
  public FileReader(FileDescriptor fd) {
    this(new FileInputStream(fd));
  }

  public FileReader(String path) throws IOException {
    this(new FileInputStream(path));
  }

  public FileReader(File file) throws IOException {
    this(new FileInputStream(file));
  }

  public int read(char[] b, int offset, int length) throws IOException {
    return in.read(b, offset, length);
  }

  public void close() throws IOException {
    in.close();
  }
}
