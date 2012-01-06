/* Copyright (c) 2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef TARGET_H
#define TARGET_H

namespace vm {

template <class T>
inline T
targetV1(T v)
{
  return v;
}

#ifdef TARGET_OPPOSITE_ENDIAN

template <class T>
inline T
targetV2(T v)
{
  return (((v >> 8) & 0xFF) |
          ((v << 8)));
}

template <class T>
inline T
targetV4(T v)
{
  return (((v >> 24) & 0x000000FF) |
          ((v >>  8) & 0x0000FF00) |
          ((v <<  8) & 0x00FF0000) |
          ((v << 24)));
}

template <class T>
inline T
targetV8(T v)
{
  return (((static_cast<uint64_t>(v) >> 56) & UINT64_C(0x00000000000000FF)) |
          ((static_cast<uint64_t>(v) >> 40) & UINT64_C(0x000000000000FF00)) |
          ((static_cast<uint64_t>(v) >> 24) & UINT64_C(0x0000000000FF0000)) |
          ((static_cast<uint64_t>(v) >>  8) & UINT64_C(0x00000000FF000000)) |
          ((static_cast<uint64_t>(v) <<  8) & UINT64_C(0x000000FF00000000)) |
          ((static_cast<uint64_t>(v) << 24) & UINT64_C(0x0000FF0000000000)) |
          ((static_cast<uint64_t>(v) << 40) & UINT64_C(0x00FF000000000000)) |
          ((static_cast<uint64_t>(v) << 56)));
}

#else
template <class T>
inline T
targetV2(T v)
{
  return v;
}

template <class T>
inline T
targetV4(T v)
{
  return v;
}

template <class T>
inline T
targetV8(T v)
{
  return v;
}
#endif

#ifdef TARGET_BYTES_PER_WORD
#  if (TARGET_BYTES_PER_WORD == 8)

template <class T>
inline T
targetVW(T v)
{
  return targetV8(v);
}

typedef uint64_t target_uintptr_t;
typedef int64_t target_intptr_t;

const unsigned TargetBytesPerWord = 8;

const unsigned TargetThreadIp = 2216;
const unsigned TargetThreadStack = 2224;
const unsigned TargetThreadTailAddress = 2272;
const unsigned TargetThreadVirtualCallTarget = 2280;
const unsigned TargetThreadVirtualCallIndex = 2288;
const unsigned TargetThreadHeapImage = 2296;
const unsigned TargetThreadCodeImage = 2304;
const unsigned TargetThreadThunkTable = 2312;
const unsigned TargetThreadStackLimit = 2360;

const unsigned TargetClassFixedSize = 12;
const unsigned TargetClassArrayElementSize = 14;
const unsigned TargetClassVtable = 128;

const unsigned TargetFieldOffset = 12;

#  elif (TARGET_BYTES_PER_WORD == 4)

template <class T>
inline T
targetVW(T v)
{
  return targetV4(v);
}

typedef uint32_t target_uintptr_t;
typedef int32_t target_intptr_t;

const unsigned TargetBytesPerWord = 4;

const unsigned TargetThreadIp = 2144;
const unsigned TargetThreadStack = 2148;
const unsigned TargetThreadTailAddress = 2172;
const unsigned TargetThreadVirtualCallTarget = 2176;
const unsigned TargetThreadVirtualCallIndex = 2180;
const unsigned TargetThreadHeapImage = 2184;
const unsigned TargetThreadCodeImage = 2188;
const unsigned TargetThreadThunkTable = 2192;
const unsigned TargetThreadStackLimit = 2216;

const unsigned TargetClassFixedSize = 8;
const unsigned TargetClassArrayElementSize = 10;
const unsigned TargetClassVtable = 68;

const unsigned TargetFieldOffset = 8;

#  else
#    error
#  endif
#else
#  error
#endif

const unsigned TargetBitsPerWord = TargetBytesPerWord * 8;

const target_uintptr_t TargetPointerMask
= ((~static_cast<target_uintptr_t>(0)) / TargetBytesPerWord)
  * TargetBytesPerWord;

const unsigned TargetArrayLength = TargetBytesPerWord;
const unsigned TargetArrayBody = TargetBytesPerWord * 2;

inline void
targetMarkBit(target_uintptr_t* map, unsigned i)
{
  map[wordOf<target_uintptr_t>(i)] |=
    targetVW(static_cast<target_uintptr_t>(1) << bitOf<target_uintptr_t>(i));
}

} // namespace vm

#endif//TARGET_H
