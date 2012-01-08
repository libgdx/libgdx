#ifndef MPG123_H_OPTIMIZE
#define MPG123_H_OPTIMIZE
/*
	optimize: get a grip on the different optimizations

	copyright 2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis, taking from mpg123.[hc]

	for building mpg123 with one optimization only, you have to choose exclusively between
	OPT_GENERIC (generic C code for everyone)
	OPT_GENERIC_DITHER (same with dithering for 1to1)
	OPT_I386 (Intel i386)
	OPT_I486 (Somewhat special code for i486; does not work together with others.)
	OPT_I586 (Intel Pentium)
	OPT_I586_DITHER (Intel Pentium with dithering/noise shaping for enhanced quality)
	OPT_MMX (Intel Pentium and compatibles with MMX, fast, but not the best accuracy)
	OPT_3DNOW (AMD 3DNow!, K6-2/3, Athlon, compatibles...)
	OPT_3DNOWEXT (AMD 3DNow! extended, generally Athlon, compatibles...)
	OPT_ALTIVEC (Motorola/IBM PPC with AltiVec under MacOSX)
	OPT_X86_64 (x86-64 / AMD64 / Intel 64)

	or you define OPT_MULTI and give a combination which makes sense (do not include i486, do not mix altivec and x86).

	I still have to examine the dynamics of this here together with REAL_IS_FIXED.
	Basic point is: Don't use REAL_IS_FIXED with something else than generic or i386.

	Also, one should minimize code size by really ensuring that only functions that are really needed are included.
	Currently, all generic functions will be always there (to be safe for fallbacks for advanced decoders).
	Strictly, at least the synth_1to1 should not be necessary for single-decoder mode.
*/


/* Runtime optimization interface now here: */

enum optdec
{ /* autodec needs to be =0 and the first, nodec needs to be the last -- for loops! */
	autodec=0, generic, generic_dither, idrei,
	ivier, ifuenf, ifuenf_dither, mmx,
	dreidnow, dreidnowext, altivec, sse, x86_64, arm, neon,
	nodec
};
enum optcla { nocla=0, normal, mmxsse };

/*  - Set up the table of synth functions for current decoder choice. */
int frame_cpu_opt(mpg123_handle *fr, const char* cpu);
/*  - Choose, from the synth table, the synth functions to use for current output format/rate. */
int set_synth_functions(mpg123_handle *fr);
/*  - Parse decoder name and return numerical code. */
enum optdec dectype(const char* decoder);
/*  - Return the default decoder type. */
enum optdec defdec(void);
/*  - Return the class of a decoder type (mmxsse or normal). */
enum optcla decclass(const enum optdec);

/* Now comes a whole lot of definitions, for multi decoder mode and single decoder mode.
   Because of the latter, it may look redundant at times. */

/* this is included in mpg123.h, which includes config.h */
#ifdef CCALIGN
#define ALIGNED(a) __attribute__((aligned(a)))
#else
#define ALIGNED(a)
#endif

/* Safety catch for invalid decoder choice. */
#ifdef REAL_IS_FIXED
#if (defined OPT_I486)  || (defined OPT_I586) || (defined OPT_I586_DITHER) \
 || (defined OPT_MMX)   || (defined OPT_SSE)  || (defined_OPT_ALTIVEC) \
 || (defined OPT_3DNOW) || (defined OPT_3DNOWEXT) || (defined OPT_X86_64) \
 || (defined OPT_NEON) || (defined OPT_GENERIC_DITHER)
#error "Bad decoder choice together with fixed point math!"
#endif
#endif

#if (defined NO_LAYER1 && defined NO_LAYER2)
#define NO_LAYER12
#endif

#ifdef OPT_GENERIC
#ifndef OPT_MULTI
#	define defopt generic
#endif
#endif

#ifdef OPT_GENERIC_DITHER
#define OPT_DITHER
#ifndef OPT_MULTI
#	define defopt generic_dither
#endif
#endif

/* i486 is special... always alone! */
#ifdef OPT_I486
#define OPT_X86
#define defopt ivier
#ifdef OPT_MULTI
#error "i486 can only work alone!"
#endif
#define FIR_BUFFER_SIZE  128
#define FIR_SIZE 16
#endif

#ifdef OPT_I386
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt idrei
#endif
#endif

#ifdef OPT_I586
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt ifuenf
#endif
#endif

#ifdef OPT_I586_DITHER
#define OPT_X86
#define OPT_DITHER
#ifndef OPT_MULTI
#	define defopt ifuenf_dither
#endif
#endif

/* We still have some special code around MMX tables. */

#ifdef OPT_MMX
#define OPT_MMXORSSE
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt mmx
#endif
#endif

#ifdef OPT_SSE
#define OPT_MMXORSSE
#define OPT_MPLAYER
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt sse
#endif
#endif

#ifdef OPT_3DNOWEXT
#define OPT_MMXORSSE
#define OPT_MPLAYER
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt dreidnowext
#	define opt_dct36(fr) dct36_3dnowext
#endif
#endif

#ifdef OPT_MPLAYER
extern const int costab_mmxsse[];
#endif

/* 3dnow used to use synth_1to1_i586 for mono / 8bit conversion - was that intentional? */
/* I'm trying to skip the pentium code here ... until I see that that is indeed a bad idea */
#ifdef OPT_3DNOW
#define OPT_X86
#ifndef OPT_MULTI
#	define defopt dreidnow
#	define opt_dct36(fr) dct36_3dnow
#endif
#endif

#ifdef OPT_ALTIVEC
#ifndef OPT_MULTI
#	define defopt altivec
#endif
#endif

#ifdef OPT_X86_64
#define OPT_MMXORSSE
#ifndef OPT_MULTI
#	define defopt x86_64
#endif
#endif

#ifdef OPT_ARM
#ifndef OPT_MULTI
#	define defopt arm
#endif
#endif

#ifdef OPT_NEON
#define OPT_MMXORSSE
#ifndef OPT_MULTI
#	define defopt neon
#endif
#endif

/* used for multi opt mode and the single 3dnow mode to have the old 3dnow test flag still working */
void check_decoders(void);

/*
	Now come two blocks of standard definitions for multi-decoder mode and single-decoder mode.
	Most stuff is so automatic that it's indeed generated by some inline shell script.
	Remember to use these scripts when possible, instead of direct repetitive hacking.
*/

#ifdef OPT_MULTI

#	define defopt nodec

#	if (defined OPT_3DNOW || defined OPT_3DNOWEXT)
#		define opt_dct36(fr) ((fr)->cpu_opts.the_dct36)
#	endif

#endif /* OPT_MULTI else */

#	ifndef opt_dct36
#		define opt_dct36(fr) dct36
#	endif

#endif /* MPG123_H_OPTIMIZE */

