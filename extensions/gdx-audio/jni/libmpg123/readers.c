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
/* For select(), I need select.h according to POSIX 2001, else: sys/time.h sys/types.h unistd.h (the latter two included in compat.h already). */
#ifdef HAVE_SYS_SELECT_H
#include <sys/select.h>
#endif
#ifdef HAVE_SYS_TIME_H
#include <sys/time.h>
#endif
#ifdef _MSC_VER
#include <io.h>
#endif

#include "compat.h"
#include "debug.h"

static int default_init(mpg123_handle *fr);
static off_t get_fileinfo(mpg123_handle *);
static ssize_t posix_read(int fd, void *buf, size_t count){ return read(fd, buf, count); }
static off_t   posix_lseek(int fd, off_t offset, int whence){ return lseek(fd, offset, whence); }
static off_t     nix_lseek(int fd, off_t offset, int whence){ return -1; }

static ssize_t plain_fullread(mpg123_handle *fr,unsigned char *buf, ssize_t count);

/* Wrapper to decide between descriptor-based and external handle-based I/O. */
static off_t io_seek(struct reader_data *rdat, off_t offset, int whence);
static ssize_t io_read(struct reader_data *rdat, void *buf, size_t count);

#ifndef NO_FEEDER
/* Bufferchain methods. */
static void bc_init(struct bufferchain *bc);
static void bc_reset(struct bufferchain *bc);
static int bc_append(struct bufferchain *bc, ssize_t size);
#if 0
static void bc_drop(struct bufferchain *bc);
#endif
static int bc_add(struct bufferchain *bc, const unsigned char *data, ssize_t size);
static ssize_t bc_give(struct bufferchain *bc, unsigned char *out, ssize_t size);
static ssize_t bc_skip(struct bufferchain *bc, ssize_t count);
static ssize_t bc_seekback(struct bufferchain *bc, ssize_t count);
static void bc_forget(struct bufferchain *bc);
#endif

/* A normal read and a read with timeout. */
static ssize_t plain_read(mpg123_handle *fr, void *buf, size_t count)
{
	ssize_t ret = io_read(&fr->rdat, buf, count);
	if(VERBOSE3) debug2("read %li bytes of %li", (long)ret, (long)count);
	return ret;
}

#ifdef TIMEOUT_READ

/* Wait for data becoming available, allowing soft-broken network connection to die
   This is needed for Shoutcast servers that have forgotten about us while connection was temporarily down. */
static ssize_t timeout_read(mpg123_handle *fr, void *buf, size_t count)
{
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
	else
	{
		ret=-1; /* no activity is the error */
		if(NOQUIET) error("stream timed out");
	}
	return ret;
}
#endif

#ifndef NO_ICY
/* stream based operation  with icy meta data*/
static ssize_t icy_fullread(mpg123_handle *fr, unsigned char *buf, ssize_t count)
{
	ssize_t ret,cnt;
	cnt = 0;
	if(fr->rdat.flags & READER_SEEKABLE)
	{
		if(NOQUIET) error("mpg123 programmer error: I don't do ICY on seekable streams.");
		return -1;
	}
	/*
		There used to be a check for expected file end here (length value or ID3 flag).
		This is not needed:
		1. EOF is indicated by fdread returning zero bytes anyway.
		2. We get false positives of EOF for either files that grew or
		3. ... files that have ID3v1 tags in between (stream with intro).
	*/

	while(cnt < count)
	{
		/* all icy code is inside this if block, everything else is the plain fullread we know */
		/* debug1("read: %li left", (long) count-cnt); */
		if(fr->icy.next < count-cnt)
		{
			unsigned char temp_buff;
			size_t meta_size;
			ssize_t cut_pos;

			/* we are near icy-metaint boundary, read up to the boundary */
			if(fr->icy.next > 0)
			{
				cut_pos = fr->icy.next;
				ret = fr->rdat.fdread(fr,buf+cnt,cut_pos);
				if(ret < 1)
				{
					if(ret == 0) break; /* Just EOF. */
					if(NOQUIET) error("icy boundary read");

					return READER_ERROR;
				}

				if(!(fr->rdat.flags & READER_BUFFERED)) fr->rdat.filepos += ret;
				cnt += ret;
				fr->icy.next -= ret;
				if(fr->icy.next > 0)
				{
					debug1("another try... still %li left", (long)fr->icy.next);
					continue;
				}
			}
			/* now off to read icy data */

			/* one byte icy-meta size (must be multiplied by 16 to get icy-meta length) */
			
			ret = fr->rdat.fdread(fr,&temp_buff,1); /* Getting one single byte hast to suceed. */
			if(ret < 0){ if(NOQUIET) error("reading icy size"); return READER_ERROR; }
			if(ret == 0) break;

			debug2("got meta-size byte: %u, at filepos %li", temp_buff, (long)fr->rdat.filepos );
			if(!(fr->rdat.flags & READER_BUFFERED)) fr->rdat.filepos += ret; /* 1... */

			if((meta_size = ((size_t) temp_buff) * 16))
			{
				/* we have got some metadata */
				char *meta_buff;
				/* TODO: Get rid of this malloc ... perhaps hooking into the reader buffer pool? */
				meta_buff = malloc(meta_size+1);
				if(meta_buff != NULL)
				{
					ssize_t left = meta_size;
					while(left > 0)
					{
						ret = fr->rdat.fdread(fr,meta_buff+meta_size-left,left);
						/* 0 is error here, too... there _must_ be the ICY data, the server promised! */
						if(ret < 1){ if(NOQUIET) error("reading icy-meta"); return READER_ERROR; }
						left -= ret;
					}
					meta_buff[meta_size] = 0; /* string paranoia */
					if(!(fr->rdat.flags & READER_BUFFERED)) fr->rdat.filepos += ret;

					if(fr->icy.data) free(fr->icy.data);
					fr->icy.data = meta_buff;
					fr->metaflags |= MPG123_NEW_ICY;
					debug2("icy-meta: %s size: %d bytes", fr->icy.data, (int)meta_size);
				}
				else
				{
					if(NOQUIET) error1("cannot allocate memory for meta_buff (%lu bytes) ... trying to skip the metadata!", (unsigned long)meta_size);
					fr->rd->skip_bytes(fr, meta_size);
				}
			}
			fr->icy.next = fr->icy.interval;
		}
		else
		{
			ret = plain_fullread(fr, buf+cnt, count-cnt);
			if(ret < 0){ if(NOQUIET) error1("reading the rest of %li", (long)(count-cnt)); return READER_ERROR; }
			if(ret == 0) break;

			cnt += ret;
			fr->icy.next -= ret;
		}
	}
	/* debug1("done reading, got %li", (long)cnt); */
	return cnt;
}
#else
#define icy_fullread NULL
#endif /* NO_ICY */

/* stream based operation */
static ssize_t plain_fullread(mpg123_handle *fr,unsigned char *buf, ssize_t count)
{
	ssize_t ret,cnt=0;

#ifdef EXTRA_DEBUG
	debug1("plain fullread of %"SSIZE_P, (size_p)count);
#endif
	/*
		There used to be a check for expected file end here (length value or ID3 flag).
		This is not needed:
		1. EOF is indicated by fdread returning zero bytes anyway.
		2. We get false positives of EOF for either files that grew or
		3. ... files that have ID3v1 tags in between (stream with intro).
	*/
	while(cnt < count)
	{
		ret = fr->rdat.fdread(fr,buf+cnt,count-cnt);
		if(ret < 0) return READER_ERROR;
		if(ret == 0) break;
		if(!(fr->rdat.flags & READER_BUFFERED)) fr->rdat.filepos += ret;
		cnt += ret;
	}
	return cnt;
}

static off_t stream_lseek(mpg123_handle *fr, off_t pos, int whence)
{
	off_t ret;
	ret = io_seek(&fr->rdat, pos, whence);
	if (ret >= 0)	fr->rdat.filepos = ret;
	else
	{
		fr->err = MPG123_LSEEK_FAILED;
		ret = READER_ERROR; /* not the original value */
	}
	return ret;
}

static void stream_close(mpg123_handle *fr)
{
	if(fr->rdat.flags & READER_FD_OPENED) compat_close(fr->rdat.filept);

	fr->rdat.filept = 0;

#ifndef NO_FEEDER
	if(fr->rdat.flags & READER_BUFFERED)  bc_reset(&fr->rdat.buffer);
#endif
	if(fr->rdat.flags & READER_HANDLEIO)
	{
		if(fr->rdat.cleanup_handle != NULL) fr->rdat.cleanup_handle(fr->rdat.iohandle);

		fr->rdat.iohandle = NULL;
	}
}

static int stream_seek_frame(mpg123_handle *fr, off_t newframe)
{
	debug2("seek_frame to %"OFF_P" (from %"OFF_P")", (off_p)newframe, (off_p)fr->num);
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

			debug2("going to %lu; just got %lu", (long unsigned)newframe, (long unsigned)preframe);
			fr->num = preframe-1; /* Watch out! I am going to read preframe... fr->num should indicate the frame before! */
		}
		while(fr->num < newframe)
		{
			/* try to be non-fatal now... frameNum only gets advanced on success anyway */
			if(!read_frame(fr)) break;
		}
		/* Now the wanted frame should be ready for decoding. */
		debug1("arrived at %lu", (long unsigned)fr->num);

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
#ifndef NO_FEEDER
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
#endif
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
		return READER_MORE;
	}
	return l;
}

static off_t generic_tell(mpg123_handle *fr)
{
#ifndef NO_FEEDER
	if(fr->rdat.flags & READER_BUFFERED)
	fr->rdat.filepos = fr->rdat.buffer.fileoff+fr->rdat.buffer.pos;
#endif

	return fr->rdat.filepos;
}

/* This does not (fully) work for non-seekable streams... You have to check for that flag, pal! */
static void stream_rewind(mpg123_handle *fr)
{
	if(fr->rdat.flags & READER_SEEKABLE)
	{
		fr->rdat.filepos = stream_lseek(fr,0,SEEK_SET);
#ifndef NO_FEEDER
		fr->rdat.buffer.fileoff = fr->rdat.filepos;
#endif
	}
#ifndef NO_FEEDER
	if(fr->rdat.flags & READER_BUFFERED)
	{
		fr->rdat.buffer.pos      = 0;
		fr->rdat.buffer.firstpos = 0;
		fr->rdat.filepos = fr->rdat.buffer.fileoff;
	}
#endif
}

/*
 * returns length of a file (if filept points to a file)
 * reads the last 128 bytes information into buffer
 * ... that is not totally safe...
 */
static off_t get_fileinfo(mpg123_handle *fr)
{
	off_t len;

	if((len=io_seek(&fr->rdat,0,SEEK_END)) < 0)	return -1;

	if(io_seek(&fr->rdat,-128,SEEK_END) < 0) return -1;

	if(fr->rd->fullread(fr,(unsigned char *)fr->id3buf,128) != 128)	return -1;

	if(!strncmp((char*)fr->id3buf,"TAG",3))	len -= 128;

	if(io_seek(&fr->rdat,0,SEEK_SET) < 0)	return -1;

	if(len <= 0)	return -1;

	return len;
}

#ifndef NO_FEEDER
/* Methods for the buffer chain, mainly used for feed reader, but not just that. */


static struct buffy* buffy_new(size_t size, size_t minsize)
{
	struct buffy *newbuf;
	newbuf = malloc(sizeof(struct buffy));
	if(newbuf == NULL) return NULL;

	newbuf->realsize = size > minsize ? size : minsize;
	newbuf->data = malloc(newbuf->realsize);
	if(newbuf->data == NULL)
	{
		free(newbuf);
		return NULL;
	}
	newbuf->size = 0;
	newbuf->next = NULL;
	return newbuf;
}

static void buffy_del(struct buffy* buf)
{
	if(buf)
	{
		free(buf->data);
		free(buf);
	}
}

/* Delete this buffy and all following buffies. */
static void buffy_del_chain(struct buffy* buf)
{
	while(buf)
	{
		struct buffy* next = buf->next;
		buffy_del(buf);
		buf = next;
	}
}

void bc_prepare(struct bufferchain *bc, size_t pool_size, size_t bufblock)
{
	bc_poolsize(bc, pool_size, bufblock);
	bc->pool = NULL;
	bc->pool_fill = 0;
	bc_init(bc); /* Ensure that members are zeroed for read-only use. */
}

size_t bc_fill(struct bufferchain *bc)
{
	return (size_t)(bc->size - bc->pos);
}

void bc_poolsize(struct bufferchain *bc, size_t pool_size, size_t bufblock)
{
	bc->pool_size = pool_size;
	bc->bufblock = bufblock;
}

void bc_cleanup(struct bufferchain *bc)
{
	buffy_del_chain(bc->pool);
	bc->pool = NULL;
	bc->pool_fill = 0;
}

/* Fetch a buffer from the pool (if possible) or create one. */
static struct buffy* bc_alloc(struct bufferchain *bc, size_t size)
{
	/* Easy route: Just try the first available buffer.
	   Size does not matter, it's only a hint for creation of new buffers. */
	if(bc->pool)
	{
		struct buffy *buf = bc->pool;
		bc->pool = buf->next;
		buf->next = NULL; /* That shall be set to a sensible value later. */
		buf->size = 0;
		--bc->pool_fill;
		debug2("bc_alloc: picked %p from pool (fill now %"SIZE_P")", buf, (size_p)bc->pool_fill);
		return buf;
	}
	else return buffy_new(size, bc->bufblock);
}

/* Either stuff the buffer back into the pool or free it for good. */
static void bc_free(struct bufferchain *bc, struct buffy* buf)
{
	if(!buf) return;

	if(bc->pool_fill < bc->pool_size)
	{
		buf->next = bc->pool;
		bc->pool = buf;
		++bc->pool_fill;
	}
	else buffy_del(buf);
}

/* Make the buffer count in the pool match the pool size. */
static int bc_fill_pool(struct bufferchain *bc)
{
	/* Remove superfluous ones. */
	while(bc->pool_fill > bc->pool_size)
	{
		/* Lazyness: Just work on the front. */
		struct buffy* buf = bc->pool;
		bc->pool = buf->next;
		buffy_del(buf);
		--bc->pool_fill;
	}

	/* Add missing ones. */
	while(bc->pool_fill < bc->pool_size)
	{
		/* Again, just work on the front. */
		struct buffy* buf;
		buf = buffy_new(0, bc->bufblock); /* Use default block size. */
		if(!buf) return -1;

		buf->next = bc->pool;
		bc->pool = buf;
		++bc->pool_fill;
	}

	return 0;
}


static void bc_init(struct bufferchain *bc)
{
	bc->first = NULL;
	bc->last  = bc->first;
	bc->size  = 0;
	bc->pos   = 0;
	bc->firstpos = 0;
	bc->fileoff  = 0;
}

static void bc_reset(struct bufferchain *bc)
{
	/* Free current chain, possibly stuffing back into the pool. */
	while(bc->first)
	{
		struct buffy* buf = bc->first;
		bc->first = buf->next;
		bc_free(bc, buf);
	}
	bc_fill_pool(bc); /* Ignoring an error here... */
	bc_init(bc);
}

/* Create a new buffy at the end to be filled. */
static int bc_append(struct bufferchain *bc, ssize_t size)
{
	struct buffy *newbuf;
	if(size < 1) return -1;

	newbuf = bc_alloc(bc, size);
	if(newbuf == NULL) return -2;

	if(bc->last != NULL)  bc->last->next = newbuf;
	else if(bc->first == NULL) bc->first = newbuf;

	bc->last  = newbuf;
	debug3("bc_append: new last buffer %p with %"SSIZE_P" B (really %"SSIZE_P")", bc->last, (ssize_p)bc->last->size, (ssize_p)bc->last->realsize);
	return 0;
}

/* Append a new buffer and copy content to it. */
static int bc_add(struct bufferchain *bc, const unsigned char *data, ssize_t size)
{
	int ret = 0;
	ssize_t part = 0;
	debug2("bc_add: adding %"SSIZE_P" bytes at %"OFF_P, (ssize_p)size, (off_p)(bc->fileoff+bc->size));
	if(size >=4) debug4("first bytes: %02x %02x %02x %02x", data[0], data[1], data[2], data[3]);

	while(size > 0)
	{
		/* Try to fill up the last buffer block. */
		if(bc->last != NULL && bc->last->size < bc->last->realsize)
		{
			part = bc->last->realsize - bc->last->size;
			if(part > size) part = size;

			debug2("bc_add: adding %"SSIZE_P" B to existing block %p", (ssize_p)part, bc->last);
			memcpy(bc->last->data+bc->last->size, data, part);
			bc->last->size += part;
			size -= part;
			bc->size += part;
			data += part;
		}

		/* If there is still data left, put it into a new buffer block. */
		if(size > 0 && (ret = bc_append(bc, size)) != 0)
		break;
	}

	return ret;
}

/* Common handler for "You want more than I can give." situation. */
static ssize_t bc_need_more(struct bufferchain *bc)
{
	debug3("hit end, back to beginning (%li - %li < %li)", (long)bc->size, (long)bc->pos, (long)bc->size);
	/* go back to firstpos, undo the previous reads */
	bc->pos = bc->firstpos;
	return READER_MORE;
}

/* Give some data, advancing position but not forgetting yet. */
static ssize_t bc_give(struct bufferchain *bc, unsigned char *out, ssize_t size)
{
	struct buffy *b = bc->first;
	ssize_t gotcount = 0;
	ssize_t offset = 0;
	if(bc->size - bc->pos < size) return bc_need_more(bc);

	/* find the current buffer */
	while(b != NULL && (offset + b->size) <= bc->pos)
	{
		offset += b->size;
		b = b->next;
	}
	/* now start copying from there */
	while(gotcount < size && (b != NULL))
	{
		ssize_t loff = bc->pos - offset;
		ssize_t chunk = size - gotcount; /* amount of bytes to get from here... */
		if(chunk > b->size - loff) chunk = b->size - loff;

#ifdef EXTRA_DEBUG
		debug3("copying %liB from %p+%li",(long)chunk, b->data, (long)loff);
#endif

		memcpy(out+gotcount, b->data+loff, chunk);
		gotcount += chunk;
		bc->pos  += chunk;
		offset += b->size;
		b = b->next;
	}
#ifdef EXTRA_DEBUG
	debug2("got %li bytes, pos advanced to %li", (long)gotcount, (long)bc->pos);
#endif

	return gotcount;
}

/* Skip some bytes and return the new position.
   The buffers are still there, just the read pointer is moved! */
static ssize_t bc_skip(struct bufferchain *bc, ssize_t count)
{
	if(count >= 0)
	{
		if(bc->size - bc->pos < count) return bc_need_more(bc);
		else return bc->pos += count;
	}
	else return READER_ERROR;
}

static ssize_t bc_seekback(struct bufferchain *bc, ssize_t count)
{
	if(count >= 0 && count <= bc->pos) return bc->pos -= count;
	else return READER_ERROR;
}

/* Throw away buffies that we passed. */
static void bc_forget(struct bufferchain *bc)
{
	struct buffy *b = bc->first;
	/* free all buffers that are def'n'tly outdated */
	/* we have buffers until filepos... delete all buffers fully below it */
	if(b) debug2("bc_forget: block %lu pos %lu", (unsigned long)b->size, (unsigned long)bc->pos);
	else debug("forget with nothing there!");

	while(b != NULL && bc->pos >= b->size)
	{
		struct buffy *n = b->next; /* != NULL or this is indeed the end and the last cycle anyway */
		if(n == NULL) bc->last = NULL; /* Going to delete the last buffy... */
		bc->fileoff += b->size;
		bc->pos  -= b->size;
		bc->size -= b->size;

		debug5("bc_forget: forgot %p with %lu, pos=%li, size=%li, fileoff=%li", (void*)b->data, (long)b->size, (long)bc->pos,  (long)bc->size, (long)bc->fileoff);

		bc_free(bc, b);
		b = n;
	}
	bc->first = b;
	bc->firstpos = bc->pos;
}

/* reader for input via manually provided buffers */

static int feed_init(mpg123_handle *fr)
{
	bc_init(&fr->rdat.buffer);
	bc_fill_pool(&fr->rdat.buffer);
	fr->rdat.filelen = 0;
	fr->rdat.filepos = 0;
	fr->rdat.flags |= READER_BUFFERED;
	return 0;
}

/* externally called function, returns 0 on success, -1 on error */
int feed_more(mpg123_handle *fr, const unsigned char *in, long count)
{
	int ret = 0;
	if(VERBOSE3) debug("feed_more");
	if((ret = bc_add(&fr->rdat.buffer, in, count)) != 0)
	{
		ret = READER_ERROR;
		if(NOQUIET) error1("Failed to add buffer, return: %i", ret);
	}
	else /* Not talking about filelen... that stays at 0. */

	if(VERBOSE3) debug3("feed_more: %p %luB bufsize=%lu", fr->rdat.buffer.last->data,
		(unsigned long)fr->rdat.buffer.last->size, (unsigned long)fr->rdat.buffer.size);
	return ret;
}

static ssize_t feed_read(mpg123_handle *fr, unsigned char *out, ssize_t count)
{
	ssize_t gotcount = bc_give(&fr->rdat.buffer, out, count);
	if(gotcount >= 0 && gotcount != count) return READER_ERROR;
	else return gotcount;
}

/* returns reached position... negative ones are bad... */
static off_t feed_skip_bytes(mpg123_handle *fr,off_t len)
{
	/* This is either the new buffer offset or some negative error value. */
	off_t res = bc_skip(&fr->rdat.buffer, (ssize_t)len);
	if(res < 0) return res;

	return fr->rdat.buffer.fileoff+res;
}

static int feed_back_bytes(mpg123_handle *fr, off_t bytes)
{
	if(bytes >=0)
	return bc_seekback(&fr->rdat.buffer, (ssize_t)bytes) >= 0 ? 0 : READER_ERROR;
	else
	return feed_skip_bytes(fr, -bytes) >= 0 ? 0 : READER_ERROR;
}

static int feed_seek_frame(mpg123_handle *fr, off_t num){ return READER_ERROR; }

/* Not just for feed reader, also for self-feeding buffered reader. */
static void buffered_forget(mpg123_handle *fr)
{
	bc_forget(&fr->rdat.buffer);
	fr->rdat.filepos = fr->rdat.buffer.fileoff + fr->rdat.buffer.pos;
}

off_t feed_set_pos(mpg123_handle *fr, off_t pos)
{
	struct bufferchain *bc = &fr->rdat.buffer;
	if(pos >= bc->fileoff && pos-bc->fileoff < bc->size)
	{ /* We have the position! */
		bc->pos = (ssize_t)(pos - bc->fileoff);
		debug1("feed_set_pos inside, next feed from %"OFF_P, (off_p)(bc->fileoff+bc->size));
		return bc->fileoff+bc->size; /* Next input after end of buffer... */
	}
	else
	{ /* I expect to get the specific position on next feed. Forget what I have now. */
		bc_reset(bc);
		bc->fileoff = pos;
		debug1("feed_set_pos outside, buffer reset, next feed from %"OFF_P, (off_p)pos);
		return pos; /* Next input from exactly that position. */
	}
}

/* The specific stuff for buffered stream reader. */

static ssize_t buffered_fullread(mpg123_handle *fr, unsigned char *out, ssize_t count)
{
	struct bufferchain *bc = &fr->rdat.buffer;
	ssize_t gotcount;
	if(bc->size - bc->pos < count)
	{ /* Add more stuff to buffer. If hitting end of file, adjust count. */
		unsigned char readbuf[4096];
		ssize_t need = count - (bc->size-bc->pos);
		while(need>0)
		{
			int ret;
			ssize_t got = fr->rdat.fullread(fr, readbuf, sizeof(readbuf));
			if(got < 0)
			{
				if(NOQUIET) error("buffer reading");
				return READER_ERROR;
			}

			if(VERBOSE3) debug1("buffered_fullread: buffering %li bytes from stream (if > 0)", (long)got);
			if(got > 0 && (ret=bc_add(bc, readbuf, got)) != 0)
			{
				if(NOQUIET) error1("unable to add to chain, return: %i", ret);
				return READER_ERROR;
			}

			need -= got; /* May underflow here... */
			if(got < sizeof(readbuf)) /* That naturally catches got == 0, too. */
			{
				if(VERBOSE3) fprintf(stderr, "Note: Input data end.\n");
				break; /* End. */
			}
		}
		if(bc->size - bc->pos < count)
		count = bc->size - bc->pos; /* We want only what we got. */
	}
	gotcount = bc_give(bc, out, count);

	if(VERBOSE3) debug2("wanted %li, got %li", (long)count, (long)gotcount);

	if(gotcount != count){ if(NOQUIET) error("gotcount != count"); return READER_ERROR; }
	else return gotcount;
}
#else
int feed_more(mpg123_handle *fr, const unsigned char *in, long count)
{
	fr->err = MPG123_MISSING_FEATURE;
	return -1;
}
off_t feed_set_pos(mpg123_handle *fr, off_t pos)
{
	fr->err = MPG123_MISSING_FEATURE;
	return -1;
}
#endif /* NO_FEEDER */

/*****************************************************************
 * read frame helper
 */

#define bugger_off { mh->err = MPG123_NO_READER; return MPG123_ERR; }
static int bad_init(mpg123_handle *mh) bugger_off
static void bad_close(mpg123_handle *mh){}
static ssize_t bad_fullread(mpg123_handle *mh, unsigned char *data, ssize_t count) bugger_off
static int bad_head_read(mpg123_handle *mh, unsigned long *newhead) bugger_off
static int bad_head_shift(mpg123_handle *mh, unsigned long *head) bugger_off
static off_t bad_skip_bytes(mpg123_handle *mh, off_t len) bugger_off
static int bad_read_frame_body(mpg123_handle *mh, unsigned char *data, int size) bugger_off
static int bad_back_bytes(mpg123_handle *mh, off_t bytes) bugger_off
static int bad_seek_frame(mpg123_handle *mh, off_t num) bugger_off
static off_t bad_tell(mpg123_handle *mh) bugger_off
static void bad_rewind(mpg123_handle *mh){}
#undef bugger_off

#define READER_STREAM 0
#define READER_ICY_STREAM 1
#define READER_FEED       2
#define READER_BUF_STREAM 3
#define READER_BUF_ICY_STREAM 4
static struct reader readers[] =
{
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
	{ /* READER_ICY_STREAM */
		default_init,
		stream_close,
		icy_fullread,
		generic_head_read,
		generic_head_shift,
		stream_skip_bytes,
		generic_read_frame_body,
		stream_back_bytes,
		stream_seek_frame,
		generic_tell,
		stream_rewind,
		NULL
	},
#ifdef NO_FEEDER
#define feed_init NULL
#define feed_read NULL
#define buffered_fullread NULL
#define feed_seek_frame NULL
#define feed_back_bytes NULL
#define feed_skip_bytes NULL
#define buffered_forget NULL
#endif
	{ /* READER_FEED */
		feed_init,
		stream_close,
		feed_read,
		generic_head_read,
		generic_head_shift,
		feed_skip_bytes,
		generic_read_frame_body,
		feed_back_bytes,
		feed_seek_frame,
		generic_tell,
		stream_rewind,
		buffered_forget
	},
	{ /* READER_BUF_STREAM */
		default_init,
		stream_close,
		buffered_fullread,
		generic_head_read,
		generic_head_shift,
		stream_skip_bytes,
		generic_read_frame_body,
		stream_back_bytes,
		stream_seek_frame,
		generic_tell,
		stream_rewind,
		buffered_forget
	} ,
	{ /* READER_BUF_ICY_STREAM */
		default_init,
		stream_close,
		buffered_fullread,
		generic_head_read,
		generic_head_shift,
		stream_skip_bytes,
		generic_read_frame_body,
		stream_back_bytes,
		stream_seek_frame,
		generic_tell,
		stream_rewind,
		buffered_forget
	},
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

static struct reader bad_reader =
{
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

static int default_init(mpg123_handle *fr)
{
#ifdef TIMEOUT_READ
	if(fr->p.timeout > 0)
	{
		int flags;
		if(fr->rdat.r_read != NULL)
		{
			error("Timeout reading does not work with user-provided read function. Implement it yourself!");
			return -1;
		}
		flags = fcntl(fr->rdat.filept, F_GETFL);
		flags |= O_NONBLOCK;
		fcntl(fr->rdat.filept, F_SETFL, flags);
		fr->rdat.fdread = timeout_read;
		fr->rdat.timeout_sec = fr->p.timeout;
		fr->rdat.flags |= READER_NONBLOCK;
	}
	else
#endif
	fr->rdat.fdread = plain_read;

	fr->rdat.read  = fr->rdat.r_read  != NULL ? fr->rdat.r_read  : posix_read;
	fr->rdat.lseek = fr->rdat.r_lseek != NULL ? fr->rdat.r_lseek : posix_lseek;
#ifndef NO_ICY
	/* ICY streams of any sort shall not be seekable. */
	if(fr->p.icy_interval > 0) fr->rdat.lseek = nix_lseek;
#endif

	fr->rdat.filelen = get_fileinfo(fr);
	fr->rdat.filepos = 0;
	/*
		Don't enable seeking on ICY streams, just plain normal files.
		This check is necessary since the client can enforce ICY parsing on files that would otherwise be seekable.
		It is a task for the future to make the ICY parsing safe with seeks ... or not.
	*/
	if(fr->rdat.filelen >= 0)
	{
		fr->rdat.flags |= READER_SEEKABLE;
		if(!strncmp((char*)fr->id3buf,"TAG",3))
		{
			fr->rdat.flags |= READER_ID3TAG;
			fr->metaflags  |= MPG123_NEW_ID3;
		}
	}
	/* Switch reader to a buffered one, if allowed. */
	else if(fr->p.flags & MPG123_SEEKBUFFER)
	{
#ifdef NO_FEEDER
		error("Buffered readers not supported in this build.");
		fr->err = MPG123_MISSING_FEATURE;
		return -1;
#else
		if     (fr->rd == &readers[READER_STREAM])
		{
			fr->rd = &readers[READER_BUF_STREAM];
			fr->rdat.fullread = plain_fullread;
		}
#ifndef NO_ICY
		else if(fr->rd == &readers[READER_ICY_STREAM])
		{
			fr->rd = &readers[READER_BUF_ICY_STREAM];
			fr->rdat.fullread = icy_fullread;
		}
#endif
		else
		{
			if(NOQUIET) error("mpg123 Programmer's fault: invalid reader");
			return -1;
		}
		bc_init(&fr->rdat.buffer);
		fr->rdat.filelen = 0; /* We carry the offset, but never know how big the stream is. */
		fr->rdat.flags |= READER_BUFFERED;
#endif /* NO_FEEDER */
	}
	return 0;
}


void open_bad(mpg123_handle *mh)
{
	debug("open_bad");
#ifndef NO_ICY
	clear_icy(&mh->icy);
#endif
	mh->rd = &bad_reader;
	mh->rdat.flags = 0;
#ifndef NO_FEEDER
	bc_init(&mh->rdat.buffer);
#endif
	mh->rdat.filelen = -1;
}

int open_feed(mpg123_handle *fr)
{
	debug("feed reader");
#ifdef NO_FEEDER
	error("Buffered readers not supported in this build.");
	fr->err = MPG123_MISSING_FEATURE;
	return -1;
#else
#ifndef NO_ICY
	if(fr->p.icy_interval > 0)
	{
		if(NOQUIET) error("Feed reader cannot do ICY parsing!");

		return -1;
	}
	clear_icy(&fr->icy);
#endif
	fr->rd = &readers[READER_FEED];
	fr->rdat.flags = 0;
	if(fr->rd->init(fr) < 0) return -1;

	debug("feed reader init successful");
	return 0;
#endif /* NO_FEEDER */
}

/* Final code common to open_stream and open_stream_handle. */
static int open_finish(mpg123_handle *fr)
{
#ifndef NO_ICY
	if(fr->p.icy_interval > 0)
	{
		debug("ICY reader");
		fr->icy.interval = fr->p.icy_interval;
		fr->icy.next = fr->icy.interval;
		fr->rd = &readers[READER_ICY_STREAM];
	}
	else
#endif
	{
		fr->rd = &readers[READER_STREAM];
		debug("stream reader");
	}

	if(fr->rd->init(fr) < 0) return -1;

	return MPG123_OK;
}

int open_stream(mpg123_handle *fr, const char *bs_filenam, int fd)
{
	int filept_opened = 1;
	int filept; /* descriptor of opened file/stream */

	clear_icy(&fr->icy); /* can be done inside frame_clear ...? */

	if(!bs_filenam) /* no file to open, got a descriptor (stdin) */
	{
		filept = fd;
		filept_opened = 0; /* and don't try to close it... */
	}
	#ifndef O_BINARY
	#define O_BINARY (0)
	#endif
	else if((filept = compat_open(bs_filenam, O_RDONLY|O_BINARY)) < 0) /* a plain old file to open... */
	{
		if(NOQUIET) error2("Cannot open file %s: %s", bs_filenam, strerror(errno));
		fr->err = MPG123_BAD_FILE;
		return MPG123_ERR; /* error... */
	}

	/* now we have something behind filept and can init the reader */
	fr->rdat.filelen = -1;
	fr->rdat.filept  = filept;
	fr->rdat.flags = 0;
	if(filept_opened)	fr->rdat.flags |= READER_FD_OPENED;

	return open_finish(fr);
}

int open_stream_handle(mpg123_handle *fr, void *iohandle)
{
	clear_icy(&fr->icy); /* can be done inside frame_clear ...? */
	fr->rdat.filelen = -1;
	fr->rdat.filept  = -1;
	fr->rdat.iohandle = iohandle;
	fr->rdat.flags = 0;
	fr->rdat.flags |= READER_HANDLEIO;

	return open_finish(fr);
}

/* Wrappers for actual reading/seeking... I'm full of wrappers here. */
static off_t io_seek(struct reader_data *rdat, off_t offset, int whence)
{
	if(rdat->flags & READER_HANDLEIO)
	{
		if(rdat->r_lseek_handle != NULL)
		{
			return rdat->r_lseek_handle(rdat->iohandle, offset, whence);
		}
		else return -1;
	}
	else
	return rdat->lseek(rdat->filept, offset, whence);
}

static ssize_t io_read(struct reader_data *rdat, void *buf, size_t count)
{
	if(rdat->flags & READER_HANDLEIO)
	{
		if(rdat->r_read_handle != NULL)
		{
			return rdat->r_read_handle(rdat->iohandle, buf, count);
		}
		else return -1;
	}
	else
	return rdat->read(rdat->filept, buf, count);
}
