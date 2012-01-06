/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef FINDER_H
#define FINDER_H

#include "common.h"
#include "system.h"
#include "allocator.h"

namespace vm {

const unsigned LocalHeaderSize = 30;
const unsigned HeaderSize = 46;

const unsigned CentralDirectorySignature = 0x06054b50;
const unsigned EntrySignature = 0x02014b50;

const unsigned CentralDirectorySearchStart = 22;

inline uint16_t get2(const uint8_t* p) {
  return
    (static_cast<uint16_t>(p[1]) <<  8) |
    (static_cast<uint16_t>(p[0])      );
}

inline uint32_t get4(const uint8_t* p) {
  return
    (static_cast<uint32_t>(p[3]) << 24) |
    (static_cast<uint32_t>(p[2]) << 16) |
    (static_cast<uint32_t>(p[1]) <<  8) |
    (static_cast<uint32_t>(p[0])      );
}

inline uint32_t signature(const uint8_t* p) {
  return get4(p);
}

inline uint16_t compressionMethod(const uint8_t* centralHeader) {
  return get2(centralHeader + 10);
}

inline uint32_t fileTime(const uint8_t* centralHeader) {
  return get4(centralHeader + 12);
}

inline uint32_t fileCRC(const uint8_t* centralHeader) {
  return get4(centralHeader + 16);
}

inline uint32_t compressedSize(const uint8_t* centralHeader) {
  return get4(centralHeader + 20);
}

inline uint32_t uncompressedSize(const uint8_t* centralHeader) {
  return get4(centralHeader + 24);
}

inline uint16_t fileNameLength(const uint8_t* centralHeader) {
  return get2(centralHeader + 28);
}

inline uint16_t extraFieldLength(const uint8_t* centralHeader) {
  return get2(centralHeader + 30);
}

inline uint16_t commentFieldLength(const uint8_t* centralHeader) {
  return get2(centralHeader + 32);
}

inline uint32_t localHeaderOffset(const uint8_t* centralHeader) {
  return get4(centralHeader + 42);
}

inline uint16_t localFileNameLength(const uint8_t* localHeader) {
  return get2(localHeader + 26);
}

inline uint16_t localExtraFieldLength(const uint8_t* localHeader) {
  return get2(localHeader + 28);
}

inline uint32_t centralDirectoryOffset(const uint8_t* centralHeader) {
  return get4(centralHeader + 16);
}

inline const uint8_t* fileName(const uint8_t* centralHeader) {
  return centralHeader + 46;
}

inline const uint8_t* fileData(const uint8_t* localHeader) {
  return localHeader + LocalHeaderSize + localFileNameLength(localHeader) +
    localExtraFieldLength(localHeader);
}

inline const uint8_t* endOfEntry(const uint8_t* p) {
  return p + HeaderSize + fileNameLength(p) + extraFieldLength(p) +
    commentFieldLength(p);
}

inline bool
readLine(const uint8_t* base, unsigned total, unsigned* start,
         unsigned* length)
{
  const uint8_t* p = base + *start;
  const uint8_t* end = base + total;
  while (p != end and (*p == '\n' or *p == '\r')) ++ p;

  *start = p - base;
  while (p != end and not (*p == '\n' or *p == '\r')) ++ p;

  *length = (p - base) - *start;

  return *length != 0;
}

class Finder {
 public:
  class IteratorImp {
   public:
    virtual const char* next(unsigned* size) = 0;
    virtual void dispose() = 0;
  };

  class Iterator {
   public:
    Iterator(Finder* finder):
      it(finder->iterator()),
      current(it->next(&currentSize))
    { }

    ~Iterator() {
      it->dispose();
    }

    bool hasMore() {
      if (current) return true;
      current = it->next(&currentSize);
      return current != 0;
    }

    const char* next(unsigned* size) {
      if (hasMore()) {
        *size = currentSize;
        const char* v = current;
        current = 0;
        return v;
      } else {
        return 0;
      }
    }

    IteratorImp* it;
    const char* current;
    unsigned currentSize;
  };

  virtual IteratorImp* iterator() = 0;
  virtual System::Region* find(const char* name) = 0;
  virtual System::FileType stat(const char* name,
                                unsigned* length,
                                bool tryDirectory = false) = 0;
  virtual const char* urlPrefix(const char* name) = 0;
  virtual const char* sourceUrl(const char* name) = 0;
  virtual const char* path() = 0;
  virtual void dispose() = 0;
};

Finder*
makeFinder(System* s, Allocator* a, const char* path, const char* bootLibrary);

Finder*
makeFinder(System* s, Allocator* a, const uint8_t* jarData,
           unsigned jarLength);

} // namespace vm

#endif//FINDER_H
