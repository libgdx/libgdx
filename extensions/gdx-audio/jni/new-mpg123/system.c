/* This is broken/outdated code for quite some time; we should drop it or integrate into libmpg123. */
/*
	system.c: system stream decoder (standalone)

	copyright 1997-2008 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp

	grabs an audio stream inside a video/audio system stream
	This Program outputs only the first audio stream to STDOUT

	currently this is an external program. You must pipe
	your streams file to this program and the output to 
	the mpg123 player.  e.g: 
	./system < my_system_stream.mpg | mpg123 -
*/

#include <stdlib.h>
#include <stdio.h>
#include <errno.h>

#include "mpg123app.h" <<<--- nope!
#include "debug.h"

static int filept;
static int verbose = 1;

#define PACKET_START		0x000001ba
#define STREAM_END		0x000001b9
#define SYSTEM_STREAM		0x000001bb

/* the following two types are not supported */
#define AUDIO_STREAM		0x000001b8
#define VIDEO_STREAM		0x000001b9

#define PADDING_STREAM		0x000001be
#define RESERVED_STREAM		0x000001bc
#define PRIVATE_STREAM_1	0x000001bd
#define PRIVATE_STREAM_2	0x000001bf

static int system_back_frame(mpg123_handle *fr,int num);
static int system_head_read(unsigned char *hbuf,unsigned long *newhead);
static int system_head_shift(unsigned char *hbuf,unsigned long *head);
static int system_skip_bytes(int len);
static int system_read_frame_body(int size);
static long system_tell(void);

struct system_info {
	unsigned long rate;
	int num_audio;
	int num_video;
	int fixed;
	int csps;
	int audio_lock;
	int video_lock;
};

struct stream_info {
	int id;
	int id1;
	int type;
	int size;
	int scale;
};

struct packet_info {
  int scale;
  int size;
  unsigned long dts;
  unsigned long pts;
};

struct system_info sys_info;
struct stream_info str_info[64];

static int my_read(int f,char *buf,int len)
{
	int len1 = 0;
	int ret;

	while(len1 < len) {
		ret = read(f,buf+len1,len-len1);
		if(ret < 0)
			return -1;
		len1 += ret;
	}
	return len;
}

static int system_raw_read_head(int f,unsigned long *head)
{
	unsigned char buf[4];
	if(my_read(f,buf,4) != 4) {
		perror("read_head");
                return -1;
        }
	*head = (buf[0]<<24) + (buf[1]<<16) + (buf[2]<<8) + buf[3];

	if(verbose > 1)
		fprintf(stderr,"head: %08lx\n",*head);
	return 0;
}

static int system_raw_read_word(int f,int *word)
{
        unsigned char buf[2];

        if(my_read(f,buf,2) != 2) {
		perror("read_word");
                return -1;
        }
        *word = (buf[0]<<8) + buf[1];
        return 0;
}

static int system_raw_read(int f,int len,unsigned char *buf)
{
	
	if(my_read(f,buf,len) != len)
              return -1;
        return 0;
}

static int system_raw_skip(int f,int len)
{
	int ret;
	int cnt = 0;

	ret = lseek(f,len,SEEK_CUR);

	if(ret < 0 && errno == ESPIPE) {
		cnt = len;
		while(cnt) {
                        char buf[1024];
			if(cnt > 1024)
                           ret = read(f,buf,1024);
                        else
                           ret = read(f,buf,cnt);
                        if(ret < 0)
                           return -1;
                        cnt -= ret;
		}
                ret = len;
	}

        return ret;
}

static unsigned long system_raw_timer_value(unsigned char *buf)
{
	unsigned long val;

	if(!(buf[0] & 0x1) || !(buf[2] & 0x1) || !(buf[4] & 0x1)) {
		if(verbose)
			fprintf(stderr,"Warning: missing marker in time stamp!\n");
	}

	val  = (buf[0] & 0xe) << (29-1);
        val |= buf[1] << 21;
        val |= (buf[2] & 0xfe) << (14-1);
        val |= buf[3] << 7;
        val |= buf[4] >> 1;

	return val;
}

static int system_raw_read_packet_data(int fd,struct packet_info *pi)
{
    static unsigned char buf[16384];
    int len;
    int pos = 0;
    int i;

    if(system_raw_read_word(filept,&len) < 0)
        return -1;
    if(verbose > 1)
    	fprintf(stderr,"Stream video/audio len: %d\n",len);

    if(system_raw_read(fd,len,buf) < 0)
      return -1;

    for(i=0;i<16;i++,pos++) {
       if(buf[pos] != 0xff)
         break;
    }
    if(i == 16) {
       fprintf(stderr,"Ouch ... too many stuffing bytes!\n");
       return -1;
    }
    
    if( (buf[pos] & 0xc0) == 0x40 ) {
       pi->scale = (buf[pos] >> 5) & 0x1;
       pi->size  = (buf[pos] & 0x1f) << 8;
       pi->size |= buf[pos+1];
       pos += 2;
    }

    switch( buf[pos] & 0xf0) {
      case 0x00:
        if(buf[pos] != 0x0f) {
          fprintf(stderr,"Ouch ... illegal timer code!\n");
          return -1;
        }
        pos++;
        break;
      case 0x20:
        pi->pts = system_raw_timer_value(buf+pos);
        pos += 5;
        break;
      case 0x30:
        pi->pts = system_raw_timer_value(buf+pos);
        pos += 5;
        if( (buf[pos] & 0xf) != 0x10) {
          if(verbose)
          	fprintf(stderr,"DTS should start with 0x1x!\n");
	}
        pi->dts = system_raw_timer_value(buf+pos);
        pos += 5;
        break;
      default:
	if(verbose)
        	fprintf(stderr,"Ouch ... illegal timer code!\n");
        return -1;
 
    }


#if 1 
	write(1,buf+pos,len-pos);
#endif

	return 0;
}


static int system_raw_read_packet_info(int f,double *clock,unsigned long *rate)
{
	unsigned char buf[8];
	int i;

	if(my_read(f,buf,8) != 8) {
		perror("read_packet_info");
		return -1;
	}

	*clock = 0.0;
	for(i=0;i<5;i++) {
		*clock *= 256.0;
		*clock += (double) buf[4-i];
	}
	*rate = (buf[5]<<16) + (buf[6]<<8) + buf[7];
	return 0;
}

static int system_raw_read_system_header(int f,struct system_info *ssi) 
{
	int rlen,len;
	unsigned char buf[6+48*3];
	int i,cnt;

	if(system_raw_read_word(filept,&len) < 0)
		return -1;

	if(verbose > 1)
		fprintf(stderr,"system len: %d\n",len);

	rlen = len;
	if(len > 6 + 48 * 3) {
		if(verbose)
			fprintf(stderr,"Oops .. large System header!\n");
		rlen = 6+48*3;
	}
	if(my_read(f,buf,rlen) != rlen) {
		perror("raw_read_system_header");
		return -1;
	}

	if(len - rlen) {
		if(system_raw_skip(filept,len-rlen) < 0)
			return -1;
	}

	if(buf[5] != 0xff) {
		if(verbose)
			fprintf(stderr,"Warning: buf[5] !=0xff \n");
	}

	ssi->rate = (buf[0]<<16)+(buf[1]<<8)+buf[2];
	if( (ssi->rate & 0x800001) != 0x800001) {
		if(verbose)
			fprintf(stderr,"System Header Byte 0: Missing bits\n");
		return -1;
	}
	ssi->rate >>= 1;
	ssi->rate &= 0x7fffff;

	ssi->num_audio = buf[3] >> 2;
	ssi->num_video = buf[4] & 0x1f;
	ssi->fixed = buf[3] & 0x2;
	ssi->csps  = buf[3] & 0x1;
	ssi->audio_lock = buf[4] & 0x80;
	ssi->video_lock = buf[4] & 0x40;

	if(verbose)
		fprintf(stderr,"Audio: %d Video: %d, Lock: %d/%d, fixed: %d, csps: %d\n",
	ssi->num_audio,ssi->num_video,ssi->audio_lock?1:0,ssi->video_lock?1:0,
	ssi->fixed?1:0,ssi->csps?1:0);

	i = 6;
	cnt = 0;
	while( i < rlen ) {
		if( !(buf[i] & 0x80) || ((buf[i+1] & 0xc0) != 0xc0) ) {
			fprintf(stderr,"system_raw_read_system_header byte %d,%d: bits not set!\n",i,i+1);
			return -1;
		}
		str_info[cnt].id = buf[i];
		if( (str_info[cnt].id & 0xe0) == 0xc0 ) {
			str_info[cnt].type = 'A';
			str_info[cnt].id1 = str_info[cnt].id & 0x1f;
		}
		else if((str_info[cnt].id & 0xf0) == 0xe0 ) {
			str_info[cnt].type = 'V';
			str_info[cnt].id1 = str_info[cnt].id & 0x0f;
		}
		else {
			str_info[cnt].type = 'R';
			str_info[cnt].id1 = str_info[cnt].id & 0x3f;
		}
		str_info[cnt].scale = buf[i+1] & 0x20;
		str_info[cnt].size = ((buf[i+1] & 0x1f)<<8)+buf[i+2];
		i += 3;

		if(verbose)
			fprintf(stderr,"ID: %#02x=%c%d, scale: %d, size %d\n",
				str_info[cnt].id,str_info[cnt].type,str_info[cnt].id1,str_info[cnt].scale?1:0,str_info[cnt].size);
	}

	return 0;
}

/***************************************************
 * init system layer read functions 
 */
int system_init(struct reader *r)
{
	unsigned long head;
	double clk;
	unsigned long rate;
	int len;
	int err;

	r->back_frame = NULL;
	r->head_read = system_head_read;
	r->head_shift = system_head_shift;
	r->skip_bytes = system_skip_bytes;
	r->read_frame_body = system_read_frame_body;
	r->tell = system_tell;

	if(system_raw_read_head(filept,&head) < 0)
		return -1;
	if(head != PACKET_START) {
		fprintf(stderr,"No PACKET_START found!\n");
		return -1;
	}
	if(system_raw_read_packet_info(filept,&clk,&rate) < 0)
		return -1;

	err = 0;
	while(err == 0) {
		if(system_raw_read_head(filept,&head) < 0)
			return -1;
		if((head & 0xffffff00) != 0x00000100)
			return -1;
	
		switch(head) {
			case PACKET_START:
				if(system_raw_read_packet_info(filept,&clk,&rate))
					return -1;
				if(verbose > 1)
					fprintf(stderr,"Packet Start\n");
				break;
			case STREAM_END:
				if(verbose)
					fprintf(stderr,"Stream End\n");
				break;
			case SYSTEM_STREAM:
				if(system_raw_read_system_header(filept,&sys_info) < 0)
					return -1; 
				break;
#if 0
			case AUDIO_STREAM:
				if(system_raw_read_word(filept,&len) < 0)
					return -1;
				if(verbose > 1)
					fprintf(stderr,"STD audio len: %d\n",len);
				if(system_raw_skip(filept,len) < 0)
					return -1;
				break;
			case VIDEO_STREAM:
				if(system_raw_read_word(filept,&len) < 0)
					return -1;
				if(verbose > 1)
					fprintf(stderr,"STD video len: %d\n",len);
				if(system_raw_skip(filept,len) < 0)
					return -1;
				break;
#endif
			default:
				if(head >= 0x000001c0 && head < 0x000001f0) {

					if(verbose > 1)
						fprintf(stderr,"Stream ID %ld\n",head - 0x000001c0);

					if( (head - 0x000001c0) == 0x0) {
						struct packet_info pi;
						if(system_raw_read_packet_data(filept,&pi) < 0 )
							return -1;
					}
					else {
						if(system_raw_read_word(filept,&len) < 0)
							return -1;

						if(system_raw_skip(filept,len) < 0)
							return -1;
					}
				
					break;
				}
				else if(head >= 0x000001bd && head < 0x000001c0) {
					if(system_raw_read_word(filept,&len) < 0)
						return -1;
					if(system_raw_skip(filept,len) < 0)
						return -1;
					break;
				}
				else {
					if(verbose)
						fprintf(stderr,"unsupported head %8lx\n",head);
					if(system_raw_read_word(filept,&len) < 0)
						return -1;
					if(verbose)
						fprintf(stderr,"Skipping: %d bytes\n",len);
					if(system_raw_skip(filept,len) < 0)
						return -1;
					break;
				}
				err = 1;
				break;
		}
	}

	return 0;
}

static int system_back_frame(mpg123_handle *fr,int num)
{
	return 0;
}

static int system_head_read(unsigned char *hbuf,unsigned long *newhead)
{
	return 0;

}

static int system_head_shift(unsigned char *hbuf,unsigned long *head)
{
	return 0;
}

static int system_skip_bytes(int len)
{
	return 0;
}

static int system_read_frame_body(int size)
{
	return 0;
}

static long system_tell(void)
{
	return 0;
}

struct reader rd1;

void main(void)
{
	int ret;
	filept = 0;
	ret = system_init(&rd1);
	fprintf(stderr,"ret: %d\n",ret);
	return ret;
}

