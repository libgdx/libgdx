/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class LineNumberReader extends BufferedReader {
  private int line;

  public LineNumberReader(Reader in, int bufferSize) {
    super(in, bufferSize);
  }

  public LineNumberReader(Reader in) {
    super(in);
  }

  public int getLineNumber() {
    return line;
  }

  public void setLineNumber(int v) {
    line = v;
  }
  
  public int read(char[] b, int offset, int length) throws IOException {
    int c = super.read(b, offset, length);
    for (int i = 0; i < c; ++i) {
      if (b[i] == '\n') {
        ++ line;
      }
    }
    return c;
  }
}
