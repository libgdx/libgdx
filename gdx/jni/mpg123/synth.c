/*
	synth.c: The functions for synthesizing samples, at the end of decoding.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, heavily dissected and rearranged by Thomas Orgis
*/

#include "mpg123lib_intern.h"
#include "sample.h"

/* Stereo-related synth, wrapping over _some_ plain synth. */
int synth_stereo_wrap(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr){
	int clip;
	clip  = (fr->synth)(bandPtr_l, 0, fr, 0);
	clip += (fr->synth)(bandPtr_r, 1, fr, 1);
	return clip;
}

/*
	Part 1: All synth functions that produce signed short.
	That is:
		- synth_1to1 with cpu-specific variants (synth_1to1_i386, synth_1to1_i586 ...)
		- synth_1to1_mono and synth_1to1_mono2stereo; which use fr->synths.plain[r_1to1][f_16].
	Nearly every decoder variant has it's own synth_1to1, while the mono conversion is shared.
*/

#define SAMPLE_T short
#define WRITE_SAMPLE(samples,sum,clip) WRITE_SHORT_SAMPLE(samples,sum,clip)

/* Part 1a: All straight 1to1 decoding functions */
#define BLOCK 0x40 /* One decoding block is 64 samples. */

#define SYNTH_NAME synth_1to1
#include "synth.h"
#undef SYNTH_NAME

/* Mono-related synths; they wrap over _some_ synth_1to1. */
#define SYNTH_NAME       fr->synths.plain[r_1to1]
#define MONO_NAME        synth_1to1_mono
#define MONO2STEREO_NAME synth_1to1_mono2stereo
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

/* Now we have possibly some special synth_1to1 ...
   ... they produce signed short; the mono functions defined above work on the special synths, too. */

#undef BLOCK /* Following functions are so special that they don't need this. */

/* Assembler routines. */
//int synth_1to1_arm_asm(real *window, real *b0, short *samples, int bo1);
/* Hull for C mpg123 API */
int synth_1to1_arm(real *bandPtr,int channel, mpg123_handle *fr, int final){
	short *samples = (short *) (fr->buffer.data+fr->buffer.fill);

	real *b0, **buf;
	int bo1;
	int clip;

	// TODO: own
	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);
		
	if(!channel) {
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	} else {
		samples++;
		buf = fr->real_buffs[1];
	}

	if(fr->bo & 0x1) {
		b0 = buf[0];
		bo1 = fr->bo;
		dct64(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	} else {
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	clip = synth_1to1_arm_asm(fr->decwin, b0, samples, bo1);

	if(final) fr->buffer.fill += 128;

	return clip;
}

/*
	Part 1b: 2to1 synth.
	Only generic and i386 functions this time.
*/
#define BLOCK 0x20 /* One decoding block is 32 samples. */

#define SYNTH_NAME synth_2to1
#include "synth.h"
#undef SYNTH_NAME

#define SYNTH_NAME       fr->synths.plain[r_2to1]
#define MONO_NAME        synth_2to1_mono
#define MONO2STEREO_NAME synth_2to1_mono2stereo
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#undef BLOCK

/*
	Part 1c: 4to1 synth.
	Same procedure as above...
*/
#define BLOCK 0x10 /* One decoding block is 16 samples. */

#define SYNTH_NAME synth_4to1
#include "synth.h"
#undef SYNTH_NAME

#define SYNTH_NAME       fr->synths.plain[r_4to1] /* This is just for the _i386 one... gotta check if it is really useful... */
#define MONO_NAME        synth_4to1_mono
#define MONO2STEREO_NAME synth_4to1_mono2stereo
#include "synth_mono.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

#undef BLOCK

/*
	Part 1d: ntom synth.
	Same procedure as above... Just no extra play anymore, straight synth that uses the plain dct64.
*/

/* These are all in one header, there's no flexibility to gain. */
#define SYNTH_NAME       synth_ntom
#define MONO_NAME        synth_ntom_mono
#define MONO2STEREO_NAME synth_ntom_mono2stereo
#include "synth_ntom.h"
#undef SYNTH_NAME
#undef MONO_NAME
#undef MONO2STEREO_NAME

/* Done with short output. */
#undef SAMPLE_T
#undef WRITE_SAMPLE

