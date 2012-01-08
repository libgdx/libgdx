/*
	lfs_alias: Aliases to the small/native API functions with the size of long int as suffix.

	copyright 2010 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis

	Use case: Client code on Linux/x86-64 that defines _FILE_OFFSET_BITS to 64, which is the only choice on that platform anyway. It should be no-op, but prompts the platform-agnostic header of mpg123 to define API calls with the corresponding suffix.
	This file provides the names for this case. It's cruft, but glibc does it, too -- so people rely on it.
	Oh, and it also caters for the lunatics that define _FILE_OFFSET_BITS=32 on 32 bit platforms.

	There is also the strange case that the mpg123 build itself is configured for unnecessary _FILE_OFFSET_BITS == LFS_ALIAS_BITS =^ sizeof(long). In that case, the "native" function will have the suffix and the alias shall be provided without the suffix.

	So, two basic cases:
	1. mpg123_bla_32 alias for mpg123_bla
	2. mpg123_bla    alias for mpg123_bla_32
	Confusing, I know. It sucks.
*/

#include "config.h"

/* Hack for Solaris: Some system headers included from compat.h might force _FILE_OFFSET_BITS. Need to follow that here. */
#include "compat.h"

#ifndef LFS_ALIAS_BITS
#error "I need the count of alias bits here."
#endif

#define MACROCAT_REALLY(a, b) a ## b
#define MACROCAT(a, b) MACROCAT_REALLY(a, b)

/* This is wicked switchery: Decide which way the aliases are facing. */

#if _FILE_OFFSET_BITS+0 == LFS_ALIAS_BITS

/* The native functions are actually _with_ suffix, so let the mpg123 header use large file hackery to define the correct interfaces. */
#include "mpg123.h"
/* Don't forget to undef the function symbols before usage... */

/* The native functions have suffix, the aliases not. */
#define NATIVE_SUFFIX MACROCAT(_, _FILE_OFFSET_BITS)
#define NATIVE_NAME(func) MACROCAT(func, NATIVE_SUFFIX)
#define ALIAS_NAME(func) func

#else

/* Native functions are without suffix... */
#define MPG123_NO_LARGENAME
#include "mpg123.h"

/* The alias functions have suffix, the native ones not. */
#define ALIAS_SUFFIX MACROCAT(_, LFS_ALIAS_BITS)
#define ALIAS_NAME(func) MACROCAT(func, ALIAS_SUFFIX)
#define NATIVE_NAME(func) func

#endif

/* Now get the rest of the infrastructure on speed, namely attribute_align_arg, to stay safe. */
#include "mpg123lib_intern.h"

/*
	Extract the list of functions we need wrappers for, pregenerating the wrappers for simple cases (inline script for nedit):
perl -ne '
if(/^\s*EXPORT\s+(\S+)\s+(mpg123_\S+)\((.*)\);\s*$/)
{
	my $type = $1;
	my $name = $2;
	my $args = $3;
	next unless ($type =~ /off_t/ or $args =~ /off_t/ or ($name =~ /open/ and $name ne mpg123_open_feed));
	$type =~ s/off_t/long/g;
	my @nargs = ();
	$args =~ s/off_t/long/g;
	foreach my $a (split(/,/, $args))
	{
		$a =~ s/^.*\s\**([a-z_]+)$/$1/;
		push(@nargs, $a);
	}
	my $nargs = join(", ", @nargs);
	$nargs = "Human: figure me out." if($nargs =~ /\(/);
	print <<EOT

##ifdef $name
##undef $name
##endif
$type attribute_align_arg ALIAS_NAME($name)($args)
{
	return NATIVE_NAME($name)($nargs);
}
EOT

}' < mpg123.h.in
*/

#ifdef mpg123_open
#undef mpg123_open
#endif
int attribute_align_arg ALIAS_NAME(mpg123_open)(mpg123_handle *mh, const char *path)
{
	return NATIVE_NAME(mpg123_open)(mh, path);
}

#ifdef mpg123_open_fd
#undef mpg123_open_fd
#endif
int attribute_align_arg ALIAS_NAME(mpg123_open_fd)(mpg123_handle *mh, int fd)
{
	return NATIVE_NAME(mpg123_open_fd)(mh, fd);
}

#ifdef mpg123_open_handle
#undef mpg123_open_handle
#endif
int attribute_align_arg ALIAS_NAME(mpg123_open_handle)(mpg123_handle *mh, void *iohandle)
{
	return NATIVE_NAME(mpg123_open_handle)(mh, iohandle);
}

#ifdef mpg123_decode_frame
#undef mpg123_decode_frame
#endif
int attribute_align_arg ALIAS_NAME(mpg123_decode_frame)(mpg123_handle *mh, long *num, unsigned char **audio, size_t *bytes)
{
	return NATIVE_NAME(mpg123_decode_frame)(mh, num, audio, bytes);
}

#ifdef mpg123_framebyframe_decode
#undef mpg123_framebyframe_decode
#endif
int attribute_align_arg ALIAS_NAME(mpg123_framebyframe_decode)(mpg123_handle *mh, long *num, unsigned char **audio, size_t *bytes)
{
	return NATIVE_NAME(mpg123_framebyframe_decode)(mh, num, audio, bytes);
}

#ifdef mpg123_framepos
#undef mpg123_framepos
#endif
long attribute_align_arg ALIAS_NAME(mpg123_framepos)(mpg123_handle *mh)
{
	return NATIVE_NAME(mpg123_framepos)(mh);
}

#ifdef mpg123_tell
#undef mpg123_tell
#endif
long attribute_align_arg ALIAS_NAME(mpg123_tell)(mpg123_handle *mh)
{
	return NATIVE_NAME(mpg123_tell)(mh);
}

#ifdef mpg123_tellframe
#undef mpg123_tellframe
#endif
long attribute_align_arg ALIAS_NAME(mpg123_tellframe)(mpg123_handle *mh)
{
	return NATIVE_NAME(mpg123_tellframe)(mh);
}

#ifdef mpg123_tell_stream
#undef mpg123_tell_stream
#endif
long attribute_align_arg ALIAS_NAME(mpg123_tell_stream)(mpg123_handle *mh)
{
	return NATIVE_NAME(mpg123_tell_stream)(mh);
}

#ifdef mpg123_seek
#undef mpg123_seek
#endif
long attribute_align_arg ALIAS_NAME(mpg123_seek)(mpg123_handle *mh, long sampleoff, int whence)
{
	return NATIVE_NAME(mpg123_seek)(mh, sampleoff, whence);
}

#ifdef mpg123_feedseek
#undef mpg123_feedseek
#endif
long attribute_align_arg ALIAS_NAME(mpg123_feedseek)(mpg123_handle *mh, long sampleoff, int whence, long *input_offset)
{
	return NATIVE_NAME(mpg123_feedseek)(mh, sampleoff, whence, input_offset);
}

#ifdef mpg123_seek_frame
#undef mpg123_seek_frame
#endif
long attribute_align_arg ALIAS_NAME(mpg123_seek_frame)(mpg123_handle *mh, long frameoff, int whence)
{
	return NATIVE_NAME(mpg123_seek_frame)(mh, frameoff, whence);
}

#ifdef mpg123_timeframe
#undef mpg123_timeframe
#endif
long attribute_align_arg ALIAS_NAME(mpg123_timeframe)(mpg123_handle *mh, double sec)
{
	return NATIVE_NAME(mpg123_timeframe)(mh, sec);
}

#ifdef mpg123_index
#undef mpg123_index
#endif
int attribute_align_arg ALIAS_NAME(mpg123_index)(mpg123_handle *mh, long **offsets, long *step, size_t *fill)
{
	return NATIVE_NAME(mpg123_index)(mh, offsets, step, fill);
}

#ifdef mpg123_set_index
#undef mpg123_set_index
#endif
int attribute_align_arg ALIAS_NAME(mpg123_set_index)(mpg123_handle *mh, long *offsets, long step, size_t fill)
{
	return NATIVE_NAME(mpg123_set_index)(mh, offsets, step, fill);
}

#ifdef mpg123_position
#undef mpg123_position
#endif
int attribute_align_arg ALIAS_NAME(mpg123_position)( mpg123_handle *mh, long frame_offset, long buffered_bytes, long *current_frame, long *frames_left, double *current_seconds, double *seconds_left)
{
	return NATIVE_NAME(mpg123_position)(mh, frame_offset, buffered_bytes, current_frame, frames_left, current_seconds, seconds_left);
}

#ifdef mpg123_length
#undef mpg123_length
#endif
long attribute_align_arg ALIAS_NAME(mpg123_length)(mpg123_handle *mh)
{
	return NATIVE_NAME(mpg123_length)(mh);
}

#ifdef mpg123_set_filesize
#undef mpg123_set_filesize
#endif
int attribute_align_arg ALIAS_NAME(mpg123_set_filesize)(mpg123_handle *mh, long size)
{
	return NATIVE_NAME(mpg123_set_filesize)(mh, size);
}

#ifdef mpg123_replace_reader
#undef mpg123_replace_reader
#endif
int attribute_align_arg ALIAS_NAME(mpg123_replace_reader)(mpg123_handle *mh, ssize_t (*r_read) (int, void *, size_t), long (*r_lseek)(int, long, int))
{
	return NATIVE_NAME(mpg123_replace_reader)(mh, r_read, r_lseek);
}

#ifdef mpg123_replace_reader_handle
#undef mpg123_replace_reader_handle
#endif
int attribute_align_arg ALIAS_NAME(mpg123_replace_reader_handle)(mpg123_handle *mh, ssize_t (*r_read) (void *, void *, size_t), long (*r_lseek)(void *, long, int), void (*cleanup)(void*))
{
	return NATIVE_NAME(mpg123_replace_reader_handle)(mh, r_read, r_lseek, cleanup);
}
