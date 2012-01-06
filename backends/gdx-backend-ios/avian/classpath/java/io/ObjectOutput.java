/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public interface ObjectOutput {
  public void close();
  public void flush();
  public void write(byte[] b);
  public void write(byte[] b, int off, int len);
  public void write(int b);
  public void writeObject(Object obj);
}
