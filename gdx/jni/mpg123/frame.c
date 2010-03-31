/*
	frame: Heap of routines dealing with the core mpg123 data structure.

	copyright 2008-9 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include "mpg123lib_intern.h"

static void frame_fixed_reset(mpg123_handle *fr);

/* that's doubled in decode_ntom.c */
#define NTOM_MUL (32768)
#define aligned_pointer(p,type,alignment) \
	(((char*)(p)-(char*)NULL) % (alignment)) \
	? (type*)((char*)(p) + (alignment) - (((char*)(p)-(char*)NULL) % (alignment))) \
	: (type*)(p)

void frame_default_pars(mpg123_pars *mp){
	mp->outscale = 1.0;
	mp->flags = MPG123_GAPLESS;
	mp->force_rate = 0;
	mp->down_sample = 0;
	mp->halfspeed = 0;
	mp->doublespeed = 0;
	mp->verbose = 0;
	mp->timeout = 0;
	mp->resync_limit = -1;
	mp->index_size = INDEX_SIZE;
	mp->preframes = 4; /* That's good  for layer 3 ISO compliance bitstream. */
	mpg123_fmt_all(mp);
}

void frame_init(mpg123_handle *fr){
	frame_init_par(fr, NULL);
}

void frame_init_par(mpg123_handle *fr, mpg123_pars *mp) {
	fr->own_buffer = FALSE;
	fr->buffer.data = NULL;
	fr->rawbuffs = NULL;
	fr->rawbuffss = 0;
	fr->rawdecwin = NULL;
	fr->rawdecwins = 0;
	fr->xing_toc = NULL;
//FIXME: dworz !!!!!!!!!!!!!!!!
	fr->cpu_type = arm;
//	fr->cpu_type = generic;
	/* these two look unnecessary, check guarantee for synth_ntom_set_step (in control_generic, even)! */
	fr->ntom_val[0] = NTOM_MUL>>1;
	fr->ntom_val[1] = NTOM_MUL>>1;
	fr->ntom_step = NTOM_MUL;
	/* frame_outbuffer is missing... */
	/* frame_buffers is missing... that one needs cpu opt setting! */
	/* after these... frame_reset is needed before starting full decode */
	invalidate_format(&fr->af);
	fr->rdat.r_read = NULL;
	fr->rdat.r_lseek = NULL;
	fr->decoder_change = 1;
	fr->err = MPG123_OK;
	if(mp == NULL) frame_default_pars(&fr->p);
	else memcpy(&fr->p, mp, sizeof(struct mpg123_pars_struct));

	fr->down_sample = 0; /* Initialize to silence harmless errors when debugging. */
	frame_fixed_reset(fr); /* Reset only the fixed data, dynamic buffers are not there yet! */
	fr->synth = NULL;
	fr->synth_mono = NULL;
	fr->make_decode_tables = NULL;
	fi_init(&fr->index);
	frame_index_setup(fr); /* Apply the size setting. */
}

mpg123_pars attribute_align_arg *mpg123_new_pars(int *error) {
	mpg123_pars *mp = malloc(sizeof(struct mpg123_pars_struct));
	if(mp != NULL){ frame_default_pars(mp); if(error != NULL) *error = MPG123_OK; }
	else if(error != NULL) *error = MPG123_OUT_OF_MEM;
	return mp;
}

void attribute_align_arg mpg123_delete_pars(mpg123_pars* mp) {
	if(mp != NULL) free(mp);
}

// TODO: own
int attribute_align_arg mpg123_reset_eq(mpg123_handle *mh)
{
	int i;
	mh->have_eq_settings = 0;
	for(i=0; i < 32; ++i) mh->equalizer[0][i] = mh->equalizer[1][i] = DOUBLE_TO_REAL(1.0);

	return MPG123_OK;
}

int frame_outbuffer(mpg123_handle *fr) {
	size_t size = mpg123_safe_buffer()*AUDIOBUFSIZE;
	if(!fr->own_buffer) fr->buffer.data = NULL;
	if(fr->buffer.data != NULL && fr->buffer.size != size) {
		free(fr->buffer.data);
		fr->buffer.data = NULL;
	}
	fr->buffer.size = size;
	if(fr->buffer.data == NULL) fr->buffer.data = (unsigned char*) malloc(fr->buffer.size);
	if(fr->buffer.data == NULL) {
		fr->err = MPG123_OUT_OF_MEM;
		return -1;
	}
	fr->own_buffer = TRUE;
	fr->buffer.fill = 0;
	return 0;
}

int frame_index_setup(mpg123_handle *fr) {
	int ret = MPG123_ERR;
	if(fr->p.index_size >= 0)	{ /* Simple fixed index. */
		fr->index.grow_size = 0;
		ret = fi_resize(&fr->index, (size_t)fr->p.index_size);
	}	else	{ /* A growing index. We give it a start, though. */
		fr->index.grow_size = (size_t)(- fr->p.index_size);
		if(fr->index.size < fr->index.grow_size) ret = fi_resize(&fr->index, fr->index.grow_size);
		else ret = MPG123_OK; /* We have minimal size already... and since growing is OK... */
	}

	return ret;
}

static void frame_decode_buffers_reset(mpg123_handle *fr) {
	memset(fr->rawbuffs, 0, fr->rawbuffss);
}

int frame_buffers(mpg123_handle *fr){
	int buffssize = 0;

	if(2*2*0x110*sizeof(real) > buffssize)
	buffssize = 2*2*0x110*sizeof(real);
	buffssize += 15; /* For 16-byte alignment (SSE likes that). */

	if(fr->rawbuffs != NULL && fr->rawbuffss != buffssize) {
		free(fr->rawbuffs);
		fr->rawbuffs = NULL;
	}

	if(fr->rawbuffs == NULL) fr->rawbuffs = (unsigned char*) malloc(buffssize);
	if(fr->rawbuffs == NULL) return -1;
	fr->rawbuffss = buffssize;
	fr->short_buffs[0][0] = aligned_pointer(fr->rawbuffs,short,16);
	fr->short_buffs[0][1] = fr->short_buffs[0][0] + 0x110;
	fr->short_buffs[1][0] = fr->short_buffs[0][1] + 0x110;
	fr->short_buffs[1][1] = fr->short_buffs[1][0] + 0x110;
	fr->real_buffs[0][0] = aligned_pointer(fr->rawbuffs,real,16);
	fr->real_buffs[0][1] = fr->real_buffs[0][0] + 0x110;
	fr->real_buffs[1][0] = fr->real_buffs[0][1] + 0x110;
	fr->real_buffs[1][1] = fr->real_buffs[1][0] + 0x110;
	/* now the different decwins... all of the same size, actually */
	/* The MMX ones want 32byte alignment, which I'll try to ensure manually */
	{
		int decwin_size = (512+32)*sizeof(real);
		if(decwin_size < (512+32)*4) decwin_size = (512+32)*4;
		decwin_size += 512*4;
		/* Hm, that's basically realloc() ... */
		if(fr->rawdecwin != NULL && fr->rawdecwins != decwin_size) {
			free(fr->rawdecwin);
			fr->rawdecwin = NULL;
		}

		if(fr->rawdecwin == NULL)
		fr->rawdecwin = (unsigned char*) malloc(decwin_size);

		if(fr->rawdecwin == NULL) return -1;

		fr->rawdecwins = decwin_size;
		fr->decwin = (real*) fr->rawdecwin;
	}
	/* Only reset the buffers we created just now. */
	frame_decode_buffers_reset(fr);

	return 0;
}

int frame_buffers_reset(mpg123_handle *fr) {
	fr->buffer.fill = 0; /* hm, reset buffer fill... did we do a flush? */
	fr->bsnum = 0;
	/* Wondering: could it be actually _wanted_ to retain buffer contents over different files? (special gapless / cut stuff) */
	fr->bsbuf = fr->bsspace[1];
	fr->bsbufold = fr->bsbuf;
	fr->bitreservoir = 0; /* Not entirely sure if this is the right place for that counter. */
	frame_decode_buffers_reset(fr);
	memset(fr->bsspace, 0, 2*(MAXFRAMESIZE+512));
	memset(fr->ssave, 0, 34);
	fr->hybrid_blc[0] = fr->hybrid_blc[1] = 0;
	memset(fr->hybrid_block, 0, sizeof(real)*2*2*SBLIMIT*SSLIMIT);
	return 0;
}

void frame_free_toc(mpg123_handle *fr) {
	if(fr->xing_toc != NULL){ free(fr->xing_toc); fr->xing_toc = NULL; }
}

/* Just copy the Xing TOC over... */
int frame_fill_toc(mpg123_handle *fr, unsigned char* in){
	if(fr->xing_toc == NULL) fr->xing_toc = malloc(100);
	if(fr->xing_toc != NULL) {
		memcpy(fr->xing_toc, in, 100);
		return TRUE;
	}
	return FALSE;
}

/* Prepare the handle for a new track.
   Reset variables, buffers... */
int frame_reset(mpg123_handle* fr) {
	frame_buffers_reset(fr);
	frame_fixed_reset(fr);
	frame_free_toc(fr);
	fi_reset(&fr->index);

	return 0;
}

/* Reset everythign except dynamic memory. */
static void frame_fixed_reset(mpg123_handle *fr) {
	open_bad(fr);
	fr->to_decode = FALSE;
	fr->to_ignore = FALSE;
	fr->metaflags = 0;
	fr->outblock = mpg123_safe_buffer();
	fr->num = -1;
	fr->playnum = -1;
	fr->accurate = TRUE;
	fr->silent_resync = 0;
	fr->audio_start = 0;
	fr->clip = 0;
	fr->oldhead = 0;
	fr->firsthead = 0;
	fr->vbr = MPG123_CBR;
	fr->abr_rate = 0;
	fr->track_frames = 0;
	fr->track_samples = -1;
	fr->framesize=0; 
	fr->mean_frames = 0;
	fr->mean_framesize = 0;
	fr->freesize = 0;
	fr->lastscale = -1;
	fr->fsizeold = 0;
	fr->firstframe = 0;
	fr->ignoreframe = fr->firstframe-fr->p.preframes;
	fr->lastframe = -1;
	fr->fresh = 1;
	fr->new_format = 0;
	frame_gapless_init(fr,0,0);
	fr->lastoff = 0;
	fr->firstoff = 0;
	fr->bo = 1; /* the usual bo */
	fr->halfphase = 0; /* here or indeed only on first-time init? */
	fr->error_protection = 0;
	fr->freeformat_framesize = -1;
}

void frame_free_buffers(mpg123_handle *fr) {
	if(fr->rawbuffs != NULL) free(fr->rawbuffs);
	fr->rawbuffs = NULL;
	fr->rawbuffss = 0;
	if(fr->rawdecwin != NULL) free(fr->rawdecwin);
	fr->rawdecwin = NULL;
	fr->rawdecwins = 0;
}

void frame_exit(mpg123_handle *fr) {
	if(fr->own_buffer && fr->buffer.data != NULL) free(fr->buffer.data);
	fr->buffer.data = NULL;
	frame_free_buffers(fr);
	frame_free_toc(fr);
	fi_exit(&fr->index);
}

// TODO: own
int attribute_align_arg mpg123_info(mpg123_handle *mh, struct mpg123_frameinfo *mi)
{
	if(mh == NULL) return MPG123_ERR;
	if(mi == NULL)
	{
		mh->err = MPG123_ERR_NULL;
		return MPG123_ERR;
	}
	mi->version = mh->mpeg25 ? MPG123_2_5 : (mh->lsf ? MPG123_2_0 : MPG123_1_0);
	mi->layer = mh->lay;
	mi->rate = frame_freq(mh);
	switch(mh->mode)
	{
	case 0: mi->mode = MPG123_M_STEREO; break;
	case 1: mi->mode = MPG123_M_JOINT;  break;
	case 2: mi->mode = MPG123_M_DUAL;   break;
	case 3: mi->mode = MPG123_M_MONO;   break;
	default: break; //error("That mode cannot be!");
	}
	mi->mode_ext = mh->mode_ext;
	mi->framesize = mh->framesize+4; /* Include header. */
	mi->flags = 0;
	if(mh->error_protection) mi->flags |= MPG123_CRC;
	if(mh->copyright)        mi->flags |= MPG123_COPYRIGHT;
	if(mh->extension)        mi->flags |= MPG123_PRIVATE;
	if(mh->original)         mi->flags |= MPG123_ORIGINAL;
	mi->emphasis = mh->emphasis;
	mi->bitrate  = frame_bitrate(mh);
	mi->abr_rate = mh->abr_rate;
	mi->vbr = mh->vbr;
	return MPG123_OK;
}

/*
	Fuzzy frame offset searching (guessing).
	When we don't have an accurate position, we may use an inaccurate one.
	Possibilities:
		- use approximate positions from Xing TOC (not yet parsed)
		- guess wildly from mean framesize and offset of first frame / beginning of file.
*/

off_t frame_fuzzy_find(mpg123_handle *fr, off_t want_frame, off_t* get_frame) {
	/* Default is to go to the beginning. */
	off_t ret = fr->audio_start;
	*get_frame = 0;

	/* But we try to find something better. */
	/* Xing VBR TOC works with relative positions, both in terms of audio frames and stream bytes.
	   Thus, it only works when whe know the length of things.
	   Oh... I assume the offsets are relative to the _total_ file length. */
	if(fr->xing_toc != NULL && fr->track_frames > 0 && fr->rdat.filelen > 0) {
		/* One could round... */
		int toc_entry = (int) ((double)want_frame*100./fr->track_frames);
		/* It is an index in the 100-entry table. */
		if(toc_entry < 0)  toc_entry = 0;
		if(toc_entry > 99) toc_entry = 99;

		/* Now estimate back what frame we get. */
		*get_frame = (off_t) ((double)toc_entry/100. * fr->track_frames);
		fr->accurate = FALSE;
		fr->silent_resync = 1;
		/* Question: Is the TOC for whole file size (with/without ID3) or the "real" audio data only?
		   ID3v1 info could also matter. */
		ret = (off_t) ((double)fr->xing_toc[toc_entry]/256.* fr->rdat.filelen);
	}	else if(fr->mean_framesize > 0)	{	/* Just guess with mean framesize (may be exact with CBR files). */
		/* Query filelen here or not? */
		fr->accurate = FALSE; /* Fuzzy! */
		fr->silent_resync = 1;
		*get_frame = want_frame;
		ret = (off_t) (fr->audio_start+fr->mean_framesize*want_frame);
	}
	return ret;
}

/*
	find the best frame in index just before the wanted one, seek to there
	then step to just before wanted one with read_frame
	do not care tabout the stuff that was in buffer but not played back
	everything that left the decoder is counted as played
	
	Decide if you want low latency reaction and accurate timing info or stable long-time playback with buffer!
*/

off_t frame_index_find(mpg123_handle *fr, off_t want_frame, off_t* get_frame) {
	/* default is file start if no index position */
	off_t gopos = 0;
	*get_frame = 0;
	/* Possibly use VBRI index, too? I'd need an example for this... */
	if(fr->index.fill) {
		/* find in index */
		size_t fi;
		/* at index fi there is frame step*fi... */
		fi = want_frame/fr->index.step;
		if(fi >= fr->index.fill) /* If we are beyond the end of frame index...*/ {
			/* When fuzzy seek is allowed, we have some limited tolerance for the frames we want to read rather then jump over. */
			if(fr->p.flags & MPG123_FUZZY && want_frame - (fr->index.fill-1)*fr->index.step > 10) {
				gopos = frame_fuzzy_find(fr, want_frame, get_frame);
				if(gopos > fr->audio_start) return gopos; /* Only in that case, we have a useful guess. */
				/* Else... just continue, fuzzyness didn't help. */
			}
			/* Use the last available position, slowly advancing from that one. */
			fi = fr->index.fill - 1;
		}
		/* We have index position, that yields frame and byte offsets. */
		*get_frame = fi*fr->index.step;
		gopos = fr->index.data[fi];
		fr->accurate = TRUE; /* When using the frame index, we are accurate. */
	} else {
		if(fr->p.flags & MPG123_FUZZY)
		return frame_fuzzy_find(fr, want_frame, get_frame);
		/* A bit hackish here... but we need to be fresh when looking for the first header again. */
		fr->firsthead = 0;
		fr->oldhead = 0;
	}
	return gopos;
}

off_t frame_ins2outs(mpg123_handle *fr, off_t ins) {	
	off_t outs = 0;
	switch(fr->down_sample) {
		case 0:
		case 1:
		case 2:
			outs = ins>>fr->down_sample;
		break;
		case 3: outs = ntom_ins2outs(fr, ins); break;
	}
	return outs;
}

off_t frame_outs(mpg123_handle *fr, off_t num) {
	off_t outs = 0;
	switch(fr->down_sample) {
		case 0:
		case 1:
		case 2:
			outs = (spf(fr)>>fr->down_sample)*num;
		break;
		case 3: outs = ntom_frmouts(fr, num); break;
	}
	return outs;
}

off_t frame_offset(mpg123_handle *fr, off_t outs) {
	off_t num = 0;
	switch(fr->down_sample) {
		case 0:
		case 1:
		case 2:
			num = outs/(spf(fr)>>fr->down_sample);
		break;
		case 3: num = ntom_frameoff(fr, outs); break;
	}
	return num;
}

/* input in _input_ samples */
void frame_gapless_init(mpg123_handle *fr, off_t b, off_t e){
	fr->begin_s = b;
	fr->end_s = e;
	/* These will get proper values later, from above plus resampling info. */
	fr->begin_os = 0;
	fr->end_os = 0;
}

void frame_gapless_realinit(mpg123_handle *fr){
	fr->begin_os = frame_ins2outs(fr, fr->begin_s);
	fr->end_os   = frame_ins2outs(fr, fr->end_s);
}

/* When we got a new sample count, update the gaplessness. */
void frame_gapless_update(mpg123_handle *fr, off_t total_samples){
	if(fr->end_s < 1)	{
		fr->end_s = total_samples;
		frame_gapless_realinit(fr);
	}	else if(fr->end_s > total_samples) {
		fr->end_s = total_samples;
	}
}


/* Compute the needed frame to ignore from, for getting accurate/consistent output for intended firstframe. */
static off_t ignoreframe(mpg123_handle *fr) {
	off_t preshift = fr->p.preframes;
	/* Layer 3 _really_ needs at least one frame before. */
	if(fr->lay==3 && preshift < 1) preshift = 1;
	/* Layer 1 & 2 reall do not need more than 2. */
	if(fr->lay!=3 && preshift > 2) preshift = 2;

	return fr->firstframe - preshift;
}

/* The frame seek... This is not simply the seek to fe*spf(fr) samples in output because we think of _input_ frames here.
   Seek to frame offset 1 may be just seek to 200 samples offset in output since the beginning of first frame is delay/padding.
   Hm, is that right? OK for the padding stuff, but actually, should the decoder delay be better totally hidden or not?
   With gapless, even the whole frame position could be advanced further than requested (since Homey don't play dat). */
void frame_set_frameseek(mpg123_handle *fr, off_t fe) {
	fr->firstframe = fe;
	if(fr->p.flags & MPG123_GAPLESS) {
		/* Take care of the beginning... */
		off_t beg_f = frame_offset(fr, fr->begin_os);
		if(fe <= beg_f)
		{
			fr->firstframe = beg_f;
			fr->firstoff   = fr->begin_os - frame_outs(fr, beg_f);
		}
		else fr->firstoff = 0;
		/* The end is set once for a track at least, on the frame_set_frameseek called in get_next_frame() */
		if(fr->end_os > 0)
		{
			fr->lastframe  = frame_offset(fr,fr->end_os);
			fr->lastoff    = fr->end_os - frame_outs(fr, fr->lastframe);
		} else fr->lastoff = 0;
	} else { fr->firstoff = fr->lastoff = 0; fr->lastframe = -1; }
	fr->ignoreframe = ignoreframe(fr);
}

void frame_skip(mpg123_handle *fr) {
	if(fr->lay == 3) set_pointer(fr, 512);
}

/* Sample accurate seek prepare for decoder. */
/* This gets unadjusted output samples and takes resampling into account */
void frame_set_seek(mpg123_handle *fr, off_t sp){
	fr->firstframe = frame_offset(fr, sp);
	if(fr->down_sample == 3) ntom_set_ntom(fr, fr->firstframe);
	fr->ignoreframe = ignoreframe(fr);
	fr->firstoff = sp - frame_outs(fr, fr->firstframe);
}

int attribute_align_arg mpg123_volume_change(mpg123_handle *mh, double change){
	if(mh == NULL) return MPG123_ERR;
	return mpg123_volume(mh, change + (double) mh->p.outscale);
}

int attribute_align_arg mpg123_volume(mpg123_handle *mh, double vol){
	if(mh == NULL) return MPG123_ERR;

	if(vol >= 0) mh->p.outscale = vol;
	else mh->p.outscale = 0.;

	return MPG123_OK;
}

