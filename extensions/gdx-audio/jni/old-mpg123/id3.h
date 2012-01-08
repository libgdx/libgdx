/*
	id3: ID3v2.3 and ID3v2.4 parsing (a relevant subset)

	copyright 2006-2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#ifndef MPG123_ID3_H
#define MPG123_ID3_H

/* really need it _here_! */
#include "frame.h"

# define init_id3(fr)
# define exit_id3(fr)
# define reset_id3(fr)
# define id3_link(fr)
int parse_new_id3(mpg123_handle *fr, unsigned long first4bytes);

#endif
