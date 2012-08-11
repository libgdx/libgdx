/* Copyright (c) 2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

public class AccessControlException extends SecurityException {
	private final Permission permission;

	public AccessControlException (String message) {
		this(message, null);
	}

	public AccessControlException (String message, Permission permission) {
		super(message);
		this.permission = permission;
	}

	public Permission getPermission () {
		return permission;
	}
}
