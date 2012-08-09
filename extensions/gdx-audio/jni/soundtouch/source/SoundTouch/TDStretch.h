////////////////////////////////////////////////////////////////////////////////
/// 
/// Sampled sound tempo changer/time stretch algorithm. Changes the sound tempo 
/// while maintaining the original pitch by using a time domain WSOLA-like method 
/// with several performance-increasing tweaks.
///
/// Note : MMX/SSE optimized functions reside in separate, platform-specific files 
/// 'mmx_optimized.cpp' and 'sse_optimized.cpp'
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
// $Id: TDStretch.h 137 2012-04-01 19:49:30Z oparviai $
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

#ifndef TDStretch_H
#define TDStretch_H

#include <stddef.h>
#include "STTypes.h"
#include "RateTransposer.h"
#include "FIFOSamplePipe.h"

namespace soundtouch
{

/// Default values for sound processing parameters:
/// Notice that the default parameters are tuned for contemporary popular music 
/// processing. For speech processing applications these parameters suit better:
///     #define DEFAULT_SEQUENCE_MS     40
///     #define DEFAULT_SEEKWINDOW_MS   15
///     #define DEFAULT_OVERLAP_MS      8
///

/// Default length of a single processing sequence, in milliseconds. This determines to how 
/// long sequences the original sound is chopped in the time-stretch algorithm.
///
/// The larger this value is, the lesser sequences are used in processing. In principle
/// a bigger value sounds better when slowing down tempo, but worse when increasing tempo
/// and vice versa.
///
/// Increasing this value reduces computational burden & vice versa.
//#define DEFAULT_SEQUENCE_MS         40
#define DEFAULT_SEQUENCE_MS         USE_AUTO_SEQUENCE_LEN

/// Giving this value for the sequence length sets automatic parameter value
/// according to tempo setting (recommended)
#define USE_AUTO_SEQUENCE_LEN       0

/// Seeking window default length in milliseconds for algorithm that finds the best possible 
/// overlapping location. This determines from how wide window the algorithm may look for an 
/// optimal joining location when mixing the sound sequences back together. 
///
/// The bigger this window setting is, the higher the possibility to find a better mixing
/// position will become, but at the same time large values may cause a "drifting" artifact
/// because consequent sequences will be taken at more uneven intervals.
///
/// If there's a disturbing artifact that sounds as if a constant frequency was drifting 
/// around, try reducing this setting.
///
/// Increasing this value increases computational burden & vice versa.
//#define DEFAULT_SEEKWINDOW_MS       15
#define DEFAULT_SEEKWINDOW_MS       USE_AUTO_SEEKWINDOW_LEN

/// Giving this value for the seek window length sets automatic parameter value
/// according to tempo setting (recommended)
#define USE_AUTO_SEEKWINDOW_LEN     0

/// Overlap length in milliseconds. When the chopped sound sequences are mixed back together, 
/// to form a continuous sound stream, this parameter defines over how long period the two 
/// consecutive sequences are let to overlap each other. 
///
/// This shouldn't be that critical parameter. If you reduce the DEFAULT_SEQUENCE_MS setting 
/// by a large amount, you might wish to try a smaller value on this.
///
/// Increasing this value increases computational burden & vice versa.
#define DEFAULT_OVERLAP_MS      8


/// Class that does the time-stretch (tempo change) effect for the processed
/// sound.
class TDStretch : public FIFOProcessor
{
protected:
    int channels;
    int sampleReq;
    float tempo;

    SAMPLETYPE *pMidBuffer;
    SAMPLETYPE *pMidBufferUnaligned;
    int overlapLength;
    int seekLength;
    int seekWindowLength;
    int overlapDividerBits;
    int slopingDivider;
    float nominalSkip;
    float skipFract;
    FIFOSampleBuffer outputBuffer;
    FIFOSampleBuffer inputBuffer;
    BOOL bQuickSeek;

    int sampleRate;
    int sequenceMs;
    int seekWindowMs;
    int overlapMs;
    BOOL bAutoSeqSetting;
    BOOL bAutoSeekSetting;

    void acceptNewOverlapLength(int newOverlapLength);

    virtual void clearCrossCorrState();
    void calculateOverlapLength(int overlapMs);

    virtual double calcCrossCorr(const SAMPLETYPE *mixingPos, const SAMPLETYPE *compare) const;

    virtual int seekBestOverlapPositionFull(const SAMPLETYPE *refPos);
    virtual int seekBestOverlapPositionQuick(const SAMPLETYPE *refPos);
    int seekBestOverlapPosition(const SAMPLETYPE *refPos);

    virtual void overlapStereo(SAMPLETYPE *output, const SAMPLETYPE *input) const;
    virtual void overlapMono(SAMPLETYPE *output, const SAMPLETYPE *input) const;

    void clearMidBuffer();
    void overlap(SAMPLETYPE *output, const SAMPLETYPE *input, uint ovlPos) const;

    void calcSeqParameters();

    /// Changes the tempo of the given sound samples.
    /// Returns amount of samples returned in the "output" buffer.
    /// The maximum amount of samples that can be returned at a time is set by
    /// the 'set_returnBuffer_size' function.
    void processSamples();
    
public:
    TDStretch();
    virtual ~TDStretch();

    /// Operator 'new' is overloaded so that it automatically creates a suitable instance 
    /// depending on if we've a MMX/SSE/etc-capable CPU available or not.
    static void *operator new(size_t s);

    /// Use this function instead of "new" operator to create a new instance of this class. 
    /// This function automatically chooses a correct feature set depending on if the CPU
    /// supports MMX/SSE/etc extensions.
    static TDStretch *newInstance();
    
    /// Returns the output buffer object
    FIFOSamplePipe *getOutput() { return &outputBuffer; };

    /// Returns the input buffer object
    FIFOSamplePipe *getInput() { return &inputBuffer; };

    /// Sets new target tempo. Normal tempo = 'SCALE', smaller values represent slower 
    /// tempo, larger faster tempo.
    void setTempo(float newTempo);

    /// Returns nonzero if there aren't any samples available for outputting.
    virtual void clear();

    /// Clears the input buffer
    void clearInput();

    /// Sets the number of channels, 1 = mono, 2 = stereo
    void setChannels(int numChannels);

    /// Enables/disables the quick position seeking algorithm. Zero to disable, 
    /// nonzero to enable
    void enableQuickSeek(BOOL enable);

    /// Returns nonzero if the quick seeking algorithm is enabled.
    BOOL isQuickSeekEnabled() const;

    /// Sets routine control parameters. These control are certain time constants
    /// defining how the sound is stretched to the desired duration.
    //
    /// 'sampleRate' = sample rate of the sound
    /// 'sequenceMS' = one processing sequence length in milliseconds
    /// 'seekwindowMS' = seeking window length for scanning the best overlapping 
    ///      position
    /// 'overlapMS' = overlapping length
    void setParameters(int sampleRate,          ///< Samplerate of sound being processed (Hz)
                       int sequenceMS = -1,     ///< Single processing sequence length (ms)
                       int seekwindowMS = -1,   ///< Offset seeking window length (ms)
                       int overlapMS = -1       ///< Sequence overlapping length (ms)
                       );

    /// Get routine control parameters, see setParameters() function.
    /// Any of the parameters to this function can be NULL, in such case corresponding parameter
    /// value isn't returned.
    void getParameters(int *pSampleRate, int *pSequenceMs, int *pSeekWindowMs, int *pOverlapMs) const;

    /// Adds 'numsamples' pcs of samples from the 'samples' memory position into
    /// the input of the object.
    virtual void putSamples(
            const SAMPLETYPE *samples,  ///< Input sample data
            uint numSamples                         ///< Number of samples in 'samples' so that one sample
                                                    ///< contains both channels if stereo
            );

    /// return nominal input sample requirement for triggering a processing batch
    int getInputSampleReq() const
    {
        return (int)(nominalSkip + 0.5);
    }

    /// return nominal output sample amount when running a processing batch
    int getOutputBatchSize() const
    {
        return seekWindowLength - overlapLength;
    }
};



// Implementation-specific class declarations:

#ifdef SOUNDTOUCH_ALLOW_MMX
    /// Class that implements MMX optimized routines for 16bit integer samples type.
    class TDStretchMMX : public TDStretch
    {
    protected:
        double calcCrossCorr(const short *mixingPos, const short *compare) const;
        virtual void overlapStereo(short *output, const short *input) const;
        virtual void clearCrossCorrState();
    };
#endif /// SOUNDTOUCH_ALLOW_MMX


#ifdef SOUNDTOUCH_ALLOW_SSE
    /// Class that implements SSE optimized routines for floating point samples type.
    class TDStretchSSE : public TDStretch
    {
    protected:
        double calcCrossCorr(const float *mixingPos, const float *compare) const;
    };

#endif /// SOUNDTOUCH_ALLOW_SSE

}
#endif  /// TDStretch_H
