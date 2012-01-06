/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef BOOTIMAGE_H
#define BOOTIMAGE_H

#include "common.h"
#include "target.h"
#include "machine.h"

namespace vm {

class BootImage {
 public:
  class Thunk {
   public:
    Thunk():
      start(0), frameSavedOffset(0), length(0)
    { }

    Thunk(uint32_t start, uint32_t frameSavedOffset, uint32_t length):
      start(start), frameSavedOffset(frameSavedOffset), length(length)
    { }

    uint32_t start;
    uint32_t frameSavedOffset;
    uint32_t length;
  } PACKED;

  class ThunkCollection {
   public:
#define THUNK_FIELD(name) Thunk name;
#include "bootimage-fields.cpp"
#undef THUNK_FIELD
  } PACKED;

  static const uint32_t Magic = 0x22377322;

#define FIELD(name) uint32_t name;
#include "bootimage-fields.cpp"
#undef FIELD

  ThunkCollection thunks;
} PACKED;

class OffsetResolver {
 public:
  virtual unsigned fieldOffset(Thread*, object) = 0;
};

#define NAME(x) Target##x
#define LABEL(x) target_##x
#include "bootimage-template.cpp"
#undef LABEL
#undef NAME

#define NAME(x) x
#define LABEL(x) x
#include "bootimage-template.cpp"
#undef LABEL
#undef NAME

} // namespace vm

#endif//BOOTIMAGE_H
