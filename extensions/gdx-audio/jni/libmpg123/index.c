/*
	index: frame index data structure and functions

	copyright 2007-8 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include "index.h"
#include "debug.h"

/* The next expected frame offset, one step ahead. */
static off_t fi_next(struct frame_index *fi)
{
	return (off_t)fi->fill*fi->step;
}

/* Shrink down the used index to the half.
   Be careful with size = 1 ... there's no shrinking possible there. */
static void fi_shrink(struct frame_index *fi)
{
	if(fi->fill < 2) return; /* Won't shrink below 1. */
	else
	{ /* Double the step, half the fill. Should work as well for fill%2 = 1 */
		size_t c;
		debug2("shrink index with fill %lu and step %lu", (unsigned long)fi->fill, (unsigned long)fi->step);
		fi->step *= 2;
		fi->fill /= 2;
		/* Move the data down. */
		for(c = 0; c < fi->fill; ++c)
		fi->data[c] = fi->data[2*c];
	}

	fi->next = fi_next(fi);
}

void fi_init(struct frame_index *fi)
{
	fi->data = NULL;
	fi->step = 1;
	fi->fill = 0;
	fi->size = 0;
	fi->grow_size = 0;
	fi->next = fi_next(fi);
}

void fi_exit(struct frame_index *fi)
{
	debug2("fi_exit: %p and %lu", (void*)fi->data, (unsigned long)fi->size);
	if(fi->size && fi->data != NULL) free(fi->data);

	fi_init(fi); /* Be prepared for further fun, still. */
}

int fi_resize(struct frame_index *fi, size_t newsize)
{
	off_t *newdata = NULL;
	if(newsize == fi->size) return 0;

	if(newsize > 0 && newsize < fi->size)
	{ /* When we reduce buffer size a bit, shrink stuff. */
		while(fi->fill > newsize){ fi_shrink(fi); }
	}

	newdata = safe_realloc(fi->data, newsize*sizeof(off_t));
	if(newsize == 0 || newdata != NULL)
	{
		fi->data = newdata;
		fi->size = newsize;
		if(fi->fill > fi->size) fi->fill = fi->size;

		fi->next = fi_next(fi);
		debug2("new index of size %lu at %p", (unsigned long)fi->size, (void*)fi->data);
		return 0;
	}
	else
	{
		error("failed to resize index!");
		return -1;
	}
}

void fi_add(struct frame_index *fi, off_t pos)
{
	debug3("wanting to add to fill %lu, step %lu, size %lu", (unsigned long)fi->fill, (unsigned long)fi->step, (unsigned long)fi->size);
	if(fi->fill == fi->size)
	{ /* Index is full, we need to shrink... or grow. */
		/* Store the current frame number to check later if we still want it. */
		off_t framenum = fi->fill*fi->step;
		/* If we want not / cannot grow, we shrink. */	
		if( !(fi->grow_size && fi_resize(fi, fi->size+fi->grow_size)==0) )
		fi_shrink(fi);

		/* Now check if we still want to add this frame (could be that not, because of changed step). */
		if(fi->next != framenum) return;
	}
	/* When we are here, we want that frame. */
	if(fi->fill < fi->size) /* safeguard for size=1, or just generally */
	{
		debug1("adding to index at %p", (void*)(fi->data+fi->fill));
		fi->data[fi->fill] = pos;
		++fi->fill;
		fi->next = fi_next(fi);
		debug3("added pos %li to index with fill %lu and step %lu", (long) pos, (unsigned long)fi->fill, (unsigned long)fi->step);
	}
}

int fi_set(struct frame_index *fi, off_t *offsets, off_t step, size_t fill)
{
	if(fi_resize(fi, fill) == -1) return -1;
	fi->step = step;
	if(offsets != NULL)
	{
		memcpy(fi->data, offsets, fill*sizeof(off_t));
		fi->fill = fill;
	}
	else
	{
		/* allocation only, no entries in index yet */
		fi->fill = 0;
	}
	fi->next = fi_next(fi);
	debug3("set new index of fill %lu, size %lu at %p",
	(unsigned long)fi->fill, (unsigned long)fi->size, (void*)fi->data);
	return 0;
}

void fi_reset(struct frame_index *fi)
{
	debug1("reset with size %"SIZE_P, (size_p)fi->size);
	fi->fill = 0;
	fi->step = 1;
	fi->next = fi_next(fi);
}
