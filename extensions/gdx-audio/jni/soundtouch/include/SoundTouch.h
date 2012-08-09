//////////////////////////////////////////////////////////////////////////////
///
/// SoundTouch - main class for tempo/pitch/rate adjusting routines. 
///
/// Notes:
/// - Initialize the SoundTouch object instance by setting up the sound stream 
///   parameters with functions 'setSampleRate' and 'setChannels', then set 
///   desired tempo/pitch/rate settings with the corresponding functions.
///
/// - The SoundTouch class behaves like a first-in-first-out pipeline: The 
///   samples that are to be processed are fed into one of the pipe by calling
///   function 'putSamples', while the ready processed samples can be read 
///   from the other end of the pipeline with function 'receiveSamples'.
/// 
/// - The SoundTouch processing classes require certain sized 'batches' of 
///   samples in order to process the sound. For this reason the classes buffer 
///   incoming samples until there are enough of samples available for 
///   processing, then they carry out the processing step and consequently
///   make the processed samples available for outputting.
/// 
/// - For the above reason, the processing routines introduce a certain 
///   'latency' between the input and output, so that the samples input to
///   SoundTouch may not be immediately available in the output, and neither 
///   the amount of outputtable samples may not immediately be in direct 
///   relationship with the amount of previously input samples.
///
/// - The tempo/pitch/rate control parameters can be altered during processing.
///   Please notice though that they aren't currently protected by semaphores,
///   so in multi-thread application external semaphore protection may be
///   required.
///
/// - This class utilizes classes 'TDStretch' for tempo change (without modifying
///   pitch) and 'RateTransposer' for changing the playback rate (that is, both 
///   tempo and pitch in the same ratio) of the sound. The third available control 
///   'pitch' (change pitch but maintain tempo) is produced by a combination of
///   combining the two other controls.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2012-04-04 20:47:28 +0100 (Mi, 04 Apr 2012) $
// File revision : $Revision: 4 $
//
// $Id: SoundTouch.h 141 2012-04-04 19:47:28Z oparviai $
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

#ifndef SoundTouch_H
#define SoundTouch_H

#include "FIFOSamplePipe.h"
#include "STTypes.h"

namespace soundtouch
{

/// Soundtouch library version string
#define SOUNDTOUCH_VERSION          "1.7.0"

/// SoundTouch library version id
#define SOUNDTOUCH_VERSION_ID       (10700)

//
// Available setting IDs for the 'setSetting' & 'get_setting' functions:

/// Enable/disable anti-alias filter in pitch transposer (0 = disable)
#define SETTING_USE_AA_FILTER       0

/// Pitch transposer anti-alias filter length (8 .. 128 taps, default = 32)
#define SETTING_AA_FILTER_LENGTH    1

/// Enable/disable quick seeking algorithm in tempo changer routine
/// (enabling quick seeking lowers CPU utilization but causes a minor sound
///  quality compromising)
#define SETTING_USE_QUICKSEEK       2

/// Time-stretch algorithm single processing sequence length in milliseconds. This determines 
/// to how long sequences the original sound is chopped in the time-stretch algorithm. 
/// See "STTypes.h" or README for more information.
#define SETTING_SEQUENCE_MS         3

/// Time-stretch algorithm seeking window length in milliseconds for algorithm that finds the 
/// best possible overlapping location. This determines from how wide window the algorithm 
/// may look for an optimal joining location when mixing the sound sequences back together. 
/// See "STTypes.h" or README for more information.
#define SETTING_SEEKWINDOW_MS       4

/// Time-stretch algorithm overlap length in milliseconds. When the chopped sound sequences 
/// are mixed back together, to form a continuous sound stream, this parameter defines over 
/// how long period the two consecutive sequences are let to overlap each other. 
/// See "STTypes.h" or README for more information.
#define SETTING_OVERLAP_MS          5


/// Call "getSetting" with this ID to query nominal average processing sequence
/// size in samples. This value tells approcimate value how many input samples 
/// SoundTouch needs to gather before it does DSP processing run for the sample batch.
///
/// Notices: 
/// - This is read-only parameter, i.e. setSetting ignores this parameter
/// - Returned value is approximate average value, exact processing batch
///   size may wary from time to time
/// - This parameter value is not constant but may change depending on 
///   tempo/pitch/rate/samplerate settings.
#define SETTING_NOMINAL_INPUT_SEQUENCE		6


/// Call "getSetting" with this ID to query nominal average processing output 
/// size in samples. This value tells approcimate value how many output samples 
/// SoundTouch outputs once it does DSP processing run for a batch of input samples.
///	
/// Notices: 
/// - This is read-only parameter, i.e. setSetting ignores this parameter
/// - Returned value is approximate average value, exact processing batch
///   size may wary from time to time
/// - This parameter value is not constant but may change depending on 
///   tempo/pitch/rate/samplerate settings.
#define SETTING_NOMINAL_OUTPUT_SEQUENCE		7

class SoundTouch : public FIFOProcessor
{
private:
    /// Rate transposer class instance
    class RateTransposer *pRateTransposer;

    /// Time-stretch class instance
    class TDStretch *pTDStretch;

    /// Virtual pitch parameter. Effective rate & tempo are calculated from these parameters.
    float virtualRate;

    /// Virtual pitch parameter. Effective rate & tempo are calculated from these parameters.
    float virtualTempo;

    /// Virtual pitch parameter. Effective rate & tempo are calculated from these parameters.
    float virtualPitch;

    /// Flag: Has sample rate been set?
    BOOL  bSrateSet;

    /// Calculates effective rate & tempo valuescfrom 'virtualRate', 'virtualTempo' and 
    /// 'virtualPitch' parameters.
    void calcEffectiveRateAndTempo();

protected :
    /// Number of channels
    uint  channels;

    /// Effective 'rate' value calculated from 'virtualRate', 'virtualTempo' and 'virtualPitch'
    float rate;

    /// Effective 'tempo' value calculated from 'virtualRate', 'virtualTempo' and 'virtualPitch'
    float tempo;

public:
    SoundTouch();
    virtual ~SoundTouch();

    /// Get SoundTouch library version string
    static const char *getVersionString();

    /// Get SoundTouch library version Id
    static uint getVersionId();

    /// Sets new rate control value. Normal rate = 1.0, smaller values
    /// represent slower rate, larger faster rates.
    void setRate(float newRate);

    /// Sets new tempo control value. Normal tempo = 1.0, smaller values
    /// represent slower tempo, larger faster tempo.
    void setTempo(float newTempo);

    /// Sets new rate control value as a difference in percents compared
    /// to the original rate (-50 .. +100 %)
    void setRateChange(float newRate);

    /// Sets new tempo control value as a difference in percents compared
    /// to the original tempo (-50 .. +100 %)
    void setTempoChange(float newTempo);

    /// Sets new pitch control value. Original pitch = 1.0, smaller values
    /// represent lower pitches, larger values higher pitch.
    void setPitch(float newPitch);

    /// Sets pitch change in octaves compared to the original pitch  
    /// (-1.00 .. +1.00)
    void setPitchOctaves(float newPitch);

    /// Sets pitch change in semi-tones compared to the original pitch
    /// (-12 .. +12)
    void setPitchSemiTones(int newPitch);
    void setPitchSemiTones(float newPitch);

    /// Sets the number of channels, 1 = mono, 2 = stereo
    void setChannels(uint numChannels);

    /// Sets sample rate.
    void setSampleRate(uint srate);

    /// Flushes the last samples from the processing pipeline to the output.
    /// Clears also the internal processing buffers.
    //
    /// Note: This function is meant for extracting the last samples of a sound
    /// stream. This function may introduce additional blank samples in the end
    /// of the sound stream, and thus it's not recommended to call this function
    /// in the middle of a sound stream.
    void flush();

    /// Adds 'numSamples' pcs of samples from the 'samples' memory position into
    /// the input of the object. Notice that sample rate _has_to_ be set before
    /// calling this function, otherwise throws a runtime_error exception.
    virtual void putSamples(
            const SAMPLETYPE *samples,  ///< Pointer to sample buffer.
            uint numSamples                         ///< Number of samples in buffer. Notice
                                                    ///< that in case of stereo-sound a single sample
                                                    ///< contains data for both channels.
            );

    /// Clears all the samples in the object's output and internal processing
    /// buffers.
    virtual void clear();

    /// Changes a setting controlling the processing system behaviour. See the
    /// 'SETTING_...' defines for available setting ID's.
    /// 
    /// \return 'TRUE' if the setting was succesfully changed
    BOOL setSetting(int settingId,   ///< Setting ID number. see SETTING_... defines.
                    int value        ///< New setting value.
                    );

    /// Reads a setting controlling the processing system behaviour. See the
    /// 'SETTING_...' defines for available setting ID's.
    ///
    /// \return the setting value.
    int getSetting(int settingId    ///< Setting ID number, see SETTING_... defines.
                   ) const;

    /// Returns number of samples currently unprocessed.
    virtual uint numUnprocessedSamples() const;


    /// Other handy functions that are implemented in the ancestor classes (see
    /// classes 'FIFOProcessor' and 'FIFOSamplePipe')
    ///
    /// - receiveSamples() : Use this function to receive 'ready' processed samples from SoundTouch.
    /// - numSamples()     : Get number of 'ready' samples that can be received with 
    ///                      function 'receiveSamples()'
    /// - isEmpty()        : Returns nonzero if there aren't any 'ready' samples.
    /// - clear()          : Clears all samples from ready/processing buffers.
};

}
#endif
