/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.lang;

import java.security.AccessController;
import java.security.Permission;
import java.security.SecurityPermission;

public class SecurityManager {

  public SecurityManager() {
  }
  
  public void checkPermission(Permission perm) {
    AccessController.checkPermission(perm);
  }
  
  public void checkSecurityAccess(String target) {
    checkPermission(new SecurityPermission(target));
  }

}
