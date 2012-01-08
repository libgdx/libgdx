/*
	synth_ntom.h: ntom-resampling synth functions

	This header is used multiple times to create different variants of this function.
	Hint: MONO_NAME, MONO2STEREO_NAME, SYNTH_NAME and SAMPLE_T as well as WRITE_SAMPLE do vary.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp, generalized by Thomas Orgis

	Well, this is very simple resampling... you may or may not like what you hear.
	But it's cheap.
	But still, we don't implement a non-autoincrement version of this one.
*/

/* Note: These mono functions would also work generically,
   it's just that they need a runtime calculation for the conversion loop...
   The fixed XtoY functions have the chance for loop unrolling... */

int MONO_NAME(real *bandPtr, mpg123_handle *fr)
{
	SAMPLE_T samples_tmp[8*64];
	SAMPLE_T *tmp1 = samples_tmp;
	size_t i;
	int ret;

	size_t pnt = fr->buffer.fill;
	unsigned char *samples = fr->buffer.data;
	fr->buffer.data = (unsigned char*) samples_tmp;
	fr->buffer.fill = 0;
	ret = SYNTH_NAME(bandPtr, 0, fr, 1);
	fr->buffer.data = samples;

	samples += pnt;
	for(i=0;i<(fr->buffer.fill/(2*sizeof(SAMPLE_T)));i++)
	{
		*( (SAMPLE_T *)samples) = *tmp1;
		samples += sizeof(SAMPLE_T);
		tmp1 += 2;
	}
	fr->buffer.fill = pnt + (fr->buffer.fill/2);

	return ret;
}


int MONO2STEREO_NAME(real *bandPtr, mpg123_handle *fr)
{
	size_t i;
	int ret;
	size_t pnt1 = fr->buffer.fill;
	unsigned char *samples = fr->buffer.data + pnt1;

	ret = SYNTH_NAME(bandPtr, 0, fr, 1);

	for(i=0;i<((fr->buffer.fill-pnt1)/(2*sizeof(SAMPLE_T)));i++)
	{
		((SAMPLE_T *)samples)[1] = ((SAMPLE_T *)samples)[0];
		samples+=2*sizeof(SAMPLE_T);
	}

	return ret;
}


int SYNTH_NAME(real *bandPtr,int channel, mpg123_handle *fr, int final)
{
	static const int step = 2;
	SAMPLE_T *samples = (SAMPLE_T *) (fr->buffer.data + fr->buffer.fill);

	real *b0, **buf; /* (*buf)[0x110]; */
	int clip = 0; 
	int bo1;
	int ntom;

	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);

	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
		ntom = fr->ntom_val[1] = fr->ntom_val[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
		ntom = fr->ntom_val[1];
	}

	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}

	{
		register int j;
		real *window = fr->decwin + 16 - bo1;

		for (j=16;j;j--,window+=0x10)
		{
			real sum;

			ntom += fr->ntom_step;
			if(ntom < NTOM_MUL)
			{
				window += 16;
				b0 += 16;
				continue;
			}

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

			while(ntom >= NTOM_MUL)
			{
				WRITE_SAMPLE(samples,sum,clip);
				samples += step;
				ntom -= NTOM_MUL;
			}
		}

		ntom += fr->ntom_step;
		if(ntom >= NTOM_MUL)
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

			while(ntom >= NTOM_MUL)
			{
				WRITE_SAMPLE(samples,sum,clip);
				samples += step;
				ntom -= NTOM_MUL;
			}
		}

		b0-=0x10,window-=0x20;
		window += bo1<<1;

		for (j=15;j;j--,b0-=0x20,window-=0x10)
		{
			real sum;

			ntom += fr->ntom_step;
			if(ntom < NTOM_MUL)
			{
				window -= 16;
				b0 += 16;
				continue;
			}

			sum = REAL_MUL_SYNTH(-*(--window), *b0++);
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

			while(ntom >= NTOM_MUL)
			{
				WRITE_SAMPLE(samples,sum,clip);
				samples += step;
				ntom -= NTOM_MUL;
			}
		}
	}

	fr->ntom_val[channel] = ntom;
	if(final) fr->buffer.fill = ((unsigned char *) samples - fr->buffer.data - (channel ? sizeof(SAMPLE_T) : 0));

	return clip;
}

