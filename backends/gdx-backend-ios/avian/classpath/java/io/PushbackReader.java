/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class PushbackReader extends Reader {
  private final Reader in;
  private char savedChar;
  private boolean hasSavedChar;

  public PushbackReader(Reader in, int bufferSize) {
    if (bufferSize > 1) {
      throw new IllegalArgumentException(bufferSize + " > 1");
    }
    this.in = in;
    this.hasSavedChar = false;
  }

  public PushbackReader(Reader in) {
    this(in, 1);
  }

  public int read(char[] b, int offset, int length) throws IOException {
    int count = 0;
    if (hasSavedChar && length > 0) {
      length--;
      b[offset++] = savedChar;
      hasSavedChar = false;
      count = 1;
    }
    if (length > 0) {
      int c = in.read(b, offset, length);
      if (c == -1) {
        if (count == 0) {
          count = -1;
        }
      } else {
        count += c;
      }
    }

    return count;
  }

  public void unread(char[] b, int offset, int length) throws IOException {
    if (length != 1) {
      throw new IOException("Can only push back 1 char, not " + length);
    } else if (hasSavedChar) {
      throw new IOException("Already have a saved char");
    } else {
      hasSavedChar = true;
      savedChar = b[offset];
    }
  }

  public void unread(char[] b) throws IOException {
    unread(b, 0, b.length);
  }

  public void unread(int c) throws IOException {
    unread(new char[] { (char) c });
  }

  public void close() throws IOException {
    in.close();
  }
}
