/*
	synth_8bit.c: The functions for synthesizing 8bit samples, at the end of decoding.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, heavily dissected and rearranged by Thomas Orgis
*/

#include "mpg123lib_intern.h"
#include "sample.h"
#include "debug.h"

/* 
	Part 2: All synth functions that produce 8bit output.
	What we need is just a special WRITE_SAMPLE. For the generic and i386 functions, that is.
	For the rather optimized synth_1to1, we will need the postprocessing 8bit converters from synth_8bit.h .
*/

#define SAMPLE_T unsigned char
#define WRITE_SAMPLE(samples,sum,clip) WRITE_8BIT_SAMPLE(samples,sum,clip)

/* Part 2a: All straight 1to1 decoding functions */
#define BLOCK 0x40 /* One decoding block is 64 samples. */

#define SYNTH_NAME synth_1to1_8bit
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_1to1_8bit (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_1to1][f_8]
#define MONO_NAME        synth_1to1_8bit_mono
#define MONO2STEREO_NAME synth_1to1_8bit_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_1to1_8bit_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

/* But now, we need functions that take the 16bit output of optimized synth_1to1 and convert it.
   I suppose that is still faster than dropping the optimization altogether! */

#define BASE_SYNTH_NAME  fr->synths.plain[r_1to1][f_16]
#define SYNTH_NAME       synth_1to1_8bit_wrap
#define MONO_NAME        synth_1to1_8bit_wrap_mono
#define MONO2STEREO_NAME synth_1to1_8bit_wrap_m2s
#include "synth_8bit.h"
#undef BASE_SYNTH_NAME
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#undef BLOCK

#ifndef NO_DOWNSAMPLE

/*
	Part 2b: 2to1 synth. Only generic and i386.
*/
#define BLOCK 0x20 /* One decoding block is 32 samples. */

#define SYNTH_NAME synth_2to1_8bit
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_2to1_8bit (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_2to1][f_8]
#define MONO_NAME        synth_2to1_8bit_mono
#define MONO2STEREO_NAME synth_2to1_8bit_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_2to1_8bit_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

#undef BLOCK

/*
	Part 2c: 4to1 synth. Only generic and i386.
*/
#define BLOCK 0x10 /* One decoding block is 16 samples. */

#define SYNTH_NAME synth_4to1_8bit
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_4to1_8bit (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_4to1][f_8]
#define MONO_NAME        synth_4to1_8bit_mono
#define MONO2STEREO_NAME synth_4to1_8bit_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_4to1_8bit_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

#undef BLOCK

#endif /* NO_DOWNSAMPLE */

#ifndef NO_NTOM
/*
	Part 2d: ntom synth.
	Same procedure as above... Just no extra play anymore, straight synth that may use an optimized dct64.
*/

/* These are all in one header, there's no flexibility to gain. */
#define SYNTH_NAME       synth_ntom_8bit
#define MONO_NAME        synth_ntom_8bit_mono
#define MONO2STEREO_NAME synth_ntom_8bit_m2s
#include "synth_ntom.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#endif

#undef SAMPLE_T
#undef WRITE_SAMPLE
