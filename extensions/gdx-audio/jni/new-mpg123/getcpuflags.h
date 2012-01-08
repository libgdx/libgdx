/*
	getcpucpuflags: get cpuflags for ia32

	copyright ?-2007 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http:#mpg123.org
	initially written by KIMURA Takuhiro (for 3DNow!)
	extended for general use by Thomas Orgis
*/

#ifndef MPG123_H_GETCPUFLAGS
#define MPG123_H_GETCPUFLAGS

/* standard level flags part 1 (ECX)*/
#define FLAG_SSE3      0x00000001

/* standard level flags part 2 (EDX) */
#define FLAG2_MMX       0x00800000
#define FLAG2_SSE       0x02000000
#define FLAG2_SSE2      0x04000000
#define FLAG2_FPU       0x00000001
/* cpuid extended level 1 (AMD) */
#define XFLAG_MMX      0x00800000
#define XFLAG_3DNOW    0x80000000
#define XFLAG_3DNOWEXT 0x40000000

struct cpuflags
{
	unsigned int id;
	unsigned int std;
	unsigned int std2;
	unsigned int ext;
};

unsigned int getcpuflags(struct cpuflags* cf);

/* checks the family */
#define cpu_i586(s) ( ((s.id & 0xf00)>>8) == 0 || ((s.id & 0xf00)>>8) > 4 )
/* checking some flags... */
#define cpu_fpu(s) (FLAG2_FPU & s.std2)
#define cpu_mmx(s) (FLAG2_MMX & s.std2 || XFLAG_MMX & s.ext)
#define cpu_3dnow(s) (XFLAG_3DNOW & s.ext)
#define cpu_3dnowext(s) (XFLAG_3DNOWEXT & s.ext)
#define cpu_sse(s) (FLAG2_SSE & s.std2)
#define cpu_sse2(s) (FLAG2_SSE2 & s.std2)
#define cpu_sse3(s) (FLAG_SSE3 & s.std)

#endif
