/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "zlib-custom.h"
#include "system.h"
#include "tokenizer.h"
#include "finder.h"

using namespace vm;

namespace {

const bool DebugFind = false;
const bool DebugStat = false;

class Element {
 public:
  class Iterator {
   public:
    virtual const char* next(unsigned* size) = 0;
    virtual void dispose() = 0;
  };

  Element(): next(0) { }

  virtual Iterator* iterator() = 0;
  virtual System::Region* find(const char* name) = 0;
  virtual System::FileType stat(const char* name, unsigned* length,
                                bool tryDirectory) = 0;
  virtual const char* urlPrefix() = 0;
  virtual const char* sourceUrl() = 0;
  virtual void dispose() = 0;

  Element* next;
};

class DirectoryElement: public Element {
 public:
  class Iterator: public Element::Iterator {
   public:
    Iterator(System* s, Allocator* allocator, const char* name, unsigned skip):
      s(s), allocator(allocator), name(name), skip(skip), directory(0),
      last(0), it(0)
    {
      if (not s->success(s->open(&directory, name))) {
        directory = 0;
      }
    }

    virtual const char* next(unsigned* size) {
      if (it) {
        const char* v = it->next(size);
        if (v) {
          return v;
        } else {
          it->dispose();
          it = 0;
        }
      }

      if (last) {
        allocator->free(last, strlen(last) + 1);
      }

      if (directory) {
        for (const char* v = directory->next(); v; v = directory->next()) {
          if (v[0] != '.') {
            last = append(allocator, name, "/", v);
            unsigned length;
            if (s->stat(last, &length) == System::TypeDirectory) {
              it = new (allocator->allocate(sizeof(Iterator)))
                Iterator(s, allocator, last, skip);
              it->name = last;
            }
            const char* result = last + skip;
            *size = strlen(result);
            return result;
          }
        }
      }

      return 0;
    }

    virtual void dispose() {
      directory->dispose();
      allocator->free(this, sizeof(*this));
    }

    System* s;
    Allocator* allocator;
    const char* name;
    unsigned skip;
    System::Directory* directory;
    const char* last;
    Iterator* it;
  };

  DirectoryElement(System* s, Allocator* allocator, const char* name):
    s(s),
    allocator(allocator),
    originalName(name),
    name(s->toAbsolutePath(allocator, name)),
    urlPrefix_(append(allocator, "file:", this->name, "/")),
    sourceUrl_(append(allocator, "file:", this->name))
  { }

  virtual Element::Iterator* iterator() {
    return new (allocator->allocate(sizeof(Iterator)))
      Iterator(s, allocator, name, strlen(name) + 1);
  }

  virtual System::Region* find(const char* name) {
    const char* file = append(allocator, this->name, "/", name);
    System::Region* region;
    System::Status status = s->map(&region, file);
    allocator->free(file, strlen(file) + 1);

    if (s->success(status)) {
      if (DebugFind) {
        fprintf(stderr, "found %s in %s\n", name, this->name);
      }
      return region;
    } else {
      if (DebugFind) {
        fprintf(stderr, "%s not found in %s\n", name, this->name);
      }
      return 0;
    }
  }

  virtual System::FileType stat(const char* name, unsigned* length, bool)  {
    const char* file = append(allocator, this->name, "/", name);
    System::FileType type = s->stat(file, length);
    if (DebugStat) {
      fprintf(stderr, "stat %s in %s: %d\n", name, this->name, type);
    }
    allocator->free(file, strlen(file) + 1);
    return type;
  }

  virtual const char* urlPrefix() {
    return urlPrefix_;
  }

  virtual const char* sourceUrl() {
    return sourceUrl_;
  }

  virtual void dispose() {
    allocator->free(originalName, strlen(originalName) + 1);
    allocator->free(name, strlen(name) + 1);
    allocator->free(urlPrefix_, strlen(urlPrefix_) + 1);
    allocator->free(sourceUrl_, strlen(sourceUrl_) + 1);
    allocator->free(this, sizeof(*this));
  }

  System* s;
  Allocator* allocator;
  const char* originalName;
  const char* name;
  const char* urlPrefix_;
  const char* sourceUrl_;
};

class PointerRegion: public System::Region {
 public:
  PointerRegion(System* s, Allocator* allocator, const uint8_t* start,
                size_t length):
    s(s),
    allocator(allocator),
    start_(start),
    length_(length)
  { }

  virtual const uint8_t* start() {
    return start_;
  }

  virtual size_t length() {
    return length_;
  }

  virtual void dispose() {
    allocator->free(this, sizeof(*this));
  }

  System* s;
  Allocator* allocator;
  const uint8_t* start_;
  size_t length_;
};

class DataRegion: public System::Region {
 public:
  DataRegion(System* s, Allocator* allocator, size_t length):
    s(s),
    allocator(allocator),
    length_(length)
  { }

  virtual const uint8_t* start() {
    return data;
  }

  virtual size_t length() {
    return length_;
  }

  virtual void dispose() {
    allocator->free(this, sizeof(*this) + length_);
  }

  System* s;
  Allocator* allocator;
  size_t length_;
  uint8_t data[0];
};

class JarIndex {
 public:
  enum CompressionMethod {
    Stored = 0,
    Deflated = 8
  };

  class Node {
   public:
    Node(uint32_t hash, const uint8_t* entry, Node* next):
      hash(hash), entry(entry), next(next)
    { }

    uint32_t hash;
    const uint8_t* entry;
    Node* next;
  };

  JarIndex(System* s, Allocator* allocator, unsigned capacity):
    s(s),
    allocator(allocator),
    capacity(capacity),
    position(0),
    nodes(static_cast<Node*>(allocator->allocate(sizeof(Node) * capacity)))
  {
    memset(table, 0, sizeof(Node*) * capacity);
  }

  static JarIndex* make(System* s, Allocator* allocator, unsigned capacity) {
    return new
      (allocator->allocate(sizeof(JarIndex) + (sizeof(Node*) * capacity)))
      JarIndex(s, allocator, capacity);
  }
  
  static JarIndex* open(System* s, Allocator* allocator,
                        System::Region* region)
  {
    JarIndex* index = make(s, allocator, 32);

    const uint8_t* start = region->start();
    const uint8_t* end = start + region->length();
    const uint8_t* p = end - CentralDirectorySearchStart;
    // Find end of central directory record
    while (p > start) {
      if (signature(p) == CentralDirectorySignature) {
	p = region->start() + centralDirectoryOffset(p);
	
	while (p < end) {
	  if (signature(p) == EntrySignature) {
	    index = index->add(hash(fileName(p), fileNameLength(p)), p);

	    p = endOfEntry(p);
	  } else {
	    return index;
	  }
	}
      } else {
	p--;
      }
    }

    return index;
  }

  JarIndex* add(uint32_t hash, const uint8_t* entry) {
    if (position < capacity) {
      unsigned i = hash & (capacity - 1);
      table[i] = new (nodes + (position++)) Node(hash, entry, table[i]);
      return this;
    } else {
      JarIndex* index = make(s, allocator, capacity * 2);
      for (unsigned i = 0; i < capacity; ++i) {
        index->add(nodes[i].hash, nodes[i].entry);
      }
      index->add(hash, entry);
      dispose();
      return index;
    }
  }

  Node* findNode(const char* name) {
    unsigned length = strlen(name);
    unsigned i = hash(name) & (capacity - 1);
    for (Node* n = table[i]; n; n = n->next) {
      const uint8_t* p = n->entry;
      if (equal(name, length, fileName(p), fileNameLength(p))) {
        return n;
      }
    }
    return 0;
  }

  System::Region* find(const char* name, const uint8_t* start) {
    Node* n = findNode(name);
    if (n) {
      const uint8_t* p = n->entry;
      switch (compressionMethod(p)) {
      case Stored: {
        return new (allocator->allocate(sizeof(PointerRegion)))
          PointerRegion(s, allocator, fileData(start + localHeaderOffset(p)),
			compressedSize(p));
      } break;

      case Deflated: {
        DataRegion* region = new
          (allocator->allocate(sizeof(DataRegion) + uncompressedSize(p)))
          DataRegion(s, allocator, uncompressedSize(p));
          
        z_stream zStream; memset(&zStream, 0, sizeof(z_stream));

        zStream.next_in = const_cast<uint8_t*>(fileData(start +
							localHeaderOffset(p)));
        zStream.avail_in = compressedSize(p);
        zStream.next_out = region->data;
        zStream.avail_out = region->length();

        // -15 means max window size and raw deflate (no zlib wrapper)
        int r = inflateInit2(&zStream, -15);
        expect(s, r == Z_OK);

        r = inflate(&zStream, Z_FINISH);
        expect(s, r == Z_STREAM_END);

        inflateEnd(&zStream);

        return region;
      } break;

      default:
        abort(s);
      }
    }

    return 0;
  }

  System::FileType stat(const char* name, unsigned* length, bool tryDirectory)
  {
    Node* node = findNode(name);
    if (node) {
      *length = uncompressedSize(node->entry);
      return System::TypeFile;
    } else if (tryDirectory) {
      *length = 0;

      // try again with '/' appended
      unsigned length = strlen(name);
      RUNTIME_ARRAY(char, n, length + 2);
      memcpy(RUNTIME_ARRAY_BODY(n), name, length);
      RUNTIME_ARRAY_BODY(n)[length] = '/';
      RUNTIME_ARRAY_BODY(n)[length + 1] = 0;

      node = findNode(RUNTIME_ARRAY_BODY(n));
      if (node) {
        return System::TypeDirectory;
      } else {
        return System::TypeDoesNotExist;
      }
    } else {
      *length = 0;
      return System::TypeDoesNotExist;
    }
  }

  void dispose() {
    allocator->free(nodes, sizeof(Node) * capacity);
    allocator->free(this, sizeof(*this) + (sizeof(Node*) * capacity));
  }

  System* s;
  Allocator* allocator;
  unsigned capacity;
  unsigned position;
  
  Node* nodes;
  Node* table[0];
};

class JarElement: public Element {
 public:
  class Iterator: public Element::Iterator {
   public:
    Iterator(System* s, Allocator* allocator, JarIndex* index):
      s(s), allocator(allocator), index(index), position(0)
    { }

    virtual const char* next(unsigned* size) {
      if (position < index->position) {
        JarIndex::Node* n = index->nodes + (position++);
        *size = fileNameLength(n->entry);
        return reinterpret_cast<const char*>(fileName(n->entry));
      } else {
        return 0;
      }
    }

    virtual void dispose() {
      allocator->free(this, sizeof(*this));
    }

    System* s;
    Allocator* allocator;
    JarIndex* index;
    unsigned position;
  };

  JarElement(System* s, Allocator* allocator, const char* name,
             bool canonicalizePath = true):
    s(s),
    allocator(allocator),
    originalName(name),
    name(name and canonicalizePath
         ? s->toAbsolutePath(allocator, name) : name),
    urlPrefix_(this->name
               ? append(allocator, "jar:file:", this->name, "!/") : 0),
    sourceUrl_(this->name
               ? append(allocator, "file:", this->name) : 0),
    region(0), index(0)
  { }

  JarElement(System* s, Allocator* allocator, const uint8_t* jarData,
             unsigned jarLength):
    s(s),
    allocator(allocator),
    name(0),
    urlPrefix_(name ? append(allocator, "jar:file:", name, "!/") : 0),
    sourceUrl_(name ? append(allocator, "file:", name) : 0),
    region(new (allocator->allocate(sizeof(PointerRegion)))
           PointerRegion(s, allocator, jarData, jarLength)),
    index(JarIndex::open(s, allocator, region))
  { }

  virtual Element::Iterator* iterator() {
    init();

    return new (allocator->allocate(sizeof(Iterator)))
      Iterator(s, allocator, index);
  }

  virtual void init() {
    if (index == 0) {
      System::Region* r;
      if (s->success(s->map(&r, name))) {
        region = r;
        index = JarIndex::open(s, allocator, r);
      }
    }
  }

  virtual System::Region* find(const char* name) {
    init();

    while (*name == '/') name++;

    System::Region* r = (index ? index->find(name, region->start()) : 0);
    if (DebugFind) {
      if (r) {
        fprintf(stderr, "found %s in %s\n", name, this->name);
      } else {
        fprintf(stderr, "%s not found in %s\n", name, this->name);
      }
    }
    return r;
  }

  virtual System::FileType stat(const char* name, unsigned* length,
                                bool tryDirectory)
  {
    init();

    while (*name == '/') name++;

    System::FileType type = (index ? index->stat(name, length, tryDirectory)
                             : System::TypeDoesNotExist);
    if (DebugStat) {
      fprintf(stderr, "stat %s in %s: %d\n", name, this->name, type);
    }
    return type;
  }

  virtual const char* urlPrefix() {
    return urlPrefix_;
  }

  virtual const char* sourceUrl() {
    return sourceUrl_;
  }

  virtual void dispose() {
    dispose(sizeof(*this));
  }

  virtual void dispose(unsigned size) {
    if (originalName != name) {
      allocator->free(originalName, strlen(originalName) + 1);
    }
    allocator->free(name, strlen(name) + 1);
    allocator->free(urlPrefix_, strlen(urlPrefix_) + 1);
    allocator->free(sourceUrl_, strlen(sourceUrl_) + 1);
    if (index) {
      index->dispose();
    }
    if (region) {
      region->dispose();
    }
    allocator->free(this, size);
  }

  System* s;
  Allocator* allocator;
  const char* originalName;
  const char* name;
  const char* urlPrefix_;
  const char* sourceUrl_;
  System::Region* region;
  JarIndex* index;
};

class BuiltinElement: public JarElement {
 public:
  BuiltinElement(System* s, Allocator* allocator, const char* name,
                 const char* libraryName):
    JarElement(s, allocator, name, false),
    libraryName(libraryName ? copy(allocator, libraryName) : 0)
  { }

  virtual void init() {
    if (index == 0) {
      if (s->success(s->load(&library, libraryName))) {
        void* p = library->resolve(name);
        if (p) {
          uint8_t* (*function)(unsigned*);
          memcpy(&function, &p, BytesPerWord);

          unsigned size;
          uint8_t* data = function(&size);
          if (data) {
            region = new (allocator->allocate(sizeof(PointerRegion)))
              PointerRegion(s, allocator, data, size);
            index = JarIndex::open(s, allocator, region);
          }
        }
      }
    }
  }

  virtual const char* urlPrefix() {
    return "resource:";
  }

  virtual const char* sourceUrl() {
    return 0;
  }

  virtual void dispose() {
    library->disposeAll();
    if (libraryName) {
      allocator->free(libraryName, strlen(libraryName) + 1);
    }
    JarElement::dispose(sizeof(*this));
  }

  System::Library* library;
  const char* libraryName;
};

void
add(Element** first, Element** last, Element* e)
{
  if (*last) {
    (*last)->next = e;
  } else {
    *first = e;
  }
  *last = e;
}

unsigned
baseName(const char* name, char fileSeparator)
{
  const char* p = name;
  const char* last = 0;
  while (*p) {
    if (*p == fileSeparator) {
      last = p;
    }
    ++p;
  }

  return last ? (last + 1) - name : 0;
}

void
add(System* s, Element** first, Element** last, Allocator* allocator,
    const char* name, unsigned nameLength, const char* bootLibrary);

void
addTokens(System* s, Element** first, Element** last, Allocator* allocator,
          const char* jarName, unsigned jarNameBase, const char* tokens,
          unsigned tokensLength, const char* bootLibrary)
{
  for (Tokenizer t(tokens, tokensLength, ' '); t.hasMore();) {
    Tokenizer::Token token(t.next());

    RUNTIME_ARRAY(char, n, jarNameBase + token.length + 1);
    memcpy(RUNTIME_ARRAY_BODY(n), jarName, jarNameBase);
    memcpy(RUNTIME_ARRAY_BODY(n) + jarNameBase, token.s, token.length);
    RUNTIME_ARRAY_BODY(n)[jarNameBase + token.length] = 0;
          
    add(s, first, last, allocator, RUNTIME_ARRAY_BODY(n),
        jarNameBase + token.length, bootLibrary);
  }
}

bool
continuationLine(const uint8_t* base, unsigned total, unsigned* start,
                 unsigned* length)
{
  return readLine(base, total, start, length)
    and *length > 0
    and base[*start] == ' ';
}

void
addJar(System* s, Element** first, Element** last, Allocator* allocator,
       const char* name, const char* bootLibrary)
{
  if (DebugFind) {
    fprintf(stderr, "add jar %s\n", name);
  }

  JarElement* e = new (allocator->allocate(sizeof(JarElement)))
    JarElement(s, allocator, name);

  unsigned nameBase = baseName(name, s->fileSeparator());

  add(first, last, e);

  System::Region* region = e->find("META-INF/MANIFEST.MF");
  if (region) {
    unsigned start = 0;
    unsigned length;
    while (readLine(region->start(), region->length(), &start, &length)) {
      unsigned multilineTotal = 0;

      const unsigned PrefixLength = 12;
      if (length > PrefixLength
          and strncmp("Class-Path: ", reinterpret_cast<const char*>
                      (region->start() + start), PrefixLength) == 0)
      {
        { unsigned nextStart = start + length;
          unsigned nextLength;
          while (continuationLine
                 (region->start(), region->length(), &nextStart, &nextLength))
          {
            multilineTotal += nextLength;
            nextStart += nextLength;
          }
        }

        const char* line = reinterpret_cast<const char*>
          (region->start() + start + PrefixLength);

        unsigned lineLength = length - PrefixLength;

        if (multilineTotal) {
          RUNTIME_ARRAY
            (char, n, (length - PrefixLength) + multilineTotal + 1);

          memcpy(RUNTIME_ARRAY_BODY(n), line, lineLength);

          unsigned offset = lineLength;
          { unsigned nextStart = start + length;
            unsigned nextLength;
            while (continuationLine
                   (region->start(), region->length(), &nextStart,
                    &nextLength))
            {
              unsigned continuationLength = nextLength - 1;

              memcpy(RUNTIME_ARRAY_BODY(n) + offset,
                     region->start() + nextStart + 1, continuationLength);
            
              offset += continuationLength;
              nextStart += nextLength;
            }
          }

          addTokens(s, first, last, allocator, name, nameBase,
                    RUNTIME_ARRAY_BODY(n), offset, bootLibrary);
        } else {
          addTokens(s, first, last, allocator, name, nameBase, line,
                    lineLength, bootLibrary);
        }
      }

      start += length + multilineTotal;
    }

    region->dispose();
  }
}

void
add(System* s, Element** first, Element** last, Allocator* allocator,
    const char* token, unsigned tokenLength, const char* bootLibrary)
{
  if (*token == '[' and token[tokenLength - 1] == ']') {
    char* name = static_cast<char*>(allocator->allocate(tokenLength - 1));
    memcpy(name, token + 1, tokenLength - 1);
    name[tokenLength - 2] = 0; 

    if (DebugFind) {
      fprintf(stderr, "add builtin %s\n", name);
    }
  
    add(first, last, new (allocator->allocate(sizeof(BuiltinElement)))
        BuiltinElement(s, allocator, name, bootLibrary));
  } else {
    char* name = static_cast<char*>(allocator->allocate(tokenLength + 1));
    memcpy(name, token, tokenLength);
    name[tokenLength] = 0;

    unsigned length;
    switch (s->stat(name, &length)) {
    case System::TypeFile: {
      addJar(s, first, last, allocator, name, bootLibrary);
    } break;

    case System::TypeDirectory: {
      if (DebugFind) {
        fprintf(stderr, "add directory %s\n", name);
      }

      add(first, last, new (allocator->allocate(sizeof(DirectoryElement)))
          DirectoryElement(s, allocator, name));
    } break;

    default: {
      if (DebugFind) {
        fprintf(stderr, "ignore nonexistent %s\n", name);
      }

      allocator->free(name, strlen(name) + 1);
    } break;
    }
  }
}

Element*
parsePath(System* s, Allocator* allocator, const char* path,
          const char* bootLibrary)
{
  Element* first = 0;
  Element* last = 0;
  for (Tokenizer t(path, s->pathSeparator()); t.hasMore();) {
    Tokenizer::Token token(t.next());

    add(s, &first, &last, allocator, token.s, token.length, bootLibrary);
  }

  return first;
}

class MyIterator: public Finder::IteratorImp {
 public:
  MyIterator(System* s, Allocator* allocator, Element* path):
    s(s), allocator(allocator), e(path ? path->next : 0),
    it(path ? path->iterator() : 0)
  { }

  virtual const char* next(unsigned* size) {
    while (it) {
      const char* v = it->next(size);
      if (v) {
        return v;
      } else {
        it->dispose();
        if (e) {
          it = e->iterator();
          e = e->next;
        } else {
          it = 0;
        }
      }
    }
    return 0;
  }

  virtual void dispose() {
    if (it) it->dispose();
    allocator->free(this, sizeof(*this));
  }

  System* s;
  Allocator* allocator;
  Element* e;
  Element::Iterator* it;
};

class MyFinder: public Finder {
 public:
  MyFinder(System* system, Allocator* allocator, const char* path,
           const char* bootLibrary):
    system(system),
    allocator(allocator),
    path_(parsePath(system, allocator, path, bootLibrary)),
    pathString(copy(allocator, path))
  { }

  MyFinder(System* system, Allocator* allocator, const uint8_t* jarData,
           unsigned jarLength):
    system(system),
    allocator(allocator),
    path_(new (allocator->allocate(sizeof(JarElement)))
          JarElement(system, allocator, jarData, jarLength)),
    pathString(0)
  { }

  virtual IteratorImp* iterator() {
    return new (allocator->allocate(sizeof(MyIterator)))
      MyIterator(system, allocator, path_);
  }

  virtual System::Region* find(const char* name) {
    for (Element* e = path_; e; e = e->next) {
      System::Region* r = e->find(name);
      if (r) {
        return r;
      }
    }
    
    return 0;
  }

  virtual System::FileType stat(const char* name, unsigned* length,
                                bool tryDirectory)
  {
    for (Element* e = path_; e; e = e->next) {
      System::FileType type = e->stat(name, length, tryDirectory);
      if (type != System::TypeDoesNotExist) {
        return type;
      }
    }
    
    return System::TypeDoesNotExist;
  }

  virtual const char* urlPrefix(const char* name) {
    for (Element* e = path_; e; e = e->next) {
      unsigned length;
      System::FileType type = e->stat(name, &length, true);
      if (type != System::TypeDoesNotExist) {
        return e->urlPrefix();
      }
    }

    return 0;
  }

  virtual const char* sourceUrl(const char* name) {
    for (Element* e = path_; e; e = e->next) {
      unsigned length;
      System::FileType type = e->stat(name, &length, true);
      if (type != System::TypeDoesNotExist) {
        return e->sourceUrl();
      }
    }

    return 0;
  }

  virtual const char* path() {
    return pathString;
  }

  virtual void dispose() {
    for (Element* e = path_; e;) {
      Element* t = e;
      e = e->next;
      t->dispose();
    }
    allocator->free(pathString, strlen(pathString) + 1);
    allocator->free(this, sizeof(*this));
  }

  System* system;
  Allocator* allocator;
  Element* path_;
  const char* pathString;
};

} // namespace

namespace vm {

JNIEXPORT Finder*
makeFinder(System* s, Allocator* a, const char* path, const char* bootLibrary)
{
  return new (a->allocate(sizeof(MyFinder))) MyFinder(s, a, path, bootLibrary);
}

Finder*
makeFinder(System* s, Allocator* a, const uint8_t* jarData, unsigned jarLength)
{
  return new (a->allocate(sizeof(MyFinder)))
    MyFinder(s, a, jarData, jarLength);
}

} // namespace vm
