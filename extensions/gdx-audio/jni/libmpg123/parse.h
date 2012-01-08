/*
	parse: spawned from common; clustering around stream/frame parsing

	copyright ?-2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp & Thomas Orgis
*/

#ifndef MPG123_PARSE_H
#define MPG123_PARSE_H

#include "frame.h"

int read_frame_init(mpg123_handle* fr);
int frame_bitrate(mpg123_handle *fr);
long frame_freq(mpg123_handle *fr);
int read_frame_recover(mpg123_handle* fr); /* dead? */
int read_frame(mpg123_handle *fr);
void set_pointer(mpg123_handle *fr, long backstep);
int position_info(mpg123_handle* fr, unsigned long no, long buffsize, unsigned long* frames_left, double* current_seconds, double* seconds_left);
double compute_bpf(mpg123_handle *fr);
long time_to_frame(mpg123_handle *fr, double seconds);
int get_songlen(mpg123_handle *fr,int no);

#endif
