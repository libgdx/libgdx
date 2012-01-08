/*
	optimize: get a grip on the different optimizations

	copyright 2006-9 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis, inspired by 3DNow stuff in mpg123.[hc]

	Currently, this file contains the struct and function to choose an optimization variant and works only when OPT_MULTI is in effect.
*/

#include "mpg123lib_intern.h" /* includes optimize.h */

#define dn_generic "generic"
#define dn_ARM "ARM"
static const char* decname[] = { "auto", dn_generic, dn_ARM, "nodec" };

const struct synth_s synth_base = {
	{ /* plain  */      synth_1to1,             synth_2to1,             synth_4to1,             synth_ntom},
	{ /* stereo */      synth_stereo_wrap,      synth_stereo_wrap,      synth_stereo_wrap,      synth_stereo_wrap},
	{ /* mono2stereo */ synth_1to1_mono2stereo, synth_2to1_mono2stereo, synth_4to1_mono2stereo, synth_ntom_mono2stereo},
	{ /* mono*/         synth_1to1_mono,        synth_2to1_mono,        synth_4to1_mono,        synth_ntom_mono}
};

static int find_synth(func_synth synth,  const func_synth synths[r_limit]){
	enum synth_resample ri;
	for(ri=0; ri<r_limit; ++ri) if(synth == synths[ri]) return TRUE;
	return FALSE;
}

/* Determine what kind of decoder is actually active
   This depends on runtime choices which may cause fallback to i386 or generic code. */
static int find_dectype(mpg123_handle *fr){
	enum optdec type = nodec;
	/* Direct and indirect usage, 1to1 stereo decoding.
	   Concentrating on the plain stereo synth should be fine, mono stuff is derived. */
	func_synth basic_synth = fr->synth;
	if (basic_synth == synth_1to1_arm) type = arm;
	else if(find_synth(basic_synth, synth_base.plain)) type = generic;

	if(type != nodec)	{
		fr->cpu_type = type;

		return MPG123_OK;
	}	else	{
		fr->err = MPG123_BAD_DECODER_SETUP;
		return MPG123_ERR;
	}
}

/* set synth functions for current frame, optimizations handled by opt_* macros */
int set_synth_functions(mpg123_handle *fr){
	enum synth_resample resample = r_none;

	switch(fr->down_sample)	{
		case 0: resample = r_1to1; break;
		case 1: resample = r_2to1; break;
		case 2: resample = r_4to1; break;
		case 3: resample = r_ntom; break;
	}
	if(resample == r_none) return -1;

	/* Finally selecting the synth functions for stereo / mono. */
	fr->synth = fr->synths.plain[resample];
	fr->synth_stereo = fr->synths.stereo[resample];
	fr->synth_mono = fr->af.channels==2
		? fr->synths.mono2stereo[resample] /* Mono MPEG file decoded to stereo. */
		: fr->synths.mono[resample];       /* Mono MPEG file decoded to mono. */

	if(find_dectype(fr) != MPG123_OK){ /* Actually determine the currently active decoder breed. */
		fr->err = MPG123_BAD_DECODER_SETUP;
		return MPG123_ERR;
	}

	if(frame_buffers(fr) != 0) {
		fr->err = MPG123_NO_BUFFERS;
		return MPG123_ERR;
	}

	init_layer3_stuff(fr, init_layer3_gainpow2);
	init_layer12_stuff(fr, init_layer12_table);
	fr->make_decode_tables = make_decode_tables;

	/* We allocated the table buffers just now, so (re)create the tables. */
	fr->make_decode_tables(fr);

	return 0;
}

int frame_cpu_opt(mpg123_handle *fr, const char* cpu){
	fr->synths = synth_base;
//FIXME: dworz !!!!!!!!!!!!!!!!!
	fr->cpu_type = arm;
	fr->synths.plain[r_1to1] = synth_1to1_arm;
//	fr->cpu_type = generic;
//	fr->synths.plain[r_1to1] = synth_1to1;
	return 1;
}

enum optdec dectype(const char* decoder){
	enum optdec dt;
	if((decoder == NULL) || (decoder[0] == 0)) return autodec;

	for(dt=autodec; dt<nodec; ++dt) if(!strcasecmp(decoder, decname[dt])) return dt;
	return nodec; /* If we found nothing... */
}

