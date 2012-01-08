/*
	id3: ID3v2.3 and ID3v2.4 parsing (a relevant subset)

	copyright 2006-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include "mpg123lib_intern.h"
#include "id3.h"

/*
	trying to parse ID3v2.3 and ID3v2.4 tags...

	returns:  0: bad or just unparseable tag
	          1: good, (possibly) new tag info
	         <0: reader error (may need more data feed, try again)
*/
int parse_new_id3(mpg123_handle *fr, unsigned long first4bytes) {
	#define UNSYNC_FLAG 128
	#define EXTHEAD_FLAG 64
	#define EXP_FLAG 32
	#define FOOTER_FLAG 16
	#define UNKNOWN_FLAGS 15 /* 00001111*/
	unsigned char buf[6];
	unsigned long length=0;
	unsigned char flags = 0;
	int ret = 1;
	int ret2;
	unsigned char major = first4bytes & 0xff;

	if(major == 0xff) return 0; /* Invalid... */
	if((ret2 = fr->rd->read_frame_body(fr, buf, 6)) < 0) /* read more header information */
	return ret2;

	if(buf[0] == 0xff) return 0; /* Revision, will never be 0xff. */

	/* second new byte are some nice flags, if these are invalid skip the whole thing */
	flags = buf[1];
	/* use 4 bytes from buf to construct 28bit uint value and return 1; return 0 if bytes are not synchsafe */
	#define synchsafe_to_long(buf,res) \
	( \
		(((buf)[0]|(buf)[1]|(buf)[2]|(buf)[3]) & 0x80) ? 0 : \
		(res =  (((unsigned long) (buf)[0]) << 21) \
		     | (((unsigned long) (buf)[1]) << 14) \
		     | (((unsigned long) (buf)[2]) << 7) \
		     |  ((unsigned long) (buf)[3]) \
		,1) \
	)
	/* id3v2.3 does not store synchsafe frame sizes, but synchsafe tag size - doh! */
	#define bytes_to_long(buf,res) \
	( \
		major == 3 ? \
		(res =  (((unsigned long) (buf)[0]) << 24) \
		     | (((unsigned long) (buf)[1]) << 16) \
		     | (((unsigned long) (buf)[2]) << 8) \
		     |  ((unsigned long) (buf)[3]) \
		,1) : synchsafe_to_long(buf,res) \
	)
	/* for id3v2.2 only */
	#define threebytes_to_long(buf,res) \
	( \
		res =  (((unsigned long) (buf)[0]) << 16) \
		     | (((unsigned long) (buf)[1]) << 8) \
		     |  ((unsigned long) (buf)[2]) \
		,1 \
	)

	/* length-10 or length-20 (footer present); 4 synchsafe integers == 28 bit number  */
	/* we have already read 10 bytes, so left are length or length+10 bytes belonging to tag */
	if(!synchsafe_to_long(buf+2,length)) return 0;
	if((ret2 = fr->rd->skip_bytes(fr,length)) < 0) /* will not store data in backbuff! */
		ret = ret2;
	/* skip footer if present */
	if((ret > 0) && (flags & FOOTER_FLAG) && ((ret2 = fr->rd->skip_bytes(fr,length)) < 0)) ret = ret2;

	return ret;
	#undef UNSYNC_FLAG
	#undef EXTHEAD_FLAG
	#undef EXP_FLAG
	#undef FOOTER_FLAG
	#undef UNKOWN_FLAGS
}

