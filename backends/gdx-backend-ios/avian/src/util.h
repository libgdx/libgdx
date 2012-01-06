/* Copyright (c) 2008-2010, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#ifndef UTIL_H
#define UTIL_H

#include "machine.h"
#include "zone.h"

namespace vm {

object
hashMapFindNode(Thread* t, object map, object key,
                uint32_t (*hash)(Thread*, object),
                bool (*equal)(Thread*, object, object));

inline object
hashMapFind(Thread* t, object map, object key,
            uint32_t (*hash)(Thread*, object),
            bool (*equal)(Thread*, object, object))
{
  object n = hashMapFindNode(t, map, key, hash, equal);
  return (n ? tripleSecond(t, n) : 0);
}

void
hashMapResize(Thread* t, object map, uint32_t (*hash)(Thread*, object),
              unsigned size);

void
hashMapInsert(Thread* t, object map, object key, object value,
              uint32_t (*hash)(Thread*, object));

inline bool
hashMapInsertOrReplace(Thread* t, object map, object key, object value,
                       uint32_t (*hash)(Thread*, object),
                       bool (*equal)(Thread*, object, object))
{
  object n = hashMapFindNode(t, map, key, hash, equal);
  if (n == 0) {
    hashMapInsert(t, map, key, value, hash);
    return true;
  } else {
    set(t, n, TripleSecond, value);
    return false;
  }
}

inline bool
hashMapInsertMaybe(Thread* t, object map, object key, object value,
                   uint32_t (*hash)(Thread*, object),
                   bool (*equal)(Thread*, object, object))
{
  object n = hashMapFindNode(t, map, key, hash, equal);
  if (n == 0) {
    hashMapInsert(t, map, key, value, hash);
    return true;
  } else {
    return false;
  }
}

object
hashMapRemove(Thread* t, object map, object key,
              uint32_t (*hash)(Thread*, object),
              bool (*equal)(Thread*, object, object));

object
hashMapIterator(Thread* t, object map);

object
hashMapIteratorNext(Thread* t, object it);

void
listAppend(Thread* t, object list, object value);

object
vectorAppend(Thread* t, object vector, object value);

object
growArray(Thread* t, object array);

object
treeQuery(Thread* t, object tree, intptr_t key, object sentinal,
          intptr_t (*compare)(Thread* t, intptr_t key, object b));

object
treeInsert(Thread* t, Zone* zone, object tree, intptr_t key, object value,
           object sentinal,
           intptr_t (*compare)(Thread* t, intptr_t key, object b));

void
treeUpdate(Thread* t, object tree, intptr_t key, object value, object sentinal,
           intptr_t (*compare)(Thread* t, intptr_t key, object b));

class HashMapIterator: public Thread::Protector {
 public:
  HashMapIterator(Thread* t, object map):
    Protector(t), map(map), node(0), index(0)
  {
    find();
  }

  void find() {
    object array = hashMapArray(t, map);
    if (array) {
      for (unsigned i = index; i < arrayLength(t, array); ++i) {
        if (arrayBody(t, array, i)) {
          node = arrayBody(t, array, i);
          index = i + 1;
          return;
        }
      }
    }
    node = 0;
  }

  bool hasMore() {
    return node != 0;
  }

  object next() {
    if (node) {
      object n = node;
      if (tripleThird(t, node)) {
        node = tripleThird(t, node);
      } else {
        find();
      }
      return n;
    } else {
      return 0;
    }
  }

  virtual void visit(Heap::Visitor* v) {
    v->visit(&map);
    v->visit(&node);
  }

  object map;
  object node;
  unsigned index;
};

} // vm

#endif//UTIL_H
