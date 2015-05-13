/*
 * Copyright (c) 2002-2007, Communications and Remote Sensing Laboratory, Universite catholique de Louvain (UCL), Belgium
 * Copyright (c) 2002-2007, Professor Benoit Macq
 * Copyright (c) 2001-2003, David Janssens
 * Copyright (c) 2002-2003, Yannick Verschueren
 * Copyright (c) 2003-2007, Francois-Olivier Devaux and Antonin Descampe
 * Copyright (c) 2005, Herve Drolon, FreeImage Team
 * Copyright (c) 2008, Jerome Fimes, Communications & Systemes <jerome.fimes@c-s.fr>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS `AS IS'
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#include "opj_includes.h"

/** @defgroup MQC MQC - Implementation of an MQ-Coder */
/*@{*/

/** @name Local static functions */
/*@{*/

/**
Output a byte, doing bit-stuffing if necessary.
After a 0xff byte, the next byte must be smaller than 0x90.
@param mqc MQC handle
*/
static void opj_mqc_byteout(opj_mqc_t *mqc);
/**
Renormalize mqc->a and mqc->c while encoding, so that mqc->a stays between 0x8000 and 0x10000
@param mqc MQC handle
*/
static void opj_mqc_renorme(opj_mqc_t *mqc);
/**
Encode the most probable symbol
@param mqc MQC handle
*/
static void opj_mqc_codemps(opj_mqc_t *mqc);
/**
Encode the most least symbol
@param mqc MQC handle
*/
static void opj_mqc_codelps(opj_mqc_t *mqc);
/**
Fill mqc->c with 1's for flushing
@param mqc MQC handle
*/
static void opj_mqc_setbits(opj_mqc_t *mqc);
/**
FIXME DOC
@param mqc MQC handle
@return 
*/
static INLINE OPJ_INT32 opj_mqc_mpsexchange(opj_mqc_t *const mqc);
/**
FIXME DOC
@param mqc MQC handle
@return 
*/
static INLINE OPJ_INT32 opj_mqc_lpsexchange(opj_mqc_t *const mqc);
/**
Input a byte
@param mqc MQC handle
*/
static INLINE void opj_mqc_bytein(opj_mqc_t *const mqc);
/**
Renormalize mqc->a and mqc->c while decoding
@param mqc MQC handle
*/
static INLINE void opj_mqc_renormd(opj_mqc_t *const mqc);
/*@}*/

/*@}*/

/* <summary> */
/* This array defines all the possible states for a context. */
/* </summary> */
static opj_mqc_state_t mqc_states[47 * 2] = {
	{0x5601, 0, &mqc_states[2], &mqc_states[3]},
	{0x5601, 1, &mqc_states[3], &mqc_states[2]},
	{0x3401, 0, &mqc_states[4], &mqc_states[12]},
	{0x3401, 1, &mqc_states[5], &mqc_states[13]},
	{0x1801, 0, &mqc_states[6], &mqc_states[18]},
	{0x1801, 1, &mqc_states[7], &mqc_states[19]},
	{0x0ac1, 0, &mqc_states[8], &mqc_states[24]},
	{0x0ac1, 1, &mqc_states[9], &mqc_states[25]},
	{0x0521, 0, &mqc_states[10], &mqc_states[58]},
	{0x0521, 1, &mqc_states[11], &mqc_states[59]},
	{0x0221, 0, &mqc_states[76], &mqc_states[66]},
	{0x0221, 1, &mqc_states[77], &mqc_states[67]},
	{0x5601, 0, &mqc_states[14], &mqc_states[13]},
	{0x5601, 1, &mqc_states[15], &mqc_states[12]},
	{0x5401, 0, &mqc_states[16], &mqc_states[28]},
	{0x5401, 1, &mqc_states[17], &mqc_states[29]},
	{0x4801, 0, &mqc_states[18], &mqc_states[28]},
	{0x4801, 1, &mqc_states[19], &mqc_states[29]},
	{0x3801, 0, &mqc_states[20], &mqc_states[28]},
	{0x3801, 1, &mqc_states[21], &mqc_states[29]},
	{0x3001, 0, &mqc_states[22], &mqc_states[34]},
	{0x3001, 1, &mqc_states[23], &mqc_states[35]},
	{0x2401, 0, &mqc_states[24], &mqc_states[36]},
	{0x2401, 1, &mqc_states[25], &mqc_states[37]},
	{0x1c01, 0, &mqc_states[26], &mqc_states[40]},
	{0x1c01, 1, &mqc_states[27], &mqc_states[41]},
	{0x1601, 0, &mqc_states[58], &mqc_states[42]},
	{0x1601, 1, &mqc_states[59], &mqc_states[43]},
	{0x5601, 0, &mqc_states[30], &mqc_states[29]},
	{0x5601, 1, &mqc_states[31], &mqc_states[28]},
	{0x5401, 0, &mqc_states[32], &mqc_states[28]},
	{0x5401, 1, &mqc_states[33], &mqc_states[29]},
	{0x5101, 0, &mqc_states[34], &mqc_states[30]},
	{0x5101, 1, &mqc_states[35], &mqc_states[31]},
	{0x4801, 0, &mqc_states[36], &mqc_states[32]},
	{0x4801, 1, &mqc_states[37], &mqc_states[33]},
	{0x3801, 0, &mqc_states[38], &mqc_states[34]},
	{0x3801, 1, &mqc_states[39], &mqc_states[35]},
	{0x3401, 0, &mqc_states[40], &mqc_states[36]},
	{0x3401, 1, &mqc_states[41], &mqc_states[37]},
	{0x3001, 0, &mqc_states[42], &mqc_states[38]},
	{0x3001, 1, &mqc_states[43], &mqc_states[39]},
	{0x2801, 0, &mqc_states[44], &mqc_states[38]},
	{0x2801, 1, &mqc_states[45], &mqc_states[39]},
	{0x2401, 0, &mqc_states[46], &mqc_states[40]},
	{0x2401, 1, &mqc_states[47], &mqc_states[41]},
	{0x2201, 0, &mqc_states[48], &mqc_states[42]},
	{0x2201, 1, &mqc_states[49], &mqc_states[43]},
	{0x1c01, 0, &mqc_states[50], &mqc_states[44]},
	{0x1c01, 1, &mqc_states[51], &mqc_states[45]},
	{0x1801, 0, &mqc_states[52], &mqc_states[46]},
	{0x1801, 1, &mqc_states[53], &mqc_states[47]},
	{0x1601, 0, &mqc_states[54], &mqc_states[48]},
	{0x1601, 1, &mqc_states[55], &mqc_states[49]},
	{0x1401, 0, &mqc_states[56], &mqc_states[50]},
	{0x1401, 1, &mqc_states[57], &mqc_states[51]},
	{0x1201, 0, &mqc_states[58], &mqc_states[52]},
	{0x1201, 1, &mqc_states[59], &mqc_states[53]},
	{0x1101, 0, &mqc_states[60], &mqc_states[54]},
	{0x1101, 1, &mqc_states[61], &mqc_states[55]},
	{0x0ac1, 0, &mqc_states[62], &mqc_states[56]},
	{0x0ac1, 1, &mqc_states[63], &mqc_states[57]},
	{0x09c1, 0, &mqc_states[64], &mqc_states[58]},
	{0x09c1, 1, &mqc_states[65], &mqc_states[59]},
	{0x08a1, 0, &mqc_states[66], &mqc_states[60]},
	{0x08a1, 1, &mqc_states[67], &mqc_states[61]},
	{0x0521, 0, &mqc_states[68], &mqc_states[62]},
	{0x0521, 1, &mqc_states[69], &mqc_states[63]},
	{0x0441, 0, &mqc_states[70], &mqc_states[64]},
	{0x0441, 1, &mqc_states[71], &mqc_states[65]},
	{0x02a1, 0, &mqc_states[72], &mqc_states[66]},
	{0x02a1, 1, &mqc_states[73], &mqc_states[67]},
	{0x0221, 0, &mqc_states[74], &mqc_states[68]},
	{0x0221, 1, &mqc_states[75], &mqc_states[69]},
	{0x0141, 0, &mqc_states[76], &mqc_states[70]},
	{0x0141, 1, &mqc_states[77], &mqc_states[71]},
	{0x0111, 0, &mqc_states[78], &mqc_states[72]},
	{0x0111, 1, &mqc_states[79], &mqc_states[73]},
	{0x0085, 0, &mqc_states[80], &mqc_states[74]},
	{0x0085, 1, &mqc_states[81], &mqc_states[75]},
	{0x0049, 0, &mqc_states[82], &mqc_states[76]},
	{0x0049, 1, &mqc_states[83], &mqc_states[77]},
	{0x0025, 0, &mqc_states[84], &mqc_states[78]},
	{0x0025, 1, &mqc_states[85], &mqc_states[79]},
	{0x0015, 0, &mqc_states[86], &mqc_states[80]},
	{0x0015, 1, &mqc_states[87], &mqc_states[81]},
	{0x0009, 0, &mqc_states[88], &mqc_states[82]},
	{0x0009, 1, &mqc_states[89], &mqc_states[83]},
	{0x0005, 0, &mqc_states[90], &mqc_states[84]},
	{0x0005, 1, &mqc_states[91], &mqc_states[85]},
	{0x0001, 0, &mqc_states[90], &mqc_states[86]},
	{0x0001, 1, &mqc_states[91], &mqc_states[87]},
	{0x5601, 0, &mqc_states[92], &mqc_states[92]},
	{0x5601, 1, &mqc_states[93], &mqc_states[93]},
};

/* 
==========================================================
   local functions
==========================================================
*/

void opj_mqc_byteout(opj_mqc_t *mqc) {
	if (*mqc->bp == 0xff) {
		mqc->bp++;
		*mqc->bp = (OPJ_BYTE)(mqc->c >> 20);
		mqc->c &= 0xfffff;
		mqc->ct = 7;
	} else {
		if ((mqc->c & 0x8000000) == 0) {	/* ((mqc->c&0x8000000)==0) CHANGE */
			mqc->bp++;
			*mqc->bp = (OPJ_BYTE)(mqc->c >> 19);
			mqc->c &= 0x7ffff;
			mqc->ct = 8;
		} else {
			(*mqc->bp)++;
			if (*mqc->bp == 0xff) {
				mqc->c &= 0x7ffffff;
				mqc->bp++;
				*mqc->bp = (OPJ_BYTE)(mqc->c >> 20);
				mqc->c &= 0xfffff;
				mqc->ct = 7;
			} else {
				mqc->bp++;
				*mqc->bp = (OPJ_BYTE)(mqc->c >> 19);
				mqc->c &= 0x7ffff;
				mqc->ct = 8;
			}
		}
	}
}

void opj_mqc_renorme(opj_mqc_t *mqc) {
	do {
		mqc->a <<= 1;
		mqc->c <<= 1;
		mqc->ct--;
		if (mqc->ct == 0) {
			opj_mqc_byteout(mqc);
		}
	} while ((mqc->a & 0x8000) == 0);
}

void opj_mqc_codemps(opj_mqc_t *mqc) {
	mqc->a -= (*mqc->curctx)->qeval;
	if ((mqc->a & 0x8000) == 0) {
		if (mqc->a < (*mqc->curctx)->qeval) {
			mqc->a = (*mqc->curctx)->qeval;
		} else {
			mqc->c += (*mqc->curctx)->qeval;
		}
		*mqc->curctx = (*mqc->curctx)->nmps;
		opj_mqc_renorme(mqc);
	} else {
		mqc->c += (*mqc->curctx)->qeval;
	}
}

void opj_mqc_codelps(opj_mqc_t *mqc) {
	mqc->a -= (*mqc->curctx)->qeval;
	if (mqc->a < (*mqc->curctx)->qeval) {
		mqc->c += (*mqc->curctx)->qeval;
	} else {
		mqc->a = (*mqc->curctx)->qeval;
	}
	*mqc->curctx = (*mqc->curctx)->nlps;
	opj_mqc_renorme(mqc);
}

void opj_mqc_setbits(opj_mqc_t *mqc) {
	OPJ_UINT32 tempc = mqc->c + mqc->a;
	mqc->c |= 0xffff;
	if (mqc->c >= tempc) {
		mqc->c -= 0x8000;
	}
}

static INLINE OPJ_INT32 opj_mqc_mpsexchange(opj_mqc_t *const mqc) {
	OPJ_INT32 d;
	if (mqc->a < (*mqc->curctx)->qeval) {
		d = (OPJ_INT32)(1 - (*mqc->curctx)->mps);
		*mqc->curctx = (*mqc->curctx)->nlps;
	} else {
		d = (OPJ_INT32)(*mqc->curctx)->mps;
		*mqc->curctx = (*mqc->curctx)->nmps;
	}
	
	return d;
}

static INLINE OPJ_INT32 opj_mqc_lpsexchange(opj_mqc_t *const mqc) {
	OPJ_INT32 d;
	if (mqc->a < (*mqc->curctx)->qeval) {
		mqc->a = (*mqc->curctx)->qeval;
		d = (OPJ_INT32)(*mqc->curctx)->mps;
		*mqc->curctx = (*mqc->curctx)->nmps;
	} else {
		mqc->a = (*mqc->curctx)->qeval;
		d = (OPJ_INT32)(1 - (*mqc->curctx)->mps);
		*mqc->curctx = (*mqc->curctx)->nlps;
	}
	
	return d;
}

#ifdef MQC_PERF_OPT
static INLINE void opj_mqc_bytein(opj_mqc_t *const mqc) {
	unsigned int i = *((unsigned int *) mqc->bp);
	mqc->c += i & 0xffff00;
	mqc->ct = i & 0x0f;
	mqc->bp += (i >> 2) & 0x04;
}
#else
static void opj_mqc_bytein(opj_mqc_t *const mqc) {
	if (mqc->bp != mqc->end) {
		OPJ_UINT32 c;
		if (mqc->bp + 1 != mqc->end) {
			c = *(mqc->bp + 1);
		} else {
			c = 0xff;
		}
		if (*mqc->bp == 0xff) {
			if (c > 0x8f) {
				mqc->c += 0xff00;
				mqc->ct = 8;
			} else {
				mqc->bp++;
				mqc->c += c << 9;
				mqc->ct = 7;
			}
		} else {
			mqc->bp++;
			mqc->c += c << 8;
			mqc->ct = 8;
		}
	} else {
		mqc->c += 0xff00;
		mqc->ct = 8;
	}
}
#endif

static INLINE void opj_mqc_renormd(opj_mqc_t *const mqc) {
	do {
		if (mqc->ct == 0) {
			opj_mqc_bytein(mqc);
		}
		mqc->a <<= 1;
		mqc->c <<= 1;
		mqc->ct--;
	} while (mqc->a < 0x8000);
}

/* 
==========================================================
   MQ-Coder interface
==========================================================
*/

opj_mqc_t* opj_mqc_create(void) {
	opj_mqc_t *mqc = (opj_mqc_t*)opj_malloc(sizeof(opj_mqc_t));
#ifdef MQC_PERF_OPT
	mqc->buffer = NULL;
#endif
	return mqc;
}

void opj_mqc_destroy(opj_mqc_t *mqc) {
	if(mqc) {
#ifdef MQC_PERF_OPT
		opj_free(mqc->buffer);
#endif
		opj_free(mqc);
	}
}

OPJ_UINT32 opj_mqc_numbytes(opj_mqc_t *mqc) {
	const ptrdiff_t diff = mqc->bp - mqc->start;
#if 0
  assert( diff <= 0xffffffff && diff >= 0 ); /* UINT32_MAX */
#endif
	return (OPJ_UINT32)diff;
}

void opj_mqc_init_enc(opj_mqc_t *mqc, OPJ_BYTE *bp) {
    /* TODO MSD: need to take a look to the v2 version */
	opj_mqc_setcurctx(mqc, 0);
	mqc->a = 0x8000;
	mqc->c = 0;
	mqc->bp = bp - 1;
	mqc->ct = 12;
	if (*mqc->bp == 0xff) {
		mqc->ct = 13;
	}
	mqc->start = bp;
}

void opj_mqc_encode(opj_mqc_t *mqc, OPJ_UINT32 d) {
	if ((*mqc->curctx)->mps == d) {
		opj_mqc_codemps(mqc);
	} else {
		opj_mqc_codelps(mqc);
	}
}

void opj_mqc_flush(opj_mqc_t *mqc) {
	opj_mqc_setbits(mqc);
	mqc->c <<= mqc->ct;
	opj_mqc_byteout(mqc);
	mqc->c <<= mqc->ct;
	opj_mqc_byteout(mqc);
	
	if (*mqc->bp != 0xff) {
		mqc->bp++;
	}
}

void opj_mqc_bypass_init_enc(opj_mqc_t *mqc) {
	mqc->c = 0;
	mqc->ct = 8;
	/*if (*mqc->bp == 0xff) {
	mqc->ct = 7;
     } */
}

void opj_mqc_bypass_enc(opj_mqc_t *mqc, OPJ_UINT32 d) {
	mqc->ct--;
	mqc->c = mqc->c + (d << mqc->ct);
	if (mqc->ct == 0) {
		mqc->bp++;
		*mqc->bp = (OPJ_BYTE)mqc->c;
		mqc->ct = 8;
		if (*mqc->bp == 0xff) {
			mqc->ct = 7;
		}
		mqc->c = 0;
	}
}

OPJ_UINT32 opj_mqc_bypass_flush_enc(opj_mqc_t *mqc) {
	OPJ_BYTE bit_padding;
	
	bit_padding = 0;
	
	if (mqc->ct != 0) {
		while (mqc->ct > 0) {
			mqc->ct--;
			mqc->c += (OPJ_UINT32)(bit_padding << mqc->ct);
			bit_padding = (bit_padding + 1) & 0x01;
		}
		mqc->bp++;
		*mqc->bp = (OPJ_BYTE)mqc->c;
		mqc->ct = 8;
		mqc->c = 0;
	}
	
	return 1;
}

void opj_mqc_reset_enc(opj_mqc_t *mqc) {
	opj_mqc_resetstates(mqc);
	opj_mqc_setstate(mqc, T1_CTXNO_UNI, 0, 46);
	opj_mqc_setstate(mqc, T1_CTXNO_AGG, 0, 3);
	opj_mqc_setstate(mqc, T1_CTXNO_ZC, 0, 4);
}

OPJ_UINT32 opj_mqc_restart_enc(opj_mqc_t *mqc) {
	OPJ_UINT32 correction = 1;
	
	/* <flush part> */
	OPJ_INT32 n = (OPJ_INT32)(27 - 15 - mqc->ct);
	mqc->c <<= mqc->ct;
	while (n > 0) {
		opj_mqc_byteout(mqc);
		n -= (OPJ_INT32)mqc->ct;
		mqc->c <<= mqc->ct;
	}
	opj_mqc_byteout(mqc);
	
	return correction;
}

void opj_mqc_restart_init_enc(opj_mqc_t *mqc) {
	/* <Re-init part> */
	opj_mqc_setcurctx(mqc, 0);
	mqc->a = 0x8000;
	mqc->c = 0;
	mqc->ct = 12;
	mqc->bp--;
	if (*mqc->bp == 0xff) {
		mqc->ct = 13;
	}
}

void opj_mqc_erterm_enc(opj_mqc_t *mqc) {
	OPJ_INT32 k = (OPJ_INT32)(11 - mqc->ct + 1);
	
	while (k > 0) {
		mqc->c <<= mqc->ct;
		mqc->ct = 0;
		opj_mqc_byteout(mqc);
		k -= (OPJ_INT32)mqc->ct;
	}
	
	if (*mqc->bp != 0xff) {
		opj_mqc_byteout(mqc);
	}
}

void opj_mqc_segmark_enc(opj_mqc_t *mqc) {
	OPJ_UINT32 i;
	opj_mqc_setcurctx(mqc, 18);
	
	for (i = 1; i < 5; i++) {
		opj_mqc_encode(mqc, i % 2);
	}
}

OPJ_BOOL opj_mqc_init_dec(opj_mqc_t *mqc, OPJ_BYTE *bp, OPJ_UINT32 len) {
	opj_mqc_setcurctx(mqc, 0);
	mqc->start = bp;
	mqc->end = bp + len;
	mqc->bp = bp;
	if (len==0) mqc->c = 0xff << 16;
	else mqc->c = (OPJ_UINT32)(*mqc->bp << 16);

#ifdef MQC_PERF_OPT /* TODO_MSD: check this option and put in experimental */
	{
        OPJ_UINT32 c;
		OPJ_UINT32 *ip;
		OPJ_BYTE *end = mqc->end - 1;
        void* new_buffer = opj_realloc(mqc->buffer, (len + 1) * sizeof(OPJ_UINT32));
        if (! new_buffer) {
            opj_free(mqc->buffer);
            mqc->buffer = NULL;
            return OPJ_FALSE;
        }
        mqc->buffer = new_buffer;
		
        ip = (OPJ_UINT32 *) mqc->buffer;

		while (bp < end) {
			c = *(bp + 1);
			if (*bp == 0xff) {
				if (c > 0x8f) {
					break;
				} else {
					*ip = 0x00000017 | (c << 9);
				}
			} else {
				*ip = 0x00000018 | (c << 8);
			}
			bp++;
			ip++;
		}

		/* Handle last byte of data */
		c = 0xff;
		if (*bp == 0xff) {
			*ip = 0x0000ff18;
		} else {
			bp++;
			*ip = 0x00000018 | (c << 8);
		}
		ip++;

		*ip = 0x0000ff08;
		mqc->bp = mqc->buffer;
	}
#endif
	opj_mqc_bytein(mqc);
	mqc->c <<= 7;
	mqc->ct -= 7;
	mqc->a = 0x8000;
        return OPJ_TRUE;
}

OPJ_INT32 opj_mqc_decode(opj_mqc_t *const mqc) {
	OPJ_INT32 d;
	mqc->a -= (*mqc->curctx)->qeval;
	if ((mqc->c >> 16) < (*mqc->curctx)->qeval) {
		d = opj_mqc_lpsexchange(mqc);
		opj_mqc_renormd(mqc);
	} else {
		mqc->c -= (*mqc->curctx)->qeval << 16;
		if ((mqc->a & 0x8000) == 0) {
			d = opj_mqc_mpsexchange(mqc);
			opj_mqc_renormd(mqc);
		} else {
			d = (OPJ_INT32)(*mqc->curctx)->mps;
		}
	}

	return d;
}

void opj_mqc_resetstates(opj_mqc_t *mqc) {
	OPJ_UINT32 i;
	for (i = 0; i < MQC_NUMCTXS; i++) {
		mqc->ctxs[i] = mqc_states;
	}
}

void opj_mqc_setstate(opj_mqc_t *mqc, OPJ_UINT32 ctxno, OPJ_UINT32 msb, OPJ_INT32 prob) {
	mqc->ctxs[ctxno] = &mqc_states[msb + (OPJ_UINT32)(prob << 1)];
}


