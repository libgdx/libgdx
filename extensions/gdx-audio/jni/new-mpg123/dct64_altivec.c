/*
	dct64_altivec.c: Discrete Cosine Tansform (DCT) for Altivec

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
	altivec optimization by tmkk
*/

/*
 * Discrete Cosine Tansform (DCT) for subband synthesis
 *
 * -funroll-loops (for gcc) will remove the loops for better performance
 * using loops in the source-code enhances readabillity
 *
 *
 * TODO: write an optimized version for the down-sampling modes
 *       (in these modes the bands 16-31 (2:1) or 8-31 (4:1) are zero 
 */

#include "mpg123lib_intern.h"

#ifndef __APPLE__
#include <altivec.h>
#endif

void dct64_altivec(real *out0,real *out1,real *samples)
{
  ALIGNED(16) real bufs[32];

	{
		register real *b1,*costab;
		
		vector unsigned char vinvert,vperm1,vperm2,vperm3,vperm4;
		vector float v1,v2,v3,v4,v5,v6,v7,v8;
		vector float vbs1,vbs2,vbs3,vbs4,vbs5,vbs6,vbs7,vbs8;
		vector float vbs9,vbs10,vbs11,vbs12,vbs13,vbs14,vbs15,vbs16;
		vector float vzero;
		b1 = samples;
		costab = pnts[0];
		
		vzero = vec_xor(vzero,vzero);
#ifdef __APPLE__
		vinvert = (vector unsigned char)(12,13,14,15,8,9,10,11,4,5,6,7,0,1,2,3);
#else
		vinvert = (vector unsigned char){12,13,14,15,8,9,10,11,4,5,6,7,0,1,2,3};
#endif
		vperm1 = vec_lvsl(0,b1);
		vperm2 = vec_perm(vperm1,vperm1,vinvert);
		
		v1 = vec_ld(0,b1);
		v2 = vec_ld(16,b1);
		v3 = vec_ld(112,b1);
		v4 = vec_ld(127,b1);
		v5 = vec_perm(v1,v2,vperm1); /* b1[0,1,2,3] */
		v6 = vec_perm(v3,v4,vperm2); /* b1[31,30,29,28] */
		
		vbs1 = vec_add(v5,v6);
		vbs8 = vec_sub(v5,v6);
		
		v1 = vec_ld(32,b1);
		v4 = vec_ld(96,b1);
		v5 = vec_perm(v2,v1,vperm1); /* b1[4,5,6,7] */
		v6 = vec_perm(v4,v3,vperm2); /* b1[27,26,25,24] */
		
		vbs2 = vec_add(v5,v6);
		vbs7 = vec_sub(v5,v6);
		
		v2 = vec_ld(48,b1);
		v3 = vec_ld(80,b1);
		v5 = vec_perm(v1,v2,vperm1); /* b1[8,9,10,11] */
		v6 = vec_perm(v3,v4,vperm2); /* b1[23,22,21,20] */
		
		vbs3 = vec_add(v5,v6);
		vbs6 = vec_sub(v5,v6);
		
		v1 = vec_ld(64,b1);
		v5 = vec_perm(v2,v1,vperm1); /* b1[12,13,14,15] */
		v6 = vec_perm(v1,v3,vperm2); /* b1[19,18,17,16] */
		
		vbs4 = vec_add(v5,v6);
		vbs5 = vec_sub(v5,v6);
		
		v1 = vec_ld(0,costab);
		vbs8 = vec_madd(vbs8,v1,vzero);
		v2 = vec_ld(16,costab);
		vbs7 = vec_madd(vbs7,v2,vzero);
		v3 = vec_ld(32,costab);
		vbs6 = vec_madd(vbs6,v3,vzero);
		v4 = vec_ld(48,costab);
		vbs5 = vec_madd(vbs5,v4,vzero);
		vbs6 = vec_perm(vbs6,vbs6,vinvert);
		vbs5 = vec_perm(vbs5,vbs5,vinvert);
		
		
		costab = pnts[1];
		
		v1 = vec_perm(vbs4,vbs4,vinvert);
		vbs9 = vec_add(vbs1,v1);
		v3 = vec_sub(vbs1,v1);
		v5 = vec_ld(0,costab);
		v2 = vec_perm(vbs3,vbs3,vinvert);
		vbs10 = vec_add(vbs2,v2);
		v4 = vec_sub(vbs2,v2);
		v6 = vec_ld(16,costab);
		vbs12 = vec_madd(v3,v5,vzero);
		vbs11 = vec_madd(v4,v6,vzero);
		
		v7 = vec_sub(vbs7,vbs6);
		v8 = vec_sub(vbs8,vbs5);
		vbs13 = vec_add(vbs5,vbs8);
		vbs14 = vec_add(vbs6,vbs7);
		vbs15 = vec_madd(v7,v6,vzero);
		vbs16 = vec_madd(v8,v5,vzero);
		
		
		costab = pnts[2];
		
		v1 = vec_perm(vbs10,vbs10,vinvert);
		v5 = vec_perm(vbs14,vbs14,vinvert);
		vbs1 = vec_add(v1,vbs9);
		vbs5 = vec_add(v5,vbs13);
		v2 = vec_sub(vbs9,v1);
		v6 = vec_sub(vbs13,v5);
		v3 = vec_ld(0,costab);
		vbs11 = vec_perm(vbs11,vbs11,vinvert);
		vbs15 = vec_perm(vbs15,vbs15,vinvert);
		vbs3 = vec_add(vbs11,vbs12);
		vbs7 = vec_add(vbs15,vbs16);
		v4 = vec_sub(vbs12,vbs11);
		v7 = vec_sub(vbs16,vbs15);
		vbs2 = vec_madd(v2,v3,vzero);
		vbs4 = vec_madd(v4,v3,vzero);
		vbs6 = vec_madd(v6,v3,vzero);
		vbs8 = vec_madd(v7,v3,vzero);
		
		vbs2 = vec_perm(vbs2,vbs2,vinvert);
		vbs4 = vec_perm(vbs4,vbs4,vinvert);
		vbs6 = vec_perm(vbs6,vbs6,vinvert);
		vbs8 = vec_perm(vbs8,vbs8,vinvert);
		
		
		costab = pnts[3];
		
#ifdef __APPLE__
		vperm1 = (vector unsigned char)(0,1,2,3,4,5,6,7,16,17,18,19,20,21,22,23);
		vperm2 = (vector unsigned char)(12,13,14,15,8,9,10,11,28,29,30,31,24,25,26,27);
		vperm3 = (vector unsigned char)(0,1,2,3,4,5,6,7,20,21,22,23,16,17,18,19);
#else
		vperm1 = (vector unsigned char){0,1,2,3,4,5,6,7,16,17,18,19,20,21,22,23};
		vperm2 = (vector unsigned char){12,13,14,15,8,9,10,11,28,29,30,31,24,25,26,27};
		vperm3 = (vector unsigned char){0,1,2,3,4,5,6,7,20,21,22,23,16,17,18,19};
#endif
		vperm4 = vec_add(vperm3,vec_splat_u8(8));
		
		v1 = vec_ld(0,costab);
		v2 = vec_splat(v1,0);
		v3 = vec_splat(v1,1);
		v1 = vec_mergeh(v2,v3);
		
		v2 = vec_perm(vbs1,vbs3,vperm1);
		v3 = vec_perm(vbs2,vbs4,vperm1);
		v4 = vec_perm(vbs1,vbs3,vperm2);
		v5 = vec_perm(vbs2,vbs4,vperm2);
		v6 = vec_sub(v2,v4);
		v7 = vec_sub(v3,v5);
		v2 = vec_add(v2,v4);
		v3 = vec_add(v3,v5);
		v4 = vec_madd(v6,v1,vzero);
		v5 = vec_nmsub(v7,v1,vzero);
		vbs9 = vec_perm(v2,v4,vperm3);
		vbs11 = vec_perm(v2,v4,vperm4);
		vbs10 = vec_perm(v3,v5,vperm3);
		vbs12 = vec_perm(v3,v5,vperm4);
		
		v2 = vec_perm(vbs5,vbs7,vperm1);
		v3 = vec_perm(vbs6,vbs8,vperm1);
		v4 = vec_perm(vbs5,vbs7,vperm2);
		v5 = vec_perm(vbs6,vbs8,vperm2);
		v6 = vec_sub(v2,v4);
		v7 = vec_sub(v3,v5);
		v2 = vec_add(v2,v4);
		v3 = vec_add(v3,v5);
		v4 = vec_madd(v6,v1,vzero);
		v5 = vec_nmsub(v7,v1,vzero);
		vbs13 = vec_perm(v2,v4,vperm3);
		vbs15 = vec_perm(v2,v4,vperm4);
		vbs14 = vec_perm(v3,v5,vperm3);
		vbs16 = vec_perm(v3,v5,vperm4);
		
		
		costab = pnts[4];
		
		v1 = vec_lde(0,costab);
#ifdef __APPLE__
		v2 = (vector float)(1.0f,-1.0f,1.0f,-1.0f);
#else
		v2 = (vector float){1.0f,-1.0f,1.0f,-1.0f};
#endif
		v3 = vec_splat(v1,0);
		v1 = vec_madd(v2,v3,vzero);
		
		v2 = vec_mergeh(vbs9,vbs10);
		v3 = vec_mergel(vbs9,vbs10);
		v4 = vec_mergeh(vbs11,vbs12);
		v5 = vec_mergel(vbs11,vbs12);
		v6 = vec_mergeh(v2,v3);
		v7 = vec_mergel(v2,v3);
		v2 = vec_mergeh(v4,v5);
		v3 = vec_mergel(v4,v5); 
		v4 = vec_sub(v6,v7);
		v5 = vec_sub(v2,v3);
		v6 = vec_add(v6,v7);
		v7 = vec_add(v2,v3);
		v2 = vec_madd(v4,v1,vzero);
		v3 = vec_madd(v5,v1,vzero);
		vbs1 = vec_mergeh(v6,v2);
		vbs2 = vec_mergel(v6,v2);
		vbs3 = vec_mergeh(v7,v3);
		vbs4 = vec_mergel(v7,v3);
		
		v2 = vec_mergeh(vbs13,vbs14);
		v3 = vec_mergel(vbs13,vbs14);
		v4 = vec_mergeh(vbs15,vbs16);
		v5 = vec_mergel(vbs15,vbs16);
		v6 = vec_mergeh(v2,v3);
		v7 = vec_mergel(v2,v3);
		v2 = vec_mergeh(v4,v5);
		v3 = vec_mergel(v4,v5); 
		v4 = vec_sub(v6,v7);
		v5 = vec_sub(v2,v3);
		v6 = vec_add(v6,v7);
		v7 = vec_add(v2,v3);
		v2 = vec_madd(v4,v1,vzero);
		v3 = vec_madd(v5,v1,vzero);
		vbs5 = vec_mergeh(v6,v2);
		vbs6 = vec_mergel(v6,v2);
		vbs7 = vec_mergeh(v7,v3);
		vbs8 = vec_mergel(v7,v3);
		
		vec_st(vbs1,0,bufs);
		vec_st(vbs2,16,bufs);
		vec_st(vbs3,32,bufs);
		vec_st(vbs4,48,bufs);
		vec_st(vbs5,64,bufs);
		vec_st(vbs6,80,bufs);
		vec_st(vbs7,96,bufs);
		vec_st(vbs8,112,bufs);
	}

 {
  register real *b1;
  register int i;

  for(b1=bufs,i=8;i;i--,b1+=4)
    b1[2] += b1[3];

  for(b1=bufs,i=4;i;i--,b1+=8)
  {
    b1[4] += b1[6];
    b1[6] += b1[5];
    b1[5] += b1[7];
  }

  for(b1=bufs,i=2;i;i--,b1+=16)
  {
    b1[8]  += b1[12];
    b1[12] += b1[10];
    b1[10] += b1[14];
    b1[14] += b1[9];
    b1[9]  += b1[13];
    b1[13] += b1[11];
    b1[11] += b1[15];
  }
 }


  out0[0x10*16] = bufs[0];
  out0[0x10*15] = bufs[16+0]  + bufs[16+8];
  out0[0x10*14] = bufs[8];
  out0[0x10*13] = bufs[16+8]  + bufs[16+4];
  out0[0x10*12] = bufs[4];
  out0[0x10*11] = bufs[16+4]  + bufs[16+12];
  out0[0x10*10] = bufs[12];
  out0[0x10* 9] = bufs[16+12] + bufs[16+2];
  out0[0x10* 8] = bufs[2];
  out0[0x10* 7] = bufs[16+2]  + bufs[16+10];
  out0[0x10* 6] = bufs[10];
  out0[0x10* 5] = bufs[16+10] + bufs[16+6];
  out0[0x10* 4] = bufs[6];
  out0[0x10* 3] = bufs[16+6]  + bufs[16+14];
  out0[0x10* 2] = bufs[14];
  out0[0x10* 1] = bufs[16+14] + bufs[16+1];
  out0[0x10* 0] = bufs[1];

  out1[0x10* 0] = bufs[1];
  out1[0x10* 1] = bufs[16+1]  + bufs[16+9];
  out1[0x10* 2] = bufs[9];
  out1[0x10* 3] = bufs[16+9]  + bufs[16+5];
  out1[0x10* 4] = bufs[5];
  out1[0x10* 5] = bufs[16+5]  + bufs[16+13];
  out1[0x10* 6] = bufs[13];
  out1[0x10* 7] = bufs[16+13] + bufs[16+3];
  out1[0x10* 8] = bufs[3];
  out1[0x10* 9] = bufs[16+3]  + bufs[16+11];
  out1[0x10*10] = bufs[11];
  out1[0x10*11] = bufs[16+11] + bufs[16+7];
  out1[0x10*12] = bufs[7];
  out1[0x10*13] = bufs[16+7]  + bufs[16+15];
  out1[0x10*14] = bufs[15];
  out1[0x10*15] = bufs[16+15];

}


