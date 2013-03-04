/*
	libmpg123: MPEG Audio Decoder library

	copyright 1995-2012 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org

*/

#include "mpg123lib_intern.h"
#include "icy2utf8.h"
#include "debug.h"

#include "gapless.h"

#define SEEKFRAME(mh) ((mh)->ignoreframe < 0 ? 0 : (mh)->ignoreframe)

static int initialized = 0;

int attribute_align_arg mpg123_init(void)
{
	if((sizeof(short) != 2) || (sizeof(long) < 4)) return MPG123_BAD_TYPES;

	if(initialized) return MPG123_OK; /* no need to initialize twice */

#ifndef NO_LAYER12
	init_layer12(); /* inits also shared tables with layer1 */
#endif
#ifndef NO_LAYER3
	init_layer3();
#endif
	prepare_decode_tables();
	check_decoders();
	initialized = 1;
	return MPG123_OK;
}

void attribute_align_arg mpg123_exit(void)
{
	/* nothing yet, but something later perhaps */
}

/* create a new handle with specified decoder, decoder can be "", "auto" or NULL for auto-detection */
mpg123_handle attribute_align_arg *mpg123_new(const char* decoder, int *error)
{
	return mpg123_parnew(NULL, decoder, error);
}

/* ...the full routine with optional initial parameters to override defaults. */
mpg123_handle attribute_align_arg *mpg123_parnew(mpg123_pars *mp, const char* decoder, int *error)
{
	mpg123_handle *fr = NULL;
	int err = MPG123_OK;

	if(initialized) fr = (mpg123_handle*) malloc(sizeof(mpg123_handle));
	else err = MPG123_NOT_INITIALIZED;
	if(fr != NULL)
	{
		frame_init_par(fr, mp);
		debug("cpu opt setting");
		if(frame_cpu_opt(fr, decoder) != 1)
		{
			err = MPG123_BAD_DECODER;
			frame_exit(fr);
			free(fr);
			fr = NULL;
		}
	}
	if(fr != NULL)
	{
		fr->decoder_change = 1;
	}
	else if(err == MPG123_OK) err = MPG123_OUT_OF_MEM;

	if(error != NULL) *error = err;
	return fr;
}

int attribute_align_arg mpg123_decoder(mpg123_handle *mh, const char* decoder)
{
	enum optdec dt = dectype(decoder);

	if(mh == NULL) return MPG123_ERR;

	if(dt == nodec)
	{
		mh->err = MPG123_BAD_DECODER;
		return MPG123_ERR;
	}
	if(dt == mh->cpu_opts.type) return MPG123_OK;

	/* Now really change. */
	/* frame_exit(mh);
	frame_init(mh); */
	debug("cpu opt setting");
	if(frame_cpu_opt(mh, decoder) != 1)
	{
		mh->err = MPG123_BAD_DECODER;
		frame_exit(mh);
		return MPG123_ERR;
	}
	/* New buffers for decoder are created in frame_buffers() */
	if((frame_outbuffer(mh) != 0))
	{
		mh->err = MPG123_NO_BUFFERS;
		frame_exit(mh);
		return MPG123_ERR;
	}
	/* Do _not_ call decode_update here! That is only allowed after a first MPEG frame has been met. */
	mh->decoder_change = 1;
	return MPG123_OK;
}

int attribute_align_arg mpg123_param(mpg123_handle *mh, enum mpg123_parms key, long val, double fval)
{
	int r;

	if(mh == NULL) return MPG123_ERR;
	r = mpg123_par(&mh->p, key, val, fval);
	if(r != MPG123_OK){ mh->err = r; r = MPG123_ERR; }
	else
	{ /* Special treatment for some settings. */
#ifdef FRAME_INDEX
		if(key == MPG123_INDEX_SIZE)
		{ /* Apply frame index size and grow property on the fly. */
			r = frame_index_setup(mh);
			if(r != MPG123_OK) mh->err = MPG123_INDEX_FAIL;
		}
#endif
#ifndef NO_FEEDER
		/* Feeder pool size is applied right away, reader will react to that. */
		if(key == MPG123_FEEDPOOL || key == MPG123_FEEDBUFFER)
		bc_poolsize(&mh->rdat.buffer, mh->p.feedpool, mh->p.feedbuffer);
#endif
	}
	return r;
}

int attribute_align_arg mpg123_par(mpg123_pars *mp, enum mpg123_parms key, long val, double fval)
{
	int ret = MPG123_OK;

	if(mp == NULL) return MPG123_BAD_PARS;
	switch(key)
	{
		case MPG123_VERBOSE:
			mp->verbose = val;
		break;
		case MPG123_FLAGS:
#ifndef GAPLESS
			if(val & MPG123_GAPLESS) ret = MPG123_NO_GAPLESS;
#endif
			if(ret == MPG123_OK) mp->flags = val;
			debug1("set flags to 0x%lx", (unsigned long) mp->flags);
		break;
		case MPG123_ADD_FLAGS:
#ifndef GAPLESS
			/* Enabling of gapless mode doesn't work when it's not there, but disabling (below) is no problem. */
			if(val & MPG123_GAPLESS) ret = MPG123_NO_GAPLESS;
			else
#endif
			mp->flags |= val;
			debug1("set flags to 0x%lx", (unsigned long) mp->flags);
		break;
		case MPG123_REMOVE_FLAGS:
			mp->flags &= ~val;
			debug1("set flags to 0x%lx", (unsigned long) mp->flags);
		break;
		case MPG123_FORCE_RATE: /* should this trigger something? */
#ifdef NO_NTOM
			if(val > 0)
			ret = MPG123_BAD_RATE;
#else
			if(val > 96000) ret = MPG123_BAD_RATE;
			else mp->force_rate = val < 0 ? 0 : val; /* >0 means enable, 0 disable */
#endif
		break;
		case MPG123_DOWN_SAMPLE:
#ifdef NO_DOWNSAMPLE
			if(val != 0) ret = MPG123_BAD_RATE;
#else
			if(val < 0 || val > 2) ret = MPG123_BAD_RATE;
			else mp->down_sample = (int)val;
#endif
		break;
		case MPG123_RVA:
			if(val < 0 || val > MPG123_RVA_MAX) ret = MPG123_BAD_RVA;
			else mp->rva = (int)val;
		break;
		case MPG123_DOWNSPEED:
			mp->halfspeed = val < 0 ? 0 : val;
		break;
		case MPG123_UPSPEED:
			mp->doublespeed = val < 0 ? 0 : val;
		break;
		case MPG123_ICY_INTERVAL:
#ifndef NO_ICY
			mp->icy_interval = val > 0 ? val : 0;
#else
			if(val > 0) ret = MPG123_BAD_PARAM;
#endif
		break;
		case MPG123_OUTSCALE:
			/* Choose the value that is non-zero, if any.
			   Downscaling integers to 1.0 . */
			mp->outscale = val == 0 ? fval : (double)val/SHORT_SCALE;
		break;
		case MPG123_TIMEOUT:
#ifdef TIMEOUT_READ
			mp->timeout = val >= 0 ? val : 0;
#else
			if(val > 0) ret = MPG123_NO_TIMEOUT;
#endif
		break;
		case MPG123_RESYNC_LIMIT:
			mp->resync_limit = val;
		break;
		case MPG123_INDEX_SIZE:
#ifdef FRAME_INDEX
			mp->index_size = val;
#else
			ret = MPG123_NO_INDEX;
#endif
		break;
		case MPG123_PREFRAMES:
			if(val >= 0) mp->preframes = val;
			else ret = MPG123_BAD_VALUE;
		break;
		case MPG123_FEEDPOOL:
#ifndef NO_FEEDER
			if(val >= 0) mp->feedpool = val;
			else ret = MPG123_BAD_VALUE;
#else
			ret = MPG123_MISSING_FEATURE;
#endif
		break;
		case MPG123_FEEDBUFFER:
#ifndef NO_FEEDER
			if(val > 0) mp->feedbuffer = val;
			else ret = MPG123_BAD_VALUE;
#else
			ret = MPG123_MISSING_FEATURE;
#endif
		break;
		default:
			ret = MPG123_BAD_PARAM;
	}
	return ret;
}

int attribute_align_arg mpg123_getparam(mpg123_handle *mh, enum mpg123_parms key, long *val, double *fval)
{
	int r;

	if(mh == NULL) return MPG123_ERR;
	r = mpg123_getpar(&mh->p, key, val, fval);
	if(r != MPG123_OK){ mh->err = r; r = MPG123_ERR; }
	return r;
}

int attribute_align_arg mpg123_getpar(mpg123_pars *mp, enum mpg123_parms key, long *val, double *fval)
{
	int ret = 0;

	if(mp == NULL) return MPG123_BAD_PARS;
	switch(key)
	{
		case MPG123_VERBOSE:
			if(val) *val = mp->verbose;
		break;
		case MPG123_FLAGS:
		case MPG123_ADD_FLAGS:
			if(val) *val = mp->flags;
		break;
		case MPG123_FORCE_RATE:
			if(val) 
#ifdef NO_NTOM
			*val = 0;
#else
			*val = mp->force_rate;
#endif
		break;
		case MPG123_DOWN_SAMPLE:
			if(val) *val = mp->down_sample;
		break;
		case MPG123_RVA:
			if(val) *val = mp->rva;
		break;
		case MPG123_DOWNSPEED:
			if(val) *val = mp->halfspeed;
		break;
		case MPG123_UPSPEED:
			if(val) *val = mp->doublespeed;
		break;
		case MPG123_ICY_INTERVAL:
#ifndef NO_ICY
			if(val) *val = (long)mp->icy_interval;
#else
			if(val) *val = 0;
#endif
		break;
		case MPG123_OUTSCALE:
			if(fval) *fval = mp->outscale;
			if(val) *val = (long)(mp->outscale*SHORT_SCALE);
		break;
		case MPG123_RESYNC_LIMIT:
			if(val) *val = mp->resync_limit;
		break;
		case MPG123_INDEX_SIZE:
			if(val)
#ifdef FRAME_INDEX
			*val = mp->index_size;
#else
			*val = 0; /* graceful fallback: no index is index of zero size */
#endif
		break;
		case MPG123_PREFRAMES:
			*val = mp->preframes;
		break;
		case MPG123_FEEDPOOL:
#ifndef NO_FEEDER
			*val = mp->feedpool;
#else
			ret = MPG123_MISSING_FEATURE;
#endif
		break;
		case MPG123_FEEDBUFFER:
#ifndef NO_FEEDER
			*val = mp->feedbuffer;
#else
			ret = MPG123_MISSING_FEATURE;
#endif
		break;
		default:
			ret = MPG123_BAD_PARAM;
	}
	return ret;
}

int attribute_align_arg mpg123_getstate(mpg123_handle *mh, enum mpg123_state key, long *val, double *fval)
{
	int ret = MPG123_OK;
	long theval = 0;
	double thefval = 0.;

	if(mh == NULL) return MPG123_ERR;

	switch(key)
	{
		case MPG123_ACCURATE:
			theval = mh->state_flags & FRAME_ACCURATE;
		break;
		case MPG123_FRANKENSTEIN:
			theval = mh->state_flags & FRAME_FRANKENSTEIN;
		break;
		case MPG123_BUFFERFILL:
#ifndef NO_FEEDER
		{
			size_t sval = bc_fill(&mh->rdat.buffer);
			theval = (long)sval;
			if((size_t)theval != sval)
			{
				mh->err = MPG123_INT_OVERFLOW;
				ret = MPG123_ERR;
			}
		}
#else
			mh->err = MPG123_MISSING_FEATURE;
			ret = MPG123_ERR;
#endif
		break;
		default:
			mh->err = MPG123_BAD_KEY;
			ret = MPG123_ERR;
	}

	if(val  != NULL) *val  = theval;
	if(fval != NULL) *fval = thefval;

	return ret;
}

int attribute_align_arg mpg123_eq(mpg123_handle *mh, enum mpg123_channels channel, int band, double val)
{
	if(mh == NULL) return MPG123_ERR;
	if(band < 0 || band > 31){ mh->err = MPG123_BAD_BAND; return MPG123_ERR; }
	switch(channel)
	{
		case MPG123_LEFT|MPG123_RIGHT:
			mh->equalizer[0][band] = mh->equalizer[1][band] = DOUBLE_TO_REAL(val);
		break;
		case MPG123_LEFT:  mh->equalizer[0][band] = DOUBLE_TO_REAL(val); break;
		case MPG123_RIGHT: mh->equalizer[1][band] = DOUBLE_TO_REAL(val); break;
		default:
			mh->err=MPG123_BAD_CHANNEL;
			return MPG123_ERR;
	}
	mh->have_eq_settings = TRUE;
	return MPG123_OK;
}

double attribute_align_arg mpg123_geteq(mpg123_handle *mh, enum mpg123_channels channel, int band)
{
	double ret = 0.;

	if(mh == NULL) return MPG123_ERR;

	/* Handle this gracefully. When there is no band, it has no volume. */
	if(band > -1 && band < 32)
	switch(channel)
	{
		case MPG123_LEFT|MPG123_RIGHT:
			ret = 0.5*(REAL_TO_DOUBLE(mh->equalizer[0][band])+REAL_TO_DOUBLE(mh->equalizer[1][band]));
		break;
		case MPG123_LEFT:  ret = REAL_TO_DOUBLE(mh->equalizer[0][band]); break;
		case MPG123_RIGHT: ret = REAL_TO_DOUBLE(mh->equalizer[1][band]); break;
		/* Default case is already handled: ret = 0 */
	}

	return ret;
}


/* plain file access, no http! */
int attribute_align_arg mpg123_open(mpg123_handle *mh, const char *path)
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	return open_stream(mh, path, -1);
}

int attribute_align_arg mpg123_open_fd(mpg123_handle *mh, int fd)
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	return open_stream(mh, NULL, fd);
}

int attribute_align_arg mpg123_open_handle(mpg123_handle *mh, void *iohandle)
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	if(mh->rdat.r_read_handle == NULL)
	{
		mh->err = MPG123_BAD_CUSTOM_IO;
		return MPG123_ERR;
	}
	return open_stream_handle(mh, iohandle);
}

int attribute_align_arg mpg123_open_feed(mpg123_handle *mh)
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	return open_feed(mh);
}

int attribute_align_arg mpg123_replace_reader( mpg123_handle *mh,
                           ssize_t (*r_read) (int, void *, size_t),
                           off_t   (*r_lseek)(int, off_t, int) )
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	mh->rdat.r_read = r_read;
	mh->rdat.r_lseek = r_lseek;
	return MPG123_OK;
}

int attribute_align_arg mpg123_replace_reader_handle( mpg123_handle *mh,
                           ssize_t (*r_read) (void*, void *, size_t),
                           off_t   (*r_lseek)(void*, off_t, int),
                           void    (*cleanup)(void*)  )
{
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	mh->rdat.r_read_handle = r_read;
	mh->rdat.r_lseek_handle = r_lseek;
	mh->rdat.cleanup_handle = cleanup;
	return MPG123_OK;
}

/* Update decoding engine for
   a) a new choice of decoder
   b) a changed native format of the MPEG stream
   ... calls are only valid after parsing some MPEG frame! */
int decode_update(mpg123_handle *mh)
{
	long native_rate;
	int b;

	if(mh->num < 0)
	{
		if(!(mh->p.flags & MPG123_QUIET)) error("decode_update() has been called before reading the first MPEG frame! Internal programming error.");

		mh->err = MPG123_BAD_DECODER_SETUP;
		return MPG123_ERR;
	}

	native_rate = frame_freq(mh);

	b = frame_output_format(mh); /* Select the new output format based on given constraints. */
	if(b < 0) return MPG123_ERR;

	if(b == 1) mh->new_format = 1; /* Store for later... */

	debug3("updating decoder structure with native rate %li and af.rate %li (new format: %i)", native_rate, mh->af.rate, mh->new_format);
	if(mh->af.rate == native_rate) mh->down_sample = 0;
	else if(mh->af.rate == native_rate>>1) mh->down_sample = 1;
	else if(mh->af.rate == native_rate>>2) mh->down_sample = 2;
	else mh->down_sample = 3; /* flexible (fixed) rate */
	switch(mh->down_sample)
	{
		case 0:
		case 1:
		case 2:
			mh->down_sample_sblimit = SBLIMIT>>(mh->down_sample);
			/* With downsampling I get less samples per frame */
			mh->outblock = samples_to_storage(mh, (spf(mh)>>mh->down_sample));
		break;
#ifndef NO_NTOM
		case 3:
		{
			if(synth_ntom_set_step(mh) != 0) return -1;
			if(frame_freq(mh) > mh->af.rate)
			{
				mh->down_sample_sblimit = SBLIMIT * mh->af.rate;
				mh->down_sample_sblimit /= frame_freq(mh);
			}
			else mh->down_sample_sblimit = SBLIMIT;
			mh->outblock = samples_to_storage(mh,
			                 ( ( NTOM_MUL-1+spf(mh)
			                   * (((size_t)NTOM_MUL*mh->af.rate)/frame_freq(mh))
			                 )/NTOM_MUL ));
		}
		break;
#endif
	}

	if(!(mh->p.flags & MPG123_FORCE_MONO))
	{
		if(mh->af.channels == 1) mh->single = SINGLE_MIX;
		else mh->single = SINGLE_STEREO;
	}
	else mh->single = (mh->p.flags & MPG123_FORCE_MONO)-1;
	if(set_synth_functions(mh) != 0) return -1;;

	/* The needed size of output buffer may have changed. */
	if(frame_outbuffer(mh) != MPG123_OK) return -1;

	do_rva(mh);
	debug3("done updating decoder structure with native rate %li and af.rate %li and down_sample %i", frame_freq(mh), mh->af.rate, mh->down_sample);

	return 0;
}

size_t attribute_align_arg mpg123_safe_buffer(void)
{
	/* real is the largest possible output (it's 32bit float, 32bit int or 64bit double). */
	return sizeof(real)*2*1152*NTOM_MAX;
}

size_t attribute_align_arg mpg123_outblock(mpg123_handle *mh)
{
	/* Try to be helpful and never return zero output block size. */
	if(mh != NULL && mh->outblock > 0) return mh->outblock;
	else return mpg123_safe_buffer();
}

/* Read in the next frame we actually want for decoding.
   This includes skipping/ignoring frames, in additon to skipping junk in the parser. */
static int get_next_frame(mpg123_handle *mh)
{
	/* We have some decoder ready, if the desired decoder has changed,
	   it is OK to use the old one for ignoring frames and activating
	   the new one for real (decode_update()) after getting the frame. */
	int change = mh->decoder_change;
	do
	{
		int b;
		/* Decode & discard some frame(s) before beginning. */
		if(mh->to_ignore && mh->num < mh->firstframe && mh->num >= mh->ignoreframe)
		{
			debug1("ignoring frame %li", (long)mh->num);
			/* Decoder structure must be current! decode_update has been called before... */
			(mh->do_layer)(mh); mh->buffer.fill = 0;
#ifndef NO_NTOM
			/* The ignored decoding may have failed. Make sure ntom stays consistent. */
			if(mh->down_sample == 3) ntom_set_ntom(mh, mh->num+1);
#endif
			mh->to_ignore = mh->to_decode = FALSE;
		}
		/* Read new frame data; possibly breaking out here for MPG123_NEED_MORE. */
		debug("read frame");
		mh->to_decode = FALSE;
		b = read_frame(mh); /* That sets to_decode only if a full frame was read. */
		debug4("read of frame %li returned %i (to_decode=%i) at sample %li", (long)mh->num, b, mh->to_decode, (long)mpg123_tell(mh));
		if(b == MPG123_NEED_MORE) return MPG123_NEED_MORE; /* need another call with data */
		else if(b <= 0)
		{
			/* More sophisticated error control? */
			if(b==0 || (mh->rdat.filelen >= 0 && mh->rdat.filepos == mh->rdat.filelen))
			{ /* We simply reached the end. */
				mh->track_frames = mh->num + 1;
				debug("What about updating/checking gapless sample count here?");
				return MPG123_DONE;
			}
			else return MPG123_ERR; /* Some real error. */
		}
		/* Now, there should be new data to decode ... and also possibly new stream properties */
		if(mh->header_change > 1)
		{
			debug("big header change");
			change = 1;
		}
		/* Now some accounting: Look at the numbers and decide if we want this frame. */
		++mh->playnum;
		/* Plain skipping without decoding, only when frame is not ignored on next cycle. */
		if(mh->num < mh->firstframe || (mh->p.doublespeed && (mh->playnum % mh->p.doublespeed)))
		{
			if(!(mh->to_ignore && mh->num < mh->firstframe && mh->num >= mh->ignoreframe))
			{
				frame_skip(mh);
				/* Should one fix NtoM here or not?
				   It is not work the trouble for doublespeed, but what with leading frames? */
			}
		}
		/* Or, we are finally done and have a new frame. */
		else break;
	} while(1);

	/* If we reach this point, we got a new frame ready to be decoded.
	   All other situations resulted in returns from the loop. */
	if(change)
	{
		if(decode_update(mh) < 0)  /* dito... */
		return MPG123_ERR;

debug1("new format: %i", mh->new_format);

		mh->decoder_change = 0;
		if(mh->fresh)
		{
#ifdef GAPLESS
			int b=0;
			/* Prepare offsets for gapless decoding. */
			debug1("preparing gapless stuff with native rate %li", frame_freq(mh));
			frame_gapless_realinit(mh);
			frame_set_frameseek(mh, mh->num);
#endif
			mh->fresh = 0;
#ifdef GAPLESS
			/* Could this possibly happen? With a real big gapless offset... */
			if(mh->num < mh->firstframe) b = get_next_frame(mh);
			if(b < 0) return b; /* Could be error, need for more, new format... */
#endif
		}
	}
	return MPG123_OK;
}

/* Assumption: A buffer full of zero samples can be constructed by repetition of this byte.
   Oh, and it handles some format conversion.
   Only to be used by decode_the_frame() ... */
static int zero_byte(mpg123_handle *fr)
{
#ifndef NO_8BIT
	return fr->af.encoding & MPG123_ENC_8 ? fr->conv16to8[0] : 0;
#else
	return 0; /* All normal signed formats have the zero here (even in byte form -- that may be an assumption for your funny machine...). */
#endif
}

/*
	Not part of the api. This just decodes the frame and fills missing bits with zeroes.
	There can be frames that are broken and thus make do_layer() fail.
*/
static void decode_the_frame(mpg123_handle *fr)
{
	size_t needed_bytes = samples_to_storage(fr, frame_expect_outsamples(fr));
	fr->clip += (fr->do_layer)(fr);
	/*fprintf(stderr, "frame %"OFF_P": got %"SIZE_P" / %"SIZE_P"\n", fr->num,(size_p)fr->buffer.fill, (size_p)needed_bytes);*/
	/* There could be less data than promised.
	   Also, then debugging, we look out for coding errors that could result in _more_ data than expected. */
#ifdef DEBUG
	if(fr->buffer.fill != needed_bytes)
	{
#endif
		if(fr->buffer.fill < needed_bytes)
		{
			if(VERBOSE2)
			fprintf(stderr, "Note: broken frame %li, filling up with %"SIZE_P" zeroes, from %"SIZE_P"\n", (long)fr->num, (size_p)(needed_bytes-fr->buffer.fill), (size_p)fr->buffer.fill);

			/*
				One could do a loop with individual samples instead... but zero is zero
				Actually, that is wrong: zero is mostly a series of null bytes,
				but we have funny 8bit formats that have a different opinion on zero...
				Unsigned 16 or 32 bit formats are handled later.
			*/
			memset( fr->buffer.data + fr->buffer.fill, zero_byte(fr), needed_bytes - fr->buffer.fill );

			fr->buffer.fill = needed_bytes;
#ifndef NO_NTOM
			/* ntom_val will be wrong when the decoding wasn't carried out completely */
			ntom_set_ntom(fr, fr->num+1);
#endif
		}
#ifdef DEBUG
		else
		{
			if(NOQUIET)
			error2("I got _more_ bytes than expected (%"SIZE_P" / %"SIZE_P"), that should not be possible!", (size_p)fr->buffer.fill, (size_p)needed_bytes);
		}
	}
#endif
	postprocess_buffer(fr);
}

/*
	Decode the current frame into the frame structure's buffer, accessible at the location stored in <audio>, with <bytes> bytes available.
	<num> will contain the last decoded frame number. This function should be called after mpg123_framebyframe_next positioned the stream at a
	valid mp3 frame. The buffer contents will get lost on the next call to mpg123_framebyframe_next or mpg123_framebyframe_decode.
	returns
	MPG123_OK -- successfully decoded or ignored the frame, you get your output data or in case of ignored frames 0 bytes
	MPG123_DONE -- decoding finished, should not happen
	MPG123_ERR -- some error occured.
	MPG123_ERR_NULL -- audio or bytes are not pointing to valid storage addresses
	MPG123_BAD_HANDLE -- mh has not been initialized
	MPG123_NO_SPACE -- not enough space in buffer for safe decoding, should not happen
*/
int attribute_align_arg mpg123_framebyframe_decode(mpg123_handle *mh, off_t *num, unsigned char **audio, size_t *bytes)
{
	if(bytes == NULL) return MPG123_ERR_NULL;
	if(audio == NULL) return MPG123_ERR_NULL;
	if(mh == NULL) return MPG123_BAD_HANDLE;
	if(mh->buffer.size < mh->outblock) return MPG123_NO_SPACE;

	*bytes = 0;
	mh->buffer.fill = 0; /* always start fresh */
	if(!mh->to_decode) return MPG123_OK;

	if(num != NULL) *num = mh->num;
	debug("decoding");
	decode_the_frame(mh);
	mh->to_decode = mh->to_ignore = FALSE;
	mh->buffer.p = mh->buffer.data;
	FRAME_BUFFERCHECK(mh);
	*audio = mh->buffer.p;
	*bytes = mh->buffer.fill;
	return MPG123_OK;
}

/*
	Find, read and parse the next mp3 frame while skipping junk and parsing id3 tags, lame headers, etc.
	Prepares everything for decoding using mpg123_framebyframe_decode.
	returns
	MPG123_OK -- new frame was read and parsed, call mpg123_framebyframe_decode to actually decode
	MPG123_NEW_FORMAT -- new frame was read, it results in changed output format, call mpg123_framebyframe_decode to actually decode
	MPG123_BAD_HANDLE -- mh has not been initialized
	MPG123_NEED_MORE  -- more input data is needed to advance to the next frame. supply more input data using mpg123_feed
*/
int attribute_align_arg mpg123_framebyframe_next(mpg123_handle *mh)
{
	int b;
	if(mh == NULL) return MPG123_BAD_HANDLE;

	mh->to_decode = mh->to_ignore = FALSE;
	mh->buffer.fill = 0;

	b = get_next_frame(mh);
	if(b < 0) return b;
	debug1("got next frame, %i", mh->to_decode);

	/* mpg123_framebyframe_decode will return MPG123_OK with 0 bytes decoded if mh->to_decode is 0 */
	if(!mh->to_decode)
		return MPG123_OK;

	if(mh->new_format)
	{
		debug("notifiying new format");
		mh->new_format = 0;
		return MPG123_NEW_FORMAT;
	}

	return MPG123_OK;
}

/*
	Put _one_ decoded frame into the frame structure's buffer, accessible at the location stored in <audio>, with <bytes> bytes available.
	The buffer contents will be lost on next call to mpg123_decode_frame.
	MPG123_OK -- successfully decoded the frame, you get your output data
	MPg123_DONE -- This is it. End.
	MPG123_ERR -- some error occured...
	MPG123_NEW_FORMAT -- new frame was read, it results in changed output format -> will be decoded on next call
	MPG123_NEED_MORE  -- that should not happen as this function is intended for in-library stream reader but if you force it...
	MPG123_NO_SPACE   -- not enough space in buffer for safe decoding, also should not happen

	num will be updated to the last decoded frame number (may possibly _not_ increase, p.ex. when format changed).
*/
int attribute_align_arg mpg123_decode_frame(mpg123_handle *mh, off_t *num, unsigned char **audio, size_t *bytes)
{
	if(bytes != NULL) *bytes = 0;
	if(mh == NULL) return MPG123_ERR;
	if(mh->buffer.size < mh->outblock) return MPG123_NO_SPACE;
	mh->buffer.fill = 0; /* always start fresh */
	while(TRUE)
	{
		/* decode if possible */
		if(mh->to_decode)
		{
			if(mh->new_format)
			{
				debug("notifiying new format");
				mh->new_format = 0;
				return MPG123_NEW_FORMAT;
			}
			if(num != NULL) *num = mh->num;
			debug("decoding");

			decode_the_frame(mh);

			mh->to_decode = mh->to_ignore = FALSE;
			mh->buffer.p = mh->buffer.data;
			FRAME_BUFFERCHECK(mh);
			if(audio != NULL) *audio = mh->buffer.p;
			if(bytes != NULL) *bytes = mh->buffer.fill;

			return MPG123_OK;
		}
		else
		{
			int b = get_next_frame(mh);
			if(b < 0) return b;
			debug1("got next frame, %i", mh->to_decode);
		}
	}
}

int attribute_align_arg mpg123_read(mpg123_handle *mh, unsigned char *out, size_t size, size_t *done)
{
	return mpg123_decode(mh, NULL, 0, out, size, done);
}

int attribute_align_arg mpg123_feed(mpg123_handle *mh, const unsigned char *in, size_t size)
{
	if(mh == NULL) return MPG123_ERR;
#ifndef NO_FEEDER
	if(size > 0)
	{
		if(in != NULL)
		{
			if(feed_more(mh, in, size) != 0) return MPG123_ERR;
			else
			{
				/* The need for more data might have triggered an error.
				   This one is outdated now with the new data. */
				if(mh->err == MPG123_ERR_READER) mh->err = MPG123_OK;

				return MPG123_OK;
			}
		}
		else
		{
			mh->err = MPG123_NULL_BUFFER;
			return MPG123_ERR;
		}
	}
	return MPG123_OK;
#else
	mh->err = MPG123_MISSING_FEATURE;
	return MPG123_ERR;
#endif
}

/*
	The old picture:
	while(1) {
		len = read(0,buf,16384);
		if(len <= 0)
			break;
		ret = decodeMP3(&mp,buf,len,out,8192,&size);
		while(ret == MP3_OK) {
			write(1,out,size);
			ret = decodeMP3(&mp,NULL,0,out,8192,&size);
		}
	}
*/

int attribute_align_arg mpg123_decode(mpg123_handle *mh, const unsigned char *inmemory, size_t inmemsize, unsigned char *outmemory, size_t outmemsize, size_t *done)
{
	int ret = MPG123_OK;
	size_t mdone = 0;

	if(done != NULL) *done = 0;
	if(mh == NULL) return MPG123_ERR;
#ifndef NO_FEEDER
	if(inmemsize > 0 && mpg123_feed(mh, inmemory, inmemsize) != MPG123_OK)
	{
		ret = MPG123_ERR;
		goto decodeend;
	}
	if(outmemory == NULL) outmemsize = 0; /* Not just give error, give chance to get a status message. */

	while(ret == MPG123_OK)
	{
		debug4("decode loop, fill %i (%li vs. %li); to_decode: %i", (int)mh->buffer.fill, (long)mh->num, (long)mh->firstframe, mh->to_decode);
		/* Decode a frame that has been read before.
		   This only happens when buffer is empty! */
		if(mh->to_decode)
		{
			if(mh->new_format)
			{
				debug("notifiying new format");
				mh->new_format = 0;
				ret = MPG123_NEW_FORMAT;
				goto decodeend;
			}
			if(mh->buffer.size - mh->buffer.fill < mh->outblock)
			{
				ret = MPG123_NO_SPACE;
				goto decodeend;
			}
			decode_the_frame(mh);
			mh->to_decode = mh->to_ignore = FALSE;
			mh->buffer.p = mh->buffer.data;
			debug2("decoded frame %li, got %li samples in buffer", (long)mh->num, (long)(mh->buffer.fill / (samples_to_bytes(mh, 1))));
			FRAME_BUFFERCHECK(mh);
		}
		if(mh->buffer.fill) /* Copy (part of) the decoded data to the caller's buffer. */
		{
			/* get what is needed - or just what is there */
			int a = mh->buffer.fill > (outmemsize - mdone) ? outmemsize - mdone : mh->buffer.fill;
			debug4("buffer fill: %i; copying %i (%i - %li)", (int)mh->buffer.fill, a, (int)outmemsize, (long)mdone);
			memcpy(outmemory, mh->buffer.p, a);
			/* less data in frame buffer, less needed, output pointer increase, more data given... */
			mh->buffer.fill -= a;
			outmemory  += a;
			mdone += a;
			mh->buffer.p += a;
			if(!(outmemsize > mdone)) goto decodeend;
		}
		else /* If we didn't have data, get a new frame. */
		{
			int b = get_next_frame(mh);
			if(b < 0){ ret = b; goto decodeend; }
		}
	}
decodeend:
	if(done != NULL) *done = mdone;
	return ret;
#else
	mh->err = MPG123_MISSING_FEATURE;
	return MPG123_ERR;
#endif
}

long attribute_align_arg mpg123_clip(mpg123_handle *mh)
{
	long ret = 0;

	if(mh != NULL)
	{
		ret = mh->clip;
		mh->clip = 0;
	}
	return ret;
}

/* Simples: Track needs initializtion if no initial frame has been read yet. */
#define track_need_init(mh) ((mh)->num < 0)

static int init_track(mpg123_handle *mh)
{
	if(track_need_init(mh))
	{
		/* Fresh track, need first frame for basic info. */
		int b = get_next_frame(mh);
		if(b < 0) return b;
	}
	return 0;
}

int attribute_align_arg mpg123_getformat(mpg123_handle *mh, long *rate, int *channels, int *encoding)
{
	int b;

	if(mh == NULL) return MPG123_ERR;
	b = init_track(mh);
	if(b < 0) return b;

	if(rate != NULL) *rate = mh->af.rate;
	if(channels != NULL) *channels = mh->af.channels;
	if(encoding != NULL) *encoding = mh->af.encoding;
	mh->new_format = 0;
	return MPG123_OK;
}

off_t attribute_align_arg mpg123_timeframe(mpg123_handle *mh, double seconds)
{
	off_t b;

	if(mh == NULL) return MPG123_ERR;
	b = init_track(mh);
	if(b<0) return b;
	return (off_t)(seconds/mpg123_tpf(mh));
}

/*
	Now, where are we? We need to know the last decoded frame... and what's left of it in buffer.
	The current frame number can mean the last decoded frame or the to-be-decoded frame.
	If mh->to_decode, then mh->num frames have been decoded, the frame mh->num now coming next.
	If not, we have the possibility of mh->num+1 frames being decoded or nothing at all.
	Then, there is firstframe...when we didn't reach it yet, then the next data will come from there.
	mh->num starts with -1
*/
off_t attribute_align_arg mpg123_tell(mpg123_handle *mh)
{
	if(mh == NULL) return MPG123_ERR;
	if(track_need_init(mh)) return 0;
	/* Now we have all the info at hand. */
	debug5("tell: %li/%i first %li buffer %lu; frame_outs=%li", (long)mh->num, mh->to_decode, (long)mh->firstframe, (unsigned long)mh->buffer.fill, (long)frame_outs(mh, mh->num));

	{ /* Funny block to keep C89 happy. */
		off_t pos = 0;
		if((mh->num < mh->firstframe) || (mh->num == mh->firstframe && mh->to_decode))
		{ /* We are at the beginning, expect output from firstframe on. */
			pos = frame_outs(mh, mh->firstframe);
#ifdef GAPLESS
			pos += mh->firstoff;
#endif
		}
		else if(mh->to_decode)
		{ /* We start fresh with this frame. Buffer should be empty, but we make sure to count it in.  */
			pos = frame_outs(mh, mh->num) - bytes_to_samples(mh, mh->buffer.fill);
		}
		else
		{ /* We serve what we have in buffer and then the beginning of next frame... */
			pos = frame_outs(mh, mh->num+1) - bytes_to_samples(mh, mh->buffer.fill);
		}
		/* Substract padding and delay from the beginning. */
		pos = SAMPLE_ADJUST(mh,pos);
		/* Negative sample offsets are not right, less than nothing is still nothing. */
		return pos>0 ? pos : 0;
	}
}

off_t attribute_align_arg mpg123_tellframe(mpg123_handle *mh)
{
	if(mh == NULL) return MPG123_ERR;
	if(mh->num < mh->firstframe) return mh->firstframe;
	if(mh->to_decode) return mh->num;
	/* Consider firstoff? */
	return mh->buffer.fill ? mh->num : mh->num + 1;
}

off_t attribute_align_arg mpg123_tell_stream(mpg123_handle *mh)
{
	if(mh == NULL) return MPG123_ERR;
	/* mh->rd is at least a bad_reader, so no worry. */
	return mh->rd->tell(mh);
}

static int do_the_seek(mpg123_handle *mh)
{
	int b;
	off_t fnum = SEEKFRAME(mh);
	mh->buffer.fill = 0;

	/* If we are inside the ignoreframe - firstframe window, we may get away without actual seeking. */
	if(mh->num < mh->firstframe)
	{
		mh->to_decode = FALSE; /* In any case, don't decode the current frame, perhaps ignore instead. */
		if(mh->num > fnum) return MPG123_OK;
	}

	/* If we are already there, we are fine either for decoding or for ignoring. */
	if(mh->num == fnum && (mh->to_decode || fnum < mh->firstframe)) return MPG123_OK;
	/* We have the frame before... just go ahead as normal. */
	if(mh->num == fnum-1)
	{
		mh->to_decode = FALSE;
		return MPG123_OK;
	}

	/* OK, real seeking follows... clear buffers and go for it. */
	frame_buffers_reset(mh);
#ifndef NO_NTOM
	if(mh->down_sample == 3)
	{
		ntom_set_ntom(mh, fnum);
		debug3("fixed ntom for frame %"OFF_P" to %lu, num=%"OFF_P, (off_p)fnum, mh->ntom_val[0], (off_p)mh->num);
	}
#endif
	b = mh->rd->seek_frame(mh, fnum);
	debug1("seek_frame returned: %i", b);
	if(b<0) return b;
	/* Only mh->to_ignore is TRUE. */
	if(mh->num < mh->firstframe) mh->to_decode = FALSE;

	mh->playnum = mh->num;
	return 0;
}

off_t attribute_align_arg mpg123_seek(mpg123_handle *mh, off_t sampleoff, int whence)
{
	int b;
	off_t pos;

	pos = mpg123_tell(mh); /* adjusted samples */
	/* pos < 0 also can mean that simply a former seek failed at the lower levels.
	  In that case, we only allow absolute seeks. */
	if(pos < 0 && whence != SEEK_SET)
	{ /* Unless we got the obvious error of NULL handle, this is a special seek failure. */
		if(mh != NULL) mh->err = MPG123_NO_RELSEEK;
		return MPG123_ERR;
	}
	if((b=init_track(mh)) < 0) return b;
	switch(whence)
	{
		case SEEK_CUR: pos += sampleoff; break;
		case SEEK_SET: pos  = sampleoff; break;
		case SEEK_END:
			/* When we do not know the end already, we can try to find it. */
			if(mh->track_frames < 1 && (mh->rdat.flags & READER_SEEKABLE))
			mpg123_scan(mh);
			if(mh->track_frames > 0) pos = SAMPLE_ADJUST(mh,frame_outs(mh, mh->track_frames)) - sampleoff;
#ifdef GAPLESS
			else if(mh->end_os > 0) pos = SAMPLE_ADJUST(mh,mh->end_os) - sampleoff;
#endif
			else
			{
				mh->err = MPG123_NO_SEEK_FROM_END;
				return MPG123_ERR;
			}
		break;
		default: mh->err = MPG123_BAD_WHENCE; return MPG123_ERR;
	}
	if(pos < 0) pos = 0;
	/* pos now holds the wanted sample offset in adjusted samples */
	frame_set_seek(mh, SAMPLE_UNADJUST(mh,pos));
	pos = do_the_seek(mh);
	if(pos < 0) return pos;

	return mpg123_tell(mh);
}

/*
	A bit more tricky... libmpg123 does not do the seeking itself.
	All it can do is to ignore frames until the wanted one is there.
	The caller doesn't know where a specific frame starts and mpg123 also only knows the general region after it scanned the file.
	Well, it is tricky...
*/
off_t attribute_align_arg mpg123_feedseek(mpg123_handle *mh, off_t sampleoff, int whence, off_t *input_offset)
{
	int b;
	off_t pos;

	pos = mpg123_tell(mh); /* adjusted samples */
	debug3("seek from %li to %li (whence=%i)", (long)pos, (long)sampleoff, whence);
	/* The special seek error handling does not apply here... there is no lowlevel I/O. */
	if(pos < 0) return pos; /* mh == NULL is covered in mpg123_tell() */
#ifndef NO_FEEDER
	if(input_offset == NULL)
	{
		mh->err = MPG123_NULL_POINTER;
		return MPG123_ERR;
	}

	if((b=init_track(mh)) < 0) return b; /* May need more to do anything at all. */

	switch(whence)
	{
		case SEEK_CUR: pos += sampleoff; break;
		case SEEK_SET: pos  = sampleoff; break;
		case SEEK_END:
			if(mh->track_frames > 0) pos = SAMPLE_ADJUST(mh,frame_outs(mh, mh->track_frames)) - sampleoff;
#ifdef GAPLESS
			else if(mh->end_os >= 0) pos = SAMPLE_ADJUST(mh,mh->end_os) - sampleoff;
#endif
			else
			{
				mh->err = MPG123_NO_SEEK_FROM_END;
				return MPG123_ERR;
			}
		break;
		default: mh->err = MPG123_BAD_WHENCE; return MPG123_ERR;
	}
	if(pos < 0) pos = 0;
	frame_set_seek(mh, SAMPLE_UNADJUST(mh,pos));
	pos = SEEKFRAME(mh);
	mh->buffer.fill = 0;

	/* Shortcuts without modifying input stream. */
	*input_offset = mh->rdat.buffer.fileoff + mh->rdat.buffer.size;
	if(mh->num < mh->firstframe) mh->to_decode = FALSE;
	if(mh->num == pos && mh->to_decode) goto feedseekend;
	if(mh->num == pos-1) goto feedseekend;
	/* Whole way. */
	*input_offset = feed_set_pos(mh, frame_index_find(mh, SEEKFRAME(mh), &pos));
	mh->num = pos-1; /* The next read frame will have num = pos. */
	if(*input_offset < 0) return MPG123_ERR;

feedseekend:
	return mpg123_tell(mh);
#else
	mh->err = MPG123_MISSING_FEATURE;
	return MPG123_ERR;
#endif
}

off_t attribute_align_arg mpg123_seek_frame(mpg123_handle *mh, off_t offset, int whence)
{
	int b;
	off_t pos = 0;

	if(mh == NULL) return MPG123_ERR;
	if((b=init_track(mh)) < 0) return b;

	/* Could play games here with to_decode... */
	pos = mh->num;
	switch(whence)
	{
		case SEEK_CUR: pos += offset; break;
		case SEEK_SET: pos  = offset; break;
		case SEEK_END:
			if(mh->track_frames > 0) pos = mh->track_frames - offset;
			else
			{
				mh->err = MPG123_NO_SEEK_FROM_END;
				return MPG123_ERR;
			}
		break;
		default:
			mh->err = MPG123_BAD_WHENCE;
			return MPG123_ERR;
	}
	if(pos < 0) pos = 0;
	/* Not limiting the possible position on end for the chance that there might be more to the stream than announced via track_frames. */

	frame_set_frameseek(mh, pos);
	pos = do_the_seek(mh);
	if(pos < 0) return pos;

	return mpg123_tellframe(mh);
}

int attribute_align_arg mpg123_set_filesize(mpg123_handle *mh, off_t size)
{
	if(mh == NULL) return MPG123_ERR;

	mh->rdat.filelen = size;
	return MPG123_OK;
}

off_t attribute_align_arg mpg123_length(mpg123_handle *mh)
{
	int b;
	off_t length;

	if(mh == NULL) return MPG123_ERR;
	b = init_track(mh);
	if(b<0) return b;
	if(mh->track_samples > -1) length = mh->track_samples;
	else if(mh->track_frames > 0) length = mh->track_frames*spf(mh);
	else if(mh->rdat.filelen > 0) /* Let the case of 0 length just fall through. */
	{
		/* A bad estimate. Ignoring tags 'n stuff. */
		double bpf = mh->mean_framesize ? mh->mean_framesize : compute_bpf(mh);
		length = (off_t)((double)(mh->rdat.filelen)/bpf*spf(mh));
	}
	else if(mh->rdat.filelen == 0) return mpg123_tell(mh); /* we could be in feeder mode */
	else return MPG123_ERR; /* No length info there! */

	debug1("mpg123_length: internal sample length: %"OFF_P, (off_p)length);

	length = frame_ins2outs(mh, length);
	debug1("mpg123_length: external sample length: %"OFF_P, (off_p)length);
	length = SAMPLE_ADJUST(mh,length);
	return length;
}

int attribute_align_arg mpg123_scan(mpg123_handle *mh)
{
	int b;
	off_t oldpos;
	off_t track_frames = 0;
	off_t track_samples = 0;

	if(mh == NULL) return MPG123_ERR;
	if(!(mh->rdat.flags & READER_SEEKABLE)){ mh->err = MPG123_NO_SEEK; return MPG123_ERR; }
	/* Scan through the _whole_ file, since the current position is no count but computed assuming constant samples per frame. */
	/* Also, we can just keep the current buffer and seek settings. Just operate on input frames here. */
	debug("issuing scan");
	b = init_track(mh); /* mh->num >= 0 !! */
	if(b<0)
	{
		if(b == MPG123_DONE) return MPG123_OK;
		else return MPG123_ERR; /* Must be error here, NEED_MORE is not for seekable streams. */
	}
	oldpos = mpg123_tell(mh);
	b = mh->rd->seek_frame(mh, 0);
	if(b<0 || mh->num != 0) return MPG123_ERR;
	/* One frame must be there now. */
	track_frames = 1;
	track_samples = spf(mh); /* Internal samples. */
	debug("TODO: We should disable gapless code when encountering inconsistent spf(mh)!");
	/* Do not increment mh->track_frames in the loop as tha would confuse Frankenstein detection. */
	while(read_frame(mh) == 1)
	{
		++track_frames;
		track_samples += spf(mh);
	}
	mh->track_frames = track_frames;
	mh->track_samples = track_samples;
	mpg123_seek_frame(mh, SEEK_SET, mh->track_frames);
	debug2("Scanning yielded %"OFF_P" track samples, %"OFF_P" frames.", (off_p)mh->track_samples, (off_p)mh->track_frames);
#ifdef GAPLESS
	/* Also, think about usefulness of that extra value track_samples ... it could be used for consistency checking. */
	frame_gapless_update(mh, mh->track_samples);
#endif
	return mpg123_seek(mh, oldpos, SEEK_SET) >= 0 ? MPG123_OK : MPG123_ERR;
}

int attribute_align_arg mpg123_meta_check(mpg123_handle *mh)
{
	if(mh != NULL) return mh->metaflags;
	else return 0;
}

void attribute_align_arg mpg123_meta_free(mpg123_handle *mh)
{
	if(mh == NULL) return;

	reset_id3(mh);
	reset_icy(&mh->icy);
}

int attribute_align_arg mpg123_id3(mpg123_handle *mh, mpg123_id3v1 **v1, mpg123_id3v2 **v2)
{
	if(v1 != NULL) *v1 = NULL;
	if(v2 != NULL) *v2 = NULL;
	if(mh == NULL) return MPG123_ERR;

	if(mh->metaflags & MPG123_ID3)
	{
		id3_link(mh);
		if(v1 != NULL && mh->rdat.flags & READER_ID3TAG) *v1 = (mpg123_id3v1*) mh->id3buf;
		if(v2 != NULL)
#ifdef NO_ID3V2
		*v2 = NULL;
#else
		*v2 = &mh->id3v2;
#endif

		mh->metaflags |= MPG123_ID3;
		mh->metaflags &= ~MPG123_NEW_ID3;
	}
	return MPG123_OK;
}

int attribute_align_arg mpg123_icy(mpg123_handle *mh, char **icy_meta)
{
	if(mh == NULL) return MPG123_ERR;
#ifndef NO_ICY
	if(icy_meta == NULL)
	{
		mh->err = MPG123_NULL_POINTER;
		return MPG123_ERR;
	}
	*icy_meta = NULL;

	if(mh->metaflags & MPG123_ICY)
	{
		*icy_meta = mh->icy.data;
		mh->metaflags |= MPG123_ICY;
		mh->metaflags &= ~MPG123_NEW_ICY;
	}
	return MPG123_OK;
#else
	mh->err = MPG123_MISSING_FEATURE;
	return MPG123_ERR;
#endif
}

char* attribute_align_arg mpg123_icy2utf8(const char* icy_text)
{
#ifndef NO_ICY
	return icy2utf8(icy_text, 0);
#else
	return NULL;
#endif
}

/* That one is always defined... it's not worth it to remove it for NO_ID3V2. */
enum mpg123_text_encoding attribute_align_arg mpg123_enc_from_id3(unsigned char id3_enc_byte)
{
	switch(id3_enc_byte)
	{
		case mpg123_id3_latin1:   return mpg123_text_latin1;
		case mpg123_id3_utf16bom: return mpg123_text_utf16bom; /* ID3v2.3 has UCS-2 with BOM here. */
		case mpg123_id3_utf16be:  return mpg123_text_utf16be;
		case mpg123_id3_utf8:     return mpg123_text_utf8;
		default: return mpg123_text_unknown;
	}
}

#ifndef NO_STRING
int mpg123_store_utf8(mpg123_string *sb, enum mpg123_text_encoding enc, const unsigned char *source, size_t source_size)
{
	switch(enc)
	{
#ifndef NO_ID3V2
		/* The encodings we get from ID3v2 tags. */
		case mpg123_text_utf8:
			id3_to_utf8(sb, mpg123_id3_utf8, source, source_size, 0);
		break;
		case mpg123_text_latin1:
			id3_to_utf8(sb, mpg123_id3_latin1, source, source_size, 0);
		break;
		case mpg123_text_utf16bom:
		case mpg123_text_utf16:
			id3_to_utf8(sb, mpg123_id3_utf16bom, source, source_size, 0);
		break;
		/* Special because one cannot skip zero bytes here. */
		case mpg123_text_utf16be:
			id3_to_utf8(sb, mpg123_id3_utf16be, source, source_size, 0);
		break;
#endif
#ifndef NO_ICY
		/* ICY encoding... */
		case mpg123_text_icy:
		case mpg123_text_cp1252:
		{
			mpg123_free_string(sb);
			/* Paranoia: Make sure that the string ends inside the buffer... */
			if(source[source_size-1] == 0)
			{
				/* Convert from ICY encoding... with force applied or not. */
				char *tmpstring = icy2utf8((const char*)source, enc == mpg123_text_cp1252 ? 1 : 0);
				if(tmpstring != NULL)
				{
					mpg123_set_string(sb, tmpstring);
					free(tmpstring);
				}
			}
		}
		break;
#endif
		default:
			mpg123_free_string(sb);
	}
	/* At least a trailing null of some form should be there... */
	return (sb->fill > 0) ? 1 : 0;
}
#endif

int attribute_align_arg mpg123_index(mpg123_handle *mh, off_t **offsets, off_t *step, size_t *fill)
{
	if(mh == NULL) return MPG123_ERR;
	if(offsets == NULL || step == NULL || fill == NULL)
	{
		mh->err = MPG123_BAD_INDEX_PAR;
		return MPG123_ERR;
	}
#ifdef FRAME_INDEX
	*offsets = mh->index.data;
	*step    = mh->index.step;
	*fill    = mh->index.fill;
#else
	*offsets = NULL;
	*step    = 0;
	*fill    = 0;
#endif
	return MPG123_OK;
}

int attribute_align_arg mpg123_set_index(mpg123_handle *mh, off_t *offsets, off_t step, size_t fill)
{
	if(mh == NULL) return MPG123_ERR;
#ifdef FRAME_INDEX
	if(step == 0)
	{
		mh->err = MPG123_BAD_INDEX_PAR;
		return MPG123_ERR;
	}
	if(fi_set(&mh->index, offsets, step, fill) == -1)
	{
		mh->err = MPG123_OUT_OF_MEM;
		return MPG123_ERR;
	}
	return MPG123_OK;
#else
	mh->err = MPG123_MISSING_FEATURE;
	return MPG123_ERR;
#endif
}

int attribute_align_arg mpg123_close(mpg123_handle *mh)
{
	if(mh == NULL) return MPG123_ERR;

	/* mh->rd is never NULL! */
	if(mh->rd->close != NULL) mh->rd->close(mh);

	if(mh->new_format)
	{
		debug("Hey, we are closing a track before the new format has been queried...");
		invalidate_format(&mh->af);
		mh->new_format = 0;
	}
	/* Always reset the frame buffers on close, so we cannot forget it in funky opening routines (wrappers, even). */
	frame_reset(mh);
	return MPG123_OK;
}

void attribute_align_arg mpg123_delete(mpg123_handle *mh)
{
	if(mh != NULL)
	{
		mpg123_close(mh);
		frame_exit(mh); /* free buffers in frame */
		free(mh); /* free struct; cast? */
	}
}

static const char *mpg123_error[] =
{
	"No error... (code 0)",
	"Unable to set up output format! (code 1)",
	"Invalid channel number specified. (code 2)",
	"Invalid sample rate specified. (code 3)",
	"Unable to allocate memory for 16 to 8 converter table! (code 4)",
	"Bad parameter id! (code 5)",
	"Bad buffer given -- invalid pointer or too small size. (code 6)",
	"Out of memory -- some malloc() failed. (code 7)",
	"You didn't initialize the library! (code 8)",
	"Invalid decoder choice. (code 9)",
	"Invalid mpg123 handle. (code 10)",
	"Unable to initialize frame buffers (out of memory?)! (code 11)",
	"Invalid RVA mode. (code 12)",
	"This build doesn't support gapless decoding. (code 13)",
	"Not enough buffer space. (code 14)",
	"Incompatible numeric data types. (code 15)",
	"Bad equalizer band. (code 16)",
	"Null pointer given where valid storage address needed. (code 17)",
	"Error reading the stream. (code 18)",
	"Cannot seek from end (end is not known). (code 19)",
	"Invalid 'whence' for seek function. (code 20)",
	"Build does not support stream timeouts. (code 21)",
	"File access error. (code 22)",
	"Seek not supported by stream. (code 23)",
	"No stream opened. (code 24)",
	"Bad parameter handle. (code 25)",
	"Invalid parameter addresses for index retrieval. (code 26)",
	"Lost track in the bytestream and did not attempt resync. (code 27)",
	"Failed to find valid MPEG data within limit on resync. (code 28)",
	"No 8bit encoding possible. (code 29)",
	"Stack alignment is not good. (code 30)",
	"You gave me a NULL buffer? (code 31)",
	"File position is screwed up, please do an absolute seek (code 32)",
	"Inappropriate NULL-pointer provided.",
	"Bad key value given.",
	"There is no frame index (disabled in this build).",
	"Frame index operation failed.",
	"Decoder setup failed (invalid combination of settings?)",
	"Feature not in this build."
	,"Some bad value has been provided."
	,"Low-level seeking has failed (call to lseek(), usually)."
	,"Custom I/O obviously not prepared."
	,"Overflow in LFS (large file support) conversion."
	,"Overflow in integer conversion."
};

const char* attribute_align_arg mpg123_plain_strerror(int errcode)
{
	if(errcode >= 0 && errcode < sizeof(mpg123_error)/sizeof(char*))
	return mpg123_error[errcode];
	else switch(errcode)
	{
		case MPG123_ERR:
			return "A generic mpg123 error.";
		case MPG123_DONE:
			return "Message: I am done with this track.";
		case MPG123_NEED_MORE:
			return "Message: Feed me more input data!";
		case MPG123_NEW_FORMAT:
			return "Message: Prepare for a changed audio format (query the new one)!";
		default:
			return "I have no idea - an unknown error code!";
	}
}

int attribute_align_arg mpg123_errcode(mpg123_handle *mh)
{
	if(mh != NULL) return mh->err;
	return MPG123_BAD_HANDLE;
}

const char* attribute_align_arg mpg123_strerror(mpg123_handle *mh)
{
	return mpg123_plain_strerror(mpg123_errcode(mh));
}
