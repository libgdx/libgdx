/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "stdint.h"
#include "stdio.h"
#include "stdlib.h"
#include "string.h"

#include "sys/stat.h"
#ifdef WIN32
#include <windows.h>
#else
#include "sys/mman.h"
#endif
#include "fcntl.h"
#include "unistd.h"

namespace binaryToObject {

bool
writeElf64Object(uint8_t* data, unsigned size, FILE* out,
                 const char* startName, const char* endName,
                 const char* architecture, unsigned alignment, bool writable,
                 bool executable);

bool
writeElf32Object(uint8_t* data, unsigned size, FILE* out,
                 const char* startName, const char* endName,
                 const char* architecture, unsigned alignment, bool writable,
                 bool executable);

bool
writeMachO64Object(uint8_t* data, unsigned size, FILE* out,
                   const char* startName, const char* endName,
                   const char* architecture, unsigned alignment, bool writable,
                   bool executable);

bool
writeMachO32Object(uint8_t* data, unsigned size, FILE* out,
                   const char* startName, const char* endName,
                   const char* architecture, unsigned alignment, bool writable,
                   bool executable);

bool
writePEObject(uint8_t* data, unsigned size, FILE* out, const char* startName,
              const char* endName, const char* architecture,
              unsigned alignment, bool writable, bool executable);

} // namespace binaryToObject

namespace {

bool
writeObject(uint8_t* data, unsigned size, FILE* out, const char* startName,
            const char* endName, const char* platform,
            const char* architecture, unsigned alignment, bool writable,
            bool executable)
{
  using namespace binaryToObject;

  bool found = false;
  bool success = false;
  if (strcmp("linux", platform) == 0) {
    if (strcmp("x86_64", architecture) == 0) {
      found = true;
      success = writeElf64Object
        (data, size, out, startName, endName, architecture, alignment,
         writable, executable);
    } else if (strcmp("i386", architecture) == 0
               or strcmp("arm", architecture) == 0
               or strcmp("powerpc", architecture) == 0)
    {
      found = true;
      success = writeElf32Object
        (data, size, out, startName, endName, architecture, alignment,
         writable, executable);
    }
  } else if (strcmp("darwin", platform) == 0) {
    if (strcmp("x86_64", architecture) == 0) {
      found = true;
      success = writeMachO64Object
        (data, size, out, startName, endName, architecture, alignment,
         writable, executable);
    } else if (strcmp("i386", architecture) == 0
               or strcmp("powerpc", architecture) == 0
               or strcmp("arm", architecture) == 0)
    {
      found = true;
      success = writeMachO32Object
        (data, size, out, startName, endName, architecture, alignment,
         writable, executable);
    }
  } else if (strcmp("windows", platform) == 0
             and ((strcmp("x86_64", architecture) == 0
                   or strcmp("i386", architecture) == 0)))
  {
    found = true;
    success = writePEObject
      (data, size, out, startName, endName, architecture, alignment, writable,
       executable);
  }

  if (not found) {
    fprintf(stderr, "unsupported platform: %s/%s\n", platform, architecture);
    return false;
  }

  return success;
}

void
usageAndExit(const char* name)
{
  fprintf(stderr,
          "usage: %s <input file> <output file> <start name> <end name> "
          "<platform> <architecture> "
          "[<alignment> [{writable|executable}...]]\n",
          name);
  exit(-1);
}

} // namespace

int
main(int argc, const char** argv)
{
  if (argc < 7 or argc > 10) {
    usageAndExit(argv[0]);
  }

  unsigned alignment = 1;
  if (argc > 7) {
    alignment = atoi(argv[7]);
  }

  bool writable = false;
  bool executable = false;

  for (int i = 8; i < argc; ++i) {
    if (strcmp("writable", argv[i]) == 0) {
      writable = true;
    } else if (strcmp("executable", argv[i]) == 0) {
      executable = true;
    } else {
      usageAndExit(argv[0]);
    }
  }

  uint8_t* data = 0;
  unsigned size;
  int fd = open(argv[1], O_RDONLY);
  if (fd != -1) {
    struct stat s;
    int r = fstat(fd, &s);
    if (r != -1) {
#ifdef WIN32
      HANDLE fm;
      HANDLE h = (HANDLE) _get_osfhandle (fd);

      fm = CreateFileMapping(
               h,
               NULL,
               PAGE_READONLY,
               0,
               0,
               NULL);
      data = static_cast<uint8_t*>(MapViewOfFile(
                fm,
                FILE_MAP_READ,
                0,
                0,
                s.st_size));

      CloseHandle(fm);
#else
      data = static_cast<uint8_t*>
        (mmap(0, s.st_size, PROT_READ, MAP_PRIVATE, fd, 0));
#endif
      size = s.st_size;
    }
    close(fd);
  }

  bool success = false;

  if (data) {
    FILE* out = fopen(argv[2], "wb");
    if (out) {
      success = writeObject
        (data, size, out, argv[3], argv[4], argv[5], argv[6], alignment,
         writable, executable);

      fclose(out);
    } else {
      fprintf(stderr, "unable to open %s\n", argv[2]);
    }

#ifdef WIN32
    UnmapViewOfFile(data);
#else
    munmap(data, size);
#endif
  } else {
    perror(argv[0]);
  }

  return (success ? 0 : -1);
}
