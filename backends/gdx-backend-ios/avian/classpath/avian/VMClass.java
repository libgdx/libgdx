/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

public class VMClass {
  public short flags;
  public short vmFlags;
  public short fixedSize;
  public byte arrayElementSize;
  public byte arrayDimensions;
  public int runtimeDataIndex;
  public int[] objectMask;
  public byte[] name;
  public byte[] sourceFile;
  public VMClass super_;
  public Object[] interfaceTable;
  public VMMethod[] virtualTable;
  public VMField[] fieldTable;
  public VMMethod[] methodTable;
  public ClassAddendum addendum;
  public Object staticTable;
  public ClassLoader loader;
  public byte[] source;
}
