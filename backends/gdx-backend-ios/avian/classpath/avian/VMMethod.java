/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

public class VMMethod {
  public byte vmFlags;
  public byte returnCode;
  public byte parameterCount;
  public byte parameterFootprint;
  public short flags;
  public short offset;
  public int nativeID;
  public int runtimeDataIndex;
  public byte[] name;
  public byte[] spec;
  public MethodAddendum addendum;
  public VMClass class_;
  public Object code;
}
