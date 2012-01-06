/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class Process {
  public abstract void destroy();

  public abstract int exitValue();

  public abstract InputStream getInputStream();

  public abstract OutputStream getOutputStream();

  public abstract InputStream getErrorStream();

  public abstract int waitFor() throws InterruptedException;
}
