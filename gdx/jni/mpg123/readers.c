/* TODO: Check all read calls (in loops, especially!) for return value 0 (EOF)! */

/*
	readers.c: reading input data

	copyright ?-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
*/

#include "mpg123lib_intern.h"
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
/* For select(), I need select.h according to POSIX 2001, else: sys/time.h sys/types.h unistd.h */
/* Including these here although it works without on my Linux install... curious about _why_. */
#include <sys/select.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>

static int default_init(mpg123_handle *fr);
static off_t get_fileinfo(mpg123_handle *);
static ssize_t posix_read(int fd, void *buf, size_t count){ return read(fd, buf, count); }
static off_t   posix_lseek(int fd, off_t offset, int whence){ return lseek(fd, offset, whence); }

static ssize_t plain_fullread(mpg123_handle *fr,unsigned char *buf, ssize_t count);

#define bc_init(a)
#define bc_reset(a)

/* A normal read and a read with timeout. */
static ssize_t plain_read(mpg123_handle *fr, void *buf, size_t count){
	ssize_t ret = fr->rdat.read(fr->rdat.filept, buf, count);
	return ret;
}

/* Wait for data becoming available, allowing soft-broken network connection to die
   This is needed for Shoutcast servers that have forgotten about us while connection was temporarily down. */
static ssize_t timeout_read(mpg123_handle *fr, void *buf, size_t count) {
	struct timeval tv;
	ssize_t ret = 0;
	fd_set fds;
	tv.tv_sec = fr->rdat.timeout_sec;
	tv.tv_usec = 0;
	FD_ZERO(&fds);
	FD_SET(fr->rdat.filept, &fds);
	ret = select(fr->rdat.filept+1, &fds, NULL, NULL, &tv);
	/* This works only with "my" read function. Not user-replaced. */
	if(ret > 0) ret = read(fr->rdat.filept, buf, count);
	else ret=-1; /* no activity is the error */
	return ret;
}

/* stream based operation */
static ssize_t plain_fullread(mpg123_handle *fr,unsigned char *buf, ssize_t count) {
	ssize_t ret,cnt=0;

	/*
		There used to be a check for expected file end here (length value or ID3 flag).
		This is not needed:
		1. EOF is indicated by fdread returning zero bytes anyway.
		2. We get false positives of EOF for either files that grew or
		3. ... files that have ID3v1 tags in between (stream with intro).
	*/
	while(cnt < count) {
		ret = fr->rdat.fdread(fr,buf+cnt,count-cnt);
		if(ret < 0) return READER_ERROR;
		if(ret == 0) break;
		if(!(fr->rdat.flags & READER_BUFFERED)) fr->rdat.filepos += ret;
		cnt += ret;
	}
	return cnt;
}

static off_t stream_lseek(mpg123_handle *fr, off_t pos, int whence) {
	off_t ret;
	ret = fr->rdat.lseek(fr->rdat.filept, pos, whence);
	if (ret >= 0)	fr->rdat.filepos = ret;
	else
	{
		fr->err = MPG123_LSEEK_FAILED;
		ret = READER_ERROR; /* not the original value */
	}
	return ret;
}

static void stream_close(mpg123_handle *fr) {
	if(fr->rdat.flags & READER_FD_OPENED) close(fr->rdat.filept);
	if(fr->rdat.flags & READER_BUFFERED)  bc_reset(&fr->rdat.buffer);
}

static int stream_seek_frame(mpg123_handle *fr, off_t newframe) {
	/* Seekable streams can go backwards and jump forwards.
	   Non-seekable streams still can go forward, just not jump. */
	if((fr->rdat.flags & READER_SEEKABLE) || (newframe >= fr->num))
	{
		off_t preframe; /* a leading frame we jump to */
		off_t seek_to;  /* the byte offset we want to reach */
		off_t to_skip;  /* bytes to skip to get there (can be negative) */
		/*
			now seek to nearest leading index position and read from there until newframe is reached.
			We use skip_bytes, which handles seekable and non-seekable streams
			(the latter only for positive offset, which we ensured before entering here).
		*/
		seek_to = frame_index_find(fr, newframe, &preframe);
		/* No need to seek to index position if we are closer already.
		   But I am picky about fr->num == newframe, play safe by reading the frame again.
		   If you think that's stupid, don't call a seek to the current frame. */
		if(fr->num >= newframe || fr->num < preframe)
		{
			to_skip = seek_to - fr->rd->tell(fr);
			if(fr->rd->skip_bytes(fr, to_skip) != seek_to)
			return READER_ERROR;

			fr->num = preframe-1; /* Watch out! I am going to read preframe... fr->num should indicate the frame before! */
		}
		while(fr->num < newframe)
		{
			/* try to be non-fatal now... frameNum only gets advanced on success anyway */
			if(!read_frame(fr)) break;
		}
		/* Now the wanted frame should be ready for decoding. */

		return MPG123_OK;
	}
	else
	{
		fr->err = MPG123_NO_SEEK;
		return READER_ERROR; /* invalid, no seek happened */
	}
}

/* return FALSE on error, TRUE on success, READER_MORE on occasion */
static int generic_head_read(mpg123_handle *fr,unsigned long *newhead)
{
	unsigned char hbuf[4];
	int ret = fr->rd->fullread(fr,hbuf,4);
	if(ret == READER_MORE) return ret;
	if(ret != 4) return FALSE;

	*newhead = ((unsigned long) hbuf[0] << 24) |
	           ((unsigned long) hbuf[1] << 16) |
	           ((unsigned long) hbuf[2] << 8)  |
	            (unsigned long) hbuf[3];

	return TRUE;
}

/* return FALSE on error, TRUE on success, READER_MORE on occasion */
static int generic_head_shift(mpg123_handle *fr,unsigned long *head)
{
	unsigned char hbuf;
	int ret = fr->rd->fullread(fr,&hbuf,1);
	if(ret == READER_MORE) return ret;
	if(ret != 1) return FALSE;

	*head <<= 8;
	*head |= hbuf;
	*head &= 0xffffffff;
	return TRUE;
}

/* returns reached position... negative ones are bad... */
static off_t stream_skip_bytes(mpg123_handle *fr,off_t len)
{
	if(fr->rdat.flags & READER_SEEKABLE)
	{
		off_t ret = stream_lseek(fr, len, SEEK_CUR);
		return (ret < 0) ? READER_ERROR : ret;
	}
	else if(len >= 0)
	{
		unsigned char buf[1024]; /* ThOr: Compaq cxx complained and it makes sense to me... or should one do a cast? What for? */
		ssize_t ret;
		while (len > 0)
		{
			ssize_t num = len < (off_t)sizeof(buf) ? (ssize_t)len : (ssize_t)sizeof(buf);
			ret = fr->rd->fullread(fr, buf, num);
			if (ret < 0) return ret;
			else if(ret == 0) break; /* EOF... an error? interface defined to tell the actual position... */
			len -= ret;
		}
		return fr->rd->tell(fr);
	}
	else if(fr->rdat.flags & READER_BUFFERED)
	{ /* Perhaps we _can_ go a bit back. */
		if(fr->rdat.buffer.pos >= -len)
		{
			fr->rdat.buffer.pos += len;
			return fr->rd->tell(fr);
		}
		else
		{
			fr->err = MPG123_NO_SEEK;
			return READER_ERROR;
		}
	}
	else
	{
		fr->err = MPG123_NO_SEEK;
		return READER_ERROR;
	}
}

/* Return 0 on success... */
static int stream_back_bytes(mpg123_handle *fr, off_t bytes)
{
	off_t want = fr->rd->tell(fr)-bytes;
	if(want < 0) return READER_ERROR;
	if(stream_skip_bytes(fr,-bytes) != want) return READER_ERROR;

	return 0;
}


/* returns size on success... */
static int generic_read_frame_body(mpg123_handle *fr,unsigned char *buf, int size)
{
	long l;

	if((l=fr->rd->fullread(fr,buf,size)) != size)
	{
		long ll = l;
		if(ll <= 0) ll = 0;

		/* This allows partial frames at the end... do we really want to pad and decode these?! */
		memset(buf+ll,0,size-ll);
	}
	return l;
}

static off_t generic_tell(mpg123_handle *fr)
{
	if(fr->rdat.flags & READER_BUFFERED)
	fr->rdat.filepos = fr->rdat.buffer.fileoff+fr->rdat.buffer.pos;

	return fr->rdat.filepos;
}

/* This does not (fully) work for non-seekable streams... You have to check for that flag, pal! */
static void stream_rewind(mpg123_handle *fr)
{
	if(fr->rdat.flags & READER_SEEKABLE)
	fr->rdat.buffer.fileoff = fr->rdat.filepos = stream_lseek(fr,0,SEEK_SET);
	if(fr->rdat.flags & READER_BUFFERED)
	{
		fr->rdat.buffer.pos      = 0;
		fr->rdat.buffer.firstpos = 0;
		fr->rdat.filepos = fr->rdat.buffer.fileoff;
	}
}

/*
 * returns length of a file (if filept points to a file)
 * reads the last 128 bytes information into buffer
 * ... that is not totally safe...
 */
static off_t get_fileinfo(mpg123_handle *fr) {
	off_t len;
	char id3buf[128];

	if((len=fr->rdat.lseek(fr->rdat.filept,0,SEEK_END)) < 0)	return -1;

	if(fr->rdat.lseek(fr->rdat.filept,-128,SEEK_END) < 0) return -1;

	if(fr->rd->fullread(fr,(unsigned char *)id3buf,128) != 128)	return -1;

	if(!strncmp((char*)id3buf,"TAG",3))	len -= 128;

	if(fr->rdat.lseek(fr->rdat.filept,0,SEEK_SET) < 0)	return -1;

	if(len <= 0)	return -1;

	return len;
}

/*****************************************************************
 * read frame helper
 */

#define bugger_off { mh->err = MPG123_NO_READER; return MPG123_ERR; }
int bad_init(mpg123_handle *mh) bugger_off
void bad_close(mpg123_handle *mh){}
ssize_t bad_fullread(mpg123_handle *mh, unsigned char *data, ssize_t count) bugger_off
int bad_head_read(mpg123_handle *mh, unsigned long *newhead) bugger_off
int bad_head_shift(mpg123_handle *mh, unsigned long *head) bugger_off
off_t bad_skip_bytes(mpg123_handle *mh, off_t len) bugger_off
int bad_read_frame_body(mpg123_handle *mh, unsigned char *data, int size) bugger_off
int bad_back_bytes(mpg123_handle *mh, off_t bytes) bugger_off
int bad_seek_frame(mpg123_handle *mh, off_t num) bugger_off
off_t bad_tell(mpg123_handle *mh) bugger_off
void bad_rewind(mpg123_handle *mh){}
#undef bugger_off

#define READER_STREAM 0
struct reader readers[] = {
	{ /* READER_STREAM */
		default_init,
		stream_close,
		plain_fullread,
		generic_head_read,
		generic_head_shift,
		stream_skip_bytes,
		generic_read_frame_body,
		stream_back_bytes,
		stream_seek_frame,
		generic_tell,
		stream_rewind,
		NULL
	} ,
#ifdef READ_SYSTEM
	,{
		system_init,
		NULL,	/* filled in by system_init() */
		fullread,
		NULL,
		NULL,
		NULL,
		NULL,
		NULL,
		NULL,
		NULL,
		NULL,
		NULL,
	}
#endif
};

struct reader bad_reader = {
	bad_init,
	bad_close,
	bad_fullread,
	bad_head_read,
	bad_head_shift,
	bad_skip_bytes,
	bad_read_frame_body,
	bad_back_bytes,
	bad_seek_frame,
	bad_tell,
	bad_rewind,
	NULL
};

static int default_init(mpg123_handle *fr) {
	if(fr->p.timeout > 0)	{
		int flags;
		if(fr->rdat.r_read != NULL) return -1;
		flags = fcntl(fr->rdat.filept, F_GETFL);
		flags |= O_NONBLOCK;
		fcntl(fr->rdat.filept, F_SETFL, flags);
		fr->rdat.fdread = timeout_read;
		fr->rdat.timeout_sec = fr->p.timeout;
		fr->rdat.flags |= READER_NONBLOCK;
	}	else fr->rdat.fdread = plain_read;

	fr->rdat.read  = fr->rdat.r_read  != NULL ? fr->rdat.r_read  : posix_read;
	fr->rdat.lseek = fr->rdat.r_lseek != NULL ? fr->rdat.r_lseek : posix_lseek;
	fr->rdat.filelen = get_fileinfo(fr);
	fr->rdat.filepos = 0;
	if(fr->rdat.filelen >= 0) fr->rdat.flags |= READER_SEEKABLE;
	/* Switch reader to a buffered one, if allowed. */
	else if(fr->p.flags & MPG123_SEEKBUFFER) {
		fr->err = MPG123_MISSING_FEATURE;
		return -1;
	}
	return 0;
}

void open_bad(mpg123_handle *mh) {
	mh->rd = &bad_reader;
	mh->rdat.flags = 0;
	bc_init(&mh->rdat.buffer);
}

#ifndef O_BINARY
#define O_BINARY (0)
#endif

int open_stream(mpg123_handle *fr, const char *bs_filenam, int fd) {
	int filept_opened = 1;
	int filept; /* descriptor of opened file/stream */

	if(!bs_filenam) /* no file to open, got a descriptor (stdin) */	{
		filept = fd;
		filept_opened = 0; /* and don't try to close it... */
	}	else if((filept = open(bs_filenam, O_RDONLY|O_BINARY)) < 0) /* a plain old file to open... */	{
		fr->err = MPG123_BAD_FILE;
		return MPG123_ERR; /* error... */
	}

	/* now we have something behind filept and can init the reader */
	fr->rdat.filelen = -1;
	fr->rdat.filept  = filept;
	fr->rdat.flags = 0;
	if(filept_opened)	fr->rdat.flags |= READER_FD_OPENED;

	fr->rd = &readers[READER_STREAM];

	if(fr->rd->init(fr) < 0) return -1;

	return MPG123_OK;
}

