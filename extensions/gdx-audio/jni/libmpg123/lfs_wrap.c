/*
	lfs_wrap: Crappy wrapper code for supporting crappy ambiguous large file support.

	copyright 2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org

	initially written by Thomas Orgis, thanks to Guido Draheim for consulting

	This file contains wrappers for the case that _FILE_OFFSET_BITS (or equivalent, theoretically, depends on mpg123.h) is defined and thus certain mpg123 API calls get renamed with a suffix (p.ex. _64).
	The renamed calls expect large off_t arguments, and possibly return large off_t values... these wrappers here provide the same functionality with long integer arguments/values.

	Prototypical idea: There is
		off_t mpg123_seek_64(mpg123_handle*, off_t, int)
	This code provides
		long mpg123_seek(mpg123_handle*, long, int)

	This is rather simple business... wouldn't mpg123 offer replacing the I/O core with callbacks. Translating the callbacks between long and off_t world is the main reason why this file contains non-trivial code.

	Note about file descriptors: We just assume that they are generally interchangeable between large and small file code... and that a large file descriptor will trigger errors when accessed with small file code where it may cause trouble (a really large file).
*/

/* It mainly needs the official API ... */
/* ... but also some inside access (frame struct, readers). */
#include "mpg123lib_intern.h"
/* Include the system headers _after_ the implied config.h!
   Otherwise _FILE_OFFSET_BITS is not in effect! */
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>
#include "compat.h"
#include "debug.h"

/*
	Now, start off easy... translate simple API calls.
	I need to deal with these here:
perl -ne '
if(/^\s*EXPORT\s+(\S+)\s+(mpg123_\S+)\((.*)\);\s*$/)
{
	$type = $1;
	$name = $2;
	$args = $3;
	next unless ($type =~ /off_t/ or $args =~ /off_t/);
	print "$name\n" unless grep {$_ eq $name} 
		("mpg123_open", "mpg123_open_fd", "mpg123_open_handle", "mpg123_replace_reader", "mpg123_replace_reader_handle");
}' < mpg123.h.in

mpg123_decode_frame
mpg123_framebyframe_decode
mpg123_framepos
mpg123_tell
mpg123_tellframe
mpg123_tell_stream
mpg123_seek
mpg123_feedseek
mpg123_seek_frame
mpg123_timeframe
mpg123_index
mpg123_set_index
mpg123_position
mpg123_length
mpg123_set_filesize
mpg123_decode_raw  ... that's experimental.

Let's work on them in that order.
*/

/* I see that I will need custom data storage. Main use is for the replaced I/O later, but the seek table for small file offsets needs extra storage, too. */

/* The wrapper handle for descriptor and handle I/O. */

/* The handle is used for nothing (0), or one of these two modes of operation: */
#define IO_FD 1 /* Wrapping over callbacks operation on integer file descriptor. */
#define IO_HANDLE 2 /* Wrapping over custom handle callbacks. */

struct wrap_data
{
	/* Storage for small offset index table. */
	long *indextable;
	/* I/O handle stuff */
	int iotype; /* IO_FD or IO_HANDLE */
	/* Data for IO_FD. */
	int fd;
	int my_fd; /* A descriptor that the wrapper code opened itself. */
	/* The actual callbacks from the outside. */
	ssize_t (*r_read) (int, void *, size_t);
	long (*r_lseek)(int, long, int);
	/* Data for IO_HANDLE. */
	void* handle;
	ssize_t (*r_h_read)(void *, void *, size_t);
	long (*r_h_lseek)(void*, long, int);
	void (*h_cleanup)(void*);
};


/* Cleanup I/O part of the handle handle... but not deleting the wrapper handle itself.
   That is stored in the frame and only deleted on mpg123_delete(). */
static void wrap_io_cleanup(void *handle)
{
	struct wrap_data *ioh = handle;
	if(ioh->iotype == IO_HANDLE)
	{
		if(ioh->h_cleanup != NULL && ioh->handle != NULL)
		ioh->h_cleanup(ioh->handle);

		ioh->handle = NULL;
	}
	if(ioh->my_fd >= 0)
	{
		close(ioh->my_fd);
		ioh->my_fd = -1;
	}
}

/* Really finish off the handle... freeing all memory. */
static void wrap_destroy(void *handle)
{
	struct wrap_data *wh = handle;
	wrap_io_cleanup(handle);
	if(wh->indextable != NULL)
	free(wh->indextable);

	free(wh);
}

/* More helper code... extract the special wrapper handle, possible allocate and initialize it. */
static struct wrap_data* wrap_get(mpg123_handle *mh)
{
	struct wrap_data* whd;
	if(mh == NULL) return NULL;

	/* Access the private storage inside the mpg123 handle.
	   The real callback functions and handles are stored there. */
	if(mh->wrapperdata == NULL)
	{
		/* Create a new one. */
		mh->wrapperdata = malloc(sizeof(struct wrap_data));
		if(mh->wrapperdata == NULL)
		{
			mh->err = MPG123_OUT_OF_MEM;
			return NULL;
		}
	/* When we have wrapper data present, the callback for its proper cleanup is needed. */
		mh->wrapperclean = wrap_destroy;

		whd = mh->wrapperdata;
		whd->indextable = NULL;
		whd->iotype = 0;
		whd->fd = -1;
		whd->my_fd = -1;
		whd->r_read = NULL;
		whd->r_lseek = NULL;
		whd->handle = NULL;
		whd->r_h_read = NULL;
		whd->r_h_lseek = NULL;
		whd->h_cleanup = NULL;
	}
	else whd = mh->wrapperdata;

	return whd;
}

/* After settling the data... start with some simple wrappers. */

#undef mpg123_decode_frame
/* int mpg123_decode_frame(mpg123_handle *mh, off_t *num, unsigned char **audio, size_t *bytes) */
int attribute_align_arg mpg123_decode_frame(mpg123_handle *mh, long *num, unsigned char **audio, size_t *bytes)
{
	off_t largenum;
	int err;

	err = MPG123_LARGENAME(mpg123_decode_frame)(mh, &largenum, audio, bytes);
	if(err == MPG123_OK && num != NULL)
	{
		*num = largenum;
		if(*num != largenum)
		{
			mh->err = MPG123_LFS_OVERFLOW;
			err = MPG123_ERR;
		}
	}
	return err;
}

#undef mpg123_framebyframe_decode
/* int mpg123_framebyframe_decode(mpg123_handle *mh, off_t *num, unsigned char **audio, size_t *bytes); */
int attribute_align_arg mpg123_framebyframe_decode(mpg123_handle *mh, long *num, unsigned char **audio, size_t *bytes)
{
	off_t largenum;
	int err;

	err = MPG123_LARGENAME(mpg123_framebyframe_decode)(mh, &largenum, audio, bytes);
	if(err == MPG123_OK && num != NULL)
	{
		*num = largenum;
		if(*num != largenum)
		{
			mh->err = MPG123_LFS_OVERFLOW;
			err = MPG123_ERR;
		}
	}
	return err;
}

#undef mpg123_framepos
/* off_t mpg123_framepos(mpg123_handle *mh); */
long attribute_align_arg mpg123_framepos(mpg123_handle *mh)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_framepos)(mh);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_tell
/* off_t mpg123_tell(mpg123_handle *mh); */
long attribute_align_arg mpg123_tell(mpg123_handle *mh)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_tell)(mh);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_tellframe
/* off_t mpg123_tellframe(mpg123_handle *mh); */
long attribute_align_arg mpg123_tellframe(mpg123_handle *mh)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_tellframe)(mh);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_tell_stream
/* off_t mpg123_tell_stream(mpg123_handle *mh); */
long attribute_align_arg mpg123_tell_stream(mpg123_handle *mh)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_tell_stream)(mh);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_seek
/* off_t mpg123_seek(mpg123_handle *mh, off_t sampleoff, int whence); */
long attribute_align_arg mpg123_seek(mpg123_handle *mh, long sampleoff, int whence)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_seek)(mh, sampleoff, whence);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_feedseek
/* off_t mpg123_feedseek(mpg123_handle *mh, off_t sampleoff, int whence, off_t *input_offset); */
long attribute_align_arg mpg123_feedseek(mpg123_handle *mh, long sampleoff, int whence, long *input_offset)
{
	long val;
	off_t largeioff;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_feedseek)(mh, sampleoff, whence, &largeioff);
	/* Error/message codes are small... */
	if(largeval < 0) return (long)largeval;

	val = largeval;
	*input_offset = largeioff;
	if(val != largeval || *input_offset != largeioff)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_seek_frame
/* off_t mpg123_seek_frame(mpg123_handle *mh, off_t frameoff, int whence); */
long attribute_align_arg mpg123_seek_frame(mpg123_handle *mh, long frameoff, int whence)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_seek_frame)(mh, frameoff, whence);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

#undef mpg123_timeframe
/* off_t mpg123_timeframe(mpg123_handle *mh, double sec); */
long attribute_align_arg mpg123_timeframe(mpg123_handle *mh, double sec)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_timeframe)(mh, sec);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

/* Now something less simple: Index retrieval and manipulation.
   The index is an _array_ of off_t, which means that I need to construct a copy with translated long values. */
#undef mpg123_index
/* int mpg123_index(mpg123_handle *mh, off_t **offsets, off_t *step, size_t *fill) */
int attribute_align_arg mpg123_index(mpg123_handle *mh, long **offsets, long *step, size_t *fill)
{
	int err;
	size_t i;
	long smallstep;
	size_t thefill;
	off_t largestep;
	off_t *largeoffsets;
	struct wrap_data *whd;

	whd = wrap_get(mh);
	if(whd == NULL) return MPG123_ERR;

	err = MPG123_LARGENAME(mpg123_index)(mh, &largeoffsets, &largestep, &thefill);
	if(err != MPG123_OK) return err;

	/* For a _very_ large file, even the step could overflow. */
	smallstep = largestep;
	if(smallstep != largestep)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	if(step != NULL) *step = smallstep;

	/* When there are no values stored, there is no table content to take care of.
	   Table pointer does not matter. Mission completed. */
	if(thefill == 0) return MPG123_OK;

	if(fill != NULL) *fill = thefill;

	/* Construct a copy of the index to hand over to the small-minded client. */
	*offsets = safe_realloc(whd->indextable, (*fill)*sizeof(long));
	if(*offsets == NULL)
	{
		mh->err = MPG123_OUT_OF_MEM;
		return MPG123_ERR;
	}
	whd->indextable = *offsets;
	/* Elaborate conversion of each index value, with overflow check. */
	for(i=0; i<*fill; ++i)
	{
		whd->indextable[i] = largeoffsets[i];
		if(whd->indextable[i] != largeoffsets[i])
		{
			mh->err = MPG123_LFS_OVERFLOW;
			return MPG123_ERR;
		}
	}
	/* If we came that far... there should be a valid copy of the table now. */
	return MPG123_OK;
}

/* The writing does basically the same than the above, just the opposite.
   Oh, and the overflow checks are not needed -- off_t is bigger than long. */
#undef mpg123_set_index
/* int mpg123_set_index(mpg123_handle *mh, off_t *offsets, off_t step, size_t fill); */
int attribute_align_arg mpg123_set_index(mpg123_handle *mh, long *offsets, long step, size_t fill)
{
	int err;
	size_t i;
	struct wrap_data *whd;
	off_t *indextmp;

	whd = wrap_get(mh);
	if(whd == NULL) return MPG123_ERR;

	/* Expensive temporary storage... for staying outside at the API layer. */
	indextmp = malloc(fill*sizeof(off_t));
	if(indextmp == NULL)
	{
		mh->err = MPG123_OUT_OF_MEM;
		return MPG123_ERR;
	}

	if(fill > 0 && offsets == NULL)
	{
		mh->err = MPG123_BAD_INDEX_PAR;
		err = MPG123_ERR;
	}
	else
	{
		/* Fill the large-file copy of the provided index, then feed it to mpg123. */
		for(i=0; i<fill; ++i)
		indextmp[i] = offsets[i];

		err = MPG123_LARGENAME(mpg123_set_index)(mh, indextmp, step, fill);
	}
	free(indextmp);

	return err;
}

/* So... breathe... a couple of simple wrappers before the big mess. */
#undef mpg123_position
/* int mpg123_position( mpg123_handle *mh, off_t frame_offset, off_t buffered_bytes, off_t *current_frame, off_t *frames_left, double *current_seconds, double *seconds_left); */
int attribute_align_arg mpg123_position(mpg123_handle *mh, long frame_offset, long buffered_bytes, long *current_frame, long *frames_left, double *current_seconds, double *seconds_left)
{
	off_t curframe, frameleft;
	long small_curframe, small_frameleft;
	int err;

	err = MPG123_LARGENAME(mpg123_position)(mh, frame_offset, buffered_bytes, &curframe, &frameleft, current_seconds, seconds_left);
	if(err != MPG123_OK) return err;

	small_curframe = curframe;
	small_frameleft = frameleft;
	if(small_curframe != curframe || small_frameleft != frameleft)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}

	if(current_frame != NULL) *current_frame = small_curframe;

	if(frames_left != NULL) *frames_left = small_frameleft;


	return MPG123_OK;
}

#undef mpg123_length
/* off_t mpg123_length(mpg123_handle *mh); */
long attribute_align_arg mpg123_length(mpg123_handle *mh)
{
	long val;
	off_t largeval;

	largeval = MPG123_LARGENAME(mpg123_length)(mh);
	val = largeval;
	if(val != largeval)
	{
		mh->err = MPG123_LFS_OVERFLOW;
		return MPG123_ERR;
	}
	return val;
}

/* The simplest wrapper of all... */
#undef mpg123_set_filesize
/* int mpg123_set_filesize(mpg123_handle *mh, off_t size); */
int attribute_align_arg mpg123_set_filesize(mpg123_handle *mh, long size)
{
	return MPG123_LARGENAME(mpg123_set_filesize)(mh, size);
}


/* =========================================
             THE BOUNDARY OF SANITY
               Behold, stranger!
   ========================================= */


/*
	The messy part: Replacement of I/O core (actally, this is only due to lseek()).
	Both descriptor and handle replaced I/O are mapped to replaced handle I/O, the handle wrapping over the actual callbacks and the actual handle/descriptor.
	You got multiple levels of handles and callbacks to think about. Have fun reading and comprehending.
*/

/* Could go into compat.h ... Windows needs that flag. */
#ifndef O_BINARY
#define O_BINARY 0
#endif

/* Read callback needs nothing special. */
ssize_t wrap_read(void* handle, void *buf, size_t count)
{
	struct wrap_data *ioh = handle;
	switch(ioh->iotype)
	{
		case IO_FD: return ioh->r_read(ioh->fd, buf, count);
		case IO_HANDLE: return ioh->r_h_read(ioh->handle, buf, count);
	}
	error("Serious breakage - bad IO type in LFS wrapper!");
	return -1;
}

/* Seek callback needs protection from too big offsets. */
off_t wrap_lseek(void *handle, off_t offset, int whence)
{
	struct wrap_data *ioh = handle;
	long smalloff = offset;
	if(smalloff == offset)
	{
		switch(ioh->iotype)
		{
			case IO_FD: return ioh->r_lseek(ioh->fd, smalloff, whence);
			case IO_HANDLE: return ioh->r_h_lseek(ioh->handle, smalloff, whence);
		}
		error("Serious breakage - bad IO type in LFS wrapper!");
		return -1;
	}
	else
	{
		errno = EOVERFLOW;
		return -1;
	}
}


/*
	Now, let's replace the API dealing with replacement I/O.
	Start with undefining the renames...
*/

#undef mpg123_replace_reader
#undef mpg123_replace_reader_handle
#undef mpg123_open
#undef mpg123_open_fd
#undef mpg123_open_handle


/* Normal reader replacement needs fallback implementations. */
static ssize_t fallback_read(int fd, void *buf, size_t count)
{
	return read(fd, buf, count);
}

static long fallback_lseek(int fd, long offset, int whence)
{
	/* Since the offset is long int already, the returned value really should fit into a long... but whatever. */
	long newpos_long;
	off_t newpos;
	newpos = lseek(fd, offset, whence);
	newpos_long = newpos;
	if(newpos_long == newpos)
	return newpos_long;
	else
	{
		errno = EOVERFLOW;
		return -1;
	}
}

/* Reader replacement prepares the hidden handle storage for next mpg123_open_fd() or plain mpg123_open(). */
int attribute_align_arg mpg123_replace_reader(mpg123_handle *mh, ssize_t (*r_read) (int, void *, size_t), long (*r_lseek)(int, long, int) )
{
	struct wrap_data* ioh;

	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	ioh = wrap_get(mh);
	if(ioh == NULL) return MPG123_ERR;

	/* If both callbacks are NULL, switch totally to internal I/O, else just use fallback for at most half of them. */
	if(r_read == NULL && r_lseek == NULL)
	{
		/* Only the type is actually important to disable the code. */
		ioh->iotype = 0;
		ioh->fd = -1;
		ioh->r_read = NULL;
		ioh->r_lseek = NULL;
	}
	else
	{
		ioh->iotype = IO_FD;
		ioh->fd = -1; /* On next mpg123_open_fd(), this gets a value. */
		ioh->r_read = r_read != NULL ? r_read : fallback_read;
		ioh->r_lseek = r_lseek != NULL ? r_lseek : fallback_lseek;
	}

	/* The real reader replacement will happen while opening. */
	return MPG123_OK;
}

int attribute_align_arg mpg123_replace_reader_handle(mpg123_handle *mh, ssize_t (*r_read) (void*, void *, size_t), long (*r_lseek)(void*, long, int), void (*cleanup)(void*))
{
	struct wrap_data* ioh;

	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	ioh = wrap_get(mh);
	if(ioh == NULL) return MPG123_ERR;

	ioh->iotype = IO_HANDLE;
	ioh->handle = NULL;
	ioh->r_h_read = r_read;
	ioh->r_h_lseek = r_lseek;
	ioh->h_cleanup = cleanup;

	/* The real reader replacement will happen while opening. */
	return MPG123_OK;
}

/*
	The open routines always need to watch out for a prepared wrapper handle to use replaced normal I/O.
	Two cases to consider:
	1. Plain normal open using internal I/O.
	2. Client called mpg123_replace_reader() before.
	The second case needs hackery to activate the client I/O callbacks. For that, we create a custom I/O handle and use the guts of mpg123_open_fd() on it.
*/
int attribute_align_arg mpg123_open(mpg123_handle *mh, const char *path)
{
	struct wrap_data* ioh;

	if(mh == NULL) return MPG123_ERR;

	ioh = mh->wrapperdata;
	/* Mimic the use of mpg123_replace_reader() functions by lower levels...
	   IO_HANDLE is not valid here, though. Only IO_FD. */
	if(ioh != NULL && ioh->iotype == IO_FD)
	{
		int err;
		err = MPG123_LARGENAME(mpg123_replace_reader_handle)(mh, wrap_read, wrap_lseek, wrap_io_cleanup);
		if(err != MPG123_OK) return MPG123_ERR;

		/* The above call implied mpg123_close() already */
		/*
			I really need to open the file here... to be able to use the replacer handle I/O ...
			my_fd is used to indicate closing of the descriptor on cleanup.
		*/
		ioh->my_fd = compat_open(path, O_RDONLY|O_BINARY);
		if(ioh->my_fd < 0)
		{
			if(!(mh->p.flags & MPG123_QUIET)) error2("Cannot open file %s: %s", path, strerror(errno));

			mh->err = MPG123_BAD_FILE;
			return MPG123_ERR;
		}
		/* Store a copy of the descriptor where it is actually used. */
		ioh->fd = ioh->my_fd;
		/* Initiate I/O operating on my handle now. */
		err = open_stream_handle(mh, ioh);
		if(err != MPG123_OK)
		{
			wrap_io_cleanup(ioh);
			return MPG123_ERR;
		}
		/* All fine... */
		return MPG123_OK;
	}
	else return MPG123_LARGENAME(mpg123_open)(mh, path);
}

/*
	This is in fact very similar to the above:
	The open routines always need to watch out for a prepared wrapper handle to use replaced normal I/O.
	Two cases to consider:
	1. Plain normal open_fd using internal I/O.
	2. Client called mpg123_replace_reader() before.
	The second case needs hackery to activate the client I/O callbacks. For that, we create a custom I/O handle and use the guts of mpg123_open_fd() on it.
*/

int attribute_align_arg mpg123_open_fd(mpg123_handle *mh, int fd)
{
	struct wrap_data* ioh;

	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	ioh = mh->wrapperdata;
	if(ioh != NULL && ioh->iotype == IO_FD)
	{
		int err;
		err = MPG123_LARGENAME(mpg123_replace_reader_handle)(mh, wrap_read, wrap_lseek, wrap_io_cleanup);
		if(err != MPG123_OK) return MPG123_ERR;

		/* The above call implied mpg123_close() already */

		/* Store the real file descriptor inside the handle. */
		ioh->fd = fd;
		/* Initiate I/O operating on my handle now. */
		err = open_stream_handle(mh, ioh);
		if(err != MPG123_OK)
		{
			wrap_io_cleanup(ioh);
			return MPG123_ERR;
		}
		/* All fine... */
		return MPG123_OK;
	}
	else return MPG123_LARGENAME(mpg123_open_fd)(mh, fd);
}

int attribute_align_arg mpg123_open_handle(mpg123_handle *mh, void *handle)
{
	struct wrap_data* ioh;

	if(mh == NULL) return MPG123_ERR;

	mpg123_close(mh);
	ioh = mh->wrapperdata;
	if(ioh != NULL && ioh->iotype == IO_HANDLE && ioh->r_h_read != NULL)
	{
		/* Wrap the custom handle into my handle. */
		int err;
		err = MPG123_LARGENAME(mpg123_replace_reader_handle)(mh, wrap_read, wrap_lseek, wrap_io_cleanup);
		if(err != MPG123_OK) return MPG123_ERR;

		ioh->handle = handle;
		/* No extra error handling, keep behaviour of the original open_handle. */
		return open_stream_handle(mh, ioh);
	}
	else
	{
		/* This is an error ... you need to prepare the I/O before using it. */
		mh->err = MPG123_BAD_CUSTOM_IO;
		return MPG123_ERR;
	}
}

