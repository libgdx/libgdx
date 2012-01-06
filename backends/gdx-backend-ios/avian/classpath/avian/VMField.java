/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package avian;

public class VMField {
  public byte vmFlags;
  public byte code;
  public short flags;
  public short offset;
  public int nativeID;
  public byte[] name;
  public byte[] spec;
  public FieldAddendum addendum;
  public VMClass class_;
}
