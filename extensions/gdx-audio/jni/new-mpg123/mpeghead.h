/*
	mpeghead: the bits of an MPEG frame header

	copyright ?-2011 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp & Thomas Orgis (from parse.c)
*/
#ifndef MPG123_MPEGHEAD_H
#define MPG123_MPEGHEAD_H

/*
	Avoid human error, let perl do the work of dissecting an MPEG header into parts.
	To be clear: Never edit the following definitions by hand, modify the code block inside this comment and run it through perl instead!

	$head = "AAAAAAAA AAABBCCD EEEEFFGH IIJJKLMM";
	%parts = qw(A sync B version C layer D crc E bitrate F samplerate G padding H private I channel J chanex K copyright L original M emphasis);
	for(sort keys %parts)
	{
		$name = uc($parts{$_});
		$bits = $head;
		$bits =~ s/$_/1/g;
		$bits =~ s/[^1 ]/0/g;
		print "\/\* $bits \*\/\n";
		$bits =~ s/\s//g;
		print "#define HDR_$name".(" " x (18-length($name))).sprintf("0x%08x", eval("0b$bits"))."\n";
		$bits =~ m/(0*)$/;
		print "#define HDR_${name}_VAL(h)".(" " x (11-length($name)))."(((h)\&HDR_$name) >> ".length($1).")\n";
	}
*/

/* 11111111 11100000 00000000 00000000 */
#define HDR_SYNC              0xffe00000
#define HDR_SYNC_VAL(h)       (((h)&HDR_SYNC) >> 21)
/* 00000000 00011000 00000000 00000000 */
#define HDR_VERSION           0x00180000
#define HDR_VERSION_VAL(h)    (((h)&HDR_VERSION) >> 19)
/* 00000000 00000110 00000000 00000000 */
#define HDR_LAYER             0x00060000
#define HDR_LAYER_VAL(h)      (((h)&HDR_LAYER) >> 17)
/* 00000000 00000001 00000000 00000000 */
#define HDR_CRC               0x00010000
#define HDR_CRC_VAL(h)        (((h)&HDR_CRC) >> 16)
/* 00000000 00000000 11110000 00000000 */
#define HDR_BITRATE           0x0000f000
#define HDR_BITRATE_VAL(h)    (((h)&HDR_BITRATE) >> 12)
/* 00000000 00000000 00001100 00000000 */
#define HDR_SAMPLERATE        0x00000c00
#define HDR_SAMPLERATE_VAL(h) (((h)&HDR_SAMPLERATE) >> 10)
/* 00000000 00000000 00000010 00000000 */
#define HDR_PADDING           0x00000200
#define HDR_PADDING_VAL(h)    (((h)&HDR_PADDING) >> 9)
/* 00000000 00000000 00000001 00000000 */
#define HDR_PRIVATE           0x00000100
#define HDR_PRIVATE_VAL(h)    (((h)&HDR_PRIVATE) >> 8)
/* 00000000 00000000 00000000 11000000 */
#define HDR_CHANNEL           0x000000c0
#define HDR_CHANNEL_VAL(h)    (((h)&HDR_CHANNEL) >> 6)
/* 00000000 00000000 00000000 00110000 */
#define HDR_CHANEX            0x00000030
#define HDR_CHANEX_VAL(h)     (((h)&HDR_CHANEX) >> 4)
/* 00000000 00000000 00000000 00001000 */
#define HDR_COPYRIGHT         0x00000008
#define HDR_COPYRIGHT_VAL(h)  (((h)&HDR_COPYRIGHT) >> 3)
/* 00000000 00000000 00000000 00000100 */
#define HDR_ORIGINAL          0x00000004
#define HDR_ORIGINAL_VAL(h)   (((h)&HDR_ORIGINAL) >> 2)
/* 00000000 00000000 00000000 00000011 */
#define HDR_EMPHASIS          0x00000003
#define HDR_EMPHASIS_VAL(h)   (((h)&HDR_EMPHASIS) >> 0)

/*
	A generic mask for telling if a header is somewhat valid for the current stream.
	Meaning: Most basic info is not allowed to change.
	Think: Why do we allow changing channel setup? A change in that means another stream (segment).
*/
#define HDR_CMPMASK (HDR_SYNC|HDR_VERSION|HDR_LAYER|HDR_SAMPLERATE)

/* A stricter mask, for matching free format headers. */
#define HDR_SAMEMASK (HDR_SYNC|HDR_VERSION|HDR_LAYER|HDR_BITRATE|HDR_SAMPLERATE|HDR_CHANNEL|HDR_CHANEX)

/* Free format headers have zero bitrate value. */
#define HDR_FREE_FORMAT(head) (!(head & HDR_BITRATE))

/* A mask for changed sampling rate (version or rate bits). */
#define HDR_SAMPMASK (HDR_VERSION|HDR_SAMPLERATE)

#endif
