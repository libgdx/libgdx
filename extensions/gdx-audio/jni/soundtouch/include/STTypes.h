////////////////////////////////////////////////////////////////////////////////
///
/// Common type definitions for SoundTouch audio processing library.
///
/// Author        : Copyright (c) Olli Parviainen
/// Author e-mail : oparviai 'at' iki.fi
/// SoundTouch WWW: http://www.surina.net/soundtouch
///
////////////////////////////////////////////////////////////////////////////////
//
// Last changed  : $Date: 2011-09-02 20:56:11 +0200 (Fr, 02 Sep 2011) $
// File revision : $Revision: 3 $
//
// $Id: STTypes.h 131 2011-09-02 18:56:11Z oparviai $
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

#ifndef STTypes_H
#define STTypes_H

typedef unsigned int    uint;
#ifdef __MINGW64__
typedef unsigned long long   ulong;
#else
typedef unsigned long ulong;
#endif

#ifndef _WINDEF_
    // if these aren't defined already by Windows headers, define now

    typedef int BOOL;

    #define FALSE   0
    #define TRUE    1

#endif  // _WINDEF_


namespace soundtouch
{
    /// Activate these undef's to overrule the possible sampletype 
    /// setting inherited from some other header file:
    #define SOUNDTOUCH_INTEGER_SAMPLES

    /// Always disable optimizations when not using a x86 systems.
    #undef SOUNDTOUCH_ALLOW_X86_OPTIMIZATIONS

    #ifdef SOUNDTOUCH_INTEGER_SAMPLES
        // 16bit integer sample type
        typedef short SAMPLETYPE;
        // data type for sample accumulation: Use 32bit integer to prevent overflows
        typedef long  LONG_SAMPLETYPE;

        #ifdef SOUNDTOUCH_FLOAT_SAMPLES
            // check that only one sample type is defined
            #error "conflicting sample types defined"
        #endif // SOUNDTOUCH_FLOAT_SAMPLES
    #endif  // SOUNDTOUCH_INTEGER_SAMPLES

};

// define ST_NO_EXCEPTION_HANDLING switch to disable throwing std exceptions:
#define ST_NO_EXCEPTION_HANDLING    1
#ifdef ST_NO_EXCEPTION_HANDLING
    // Exceptions disabled. Throw asserts instead if enabled.
    #include <assert.h>
    #define ST_THROW_RT_ERROR(x)    {assert((const char *)x);}
#else
    // use c++ standard exceptions
    #include <stdexcept>
    #define ST_THROW_RT_ERROR(x)    {throw std::runtime_error(x);}
#endif

// When this #define is active, eliminates a clicking sound when the "rate" or "pitch" 
// parameter setting crosses from value <1 to >=1 or vice versa during processing. 
// Default is off as such crossover is untypical case and involves a slight sound 
// quality compromise.
//#define SOUNDTOUCH_PREVENT_CLICK_AT_RATE_CROSSOVER   1

#endif
