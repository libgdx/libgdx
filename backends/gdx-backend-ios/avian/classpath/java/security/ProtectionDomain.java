/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

public class ProtectionDomain {
  private final CodeSource codeSource;
  private final PermissionCollection permissions;

  public ProtectionDomain(CodeSource codeSource,
                          PermissionCollection permissions)
  {
    this.codeSource = codeSource;
    this.permissions = permissions;
  }

  public CodeSource getCodeSource() {
    return codeSource;
  }
}
