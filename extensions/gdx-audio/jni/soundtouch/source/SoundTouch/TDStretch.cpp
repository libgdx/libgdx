////////////////////////////////////////////////////////////////////////////////
/// 
/// Sampled sound tempo changer/time stretch algorithm. Changes the sound tempo 
/// while maintaining the original pitch by using a time domain WSOLA-like 
/// method with several performance-increasing tweaks.
///
/// Note : MMX optimized functions reside in a separate, platform-specific 
/// file, e.g. 'mmx_win.cpp' or 'mmx_gcc.cpp'
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2012-04-01 20:49:30 +0100 (So, 01 Apr 2012) $
// File revision : $Revision: 1.12 $
//
// $Id: TDStretch.cpp 137 2012-04-01 19:49:30Z oparviai $
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

#include <string.h>
#include <limits.h>
#include <assert.h>
#include <math.h>
#include <float.h>

#include "STTypes.h"
#include "cpu_detect.h"
#include "TDStretch.h"

#include <stdio.h>

using namespace soundtouch;

#define max(x, y) (((x) > (y)) ? (x) : (y))


/*****************************************************************************
 *
 * Constant definitions
 *
 *****************************************************************************/

// Table for the hierarchical mixing position seeking algorithm
static const short _scanOffsets[5][24]={
    { 124,  186,  248,  310,  372,  434,  496,  558,  620,  682,  744, 806,
      868,  930,  992, 1054, 1116, 1178, 1240, 1302, 1364, 1426, 1488,   0},
    {-100,  -75,  -50,  -25,   25,   50,   75,  100,    0,    0,    0,   0,
        0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,   0},
    { -20,  -15,  -10,   -5,    5,   10,   15,   20,    0,    0,    0,   0,
        0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,   0},
    {  -4,   -3,   -2,   -1,    1,    2,    3,    4,    0,    0,    0,   0,
        0,    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,   0},
    { 121,  114,   97,  114,   98,  105,  108,   32,  104,   99,  117,  111,
      116,  100,  110,  117,  111,  115,    0,    0,    0,    0,    0,   0}};

/*****************************************************************************
 *
 * Implementation of the class 'TDStretch'
 *
 *****************************************************************************/


TDStretch::TDStretch() : FIFOProcessor(&outputBuffer)
{
    bQuickSeek = FALSE;
    channels = 2;

    pMidBuffer = NULL;
    pMidBufferUnaligned = NULL;
    overlapLength = 0;

    bAutoSeqSetting = TRUE;
    bAutoSeekSetting = TRUE;

//    outDebt = 0;
    skipFract = 0;

    tempo = 1.0f;
    setParameters(44100, DEFAULT_SEQUENCE_MS, DEFAULT_SEEKWINDOW_MS, DEFAULT_OVERLAP_MS);
    setTempo(1.0f);

    clear();
}



TDStretch::~TDStretch()
{
    delete[] pMidBufferUnaligned;
}



// Sets routine control parameters. These control are certain time constants
// defining how the sound is stretched to the desired duration.
//
// 'sampleRate' = sample rate of the sound
// 'sequenceMS' = one processing sequence length in milliseconds (default = 82 ms)
// 'seekwindowMS' = seeking window length for scanning the best overlapping 
//      position (default = 28 ms)
// 'overlapMS' = overlapping length (default = 12 ms)

void TDStretch::setParameters(int aSampleRate, int aSequenceMS, 
                              int aSeekWindowMS, int aOverlapMS)
{
    // accept only positive parameter values - if zero or negative, use old values instead
    if (aSampleRate > 0)   this->sampleRate = aSampleRate;
    if (aOverlapMS > 0)    this->overlapMs = aOverlapMS;

    if (aSequenceMS > 0)
    {
        this->sequenceMs = aSequenceMS;
        bAutoSeqSetting = FALSE;
    } 
    else if (aSequenceMS == 0)
    {
        // if zero, use automatic setting
        bAutoSeqSetting = TRUE;
    }

    if (aSeekWindowMS > 0) 
    {
        this->seekWindowMs = aSeekWindowMS;
        bAutoSeekSetting = FALSE;
    } 
    else if (aSeekWindowMS == 0) 
    {
        // if zero, use automatic setting
        bAutoSeekSetting = TRUE;
    }

    calcSeqParameters();

    calculateOverlapLength(overlapMs);

    // set tempo to recalculate 'sampleReq'
    setTempo(tempo);

}



/// Get routine control parameters, see setParameters() function.
/// Any of the parameters to this function can be NULL, in such case corresponding parameter
/// value isn't returned.
void TDStretch::getParameters(int *pSampleRate, int *pSequenceMs, int *pSeekWindowMs, int *pOverlapMs) const
{
    if (pSampleRate)
    {
        *pSampleRate = sampleRate;
    }

    if (pSequenceMs)
    {
        *pSequenceMs = (bAutoSeqSetting) ? (USE_AUTO_SEQUENCE_LEN) : sequenceMs;
    }

    if (pSeekWindowMs)
    {
        *pSeekWindowMs = (bAutoSeekSetting) ? (USE_AUTO_SEEKWINDOW_LEN) : seekWindowMs;
    }

    if (pOverlapMs)
    {
        *pOverlapMs = overlapMs;
    }
}


// Overlaps samples in 'midBuffer' with the samples in 'pInput'
void TDStretch::overlapMono(SAMPLETYPE *pOutput, const SAMPLETYPE *pInput) const
{
    int i;
    SAMPLETYPE m1, m2;

    m1 = (SAMPLETYPE)0;
    m2 = (SAMPLETYPE)overlapLength;

    for (i = 0; i < overlapLength ; i ++) 
    {
        pOutput[i] = (pInput[i] * m1 + pMidBuffer[i] * m2 ) / overlapLength;
        m1 += 1;
        m2 -= 1;
    }
}



void TDStretch::clearMidBuffer()
{
    memset(pMidBuffer, 0, 2 * sizeof(SAMPLETYPE) * overlapLength);
}


void TDStretch::clearInput()
{
    inputBuffer.clear();
    clearMidBuffer();
}


// Clears the sample buffers
void TDStretch::clear()
{
    outputBuffer.clear();
    clearInput();
}



// Enables/disables the quick position seeking algorithm. Zero to disable, nonzero
// to enable
void TDStretch::enableQuickSeek(BOOL enable)
{
    bQuickSeek = enable;
}


// Returns nonzero if the quick seeking algorithm is enabled.
BOOL TDStretch::isQuickSeekEnabled() const
{
    return bQuickSeek;
}


// Seeks for the optimal overlap-mixing position.
int TDStretch::seekBestOverlapPosition(const SAMPLETYPE *refPos)
{
    if (bQuickSeek) 
    {
        return seekBestOverlapPositionQuick(refPos);
    } 
    else 
    {
        return seekBestOverlapPositionFull(refPos);
    }
}


// Overlaps samples in 'midBuffer' with the samples in 'pInputBuffer' at position
// of 'ovlPos'.
inline void TDStretch::overlap(SAMPLETYPE *pOutput, const SAMPLETYPE *pInput, uint ovlPos) const
{
    if (channels == 2) 
    {
        // stereo sound
        overlapStereo(pOutput, pInput + 2 * ovlPos);
    } else {
        // mono sound.
        overlapMono(pOutput, pInput + ovlPos);
    }
}



// Seeks for the optimal overlap-mixing position. The 'stereo' version of the
// routine
//
// The best position is determined as the position where the two overlapped
// sample sequences are 'most alike', in terms of the highest cross-correlation
// value over the overlapping period
int TDStretch::seekBestOverlapPositionFull(const SAMPLETYPE *refPos) 
{
    int bestOffs;
    double bestCorr, corr;
    int i;

    bestCorr = FLT_MIN;
    bestOffs = 0;

    // Scans for the best correlation value by testing each possible position
    // over the permitted range.
    for (i = 0; i < seekLength; i ++) 
    {
        // Calculates correlation value for the mixing position corresponding
        // to 'i'
        corr = calcCrossCorr(refPos + channels * i, pMidBuffer);
        // heuristic rule to slightly favour values close to mid of the range
        double tmp = (double)(2 * i - seekLength) / (double)seekLength;
        corr = ((corr + 0.1) * (1.0 - 0.25 * tmp * tmp));

        // Checks for the highest correlation value
        if (corr > bestCorr) 
        {
            bestCorr = corr;
            bestOffs = i;
        }
    }
    // clear cross correlation routine state if necessary (is so e.g. in MMX routines).
    clearCrossCorrState();

    return bestOffs;
}


// Seeks for the optimal overlap-mixing position. The 'stereo' version of the
// routine
//
// The best position is determined as the position where the two overlapped
// sample sequences are 'most alike', in terms of the highest cross-correlation
// value over the overlapping period
int TDStretch::seekBestOverlapPositionQuick(const SAMPLETYPE *refPos) 
{
    int j;
    int bestOffs;
    double bestCorr, corr;
    int scanCount, corrOffset, tempOffset;

    bestCorr = FLT_MIN;
    bestOffs = _scanOffsets[0][0];
    corrOffset = 0;
    tempOffset = 0;

    // Scans for the best correlation value using four-pass hierarchical search.
    //
    // The look-up table 'scans' has hierarchical position adjusting steps.
    // In first pass the routine searhes for the highest correlation with 
    // relatively coarse steps, then rescans the neighbourhood of the highest
    // correlation with better resolution and so on.
    for (scanCount = 0;scanCount < 4; scanCount ++) 
    {
        j = 0;
        while (_scanOffsets[scanCount][j]) 
        {
            tempOffset = corrOffset + _scanOffsets[scanCount][j];
            if (tempOffset >= seekLength) break;

            // Calculates correlation value for the mixing position corresponding
            // to 'tempOffset'
            corr = (double)calcCrossCorr(refPos + channels * tempOffset, pMidBuffer);
            // heuristic rule to slightly favour values close to mid of the range
            double tmp = (double)(2 * tempOffset - seekLength) / seekLength;
            corr = ((corr + 0.1) * (1.0 - 0.25 * tmp * tmp));

            // Checks for the highest correlation value
            if (corr > bestCorr) 
            {
                bestCorr = corr;
                bestOffs = tempOffset;
            }
            j ++;
        }
        corrOffset = bestOffs;
    }
    // clear cross correlation routine state if necessary (is so e.g. in MMX routines).
    clearCrossCorrState();

    return bestOffs;
}



/// clear cross correlation routine state if necessary 
void TDStretch::clearCrossCorrState()
{
    // default implementation is empty.
}


/// Calculates processing sequence length according to tempo setting
void TDStretch::calcSeqParameters()
{
    // Adjust tempo param according to tempo, so that variating processing sequence length is used
    // at varius tempo settings, between the given low...top limits
    #define AUTOSEQ_TEMPO_LOW   0.5     // auto setting low tempo range (-50%)
    #define AUTOSEQ_TEMPO_TOP   2.0     // auto setting top tempo range (+100%)

    // sequence-ms setting values at above low & top tempo
    #define AUTOSEQ_AT_MIN      125.0
    #define AUTOSEQ_AT_MAX      50.0
    #define AUTOSEQ_K           ((AUTOSEQ_AT_MAX - AUTOSEQ_AT_MIN) / (AUTOSEQ_TEMPO_TOP - AUTOSEQ_TEMPO_LOW))
    #define AUTOSEQ_C           (AUTOSEQ_AT_MIN - (AUTOSEQ_K) * (AUTOSEQ_TEMPO_LOW))

    // seek-window-ms setting values at above low & top tempo
    #define AUTOSEEK_AT_MIN     25.0
    #define AUTOSEEK_AT_MAX     15.0
    #define AUTOSEEK_K          ((AUTOSEEK_AT_MAX - AUTOSEEK_AT_MIN) / (AUTOSEQ_TEMPO_TOP - AUTOSEQ_TEMPO_LOW))
    #define AUTOSEEK_C          (AUTOSEEK_AT_MIN - (AUTOSEEK_K) * (AUTOSEQ_TEMPO_LOW))

    #define CHECK_LIMITS(x, mi, ma) (((x) < (mi)) ? (mi) : (((x) > (ma)) ? (ma) : (x)))

    double seq, seek;
    
    if (bAutoSeqSetting)
    {
        seq = AUTOSEQ_C + AUTOSEQ_K * tempo;
        seq = CHECK_LIMITS(seq, AUTOSEQ_AT_MAX, AUTOSEQ_AT_MIN);
        sequenceMs = (int)(seq + 0.5);
    }

    if (bAutoSeekSetting)
    {
        seek = AUTOSEEK_C + AUTOSEEK_K * tempo;
        seek = CHECK_LIMITS(seek, AUTOSEEK_AT_MAX, AUTOSEEK_AT_MIN);
        seekWindowMs = (int)(seek + 0.5);
    }

    // Update seek window lengths
    seekWindowLength = (sampleRate * sequenceMs) / 1000;
    if (seekWindowLength < 2 * overlapLength) 
    {
        seekWindowLength = 2 * overlapLength;
    }
    seekLength = (sampleRate * seekWindowMs) / 1000;
}



// Sets new target tempo. Normal tempo = 'SCALE', smaller values represent slower 
// tempo, larger faster tempo.
void TDStretch::setTempo(float newTempo)
{
    int intskip;

    tempo = newTempo;

    // Calculate new sequence duration
    calcSeqParameters();

    // Calculate ideal skip length (according to tempo value) 
    nominalSkip = tempo * (seekWindowLength - overlapLength);
    intskip = (int)(nominalSkip + 0.5f);

    // Calculate how many samples are needed in the 'inputBuffer' to 
    // process another batch of samples
    //sampleReq = max(intskip + overlapLength, seekWindowLength) + seekLength / 2;
    sampleReq = max(intskip + overlapLength, seekWindowLength) + seekLength;
}



// Sets the number of channels, 1 = mono, 2 = stereo
void TDStretch::setChannels(int numChannels)
{
    assert(numChannels > 0);
    if (channels == numChannels) return;
    assert(numChannels == 1 || numChannels == 2);

    channels = numChannels;
    inputBuffer.setChannels(channels);
    outputBuffer.setChannels(channels);
}


// nominal tempo, no need for processing, just pass the samples through
// to outputBuffer
/*
void TDStretch::processNominalTempo()
{
    assert(tempo == 1.0f);

    if (bMidBufferDirty) 
    {
        // If there are samples in pMidBuffer waiting for overlapping,
        // do a single sliding overlapping with them in order to prevent a 
        // clicking distortion in the output sound
        if (inputBuffer.numSamples() < overlapLength) 
        {
            // wait until we've got overlapLength input samples
            return;
        }
        // Mix the samples in the beginning of 'inputBuffer' with the 
        // samples in 'midBuffer' using sliding overlapping 
        overlap(outputBuffer.ptrEnd(overlapLength), inputBuffer.ptrBegin(), 0);
        outputBuffer.putSamples(overlapLength);
        inputBuffer.receiveSamples(overlapLength);
        clearMidBuffer();
        // now we've caught the nominal sample flow and may switch to
        // bypass mode
    }

    // Simply bypass samples from input to output
    outputBuffer.moveSamples(inputBuffer);
}
*/

#include <stdio.h>

// Processes as many processing frames of the samples 'inputBuffer', store
// the result into 'outputBuffer'
void TDStretch::processSamples()
{
    int ovlSkip, offset;
    int temp;

    /* Removed this small optimization - can introduce a click to sound when tempo setting
       crosses the nominal value
    if (tempo == 1.0f) 
    {
        // tempo not changed from the original, so bypass the processing
        processNominalTempo();
        return;
    }
    */

    // Process samples as long as there are enough samples in 'inputBuffer'
    // to form a processing frame.
    while ((int)inputBuffer.numSamples() >= sampleReq) 
    {
        // If tempo differs from the normal ('SCALE'), scan for the best overlapping
        // position
        offset = seekBestOverlapPosition(inputBuffer.ptrBegin());

        // Mix the samples in the 'inputBuffer' at position of 'offset' with the 
        // samples in 'midBuffer' using sliding overlapping
        // ... first partially overlap with the end of the previous sequence
        // (that's in 'midBuffer')
        overlap(outputBuffer.ptrEnd((uint)overlapLength), inputBuffer.ptrBegin(), (uint)offset);
        outputBuffer.putSamples((uint)overlapLength);

        // ... then copy sequence samples from 'inputBuffer' to output:

        // length of sequence
        temp = (seekWindowLength - 2 * overlapLength);

        // crosscheck that we don't have buffer overflow...
        if ((int)inputBuffer.numSamples() < (offset + temp + overlapLength * 2))
        {
            continue;    // just in case, shouldn't really happen
        }

        outputBuffer.putSamples(inputBuffer.ptrBegin() + channels * (offset + overlapLength), (uint)temp);

        // Copies the end of the current sequence from 'inputBuffer' to 
        // 'midBuffer' for being mixed with the beginning of the next 
        // processing sequence and so on
        assert((offset + temp + overlapLength * 2) <= (int)inputBuffer.numSamples());
        memcpy(pMidBuffer, inputBuffer.ptrBegin() + channels * (offset + temp + overlapLength), 
            channels * sizeof(SAMPLETYPE) * overlapLength);

        // Remove the processed samples from the input buffer. Update
        // the difference between integer & nominal skip step to 'skipFract'
        // in order to prevent the error from accumulating over time.
        skipFract += nominalSkip;   // real skip size
        ovlSkip = (int)skipFract;   // rounded to integer skip
        skipFract -= ovlSkip;       // maintain the fraction part, i.e. real vs. integer skip
        inputBuffer.receiveSamples((uint)ovlSkip);
    }
}


// Adds 'numsamples' pcs of samples from the 'samples' memory position into
// the input of the object.
void TDStretch::putSamples(const SAMPLETYPE *samples, uint nSamples)
{
    // Add the samples into the input buffer
    inputBuffer.putSamples(samples, nSamples);
    // Process the samples in input buffer
    processSamples();
}



/// Set new overlap length parameter & reallocate RefMidBuffer if necessary.
void TDStretch::acceptNewOverlapLength(int newOverlapLength)
{
    int prevOvl;

    assert(newOverlapLength >= 0);
    prevOvl = overlapLength;
    overlapLength = newOverlapLength;

    if (overlapLength > prevOvl)
    {
        delete[] pMidBufferUnaligned;

        pMidBufferUnaligned = new SAMPLETYPE[overlapLength * 2 + 16 / sizeof(SAMPLETYPE)];
        // ensure that 'pMidBuffer' is aligned to 16 byte boundary for efficiency
        pMidBuffer = (SAMPLETYPE *)((((ulong)pMidBufferUnaligned) + 15) & (ulong)-16);

        clearMidBuffer();
    }
}


// Operator 'new' is overloaded so that it automatically creates a suitable instance 
// depending on if we've a MMX/SSE/etc-capable CPU available or not.
void * TDStretch::operator new(size_t s)
{
    // Notice! don't use "new TDStretch" directly, use "newInstance" to create a new instance instead!
    ST_THROW_RT_ERROR("Error in TDStretch::new: Don't use 'new TDStretch' directly, use 'newInstance' member instead!");
    return newInstance();
}


TDStretch * TDStretch::newInstance()
{
    uint uExtensions;

    uExtensions = detectCPUextensions();

    // Check if MMX/SSE instruction set extensions supported by CPU

#ifdef SOUNDTOUCH_ALLOW_MMX
    // MMX routines available only with integer sample types
    if (uExtensions & SUPPORT_MMX)
    {
        return ::new TDStretchMMX;
    }
    else
#endif // SOUNDTOUCH_ALLOW_MMX


#ifdef SOUNDTOUCH_ALLOW_SSE
    if (uExtensions & SUPPORT_SSE)
    {
        // SSE support
        return ::new TDStretchSSE;
    }
    else
#endif // SOUNDTOUCH_ALLOW_SSE

    {
        // ISA optimizations not supported, use plain C version
        return ::new TDStretch;
    }
}


//////////////////////////////////////////////////////////////////////////////
//
// Integer arithmetics specific algorithm implementations.
//
//////////////////////////////////////////////////////////////////////////////

#ifdef SOUNDTOUCH_INTEGER_SAMPLES

// Overlaps samples in 'midBuffer' with the samples in 'input'. The 'Stereo' 
// version of the routine.
void TDStretch::overlapStereo(short *poutput, const short *input) const
{
    int i;
    short temp;
    int cnt2;

    for (i = 0; i < overlapLength ; i ++) 
    {
        temp = (short)(overlapLength - i);
        cnt2 = 2 * i;
        poutput[cnt2] = (input[cnt2] * i + pMidBuffer[cnt2] * temp )  / overlapLength;
        poutput[cnt2 + 1] = (input[cnt2 + 1] * i + pMidBuffer[cnt2 + 1] * temp ) / overlapLength;
    }
}

// Calculates the x having the closest 2^x value for the given value
static int _getClosest2Power(double value)
{
    return (int)(log(value) / log(2.0) + 0.5);
}


/// Calculates overlap period length in samples.
/// Integer version rounds overlap length to closest power of 2
/// for a divide scaling operation.
void TDStretch::calculateOverlapLength(int aoverlapMs)
{
    int newOvl;

    assert(aoverlapMs >= 0);

    // calculate overlap length so that it's power of 2 - thus it's easy to do
    // integer division by right-shifting. Term "-1" at end is to account for 
    // the extra most significatnt bit left unused in result by signed multiplication 
    overlapDividerBits = _getClosest2Power((sampleRate * aoverlapMs) / 1000.0) - 1;
    if (overlapDividerBits > 9) overlapDividerBits = 9;
    if (overlapDividerBits < 3) overlapDividerBits = 3;
    newOvl = (int)pow(2.0, (int)overlapDividerBits + 1);    // +1 => account for -1 above

    acceptNewOverlapLength(newOvl);

    // calculate sloping divider so that crosscorrelation operation won't 
    // overflow 32-bit register. Max. sum of the crosscorrelation sum without 
    // divider would be 2^30*(N^3-N)/3, where N = overlap length
    slopingDivider = (newOvl * newOvl - 1) / 3;
}


double TDStretch::calcCrossCorr(const short *mixingPos, const short *compare) const
{
    long corr;
    long norm;
    int i;

    corr = norm = 0;
    // Same routine for stereo and mono. For stereo, unroll loop for better
    // efficiency and gives slightly better resolution against rounding. 
    // For mono it same routine, just  unrolls loop by factor of 4
    for (i = 0; i < channels * overlapLength; i += 4) 
    {
        corr += (mixingPos[i] * compare[i] + 
                 mixingPos[i + 1] * compare[i + 1] +
                 mixingPos[i + 2] * compare[i + 2] + 
                 mixingPos[i + 3] * compare[i + 3]) >> overlapDividerBits;
        norm += (mixingPos[i] * mixingPos[i] + 
                 mixingPos[i + 1] * mixingPos[i + 1] +
                 mixingPos[i + 2] * mixingPos[i + 2] + 
                 mixingPos[i + 3] * mixingPos[i + 3]) >> overlapDividerBits;
    }

    // Normalize result by dividing by sqrt(norm) - this step is easiest 
    // done using floating point operation
    if (norm == 0) norm = 1;    // to avoid div by zero
    return (double)corr / sqrt((double)norm);
}

#endif // SOUNDTOUCH_INTEGER_SAMPLES

//////////////////////////////////////////////////////////////////////////////
//
// Floating point arithmetics specific algorithm implementations.
//

#ifdef SOUNDTOUCH_FLOAT_SAMPLES

// Overlaps samples in 'midBuffer' with the samples in 'pInput'
void TDStretch::overlapStereo(float *pOutput, const float *pInput) const
{
    int i;
    float fScale;
    float f1;
    float f2;

    fScale = 1.0f / (float)overlapLength;

    f1 = 0;
    f2 = 1.0f;

    for (i = 0; i < 2 * (int)overlapLength ; i += 2) 
    {
        pOutput[i + 0] = pInput[i + 0] * f1 + pMidBuffer[i + 0] * f2;
        pOutput[i + 1] = pInput[i + 1] * f1 + pMidBuffer[i + 1] * f2;

        f1 += fScale;
        f2 -= fScale;
    }
}


/// Calculates overlapInMsec period length in samples.
void TDStretch::calculateOverlapLength(int overlapInMsec)
{
    int newOvl;

    assert(overlapInMsec >= 0);
    newOvl = (sampleRate * overlapInMsec) / 1000;
    if (newOvl < 16) newOvl = 16;

    // must be divisible by 8
    newOvl -= newOvl % 8;

    acceptNewOverlapLength(newOvl);
}


double TDStretch::calcCrossCorr(const float *mixingPos, const float *compare) const
{
    double corr;
    double norm;
    int i;

    corr = norm = 0;
    // Same routine for stereo and mono. For Stereo, unroll by factor of 2.
    // For mono it's same routine yet unrollsd by factor of 4.
    for (i = 0; i < channels * overlapLength; i += 4) 
    {
        corr += mixingPos[i] * compare[i] +
                mixingPos[i + 1] * compare[i + 1];

        norm += mixingPos[i] * mixingPos[i] + 
                mixingPos[i + 1] * mixingPos[i + 1];

        // unroll the loop for better CPU efficiency:
        corr += mixingPos[i + 2] * compare[i + 2] +
                mixingPos[i + 3] * compare[i + 3];

        norm += mixingPos[i + 2] * mixingPos[i + 2] +
                mixingPos[i + 3] * mixingPos[i + 3];
    }

    if (norm < 1e-9) norm = 1.0;    // to avoid div by zero
    return corr / sqrt(norm);
}

#endif // SOUNDTOUCH_FLOAT_SAMPLES
