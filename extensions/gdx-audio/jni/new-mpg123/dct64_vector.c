/*
	dct64.c: DCT64, another plain C version
...wondering about vectors...

	copyright ?-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Michael Hipp
	Thomas made it look more explicit to see what's going on...
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

static void vec4add(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] += a[i];
}

static void vec4iadd(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] += a[3-i];
}

static void vec4sub(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] -= a[i];
}

static void vec4isub(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] -= a[3-i];
}

static void vec4copy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] = a[i];
}

static void vec4icopy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<4;++i) b[i] = a[3-i];
}

static void vec4mul(real *a, real *b)
{
	int i;
	for(i=0;i<4;++i) b[i] *= a[i];
}

static void vec4imul(real *a, real *b)
{
	int i;
	for(i=0;i<4;++i) b[i] *= a[3-i];
}

static void vec8add(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] += a[i];
}

static void vec8iadd(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] += a[7-i];
}

static void vec8sub(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] -= a[i];
}

static void vec8isub(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] -= a[7-i];
}

static void vec8copy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] = a[i];
}

static void vec8icopy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<8;++i) b[i] = a[7-i];
}

static void vec8mul(real *a, real *b)
{
	int i;
	for(i=0;i<8;++i) b[i] *= a[i];
}

static void vec8imul(real *a, real *b)
{
	int i;
	for(i=0;i<8;++i) b[i] *= a[7-i];
}

static void vec16add(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<16;++i) b[i] += a[i];
}

static void vec16iadd(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<16;++i) b[i] += a[15-i];
}

static void vec16sub(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<16;++i) b[i] -= a[i];
}

static void vec16copy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<16;++i) b[i] = a[i];
}

static void vec16icopy(real * __restrict__ a, real * __restrict__ b)
{
	int i;
	for(i=0;i<16;++i) b[i] = a[15-i];
}

static void vec16mul(real *a, real *b)
{
	int i;
	for(i=0;i<16;++i) b[i] *= a[i];
}

static void vec16imul(real *a, real *b)
{
	int i;
	for(i=0;i<16;++i) b[i] *= a[15-i];
}

void dct64(real *out0,real *out1,real * samples)
{
	real bufs[64];

	{
		int i,j;

		/* for(i=0; i<16; ++i) bufs[i] = samples[i] + samples[31-i]; */
		vec16copy(samples,    bufs);
		vec16iadd(samples+16, bufs);

		/*for(i=0;i<16;++i) bufs[16+i] = REAL_MUL(samples[15-i] - samples[16+i], pnts[0][15-i]);*/
		vec16icopy(samples,    bufs+16);
		vec16sub  (samples+16, bufs+16);
		vec16imul (pnts[0],    bufs+16);

		/* for(i=0;i<8;++i) bufs[32+i] = bufs[i]    + bufs[15-i]; */
		vec8copy(bufs,   bufs+32);
		vec8iadd(bufs+8, bufs+32);
		/* for(i=0;i<8;++i) bufs[40+i] = REAL_MUL( bufs[7-i]  - bufs[8+i],  pnts[1][7-i] ); */
		vec8icopy(bufs,    bufs+40);
		vec8sub  (bufs+8,  bufs+40);
		vec8imul (pnts[1], bufs+40);
		/* for(i=0;i<8;++i) bufs[48+i] = bufs[16+i] + bufs[31-i]; */
		vec8copy(bufs+16, bufs+48);
		vec8iadd(bufs+24, bufs+48);
		/* for(i=0;i<8;++i) bufs[56+i] = REAL_MUL( bufs[24+i] - bufs[23-i], pnts[1][7-i] ); */
		vec8copy(bufs+24, bufs+56);
		vec8isub(bufs+16, bufs+56);
		vec8imul(pnts[1], bufs+56);


		for(j=0;j<2;++j)
		{
			int off = j*16;
			/*for(i=0;i<4;++i) bufs[off+i]    = bufs[off+32+i] + bufs[off+39-i];*/
			vec4copy(bufs+off+32, bufs+off);
			vec4iadd(bufs+off+36, bufs+off);
			/*for(i=0;i<4;++i) bufs[off+4+i]  = REAL_MUL( bufs[off+35-i] - bufs[off+36+i], pnts[2][3-i] );*/
			vec4icopy(bufs+off+32, bufs+off+4);
			vec4sub  (bufs+off+36, bufs+off+4);
			vec4imul (pnts[2],     bufs+off+4);
			/*for(i=0;i<4;++i) bufs[off+8+i]  = bufs[off+40+i] + bufs[off+47-i];*/
			vec4copy(bufs+off+40, bufs+off+8);
			vec4iadd(bufs+off+44, bufs+off+8);
			/*for(i=0;i<4;++i) bufs[off+12+i] = REAL_MUL( bufs[off+44+i] - bufs[off+43-i], pnts[2][3-i] );*/
			vec4copy(bufs+off+44, bufs+off+12);
			vec4isub(bufs+off+40, bufs+off+12);
			vec4imul(pnts[2],     bufs+off+12);
		}

		for(j=0;j<4;++j)
		{
			int off = j*8;
			bufs[off+32+0] = bufs[off+0] + bufs[off+3];
			bufs[off+32+1] = bufs[off+1] + bufs[off+2];
			bufs[off+32+2] = REAL_MUL( bufs[off+1] - bufs[off+2], pnts[3][1] );
			bufs[off+32+3] = REAL_MUL( bufs[off+0] - bufs[off+3], pnts[3][0] );
			bufs[off+32+4] = bufs[off+4] + bufs[off+7];
			bufs[off+32+5] = bufs[off+5] + bufs[off+6];
			bufs[off+32+6] = REAL_MUL( bufs[off+6] - bufs[off+5], pnts[3][1] );
			bufs[off+32+7] = REAL_MUL( bufs[off+7] - bufs[off+4], pnts[3][0] );
		}

		for(j=0;j<8;++j)
		{
			int off = j*4;
			real v0,v1;
			v0 = bufs[off+32+0]; v1 = bufs[off+32+1];
			bufs[off+0] = v0 + v1;
			bufs[off+1] = REAL_MUL( v0-v1, pnts[4][0] );
			v0 = bufs[off+32+2]; v1 = bufs[off+32+3];
			bufs[off+2] = v0 + v1;
			bufs[off+3] = REAL_MUL( v1-v0, pnts[4][0] );
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


