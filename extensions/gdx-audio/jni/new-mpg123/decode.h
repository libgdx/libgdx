/*
	decode.h: common definitions for decode functions

	This file is strongly tied with optimize.h concerning the synth functions.
	Perhaps one should restructure that a bit.

	copyright 2007-8 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis, taking WRITE_SAMPLE from decode.c
*/
#ifndef MPG123_DECODE_H
#define MPG123_DECODE_H

/* Selection of class of output routines for basic format. */
#ifndef REAL_IS_FIXED
#define OUT_FORMATS 4 /* Basic output formats: 16bit, 8bit, real and s32 */
#else
#define OUT_FORMATS 2 /* Only up to 16bit */
#endif

#define OUT_16 0
#define OUT_8  1
/* Those are defined but not supported for fixed point decoding! */
#define OUT_REAL 2 /* Write a floating point sample (that is, one matching the internal real type). */
#define OUT_S32 3

#ifdef NO_NTOM
#define NTOM_MAX 1
#else
#define NTOM_MAX 8          /* maximum allowed factor for upsampling */
#define NTOM_MAX_FREQ 96000 /* maximum frequency to upsample to / downsample from */
#define NTOM_MUL (32768)
void ntom_set_ntom(mpg123_handle *fr, off_t num);
#endif

/* Let's collect all possible synth functions here, for an overview.
   If they are actually defined and used depends on preprocessor machinery.
   See synth.c and optimize.h for that, also some special C and assembler files. */

#ifndef NO_16BIT
/* The signed-16bit-producing variants. */
int synth_1to1            (real*, int, mpg123_handle*, int);
int synth_1to1_dither     (real*, int, mpg123_handle*, int);
int synth_1to1_i386       (real*, int, mpg123_handle*, int);
int synth_1to1_i586       (real*, int, mpg123_handle*, int);
int synth_1to1_i586_dither(real*, int, mpg123_handle*, int);
int synth_1to1_mmx        (real*, int, mpg123_handle*, int);
int synth_1to1_3dnow      (real*, int, mpg123_handle*, int);
int synth_1to1_sse        (real*, int, mpg123_handle*, int);
int synth_1to1_stereo_sse (real*, real*, mpg123_handle*);
int synth_1to1_3dnowext   (real*, int, mpg123_handle*, int);
int synth_1to1_altivec    (real*, int, mpg123_handle*, int);
int synth_1to1_stereo_altivec(real*, real*, mpg123_handle*);
int synth_1to1_x86_64     (real*, int, mpg123_handle*, int);
int synth_1to1_stereo_x86_64(real*, real*, mpg123_handle*);
int synth_1to1_arm        (real*, int, mpg123_handle*, int);
int synth_1to1_neon       (real*, int, mpg123_handle*, int);
int synth_1to1_stereo_neon(real*, real*, mpg123_handle*);
/* This is different, special usage in layer3.c only.
   Hence, the name... and now forget about it.
   Never use it outside that special portion of code inside layer3.c! */
int absynth_1to1_i486(real*, int, mpg123_handle*, int);
/* These mono/stereo converters use one of the above for the grunt work. */
int synth_1to1_mono       (real*, mpg123_handle*);
int synth_1to1_m2s(real*, mpg123_handle*);

/* Sample rate decimation comes in less flavours. */
#ifndef NO_DOWNSAMPLE
int synth_2to1            (real*, int, mpg123_handle*, int);
int synth_2to1_dither     (real*, int, mpg123_handle*, int);
int synth_2to1_i386       (real*, int, mpg123_handle*, int);
int synth_2to1_mono       (real*, mpg123_handle*);
int synth_2to1_m2s(real*, mpg123_handle*);
int synth_4to1            (real *,int, mpg123_handle*, int);
int synth_4to1_dither     (real *,int, mpg123_handle*, int);
int synth_4to1_i386       (real*, int, mpg123_handle*, int);
int synth_4to1_mono       (real*, mpg123_handle*);
int synth_4to1_m2s(real*, mpg123_handle*);
#endif
#ifndef NO_NTOM
/* NtoM is really just one implementation. */
int synth_ntom (real *,int, mpg123_handle*, int);
int synth_ntom_mono (real *, mpg123_handle *);
int synth_ntom_m2s (real *, mpg123_handle *);
#endif
#endif

#ifndef NO_8BIT
/* The 8bit-producing variants. */
/* There are direct 8-bit synths and wrappers over a possibly optimized 16bit one. */
int synth_1to1_8bit            (real*, int, mpg123_handle*, int);
int synth_1to1_8bit_i386       (real*, int, mpg123_handle*, int);
#ifndef NO_16BIT
int synth_1to1_8bit_wrap       (real*, int, mpg123_handle*, int);
int synth_1to1_8bit_mono       (real*, mpg123_handle*);
#endif
int synth_1to1_8bit_m2s(real*, mpg123_handle*);
#ifndef NO_16BIT
int synth_1to1_8bit_wrap_mono       (real*, mpg123_handle*);
int synth_1to1_8bit_wrap_m2s(real*, mpg123_handle*);
#endif
#ifndef NO_DOWNSAMPLE
int synth_2to1_8bit            (real*, int, mpg123_handle*, int);
int synth_2to1_8bit_i386       (real*, int, mpg123_handle*, int);
int synth_2to1_8bit_mono       (real*, mpg123_handle*);
int synth_2to1_8bit_m2s(real*, mpg123_handle*);
int synth_4to1_8bit            (real*, int, mpg123_handle*, int);
int synth_4to1_8bit_i386       (real*, int, mpg123_handle*, int);
int synth_4to1_8bit_mono       (real*, mpg123_handle*);
int synth_4to1_8bit_m2s(real*, mpg123_handle*);
#endif
#ifndef NO_NTOM
int synth_ntom_8bit            (real*, int, mpg123_handle*, int);
int synth_ntom_8bit_mono       (real*, mpg123_handle*);
int synth_ntom_8bit_m2s(real*, mpg123_handle*);
#endif
#endif

#ifndef REAL_IS_FIXED

#ifndef NO_REAL
/* The real-producing variants. */
int synth_1to1_real            (real*, int, mpg123_handle*, int);
int synth_1to1_real_i386       (real*, int, mpg123_handle*, int);
int synth_1to1_real_sse        (real*, int, mpg123_handle*, int);
int synth_1to1_real_stereo_sse (real*, real*, mpg123_handle*);
int synth_1to1_real_x86_64     (real*, int, mpg123_handle*, int);
int synth_1to1_real_stereo_x86_64(real*, real*, mpg123_handle*);
int synth_1to1_real_altivec    (real*, int, mpg123_handle*, int);
int synth_1to1_real_stereo_altivec(real*, real*, mpg123_handle*);
int synth_1to1_real_neon       (real*, int, mpg123_handle*, int);
int synth_1to1_real_stereo_neon(real*, real*, mpg123_handle*);
int synth_1to1_real_mono       (real*, mpg123_handle*);
int synth_1to1_real_m2s(real*, mpg123_handle*);
#ifndef NO_DOWNSAMPLE
int synth_2to1_real            (real*, int, mpg123_handle*, int);
int synth_2to1_real_i386       (real*, int, mpg123_handle*, int);
int synth_2to1_real_mono       (real*, mpg123_handle*);
int synth_2to1_real_m2s(real*, mpg123_handle*);
int synth_4to1_real            (real*, int, mpg123_handle*, int);
int synth_4to1_real_i386       (real*, int, mpg123_handle*, int);
int synth_4to1_real_mono       (real*, mpg123_handle*);
int synth_4to1_real_m2s(real*, mpg123_handle*);
#endif
#ifndef NO_NTOM
int synth_ntom_real            (real*, int, mpg123_handle*, int);
int synth_ntom_real_mono       (real*, mpg123_handle*);
int synth_ntom_real_m2s(real*, mpg123_handle*);
#endif
#endif

#ifndef NO_32BIT
/* 32bit integer */
int synth_1to1_s32            (real*, int, mpg123_handle*, int);
int synth_1to1_s32_i386       (real*, int, mpg123_handle*, int);
int synth_1to1_s32_sse        (real*, int, mpg123_handle*, int);
int synth_1to1_s32_stereo_sse (real*, real*, mpg123_handle*);
int synth_1to1_s32_x86_64     (real*, int, mpg123_handle*, int);
int synth_1to1_s32_stereo_x86_64(real*, real*, mpg123_handle*);
int synth_1to1_s32_altivec    (real*, int, mpg123_handle*, int);
int synth_1to1_s32_stereo_altivec(real*, real*, mpg123_handle*);
int synth_1to1_s32_neon       (real*, int, mpg123_handle*, int);
int synth_1to1_s32_stereo_neon(real*, real*, mpg123_handle*);
int synth_1to1_s32_mono       (real*, mpg123_handle*);
int synth_1to1_s32_m2s(real*, mpg123_handle*);
#ifndef NO_DOWNSAMPLE
int synth_2to1_s32            (real*, int, mpg123_handle*, int);
int synth_2to1_s32_i386       (real*, int, mpg123_handle*, int);
int synth_2to1_s32_mono       (real*, mpg123_handle*);
int synth_2to1_s32_m2s(real*, mpg123_handle*);
int synth_4to1_s32            (real*, int, mpg123_handle*, int);
int synth_4to1_s32_i386       (real*, int, mpg123_handle*, int);
int synth_4to1_s32_mono       (real*, mpg123_handle*);
int synth_4to1_s32_m2s(real*, mpg123_handle*);
#endif
#ifndef NO_NTOM
int synth_ntom_s32            (real*, int, mpg123_handle*, int);
int synth_ntom_s32_mono       (real*, mpg123_handle*);
int synth_ntom_s32_m2s(real*, mpg123_handle*);
#endif
#endif

#endif /* FIXED */


/* Inside these synth functions, some dct64 variants may be used.
   The special optimized ones that only appear in assembler code are not mentioned here.
   And, generally, these functions are only employed in a matching synth function. */
void dct64        (real *,real *,real *);
void dct64_i386   (real *,real *,real *);
void dct64_altivec(real *,real *,real *);
void dct64_i486(int*, int* , real*); /* Yeah, of no use outside of synth_i486.c .*/

/* This is used by the layer 3 decoder, one generic function and 3DNow variants. */
void dct36         (real *,real *,real *,real *,real *);
void dct36_3dnow   (real *,real *,real *,real *,real *);
void dct36_3dnowext(real *,real *,real *,real *,real *);

/* Tools for NtoM resampling synth, defined in ntom.c . */
int synth_ntom_set_step(mpg123_handle *fr); /* prepare ntom decoding */
unsigned long ntom_val(mpg123_handle *fr, off_t frame); /* compute ntom_val for frame offset */
/* Frame and sample offsets. */
#ifndef NO_NTOM
/*
	Outsamples of _this_ frame.
	To be exact: The samples to be expected from the next frame decode (using the current ntom_val). When you already decoded _this_ frame, this is the number of samples to be expected from the next one.
*/
off_t ntom_frame_outsamples(mpg123_handle *fr);
/* Total out/insample offset. */
off_t ntom_frmouts(mpg123_handle *fr, off_t frame);
off_t ntom_ins2outs(mpg123_handle *fr, off_t ins);
off_t ntom_frameoff(mpg123_handle *fr, off_t soff);
#endif

/* Initialization of any static data that majy be needed at runtime.
   Make sure you call these once before it is too late. */
#ifndef NO_LAYER3
void init_layer3(void);
real init_layer3_gainpow2(mpg123_handle *fr, int i);
void init_layer3_stuff(mpg123_handle *fr, real (*gainpow2)(mpg123_handle *fr, int i));
#endif
#ifndef NO_LAYER12
void  init_layer12(void);
real* init_layer12_table(mpg123_handle *fr, real *table, int m);
void  init_layer12_stuff(mpg123_handle *fr, real* (*init_table)(mpg123_handle *fr, real *table, int m));
#endif

void prepare_decode_tables(void);

extern real *pnts[5]; /* tabinit provides, dct64 needs */

/* Runtime (re)init functions; needed more often. */
void make_decode_tables(mpg123_handle *fr); /* For every volume change. */
/* Stuff needed after updating synth setup (see set_synth_functions()). */

#ifdef OPT_MMXORSSE
/* Special treatment for mmx-like decoders, these functions go into the slots below. */
void make_decode_tables_mmx(mpg123_handle *fr);
#ifndef NO_LAYER3
real init_layer3_gainpow2_mmx(mpg123_handle *fr, int i);
#endif
#ifndef NO_LAYER12
real* init_layer12_table_mmx(mpg123_handle *fr, real *table, int m);
#endif
#endif

#ifndef NO_8BIT
/* Needed when switching to 8bit output. */
int make_conv16to8_table(mpg123_handle *fr);
#endif

/* These are the actual workers.
   They operate on the parsed frame data and handle decompression to audio samples.
   The synth functions defined above are called from inside the layer handlers. */

#ifndef NO_LAYER3
int do_layer3(mpg123_handle *fr);
#endif
#ifndef NO_LAYER2
int do_layer2(mpg123_handle *fr);
#endif
#ifndef NO_LAYER1
int do_layer1(mpg123_handle *fr);
#endif
/* There's an 3DNow counterpart in asm. */
void do_equalizer(real *bandPtr,int channel, real equalizer[2][32]);

#endif
