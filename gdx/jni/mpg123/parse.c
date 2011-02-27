/*
	parse: spawned from common; clustering around stream/frame parsing

	copyright ?-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp & Thomas Orgis
*/

#include "mpg123lib_intern.h"

#include <sys/stat.h>
#include <fcntl.h>

#include "getbits.h"

#include <limits.h>
#ifndef ULONG_MAX
/* hm, is this portable across preprocessors? */
#define ULONG_MAX ((unsigned long)-1)
#endif
#define TRACK_MAX_FRAMES ULONG_MAX/4/1152

#define bsbufid(fr) (fr)->bsbuf==(fr)->bsspace[0] ? 0 : ((fr)->bsbuf==fr->bsspace[1] ? 1 : ( (fr)->bsbuf==(fr)->bsspace[0]+512 ? 2 : ((fr)->bsbuf==fr->bsspace[1]+512 ? 3 : -1) ) )

/*
	AAAAAAAA AAABBCCD EEEEFFGH IIJJKLMM
	A: sync
	B: mpeg version
	C: layer
	D: CRC
	E: bitrate
	F:sampling rate
	G: padding
	H: private
	I: channel mode
	J: mode ext
	K: copyright
	L: original
	M: emphasis

	old compare mask 0xfffffd00:
	11111111 11111111 11111101 00000000

	means: everything must match excluding padding and channel mode, ext mode, ...
	But a vbr stream's headers will differ in bitrate!
	We are already strict in allowing only frames of same type in stream, we should at least watch out for VBR while being strict.

	So a better mask is:
	11111111 11111111 00001101 00000000

	Even more, I'll allow varying crc bit.
	11111111 11111110 00001101 00000000

	(still unsure about this private bit)
*/
#define HDRCMPMASK 0xfffe0d00
#define HDRSAMPMASK 0xc00 /* 1100 00000000, FF bits (sample rate) */

/* bitrates for [mpeg1/2][layer] */
static const int tabsel_123[2][3][16] = {
	{
		{0,32,64,96,128,160,192,224,256,288,320,352,384,416,448,},
		{0,32,48,56, 64, 80, 96,112,128,160,192,224,256,320,384,},
		{0,32,40,48, 56, 64, 80, 96,112,128,160,192,224,256,320,}
	},
	{
		{0,32,48,56,64,80,96,112,128,144,160,176,192,224,256,},
		{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,},
		{0,8,16,24,32,40,48,56,64,80,96,112,128,144,160,}
	}
};

const long freqs[9] = { 44100, 48000, 32000, 22050, 24000, 16000 , 11025 , 12000 , 8000 };

static int decode_header(mpg123_handle *fr,unsigned long newhead);

int read_frame_init(mpg123_handle* fr) {
	if(frame_reset(fr) != 0) return -1;
	return 0;
}

/* These two are to be replaced by one function that gives all the frame parameters (for outsiders).*/

int frame_bitrate(mpg123_handle *fr) {
	return tabsel_123[fr->lsf][fr->lay-1][fr->bitrate_index];
}

long frame_freq(mpg123_handle *fr) {
	return freqs[fr->sampling_frequency];
}

#define free_format_header(head) ( ((head & 0xffe00000) == 0xffe00000) && ((head>>17)&3) && (((head>>12)&0xf) == 0x0) && (((head>>10)&0x3) != 0x3 ))

/* compiler is smart enought to inline this one or should I really do it as macro...? */
int head_check(unsigned long head) {
	if (
		/* first 11 bits are set to 1 for frame sync */
		((head & 0xffe00000) != 0xffe00000)
		||
		/* layer: 01,10,11 is 1,2,3; 00 is reserved */
		(!((head>>17)&3))
		||
		/* 1111 means bad bitrate */
		(((head>>12)&0xf) == 0xf)
		||
		/* sampling freq: 11 is reserved */
		(((head>>10)&0x3) == 0x3 )
		/* here used to be a mpeg 2.5 check... re-enabled 2.5 decoding due to lack of evidence that it is really not good */
	)
	{	return FALSE;	} /* if no check failed, the header is valid (hopefully)*/ else	{	return TRUE; }
}

static int check_lame_tag(mpg123_handle *fr) {
	/*
		going to look for Xing or Info at some position after the header
		                                   MPEG 1  MPEG 2/2.5 (LSF)
		Stereo, Joint Stereo, Dual Channel  32      17
		Mono                                17       9

		Also, how to avoid false positives? I guess I should interpret more of the header to rule that out(?).
		I hope that ensuring all zeros until tag start is enough.
	*/
	int lame_offset = (fr->stereo == 2) ? (fr->lsf ? 17 : 32 ) : (fr->lsf ? 9 : 17);
	/* At least skip the decoder delay. */
	if(fr->begin_s == 0) frame_gapless_init(fr, GAPLESS_DELAY, 0);

	if(fr->framesize >= 120+lame_offset) /* traditional Xing header is 120 bytes */	{
		int i;
		int lame_type = 0;
		/* only search for tag when all zero before it (apart from checksum) */
		for(i=2; i < lame_offset; ++i) if(fr->bsbuf[i] != 0) break;
		if(i == lame_offset) {
			if (
					   (fr->bsbuf[lame_offset] == 'I')
				&& (fr->bsbuf[lame_offset+1] == 'n')
				&& (fr->bsbuf[lame_offset+2] == 'f')
				&& (fr->bsbuf[lame_offset+3] == 'o')
			)	{
				lame_type = 1; /* We still have to see what there is */
			}	else if	(
					   (fr->bsbuf[lame_offset] == 'X')
				&& (fr->bsbuf[lame_offset+1] == 'i')
				&& (fr->bsbuf[lame_offset+2] == 'n')
				&& (fr->bsbuf[lame_offset+3] == 'g')
			)	{
				lame_type = 2;
				fr->vbr = MPG123_VBR; /* Xing header means always VBR */
			}
			if(lame_type)	{
				unsigned long xing_flags;

				/* we have one of these headers... */
				/* now interpret the Xing part, I have 120 bytes total for sure */
				/* there are 4 bytes for flags, but only the last byte contains known ones */
				lame_offset += 4; /* now first byte after Xing/Name */
				/* 4 bytes dword for flags */
				#define make_long(a, o) ((((unsigned long) a[o]) << 24) | (((unsigned long) a[o+1]) << 16) | (((unsigned long) a[o+2]) << 8) | ((unsigned long) a[o+3]))
				/* 16 bit */
				#define make_short(a,o) ((((unsigned short) a[o]) << 8) | ((unsigned short) a[o+1]))
				xing_flags = make_long(fr->bsbuf, lame_offset);
				lame_offset += 4;
				if(xing_flags & 1) /* frames */	{
					/*
						In theory, one should use that value for skipping...
						When I know the exact number of samples I could simply count in flush_output,
						but that's problematic with seeking and such.
						I still miss the real solution for detecting the end.
					*/
					fr->track_frames = (off_t) make_long(fr->bsbuf, lame_offset);
					if(fr->track_frames > TRACK_MAX_FRAMES) fr->track_frames = 0; /* endless stream? */
					/* if no further info there, remove/add at least the decoder delay */
					if(fr->p.flags & MPG123_GAPLESS) {
						off_t length = fr->track_frames * spf(fr);
						if(length > 1)
						frame_gapless_init(fr, GAPLESS_DELAY, length+GAPLESS_DELAY);
					}
					lame_offset += 4;
				}
				if(xing_flags & 0x2) /* bytes */	{
					unsigned long xing_bytes = make_long(fr->bsbuf, lame_offset);					/* We assume that this is the _total_ size of the file, including Xing frame ... and ID3 frames...
					   It's not that clearly documented... */
					if(fr->rdat.filelen < 1) fr->rdat.filelen = (off_t) xing_bytes; /* One could start caring for overflow here. */
					lame_offset += 4;
				}
				if(xing_flags & 0x4) /* TOC */ {
					frame_fill_toc(fr, fr->bsbuf+lame_offset);
					lame_offset += 100; /* just skip */
				}
				if(xing_flags & 0x8) /* VBR quality */ lame_offset += 4;
				/* I guess that either 0 or LAME extra data follows */
				/* there may this crc16 be floating around... (?) */
				if(fr->bsbuf[lame_offset] != 0) {
					unsigned char lame_vbr;
					float replay_gain[2] = {0,0};
					float peak = 0;
					float gain_offset = 0; /* going to be +6 for old lame that used 83dB */
					char nb[10];
					memcpy(nb, fr->bsbuf+lame_offset, 9);
					nb[9] = 0;
					if(!strncmp("LAME", nb, 4)) gain_offset = 6;
					lame_offset += 9;
					/* the 4 big bits are tag revision, the small bits vbr method */
					lame_vbr = fr->bsbuf[lame_offset] & 15;
					lame_offset += 1;
					switch(lame_vbr) {
						/* from rev1 proposal... not sure if all good in practice */
						case 1:
						case 8: fr->vbr = MPG123_CBR; break;
						case 2:
						case 9: fr->vbr = MPG123_ABR; break;
						default: fr->vbr = MPG123_VBR; /* 00==unknown is taken as VBR */
					}
					/* skipping: lowpass filter value */
					lame_offset += 1;
					/* replaygain */
					/* 32bit float: peak amplitude -- why did I parse it as int before??*/
					/* Ah, yes, lame seems to store it as int since some day in 2003; I've only seen zeros anyway until now, bah! */
					if (
							 (fr->bsbuf[lame_offset] != 0)
						|| (fr->bsbuf[lame_offset+1] != 0)
						|| (fr->bsbuf[lame_offset+2] != 0)
						|| (fr->bsbuf[lame_offset+3] != 0)
					) {
						/* byte*peak_bytes = (byte*) &peak;
						... endianess ... just copy bytes to avoid floating point operation on unaligned memory?
						peak_bytes[0] = ...
						peak = *(float*) (fr->bsbuf+lame_offset); */
					}
					peak = 0; /* until better times arrived */
					lame_offset += 4;
					/*
						ReplayGain values - lame only writes radio mode gain...
						16bit gain, 3 bits name, 3 bits originator, sign (1=-, 0=+), dB value*10 in 9 bits (fixed point)
						ignore the setting if name or originator == 000!
						radio 0 0 1 0 1 1 1 0 0 1 1 1 1 1 0 1
						audiophile 0 1 0 0 1 0 0 0 0 0 0 1 0 1 0 0
					*/

					for(i =0; i < 2; ++i) {
						unsigned char origin = (fr->bsbuf[lame_offset] >> 2) & 0x7; /* the 3 bits after that... */
						if(origin != 0) {
							unsigned char gt = fr->bsbuf[lame_offset] >> 5; /* only first 3 bits */
							if(gt == 1) gt = 0; /* radio */
							else if(gt == 2) gt = 1; /* audiophile */
							else continue;
							/* get the 9 bits into a number, divide by 10, multiply sign... happy bit banging */
							replay_gain[0] = (float) ((fr->bsbuf[lame_offset] & 0x2) ? -0.1 : 0.1) * (make_short(fr->bsbuf, lame_offset) & 0x1f);
						}
						lame_offset += 2;
					}
					lame_offset += 1; /* skipping encoding flags byte */
					if(fr->vbr == MPG123_ABR) fr->abr_rate = fr->bsbuf[lame_offset];
					lame_offset += 1;
					/* encoder delay and padding, two 12 bit values... lame does write them from int ...*/
					if(fr->p.flags & MPG123_GAPLESS) {
						off_t length = fr->track_frames * spf(fr);
						off_t skipbegin = GAPLESS_DELAY + ((((int) fr->bsbuf[lame_offset]) << 4) | (((int) fr->bsbuf[lame_offset+1]) >> 4));
						off_t skipend = -GAPLESS_DELAY + (((((int) fr->bsbuf[lame_offset+1]) << 8) | (((int) fr->bsbuf[lame_offset+2]))) & 0xfff);
						if(length > 1)
						frame_gapless_init(fr, skipbegin, (skipend < length) ? length-skipend : length);
					}
				}
				/* switch buffer back ... */
				fr->bsbuf = fr->bsspace[fr->bsnum]+512;
				fr->bsnum = (fr->bsnum + 1) & 1;
				return 1; /* got it! */
			}
		}
	}
	return 0; /* no lame tag */
}

/* Just tell if the header is some mono. */
static int header_mono(unsigned long newhead) {
	return ((newhead>>6)&0x3) == MPG_MD_MONO ? TRUE : FALSE;
}

/*
	That's a big one: read the next frame. 1 is success, <= 0 is some error
	Special error READER_MORE means: Please feed more data and try again.
*/
int read_frame(mpg123_handle *fr) {
	/* TODO: rework this thing */
	unsigned long newhead;
	off_t framepos;
	int ret;
	/* stuff that needs resetting if complete frame reading fails */
	int oldsize  = fr->framesize;
	int oldphase = fr->halfphase;
	fr->fsizeold=fr->framesize;       /* for Layer3 */

	/* Speed-down hack: Play it again, Sam (the frame, I mean). */
	if (fr->p.halfspeed) {
		if(fr->halfphase) /* repeat last frame */	{
			fr->to_decode = fr->to_ignore = TRUE;
			--fr->halfphase;
			fr->bitindex = 0;
			fr->wordpointer = (unsigned char *) fr->bsbuf;
			if(fr->lay == 3) memcpy (fr->bsbuf, fr->ssave, fr->ssize);
			if(fr->error_protection) fr->crc = getbits(fr, 16); /* skip crc */
			return 1;
		}	else fr->halfphase = fr->p.halfspeed - 1;
	}

read_again:
	if((ret = fr->rd->head_read(fr,&newhead)) <= 0){ goto read_frame_bad;}

init_resync:

	fr->header_change = 2; /* output format change is possible... */
	if(fr->oldhead)        /* check a following header for change */ {
		if(fr->oldhead == newhead) fr->header_change = 0;
		else
		/* If they have the same sample rate. Note that only is _not_ the case for the first header, as we enforce sample rate match for following frames.
			 So, during one stream, only change of stereoness is possible and indicated by header_change == 2. */
		if((fr->oldhead & HDRSAMPMASK) == (newhead & HDRSAMPMASK)) {
			/* Now if both channel modes are mono or both stereo, it's no big deal. */
			if( header_mono(fr->oldhead) == header_mono(newhead))
			fr->header_change = 1;
		}
	}

	/* watch out for junk/tags on beginning of stream by invalid header */
	if(!fr->firsthead && !head_check(newhead)) {
		int i;

		/* check for id3v2; first three bytes (of 4) are "ID3" */
		if((newhead & (unsigned long) 0xffffff00) == (unsigned long) 0x49443300) {
			int id3ret = 0;
			id3ret = parse_new_id3(fr, newhead);
			if (id3ret < 0){ ret = id3ret; goto read_frame_bad; }
			fr->oldhead = 0;
			goto read_again; /* Also in case of invalid ID3 tag (ret==0), try to get on track again. */
		}

		/* I even saw RIFF headers at the beginning of MPEG streams ;( */
		if(newhead == ('R'<<24)+('I'<<16)+('F'<<8)+'F') {
			if((ret=fr->rd->head_read(fr,&newhead))<=0) goto read_frame_bad;
			while(newhead != ('d'<<24)+('a'<<16)+('t'<<8)+'a') if((ret=fr->rd->head_shift(fr,&newhead))<=0) goto read_frame_bad;
			if((ret=fr->rd->head_read(fr,&newhead))<=0) goto read_frame_bad;
			fr->oldhead = 0;
			goto read_again;
		}
		/* unhandled junk... just continue search for a header */
		/* step in byte steps through next 64K */
		for(i=0;i<65536;i++) {
			if((ret=fr->rd->head_shift(fr,&newhead))<=0) goto read_frame_bad;
			/* if(head_check(newhead)) */
			if(head_check(newhead) && decode_header(fr, newhead))	break;
		}
		if(i == 65536) return 0;
		/* 
		 * should we additionaly check, whether a new frame starts at
		 * the next expected position? (some kind of read ahead)
		 * We could implement this easily, at least for files.
		 */
	}

	/* first attempt of read ahead check to find the real first header; cannot believe what junk is out there! */
	/* for now, a spurious first free format header screws up here; need free format support for detecting false free format headers... */
	if(!fr->firsthead && fr->rdat.flags & (READER_SEEKABLE|READER_BUFFERED) && head_check(newhead) && decode_header(fr, newhead)) {
		unsigned long nexthead = 0;
		int hd = 0;
		off_t start = fr->rd->tell(fr);
		/* step framesize bytes forward and read next possible header*/
		if((ret=fr->rd->skip_bytes(fr, fr->framesize))<0) goto read_frame_bad;
		hd = fr->rd->head_read(fr,&nexthead);
		if(hd==MPG123_NEED_MORE){ ret = hd; goto read_frame_bad; }
		if((ret=fr->rd->back_bytes(fr, fr->rd->tell(fr)-start))<0) goto read_frame_bad;
		if(hd) {
			/* not allowing free format yet */
			if(!head_check(nexthead) || (nexthead & HDRCMPMASK) != (newhead & HDRCMPMASK)) {
				fr->oldhead = 0; /* start over */
				/* try next byte for valid header */
				if((ret=fr->rd->back_bytes(fr, 3))<0) goto read_frame_bad;
				goto read_again;
			}
		}
	}


	/* why has this head check been avoided here before? */
	if(!head_check(newhead)) {
		/* Do resync if not forbidden by flag.
		I used to have a check for not-icy-meta here, but concluded that the desync issues came from a reader bug, not the stream. */
		if( !(fr->p.flags & MPG123_NO_RESYNC))	{
			long try = 0;
			long limit = fr->p.resync_limit;
			
			/* If a resync is needed the bitreservoir of previous frames is no longer valid */
			fr->bitreservoir = 0;

			/* TODO: make this more robust, I'd like to cat two mp3 fragments together (in a dirty way) and still have mpg123 beign able to decode all it somehow. */
			/* Read more bytes until we find something that looks
			 reasonably like a valid header.  This is not a
			 perfect strategy, but it should get us back on the
			 track within a short time (and hopefully without
			 too much distortion in the audio output).  */
			do {
				++try;
				if(limit >= 0 && try >= limit) break;				
				if((ret=fr->rd->head_shift(fr,&newhead)) <= 0) goto read_frame_bad;
				if(!fr->oldhead) goto init_resync;       /* "considered harmful", eh? */
				/* we should perhaps collect a list of valid headers that occured in file... there can be more */
				/* Michael's new resync routine seems to work better with the one frame readahead (and some input buffering?) */
			} while	(!head_check(newhead) /* Simply check for any valid header... we have the readahead to get it straight now(?) */);

			if(limit >= 0 && try >= limit) {
				fr->err = MPG123_RESYNC_FAIL;
				return READER_ERROR;
			}	else {
				fr->firsthead = 0;
				goto init_resync;
			}
		}	else {
			fr->err = MPG123_OUT_OF_SYNC;
			return READER_ERROR;
		}
	}

	if (!fr->firsthead) {
		if(!decode_header(fr,newhead)) goto read_again;
	}	else if(!decode_header(fr,newhead)) goto init_resync;

	/* if filepos is invalid, so is framepos */
	framepos = fr->rd->tell(fr) - 4;
	/* flip/init buffer for Layer 3 */
	{
		unsigned char *newbuf = fr->bsspace[fr->bsnum]+512;
		/* read main data into memory */
		if((ret=fr->rd->read_frame_body(fr,newbuf,fr->framesize))<0) goto read_frame_bad;
		fr->bsbufold = fr->bsbuf;
		fr->bsbuf = newbuf;
	}
	fr->bsnum = (fr->bsnum + 1) & 1;

	if(!fr->firsthead) {
		fr->firsthead = newhead; /* _now_ it's time to store it... the first real header */
		/* This is the first header of our current stream segment.
		   It is only the actual first header of the whole stream when fr->num is still below zero!
		   Think of resyncs where firsthead has been reset for format flexibility. */
		if(fr->num < 0) {
			fr->audio_start = framepos;
			/* Only check for LAME  tag at beginning of whole stream
			   ... when there indeed is one in between, it's the user's problem. */
			if(fr->lay == 3 && check_lame_tag(fr) == 1) { /* ...in practice, Xing/LAME tags are layer 3 only. */
				if(fr->rd->forget != NULL) fr->rd->forget(fr);

				fr->oldhead = 0;
				goto read_again;
			}
			/* now adjust volume */
		}
	}

  fr->bitindex = 0;
  fr->wordpointer = (unsigned char *) fr->bsbuf;
	/* Question: How bad does the floating point value get with repeated recomputation?
	   Also, considering that we can play the file or parts of many times. */
	if(++fr->mean_frames != 0) fr->mean_framesize = ((fr->mean_frames-1)*fr->mean_framesize+compute_bpf(fr)) / fr->mean_frames ;
	++fr->num; /* 0 for first frame! */
	/* save for repetition */
	if(fr->p.halfspeed && fr->lay == 3) memcpy (fr->ssave, fr->bsbuf, fr->ssize);

	/* Keep track of true frame positions in our frame index.
	   but only do so when we are sure that the frame number is accurate... */
	if(fr->accurate && FI_NEXT(fr->index, fr->num))
	fi_add(&fr->index, framepos);

	if(fr->silent_resync > 0) --fr->silent_resync;

	if(fr->rd->forget != NULL) fr->rd->forget(fr);

	fr->to_decode = fr->to_ignore = TRUE;
	if(fr->error_protection) fr->crc = getbits(fr, 16); /* skip crc */

	return 1;
read_frame_bad:
	fr->silent_resync = 0;
	if(fr->err == MPG123_OK) fr->err = MPG123_ERR_READER;
	fr->framesize = oldsize;
	fr->halfphase = oldphase;
	return ret;
}


/*
 * read ahead and find the next MPEG header, to guess framesize
 * return value: guessed framesize
 */
static long guess_freeformat_framesize(mpg123_handle *fr) {
	long i;
	int ret;
	unsigned long head;
	if(!(fr->rdat.flags & (READER_SEEKABLE|READER_BUFFERED))) return -1;
	/* FIXME: We need proper handling/passing of MPG123_NEED_MORE! */
	if((ret=fr->rd->head_read(fr,&head))<=0) return -1;

	/* We are already 4 bytes into it */
	for(i=4;i<65536;i++) {
		if((ret=fr->rd->head_shift(fr,&head))<=0) return -1;
		if(head_check(head)) {
			int sampling_frequency,mpeg25,lsf;
			
			if(head & (1<<20)) {
				lsf = (head & (1<<19)) ? 0x0 : 0x1;
				mpeg25 = 0;
			}	else {
				lsf = 1;
				mpeg25 = 1;
			}
			
			if(mpeg25) sampling_frequency = 6 + ((head>>10)&0x3);
			else sampling_frequency = ((head>>10)&0x3) + (lsf*3);
			
			if((lsf==fr->lsf) && (mpeg25==fr->mpeg25) && (sampling_frequency == fr->sampling_frequency)) {
				fr->rd->back_bytes(fr,i+1);
				return i-3;
			}
		}
	}
	fr->rd->back_bytes(fr,i);
	return -1;
}


/*
 * decode a header and write the information
 * into the frame structure
 */
static int decode_header(mpg123_handle *fr,unsigned long newhead) {
	if(!head_check(newhead)) return 0;
	if( newhead & (1<<20) ) {
		fr->lsf = (newhead & (1<<19)) ? 0x0 : 0x1;
		fr->mpeg25 = 0;
	} else {
		fr->lsf = 1;
		fr->mpeg25 = 1;
	}

	if((fr->p.flags & MPG123_NO_RESYNC) || !fr->oldhead || (((fr->oldhead>>19)&0x3) ^ ((newhead>>19)&0x3)))	{
		/* If "tryresync" is false, assume that certain
		parameters do not change within the stream!
		Force an update if lsf or mpeg25 settings
		have changed. */
		fr->lay = 4-((newhead>>17)&3);
		if( ((newhead>>10)&0x3) == 0x3) return 0; /* exit() here really is too much, isn't it? */
		if(fr->mpeg25) fr->sampling_frequency = 6 + ((newhead>>10)&0x3);
		else fr->sampling_frequency = ((newhead>>10)&0x3) + (fr->lsf*3);
	}

	fr->error_protection = ((newhead>>16)&0x1)^0x1; /* seen a file where this varies (old lame tag without crc, track with crc) */
	fr->bitrate_index = ((newhead>>12)&0xf);
	fr->padding   = ((newhead>>9)&0x1);
	fr->extension = ((newhead>>8)&0x1);
	fr->mode      = ((newhead>>6)&0x3);
	fr->mode_ext  = ((newhead>>4)&0x3);
	fr->copyright = ((newhead>>3)&0x1);
	fr->original  = ((newhead>>2)&0x1);
	fr->emphasis  = newhead & 0x3;
	fr->freeformat = free_format_header(newhead);

	fr->stereo    = (fr->mode == MPG_MD_MONO) ? 1 : 2;

	fr->oldhead = newhead;
	
	/* we can't use tabsel_123 for freeformat, so trying to guess framesize... */
	/* FIXME: We need proper handling/passing of MPG123_NEED_MORE! */
	if(fr->freeformat) {
		/* when we first encounter the frame with freeformat, guess framesize */
		if(fr->freeformat_framesize < 0) {
			fr->framesize = guess_freeformat_framesize(fr);
			if(fr->framesize > 0) fr->freeformat_framesize = fr->framesize - fr->padding;
			else return 0;
		}	/* freeformat should be CBR, so the same framesize can be used at the 2nd reading or later */
		else fr->framesize = fr->freeformat_framesize + fr->padding;
	}

	switch(fr->lay)	{
		case 1:
			fr->do_layer = do_layer1;
			if(!fr->freeformat) {
				fr->framesize  = (long) tabsel_123[fr->lsf][0][fr->bitrate_index] * 12000;
				fr->framesize /= freqs[fr->sampling_frequency];
				fr->framesize  = ((fr->framesize+fr->padding)<<2)-4;
			}
		break;
		case 2:
			fr->do_layer = do_layer2;
			if(!fr->freeformat)	{
				fr->framesize = (long) tabsel_123[fr->lsf][1][fr->bitrate_index] * 144000;
				fr->framesize /= freqs[fr->sampling_frequency];
				fr->framesize += fr->padding - 4;
			}
		break;
		case 3:
			fr->do_layer = do_layer3;
			if(fr->lsf)	fr->ssize = (fr->stereo == 1) ? 9 : 17;
			else fr->ssize = (fr->stereo == 1) ? 17 : 32;

			if(fr->error_protection)
			fr->ssize += 2;

			if(!fr->freeformat)	{
				fr->framesize  = (long) tabsel_123[fr->lsf][2][fr->bitrate_index] * 144000;
				fr->framesize /= freqs[fr->sampling_frequency]<<(fr->lsf);
				fr->framesize = fr->framesize + fr->padding - 4;
			}
		break;
		default:
			return 0;
	}
	if (fr->framesize > MAXFRAMESIZE) return (0);
	return 1;
}

void set_pointer(mpg123_handle *fr, long backstep) {
	fr->wordpointer = fr->bsbuf + fr->ssize - backstep;
	if (backstep) memcpy(fr->wordpointer,fr->bsbufold+fr->fsizeold-backstep,backstep);
	fr->bitindex = 0; 
}

/********************************/

double compute_bpf(mpg123_handle *fr) {
	double bpf;

	switch(fr->lay) {
		case 1:
			bpf = tabsel_123[fr->lsf][0][fr->bitrate_index];
			bpf *= 12000.0 * 4.0;
			bpf /= freqs[fr->sampling_frequency] <<(fr->lsf);
		break;
		case 2:
		case 3:
			bpf = tabsel_123[fr->lsf][fr->lay-1][fr->bitrate_index];
			bpf *= 144000;
			bpf /= freqs[fr->sampling_frequency] << (fr->lsf);
		break;
		default:
			bpf = 1.0;
	}

	return bpf;
}

