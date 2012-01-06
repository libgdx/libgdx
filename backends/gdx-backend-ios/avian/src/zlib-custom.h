/* Copyright (c) 2008, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "zlib.h"

#ifdef inflateInit2
#undef inflateInit2
#define inflateInit2(strm, windowBits) \
        inflateInit2_((strm), (windowBits), ZLIB_VERSION, static_cast<int>(sizeof(z_stream)))
#endif

#ifdef deflateInit2
#undef deflateInit2
#define deflateInit2(strm, level, windowBits) \
        deflateInit2_((strm), (level), Z_DEFLATED, (windowBits), 8, Z_DEFAULT_STRATEGY, ZLIB_VERSION, static_cast<int>(sizeof(z_stream)))
#endif
