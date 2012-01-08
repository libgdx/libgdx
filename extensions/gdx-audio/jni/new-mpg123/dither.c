/*
	dither: Generate shaped noise for dithering

	copyright 2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Taihei Monma
*/

#include "config.h"
#include "compat.h"
#include "dither.h"

static const uint32_t init_seed = 2463534242UL;

#define LAP 100

/*
	xorshift random number generator, with output scaling to [-0.5, 0.5]
	This is the white noise...
	See http://www.jstatsoft.org/v08/i14/paper on XOR shift random number generators.
*/
static float rand_xorshift32(uint32_t *seed)
{
	union
	{
		uint32_t i;
		float f;
	} fi;
	
	fi.i = *seed;
	fi.i ^= (fi.i<<13);
	fi.i ^= (fi.i>>17);
	fi.i ^= (fi.i<<5);
	*seed = fi.i;
	
	/* scale the number to [-0.5, 0.5] */
#ifdef IEEE_FLOAT
	fi.i = (fi.i>>9)|0x3f800000;
	fi.f -= 1.5f;
#else
	fi.f = (double)fi.i / 4294967295.0;
	fi.f -= 0.5f;
#endif
	return fi.f;
}

static void white_noise(float *table, size_t count)
{
	size_t i;
	uint32_t seed = init_seed;
	
	for(i=0; i<count; ++i)
	table[i] = rand_xorshift32(&seed);
}

static void tpdf_noise(float *table, size_t count)
{
	size_t i;
	uint32_t seed = init_seed;
	
	for(i=0; i<count; ++i)
	table[i] = rand_xorshift32(&seed) + rand_xorshift32(&seed);
}

static void highpass_tpdf_noise(float *table, size_t count)
{
	size_t i;
	uint32_t seed = init_seed;
	/* Ensure some minimum lap for keeping the high-pass filter circular. */
	size_t lap = count > 2*LAP ? LAP : count/2;

	float input_noise;
	float xv[9], yv[9];

	for(i=0;i<9;i++)
	{
		xv[i] = yv[i] = 0.0f;
	}

	for(i=0;i<count+lap;i++)
	{
		if(i==count) seed=init_seed;
		
		/* generate and add 2 random numbers, to make a TPDF noise distribution */
		input_noise = rand_xorshift32(&seed) + rand_xorshift32(&seed);

		/* apply 8th order Chebyshev high-pass IIR filter */
		/* Coefficients are from http://www-users.cs.york.ac.uk/~fisher/mkfilter/trad.html
		   Given parameters are: Chebyshev, Highpass, ripple=-1, order=8, samplerate=44100, corner1=19000 */
		xv[0] = xv[1]; xv[1] = xv[2]; xv[2] = xv[3]; xv[3] = xv[4]; xv[4] = xv[5]; xv[5] = xv[6]; xv[6] = xv[7]; xv[7] = xv[8]; 
		xv[8] = input_noise / 1.382814179e+07;
		yv[0] = yv[1]; yv[1] = yv[2]; yv[2] = yv[3]; yv[3] = yv[4]; yv[4] = yv[5]; yv[5] = yv[6]; yv[6] = yv[7]; yv[7] = yv[8]; 
		yv[8] = (xv[0] + xv[8]) - 8 * (xv[1] + xv[7]) + 28 * (xv[2] + xv[6])
				- 56 * (xv[3] + xv[5]) + 70 * xv[4]
				+ ( -0.6706204984 * yv[0]) + ( -5.3720827038 * yv[1])
				+ (-19.0865382480 * yv[2]) + (-39.2831607860 * yv[3])
				+ (-51.2308985070 * yv[4]) + (-43.3590135780 * yv[5])
				+ (-23.2632305320 * yv[6]) + ( -7.2370122050 * yv[7]);
		if(i>=lap) table[i-lap] = yv[8] * 3.0f;
	}
}

void mpg123_noise(float* table, size_t count, enum mpg123_noise_type noisetype)
{
	switch(noisetype)
	{
		case mpg123_white_noise: white_noise(table, count); break;
		case mpg123_tpdf_noise:  tpdf_noise(table, count);  break;
		case mpg123_highpass_tpdf_noise:
			highpass_tpdf_noise(table, count);
		break;
	}
}

/* Generate white noise and shape it with a high pass filter. */
void dither_table_init(float *dithertable)
{
	highpass_tpdf_noise(dithertable, DITHERSIZE);
}
