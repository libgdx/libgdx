## Makefile.am: produce Makefile.in from this

## copyright by the mpg123 project - free software under the terms of the LGPL 2.1
## see COPYING and AUTHORS files in distribution or http://mpg123.org
## initially written by Nicholas J. Humfrey

#AM_CFLAGS = @AUDIO_CFLAGS@
#AM_LDFLAGS = 
INCLUDES = -I$(top_srcdir)/src -I$(top_srcdir)/src/libmpg123

EXTRA_DIST = mpg123.h.in

EXTRA_PROGRAMS = testcpu
testcpu_dependencies = getcpuflags.$(OBJEXT)
testcpu_sources = testcpu.c
testcpu_LDADD = getcpuflags.$(OBJEXT)


CLEANFILES = *.a


# The library can have different names, depending on largefile setup.
# Libtool macros think they're smart. Because of that mpg123.la does not work, it must be libmpg123.la .
lib_LTLIBRARIES = libmpg123.la
nodist_include_HEADERS = mpg123.h

libmpg123_la_LDFLAGS = -no-undefined -version-info @LIBMPG123_VERSION@ -export-symbols-regex '^mpg123_'
libmpg123_la_LIBADD = @DECODER_LOBJ@ @LFS_LOBJ@
libmpg123_la_DEPENDENCIES = @DECODER_LOBJ@ @LFS_LOBJ@

libmpg123_la_SOURCES = \
	intsym.h \
	compat.c \
	compat.h \
	mpeghead.h \
	parse.c \
	parse.h \
	frame.c \
	format.c \
	frame.h \
	reader.h \
	debug.h \
	decode.h \
	sample.h \
	dct64.c \
	synth.h \
	synth_mono.h \
	synth_ntom.h \
	synth_8bit.h \
	synths.h \
	equalizer.c \
	huffman.h \
	icy.h \
	icy2utf8.h \
	id3.h \
	id3.c \
	true.h \
	getbits.h \
	optimize.h \
	optimize.c \
	readers.c \
	tabinit.c \
	libmpg123.c \
	gapless.h \
	mpg123lib_intern.h \
	mangle.h \
	getcpuflags.h \
	index.h \
	index.c

EXTRA_libmpg123_la_SOURCES = \
	lfs_alias.c \
	lfs_wrap.c \
	icy.c \
	icy2utf8.c \
	l2tables.h \
	layer1.c \
	layer2.c \
	layer3.c \
	dither.h \
	dither.c \
	feature.c \
	dct36_3dnowext.S \
	dct36_3dnow.S \
	dct64_3dnowext.S \
	dct64_3dnow.S \
	dct64_altivec.c \
	dct64_i386.c \
	dct64_i486.c \
	dct64_mmx.S \
	dct64_sse.S \
	dct64_sse_float.S \
	dct64_x86_64.S \
	dct64_x86_64_float.S \
	dct64_neon.S \
	dct64_neon_float.S \
	synth_3dnowext.S \
	synth_3dnow.S \
	synth_altivec.c \
	synth_i486.c \
	synth_i586_dither.S \
	synth_i586.S \
	synth_mmx.S \
	synth_sse3d.h \
	synth_sse.S \
	synth_sse_float.S \
	synth_sse_s32.S \
	synth_sse_accurate.S \
	synth_stereo_sse_float.S \
	synth_stereo_sse_s32.S \
	synth_stereo_sse_accurate.S \
	synth_x86_64.S \
	synth_x86_64_float.S \
	synth_x86_64_s32.S \
	synth_x86_64_accurate.S \
	synth_stereo_x86_64.S \
	synth_stereo_x86_64_float.S \
	synth_stereo_x86_64_s32.S \
	synth_stereo_x86_64_accurate.S \
	synth_arm.S \
	synth_arm_accurate.S \
	synth_neon.S \
	synth_neon_float.S \
	synth_neon_s32.S \
	synth_neon_accurate.S \
	synth_stereo_neon.S \
	synth_stereo_neon_float.S \
	synth_stereo_neon_s32.S \
	synth_stereo_neon_accurate.S \
	ntom.c \
	synth.c \
	synth_8bit.c \
	synth_real.c \
	synth_s32.c \
	equalizer_3dnow.S \
	tabinit_mmx.S \
	stringbuf.c \
	getcpuflags.S \
	l12_integer_tables.h \
	l3_integer_tables.h

