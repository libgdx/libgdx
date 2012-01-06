/* Copyright (c) 2008-2009, Avian Contributors

   Permission to use, copy, modify, and/or distribute this software
   for any purpose with or without fee is hereby granted, provided
   that the above copyright notice and this permission notice appear
   in all copies.

   There is NO WARRANTY for this software.  See license.txt for
   details. */

#include "stdlib.h"
#include "string.h"
#include "zlib-custom.h"

#include "jni.h"
#include "jni-util.h"

extern "C" JNIEXPORT jlong JNICALL
Java_java_util_zip_Inflater_make
(JNIEnv* e, jclass, jboolean nowrap)
{
  z_stream* s = static_cast<z_stream*>(malloc(sizeof(z_stream)));
  if (s == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return 0;    
  }

  memset(s, 0, sizeof(z_stream));
  
  int r = inflateInit2(s, (nowrap ? -15 : 15));
  if (r != Z_OK) {
    free(s);
    throwNew(e, "java/lang/RuntimeException", zError(r));
    return 0;
  }

  return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_dispose(JNIEnv*, jclass, jlong peer)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  inflateEnd(s);
  free(s);
}

extern "C" JNIEXPORT void JNICALL
Java_java_util_zip_Inflater_inflate
(JNIEnv* e, jclass, jlong peer,
 jbyteArray input, jint inputOffset, jint inputLength,
 jbyteArray output, jint outputOffset, jint outputLength,
 jintArray results)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);

  jbyte* in = static_cast<jbyte*>(malloc(inputLength));
  if (in == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;    
  }

  jbyte* out = static_cast<jbyte*>(malloc(outputLength));
  if (out == 0) {
    free(in);
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;    
  }

  e->GetByteArrayRegion(input, inputOffset, inputLength, in);
  
  s->next_in = reinterpret_cast<Bytef*>(in);
  s->avail_in = inputLength;
  s->next_out = reinterpret_cast<Bytef*>(out);
  s->avail_out = outputLength;

  int r = inflate(s, Z_SYNC_FLUSH);
  jint resultArray[3]
    = { r, inputLength - s->avail_in, outputLength - s->avail_out };

  free(in);

  e->SetByteArrayRegion(output, outputOffset, resultArray[2], out);
  free(out);

  e->SetIntArrayRegion(results, 0, 3, resultArray);
}

extern "C" JNIEXPORT jlong JNICALL
Java_java_util_zip_Deflater_make
(JNIEnv* e, jclass, jboolean nowrap, jint level)
{
  z_stream* s = static_cast<z_stream*>(malloc(sizeof(z_stream)));
  if (s == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return 0;    
  }

  memset(s, 0, sizeof(z_stream));
  
  int r = deflateInit2(s, level, (nowrap ? -15 : 15));
  if (r != Z_OK) {
    free(s);
    throwNew(e, "java/lang/RuntimeException", zError(r));
    return 0;
  }

  return reinterpret_cast<jlong>(s);
}

extern "C" JNIEXPORT void JNICALL
Java_java_util_zip_Deflater_dispose(JNIEnv*, jclass, jlong peer)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);
  deflateEnd(s);
  free(s);
}

extern "C" JNIEXPORT void JNICALL
Java_java_util_zip_Deflater_deflate
(JNIEnv* e, jclass, jlong peer, 
 jbyteArray input, jint inputOffset, jint inputLength,
 jbyteArray output, jint outputOffset, jint outputLength,
 jboolean finish, jintArray results)
{
  z_stream* s = reinterpret_cast<z_stream*>(peer);

  jbyte* in = static_cast<jbyte*>(malloc(inputLength));
  if (in == 0) {
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;    
  }

  jbyte* out = static_cast<jbyte*>(malloc(outputLength));
  if (out == 0) {
    free(in);
    throwNew(e, "java/lang/OutOfMemoryError", 0);
    return;    
  }

  e->GetByteArrayRegion(input, inputOffset, inputLength, in);
  
  s->next_in = reinterpret_cast<Bytef*>(in);
  s->avail_in = inputLength;
  s->next_out = reinterpret_cast<Bytef*>(out);
  s->avail_out = outputLength;

  int r = deflate(s, finish ? Z_FINISH : Z_NO_FLUSH);
  jint resultArray[3]
    = { r, inputLength - s->avail_in, outputLength - s->avail_out };

  free(in);

  e->SetByteArrayRegion(output, outputOffset, resultArray[2], out);
  free(out);

  e->SetIntArrayRegion(results, 0, 3, resultArray);
}
