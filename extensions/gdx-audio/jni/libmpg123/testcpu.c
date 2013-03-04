/*
	testcpu: standalone CPU flags tester

	copyright 2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Thomas Orgis
*/

#include <stdio.h>
#include "getcpuflags.h"

int main()
{
	int family;
	struct cpuflags flags;
	if(!getcpuflags(&flags)){ printf("CPU won't do cpuid (some old i386 or i486)\n"); return 0; }
	family = (flags.id & 0xf00)>>8;
	printf("family: %i\n", family);
	printf("stdcpuflags:  0x%08x\n", flags.std);
	printf("std2cpuflags: 0x%08x\n", flags.std2);
	printf("extcpuflags:  0x%08x\n", flags.ext);
	if(cpu_i586(flags))
	{
		printf("A i586 or better cpu with:");
		if(cpu_mmx(flags)) printf(" mmx");
		if(cpu_3dnow(flags)) printf(" 3dnow");
		if(cpu_3dnowext(flags)) printf(" 3dnowext");
		if(cpu_sse(flags)) printf(" sse");
		if(cpu_sse2(flags)) printf(" sse2");
		if(cpu_sse3(flags)) printf(" sse3");
		printf("\n");
	}
	else printf("I guess you have some i486\n");
	return 0;
}
