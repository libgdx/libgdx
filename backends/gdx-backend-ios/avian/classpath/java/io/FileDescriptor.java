/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.io;

public class FileDescriptor {
  public static final FileDescriptor in = new FileDescriptor(0);
  public static final FileDescriptor out = new FileDescriptor(1);
  public static final FileDescriptor err = new FileDescriptor(2);

  final int value;

  public FileDescriptor(int value) {
    this.value = value;
  }

  public FileDescriptor() {
    this(-1);
  }
}
