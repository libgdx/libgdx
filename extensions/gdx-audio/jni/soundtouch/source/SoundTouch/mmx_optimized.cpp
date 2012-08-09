////////////////////////////////////////////////////////////////////////////////
///
/// MMX optimized routines. All MMX optimized functions have been gathered into 
/// this single source code file, regardless to their class or original source 
/// code file, in order to ease porting the library to other compiler and 
/// processor platforms.
///
/// The MMX-optimizations are programmed using MMX compiler intrinsics that
/// are supported both by Microsoft Visual C++ and GCC compilers, so this file
/// should compile with both toolsets.
///
/// NOTICE: If using Visual Studio 6.0, you'll need to install the "Visual C++ 
/// 6.0 processor pack" update to support compiler intrinsic syntax. The update
/// is available for download at Microsoft Developers Network, see here:
/// http://msdn.microsoft.com/en-us/vstudio/aa718349.aspx
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2012-04-01 20:49:30 +0100 (So, 01 Apr 2012) $
// File revision : $Revision: 4 $
//
// $Id: mmx_optimized.cpp 137 2012-04-01 19:49:30Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////
//
// License :
//
//  SoundTouch audio processing library
//  Copyright (c) Olli Parviainen
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
////////////////////////////////////////////////////////////////////////////////

#include "STTypes.h"

#ifdef SOUNDTOUCH_ALLOW_MMX
// MMX routines available only with integer sample type

using namespace soundtouch;

//////////////////////////////////////////////////////////////////////////////
//
// implementation of MMX optimized functions of class 'TDStretchMMX'
//
//////////////////////////////////////////////////////////////////////////////

#include "TDStretch.h"
#include <mmintrin.h>
#include <limits.h>
#include <math.h>


// Calculates cross correlation of two buffers
double TDStretchMMX::calcCrossCorr(const short *pV1, const short *pV2) const
{
    const __m64 *pVec1, *pVec2;
    __m64 shifter;
    __m64 accu, normaccu;
    long corr, norm;
    int i;
   
    pVec1 = (__m64*)pV1;
    pVec2 = (__m64*)pV2;

    shifter = _m_from_int(overlapDividerBits);
    normaccu = accu = _mm_setzero_si64();

    // Process 4 parallel sets of 2 * stereo samples or 4 * mono samples 
    // during each round for improved CPU-level parallellization.
    for (i = 0; i < channels * overlapLength / 16; i ++)
    {
        __m64 temp, temp2;

        // dictionary of instructions:
        // _m_pmaddwd   : 4*16bit multiply-add, resulting two 32bits = [a0*b0+a1*b1 ; a2*b2+a3*b3]
        // _mm_add_pi32 : 2*32bit add
        // _m_psrad     : 32bit right-shift

        temp = _mm_add_pi32(_mm_madd_pi16(pVec1[0], pVec2[0]),
                            _mm_madd_pi16(pVec1[1], pVec2[1]));
        temp2 = _mm_add_pi32(_mm_madd_pi16(pVec1[0], pVec1[0]),
                             _mm_madd_pi16(pVec1[1], pVec1[1]));
        accu = _mm_add_pi32(accu, _mm_sra_pi32(temp, shifter));
        normaccu = _mm_add_pi32(normaccu, _mm_sra_pi32(temp2, shifter));

        temp = _mm_add_pi32(_mm_madd_pi16(pVec1[2], pVec2[2]),
                            _mm_madd_pi16(pVec1[3], pVec2[3]));
        temp2 = _mm_add_pi32(_mm_madd_pi16(pVec1[2], pVec1[2]),
                             _mm_madd_pi16(pVec1[3], pVec1[3]));
        accu = _mm_add_pi32(accu, _mm_sra_pi32(temp, shifter));
        normaccu = _mm_add_pi32(normaccu, _mm_sra_pi32(temp2, shifter));

        pVec1 += 4;
        pVec2 += 4;
    }

    // copy hi-dword of mm0 to lo-dword of mm1, then sum mmo+mm1
    // and finally store the result into the variable "corr"

    accu = _mm_add_pi32(accu, _mm_srli_si64(accu, 32));
    corr = _m_to_int(accu);

    normaccu = _mm_add_pi32(normaccu, _mm_srli_si64(normaccu, 32));
    norm = _m_to_int(normaccu);

    // Clear MMS state
    _m_empty();

    // Normalize result by dividing by sqrt(norm) - this step is easiest 
    // done using floating point operation
    if (norm == 0) norm = 1;    // to avoid div by zero

    return (double)corr / sqrt((double)norm);
    // Note: Warning about the missing EMMS instruction is harmless
    // as it'll be called elsewhere.
}



void TDStretchMMX::clearCrossCorrState()
{
    // Clear MMS state
    _m_empty();
    //_asm EMMS;
}



// MMX-optimized version of the function overlapStereo
void TDStretchMMX::overlapStereo(short *output, const short *input) const
{
    const __m64 *pVinput, *pVMidBuf;
    __m64 *pVdest;
    __m64 mix1, mix2, adder, shifter;
    int i;

    pVinput  = (const __m64*)input;
    pVMidBuf = (const __m64*)pMidBuffer;
    pVdest   = (__m64*)output;

    // mix1  = mixer values for 1st stereo sample
    // mix1  = mixer values for 2nd stereo sample
    // adder = adder for updating mixer values after each round
    
    mix1  = _mm_set_pi16(0, overlapLength,   0, overlapLength);
    adder = _mm_set_pi16(1, -1, 1, -1);
    mix2  = _mm_add_pi16(mix1, adder);
    adder = _mm_add_pi16(adder, adder);

    // Overlaplength-division by shifter. "+1" is to account for "-1" deduced in
    // overlapDividerBits calculation earlier.
    shifter = _m_from_int(overlapDividerBits + 1);

    for (i = 0; i < overlapLength / 4; i ++)
    {
        __m64 temp1, temp2;
                
        // load & shuffle data so that input & mixbuffer data samples are paired
        temp1 = _mm_unpacklo_pi16(pVMidBuf[0], pVinput[0]);     // = i0l m0l i0r m0r
        temp2 = _mm_unpackhi_pi16(pVMidBuf[0], pVinput[0]);     // = i1l m1l i1r m1r

        // temp = (temp .* mix) >> shifter
        temp1 = _mm_sra_pi32(_mm_madd_pi16(temp1, mix1), shifter);
        temp2 = _mm_sra_pi32(_mm_madd_pi16(temp2, mix2), shifter);
        pVdest[0] = _mm_packs_pi32(temp1, temp2); // pack 2*2*32bit => 4*16bit

        // update mix += adder
        mix1 = _mm_add_pi16(mix1, adder);
        mix2 = _mm_add_pi16(mix2, adder);

        // --- second round begins here ---

        // load & shuffle data so that input & mixbuffer data samples are paired
        temp1 = _mm_unpacklo_pi16(pVMidBuf[1], pVinput[1]);       // = i2l m2l i2r m2r
        temp2 = _mm_unpackhi_pi16(pVMidBuf[1], pVinput[1]);       // = i3l m3l i3r m3r

        // temp = (temp .* mix) >> shifter
        temp1 = _mm_sra_pi32(_mm_madd_pi16(temp1, mix1), shifter);
        temp2 = _mm_sra_pi32(_mm_madd_pi16(temp2, mix2), shifter);
        pVdest[1] = _mm_packs_pi32(temp1, temp2); // pack 2*2*32bit => 4*16bit

        // update mix += adder
        mix1 = _mm_add_pi16(mix1, adder);
        mix2 = _mm_add_pi16(mix2, adder);

        pVinput  += 2;
        pVMidBuf += 2;
        pVdest   += 2;
    }

    _m_empty(); // clear MMS state
}


//////////////////////////////////////////////////////////////////////////////
//
// implementation of MMX optimized functions of class 'FIRFilter'
//
//////////////////////////////////////////////////////////////////////////////

#include "FIRFilter.h"


FIRFilterMMX::FIRFilterMMX() : FIRFilter()
{
    filterCoeffsUnalign = NULL;
}


FIRFilterMMX::~FIRFilterMMX()
{
    delete[] filterCoeffsUnalign;
}


// (overloaded) Calculates filter coefficients for MMX routine
void FIRFilterMMX::setCoefficients(const short *coeffs, uint newLength, uint uResultDivFactor)
{
    uint i;
    FIRFilter::setCoefficients(coeffs, newLength, uResultDivFactor);

    // Ensure that filter coeffs array is aligned to 16-byte boundary
    delete[] filterCoeffsUnalign;
    filterCoeffsUnalign = new short[2 * newLength + 8];
    filterCoeffsAlign = (short *)(((ulong)filterCoeffsUnalign + 15) & -16);

    // rearrange the filter coefficients for mmx routines 
    for (i = 0;i < length; i += 4) 
    {
        filterCoeffsAlign[2 * i + 0] = coeffs[i + 0];
        filterCoeffsAlign[2 * i + 1] = coeffs[i + 2];
        filterCoeffsAlign[2 * i + 2] = coeffs[i + 0];
        filterCoeffsAlign[2 * i + 3] = coeffs[i + 2];

        filterCoeffsAlign[2 * i + 4] = coeffs[i + 1];
        filterCoeffsAlign[2 * i + 5] = coeffs[i + 3];
        filterCoeffsAlign[2 * i + 6] = coeffs[i + 1];
        filterCoeffsAlign[2 * i + 7] = coeffs[i + 3];
    }
}



// mmx-optimized version of the filter routine for stereo sound
uint FIRFilterMMX::evaluateFilterStereo(short *dest, const short *src, uint numSamples) const
{
    // Create stack copies of the needed member variables for asm routines :
    uint i, j;
    __m64 *pVdest = (__m64*)dest;

    if (length < 2) return 0;

    for (i = 0; i < (numSamples - length) / 2; i ++)
    {
        __m64 accu1;
        __m64 accu2;
        const __m64 *pVsrc = (const __m64*)src;
        const __m64 *pVfilter = (const __m64*)filterCoeffsAlign;

        accu1 = accu2 = _mm_setzero_si64();
        for (j = 0; j < lengthDiv8 * 2; j ++)
        {
            __m64 temp1, temp2;

            temp1 = _mm_unpacklo_pi16(pVsrc[0], pVsrc[1]);  // = l2 l0 r2 r0
            temp2 = _mm_unpackhi_pi16(pVsrc[0], pVsrc[1]);  // = l3 l1 r3 r1

            accu1 = _mm_add_pi32(accu1, _mm_madd_pi16(temp1, pVfilter[0]));  // += l2*f2+l0*f0 r2*f2+r0*f0
            accu1 = _mm_add_pi32(accu1, _mm_madd_pi16(temp2, pVfilter[1]));  // += l3*f3+l1*f1 r3*f3+r1*f1

            temp1 = _mm_unpacklo_pi16(pVsrc[1], pVsrc[2]);  // = l4 l2 r4 r2

            accu2 = _mm_add_pi32(accu2, _mm_madd_pi16(temp2, pVfilter[0]));  // += l3*f2+l1*f0 r3*f2+r1*f0
            accu2 = _mm_add_pi32(accu2, _mm_madd_pi16(temp1, pVfilter[1]));  // += l4*f3+l2*f1 r4*f3+r2*f1

            // accu1 += l2*f2+l0*f0 r2*f2+r0*f0
            //       += l3*f3+l1*f1 r3*f3+r1*f1

            // accu2 += l3*f2+l1*f0 r3*f2+r1*f0
            //          l4*f3+l2*f1 r4*f3+r2*f1

            pVfilter += 2;
            pVsrc += 2;
        }
        // accu >>= resultDivFactor
        accu1 = _mm_srai_pi32(accu1, resultDivFactor);
        accu2 = _mm_srai_pi32(accu2, resultDivFactor);

        // pack 2*2*32bits => 4*16 bits
        pVdest[0] = _mm_packs_pi32(accu1, accu2);
        src += 4;
        pVdest ++;
    }

   _m_empty();  // clear emms state

    return (numSamples & 0xfffffffe) - length;
}

#endif  // SOUNDTOUCH_ALLOW_MMX
