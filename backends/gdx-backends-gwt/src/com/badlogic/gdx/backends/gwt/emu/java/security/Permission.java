/* Copyright (c) 2009-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.security;

public abstract class Permission {

	protected String name;

	public Permission (String name) {
		this.name = name;
	}

	public String getName () {
		return name;
	}

	@Override
	public String toString () {
		return this.getClass().getName() + '[' + name + ']';
	}

	public PermissionCollection newPermissionCollection () {
		return null;
	}
}
