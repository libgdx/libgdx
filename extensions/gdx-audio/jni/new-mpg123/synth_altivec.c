/*
	decode.c: decoding samples...

	copyright 1995-2009 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
	altivec optimization by tmkk
*/

#include "mpg123lib_intern.h"

#ifndef __APPLE__
#include <altivec.h>
#endif

/* A macro for normal synth functions */
#define SYNTH_ALTIVEC(B0STEP) \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0); \
	 \
	vsum = vec_madd(v1,v6,vzero); \
	vsum = vec_madd(v2,v7,vsum); \
	vsum = vec_madd(v3,v8,vsum); \
	vsum = vec_madd(v4,v9,vsum); \
	 \
	window += 32; \
	b0 += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0); \
	 \
	vsum2 = vec_madd(v1,v6,vzero); \
	vsum2 = vec_madd(v2,v7,vsum2); \
	vsum2 = vec_madd(v3,v8,vsum2); \
	vsum2 = vec_madd(v4,v9,vsum2); \
	 \
	window += 32; \
	b0 += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0); \
	 \
	vsum3 = vec_madd(v1,v6,vzero); \
	vsum3 = vec_madd(v2,v7,vsum3); \
	vsum3 = vec_madd(v3,v8,vsum3); \
	vsum3 = vec_madd(v4,v9,vsum3); \
	 \
	window += 32; \
	b0 += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0); \
	 \
	vsum4 = vec_madd(v1,v6,vzero); \
	vsum4 = vec_madd(v2,v7,vsum4); \
	vsum4 = vec_madd(v3,v8,vsum4); \
	vsum4 = vec_madd(v4,v9,vsum4); \
	 \
	window += 32; \
	b0 += B0STEP; \
	 \
	v1 = vec_mergeh(vsum,vsum3); \
	v2 = vec_mergeh(vsum2,vsum4); \
	v3 = vec_mergel(vsum,vsum3); \
	v4 = vec_mergel(vsum2,vsum4); \
	v5 = vec_mergeh(v1,v2); \
	v6 = vec_mergel(v1,v2); \
	v7 = vec_mergeh(v3,v4); \
	v8 = vec_mergel(v3,v4);

/* A macro for stereo synth functions */
#define SYNTH_STEREO_ALTIVEC(B0STEP) \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0l); \
	v10 = vec_ld(0,b0r); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0l); \
	v11 = vec_ld(16,b0r); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0l); \
	v12 = vec_ld(32,b0r); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0l); \
	v13 = vec_ld(48,b0r); \
	 \
	vsum = vec_madd(v1,v6,vzero); \
	vsum5 = vec_madd(v1,v10,vzero); \
	vsum = vec_madd(v2,v7,vsum); \
	vsum5 = vec_madd(v2,v11,vsum5); \
	vsum = vec_madd(v3,v8,vsum); \
	vsum5 = vec_madd(v3,v12,vsum5); \
	vsum = vec_madd(v4,v9,vsum); \
	vsum5 = vec_madd(v4,v13,vsum5); \
	 \
	window += 32; \
	b0l += B0STEP; \
	b0r += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0l); \
	v10 = vec_ld(0,b0r); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0l); \
	v11 = vec_ld(16,b0r); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0l); \
	v12 = vec_ld(32,b0r); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0l); \
	v13 = vec_ld(48,b0r); \
	 \
	vsum2 = vec_madd(v1,v6,vzero); \
	vsum6 = vec_madd(v1,v10,vzero); \
	vsum2 = vec_madd(v2,v7,vsum2); \
	vsum6 = vec_madd(v2,v11,vsum6); \
	vsum2 = vec_madd(v3,v8,vsum2); \
	vsum6 = vec_madd(v3,v12,vsum6); \
	vsum2 = vec_madd(v4,v9,vsum2); \
	vsum6 = vec_madd(v4,v13,vsum6); \
	 \
	window += 32; \
	b0l += B0STEP; \
	b0r += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0l); \
	v10 = vec_ld(0,b0r); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0l); \
	v11 = vec_ld(16,b0r); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0l); \
	v12 = vec_ld(32,b0r); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0l); \
	v13 = vec_ld(48,b0r); \
	 \
	vsum3 = vec_madd(v1,v6,vzero); \
	vsum7 = vec_madd(v1,v10,vzero); \
	vsum3 = vec_madd(v2,v7,vsum3); \
	vsum7 = vec_madd(v2,v11,vsum7); \
	vsum3 = vec_madd(v3,v8,vsum3); \
	vsum7 = vec_madd(v3,v12,vsum7); \
	vsum3 = vec_madd(v4,v9,vsum3); \
	vsum7 = vec_madd(v4,v13,vsum7); \
	 \
	window += 32; \
	b0l += B0STEP; \
	b0r += B0STEP; \
	 \
	v1 = vec_ld(0,window); \
	v2 = vec_ld(16,window); \
	v3 = vec_ld(32,window); \
	v4 = vec_ld(48,window); \
	v5 = vec_ld(64,window); \
	v1 = vec_perm(v1,v2,vperm1); \
	v6 = vec_ld(0,b0l); \
	v10 = vec_ld(0,b0r); \
	v2 = vec_perm(v2,v3,vperm1); \
	v7 = vec_ld(16,b0l); \
	v11 = vec_ld(16,b0r); \
	v3 = vec_perm(v3,v4,vperm1); \
	v8 = vec_ld(32,b0l); \
	v12 = vec_ld(32,b0r); \
	v4 = vec_perm(v4,v5,vperm1); \
	v9 = vec_ld(48,b0l); \
	v13 = vec_ld(48,b0r); \
	 \
	vsum4 = vec_madd(v1,v6,vzero); \
	vsum8 = vec_madd(v1,v10,vzero); \
	vsum4 = vec_madd(v2,v7,vsum4); \
	vsum8 = vec_madd(v2,v11,vsum8); \
	vsum4 = vec_madd(v3,v8,vsum4); \
	vsum8 = vec_madd(v3,v12,vsum8); \
	vsum4 = vec_madd(v4,v9,vsum4); \
	vsum8 = vec_madd(v4,v13,vsum8); \
	 \
	window += 32; \
	b0l += B0STEP; \
	b0r += B0STEP; \
	 \
	v1 = vec_mergeh(vsum,vsum3); \
	v5 = vec_mergeh(vsum5,vsum7); \
	v2 = vec_mergeh(vsum2,vsum4); \
	v6 = vec_mergeh(vsum6,vsum8); \
	v3 = vec_mergel(vsum,vsum3); \
	v7 = vec_mergel(vsum5,vsum7); \
	v4 = vec_mergel(vsum2,vsum4); \
	v8 = vec_mergel(vsum6,vsum8); \
	vsum = vec_mergeh(v1,v2); \
	vsum5 = vec_mergeh(v5,v6); \
	vsum2 = vec_mergel(v1,v2); \
	vsum6 = vec_mergel(v5,v6); \
	vsum3 = vec_mergeh(v3,v4); \
	vsum7 = vec_mergeh(v7,v8); \
	vsum4 = vec_mergel(v3,v4); \
	vsum8 = vec_mergel(v7,v8);

int synth_1to1_altivec(real *bandPtr,int channel,mpg123_handle *fr, int final)
{
	short *samples = (short *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0, **buf;
	int clip; 
	int bo1;
	
	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);
	
	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}
	
	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_altivec(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_altivec(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		ALIGNED(16) int clip_tmp[4];
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9;
		vector unsigned char vperm1,vperm2,vperm3,vperm4;
		vector float vsum,vsum2,vsum3,vsum4,vmin,vmax,vzero;
		vector signed int vclip;
		vector signed short vsample1,vsample2;
		vector unsigned int vshift;
		vclip = vec_xor(vclip,vclip);
		vzero = vec_xor(vzero,vzero);
		vshift = vec_splat_u32(-1); /* 31 */
#ifdef __APPLE__
		vmax = (vector float)(32767.0f);
		vmin = (vector float)(-32768.0f);
		vperm4 = (vector unsigned char)(0,1,18,19,2,3,22,23,4,5,26,27,6,7,30,31);
#else
		vmax = (vector float){32767.0f,32767.0f,32767.0f,32767.0f};
		vmin = (vector float){-32768.0f,-32768.0f,-32768.0f,-32768.0f};
		vperm4 = (vector unsigned char){0,1,18,19,2,3,22,23,4,5,26,27,6,7,30,31};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsl(0,samples);
		vperm3 = vec_lvsr(0,samples);
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(16);
			
			vsum = vec_sub(v5,v6);
			v9 = vec_sub(v7,v8);
			vsum = vec_add(vsum,v9);
			
			v3 = vec_round(vsum);
			v3 = (vector float)vec_cts(v3,0);
			v1 = (vector float)vec_cmpgt(vsum,vmax);
			v2 = (vector float)vec_cmplt(vsum,vmin);
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(15,samples);
			v3 = (vector float)vec_packs((vector signed int)v3,(vector signed int)v3);
			v4 = (vector float)vec_perm(vsample1,vsample2,vperm2);
			v5 = (vector float)vec_perm(v3,v4,vperm4);
			v6 = (vector float)vec_perm(vsample2,vsample1,vperm2);
			v7 = (vector float)vec_perm(v5,v6,vperm3);
			v8 = (vector float)vec_perm(v6,v5,vperm3);
			vec_st((vector signed short)v7,15,samples);
			vec_st((vector signed short)v8,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v1, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v2, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			vclip = vec_sums((vector signed int)v1,vclip);
		}
		
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(-16);
			
			vsum = vec_add(v5,v6);
			v9 = vec_add(v7,v8);
			vsum = vec_add(vsum,v9);
			
			v3 = vec_round(vsum);
			v3 = (vector float)vec_cts(v3,0);
			v1 = (vector float)vec_cmpgt(vsum,vmax);
			v2 = (vector float)vec_cmplt(vsum,vmin);
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(15,samples);
			v3 = (vector float)vec_packs((vector signed int)v3,(vector signed int)v3);
			v4 = (vector float)vec_perm(vsample1,vsample2,vperm2);
			v5 = (vector float)vec_perm(v3,v4,vperm4);
			v6 = (vector float)vec_perm(vsample2,vsample1,vperm2);
			v7 = (vector float)vec_perm(v5,v6,vperm3);
			v8 = (vector float)vec_perm(v6,v5,vperm3);
			vec_st((vector signed short)v7,15,samples);
			vec_st((vector signed short)v8,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v1, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v2, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			vclip = vec_sums((vector signed int)v1,vclip);
		}

		vec_st(vclip,0,clip_tmp);
		clip = clip_tmp[3];
	}
	if(final) fr->buffer.fill += 128;
	
	return clip;
}

int synth_1to1_stereo_altivec(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	short *samples = (short *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0l, *b0r, **bufl, **bufr;
	int clip; 
	int bo1;
	
	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}
	
	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];
	
	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_altivec(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_altivec(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_altivec(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_altivec(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		ALIGNED(16) int clip_tmp[4];
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13;
		vector unsigned char vperm1,vperm2;
		vector float vsum,vsum2,vsum3,vsum4,vsum5,vsum6,vsum7,vsum8,vmin,vmax,vzero;
		vector signed int vclip;
		vector unsigned int vshift;
		vector signed short vprev;
		vclip = vec_xor(vclip,vclip);
		vzero = vec_xor(vzero,vzero);
		vshift = vec_splat_u32(-1); /* 31 */
#ifdef __APPLE__
		vmax = (vector float)(32767.0f);
		vmin = (vector float)(-32768.0f);
#else
		vmax = (vector float){32767.0f,32767.0f,32767.0f,32767.0f};
		vmin = (vector float){-32768.0f,-32768.0f,-32768.0f,-32768.0f};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsr(0,samples);
		vprev = vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(16);
			
			vsum = vec_sub(vsum,vsum2);
			vsum2 = vec_sub(vsum5,vsum6);
			vsum3 = vec_sub(vsum3,vsum4);
			vsum4 = vec_sub(vsum7,vsum8);
			vsum = vec_add(vsum,vsum3);
			vsum2 = vec_add(vsum2,vsum4);
			
			v1 = vec_round(vsum);
			v2 = vec_round(vsum2);
			v1 = (vector float)vec_cts(v1,0);
			v2 = (vector float)vec_cts(v2,0);
			v3 = vec_mergeh(v1, v2);
			v4 = vec_mergel(v1, v2);
			v5 = (vector float)vec_packs((vector signed int)v3,(vector signed int)v4);
			v6 = (vector float)vec_perm(vprev,(vector signed short)v5,vperm2);
			vprev = (vector signed short)v5;
			v1 = (vector float)vec_cmpgt(vsum,vmax);
			v2 = (vector float)vec_cmplt(vsum,vmin);
			v3 = (vector float)vec_cmpgt(vsum2,vmax);
			v4 = (vector float)vec_cmplt(vsum2,vmin);
			vec_st((vector signed short)v6,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v1, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v2, vshift);
			v3 = (vector float)vec_sr((vector unsigned int)v3, vshift);
			v4 = (vector float)vec_sr((vector unsigned int)v4, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			v2 = (vector float)vec_add((vector unsigned int)v3,(vector unsigned int)v4);
			vclip = vec_sums((vector signed int)v1,vclip);
			vclip = vec_sums((vector signed int)v2,vclip);
		}
		
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(-16);
			
			vsum = vec_add(vsum,vsum2);
			vsum2 = vec_add(vsum5,vsum6);
			vsum3 = vec_add(vsum3,vsum4);
			vsum4 = vec_add(vsum7,vsum8);
			vsum = vec_add(vsum,vsum3);
			vsum2 = vec_add(vsum2,vsum4);
			
			v1 = vec_round(vsum);
			v2 = vec_round(vsum2);
			v1 = (vector float)vec_cts(v1,0);
			v2 = (vector float)vec_cts(v2,0);
			v3 = vec_mergeh(v1, v2);
			v4 = vec_mergel(v1, v2);
			v5 = (vector float)vec_packs((vector signed int)v3,(vector signed int)v4);
			v6 = (vector float)vec_perm(vprev,(vector signed short)v5,vperm2);
			vprev = (vector signed short)v5;
			v1 = (vector float)vec_cmpgt(vsum,vmax);
			v2 = (vector float)vec_cmplt(vsum,vmin);
			v3 = (vector float)vec_cmpgt(vsum2,vmax);
			v4 = (vector float)vec_cmplt(vsum2,vmin);
			vec_st((vector signed short)v6,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v1, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v2, vshift);
			v3 = (vector float)vec_sr((vector unsigned int)v3, vshift);
			v4 = (vector float)vec_sr((vector unsigned int)v4, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			v2 = (vector float)vec_add((vector unsigned int)v3,(vector unsigned int)v4);
			vclip = vec_sums((vector signed int)v1,vclip);
			vclip = vec_sums((vector signed int)v2,vclip);
		}
		
		if((size_t)samples & 0xf)
		{
			v1 = (vector float)vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
			v2 = (vector float)vec_perm(vprev,(vector signed short)v1,vperm2);
			vec_st((vector signed short)v2,0,samples);
		}

		vec_st(vclip,0,clip_tmp);
		clip = clip_tmp[3];
	}
	fr->buffer.fill += 128;
	
	return clip;
}

int synth_1to1_real_altivec(real *bandPtr,int channel,mpg123_handle *fr, int final)
{
	real *samples = (real *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0, **buf;
	int bo1;
	
	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);
	
	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}
	
	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_altivec(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_altivec(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9;
		vector unsigned char vperm1,vperm2,vperm3,vperm4, vperm5;
		vector float vsum,vsum2,vsum3,vsum4,vscale,vzero;
		vector float vsample1,vsample2,vsample3;
		vzero = vec_xor(vzero, vzero);
#ifdef __APPLE__
		vscale = (vector float)(1.0f/32768.0f);
		vperm4 = (vector unsigned char)(0,1,2,3,20,21,22,23,4,5,6,7,28,29,30,31);
		vperm5 = (vector unsigned char)(8,9,10,11,20,21,22,23,12,13,14,15,28,29,30,31);
#else
		vscale = (vector float){1.0f/32768.0f,1.0f/32768.0f,1.0f/32768.0f,1.0f/32768.0f};
		vperm4 = (vector unsigned char){0,1,2,3,20,21,22,23,4,5,6,7,28,29,30,31};
		vperm5 = (vector unsigned char){8,9,10,11,20,21,22,23,12,13,14,15,28,29,30,31};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsl(0,samples);
		vperm3 = vec_lvsr(0,samples);
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(16);
			
			vsum = vec_sub(v5,v6);
			v9 = vec_sub(v7,v8);
			vsum = vec_add(vsum,v9);
			vsum = vec_madd(vsum, vscale, vzero);
			
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(16,samples);
			vsample3 = vec_ld(31,samples);
			v1 = vec_perm(vsample1, vsample2, vperm2);
			v2 = vec_perm(vsample2, vsample3, vperm2);
			v1 = vec_perm(vsum, v1, vperm4);
			v2 = vec_perm(vsum, v2, vperm5);
			v3 = vec_perm(vsample3, vsample2, vperm2);
			v4 = vec_perm(vsample2, vsample1, vperm2);
			v5 = vec_perm(v2, v3, vperm3);
			v6 = vec_perm(v1, v2, vperm3);
			v7 = vec_perm(v4, v1, vperm3);
			vec_st(v5,31,samples);
			vec_st(v6,16,samples);
			vec_st(v7,0,samples);
			samples += 8;
		}
		
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(-16);
			
			vsum = vec_add(v5,v6);
			v9 = vec_add(v7,v8);
			vsum = vec_add(vsum,v9);
			vsum = vec_madd(vsum, vscale, vzero);
			
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(16,samples);
			vsample3 = vec_ld(31,samples);
			v1 = vec_perm(vsample1, vsample2, vperm2);
			v2 = vec_perm(vsample2, vsample3, vperm2);
			v1 = vec_perm(vsum, v1, vperm4);
			v2 = vec_perm(vsum, v2, vperm5);
			v3 = vec_perm(vsample3, vsample2, vperm2);
			v4 = vec_perm(vsample2, vsample1, vperm2);
			v5 = vec_perm(v2, v3, vperm3);
			v6 = vec_perm(v1, v2, vperm3);
			v7 = vec_perm(v4, v1, vperm3);
			vec_st(v5,31,samples);
			vec_st(v6,16,samples);
			vec_st(v7,0,samples);
			samples += 8;
		}
	}
	if(final) fr->buffer.fill += 256;
	
	return 0;
}

int synth_1to1_real_stereo_altivec(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	real *samples = (real *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0l, *b0r, **bufl, **bufr;
	int bo1;
	
	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}
	
	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];
	
	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_altivec(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_altivec(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_altivec(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_altivec(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13;
		vector unsigned char vperm1,vperm2;
		vector float vsum,vsum2,vsum3,vsum4,vsum5,vsum6,vsum7,vsum8,vscale,vzero;
		vector float vprev;
		vzero = vec_xor(vzero,vzero);
#ifdef __APPLE__
		vscale = (vector float)(1.0f/32768.0f);
#else
		vscale = (vector float){1.0f/32768.0f,1.0f/32768.0f,1.0f/32768.0f,1.0f/32768.0f};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsr(0,samples);
		vprev = vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(16);
			
			vsum = vec_sub(vsum,vsum2);
			vsum2 = vec_sub(vsum5,vsum6);
			vsum3 = vec_sub(vsum3,vsum4);
			vsum4 = vec_sub(vsum7,vsum8);
			vsum = vec_add(vsum,vsum3);
			vsum2 = vec_add(vsum2,vsum4);
			vsum = vec_madd(vsum, vscale, vzero);
			vsum2 = vec_madd(vsum2, vscale, vzero);
			
			v1 = vec_mergeh(vsum, vsum2);
			v2 = vec_mergel(vsum, vsum2);
			v3 = vec_perm(vprev,v1,vperm2);
			v4 = vec_perm(v1,v2,vperm2);
			vprev = v2;
			vec_st(v3,0,samples);
			vec_st(v4,16,samples);
			samples += 8;
		}
		
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(-16);
			
			vsum = vec_add(vsum,vsum2);
			vsum2 = vec_add(vsum5,vsum6);
			vsum3 = vec_add(vsum3,vsum4);
			vsum4 = vec_add(vsum7,vsum8);
			vsum = vec_add(vsum,vsum3);
			vsum2 = vec_add(vsum2,vsum4);
			vsum = vec_madd(vsum, vscale, vzero);
			vsum2 = vec_madd(vsum2, vscale, vzero);
			
			v1 = vec_mergeh(vsum, vsum2);
			v2 = vec_mergel(vsum, vsum2);
			v3 = vec_perm(vprev,v1,vperm2);
			v4 = vec_perm(v1,v2,vperm2);
			vprev = v2;
			vec_st(v3,0,samples);
			vec_st(v4,16,samples);
			samples += 8;
		}
		
		if((size_t)samples & 0xf)
		{
			v1 = (vector float)vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
			v2 = (vector float)vec_perm(vprev,v1,vperm2);
			vec_st(v2,0,samples);
		}
	}
	fr->buffer.fill += 256;
	
	return 0;
}

int synth_1to1_s32_altivec(real *bandPtr,int channel,mpg123_handle *fr, int final)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0, **buf;
	int clip;
	int bo1;
	
	if(fr->have_eq_settings) do_equalizer(bandPtr,channel,fr->equalizer);
	
	if(!channel)
	{
		fr->bo--;
		fr->bo &= 0xf;
		buf = fr->real_buffs[0];
	}
	else
	{
		samples++;
		buf = fr->real_buffs[1];
	}
	
	if(fr->bo & 0x1)
	{
		b0 = buf[0];
		bo1 = fr->bo;
		dct64_altivec(buf[1]+((fr->bo+1)&0xf),buf[0]+fr->bo,bandPtr);
	}
	else
	{
		b0 = buf[1];
		bo1 = fr->bo+1;
		dct64_altivec(buf[0]+fr->bo,buf[1]+fr->bo+1,bandPtr);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		ALIGNED(16) int clip_tmp[4];
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9;
		vector unsigned char vperm1,vperm2,vperm3,vperm4,vperm5;
		vector float vsum,vsum2,vsum3,vsum4,vmax,vmin,vzero;
		vector signed int vsample1,vsample2,vsample3;
		vector unsigned int vshift;
		vector signed int vclip;
		vzero = vec_xor(vzero, vzero);
		vclip = vec_xor(vclip, vclip);
		vshift = vec_splat_u32(-1); /* 31 */
#ifdef __APPLE__
		vmax = (vector float)(32767.999f);
		vmin = (vector float)(-32768.0f);
		vperm4 = (vector unsigned char)(0,1,2,3,20,21,22,23,4,5,6,7,28,29,30,31);
		vperm5 = (vector unsigned char)(8,9,10,11,20,21,22,23,12,13,14,15,28,29,30,31);
#else
		vmax = (vector float){32767.999f,32767.999f,32767.999f,32767.999f};
		vmin = (vector float){-32768.0f,-32768.0f,-32768.0f,-32768.0f};
		vperm4 = (vector unsigned char){0,1,2,3,20,21,22,23,4,5,6,7,28,29,30,31};
		vperm5 = (vector unsigned char){8,9,10,11,20,21,22,23,12,13,14,15,28,29,30,31};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsl(0,samples);
		vperm3 = vec_lvsr(0,samples);
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(16);
			
			vsum = vec_sub(v5,v6);
			v9 = vec_sub(v7,v8);
			v1 = vec_add(vsum,v9);
			vsum = (vector float)vec_cts(v1,16);
			v8 = (vector float)vec_cmpgt(v1,vmax);
			v9 = (vector float)vec_cmplt(v1,vmin);
			
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(16,samples);
			vsample3 = vec_ld(31,samples);
			v1 = (vector float)vec_perm(vsample1, vsample2, vperm2);
			v2 = (vector float)vec_perm(vsample2, vsample3, vperm2);
			v1 = vec_perm(vsum, v1, vperm4);
			v2 = vec_perm(vsum, v2, vperm5);
			v3 = (vector float)vec_perm(vsample3, vsample2, vperm2);
			v4 = (vector float)vec_perm(vsample2, vsample1, vperm2);
			v5 = vec_perm(v2, v3, vperm3);
			v6 = vec_perm(v1, v2, vperm3);
			v7 = vec_perm(v4, v1, vperm3);
			vec_st((vector signed int)v5,31,samples);
			vec_st((vector signed int)v6,16,samples);
			vec_st((vector signed int)v7,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v8, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v9, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			vclip = vec_sums((vector signed int)v1,vclip);
		}
		
		for (j=4;j;j--)
		{
			SYNTH_ALTIVEC(-16);
			
			vsum = vec_add(v5,v6);
			v9 = vec_add(v7,v8);
			v1 = vec_add(vsum,v9);
			vsum = (vector float)vec_cts(v1,16);
			v8 = (vector float)vec_cmpgt(v1,vmax);
			v9 = (vector float)vec_cmplt(v1,vmin);
			
			vsample1 = vec_ld(0,samples);
			vsample2 = vec_ld(16,samples);
			vsample3 = vec_ld(31,samples);
			v1 = (vector float)vec_perm(vsample1, vsample2, vperm2);
			v2 = (vector float)vec_perm(vsample2, vsample3, vperm2);
			v1 = vec_perm(vsum, v1, vperm4);
			v2 = vec_perm(vsum, v2, vperm5);
			v3 = (vector float)vec_perm(vsample3, vsample2, vperm2);
			v4 = (vector float)vec_perm(vsample2, vsample1, vperm2);
			v5 = vec_perm(v2, v3, vperm3);
			v6 = vec_perm(v1, v2, vperm3);
			v7 = vec_perm(v4, v1, vperm3);
			vec_st((vector signed int)v5,31,samples);
			vec_st((vector signed int)v6,16,samples);
			vec_st((vector signed int)v7,0,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v8, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v9, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			vclip = vec_sums((vector signed int)v1,vclip);
		}
		
		vec_st(vclip,0,clip_tmp);
		clip = clip_tmp[3];
	}
	if(final) fr->buffer.fill += 256;
	
	return clip;
}


int synth_1to1_s32_stereo_altivec(real *bandPtr_l, real *bandPtr_r, mpg123_handle *fr)
{
	int32_t *samples = (int32_t *) (fr->buffer.data+fr->buffer.fill);
	
	real *b0l, *b0r, **bufl, **bufr;
	int clip;
	int bo1;
	
	if(fr->have_eq_settings)
	{
		do_equalizer(bandPtr_l,0,fr->equalizer);
		do_equalizer(bandPtr_r,1,fr->equalizer);
	}
	
	fr->bo--;
	fr->bo &= 0xf;
	bufl = fr->real_buffs[0];
	bufr = fr->real_buffs[1];
	
	if(fr->bo & 0x1)
	{
		b0l = bufl[0];
		b0r = bufr[0];
		bo1 = fr->bo;
		dct64_altivec(bufl[1]+((fr->bo+1)&0xf),bufl[0]+fr->bo,bandPtr_l);
		dct64_altivec(bufr[1]+((fr->bo+1)&0xf),bufr[0]+fr->bo,bandPtr_r);
	}
	else
	{
		b0l = bufl[1];
		b0r = bufr[1];
		bo1 = fr->bo+1;
		dct64_altivec(bufl[0]+fr->bo,bufl[1]+fr->bo+1,bandPtr_l);
		dct64_altivec(bufr[0]+fr->bo,bufr[1]+fr->bo+1,bandPtr_r);
	}
	
	
	{
		register int j;
		real *window = fr->decwin + 16 - bo1;
		
		ALIGNED(16) int clip_tmp[4];
		vector float v1,v2,v3,v4,v5,v6,v7,v8,v9,v10,v11,v12,v13;
		vector unsigned char vperm1,vperm2;
		vector float vsum,vsum2,vsum3,vsum4,vsum5,vsum6,vsum7,vsum8,vmax,vmin,vzero;
		vector float vprev;
		vector unsigned int vshift;
		vector signed int vclip;
		vzero = vec_xor(vzero, vzero);
		vclip = vec_xor(vclip, vclip);
		vshift = vec_splat_u32(-1); /* 31 */
#ifdef __APPLE__
		vmax = (vector float)(32767.999f);
		vmin = (vector float)(-32768.0f);
#else
		vmax = (vector float){32767.999f,32767.999f,32767.999f,32767.999f};
		vmin = (vector float){-32768.0f,-32768.0f,-32768.0f,-32768.0f};
#endif
		
		vperm1 = vec_lvsl(0,window);
		vperm2 = vec_lvsr(0,samples);
		vprev = (vector float)vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(16);
			
			vsum = vec_sub(vsum,vsum2);
			vsum2 = vec_sub(vsum5,vsum6);
			vsum3 = vec_sub(vsum3,vsum4);
			vsum4 = vec_sub(vsum7,vsum8);
			v1 = vec_add(vsum,vsum3);
			v2 = vec_add(vsum2,vsum4);
			vsum = (vector float)vec_cts(v1,16);
			vsum2 = (vector float)vec_cts(v2,16);
			v5 = (vector float)vec_cmpgt(v1,vmax);
			v6 = (vector float)vec_cmplt(v1,vmin);
			v7 = (vector float)vec_cmpgt(v2,vmax);
			v8 = (vector float)vec_cmplt(v2,vmin);
			
			v1 = vec_mergeh(vsum, vsum2);
			v2 = vec_mergel(vsum, vsum2);
			v3 = vec_perm(vprev,v1,vperm2);
			v4 = vec_perm(v1,v2,vperm2);
			vprev = v2;
			vec_st((vector signed int)v3,0,samples);
			vec_st((vector signed int)v4,16,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v5, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v6, vshift);
			v3 = (vector float)vec_sr((vector unsigned int)v7, vshift);
			v4 = (vector float)vec_sr((vector unsigned int)v8, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			v2 = (vector float)vec_add((vector unsigned int)v3,(vector unsigned int)v4);
			vclip = vec_sums((vector signed int)v1,vclip);
			vclip = vec_sums((vector signed int)v2,vclip);
		}
		
		for (j=4;j;j--)
		{
			SYNTH_STEREO_ALTIVEC(-16);
			
			vsum = vec_add(vsum,vsum2);
			vsum2 = vec_add(vsum5,vsum6);
			vsum3 = vec_add(vsum3,vsum4);
			vsum4 = vec_add(vsum7,vsum8);
			v1 = vec_add(vsum,vsum3);
			v2 = vec_add(vsum2,vsum4);
			vsum = (vector float)vec_cts(v1,16);
			vsum2 = (vector float)vec_cts(v2,16);
			v5 = (vector float)vec_cmpgt(v1,vmax);
			v6 = (vector float)vec_cmplt(v1,vmin);
			v7 = (vector float)vec_cmpgt(v2,vmax);
			v8 = (vector float)vec_cmplt(v2,vmin);
			
			v1 = vec_mergeh(vsum, vsum2);
			v2 = vec_mergel(vsum, vsum2);
			v3 = vec_perm(vprev,v1,vperm2);
			v4 = vec_perm(v1,v2,vperm2);
			vprev = v2;
			vec_st((vector signed int)v3,0,samples);
			vec_st((vector signed int)v4,16,samples);
			samples += 8;
			
			v1 = (vector float)vec_sr((vector unsigned int)v5, vshift);
			v2 = (vector float)vec_sr((vector unsigned int)v6, vshift);
			v3 = (vector float)vec_sr((vector unsigned int)v7, vshift);
			v4 = (vector float)vec_sr((vector unsigned int)v8, vshift);
			v1 = (vector float)vec_add((vector unsigned int)v1,(vector unsigned int)v2);
			v2 = (vector float)vec_add((vector unsigned int)v3,(vector unsigned int)v4);
			vclip = vec_sums((vector signed int)v1,vclip);
			vclip = vec_sums((vector signed int)v2,vclip);
		}
		
		if((size_t)samples & 0xf)
		{
			v1 = (vector float)vec_perm(vec_ld(0,samples),vec_ld(0,samples),vec_lvsl(0,samples));
			v2 = (vector float)vec_perm(vprev,v1,vperm2);
			vec_st((vector signed int)v2,0,samples);
		}
		
		vec_st(vclip,0,clip_tmp);
		clip = clip_tmp[3];
	}
	fr->buffer.fill += 256;
	
	return clip;
}
