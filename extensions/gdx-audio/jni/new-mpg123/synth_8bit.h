/*
	synth_8bit.h: Wrappers over optimized synth_xtoy for converting signed short to 8bit.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, code generalized to the wrapper by Thomas Orgis

	Only variable is the BLOCK size to choose 1to1, 2to1 or 4to1.
	Oh, and the names: BASE_SYNTH_NAME, SYNTH_NAME, MONO_NAME, MONO2STEREO_NAME
	(p.ex. opt_synth_1to1(fr), synth_1to1_8bit, synth_1to1_8bit_mono, ...).
*/

int SYNTH_NAME(real *bandPtr, int channel, mpg123_handle *fr, int final)
{
	short samples_tmp[BLOCK];
	short *tmp1 = samples_tmp + channel;
	int i,ret;

	unsigned char *samples = fr->buffer.data;
	int pnt = fr->buffer.fill;
	fr->buffer.data = (unsigned char*) samples_tmp;
	fr->buffer.fill = 0;
	ret = BASE_SYNTH_NAME(bandPtr, channel, fr , 0);
	fr->buffer.data = samples;

	samples += channel + pnt;
	for(i=0;i<(BLOCK/2);i++)
	{
		*samples = fr->conv16to8[*tmp1>>AUSHIFT];
		samples += 2;
		tmp1 += 2;
	}
	fr->buffer.fill = pnt + (final ? BLOCK : 0 );

	return ret;
}

int MONO_NAME(real *bandPtr, mpg123_handle *fr)
{
	short samples_tmp[BLOCK];
	short *tmp1 = samples_tmp;
	int i,ret;
 
	unsigned char *samples = fr->buffer.data;
	int pnt = fr->buffer.fill;
	fr->buffer.data = (unsigned char*) samples_tmp;
	fr->buffer.fill = 0;
	ret = BASE_SYNTH_NAME(bandPtr, 0, fr, 0);
	fr->buffer.data = samples;

	samples += pnt;
	for(i=0;i<(BLOCK/2);i++)
	{
		*samples++ = fr->conv16to8[*tmp1>>AUSHIFT];
		tmp1+=2;
	}
	fr->buffer.fill = pnt + BLOCK/2;

	return ret;
}

int MONO2STEREO_NAME(real *bandPtr, mpg123_handle *fr)
{
	short samples_tmp[BLOCK];
	short *tmp1 = samples_tmp;
	int i,ret;

	unsigned char *samples = fr->buffer.data;
	int pnt = fr->buffer.fill;
	fr->buffer.data = (unsigned char*) samples_tmp;
	fr->buffer.fill = 0;
	ret = BASE_SYNTH_NAME(bandPtr, 0, fr, 0);
	fr->buffer.data = samples;

	samples += pnt;
	for(i=0;i<(BLOCK/2);i++)
	{
		*samples++ = fr->conv16to8[*tmp1>>AUSHIFT];
		*samples++ = fr->conv16to8[*tmp1>>AUSHIFT];
		tmp1 += 2;
	}
	fr->buffer.fill = pnt + BLOCK;

	return ret;
}

