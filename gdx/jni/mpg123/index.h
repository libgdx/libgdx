#ifndef MPG123_H_INDEX
#define MPG123_H_INDEX

/*
	index: frame index data structure and functions

	This is for keeping track of frame positions for accurate seeking.
	Now in it's own file, with initial code from frame.c and parse.c .

	The idea of the index with a certain amount of entries is to cover
	all yet-encountered frame positions with minimal coarseness.
	Meaning: At first every frame position is recorded, then, when
	the index is full, every second position is trown away to make
	space. Next time it is full, the same happens. And so on.
	In this manner we maintain a good resolution with the given
	maximum index size while covering the whole stream.

	copyright 2007-8 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include "config.h"
#include "compat.h"

struct frame_index
{
	off_t *data; /* actual data, the frame positions */
	off_t  step; /* advancement in frame number per index point */
	off_t  next; /* frame offset supposed to come next into the index */
	size_t size; /* total number of possible entries */
	size_t fill; /* number of used entries */
	size_t grow_size; /* if > 0: index allowed to grow on need with these steps, instead of lowering resolution */
};

/* The condition for a framenum to be appended to the index. 
  if(FI_NEXT(fr->index, fr->num)) fi_add(offset); */
#define FI_NEXT(fi, framenum) ((fi).size && framenum == (fi).next)

/* Initialize stuff, set things to zero and NULL... */
void fi_init(struct frame_index *fi);
/* Deallocate/zero things. */
void fi_exit(struct frame_index *fi);

/* Prepare a given size, preserving current fill, if possible.
   If the new size is smaller than fill, the entry density is reduced.
   Return 0 on success. */
int fi_resize(struct frame_index *fi, size_t newsize);

/* Append a frame position, reducing index density if needed. */
void fi_add(struct frame_index *fi, off_t pos);

/* Empty the index (setting fill=0 and step=1), but keep current size. */
void fi_reset(struct frame_index *fi);

#endif
