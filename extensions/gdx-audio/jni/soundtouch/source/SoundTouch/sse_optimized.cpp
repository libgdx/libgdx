////////////////////////////////////////////////////////////////////////////////
///
/// SSE optimized routines for Pentium-III, Athlon-XP and later CPUs. All SSE 
/// optimized functions have been gathered into this single source 
/// code file, regardless to their class or original source code file, in order 
/// to ease porting the library to other compiler and processor platforms.
///
/// The SSE-optimizations are programmed using SSE compiler intrinsics that
/// are supported both by Microsoft Visual C++ and GCC compilers, so this file
/// should compile with both toolsets.
///
/// NOTICE: If using Visual Studio 6.0, you'll need to install the "Visual C++ 
/// 6.0 processor pack" update to support SSE instruction set. The update is 
/// available for download at Microsoft Developers Network, see here:
/// http://msdn.microsoft.com/en-us/vstudio/aa718349.aspx
///
/// If the above URL is expired or removed, go to "http://msdn.microsoft.com" and 
/// perform a search with keywords "processor pack".
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
// $Id: sse_optimized.cpp 137 2012-04-01 19:49:30Z oparviai $
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

#include "cpu_detect.h"
#include "STTypes.h"

using namespace soundtouch;

#ifdef SOUNDTOUCH_ALLOW_SSE

// SSE routines available only with float sample type    

//////////////////////////////////////////////////////////////////////////////
//
// implementation of SSE optimized functions of class 'TDStretchSSE'
//
//////////////////////////////////////////////////////////////////////////////

#include "TDStretch.h"
#include <xmmintrin.h>
#include <math.h>

// Calculates cross correlation of two buffers
double TDStretchSSE::calcCrossCorr(const float *pV1, const float *pV2) const
{
    int i;
    const float *pVec1;
    const __m128 *pVec2;
    __m128 vSum, vNorm;

    // Note. It means a major slow-down if the routine needs to tolerate 
    // unaligned __m128 memory accesses. It's way faster if we can skip 
    // unaligned slots and use _mm_load_ps instruction instead of _mm_loadu_ps.
    // This can mean up to ~ 10-fold difference (incl. part of which is
    // due to skipping every second round for stereo sound though).
    //
    // Compile-time define SOUNDTOUCH_ALLOW_NONEXACT_SIMD_OPTIMIZATION is provided
    // for choosing if this little cheating is allowed.

#ifdef SOUNDTOUCH_ALLOW_NONEXACT_SIMD_OPTIMIZATION
    // Little cheating allowed, return valid correlation only for 
    // aligned locations, meaning every second round for stereo sound.

    #define _MM_LOAD    _mm_load_ps

    if (((ulong)pV1) & 15) return -1e50;    // skip unaligned locations

#else
    // No cheating allowed, use unaligned load & take the resulting
    // performance hit.
    #define _MM_LOAD    _mm_loadu_ps
#endif 

    // ensure overlapLength is divisible by 8
    assert((overlapLength % 8) == 0);

    // Calculates the cross-correlation value between 'pV1' and 'pV2' vectors
    // Note: pV2 _must_ be aligned to 16-bit boundary, pV1 need not.
    pVec1 = (const float*)pV1;
    pVec2 = (const __m128*)pV2;
    vSum = vNorm = _mm_setzero_ps();

    // Unroll the loop by factor of 4 * 4 operations. Use same routine for
    // stereo & mono, for mono it just means twice the amount of unrolling.
    for (i = 0; i < channels * overlapLength / 16; i ++) 
    {
        __m128 vTemp;
        // vSum += pV1[0..3] * pV2[0..3]
        vTemp = _MM_LOAD(pVec1);
        vSum  = _mm_add_ps(vSum,  _mm_mul_ps(vTemp ,pVec2[0]));
        vNorm = _mm_add_ps(vNorm, _mm_mul_ps(vTemp ,vTemp));

        // vSum += pV1[4..7] * pV2[4..7]
        vTemp = _MM_LOAD(pVec1 + 4);
        vSum  = _mm_add_ps(vSum, _mm_mul_ps(vTemp, pVec2[1]));
        vNorm = _mm_add_ps(vNorm, _mm_mul_ps(vTemp ,vTemp));

        // vSum += pV1[8..11] * pV2[8..11]
        vTemp = _MM_LOAD(pVec1 + 8);
        vSum  = _mm_add_ps(vSum, _mm_mul_ps(vTemp, pVec2[2]));
        vNorm = _mm_add_ps(vNorm, _mm_mul_ps(vTemp ,vTemp));

        // vSum += pV1[12..15] * pV2[12..15]
        vTemp = _MM_LOAD(pVec1 + 12);
        vSum  = _mm_add_ps(vSum, _mm_mul_ps(vTemp, pVec2[3]));
        vNorm = _mm_add_ps(vNorm, _mm_mul_ps(vTemp ,vTemp));

        pVec1 += 16;
        pVec2 += 4;
    }

    // return value = vSum[0] + vSum[1] + vSum[2] + vSum[3]
    float *pvNorm = (float*)&vNorm;
    double norm = sqrt(pvNorm[0] + pvNorm[1] + pvNorm[2] + pvNorm[3]);
    if (norm < 1e-9) norm = 1.0;    // to avoid div by zero

    float *pvSum = (float*)&vSum;
    return (double)(pvSum[0] + pvSum[1] + pvSum[2] + pvSum[3]) / norm;

    /* This is approximately corresponding routine in C-language yet without normalization:
    double corr, norm;
    uint i;

    // Calculates the cross-correlation value between 'pV1' and 'pV2' vectors
    corr = norm = 0.0;
    for (i = 0; i < channels * overlapLength / 16; i ++) 
    {
        corr += pV1[0] * pV2[0] +
                pV1[1] * pV2[1] +
                pV1[2] * pV2[2] +
                pV1[3] * pV2[3] +
                pV1[4] * pV2[4] +
                pV1[5] * pV2[5] +
                pV1[6] * pV2[6] +
                pV1[7] * pV2[7] +
                pV1[8] * pV2[8] +
                pV1[9] * pV2[9] +
                pV1[10] * pV2[10] +
                pV1[11] * pV2[11] +
                pV1[12] * pV2[12] +
                pV1[13] * pV2[13] +
                pV1[14] * pV2[14] +
                pV1[15] * pV2[15];

    for (j = 0; j < 15; j ++) norm += pV1[j] * pV1[j];

        pV1 += 16;
        pV2 += 16;
    }
    return corr / sqrt(norm);
    */
}


//////////////////////////////////////////////////////////////////////////////
//
// implementation of SSE optimized functions of class 'FIRFilter'
//
//////////////////////////////////////////////////////////////////////////////

#include "FIRFilter.h"

FIRFilterSSE::FIRFilterSSE() : FIRFilter()
{
    filterCoeffsAlign = NULL;
    filterCoeffsUnalign = NULL;
}


FIRFilterSSE::~FIRFilterSSE()
{
    delete[] filterCoeffsUnalign;
    filterCoeffsAlign = NULL;
    filterCoeffsUnalign = NULL;
}


// (overloaded) Calculates filter coefficients for SSE routine
void FIRFilterSSE::setCoefficients(const float *coeffs, uint newLength, uint uResultDivFactor)
{
    uint i;
    float fDivider;

    FIRFilter::setCoefficients(coeffs, newLength, uResultDivFactor);

    // Scale the filter coefficients so that it won't be necessary to scale the filtering result
    // also rearrange coefficients suitably for SSE
    // Ensure that filter coeffs array is aligned to 16-byte boundary
    delete[] filterCoeffsUnalign;
    filterCoeffsUnalign = new float[2 * newLength + 4];
    filterCoeffsAlign = (float *)(((unsigned long)filterCoeffsUnalign + 15) & (ulong)-16);

    fDivider = (float)resultDivider;

    // rearrange the filter coefficients for mmx routines 
    for (i = 0; i < newLength; i ++)
    {
        filterCoeffsAlign[2 * i + 0] =
        filterCoeffsAlign[2 * i + 1] = coeffs[i + 0] / fDivider;
    }
}



// SSE-optimized version of the filter routine for stereo sound
uint FIRFilterSSE::evaluateFilterStereo(float *dest, const float *source, uint numSamples) const
{
    int count = (int)((numSamples - length) & (uint)-2);
    int j;

    assert(count % 2 == 0);

    if (count < 2) return 0;

    assert(source != NULL);
    assert(dest != NULL);
    assert((length % 8) == 0);
    assert(filterCoeffsAlign != NULL);
    assert(((ulong)filterCoeffsAlign) % 16 == 0);

    // filter is evaluated for two stereo samples with each iteration, thus use of 'j += 2'
    for (j = 0; j < count; j += 2)
    {
        const float *pSrc;
        const __m128 *pFil;
        __m128 sum1, sum2;
        uint i;

        pSrc = (const float*)source;              // source audio data
        pFil = (const __m128*)filterCoeffsAlign;  // filter coefficients. NOTE: Assumes coefficients 
                                                  // are aligned to 16-byte boundary
        sum1 = sum2 = _mm_setzero_ps();

        for (i = 0; i < length / 8; i ++) 
        {
            // Unroll loop for efficiency & calculate filter for 2*2 stereo samples 
            // at each pass

            // sum1 is accu for 2*2 filtered stereo sound data at the primary sound data offset
            // sum2 is accu for 2*2 filtered stereo sound data for the next sound sample offset.

            sum1 = _mm_add_ps(sum1, _mm_mul_ps(_mm_loadu_ps(pSrc)    , pFil[0]));
            sum2 = _mm_add_ps(sum2, _mm_mul_ps(_mm_loadu_ps(pSrc + 2), pFil[0]));

            sum1 = _mm_add_ps(sum1, _mm_mul_ps(_mm_loadu_ps(pSrc + 4), pFil[1]));
            sum2 = _mm_add_ps(sum2, _mm_mul_ps(_mm_loadu_ps(pSrc + 6), pFil[1]));

            sum1 = _mm_add_ps(sum1, _mm_mul_ps(_mm_loadu_ps(pSrc + 8) ,  pFil[2]));
            sum2 = _mm_add_ps(sum2, _mm_mul_ps(_mm_loadu_ps(pSrc + 10), pFil[2]));

            sum1 = _mm_add_ps(sum1, _mm_mul_ps(_mm_loadu_ps(pSrc + 12), pFil[3]));
            sum2 = _mm_add_ps(sum2, _mm_mul_ps(_mm_loadu_ps(pSrc + 14), pFil[3]));

            pSrc += 16;
            pFil += 4;
        }

        // Now sum1 and sum2 both have a filtered 2-channel sample each, but we still need
        // to sum the two hi- and lo-floats of these registers together.

        // post-shuffle & add the filtered values and store to dest.
        _mm_storeu_ps(dest, _mm_add_ps(
                    _mm_shuffle_ps(sum1, sum2, _MM_SHUFFLE(1,0,3,2)),   // s2_1 s2_0 s1_3 s1_2
                    _mm_shuffle_ps(sum1, sum2, _MM_SHUFFLE(3,2,1,0))    // s2_3 s2_2 s1_1 s1_0
                    ));
        source += 4;
        dest += 4;
    }

    // Ideas for further improvement:
    // 1. If it could be guaranteed that 'source' were always aligned to 16-byte 
    //    boundary, a faster aligned '_mm_load_ps' instruction could be used.
    // 2. If it could be guaranteed that 'dest' were always aligned to 16-byte 
    //    boundary, a faster '_mm_store_ps' instruction could be used.

    return (uint)count;

    /* original routine in C-language. please notice the C-version has differently 
       organized coefficients though.
    double suml1, suml2;
    double sumr1, sumr2;
    uint i, j;

    for (j = 0; j < count; j += 2)
    {
        const float *ptr;
        const float *pFil;

        suml1 = sumr1 = 0.0;
        suml2 = sumr2 = 0.0;
        ptr = src;
        pFil = filterCoeffs;
        for (i = 0; i < lengthLocal; i ++) 
        {
            // unroll loop for efficiency.

            suml1 += ptr[0] * pFil[0] + 
                     ptr[2] * pFil[2] +
                     ptr[4] * pFil[4] +
                     ptr[6] * pFil[6];

            sumr1 += ptr[1] * pFil[1] + 
                     ptr[3] * pFil[3] +
                     ptr[5] * pFil[5] +
                     ptr[7] * pFil[7];

            suml2 += ptr[8] * pFil[0] + 
                     ptr[10] * pFil[2] +
                     ptr[12] * pFil[4] +
                     ptr[14] * pFil[6];

            sumr2 += ptr[9] * pFil[1] + 
                     ptr[11] * pFil[3] +
                     ptr[13] * pFil[5] +
                     ptr[15] * pFil[7];

            ptr += 16;
            pFil += 8;
        }
        dest[0] = (float)suml1;
        dest[1] = (float)sumr1;
        dest[2] = (float)suml2;
        dest[3] = (float)sumr2;

        src += 4;
        dest += 4;
    }
    */
}

#endif  // SOUNDTOUCH_ALLOW_SSE
