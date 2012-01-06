/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "stdlib.h"

#ifdef _MSC_VER

typedef unsigned char uint8_t;

#else // not _MSC_VER

#  include "stdint.h"

// since we aren't linking against libstdc++, we must implement this
// ourselves:
extern "C" void __cxa_pure_virtual(void) { abort(); }

#endif // not _MSC_VER

#if (defined __MINGW32__) || (defined _MSC_VER)
#  define EXPORT __declspec(dllexport)
#else
#  define EXPORT __attribute__ ((visibility("default"))) \
  __attribute__ ((used))
#endif

#ifdef BOOT_IMAGE

#if (! defined __x86_64__) && ((defined __MINGW32__) || (defined _MSC_VER))
#  define BOOTIMAGE_SYMBOL(x) binary_bootimage_bin_##x
#  define CODEIMAGE_SYMBOL(x) binary_codeimage_bin_##x
#else
#  define BOOTIMAGE_SYMBOL(x) _binary_bootimage_bin_##x
#  define CODEIMAGE_SYMBOL(x) _binary_codeimage_bin_##x
#endif

extern "C" {

  extern const uint8_t BOOTIMAGE_SYMBOL(start)[];
  extern const uint8_t BOOTIMAGE_SYMBOL(end)[];

  EXPORT const uint8_t*
  bootimageBin(unsigned* size)
  {
    *size = BOOTIMAGE_SYMBOL(end) - BOOTIMAGE_SYMBOL(start);
    return BOOTIMAGE_SYMBOL(start);
  }

  extern const uint8_t CODEIMAGE_SYMBOL(start)[];
  extern const uint8_t CODEIMAGE_SYMBOL(end)[];

  EXPORT const uint8_t*
  codeimageBin(unsigned* size)
  {
    *size = CODEIMAGE_SYMBOL(end) - CODEIMAGE_SYMBOL(start);
    return CODEIMAGE_SYMBOL(start);
  }

}

#undef SYMBOL

#endif//BOOT_IMAGE

#ifdef BOOT_CLASSPATH

#if (! defined __x86_64__) && ((defined __MINGW32__) || (defined _MSC_VER))
#  define SYMBOL(x) binary_classpath_jar_##x
#else
#  define SYMBOL(x) _binary_classpath_jar_##x
#endif

extern "C" {

  extern const uint8_t SYMBOL(start)[];
  extern const uint8_t SYMBOL(end)[];

  EXPORT const uint8_t*
  classpathJar(unsigned* size)
  {
    *size = SYMBOL(end) - SYMBOL(start);
    return SYMBOL(start);
  }

}

#undef SYMBOL

#endif//BOOT_CLASSPATH
