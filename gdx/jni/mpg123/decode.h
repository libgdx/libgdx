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

#define OUT_FORMATS 2 /* Only up to 16bit */

#define OUT_16 0
#define OUT_8  1

#define NTOM_MAX 8          /* maximum allowed factor for upsampling */
#define NTOM_MAX_FREQ 96000 /* maximum frequency to upsample to / downsample from */
#define NTOM_MUL (32768)

/* Let's collect all possible synth functions here, for an overview.
   If they are actually defined and used depends on preprocessor machinery.
   See synth.c and optimize.h for that, also some special C and assembler files. */

/* The call of left and right plain synth, wrapped.
   This may be replaced by a direct stereo optimized synth. */
int synth_stereo_wrap(real*, real*, mpg123_handle*);

/* The signed-16bit-producing variants. */
int synth_1to1            (real*, int, mpg123_handle*, int);
int synth_1to1_arm        (real*, int, mpg123_handle*, int);
/* These mono/stereo converters use one of the above for the grunt work. */
int synth_1to1_mono       (real*, mpg123_handle*);
int synth_1to1_mono2stereo(real*, mpg123_handle*);
/* Sample rate decimation comes in less flavours. */
int synth_2to1            (real*, int, mpg123_handle*, int);
int synth_2to1_mono       (real*, mpg123_handle*);
int synth_2to1_mono2stereo(real*, mpg123_handle*);
int synth_4to1            (real *,int, mpg123_handle*, int);
int synth_4to1_mono       (real*, mpg123_handle*);
int synth_4to1_mono2stereo(real*, mpg123_handle*);
/* NtoM is really just one implementation. */
int synth_ntom (real *,int, mpg123_handle*, int);
int synth_ntom_mono (real *, mpg123_handle *);
int synth_ntom_mono2stereo (real *, mpg123_handle *);

/* Inside these synth functions, some dct64 variants may be used.
   The special optimized ones that only appear in assembler code are not mentioned here.
   And, generally, these functions are only employed in a matching synth function. */
void dct64        (real *,real *,real *);

/* This is used by the layer 3 decoder, one generic function and 3DNow variants. */
void dct36         (real *,real *,real *,real *,real *);

/* Tools for NtoM resampling synth, defined in ntom.c . */
int synth_ntom_set_step(mpg123_handle *fr); /* prepare ntom decoding */
unsigned long ntom_val(mpg123_handle *fr, off_t frame); /* compute ntom_val for frame offset */
/* Frame and sample offsets. */
off_t ntom_frmouts(mpg123_handle *fr, off_t frame);
off_t ntom_ins2outs(mpg123_handle *fr, off_t ins);
off_t ntom_frameoff(mpg123_handle *fr, off_t soff);

/* Initialization of any static data that majy be needed at runtime.
   Make sure you call these once before it is too late. */
void init_layer3(void);
real init_layer3_gainpow2(mpg123_handle *fr, int i);
void init_layer3_stuff(mpg123_handle *fr, real (*gainpow2)(mpg123_handle *fr, int i));
void  init_layer12(void);
real* init_layer12_table(mpg123_handle *fr, real *table, int m);
void  init_layer12_stuff(mpg123_handle *fr, real* (*init_table)(mpg123_handle *fr, real *table, int m));

void prepare_decode_tables(void);

extern real *pnts[5]; /* tabinit provides, dct64 needs */

/* Runtime (re)init functions; needed more often. */
void make_decode_tables(mpg123_handle *fr); /* For every volume change. */

/* These are the actual workers.
   They operate on the parsed frame data and handle decompression to audio samples.
   The synth functions defined above are called from inside the layer handlers. */
int do_layer3(mpg123_handle *fr);
int do_layer2(mpg123_handle *fr);
int do_layer1(mpg123_handle *fr);

#endif

