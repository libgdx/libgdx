/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "machine.h"
#include "heapwalk.h"

using namespace vm;

namespace {

namespace local {

enum {
  Root,
  Size,
  ClassName,
  Push,
  Pop
};

void
write1(FILE* out, uint8_t v)
{
  size_t n UNUSED = fwrite(&v, 1, 1, out);
}

void
write4(FILE* out, uint32_t v)
{
  uint8_t b[] = { v >> 24, (v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF };
  size_t n UNUSED = fwrite(b, 4, 1, out);
}

void
writeString(FILE* out, int8_t* p, unsigned size)
{
  write4(out, size);
  size_t n UNUSED = fwrite(p, size, 1, out);
}

unsigned
objectSize(Thread* t, object o)
{
  return extendedSize(t, o, baseSize(t, o, objectClass(t, o)));
}

} // namespace local

} // namespace

namespace vm {

void
dumpHeap(Thread* t, FILE* out)
{
  class Visitor: public HeapVisitor {
   public:
    Visitor(Thread* t, FILE* out): t(t), out(out), nextNumber(1) { }

    virtual void root() {
      write1(out, local::Root);      
    }

    virtual unsigned visitNew(object p) {
      if (p) {
        unsigned number = nextNumber++;
        local::write4(out, number);

        local::write1(out, local::Size);
        local::write4(out, local::objectSize(t, p));

        if (objectClass(t, p) == type(t, Machine::ClassType)) {
          object name = className(t, p);
          if (name) {
            local::write1(out, local::ClassName);
            local::writeString(out, &byteArrayBody(t, name, 0),
                               byteArrayLength(t, name) - 1);
          }
        }

        return number;
      } else {
        return 0;
      }
    }

    virtual void visitOld(object, unsigned number) {
      local::write4(out, number);      
    }

    virtual void push(object, unsigned, unsigned) {
      local::write1(out, local::Push);
    }

    virtual void pop() {
      local::write1(out, local::Pop);
    }

    Thread* t;
    FILE* out;
    unsigned nextNumber;
  } visitor(t, out);

  HeapWalker* w = makeHeapWalker(t, &visitor);
  w->visitAllRoots();
  w->dispose();
}

} // namespace vm
