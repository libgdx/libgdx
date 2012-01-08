/*
	monosynth.h: generic mono related synth functions 

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, generalized by Thomas Orgis

	This header is used multiple times to create different variants of these functions.
	See decode.c and synth.h .
	Hint: BLOCK, MONO_NAME, MONO2STEREO_NAME, SYNTH_NAME and SAMPLE_T do vary.

	Thomas looked closely at the decode_1to1, decode_2to1 and decode_4to1 contents, seeing that they are too similar to be separate files.
	This is what resulted...

	Reason to separate this from synth.h:
	There are decoders that have a special synth_1to1 but still can use these generic derivations for the mono stuff.
	It generally makes a good deal of sense to set SYNTH_NAME to opt_synth_1to1(fr) (or opt_synth_2to1(fr), etc.).
*/

/* Mono synth, wrapping over SYNTH_NAME */
int MONO_NAME(real *bandPtr, mpg123_handle *fr)
{
	SAMPLE_T samples_tmp[BLOCK];
	SAMPLE_T *tmp1 = samples_tmp;
	int i,ret;

	/* save buffer stuff, trick samples_tmp into there, decode, restore */
	unsigned char *samples = fr->buffer.data;
	int pnt = fr->buffer.fill;
	fr->buffer.data = (unsigned char*) samples_tmp;
	fr->buffer.fill = 0;
	ret = SYNTH_NAME(bandPtr, 0, fr, 0); /* decode into samples_tmp */
	fr->buffer.data = samples; /* restore original value */

	/* now append samples from samples_tmp */
	samples += pnt; /* just the next mem in frame buffer */
	for(i=0;i<(BLOCK/2);i++)
	{
		*( (SAMPLE_T *)samples) = *tmp1;
		samples += sizeof(SAMPLE_T);
		tmp1 += 2;
	}
	fr->buffer.fill = pnt + (BLOCK/2)*sizeof(SAMPLE_T);

	return ret;
}

/* Mono to stereo synth, wrapping over SYNTH_NAME */
int MONO2STEREO_NAME(real *bandPtr, mpg123_handle *fr)
{
	int i,ret;
	unsigned char *samples = fr->buffer.data;

	ret = SYNTH_NAME(bandPtr,0,fr,1);
	samples += fr->buffer.fill - BLOCK*sizeof(SAMPLE_T);

	for(i=0;i<(BLOCK/2);i++)
	{
		((SAMPLE_T *)samples)[1] = ((SAMPLE_T *)samples)[0];
		samples+=2*sizeof(SAMPLE_T);
	}

	return ret;
}
