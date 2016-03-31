noinst_LTLIBRARIES = libwebpdsp.la libwebpdsp_avx2.la
noinst_LTLIBRARIES += libwebpdsp_sse2.la libwebpdspdecode_sse2.la
noinst_LTLIBRARIES += libwebpdsp_sse41.la libwebpdspdecode_sse41.la

if BUILD_LIBWEBPDECODER
  noinst_LTLIBRARIES += libwebpdspdecode.la
endif

common_HEADERS = ../webp/types.h
commondir = $(includedir)/webp

COMMON_SOURCES =
COMMON_SOURCES += alpha_processing.c
COMMON_SOURCES += alpha_processing_mips_dsp_r2.c
COMMON_SOURCES += cpu.c
COMMON_SOURCES += dec.c
COMMON_SOURCES += dec_clip_tables.c
COMMON_SOURCES += dec_mips32.c
COMMON_SOURCES += dec_mips_dsp_r2.c
COMMON_SOURCES += dec_neon.c
COMMON_SOURCES += dsp.h
COMMON_SOURCES += filters.c
COMMON_SOURCES += filters_mips_dsp_r2.c
COMMON_SOURCES += lossless.c
COMMON_SOURCES += lossless.h
COMMON_SOURCES += lossless_mips_dsp_r2.c
COMMON_SOURCES += lossless_neon.c
COMMON_SOURCES += mips_macro.h
COMMON_SOURCES += neon.h
COMMON_SOURCES += rescaler.c
COMMON_SOURCES += rescaler_mips32.c
COMMON_SOURCES += rescaler_mips_dsp_r2.c
COMMON_SOURCES += rescaler_neon.c
COMMON_SOURCES += upsampling.c
COMMON_SOURCES += upsampling_mips_dsp_r2.c
COMMON_SOURCES += upsampling_neon.c
COMMON_SOURCES += yuv.c
COMMON_SOURCES += yuv.h
COMMON_SOURCES += yuv_mips32.c
COMMON_SOURCES += yuv_mips_dsp_r2.c

ENC_SOURCES =
ENC_SOURCES += argb.c
ENC_SOURCES += argb_mips_dsp_r2.c
ENC_SOURCES += cost.c
ENC_SOURCES += cost_mips32.c
ENC_SOURCES += cost_mips_dsp_r2.c
ENC_SOURCES += enc.c
ENC_SOURCES += enc_mips32.c
ENC_SOURCES += enc_mips_dsp_r2.c
ENC_SOURCES += enc_neon.c
ENC_SOURCES += lossless_enc.c
ENC_SOURCES += lossless_enc_mips32.c
ENC_SOURCES += lossless_enc_mips_dsp_r2.c
ENC_SOURCES += lossless_enc_neon.c

libwebpdsp_avx2_la_SOURCES =
libwebpdsp_avx2_la_SOURCES += enc_avx2.c
libwebpdsp_avx2_la_CPPFLAGS = $(libwebpdsp_la_CPPFLAGS)
libwebpdsp_avx2_la_CFLAGS = $(AM_CFLAGS) $(AVX2_FLAGS)

libwebpdspdecode_sse41_la_SOURCES =
libwebpdspdecode_sse41_la_SOURCES += alpha_processing_sse41.c
libwebpdspdecode_sse41_la_SOURCES += dec_sse41.c
libwebpdspdecode_sse41_la_CPPFLAGS = $(libwebpdsp_la_CPPFLAGS)
libwebpdspdecode_sse41_la_CFLAGS = $(AM_CFLAGS) $(SSE41_FLAGS)

libwebpdspdecode_sse2_la_SOURCES =
libwebpdspdecode_sse2_la_SOURCES += alpha_processing_sse2.c
libwebpdspdecode_sse2_la_SOURCES += dec_sse2.c
libwebpdspdecode_sse2_la_SOURCES += filters_sse2.c
libwebpdspdecode_sse2_la_SOURCES += lossless_sse2.c
libwebpdspdecode_sse2_la_SOURCES += rescaler_sse2.c
libwebpdspdecode_sse2_la_SOURCES += upsampling_sse2.c
libwebpdspdecode_sse2_la_SOURCES += yuv_sse2.c
libwebpdspdecode_sse2_la_CPPFLAGS = $(libwebpdsp_sse2_la_CPPFLAGS)
libwebpdspdecode_sse2_la_CFLAGS = $(libwebpdsp_sse2_la_CFLAGS)

libwebpdsp_sse2_la_SOURCES =
libwebpdsp_sse2_la_SOURCES += argb_sse2.c
libwebpdsp_sse2_la_SOURCES += cost_sse2.c
libwebpdsp_sse2_la_SOURCES += enc_sse2.c
libwebpdsp_sse2_la_SOURCES += lossless_enc_sse2.c
libwebpdsp_sse2_la_CPPFLAGS = $(libwebpdsp_la_CPPFLAGS)
libwebpdsp_sse2_la_CFLAGS = $(AM_CFLAGS) $(SSE2_FLAGS)
libwebpdsp_sse2_la_LIBADD = libwebpdspdecode_sse2.la

libwebpdsp_sse41_la_SOURCES =
libwebpdsp_sse41_la_SOURCES += enc_sse41.c
libwebpdsp_sse41_la_SOURCES += lossless_enc_sse41.c
libwebpdsp_sse41_la_CPPFLAGS = $(libwebpdsp_la_CPPFLAGS)
libwebpdsp_sse41_la_CFLAGS = $(AM_CFLAGS) $(SSE41_FLAGS)
libwebpdsp_sse41_la_LIBADD = libwebpdspdecode_sse41.la

libwebpdsp_la_SOURCES = $(COMMON_SOURCES) $(ENC_SOURCES)

noinst_HEADERS =
noinst_HEADERS += ../dec/decode_vp8.h
noinst_HEADERS += ../webp/decode.h

libwebpdsp_la_CPPFLAGS =
libwebpdsp_la_CPPFLAGS += $(AM_CPPFLAGS)
libwebpdsp_la_CPPFLAGS += $(USE_EXPERIMENTAL_CODE) $(USE_SWAP_16BIT_CSP)
libwebpdsp_la_LDFLAGS = -lm
libwebpdsp_la_LIBADD =
libwebpdsp_la_LIBADD += libwebpdsp_avx2.la libwebpdsp_sse2.la
libwebpdsp_la_LIBADD += libwebpdsp_sse41.la

if BUILD_LIBWEBPDECODER
  libwebpdspdecode_la_SOURCES = $(COMMON_SOURCES)

  libwebpdspdecode_la_CPPFLAGS = $(libwebpdsp_la_CPPFLAGS)
  libwebpdspdecode_la_LDFLAGS = $(libwebpdsp_la_LDFLAGS)
  libwebpdspdecode_la_LIBADD =
  libwebpdspdecode_la_LIBADD += libwebpdspdecode_sse2.la
  libwebpdspdecode_la_LIBADD += libwebpdspdecode_sse41.la
endif
