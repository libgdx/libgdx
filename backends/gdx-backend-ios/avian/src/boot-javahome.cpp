/* Copyright (c) 2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifdef _MSC_VER

typedef unsigned char uint8_t;

#else
#  include "stdint.h"
#endif

#ifdef BOOT_JAVAHOME

#if (! defined __x86_64__) && ((defined __MINGW32__) || (defined _MSC_VER))
#  define EXPORT __declspec(dllexport)
#  define SYMBOL(x) binary_javahome_jar_##x
#else
#  define EXPORT __attribute__ ((visibility("default"))) \
  __attribute__ ((used))
#  define SYMBOL(x) _binary_javahome_jar_##x
#endif

extern "C" {

  extern const uint8_t SYMBOL(start)[];
  extern const uint8_t SYMBOL(end)[];

  EXPORT const uint8_t*
  javahomeJar(unsigned* size)
  {
    *size = SYMBOL(end) - SYMBOL(start);
    return SYMBOL(start);
  }

}

#undef SYMBOL

#endif//BOOT_JAVAHOME
