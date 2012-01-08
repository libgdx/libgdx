/*
	dct64.c: DCT64, the plain C version

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
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

void dct64(real *out0,real *out1,real *samples)
{
  real bufs[64];

 {
  register int i,j;
  register real *b1,*b2,*bs,*costab;

  b1 = samples;
  bs = bufs;
  costab = pnts[0]+16;
  b2 = b1 + 32;

  for(i=15;i>=0;i--)
    *bs++ = (*b1++ + *--b2); 
  for(i=15;i>=0;i--)
    *bs++ = REAL_MUL((*--b2 - *b1++), *--costab);

  b1 = bufs;
  costab = pnts[1]+8;
  b2 = b1 + 16;

  {
    for(i=7;i>=0;i--)
      *bs++ = (*b1++ + *--b2); 
    for(i=7;i>=0;i--)
      *bs++ = REAL_MUL((*--b2 - *b1++), *--costab);
    b2 += 32;
    costab += 8;
    for(i=7;i>=0;i--)
      *bs++ = (*b1++ + *--b2); 
    for(i=7;i>=0;i--)
      *bs++ = REAL_MUL((*b1++ - *--b2), *--costab);
    b2 += 32;
  }

  bs = bufs;
  costab = pnts[2];
  b2 = b1 + 8;

  for(j=2;j;j--)
  {
    for(i=3;i>=0;i--)
      *bs++ = (*b1++ + *--b2); 
    for(i=3;i>=0;i--)
      *bs++ = REAL_MUL((*--b2 - *b1++), costab[i]);
    b2 += 16;
    for(i=3;i>=0;i--)
      *bs++ = (*b1++ + *--b2); 
    for(i=3;i>=0;i--)
      *bs++ = REAL_MUL((*b1++ - *--b2), costab[i]);
    b2 += 16;
  }

  b1 = bufs;
  costab = pnts[3];
  b2 = b1 + 4;

  for(j=4;j;j--)
  {
    *bs++ = (*b1++ + *--b2); 
    *bs++ = (*b1++ + *--b2);
    *bs++ = REAL_MUL((*--b2 - *b1++), costab[1]);
    *bs++ = REAL_MUL((*--b2 - *b1++), costab[0]);
    b2 += 8;
    *bs++ = (*b1++ + *--b2); 
    *bs++ = (*b1++ + *--b2);
    *bs++ = REAL_MUL((*b1++ - *--b2), costab[1]);
    *bs++ = REAL_MUL((*b1++ - *--b2), costab[0]);
    b2 += 8;
  }
  bs = bufs;
  costab = pnts[4];

  for(j=8;j;j--)
  {
    real v0,v1;
    v0=*b1++; v1 = *b1++;
    *bs++ = (v0 + v1);
    *bs++ = REAL_MUL((v0 - v1), (*costab));
    v0=*b1++; v1 = *b1++;
    *bs++ = (v0 + v1);
    *bs++ = REAL_MUL((v1 - v0), (*costab));
  }

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


  out0[0x10*16] = REAL_SCALE_DCT64(bufs[0]);
  out0[0x10*15] = REAL_SCALE_DCT64(bufs[16+0]  + bufs[16+8]);
  out0[0x10*14] = REAL_SCALE_DCT64(bufs[8]);
  out0[0x10*13] = REAL_SCALE_DCT64(bufs[16+8]  + bufs[16+4]);
  out0[0x10*12] = REAL_SCALE_DCT64(bufs[4]);
  out0[0x10*11] = REAL_SCALE_DCT64(bufs[16+4]  + bufs[16+12]);
  out0[0x10*10] = REAL_SCALE_DCT64(bufs[12]);
  out0[0x10* 9] = REAL_SCALE_DCT64(bufs[16+12] + bufs[16+2]);
  out0[0x10* 8] = REAL_SCALE_DCT64(bufs[2]);
  out0[0x10* 7] = REAL_SCALE_DCT64(bufs[16+2]  + bufs[16+10]);
  out0[0x10* 6] = REAL_SCALE_DCT64(bufs[10]);
  out0[0x10* 5] = REAL_SCALE_DCT64(bufs[16+10] + bufs[16+6]);
  out0[0x10* 4] = REAL_SCALE_DCT64(bufs[6]);
  out0[0x10* 3] = REAL_SCALE_DCT64(bufs[16+6]  + bufs[16+14]);
  out0[0x10* 2] = REAL_SCALE_DCT64(bufs[14]);
  out0[0x10* 1] = REAL_SCALE_DCT64(bufs[16+14] + bufs[16+1]);
  out0[0x10* 0] = REAL_SCALE_DCT64(bufs[1]);

  out1[0x10* 0] = REAL_SCALE_DCT64(bufs[1]);
  out1[0x10* 1] = REAL_SCALE_DCT64(bufs[16+1]  + bufs[16+9]);
  out1[0x10* 2] = REAL_SCALE_DCT64(bufs[9]);
  out1[0x10* 3] = REAL_SCALE_DCT64(bufs[16+9]  + bufs[16+5]);
  out1[0x10* 4] = REAL_SCALE_DCT64(bufs[5]);
  out1[0x10* 5] = REAL_SCALE_DCT64(bufs[16+5]  + bufs[16+13]);
  out1[0x10* 6] = REAL_SCALE_DCT64(bufs[13]);
  out1[0x10* 7] = REAL_SCALE_DCT64(bufs[16+13] + bufs[16+3]);
  out1[0x10* 8] = REAL_SCALE_DCT64(bufs[3]);
  out1[0x10* 9] = REAL_SCALE_DCT64(bufs[16+3]  + bufs[16+11]);
  out1[0x10*10] = REAL_SCALE_DCT64(bufs[11]);
  out1[0x10*11] = REAL_SCALE_DCT64(bufs[16+11] + bufs[16+7]);
  out1[0x10*12] = REAL_SCALE_DCT64(bufs[7]);
  out1[0x10*13] = REAL_SCALE_DCT64(bufs[16+7]  + bufs[16+15]);
  out1[0x10*14] = REAL_SCALE_DCT64(bufs[15]);
  out1[0x10*15] = REAL_SCALE_DCT64(bufs[16+15]);

}


