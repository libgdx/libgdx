/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "jni.h"
#include "jni-util.h"

#ifdef PLATFORM_WINDOWS
#  include <winsock2.h>
#  define ONLY_ON_WINDOWS(x) x
#else
#  include <netdb.h>
#  define ONLY_ON_WINDOWS(x)
#endif

extern "C" JNIEXPORT void JNICALL
Java_java_net_Socket_init(JNIEnv* ONLY_ON_WINDOWS(e), jclass)
{
#ifdef PLATFORM_WINDOWS
  static bool wsaInitialized = false;
  if (not wsaInitialized) {
    WSADATA data;
    int r = WSAStartup(MAKEWORD(2, 2), &data);
    if (r or LOBYTE(data.wVersion) != 2 or HIBYTE(data.wVersion) != 2) {
      throwNew(e, "java/io/IOException", "WSAStartup failed");
    } else {
      wsaInitialized = true;
    }
  }
#endif
}

extern "C" JNIEXPORT jint JNICALL
Java_java_net_InetAddress_ipv4AddressForName(JNIEnv* e,
                                             jclass,
                                             jstring name)
{
  const char* chars = e->GetStringUTFChars(name, 0);
  if (chars) {
#ifdef PLATFORM_WINDOWS
    hostent* host = gethostbyname(chars);
    e->ReleaseStringUTFChars(name, chars);
    if (host) {
      return htonl(reinterpret_cast<in_addr*>(host->h_addr_list[0])->s_addr);
    } else {
      fprintf(stderr, "trouble %d\n", WSAGetLastError());
    }
#else
    addrinfo hints;
    memset(&hints, 0, sizeof(addrinfo));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;

    addrinfo* result;
    int r = getaddrinfo(chars, 0, &hints, &result);
    e->ReleaseStringUTFChars(name, chars);

    jint address;
    if (r != 0) {
      address = 0;
    } else {
      address = htonl
        (reinterpret_cast<sockaddr_in*>(result->ai_addr)->sin_addr.s_addr);

      freeaddrinfo(result);
    }

    return address;
#endif
  }
  return 0;
}
