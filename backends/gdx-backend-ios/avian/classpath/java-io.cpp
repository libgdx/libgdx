/* Copyright (c) 2008-2011, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include "jni.h"
#include "jni-util.h"

#ifdef PLATFORM_WINDOWS

#  include <windows.h>
#  include <io.h>
#  include <direct.h>
#  include <share.h>

#  define ACCESS _waccess
#  define CLOSE _close
#  define READ _read
#  define WRITE _write
#  define STAT _wstat
#  define STRUCT_STAT struct _stat
#  define MKDIR(path, mode) _wmkdir(path)
#  define UNLINK _wunlink
#  define RENAME _wrename
#  define OPEN_MASK O_BINARY

#  ifdef _MSC_VER
#    define S_ISREG(x) ((x) & _S_IFREG)
#    define S_ISDIR(x) ((x) & _S_IFDIR)
#    define S_IRUSR _S_IREAD
#    define S_IWUSR _S_IWRITE
#    define W_OK 2
#    define R_OK 4
#  else
#    define OPEN _wopen
#    define CREAT _wcreat
#  endif

#  define GET_CHARS GetStringChars
#  define RELEASE_CHARS(path, chars) ReleaseStringChars(path, reinterpret_cast<const jchar*>(chars))

typedef wchar_t char_t;

#else // not PLATFORM_WINDOWS

#  include <dirent.h>
#  include <unistd.h>
#  include "sys/mman.h"

#  define ACCESS access
#  define OPEN open
#  define CLOSE close
#  define READ read
#  define WRITE write
#  define STAT stat
#  define STRUCT_STAT struct stat
#  define MKDIR mkdir
#  define CREAT creat
#  define UNLINK unlink
#  define RENAME rename
#  define OPEN_MASK 0

#  define GET_CHARS GetStringUTFChars
#  define RELEASE_CHARS ReleaseStringUTFChars

typedef char char_t;

#endif // not PLATFORM_WINDOWS

inline void* operator new(size_t, void* p) throw() { return p; }

typedef const char_t* string_t;

namespace {

#ifdef _MSC_VER
inline int 
OPEN(string_t path, int mask, int mode)
{
  int fd; 
  if (_wsopen_s(&fd, path, mask, _SH_DENYNO, mode) == 0) {
    return fd; 
  } else {
    return -1; 
  }
}

inline int
CREAT(string_t path, int mode)
{
  return OPEN(path, _O_CREAT, mode);
}
#endif

inline bool
exists(string_t path)
{
  STRUCT_STAT s;
  return STAT(path, &s) == 0;
}

inline int
doOpen(JNIEnv* e, string_t path, int mask)
{
  int fd = OPEN(path, mask | OPEN_MASK, S_IRUSR | S_IWUSR);
  if (fd == -1) {
    if (errno == ENOENT) {
      throwNewErrno(e, "java/io/FileNotFoundException");
    } else {
      throwNewErrno(e, "java/io/IOException");
    }
  }
  return fd;
}

inline void
doClose(JNIEnv* e, jint fd)
{
  int r = CLOSE(fd);
  if (r == -1) {
    throwNewErrno(e, "java/io/IOException");
  }
}

inline int
doRead(JNIEnv* e, jint fd, jbyte* data, jint length)
{
  int r = READ(fd, data, length);
  if (r > 0) {
    return r;
  } else if (r == 0) {
    return -1;
  } else {
    throwNewErrno(e, "java/io/IOException");
    return 0;
  }  
}

inline void
doWrite(JNIEnv* e, jint fd, const jbyte* data, jint length)
{
  int r = WRITE(fd, data, length);
  if (r != length) {
    throwNewErrno(e, "java/io/IOException");
  }
}

#ifdef PLATFORM_WINDOWS

class Mapping {
 public:
  Mapping(uint8_t* start, size_t length, HANDLE mapping, HANDLE file):
    start(start),
    length(length),
    mapping(mapping),
    file(file)
  { }

  uint8_t* start;
  size_t length;
  HANDLE mapping;
  HANDLE file;
};

inline Mapping*
map(JNIEnv* e, string_t path)
{
  Mapping* result = 0;
  HANDLE file = CreateFileW(path, FILE_READ_DATA, FILE_SHARE_READ, 0,
                            OPEN_EXISTING, 0, 0);
  if (file != INVALID_HANDLE_VALUE) {
    unsigned size = GetFileSize(file, 0);
    if (size != INVALID_FILE_SIZE) {
      HANDLE mapping = CreateFileMapping(file, 0, PAGE_READONLY, 0, size, 0);
      if (mapping) {
        void* data = MapViewOfFile(mapping, FILE_MAP_READ, 0, 0, 0);
        if (data) {
          void* p = allocate(e, sizeof(Mapping));
          if (not e->ExceptionCheck()) {
            result = new (p)
              Mapping(static_cast<uint8_t*>(data), size, file, mapping);
          }   
        }

        if (result == 0) {
          CloseHandle(mapping);
        }
      }
    }

    if (result == 0) {
      CloseHandle(file);
    }
  }
  if (result == 0 and not e->ExceptionCheck()) {
    throwNew(e, "java/io/IOException", "%d", GetLastError());
  }
  return result;
}

inline void
unmap(JNIEnv*, Mapping* mapping)
{
  UnmapViewOfFile(mapping->start);
  CloseHandle(mapping->mapping);
  CloseHandle(mapping->file);
  free(mapping);
}

class Directory {
 public:
  Directory(): handle(0), findNext(false) { }

  virtual string_t next() {
    if (handle and handle != INVALID_HANDLE_VALUE) {
      if (findNext) {
        if (FindNextFileW(handle, &data)) {
          return data.cFileName;
        }
      } else {
        findNext = true;
        return data.cFileName;
      }
    }
    return 0;
  }

  virtual void dispose() {
    if (handle and handle != INVALID_HANDLE_VALUE) {
      FindClose(handle);
    }
    free(this);
  }

  HANDLE handle;
  WIN32_FIND_DATAW data;
  bool findNext;
};

#else // not PLATFORM_WINDOWS

class Mapping {
 public:
  Mapping(uint8_t* start, size_t length):
    start(start),
    length(length)
  { }

  uint8_t* start;
  size_t length;
};

inline Mapping*
map(JNIEnv* e, string_t path)
{
  Mapping* result = 0;
  int fd = open(path, O_RDONLY);
  if (fd != -1) {
    struct stat s;
    int r = fstat(fd, &s);
    if (r != -1) {
      void* data = mmap(0, s.st_size, PROT_READ, MAP_PRIVATE, fd, 0);
      if (data) {
        void* p = allocate(e, sizeof(Mapping));
        if (not e->ExceptionCheck()) {
          result = new (p) Mapping(static_cast<uint8_t*>(data), s.st_size);
        }
      }
    }
    close(fd);
  }
  if (result == 0 and not e->ExceptionCheck()) {
    throwNewErrno(e, "java/io/IOException");
  }
  return result;
}

inline void
unmap(JNIEnv*, Mapping* mapping)
{
  munmap(mapping->start, mapping->length);
  free(mapping);
}

#endif // not PLATFORM_WINDOWS

} // namespace

inline string_t getChars(JNIEnv* e, jstring path) {
  return reinterpret_cast<string_t>(e->GET_CHARS(path, 0));
}

inline void releaseChars(JNIEnv* e, jstring path, string_t chars) {
  e->RELEASE_CHARS(path, chars);
}

extern "C" JNIEXPORT jstring JNICALL
Java_java_io_File_toCanonicalPath(JNIEnv* /*e*/, jclass, jstring path)
{
  // todo
  return path;
}

extern "C" JNIEXPORT jstring JNICALL
Java_java_io_File_toAbsolutePath(JNIEnv* e UNUSED, jclass, jstring path)
{
#ifdef PLATFORM_WINDOWS
  // todo
  return path;
#else
  jstring result = path;
  string_t chars = getChars(e, path);
  if (chars) {
    if (chars[0] != '/') {
      char* cwd = getcwd(NULL, 0);
      if (cwd) {
        unsigned size = strlen(cwd) + strlen(chars) + 2;
        RUNTIME_ARRAY(char, buffer, size);
        snprintf(RUNTIME_ARRAY_BODY(buffer), size, "%s/%s", cwd, chars);
        result = e->NewStringUTF(RUNTIME_ARRAY_BODY(buffer));
        free(cwd);
      }
    }
    releaseChars(e, path, chars);
  }
  return result;
#endif
}

extern "C" JNIEXPORT jlong JNICALL
Java_java_io_File_length(JNIEnv* e, jclass, jstring path)
{

  #ifdef PLATFORM_WINDOWS

    LARGE_INTEGER fileSize;
    string_t chars = getChars(e, path);
    HANDLE file = CreateFileW
      (chars, FILE_READ_DATA, FILE_SHARE_READ, 0, OPEN_EXISTING, 0, 0);
    releaseChars(e, path, chars);
    if (file != INVALID_HANDLE_VALUE)
      GetFileSizeEx(file, &fileSize);
    else return 0;
    CloseHandle(file);
    return static_cast<jlong>(fileSize.QuadPart);

  #else

    string_t chars = getChars(e, path);
    if (chars) {
      STRUCT_STAT s;
      int r = STAT(chars, &s);
      releaseChars(e, path, chars);
      if (r == 0) {
        return s.st_size;
      }
    }

  #endif

  return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_File_mkdir(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    if (not exists(chars)) {
      int r = ::MKDIR(chars, 0700);
      if (r != 0) {
        throwNewErrno(e, "java/io/IOException");
      }
    }
    releaseChars(e, path, chars);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_File_createNewFile(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    if (not exists(chars)) {
      int fd = CREAT(chars, 0600);
      if (fd == -1) {
        throwNewErrno(e, "java/io/IOException");
      } else {
        doClose(e, fd);
      }
    }
    releaseChars(e, path, chars);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_File_delete(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    int r = UNLINK(chars);
    if (r != 0) {
      throwNewErrno(e, "java/io/IOException");
    }
    releaseChars(e, path, chars);
  }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_canRead(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    int r = ACCESS(chars, R_OK);
    releaseChars(e, path, chars);
    return (r == 0);
  }
  return false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_canWrite(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    int r = ACCESS(chars, W_OK);
    releaseChars(e, path, chars);
    return (r == 0);
  }
  return false;
}


extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_rename(JNIEnv* e, jclass, jstring old, jstring new_)
{
  string_t oldChars = getChars(e, old);
  string_t newChars = getChars(e, new_);
  if (oldChars) {
    bool v;
    if (newChars) {
      v = RENAME(oldChars, newChars) == 0;

      releaseChars(e, new_, newChars);
    } else {
      v = false;
    }
    releaseChars(e, old, oldChars);
    return v;
  } else {
    return false;
  }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_isDirectory(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    STRUCT_STAT s;
    int r = STAT(chars, &s);
    bool v = (r == 0 and S_ISDIR(s.st_mode));
    releaseChars(e, path, chars);
    return v;
  } else {
    return false;
  }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_isFile(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    STRUCT_STAT s;
    int r = STAT(chars, &s);
    bool v = (r == 0 and S_ISREG(s.st_mode));
    releaseChars(e, path, chars);
    return v;
  } else {
    return false;
  }
}

extern "C" JNIEXPORT jboolean JNICALL
Java_java_io_File_exists(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    bool v = exists(chars);
    releaseChars(e, path, chars);
    return v;
  } else {
    return false;
  }
}

#ifdef PLATFORM_WINDOWS

extern "C" JNIEXPORT jlong JNICALL
Java_java_io_File_openDir(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    unsigned length = wcslen(chars);
    unsigned size = length * sizeof(char_t);

    RUNTIME_ARRAY(char_t, buffer, length + 3);
    memcpy(RUNTIME_ARRAY_BODY(buffer), chars, size);
    memcpy(RUNTIME_ARRAY_BODY(buffer) + length, L"\\*", 6);

    releaseChars(e, path, chars);

    Directory* d = new (malloc(sizeof(Directory))) Directory;
    d->handle = FindFirstFileW(RUNTIME_ARRAY_BODY(buffer), &(d->data));
    if (d->handle == INVALID_HANDLE_VALUE) {
      d->dispose();
      d = 0;
    }

    return reinterpret_cast<jlong>(d);
  } else {
    return 0;
  }
}

extern "C" JNIEXPORT jstring JNICALL
Java_java_io_File_readDir(JNIEnv* e, jclass, jlong handle)
{
  Directory* d = reinterpret_cast<Directory*>(handle);

  while (true) {
    string_t s = d->next();
    if (s) {
      if (wcscmp(s, L".") == 0 || wcscmp(s, L"..") == 0) {
        // skip . or .. and try again
      } else {
        return e->NewString(reinterpret_cast<const jchar*>(s), wcslen(s));
      }
    } else {
      return 0;
    }
  }
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_File_closeDir(JNIEnv* , jclass, jlong handle)
{
  reinterpret_cast<Directory*>(handle)->dispose();
}

#else // not PLATFORM_WINDOWS

extern "C" JNIEXPORT jlong JNICALL
Java_java_io_File_openDir(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path);
  if (chars) {
    jlong handle = reinterpret_cast<jlong>(opendir(chars));
    releaseChars(e, path, chars);
    return handle;
  } else {
    return 0;
  }
}

extern "C" JNIEXPORT jstring JNICALL
Java_java_io_File_readDir(JNIEnv* e, jclass, jlong handle)
{
  struct dirent * directoryEntry;

  if (handle!=0) {
    while (true) {
      directoryEntry = readdir(reinterpret_cast<DIR*>(handle));
      if (directoryEntry == NULL) {
        return NULL;
      } else if (strcmp(directoryEntry->d_name, ".") == 0
                 || strcmp(directoryEntry->d_name, "..") == 0)
      {
        // skip . or .. and try again
      } else {
        return e->NewStringUTF(directoryEntry->d_name);
      }
    }
  }
  return NULL;
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_File_closeDir(JNIEnv* , jclass, jlong handle)
{
  if (handle!=0) {
    closedir(reinterpret_cast<DIR*>(handle));
  }
}

#endif // not PLATFORM_WINDOWS

extern "C" JNIEXPORT jint JNICALL
Java_java_io_FileInputStream_open(JNIEnv* e, jclass, jstring path)
{
  string_t chars = getChars(e, path); 
  if (chars) {
    int fd = doOpen(e, chars, O_RDONLY);
    releaseChars(e, path, chars);
    return fd; 
  } else {
    return -1; 
  }
}

extern "C" JNIEXPORT jint JNICALL
Java_java_io_FileInputStream_read__I(JNIEnv* e, jclass, jint fd)
{
  jbyte data;
  int r = doRead(e, fd, &data, 1);
  if (r <= 0) {
    return -1;
  } else {
    return data & 0xff;
  }
}

extern "C" JNIEXPORT jint JNICALL
Java_java_io_FileInputStream_read__I_3BII
(JNIEnv* e, jclass, jint fd, jbyteArray b, jint offset, jint length)
{
  jbyte* data = static_cast<jbyte*>(malloc(length));
  if (data == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return 0;    
  }

  int r = doRead(e, fd, data, length);

  e->SetByteArrayRegion(b, offset, length, data);

  free(data);

  return r;
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_FileInputStream_close(JNIEnv* e, jclass, jint fd)
{
  doClose(e, fd);
}

extern "C" JNIEXPORT jint JNICALL
Java_java_io_FileOutputStream_open(JNIEnv* e, jclass, jstring path, jboolean append)
{
  string_t chars = getChars(e, path);
  if (chars) {
    int fd = doOpen(e, chars, append
                    ? (O_WRONLY | O_CREAT | O_APPEND)
                    : (O_WRONLY | O_CREAT | O_TRUNC));
    releaseChars(e, path, chars);
    return fd;
  } else {
    return -1;
  }
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_FileOutputStream_write__II(JNIEnv* e, jclass, jint fd, jint c)
{
  jbyte data = c;
  doWrite(e, fd, &data, 1);
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_FileOutputStream_write__I_3BII
(JNIEnv* e, jclass, jint fd, jbyteArray b, jint offset, jint length)
{
  jbyte* data = static_cast<jbyte*>(malloc(length));
  if (data == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;    
  }

  e->GetByteArrayRegion(b, offset, length, data);

  if (not e->ExceptionCheck()) {
    doWrite(e, fd, data, length);
  }

  free(data);
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_FileOutputStream_close(JNIEnv* e, jclass, jint fd)
{
  doClose(e, fd);
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_RandomAccessFile_open(JNIEnv* e, jclass, jstring path,
                                   jlongArray result)
{
  string_t chars = getChars(e, path);
  if (chars) {
    Mapping* mapping = map(e, chars);

    jlong peer = reinterpret_cast<jlong>(mapping);
    e->SetLongArrayRegion(result, 0, 1, &peer);

    jlong length = (mapping ? mapping->length : 0);
    e->SetLongArrayRegion(result, 1, 1, &length);

    releaseChars(e, path, chars);
  }
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_RandomAccessFile_copy(JNIEnv* e, jclass, jlong peer,
                                   jlong position, jbyteArray buffer,
                                   int offset, int length)
{
  uint8_t* dst = reinterpret_cast<uint8_t*>
    (e->GetPrimitiveArrayCritical(buffer, 0));

  memcpy(dst + offset,
         reinterpret_cast<Mapping*>(peer)->start + position,
         length);

  e->ReleasePrimitiveArrayCritical(buffer, dst, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_java_io_RandomAccessFile_close(JNIEnv* e, jclass, jlong peer)
{
  unmap(e, reinterpret_cast<Mapping*>(peer));
}
