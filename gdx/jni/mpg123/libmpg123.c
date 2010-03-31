/*
	libmpg123: MPEG Audio Decoder library

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org

*/

#include "mpg123lib_intern.h"
#define SAMPLE_ADJUST(x)   ((x) - ((mh->p.flags & MPG123_GAPLESS) ? mh->begin_os : 0))
#define SAMPLE_UNADJUST(x) ((x) + ((mh->p.flags & MPG123_GAPLESS) ? mh->begin_os : 0))
#define SEEKFRAME(mh) ((mh)->ignoreframe < 0 ? 0 : (mh)->ignoreframe)

static int initialized = 0;

/*
	Take the buffer after a frame decode (strictly: it is the data from frame fr->num!) and cut samples out.
	fr->buffer.fill may then be smaller than before...
*/
static void frame_buffercheck(mpg123_handle *fr){
	/* When we have no accurate position, gapless code does not make sense. */
	if(!fr->accurate) return;

	/* The first interesting frame: Skip some leading samples. */
	if(fr->firstoff && fr->num == fr->firstframe)
	{
		off_t byteoff = samples_to_bytes(fr, fr->firstoff);
		if((off_t)fr->buffer.fill > byteoff)
		{
			fr->buffer.fill -= byteoff;
			/* buffer.p != buffer.data only for own buffer */
			if(fr->own_buffer) fr->buffer.p = fr->buffer.data + byteoff;
			else memmove(fr->buffer.data, fr->buffer.data + byteoff, fr->buffer.fill);
		}
		else fr->buffer.fill = 0;
		fr->firstoff = 0; /* Only enter here once... when you seek, firstoff should be reset. */
	}
	/* The last interesting (planned) frame: Only use some leading samples. */
	if(fr->lastoff && fr->num == fr->lastframe)
	{
		off_t byteoff = samples_to_bytes(fr, fr->lastoff);
		if((off_t)fr->buffer.fill > byteoff)
		{
			fr->buffer.fill = byteoff;
		}
		fr->lastoff = 0; /* Only enter here once... when you seek, lastoff should be reset. */
	}
}

int attribute_align_arg mpg123_init(void){
	if((sizeof(short) != 2) || (sizeof(long) < 4)) return MPG123_BAD_TYPES;

	if(initialized) return MPG123_OK; /* no need to initialize twice */

	init_layer12(); /* inits also shared tables with layer1 */
	init_layer3();
	prepare_decode_tables();
	initialized = 1;
	return MPG123_OK;
}

void attribute_align_arg mpg123_exit(void) {
	/* nothing yet, but something later perhaps */
}

/* create a new handle with specified decoder, decoder can be "", "auto" or NULL for auto-detection */
mpg123_handle attribute_align_arg *mpg123_new(const char* decoder, int *error){
	return mpg123_parnew(NULL, decoder, error);
}

/* ...the full routine with optional initial parameters to override defaults. */
mpg123_handle attribute_align_arg *mpg123_parnew(mpg123_pars *mp, const char* decoder, int *error){
	mpg123_handle *fr = NULL;
	int err = MPG123_OK;

	if(initialized) fr = (mpg123_handle*) malloc(sizeof(mpg123_handle));
	else err = MPG123_NOT_INITIALIZED;
	if(fr != NULL) {
		frame_init_par(fr, mp);
		if(frame_cpu_opt(fr, decoder) != 1) {
			err = MPG123_BAD_DECODER;
			frame_exit(fr);
			free(fr);
			fr = NULL;
		}
	}
	if(fr != NULL) {
		/* Cleanup that mess! ... use mpg123_decoder / decode_update! */
		if(frame_outbuffer(fr) != 0) {
			err = MPG123_NO_BUFFERS;
			frame_exit(fr);
			free(fr);
			fr = NULL;
		} else {
			/* I smell cleanup here... with get_next_frame() */
/*			if(decode_update(fr) != 0)
			{
				err = fr->err != MPG123_OK ? fr->err : MPG123_BAD_DECODER;
				frame_exit(fr);
				free(fr);
				fr = NULL;
			}
			else */
			fr->decoder_change = 1;
		}
	}
	else if(err == MPG123_OK) err = MPG123_OUT_OF_MEM;

	if(error != NULL) *error = err;
	return fr;
}

int attribute_align_arg mpg123_decoder(mpg123_handle *mh, const char* decoder){
	enum optdec dt = dectype(decoder);
	if(mh == NULL) return MPG123_ERR;

	if(dt == nodec) {
		mh->err = MPG123_BAD_DECODER;
		return MPG123_ERR;
	}
	if(dt == mh->cpu_type) return MPG123_OK;

	/* Now really change. */
	/* frame_exit(mh);	frame_init(mh); */
	if(frame_cpu_opt(mh, decoder) != 1)	{
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
	/* I smell cleanup here... with get_next_frame() */
	decode_update(mh);
	mh->decoder_change = 1;
	return MPG123_OK;
}

int attribute_align_arg mpg123_param(mpg123_handle *mh, enum mpg123_parms key, long val, double fval)
{
	int r;
	if(mh == NULL) return MPG123_ERR;
	r = mpg123_par(&mh->p, key, val, fval);
	if(r != MPG123_OK){ mh->err = r; r = MPG123_ERR; }
	else { /* Special treatment for some settings. */
		if(key == MPG123_INDEX_SIZE) { /* Apply frame index size and grow property on the fly. */
			r = frame_index_setup(mh);
			if(r != MPG123_OK) mh->err = MPG123_INDEX_FAIL;
		}
	}
	return r;
}

int attribute_align_arg mpg123_par(mpg123_pars *mp, enum mpg123_parms key, long val, double fval) {
	int ret = MPG123_OK;
	if(mp == NULL) return MPG123_BAD_PARS;
	switch(key)	{
		case MPG123_VERBOSE:
			mp->verbose = val;
		break;
		case MPG123_FLAGS:
			if(ret == MPG123_OK) mp->flags = val;
		break;
		case MPG123_ADD_FLAGS:
			mp->flags |= val;
		break;
		case MPG123_REMOVE_FLAGS:
			mp->flags &= ~val;
		break;
		case MPG123_FORCE_RATE: /* should this trigger something? */
			if(val > 96000) ret = MPG123_BAD_RATE;
			else mp->force_rate = val < 0 ? 0 : val; /* >0 means enable, 0 disable */
		break;
		case MPG123_DOWN_SAMPLE:
			if(val < 0 || val > 2) ret = MPG123_BAD_RATE;
			else mp->down_sample = (int)val;
		break;
		case MPG123_DOWNSPEED:
			mp->halfspeed = val < 0 ? 0 : val;
		break;
		case MPG123_UPSPEED:
			mp->doublespeed = val < 0 ? 0 : val;
		break;
		case MPG123_OUTSCALE:
			/* Choose the value that is non-zero, if any.
			   Downscaling integers to 1.0 . */
			mp->outscale = val == 0 ? fval : (double)val/SHORT_SCALE;
		break;
		case MPG123_TIMEOUT:
			mp->timeout = val >= 0 ? val : 0;
		break;
		case MPG123_RESYNC_LIMIT:
			mp->resync_limit = val;
		break;
		case MPG123_INDEX_SIZE:
			mp->index_size = val;
		break;
		case MPG123_PREFRAMES:
			if(val >= 0) mp->preframes = val;
			else ret = MPG123_BAD_VALUE;
		break;
		default:
			ret = MPG123_BAD_PARAM;
	}
	return ret;
}

int attribute_align_arg mpg123_getparam(mpg123_handle *mh, enum mpg123_parms key, long *val, double *fval) {
	int r;
	if(mh == NULL) return MPG123_ERR;
	r = mpg123_getpar(&mh->p, key, val, fval);
	if(r != MPG123_OK){ mh->err = r; r = MPG123_ERR; }
	return r;
}

int attribute_align_arg mpg123_getpar(mpg123_pars *mp, enum mpg123_parms key, long *val, double *fval) {
	int ret = 0;
	if(mp == NULL) return MPG123_BAD_PARS;
	switch(key) {
		case MPG123_VERBOSE:
			if(val) *val = mp->verbose;
		break;
		case MPG123_FLAGS:
		case MPG123_ADD_FLAGS:
			if(val) *val = mp->flags;
		break;
		case MPG123_FORCE_RATE:
			if(val) 
			*val = mp->force_rate;
		break;
		case MPG123_DOWN_SAMPLE:
			if(val) *val = mp->down_sample;
		break;
		case MPG123_DOWNSPEED:
			if(val) *val = mp->halfspeed;
		break;
		case MPG123_UPSPEED:
			if(val) *val = mp->doublespeed;
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
			*val = mp->index_size;
		break;
		case MPG123_PREFRAMES:
			*val = mp->preframes;
		break;
		default:
			ret = MPG123_BAD_PARAM;
	}
	return ret;
}

// TODO: own
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

/* plain file access, no http! */
int attribute_align_arg mpg123_open(mpg123_handle *mh, const char *path) {
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	frame_reset(mh);
	return open_stream(mh, path, -1);
}

int attribute_align_arg mpg123_open_fd(mpg123_handle *mh, int fd) {
	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	frame_reset(mh);
	return open_stream(mh, NULL, fd);
}

int decode_update(mpg123_handle *mh) {
	long native_rate;
	int b;
	native_rate = frame_freq(mh);

	b = frame_output_format(mh); /* Select the new output format based on given constraints. */
	if(b < 0) return MPG123_ERR;

	if(b == 1) mh->new_format = 1; /* Store for later... */

	if(mh->af.rate == native_rate) mh->down_sample = 0;
	else if(mh->af.rate == native_rate>>1) mh->down_sample = 1;
	else if(mh->af.rate == native_rate>>2) mh->down_sample = 2;
	else mh->down_sample = 3; /* flexible (fixed) rate */
	switch(mh->down_sample)	{
		case 0:
		case 1:
		case 2:
			mh->down_sample_sblimit = SBLIMIT>>(mh->down_sample);
			/* With downsampling I get less samples per frame */
			mh->outblock = samples_to_bytes(mh, (spf(mh)>>mh->down_sample));
		break;
		case 3: {
			if(synth_ntom_set_step(mh) != 0) return -1;
			if(frame_freq(mh) > mh->af.rate) {
				mh->down_sample_sblimit = SBLIMIT * mh->af.rate;
				mh->down_sample_sblimit /= frame_freq(mh);
			}	else mh->down_sample_sblimit = SBLIMIT;
			mh->outblock = mh->af.encsize * mh->af.channels * ((NTOM_MUL-1+spf(mh) * (((size_t)NTOM_MUL*mh->af.rate)/frame_freq(mh)))/NTOM_MUL );
		}
		break;
	}

	if(!(mh->p.flags & MPG123_FORCE_MONO)) {
		if(mh->af.channels == 1) mh->single = SINGLE_MIX;
		else mh->single = SINGLE_STEREO;
	}	else mh->single = (mh->p.flags & MPG123_FORCE_MONO)-1;
	if(set_synth_functions(mh) != 0) return -1;;

	return 0;
}

size_t attribute_align_arg mpg123_safe_buffer() {
	/* real is the largest possible output (it's 32bit float, 32bit int or 64bit double). */
	return sizeof(real)*2*1152*NTOM_MAX;
}

size_t attribute_align_arg mpg123_outblock(mpg123_handle *mh) {
	if(mh != NULL) return mh->outblock;
	else return mpg123_safe_buffer();
}

static int get_next_frame(mpg123_handle *mh) {
	int change = mh->decoder_change;
	do {
		int b;
		/* Decode & discard some frame(s) before beginning. */
		if(mh->to_ignore && mh->num < mh->firstframe && mh->num >= mh->ignoreframe) {
			/* Decoder structure must be current! decode_update has been called before... */
			(mh->do_layer)(mh); mh->buffer.fill = 0;
			/* The ignored decoding may have failed. Make sure ntom stays consistent. */
			if(mh->down_sample == 3) ntom_set_ntom(mh, mh->num+1);
			mh->to_ignore = mh->to_decode = FALSE;
		}
		/* Read new frame data; possibly breaking out here for MPG123_NEED_MORE. */
		mh->to_decode = FALSE;
		b = read_frame(mh); /* That sets to_decode only if a full frame was read. */
		if(b == MPG123_NEED_MORE) return MPG123_NEED_MORE; /* need another call with data */
		else if(b <= 0) {
			/* More sophisticated error control? */
			if(b==0 || mh->rdat.filepos == mh->rdat.filelen) { /* We simply reached the end. */
				mh->track_frames = mh->num + 1;
				return MPG123_DONE;
			} else return MPG123_ERR; /* Some real error. */
		}
		/* Now, there should be new data to decode ... and also possibly new stream properties */
		if(mh->header_change > 1) change = 1;
		/* Now some accounting: Look at the numbers and decide if we want this frame. */
		++mh->playnum;
		/* Plain skipping without decoding, only when frame is not ignored on next cycle. */
		if(mh->num < mh->firstframe || (mh->p.doublespeed && (mh->playnum % mh->p.doublespeed))) {
			if(!(mh->to_ignore && mh->num < mh->firstframe && mh->num >= mh->ignoreframe)) {
				frame_skip(mh);
				/* Should one fix NtoM here or not?
				   It is not work the trouble for doublespeed, but what with leading frames? */
			}
		}	/* Or, we are finally done and have a new frame. */	else break;
	} while(1);
	/* When we start actually using the CRC, this could move into the loop... */
	/* A question of semantics ... should I fold start_frame and frame_number into firstframe/lastframe? */
	if(mh->lastframe >= 0 && mh->num > mh->lastframe)	{
		mh->to_decode = mh->to_ignore = FALSE;
		return MPG123_DONE;
	}
	if(change) {
		if(decode_update(mh) < 0)  /* dito... */
		return MPG123_ERR;

		mh->decoder_change = 0;
		if(mh->fresh) {
			int b=0;
			/* Prepare offsets for gapless decoding. */
			frame_gapless_realinit(mh);
			frame_set_frameseek(mh, mh->num);
			mh->fresh = 0;
			/* Could this possibly happen? With a real big gapless offset... */
			if(mh->num < mh->firstframe) b = get_next_frame(mh);
			if(b < 0) return b; /* Could be error, need for more, new format... */
		}
	}
	return MPG123_OK;
}

/*
	Not part of the api. This just decodes the frame and fills missing bits with zeroes.
	There can be frames that are broken and thus make do_layer() fail.
*/
void decode_the_frame(mpg123_handle *fr) {
	size_t needed_bytes = samples_to_bytes(fr, frame_outs(fr, fr->num+1)-frame_outs(fr, fr->num));
	fr->clip += (fr->do_layer)(fr);
	/*fprintf(stderr, "frame %"OFF_P": got %"SIZE_P" / %"SIZE_P"\n", fr->num,(size_p)fr->buffer.fill, (size_p)needed_bytes);*/
	/* There could be less data than promised.
	   Also, then debugging, we look out for coding errors that could result in _more_ data than expected. */
	if(fr->buffer.fill < needed_bytes) {
		/* One could do a loop with individual samples instead... but zero is zero. */
		memset(fr->buffer.data + fr->buffer.fill, 0, needed_bytes - fr->buffer.fill);
		fr->buffer.fill = needed_bytes;
		/* ntom_val will be wrong when the decoding wasn't carried out completely */
		ntom_set_ntom(fr, fr->num+1);
	}
	/* Handle unsigned output formats via reshifting after decode here. */
	if(fr->af.encoding == MPG123_ENC_UNSIGNED_16) {
		size_t i;
		short *ssamples;
		unsigned short *usamples;
		ssamples = (short*)fr->buffer.data;
		usamples = (unsigned short*)fr->buffer.data;
		for(i=0; i<fr->buffer.fill/sizeof(short); ++i) {
			long tmp = (long)ssamples[i]+32768;
			usamples[i] = (unsigned short)tmp;
		}
	}
}

int attribute_align_arg mpg123_read(mpg123_handle *mh, unsigned char *out, size_t size, size_t *done){
	return mpg123_decode(mh, NULL, 0, out, size, done);
}

int attribute_align_arg mpg123_decode(mpg123_handle *mh, const unsigned char *inmemory, size_t inmemsize, unsigned char *outmemory, size_t outmemsize, size_t *done){
	int ret = MPG123_OK;
	size_t mdone = 0;
	if(done != NULL) *done = 0;
	if(mh == NULL) return MPG123_ERR;
	if(inmemsize > 0) {
		ret = MPG123_ERR;
		goto decodeend;
	}
	if(outmemory == NULL) outmemsize = 0; /* Not just give error, give chance to get a status message. */

	while(ret == MPG123_OK)	{
		/* Decode a frame that has been read before.
		   This only happens when buffer is empty! */
		if(mh->to_decode) {
			if(mh->new_format) {
				mh->new_format = 0;
				return MPG123_NEW_FORMAT;
			}
			if(mh->buffer.size - mh->buffer.fill < mh->outblock) {
				ret = MPG123_NO_SPACE;
				goto decodeend;
			}
			decode_the_frame(mh);
			mh->to_decode = mh->to_ignore = FALSE;
			mh->buffer.p = mh->buffer.data;
			frame_buffercheck(mh); /* Seek & gapless. */
		}
		if(mh->buffer.fill) /* Copy (part of) the decoded data to the caller's buffer. */	{
			/* get what is needed - or just what is there */
			int a = mh->buffer.fill > (outmemsize - mdone) ? outmemsize - mdone : mh->buffer.fill;
			memcpy(outmemory, mh->buffer.p, a);
			/* less data in frame buffer, less needed, output pointer increase, more data given... */
			mh->buffer.fill -= a;
			outmemory  += a;
			mdone += a;
			mh->buffer.p += a;
			if(!(outmemsize > mdone)) goto decodeend;
		}	else /* If we didn't have data, get a new frame. */	{
			int b = get_next_frame(mh);
			if(b < 0){ ret = b; goto decodeend; }
		}
	}
decodeend:
	if(done != NULL) *done = mdone;
	return ret;
}

#define track_need_init(mh) (!(mh)->to_decode && (mh)->fresh)

static int init_track(mpg123_handle *mh){
	if(track_need_init(mh)) {
		/* Fresh track, need first frame for basic info. */
		int b = get_next_frame(mh);
		if(b < 0) return b;
	}
	return 0;
}

int attribute_align_arg mpg123_getformat(mpg123_handle *mh, long *rate, int *channels, int *encoding){
	if(mh == NULL) return MPG123_ERR;
	if(init_track(mh) == MPG123_ERR) return MPG123_ERR;

	if(rate != NULL) *rate = mh->af.rate;
	if(channels != NULL) *channels = mh->af.channels;
	if(encoding != NULL) *encoding = mh->af.encoding;
	mh->new_format = 0;
	return MPG123_OK;
}

/*
	Now, where are we? We need to know the last decoded frame... and what's left of it in buffer.
	The current frame number can mean the last decoded frame or the to-be-decoded frame.
	If mh->to_decode, then mh->num frames have been decoded, the frame mh->num now coming next.
	If not, we have the possibility of mh->num+1 frames being decoded or nothing at all.
	Then, there is firstframe...when we didn't reach it yet, then the next data will come from there.
	mh->num starts with -1
*/
off_t attribute_align_arg mpg123_tell(mpg123_handle *mh){
	if(mh == NULL) return MPG123_ERR;
	if(track_need_init(mh)) return 0;

	{ /* Funny block to keep C89 happy. */
		off_t pos = 0;
		if((mh->num < mh->firstframe) || (mh->num == mh->firstframe && mh->to_decode)) { /* We are at the beginning, expect output from firstframe on. */
			pos = frame_outs(mh, mh->firstframe);
			pos += mh->firstoff;
		}	else if(mh->to_decode) { /* We start fresh with this frame. Buffer should be empty, but we make sure to count it in.  */
			pos = frame_outs(mh, mh->num) - bytes_to_samples(mh, mh->buffer.fill);
		}	else { /* We serve what we have in buffer and then the beginning of next frame... */
			pos = frame_outs(mh, mh->num+1) - bytes_to_samples(mh, mh->buffer.fill);
		}
		/* Substract padding and delay from the beginning. */
		pos = SAMPLE_ADJUST(pos);
		/* Negative sample offsets are not right, less than nothing is still nothing. */
		return pos>0 ? pos : 0;
	}
}

static int do_the_seek(mpg123_handle *mh){
	int b;
	off_t fnum = SEEKFRAME(mh);
	mh->buffer.fill = 0;

	/* If we are inside the ignoreframe - firstframe window, we may get away without actual seeking. */
	if(mh->num < mh->firstframe)	{
		mh->to_decode = FALSE; /* In any case, don't decode the current frame, perhaps ignore instead. */
		if(mh->num > fnum) return MPG123_OK;
	}

	/* If we are already there, we are fine either for decoding or for ignoring. */
	if(mh->num == fnum && (mh->to_decode || fnum < mh->firstframe)) return MPG123_OK;
	/* We have the frame before... just go ahead as normal. */
	if(mh->num == fnum-1)	{
		mh->to_decode = FALSE;
		return MPG123_OK;
	}

	/* OK, real seeking follows... clear buffers and go for it. */
	frame_buffers_reset(mh);
	if(mh->down_sample == 3) {
		ntom_set_ntom(mh, fnum);
	}
	b = mh->rd->seek_frame(mh, fnum);
	if(b<0) return b;
	/* Only mh->to_ignore is TRUE. */
	if(mh->num < mh->firstframe) mh->to_decode = FALSE;

	mh->playnum = mh->num;
	return 0;
}

off_t attribute_align_arg mpg123_seek(mpg123_handle *mh, off_t sampleoff, int whence){
	int b;
	off_t pos;
	pos = mpg123_tell(mh); /* adjusted samples */
	/* pos < 0 also can mean that simply a former seek failed at the lower levels.
	  In that case, we only allow absolute seeks. */
	if(pos < 0 && whence != SEEK_SET)	{ /* Unless we got the obvious error of NULL handle, this is a special seek failure. */
		if(mh != NULL) mh->err = MPG123_NO_RELSEEK;
		return MPG123_ERR;
	}
	if((b=init_track(mh)) < 0) return b;
	switch(whence)	{
		case SEEK_CUR: pos += sampleoff; break;
		case SEEK_SET: pos  = sampleoff; break;
		default: mh->err = MPG123_BAD_WHENCE; return MPG123_ERR;
	}
	if(pos < 0) pos = 0;
	/* pos now holds the wanted sample offset in adjusted samples */
	frame_set_seek(mh, SAMPLE_UNADJUST(pos));
	pos = do_the_seek(mh);
	if(pos < 0) return pos;

	return mpg123_tell(mh);
}

// TODO: own
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

	length = frame_ins2outs(mh, length);
#ifdef GAPLESS
	if(mh->end_os > 0 && length > mh->end_os) length = mh->end_os;
	length -= mh->begin_os;
#endif
	return length;
}

int attribute_align_arg mpg123_meta_check(mpg123_handle *mh){
	if(mh != NULL) return mh->metaflags;
	else return 0;
}

int attribute_align_arg mpg123_close(mpg123_handle *mh){
	if(mh == NULL) return MPG123_ERR;
	if(mh->rd != NULL && mh->rd->close != NULL) mh->rd->close(mh);
	mh->rd = NULL;
	if(mh->new_format) {
		invalidate_format(&mh->af);
		mh->new_format = 0;
	}
	return MPG123_OK;
}

void attribute_align_arg mpg123_delete(mpg123_handle *mh) {
	if(mh != NULL) {
		mpg123_close(mh);
		frame_exit(mh); /* free buffers in frame */
		free(mh); /* free struct; cast? */
	}
}

static const char *mpg123_error[] = {
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
};

const char* attribute_align_arg mpg123_plain_strerror(int errcode) {
	if(errcode >= 0 && errcode < sizeof(mpg123_error)/sizeof(char*))
	return mpg123_error[errcode];
	else switch(errcode) {
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

int attribute_align_arg mpg123_errcode(mpg123_handle *mh) {
	if(mh != NULL) return mh->err;
	return MPG123_BAD_HANDLE;
}

const char* attribute_align_arg mpg123_strerror(mpg123_handle *mh) {
	return mpg123_plain_strerror(mpg123_errcode(mh));
}

