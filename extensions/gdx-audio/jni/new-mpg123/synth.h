/*
	synth.h: generic synth functions 

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, generalized by Thomas Orgis

	This header is used multiple times to create different variants of these functions.
	See decode.c and friends.
	Hint: BLOCK, MONO_NAME, MONO2STEREO_NAME, SYNTH_NAME and SAMPLE_T as well as WRITE_SAMPLE do vary.

	Thomas looked closely at the decode_1to1, decode_2to1 and decode_4to1 contents, seeing that they are too similar to be separate files.
	This is what resulted...

	Basically, you need one set of these functions for each output sample type.
	That currently means signed short, 8bit or float/double; though unsigned short may come, too.

	Define NO_AUTOINCREMENT i386 code that shall not rely on autoincrement.
	Actual benefit of this has to be examined; may apply to specific (old) compilers, only.
*/


/* Main synth function, uses the plain dct64 or dct64_i386. */
int SYNTH_NAME(real *bandPtr, int channel, mpg123_handle *fr, int final)
{
#ifndef NO_AUTOINCREMENT
#define BACKPEDAL 0x10 /* We use autoincrement and thus need this re-adjustment for window/b0. */
#define MY_DCT64 dct64
#else
#define BACKPEDAL 0x00 /* i386 code does not need that. */
#define MY_DCT64 dct64_i386
#endif
	static const int step = 2;
	SAMPLE_T *samples = (SAMPLE_T *) (fr->buffer.data + fr->buffer.fill);

	real *b0, **buf; /* (*buf)[0x110]; */
	int clip = 0; 
	int bo1;

	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);

	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
#ifdef USE_DITHER
		/* We always go forward 32 dither points (and back again for the second channel),
		   (re)sampling the noise the same way as the original signal. */
		fr->ditherindex -= 32;
#endif
		samples++;
		buf = fr->real_buffs[1];
	}
#ifdef USE_DITHER
	/* We check only once for the overflow of dither index here ...
	   this wraps differently than the original i586 dither code, in theory (but when DITHERSIZE % BLOCK/2 == 0 it's the same). */
	if(DITHERSIZE-fr->ditherindex < 32) fr->ditherindex = 0;
	/* And we define a macro for the dither action... */
	#define ADD_DITHER(fr,sum) sum+=fr->dithernoise[fr->ditherindex]; fr->ditherindex += 64/BLOCK;
#else
	#define ADD_DITHER(fr,sum)
#endif

	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		MY_DCT64(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		MY_DCT64(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	{
		register int j;
		real *window = fr->decwin + 16 - bo1;

		for(j=(BLOCK/4); j; j--, b0+=0x400/BLOCK-BACKPEDAL, window+=0x800/BLOCK-BACKPEDAL, samples+=step)
		{
			real sum;
#ifndef NO_AUTOINCREMENT
			sum  = REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
			sum += REAL_MUL_SYNTH(*window++, *b0++);
			sum -= REAL_MUL_SYNTH(*window++, *b0++);
#else
			sum  = REAL_MUL_SYNTH(window[0x0], b0[0x0]);
			sum -= REAL_MUL_SYNTH(window[0x1], b0[0x1]);
			sum += REAL_MUL_SYNTH(window[0x2], b0[0x2]);
			sum -= REAL_MUL_SYNTH(window[0x3], b0[0x3]);
			sum += REAL_MUL_SYNTH(window[0x4], b0[0x4]);
			sum -= REAL_MUL_SYNTH(window[0x5], b0[0x5]);
			sum += REAL_MUL_SYNTH(window[0x6], b0[0x6]);
			sum -= REAL_MUL_SYNTH(window[0x7], b0[0x7]);
			sum += REAL_MUL_SYNTH(window[0x8], b0[0x8]);
			sum -= REAL_MUL_SYNTH(window[0x9], b0[0x9]);
			sum += REAL_MUL_SYNTH(window[0xA], b0[0xA]);
			sum -= REAL_MUL_SYNTH(window[0xB], b0[0xB]);
			sum += REAL_MUL_SYNTH(window[0xC], b0[0xC]);
			sum -= REAL_MUL_SYNTH(window[0xD], b0[0xD]);
			sum += REAL_MUL_SYNTH(window[0xE], b0[0xE]);
			sum -= REAL_MUL_SYNTH(window[0xF], b0[0xF]);
#endif

			ADD_DITHER(fr,sum)
			WRITE_SAMPLE(samples,sum,clip);
		}

		{
			real sum;
			sum  = REAL_MUL_SYNTH(window[0x0], b0[0x0]);
			sum += REAL_MUL_SYNTH(window[0x2], b0[0x2]);
			sum += REAL_MUL_SYNTH(window[0x4], b0[0x4]);
			sum += REAL_MUL_SYNTH(window[0x6], b0[0x6]);
			sum += REAL_MUL_SYNTH(window[0x8], b0[0x8]);
			sum += REAL_MUL_SYNTH(window[0xA], b0[0xA]);
			sum += REAL_MUL_SYNTH(window[0xC], b0[0xC]);
			sum += REAL_MUL_SYNTH(window[0xE], b0[0xE]);

			ADD_DITHER(fr,sum)
			WRITE_SAMPLE(samples,sum,clip);
			samples += step;
			b0-=0x400/BLOCK;
			window-=0x800/BLOCK;
		}
		window += bo1<<1;

		for(j=(BLOCK/4)-1; j; j--, b0-=0x400/BLOCK+BACKPEDAL, window-=0x800/BLOCK-BACKPEDAL, samples+=step)
		{
			real sum;
#ifndef NO_AUTOINCREMENT
			sum = -REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
			sum -= REAL_MUL_SYNTH(*(--window), *b0++);
#else
			sum = -REAL_MUL_SYNTH(window[-0x1], b0[0x0]);
			sum -= REAL_MUL_SYNTH(window[-0x2], b0[0x1]);
			sum -= REAL_MUL_SYNTH(window[-0x3], b0[0x2]);
			sum -= REAL_MUL_SYNTH(window[-0x4], b0[0x3]);
			sum -= REAL_MUL_SYNTH(window[-0x5], b0[0x4]);
			sum -= REAL_MUL_SYNTH(window[-0x6], b0[0x5]);
			sum -= REAL_MUL_SYNTH(window[-0x7], b0[0x6]);
			sum -= REAL_MUL_SYNTH(window[-0x8], b0[0x7]);
			sum -= REAL_MUL_SYNTH(window[-0x9], b0[0x8]);
			sum -= REAL_MUL_SYNTH(window[-0xA], b0[0x9]);
			sum -= REAL_MUL_SYNTH(window[-0xB], b0[0xA]);
			sum -= REAL_MUL_SYNTH(window[-0xC], b0[0xB]);
			sum -= REAL_MUL_SYNTH(window[-0xD], b0[0xC]);
			sum -= REAL_MUL_SYNTH(window[-0xE], b0[0xD]);
			sum -= REAL_MUL_SYNTH(window[-0xF], b0[0xE]);
			sum -= REAL_MUL_SYNTH(window[-0x0], b0[0xF]); /* Is that right? 0x0? Just wondering... */
#endif
			ADD_DITHER(fr,sum)
			WRITE_SAMPLE(samples,sum,clip);
		}
	}

	if(final) fr->buffer.fill += BLOCK*sizeof(SAMPLE_T);

	return clip;
#undef ADD_DITHER
#undef BACKPEDAL
#undef MY_DCT64
}
