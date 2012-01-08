/*
	ntom.c: N->M down/up sampling; the setup code.

	copyright 1995-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
*/

#define SAFE_NTOM /* Do not depend on off_t*off_t with big values still being in the range... */
#include "mpg123lib_intern.h"

int synth_ntom_set_step(mpg123_handle *fr) {
	long m,n;
	m = frame_freq(fr);
	n = fr->af.rate;

	if(n > NTOM_MAX_FREQ || m > NTOM_MAX_FREQ || m <= 0 || n <= 0) {
		fr->err = MPG123_BAD_RATE;
		return -1;
	}

	n *= NTOM_MUL;
	fr->ntom_step = (unsigned long) n / m;

	if(fr->ntom_step > (unsigned long)NTOM_MAX*NTOM_MUL) {
		fr->err = MPG123_BAD_RATE;
		return -1;
	}

	fr->ntom_val[0] = fr->ntom_val[1] = ntom_val(fr, fr->num);
	return 0;
}

/*
	The SAFE_NTOM does iterative loops instead of straight multiplication.
	The safety is not just about the algorithm closely mimicking the decoder instead of applying some formula,
	it is more about avoiding multiplication of possibly big sample offsets (a 32bit off_t could overflow too easily).
*/

unsigned long ntom_val(mpg123_handle *fr, off_t frame) {
	off_t ntm;
#ifdef SAFE_NTOM /* Carry out the loop, without the threatening integer overflow. */
	off_t f;
	ntm = NTOM_MUL>>1; /* for frame 0 */
	for(f=0; f<frame; ++f)   /* for frame > 0 */ {
		ntm += spf(fr)*fr->ntom_step;
		ntm -= (ntm/NTOM_MUL)*NTOM_MUL;
	}
#else /* Just make one computation with overall sample offset. */
	ntm  = (NTOM_MUL>>1) + spf(fr)*frame*fr->ntom_step;
	ntm -= (ntm/NTOM_MUL)*NTOM_MUL;
#endif
	return (unsigned long) ntm;
}

/* Set the ntom value for next expected frame to be decoded.
   This is for keeping output consistent across seeks. */
void ntom_set_ntom(mpg123_handle *fr, off_t num) {
	fr->ntom_val[1] = fr->ntom_val[0] = ntom_val(fr, num);
}

/* Convert frame offset to unadjusted output sample offset. */
off_t ntom_frmouts(mpg123_handle *fr, off_t frame) {
#ifdef SAFE_NTOM
	off_t f;
#endif
	off_t soff = 0;
	off_t ntm = ntom_val(fr,0);
#ifdef SAFE_NTOM
	if(frame <= 0) return 0;
	for(f=0; f<frame; ++f)
	{
		ntm  += spf(fr)*fr->ntom_step;
		soff += ntm/NTOM_MUL;
		ntm  -= (ntm/NTOM_MUL)*NTOM_MUL;
	}
#else
	soff = (ntm + frame*(off_t)spf(fr)*(off_t)fr->ntom_step)/(off_t)NTOM_MUL;
#endif
	return soff;
}

/* Convert input samples to unadjusted output samples. */
off_t ntom_ins2outs(mpg123_handle *fr, off_t ins) {
	off_t soff = 0;
	off_t ntm = ntom_val(fr,0);
#ifdef SAFE_NTOM
	{
		off_t block = spf(fr);
		if(ins <= 0) return 0;
		do
		{
			off_t nowblock = ins > block ? block : ins;
			ntm  += nowblock*fr->ntom_step;
			soff += ntm/NTOM_MUL;
			ntm  -= (ntm/NTOM_MUL)*NTOM_MUL;
			ins -= nowblock;
		} while(ins > 0);
	}
#else
	/* Beware of overflows: when off_t is 32bits, the multiplication blows too easily.
	   Of course, it blows for 64bits, too, in theory, but that's for _really_ large files. */
	soff = ((off_t)ntm + (off_t)ins*(off_t)fr->ntom_step)/(off_t)NTOM_MUL;
#endif
	return soff;
}

/* Determine frame offset from unadjusted output sample offset. */
off_t ntom_frameoff(mpg123_handle *fr, off_t soff) {
	off_t ioff = 0; /* frames or samples */
	off_t ntm = ntom_val(fr,0);
#ifdef SAFE_NTOM
	if(soff <= 0) return 0;
	for(ioff=0; 1; ++ioff)
	{
		ntm  += spf(fr)*fr->ntom_step;
		if(ntm/NTOM_MUL > soff) break;
		soff -= ntm/NTOM_MUL;
		ntm  -= (ntm/NTOM_MUL)*NTOM_MUL;
	}
	return ioff;
#else
	ioff = (soff*(off_t)NTOM_MUL-ntm)/(off_t)fr->ntom_step;
	return ioff/(off_t)spf(fr);
#endif
}

