/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

package java.net;

import java.io.IOException;

public class InetAddress {
  private final String address;

  private InetAddress(String address) {
    this.address = address;
  }

  public String getHostAddress() {
    return address;
  }

  public static InetAddress getByName(String name)
    throws UnknownHostException
  {
    try {
      Socket.init();
    } catch (IOException e) {
      UnknownHostException uhe = new UnknownHostException(name);
      uhe.initCause(e);
      throw uhe;
    }

    int address = ipv4AddressForName(name);
    if (address == 0) {
      throw new UnknownHostException(name);
    } else {
      return new InetAddress(ipv4AddressToString(address));
    }
  }

  private static String ipv4AddressToString(int address) {
    return (((address >>> 24)       ) + "." +
            ((address >>> 16) & 0xFF) + "." +
            ((address >>> 8 ) & 0xFF) + "." +
            ((address       ) & 0xFF));
  }

  private static native int ipv4AddressForName(String name);
}
