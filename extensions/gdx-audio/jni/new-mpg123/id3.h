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

#ifdef NO_ID3V2
# ifdef init_id3
#  undef init_id3
# endif
# define init_id3(fr)
# ifdef exit_id3
#  undef exit_id3
# endif
# define exit_id3(fr)
# ifdef reset_id3
#  undef reset_id3
# endif
# define reset_id3(fr)
# ifdef id3_link
#  undef id3_link
# endif
# define id3_link(fr)
#else
void init_id3(mpg123_handle *fr);
void exit_id3(mpg123_handle *fr);
void reset_id3(mpg123_handle *fr);
void id3_link(mpg123_handle *fr);
#endif
int  parse_new_id3(mpg123_handle *fr, unsigned long first4bytes);
/* Convert text from some ID3 encoding to UTf-8.
   On error, sb->fill is 0. The noquiet flag enables warning/error messages. */
void id3_to_utf8(mpg123_string *sb, unsigned char encoding, const unsigned char *source, size_t source_size, int noquiet);

#endif
