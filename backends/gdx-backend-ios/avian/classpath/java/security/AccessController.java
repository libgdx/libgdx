/* Copyright (c) 2008, 2010 Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

/**
 * No real access control is implemented here.
 * 
 * @author zsombor
 *
 */
public class AccessController {

  public static Object doPrivileged (PrivilegedAction action) {
    return action.run();
  }
  
  public static void checkPermission(Permission perm) throws AccessControlException {
    
  }

}
