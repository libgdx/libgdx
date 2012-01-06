#ifndef ENDIANNESS_H
#define ENDIANNESS_H

#define V1(v) (v)

#ifdef OPPOSITE_ENDIAN
#  define V2(v) \
  ((((v) >> 8) & 0xFF) | \
   (((v) << 8)))
#  define V4(v) \
  ((((v) >> 24) & 0x000000FF) | \
   (((v) >>  8) & 0x0000FF00) | \
   (((v) <<  8) & 0x00FF0000) | \
   (((v) << 24)))
#  define V8(v) \
  (((static_cast<uint64_t>(v) >> 56) & UINT64_C(0x00000000000000FF)) | \
   ((static_cast<uint64_t>(v) >> 40) & UINT64_C(0x000000000000FF00)) | \
   ((static_cast<uint64_t>(v) >> 24) & UINT64_C(0x0000000000FF0000)) | \
   ((static_cast<uint64_t>(v) >>  8) & UINT64_C(0x00000000FF000000)) | \
   ((static_cast<uint64_t>(v) <<  8) & UINT64_C(0x000000FF00000000)) | \
   ((static_cast<uint64_t>(v) << 24) & UINT64_C(0x0000FF0000000000)) | \
   ((static_cast<uint64_t>(v) << 40) & UINT64_C(0x00FF000000000000)) | \
   ((static_cast<uint64_t>(v) << 56)))
#else
#  define V2(v) (v)
#  define V4(v) (v)
#  define V8(v) (v)
#endif

#if (BITS_PER_WORD == 64)
#  define VW(v) V8(v)
#elif (BITS_PER_WORD == 32)
#  define VW(v) V4(v)
#else
#  error
#endif

#endif//ENDIANNESS_H
