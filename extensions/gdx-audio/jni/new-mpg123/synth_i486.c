/*
	decode_i486.c: i486 decode

	copyright 1998-2006 by the mpg123 project - free software under the terms of the LGPL 2.1
	see COPYING and AUTHORS files in distribution or http://mpg123.org
	initially written by Fabrice Bellard

	One has to see if the modification for non-static memory kills this optimization (cache locality?).
*/

/* 
 * Subband Synthesis for MPEG Audio. 
 *
 * Version optimized for 80486 by using integer arithmetic,
 * multiplications by shift and add, and by increasing locality in
 * order to fit the 8KB L1 cache. This code should be compiled with gcc
 * 2.7.2 or higher.
 *
 * Note: this version does not guaranty a good accuracy. The filter
 * coefficients are quantified on 14 bits.
 *
 * (c) 1998 Fabrice Bellard 
 */

#include "mpg123lib_intern.h"

#define FIR16_1(pos,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15) \
{\
  int sum;\
  sum=(c0)*b0[0]+(c1)*b0[1]+(c2)*b0[2]+(c3)*b0[3]+\
  (c4)*b0[4]+(c5)*b0[5]+(c6)*b0[6]+(c7)*b0[7]+\
  (c8)*b0[8]+(c9)*b0[9]+(c10)*b0[10]+(c11)*b0[11]+\
  (c12)*b0[12]+(c13)*b0[13]+(c14)*b0[14]+(c15)*b0[15];\
  sum=(sum+(1 << 13))>>14;\
  if (sum<-32768) sum=-32768;\
  else if (sum>32767) sum=32767;\
  samples[2*(pos)]=sum;\
  b0+=FIR_BUFFER_SIZE;\
}

#define FIR16_2(pos1,c0,c1,c2,c3,c4,c5,c6,c7,c8,c9,c10,c11,c12,c13,c14,c15,\
              pos2,d0,d1,d2,d3,d4,d5,d6,d7,d8,d9,d10,d11,d12,d13,d14,d15) \
{\
  int sum1,sum2,v;\
\
  v=b0[0];\
  sum1=(c0)*v;\
  sum2=(d0)*v;\
  v=b0[1];\
  sum1+=(c1)*v;\
  sum2+=(d1)*v;\
  v=b0[2];\
  sum1+=(c2)*v;\
  sum2+=(d2)*v;\
  v=b0[3];\
  sum1+=(c3)*v;\
  sum2+=(d3)*v;\
  v=b0[4];\
  sum1+=(c4)*v;\
  sum2+=(d4)*v;\
  v=b0[5];\
  sum1+=(c5)*v;\
  sum2+=(d5)*v;\
  v=b0[6];\
  sum1+=(c6)*v;\
  sum2+=(d6)*v;\
  v=b0[7];\
  sum1+=(c7)*v;\
  sum2+=(d7)*v;\
  v=b0[8];\
  sum1+=(c8)*v;\
  sum2+=(d8)*v;\
  v=b0[9];\
  sum1+=(c9)*v;\
  sum2+=(d9)*v;\
  v=b0[10];\
  sum1+=(c10)*v;\
  sum2+=(d10)*v;\
  v=b0[11];\
  sum1+=(c11)*v;\
  sum2+=(d11)*v;\
  v=b0[12];\
  sum1+=(c12)*v;\
  sum2+=(d12)*v;\
  v=b0[13];\
  sum1+=(c13)*v;\
  sum2+=(d13)*v;\
  v=b0[14];\
  sum1+=(c14)*v;\
  sum2+=(d14)*v;\
  v=b0[15];\
  sum1+=(c15)*v;\
  sum2+=(d15)*v;\
\
  sum1=(sum1+(1<<13))>>14;\
  sum2=(sum2+(1<<13))>>14;\
\
  if (sum1<-32768) sum1=-32768;\
  else if (sum1>32767) sum1=32767;\
  samples[(pos1)*2]=sum1;\
\
  if (sum2<-32768) sum2=-32768;\
  else if (sum2>32767) sum2=32767;\
  samples[(pos2)*2]=sum2;\
  b0+=FIR_BUFFER_SIZE;\
}

int absynth_1to1_i486(real *bandPtr, int channel, mpg123_handle *fr, int nb_blocks)
{
  short *samples = (short *) (fr->buffer.data+fr->buffer.fill);
  int *b0,**buf;
  int clip = 0; 
  int block,b,bo_start;

  /* samples address */
  samples+=channel;

  bo_start=fr->i486bo[channel];
  buf = fr->int_buffs[channel];

  b=bo_start;
  for(block=0;block<nb_blocks;block++) {

    /* FIR offset */
    b++;
    if (b >= FIR_BUFFER_SIZE) {
      int *p,*q;
      int c,i,j;
      
      /* we shift the buffers */
      for(c=0;c<2;c++) {
        p=&buf[c][0]+1;
        q=p+(FIR_BUFFER_SIZE-FIR_SIZE);
        for(i=0;i<17;i++) {
          for(j=0;j<FIR_SIZE-1;j++) p[j]=q[j];
          p+=FIR_BUFFER_SIZE;
          q+=FIR_BUFFER_SIZE;
        }
      }
      /* we update 'bo' accordingly */
      b=fr->i486bo[channel]=FIR_SIZE;
    }
    
    if(b & 1) {
      dct64_i486(buf[1]+b,buf[0]+b,bandPtr);
    } else {
      dct64_i486(buf[0]+b,buf[1]+b,bandPtr);
    }
    bandPtr+=32;
  }
  fr->i486bo[channel]=b;

  /* filter bank: part 1 */
  b=bo_start;
  for(block=0;block<nb_blocks;block++) {
    b++;
    if (b >= FIR_BUFFER_SIZE) b=FIR_SIZE;
    if(b & 1) {
      b0 = buf[0] + b - (FIR_SIZE-1);
    } else {
      b0 = buf[1] + b - (FIR_SIZE-1);
    }

 FIR16_1(0,-7,53,-114,509,-1288,1643,-9372,18759,9372,1643,1288,509,114,53,7,0);
 FIR16_2(1,-6,52,-100,515,-1197,1783,-8910,18748,9834,1489,1379,500,129,54,7,0,
 31,0,-7,54,-129,500,-1379,1489,-9834,18748,8910,1783,1197,515,100,52,6);
 FIR16_2(2,-6,50,-86,520,-1106,1910,-8447,18714,10294,1322,1469,488,145,55,8,0,
 30,0,-8,55,-145,488,-1469,1322,-10294,18714,8447,1910,1106,520,86,50,6);
 FIR16_2(3,-5,49,-73,521,-1015,2023,-7986,18657,10751,1140,1559,473,161,56,9,0,
 29,0,-9,56,-161,473,-1559,1140,-10751,18657,7986,2023,1015,521,73,49,5);              
    samples+=64;
  }
  samples-=64*nb_blocks;
  
  /* filter bank: part 2 */

  b=bo_start;
  for(block=0;block<nb_blocks;block++) {
    b++;
    if (b >= FIR_BUFFER_SIZE) b=FIR_SIZE;
    if(b & 1) {
      b0 = buf[0] + b - (FIR_SIZE-1) + 4*FIR_BUFFER_SIZE;
    } else {
      b0 = buf[1] + b - (FIR_SIZE-1) + 4*FIR_BUFFER_SIZE;
    }

 FIR16_2(4,-4,47,-61,521,-926,2123,-7528,18578,11205,944,1647,455,177,56,10,0,
 28,0,-10,56,-177,455,-1647,944,-11205,18578,7528,2123,926,521,61,47,4);
 FIR16_2(5,-4,45,-49,518,-837,2210,-7072,18477,11654,733,1733,434,194,57,11,0,
 27,0,-11,57,-194,434,-1733,733,-11654,18477,7072,2210,837,518,49,45,4);
 FIR16_2(6,-4,44,-38,514,-751,2284,-6620,18353,12097,509,1817,411,212,57,12,0,
 26,0,-12,57,-212,411,-1817,509,-12097,18353,6620,2284,751,514,38,44,4);
 FIR16_2(7,-3,42,-27,508,-665,2347,-6173,18208,12534,270,1899,383,229,56,13,0,
 25,0,-13,56,-229,383,-1899,270,-12534,18208,6173,2347,665,508,27,42,3);             

    samples+=64;
  }
  samples-=64*nb_blocks;

  /* filter bank: part 3 */

  b=bo_start;
  for(block=0;block<nb_blocks;block++) {
    b++;
    if (b >= FIR_BUFFER_SIZE) b=FIR_SIZE;
    if(b & 1) {
      b0 = buf[0] + b - (FIR_SIZE-1) + 8*FIR_BUFFER_SIZE;
    } else {
      b0 = buf[1] + b - (FIR_SIZE-1) + 8*FIR_BUFFER_SIZE;
    }

 FIR16_2(8,-3,40,-18,500,-582,2398,-5732,18042,12963,17,1977,353,247,56,14,0,
 24,0,-14,56,-247,353,-1977,17,-12963,18042,5732,2398,582,500,18,40,3);
 FIR16_2(9,-2,38,-9,490,-501,2437,-5297,17855,13383,-249,2052,320,266,55,15,0,
 23,0,-15,55,-266,320,-2052,-249,-13383,17855,5297,2437,501,490,9,38,2);
 FIR16_2(10,-2,36,0,479,-423,2465,-4869,17647,13794,-530,2122,282,284,53,17,0,
 22,0,-17,53,-284,282,-2122,-530,-13794,17647,4869,2465,423,479,0,36,2);
 FIR16_2(11,-2,34,7,467,-347,2483,-4449,17419,14194,-825,2188,242,302,52,18,0,
 21,0,-18,52,-302,242,-2188,-825,-14194,17419,4449,2483,347,467,-7,34,2);          

    samples+=64;
  }
  samples-=64*nb_blocks;

  /* filter bank: part 4 */

  b=bo_start;
  for(block=0;block<nb_blocks;block++) {
    b++;
    if (b >= FIR_BUFFER_SIZE) b=FIR_SIZE;
    if(b & 1) {
      b0 = buf[0] + b - (FIR_SIZE-1) + 12*FIR_BUFFER_SIZE;
    } else {
      b0 = buf[1] + b - (FIR_SIZE-1) + 12*FIR_BUFFER_SIZE;
    }

 FIR16_2(12,-2,33,14,454,-273,2491,-4038,17173,14583,-1133,2249,198,320,50,19,0,
 20,0,-19,50,-320,198,-2249,-1133,-14583,17173,4038,2491,273,454,-14,33,2);
 FIR16_2(13,-1,31,20,439,-203,2489,-3637,16907,14959,-1454,2304,151,339,47,21,-1,
 19,-1,-21,47,-339,151,-2304,-1454,-14959,16907,3637,2489,203,439,-20,31,1);
 FIR16_2(14,-1,29,26,424,-136,2479,-3245,16623,15322,-1788,2354,100,357,44,22,-1,
 18,-1,-22,44,-357,100,-2354,-1788,-15322,16623,3245,2479,136,424,-26,29,1);
 FIR16_2(15,-1,27,31,408,-72,2459,-2863,16322,15671,-2135,2396,46,374,40,24,-1,
 17,-1,-24,40,-374,46,-2396,-2135,-15671,16322,2863,2459,72,408,-31,27,1);
 FIR16_1(16,-1,0,36,0,-11,0,-2493,0,16004,0,2431,0,391,0,26,0);      

    samples+=64;
  }

  return clip;
}

