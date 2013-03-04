/*
	sampleadjust: gapless sample offset math

	copyright 1995-2012 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org

	This is no stand-alone header, precisely to be able to fool it into using fake handle types for testing the math.
*/

#include "debug.h"

#ifdef GAPLESS
/* From internal sample number to external. */
static off_t sample_adjust(mpg123_handle *mh, off_t x)
{
	off_t s;
	if(mh->p.flags & MPG123_GAPLESS)
	{
		/* It's a bit tricky to do this computation for the padding samples.
		   They are not there on the outside. */
		if(x > mh->end_os)
		{
			if(x < mh->fullend_os)
			s = mh->end_os - mh->begin_os;
			else
			s = x - (mh->fullend_os - mh->end_os + mh->begin_os);
		}
		else
		s = x - mh->begin_os;
	}
	else
	s = x;

	return s;
}

/* from external samples to internal */
static off_t sample_unadjust(mpg123_handle *mh, off_t x)
{
	off_t s;
	if(mh->p.flags & MPG123_GAPLESS)
	{
		s = x + mh->begin_os;
		/* There is a hole; we don't create sample positions in there.
		   Jump from the end of the gapless track directly to after the padding. */
		if(s >= mh->end_os)
		s += mh->fullend_os - mh->end_os;
	}
	else s = x;

	return s;
}

/*
	Take the buffer after a frame decode (strictly: it is the data from frame fr->num!) and cut samples out.
	fr->buffer.fill may then be smaller than before...
*/
static void frame_buffercheck(mpg123_handle *fr)
{
	/* When we have no accurate position, gapless code does not make sense. */
	if(!(fr->state_flags & FRAME_ACCURATE)) return;

	/* Get a grip on dirty streams that start with a gapless header.
	   Simply accept all data from frames that are too much,
	   they are supposedly attached to the stream after the fact. */
	if(fr->gapless_frames > 0 && fr->num >= fr->gapless_frames) return;

	/* Important: We first cut samples from the end, then cut from beginning (including left-shift of the buffer).
	   This order works also for the case where firstframe == lastframe. */

	/* The last interesting (planned) frame: Only use some leading samples.
	   Note a difference from the below: The last frame and offset are unchanges by seeks.
	   The lastoff keeps being valid. */
	if(fr->lastframe > -1 && fr->num >= fr->lastframe)
	{
		/* There can be more than one frame of padding at the end, so we ignore the whole frame if we are beyond lastframe. */
		off_t byteoff = (fr->num == fr->lastframe) ? samples_to_bytes(fr, fr->lastoff) : 0;
		if((off_t)fr->buffer.fill > byteoff)
		{
			fr->buffer.fill = byteoff;
		}
		if(VERBOSE3) fprintf(stderr, "\nNote: Cut frame %"OFF_P" buffer on end of stream to %"OFF_P" samples, fill now %"SIZE_P" bytes.\n", (off_p)fr->num, (off_p)(fr->num == fr->lastframe ? fr->lastoff : 0), (size_p)fr->buffer.fill);
	}

	/* The first interesting frame: Skip some leading samples. */
	if(fr->firstoff && fr->num == fr->firstframe)
	{
		off_t byteoff = samples_to_bytes(fr, fr->firstoff);
		if((off_t)fr->buffer.fill > byteoff)
		{
			fr->buffer.fill -= byteoff;
			/* buffer.p != buffer.data only for own buffer */
			debug6("cutting %li samples/%li bytes on begin, own_buffer=%i at %p=%p, buf[1]=%i",
			        (long)fr->firstoff, (long)byteoff, fr->own_buffer, (void*)fr->buffer.p, (void*)fr->buffer.data, ((short*)fr->buffer.p)[2]);
			if(fr->own_buffer) fr->buffer.p = fr->buffer.data + byteoff;
			else memmove(fr->buffer.data, fr->buffer.data + byteoff, fr->buffer.fill);
			debug3("done cutting, buffer at %p =? %p, buf[1]=%i",
			        (void*)fr->buffer.p, (void*)fr->buffer.data, ((short*)fr->buffer.p)[2]);
		}
		else fr->buffer.fill = 0;

		if(VERBOSE3) fprintf(stderr, "\nNote: Cut frame %"OFF_P" buffer on beginning of stream by %"OFF_P" samples, fill now %"SIZE_P" bytes.\n", (off_p)fr->num, (off_p)fr->firstoff, (size_p)fr->buffer.fill);
		/* We can only reach this frame again by seeking. And on seeking, firstoff will be recomputed.
		   So it is safe to null it here (and it makes the if() decision abort earlier). */
		fr->firstoff = 0;
	}
}

#define SAMPLE_ADJUST(mh,x)     sample_adjust(mh,x)
#define SAMPLE_UNADJUST(mh,x)   sample_unadjust(mh,x)
#define FRAME_BUFFERCHECK(mh) frame_buffercheck(mh)

#else /* no gapless code included */

#define SAMPLE_ADJUST(mh,x)   (x)
#define SAMPLE_UNADJUST(mh,x) (x)
#define FRAME_BUFFERCHECK(mh)

#endif
