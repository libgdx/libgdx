/*
	icy: Puny code to pretend for a serious ICY data structure.

	copyright 2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include "icy.h"

void init_icy(struct icy_meta *icy)
{
	icy->data = NULL;
}

void clear_icy(struct icy_meta *icy)
{
	if(icy->data != NULL) free(icy->data);
	init_icy(icy);
}

void reset_icy(struct icy_meta *icy)
{
	clear_icy(icy);
	init_icy(icy);
}
/*void set_icy(struct icy_meta *icy, char* new_data)
{
	if(icy->data) free(icy->data);
	icy->data = new_data;
	icy->changed = 1;
}*/
