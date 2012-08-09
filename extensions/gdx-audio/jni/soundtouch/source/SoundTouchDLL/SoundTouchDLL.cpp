//////////////////////////////////////////////////////////////////////////////
///
/// SoundTouch DLL wrapper - wraps SoundTouch routines into a Dynamic Load 
/// Library interface.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouchDLL.cpp 96 2010-12-12 19:12:12Z oparviai $
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

#include <windows.h>
#include <string.h>
#include "SoundTouchDLL.h"
#include "soundtouch.h"

using namespace soundtouch;

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH:
	case DLL_THREAD_ATTACH:
	case DLL_THREAD_DETACH:
	case DLL_PROCESS_DETACH:
		break;
	}
    return TRUE;
}


//////////////

typedef struct
{
    DWORD dwMagic;
    SoundTouch *pst;
} STHANDLE;

#define STMAGIC 0x1770C001

SOUNDTOUCHDLL_API HANDLE __stdcall soundtouch_createInstance()
{
    STHANDLE *tmp = new STHANDLE;

    if (tmp)
    {
        tmp->dwMagic = STMAGIC;
        tmp->pst = new SoundTouch();
        if (tmp->pst == NULL)
        {
            delete tmp;
            tmp = NULL;
        }
    }
    return (HANDLE)tmp;
}


SOUNDTOUCHDLL_API void __stdcall soundtouch_destroyInstance(HANDLE h)
{
    /*
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;
*/

    /*
    sth->dwMagic = 0;
    delete sth->pst;
    sth->pst = NULL;
    delete sth;
*/
}


/// Get SoundTouch library version string
SOUNDTOUCHDLL_API const char *__stdcall soundtouch_getVersionString()
{
    return SoundTouch::getVersionString();
}


/// Get SoundTouch library version string - alternative function for 
/// environments that can't properly handle character string as return value
SOUNDTOUCHDLL_API void __stdcall soundtouch_getVersionString2(char* versionString, int bufferSize)
{
    strncpy(versionString, SoundTouch::getVersionString(), bufferSize - 1);
    versionString[bufferSize - 1] = 0;
}


/// Get SoundTouch library version Id
SOUNDTOUCHDLL_API uint __stdcall soundtouch_getVersionId()
{
    return SoundTouch::getVersionId();
}

/// Sets new rate control value. Normal rate = 1.0, smaller values
/// represent slower rate, larger faster rates.
SOUNDTOUCHDLL_API void __stdcall soundtouch_setRate(HANDLE h, float newRate)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setRate(newRate);
}


/// Sets new tempo control value. Normal tempo = 1.0, smaller values
/// represent slower tempo, larger faster tempo.
SOUNDTOUCHDLL_API void __stdcall soundtouch_setTempo(HANDLE h, float newTempo)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setTempo(newTempo);
}

/// Sets new rate control value as a difference in percents compared
/// to the original rate (-50 .. +100 %)
SOUNDTOUCHDLL_API void __stdcall soundtouch_setRateChange(HANDLE h, float newRate)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setRateChange(newRate);
}

/// Sets new tempo control value as a difference in percents compared
/// to the original tempo (-50 .. +100 %)
SOUNDTOUCHDLL_API void __stdcall soundtouch_setTempoChange(HANDLE h, float newTempo)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setTempoChange(newTempo);
}

/// Sets new pitch control value. Original pitch = 1.0, smaller values
/// represent lower pitches, larger values higher pitch.
SOUNDTOUCHDLL_API void __stdcall soundtouch_setPitch(HANDLE h, float newPitch)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setPitch(newPitch);
}

/// Sets pitch change in octaves compared to the original pitch  
/// (-1.00 .. +1.00)
SOUNDTOUCHDLL_API void __stdcall soundtouch_setPitchOctaves(HANDLE h, float newPitch)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setPitchOctaves(newPitch);
}

/// Sets pitch change in semi-tones compared to the original pitch
/// (-12 .. +12)
SOUNDTOUCHDLL_API void __stdcall soundtouch_setPitchSemiTones(HANDLE h, float newPitch)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setPitchSemiTones(newPitch);
}


/// Sets the number of channels, 1 = mono, 2 = stereo
SOUNDTOUCHDLL_API void __stdcall soundtouch_setChannels(HANDLE h, uint numChannels)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setChannels(numChannels);
}

/// Sets sample rate.
SOUNDTOUCHDLL_API void __stdcall soundtouch_setSampleRate(HANDLE h, uint srate)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->setSampleRate(srate);
}

/// Flushes the last samples from the processing pipeline to the output.
/// Clears also the internal processing buffers.
//
/// Note: This function is meant for extracting the last samples of a sound
/// stream. This function may introduce additional blank samples in the end
/// of the sound stream, and thus it's not recommended to call this function
/// in the middle of a sound stream.
SOUNDTOUCHDLL_API void __stdcall soundtouch_flush(HANDLE h)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->flush();
}

/// Adds 'numSamples' pcs of samples from the 'samples' memory position into
/// the input of the object. Notice that sample rate _has_to_ be set before
/// calling this function, otherwise throws a runtime_error exception.
SOUNDTOUCHDLL_API void __stdcall soundtouch_putSamples(HANDLE h, 
        const SAMPLETYPE *samples,  ///< Pointer to sample buffer.
        uint numSamples                         ///< Number of samples in buffer. Notice
                                                ///< that in case of stereo-sound a single sample
                                                ///< contains data for both channels.
        )
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->putSamples(samples, numSamples);
}

/// Clears all the samples in the object's output and internal processing
/// buffers.
SOUNDTOUCHDLL_API void __stdcall soundtouch_clear(HANDLE h)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return;

    sth->pst->clear();
}

/// Changes a setting controlling the processing system behaviour. See the
/// 'SETTING_...' defines for available setting ID's.
/// 
/// \return 'TRUE' if the setting was succesfully changed
SOUNDTOUCHDLL_API BOOL __stdcall soundtouch_setSetting(HANDLE h, 
                int settingId,   ///< Setting ID number. see SETTING_... defines.
                int value        ///< New setting value.
                )
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return FALSE;

    return sth->pst->setSetting(settingId, value);
}

/// Reads a setting controlling the processing system behaviour. See the
/// 'SETTING_...' defines for available setting ID's.
///
/// \return the setting value.
SOUNDTOUCHDLL_API int __stdcall soundtouch_getSetting(HANDLE h, 
                          int settingId    ///< Setting ID number, see SETTING_... defines.
                )
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return -1;

    return sth->pst->getSetting(settingId);
}


/// Returns number of samples currently unprocessed.
SOUNDTOUCHDLL_API uint __stdcall soundtouch_numUnprocessedSamples(HANDLE h)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return 0;

    return sth->pst->numUnprocessedSamples();
}


/// Adjusts book-keeping so that given number of samples are removed from beginning of the 
/// sample buffer without copying them anywhere. 
///
/// Used to reduce the number of samples in the buffer when accessing the sample buffer directly
/// with 'ptrBegin' function.
SOUNDTOUCHDLL_API uint __stdcall soundtouch_receiveSamples(HANDLE h, 
                               SAMPLETYPE *outBuffer, ///< Buffer where to copy output samples.
                        uint maxSamples                    ///< How many samples to receive at max.
                        )
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return 0;

    if (outBuffer)
    {
        return sth->pst->receiveSamples(outBuffer, maxSamples);
    }
    else
    {
        return sth->pst->receiveSamples(maxSamples);
    }
}

/// Returns number of samples currently available.
SOUNDTOUCHDLL_API uint __stdcall soundtouch_numSamples(HANDLE h)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return 0;

    return sth->pst->numSamples();
}


/// Returns nonzero if there aren't any samples available for outputting.
SOUNDTOUCHDLL_API int __stdcall soundtouch_isEmpty(HANDLE h)
{
    STHANDLE *sth = (STHANDLE*)h;
    if (sth->dwMagic != STMAGIC) return -1;

    return sth->pst->isEmpty();
}
