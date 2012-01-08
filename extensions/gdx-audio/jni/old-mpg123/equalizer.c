/*
	equalizer.c: equalizer settings

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
*/


#include "mpg123lib_intern.h"

void do_equalizer(real *bandPtr,int channel, real equalizer[2][32]) 
{
	int i;
	for(i=0;i<32;i++)
	bandPtr[i] = REAL_MUL(bandPtr[i], equalizer[channel][i]);
}
