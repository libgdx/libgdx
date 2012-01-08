/*
	synth_s32.c: The functions for synthesizing real (float) samples, at the end of decoding.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, heavily dissected and rearranged by Thomas Orgis
*/

#include "mpg123lib_intern.h"
#include "sample.h"
#include "debug.h"

#ifdef REAL_IS_FIXED
#error "Do not build this file with fixed point math!"
#else
/* 
	Part 4: All synth functions that produce signed 32 bit output.
	What we need is just a special WRITE_SAMPLE.
*/

#define SAMPLE_T int32_t
#define WRITE_SAMPLE(samples,sum,clip) WRITE_S32_SAMPLE(samples,sum,clip)

/* Part 4a: All straight 1to1 decoding functions */
#define BLOCK 0x40 /* One decoding block is 64 samples. */

#define SYNTH_NAME synth_1to1_s32
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_1to1_s32 (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_1to1][f_32]
#define MONO_NAME        synth_1to1_s32_mono
#define MONO2STEREO_NAME synth_1to1_s32_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_1to1_s32_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

#ifdef OPT_X86_64
/* Assembler routines. */
int synth_1to1_s32_x86_64_asm(real *window, real *b0, int32_t *samples, int bo1);
int synth_1to1_s32_s_x86_64_asm(real *window, real *b0l, real *b0r, int32_t *samples, int bo1);
void dct64_real_x86_64(real *out0, real *out1, real *samples);
/* Hull for C mpg123 API */
int synth_1to1_s32_x86_64(real *bandPtr,int channel, mpg123_handle *fr, int final)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0, **buf;
	int bo1;
	int clip;

	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);

	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}

	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_real_x86_64(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_real_x86_64(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	clip = synth_1to1_s32_x86_64_asm(fr->decwin, b0, samples, bo1);

	if(final) fr->buffer.fill += 256;

	return clip;
}


int synth_1to1_s32_stereo_x86_64(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0l, *b0r, **bufl, **bufr;
	int bo1;
	int clip;

	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}

	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];

	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_real_x86_64(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_real_x86_64(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_real_x86_64(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_real_x86_64(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}

	clip = synth_1to1_s32_s_x86_64_asm(fr->decwin, b0l, b0r, samples, bo1);

	fr->buffer.fill += 256;

	return clip;
}
#endif

#ifdef OPT_SSE
/* Assembler routines. */
int synth_1to1_s32_sse_asm(real *window, real *b0, int32_t *samples, int bo1);
int synth_1to1_s32_s_sse_asm(real *window, real *b0l, real *b0r, int32_t *samples, int bo1);
void dct64_real_sse(real *out0, real *out1, real *samples);
/* Hull for C mpg123 API */
int synth_1to1_s32_sse(real *bandPtr,int channel, mpg123_handle *fr, int final)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0, **buf;
	int bo1;
	int clip;

	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);

	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}

	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_real_sse(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_real_sse(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	clip = synth_1to1_s32_sse_asm(fr->decwin, b0, samples, bo1);

	if(final) fr->buffer.fill += 256;

	return clip;
}


int synth_1to1_s32_stereo_sse(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0l, *b0r, **bufl, **bufr;
	int bo1;
	int clip;

	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}

	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];

	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_real_sse(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_real_sse(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_real_sse(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_real_sse(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}

	clip = synth_1to1_s32_s_sse_asm(fr->decwin, b0l, b0r, samples, bo1);

	fr->buffer.fill += 256;

	return clip;
}
#endif

#ifdef OPT_NEON
/* Assembler routines. */
int synth_1to1_s32_neon_asm(real *window, real *b0, int32_t *samples, int bo1);
int synth_1to1_s32_s_neon_asm(real *window, real *b0l, real *b0r, int32_t *samples, int bo1);
void dct64_real_neon(real *out0, real *out1, real *samples);
/* Hull for C mpg123 API */
int synth_1to1_s32_neon(real *bandPtr,int channel, mpg123_handle *fr, int final)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0, **buf;
	int bo1;
	int clip;

	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);

	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}

	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_real_neon(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_real_neon(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	clip = synth_1to1_s32_neon_asm(fr->decwin, b0, samples, bo1);

	if(final) fr->buffer.fill += 256;

	return clip;
}

int synth_1to1_s32_stereo_neon(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);

	real *b0l, *b0r, **bufl, **bufr;
	int bo1;
	int clip;

	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}

	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];

	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_real_neon(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_real_neon(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_real_neon(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_real_neon(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}

	clip = synth_1to1_s32_s_neon_asm(fr->decwin, b0l, b0r, samples, bo1);

	fr->buffer.fill += 256;

	return clip;
}
#endif

#undef BLOCK

#ifndef NO_DOWNSAMPLE

/*
	Part 4b: 2to1 synth. Only generic and i386.
*/
#define BLOCK 0x20 /* One decoding block is 32 samples. */

#define SYNTH_NAME synth_2to1_s32
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_2to1_s32 (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_2to1][f_32]
#define MONO_NAME        synth_2to1_s32_mono
#define MONO2STEREO_NAME synth_2to1_s32_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_2to1_s32_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

#undef BLOCK

/*
	Part 4c: 4to1 synth. Only generic and i386.
*/
#define BLOCK 0x10 /* One decoding block is 16 samples. */

#define SYNTH_NAME synth_4to1_s32
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_4to1_s32 (could be generic, could be i386). */
#define SYNTH_NAME       fr->synths.plain[r_4to1][f_32]
#define MONO_NAME        synth_4to1_s32_mono
#define MONO2STEREO_NAME synth_4to1_s32_m2s
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#ifdef OPT_X86
#define NO_AUTOINCREMENT
#define SYNTH_NAME synth_4to1_s32_i386
#include "synth.h"
#undef SYNTH_NAME
/* i386 uses the normal mono functions. */
#undef NO_AUTOINCREMENT
#endif

#undef BLOCK

#endif /* NO_DOWNSAMPLE */

#ifndef NO_NTOM
/*
	Part 4d: ntom synth.
	Same procedure as above... Just no extra play anymore, straight synth that may use an optimized dct64.
*/

/* These are all in one header, there's no flexibility to gain. */
#define SYNTH_NAME       synth_ntom_s32
#define MONO_NAME        synth_ntom_s32_mono
#define MONO2STEREO_NAME synth_ntom_s32_m2s
#include "synth_ntom.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#endif

#undef SAMPLE_T
#undef WRITE_SAMPLE

#endif /* non-fixed type */
